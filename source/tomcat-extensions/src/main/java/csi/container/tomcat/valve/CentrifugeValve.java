package csi.container.tomcat.valve;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

import csi.container.tomcat.ConfigResponse;
import csi.container.tomcat.LogThreadContextUtil;
import csi.container.tomcat.realm.CentrifugeRealm;
import csi.security.jaas.spi.AuthorizationMode;

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
public class CentrifugeValve extends FormAuthenticator {
   private static final Logger LOG = LogManager.getLogger(CentrifugeValve.class);

    private static boolean _doDebug = false;


    private static boolean _initialized = false;
    private static java.lang.String _userHeader = null;
    private static boolean _isClosedSystem = false;
    private static boolean _isKerberosSystem = false;
    private static boolean _fix8point4bug = true;
    private static ConfigResponse _configuration = null;
    private static AuthorizationMode[] _authenticationOrder = null;

    private byte[] _kerberosTicket = null;

    /*
     * NB: This is not the log4j logger, and is not configured by log4j.xml.
     */
    // private static Log LOG = LogFactory.getLog(CentrifugeValve.class);
    //private Logger LOG;

    // **********************************************************************
    // Constructor
    // **********************************************************************
    public CentrifugeValve()
    {
        super();

    	if (_doDebug) {

            LOG.info("<> <> <>  CentrifugeValve::CentrifugeValve()");
        }
    }
    
    public static void logDebug(boolean doDebugIn)
    {
        if (doDebugIn)
    	{
        	_doDebug = true;
        	
        	if (_doDebug) LOG.info("<> <> <>  CentrifugeValve::logDebug(true)");
    	}
    	else
    	{
        	_doDebug = false;
    	}
        CentrifugeRealm.logDebug(_doDebug);
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
        LogThreadContextUtil.putRequestThreadContext(requestIn);

        if (_doDebug) {
           LOG.info(">> >> >>  CentrifugeValve::invoke(Request request, Response response)");
        }

        requestIn.setAsyncSupported(true);

        LoginConfig myConfig = this.context.getLoginConfig();
        Realm myRealm = context.getRealm();

        // Initalize here to ensure access to realm and authentication
        // in order to retrieve values.
        if (!_initialized)
        {
            initializeStaticValues(myRealm);
        }

        //
        // Before we do anything, is the request URI subject to a security constraint?
        //
        SecurityConstraint[] myConstraints = myRealm.findSecurityConstraints(requestIn, this.context);

        if (myConstraints == null)
        {

            if (_doDebug) LOG.info("           - there are no security constrains, so ... getNext().invoke(request, response);");
            if (_doDebug) LOG.info("<< << <<  CentrifugeValve::invoke(Request request, Response response)");

            getNext().invoke(requestIn, responseIn);
            return;
        }

        // If we're here, we have to do some authentication ...

        //
        // Have we got a cached authenticated Principal?
        //
        if (cache)
        {

            if (_doDebug) LOG.info("           - check for a cached principal");

            checkForCachedPrincipal(requestIn);
        }

        //
        // Special handling for form-based logins to deal with the case
        // where the login form (and therefore the "j_security_check" URI
        // to which it submits) might be outside the secured area
        //
        java.lang.String myContextPath = this.context.getPath();
        java.lang.String myRequestURI = requestIn.getDecodedRequestURI();

        if (myRequestURI.startsWith(myContextPath) && myRequestURI.endsWith(Constants.FORM_ACTION))
        {

            if (_doDebug) LOG.info("           - continue on to login form that is being requested");

            if (!authenticate(requestIn, responseIn)) // RU - this is a call to FORM based auth!
            {
                if (_doDebug) LOG.info("           - failed authentication through login form");
                if (_doDebug) LOG.info("<< << <<  CentrifugeValve::invoke(Request request, Response response)");

                return;
            }
        }

        // Enforce any user data constraint for this security constraint

        if (!myRealm.hasUserDataPermission(requestIn, responseIn, myConstraints))
        {

            if (_doDebug) LOG.info("           - failed data access permissions");
            if (_doDebug) LOG.info("<< << <<  CentrifugeValve::invoke(Request request, Response response)");

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
                java.lang.String[] roles = myConstraints[i].findAuthRoles();

                if (roles == null || roles.length == 0)
                {
                    myAuthenticationRequired = false;
                }
            }
        }

        //
        // Authentication is required.
        //
        if (myAuthenticationRequired) {

            if (okToProceed(requestIn, responseIn)) {

                if (_doDebug)
                   LOG.info("           - authorization required -- so check");

                if (_isClosedSystem && (null != _authenticationOrder) && (0 < _authenticationOrder.length)) {

                    boolean myAuthenticationFlag = false;
                    CentrifugeRealm myCsiRealm = (CentrifugeRealm)myRealm;

                    for (int i = 0; _authenticationOrder.length > i; i++) {

                        AuthorizationMode myMode = _authenticationOrder[i];

                        try {

                            switch (myMode) {

                                case NONE:

                                    myAuthenticationFlag = autoAuthenticate(requestIn, responseIn, myConfig);
                                    break;

                                case FORM:

                                    myAuthenticationFlag = authenticateWithDialog(requestIn, responseIn, myConfig);
                                    break;

                                case CERT:

                                    myAuthenticationFlag = authenticateSSL(requestIn, responseIn, myConfig);
                                    break;

                                case KERBEROS:

                                    myAuthenticationFlag = authenticateKerberos(requestIn, responseIn, myConfig);
                                    break;

                                case DEFAULT:

                                    myAuthenticationFlag = authenticateDefault(requestIn, responseIn, myConfig);
                                    break;
                            }
                            if (myAuthenticationFlag) {

                                break;
                            }

                        } catch (Exception myException) {

                        }
                    }
                    if (!myAuthenticationFlag) {

                        if (_doDebug)
                           LOG.info("           - failed authentication check");
                        if (_doDebug)
                           LOG.info("<< << <<  CentrifugeValve::invoke(Request request, Response response)");

                        responseIn.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                } else {

                    if ((!requestIn.isSecure())
                            || (!authenticateSSL(requestIn, responseIn, myConfig))) {
                        // unsuccessful -- attempt form based authentication
                        if (!authenticateWithDialog(requestIn, responseIn, myConfig)) {
                            if (_doDebug)
                               LOG.info("           - failed password authentication check");
                            if (_doDebug)
                               LOG.info("<< << <<  CentrifugeValve::invoke(Request request, Response response)");

                            return;
                        }
                    }
                }

            } else {

                if (_doDebug)
                   LOG.info("<< << <<  CentrifugeValve::invoke(Request request, Response response)");
                return;
            }
        }

        if (_doDebug)
           LOG.info("           - check against realm resource permissions");

        if (!myRealm.hasResourcePermission(requestIn, responseIn, myConstraints, this.context)) {
            /*
             * ASSERT: AccessControl method has already set the appropriate HTTP status code, so we do not have to do
             * anything special
             */

            if (_doDebug)
               LOG.info("           - failed realm check against resource permissions");
            if (_doDebug)
               LOG.info("<< << <<  CentrifugeValve::invoke(Request request, Response response)");

            return;
        }

        if (_doDebug) LOG.info("           - passed authentication check");
        if (_doDebug)
           LOG.info("<< << <<  CentrifugeValve::invoke(Request request, Response response)");

        getNext().invoke(requestIn, responseIn);
    }
    public boolean authenticateWithDialog(Request requestIn, Response responseIn, LoginConfig configIn) throws IOException
    {
        boolean mySuccess = false;

        try {

            mySuccess = authenticate(requestIn, responseIn);

        } catch (Exception myException) {

        }
        return mySuccess;
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
    public boolean authenticateSSL(Request requestIn, Response responseIn, LoginConfig configIn) throws IOException
    {
    	boolean mySuccess = false;
    	
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeValve::authenticateSSL(Request request, Response response, LoginConfig config)");

        // Have we already authenticated someone?
        java.security.Principal myPrincipal = requestIn.getUserPrincipal();

        if (null != myPrincipal)
        {
        	
        	if (_doDebug) LOG.info("           - principal already set");
        	
            // Associate the session with any existing SSO session in order
            // to get coordinated session invalidation at logout
            java.lang.String ssoId = (java.lang.String) requestIn.getNote(Constants.REQ_SSOID_NOTE);

            if (ssoId != null)
            {
                associate(ssoId, requestIn.getSessionInternal(true));
            }
            mySuccess = true;
        }
        else
        {
            // Retrieve the certificate chain for this client
            X509Certificate myCerts[] = (X509Certificate[]) requestIn.getAttribute(Globals.CERTIFICATES_ATTR);

            if ((null == myCerts) || (0 == myCerts.length))
            {
            	
            	if (_doDebug) LOG.info("           - no local cert -- check global");
            	
            	myCerts = (X509Certificate[]) requestIn.getAttribute(Globals.CERTIFICATES_ATTR);
            }

            if ((null != myCerts) && (0 < myCerts.length))
            {
            	
            	if (_doDebug) LOG.info("           - user realm to authenticate cert");
            	
                // Authenticate the specified certificate chain
                myPrincipal = context.getRealm().authenticate(myCerts);
                if (myPrincipal != null)
                {
                    // Looks authentic but now we have to see if the common name matches
                    // a registered user. If not, we'll return false which will fallback
                    // to a traditional form-based login.

                    // extract the common name and use that as the username to check. it
                    // is expected that the common name matches a registered user.

                    // java.lang.String pname = principal.getName();
                    // java.lang.String uname = getUserNameFromDN(pname);

                    // Realm realm = this.context.getRealm();
                    // java.security.Principal prn = ((centrifuge.container.realm.JDBCRealm) realm).createPrincipal(uname);

                	
                	if (_doDebug) LOG.info("           - authenticated the cert, so register it");
                	
                    // Cache the principal (if requested) and record this authentication
                    register(requestIn, responseIn, myPrincipal, HttpServletRequest.CLIENT_CERT_AUTH, null, null);
                    mySuccess = true;
                }
                else
                {
                	
                	if (_doDebug) LOG.info("           - failed authentication so return without success");
                	
                }
            }
            else
            {
            	
            	if (_doDebug) LOG.info("           - no cert found so return without success");
            	
            }
        }
    	
    	if (_doDebug) LOG.info("<< << <<  CentrifugeValve::authenticateSSL(Request request, Response response, LoginConfig config) -->> " + display(mySuccess));

        return mySuccess;
    }
    public boolean autoAuthenticate(Request requestIn, Response responseIn, LoginConfig configIn) throws IOException
    {
    	
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeValve::autoAuthenticate(Request request, Response response, LoginConfig config)");

    	boolean mySuccess = false;
    	
    	if ((null != _userHeader) && (0 < _userHeader.length()))
    	{
        	java.lang.String myUsername = getHeader(requestIn, _userHeader);

        	if ((null != myUsername) && (0 < myUsername.length()))
        	{
                // Have we already authenticated someone?
                Principal myPrincipal = requestIn.getUserPrincipal();

                if (null != myPrincipal)
                {
                	
                	if (_doDebug) LOG.info("           - principal already set");
                	
                    // Associate the session with any existing SSO session in order
                    // to get coordinated session invalidation at logout
                    java.lang.String mySsoId = (java.lang.String) requestIn.getNote(Constants.REQ_SSOID_NOTE);

                    if (mySsoId != null)
                    {
                        associate(mySsoId, requestIn.getSessionInternal(true));
                    }
                    mySuccess = true;
                }
                else if (_isClosedSystem)
                {
                    // Authenticate the specified certificate chain
                    myPrincipal = ((CentrifugeRealm)context.getRealm()).autoAuthenticate(myUsername);
                    if (myPrincipal != null)
                    {
    	            	if (_doDebug) LOG.info("           - auto-authenticated, so register it");
    	            	
    	                // Cache the principal (if requested) and record this authentication
    	                register(requestIn, responseIn, myPrincipal, Constants.DEFAULT_JAAS_CONF, null, null);
    	                mySuccess = true;
                    }
                }
        	}
    	}
    	
    	if (_doDebug) LOG.info("<< << <<  CentrifugeValve::autoAuthenticate(Request request, Response response, LoginConfig config) -->> " + display(mySuccess));

        return mySuccess;
    }

    public boolean authenticateKerberos(Request requestIn, Response responseIn, LoginConfig configIn) throws IOException
    {
        boolean mySuccess = false;
        java.security.Principal myPrincipal = requestIn.getUserPrincipal();

        // Have we already authenticated someone?
        if (null != myPrincipal)
        {

            if (_doDebug) LOG.info("           - principal already set");

            // Associate the session with any existing SSO session in order
            // to get coordinated session invalidation at logout
            java.lang.String mySsoId = (java.lang.String) requestIn.getNote(Constants.REQ_SSOID_NOTE);

            if (mySsoId != null)
            {
                associate(mySsoId, requestIn.getSessionInternal(true));
            }
            mySuccess = true;
        }
        else if (_isClosedSystem)
        {
            try {

                CentrifugeRealm myRealm = (CentrifugeRealm) context.getRealm();
                CentrifugeRealm.KerberosResult myResult = myRealm.authenticateKerberos(_kerberosTicket);

                myPrincipal = myResult.getPrincipal();

                if (myPrincipal != null) {
                    if (_doDebug)
                       LOG.info("           - auto-authenticated, so register it");

                    // Send response token on success and failure
                    responseIn.setHeader("WWW-Authenticate", "Negotiate "
                            + Base64.encodeBase64String(myResult.getKerberosTicket()));

                    // Cache the principal (if requested) and record this authentication
                    register(requestIn, responseIn, myPrincipal, Constants.DEFAULT_JAAS_CONF, null, null);
                    mySuccess = true;
                }
            } catch (Exception myException) {

            }
        }
        return mySuccess;
    }

    public boolean authenticateDefault(Request requestIn, Response responseIn, LoginConfig configIn) throws IOException
    {
        boolean mySuccess = false;
        // Have we already authenticated someone?
        java.security.Principal myPrincipal = requestIn.getUserPrincipal();

        if (null != myPrincipal)
        {

            if (_doDebug) LOG.info("           - principal already set");

            // Associate the session with any existing SSO session in order
            // to get coordinated session invalidation at logout
            java.lang.String mySsoId = (java.lang.String) requestIn.getNote(Constants.REQ_SSOID_NOTE);

            if (mySsoId != null)
            {
                associate(mySsoId, requestIn.getSessionInternal(true));
            }
            mySuccess = true;
        }
        else if (_isClosedSystem)
        {
            // Authenticate the specified certificate chain
            myPrincipal = ((CentrifugeRealm)context.getRealm()).authenticateDefault();
            if (myPrincipal != null)
            {
                if (_doDebug) LOG.info("           - auto-authenticated, so register it");

                // Cache the principal (if requested) and record this authentication
                register(requestIn, responseIn, myPrincipal, Constants.SPNEGO_METHOD, null, null);
                mySuccess = true;
            }
        }
        return mySuccess;
    }

    private boolean showDialog(String usernameIn, String passwordIn) {

        return ((null == usernameIn) || (0 == usernameIn.length())
                || (null == passwordIn) || (0 == passwordIn.length()));
    }

    // **********************************************************************
    //
    // See if we have a cached authenticated java.security.Principal. If so,
    // set the right values in the request.
    //
    // **********************************************************************
    private void checkForCachedPrincipal(Request requestIn)
    {
    	
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeValve::checkForCachedPrincipal(Request request)");
    	
        java.security.Principal myPrincipal = requestIn.getUserPrincipal();
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
                	
                    if (_doDebug) LOG.info("           - Created cached principal: " + display(myPrincipal.getName()) + " with authorization: " + display(requestIn.getAuthType()));
                	
                }
            }
        }
        else
        {
        	
        	if (_doDebug) LOG.info("           - Found cached principal: " + display(myPrincipal.getName()) + " with authorization: " + display(requestIn.getAuthType()));
        	
        }
    	
    	if (_doDebug) LOG.info("<< << <<  CentrifugeValve::checkForCachedPrincipal(Request request)");
    	
    }

    private java.lang.String getHeader(Request requestIn, java.lang.String headerNameIn) {

        if (_doDebug)
        {
            java.util.Enumeration<java.lang.String> myHeaders = requestIn.getHeaderNames();

            while(myHeaders.hasMoreElements())
            {
                java.lang.String myHeaderName = myHeaders.nextElement();
                java.lang.String myHeaderValue = requestIn.getHeader(myHeaderName);
                LOG.info("           - Header -- " + display(myHeaderName) + ":" + display(myHeaderValue));
            }
        }
        return requestIn.getHeader(headerNameIn);
    }

    // **********************************************************************
    //
    // Extract the user name out of a DN string. For now, we're just
    // grabbing the first common name. Not sure if that'll be ok but it
    // is what it is for right now!
    //
    // **********************************************************************
    // private java.lang.String getUserNameFromDN(java.lang.String dn)
    // {
    // java.lang.String attr = null;
    // java.lang.String work = null;
    // boolean hit = false;
    //
    // // break the string at commas (have to be careful here because you
    // // might have values that contain commas. values like that will be
    // // quoted and you'd be able to reconstruct the value so not too much
    // // worry)
    // java.lang.StringTokenizer stk = new java.lang.StringTokenizer(dn, ",");
    //
    // while (stk.hasMoreTokens()) {
    // attr = (java.lang.String) stk.nextToken();
    // work = attr.toLowerCase().trim();
    // // look for the common name attribute
    // if (work.startsWith("cn")) {
    // hit = true;
    // break;
    // }
    // }
    //
    // java.lang.String uname = null;
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

    private static synchronized void initializeStaticValues(Realm realmIn)
    {
    	if (!_initialized)
    	{
        	if (_doDebug) LOG.info(">> >> >>  CentrifugeValve::initializeStaticValues()");

            _isClosedSystem = realmIn instanceof CentrifugeRealm;

            if (_isClosedSystem) {

                _configuration = ((CentrifugeRealm)realmIn).getConfigData();

                if (null != _configuration)
                {
                    try
                    {
                        java.lang.String[] myMethods = _configuration.getAuthenticationOrder().split(",");
                        _authenticationOrder = new AuthorizationMode[myMethods.length];

                        for (int i = 0; _authenticationOrder.length > i; i++) {

                            _authenticationOrder[i] = AuthorizationMode.values()[Integer.decode(myMethods[i])];

                            if (AuthorizationMode.KERBEROS.equals(_authenticationOrder[i])) {

                                _isKerberosSystem = true;
                            }
                        }
                    }
                    catch (Exception myException)
                    {
                        _authenticationOrder = null;
                    }
                    if ((null == _authenticationOrder) || (0 == _authenticationOrder.length))
                    {
                       LOG.error("Could not determine authentication methods");
                    }
                    _userHeader = _configuration.getUserHeaderKey();
                    logDebug(_configuration.getDoDebug());
                    _initialized = true;
                }
            }

            if (_doDebug) LOG.info("<< << <<  CentrifugeValve::initializeStaticValues() -->> " + display(_userHeader));
    	}
    }
    
    private static java.lang.String display(java.lang.String stringIn)
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

    private static java.lang.String display(boolean valueIn)
    {
    	if (valueIn)
    	{
    		return "true";
    	}
    	else
    	{
    		return "false";
    	}
    }

    private boolean okToProceed(Request requestIn, Response responseIn) throws IOException{

        java.security.Principal myPrincipal = requestIn.getUserPrincipal();
        boolean myOk = !(_isKerberosSystem && (null == myPrincipal));

        if (!myOk) {

            String myHeader = getHeader(requestIn, "authorization"); // case-insensitive
            String myTicket = ((null != myHeader) && "negotiate ".equals(myHeader.substring(0, 10).toLowerCase()))
                                ? myHeader.substring(10) : null;

            if (null != myTicket) {

                try {

                    byte[] myBase64Array = myTicket.getBytes("UTF-8");
                    _kerberosTicket = Base64.decodeBase64(myBase64Array);

                    if (_fix8point4bug) {
                        SpnegoTokenFixer.fix(_kerberosTicket);
                    }

                    myOk = (null != _kerberosTicket) && (0 < _kerberosTicket.length);

                } catch (Exception myException) {

                   LOG.error("Caught exception decoding KERBEROS ticket: " + display(myException.getMessage()));
                }
            }

            if (!myOk) {

                if (_doDebug) LOG.info("           - KERBEROS ticket requested.");

                responseIn.addHeader("WWW-Authenticate", "Negotiate");
                responseIn.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        return myOk;
    }

    /*
    * The following code is copied in its entirety from the Tomcat SpnegoAuthenticator !!
    */

    /**
     * This class implements a hack around an incompatibility between the
     * SPNEGO implementation in Windows and the SPNEGO implementation in Java 8
     * update 40 onwards. It was introduced by the change to fix this bug:
     * https://bugs.openjdk.java.net/browse/JDK-8048194
     * (note: the change applied is not the one suggested in the bug report)
     * <p>
     * It is not clear to me if Windows, Java or Tomcat is at fault here. I
     * think it is Java but I could be wrong.
     * <p>
     * This hack works by re-ordering the list of mechTypes in the NegTokenInit
     * token.
     */
    private static class SpnegoTokenFixer {

        public static void fix(byte[] token) {
            SpnegoTokenFixer fixer = new SpnegoTokenFixer(token);
            fixer.fix();
        }


        private final byte[] token;
        private int pos = 0;


        private SpnegoTokenFixer(byte[] token) {
            this.token = token;
        }


        // Fixes the token in-place
        private void fix() {
            /*
             * Useful references:
             * http://tools.ietf.org/html/rfc4121#page-5
             * http://tools.ietf.org/html/rfc2743#page-81
             * https://msdn.microsoft.com/en-us/library/ms995330.aspx
             */

            // Scan until we find the mech types list. If we find anything
            // unexpected, abort the fix process.
            if (!tag(0x60)) return;
            if (!length()) return;
            if (!oid("1.3.6.1.5.5.2")) return;
            if (!tag(0xa0)) return;
            if (!length()) return;
            if (!tag(0x30)) return;
            if (!length()) return;
            if (!tag(0xa0)) return;
            lengthAsInt();
            if (!tag(0x30)) return;
            // Now at the start of the mechType list.
            // Read the mechTypes into an ordered set
            int mechTypesLen = lengthAsInt();
            int mechTypesStart = pos;
            LinkedHashMap<String, int[]> mechTypeEntries = new LinkedHashMap<String, int[]>();
            while (pos < mechTypesStart + mechTypesLen) {
                int[] value = new int[2];
                value[0] = pos;
                String key = oidAsString();
                value[1] = pos - value[0];
                mechTypeEntries.put(key, value);
            }
            // Now construct the re-ordered mechType list
            byte[] replacement = new byte[mechTypesLen];
            int replacementPos = 0;

            int[] first = mechTypeEntries.remove("1.2.840.113554.1.2.2");
            if (first != null) {
                System.arraycopy(token, first[0], replacement, replacementPos, first[1]);
                replacementPos += first[1];
            }
            for (int[] markers : mechTypeEntries.values()) {
                System.arraycopy(token, markers[0], replacement, replacementPos, markers[1]);
                replacementPos += markers[1];
            }

            // Finally, replace the original mechType list with the re-ordered
            // one.
            System.arraycopy(replacement, 0, token, mechTypesStart, mechTypesLen);
        }


        private boolean tag(int expected) {
            return (token[pos++] & 0xFF) == expected;
        }


        private boolean length() {
            // No need to retain the length - just need to consume it and make
            // sure it is valid.
            int len = lengthAsInt();
            return pos + len == token.length;
        }


        private int lengthAsInt() {
            int len = token[pos++] & 0xFF;
            if (len > 127) {
                int bytes = len - 128;
                len = 0;
                for (int i = 0; i < bytes; i++) {
                    len = len << 8;
                    len = len + (token[pos++] & 0xff);
                }
            }
            return len;
        }


        private boolean oid(String expected) {
            return expected.equals(oidAsString());
        }


        private String oidAsString() {
            if (!tag(0x06)) return null;
            StringBuilder result = new StringBuilder();
            int len = lengthAsInt();
            // First byte is special case
            int v = token[pos++] & 0xFF;
            int c2 = v % 40;
            int c1 = (v - c2) / 40;
            result.append(c1);
            result.append('.');
            result.append(c2);
            int c = 0;
            boolean write = false;
            for (int i = 1; i < len; i++) {
                int b = token[pos++] & 0xFF;
                if (b > 127) {
                    b -= 128;
                } else {
                    write = true;
                }
                c = c << 7;
                c += b;
                if (write) {
                    result.append('.');
                    result.append(c);
                    c = 0;
                    write = false;
                }
            }
            return result.toString();
        }
    }
}
