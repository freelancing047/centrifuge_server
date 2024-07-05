package csi.startup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.util.concurrent.AbstractIdleService;
import com.sun.jersey.api.client.Client;

import csi.config.Configuration;
import csi.config.advanced.graph.PatternConfig;

public class Neo4jService extends AbstractIdleService {
   private static boolean debug = false;

   private PatternConfig patternConfig;

   @Override
   protected void startUp() throws Exception {
      patternConfig = Configuration.getInstance().getGraphAdvConfig().getPatternConfig();
      if (!patternConfig.getEnabled()) {
         return;
      }
      if (!patternConfig.isStartWithServer()) {
         return;
      }

      List<String> command = new ArrayList<String>();
      if (debug) {
         for (Map.Entry<String,String> entry : System.getenv().entrySet()) {
            System.out.println("key: " + entry.getKey());
            System.out.println("value: " + entry.getValue());
         }
      }

      // Fixme: do we have a better test
      boolean windows = new File("cachedb/neo4j/bin/Neo4j.bat").exists();

      if (windows) {
         command.add("cachedb/neo4j/bin/Neo4j.bat");
      } else {
         command.add("cachedb/neo4j/bin/neo4j");
         command.add("start");
      }

      ProcessBuilder processBuilder = new ProcessBuilder(command);
      {
         String javaHome = System.getProperty("java.home");
         processBuilder.environment().put("JAVA_HOME", javaHome);
      }

      if (debug) {
         for (Map.Entry<String,String> entry : processBuilder.environment().entrySet()) {
            System.out.println("key: " + entry.getKey());
            System.out.println("value: " + entry.getValue());
         }
      }
      try {
         processBuilder.start();
      } catch (IOException ignored) {
      }
   }

   @Override
   protected void shutDown() throws Exception {
      if (patternConfig.getEnabled() && patternConfig.isStartWithServer()) {
         String host = patternConfig.getHost();
         String path = "/csi/stop/now";
         Client.create().resource(host + path).get(String.class);
      }
   }
}
