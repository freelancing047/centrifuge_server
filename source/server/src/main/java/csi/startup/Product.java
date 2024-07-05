package csi.startup;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import csi.config.Configuration;
import csi.license.model.AbstractLicense;
import csi.license.model.ConcurrentLicense;
import csi.license.model.LicenseFactory;
import csi.security.AclTagRepository;
import csi.security.monitors.ResourceACLMonitor;
import csi.security.queries.Users;
import csi.server.business.samples.SamplesManager;
import csi.server.business.service.ServiceLocator;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.dto.user.UserSystemValues;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.util.SystemParameters;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.BuildNumber;
import csi.server.util.DateUtil;
import csi.server.util.Version;
import csi.tools.DataMover;

/**
 * Performs product startup and initialization.
 * <p/>
 * This serves as the place holder for all known component initialization.
 * License Management is hard-coded to always execute.
 * <p/>
 *
 * @author Tildenwoods
 */
public class Product {
   private static final Logger LOG = LogManager.getLogger(Product.class);

   private static boolean isDebug = LOG.isDebugEnabled();
   private static AbstractLicense license;
   private static boolean isShowBuildNumber = true;
   private static boolean startingUp = false;

   private Neo4jService neo4jService;

   public Product() {
      license = LicenseFactory.create();
   }

   public static AbstractLicense getLicense() {
      return license;
   }
   public static boolean inStartUp() {
      return startingUp;
   }

   public void startup(ServletContext servletContext) throws InitializationException {
      try {
         String javaOpts = System.getenv("JAVA_OPTS");

         if (LOG.isInfoEnabled()) {
            LOG.info("Centrifuge is starting ... ");

            if (isShowBuildNumber) {
               LOG.info("Version number {} Build Number: {}",
                        () -> Version.getVersionString(), () -> BuildNumber.getBuildNumber());
            }
            if (StringUtils.isNotEmpty(javaOpts)) {
               LOG.info("JAVA_OPTS: {}", () -> javaOpts);
            }
         } else {
            System.out.println("Centrifuge is starting ..." + BuildNumber.getBuildNumber());

            if (isShowBuildNumber) {
               System.out.println("Version number " + Version.getVersionString() +
                                  " Build Number: " + BuildNumber.getBuildNumber());
            }
            if (StringUtils.isNotEmpty(javaOpts)) {
               System.out.println("JAVA_OPTS: " + javaOpts);
            }
         }
         if (isDebug) {
            LOG.debug("... LoadConfiguration.");
         }
         loadConfiguration(servletContext);

         if (isDebug) {
            LOG.debug("... Initialize JULI Logging.");
         }
         initializeJULILogging();

         if (isDebug) {
            LOG.debug("... Initialize System Value Access.");
         }
         initializeSystemValueAccess();

         if (isDebug) {
            LOG.debug("... Initialize Cache.");
         }
         initializeCache();

         if (isDebug) {
            LOG.debug("... Verify License.");
         }
         verifyLicense();

         if (DataMover.requiresCleanup()) {
            if (isDebug) {
               LOG.debug("... Perform cleanup after data migration.");
            }
            startingUp = true;
            DataMover.cleanupMigration();
            startingUp = false;
         }
         if (isDebug) {
            LOG.debug("... Verify necessary database access.");
         }
         gutCheck();

         if (isDebug) {
            LOG.debug("... Activating Resource ACL Monitor.");
         }
         ResourceACLMonitor.activate();

         if (isDebug) {
            LOG.debug("... Restrict Active Users.");
         }
         restrictActiveUsers();

         if (isDebug) {
            LOG.debug("... Update Configured Security Items.");
         }
         updateConfiguredSecurityItems();

         if (isDebug) {
            LOG.debug("... Initialize Services.");
         }
         initializeServices();

         if (isDebug) {
            LOG.debug("... Install user administration tools.");
         }
         InstallAdminTools.execute();

         if (isDebug) {
            LOG.debug("... Load Samples.");
         }
         SamplesManager.getInstance().loadSamples(servletContext.getRealPath(""));

         if (isDebug) {
            LOG.debug("... Initialize PostgresSQL");
         }
         initializePostgres();

         if (isDebug) {
            LOG.debug("... Initialize Neo4j.");
         }
         initializeNeo4j();

         //SelectionBroadcastCache.getInstance().restore();

         //TODO: get this optimized one day
         if (isDebug) {
            LOG.debug("... Cache Templates.");
         }
         cacheTemplates();

         if (isDebug) {
            LOG.debug("... Launch Clean-Up Thread.");
         }
         CleanUpThread.launch();

         {
            Session session = CsiPersistenceManager.getMetaEntityManager().unwrap(Session.class);
            SessionFactory sessionFactory = session.getSessionFactory();
            sessionFactory.getCache().evictAllRegions();
         }
         System.out.println("Centrifuge has successfully started.");
      } catch (Throwable t) {
         if (t instanceof InitializationException) {
            throw (InitializationException) t;
         }
         throw new InitializationException(t);
      }
   }

   private void loadConfiguration(ServletContext servletContext) throws Exception {
      ConfigurationLoader loader = new ConfigurationLoader();

      loader.load(servletContext);

      Configuration config = Configuration.getInstance();

      if (config != null) {
         servletContext.setAttribute(Bootstrap.CONFIG, config);
         config.validateSettings();
      }
   }

   private void initializeJULILogging() {
      new JULIInitializer().initialize();
   }

   private void initializeSystemValueAccess() {
      SystemParameters.setValueProvider(new UserSystemValues());
   }

   private void initializeCache() throws CentrifugeException {
      AbstractCacheInitializer.getInstance().initialize();
   }

   private void verifyLicense() throws InitializationException {
      if (license.isValid()) {
         int userCount = license.getUserCount();

         if (license.isExpiring()) {
            LOG.info("License expires on: {}",
                     () -> license.getEndDateTime().format(DateUtil.JAVA_UTIL_DATE_TOSTRING_FORMATTER));
         }
         LOG.info("License is valid for {}{} user{}", () -> Integer.toString(userCount),
                  () -> ((license instanceof ConcurrentLicense) ? " concurrent" : ""),
                  () -> (userCount == 1) ? "" : "s");
      } else {
         throw new InitializationException("License verification failed");
      }
   }

   private void gutCheck() throws CentrifugeException {
      try (Connection connection = CsiPersistenceManager.getMetaConnection()) {
         if (connection == null) {
            throw new CentrifugeException("Unable to connect to MetaDB database!");
         }
      } catch (SQLException sqle) {
      }
      try (Connection connection = CsiPersistenceManager.getCacheConnection()) {
         if (connection == null) {
            throw new CentrifugeException("Unable to connect to CacheDB database as unrestricted user!");
         }
      } catch (SQLException sqle) {
      }
      try (Connection connection = CsiPersistenceManager.getUserConnection()) {
         if (connection == null) {
            throw new CentrifugeException("Unable to connect to CacheDB database as restricted user!");
         }
      } catch (SQLException sqle) {
      }
      LOG.info("Required database access has been confirmed.");
   }

   private void restrictActiveUsers() {
      int persistentUserCount = (int) Users.getUserCount(true);

      if (!license.persistedUsersWithinLimit(persistentUserCount)) {
         /*
          * Check User Count in database. This is to prevent someone from adding users to the database directly
          * rather than via the admin UI, thereby circumventing the max user count that the license specifies.
          */
         int authorizedUserCount = license.getUserCount();

         if (persistentUserCount > authorizedUserCount) {
/*
            LOG.error("User count of " + Long.toString(persistentUserCount) + " exceeds licensed number of users "
                        + Long.toString(authorizedUserCount) + " -- disabling "
                        + Long.toString(authorizedUserCount - persistentUserCount) + " users.");

            if (!Users.reduceActiveUserCount(persistentUserCount - authorizedUserCount)) {
                throw new InitializationException("Unable to reduce number of users to authorized count.");
            }
*/
            LOG.error("User count of {} exceeds licensed number of users {} -- disable at least {} users.",
                      () -> Long.toString(persistentUserCount), () -> Long.toString(authorizedUserCount),
                      () -> Long.toString(persistentUserCount - authorizedUserCount));
         }
      }
   }

   private void updateConfiguredSecurityItems() throws CsiSecurityException {
      AclTagRepository.initializeStaticData();
   }

   private void initializeServices() {
      // Instantiating the ServiceLocator - loading all the annotated service classes
      ServiceLocator.getInstance();
      ConnectionFactoryManager.getInstance().init();
      XStreamHelper.initCodecs();
   }

   private void initializePostgres() throws InitializationException {
      new PGSQLFunctionInitializer().initialize();
   }

   private void initializeNeo4j() {
      neo4jService = new Neo4jService();

      neo4jService.startAsync();
   }

   private void cacheTemplates() {
      new TemplateCachingService().startAsync();
   }

   public void shutdown() {
      try {
         license.shutdown();
         neo4jService.shutDown();
      } catch (Exception e) {
         //e.printStackTrace();
      }
   }
}
