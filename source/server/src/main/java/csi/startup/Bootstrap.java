package csi.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import csi.config.Configuration;
import csi.security.jaas.LogThreadContextKey;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.task.api.TaskController;
import csi.server.util.Messages;
import csi.server.util.SystemInfo;

/**
 * Main Web Application bootstrap. Registered via the web.xml.
 * <p>
 * Kicks off initialization of other sub-systems that Centrifuge depends on:
 * <ul>
 * <li>Derby</li>
 * <li>JPA (indirectly)</li>
 * </ul>
 *
 * @web.listener
 *
 * @author Centrifuge Systemss
 */
public class Bootstrap implements ServletContextListener {
   private static final Logger LOG = LogManager.getLogger(Bootstrap.class);

   public static final String CONFIG = "csi.config";

   private static Product product;

   public void contextInitialized(ServletContextEvent event) {
      ReleaseInfo.initialize(SystemInfo.getReleaseVersion(), SystemInfo.getBuildNumber());

      ServletContext servletContext = event.getServletContext();

      try {
         // initialize cache db
         // TODO: remove me once we switch meta db to be created at install time.
         product = new Product();

         product.startup(servletContext);
      } catch (Throwable t) {
         LOG.error(t.getMessage());
         LOG.error("Error occurred during initialization ", t);
         shutdownCacheDB();
         System.exit(0);
      }
      // Initialize ThreadContext with applicationId now that the configurations are ready
      String applicationId = Configuration.getInstance().getApplicationConfig().getApplicationId();

      if (applicationId != null) {
         ThreadContext.put(LogThreadContextKey.APPLICATION_ID, applicationId);
      }
      TaskController.getInstance();

      LOG.info("System configuration initialized"); //$NON-NLS-1$
      LOG.info(Messages.getString("Bootstrap.startup_complete")); //$NON-NLS-1$
      LOG.info(Messages.getString("Bootstrap.startup_ready")); //$NON-NLS-1$
   }

   public void contextDestroyed(ServletContextEvent event) {
      destroyContext();
   }

   public static void destroyContext() {
      LOG.info(Messages.getString("Bootstrap.shutdown")); //$NON-NLS-1$

      // SelectionBroadcastCache.getInstance().persist();

      CleanUpThread.terminate();

      shutdownCacheDB();

      TaskController.getInstance().shutdown();
      product.shutdown();

      LOG.info("{} (Release {}, build {})", () -> Messages.getString("Bootstrap.shutdown_complete"),
               () -> ReleaseInfo.version, () -> ReleaseInfo.build);

      ThreadContext.remove(LogThreadContextKey.APPLICATION_ID);

      LogManager.shutdown();
   }

   public static void shutdownCacheDB() {
      csi.server.dao.CsiPersistenceManager.closeEntityManagerFactories();

      Configuration config = Configuration.getInstance();

      if (config.getDataCacheConfig().isShutdownCache()) {
         AbstractCacheInitializer.getInstance().shutdown();
      }
   }
}
