package csi.container.tomcat.security;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public class ApiAuthenticatorValve extends ValveBase {

    private static String API_LOGIN_PATH = "/api/login";
    private static String API_V1_LOGIN_PATH = "/api-v1/login";
    private static String API_LOGOUT_PATH = "/api/logout";
    private static String API_V1_LOGOUT_PATH = "/api-v1/logout";
    
    private String myMethod = null;

    public void invoke(Request request, Response response) throws IOException, ServletException {
        request.setAsyncSupported(true);
        try {
            ServletRequest servRequest = request.getRequest();
            if (servRequest instanceof HttpServletRequest) {
                ApiAuthenticator.init(getContainer(), request);

                HttpServletRequest hrequest = ((HttpServletRequest) servRequest);
                String path = hrequest.getServletPath();
                String pathInfo = request.getPathInfo();
                if (pathInfo == null) {
                    pathInfo = "";
                } else {
                    pathInfo = pathInfo.trim();
                }
                
                if( path == null ) {
                    // no-op.  it appears servletPath is always null here?!?
                } else if (path.equalsIgnoreCase(API_LOGIN_PATH) || API_V1_LOGIN_PATH.equalsIgnoreCase(path + pathInfo)) {
                    if (!request.getMethod().equalsIgnoreCase("POST")) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                    setNoCacheHeaders(response);
                    // logout first since the user is explicitly asking
                    // to re-login
                    // ApiAuthenticator.logout(false);

                    Principal p = ApiAuthenticator.login();

                    if (p != null) {
                        response.setHeader("P3P", "CP=\"STA\"");
                        response.setContentType("text/plain; charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_OK);
                        // response.flushBuffer();
                    } else {
                        boolean usingBasic = false;
                        String authHeader = request.getHeader("Authorization");
                        if (authHeader != null && authHeader.toLowerCase().startsWith("basic ", 0)) {
                            usingBasic = true;
                        }

                        String interactive = request.getParameter("interactive");

                        if (usingBasic && interactive != null && interactive.equalsIgnoreCase("true")) {
                            // set auth scheme to Basic so browser will prompt
                            // user for auth
                            response.setHeader("WWW-Authenticate", "Basic");
                        } else {
                            // Since the http specs requires that is header be
                            // present when sending a 401, we'll send a custom auth scheme for
                            // compliance. The client/browser won't recognize this scheme so
                            // it will not prompt the user for auth.
                            response.setHeader("WWW-Authenticate", "csi.api.login");
                        }

                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }

                    return;
                } else if (path.equalsIgnoreCase(API_LOGOUT_PATH) || API_V1_LOGOUT_PATH.equalsIgnoreCase(path + pathInfo)) {
                    myMethod = request.getMethod();
                    if (!myMethod.equalsIgnoreCase("POST")) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }

                    setNoCacheHeaders(response);

                    ApiAuthenticator.logout();

                    response.setContentType("text/plain; charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            }

            Valve next = getNext();
            if (next != null)
                next.invoke(request, response);
        } finally {
            ApiAuthenticator.reset();
        }
    }

    private void setNoCacheHeaders(Response response) {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
    }

}
