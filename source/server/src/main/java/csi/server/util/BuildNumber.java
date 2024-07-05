package csi.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuildNumber {
   private static final Logger LOG = LogManager.getLogger(BuildNumber.class);

   private static final String BUILDNUMBER_PROPERTIES_FILE = "/buildnumber.properties";
   private static final String BUILD_NUMBER = "build.number";

   private static Properties props;

   static {
      props = new Properties();

      try (InputStream in = BuildNumber.class.getResourceAsStream(BUILDNUMBER_PROPERTIES_FILE)) {
         props.load(in);
      } catch (IOException e) {
         LOG.error("Failed to load build properties", e);
      }
   }

   public static String getBuildNumber() {
      String bn = props.getProperty(BUILD_NUMBER);

      if (bn != null) {
         bn = bn.trim();
      }
      return bn;
   }
}
