package csi.container.tomcat.valve;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.Globals;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
//import org.apache.coyote.ActionCode;

/**********************************************************************
 * <p>
 * Authenticator Valve for Tomcat
 * </p>
 * <p>
 * This Valve implements a special login method - SSL Certificate based, with FORM fallback. It is intended to be used
 * on an SSL protected channel. The Authenticator first tries to authenticate based on a client certificate included in
 * the SSL Request. If this is not possible, or the request does not contain a client certificate, the Authenticator
 * attempts to authenticate using the FORM login method.
 * </p>
 * <p>
 * To use this authenticator, include a <login-config> in your web.xml, but do not include a <auth-method> tag. Do
 * include the <form-login-config> tag to configure the pages used if FORM login occurs.
 * </p>
 * <p>
 * Instead of the <auth-method> tag, manually configure a Valve in your tomcat configuration (server.xml or
 * context.xml). Configure the Valve on the corresponding context, and use this class as the Valve type.
 * </p>
 * 
 * @author Richard Unger
 * 
 *         This class was based upon Mr. Unger's original work but is no longer just a copy of it.
 * 
 * 
 **********************************************************************/
public class CentrifugeAuthenticator extends FormAuthenticator {
   private static final Logger LOG = LogManager.getLogger(CentrifugeAuthenticator.class);

   // static values used for controlling user logon prompt
    private static final String _suppressPrompt = "SuppressUserPrompt";
    private static Boolean _doPrompt = null;

    // **********************************************************************
    // Constructor
    // **********************************************************************
    public CentrifugeAuthenticator() {
        super();
    }

    // **********************************************************************
    // @see org.apache.catalina.authenticator.AuthenticatorBase#invoke(
    // org.apache.catalina.connector.Request,
    // org.apache.catalina.connector.Response
    // )
    // **********************************************************************
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
    	
    	//log.info("                  [Centrifuge] INFO  - >> >> >>  CentrifugeAuthenticator::invoke()");

    	request.setAsyncSupported(true);
        boolean isLogDebug = LOG.isDebugEnabled();
        if (isLogDebug) {
           LOG.debug("Checking request to " + request.getMethod() + " " + request.getRequestURI());
        }

        LoginConfig config = this.context.getLoginConfig();
        Realm realm = this.context.getRealm();

        //
        // Before we do anything, is the request URI subject to a security constraint?
        //

        SecurityConstraint[] constraints = realm.findSecurityConstraints(request, this.context);

        if ((constraints == null)/*
                                                    * && (!Constants.FORM_METHOD.equals(config.getAuthMethod()))
                                                    */) {
            if (isLogDebug) {
               LOG.debug("Resource is not subject to any security constraint. RETURN");
            }
            getNext().invoke(request, response);
            return;
        }

        // If we're here, we have to do some authentication ...

        //
        // Have we got a cached authenticated Principal?
        //
        if (cache) {
            if (isLogDebug) {
               LOG.debug("Checking for cached authenticated Principal");
            }
            checkForCachedPrincipal(request);
        }

        //
        // Special handling for form-based logins to deal with the case
        // where the login form (and therefore the "j_security_check" URI
        // to which it submits) might be outside the secured area
        //
        String contextPath = this.context.getPath();
        String requestURI = request.getDecodedRequestURI();

        if (requestURI.startsWith(contextPath) && requestURI.endsWith(Constants.FORM_ACTION)) {
            if (isLogDebug) {
               LOG.debug("Looks like simple FORM-based auth is required, calling authenticate()");
               LOG.debug(">>> requestURI: " + requestURI);
            }
            if (!authenticate(request, response)) { // RU - this is a call to FORM based auth!
                if (isLogDebug) {
                   LOG.debug("Failed authenticate() test: " + requestURI + ". RETURN");
                }
                return;
            }
        }

        // Enforce any user data constraint for this security constraint
        if (isLogDebug) {
           LOG.debug("Calling hasUserDataPermission()");
        }
        if (!realm.hasUserDataPermission(request, response, constraints)) {
            if (isLogDebug) {
               LOG.debug("Failed hasUserDataPermission() test. RETURN");
            }

            /*
             * ASSERT: Authenticator already set the appropriate HTTP status code, so we do not have to do anything
             * special
             */
            return;
        }
        if (isLogDebug) {
           LOG.debug("User has data permission");
        }

        //
        // Since authenticate modifies the response on failure,
        // we have to check for allow-from-all first.
        //
        boolean authRequired = true;

        for (int i = 0; i < constraints.length && authRequired; i++) {
            if (!constraints[i].getAuthConstraint()) {
                authRequired = false;
            } else if (!constraints[i].getAllRoles()) {
                String[] roles = constraints[i].findAuthRoles();

                if (roles == null || roles.length == 0) {
                    authRequired = false;
                }
            }
        }

        //
        // Authentication is required.
        //
        if (authRequired) {

            // If not SSL, skip SSL bits
            if (!request.isSecure()) {
                if (isLogDebug) {
                   LOG.debug("No SSL, calling authenticate()");
                }
                if (!authenticate(request, response)) {
                    if (isLogDebug) {
                       LOG.debug("Failed authenticate() test. RETURN");
                    }

                    /*
                     * ASSERT: Authenticator already set the appropriate HTTP status code, so we do not have to do
                     * anything special
                     */
                    return;
                }
            }
            // Secure connection so try client cert then fallback to form
            else {
                if (isLogDebug) {
                   LOG.debug("SSL, calling authenticateSSL(), fallback to authenticate()");
                }
                // Didn't get a username from the cert so fallback to form authentication
                if (!authenticateSSL(request, response, config)) {
                	if ((!promptUser(realm)) || (!authenticate(request, response))) {
                        if (isLogDebug) {
                           LOG.debug("Failed authenticate() test for both SSL and form. RETURN");
                        }
                        return;
                    }
                }
            }
        }

        if (isLogDebug) {
           LOG.debug("User has been authenticated. Calling hasResourcePermission()");
        }

        if (!realm.hasResourcePermission(request, response, constraints, this.context)) {
            if (isLogDebug) {
               LOG.debug("Failed hasResourcePermission() test. RETURN");
            }

            /*
             * ASSERT: AccessControl method has already set the appropriate HTTP status code, so we do not have to do
             * anything special
             */
            return;
        }
        if (isLogDebug) {
           LOG.debug("User has resource permission");
        }

        // Any and all specified constraints have been satisfied
        if (isLogDebug) {
           LOG.debug("Successfully passed all security constraints");
        }

        getNext().invoke(request, response);
    	
    	//log.info("                  [Centrifuge] INFO  - << << <<  CentrifugeAuthenticator::invoke()");

    }

    // **********************************************************************
    //
    // Authenticate the user by checking for the existence of a certificate
    // chain, and optionally asking a trust manager to validate that we
    // trust this user.
    //
    //
    // This method was modified from the original SSLAuthenticator not to
    // throw exceptions or modify the response, since there will be FORM
    // authentication fallback.
    //
    //
    // @param request Request we are processing
    // @param response Response we are creating
    // @param config Login configuration describing how authentication
    // should be performed
    //
    // @exception IOException if an input/output error occurs
    //
    // **********************************************************************
    public boolean authenticateSSL(Request request, Response response, LoginConfig config) throws IOException {

        boolean isLogDebug = LOG.isDebugEnabled();

        // Have we already authenticated someone?
        Principal principal = request.getUserPrincipal();

        // String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (principal != null) {
            if (isLogDebug) {
               LOG.debug("Already authenticated '" + principal.getName() + "', good to go");
            }
            // Associate the session with any existing SSO session in order
            // to get coordinated session invalidation at logout
            String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);

            if (ssoId != null) {
                associate(ssoId, request.getSessionInternal(true));
            }
            return (true);
        }

        // Retrieve the certificate chain for this client
        if (isLogDebug) {
           LOG.debug("Examining client certificates");
        }

        X509Certificate certs[] = (X509Certificate[]) request.getAttribute(Globals.CERTIFICATES_ATTR);

        if ((certs == null) || (certs.length < 1)) {
           LOG.debug("No certificates found in HttpRequest.");
            //request.getCoyoteRequest().action(ActionCode.ACTION_REQ_SSL_CERTIFICATE, null);
            certs = (X509Certificate[]) request.getAttribute(Globals.CERTIFICATES_ATTR);
            if (certs != null && (certs.length >= 1)) {
               LOG.debug("Certificates found in CoyoteRequest");
            }
        } else {
           LOG.debug("Certificates found in HttpRequest");
        }

        if ((certs == null) || (certs.length < 1)) {
            if (isLogDebug) {
               LOG.debug("No certificates included with this request");
            }
            return (false);
        }

        LOG.debug("Found certificates, authenticating using them!");

        // Authenticate the specified certificate chain
        principal = context.getRealm().authenticate(certs);
        if (principal == null) {
            if (isLogDebug) {
               LOG.debug("Realm.authenticate() returned false");
            }
            return (false);
        }

        // Looks authentic but now we have to see if the common name matches
        // a registered user. If not, we'll return false which will fallback
        // to a traditional form-based login.

        // extract the common name and use that as the username to check. it
        // is expected that the common name matches a registered user.

        // String pname = principal.getName();
        // String uname = getUserNameFromDN(pname);

        // Realm realm = this.context.getRealm();
        // Principal prn = ((centrifuge.container.realm.JDBCRealm) realm).createPrincipal(uname);

        if (isLogDebug) {
           LOG.debug("Principal = " + principal);
        }

        // Cache the principal (if requested) and record this authentication
        register(request, response, principal, HttpServletRequest.CLIENT_CERT_AUTH, null, null);
        return (true);
    }

    // **********************************************************************
    //
    // See if we have a cached authenticated Principal. If so, set the
    // right values in the request.
    //
    // **********************************************************************
    private void checkForCachedPrincipal(Request request) {
        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            Session session = request.getSessionInternal(false);

            if (session != null) {
                principal = session.getPrincipal();
                if (principal != null) {
                    if (LOG.isDebugEnabled()) {
                       LOG.debug("Cache hit: auth type=" + session.getAuthType() + ", principal=" + session.getPrincipal());
                    }
                    request.setAuthType(session.getAuthType());
                    request.setUserPrincipal(principal);
                }
            }
        }
    }
    
    private boolean promptUser(Realm realmIn) {
    	
    	//log.info("                  [Centrifuge] INFO  - >> >> >>  CentrifugeAuthenticator::promptUser()");
       
        // Use a kludge method to determine if logon prompt is to be suppressed
        if (null == _doPrompt) {
        	
        	String myPromptCheck = realmIn.toString();
        	
        	if ((null != myPromptCheck) && (_suppressPrompt.length() <= myPromptCheck.length())
        			&& _suppressPrompt.equals(myPromptCheck.substring(0, _suppressPrompt.length()))) {
        		_doPrompt = false;
        	} else {
        		_doPrompt = true;
        	}
        	
        	//log.info("                  [Centrifuge] INFO  - >> >> >>  CentrifugeRealm::toString() -->> " + ((null != myPromptCheck) ? myPromptCheck : "<null>"));
        }
    	
    	//log.info("                  [Centrifuge] INFO  - << << <<  CentrifugeAuthenticator::promptUser()");
    	
    	return _doPrompt;
    }

    // **********************************************************************
    //
    // Extract the user name out of a DN string. For now, we're just
    // grabbing the first common name. Not sure if that'll be ok but it
    // is what it is for right now!
    //
    // **********************************************************************
    // private String getUserNameFromDN(String dn)
    // {
    // String attr = null;
    // String work = null;
    // boolean hit = false;
    //
    // // break the string at commas (have to be careful here because you
    // // might have values that contain commas. values like that will be
    // // quoted and you'd be able to reconstruct the value so not too much
    // // worry)
    // StringTokenizer stk = new StringTokenizer(dn, ",");
    //
    // while (stk.hasMoreTokens()) {
    // attr = (String) stk.nextToken();
    // work = attr.toLowerCase().trim();
    // // look for the common name attribute
    // if (work.startsWith("cn")) {
    // hit = true;
    // break;
    // }
    // }
    //
    // String uname = null;
    //
    // if (hit) {
    // int x = attr.indexOf("=");
    //
    // if (x > 0) {
    // uname = attr.substring(x + 1);
    // uname = uname.trim();
    // }
    // }
    // return uname;
    // }

}
