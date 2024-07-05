package csi.container.tomcat.security;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;

/**
 * Concrete implementation of RequestFilterValve that handles cross orign requests. see: http://www.w3.org/TR/cors/
 */
public class CrossOrginValve extends RequestFilterValve {

    private static final String info = "csi.container.tomcat.security.CrossOriginValve/1.0";
    protected String allowUrl;
    protected String denyUrl;
    protected Pattern allowUrls[];
    protected Pattern denyUrls[];

    public CrossOrginValve() {
        super();
        allowUrl = null;
        allowUrls = new Pattern[0];

        denyUrl = null;
        denyUrls = new Pattern[0];
    }

    public String getAllowUrl() {
        return allowUrl;
    }

    public void setAllowUrl(String list) {
        this.allowUrl = list;
//        this.allowUrls = precalculate(list);
    }

    public String getDenyUrl() {
        return denyUrl;
    }

    public void setDenyUrl(String list) {
        this.denyUrl = list;
//        this.denyUrls = precalculate(list);
    }

    protected boolean isAllowedOrigin(String property) throws IOException, ServletException {
        return true;
//        for (int i = 0; i < denies.length; i++) {
//            if (denies[i].matcher(property).matches()) {
//                return false;
//            }
//        }
//
//        for (int i = 0; i < allows.length; i++) {
//            if (allows[i].matcher(property).matches()) {
//                return true;
//            }
//        }
//
//        if (denies.length > 0 && allows.length == 0) {
//            return true;
//        } else {
//            return false;
//        }
    }

    protected boolean isAllowedUrl(String url) throws IOException, ServletException {

        for (int i = 0; i < denyUrls.length; i++) {
            if (denyUrls[i].matcher(url).matches()) {
                return false;
            }
        }

        for (int i = 0; i < allowUrls.length; i++) {
            if (allowUrls[i].matcher(url).matches()) {
                return true;
            }
        }

        if (denyUrls.length > 0 && allowUrls.length == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {

        ServletRequest servRequest = request.getRequest();
        if (servRequest instanceof HttpServletRequest) {
            ApiAuthenticator.init(getContainer(), request);

            // handle cross orign requests. see: http://www.w3.org/TR/cors/
            String origin = request.getHeader("Origin");
            if (origin != null) {
                response.setContentType("text/plain; charset=UTF-8");
                setNoCacheHeaders(response);

                boolean allowed = isAllowedOrigin(origin);
                boolean allowedUrl = isAllowedUrl(request.getRequestURL().toString());
                if (allowed && allowedUrl) {
                    // if (origin.equalsIgnoreCase("null")) {
                    // origin = "*";
                    // }
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Expose-Headers", "X-Auth-Required, X-Task-Status, X-Client-Id, X-Task-Id");
                    // response.setHeader("Access-Control-Max-Age", "1200");

                    // handle pre-flight crossdomain options request
                    if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
                        String accessMethod = request.getHeader("Access-Control-Request-Method");
                        String accessHeaders = request.getHeader("Access-Control-Request-Headers");
                        response.setHeader("Access-Control-Allow-Methods", accessMethod);
                        response.setHeader("Access-Control-Allow-Headers", accessHeaders);
                        response.setHeader("Access-Control-Allow-Credentials", "true");
                        response.setStatus(HttpServletResponse.SC_OK);
                        // response.flushBuffer();
                        return;
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    // response.flushBuffer();
                    return;
                }
            }
        }

        Valve next = getNext();
        if (next != null) {
            next.invoke(request, response);
        }
    }

    private void setNoCacheHeaders(Response response) {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
    }

}
