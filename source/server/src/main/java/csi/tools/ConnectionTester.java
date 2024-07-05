package csi.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConnectionTester {
    private static String ORA_PKI_PROVIDER = "oracle.security.pki.OraclePKIProvider";

    public static void main(String[] args) {
       try {
          if (args.length < 3) {
             showUsage();
             return;
          }

          File serverdir = new File(args[1]);
          File propFile = new File(args[2]);

          File commonLib = new File(serverdir, "lib");

          List<URL> jars = new ArrayList<URL>();
          addJars(jars, commonLib);

          for (URL url2 : jars) {
             System.out.println(url2);
             // Launcher.addURLToAppClassLoader(url2);
          }
          System.out.println(ConnectionTester.class.getClassLoader());

          for (String jarPath : System.getProperty("sun.boot.class.path").split(";")) {
             try {
                System.out.println(new URL("file", null, jarPath));
             } catch (MalformedURLException mue) {
             }
          }

          Properties testProps = new Properties();

          if (propFile.exists()) {
             try (FileInputStream ins = new FileInputStream(propFile)) {
                testProps.load(ins);
             }
          } else {
             System.out.println("Property file not found: " + propFile.getCanonicalPath());
             return;
          }

          String connUrl = testProps.getProperty("connection.url");
//          Driver driver = DriverManager.getDriver(connUrl);

          Properties connProps = new Properties();
          for (String key : testProps.stringPropertyNames()) {
             if ((key != null) && key.startsWith("connection.prop.")) {
                connProps.put(key, testProps.get(key));
             }
          }

          Boolean autoCommit = null;
          String autoc = testProps.getProperty("connection.autoCommit");
          if (autoc != null) {
             autoCommit = Boolean.valueOf(testProps.getProperty("connection.autoCommit"));
          }

          Boolean readResult = Boolean.valueOf(testProps.getProperty("test.readResult"));

          String preSql = testProps.getProperty("test.preSql");
          String sql = testProps.getProperty("test.sql");
          String postSql = testProps.getProperty("test.postSql");

          boolean loadOraPki = Boolean.parseBoolean(testProps.getProperty("misc.initOraPki"));
          if (loadOraPki) {
             // init the oracle pki provider if it's available
             Class clz = null;
             try {
                clz = Class.forName(ORA_PKI_PROVIDER);
             } catch (ClassNotFoundException e) {
                // ignore
             }

             if (clz != null) {
                try {
                   clz.newInstance();
                } catch (Throwable t) {
                   throw new Exception("Failed to initialize Oracle PKI provider", t);
                }
             }
          }

          int numThreads = Integer.parseInt(testProps.getProperty("test.numThreads"));
          int iterations = Integer.parseInt(testProps.getProperty("test.iterations"));
          int sleepTime = Integer.parseInt(testProps.getProperty("test.sleepTime"));

          Object waitObj = new Object();

          for (int i = 0; i < numThreads; i++) {
             Thread t = new TestRunner(connUrl, connProps, waitObj, autoCommit, preSql, sql, postSql, iterations,
                   sleepTime, readResult);
             t.start();
          }

          waitObj.notifyAll();
          System.out.println("Started " + numThreads + " threads.");
       } catch (Exception e) {
       }
    }

    public static class TestRunner extends Thread {
        private Properties props;
        private String url;
        private Object waitObj;
        private String preSql;
        private String sql;
        private String postSql;
        private int sleepTime;
        private int iterations;
        private Boolean autoCommit;
        private Boolean readResult;

        public TestRunner(String connUrl, Properties connProps, Object waitObj, Boolean autoCommit,
                          String preSql, String sql, String postSql, int iterations, int sleepTime,
                          Boolean readResult) {
            this.url = connUrl;
            this.props = connProps;
            this.waitObj = waitObj;
            this.preSql = preSql;
            this.sql = sql;
            this.postSql = postSql;
            this.iterations = iterations;
            this.sleepTime = sleepTime;
            this.autoCommit = autoCommit;
            this.readResult = readResult;
        }

        public void run() {
            try {
                waitObj.wait();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted");
                return;
            }

            Driver driver;
            try {
                driver = DriverManager.getDriver(url);
            } catch (SQLException e1) {
                e1.printStackTrace();
                return;
            }
            for (int i = 0; i < iterations; i++) {
               try (Connection conn = driver.connect(url, props)) {
                  if (autoCommit != null) {
                     conn.setAutoCommit(autoCommit);
                  }
                  if ((preSql != null) && !preSql.trim().isEmpty()) {
                     try (Statement preStmt = conn.createStatement()) {
                        preStmt.execute(preSql);
                     }
                  }
                  if ((sql != null) && !sql.trim().isEmpty()) {
                     try (Statement sqlStmt = conn.createStatement();
                          ResultSet rs = sqlStmt.executeQuery(sql)) {
                        if (readResult.booleanValue()) {
                           while (rs.next()) {
                              rs.getObject(1);
                           }
                        }
                     }
                  }
                  if ((postSql != null) && postSql.trim().isEmpty()) {
                     try (Statement postStmt = conn.createStatement()) {
                        postStmt.execute(postSql);
                     }
                  }
                  if (sleepTime > 0) {
                     try {
                        Thread.sleep(sleepTime);
                     } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                     }
                  }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
            }
            System.out.println("Completed: " + this.toString());
        }
    }

    private static void addResource(List<URL> jars, File path) throws MalformedURLException {
        jars.add(path.toURL());
    }

    private static void addJars(List<URL> jars, File path) throws MalformedURLException {
        if (path.isFile()) {
            jars.add(path.toURL());
        } else {
            if (!path.exists()) {
                return;
            }

            FileFilter jarFilter = new FileFilter() {

                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                }
            };

            File[] listing = path.listFiles(jarFilter);
            for (File child : listing) {
                jars.add(child.toURL());
            }
        }
    }

   private static void showUsage() {
      System.out.println("\nSYNTAX: testconn server-dir property-file" +
                         "\n\n  server-home       - Home directory of Centrifuge Server" +
                         "\n\n  property-file     - Full path of test property file ");
   }
}
