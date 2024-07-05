package csi.config.app;

import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * The ConfiguredServletListener implements the {@link ServletContextListener}
 * interface enabling instances of this class to participate in the
 * web-application life-cycle.
 * <p>
 * During context initialization this listener uses the initialization
 * parameters specified in the web.xml to determine which modules are
 * configured.
 * <p>
 * Misconfiguration of any of the following settings results in an error message
 * being logged and failure deploying Centrifuge. Thorough testing should be
 * performed to verify all configuration values.
 *
 * <p>
 * The following web-application parameters are consulted during initialization:
 * <dl>
 * <dt>guice-modules</dt>
 * <dd>comma separated list of modules to register with the run-time. The
 * modules are expected to conform to the {@link Module} interface. This list
 * may contain whitespace between each configured module.</dd>
 *
 * <dt>guice-stage</dt>
 * <dd>identifies the deployment stage and can be one of the following values:
 * TOOL, DEVELOPMENT, PRODUCTION</dd>
 * </dl>
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public class ConfiguredServletListener extends GuiceServletContextListener {
   public static final String MODULES_PROP = "guice-modules";
   public static final String STAGE_PROP = "guice-stage";

   private static final Pattern NAME_PATTERN = Pattern.compile("\\s*,\\s*");
   private static final String INVALID_CONFIGURATION =
      "Invalid configuration encountered.  The guice-modules initialization parameter is a required.";

   private ServletContext context;
   /*
    * (non-Javadoc)
    *
    * @see com.google.inject.servlet.GuiceServletContextListener#getInjector()
    */
   @Override
   protected Injector getInjector() {
      String value = context.getInitParameter(MODULES_PROP);
      value = (value == null) ? "" : value.trim();

      if (value.isEmpty()) {
         throw new RuntimeException(INVALID_CONFIGURATION);
      }
      String[] names = NAME_PATTERN.split(value);
      Module module = new DynamicConfiguration(names);
      Stage stage = getStage();

      return Guice.createInjector(stage, module);
   }

   @Override
   public void contextInitialized(ServletContextEvent servletContextEvent) {
      context = servletContextEvent.getServletContext();
      super.contextInitialized(servletContextEvent);
   }

   @Override
   public void contextDestroyed(ServletContextEvent servletContextEvent) {
      super.contextDestroyed(servletContextEvent);
      context = null;
   }

   private static Stage getStage() {
      // TODO: obtain value from servletContext; default to PRODUCTION if invalid or not present.
      return Stage.PRODUCTION;
   }
}
