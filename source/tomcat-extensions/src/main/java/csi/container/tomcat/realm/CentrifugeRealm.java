package csi.container.tomcat.realm;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.JAASCallbackHandler;
import org.apache.catalina.realm.JAASRealm;
import org.apache.catalina.realm.MessageDigestCredentialHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import csi.container.tomcat.ConfigResponse;
import csi.security.jaas.JAASPrincipal;
import csi.security.jaas.spi.AuthorizationMode;
import csi.security.jaas.spi.callback.AuthorizationCallback;

public class CentrifugeRealm extends JAASRealm {
   private static final Logger LOG = LogManager.getLogger(CentrifugeRealm.class);

    public class KerberosResult
    {
        private java.security.Principal principal;
        private byte[] kerberosTicket;

        public KerberosResult(java.security.Principal principalIn, byte[] kerberosTicketIn) {

            principal = principalIn;
            kerberosTicket = kerberosTicketIn;
        }

        public java.security.Principal getPrincipal() {

            return principal;
        }

        public byte[] getKerberosTicket() {

            return kerberosTicket;
        }
    }
    class TicketContents
    {
        byte[] token;
        String clientName;
    }

    private static ConfigResponse _configuration = null;
	private static boolean _doDebug = false;

    private static String _kerberosKeyTab = null;
    private static String _kerberosPrincipal = null;
    private static boolean _kerberosUseDomainName = false;
    private static Map<String, String> _kerberosConfig = null;

    private static Configuration getServerJAAS() {
        return new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String nameIn) {

                if ( null == _kerberosConfig) {

                    _kerberosConfig = new HashMap<String, String>();

                    _kerberosConfig.put("principal", _kerberosPrincipal);
                    _kerberosConfig.put("keyTab", _kerberosKeyTab);
                    _kerberosConfig.put("doNotPrompt", "true");
                    _kerberosConfig.put("useKeyTab", "true");
                    _kerberosConfig.put("storeKey", "true");
                    _kerberosConfig.put("isInitiator", "false");
                    _kerberosConfig.put("refreshKrb5Config", "false");
                    _kerberosConfig.put("debug", (_doDebug) ? "true" : "false");
                }
                return new AppConfigurationEntry[] {
                                new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                                                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                                    _kerberosConfig)
                };
            }
        };
    }

    public CentrifugeRealm()
    {
        super();
        MessageDigestCredentialHandler credentialHandler = new MessageDigestCredentialHandler();
        try {
            credentialHandler.setAlgorithm("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        credentialHandler.setSaltLength(0);
        setCredentialHandler(credentialHandler);
        
    	if (_doDebug) LOG.info("<> <> <>  CentrifugeRealm::CentrifugeRealm()");

    }
    
    public static void logDebug(boolean doDebugIn)
    {
    	if (doDebugIn)
    	{
        	_doDebug = true;
        	
        	if (_doDebug) LOG.info("<> <> <>  CentrifugeRealm::logDebug(true)");
    	}
    	else
    	{
        	_doDebug = false;
    	}
    }

    public ConfigResponse getConfigData()
    {
    	java.lang.String myString = null;
    	
    	
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeRealm::tojava.lang.String() -- retrieve header logon key.");

        try
        {
            LoginContext loginContext = null;
            if (appName == null)
                appName = "Tomcat";
            ClassLoader ocl = null;

            // What if the LoginModule is in the container class loader ?
            if (!isUseContextClassLoader())
            {
                ocl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            }

            // get login context
            try
            {
                CentrifugeCallbackHandler myCallbackHandler = new CentrifugeCallbackHandler(this, AuthorizationMode.INFO);
                loginContext = new LoginContext(appName, myCallbackHandler);

                if (null != loginContext)
                {
                	try
                	{
	                    // defer to login module
	                    loginContext.login();
                	}
                    catch (FailedLoginException myException)
                    {
                    	// IGNORE -- used to avoid any login processing that may occur
                    }

                    _configuration = myCallbackHandler.getConfigResponse();
                    _kerberosKeyTab = _configuration.getKerberosKeyTab();
                    _kerberosPrincipal = _configuration.getKerberosPrincipal();
                    _kerberosUseDomainName = _configuration.getKerberosUseDomainName();
                }
            }
            catch (Throwable myException)
            {
            	if (_doDebug) LOG.info("           - caught exception creating login context:" + display(myException));
            }
            finally
            {
                if (!isUseContextClassLoader())
                {
                    Thread.currentThread().setContextClassLoader(ocl);
                }
            }
        }
        catch (Exception myException)
        {
        	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
        }
    	
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeRealm::tojava.lang.String() -->> " + display(myString));

        _doDebug = false;

    	return _configuration;
    }

    @Override
    // override default authenticate so suppress login exception stacktrace when user
    // authentication fails.
    public Principal authenticate(java.lang.String usernameIn, java.lang.String credentialsIn)
    {
        return authenticate(usernameIn, credentialsIn, AuthorizationMode.FORM);
    }

    private Principal authenticate(java.lang.String usernameIn, java.lang.String credentialsIn, AuthorizationMode modeIn)
    {
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeRealm::authenticate("
    					+ display(usernameIn) + ", " + display(credentialsIn, "*raw-password*") + ", "
                        + display((null != modeIn) ? modeIn.name() : (java.lang.String)null) + ")");

        Principal myPrincipal = null;
        
        // Prevent problems with org.apache.catalina.realm.RealmBase
        // by removing null pointers
        String myUsername = (null != usernameIn) ? usernameIn : "";
        String myCredentials = (null != credentialsIn) ? credentialsIn : "";

        try
        {
            CentrifugeCallbackHandler myCallbackHandler = null;
            LoginContext myContext = null;
            Subject mySubject = null;
            if (appName == null)
                appName = "Tomcat";
            ClassLoader myLoader = null;

            // What if the LoginModule is in the container class loader ?
            if (!isUseContextClassLoader())
            {
                myLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            }

            // get login context
            try
            {
                myCallbackHandler = new CentrifugeCallbackHandler(this, myUsername, myCredentials, modeIn);
                myContext = new LoginContext(appName, myCallbackHandler);
            }
            catch (Throwable myException)
            {
            	if (_doDebug) LOG.info("           - caught exception creating login context:" + display(myException));
            }
            finally
            {
                if (!isUseContextClassLoader())
                {
                    Thread.currentThread().setContextClassLoader(myLoader);
                }
            }

            if (null != myContext)
            {
                // defer to login module
                myContext.login();

                if (null != myContext.getSubject())
                {
                    myPrincipal = createPrincipal(myCallbackHandler, myContext);
                }
            }
        }
        catch (FailedLoginException myException)
        {
        	if (_doDebug) LOG.info("           - caught failed login exception:" + display(myException));
        }
        catch (Exception myException)
        {
        	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
        }
    	
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeRealm::authenticate("
                + display(myUsername) + ", " + display(myCredentials, "*raw-password*") + ") -->> "
    					+ ((null != myPrincipal) ? "succeeded" : "failed"));
    	
        return myPrincipal;
    }

    public Principal authenticateDefault()
    {
        java.security.Principal principal = null;

        if (_doDebug) LOG.info(">> >> >>  CentrifugeRealm::authenticateDefault()");

        principal = authenticate(null, null, AuthorizationMode.DEFAULT);

        if (_doDebug) LOG.info("<< << <<  CentrifugeRealm::authenticateDefault() -->> "
                + ((null != principal) ? "succeeded" : "failed"));

        return (principal);
    }

    public KerberosResult authenticateKerberos(final byte[] ticketIn)
    {
        KerberosResult myResult = null;
        LoginContext myContext = null;

        if (_doDebug) LOG.info(">> >> >>  CentrifugeRealm::authenticateKerberos()");

        if (_doDebug) LOG.info("           - oricessing ticket: " + display(ticketIn.toString()));
        try
        {
            if (appName == null)
                appName = "Tomcat";
            ClassLoader myLoader = null;

            // What if the LoginModule is in the container class loader ?
            if (!isUseContextClassLoader())
            {
                myLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            }

            // get login context
            try {
                JAASCallbackHandler myCallback = new JAASCallbackHandler(this, _kerberosPrincipal, "");
                Principal myAuthority = new KerberosPrincipal(_kerberosPrincipal, KerberosPrincipal.KRB_NT_SRV_INST);
                Set<Principal> myPrincipals = new HashSet<Principal>();
                myPrincipals.add(myAuthority);

                Subject myValidatingSubject = new Subject(false, myPrincipals, new HashSet<Object>(), new HashSet<Object>());

                myContext = new LoginContext("Server", myValidatingSubject, myCallback, getServerJAAS());
                myContext.login();

                TicketContents myContents = Subject.doAs(myValidatingSubject, new PrivilegedAction<TicketContents>() {

                    public TicketContents run() {

                        try {

                            final Oid myOid = new Oid("1.3.6.1.5.5.2");

                            TicketContents myContents = new TicketContents();
                            GSSManager myManager = GSSManager.getInstance();
                            GSSName myServiceName = myManager.createName(_kerberosPrincipal, GSSName.NT_USER_NAME);
                            GSSCredential myServiceCredentials = myManager.createCredential(myServiceName,
                                                                                    GSSCredential.INDEFINITE_LIFETIME,
                                                                                    myOid, GSSCredential.ACCEPT_ONLY);
                            GSSContext myContext = myManager.createContext(myServiceCredentials);

                            myContents.token = myContext.acceptSecContext(ticketIn, 0, ticketIn.length);
                            myContents.clientName = myContext.getSrcName().toString();

                            return myContents;
                        }
                        catch (Exception myException) {

                           LOG.error("Caught exception establishing KERBEROS context: " + display(myException));
                            return null;
                        }
                    }
                });

                if (null != myContents) {

                    byte[] myToken = myContents.token;
                    String myUsername = _kerberosUseDomainName
                                            ? myContents.clientName
                                            : myContents.clientName.substring(0, myContents.clientName.lastIndexOf('@'));

                    if ((null != myToken) && (null != myUsername)
                            && (0 < myToken.length) && (0 < myUsername.length())) {

                        if (_doDebug) LOG.info("           - found client:" + display(myUsername));
                        if (_doDebug) LOG.info("           - returning token:" + display(myToken.toString()));

                        CentrifugeCallbackHandler myCallbackHandler = new CentrifugeCallbackHandler(this, myUsername,
                                                                                    null, AuthorizationMode.KERBEROS);
                        LoginContext myLoginContext = new LoginContext(appName, myCallbackHandler);

                        if (null != myLoginContext)
                        {
                            // defer to login module
                            myLoginContext.login();

                            if (null != myLoginContext.getSubject())
                            {
                                Principal myPrincipal = createPrincipal(myCallbackHandler, myLoginContext);
                                myResult = new KerberosResult(myPrincipal, myToken);
                            }
                        }
                    }
                }
            }
            catch (FailedLoginException myException)
            {
                if (_doDebug) LOG.info("           - caught failed login exception:" + display(myException));
            }
            catch (Exception myException)
            {
                if (_doDebug) LOG.info("           - caught exception:" + display(myException));
            }
            finally
            {
                if (!isUseContextClassLoader())
                {
                    Thread.currentThread().setContextClassLoader(myLoader);
                }
            }
        }
        catch (Exception myException)
        {
            if (_doDebug) LOG.info("           - caught exception:" + display(myException));
        }
        if (_doDebug) LOG.info("<< << <<  CentrifugeRealm::authenticateKerberos() -->> "
                                + ((null != myResult) ? "succeeded" : "failed"));

        return myResult;
    }

    public java.security.Principal autoAuthenticate(java.lang.String usernameIn)
    {
        java.security.Principal principal = null;

        if (_doDebug) LOG.info(">> >> >>  CentrifugeRealm::autoAuthenticate()");

        principal = authenticate(usernameIn, null, AuthorizationMode.NONE);

        if (_doDebug) LOG.info("<< << <<  CentrifugeRealm::autoAuthenticate() -->> "
                + ((null != principal) ? "succeeded" : "failed"));

        return (principal);
    }

    @Override
    public java.security.Principal authenticate(X509Certificate[] certs)
    {
        java.security.Principal principal = null;
    	
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeRealm::authenticate(X509Certificate[] certs)");
    	
        if (null != certs)
        {
	        if (0 < certs.length)
	        {
		        // Establish a LoginContext to use for authentication
		        try
		        {
		            LoginContext myLoginContext = null;
		            if (appName == null)
		            {
		                appName = "Tomcat";
		            }
		
		
		            // What if the LoginModule is in the container class loader ?
		            ClassLoader ocl = null;
		
		            if (!isUseContextClassLoader())
		            {
		                ocl = Thread.currentThread().getContextClassLoader();
		                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		            }
		
		            try
		            {
		                CentrifugeCallbackHandler myCallbackHandler = new CentrifugeCallbackHandler(this, "", "", AuthorizationMode.CERT);
		                myCallbackHandler.setChain(certs);
		                myLoginContext = new LoginContext(appName, myCallbackHandler);

			            if (null != myLoginContext)
			            {
			                // Negotiate a login via this LoginContext
			                Subject mySubject = null;
			                try
			                {
			                	myLoginContext.login();
			                	mySubject = myLoginContext.getSubject();
			        			
				                if (null != mySubject)
				                {
				                	java.lang.String myUsername = myCallbackHandler.getUserName();

                                    if (null != myLoginContext.getSubject())
                                    {
                                        Principal myPrincipal = createPrincipal(myCallbackHandler, myLoginContext);
                                    }
				                }
			                }
			                catch (AccountExpiredException myException)
			                {
			                	if (_doDebug) LOG.info("           - caught account expired exception:" + display(myException));
			                }
			                catch (CredentialExpiredException myException)
			                {
			                	if (_doDebug) LOG.info("           - caught credential expired exception:" + display(myException));
			                }
			                catch (FailedLoginException myException)
			                {
			                	if (_doDebug) LOG.info("           - caught failed login exception:" + display(myException));
			                }
			                catch (LoginException myException)
			                {
			                	if (_doDebug) LOG.info("           - caught login exception:" + display(myException));
			                }
			                catch (Throwable myException)
			                {
			                	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
			                }
			            }
		            }
		            catch (Throwable myException)
		            {
		            	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
		            }
		            finally
		            {
		                if (!isUseContextClassLoader())
		                {
		                    Thread.currentThread().setContextClassLoader(ocl);
		                }
		            }
		
		        }
		        catch (Throwable myException)
		        {
	            	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
		        }
	        }
        }
    	
    	if (_doDebug) LOG.info("<< << <<  CentrifugeRealm::authenticate(X509Certificate[] certs) -->> "
    					+ ((null != principal) ? "succeeded" : "failed"));

        return (principal);
    }

    private static java.lang.String display(java.lang.String stringIn, java.lang.String replacementIn)
    {
    	if ((null != stringIn) && (0 < stringIn.length()))
    	{
    		if (null != replacementIn)
    		{
        		return replacementIn;
    		}
    		else
    		{
        		return "\"" + stringIn + "\"";
    		}
    	}
    	else
    	{
    		return "<null>";
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

    private static java.lang.String display(int valueIn)
    {
        return Integer.toString(valueIn);
    }

    private static java.lang.String display(Throwable exceptionIn)
    {
    	if ((null != exceptionIn) && (null != exceptionIn.getMessage())
    			&& (0 < exceptionIn.getMessage().length()))
    	{
    		return "\n" + exceptionIn.getMessage();
    	}
    	else
    	{
    		return "<null>";
    	}
    }

    private Principal createPrincipal(CentrifugeCallbackHandler callbackHandlerIn, LoginContext loginContextIn) {

        try {

            AuthorizationCallback myAuthorization = new AuthorizationCallback();
            callbackHandlerIn.handle(new Callback[] {myAuthorization});
            String myUserName = myAuthorization.getUsername();
            String myDistinguishedName = myAuthorization.getDN();
            List<java.lang.String> myRoles = extractRoleNames(myUserName, loginContextIn.getSubject());
            JAASPrincipal myPrincipal = new JAASPrincipal(myUserName, myDistinguishedName, myRoles);

            return new GenericPrincipal(myUserName, null, myRoles, myPrincipal, loginContextIn);

        } catch (Exception myException) {

        }
        return null;
    }

    private List<java.lang.String> extractRoleNames(java.lang.String userNameIn, Subject subjectIn) {

        Set<Principal> myPrincipals = (null != subjectIn) ? subjectIn.getPrincipals() : null;

        if ((null != myPrincipals) && (0 < myPrincipals.size())) {

            Set<java.lang.String> myRoles = new TreeSet<String>();

            myRoles.add(userNameIn);
            for (Principal myRole : myPrincipals) {

                myRoles.add(myRole.getName());
            }
            return new ArrayList(myRoles);
        }
        return null;
    }
}
