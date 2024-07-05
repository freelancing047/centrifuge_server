package csi.security.jaas.spi;


import csi.security.jaas.LogThreadContextUtil;
import csi.security.jaas.spi.callback.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * This class provides a standard JDBC LoginModule implementation for JAAS. This implemenation also
 * enables the use of groups.
 * <p>
 * By specifying the group related properties, this class performs a recursive search resolving out
 * hierarchical groups (e.g. groups of groups). The search procedure takes into account potential
 * situations where circular references between groups exist.
 * <p>
 * Upon successful authentication the JAAS subject is populated with the following information:
 * <ul>
 * <li>JAASPrincipal with the name equal to the name provided via a NameCallback.
 * <li>Set of JAASRoles where the name is comes from all known roles (Roles Query) and all resolved
 * group names to which the user belongs
 * <li>A private credential obtained from the PasswordCallback
 * </ul>
 *
 * @author Centrifuge Systems, Inc.
 *
 */
//@SuppressWarnings( { "all", "unchecked", "JavaDoc", "allJavadoc", "unused", "ForLoopReplaceableByForEach", "WhileLoopReplaceableByForEach", "SimplifiableConditionalExpression",
//        "StringEquality", "ThrowableResultOfMethodCallIgnored" })
public class CentrifugeAuthenticationModule extends SimpleLoginModule {
   private static final Logger LOG = LogManager.getLogger(CentrifugeAuthenticationModule.class);

    class UserInfo
    {
        String name = null;
        String password = null;
        String ldapName = null;
        String distinguishedName = null;
    }

    class StringPair
    {
        String valueOne;
        String valueTwo;

        public StringPair(String valueOneIn, String valueTwoIn)
        {
            valueOne = valueOneIn;
            valueTwo = valueTwoIn;
        }
    }
/*
    class TicketContents
    {
        String clientName;
    }
*/
    private static final String DN_SPECIAL_CHARACTERS = ",=#+;<>\\";
    private static final String DN_TOKEN = "\\{DN\\}";
    private static final String USER_TOKEN = "\\{USERNAME\\}";

    // Property values for all authentication methods
    private static final String PROPERTY_AUTHENTICATION = "authentication";
    private static final String PROPERTY_LOG_FAILURE = "logFailure";
    private static final String PROPERTY_LOG_SUCCESS = "logSuccess";
    private static final String PROPERTY_DEBUG = "debug";

    // Property values for JDBC authentication method
    private static final String PROPERTY_AUTO_AUTHENTICATE = "autoAuthenticate";

    // Property values for service authentication method
    private static final String PROPERTY_SERVICE_ADDRESS = "serviceAddress";

    // Property values for CERT authentication method
    private static final String PROPERTY_CERT_USE_FULL_DN = "certUseDN";
    private static final String PROPERTY_CERT_USE_LDAP = "certUseLdap";
    private static final String PROPERTY_CERT_GROUP_URL = "certGroupUrl";
    private static final String PROPERTY_CERT_GROUP_TAG = "certGroupTag";
    private static final String PROPERTY_CERT_VALID_GROUPS = "certValidGroups";
    private static final String PROPERTY_CERT_EXTRACT_USER = "certExtractUser";

    // Property values for SAML authentication method
    private static final String PROPERTY_SAML_USE_LDAP = "samlUseLdap";
    private static final String PROPERTY_SAML_GROUP_URL = "samlGroupUrl";
    private static final String PROPERTY_SAML_GROUP_TAG = "samlGroupTag";
    private static final String PROPERTY_SAML_VALID_GROUPS = "samlValidGroups";

    // Property values for Kerberos authentication method
    private static final String PROPERTY_KERBEROS_PRINCIPAL = "kerberosPrincipal";
    private static final String PROPERTY_KERBEROS_USE_DOMAIN_NAME = "kerberosUseDomainName";
    private static final String PROPERTY_KERBEROS_KEY_TAB = "kerberosKeyTab";
    private static final String PROPERTY_KERBEROS_USE_LDAP = "kerberosUseLdap";
    private static final String PROPERTY_KERBEROS_GROUP_URL = "kerberosGroupUrl";
    private static final String PROPERTY_KERBEROS_GROUP_TAG = "kerberosGroupTag";
    private static final String PROPERTY_KERBEROS_VALID_GROUPS = "kerberosValidGroups";

    // Property values for NONE authentication method
    private static final String PROPERTY_USERNAME_HEADER_KEY = "userHeaderKey";

    // Property values for LDAP authentication method
    private static final String PROPERTY_LDAP_CONNECTION= "ldapConnection";
    private static final String PROPERTY_LDAP_USE_FULL_DN = "ldapUseDN";
    // SSL support
    private static final String PROPERTY_LDAP_KEY_FILE_KEY = "ldapKeyFile";
    private static final String PROPERTY_LDAP_KEY_TYPE_KEY = "ldapKeyFileType";
    private static final String PROPERTY_LDAP_KEY_PASSWORD_KEY = "ldapKeyPassword";
    private static final String PROPERTY_LDAP_TRUST_FILE_KEY = "ldapTrustFile";
    private static final String PROPERTY_LDAP_TRUST_TYPE_KEY = "ldapTrustFileType";
    private static final String PROPERTY_LDAP_TRUST_PASSWORD_KEY = "ldapTrustPassword";
    private static final String PROPERTY_EXTENDED_DEBUG = "ldapExtendedDebug";
    private static final String PROPERTY_TRUST_LDAP_SERVER_KEY = "ldapTrustServer.DEBUG_ONLY";
    // Simple logon
    private static final String PROPERTY_LDAP_USER_DN = "ldapUserDn";
    // LDAP search
    private static final String PROPERTY_LDAP_SEARCH_CONNECTION = "ldapSearchConnection";
    private static final String PROPERTY_LDAP_READER = "ldapReader";
    private static final String PROPERTY_LDAP_PASSWORD = "ldapPassword";
    private static final String PROPERTY_LDAP_BASE = "ldapBase";
    private static final String PROPERTY_LDAP_IDENTIFIER = "ldapIdentifier";
    private static final String PROPERTY_LDAP_CLASS = "ldapClass";
    private static final String PROPERTY_LDAP_GROUP_DN = "ldapGroup";
    // Extended group membership support
    private static final String PROPERTY_CHECK_GROUP_SIDS = "ldapCheckGroupSIDs";
    private static final String PROPERTY_LDAP_GROUP_BASE = "ldapGroupBase";
    // Flexible support
    private static final String PROPERTY_LDAP_DIGEST = "ldapDigest";
    private static final String PROPERTY_LDAP_GROUP_CLASS = "ldapGroupClass";
    private static final String PROPERTY_LDAP_GROUP_FILTER = "ldapGroupFilter";
    private static final String PROPERTY_LDAP_USER_FILTER = "ldapUserFilter";
    private static final String PROPERTY_LDAP_USER_GROUP_FILTER = "ldapUserGroupFilter";

    private static final String OBJECT_SID = "objectSid";
    private static final String TOKEN_GROUPS = "tokenGroups";
    private static final String GROUP_MEMBER = "memberOf";

    private static final String DEFAULT_USER = "defaultUser";

    private static final String _dividerString = "\n==================================================================================\n";

	private static final Map<String, Integer> _authenticationMap;

    static {
        // Provide a map for all possible authorization requests except for INFO which is a pseudo-request
        _authenticationMap = new HashMap<String, Integer>();
        for (AuthorizationMode myMode : AuthorizationMode.values()) {

            _authenticationMap.put(myMode.name().toLowerCase(), myMode.ordinal());
        }
    }

        // Values set only once during first call to "initialize".
    private static Map<String, ?> _options = null;
    private static String[] _ldapGroup = null;
    private static String[] _ldapClass = null;
    private static String _ldapConnection = null;
    private static String _ldapSearchConnection = null;
    private static String _ldapReader = null;
    private static String _ldapPassword = null;
    private static String _ldapUserDn = null;
    private static String _ldapDigest = null;
    private static String _ldapBase = null;
    private static String _ldapGroupBase = null;
    private static String _groupClass = null;
    private static String _groupFilter = null;
    private static String _userFilter = null;
    private static String _userGroupFilter = null;
    private static String _ldapIdentifier = null;
    private static String _userHeaderKey = "user_name";
    private static String _dataSourceName = "java:comp/env/jdbc/MetaDB";
    private static String _groupQuery = "select group_name from GroupMembershipView where role_name=?";
    private static String _baseUserQuery = "select password from UsersView where name=?";
    private static String _ldapKeyFile = "conf/keystore.kdb";
    private static String _ldapKeyType = "jks";
    private static String _ldapKeyPassword = "changeit";
    private static String _ldapTrustFile = "conf/ldaptrust.jks";
    private static String _ldapTrustType = "jks";
    private static String _ldapTrustPassword = "changeit";
    private static String _defaultUserName = null;
    private static String _serviceAddress = null;

    private static boolean _certUseLdap = false;
    private static boolean _certUseGroupService = false;
    private static String _certGroupUrl = null;
    private static String _certGroupTag = null;
    private static String _certExtractUser = null;
    private static List<String> _certValidGroups = null;

    private static String _kerberosPrincipal = null;
    private static boolean _kerberosUseDomainName = false;
    private static String _kerberosKeyTab = null;
    private static boolean _kerberosUseLdap = false;
    private static boolean _kerberosUseGroupService = false;
    private static String _kerberosGroupUrl = null;
    private static String _kerberosGroupTag = null;
    private static List<String> _kerberosValidGroups = null;

    private static boolean _samlUseLdap = false;
    private static boolean _samlUseGroupService = false;
    private static String _samlGroupUrl = null;
    private static String _samlGroupTag = null;
    private static List<String> _samlValidGroups = null;

    private static byte[][] _groupSID = null;
    private static String _authenticationOrder = null;
    private static Integer _authenticationMethod = null;
    private static boolean _autoAuthenticate = false;
    private static boolean _doDebug = false;
    private static boolean _doExtendedDebug = false;
    private static boolean _logFailure = false;
    private static boolean _logSuccess = false;
    private static boolean _staticValuesSet = false;
    private static boolean _useClass = false;
    private static boolean _useGroup = false;
    private static boolean _useAnonymous = false;
    private static boolean _useDN = false;
    private static boolean _doDigest = false;
    private static boolean _useQuickSearch = true;
    private static boolean _checkGroupSIDs = false;
    private static boolean _useCertFullDN = false;
    private static boolean _useLdapFullDN = false;
    private static boolean _useLdapSSL = false;
    private static boolean _trustLdapServer = false;
    private static boolean _useReaderCert = false;
    private static boolean _useSearchTLS = false;
    private static boolean _doEscape = true;
    private static boolean _tryJDBC = false;

// Oid krb5Oid = new Oid( "1.2.840.113554.1.2.2");

    // Working values for current object
    private CallbackHandler _callbackHandler = null;
    private UserInfo _currentUser = null;
    private String _userQuery = null;
    private DataSource _dataSource = null;
    private boolean _authenticated = false;

    private AuthorizationMode _mode = null;

    private Subject _subject = null;
    private Map<String, ?> _sharedState = null;
    private List<String> _testedDNs = null;
    private String _logonName = "";

    public static <T> T lookupResource(Class<T> classIn, String resultUrlIn) throws NamingException
    {

    	if (_doDebug) LOG.info("<> <> <>  CentrifugeAuthenticationModule::lookupResource(Class<T> classIn, " + display(resultUrlIn) + ")");

        Context myInitCtx = null;
        try
        {
            myInitCtx = new InitialContext();
            Object myObject = myInitCtx.lookup(resultUrlIn);
            return (T) myObject;
        }
        finally
        {
        	close(myInitCtx);
        }
    }

    public static boolean matchSID(byte[][] groupSIDsIn, byte[] sidIn)
    {
    	boolean mySuccess = false;

  		if (_doDebug) LOG.info("           - Testing user group SID = " + display(sidIn));

    	if (null != groupSIDsIn)
    	{
    		for (int i = 0; groupSIDsIn.length > i; i++)
    		{
    			byte[] myTest = groupSIDsIn[i];
    			int myLimit = myTest.length;

    	  		if (_doDebug) LOG.info("             - Testing against group SID = " + display(myTest));


    			if (sidIn.length == myLimit)
    			{
    				int myByte;
    				for (myByte = 0; myLimit > myByte; myByte++)
    				{
    					if (sidIn[myByte] != myTest[myByte])
    					{
    						break;
    					}
    				}
    				if (myByte == myLimit)
    				{
    					mySuccess = true;
    					break;
    				}
    			}
    		}
    	}

  		if (_doDebug) LOG.info("           - " + ((mySuccess) ? "" : "no ") + "match found.");

		return mySuccess;
	}

    public static String createFilterFromList(String tokenIn, String[] listIn)
    {
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::createFilterFromList(" + display(tokenIn) + ", String[] listIn)");

    	String myString = "";

    	if ((null != tokenIn) && (null != listIn))
    	{
        	if (1 < listIn.length)
        	{
	    		StringBuilder myBuffer = new StringBuilder("(|");

	    		for (int i = 0; listIn.length > i; i++)
	    		{
	    			if (_doEscape)
	    			{
		    			myBuffer.append("(" + tokenIn + "=" + escapeLdapIdentifier(listIn[i]) + ")");
	    			}
	    			else
	    			{
		    			myBuffer.append("(" + tokenIn + "=" + listIn[i] + ")");
	    			}
	    		}
	    		myBuffer.append(")");
	    		myString = myBuffer.toString();
        	}
        	else if (_doEscape)
        	{
	    		myString = "(" + tokenIn + "=" + escapeLdapIdentifier(listIn[0]) + ")";
	    	}
	    	else
	    	{
	    		myString = "(" + tokenIn + "=" + listIn[0] + ")";
	    	}
    	}

    	if (_doDebug) LOG.info("           -- filter:\"" + myString + "\"");

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::createFilterFromList(" + display(tokenIn) + ", String[] listIn) -->> " + display(myString));

    	return myString;
    }

    public void initialize(Subject subjectIn, CallbackHandler callbackHandlerIn, Map<String, ?> sharedStateIn, Map<String, ?> optionsIn)
    {
        if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::initialize()");

        _subject = subjectIn;
        _callbackHandler = callbackHandlerIn;
        _sharedState = sharedStateIn;

        if (!_staticValuesSet) setStaticValues(optionsIn);

        try
        {
            _dataSource = lookupResource(DataSource.class, _dataSourceName);
            if (null == _dataSource)
            {
                throw new IllegalStateException("Failed to initialize CentrifugeAuthenticationModule. Found null datasource bound to name: " + _dataSourceName);
            }
        }
        catch (NamingException myException)
        {
            throw new IllegalStateException("Failed to initialize CentrifugeAuthenticationModule.  Failed to find datasource with name: " + _dataSourceName, myException);
        }

        if (isUserExpirationEnabled())
        {
            _userQuery = _baseUserQuery + " AND (perpetual is null OR perpetual=true OR (perpetual=false AND expirationdate > CURRENT_TIMESTAMP ))";
        }
        else
        {
        	_userQuery = _baseUserQuery;
        }

        if (_doDebug)
        {
            LOG.info("           - userQuery: " + display(_userQuery));
            LOG.info("           - groupQuery: " + display(_groupQuery));
        }

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::initialize()");

    }

    @Override
    public boolean login() throws LoginException
    {
        if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::login()");

        Callback[] myCallbacks = null;
        boolean myInfoRequest = false;

        _currentUser = new UserInfo();

        if (_authenticated) {

            return true;
        }

        if (null != _callbackHandler)
        {
	        try
	        {
	        	myCallbacks = initializeCallbacks();
	        }
	        catch (Exception myException)
	        {
	            LOG.error("Caught exception preparing callbacks during "
	            			+ display(getUserName()) + " login: " + display(myException));
	            myCallbacks = null;
	        }
        }

        if ((null != myCallbacks) && (null != _authenticationMethod))
        {
        	String myRawPassword = ((LDAPCallback)myCallbacks[CallBackId.LDAP_PASSWORD.getOrdinal()]).getPassCode();

            _logonName = ((NameCallback)myCallbacks[CallBackId.USERNAME.getOrdinal()]).getName();
            _mode = ((AuthorizationCallback)myCallbacks[CallBackId.AUTHORIZE.getOrdinal()]).getMode();

            if (null != _mode) {

                switch (_mode)
                {
                    case KERBEROS:

                        _authenticated = authenticateKerberos(myCallbacks);
                        break;

                    case CERT:

                        _authenticated = authenticateSecure(myCallbacks);
                        break;

                    case FORM:

                        _authenticated = authenticateForm(myCallbacks);
                        break;

                    case NONE:

                        _authenticated = true;
                        break;

                    case DEFAULT:

                        if (null != _defaultUserName) {
                            ((NameCallback) myCallbacks[CallBackId.USERNAME.getOrdinal()]).setName(_defaultUserName);
                            _mode = AuthorizationMode.DEFAULT;
                            _authenticated = true;
                        }
                        break;

                    case INFO:

                        myInfoRequest = true;
                        sendResponse(myCallbacks, myRawPassword);
                        break;
                }
            }
            else if ((null != _logonName) && (0 < _logonName.length()))
            {
                if ((null != myRawPassword) && (0 < myRawPassword.length()))
                {
                    if (AuthorizationMode.LDAP.isSet(_authenticationMethod))
                    {
                        _authenticated = authenticateLDAP(myCallbacks);
                    }
                    if ((!_authenticated)
                            && (AuthorizationMode.JDBC.isSet(_authenticationMethod)))
                    {
                        _authenticated = authenticateJDBC(myCallbacks);
                    }
                }
                else if (AuthorizationMode.NONE.isSet(_authenticationMethod))
                {
                    _mode = AuthorizationMode.NONE;
                    _authenticated = true;
                }
            }
            else if ((null != myRawPassword) && (0 < myRawPassword.length()))
            {
                myInfoRequest = true;
                sendResponse(myCallbacks, myRawPassword);
            }
            else if (AuthorizationMode.CERT.isSet(_authenticationMethod))
            {
                _authenticated = authenticateSecure(myCallbacks);
            }
            else if (AuthorizationMode.DEFAULT.isSet(_authenticationMethod))
            {
                if (null != _defaultUserName)
                {
                    ((NameCallback) myCallbacks[CallBackId.USERNAME.getOrdinal()]).setName(_defaultUserName);
                    _mode = AuthorizationMode.DEFAULT;
                    _authenticated = true;
                }
            }
    	}
        LogThreadContextUtil.putUserName(getUserName());
        if (_authenticated)
        {
            LogThreadContextUtil.putDistinguishedName(getDistinguishedName());
    		if (_doDebug || _logSuccess) LOG.info(" ** Authentication succeeded for user " + display(getUserName()) + " **");
        }
        else
        {
        	if (myInfoRequest)
        	{
        		throw new FailedLoginException("Returning response data.");
        	}
        	else
        	{
            	String myFailure = "!! Authentication failed for user " + display(getUserName()) + " !!";
        		if (_doDebug || _logFailure) LOG.info(myFailure);

            	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::login() -->> " + display(_authenticated));

        		throw new FailedLoginException(myFailure);
        	}
        }
        if ((null != myCallbacks) && (null != _mode))
        {
	        try
	        {
                AuthorizationCallback myAuthorization = ((AuthorizationCallback)myCallbacks[CallBackId.AUTHORIZE.getOrdinal()]);
                myAuthorization.setMode(_mode);
                myAuthorization.setUsername(_currentUser.name);
                myAuthorization.setDN(_currentUser.distinguishedName);
	        	storeCallbackUpdates(myCallbacks);
	        }
	        catch (Exception myException)
	        {
	        	if (null != _currentUser)
	        	{
		            LOG.error("Caught exception storing callback updastes during "
		            			+ display(getUserName()) + " login: " + display(myException));
	        	}
	        	else
	        	{
		            LOG.error("Caught exception storing callback updastes during "
		            			+ " configuration transfer: " + display(myException));
	        	}
	        }
        }

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::login() -->> " + display(_authenticated));

        return _authenticated;
    }

    protected boolean authenticateService(Callback[] callBacksIn) throws LoginException {

//        String query = "http://localhost:4000/userauth";
        String query = _serviceAddress;

        LDAPCallback myLdapCallback = (LDAPCallback) callBacksIn[CallBackId.LDAP_PASSWORD.getOrdinal()];

        _currentUser.name = ((NameCallback)callBacksIn[CallBackId.USERNAME.getOrdinal()]).getName();
        String myRawPassword = myLdapCallback.getPassCode();


        String json = "{ \"username\": " + "\"" + _currentUser.name + "\"" +  ", " + "\"password\": " + "\"" + myRawPassword + "\"" + ", " + "\"product\": " + "\"Centrifuge\"" + "}";

        try {
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();

            String jsonResponseString = "";

            try (InputStream inputStream = conn.getInputStream()) {
                jsonResponseString = readInputStream(inputStream);
            }

            conn.disconnect();

            if (jsonResponseString.contains("decline")) {
                _authenticated = false;
            } else {
                _authenticated = true;
            }

        } catch (Exception myException) {
            if (_doDebug) LOG.info("           - caught exception:" + display(myException));
        }
        return _authenticated;
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
    }


    protected boolean authenticateForm(Callback[] callBacksIn) throws LoginException {

        boolean myOk = false;

        if (AuthorizationMode.LDAP.isSet(_authenticationMethod)) {

            myOk = authenticateLDAP(callBacksIn);
        }
        if ((!_authenticated) &&AuthorizationMode.SERVICE.isSet(_authenticationMethod)) {
            myOk = authenticateService(callBacksIn);

        }
        if ((!_authenticated) && AuthorizationMode.JDBC.isSet(_authenticationMethod)) {

            myOk = authenticateJDBC(callBacksIn);
        }
        return myOk;
    }

    protected boolean authenticateKerberos(Callback[] callBacksIn)
    {
        if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateKerberos(Callback[] callBacksIn)");

        boolean mySuccess = false;

        try
        {
            if (_kerberosUseLdap)
            {
                if (_kerberosUseDomainName) {

                    String myKerberoseName = ((NameCallback)callBacksIn[CallBackId.USERNAME.getOrdinal()]).getName();

                    _currentUser.name = myKerberoseName.substring(0, myKerberoseName.lastIndexOf('@'));

                } else {

                    _currentUser.name = ((NameCallback)callBacksIn[CallBackId.USERNAME.getOrdinal()]).getName();
                }
                LogThreadContextUtil.putUserName(getUserName());
                _currentUser.ldapName = escapeLdapIdentifier(_currentUser.name);
                // Require user to belong to an LDAP group if requested.
                mySuccess = authenticateSearchLDAP(null, _doDigest ? _ldapDigest : "simple", true, false);

            } else if (_kerberosUseGroupService) {

                mySuccess = validateUserGroup(_kerberosGroupUrl, _kerberosGroupTag, _kerberosValidGroups);

            } else {

            mySuccess = true;
            }
        }
        catch (Exception myException)
        {
            if (_doDebug) LOG.info("           - caught exception:" + display(myException));
        }

        if (_doDebug) LOG.info(" << << << CentrifugeAuthenticationModule::authenticateKerberos("
                + display(getUserName()) + ") -->> " + display(mySuccess));

        return mySuccess;
    }

    protected boolean authenticateSecure(Callback[] callBacksIn)
    {
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateSecure(Callback[] callBacksIn)");

    	boolean mySuccess = false;

        try
        {
            CertCallback myCertCallback = (CertCallback) callBacksIn[CallBackId.CERTIFICATE.getOrdinal()];
            X509Certificate[] myPeerChain = myCertCallback.getChain();
            if ((null != myPeerChain) && (0 < myPeerChain.length))
            {
                _currentUser.distinguishedName = myPeerChain[0].getSubjectX500Principal().getName();
                _currentUser.name = getUserNameFromCert(myPeerChain[0]);
                LogThreadContextUtil.putUserName(getUserName());
                if (_certUseLdap)
                {
                    _currentUser.ldapName = escapeLdapIdentifier(_currentUser.name);
                    // Require user to belong to an LDAP group if requested.
                    mySuccess = authenticateSearchLDAP(null, _doDigest ? _ldapDigest : "simple", true, false);

                } else if (_certUseGroupService) {

                    mySuccess = validateUserGroup(_certGroupUrl, _certGroupTag, _certValidGroups);

                } else {

                    mySuccess = true;
                }
                if (mySuccess) {

                    _mode = AuthorizationMode.CERT;
                    LogThreadContextUtil.putUserName(getUserName());
                    myCertCallback.setName(_currentUser.name);
                    myCertCallback.setDistinguishedName(_currentUser.distinguishedName);
                }
            }
        }
        catch (Exception myException)
        {
        	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
        }

    	if (_doDebug) LOG.info(" << << << CentrifugeAuthenticationModule::authenticateSecure("
    							+ display(getUserName()) + ") -->> " + display(mySuccess));
        return mySuccess;
    }

    protected boolean authenticateLDAP(Callback[] callBacksIn)
    {
        if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateLDAP(Callback[] callBacksIn)");

    	boolean mySuccess = false;
    	String myPassword = null;

        try
        {
        	// Partially setup environment for authenticating

        	String myMode;
            LDAPCallback myLdapCallback = (LDAPCallback) callBacksIn[CallBackId.LDAP_PASSWORD.getOrdinal()];

        	_currentUser.name = ((NameCallback)callBacksIn[CallBackId.USERNAME.getOrdinal()]).getName();
        	_currentUser.ldapName = escapeLdapIdentifier(_currentUser.name);
        	_testedDNs = new ArrayList<String>();
            LogThreadContextUtil.putUserName(getUserName());

        	if (_doDigest)
        	{
            	String myRawPassword = myLdapCallback.getPassCode();

            	myMode = _ldapDigest;

            	if ((null != myRawPassword) && (0 < myRawPassword.length()))
            	{
	                MessageDigest myDigest = MessageDigest.getInstance(_ldapDigest);
	                myPassword = display(myDigest.digest(myRawPassword.getBytes()));
	        		myRawPassword = null;
            	}
        	}
        	else
        	{
        		myMode = "simple";
            	myPassword = myLdapCallback.getPassCode();
        	}

        	if ((null != myPassword) && (0 < myPassword.length()))
        	{
        		if (_useDN)
        		{
                	String myFormat = _ldapUserDn;
                	_currentUser.ldapName = myFormat.replaceAll(USER_TOKEN, Matcher.quoteReplacement(_currentUser.ldapName));
        		}
        		if (_useDN && !(_useClass || _useGroup))
            	{
	        		mySuccess = authenticateSimpleLDAP(myPassword, myMode);
	        	}
	        	else if (_useQuickSearch)
	        	{
	        		mySuccess = authenticateSearchLDAP(myPassword, myMode, true, true);
	        	}
            	if ((!mySuccess) && _useGroup)
            	{
                	if (_checkGroupSIDs)
                	{
                		mySuccess = authenticateSearchLDAP(myPassword, myMode, false, true);
    	        	}
	        	}
            	if (mySuccess)
            	{
                   	_mode = AuthorizationMode.LDAP;
                    LogThreadContextUtil.putUserName(getUserName());
                    myLdapCallback.setDistinguishedName(_currentUser.distinguishedName);
            	}
        	}
       	}
        catch (Exception myException)
        {
        	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
        }

    	if (_doDebug) LOG.info(" << << << CentrifugeAuthenticationModule::authenticateLDAP(Callback[] callBacksIn) -->> " + display(mySuccess));

        if (!mySuccess) {

            if ((0 != ((1 << AuthorizationMode.JDBC.ordinal()) & _authenticationMethod))) {

                authenticateJDBC(callBacksIn);
            }
        }

        return mySuccess;
    }

    protected boolean authenticateSimpleLDAP(String passwordIn, String modeIn) throws Exception
    {

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateSimpleLDAP("
    							+ display(getUserName()) + ", "
    							+ display(passwordIn, (_doDigest ? "digested-password" : "*raw-password*")) + ")");

    	boolean mySuccess = false;
    	Hashtable<String, String> myEnvironment = new Hashtable<String, String>();
    	if (_useLdapSSL)
    	{
    		prepareLdapSSL(myEnvironment, false);
    	}
    	myEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    	myEnvironment.put(Context.PROVIDER_URL, _ldapConnection);

    	// Define the LDAP context outside the try-catch structure
    	// so it can be properly closed even if an exception is encountered.
    	LdapContext myAuthContext = null;

    	// Complete environment for authenticating

    	myEnvironment.put(Context.SECURITY_AUTHENTICATION, modeIn);
    	myEnvironment.put(Context.SECURITY_PRINCIPAL, _currentUser.ldapName);
    	myEnvironment.put(Context.SECURITY_CREDENTIALS, passwordIn);

    	try
    	{
	    	if (_doDebug) LOG.info("           - connect to LDAP as " + display(_currentUser.ldapName) + " with " + display(passwordIn, (_doDigest ? "digested-password" : "*raw-password*")));

        	myAuthContext = new InitialLdapContext(myEnvironment, null);

        	if (null != myAuthContext)
        	{
        		// user is authenticated
        		if (_useLdapFullDN)
        		{
        			_currentUser.name = _currentUser.ldapName;
        		}
                LogThreadContextUtil.putUserName(getUserName());
        		mySuccess = true;
        	}
    	}
    	catch (AuthenticationException myException)
    	{
        	// Authentication failed

    		if (_doDebug) LOG.info("           - LDAP authetication failed.");

    	}
    	catch (NamingException myException)
    	{
        	// Attempt to authenticate anonymously encountered

    		if (_doDebug) LOG.info("           - Attempt to authenticate anonymously captured by LDAP.");

    	}
    	catch (Exception myException)
    	{
        	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
    	}
    	finally
    	{
    		// Close the LDAP context if it exists
   			close(myAuthContext);
    	}

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateSimpleLDAP("
				+ display(getUserName()) + ", "
				+ display(passwordIn, (_doDigest ? "digested-password" : "*raw-password*")) + ") -->> " + display(mySuccess));

        return mySuccess;
    }

    protected boolean authenticateSearchLDAP(String passwordIn, String modeIn, boolean prefilterIn, boolean authenticateIn) throws Exception
    {

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateSearchLDAP("
								+ display(getUserName()) + ", "
    							+ display(passwordIn, (_doDigest ? "digested-password" : "*raw-password*")) + ")");

    	boolean mySuccess = false;
    	Hashtable<String, String> myEnvironment = new Hashtable<String, String>();
    	if (_useLdapSSL)
    	{
    		prepareLdapSSL(myEnvironment, _useReaderCert);
    	}
    	myEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    	myEnvironment.put(Context.PROVIDER_URL, _ldapSearchConnection);

    	if ((_useReaderCert || _useAnonymous || ((null != _ldapPassword) && (0 < _ldapPassword.length())))
    			&& (null != _ldapIdentifier) && (0 < _ldapIdentifier.length()))
    	{
        	// Define the LDAP context and results enumeration outside the try-catch structure
        	// so they can be properly closed even if an exception is encountered.
    		LdapContext myReaderContext = null;
    		NamingEnumeration<SearchResult> myResponse = null;

        	// Complete environment for authenticating

    		if (_useReaderCert)
    		{
    			if (!_useSearchTLS)
    			{
        	    	if (_doDebug) LOG.info("           - connect to LDAP using reader cert");

        	    	myEnvironment.put(Context.SECURITY_AUTHENTICATION, "EXTERNAL");
    			}
    		}
    		else if (_useAnonymous)
    		{
    	    	if (_doDebug) LOG.info("           - connect to LDAP using anonymous bind");

    	    	myEnvironment.put(Context.SECURITY_AUTHENTICATION, "none");
    		}
    		else
    		{
    	    	if (_doDebug) LOG.info("           - connect to LDAP as " + display(_ldapReader) + " with " + display(_ldapPassword, "*configured-password*"));

    	    	myEnvironment.put(Context.SECURITY_AUTHENTICATION, modeIn);
    	    	myEnvironment.put(Context.SECURITY_PRINCIPAL, _ldapReader);
    	    	myEnvironment.put(Context.SECURITY_CREDENTIALS, _ldapPassword);
    		}
        	//specify attributes to be returned in binary format
    		myEnvironment.put("java.naming.ldap.attributes.binary",OBJECT_SID);

        	try
        	{
            	myReaderContext = new InitialLdapContext(myEnvironment, null);

            	if (null != myReaderContext)
            	{
            		if (_useReaderCert && _useSearchTLS)
            		{
            	    	if (_doDebug) LOG.info("           - connect to LDAP using reader cert");

            	    	StartTlsResponse myTls = (StartTlsResponse) myReaderContext.extendedOperation(new StartTlsRequest());
            	    	myTls.negotiate();

            	    	myReaderContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, "EXTERNAL");
            		}

            		// reader is authenticated

            		if (_doDebug && !_useAnonymous) LOG.info("           - LDAP reader is authenticated.");

            		// search for user
            		String myBase = (null != _ldapBase) ? _ldapBase : "";
            		String myFilter = null;
            		if (prefilterIn && (null != _userGroupFilter) && (0 < _userGroupFilter.length()))
            		{
                       	String myString = _userGroupFilter;

            	    	if (_doDebug) LOG.info("           - prefilter = \"" + display(myString) + "\"");

                       	myFilter = myString.replaceAll(USER_TOKEN, escapeLdapIdentifier(getUserName()));
            		}
            		else if ((!prefilterIn) && (null != _userFilter) && (0 < _userFilter.length()))
            		{
                       	String myString = _userFilter;
                       	myFilter = myString.replaceAll(USER_TOKEN, escapeLdapIdentifier(getUserName()));
            		}
            		else
            		{
                		myFilter = createFilter(prefilterIn);
            		}
            		myResponse = searchLdap(myReaderContext, myBase, myFilter, SearchControls.SUBTREE_SCOPE, null);

                    if (authenticateIn)
                    {
                        mySuccess = authenticateFromList(myResponse, modeIn, passwordIn, prefilterIn);
                    }
                    else
                    {
                        mySuccess = ((null != myResponse) && myResponse.hasMoreElements());
                    }
            	}
        	}
        	catch (AuthenticationException myException)
        	{
            	// Authentication failed

        		if (_doDebug) LOG.info("           - LDAP authetication failed.");

        	}
        	catch (NamingException myException)
        	{
            	// Attempt to authenticate anonymously encountered

        		if (_doDebug) LOG.info("           - Naming Exception generated by LDAP" + display(myException.toString(true)));
            	//if (_doDebug) LOG.info("------------------------------------------------------------------------------------------");
            	//if (_doDebug) myException.printStackTrace();

        	}
        	catch (Exception myException)
        	{
            	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
        	}
        	finally
        	{
           		// Close the LDAP results enumeration if it exists
        		close(myResponse);
        		// Close the LDAP context if it exists
        		close(myReaderContext);
        	}
    	}
    	else
    	{
    		LOG.error("Missing required information to perform an LDAP search.");
    	}

    	if (_doDebug) LOG.info(" << << << CentrifugeAuthenticationModule::authenticateSearchLDAP("
    							+ display(getUserName()) + ", "
    							+ display(passwordIn, (_doDigest ? "digested-password" : "*raw-password*")) + ") -->> " + display(mySuccess));

        return mySuccess;
    }

    private boolean authenticateFromList(NamingEnumeration<SearchResult> responseIn, String modeIn, String passwordIn, boolean ignoreGroupCheckIn) throws NamingException
    {

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateFromList(NamingEnumeration<SearchResult>, "
    							+ display(modeIn) + ", "
    							+ display(passwordIn, (_doDigest ? "digested-password" : "*raw-password*")) + ", "
    							+ display(ignoreGroupCheckIn) + ")");

    	boolean mySuccess = false;
    	LdapContext myGroupCheckContext = null; // Requires LDAP V3 for TLS
    	int myCount = 0;

    	if (!ignoreGroupCheckIn)
    	{
    		myGroupCheckContext = createGroupCheckContext(modeIn);
    	}

    	if (ignoreGroupCheckIn || (null != myGroupCheckContext))
    	{
        	Hashtable<String, String> myEnvironment = new Hashtable<String, String>();
        	if (_useLdapSSL)
        	{
        		prepareLdapSSL(myEnvironment, false);
        	}
        	myEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        	myEnvironment.put(Context.PROVIDER_URL, _ldapConnection);
        	myEnvironment.put(Context.SECURITY_AUTHENTICATION, modeIn);
        	myEnvironment.put(Context.SECURITY_CREDENTIALS, passwordIn);

    		while (responseIn.hasMoreElements())
    		{
            	// Define the LDAP context outside the try-catch structure
            	// so it can be properly closed even if an exception is encountered.
    			LdapContext myAuthContext = null; // Requires LDAP V3 for TLS
    			SearchResult myResult = (SearchResult)responseIn.next();

    			myCount++;

    			try
    			{
            		String myDistinguishedName = myResult.getNameInNamespace();

            		if (ignoreGroupCheckIn)
            		{
	            		_testedDNs.add(myDistinguishedName);
            		}

            		if (_doDebug) LOG.info("           - testing " + display(myDistinguishedName));

            		if (ignoreGroupCheckIn || !_testedDNs.contains(myDistinguishedName))
        			{
	    		    	if (_doDebug) LOG.info("           - user DN " + display(myDistinguishedName));

	            		if (ignoreGroupCheckIn || authenticateGroup(myDistinguishedName, myGroupCheckContext, modeIn))
	            		{
	    			    	myEnvironment.put(Context.SECURITY_PRINCIPAL, myDistinguishedName);

	    			    	if (_doDebug) LOG.info("           - connect to LDAP as " + display(myDistinguishedName) + " with " + display(passwordIn, (_doDigest ? "digested-password" : "*raw-password*")));

	    		        	myAuthContext = new InitialLdapContext(myEnvironment, null); // Requires LDAP V3 for TLS

	    		        	if (null != myAuthContext)
	    		        	{
                                _currentUser.distinguishedName = myDistinguishedName;
	    		        		if (_useLdapFullDN)
	    		        		{
	    		        			_currentUser.ldapName = myDistinguishedName;
	    		        		}
	    		        		mySuccess = true;
	    		        		break;
		    	        	}
            			}
            		}
    			}
    			catch (AuthenticationException myException)
    			{

    		    	if (_doDebug) LOG.info("           - failed authentication.");

    			}
    			catch (Exception myException)
    			{

    		    	if (_doDebug) LOG.info("           - caught exception:" + display(myException));

    			}
    			finally
    			{
    	    		// Close the LDAP context if it exists
    				close(myAuthContext);
    			}
    		}
    	}

    	close(myGroupCheckContext);

    	if (_doDebug && (0 == myCount)) LOG.info("           - No users returned from search.");

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::authenticateFromList(NamingEnumeration<SearchResult>, "
				+ display(modeIn) + ", "
				+ display(passwordIn, (_doDigest ? "digested-password" : "*raw-password*")) + ", "
				+ display(ignoreGroupCheckIn) + ") -->> " + display(mySuccess));

    	return mySuccess;
    }

    private LdapContext createGroupCheckContext(String modeIn)
    {
    	LdapContext myGroupCheckContext = null; // Requires LDAP V3 for TLS

    	try
		{
        	Hashtable<String, String> myEnvironment = new Hashtable<String, String>();
        	if (_useLdapSSL)
        	{
        		prepareLdapSSL(myEnvironment, _useReaderCert);
        	}
        	myEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        	myEnvironment.put(Context.PROVIDER_URL, _ldapSearchConnection);
        	if (_useReaderCert)
    		{
    			if (!_useSearchTLS)
    			{
        	    	if (_doDebug) LOG.info("           - connect to LDAP using reader cert");

        	    	myEnvironment.put(Context.SECURITY_AUTHENTICATION, "EXTERNAL");
    			}
    		}
    		else if (_useAnonymous)
        	{
    	    	if (_doDebug) LOG.info("           - connect to LDAP using anonymous bind");

        		myEnvironment.put(Context.SECURITY_AUTHENTICATION, "none");
        	}
        	else
        	{
    	    	if (_doDebug) LOG.info("           - connect to LDAP as " + display(_ldapReader) + " with " + display(_ldapPassword, "*configured-password*"));

            	myEnvironment.put(Context.SECURITY_PRINCIPAL, _ldapReader);
            	myEnvironment.put(Context.SECURITY_CREDENTIALS, _ldapPassword);
            	myEnvironment.put(Context.SECURITY_AUTHENTICATION, modeIn);
        	}
        	myEnvironment.put("java.naming.ldap.attributes.binary",TOKEN_GROUPS);
        	myGroupCheckContext = new InitialLdapContext(myEnvironment, null); // Requires LDAP V3 for TLS
    		if (_useReaderCert && _useSearchTLS)
    		{
    	    	if (_doDebug) LOG.info("           - connect to LDAP using reader cert");

    	    	StartTlsResponse myTls = (StartTlsResponse) myGroupCheckContext.extendedOperation(new StartTlsRequest());
    	    	myTls.negotiate();

    	    	myGroupCheckContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, "EXTERNAL");
    		}

		}
		catch (Exception myException)
		{
			LOG.error("Caught exception while creating group check context." + display(myException));
		}
    	return myGroupCheckContext;
    }

    private boolean authenticateGroup(String userDnIn, LdapContext readerContextIn, String modeIn)
    {
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateGroup(" + display(userDnIn) + ", LdapContext readerContextIn)");

    	boolean mySuccess = true;

    	if (_useGroup)
    	{
        	List<byte[]> mySidList = new ArrayList<byte[]>();
        	List<String> myDnList = new ArrayList<String>();

        	mySuccess = false;

        	if (retrieveUserGroupSIDs(userDnIn, readerContextIn, mySidList, myDnList))
        	{
        		if (0 < mySidList.size())
        		{
            		byte[][] myGroupSIDs = getGroupSIDs(modeIn);
            		int mySize = mySidList.size();

            		for (int i = 0; mySize > i; i++)
            		{
            			if (matchSID(myGroupSIDs, mySidList.get(i)))
            			{
            				mySuccess = true;
            				break;
            			}
            		}
        		}
        		if ((!mySuccess) && (0 < myDnList.size()))
        		{
            		int mySize = myDnList.size();

            		for (int i = 0; mySize > i; i++)
            		{
            			for (int j = 0; _ldapGroup.length > j; j++)
            			{
                			if (_ldapGroup[j].equals(myDnList.get(i)))
                			{
                				mySuccess = true;
                				break;
                			}
            			}
            		}
        		}
        	}
        	else
        	{
            	if (_doDebug) LOG.info("           -- no groups returned for user.");
        	}
    	}

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::authenticateGroup(" + display(userDnIn) + ", LdapContext readerContextIn) -->> " + display(mySuccess));

    	return mySuccess;
    }

    private boolean retrieveUserGroupSIDs(String userDnIn, LdapContext readerContextIn, List<byte[]> sidListIn, List<String> dnListIn)
    {
    	boolean mySuccess = false;
    	NamingEnumeration<SearchResult> myResponse = null;

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::retrieveUserGroupSIDs(" + display(userDnIn) + ", LdapContext readerContextIn)");

    	try
    	{
    		String myBase = userDnIn;
    		String myFilter = _useClass ? createFilterFromList("objectClass", _ldapClass) : "(objectClass=*)";
        	String myRequestedAttributes[] = (!_doDebug) ? new String[]{TOKEN_GROUPS, GROUP_MEMBER} : null;

        	myResponse = searchLdap(readerContextIn, myBase, myFilter, SearchControls.OBJECT_SCOPE, myRequestedAttributes);

    		while (myResponse.hasMoreElements())
    		{
    		    SearchResult myResult = myResponse.next();
    		    Attributes myAttributes = myResult.getAttributes();

    		    if ((myAttributes != null) && (0 < myAttributes.size()))
    		    {
    		    	try
    		    	{
    		    		NamingEnumeration<?> myAttributeList = myAttributes.getAll();

    		    		while (myAttributeList.hasMoreElements())
    		    		{
    		    			Attribute myAttribute = (Attribute)myAttributeList.next();
    		    			String myID = myAttribute.getID();

		    		    	if (_doDebug) LOG.info("           - return attribute " + myID);

    		    			if (TOKEN_GROUPS.equals(myID))
    		    			{
        		    			NamingEnumeration<?> mySIDs = myAttribute.getAll();

        		    			while (mySIDs.hasMoreElements())
        		    	        {
        		    				Object mySID = mySIDs.next();

        		    				try
        		    				{
            		    				byte[] myGroupSID = (byte[])mySID;

            		    	      		if (_doDebug) LOG.info("           - - ** SID = " + display(myGroupSID));

            		    	      		if (null != sidListIn)
            		    	      		{
                		    	      		sidListIn.add(myGroupSID);
                		    	      		mySuccess = true;
            		    	      		}
        		    				}
        		    				catch (Exception myException)
        		    				{
        		    				}
        		    	        }
    		    			}
    		    			else if (GROUP_MEMBER.equals(myID))
    		    			{
        		    			NamingEnumeration<?> myDNs = myAttribute.getAll();

        		    			while (myDNs.hasMoreElements())
        		    	        {
        		    				Object myDN = myDNs.next();

        		    				try
        		    				{
        		    					String myGroupDN = (String)myDN;

            		    	      		LOG.info("           - - ** DN = " + display(myGroupDN));

            		    	      		if (null != dnListIn)
            		    	      		{
	            		    	      		dnListIn.add(myGroupDN);
	            		    	      		mySuccess = true;
            		    	      		}
        		    				}
        		    				catch (Exception myException)
        		    				{
        		    				}
        		    	        }
    		    			}
    		    		}
    		    	}
    		    	catch (Exception myException)
    		    	{
    		        	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
    		    	}
    		    }
    		    else
    		    {
		        	if (_doDebug) LOG.info("           - returned no attributes");
    		    }
    		}
    	}
    	catch (Exception myException)
    	{
        	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
    	}
    	finally
    	{
    		if (null != myResponse)
    		{
        		close(myResponse);
    		}
    	}

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::retrieveUserGroupSIDs(" + display(userDnIn) + ", LdapContext readerContextIn)");

    	return mySuccess;
    }

    private boolean authenticateJDBC(Callback[] callBacksIn)
    {

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::authenticateJDBC(Callback[] callBacksIn)");

        Connection connection = null;
        PreparedStatement statement = null;
        boolean mySuccess = false;

        try
        {
        	String myRawPassword = ((LDAPCallback)callBacksIn[CallBackId.LDAP_PASSWORD.getOrdinal()]).getPassCode();

        	if ((null != myRawPassword) && (0 < myRawPassword.length()))
        	{
        		myRawPassword = null;
                _currentUser.name = ((NameCallback)callBacksIn[CallBackId.USERNAME.getOrdinal()]).getName();
                LogThreadContextUtil.putUserName(getUserName());
                _currentUser.password = new String(((PasswordCallback)callBacksIn[CallBackId.ENCR_PASSWORD.getOrdinal()]).getPassword());

                connection = getConnection();
                statement = connection.prepareStatement(_userQuery);
                statement.setString(1, _currentUser.name.toLowerCase());
                ResultSet results = statement.executeQuery();

                String storedCredential = "";
                if (results.next())
                {
                    storedCredential = results.getString(1);

                    mySuccess = ((null != storedCredential) && storedCredential.equalsIgnoreCase(_currentUser.password));

                    if (!mySuccess)
                    {
                    	if (_doDebug) LOG.info("           - First matching user "
                				+ display(_currentUser.name) + " did not match password.");
                    }
                }
                else if (_autoAuthenticate && (null != _currentUser.password) && (0 < _currentUser.password.length()))
                {
                   	_mode = AuthorizationMode.JDBC;
                	 mySuccess = true;
                }

            	if (_doDebug) LOG.info(" << << << CentrifugeAuthenticationModule::authenticateJDBC("
            							+ display(_currentUser.name) + ", "
            							+ display(_currentUser.password, "*digested-password*") + ") -->> " + display(mySuccess) + (_autoAuthenticate ? "*" : ""));
        	}
        }
        catch (Exception myException)
        {

        	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::authenticateJDBC(Callback[] callBacksIn) -->> EXCEPTION:" + display(myException));

        }
        finally
        {
            if (null != connection)
            {
            	if (null != statement)
            	{
                    try
                    {
                    	statement.close();
                    }
                    catch (Exception myException)
                    {
                    	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
                    }
            	}
                try
                {
                    connection.close();
                }
                catch (Exception myException)
                {
                	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
                }
            }
        }

        return mySuccess;
   }

    private boolean isUserExpirationEnabled()
    {

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::isUserExpirationEnabled()");

        Boolean flag = (Boolean) _sharedState.get("csi.security.policy.enableUserExpiration");
        if (null == flag)
        {
            flag = Boolean.FALSE;
        }

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::isUserExpirationEnabled() -->> " + display(flag.booleanValue()));

        return flag;

    }

    protected Connection getConnection() throws Exception
    {
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::getConnection()");

    	Connection myConnection = _dataSource.getConnection();

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::getConnection()");

        return myConnection;
    }

    private void storeCallbackUpdates(Callback[] callbacksIn) throws IOException, UnsupportedCallbackException
    {
    	// Prepare values for authorization
        _callbackHandler.handle(callbacksIn);
    }

    private Callback[] initializeCallbacks() throws IOException, UnsupportedCallbackException
    {

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::initializeCallbacks()");

    	Callback[] myCallbacks = configureCallbacks();
        _callbackHandler.handle(myCallbacks);

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::initializeCallbacks()");

        return myCallbacks;
    }

    private Callback[] configureCallbacks()
    {

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::configureCallbacks()");

        Callback[] myCallbacks = new Callback[CallBackId.UNSUPPORTED.getOrdinal()];
        myCallbacks[CallBackId.INITIALIZE.getOrdinal()] = new InitializationCallback();
        myCallbacks[CallBackId.AUTHORIZE.getOrdinal()] = new AuthorizationCallback();
        myCallbacks[CallBackId.CERTIFICATE.getOrdinal()] = new CertCallback();
        myCallbacks[CallBackId.LDAP_PASSWORD.getOrdinal()] = new LDAPCallback();
        myCallbacks[CallBackId.USERNAME.getOrdinal()] = new NameCallback(" User name: ");
        myCallbacks[CallBackId.ENCR_PASSWORD.getOrdinal()] = new PasswordCallback("Password: ", false);
        myCallbacks[CallBackId.RESPONSE.getOrdinal()] = new ConfigurationCallback();

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::configureCallbacks()");

        return myCallbacks;
    }

    private static byte[][] getGroupSIDs(String modeIn)
    {
    	if ((null == _groupSID) && _useGroup)
    	{
        	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::getGroupSIDs(" + display(modeIn) + ")");

        	// Define the LDAP context and results enumeration outside the try-catch structure
        	// so they can be properly closed even if an exception is encountered.
    		LdapContext myReaderContext = null; // Requires LDAP V3 for TLS
    		NamingEnumeration<SearchResult> myResponse = null;
    		ArrayList<byte[]> mySidList = new ArrayList<byte[]>();

        	Hashtable<String, String> myEnvironment = new Hashtable<String, String>();

        	if (_useLdapSSL)
        	{
        		prepareLdapSSL(myEnvironment, _useReaderCert);
        	}
        	myEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        	myEnvironment.put(Context.PROVIDER_URL, _ldapSearchConnection);
        	if (_useReaderCert)
    		{
    			if (!_useSearchTLS)
    			{
        	    	if (_doDebug) LOG.info("           - connect to LDAP using reader cert");

        	    	myEnvironment.put(Context.SECURITY_AUTHENTICATION, "EXTERNAL");
    			}
    		}
    		else if (_useAnonymous)
        	{
    	    	if (_doDebug) LOG.info("           - connect to LDAP using anonymous bind");

            	myEnvironment.put(Context.SECURITY_AUTHENTICATION, "none");
        	}
        	else
        	{
    	    	if (_doDebug) LOG.info("           - connect to LDAP as " + display(_ldapReader));

            	myEnvironment.put(Context.SECURITY_AUTHENTICATION, modeIn);
            	myEnvironment.put(Context.SECURITY_PRINCIPAL, _ldapReader);
            	myEnvironment.put(Context.SECURITY_CREDENTIALS, _ldapPassword);
        	}
        	myEnvironment.put("java.naming.ldap.attributes.binary",OBJECT_SID);

	      	try
	      	{
	          	myReaderContext = new InitialLdapContext(myEnvironment, null); // Requires LDAP V3 for TLS

	          	if (null != myReaderContext)
	          	{
            		if (_useReaderCert && _useSearchTLS)
            		{
            	    	if (_doDebug) LOG.info("           - connect to LDAP using reader cert");

            	    	StartTlsResponse myTls = (StartTlsResponse) myReaderContext.extendedOperation(new StartTlsRequest());
            	    	myTls.negotiate();

            	    	myReaderContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, "EXTERNAL");
            		}

	          		if (_doDebug && ! _useAnonymous) LOG.info("           - LDAP reader is authenticated.");

            		String myBase = (null != _ldapGroupBase) ? _ldapGroupBase : _ldapBase;
            		String myFilter = null;
            		if ((null != _groupFilter) && (0 < _groupFilter.length()))
            		{
            			myFilter = _groupFilter;
            		}
            		else
            		{
	            		if ((null != _groupClass) && (0 < _groupClass.length()))
	            		{
	            			myFilter = "(&(objectClass=" + _groupClass + ")" + createFilterFromList("distinguishedName", _ldapGroup) + ")";
	            		}
	            		else
	            		{
	            			myFilter = createFilterFromList("distinguishedName", _ldapGroup);
	            		}
            		}
                	String myRequestedAttributes[] = (!_doDebug) ? new String[]{OBJECT_SID} : null;

            		myResponse = searchLdap(myReaderContext, myBase, myFilter, SearchControls.SUBTREE_SCOPE, myRequestedAttributes);

	        		while (myResponse.hasMoreElements())
	        		{
	        			SearchResult myResult = (SearchResult)myResponse.next();

	        			try
	        			{
	            		    Attributes myAttributes = myResult.getAttributes();
	                		String myDistinguishedName = myResult.getNameInNamespace();
	                		Attribute myAttribute = myAttributes.get(OBJECT_SID);

	        		    	if (_doDebug) LOG.info("           - group " + display(myDistinguishedName) + " returned " + Integer.toString(myAttributes.size()) + " " + OBJECT_SID + " attributes.");

	        		    	if (null != myAttribute)
	        		    	{
		            		    byte[] mySID = (byte[])myAttribute.get();

			    	      		if (_doDebug) LOG.info("           - ++ SID = " + display(mySID));

			    				mySidList.add(mySID);
	        		    	}
	        		    	else if (0 < myAttributes.size())
	            		    {
			    	      		if (_doDebug) LOG.info("           - NO SID RETURNED");

			    	      		NamingEnumeration<String> myIDs = myAttributes.getIDs();

			    	      		while (myIDs.hasMoreElements())
			    	      		{
			    	      			if (_doDebug) LOG.info("             - returned: " + myIDs.nextElement());
			    	      		}
	            		    }
	            		    else
	            		    {
			    	      		if (_doDebug) LOG.info("           - NO ATTRIBUTES RETURNED");
	            		    }
	        			}
	        			catch (Exception myException)
	        			{
	        	        	if (_doDebug) LOG.info("           - caught exception:" + display(myException));
	        			}
	        		}
	          	}
	      	}
	      	catch (AuthenticationException myException)
	      	{
	          	// Authentication failed

	      		if (_doDebug) LOG.info("           - LDAP authetication failed.");

	      	}
	      	catch (NamingException myException)
	      	{
	          	// Attempt to authenticate anonymously encountered

	      		if (_doDebug) LOG.info("           - Naming Exception generated by LDAP.");

	      	}
	      	catch (Exception myException)
	      	{

	      		if (_doDebug) LOG.info("           - Unexpected Exception generated by LDAP:" + display(myException));

	      	}
	      	finally
	      	{
	         	// Close the LDAP results enumeration if it exists
	      		close(myResponse);
	      		// Close the LDAP context if it exists
	      		close(myReaderContext);
	      	}

	      	if (0 < mySidList.size())
	      	{
	      		byte[][] myDummy = new byte[0][];

	      		if (_doDebug) LOG.info("           - move group SIDs to static array.");

	      		_groupSID = mySidList.toArray(myDummy);
	      	}

	    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::getGroupSIDs(" + display(modeIn) + ")");

    	}
    	return _groupSID;
    }

    private static void prepareLdapSSL(Hashtable<String, String> environmentIn, boolean SetKeyStore)
    {
    	if (SetKeyStore)
    	{
        	if (_doDebug) LOG.info("           - using SSL and keystore " + display(_ldapKeyFile) + " of type " + display(_ldapKeyType));

    		System.setProperty("javax.net.ssl.keyStore", _ldapKeyFile);
    		System.setProperty("javax.net.ssl.keyStoreType", _ldapKeyType);
    		System.setProperty("javax.net.ssl.keyStorePassword", _ldapKeyPassword);
    	}

    	if (_doDebug) LOG.info("           - using SSL and truststore " + display(_ldapTrustFile) + " of type " + display(_ldapTrustType));

		System.setProperty("javax.net.ssl.trustStore", _ldapTrustFile);
		System.setProperty("javax.net.ssl.trustStoreType", _ldapTrustType);
		System.setProperty("javax.net.ssl.trustStorePassword", _ldapTrustPassword);
		//myEnvironment.put(Context.SECURITY_PROTOCOL, "ssl");
		if (_trustLdapServer)
		{
			environmentIn.put("java.naming.ldap.factory.socket", "csi.security.jaas.spi.SSLTestingWorkaround");
		}
    }

    public static Integer getAuthenticationMethod() {
        return _authenticationMethod;
    }

    private String createFilter(boolean prefilterIn)
    {
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::createFilter(" + display(_currentUser.ldapName) + ")");

    	String myString = null;
    	String myUser = _currentUser.ldapName;
    	String myTag = (_useDN) ? "distinguishedName" : _ldapIdentifier;

    	if ((_useClass || (prefilterIn && _useGroup)))
    	{
       		myString = "(&(" + myTag + "=" + myUser + ")"
   					+ (_useClass ? createFilterFromList("objectClass", _ldapClass) : "")
   					+ ((prefilterIn && _useGroup) ? createFilterFromList("memberOf", _ldapGroup) : "") + ")";
    	}
    	else if (_doEscape && _useDN)
    	{
       		myString = "("+ myTag + "=" + escapeLdapIdentifier(myUser) + ")";
    	}
    	else
    	{
       		myString = "("+ myTag + "=" + myUser + ")";
    	}

    	if (_doDebug) LOG.info("           -- filter:\"" + myString + "\"");

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::createFilter(" + display(_currentUser.ldapName) + ") -->> " + display(myString));

    	return myString;
    }

    private static String escapeLdapIdentifier(String nameIn)
    {
    	String myString = nameIn;

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::escapeDN(" + display(nameIn) + ")");

    	if (null != nameIn)
    	{
        	StringBuilder myBuffer = new StringBuilder();

        	for (int i = 0; nameIn.length() > i; )
        	{
        		char myCharacter = nameIn.charAt(i++);

        		// if ('\\' == myCharacter) do something to handle previously escaped characters
        		// else do the following
        		if (0 <= DN_SPECIAL_CHARACTERS.indexOf(myCharacter))
        		{
        			myBuffer.append('\\');
        		}
    			myBuffer.append(myCharacter);
        	}

           	myString = myBuffer.toString();
    	}

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::escapeDN(" + display(nameIn) + ") -->> " + display(myString));

    	return myString;
    }

    private static String getUserNameFromCert(X509Certificate cert)
    {

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::getUserNameFromCert(X509Certificate cert)");

        String myName = null;
        String myDN = cert.getSubjectX500Principal().getName();

    	if (_doDebug) LOG.info("           - DN = " + display(myDN));

        if (_useCertFullDN)
        {
        	myName = myDN;
        }
        else
        {
        	int myLimit = myDN.length();
        	int i = 0;

        	while ((null == myName) && (myLimit > i))
        	{
        		if (' ' < myDN.charAt(i))
        		{
        			int myTagOffset = i;	// identify start of component tag

        			while (myLimit > i)
        			{
        				if ('=' == myDN.charAt(i))
        				{
        					if (myDN.substring(myTagOffset, i).equalsIgnoreCase("cn"))
        					{
        			        	StringBuilder myBuffer = new StringBuilder();

        			        	i++;	// skip over equal sign

        			        	while (myLimit > i)
        	        			{
        			        		char myCharacter = myDN.charAt(i++);
        							if ('\\' == myCharacter)
        							{
        								myBuffer.append(myDN.charAt(i++));
        							}
        							else if (',' == myCharacter)
        							{
        								break;
        							}
        							else
        							{
        								myBuffer.append(myCharacter);
        							}
        	        			}
        			        	myName = myBuffer.toString();
        					}
        					else
        					{
        			        	while (myLimit > i)
        	        			{
        			        		char myCharacter = myDN.charAt(i++);
        							if ('\\' == myCharacter)
        							{
        								i++;
        							}
        							else if (',' == myCharacter)
        							{
        								break;
        							}
        	        			}
        					}
    			        	break;
        				}
        				else
        				{
        					i++;
        				}
        			}
        		}
        		else // Trim leading white-space
        		{
        			i++;
        		}
        	}
        }

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::getUserNameFromCert(X509Certificate cert) -->> " + display(myName));

        return (null != _certExtractUser) ? extractPattern(myName, _certExtractUser) : myName;
    }

    private void sendResponse(Callback[] callBacksIn, String requestIn) throws LoginException
    {

    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::sendResponse(Callback[] callBacksIn, " + display(requestIn) + ")");

        try
        {

            if (_doDebug) LOG.info(">> >> >>   - returning _authenticationOrder string " + display(_authenticationOrder));
            if (_doDebug) LOG.info(">> >> >>   - returning _userHeaderKey string " + display(_userHeaderKey));
            if (_doDebug) LOG.info(">> >> >>   - returning _kerberosKeyTab string " + display(_kerberosKeyTab));
            if (_doDebug) LOG.info(">> >> >>   - returning _kerberosPrincipal string " + display(_kerberosPrincipal));
            if (_doDebug) LOG.info(">> >> >>   - returning _kerberosUseLdap string " + display(_kerberosUseLdap));
            if (_doDebug) LOG.info(">> >> >>   - returning _doDebug value " + display(_doDebug));

            ((ConfigurationCallback)callBacksIn[CallBackId.RESPONSE.getOrdinal()]).setAuthenticationOrder(_authenticationOrder);
            ((ConfigurationCallback)callBacksIn[CallBackId.RESPONSE.getOrdinal()]).setUserHeaderKey(_userHeaderKey);
            ((ConfigurationCallback)callBacksIn[CallBackId.RESPONSE.getOrdinal()]).setKerberosKeyTab(_kerberosKeyTab);
            ((ConfigurationCallback)callBacksIn[CallBackId.RESPONSE.getOrdinal()]).setKerberosPrincipal(_kerberosPrincipal);
            ((ConfigurationCallback)callBacksIn[CallBackId.RESPONSE.getOrdinal()]).setKerberosUseDomainName(_kerberosUseDomainName);
            ((ConfigurationCallback)callBacksIn[CallBackId.RESPONSE.getOrdinal()]).setDoDebug(_doDebug);
        	storeCallbackUpdates(callBacksIn);
        }
        catch (Exception myException)
        {
            LOG.error("Caught exception storing response callback during "
            			+ display(getUserName()) + " login: " + display(myException));
        }

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::sendResponse(Callback[] callBacksIn, " + display(requestIn) + ") -->> " + display(_userHeaderKey));

    }

    private static NamingEnumeration<SearchResult> searchLdap(LdapContext readerContextIn, String baseIn,
    										String filterIn, int scopeIn, String[] attributeRequestIn)
    										throws NamingException
    {
    	NamingEnumeration<SearchResult> myResponse = null;

    	if (_doDebug)
    	{
        	StringBuilder myAttributeRequest = new StringBuilder();
        	String myScopeString = (SearchControls.SUBTREE_SCOPE == scopeIn) ? "SUBTREE_SCOPE" :
				(SearchControls.OBJECT_SCOPE == scopeIn) ? "OBJECT_SCOPE" :
				(SearchControls.ONELEVEL_SCOPE == scopeIn) ? "ONELEVEL_SCOPE" : "UNKNOWN_SCOPE";

        	if ((null != attributeRequestIn) && (0 < attributeRequestIn.length))
        	{
        		char mySeparator = ':';

        		myAttributeRequest.append(" returning attributes");
        		for (String myRequest : attributeRequestIn)
        		{
        			myAttributeRequest.append(mySeparator + ' ' + myRequest);
        			mySeparator = ',';
        		}
        	}
        	LOG.info("           - search LDAP base " + display(baseIn) + " using filter " + display(filterIn) + " with " + myScopeString + myAttributeRequest.toString());
    	}

		SearchControls myControls = new SearchControls();
		myControls.setSearchScope(scopeIn);
		if (null != attributeRequestIn)
		{
			myControls.setReturningAttributes(attributeRequestIn);
		}

		myResponse = readerContextIn.search(baseIn, filterIn, myControls);

    	return myResponse;
    }

    private static void setStaticValues(Map<String, ?> optionsIn)
    {
        if (null != optionsIn) {

            String myDoDebug = (String) optionsIn.get(PROPERTY_DEBUG);
            String myDoExtendedDebug = (String) optionsIn.get(PROPERTY_EXTENDED_DEBUG);
            String myAutoAuthenticate = (String) optionsIn.get(PROPERTY_AUTO_AUTHENTICATE);
            String myLogFailure = (String) optionsIn.get(PROPERTY_LOG_FAILURE);
            String myLogSuccess = (String) optionsIn.get(PROPERTY_LOG_SUCCESS);
            String myAuthenticationList = (String) optionsIn.get(PROPERTY_AUTHENTICATION);
            String myUserHeaderKey = (String) optionsIn.get(PROPERTY_USERNAME_HEADER_KEY);
            String myDefaultUser = (String) optionsIn.get(DEFAULT_USER);

            _options = optionsIn;
            _doDebug = ((null != myDoDebug) && myDoDebug.trim().equalsIgnoreCase("true"));
            _doExtendedDebug = ((null != myDoExtendedDebug) && myDoExtendedDebug.trim().equalsIgnoreCase("true"));
            _autoAuthenticate = ((null != myAutoAuthenticate) && myAutoAuthenticate.equalsIgnoreCase("true"));
            _logFailure = ((null != myLogFailure) && myLogFailure.equalsIgnoreCase("true"));
            _logSuccess = ((null != myLogSuccess) && myLogSuccess.equalsIgnoreCase("true"));

            if ((null != myAuthenticationList) && (0 < myAuthenticationList.length())) {
                String[] myAuthenticationArray = myAuthenticationList.split("\\|");

                if ((null != myAuthenticationArray) && (0 < myAuthenticationArray.length)) {

                    StringBuilder myBuffer = new StringBuilder();

                    for (int i = 0; myAuthenticationArray.length > i; i++) {

                        Integer myAuthentication = _authenticationMap.get(myAuthenticationArray[i].trim().toLowerCase());

                        if (null != myAuthentication) {
                            if (null == _authenticationMethod) {
                                _authenticationMethod = 1 << myAuthentication;
                            } else {
                                _authenticationMethod |= (1 << myAuthentication);
                            }
                            if ((AuthorizationMode.JDBC.ordinal() != myAuthentication)
                                    && (AuthorizationMode.LDAP.ordinal() != myAuthentication)) {

                                if (0 < myBuffer.length()) {
                                    myBuffer.append(',');
                                }
                                myBuffer.append(myAuthentication.toString());
                            }
                        }
                    }
                    if (AuthorizationMode.LDAP.isSet(_authenticationMethod)
                            || AuthorizationMode.JDBC.isSet(_authenticationMethod)) {

                        if (0 < myBuffer.length()) {
                            myBuffer.append(',');
                        }
                        myBuffer.append(Integer.toString(AuthorizationMode.FORM.ordinal()));
                    }
                    if (AuthorizationMode.SERVICE.isSet(_authenticationMethod)) {
                        if (0 < myBuffer.length()) {
                            myBuffer.append(',');
                        }
                        myBuffer.append(Integer.toString(AuthorizationMode.FORM.ordinal()));
                        _serviceAddress = (String) optionsIn.get(PROPERTY_SERVICE_ADDRESS);
                    }
                    _authenticationOrder = (0 < myBuffer.length()) ? myBuffer.toString() : "";
                }
            }
            _defaultUserName = myDefaultUser;
            if ((null != myUserHeaderKey) && (0 < myUserHeaderKey.length())) {
                _userHeaderKey = myUserHeaderKey;
            }


            if (AuthorizationMode.CERT.isSet(_authenticationMethod))
            {
                String myCertUseLdap = (String) optionsIn.get(PROPERTY_CERT_USE_LDAP);
                String myCertUseDN = (String) optionsIn.get(PROPERTY_CERT_USE_FULL_DN);
                String myCertValidGroups = (String) optionsIn.get(PROPERTY_CERT_VALID_GROUPS);

                _certExtractUser = (String) optionsIn.get(PROPERTY_CERT_EXTRACT_USER);
                _useCertFullDN = ((null != myCertUseDN) && myCertUseDN.equalsIgnoreCase("true"));
                _certUseLdap = ((null != myCertUseLdap) && myCertUseLdap.trim().equalsIgnoreCase("true"));
                _certGroupUrl = (String) optionsIn.get(PROPERTY_CERT_GROUP_URL);
                _certGroupTag = (String) optionsIn.get(PROPERTY_CERT_GROUP_TAG);

                if ((null != _certGroupUrl) && (null != _certGroupTag) && (null != myCertValidGroups)) {

                    String[] myGroupArray = myCertValidGroups.split("\\|");

                    _certGroupUrl = _certGroupUrl.trim();
                    _certGroupTag = _certGroupTag.trim();

                    if ((0 < _certGroupUrl.length())
                            && (0 < _certGroupTag.length()) && (0 < myGroupArray.length)) {

                        _certValidGroups = new ArrayList<String>();

                        for (int i = 0; myGroupArray.length > i; i++) {

                            String myGroup = (null != myGroupArray[i]) ? myGroupArray[i].trim().toLowerCase() : null;

                            if ((null != myGroup) && (0 < myGroup.length())) {

                                _certValidGroups.add(myGroup);
                            }
                        }
                        if (0 < _certValidGroups.size()) {

                            _certUseGroupService = true;
                        }
                    }
                }
            }
            if (AuthorizationMode.KERBEROS.isSet(_authenticationMethod))
            {
                String myKerberosUseDomainName = (String) optionsIn.get(PROPERTY_KERBEROS_USE_DOMAIN_NAME);
                String myKerberosKeyTab = (String) optionsIn.get(PROPERTY_KERBEROS_KEY_TAB);
                String myKerberosUseLdap = (String) optionsIn.get(PROPERTY_KERBEROS_USE_LDAP);
                String myKerberosValidGroups = (String) optionsIn.get(PROPERTY_KERBEROS_VALID_GROUPS);

                _kerberosUseLdap = ((null != myKerberosUseLdap) && myKerberosUseLdap.trim().equalsIgnoreCase("true"));
                _kerberosUseDomainName = ((null != myKerberosUseDomainName) && myKerberosUseDomainName.trim().equalsIgnoreCase("true"));
                _kerberosPrincipal = (String) _options.get(PROPERTY_KERBEROS_PRINCIPAL);
                _kerberosGroupUrl = (String) optionsIn.get(PROPERTY_KERBEROS_GROUP_URL);
                _kerberosGroupTag = (String) optionsIn.get(PROPERTY_KERBEROS_GROUP_TAG);

                if ((null != _kerberosGroupUrl) && (null != _kerberosGroupTag) && (null != myKerberosValidGroups)) {

                    String[] myGroupArray = myKerberosValidGroups.split("\\|");

                    _kerberosGroupUrl = _kerberosGroupUrl.trim();
                    _kerberosGroupTag = _kerberosGroupTag.trim();

                    if ((0 < _kerberosGroupUrl.length())
                            && (0 < _kerberosGroupTag.length()) && (0 < myGroupArray.length)) {

                        _kerberosValidGroups = new ArrayList<String>();

                        for (int i = 0; myGroupArray.length > i; i++) {

                            String myGroup = (null != myGroupArray[i]) ? myGroupArray[i].trim().toLowerCase() : null;

                            if ((null != myGroup) && (0 < myGroup.length())) {

                                _kerberosValidGroups.add(myGroup);
                            }
                        }
                        if (0 < _kerberosValidGroups.size()) {

                            _kerberosUseGroupService = true;
                        }
                    }
                }

                if ((null != myKerberosKeyTab) && (0 < myKerberosKeyTab.length())) {

                    try {

                        File myKeyTabFile = new File(myKerberosKeyTab);

                        if (null != myKeyTabFile) {

                            _kerberosKeyTab = myKeyTabFile.getAbsolutePath();
                        }

                    } catch (Exception ignore) {
                    }

                    if (null == _kerberosKeyTab) {

                        LOG.error("Unable to access Kerberos keytab file: " + display(myKerberosKeyTab));
                    }
                }
            }
            if (AuthorizationMode.SAML.isSet(_authenticationMethod))
            {
                String mySamlUseLdap = (String) optionsIn.get(PROPERTY_SAML_USE_LDAP);
                String mySamlValidGroups = (String) optionsIn.get(PROPERTY_SAML_VALID_GROUPS);

                _samlUseLdap = ((null != mySamlUseLdap) && mySamlUseLdap.trim().equalsIgnoreCase("true"));
                _samlGroupUrl = (String) optionsIn.get(PROPERTY_SAML_GROUP_URL);
                _samlGroupTag = (String) optionsIn.get(PROPERTY_SAML_GROUP_TAG);

                if ((null != _samlGroupUrl) && (null != _samlGroupTag) && (null != mySamlValidGroups)) {

                    String[] myGroupArray = mySamlValidGroups.split("\\|");

                    _samlGroupUrl = _samlGroupUrl.trim();
                    _samlGroupTag = _samlGroupTag.trim();

                    if ((0 < _samlGroupUrl.length())
                            && (0 < _samlGroupTag.length()) && (0 < myGroupArray.length)) {

                        _samlValidGroups = new ArrayList<String>();

                        for (int i = 0; myGroupArray.length > i; i++) {

                            String myGroup = (null != myGroupArray[i]) ? myGroupArray[i].trim().toLowerCase() : null;

                            if ((null != myGroup) && (0 < myGroup.length())) {

                                _samlValidGroups.add(myGroup);
                            }
                        }
                        if (0 < _samlValidGroups.size()) {

                            _samlUseGroupService = true;
                        }
                    }
                }
            }
            if (_certUseLdap || _kerberosUseLdap || _samlUseLdap || AuthorizationMode.LDAP.isSet(_authenticationMethod))
            {
                String myLdapUseDN = (String) optionsIn.get(PROPERTY_LDAP_USE_FULL_DN);
                String myLdapGroupList = (String) optionsIn.get(PROPERTY_LDAP_GROUP_DN);
                String myLdapClassList = (String) optionsIn.get(PROPERTY_LDAP_CLASS);
                String myLdapKeyFile = (String) optionsIn.get(PROPERTY_LDAP_KEY_FILE_KEY);
                String myLdapKeyType = (String) optionsIn.get(PROPERTY_LDAP_KEY_TYPE_KEY);
                String myLdapKeyPassword = (String) optionsIn.get(PROPERTY_LDAP_KEY_PASSWORD_KEY);
                String myLdapTrustFile = (String) optionsIn.get(PROPERTY_LDAP_TRUST_FILE_KEY);
                String myLdapTrustType = (String) optionsIn.get(PROPERTY_LDAP_TRUST_TYPE_KEY);
                String myLdapTrustPassword = (String) optionsIn.get(PROPERTY_LDAP_TRUST_PASSWORD_KEY);
                String myTrustLdapServer = (String) optionsIn.get(PROPERTY_TRUST_LDAP_SERVER_KEY);
                String myCheckGroupSIDs = (String) optionsIn.get(PROPERTY_CHECK_GROUP_SIDS);

                _useLdapFullDN = ((null != myLdapUseDN) && myLdapUseDN.equalsIgnoreCase("true"));
                if ((null != myLdapGroupList) && (0 < myLdapGroupList.length())) {
                    _ldapGroup = myLdapGroupList.split("\\|");
                }
                if ((null != myLdapClassList) && (0 < myLdapClassList.length())) {
                    _ldapClass = myLdapClassList.split("\\|");
                }
                _ldapConnection = (String) optionsIn.get(PROPERTY_LDAP_CONNECTION);
                _ldapSearchConnection = (String) optionsIn.get(PROPERTY_LDAP_SEARCH_CONNECTION);
                _ldapUserDn = (String) optionsIn.get(PROPERTY_LDAP_USER_DN);
                _ldapReader = (String) optionsIn.get(PROPERTY_LDAP_READER);
                _ldapPassword = (String) optionsIn.get(PROPERTY_LDAP_PASSWORD);
                _ldapIdentifier = (String) optionsIn.get(PROPERTY_LDAP_IDENTIFIER);
                _ldapDigest = (String) optionsIn.get(PROPERTY_LDAP_DIGEST);
                _ldapBase = (String) optionsIn.get(PROPERTY_LDAP_BASE);
                _ldapGroupBase = (String) optionsIn.get(PROPERTY_LDAP_GROUP_BASE);
                _groupClass = (String) optionsIn.get(PROPERTY_LDAP_GROUP_CLASS);
                _groupFilter = (String) optionsIn.get(PROPERTY_LDAP_GROUP_FILTER);
                _userFilter = (String) optionsIn.get(PROPERTY_LDAP_USER_FILTER);
                _userGroupFilter = (String) optionsIn.get(PROPERTY_LDAP_USER_GROUP_FILTER);

                _useClass = ((null != _ldapClass) && (0 < _ldapClass.length));
                _useGroup = ((null != _ldapGroup) && (0 < _ldapGroup.length));
                _useDN = ((null != _ldapUserDn) && (0 < _ldapUserDn.length()));
                _doDigest = ((null != _ldapDigest) && (0 < _ldapDigest.length()));
                _useAnonymous = !((null != _ldapReader) && (0 < _ldapReader.length()));
                _useReaderCert = ((null != _ldapReader) && (1 == _ldapReader.length()) && ('*' == _ldapReader.charAt(0)));

                _checkGroupSIDs = (_useGroup && (null != myCheckGroupSIDs) && myCheckGroupSIDs.equalsIgnoreCase("true"));

                if ((null != _ldapConnection) && (8 < _ldapConnection.length()))
                {
                    if ((null == _ldapSearchConnection) || (0 == _ldapSearchConnection.length()))
                    {
                        _ldapSearchConnection = _ldapConnection;
                    }

                    if (_useReaderCert && _ldapSearchConnection.substring(0, 7).equalsIgnoreCase("ldap://"))
                    {
                        _useSearchTLS = true;
                    }

                    if (_ldapConnection.substring(0, 8).equalsIgnoreCase("ldaps://"))
                    {
                        _useLdapSSL = true;

                        if ((null != myLdapKeyFile) && (0 < myLdapKeyFile.length()))
                        {
                            _ldapKeyFile = myLdapKeyFile;
                        }
                        if ((null != myLdapKeyType) && (0 < myLdapKeyType.length()))
                        {
                            _ldapKeyType = myLdapKeyType;
                        }
                        if ((null != myLdapKeyPassword) && (0 < myLdapKeyPassword.length()))
                        {
                            _ldapKeyPassword = myLdapKeyPassword;
                        }
                        if ((null != myLdapTrustFile) && (0 < myLdapTrustFile.length()))
                        {
                            _ldapTrustFile = myLdapTrustFile;
                        }
                        if ((null != myLdapTrustType) && (0 < myLdapTrustType.length()))
                        {
                            _ldapTrustType = myLdapTrustType;
                        }
                        if ((null != myLdapTrustPassword) && (0 < myLdapTrustPassword.length()))
                        {
                            _ldapTrustPassword = myLdapTrustPassword;
                        }

                        verifyTrustStore();

                        _trustLdapServer = ((null != myTrustLdapServer) && myTrustLdapServer.equalsIgnoreCase("true"));
                    }
                }
                else
                {
                    LOG.error("LDAP not configured correctly!");
                }
            }

            if (_useLdapSSL)
            {

            }

            _staticValuesSet = true;

            if (_doDebug) LOG.info("           - authentication method(s): " + myAuthenticationList);
            if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::setStaticValues(Map<String, ?> optionsIn)");
        }
    }

    private static void verifyTrustStore()
    {
    	if (_doDebug) LOG.info(">> >> >>  CentrifugeAuthenticationModule::verifyTrustStore()");

        char[] myPassword = _ldapTrustPassword.toCharArray();
        Integer myCertificateCount = 0;

        try
        {
            FileInputStream myStream = new FileInputStream(_ldapTrustFile);
            KeyStore myTruststore = KeyStore.getInstance(_ldapTrustType);

            myTruststore.load(myStream, myPassword);

            for (Enumeration<String> myAliases = myTruststore.aliases(); myAliases.hasMoreElements();)
            {
            	String myAlias = myAliases.nextElement();
            	Certificate myCertificate = myTruststore.getCertificate(myAlias);
            	myCertificateCount++;

            	if (_doExtendedDebug) LOG.info(_dividerString + "(" + myCertificateCount.toString()
            									+ ") Certificate alias = " + myAlias + _dividerString
            									+ myCertificate.toString() + _dividerString);
            }
        }
		catch (Exception myException)
		{
			LOG.error("\nCaught exception loading truststore file "
									+ display(_ldapTrustFile) + " of type "
									+ display(_ldapTrustType) + " using password "
									+ display(_ldapTrustPassword, "<" + PROPERTY_LDAP_TRUST_PASSWORD_KEY + ">")
									+ "." + display(myException));
		}

    	if (_doDebug) LOG.info("<< << <<  CentrifugeAuthenticationModule::verifyTrustStore() -->> " + myCertificateCount.toString() + " certs");
    }

    private static void close(Context valueIn)
    {
    	if (null != valueIn)
    	{
        	try
        	{
        		valueIn.close();
        	}
        	catch (Exception myException)
        	{
        		// ignore the error
        	}
    	}
    }

    private static void close(NamingEnumeration<SearchResult> valueIn)
    {
    	if (null != valueIn)
    	{
        	try
        	{
        		valueIn.close();
        	}
        	catch (Exception myException)
        	{
        		// ignore the error
        	}
    	}
    }

    private static String display(boolean valueIn)
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

    private static String display(String stringIn, String replacementIn)
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

    static final char[] _hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String display(byte[] arrayIn)
    {
    	if (null != arrayIn)
    	{
    		int mySize = (arrayIn.length * 3) - 1;
    		char[] myChar = new char[mySize];

    		for (int i = 0, j = 0; mySize > i; j++)
    		{
    			int myUpperNibble = ((int)(arrayIn[j]) & 0x000000ff) / 16;
    			int myLowerNibble = ((int)(arrayIn[j]) & 0x000000ff) % 16;

    			if (0 < i)
    			{
    				myChar[i++] = ' ';
    			}
    			myChar[i++] = _hexChar[myUpperNibble];
    			myChar[i++] =_hexChar[myLowerNibble];
    		}
    		return new String(myChar);
    	}
    	else
    	{
    		return "<null>";
    	}
    }

    private static String display(Throwable exceptionIn)
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

    private boolean validateUserGroup(String groupUrlIn, String groupTagIn, List<String> validGroupsIn) {

        boolean mySuccess = false;
        String myFormattedRequest = substituteValues(groupUrlIn);

        try {

            URL myUrl = new URL(substituteValues(groupUrlIn));
            HttpURLConnection myConnection = (HttpURLConnection)myUrl.openConnection();

            try {

                myConnection.setRequestMethod("GET");
                myConnection.setRequestProperty("Accept", "application/xml");
                List<String> myGroupList = extractUserGroups(myConnection.getInputStream(), groupTagIn);

                if ((null != myGroupList) && (0 < myGroupList.size())) {

                    for (String myValidGroup : validGroupsIn) {

                        for (String myGroup : myGroupList) {

                            if (myValidGroup.equals(myGroup)) {

                                mySuccess = true;
                                break;
                            }
                        }
                        if (mySuccess) {

                            break;
                        }
                    }
                }

            } catch (FileNotFoundException myException) {

                LOG.error("URL " + display(myFormattedRequest) + " returned File Not Found.");

            } catch (Exception myException) {

                LOG.error("Caught exception while importing user roles from "
                            + display(myFormattedRequest), myException);

            } finally {

                myConnection.disconnect();
            }

        } catch (Exception myException) {

            LOG.error("Caught exception opening connection to " + display(myFormattedRequest), myException);
        }
        return mySuccess;
    }

    private List<String> extractUserGroups(InputStream streamIn, String groupTagIn) throws Exception {

        List<String> myList = null;

        if (null != streamIn) {

            try {

                DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder myBuilder = myFactory.newDocumentBuilder();
                Document myDocument = myBuilder.parse(streamIn);
                Element myRoot = myDocument.getDocumentElement();
                NodeList myNodeList = myRoot.getElementsByTagName(groupTagIn);
                int myNodeCount = myNodeList.getLength();

                myList = new ArrayList<String>();

                for (int i = 0; myNodeCount > i; i++) {

                    Node myNode = myNodeList.item(i);
                    Node myData = myNode.getFirstChild();
                    String myGroup = (null != myData) ? myData.getNodeValue() : null;

                    if (null != myGroup) {

                        myGroup = myGroup.trim();

                        if (0 < myGroup.length()) {

                            myList.add(myGroup);
                        }
                    }
                }

            } finally {

                try {

                    streamIn.close();

                } catch (Exception myException) {

                    // Ignore
                }
            }
        }
        return myList;
    }

    private static String extractPattern(String sourceIn, String patternIn) {

        String myResult = null;

        try {

            String[] myInstructions = patternIn.split("\\|");
            int myGroup = (1 < myInstructions.length) ? Integer.parseInt(myInstructions[0]) : 0;
            Pattern myPattern = Pattern.compile(myInstructions[(1 < myInstructions.length) ? 1 : 0]);
            Matcher mySearch = myPattern.matcher(sourceIn);

            if (mySearch.find()) {

                myResult = mySearch.group(myGroup);
            }

        } catch (Exception myException) {

            LOG.error("Caught exception extracting username substring:", myException);
        }
        return myResult;
    }

    private String substituteValues(String stringIn) {

        if (null != stringIn) {

            String myUser = (null != _currentUser.name) ? _currentUser.name : "";
            String myDN = (null != _currentUser.distinguishedName) ? _currentUser.distinguishedName : "";

            return stringIn.replaceAll(USER_TOKEN, myUser).replaceAll(DN_TOKEN, myDN);
        }
        return stringIn;
    }

    private String getUserName() {

        return (null != _currentUser) ? _currentUser.name : (null != _logonName) ? _logonName : "";
    }

    private String getDistinguishedName() {

        return (null != _currentUser) ? _currentUser.distinguishedName : null;
    }
/*
    private TicketContents getTicketContents(Subject subjectIn, final byte[] ticketIn)
            throws GSSException {

        TicketContents myTicketContents = Subject.doAs(subjectIn, new PrivilegedAction<TicketContents>() {

            public TicketContents run() {

                try {

                    TicketContents myContents = new TicketContents();
                    GSSManager myManager = GSSManager.getInstance();
                    GSSContext myContext = myManager.createContext((GSSCredential)null);

                    myContext.acceptSecContext(ticketIn, 0, ticketIn.length);
                    myContents.clientName = myContext.getSrcName().toString();

                    return myContents;
                }
                catch (Exception myException) {

                    LOG.error("Caught exception establishing KERBEROS context: " + display(myException));
                    return null;
                }
            }
        });
        return myTicketContents;
    }

    private Subject createServerSubject(String passwordIn) throws LoginException {

        LoginContext myContext = new LoginContext("Server", new CsiSpecialCallbackHandler(passwordIn));
        myContext.login();

        return myContext.getSubject();
    }
    */
}
