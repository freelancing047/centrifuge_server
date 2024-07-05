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
public class CentrifugeCertValve extends FormAuthenticator {
   private static final Logger LOG = LogManager.getLogger(CentrifugeValve.class);

   private static boolean _doDebug = true;


/*
 * NB: This is not the log4j logger, and is not configured by log4j.xml.
 */
    // private static Log LOG = LogFactory.getLog(CentrifugeValve.class);
    //private Logger LOG;

    // **********************************************************************
    // Constructor
    // **********************************************************************
    public CentrifugeCertValve()
    {
        super();

        if (_doDebug) LOG.info("                  [Centrifuge] INFO  - <> <> <>  CentrifugeValve::CentrifugeValve()");

    }

    public static void logDebug(boolean doDebugIn)
    {
        if (doDebugIn)
        {
            _doDebug = true;

            if (_doDebug) LOG.info("                  [Centrifuge] INFO  - <> <> <>  CentrifugeValve::logDebug(true)");
        }
        else
        {
            _doDebug = false;
        }
    }

    // **********************************************************************
    // @see org.apache.catalina.authenticator.AuthenticatorBase#invoke(
    // org.apache.catalina.connector.Request,
    // org.apache.catalina.connector.Response
    // )
    // **********************************************************************
    @Override
    public void invoke(Request requestIn, Response responseIn) throws IOException, ServletException
    {

        if (_doDebug) LOG.info("                  [Centrifuge] INFO  - >> >> >>  CentrifugeValve::invoke(Request request, Response response)");

        requestIn.setAsyncSupported(true);

        LoginConfig myConfig = this.context.getLoginConfig();
        Realm myRealm = this.context.getRealm();

        //
        // Before we do anything, is the request URI subject to a security constraint?
        //
        SecurityConstraint[] myConstraints = myRealm.findSecurityConstraints(requestIn, this.context);

        if (myConstraints == null)
        {

            if (_doDebug) LOG.info("                                                  - there are no security constrains, so ... getNext().invoke(request, response);");

            getNext().invoke(requestIn, responseIn);
            return;
        }

        // If we're here, we have to do some authentication ...

        //
        // Have we got a cached authenticated Principal?
        //
        if (cache)
        {

            if (_doDebug) LOG.info("                                                  - check for a cached principal");

            checkForCachedPrincipal(requestIn);
        }

        //
        // Special handling for form-based logins to deal with the case
        // where the login form (and therefore the "j_security_check" URI
        // to which it submits) might be outside the secured area
        //
        String myContextPath = this.context.getPath();
        String myRequestURI = requestIn.getDecodedRequestURI();

        if (myRequestURI.startsWith(myContextPath) && myRequestURI.endsWith(Constants.FORM_ACTION))
        {

            if (_doDebug) LOG.info("                                                  - continue on to login form that is being requested");

            if (!authenticate(requestIn, responseIn)) // RU - this is a call to FORM based auth!
            {
                return;
            }
        }

        // Enforce any user data constraint for this security constraint

        if (!myRealm.hasUserDataPermission(requestIn, responseIn, myConstraints))
        {

            if (_doDebug) LOG.info("                                                  - failed data access permissions");

        /*
         * ASSERT: Authenticator already set the appropriate HTTP status code, so we do not have to do anything
         * special
         */
            return;
        }

        //
        // Since authenticate modifies the response on failure,
        // we have to check for allow-from-all first.
        //
        boolean myAuthenticationRequired = true;

        for (int i = 0; (myConstraints.length > i) && myAuthenticationRequired; i++)
        {
            if (!myConstraints[i].getAuthConstraint())
            {
                myAuthenticationRequired = false;
            }
            else if (!myConstraints[i].getAllRoles())
            {
                String[] roles = myConstraints[i].findAuthRoles();

                if (roles == null || roles.length == 0)
                {
                    myAuthenticationRequired = false;
                }
            }
        }

        //
        // Authentication is required.
        //
        if (myAuthenticationRequired)
        {
            if (_doDebug) LOG.info("                                                  - authorization required -- so check");

            if ((!requestIn.isSecure())
                    || (!authenticateSSL(requestIn, responseIn, myConfig)))
            {
                return;
            }
        }

        if (_doDebug) LOG.info("                                                  - check against realm resource permissions");

        if (!myRealm.hasResourcePermission(requestIn, responseIn, myConstraints, this.context))
        {
        /*
         * ASSERT: AccessControl method has already set the appropriate HTTP status code, so we do not have to do
         * anything special
         */

            if (_doDebug) LOG.info("                                                  - failed realm check against resource permissions");

            return;
        }

        if (_doDebug) LOG.info("                                                  - move on ...");

        getNext().invoke(requestIn, responseIn);

        if (_doDebug) LOG.info("                  [Centrifuge] INFO  - << << <<  CentrifugeValve::invoke(Request request, Response response)");

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
    public boolean authenticateSSL(Request requestIn, Response responseIn, LoginConfig configIn) {
        boolean mySuccess = false;

        if (_doDebug) LOG.info("                  [Centrifuge] INFO  - >> >> >>  CentrifugeValve::authenticateSSL(Request request, Response response, LoginConfig config)");

        // Have we already authenticated someone?
        Principal myPrincipal = requestIn.getUserPrincipal();

        if (null != myPrincipal)
        {
            if (_doDebug) LOG.info("                                                  - principal already set");

            // Associate the session with any existing SSO session in order
            // to get coordinated session invalidation at logout
            String ssoId = (String) requestIn.getNote(Constants.REQ_SSOID_NOTE);

            if (ssoId != null)
            {
                associate(ssoId, requestIn.getSessionInternal(true));
            }
            mySuccess = true;;
        }
        else
        {
            // Retrieve the certificate chain for this client
            X509Certificate myCerts[] = (X509Certificate[]) requestIn.getAttribute(Globals.CERTIFICATES_ATTR);

            if ((null == myCerts) || (0 == myCerts.length))
            {
                if (_doDebug) LOG.info("                                                  - no local cert -- check global");

                myCerts = (X509Certificate[]) requestIn.getAttribute(Globals.CERTIFICATES_ATTR);
            }

            if ((null != myCerts) && (0 < myCerts.length))
            {
                if (_doDebug) LOG.info("                                                  - user realm to authenticate cert");

                // Authenticate the specified certificate chain
                myPrincipal = context.getRealm().authenticate(myCerts);
                if (myPrincipal != null)
                {
                    // Looks authentic but now we have to see if the common name matches
                    // a registered user. If not, we'll return false which will fallback
                    // to a traditional form-based login.

                    // extract the common name and use that as the username to check. it
                    // is expected that the common name matches a registered user.

                    // String pname = principal.getName();
                    // String uname = getUserNameFromDN(pname);

                    // Realm realm = this.context.getRealm();
                    // Principal prn = ((centrifuge.container.realm.JDBCRealm) realm).createPrincipal(uname);

                    if (_doDebug) LOG.info("           - authenticated the cert, so register it");

                    // Cache the principal (if requested) and record this authentication
                    register(requestIn, responseIn, myPrincipal, HttpServletRequest.CLIENT_CERT_AUTH, null, null);
                    mySuccess = true;
                }
                else
                {
                    if (_doDebug) LOG.info("                                                  - failed authentication so return without success");
                }
            }
            else
            {
                if (_doDebug) LOG.info("                                                  - no cert found so return without success");
            }
        }
        if (_doDebug) LOG.info("                  [Centrifuge] INFO  - << << <<  CentrifugeValve::authenticateSSL(Request request, Response response, LoginConfig config) -->> " + Boolean.toString(mySuccess));

        return mySuccess;
    }

    // **********************************************************************
    //
    // See if we have a cached authenticated Principal. If so, set the
    // right values in the request.
    //
    // **********************************************************************
    private void checkForCachedPrincipal(Request requestIn)
    {

        if (_doDebug) LOG.info("                  [Centrifuge] INFO  - >> >> >>  CentrifugeValve::checkForCachedPrincipal(Request request)");

        Principal myPrincipal = requestIn.getUserPrincipal();
        if (myPrincipal == null)
        {
            Session session = requestIn.getSessionInternal(false);
            if (session != null)
            {
                myPrincipal = session.getPrincipal();
                if (myPrincipal != null)
                {
                    requestIn.setAuthType(session.getAuthType());
                    requestIn.setUserPrincipal(myPrincipal);

                    if (_doDebug) LOG.info("                                                  - Created cached principal: " + display(myPrincipal.getName()) + " with authorization: " + display(requestIn.getAuthType()));
                }
            }
        }
        else
        {
            if (_doDebug) LOG.info("                                                  - Found cached principal: " + display(myPrincipal.getName()) + " with authorization: " + display(requestIn.getAuthType()));
        }
        if (_doDebug) LOG.info("                  [Centrifuge] INFO  - << << <<  CentrifugeValve::checkForCachedPrincipal(Request request)");
    }

    private static String display(String stringIn)
    {
        if (null != stringIn)
        {
            return "\"" + stringIn + "\"";
        }
        else
        {
            return "<null>";
        }
    }
}
