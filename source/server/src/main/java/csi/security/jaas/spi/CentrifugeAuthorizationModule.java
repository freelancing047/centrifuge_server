package csi.security.jaas.spi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.security.LoginService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.license.model.AbstractLicense;
import csi.log.LogThreadContextUtil;
import csi.security.jaas.JAASPrincipal;
import csi.security.jaas.JAASRole;
import csi.security.jaas.spi.callback.AuthorizationCallback;
import csi.security.jaas.spi.callback.CertCallback;
import csi.security.loginevent.EventReasons;
import csi.security.loginevent.LoginEventService;
import csi.security.queries.Users;
import csi.server.business.service.InternationalizationService;
import csi.server.common.identity.User;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.DateUtil;
import csi.startup.Product;

public class CentrifugeAuthorizationModule implements LoginModule {
   private static final Logger LOG = LogManager.getLogger(CentrifugeAuthorizationModule.class);

   public class UserInfo {
      String name;
      String password;
      String ldapName = null;
      String distinguishedName = null;
      Map<String,JAASPrincipal> roles = new HashMap<String,JAASPrincipal>();
   }

    private static final String DIVIDER_STRING = "\n==================================================================================\n";
    private static final String DN_SPECIAL_CHARACTERS = ",=#+;<>\\";
    private static final Pattern DN_TOKEN_PATTERN = Pattern.compile("\\{DN\\}");
    private static final Pattern USER_TOKEN_PATTERN = Pattern.compile("\\{USERNAME\\}");
    // SSL support
    private static final String PROPERTY_LDAP_KEY_FILE_KEY = "ldapKeyFile";
    private static final String PROPERTY_LDAP_KEY_TYPE_KEY = "ldapKeyFileType";
    private static final String PROPERTY_LDAP_KEY_PASSWORD_KEY = "ldapKeyPassword";
    private static final String PROPERTY_LDAP_TRUST_FILE_KEY = "ldapTrustFile";
    private static final String PROPERTY_LDAP_TRUST_TYPE_KEY = "ldapTrustFileType";
    private static final String PROPERTY_LDAP_TRUST_PASSWORD_KEY = "ldapTrustPassword";
    private static final String PROPERTY_EXTENDED_DEBUG = "ldapExtendedDebug";
    private static final String PROPERTY_TRUST_LDAP_SERVER_KEY = "ldapTrustserver.DEBUG_ONLY";
    // LDAP search
    private static final String PROPERTY_LDAP_CONNECTION= "ldapConnection";
    private static final String PROPERTY_LDAP_READER= "ldapReader";
    private static final String PROPERTY_LDAP_PASSWORD= "ldapPassword";
    private static final String PROPERTY_LDAP_BASE = "ldapBase";
    private static final String PROPERTY_LDAP_IDENTIFIER = "ldapIdentifier";
    private static final String PROPERTY_LDAP_CLASS = "ldapClass";
    private static final String PROPERTY_LDAP_ROLEMAP_FILE = "ldapRoleMapFile";

    private static final String PROPERTY_RESTFUL_ROLEMAP_FILE = "restfulRoleMapFile";
    private static final String PROPERTY_RESTFUL_ROLE_URL = "restfulRoleUrl";
    private static final String PROPERTY_RESTFUL_ROLE_TAG = "restfulRoleTag";

    private static final String PROPERTY_LOG_FAILURE = "logFailure";
    private static final String PROPERTY_LOG_SUCCESS = "logSuccess";
    private static final String PROPERTY_LOG_EXIT = "logExit";
    private static final String PROPERTY_DEBUG = "debug";
    private static final String PROPERTY_AUTO_REGISTER = "autoRegister";
    private static final String PROPERTY_ROLE_SOURCE = "roleSource";
    private static final String OBJECT_SID = "objectSid";

    private static final int RANDOM_PASSWORD_LENGTH = 64;

    private static final Set<String> ADMIN_USER_NAMES =
      new HashSet<String>(Arrays.asList(JAASRole.ADMIN_USER_NAME, JAASRole.SECURITY_USER_NAME));

    // Values set only once during first call to "initialize".
    private static Map<String, ?> _options = null;
    private static String _dataSourceName = "java:comp/env/jdbc/MetaDB";
    private static String _groupQuery = "select group_name from GroupMembershipView where role_name=?";

    private static Map<String, List<String>> _ldapRoleMap = null;
    private static Map<String, List<String>> _restfulRoleMap = null;
    private static String[] _ldapClass = null;
    private static boolean _autoRegister = false;
    private static boolean _logFailure = false;
    private static boolean _logSuccess = false;
    private static boolean _logExit = false;
    private static boolean _doDebug = false;
    private static boolean _doExtendedDebug = false;
    private static boolean _staticValuesSet = false;
    private static boolean _useJdbcRoles = false;
    private static boolean _useLdapRoles = false;
    private static boolean _useRestfulRoles = false;

    private static String _ldapConnection = null;
    private static String _ldapKeyFile = "conf/keystore.kdb";
    private static String _ldapKeyType = "jks";
    private static String _ldapKeyPassword = "changeit";
    private static String _ldapTrustFile = "conf/ldaptrust.jks";
    private static String _ldapTrustType = "jks";
    private static String _ldapTrustPassword = "changeit";
    private static String _ldapReader = null;
    private static String _ldapPassword = null;
    private static boolean _useAnonymous = false;
    private static boolean _useLdapSSL = false;
    private static boolean _trustLdapServer = false;
    private static boolean _useReaderCert = false;
    private static boolean _useSearchTLS = false;
    private static boolean _doEscape = true;
    private static String _ldapIdentifier = null;
    private static String _ldapBase = null;
    private static String _passwordMode = "simple";
    private static boolean _useClass = false;
    private static boolean _useDN = false;

    private static List<String> _restfulRoleUrlList = null;
    private static List<String> _restfulRoleTagList = null;

    // Working values for current object
    private CallbackHandler _callbackHandler = null;
    private UserInfo _currentUser = null;
    private DataSource _dataSource = null;
    private Subject _subject = null;
    private boolean _authorized = false;
    private boolean _committed = false;
    private String _userName = null;
    private AuthorizationMode _authorizationMode = null;

   public static <T> T lookupResource(Class<T> classIn, String resultUrlIn) throws NamingException {
      if (_doDebug) {
         LOG.info("<> <> <>  CentrifugeAuthorizationModule::lookupResource(Class<T> classIn, {})", () -> Format.value(resultUrlIn));
      }
      Context initCtx = null;

      try {
         initCtx = new InitialContext();

         return (T) initCtx.lookup(resultUrlIn);
      } finally {
         try {
            if (initCtx != null) {
               initCtx.close();
            }
         } catch (NamingException ne) {
            // ignore
         }
      }
   }

   @Override
   public void initialize(Subject subjectIn, CallbackHandler callbackHandlerIn, Map<String, ?> sharedStateIn, Map<String, ?> optionsIn) {
      if (!_staticValuesSet) {
         setStaticValues(optionsIn);
      }
      if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options)");
      }
      _subject = subjectIn;
      _callbackHandler = callbackHandlerIn;

      try {
         _dataSource = lookupResource(DataSource.class, _dataSourceName);

         if (_dataSource == null) {
            throw new IllegalStateException("Failed to initialize CentrifugeAuthorizationModule. Found null datasource bound to name: " + _dataSourceName);
         }
      } catch (NamingException ne) {
         throw new IllegalStateException("Failed to initialize CentrifugeAuthorizationModule.  Failed to find datasource with name: " + _dataSourceName, ne);
      }
      if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options)");
      }
   }

   @Override
   public boolean login() throws LoginException {
      if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::login()");
      }
      if (_callbackHandler == null) {
         throw new LoginException("no.callback.handler");
      }
      Map<String,String> properties = InternationalizationService.getProperties(LogThreadContextUtil.getLocale());
      Configuration.getInstance().getWebApplicationContext().getServletContext().setAttribute("REASON", properties.get("loginFailedText"));
      _currentUser = new UserInfo();

      try {
         try {
            Callback[] callbacks = new Callback[] { new AuthorizationCallback(), new CertCallback() };
            AuthorizationCallback authorizationCallback = (AuthorizationCallback) callbacks[0];
            _authorizationMode = authorizationCallback.getMode();
            _callbackHandler.handle(callbacks);

            _currentUser.name = authorizationCallback.getUsername();
            _userName = authorizationCallback.getUsername();
            _currentUser.password = authorizationCallback.getPassword();
            _currentUser.distinguishedName = authorizationCallback.getDN();

            ((AuthorizationCallback) callbacks[0]).clearPassword();
         } catch (Exception exception) {
            Callback[] callbacks = new Callback[] { new NameCallback("Username: ") };

            _callbackHandler.handle(callbacks);

            _currentUser.name = ((NameCallback) callbacks[0]).getName();
         }
         LogThreadContextUtil.putUserName(getUserName());

         if (_doDebug) {
            LOG.info("User:{}, Password:{}", () -> getUserName(), () -> Format.value(_currentUser.password, "*digested-password*"));
         }

         _authorized = authorize(_currentUser);
         Integer authMethod = CentrifugeAuthenticationModule.getAuthenticationMethod();
         if (_authorizationMode.SERVICE.isSet(authMethod)) {
             _authorized = true;
         }
         // if authenticated, get roles/groups
         if (_authorized) {
             AbstractLicense license = Product.getLicense();
             String userName = getUserName();

             if (license.addUserToLicense(userName, ADMIN_USER_NAMES.contains(userName))) {
                try {
                   populateGroups();
                   Users.recordLogon(userName);
                   LoginEventService.saveLoginSuccess(userName);
                } catch (Exception exception) {
                   throw new LoginException("Caught exception during post-authorization" + Format.value(exception));
                }
             } else {
                if (license.licenseAvailable(ADMIN_USER_NAMES.contains(userName))) {
                   Configuration.getInstance().getWebApplicationContext().getServletContext().setAttribute("REASON", properties.get("loginLicenseTime"));
                   throw new FailedLoginException(properties.get("loginLicenseTime"));
                }
                LoginEventService.saveLoginConcurrentLimit(userName);
                Configuration.getInstance().getWebApplicationContext().getServletContext().setAttribute("REASON", properties.get("loginLicenseConcurrent"));
                throw new FailedLoginException(properties.get("loginLicenseConcurrent"));
             }
         }
      } catch (Exception exception) {
         LOG.error("Caught exception while authorizing user {}:{}", () -> Format.value(getUserName()), () -> Format.value(exception));

         if (_doDebug) {
            LOG.info("<< << <<  CentrifugeAuthorizationModule::login() -->> {}", () -> Boolean.toString(_authorized));
         }
         throw new FailedLoginException(exception.getMessage());
      }
      LogThreadContextUtil.putUserName(getUserName());

      if (_authorized) {
         if (_doDebug || _logSuccess) {
            LOG.info("** Authorization succeeded for user {} **", () -> Format.value(getUserName()));
         }
      } else {
         String failure = new StringBuilder("!! Authorization failed for user ")
                                    .append(Format.value(getUserName()))
                                    .append(" !!").toString();

         if (_doDebug || _logFailure) {
            LOG.info(failure);
         }
         if (_doDebug) {
            LOG.info("<< << <<  CentrifugeAuthorizationModule::login() -->> {}", () -> Boolean.toString(_authorized));
         }
         throw new FailedLoginException(failure);
      }
      if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::login() -->> {}", () -> Boolean.toString(_authorized));
      }
      return _authorized;
   }

   @Override
   public boolean commit() throws LoginException {
      if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::commit()");
      }
      if (_authorized) {
         _committed = populateSubject();
      } else {
         _currentUser = null;
         _committed = false;
      }
      if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::commit()");
      }
      return _committed;
   }

   @Override
   public boolean abort() throws LoginException {
      if (_doDebug) {
         LOG.info("<> <> <>  CentrifugeAuthorizationModule::abort()");
      }
       try {
            Callback[] callbacks = new Callback[] { new AuthorizationCallback() };
            AuthorizationCallback authorizationCallback = (AuthorizationCallback) callbacks[0];
            _callbackHandler.handle(callbacks);
             LoginEventService.saveLoginBadPassword(authorizationCallback.getUsername());
       } catch (Exception e) {
           LoginEventService.saveLoginBadPassword("null");
       }
      _currentUser = null;
      return true;
   }

   @Override
   public boolean logout() throws LoginException {
      String userName = (_currentUser == null) ? "???" : getUserName();

      if (_doDebug) {
         LOG.info("<> <> <>  CentrifugeAuthorizationModule::logout()");
      }
      if (_doDebug || _logExit) {
         LOG.info("** User {} logged out **", () -> display(userName));
      }
      Product.getLicense().removeUserFromLicense(userName, EventReasons.LOGOUT_SUCCESS);
      LoginEventService.saveLogoutSuccess(userName);
      _currentUser = null;
      return true;
   }

   private boolean authorize(UserInfo userIn) {
      boolean authorization = false;

      if ((userIn != null) && StringUtils.isNotEmpty(userIn.name)) {
         String name = userIn.name.toLowerCase();

         if (_doDebug) {
            LOG.info(">> >> >>  CentrifugeAuthorizationModule::authorize({} ({}), {})",
                     () -> Format.value(userIn.name), () -> Format.value(name),
                     () -> Format.value(userIn.password, "*digested-password*"));
         }
         User user = null;

         try {
            // Make sure expired user accounts have been disabled sometime today
            Users.checkExpiration(false);

            // Retrieve information if this user account exists
            user = Users.getUserByName(name);

            if ((user != null) && user.isDisabled() &&
                (user.isPerpetual().booleanValue() ||
                 ((user.getExpirationDateTime() == null) ||
                  user.getExpirationDateTime().atZone(ZoneId.systemDefault()).isAfter(ZonedDateTime.now())))) {
               user.setDisabled(Boolean.FALSE);
               Users.update(user);
            }
         } catch (Exception exception) {
            LOG.error("Caught exception while attempting to authorize user {}", Format.value(getUserName()), exception);
         }
         if ((user == null) && _autoRegister) {
            try {
               if (userIn.password == null) {
                  userIn.password = randomPassword();
               }
               addUser(userIn);
            } catch (Exception exception) {
               LOG.error("Caught exception while attempting to auto-register user {}", Format.value(getUserName()), exception);
            }
            try {
               user = Users.getUserByName(name);
            } catch (Exception exception) {
               LOG.error("Caught exception while attempting to authorize user {} after auto-registering.", Format.value(getUserName()), exception);
            }
         }
         if (user == null) {
            LoginEventService.saveLoginUnknownUser(name);
         } else {
            if (user.isDisabled()) {
               LoginEventService.saveLoginDisabled(name);
            } else {
               authorization = true;
            }
         }
         CsiPersistenceManager.close();

         if (_doDebug) {
            String isAuthorized = Boolean.toString(authorization);

            LOG.info(" << << << CentrifugeAuthorizationModule::authorize({}, {}) -->> {}", () -> Format.value(userIn.name),
                     () -> Format.value(userIn.password, "*digested-password*"), () -> isAuthorized);
         }
      }
      return authorization;
   }

   private boolean populateSubject() {
      boolean success = false;

      if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::populateSubject()");
      }
      JAASPrincipal myPrincipal = new JAASPrincipal(getUserName().toLowerCase(), getDistinguishedName());
      Set<Principal> myPrincipals = _subject.getPrincipals();

      myPrincipals.add(myPrincipal);

      if (_currentUser.password != null) {
         _subject.getPrivateCredentials().add(_currentUser.password);
      }
      myPrincipals.addAll(_currentUser.roles.values());
      myPrincipals.add(new JAASRole("Authenticated"));
      LogThreadContextUtil.putUserName(getUserName());
      LogThreadContextUtil.putDistinguishedName(getDistinguishedName());

      success = true;

      if (_doDebug) {
         LOG.info("           - user roles ...");

         for (Principal myValue : myPrincipals) {
            LOG.info("             -- {} :: {} :: {}", () -> Format.value(myValue.toString()),
                     () -> Format.value(myValue.getName()), () -> Integer.toString(myValue.hashCode()));
         }
         LOG.info("<< << <<  CentrifugeAuthorizationModule::populateSubject() -->> " + Boolean.toString(success));
      }
      return success;
   }

    /**
     * Populates this user's roles with all known groups the user is a member.
     *
     * This performs a nested search such that Groups of Groups are resolved.
     * For example if the user foo is a member of G1, G1 is a member of G2, then
     * the resolved roles for the user include both G1 and G2.
     *
     * @throws Exception
     */
   private void populateGroups() throws Exception {
      String myName = getUserName().toLowerCase();

      if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::populateGroups({} ({}), {})",
                  () -> Format.value(getUserName()), () -> Format.value(myName),
                  () -> Format.value(_currentUser.password, "*digested-password*"));
      }
      if (_groupQuery != null) {
         try (Connection connection = getConnection();
              PreparedStatement statement = connection.prepareStatement(_groupQuery)) {
            Deque<String> pendingList = new LinkedList<String>();

            if (_useLdapRoles) {
                loadLdapRoles(pendingList);
            }
            if (_useRestfulRoles) {
                int myUrlCount = _restfulRoleUrlList.size();
                int myTagCount = _restfulRoleTagList.size();

                for (int i = 0; i < myUrlCount; i++) {
                    int myTagIndex = (myTagCount > i) ? i : myTagCount - 1;

                    loadRestfulRoles(_restfulRoleUrlList.get(i), _restfulRoleTagList.get(myTagIndex), pendingList);
                }
            }
            if (_useJdbcRoles) {
                statement.setString(1, myName);

                try (ResultSet results = statement.executeQuery()) {
                   if (LOG.isDebugEnabled()) {
                      LOG.debug("searching for group membership");
                   }
                   // search for the first level of group membership for this user --
                   // we'll skip any groups for which there is already an existing role name
                   while (results.next()) {
                      String groupRole = results.getString(1).toLowerCase();

                      if (!_currentUser.roles.containsKey(groupRole)) {
                         JAASRole role = new JAASRole(groupRole);

                         _currentUser.roles.put(groupRole, role);
                         pendingList.push(groupRole);
                      }
                   }
                }
            }
            String myAllUserString = "everyone";
            JAASRole myAllUserRole = new JAASRole(myAllUserString);

            _currentUser.roles.put(myAllUserString, myAllUserRole);
            pendingList.push(myAllUserString);

            if (_doDebug) {
               LOG.info("           - First Level Group search complete.");
               LOG.info("           - Checking for nested group membership");
            }
            while (!pendingList.isEmpty()) {
                String name = pendingList.pop();

                statement.setString(1, name);

                try (ResultSet results = statement.executeQuery()) {
                   while (results.next()) {
                      String nested = results.getString(1).toLowerCase();

                      if (!_currentUser.roles.containsKey(nested)) {
                         JAASRole role = new JAASRole(nested);

                         _currentUser.roles.put(nested, role);
                         pendingList.push(nested);
                      }
                   }
                }
            }
         }
         if (_doDebug) {
            LOG.info("<< << <<  CentrifugeAuthorizationModule::populateGroups({}, {})",
                      () -> Format.value(getUserName()),
                      () -> Format.value(_currentUser.password, "*digested-password*"));
         }
      }
   }

   private static boolean addUser(UserInfo userIn) {
      boolean success = false;
      String name = userIn.name.toLowerCase();

      if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::addUser({}, {})",
                  () -> Format.value(userIn.name), () -> Format.value(userIn.password, "*digested-password*"));
      }
      SecurityPolicyConfig policy = Configuration.getInstance().getSecurityPolicyConfig();
      User user = new User();

      user.setName(name);
      user.setRemark("Auto-registered");
      policy.applyExpirationPolicy(user);

      if (userIn.password != null) {
         user.setPassword(userIn.password);
      }
      try {
         if (_doDebug) {
            LOG.info("           - Auto-registering new user {} ({}) on {}.",
                     () -> Format.value(userIn.name), () -> Format.value(name),
                     () -> ZonedDateTime.now().format(DateUtil.JAVA_UTIL_DATE_TOSTRING_FORMATTER));
         }
         CsiPersistenceManager.begin();

         if (_doDebug) {
            LOG.info("           - Add new user {} ({}).", () -> Format.value(userIn.name), () -> Format.value(name));
         }
         Users.add(user);

         if (_doDebug) {
            LOG.info("           - Commit transaction.");
         }
         CsiPersistenceManager.commit();
         success = true;
      } catch (Exception exception) {
         if (_doDebug) {
            LOG.info("Auto-Registration encountered errors while attempting to add a new user {0}",
                     Format.value(userIn.name), exception);
         }
      }
      if (_doDebug) {
         LOG.info(" << << << AutoRegistrationModule::addUser({} ({}), {})", () -> Format.value(userIn.name),
                  () -> Format.value(name), () -> Format.value(userIn.password, "*digested-password*"));
      }
      return success;
   }

   protected Connection getConnection() throws Exception {
      if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::getConnection()");
      }
      Connection connection = _dataSource.getConnection();

      if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::getConnection()");
      }
      return connection;
   }

   private String randomPassword() {
      StringBuilder myBuffer = new StringBuilder(RANDOM_PASSWORD_LENGTH);
      Random myGenerator = new Random();
      int myBase = 33;
      int myRange = 254 - myBase;

      for (int i = 0; i < RANDOM_PASSWORD_LENGTH; i++) {
         myBuffer.append((char)(myBase + myGenerator.nextInt(myRange)));
      }
      return myBuffer.toString();
   }

   protected void loadLdapRoles(Deque<String> pendingListIn) {
      if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::loadLdapRoles()");
      }
      Hashtable<String,String> env = new Hashtable<String,String>();

      if (_useLdapSSL) {
         prepareLdapSSL(env, _useReaderCert);
      }
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, _ldapConnection);

      if ((_useReaderCert || _useAnonymous || ((_ldapPassword != null) && (_ldapPassword.length() > 0))) &&
          (_ldapIdentifier != null) && (_ldapIdentifier.length() > 0)) {
         // Define the LDAP context and results enumeration outside the try-catch structure
         // so they can be properly closed even if an exception is encountered.
         LdapContext readerContext = null;
         NamingEnumeration<SearchResult> response = null;

         // Complete environment for authenticating
         if (_useReaderCert) {
            if (!_useSearchTLS) {
               if (_doDebug) {
                  LOG.info("           - connect to LDAP using reader cert");
               }
               env.put(Context.SECURITY_AUTHENTICATION, "EXTERNAL");
            }
         } else if (_useAnonymous) {
            if (_doDebug) {
               LOG.info("           - connect to LDAP using anonymous bind");
            }
            env.put(Context.SECURITY_AUTHENTICATION, "none");
         } else {
            if (_doDebug) {
               LOG.info("           - connect to LDAP as " + display(_ldapReader) + " with "
                     + display(_ldapPassword, "*configured-password*"));
            }
            env.put(Context.SECURITY_AUTHENTICATION, _passwordMode);
            env.put(Context.SECURITY_PRINCIPAL, _ldapReader);
            env.put(Context.SECURITY_CREDENTIALS, _ldapPassword);
         }
         //specify attributes to be returned in binary format
         env.put("java.naming.ldap.attributes.binary", OBJECT_SID);

         try {
            readerContext = new InitialLdapContext(env, null);

            if (_useReaderCert && _useSearchTLS) {
               if (_doDebug) {
                  LOG.info("           - connect to LDAP using reader cert");
               }
               StartTlsResponse tls = (StartTlsResponse) readerContext.extendedOperation(new StartTlsRequest());

               tls.negotiate();
               readerContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, "EXTERNAL");
            }
            // reader is authenticated
            if (_doDebug && !_useAnonymous) {
               LOG.info("           - LDAP reader is authenticated.");
            }
            // search for user
            String base = (_ldapBase == null) ? "" : _ldapBase;

            for (Map.Entry<String,List<String>> entry : _ldapRoleMap.entrySet()) {
               String key = entry.getKey();
               String filter = createFilter(key);
               response = searchLdap(readerContext, base, filter, SearchControls.SUBTREE_SCOPE, null);

               if (response.hasMoreElements()) {
                  List<String> roles = _ldapRoleMap.get(key);

                  if (roles != null) {
                     for (String roleName : roles) {
                        JAASRole role = new JAASRole(roleName);

                        if (roleName != null) {
                           _currentUser.roles.put(roleName, role);
                           pendingListIn.push(roleName);
                        }
                     }
                  }
               }
            }
         } catch (AuthenticationException ae) {
            // Authentication failed
            if (_doDebug) {
               LOG.info("           - LDAP authetication failed.");
            }
         } catch (NamingException ne) {
            // Attempt to authenticate anonymously encountered
            if (_doDebug) {
               LOG.info("           - Naming Exception generated by LDAP" + display(ne.toString(true)));
               //if (_doDebug) LOG.info("------------------------------------------------------------------------------------------");
               //if (_doDebug) myException.printStackTrace();
            }
         } catch (Exception exception) {
            if (_doDebug) {
               LOG.info("           - caught exception:" + display(exception));
            }
         } finally {
            // Close the LDAP results enumeration if it exists
            close(response);
            // Close the LDAP context if it exists
            close(readerContext);
         }
      } else {
         LOG.error("Missing required information to perform an LDAP search.");
      }
      if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::loadLdapRoles()");
      }
   }

    public static String createFilterFromList(String tokenIn, String[] listIn)
    {
        if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::createFilterFromList(" + display(tokenIn) + ", String[] listIn)");
      }

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

        if (_doDebug) {
         LOG.info("           -- filter:\"" + myString + "\"");
      }

        if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::createFilterFromList(" + display(tokenIn) + ", String[] listIn) -->> " + display(myString));
      }

        return myString;
    }

    private String createFilter(String groupIn)
    {
        if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::createFilter(" + display(getUserName()) + ")");
      }

        String myUser = getUserName();
        String myTag = _ldapIdentifier;
        String myString = "(&(" + myTag + "=" + myUser + ")"
                    + (_useClass ? createFilterFromList("objectClass", _ldapClass) : "")
                    + createFilterFromList("memberOf", new String[]{groupIn}) + ")";

        if (_doDebug) {
         LOG.info("           -- filter:\"" + myString + "\"");
      }

        if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::createFilter(" + display(getUserName()) + ") -->> " + display(myString));
      }

        return myString;
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

    private static String escapeLdapIdentifier(String nameIn)
    {
        String myString = nameIn;

        if (_doDebug) {
         LOG.info(">> >> >>  CentrifugeAuthorizationModule::escapeDN(" + display(nameIn) + ")");
      }

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

        if (_doDebug) {
         LOG.info("<< << <<  CentrifugeAuthorizationModule::escapeDN(" + display(nameIn) + ") -->> " + display(myString));
      }

        return myString;
    }

    private static void prepareLdapSSL(Hashtable<String, String> environmentIn, boolean SetKeyStore)
    {
        if (SetKeyStore)
        {
            if (_doDebug) {
               LOG.info("           - using SSL and keystore " + display(_ldapKeyFile) + " of type " + display(_ldapKeyType));
            }

            System.setProperty("javax.net.ssl.keyStore", _ldapKeyFile);
            System.setProperty("javax.net.ssl.keyStoreType", _ldapKeyType);
            System.setProperty("javax.net.ssl.keyStorePassword", _ldapKeyPassword);
        }

        if (_doDebug) {
         LOG.info("           - using SSL and truststore " + display(_ldapTrustFile) + " of type " + display(_ldapTrustType));
      }

        System.setProperty("javax.net.ssl.trustStore", _ldapTrustFile);
        System.setProperty("javax.net.ssl.trustStoreType", _ldapTrustType);
        System.setProperty("javax.net.ssl.trustStorePassword", _ldapTrustPassword);
        //myEnvironment.put(Context.SECURITY_PROTOCOL, "ssl");
        if (_trustLdapServer)
        {
            environmentIn.put("java.naming.ldap.factory.socket", "csi.security.jaas.spi.SSLTestingWorkaround");
        }
    }

    private static void verifyTrustStore() {
       if (_doDebug) {
          LOG.info(">> >> >>  CentrifugeAuthorizationModule::verifyTrustStore()");
       }
       char[] myPassword = _ldapTrustPassword.toCharArray();
       int myCertificateCount = 0;

       try (FileInputStream myStream = new FileInputStream(_ldapTrustFile)) {
          KeyStore myTruststore = KeyStore.getInstance(_ldapTrustType);

          myTruststore.load(myStream, myPassword);

          for (Enumeration<String> myAliases = myTruststore.aliases(); myAliases.hasMoreElements();) {
             String myAlias = myAliases.nextElement();
             Certificate myCertificate = myTruststore.getCertificate(myAlias);

             myCertificateCount++;

             if (_doExtendedDebug) {
                LOG.info(DIVIDER_STRING + "(" + Integer.toString(myCertificateCount) + ") Certificate alias = " + myAlias
                      + DIVIDER_STRING + myCertificate.toString() + DIVIDER_STRING);
             }
          }
       } catch (Exception myException) {
          LOG.error("\nCaught exception loading truststore file " + display(_ldapTrustFile) + " of type "
                + display(_ldapTrustType) + " using password "
                + display(_ldapTrustPassword, "<" + PROPERTY_LDAP_TRUST_PASSWORD_KEY + ">") + "."
                + display(myException));
       }
       if (_doDebug) {
          LOG.info("<< << <<  CentrifugeAuthorizationModule::verifyTrustStore() -->> " + Integer.toString(myCertificateCount)
                + " certs");
       }
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

    private static void setStaticValues(Map<String, ?> optionsIn)
    {
        if (null != optionsIn) {

            String myLogExit = (String)optionsIn.get(PROPERTY_LOG_EXIT);
            String myDoDebug = (String)optionsIn.get(PROPERTY_DEBUG);
            String myDoExtendedDebug = (String)optionsIn.get(PROPERTY_EXTENDED_DEBUG);

            _options = optionsIn;
            _logExit = ((null != myLogExit) && myLogExit.trim().equalsIgnoreCase("true"));
            _doDebug = ((null != myDoDebug) && myDoDebug.trim().equalsIgnoreCase("true"));
            _doExtendedDebug = ((null != myDoExtendedDebug) && myDoExtendedDebug.trim().equalsIgnoreCase("true"));

            if (_doDebug) {
               LOG.info(">> >> >>  CentrifugeAuthorizationModule::setStaticValues()");
            }

            String myRoleSourceList = (null != optionsIn.get(PROPERTY_ROLE_SOURCE))
                    ? ((String)optionsIn.get(PROPERTY_ROLE_SOURCE)).toLowerCase() : null;
            String myAutoRegister = (null != optionsIn.get(PROPERTY_AUTO_REGISTER))
                    ? ((String) optionsIn.get(PROPERTY_AUTO_REGISTER)).toLowerCase().trim() : null;
            String myLogFailure = (null != optionsIn.get(PROPERTY_LOG_FAILURE))
                    ? ((String) optionsIn.get(PROPERTY_LOG_FAILURE)).toLowerCase().trim() : null;
            String myLogSuccess = (null != optionsIn.get(PROPERTY_LOG_SUCCESS))
                    ? ((String) optionsIn.get(PROPERTY_LOG_SUCCESS)).toLowerCase().trim() : null;

            String[] myRoleSources = ((null != myRoleSourceList) && (0 < myRoleSourceList.length()))
                    ? myRoleSourceList.split("\\|") : null;

            if ((null != myRoleSources) && (0 < myRoleSources.length)) {

                for (int i = 0; myRoleSources.length > i; i++) {

                    String mySource = myRoleSources[i].trim();

                    if ("jdbc".equals(mySource)) {

                        _useJdbcRoles = true;

                    } else if ("ldap".equals(mySource)) {

                        _useLdapRoles = true;

                    } else if ("rest".equals(mySource)) {

                        _useRestfulRoles = true;
                    }
                }
            }

            if (_useLdapRoles)
            {
                String myLdapRoleMapFile = (String)optionsIn.get(PROPERTY_LDAP_ROLEMAP_FILE);

                _ldapConnection = (String)optionsIn.get(PROPERTY_LDAP_CONNECTION);

                if ((null != _ldapConnection) && (8 < _ldapConnection.length())) {

                    _ldapRoleMap = new HashMap<String, List<String>>();

                    if ((null != myLdapRoleMapFile) && (0 < myLdapRoleMapFile.length()))
                    {
                        loadRoleMapFromFile(_ldapRoleMap, myLdapRoleMapFile);

                        if ((null != _ldapRoleMap.keySet()) || !_ldapRoleMap.isEmpty()) {

                            String myLdapClassList = (String)optionsIn.get(PROPERTY_LDAP_CLASS);
                            String myLdapKeyFile = (String)optionsIn.get(PROPERTY_LDAP_KEY_FILE_KEY);
                            String myLdapKeyType = (String)optionsIn.get(PROPERTY_LDAP_KEY_TYPE_KEY);
                            String myLdapKeyPassword = (String)optionsIn.get(PROPERTY_LDAP_KEY_PASSWORD_KEY);
                            String myLdapTrustFile = (String)optionsIn.get(PROPERTY_LDAP_TRUST_FILE_KEY);
                            String myLdapTrustType = (String)optionsIn.get(PROPERTY_LDAP_TRUST_TYPE_KEY);
                            String myLdapTrustPassword = (String)optionsIn.get(PROPERTY_LDAP_TRUST_PASSWORD_KEY);
                            String myTrustLdapServer = (String)optionsIn.get(PROPERTY_TRUST_LDAP_SERVER_KEY);

                            _ldapReader = (String)optionsIn.get(PROPERTY_LDAP_READER);
                            _ldapPassword = (String)optionsIn.get(PROPERTY_LDAP_PASSWORD);
                            _ldapIdentifier = (String)optionsIn.get(PROPERTY_LDAP_IDENTIFIER);
                            _ldapBase = (String)optionsIn.get(PROPERTY_LDAP_BASE);

                            _useClass = ((null != _ldapClass) && (0 < _ldapClass.length));
                            _useAnonymous = ((null == _ldapReader) || (0 >= _ldapReader.length()));
                            _useReaderCert = ((null != _ldapReader) && (1 == _ldapReader.length()) && ('*' == _ldapReader.charAt(0)));

                            if ((null != myLdapClassList) && (0 < myLdapClassList.length()))
                            {
                                _ldapClass = myLdapClassList.split("\\|");
                            }
                            if (_useReaderCert && _ldapConnection.substring(0, 7).equalsIgnoreCase("ldap://"))
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

                        } else {

                            _ldapRoleMap = null;
                        }
                    }
                }
                else
                {
                    LOG.error("LDAP not configured correctly!");
                }
            }
            if (_useRestfulRoles)
            {
                String myRestfulRoleMapFile = (String) optionsIn.get(PROPERTY_RESTFUL_ROLEMAP_FILE);
                String myRestfulRollUrlString = (String) optionsIn.get(PROPERTY_RESTFUL_ROLE_URL);
                String myRestfulRollTagString = (String) optionsIn.get(PROPERTY_RESTFUL_ROLE_TAG);

                if ((null != myRestfulRoleMapFile) && (null != myRestfulRollUrlString) && (null != myRestfulRollTagString)) {

                    String[] myRestfulRollUrlArray = myRestfulRollUrlString.split("\\|");
                    String[] myRestfulRollTagArray = myRestfulRollTagString.split("\\|");

                    _restfulRoleUrlList = new ArrayList<String>();
                    _restfulRoleTagList = new ArrayList<String>();
                    myRestfulRoleMapFile = myRestfulRoleMapFile.trim();

                    for (int i = 0; myRestfulRollUrlArray.length > i; i++) {

                        String myRestfulRollUrl = myRestfulRollUrlArray[i].trim();

                        if (0 < myRestfulRollUrl.length()) {

                            _restfulRoleUrlList.add(myRestfulRollUrl);
                        }
                    }
                    for (int i = 0; myRestfulRollTagArray.length > i; i++) {

                        String myRestfulRollTag = myRestfulRollTagArray[i].trim();

                        if (0 < myRestfulRollTag.length()) {

                            _restfulRoleTagList.add(myRestfulRollTag);
                        }
                    }

                    if ((0 < myRestfulRoleMapFile.length())
                            && !_restfulRoleUrlList.isEmpty() && !_restfulRoleTagList.isEmpty()) {

                        _restfulRoleMap = new HashMap<String, List<String>>();
                        _restfulRoleMap = loadRoleMapFromFile(_restfulRoleMap, myRestfulRoleMapFile);

                    } else {

                        _restfulRoleUrlList = null;
                        _restfulRoleTagList = null;
                        _restfulRoleMap = null;
                    }
                }
                if ((null == _restfulRoleUrlList) || (null == _restfulRoleTagList) || (null == _restfulRoleMap)) {

                    _useRestfulRoles = false;
                }
            }
            _useLdapRoles = ((null != _ldapRoleMap)
                            && (null != _ldapRoleMap.keySet()) && !_ldapRoleMap.isEmpty());
            _useRestfulRoles = ((null != _restfulRoleMap)
                                && (null != _restfulRoleMap.keySet()) && !_restfulRoleMap.isEmpty());

            _autoRegister = "true".equals(myAutoRegister);
            _logFailure = "true".equals(myLogFailure);
            _logSuccess = "true".equals(myLogSuccess);
            _staticValuesSet = true;

            if (_doDebug) {
               LOG.info("           - myAutoRegister = " + Boolean.toString(_autoRegister));
            }

            if (_doDebug) {
               LOG.info("<< << <<  CentrifugeAuthorizationModule::setStaticValues()");
            }
        }
        if ((!_useLdapRoles && !_useRestfulRoles)) {

            _useJdbcRoles = true;
        }
    }

   private static Map<String, List<String>> loadRoleMapFromFile(Map<String, List<String>> mapIn, String filePathIn) {
      Map<String, List<String>> myMap = (null != mapIn) ? mapIn : new TreeMap<String, List<String>>();

      try (FileReader fileReader = new FileReader(filePathIn);
           BufferedReader reader = new BufferedReader(fileReader)) {
         for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] values = line.split("\\|");

            if (values.length > 1) {
               String group = values[0].trim();
               List<String> roles = myMap.get(group);

               if (roles == null) {
                  roles = new ArrayList<String>(values.length - 1);
               }
               for (int i = 1; i < values.length; i++) {
                  String role = values[i].trim().toLowerCase();

                  if (role.length() > 0) {
                     roles.add(role);
                  }
               }
               if (!roles.isEmpty()) {
                  myMap.put(group, roles);
               }
            }
         }
      } catch (Exception myException) {
         LOG.error("Caught exception reading role-mapping file {0}", display(filePathIn), myException);
      }
      return myMap;
   }

   private void loadRestfulRoles(String roleUrlIn, String roleTagIn, Deque<String> pendingListIn) {
      String formattedRequest = substituteValues(roleUrlIn);

      try {
         URL url = new URL(formattedRequest);
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();

         try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");

            for (String group : extractUserGroups(connection, roleTagIn)) {
               List<String> roles = _restfulRoleMap.get(group);

               if (roles != null) {
                  for (String roleName : roles) {
                     if (roleName != null) {
                        JAASRole role = new JAASRole(roleName);

                        _currentUser.roles.put(roleName, role);
                        pendingListIn.push(roleName);
                     }
                  }
               }
            }
         } catch (FileNotFoundException fnfe) {
            LOG.error("URL {} returned File Not Found.", () -> Format.value(formattedRequest));
         } catch (Exception exception) {
            LOG.error("Caught exception while importing user roles from {0}", Format.value(formattedRequest), exception);
         } finally {
            connection.disconnect();
         }
      } catch (Exception exception) {
         LOG.error("Caught exception opening connection to {0}", Format.value(formattedRequest), exception);
      }
   }

   private static List<String> extractUserGroups(HttpURLConnection connection, String groupTagIn) throws Exception {
      List<String> groups = Collections.emptyList();

      if (connection != null) {
         try (InputStream streamIn = connection.getInputStream()) {
            DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder myBuilder = myFactory.newDocumentBuilder();
            Document myDocument = myBuilder.parse(streamIn);
            Element myRoot = myDocument.getDocumentElement();
            NodeList myNodeList = myRoot.getElementsByTagName(groupTagIn);
            int myNodeCount = myNodeList.getLength();

            if (myNodeCount > 0) {
               groups = new ArrayList<String>();

               for (int i = 0; i < myNodeCount; i++) {
                  Node myNode = myNodeList.item(i);
                  Node myData = myNode.getFirstChild();
                  String myGroup = (myData != null) ? myData.getNodeValue() : null;

                  if (myGroup != null) {
                     myGroup = myGroup.trim();

                     if (myGroup.length() > 0) {
                        groups.add(myGroup);
                     }
                  }
               }
            }
         }
      }
      return groups;
   }

   private String substituteValues(String stringIn) {
      String result = null;

      if (stringIn != null) {
         result = USER_TOKEN_PATTERN.matcher(stringIn).replaceAll((_currentUser.name == null) ? "" : _currentUser.name);
         result = DN_TOKEN_PATTERN.matcher(result).replaceAll((_currentUser.distinguishedName == null) ? "" : _currentUser.distinguishedName);
      }
      return result;
   }

   private String getUserName() {
      return (_currentUser == null) ? "" : _currentUser.name;
   }

   private String getDistinguishedName() {
      return (_currentUser == null) ? null : _currentUser.distinguishedName;
   }
}
