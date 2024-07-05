package csi.server.ws.filemanager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.CsiSecurityManager;

/**
 *  Servlet responsible for locating and processing a file
 *
 *  @version   1.0
 *  @author    Mitch Shue
 */
@SuppressWarnings("serial")
public class FileProcessor extends AbstractHandler {
   private static final Logger LOG = LogManager.getLogger(FileProcessor.class);

   static final String HANDLER_ID = "FileProcessor";
    static String toplevel;
    static StringEncrypter _encrypter;
    static String integrationsvc;
    @SuppressWarnings("unchecked")
    static Class fileparser;

    // constants known by integration layer
    static final String COUNT = "Count";
    static final String CHARTTYPE = "chartIndex";
    static final String RGLAYOUT = "layout";
    static final String TLLABELS = "labels";
    static final String DIMENSION = "col";
    static final String VIEW = "tab";
    static final String DVNAME = "name";
    static final String DVID = "id";
    static final String PARAM = "param";
    static final String PARAMCOUNT = PARAM + COUNT;
    static final String FIELDNAME = "cell";
    static final String FUNCTION = "func";

    static final String PROC = "proc";
    static final String PROCCOUNT = PROC + COUNT;
    static final String PROCPARAM = "Param";
    static final String PROCPARAMCOUNT = PROCPARAM + COUNT;

    static final String USERID = "userID";

    static String[] agentroles;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        _encrypter = new StringEncrypter(FileManager.getEncryptionPhrase());
        toplevel = FileManager.getTopLevelFolderName();

        // Get path to integration service. This is the service that handles
        // integration tasks.
        String is = config.getInitParameter("IntegrationService");
        if (null == is) {
            integrationsvc = "/services/integration";
        } else {
            integrationsvc = is;
        }
        if (!integrationsvc.startsWith("/")) {
            integrationsvc = "/" + integrationsvc;
        }

        // Get Class name of file parser. Must be an instance of IFileParser.
        String fp = config.getInitParameter("FileParserClass");
        if (null == fp) {
            fp = "csi.server.ws.filemanager.FileParser";
        }
        try {
            fileparser = Class.forName(fp);
        } catch (ClassNotFoundException cnf) {
            debug(HANDLER_ID, "File parser class " + fp + " not found");
        }

        // get list of acceptable roles/groups that can process
        // files on behalf of other users.
        String roles = config.getInitParameter("AgentRoles");
        if (null == roles) {
            roles = "administrators";
        }
        agentroles = roles.split(",");
    }

    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, int httpmethod) throws ServletException, IOException {
        debug(HANDLER_ID, "processRequest entered");

        // decrypt the token to get the file path
        String token = request.getParameter("token");

        File subject = null;

        if (null != token) {
            debug(HANDLER_ID, "received token: " + token);
            subject = getFileFromToken(token);
            debug(HANDLER_ID, "path = " + subject);
        }

        if ((null == subject) || (!subject.exists())) {
            returnError(request, response, "7404", HANDLER_ID + " cannot locate file to process; stale, invalid, or no token provided");
            return;
        }

        HashMap<String, Object> authchk = new HashMap<String, Object>();
        getRequesterInfo(request, subject, authchk);
        boolean authorized = checkAuthorization(authchk);

        // end this quickly if authorization checks fail.
        if (!authorized) {
            LOG.info("User " + authchk.get(FileManager.$AGENT) + " attempted to execute file " + subject.getPath() + "; not authorized");
            debug(HANDLER_ID, "Authorization failed, access denied");
            returnError(request, response, "7403", HANDLER_ID + " cannot complete requested operation, access denied");
            return;
        } else {
            LOG.info("User " + authchk.get(FileManager.$AGENT) + " executed file " + subject.getPath());
            debug(HANDLER_ID, "Authorization successful");
        }

        // Get a hold of a FileParser object which will read in the file, parse it, and
        // populate the request attributes for the integration servlet.
        IFileParser ifp = null;
        try {
            ifp = (IFileParser) fileparser.newInstance();
        } catch (Exception ix) {
            returnError(request, response, "7501", HANDLER_ID + " cannot load file parser for this file");
            return;
        }

        // Go parse the file and get a hashmap with all the values in it. easier for
        // us to work with
//        @SuppressWarnings("unused")
//        HashMap pmap = null;
        try {
            /*pmap = */ifp.parse(subject);
        } catch (Exception px) {
            String eMsg = HANDLER_ID + " encountered errors parsing service request document";
            LOG.info(eMsg, px);
            returnError(request, response, "7400", eMsg);
            return;
        }

        //
        // Extract values to pass to the integration service
        //

        // Get the view type. Should be an integer if the parser did its job correctly
        String vtype$ = ifp.getViewType();
        int vtype;
        try {
            vtype = Integer.parseInt(vtype$);
        } catch (NumberFormatException nfx) {
            returnError(request, response, "7400", HANDLER_ID + " encountered unknown view type in service request document");
            return;
        }

        // Set values related to context
        String idtype = ifp.getUserIDType();
        String userid = ifp.getUserID();
        request.setAttribute(USERID, idtype + "," + userid);
        debug(HANDLER_ID, USERID + "=" + idtype + "," + userid);

        // Set values not specific to a particular view type
        String dvnm = ifp.getDataViewName();
        String dvid = ifp.getDataViewID();

        // if both dataview name and id are specified, use the dataview id
        if ((null != dvnm) && (null != dvid)) {
            request.setAttribute(DVID, dvid);
            debug(HANDLER_ID, "Dataview " + DVID + "=" + dvid);
        }
        // if dataview id is specified, use it
        else if (null != dvid) {
            request.setAttribute(DVID, dvid);
            debug(HANDLER_ID, "Dataview " + DVID + "=" + dvid);
        }
        // otherwise, use what is hopefully a dataview name. if not specified
        // this will get caught downstream
        else {
            request.setAttribute(DVNAME, dvnm);
            debug(HANDLER_ID, "Dataview " + DVNAME + "=" + dvnm);
        }

        request.setAttribute(VIEW, vtype$);
        debug(HANDLER_ID, VIEW + "=" + vtype$);

        try {
            setInputParameters(ifp, request); // Set dataview query parameters
            setProcParameters(ifp, request); // Set procedure parameters
        } catch (Exception ex) {
            returnError(request, response, "7400", HANDLER_ID + " encountered unexpected errors in service request document");
            return;
        }

        // Set view specific parameters
        switch (vtype) {
        case FileParser.VIEW_TABLE:
            break;
        case FileParser.VIEW_RELGR:
            request.setAttribute(RGLAYOUT, ifp.getRGLayout());
            debug(HANDLER_ID, RGLAYOUT + "=" + ifp.getRGLayout());
            break;
        case FileParser.VIEW_CHART:
            request.setAttribute(CHARTTYPE, ifp.getChartType());
            debug(HANDLER_ID, CHARTTYPE + "=" + ifp.getChartType());
            // pick up chart dimensions
            String[] dimensions = null;
            try {
                dimensions = ifp.getChartDimensions();
            } catch (Exception x) {
                returnError(request, response, "7400", HANDLER_ID + " encountered invalid chart dimension in service request document");
                return;
            }
            if (null != dimensions) {
                for (int i = 0; i < dimensions.length; i++) {
                    request.setAttribute(DIMENSION + (i + 1), dimensions[i]);
                    debug(HANDLER_ID, DIMENSION + (i + 1) + "=" + dimensions[i]);
                }
            }

            // pick up cell to aggregate and function to apply
            String[] aggregatinginfo = ifp.getAggregatingInfo();
            if (null != aggregatinginfo) {
                request.setAttribute(FIELDNAME, aggregatinginfo[0]);
                request.setAttribute(FUNCTION, aggregatinginfo[1]);
            }

            break;
        case FileParser.VIEW_TIMEL:
            String labels = ifp.getTLLabelSwitch();
            String val = "false";
            if (null != labels) {
                val = (labels.equalsIgnoreCase("yes") || labels.equalsIgnoreCase("true") || labels.equalsIgnoreCase("on")) ? "1" : "0";
            }
            request.setAttribute(TLLABELS, val);
            debug(HANDLER_ID, TLLABELS + "=" + val);
            break;
        case FileParser.VIEW_GEOSP:
            break;
        default:
            break;
        }

        RequestDispatcher dsp = getServletContext().getRequestDispatcher(integrationsvc);
        dsp.forward(request, response);

        return;
    }

    private void setInputParameters(IFileParser ifp, HttpServletRequest request) throws Exception {
        String[] qparms = ifp.getInputParameters();
        if (null == qparms) {
         return;
      }

        // set parameter count for this dataview
        request.setAttribute(PARAMCOUNT, Integer.toString(qparms.length));
        debug(HANDLER_ID, PARAMCOUNT + "=" + qparms.length);
        // set parameters for this dataview
        for (int i = 0; i < qparms.length; i++) {
            request.setAttribute(PARAM + (i + 1), qparms[i]);
            debug(HANDLER_ID, PARAM + (i + 1) + "=" + qparms[i]);
        }
    }

    private void setProcParameters(IFileParser ifp, HttpServletRequest request) throws Exception {
        String[] procnames = ifp.getProcNames();
        if (null == procnames) {
         return;
      }

        int pcount = procnames.length;
        request.setAttribute(PROCCOUNT, Integer.toString(pcount));
        debug(HANDLER_ID, PROCCOUNT + "=" + pcount);

        String key;
        String[] pp;

        for (int i = 0; i < procnames.length; i++) {
            key = PROC + (i + 1);
            // set proc name
            request.setAttribute(key, procnames[i]);
            debug(HANDLER_ID, key + "=" + procnames[i]);
            // go fetch proc parameters
            pp = ifp.getProcParameters(procnames[i]);
            if (null == pp)
             {
               break; // proc has no parameters
            }

            // set parameter count for this proc
            String procParamCountKey = key + PROCPARAMCOUNT;
            LOG.debug(String.format("Key for proc param cnt: '%s'", procParamCountKey));
            request.setAttribute(procParamCountKey, Integer.toString(pp.length));
            debug(HANDLER_ID, procParamCountKey + "=" + pp.length);
            // set parameters for this proc
            for (int j = 0; j < pp.length; j++) {
                request.setAttribute(key + PROCPARAM + (j + 1), pp[j]);
                debug(HANDLER_ID, key + PROCPARAM + (j + 1) + "=" + pp[j]);
            }
        }
    }

    public static String decodeToken(String token) {
        debug(HANDLER_ID, "decodeToken entered");
        String decodedtoken = _encrypter.decrypt(token);
        return decodedtoken;
    }

    public static File getFileFromToken(String token) {
        debug(HANDLER_ID, "getFileFromToken entered");
        File fileobj = null;
        // TODO: remove replaceAll with correct unmarshalling
        token = token.replace(" ", "+");
        String path = decodeToken(token);
        debug(HANDLER_ID, "decodeToken returned: " + path);
        if (null != path) {
            path = normalizePath(path);
            fileobj = new File(path);
        }
        return fileobj;
    }

    protected void getRequesterInfo(HttpServletRequest request, File subject, HashMap<String, Object> authchk) {
        // split barfs on \ so we'll do a workaround for now.
        // String[] wrk = subject.getPath().split(File.separator);
        String tmp = subject.getPath();
        tmp = tmp.replace("\\", "/");
        String[] wrk = tmp.split("/");

        String username = null;

        debug(HANDLER_ID, "getRequesterInfo toplevel=" + wrk[0] + " split=" + wrk.length);

        if ((wrk.length >= 2) && (wrk[0].equals(toplevel))) {
            username = wrk[1]; // wrk[0]=toplevel, wrk[1]=username
        }
        // if the subject does not represent a file in the sandbox, require admin
        // privileges to be checked downstream. do this by specifying an arbitrary or
        // non-existent username in the return material
        else {
            username = "$y$t3m";
        }

        String agentname = request.getParameter(FileManager.$AGENT);
        String pwhash = request.getParameter(FileManager.$PW);
        String prname = CsiSecurityManager.getUserName();

        authchk.put(FileManager.$USER, username);
        authchk.put(FileManager.$AGENT, agentname);
        authchk.put(FileManager.$PW, pwhash);
        authchk.put("principalname", prname);
        authchk.put("request", request);

        debug(HANDLER_ID, "RequesterInfo: user=" + username + ", agent=" + agentname + ", pwhash=" + pwhash + ", principal=" + prname);

        return;
    }

    protected boolean checkAuthorization(HashMap<String, Object> authchk) {
        debug(HANDLER_ID, "Checking authorization ...");
        boolean authorized = false;

        //
        // If there is a Principal name, we can assume that all auth/auth
        // has already occurred. If the Principal name is present, it is
        // used to identify the user's folder in the user file space. If
        // a user parameter is also specified and it does not match the
        // Principal name, we have to check that the Principal is allowed
        // to act on behalf of the user specified in the user parameter and
        // reject the request if the Principal is not authorized.
        //
        String username = (String) authchk.get(FileManager.$USER);
        String agentname = (String) authchk.get(FileManager.$AGENT);
        String pwhash = (String) authchk.get(FileManager.$PW);
        String prname = (String) authchk.get("principalname");
        if (null != prname) {
            // user can do things in his own user folder
            if ((null == username) || (prname.equals(username))) {
                authorized = true;
                username = prname;
            }
            // otherwise check to see if principal is allowed to do things
            // in the named user's folder
            else {
                authorized = CsiSecurityManager.hasAnyRole(agentroles);
            }
        }
        //
        // If there is no Principal name, it means that this servlet has
        // been invoked by way of an unprotected URL. This means that we
        // will examine the invoking parameters for user identification and
        // password credentials.
        //
        else {
            authorized = CsiSecurityManager.userHasAnyRole(agentname, pwhash, agentroles);
            if ((null == username) && (null != agentname)) {
               username = agentname;
            }
        }
        authchk.put(FileManager.$USER, username);
        if (null == agentname) {
         authchk.put(FileManager.$AGENT, prname);
      }

        if (authorized) {
            debug(HANDLER_ID, "User " + prname + " is GRANTED access to user " + username + "'s files");
        } else {
            debug(HANDLER_ID, "User " + prname + " is DENIED access to user " + username + "'s files");
        }

        return authorized;
    }
}
