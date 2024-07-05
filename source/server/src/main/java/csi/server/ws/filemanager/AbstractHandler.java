package csi.server.ws.filemanager;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *  Base servlet responsible for handling API requests
 *
 *  @version   1.0
 *  @author    Mitch Shue
 */
public abstract class AbstractHandler extends HttpServlet {
   protected static final Logger LOG = LogManager.getLogger(AbstractHandler.class);

    private static final String HANDLER_ID = "BaseHandler";

    // some constants
    static final int GET = 1;
    static final int PUT = 2;
    static final int POST = 3;
    static final int DELETE = 4;

    static final String FORWARD_SLASH = "/";
    static final String BACKWARD_SLASH = "\\";

    // Debugging flag
    static String debugmode;
    static boolean debugging;

    // servlet paths from init configuration
    static String return_success;
    static String return_error;
    static String return_token;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // Read servlet init parameters and set defaults if missing.
        debugmode = config.getInitParameter("debugging");
        if (null == debugmode) {
            debugmode = "on";
        }
        debugging = debugmode.equalsIgnoreCase("on");

        return_success = config.getInitParameter("ReturnSuccess");
        if (null == return_success) {
            return_success = "/filemanager/SimpleOK.jsp";
        }

        return_error = config.getInitParameter("ReturnError");
        if (null == return_error) {
            return_error = "/filemanager/Error.jsp";
        }

        return_token = config.getInitParameter("ReturnToken");
        if (null == return_token) {
            return_token = "/filemanager/Token.jsp";
        }
    }

    //
    // Debugging
    //
    protected static void debug(String id, Object text) {
        if (LOG.isDebugEnabled()) {
           LOG.debug(id + ": " + text);
        }
    }

    //
    // Handle HTTP GET
    //
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response, GET);
    }

    //
    // Handle HTTP PUT
    //
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response, PUT);
    }

    //
    // Handle HTTP POST
    //
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response, POST);
    }

    //
    // Handle HTTP DELETE
    //
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response, DELETE);
    }

    //
    // Create a string to identify the path to the resource to operate on.
    // This is here to give us the ability to alter the path, perhaps to
    // incorporate user/group isolation.
    //
    protected String getResourcePath(StringTokenizer stk, String username) {
        StringBuilder pathBuff = new StringBuilder("");

        if (null != stk) {
            boolean firstToken = true;
            while (stk.hasMoreTokens()) {
                if (firstToken) {
                    firstToken = false;
                } else {
                    pathBuff.append(File.separator);
                }
                pathBuff.append(stk.nextToken());
            }
        }

        return pathBuff.toString();
    }

    //
    // Convert a path name to use only File.separator characters.
    //
   public static String normalizePath(String path) {
      String rp = null;

      if (path != null) {
         rp = path;

         if (path.contains(FORWARD_SLASH)) {
            rp = rp.replace(FORWARD_SLASH, File.separator);
         }
         if (path.contains(BACKWARD_SLASH)) {
            rp = rp.replace(BACKWARD_SLASH, File.separator);
         }
         debug(HANDLER_ID, "path/file name normalized to " + rp);
      }
      return rp;
   }

    //
    // Return a StringTokenizer for the part of the request URI that identifies the
    // resource to operate on. The first part of the request URI identifies the handling
    // servlet and the second part identifies the actual resource (file/folder) to operate
    // on.
    //
    // Given: http://server/Centrifuge/services/managefile/chris/superman.xml
    //
    // http://server/Centrifuge/services/managefile identifies the handling servlet
    // chris/superman.xml identifies a file
    //
    // This method returns a StringTokenizer for chris/superman.xml
    //
    protected StringTokenizer getResourcePartFromURI(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reqURI = request.getRequestURI();

        String servletpath = request.getServletPath();
        String delimiter = servletpath.substring(servletpath.lastIndexOf("/") + 1);

        debug(HANDLER_ID, "reqURI = " + reqURI + "  delimiter = " + delimiter);

        StringTokenizer stk = new StringTokenizer(reqURI, "/");

        // look for the keyword in the URI that marks the end of request and the beginning
        // of the variable information
        while (stk.hasMoreTokens()) {
            String str = stk.nextToken();

            if (delimiter.equalsIgnoreCase(str)) {
                break;
            }
        }

        return stk;
    }

    //
    // Return error response
    //
    protected void returnError(HttpServletRequest request, HttpServletResponse response, String errorcode, String errormsg) throws ServletException, IOException {
        request.setAttribute("error-code", errorcode);
        request.setAttribute("error-message", errormsg);
        RequestDispatcher dsp = getServletContext().getRequestDispatcher(return_error);

        dsp.forward(request, response);
    }

    //
    // Return simple ok response
    //
    protected void returnOK(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dsp = getServletContext().getRequestDispatcher(return_success);

        dsp.forward(request, response);
    }

    //
    // Return ok response with token
    //
    protected void returnToken(HttpServletRequest request, HttpServletResponse response, String username, String token) throws ServletException, IOException {
        request.setAttribute("user", username);
        request.setAttribute("token", token);
        RequestDispatcher dsp = getServletContext().getRequestDispatcher(return_token);

        dsp.forward(request, response);
    }

    //
    // Process incoming HTTP request
    //
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response, int httpmethod) throws ServletException, IOException;

}
