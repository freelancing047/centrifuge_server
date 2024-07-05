package csi.security;

import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import csi.config.Configuration;
import csi.container.tomcat.security.ApiAuthenticator;
import csi.security.jaas.JAASRole;
import csi.security.loginevent.LoginEventService;
import csi.security.monitors.CapcoRollup;
import csi.security.queries.AclRequest;
import csi.security.queries.Users;
import csi.security.spi.AuthorizationContext;
import csi.security.spi.AuthorizationService;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.identity.Role;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.publishing.Asset;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.connector.ConnectionFactoryNotFoundException;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskHelper;
import csi.startup.CleanUpThread;
import csi.startup.Product;

// TODO: need to be able to get to the underlying security provider find users, roles, etc.
// Some of these methods assume we're using the JPA user object.  We can't assume that the security provider
// is our implementation (DB based).  It could be ldap or others.
public class CsiSecurityManager {
    private static final Logger LOG = LogManager.getLogger(CsiSecurityManager.class);

    static ThreadLocal<Authorization> localAuthorization;

    private static Long _authorizedThreadId = null;

    @Inject
    static AuthorizationService AuthService;

    static {
        localAuthorization = new ThreadLocal<Authorization>();
    }

    private static final String ACL_Search = "select acl from ACL acl where acl.uuid = :uuid order by acl.id desc";

    private static CapcoRollup _capcoRollup = null;
    private static CapcoInfo _capcoInfo = null;

    public static Authorization getAuthorization() {
        return localAuthorization.get();
    }

    public static void setAuthorization(Authorization authz) {
        localAuthorization.set(authz);
    }

    public static boolean isAuthorized(String uuidIn, AclControlType[] permissionsIn) {

        return isAuthorized(uuidIn, permissionsIn, true);
    }

    public static boolean isAuthorized(String uuidIn, AclControlType[] permissionsIn, boolean doSecurityIn) {

        boolean myAuthorization = false;

        try {

            myAuthorization = AclRequest.isAuthorized(uuidIn, permissionsIn, doSecurityIn);

        } catch (CsiSecurityException myException) {

            LOG.error("Caught Exception while checking authorization:\n{}", () -> Format.value(myException));
        }
        return myAuthorization;
    }

    public static boolean isAuthorizedAll(String uuidIn, AclControlType[] permissionsIn, boolean doSecurityIn) {

        boolean myAuthorization = false;

        try {

            myAuthorization = AclRequest.isAuthorizedAll(uuidIn, permissionsIn, doSecurityIn);

        } catch (CsiSecurityException myException) {

            LOG.error("Caught Exception while checking authorization:\n{}", () -> Format.value(myException));
        }
        return myAuthorization;
    }

    public static void clearAuthorization() {
        localAuthorization.remove();
    }

    public static String getUserName() {
        Authorization myAuthorization = getAuthorization();
        return (null != myAuthorization) ? myAuthorization.getName() : null;
    }

   private static String getUserNameNonNull() {
      String userName = getUserName();

      return (userName == null) ? "" : userName;
   }

    public static String getDistinguishedName() {
        Authorization myAuthorization = getAuthorization();
        return (null != myAuthorization) ? myAuthorization.getDistinguishedName() : null;
    }

    public static boolean isAdmin() {
        return hasRole(JAASRole.ADMIN_ROLE_NAME) || getUserNameNonNull().equalsIgnoreCase(JAASRole.ADMIN_USER_NAME);
    }

    public static boolean isSecurity() {
        return hasRole(JAASRole.SECURITY_ROLE_NAME) || getUserNameNonNull().equalsIgnoreCase(JAASRole.SECURITY_USER_NAME);
    }

    public static boolean isOriginator() {
        return hasRole(JAASRole.ORIGINATOR_ROLE_NAME) || getUserNameNonNull().equalsIgnoreCase(JAASRole.SECURITY_USER_NAME);
    }

    public static boolean isAuthorizedThread() {

        Thread myThread = Thread.currentThread();

        return (myThread instanceof CleanUpThread) && (myThread.getId() == _authorizedThreadId);
    }

    public static boolean isSpecialAccess() {

        return isAdmin() || isSecurity() || isAuthorizedThread();
    }

    public static boolean isRestricted() {

        String myUser = getUserNameNonNull().toLowerCase();
        return myUser.equals(JAASRole.ADMIN_USER_NAME)
                || myUser.equals(JAASRole.SECURITY_USER_NAME);
    }

    public static boolean isAuthorized(String uuid, AclControlType operation) {

        AclControlType[] myOperations = (null != operation) ? new AclControlType[]{operation} : null;

        return Product.inStartUp() || isAuthorizedThread() || isAuthorized(uuid, myOperations);
    }

    public static boolean isAuthorized(String uuid, AclControlType operation, boolean doSecurityIn) {

        AclControlType[] myOperations = (null != operation) ? new AclControlType[]{operation} : null;

        return Product.inStartUp() || isAuthorizedThread() || isAuthorized(uuid, myOperations, doSecurityIn);
    }

    public static boolean hasRole(String role) {
        return getAuthorization().hasRole(role);
    }

    public static boolean hasAnyRole(String[] roles) {
        return getAuthorization().hasAnyRole(roles);
    }

    public static boolean hasAllRoles(String[] roles) {
        return getAuthorization().hasAllRoles(roles);
    }

    public static Set<String> getUserRoles() {
        return getAuthorization().listRoles();
    }

   public static boolean isIconAdmin() {
      boolean result = true;
      String iconAdmin = Configuration.getInstance().getApplicationConfig().getIconManagementAccess();

      if ((iconAdmin != null) && (iconAdmin.length() > 0)) {
         result = false;

         for (String role : getUserRoles()) {
            if (role.equalsIgnoreCase(iconAdmin)) {
               result = true;
               break;
            }
         }
      }
      return result;
   }

   public static boolean isFieldListAdmin() {
      boolean result = true;
      String fieldListAdmin = Configuration.getInstance().getApplicationConfig().getAllowInUseFieldTypeChange();

      if ((fieldListAdmin != null) && (fieldListAdmin.length() > 0)) {
         result = false;

         for (String role : getUserRoles()) {
            if (role.equalsIgnoreCase(fieldListAdmin)) {
               result = true;
               break;
            }
         }
      }
      return result;
   }

    public static String getDefaultSecurityBanner() {

        String myBanner = Configuration.getInstance().getSecurityPolicyConfig().getDefaultBanner();

        if ((null == myBanner) || (0 == myBanner.length())) {

            CapcoInfo myCapcoInfo = getCapcoInfo();
            Boolean myAbreviationsFlag = Configuration.getInstance().getSecurityPolicyConfig().getUseAbreviations();

            if (null != myCapcoInfo) {

                if ((null != myAbreviationsFlag) && myAbreviationsFlag.booleanValue()) {

                    myBanner = myCapcoInfo.getAbbreviation();

                } else {

                    myBanner = myCapcoInfo.getBanner();
                }
            }
        }
        return myBanner;
    }

   public static Set<String> getDefaultCapcoAccessList() {
      return getCapcoRollup().getPositiveAclList();
   }

   public static Set<String> getDefaultCapcoRelToList() {
      return getCapcoRollup().getNegativeAclList();
   }

    private static CapcoRollup getCapcoRollup() {

        if (_capcoRollup == null) {

            _capcoRollup = CapcoRollup.rollupDefault();
            _capcoInfo = _capcoRollup.getCapcoInfo();
        }
        return _capcoRollup;
    }

    private static CapcoInfo getCapcoInfo() {

        if (_capcoInfo == null) {

            getCapcoRollup();
        }
        return _capcoInfo;
    }

    //
    // see if username is known to server
    //
    public static boolean isRegisteredUser(String usernameIn) {
        String myUser = usernameIn.toLowerCase();
        csi.server.common.identity.User userobj = Users.getUserByName(myUser);

        if (null != userobj) {
            LOG.debug("isRegisteredUser: Found registered user {}, hash={}", () -> usernameIn, () -> userobj.getPassword());
        } else {
            LOG.debug("isRegisteredUser: User {} is not a registered user", () -> usernameIn);
        }
        return (null != userobj);
    }

    //
    // See if password value matches user's password
    //
    public static boolean validateUserAuth(String usernameIn, String passwordhash) {
        String myUser = usernameIn.toLowerCase();
        Authorization auth = null;
        try {
            auth = AuthorizationFactory.getAuthorization(myUser, passwordhash);
        } catch (GeneralSecurityException e) {
            LOG.info("Unable to validate user authentication.");
        }
        return (auth != null);
    }

    //
    // see if username/password has at least one of the specified roles
    //
   public static boolean userHasAnyRole(String usernameIn, String pwhash, String[] roles) {
      boolean authorized = false;

      if (roles != null) {
         String myUser = usernameIn.toLowerCase();

         try {
            Authorization auth = AuthorizationFactory.getAuthorization(myUser, pwhash);

            for (int i = 0; i < roles.length; i++) {
               String role = roles[i].trim();

               LOG.debug("Does user {} have the role {}?", () -> usernameIn, () -> role);

               authorized = auth.hasRole(role);
               String authorizedStr = Boolean.toString(authorized);

               if (authorized) {
                  LOG.debug("User {} has the role {}: {}", () -> usernameIn, () -> role, () -> authorizedStr);
                  break;
               }
               LOG.debug("User {} does not have the role {}: {}", () -> usernameIn, () -> role, () -> authorizedStr);
            }
         } catch (GeneralSecurityException gsx) {
            LOG.info("Unable to obtain user's roles given username and password");
         }
      }
      return authorized;
   }

    // TODO: this one has multiple usecases weaved in. Should refactor into
    // multiple methods to
    // and each use case individually
    //
    // Another flavor of authorization check. Given an agent, user, and a
    // password
    // hash, decide whether or not to authorize. If both agent and user are
    // specified,
    // the pwhash belongs to the agent.
    //
   public static boolean userOrAgentHasAnyRole(String agentnameIn, String usernameIn, String passwordhash, String[] roles) {
      LOG.debug("checkAuthorization: agent={} user={} pw={}", () -> agentnameIn, () -> usernameIn, () -> passwordhash);
      boolean isauth = false;

      if (passwordhash != null) {
         String user = usernameIn.toLowerCase();
         String agent = agentnameIn.toLowerCase();

         if (user == null) {
            if (agent != null) {
               // if just agentname, check that the password is correct for the agent.
               // the agent is just doing something for himself as if username=agentname
               isauth = validateUserAuth(agent, passwordhash);
               String isAuthStr = Boolean.toString(isauth);

               LOG.debug("checkAuthorization: checked agent {} isauth={}", () -> agentnameIn, () -> isAuthStr);
            }
         } else {
            if (agent == null) {
               // if just username, check that the password is correct for the user
               isauth = validateUserAuth(user, passwordhash);
               String isAuthStr = Boolean.toString(isauth);

               LOG.debug("checkAuthorization: checked user {}  isauth={}", () -> usernameIn, () -> isAuthStr);

               if (!isauth) {
                  LoginEventService.saveLoginBadPassword(user);
               }
            } else {
               // if both agent and user are specified, check that the password is correct
               // for the agent.
               isauth = userHasAnyRole(agent, passwordhash, roles);
            }
         }
      }
      return isauth;
   }

   private static ACL findACL(String identifier) {
      ACL myAcl = null;
      EntityManager entityManager = CsiPersistenceManager.getMetaEntityManager();
      Query query = entityManager.createQuery(ACL_Search);

      query.setParameter("uuid", identifier);

      try {
         List<ACL> results = query.getResultList();

         if ((results != null) && !results.isEmpty()) {
            myAcl = results.get(0);
         }
      } catch(Exception myException) {
         // IGNORE
      }
      return myAcl;
   }

    @SuppressWarnings("unchecked")
    public static List<Asset> filterAssets(String operation, List<Asset> list) throws CsiSecurityException {
        List<Asset> filtered = new ArrayList<Asset>();

        for (Asset asset : list) {

            if(isAuthorized(asset.getAssetID(), AclControlType.READ)) {
                filtered.add(asset);
            }
        }

        return filtered;
    }

   public static boolean canCreateConnectionType(String type, String url) throws CentrifugeException {
      ConnectionFactory factory = ConnectionFactoryManager.getInstance().getConnectionFactory(type);

      if (factory == null) {
         throw new CentrifugeException("Unknown connection type: " + type + " url:" + url);
      }
      String role = factory.getDriverAccessRole();

      return (role == null) || role.isEmpty() || hasRole(role);
   }


    public static boolean canEditResourceConnections(Resource res) throws CentrifugeException {
        List<DataSourceDef> dsList = null;
        if (res instanceof DataView) {
            dsList = ((DataView) res).getMeta().getDataSources();
        } else if (res instanceof DataViewDef) {
            dsList = ((DataViewDef) res).getDataSources();
        }

        if (dsList != null) {
            for (DataSourceDef ds : dsList) {
                String type = ds.getConnection().getType();
                String url = ds.getConnection().getConnectString();
                try {
                    if (!canCreateConnectionType(type, url)) {
                        return false;
                    }
                } catch (ConnectionFactoryNotFoundException cfe) {
                    if (!isAdmin()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static void logout() {
        clearAuthorization();
        TaskController.getInstance().cleanSessionTasks(TaskHelper.getCurrentSession().getId());
        ApiAuthenticator.logout();
    }

    public static boolean login(String username, String password) {
        Principal p = ApiAuthenticator.login(username, password);
        return (p != null);
    }

    public static boolean login() {
        Principal p = ApiAuthenticator.login();
        return (p != null);
    }

    public static boolean authenticate(String username, String password) {
        Principal p = ApiAuthenticator.authenticate(username, password);
        return (p != null);
    }

    public static boolean authenticate(X509Certificate[] certs) {
        Principal p = ApiAuthenticator.authenticate(certs);
        return (p != null);
    }

    public static Set<String> listRoles() {
        return getAuthorization().listRoles();
    }

    // assumes the list of roles are those known only by Centrifuge.
    // augment the known list with ones that are 'external' i.e.
    // roles added by JAAS modules that are not known by Centrifuge.
    public static Collection<Role> augmentWithRuntimeRoles(Collection<Role> known) {

        Set<String> knownNames = mapAsNames(known);
        Set<String> runtimeRoles = getUserRoles();
        Set<Role> combined = new HashSet<Role>();
        combined.addAll(known);

        for( String rt : runtimeRoles) {
            if( !knownNames.contains(rt) && !rt.equals(JAASRole.AUTHENTICATED_ROLE)) {
                Role r = new TransientRole( rt );
                combined.add(r);
            }
        }

        return combined;

    }

    public static void identifyAuthorizedThread(long threadIdIn) {

        if (null == _authorizedThreadId) {

            _authorizedThreadId = threadIdIn;
        }
    }

    private static Role createTransientRole(String name) {
        return new Role() {
            {
                this.name = name;
                this.id = 1L;
            }
        };
    }


    public static Set<String> mapAsNames(Collection<Role> roles) {
       Set<String> names = new HashSet<String>();

       for (Role r : roles) {
          names.add(r.getName());
       }
       return names;
    }

    public static List<String> listOwnerAndPermissions(Asset asset) {

        List<String> permissions = new ArrayList<String>();

        ACL acl = findACL(asset.getAssetID());
        if (acl != null) {

            permissions.add(acl.getOwner());

            for (AccessControlEntry entry : acl.getEntries()) {
                if (hasRole(entry.getRoleName())) {
                    permissions.add(entry.getAccessType().getKey());
                }
            }
        }

        return permissions;
    }

    public static String retrieveOwner(String uuid){
    	ACL acl = findACL(uuid);

    	return (acl == null) ? null : acl.getOwner();
    }

    private static ACL copyACL(ACL acl, ACL dest) {
        if (dest == null) {
            dest = new ACL(acl.getOwner(), acl.getUuid());
        }
        List<AccessControlEntry> entries = dest.getEntries();

        for (AccessControlEntry entry : acl.getEntries()) {
            AccessControlEntry newEntry = new AccessControlEntry(entry.getAccessType(), entry.getRoleName());
            entries.add(newEntry);
        }
        return dest;
    }

    public static boolean isAuthorized(AuthorizationContext context) {
        return false;
    }

    public static boolean canChangeSecurity(String uuidIn) {
        return (isOriginator() || (Configuration.getInstance().getSecurityPolicyConfig().getOwnerSetsSecurity().booleanValue()
                                    && AclRequest.isOwner(uuidIn)));
    }

    public static boolean canChangeSecurity() {
        return (isOriginator() || ((!isRestricted())
                                    && Configuration.getInstance().getSecurityPolicyConfig().getOwnerSetsSecurity().booleanValue()));
    }

    static class TransientRole extends Role {
        static long counter = Long.MIN_VALUE;

        static synchronized long next() {
            counter++;
            if( counter >= 0 ) {
                counter = Long.MIN_VALUE;
            }

            return counter;
        }

        public TransientRole(String name ) {
            this.name = name;
            this.id = next();
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
           return (this == obj) ||
                  ((obj != null) &&
                   (obj instanceof TransientRole) &&
                   name.equals(((TransientRole) obj).getName()));
        }
    }
}
