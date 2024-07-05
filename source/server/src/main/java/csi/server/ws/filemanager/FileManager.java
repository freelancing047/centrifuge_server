package csi.server.ws.filemanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.security.CsiSecurityManager;
import csi.security.queries.Users;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.identity.User;
import csi.server.dao.CsiPersistenceManager;
import csi.startup.Product;

/**
 *  Servlet responsible for handling generic file management including
 *  file upload, rename, delete, etc.
 *
 *  @version   1.0
 *  @author    Mitch Shue
 */
@SuppressWarnings("serial")
public class FileManager extends AbstractHandler {
   private static final Logger LOG = LogManager.getLogger(FileManager.class);

   private static final Pattern REQUEST_PATTERN = Pattern.compile("the request");
   private static final Pattern INVALID_FILE_CHARS_PATTERN = Pattern.compile("[\\/|*?:<>%\"]");

    static final String HANDLER_ID = "FileManager";
    static final String ENCRYPTION_PHRASE = "C3ntr1fug353rv3r";

    static String return_filelist;
    static String return_fileinfo;
    static String toplevel;
    static boolean encrypttoken;
    static StringEncrypter _encrypter;
    static String encryptionphrase;
    static String retrievalsvc;
    static String processingsvc;

    // If inbound file is bigger than the threshold value, write it to temp
    // directory first instead of keeping it all in memory.
    static String TempThreshold;
    static int IntTempThreshold;
    static String TempDirectory;

    // Maximum file size allowed
    static String MaxFileSize;
    static long LongMaxFileSize;

    // key names
    public static final String $USER = "user";
    public static final String $AGENT = "agent";
    public static final String $PW = "password";
    public static final String $RENAME = "rename";
    public static final String $FILENAME = "filename";
    public static final String $PATH = "path";
    public static final String $FILE = "file";
    public static final String $OVERWRITE = "overwrite";
    public static final String $TYPE = "type";

    public static final String DEFAULT_FILENAME = "FilenameNotProvided";

    static final String $FILEITEMS = "__fileitems";

    static boolean autoregister = false;
    static String[] agentroles;

    static final String[] MEANS_TRUE = { "y", "yes", "1", "on", "true" };

    @Override
   public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // Identify name of top level folder in the system to contain
        // all user files. Typically, the hierarchy will be:
        // [toplevel]/[username]/[user files and folders]
        toplevel = config.getInitParameter("TopLevelFolder");
        if (null == toplevel) {
            toplevel = "userfiles";
        }

        // Determine if information to be returned to caller on upload
        // or rename is path information in the clear or DES encrypted
        String tmp = config.getInitParameter("ShouldEncryptToken");
        if (null == tmp) {
            encrypttoken = true;
        } else {
            encrypttoken = (tmp.equalsIgnoreCase("yes") || tmp.equalsIgnoreCase("true"));
        }
        if (encrypttoken) {
            encryptionphrase = config.getInitParameter("EncryptionPhrase");
            if (null == encryptionphrase) {
                encryptionphrase = ENCRYPTION_PHRASE;
            }
            _encrypter = new StringEncrypter(encryptionphrase);
        }

        // Get path to file retrieval service. This is a service that will accept
        // the token we returned for a file upload/rename, locate the file identified
        // by the token, and return the contents of the file.
        String rs = config.getInitParameter("RetrievalService");
        if (null == rs) {
            retrievalsvc = "/services/getfile";
        } else {
            retrievalsvc = rs;
        }
        if (!retrievalsvc.startsWith("/")) {
            retrievalsvc = "/" + retrievalsvc;
        }

        // Get path to file processing service. This is a service that will accept
        // the token we returned for a file upload/rename and do something with the
        // file.
        String ps = config.getInitParameter("ProcessingService");
        if (null == ps) {
            processingsvc = "/services/processfile";
        } else {
            processingsvc = ps;
        }
        if (!processingsvc.startsWith("/")) {
            processingsvc = "/" + processingsvc;
        }

        // Read servlet init parameters and set defaults if missing.
        TempThreshold = config.getInitParameter("TempThreshold");
        TempDirectory = config.getInitParameter("TempDirectory");
        MaxFileSize = config.getInitParameter("MaxFileSize");

        if (null == TempDirectory) {
            TempDirectory = "temp";
        }
        if (null == TempThreshold) {
            TempThreshold = "1048576"; // 1MB
        }
        if (null == MaxFileSize) {
            MaxFileSize = "1073741824"; // 1024*1MB
        }

        // Convert number strings to numbers for later use
        try {
            IntTempThreshold = Integer.parseInt(TempThreshold);
            LongMaxFileSize = Long.parseLong(MaxFileSize);
        }
        // If any exceptions, just set defaults
        catch (NumberFormatException nfx) {
            IntTempThreshold = 1048576; // 1MB
            LongMaxFileSize = 134217728; // 128MB
        }

        LOG.debug("File Upload MaxFileSize: " + LongMaxFileSize);

        // Identify JSP/servlet to handle return information
        return_filelist = config.getInitParameter("ReturnFileList");
        if (null == return_filelist) {
            return_filelist = "/filemanager/FileList.jsp";
        }

        return_fileinfo = config.getInitParameter("ReturnFileInfo");
        if (null == return_fileinfo) {
            return_fileinfo = "/filemanager/FileInfo.jsp";
        }

        // see if we should auto register unknown users
        String wrk = config.getInitParameter("AutoRegisterUnknownUser");
        if (null == wrk) {
            wrk = "false";
        }
        autoregister = wrk.equalsIgnoreCase("true");

        // get list of acceptable roles/groups that can upload/manage
        // files on behalf of other users.
        String roles = config.getInitParameter("AgentRoles");
        if (null == roles) {
            roles = "administrators";
        }
        agentroles = roles.split(",");
    }

    //
    // The servlet gets control here and uses the HTTP method ID
    // to figure out what do with the request. Since this is a
    // RESTful implementation, GET is not destructive, POST and
    // DELETE are. PUT is not used.
    //
    // The request URI specifies a resource (i.e. a file or
    // directory) to operate on. GET is used to retrieve information
    // about that resource. POST is used to upload a file or to
    // rename a file or directory identified by the request URI.
    //
    @Override
   @SuppressWarnings( { "deprecation", "unchecked" })
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, int httpmethod) throws ServletException, IOException {
        debug(HANDLER_ID, "processRequest entered");

        boolean authorized = false;

        String username = request.getParameter($USER);
        String agentname = request.getParameter($AGENT);
        String pwhash = request.getParameter($PW);


        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        // if multipart content, we need to look at all the form fields to see
        // if we need any of that information.
        HashMap<String, Object> mpinfo = null;
        DiskFileItemFactory dfifactory;
        ServletFileUpload upload;
        List<FileItem> /* FileItem */fileItems;
        if (isMultipart) {
            try {
                mpinfo = new HashMap<String, Object>();
                // Create factory for disk-based file items, set constraints
                dfifactory = new DiskFileItemFactory();
                dfifactory.setSizeThreshold(IntTempThreshold);
                dfifactory.setRepository(new File(TempDirectory));

                // Create new file upload handler, set constraints
                upload = new ServletFileUpload(dfifactory);
                upload.setSizeMax(LongMaxFileSize);

                debug(HANDLER_ID, "Looking for form field parameters");

                fileItems = upload.parseRequest(request);
                FileItem item;
                Iterator<FileItem> iter = fileItems.iterator();
                String fieldname, fieldvalue;

                while (iter.hasNext()) {
                    item = iter.next();
                    if (item.isFormField()) {
                        fieldname = item.getFieldName();
                        fieldvalue = item.getString();
                        debug(HANDLER_ID, "  Found " + fieldname + "=" + fieldvalue);
                        // if we've already seen this key, ignore others
                        if (!mpinfo.containsKey(fieldname)) {
                            mpinfo.put(fieldname, fieldvalue);
                        }
                    }
                }
                // if we find agent, user, password values in the multipart content
                // they will override what we might have found in the request parameters
                if (mpinfo.containsKey($USER)) {
                  username = (String) mpinfo.get($USER);
               }
                if (mpinfo.containsKey($AGENT)) {
                  agentname = (String) mpinfo.get($AGENT);
               }
                if (mpinfo.containsKey($PW)) {
                  pwhash = (String) mpinfo.get($PW);
               }

                mpinfo.put($FILEITEMS, fileItems);
            } catch (FileUploadException fux) {
                handleFileUploadException(fux, request, response);
                return;
            } catch (Exception ex) {
                debug(HANDLER_ID, "Exception: " + ex.toString() + " (Exception)");
                returnError(request, response, "7500", HANDLER_ID + " encountered errors: " + ex.getMessage());
                return;
            }
        }
        // end multipart info collection

        //
        // If there is a Principal name, we can assume that all auth/auth
        // has already occurred. If the Principal name is present, it is
        // used to identify the user's folder in the user file space. If
        // a user parameter is also specified and it does not match the
        // Principal name, we have to check that the Principal is allowed
        // to act on behalf of the user specified in the user parameter and
        // reject the request if the Principal is not authorized.
        //
        String prname = CsiSecurityManager.getUserName();
        if ((null != prname) && (!prname.equalsIgnoreCase("guest"))) {
            agentname = prname;
            // user can do things in his own user folder
            if ((null == username) || (prname.equals(username))) {
                authorized = true;
                username = prname;
            }
            // otherwise check to see if principal is allowed to do things
            // in the named user's folder
            else {
                //
                // check to see if the username is known to the server. if it is,
                // continue. if not, we have to see if we should auto register
                // the username. this decision is based on the servlet init
                // parameter AutoRegisterUnknownUser. The default is false.
                //
                boolean knownuser = CsiSecurityManager.isRegisteredUser(username);
                if (knownuser) {
                    authorized = CsiSecurityManager.hasAnyRole(agentroles);
                } else {
                    if (autoregister) {
                        authorized = CsiSecurityManager.hasAnyRole(agentroles);
                        if (authorized) {
                            boolean added = addUser(username);
                            if (!added) {
                                returnError(request, response, "7406", HANDLER_ID + " cannot auto-register unknown user " + username
                                        + "; licensed user count exceeded or internal error occurred");
                                return;
                            }
                        }
                    } else {
                        authorized = false;
                    }
                }
            }
        }
        //
        // If there is no Principal name, it means that this servlet has
        // been invoked by way of an unprotected URL. This means that we
        // will examine the invoking parameters for user identification and
        // password credentials.
        //
        else {
            // if both agent and user name are present, see if they're the same.
            // if so, ignore agentname
            if ((null != agentname) && (null != username)) {
                if (agentname.equals(username)) {
                    agentname = null;
                }
            }
            authorized = CsiSecurityManager.userOrAgentHasAnyRole(agentname, username, pwhash, agentroles);
            if ((null != agentname) && (null == username)) {
               username = agentname;
            }
            // if authorized and (agentname != username) we need to see if the username
            // is known to the server. if it is, continue. if not, we have to see if we
            // should auto register the username. this decision is based on the servlet
            // init parameter AutoRegisterUnknownUser. The default is false.
            if ((authorized) && (null != username) && (null != agentname)) {
                if (!username.equals(agentname)) {
                    boolean knownuser = CsiSecurityManager.isRegisteredUser(username);
                    if (!knownuser) {
                        if (autoregister) {
                            boolean added = addUser(username);
                            if (!added) {
                                returnError(request, response, "7406", HANDLER_ID + " cannot auto-register unknown user " + username
                                        + "; licensed user count exceeded or internal error occurred");
                                return;
                            }
                        } else {
                            authorized = false; // disallow since user is not known and autoregister is off
                        }
                    }
                }
            }
        }

        // end this quickly if authorization checks fail.
        if (!authorized) {
            LOG.info("User " + agentname + " attempted to access files on behalf of user " + username + "; not authorized or user does not exist");
            debug(HANDLER_ID, "Authorization failed, access denied");
            returnError(request, response, "7403", HANDLER_ID + " cannot complete requested operation, access denied");
            return;
        } else {
            debug(HANDLER_ID, "Authorization successful");
        }

        // this should never happen anymore
        if (null == username) {
            username = "anonymous";
        }

        //
        // We'll split the handling of this request by HTTP method for slightly better readability
        //
        switch (httpmethod) {
        case GET:
            handleGET(request, response, username);
            break;

        case PUT:
            handlePUT(request, response, username);
            break;

        case POST:
            handlePOST(request, response, username, agentname, mpinfo);
            break;

        case DELETE:
            handleDELETE(request, response, username, agentname);
            break;

        default:
            returnError(request, response, "7405", HANDLER_ID + " does not support this HTTP method");
            break;
        }
    }

    // -------------------------------------------------------------------------------------
    //
    // Handle GET method - List Files and Directories
    //
    // -------------------------------------------------------------------------------------
    protected void handleGET(HttpServletRequest request, HttpServletResponse response, String username) throws ServletException, IOException {
        debug(HANDLER_ID, "handleGET entered, username=" + username);

        // Get the resource path from the request URI.
        StringTokenizer stk = getResourcePartFromURI(request, response);
        String resourcepath = getResourcePath(stk, username);

        // constrain path to toplevel/username/path
        String path = getUserFilePath(username, resourcepath);
        File subject = new File(path);

        // if the resource path doesn't exist, bail out
        if (!subject.exists()) {
            if (0 == resourcepath.length()) {
                resourcepath = "top-level folder for this user";
            }
            returnError(request, response, "7404", HANDLER_ID + " cannot find " + resourcepath);
            return;
        }

        RequestDispatcher dsp = null;

        if (subject.isFile()) {
            // If the resource path identifies a file, return information about
            // the file.
            request.setAttribute($USER, username);
            request.setAttribute("file", subject);
            request.setAttribute("toplevel", toplevel);
            String tok = getToken(subject.getPath());
            request.setAttribute("token", tok);
            request.setAttribute("urltoken", urlEncode(tok));



            /*dsp = */getServletContext().getRequestDispatcher(return_fileinfo);

        } else {
            // Otherwise, the resource path identifies a directory.
            // see if the directory is empty

            String[] contents = subject.list();

            if (contents == null) {
                contents = new String[0];
            }

            ArrayDeque<File> fileinfo = new ArrayDeque<File>();
            ArrayDeque<String> filetokens = new ArrayDeque<String>();
            ArrayDeque<String> fileurltokens = new ArrayDeque<String>();

            String tok;

            for (int i = 0; i < contents.length; i++) {
                File x = new File(subject, contents[i]);
                fileinfo.add(x);
                tok = getToken(x.getPath());
                filetokens.add(tok);
                fileurltokens.add(urlEncode(tok));
            }
            // ok, we've done everything so get out of dodge
            File[] fileinforeturn = fileinfo.toArray(new File[0]);
            String[] filetokensreturn = filetokens.toArray(new String[0]);
            String[] fileurltokensreturn = fileurltokens.toArray(new String[0]);

            // set all the attributes to pass to the return JSP
            request.setAttribute($USER, username);
            request.setAttribute("files", fileinforeturn);
            request.setAttribute("tokens", filetokensreturn);
            request.setAttribute("urltokens", fileurltokensreturn);
            request.setAttribute("toplevel", toplevel);
            String type = request.getParameter($TYPE);

            if ((type != null) && type.equalsIgnoreCase("JSON"))
            {
//            	FlexJsonCodec c = new FlexJsonCodec(CodecType.JSON_DEEP);
//            	Writer writer = new OutputStreamWriter( response.getOutputStream(), "UTF-8");
//            	c.marshal(fileinforeturn, writer);
           		return;
            }

	        dsp = getServletContext().getRequestDispatcher(return_filelist);
	        dsp.forward(request, response);

	        return;

        }
    }

    // -------------------------------------------------------------------------------------
    //
    // Handle PUT method: NOT USED
    //
    // -------------------------------------------------------------------------------------
    protected void handlePUT(HttpServletRequest request, HttpServletResponse response, String username) throws ServletException, IOException {
        // We don't support PUT for anything
        returnError(request, response, "7405", HANDLER_ID + " does not support the HTTP PUT method");
    }

    // -------------------------------------------------------------------------------------
    //
    // Handle DELETE method: Delete a file or directory
    //
    // Request URI must identify the resource to delete.
    //
    // -------------------------------------------------------------------------------------
    protected void handleDELETE(HttpServletRequest request, HttpServletResponse response, String username, String agentname) throws ServletException, IOException {
        debug(HANDLER_ID, "handleDELETE entered, username=" + username);

        if (null == agentname) {
         agentname = username;
      }

        // Get the resource path from the request URI.
        StringTokenizer stk = getResourcePartFromURI(request, response);
        String resourcepath = getResourcePath(stk, username);

        // constrain path to toplevel/username/path
        String path = getUserFilePath(username, resourcepath);

        File subject = new File(path);

        // if the resource path doesn't exist, bail out
        if (!subject.exists()) {
            returnError(request, response, "7404", HANDLER_ID + " cannot delete " + resourcepath + " (not found)");
            return;
        }

        // if the resource path identifies a file, delete it
        if (subject.isFile()) {
            subject.delete();
            LOG.info("User " + agentname + " deleted file " + subject.getPath());

        } else {
            // Otherwise, the resource path identifies a directory.
            // See if the directory is empty
            String[] contents = subject.list();

            // if it's empty, delete it
            if (null == contents) {
                returnError(request, response, "7409", HANDLER_ID + " cannot delete " + resourcepath + " (does not exist)");
                return;
            } else if (0 == contents.length) {
                subject.delete();
                LOG.info("User " + agentname + " deleted directory " + subject.getPath());
            }
            // otherwise say we cannot delete a non-empty directory
            else {
                returnError(request, response, "7409", HANDLER_ID + " cannot delete " + resourcepath + " (non-empty directory)");
                return;
            }
        }

        // return a simple ok response - maybe have to change this to
        // return information about the deleted file.
        returnOK(request, response);
    }

    // -------------------------------------------------------------------------------------
    //
    // Handle POST method: Upload a file, rename a file/directory, etc.
    //
    // -------------------------------------------------------------------------------------
    @SuppressWarnings( { "deprecation", "unchecked" })
    protected void handlePOST(HttpServletRequest request, HttpServletResponse response, String username, String agentname, HashMap mpinfo) throws ServletException, IOException {
        debug(HANDLER_ID, "handlePOST entered, username=" + username);

        // See if this POST contains multipart content
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        // Get the resource path from the request URI.
        StringTokenizer stk = getResourcePartFromURI(request, response);

        // We expect the request URI to identify the resource path either
        // in its entirety or in combination with a filename passed in the
        // URL or passed in the POST form fields for multipart content.

        // See if caller specified a file name in the parameters. This will
        // be used to identify a file to rename or specify a different name
        // for an uploaded file.
        String filename = request.getParameter($FILENAME);

        // See if caller specified a path in the parameters.
        String resourcepath = request.getParameter($PATH);

        // See if caller specified a rename value in the parameters.
        String rename = request.getParameter($RENAME);

        // See if caller passed file contents in a request parameter
        String content = request.getParameter($FILE);

        // See if caller requested file overwrite
        String overwrite = request.getParameter($OVERWRITE);
        boolean overwritefile = getBooleanFromString(overwrite);

        // If path is not specified as a URL parameter, assume we can derive
        // the path from request URI. URL parameter overrides request URI

        resourcepath = (null == resourcepath) ? getResourcePath(stk, username) : normalizePath(resourcepath);

        // constrain path to toplevel/username/path
        String path = getUserFilePath(username, resourcepath);

        // used to store File information to return to the requester
        ArrayDeque<File> fileinfo = new ArrayDeque<File>();

        // will indicate later whether or not a file was uploaded
        boolean uploaded = false;

        String token = "";
        String retrievalURL = "";
        String processingURL = "";

        // if we have multipart content, go upload the file(s) - we actually
        // support multiple file uploads but we're not going to publicize it.
        if (isMultipart) {
            debug(HANDLER_ID, "Processing multipart content");

            try {
                // grab important parameters gathered earlier from the field names
                if (mpinfo.containsKey($FILENAME)) {
                  filename = (String) mpinfo.get($FILENAME);
               }
                if (mpinfo.containsKey($PATH)) {
                  path = getUserFilePath(username, (String) mpinfo.get($PATH));
               }
                if (mpinfo.containsKey($RENAME)) {
                  rename = (String) mpinfo.get($RENAME);
               }

                List<Object> fileItems = (List<Object>) mpinfo.get($FILEITEMS);

                // make sure filename value is legal
                if (!validateFilename(request, response, filename)){
                    return;
                }

                FileItem item;
                File uploadedfile;
                Iterator<Object> iter = fileItems.iterator();

                debug(HANDLER_ID, "Looking for files to upload");

                while (iter.hasNext()) {
                    item = (FileItem) iter.next();
                    if (!item.isFormField()) {
                        uploadedfile = processUploadedFile(path, filename, item, overwritefile);
                        debug(HANDLER_ID, "File " + uploadedfile.getPath() + " committed to disk");
                        if (null != agentname) {
                            LOG.info("User " + agentname + " uploaded file " + uploadedfile.getPath());
                        } else {
                            LOG.info("User " + username + " uploaded file " + uploadedfile.getPath());
                        }
                        token = getToken(uploadedfile.getPath());
                        retrievalURL = getHandlerURL(request, token, retrievalsvc);
                        processingURL = getHandlerURL(request, token, processingsvc);
                        fileinfo.add(uploadedfile);
                        uploaded = true;
                    }
                    item.delete();

                    // we're only going to allow single file uploads for now. we'll need to
                    // rework this to support multi-file uploads. right now, we support renaming
                    // a file upon upload. We'd have to do some rework to support that for
                    // multi-file upload.
                    if (uploaded) {
                     break;
                  }
                }
            } catch (FileNotFoundException fnf) {
                debug(HANDLER_ID, "Exception: " + fnf.toString() + " (FileUpload)");
                returnError(request, response, "7404", HANDLER_ID + " cannot find/create file path " + path);
                return;
            } catch (FileUploadException fux) {
                handleFileUploadException(fux, request, response);
                return;
            } catch (SecurityException scf) {
                debug(HANDLER_ID, "Exception: " + scf.toString() + " (File/Directory Operation)");
                returnError(request, response, "7403", HANDLER_ID + " cannot complete requested operation, access denied");
                return;
            } catch (Exception ex) {
                debug(HANDLER_ID, "Exception: " + ex.toString() + " (Exception)");
                returnError(request, response, "7500", HANDLER_ID + " encountered errors: " + ex.getMessage());
                return;
            }
        }
        //
        // not multipart so let's see if caller passed file content in a request parameter
        //
        else {
            if (null != content) {
                // make sure filename value is legal
                if (!validateFilename(request, response, filename)){
                    return;
                }

                try {
                    File uploadedfile = processUploadedFile(path, filename, content, overwritefile);
                    debug(HANDLER_ID, "File " + uploadedfile.getPath() + " committed to disk");
                    if (null != agentname) {
                        LOG.info("User " + agentname + " uploaded file " + uploadedfile.getPath());
                    } else {
                        LOG.info("User " + username + " uploaded file " + uploadedfile.getPath());
                    }
                    token = getToken(uploadedfile.getPath());
                    retrievalURL = getHandlerURL(request, token, retrievalsvc);
                    processingURL = getHandlerURL(request, token, processingsvc);
                    fileinfo.add(uploadedfile);
                    uploaded = true;
                } catch (Exception ex2) {
                    debug(HANDLER_ID, "Exception: " + ex2.toString() + " (Exception)");
                    returnError(request, response, "7500", HANDLER_ID + " encountered errors: " + ex2.getMessage());
                    return;
                }
            }
        }

        // if nothing was uploaded, let's see if a file/directory operation like rename
        // was requested.
        debug(HANDLER_ID, "check to see if file uploaded or if file/directory operation is pending");
        if ((!uploaded) && (null != rename)) {
            debug(HANDLER_ID, "attempting to rename file/directory");
            File f = renameResource(agentname, username, path, filename, rename);

            debug(HANDLER_ID, "renameResource returned " + f);

            if (null != f) {
                fileinfo.add(f);
                token = getToken(f.getPath());
                retrievalURL = getHandlerURL(request, token, retrievalsvc);
                processingURL = getHandlerURL(request, token, processingsvc);
            } else {
                returnError(request, response, "7409", HANDLER_ID + " cannot rename " + resourcepath + " (not found, not allowed, or new name in use");
                return;
            }
        }

        debug(HANDLER_ID, "prepare to forward to return JSP");

        // if we don't have a retrieval URL, we really didn't do anything
        // so just return a simple OK
        if (retrievalURL.equals("")) {
            RequestDispatcher dsp = getServletContext().getRequestDispatcher(return_success);
            dsp.forward(request, response);
            return;
        }

        // ok, we've done everything so get out of dodge
        File[] fileinforeturn = fileinfo.toArray(new File[0]);

        // set all the attributes to pass to the return JSP

        request.setAttribute($USER, username);
        request.setAttribute("files", fileinforeturn);
        request.setAttribute("toplevel", toplevel);
        request.setAttribute("token", token);
        request.setAttribute("retrievalurl", retrievalURL);
        request.setAttribute("processingurl", processingURL);


        // return results
        RequestDispatcher dsp = getServletContext().getRequestDispatcher(return_filelist);
        dsp.forward(request, response);
    }

    /*
     * Checks to make sure the provided filename doesn't include a file separator, isn't . or .., and doesn't contain
     * any disallowed characters.
     */
   private boolean validateFilename(HttpServletRequest request, HttpServletResponse response, String filename)
         throws ServletException, IOException {
      boolean valid = true;

      if (filename != null) {
         /*
          * if name == . || .. if name contains \/|*?:<>%"
          */
         String normalizedFilename = normalizePath(filename);

         if (normalizedFilename.contains(File.separator)) {
            returnError(request, response, "7400", HANDLER_ID + " does not support complex filename values; use path parameter to specify a target folder");
            valid = false;
         }
         if (valid && (".".equals(normalizedFilename) || "..".equals(normalizedFilename))) {
            returnError(request, response, "7400", HANDLER_ID + " does not support files named \".\" or \"..\"");
            valid = false;
         }
         if (valid && INVALID_FILE_CHARS_PATTERN.matcher(normalizedFilename).find()) {
            returnError(request, response, "7400", HANDLER_ID + " does not support the following characters in filenames: / \\ | * ? : < > % \"");
            valid = false;
         }
      }
      return valid;
   }

    // -------------------------------------------------------------------------------------
    //
    // Supporting methods for HTTP GET
    //
    // -------------------------------------------------------------------------------------

    // maybe need to support some sort of pattern filtering

    // -------------------------------------------------------------------------------------
    //
    // Supporting methods for HTTP POST
    //
    // -------------------------------------------------------------------------------------

    //
    // Get uploaded file into the right place on the filesystem.
    //
    protected File processUploadedFile(String path, String newfilename, Object item, boolean overwritefile) throws Exception {
        debug(HANDLER_ID, "processUploadedFile entered");
        String filename = "";

        boolean isFileItem = (item instanceof FileItem);
        boolean isStringItem = (item instanceof String);

        if ((!isFileItem) && (!isStringItem)) {
            throw new Exception("Content is not multipart/form-data or string; unable to upload file");
        }

        // If FileItem by default, the filename is carried over
        if (isFileItem) {
            filename = ((FileItem) item).getName();
            debug(HANDLER_ID, "path=" + path + "  newfn=" + newfilename + "  fn=" + filename);

            // Some browsers and utilities send path information in the name, others do not.
            // We just want the filename and have no need for the path info.
            filename = normalizePath(filename);
            if (filename.contains(File.separator)) {
                filename = filename.substring(filename.lastIndexOf(File.separator) + 1);
                debug(HANDLER_ID, "name trimmed to " + filename);
            }
            // no filename means no file was uploaded even though
            // multipart content. how bogus
            if (filename.equalsIgnoreCase("")) {
                throw new Exception("Content is multipart/form-data but no file found to upload");
            }
        }

        String fn = filename;

        // see if file is to be named differently
        if (null != newfilename) {
            fn = normalizePath(newfilename);
        }

        // make sure we have a filename
        if ((null == fn) || (fn.equals(""))) {
            fn = normalizePath(DEFAULT_FILENAME);
        }

        // make sure path is relative
        if (null != path) {
            path = normalizePath(path);
            if (path.startsWith(File.separator)) {
                path = path.substring(1);
            }
        }
        File pathfile = new File(path);

        // See if path exists. If not, create the path ...
        if (!pathfile.exists()) {
            pathfile.mkdirs();
            debug(HANDLER_ID, "creating path");
        }

        debug(HANDLER_ID, "relPath=" + path + " absPath=" + pathfile.getAbsolutePath() + " exists=" + pathfile.exists());

        // Get file descriptor using path and filename.
        File tmpfile = new File(path + File.separator + fn);

        File uploadedfile = null;

        if (overwritefile) {
            uploadedfile = tmpfile;
        } else {
            // Handle name collision. If the name to be used for the uploaded
            // file already exists, tack on a number to make it unique.
            uploadedfile = getUnusedFile(tmpfile);
        }

        // Commit the file to disk
        if (isFileItem) {
            ((FileItem) item).write(uploadedfile);
        } else {
           // If content is from a string, write that
           try (FileWriter fileWriter = new FileWriter(uploadedfile);
                Writer output = new BufferedWriter(fileWriter)) {
              // FileWriter always assumes default encoding is OK!
              output.write((String) item);
           }
        }
        return uploadedfile;
    }

    //
    // Rename a file or directory
    //
    protected File renameResource(String agentname, String username, String path, String filename, String rename) {
        // If no rename value, nothing to do.
        if (null == rename) {
            return null;
        }

        if (null == agentname) {
         agentname = username;
      }

        path = normalizePath(path);
        rename = normalizePath(rename);
        filename = normalizePath(filename);

        boolean renamed = false;
        File newfile = null;

        debug(HANDLER_ID, "renameResource: path=" + path + "  fn=" + filename + "  rn=" + rename);

        // disallow renaming of user's toplevel directory for now
        String usertop = getUserFilePath(username, null) + File.separator;
        if ((path.equals(usertop)) && (null == filename)) {
            return null;
        }

        // figure out what we are renaming
        //
        // if the path specifies a file, the filename value is ignored and
        // we know immediately that we are renaming a file
        //
        // if the path is not to a file and the filename value is present,
        // combine them to determine what we are renaming
        //
        // if the path is not to a file and the filename value is absent,
        // we are renaming a directory
        File subject = new File(path);

        // If the path doesn't exist, nothing to do.
        if (!subject.exists()) {
            return null;
        }

        if (subject.isFile()) {
            debug(HANDLER_ID, "renaming a FILE - subject: " + path);
            // get the directory part of the path
            String wrk = subject.getPath();
            String dirpart = wrk.substring(0, wrk.lastIndexOf(File.separator));

            // handle a complex rename value and a simple rename value
            newfile = getSubjectFile(username, dirpart, rename, true);
        }
        // if path does not specify a file and a filename is present, we need
        // to combine them to identify the file to rename. If filename is
        // not present, just rename the directory identified by the path.
        else {
            if (null != filename) {
                // handle a complex filename value and a simple filename value
                subject = getSubjectFile(username, path, filename);
                // handle a complex rename value and a simple rename value
                newfile = getSubjectFile(username, path, rename, true);
            }
            // this is a directory rename
            else {
                debug(HANDLER_ID, "subject: " + path);
                debug(HANDLER_ID, "newfile: " + rename);
                newfile = getSubjectFile(username, getUserFilePath(username, null), rename, true);
            }
        }

        // do the actual rename but do not allow rename to clobber an
        // existing file/directory
        if (subject.exists() && !newfile.exists()) {
            if (subject.renameTo(newfile)) {
                debug(HANDLER_ID, "renamed " + subject.getPath() + " to " + newfile.getPath());
                LOG.info("User " + agentname + " renamed " + subject.getPath() + " to " + newfile.getPath());
                renamed = true;
            } else {
                debug(HANDLER_ID, "rename " + subject.getPath() + " to " + newfile.getPath() + " FAILED");
            }
        }

        if (renamed) {
            return newfile;
        } else {
            return null;
        }
    }

    //
    // handle a FileUploadException
    //
    protected void handleFileUploadException(FileUploadException fux, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (fux instanceof FileUploadBase.SizeLimitExceededException) {
            String cause = REQUEST_PATTERN.matcher(fux.getMessage()).replaceFirst("Request");

            // The message looks like this:
            // Request was rejected because its size (2257338) exceeds the configured maximum (153600)
            debug(HANDLER_ID, "Exception: " + cause);

            // We'll return something nicer to the client
            cause = "The size of the file you are trying to upload exceeds the currently configured limit of " + (LongMaxFileSize / 1024) + "K.";

            returnError(request, response, "7501", cause);
            return;
        }
        debug(HANDLER_ID, "Exception: " + fux.toString() + " (FileUploadException)");
        fux.printStackTrace();
        returnError(request, response, "7501", HANDLER_ID + " encountered errors uploading file");
    }

    // -------------------------------------------------------------------------------------
    //
    // General supporting methods
    //
    // -------------------------------------------------------------------------------------

    protected static String getEncryptionPhrase() {
        return encryptionphrase;
    }

    protected static String getTopLevelFolderName() {
        return toplevel;
    }

    //
    // Get file descriptor using a combination of path and filename. The
    // tricky part is that the filename can be simple or complex. A simple
    // name is one that does not include File.separators. A complex one
    // does and we have to account for that by using the last part as the
    // filename and the front part as part of the path.
    //
    protected File getSubjectFile(String username, String path, String filename) {
        return getSubjectFile(username, path, filename, false);
    }

    protected File getSubjectFile(String username, String path, String filename, boolean create) {
        debug(HANDLER_ID, "getSubjectFile: path=" + path + "  filename=" + filename);
        if (null == filename) {
            return new File(path);
        }

        File subject = null;

        // does the filename value contain any File.separators? if so,
        // we need to account for that
        String fpath = null;
        String fname = filename;

        if (filename.contains(File.separator)) {
            fpath = filename.substring(0, filename.lastIndexOf(File.separator));
            fname = filename.substring(filename.lastIndexOf(File.separator) + 1);
            debug(HANDLER_ID, "split filename value: " + fpath + " , " + fname);
        }
        // handle a complex filename value and a simple filename value
        if (null != fpath) {

            // if fpath.length() == 0 or if fpath starts with / this means that
            // the filename should be relative to the toplevel user directory
            if ((0 == fpath.length()) || (fpath.startsWith(File.separator))) {
                path = getUserFilePath(username, null);
            }

            debug(HANDLER_ID, "subject: " + path + File.separator + fpath + "  " + fname);
            File subdir = new File(path + File.separator + fpath);

            if (create) {
                subdir.mkdirs();
            }
            subject = new File(subdir, fname);
        } else {
            subject = new File(path, filename);
        }
        return subject;
    }

    //
    // Get a string that identifies the constrained path for a
    // given user.
    //
    protected String getUserFilePath(String username, String path) {
        if (null == path) {
            return toplevel + File.separator + username;
        } else {
            return toplevel + File.separator + username + File.separator + path;
        }
    }

    //
    // Avoid file naming collisions!
    //
    // @param file
    // @return original file if it does not exist,
    // otherwise, return a new, non-existent file to avoid overwrite
    // new filename will be in the format filename-#.ext
    //
    protected static File getUnusedFile(File file) {
        if (!file.exists()) {
            return file;
        }
        int count = 0;

        String parent = file.getParent();
        String fn = file.getName();
        // String fn = file.getPath();

        int dotIndex = fn.lastIndexOf(".");
        String ext = "";

        if (dotIndex >= 0) {
            ext = fn.substring(dotIndex);
            fn = fn.substring(0, dotIndex);
        }

        do {
            file = new File(parent, fn + "-" + (++count) + ext);
        } while (file.exists());

        return file;
    }

    //
    // Return a boolean based on the string passed in. If it is
    // yes, y, 1, true, on return true, else return false
    //
    protected boolean getBooleanFromString(String val) {
        if (null == val) {
         return false;
      }

        boolean rc = false;

        for (int i = 0; i < MEANS_TRUE.length; i++) {
            if (val.equalsIgnoreCase(MEANS_TRUE[i])) {
                rc = true;
                break;
            }
        }
        return rc;
    }

    //
    // Generate a unique id to return to the user for file uploads
    //
    protected String getToken(String path) {
        String token = path;
        if (encrypttoken) {
            token = _encrypter.encrypt(path);
            debug(HANDLER_ID, "token: (" + path + ") " + token + "  length=" + token.length());
            String test = _encrypter.decrypt(token);
            debug(HANDLER_ID, "decrypted: " + test);
        } else {
            debug(HANDLER_ID, "token: (" + path + ") " + token + "  length=" + token.length());
        }

        return token;
    }

    //
    // URL encode a string value
    //
    protected String urlEncode(String val) {
        String ue_val = null;
        try {
            debug(HANDLER_ID, "value (pre-urlenc) : " + val + "  length=" + val.length());
            ue_val = java.net.URLEncoder.encode(val, "UTF-8");
            debug(HANDLER_ID, "value (post-urlenc): " + ue_val + "  length=" + ue_val.length());
            String test = java.net.URLDecoder.decode(ue_val, "UTF-8");
            debug(HANDLER_ID, "value (post-urldec): " + test + "  length=" + test.length());
        } catch (UnsupportedEncodingException uex) {
        }

        return ue_val;
    }

    //
    // Generate URL to retrieve/process file that was uploaded, renamed
    //
    protected String getHandlerURL(HttpServletRequest request, String token, String service) {
        // grab request URL so we can take it apart
        URL requestURL;
        try {
            requestURL = new URL(request.getRequestURL().toString());
        } catch (java.net.MalformedURLException mx) {
            return null;
        }
        String prot = requestURL.getProtocol();
        String host = requestURL.getHost();
        String port = Integer.toString(requestURL.getPort());
        String ctx = request.getContextPath();

        // be sure to url encode the token value
        String ue_token = urlEncode(token);

        String hurl;

        if (null != port) {
            hurl = prot + "://" + host + ":" + port + ctx + service + "?token=" + ue_token;
        } else {
            hurl = prot + "://" + host + ctx + service + "?token=" + ue_token;
        }
        debug(HANDLER_ID, "handler URL: " + hurl);
        return hurl;
    }

    protected boolean addUser(String username) {
       boolean rc = false;
       User user = new User();

       user.setName(username);
       user.setRemark("Auto-registered by " + HANDLER_ID);

       SecurityPolicyConfig policy = Configuration.getInstance().getSecurityPolicyConfig();

       policy.applyExpirationPolicy(user);

       try {
          if (Product.getLicense().addingUserAllowed((int) Users.getUserCount())) {
             Users.add(user);
             LOG.info("Previously unknown user {} auto-registered by {}", () -> username, () -> HANDLER_ID);
             rc = true;
          } else {
             LOG.info("Cannot auto-register unknown user {}; licensed user count ({}) exceeded",
                      () -> username, () -> Integer.toString(Product.getLicense().getUserCount()));
          }
       } catch (CentrifugeException cex) {
          CsiPersistenceManager.markForRollback();
          LOG.info(HANDLER_ID + " encountered errors auto-registering user " + username);
          cex.printStackTrace();
       }
       return rc;
    }
}
