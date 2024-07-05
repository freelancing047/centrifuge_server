package csi.container.tomcat.security;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.coyote.ActionCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiAuthenticator {
   private static final Logger LOG = LogManager.getLogger("csi.security");
   
    public static String API_AUTH_TYPE = "API_AUTH";
    private static ThreadLocal<Container> localContainer = new ThreadLocal<Container>();
    private static ThreadLocal<Request> localRequest = new ThreadLocal<Request>();

    public static Container getCurrentContainer() {
        return localContainer.get();
    }

    public static Request getCurrentRequest() {
        return localRequest.get();
    }

    public static void init(Container container, Request request) {
        localContainer.set(container);
        localRequest.set(request);

        // copy what AuthenticatorBase.invoke() does to init the servlet
        // request with a principal
        Principal principal = request.getRequest().getUserPrincipal();
        if (principal == null) {
            Session session = getCatalinaSession(request, false);
            if (session != null) {
                principal = session.getPrincipal();
                if (principal != null) {
                    request.setAuthType(session.getAuthType());
                    request.setUserPrincipal(principal);
                }
            }
        }
    }

    public static void reset() {
        localContainer.remove();
        localRequest.remove();
    }

    public static Principal authenticate(X509Certificate[] certs) {
        Container container = getCurrentContainer();
        return container.getRealm().authenticate(certs);
    }

    public static Principal authenticate(String username, String password) {
        Container container = getCurrentContainer();
        return container.getRealm().authenticate(username, password);
    }

    public static Principal loginSSL() {
        Request catalinaRequest = getCurrentRequest();
        if (!catalinaRequest.isSecure()) {
            return null;
        }

        Principal p = null;

        X509Certificate certs[] = (X509Certificate[]) catalinaRequest.getAttribute(Globals.CERTIFICATES_ATTR);

        if ((certs == null) || (certs.length < 1)) {
            catalinaRequest.getCoyoteRequest().action(ActionCode.REQ_SSL_CERTIFICATE, null);
            certs = (X509Certificate[]) catalinaRequest.getAttribute(Globals.CERTIFICATES_ATTR);
            if (certs != null && (certs.length >= 1)) {
                p = authenticate(certs);
                if (p != null) {
                    register(p, HttpServletRequest.CLIENT_CERT_AUTH, null, null);
                }
            }
        }

        return p;
    }

    public static Principal login() {

        // Principal p = getExistingPrincipal();
        // if (p != null) {
        // return p;
        // }

        Principal p = loginSSL();
        if (p != null) {
            return p;
        }

        p = ApiAuthenticator.loginForm();
        if (p != null) {
            return p;
        }

        p = ApiAuthenticator.loginBasic();

        return p;
    }

    public static Principal loginBasic() {
        // Principal p = getExistingPrincipal();
        // if (p != null) {
        //    return p;
        // }

        Principal principal = null;
        Request catalinaRequest = getCurrentRequest();
        String authHeader = catalinaRequest.getHeader("Authorization");

        if ((authHeader != null) && authHeader.toLowerCase().startsWith("basic ", 0)) {
            byte[] decoded = Base64.getDecoder().decode(authHeader.substring(6));
            String auth = new String(decoded);
            String username = null;
            String password = null;
            int colon = auth.indexOf(':');

            if (colon < 0) {
                username = auth;
            } else {
                username = auth.substring(0, colon);
                password = auth.substring(colon + 1);
            }
            principal = login(username, password, HttpServletRequest.BASIC_AUTH);
        }
        return principal;
    }


    public static Principal loginForm() {
        // Principal p = getExistingPrincipal();
        // if (p != null) {
        // return p;
        // }

        Request catalinaRequest = getCurrentRequest();
        String username = catalinaRequest.getParameter("j_username");
        String password = catalinaRequest.getParameter("j_password");

        return login(username, password, HttpServletRequest.FORM_AUTH);
    }

    public static Principal login(String username, String password) {
        return login(username, password, API_AUTH_TYPE);
    }

    // Authenticate the user and associate with the current session. This is taken
    // from AuthenticatorBase.register()
    public static Principal login(String username, String password, String authType) {
        // Principal p = getExistingPrincipal();
        // if (p != null) {
        // return p;
        // }

        Request catalinaRequest = getCurrentRequest();

        Principal principal = null;

        if (catalinaRequest != null) {

            if (username != null && !username.isEmpty()) {
                principal = authenticate(username, password);
            }

            if (principal != null) {
                catalinaRequest.setAuthType(authType);
                catalinaRequest.setUserPrincipal(principal);

                register(principal, authType, username, password);
            } else {
                // log.warning(String.format("User \'%s\' login failed", username));
            }
        }

        return principal;
    }

    private static void register(Principal principal, String authType, String username, String password) {
        // Cache the authentication information in the session
       LOG.info(String.format("User logged in \'%1s\' with type %2s", principal.getName(), authType));

        Request catalinaRequest = getCurrentRequest();
        catalinaRequest.setAuthType(authType);
        catalinaRequest.setUserPrincipal(principal);

        Session session = getCatalinaSession(catalinaRequest, true);

        if (session != null) {
            // Cache the authentication information in the session
            session.setAuthType(authType);
            session.setPrincipal(principal);

            if (username != null) {
                session.setNote(Constants.SESS_USERNAME_NOTE, username);
            } else {
                session.removeNote(Constants.SESS_USERNAME_NOTE);
            }

            if (password != null) {
                session.setNote(Constants.SESS_PASSWORD_NOTE, password);
            } else {
                session.removeNote(Constants.SESS_PASSWORD_NOTE);
            }
        }
    }

    // public static boolean authorize(Principal principal, List roles)
    // {
    // Container container = getContainer();
    // Realm realm = container.getRealm();
    // Iterator iter = roles.iterator();
    // while (iter.hasNext())
    // {
    // String role = (String)iter.next();
    // if (realm.hasRole(principal, role))
    // return true;
    // }
    // return false;
    // }

    public static void logout() {
        logout(true);
    }

    public static void logout(boolean invalidateSession) {
        Request catalinaRequest = getCurrentRequest();

        if (catalinaRequest != null) {
            Session catalinaSession = getCatalinaSession(catalinaRequest, false);
            if (catalinaSession != null) {
                Principal p = catalinaSession.getPrincipal();
                if (p != null) {
                   LOG.info(String.format("User \'%s\' has logged out", p.getName()));
                }

                catalinaSession.setPrincipal(null);
                catalinaSession.setAuthType(null);
                catalinaSession.removeNote(Constants.SESS_USERNAME_NOTE);
                catalinaSession.removeNote(Constants.SESS_PASSWORD_NOTE);

                if (invalidateSession) {
                    catalinaSession.getSession().invalidate();
                }
            }
        }
    }

    // from AuthenticatorBase.getSession()
    private static Session getCatalinaSession(Request request, boolean create) {

        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();

        HttpSession hses = hreq.getSession(create);

        if (hses == null)
            return (null);
        Manager manager = request.getContext().getManager();

        if (manager == null)
            return (null);
        else {
            try {
                return manager.findSession(hses.getId());
            } catch (IOException e) {
                // e.printStackTrace();
                return null;
            }
        }
    }

}
