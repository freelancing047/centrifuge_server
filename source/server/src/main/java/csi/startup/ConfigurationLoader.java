package csi.startup;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import csi.config.Configuration;

public class ConfigurationLoader {
   public Configuration load(ServletContext context) throws Exception {
      WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
      Configuration config = (Configuration) applicationContext.getBean("configuration");

      Configuration.setInstance(config);
      config.normalize();
      config.validateDBDriverConfig();
      config.setWebApplicationContext(applicationContext);
      return config;
   }
}
