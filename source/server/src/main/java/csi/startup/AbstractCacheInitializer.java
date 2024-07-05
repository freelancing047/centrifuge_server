package csi.startup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import liquibase.Liquibase;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;

import csi.config.Configuration;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.FileUtil;
import csi.server.util.SqlUtil;
import csi.server.util.WorkingDisplay;

public abstract class AbstractCacheInitializer {
   protected static final Logger LOG = LogManager.getLogger(AbstractCacheInitializer.class);

   private static final String PG_HBA_CONF = "pg_hba.conf";
   private static final int DB_MAX_WAIT = 10000;
   private static final int DBCHECK_INTERVAL = 500;
   private static final String DELIM = ";";
   private static final String DB_LOCAL = "127.0.0.1";
   private static final String INIT_DB = "initdb;--encoding=UTF8;-D;%1$s;";
   private static final String START_DBSERVER = "pg_ctl;start;-w;-t;60;-D;%1$s";
   private static final String STOP_DBSERVER = "pg_ctl;stop;-w;-t;60;-m;fast;-D;%1$s";
   private static final String RESTART_DBSERVER = "pg_ctl;restart;-w;-t;60;-D;%1$s";
   private static final String RELOAD_DBSERVER = "pg_ctl;reload;-D;%1$s";
   private static final String EXEC_SQL_FILE = "psql;-h;%1$s;-p;%2$s;-d;postgres;-f;%3$s;-w";
   private static final String DB_CONFIG = "postgresql.conf";
   private static final String DB_PORT_KEY = "port";
   private static final String PG_DEFAULT_PORT = "5432";
   private static final String DB_MIGRATE_KEY = "migrateCommand";
   private static final String SCRATCH = "_scratch";

   private static final Pattern CLEAN_PATTERN = Pattern.compile("\\s#.*$");
   private static final Pattern DELIM_PATTERN = Pattern.compile(DELIM);

    public static final int MAX_CALL_DEPTH = 10;

    public enum StreamType {
        INFO,
        ERROR,
        DEBUG
    }

   class StreamGobbler extends Thread {
      private InputStream stream;
      private StreamType type;

      public StreamGobbler(InputStream streamIn, StreamType typeIn) {
         stream = streamIn;
         type = typeIn;
      }

      @Override
      public void run() {
         try (InputStreamReader reader = new InputStreamReader(stream);
              BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
               switch (type) {
                  case INFO :
                     LOG.info(line);
                     break;
                  case ERROR :
                     LOG.error(line);
                     break;
                  case DEBUG :
                     LOG.debug(line);
                     break;
               }
            }
         } catch (IOException ioe) {
            switch (type) {
               case INFO :
                  LOG.info("Caught exception:\n" + Format.value(ioe));
                  break;
               case ERROR :
                  LOG.error("Caught exception:\n" + Format.value(ioe));
                  break;
               case DEBUG :
                  LOG.debug("Caught exception:\n" + Format.value(ioe));
                  break;
            }
         }
      }
   }

    abstract void createIfDontExist(Connection conn) throws CentrifugeException;

    private static AbstractCacheInitializer instance = new LiquibaseCacheInitializer();
    //private static CacheInitializer instance = new HibernateAutoGenCacheInitializer();

    private enum SupportedLevel {
       OFF(Level.OFF, LogLevel.OFF),
       ERROR(Level.ERROR, LogLevel.SEVERE),
       WARN(Level.WARN, LogLevel.WARNING),
       INFO(Level.INFO, LogLevel.INFO),
       DEBUG(Level.DEBUG, LogLevel.DEBUG);

       private static Map<Level,LogLevel> levels;

       static {
          levels = new HashMap<Level,LogLevel>();

          for (SupportedLevel supportedLevel : SupportedLevel.values()) {
             levels.put(supportedLevel.getLevel(), supportedLevel.getLogLevel());
          }
       }

       private Level level;
       private LogLevel logLevel;

       private SupportedLevel(Level level, LogLevel logLevel) {
          this.level = level;
          this.logLevel = logLevel;
       }

       public static LogLevel getSupportedLevel(final Level level) {
          LogLevel logLevel = levels.get(level);

          return (logLevel == null) ? LogLevel.SEVERE : logLevel;
       }

       public Level getLevel() {
          return level;
       }
       public LogLevel getLogLevel() {
          return logLevel;
       }
    }

   public static AbstractCacheInitializer getInstance() {
      LogFactory.getInstance().setDefaultLoggingLevel(
         SupportedLevel.getSupportedLevel(LogManager.getLogger(Liquibase.class).getLevel()));

      return instance;
   }

    private String[] _configuredCommandFiles = new String[] {
            "CastingFunctions.sql",
            "CsiFunctions.sql",
            "UserFunctions.sql"
    };

    private File newdbdata;
    private File cacheTag;
    private File dbdata;
    private File pgsql;
    private File newpgsql;
    private File newdbbin;
    private File dbbin;
    protected File dbroot;
    protected File ddl;
    private File createMetaDB;
    private File createCacheDB;

    private String migrateCommand;
    private String binpath;
    private String newbinpath;
    private File configDir;
    private File configFile;
    private String dbPort;
    private boolean unixFlag;
    private boolean dbRunning = false;

    public AbstractCacheInitializer() {
        String myOS = System.getProperty("os.name");

        // this.home = new File( System.getProperty( CATALINA_HOME ) );
        this.unixFlag =  myOS.contains("nux") || myOS.contains("nix");
        this.dbroot = new File("cachedb");
        this.cacheTag = new File(dbroot, "cache_tag");
        this.pgsql = new File(dbroot, "pgsql");
        this.newpgsql = new File(dbroot, "pgsql_" + ReleaseInfo.version);
        this.newdbbin = new File(newpgsql, "/bin");
        this.dbbin = new File(pgsql, "/bin");
        this.newdbdata = new File(dbroot, "new_metadb");
        this.dbdata = new File(dbroot, "metadb");
        // this.pgCtl = new File( dbbin, "pg_ctl" );

        this.configDir = new File(dbroot, "config");
        this.configFile = new File(configDir, DB_CONFIG);

        Properties props = loadPGConfigFile(configFile);
        this.dbPort = props.getProperty(DB_PORT_KEY);
        if (this.dbPort == null) {
            this.dbPort = PG_DEFAULT_PORT;
        }
        this.migrateCommand = props.getProperty(DB_MIGRATE_KEY);

        ddl = new File(dbroot, "ddl");
        this.createMetaDB = new File(ddl, "CreateMetaDB.sql");
        this.createCacheDB = new File(ddl, "CreateCacheDB.sql");

        try {
            this.binpath = dbbin.getCanonicalPath() + File.separator;
            this.newbinpath = newdbbin.getCanonicalPath() + File.separator;
        } catch (IOException e) {
            LOG.error("Failed to get cache binary path", e);
        }
//        checkDataBases();
    }

   private Properties loadPGConfigFile(File file) {
      Properties props = loadProperties(file);

      // clean out comments etc.
      for (Map.Entry<Object,Object> entry : props.entrySet()) {
         String s = (String) entry.getValue();

         if (s != null) {
            String clean = CLEAN_PATTERN.matcher(s).replaceAll("").trim();

            if (s.isEmpty()) {
               props.remove(entry.getKey());
            } else {
               props.put(entry.getKey(), clean);
            }
         }
      }
      return props;
   }

   private Properties loadProperties(File file) throws RuntimeException {
      Properties props = new Properties();

      try (InputStream inputStream = new FileInputStream(file)) {
         props.load(inputStream);
      } catch (FileNotFoundException e) {
         throw new RuntimeException("Missing cache db configuration file", e);
      } catch (IOException e) {
         throw new RuntimeException("Failed to load cache db configuration file", e);
      }
      return props;
   }

   public void initialize() throws CentrifugeException {
      Connection connection = null;

      try {
         if (!Configuration.getInstance().getDbConfig().isUsingRemoteDB()) {
            finalizeDirectoryDelete(pgsql);
            finalizeDirectoryDelete(dbdata);
         }
         // try to make a connection to the cachedb
         connection = CsiPersistenceManager.getMetaConnection();

         if (newpgsql.exists() && !Configuration.getInstance().getDbConfig().isUsingRemoteDB()) {
            String myFatalError1 = "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
            String myFatalError2 = "!! Database must be shutdown before launching Centrifuge for the  !!";
            String myFatalError3 = "!! first time after an initial install or upgrade of the software !!";
            String myFatalError4 = "!! Enter a carriage return to exit cleanly !!";

            LOG.fatal("");
            LOG.fatal(myFatalError1);
            LOG.fatal("");
            LOG.fatal(myFatalError2);
            LOG.fatal(myFatalError3);
            LOG.fatal("");
            LOG.fatal(myFatalError4);
            LOG.fatal("");
            LOG.fatal(myFatalError1);
            LOG.fatal("");
            waitForUserResponse();
            throw new InitializationException(myFatalError2 + " " + myFatalError3);
         }
         clearLiquibaseLocks(connection);
      } catch (InitializationException myException) {

         throw myException;

      } catch (CentrifugeException IGNORE) {

         clearLiquibaseLocks(start());

      } finally {
         connection = SqlUtil.quietCloseConnection(connection);
      }
      // initialize persistence manager
      LOG.info("Initializing Entity Manager Factories");
      WorkingDisplay.begin();
      CsiPersistenceManager.initializeEntityManagerFactories();
      LOG.info("Updating Structural Application Data");
      createIfDontExist(connection);
      WorkingDisplay.cancel();
   }

    private Connection start() throws CentrifugeException {

        boolean myPgSqlExists = pgsql.exists();
        boolean myNewPgSqlExists = newpgsql.exists();

        try {

            if (myNewPgSqlExists) {

                LOG.info("New PGSQL directory found.");
                LOG.info("Checking PostgreSQL version.");
                if (upgradeNeeded()) {

                    try {

                        stopDataServer();
                        LOG.info("Existing database needs to be upgraded.");

                        if (canRename(pgsql) && canRename(dbdata)) {

                            newdbdata = new File(dbroot, "new_metadb");
                            initDataServer(newbinpath, newdbdata);
                            FileUtil.copyDirectory(configDir, newdbdata, null);

                            // Remove database security
                            removeAccessControls();
                            migrateData(newbinpath);
                            restoreAccessControls();
                            stopDataServer();

                            LOG.info("Delete previous PGSQL directory.");
                            deleteDirectory(pgsql);
                            LOG.info("Delete previous database.");
                            deleteDirectory(dbdata);

                            LOG.info("Begin using new PGSQL directory.");
                            newpgsql.renameTo(new File(dbroot, "pgsql"));
                            LOG.info("Begin using new database pair, \"metadb\" and \"cachedb\".");
                            newdbdata.renameTo(new File(dbroot, "metadb"));

                        } else {

                            String myFatalError1 = "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
                            String myFatalError2 = "!! Unable to migrate PostgreSQL to new version. Either the !!";
                            String myFatalError3 = "!! old \"pgsql\" or \"metadb\" directory could not be deleted. !!";
                            String myFatalError4 = "!! Enter a carriage return to exit cleanly.                !!";

                            LOG.fatal("");
                            LOG.fatal(myFatalError1);
                            LOG.fatal("");
                            LOG.fatal(myFatalError2);
                            LOG.fatal(myFatalError3);
                            LOG.fatal("");
                            LOG.fatal(myFatalError4);
                            LOG.fatal("");
                            LOG.fatal(myFatalError1);
                            LOG.fatal("");
                            waitForUserResponse();
                            throw new InitializationException(myFatalError2 + " " + myFatalError3);
                        }

                    } catch (Exception myException) {

                        throw myException;
                    }

                } else {

                    if (myPgSqlExists) {
                        LOG.info("Leaving database software unchanged.");
                        deleteDirectory(newpgsql);
                    } else {
                        LOG.info("Begin using new PGSQL directory.");
                        newpgsql.renameTo(pgsql);
                    }
                }
                createDataServer();
                installSqlFunctions();
            }
            if (!dbRunning) {

                startDataServer();
            }

        } catch (Exception e) {

            if ((null != newdbdata) && (null != dbdata) && newdbdata.exists() && dbdata.exists()) {

                newdbdata.delete();

            } else if ((!cacheTag.exists()) && (!newpgsql.exists()) && pgsql.exists()) {

                pgsql.renameTo(newpgsql);
            }
            throw new CentrifugeException("Failed to start cache server", e);
        }
        return CsiPersistenceManager.getMetaConnection();
    }

   public void shutdown() {
      if (isShutdownCacheServer()) {
         try {
            LOG.info("Stopping the database server.");
            WorkingDisplay.begin();
            stopDataServer(0);
         } catch (Exception myException) {
            LOG.error("Caught exception shutting down database server.", myException);
         }
      } else {
         LOG.info("Cache Server left running.");
         LOG.debug("Cache Server shutdown can be changed in the configuration if required.");
      }
   }

   public void clearLiquibaseLocks(Connection connection) {
      String command = "UPDATE \"databasechangeloglock\" SET \"locked\" = false;";

      try (Statement statement = connection.createStatement()) {
         statement.execute(command);
         connection.commit();
      } catch (Exception IGNORE) {
      }
   }

   private boolean isShutdownCacheServer() {
      return Configuration.getInstance().getDataCacheConfig().isShutdownCache();
   }

    private boolean upgradeNeeded() throws IOException {

        boolean myUpgadeNeeded = false;

        if (newdbbin.exists() && dbbin.exists() && dbdata.exists()) {

            String myNewBinPath = newdbbin.getCanonicalPath() + File.separator;
            Integer myNewVersion = getVersion(myNewBinPath);
            Integer myOldVersion = getVersion(binpath);

            if ((null != myNewVersion) && (null != myOldVersion)) {

                myUpgadeNeeded = (myNewVersion > myOldVersion);
                if (myUpgadeNeeded) {

                    LOG.info("Upgrading database to rev {}.{}",
                             () -> Integer.toString(myNewVersion / 1000),
                             () -> Integer.toString(myNewVersion % 1000));
                }
            }
        }
        return myUpgadeNeeded;
    }

   private Integer getVersion(String pathIn) {
      Integer myVersion = null;

      try {
         Process myProcess = Runtime.getRuntime().exec(new String[]{pathIn + "pg_config", "--version"});

         if (waitForResponse(myProcess) == 0) {
            InputStream myStream = myProcess.getInputStream();

            try (InputStreamReader reader = new InputStreamReader(myStream, Charsets.UTF_8)) {
               String myVersionString = CharStreams.toString(reader);
               String[] myVersionArray = (myVersionString != null) ? myVersionString.split(" ") : null;
               String[] myDigits = ((myVersionArray != null) && (1 < myVersionArray.length)) ? myVersionArray[1].split("\\.") : null;

               if (myDigits != null) {
                  Integer myMajorRev = Integer.decode(myDigits[0]);
                  Integer myMinorRev = Integer.decode(myDigits[1]);

                  if ((myMajorRev != null) && (myMinorRev != null)) {
                     myVersion = Integer.valueOf((myMajorRev.intValue() * 1000) + myMinorRev.intValue());
                  }
               }
            }
         }
      } catch (Exception IGNORE) {}
      return myVersion;
   }

   private void removeAccessControls() throws IOException {
      LOG.info("Remove access controls from database server.");

      String myNewFileName = newdbdata.getCanonicalPath() + File.separator + PG_HBA_CONF;
      String myOldFileName = dbdata.getCanonicalPath() + File.separator + PG_HBA_CONF;
      String myNewBackupName = newdbdata.getCanonicalPath() + File.separator + "save_" + PG_HBA_CONF;
      String myOldBackupName = dbdata.getCanonicalPath() + File.separator + "save_" + PG_HBA_CONF;
      File myOldHba = new File(myOldFileName);
      File myOldHbaBak = new File(myOldBackupName);
      File myNewHba = new File(myNewFileName);
      File myNewHbaBak = new File(myNewBackupName);
      String myOS = System.getProperty("os.name");
      boolean myUnixFlag =  myOS.contains("nux") || myOS.contains("nix");

      if (myOldHba.exists()) {
         myOldHba.renameTo(myOldHbaBak);

         try (FileWriter fileWriter = new FileWriter(myOldFileName);
              BufferedWriter myOldWriter = new BufferedWriter(fileWriter)) {
            myOldWriter.write("host\tall\tall\t127.0.0.1/32\ttrust\n");
            myOldWriter.write("host\tall\tall\t::1/128\ttrust\n");

            if (myUnixFlag) {
               myOldWriter.write("local\tall\tall\ttrust\n");
            }
         }
      }
      if (myNewHba.exists()) {
         myNewHba.renameTo(myNewHbaBak);

         try (FileWriter fileWriter = new FileWriter(myNewFileName);
              BufferedWriter myNewWriter = new BufferedWriter(fileWriter)) {
            myNewWriter.write("host\tall\tall\t127.0.0.1/32\ttrust\n");
            myNewWriter.write("host\tall\tall\t::1/128\ttrust\n");

            if (myUnixFlag) {
               myNewWriter.write("local\tall\tall\ttrust\n");
            }
         }
      }
   }

    private void restoreAccessControls() throws IOException {

        LOG.info("Restore access controls for database server.");
        String myNewFileName = newdbdata.getCanonicalPath() + File.separator + PG_HBA_CONF;
        String myOldFileName = dbdata.getCanonicalPath() + File.separator + PG_HBA_CONF;
        String myNewBackupName = newdbdata.getCanonicalPath() + File.separator + "save_" + PG_HBA_CONF;
        String myOldBackupName = dbdata.getCanonicalPath() + File.separator + "save_" + PG_HBA_CONF;
        File myOldHba = new File(myOldFileName);
        File myOldHbaBak = new File(myOldBackupName);
        File myNewHba = new File(myNewFileName);
        File myNewHbaBak = new File(myNewBackupName);

        if (myOldHba.exists()) {

            myOldHba.delete();
            myOldHbaBak.renameTo(myOldHba);
        }
        if (myNewHba.exists()) {

            myNewHba.delete();
            myNewHbaBak.renameTo(myNewHba);
        }
    }

    private void migrateData(String pathIn) throws IOException {

        String command = (null != migrateCommand) ? migrateCommand : buildMigrateCommand(pathIn);

        LOG.info("Migrate data from existing database to new database using command:\n\n{}\n", () -> command);

        Process myProcess = Runtime.getRuntime().exec(command);
        StreamGobbler myErrorLogger = new StreamGobbler(myProcess.getErrorStream(), StreamType.ERROR);
        StreamGobbler myInfoLogger = new StreamGobbler(myProcess.getInputStream(), StreamType.INFO);
        myErrorLogger.start();
        myInfoLogger.start();

        if (waitForResponse(myProcess) != 0) {

            LOG.error("Data migration failed.");
            throw new IOException("Failed migrating \"metadb\"");
        }
        LOG.info("Data migration succeeded.");
    }

    private String buildMigrateCommand(String pathIn) throws IOException {

        StringBuilder myBuffer = new StringBuilder();

        if (unixFlag) {

            String myBasePath = dbroot.getCanonicalPath();

            myBuffer.append(myBasePath);
            myBuffer.append("/migrate.sh ");
            myBuffer.append(dbPort);

        } else {

			myBuffer.append(pathIn);
			myBuffer.append("pg_upgrade");
			myBuffer.append(" -d ");
			myBuffer.append(Format.value(dbdata.getCanonicalPath()));
			myBuffer.append(" -D ");
			myBuffer.append(Format.value(newdbdata.getCanonicalPath()));
			myBuffer.append(" -b ");
			myBuffer.append(Format.value(dbbin.getCanonicalPath()));
			myBuffer.append(" -B ");
			myBuffer.append(Format.value(newdbbin.getCanonicalPath()));
			myBuffer.append(" -p ");
			myBuffer.append(dbPort);
			myBuffer.append(" -P ");
			myBuffer.append(dbPort);

            LOG.info("Begin database migration using the command:\n{}", () -> myBuffer.toString());
        }

        return myBuffer.toString();
    }
/*
    private void migrateData(String pathIn) throws IOException {

        LOG.info("Migrate data from existing database to new database.");
        String[] command = new String[] {

                pathIn + "pg_upgrade",
                "--old-datadir",
                "\"" + dbdata.getCanonicalPath() + "\"",
                "--new-datadir",
                "\"" + newdbdata.getCanonicalPath() + "\"",
                "--old-bindir",
                "\"" + dbbin.getCanonicalPath() + "\"",
                "--new-bindir",
                "\"" + newdbbin.getCanonicalPath() + "\"",
                "--old-port=" + dbPort,
                "--new-port=" + dbPort
        };
        LOG.info("using command:\n\n"
                + pathIn + "pg_upgrade\n"
                + "\t--old-datadir" + " \"" + dbdata.getCanonicalPath() + "\"\n"
                + "\t--new-datadir" + " \"" + newdbdata.getCanonicalPath() + "\"\n"
                + "\t--old-bindir" + " \"" + dbbin.getCanonicalPath() + "\"\n"
                + "\t--new-bindir" + " \"" + newdbbin.getCanonicalPath() + "\"\n"
                + "\t--old-port=" + dbPort + "\n"
                + "\t--new-port=" + dbPort + "\n");

        Process myProcess = Runtime.getRuntime().exec(command);

        StreamGobbler myErrorLogger = new StreamGobbler(myProcess.getErrorStream(), StreamType.ERROR);
        StreamGobbler myInfoLogger = new StreamGobbler(myProcess.getInputStream(), StreamType.INFO);
        myErrorLogger.start();
        myInfoLogger.start();

        if (0 != waitForResponse(myProcess)) {

            LOG.error("Data migration failed.");
            throw new IOException("Failed migrating \"metadb\"");
        }
        LOG.error("Data migration succeeded.");
    }
*/
    private int waitForResponse(Process processIn) {

        int myReturnCode = 1;
        boolean myRetry = true;

        while (myRetry) {

            try {

                myRetry = false;
                myReturnCode = processIn.waitFor();

            } catch (InterruptedException myException) {

                myRetry = true;
            }
        }
        return myReturnCode;
    }

    private void initDataServer(String pathIn, File dbDataIn) throws Exception {

        String effort = "Initializing the database server.";

        try {
           LOG.info(effort);
            WorkingDisplay.begin();
            preCommandWait();
            execCommand(true, pathIn, INIT_DB, dbDataIn.getCanonicalPath());
            postCommandWait();
            FileUtil.copyDirectory(configDir, dbDataIn, new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return !name.equalsIgnoreCase(PG_HBA_CONF);
                }
            });
            WorkingDisplay.cancel();

        } catch (Exception myException) {

            String myError = "Caught exception " + effort;

            WorkingDisplay.cancel();
            LOG.error(myError, myException);
            stopDataServer();
            throw new CentrifugeException(myError, myException);
        }
    }

    private void createDataServer() throws CentrifugeException {

        boolean myclusterExists = dbdata.exists();
        try {

            if (myclusterExists) {

                removeAccessControls();
                startDataServer();
                if (!cacheTag.exists()) {

                    createCacheDataBase();
                }

            } else {

                initDataServer(binpath, dbdata);
                startDataServer();
                createMetaDataBase();
                createCacheDataBase();
            }
            restoreAccessControls();
            LOG.info("Copy the generic configuration file to the MetaDB database.");
            FileUtil.copyDirectory(configDir, dbdata, new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.equalsIgnoreCase(PG_HBA_CONF);
                }
            });
            reloadConfiguration();

        } catch (Exception myException) {

            LOG.error("Caught exception creating the database server and the MetaDB database.", myException);
            stopDataServer();
            if (!myclusterExists) {

                FileUtil.deleteDir(dbdata);
            }
            throw new CentrifugeException("Failed to create cache database", myException);
        }
    }

    private void createMetaDataBase() throws CentrifugeException {

        String effort = "Creating the MetaDB database.";

        try {
           LOG.info(effort);
            WorkingDisplay.begin();
            preCommandWait();
            execCommand(true, binpath, EXEC_SQL_FILE, DB_LOCAL, dbPort, createMetaDB.getCanonicalPath());
            postCommandWait();
            WorkingDisplay.cancel();

        } catch (Exception myException) {

            String myError = "Caught exception " + effort;

            WorkingDisplay.cancel();
            LOG.error(myError, myException);
            stopDataServer();
            throw new CentrifugeException(myError, myException);
        }
    }

    private void createCacheDataBase() throws CentrifugeException {

        String effort = "Creating the CacheDB database.";

        try {
            LOG.info(effort);
            WorkingDisplay.begin();
            preCommandWait();
            execCommand(true, binpath, EXEC_SQL_FILE, DB_LOCAL, dbPort, createCacheDB.getCanonicalPath());
            cacheTag.createNewFile();
            postCommandWait();
            WorkingDisplay.cancel();

        } catch (Exception myException) {

            String myError = "Caught exception " + effort;

            WorkingDisplay.cancel();
            LOG.error(myError, myException);
            stopDataServer();
            throw new CentrifugeException(myError, myException);
        }
    }

    private void reloadConfiguration() throws CentrifugeException {

        String effort = "Reloading the configuration.";

        try {

            LOG.info(effort);
            WorkingDisplay.begin();
            preCommandWait();
//            execCommand(true, binpath, RELOAD_DBSERVER, dbdata.getCanonicalPath());
            execCommand(true, binpath, RESTART_DBSERVER, dbdata.getCanonicalPath());
            postCommandWait();
            WorkingDisplay.cancel();
            dbRunning = true;

        } catch (Throwable myException) {

            String myError = "Caught exception " + effort;

            WorkingDisplay.cancel();
            LOG.error(myError, myException);
            stopDataServer();
            throw new CentrifugeException(myError, myException);
        }
    }

    private void startDataServer() throws CentrifugeException {

        String effort = "Starting the database server.";

        try {

           LOG.info(effort);
           WorkingDisplay.begin();
            preCommandWait();
            execCommand(true, binpath, START_DBSERVER, dbdata.getCanonicalPath());
            // since -w seems to be unreliable, go wait for the server to start
            postCommandWait();
            WorkingDisplay.cancel();
            dbRunning = true;

        } catch (Throwable myException) {

            String myError = "Caught exception " + effort;

            WorkingDisplay.cancel();
            LOG.error(myError, myException);
            stopDataServer();
            throw new CentrifugeException(myError, myException);
        }
    }

    private void stopDataServer() throws CentrifugeException {

        if (dbRunning) {

            LOG.info("Stopping the database server.");
            WorkingDisplay.begin();
            stopDataServer(0);
        }
    }

    private void stopDataServer(int depthIn) throws CentrifugeException {

        //prevent stack overflow
        if (MAX_CALL_DEPTH > depthIn) {

            try {

                preCommandWait();
                execCommand(true, binpath, STOP_DBSERVER, dbdata.getCanonicalPath());
                postCommandWait();
                WorkingDisplay.cancel();
                dbRunning = false;

            } catch (Throwable myException) {

                stopDataServer(depthIn + 1);
                if (0 == depthIn) {

                    String myError = "Caught exception Stopping the database server.";

                    WorkingDisplay.cancel();
                    LOG.error(myError, myException);
                    throw new CentrifugeException(myError, myException);
                }
                // Otherwise IGNORE!
            }
        }
    }

    private int execCommand(boolean waitIn, String exepathIn, String commandIn, Object... argsIn) throws Exception {

        String formatted = String.format(commandIn, argsIn);
        LOG.info(exepathIn + formatted.replace(DELIM," "));

        String[] commands = getCommandArray(exepathIn + formatted);
        Integer myReturnCode = execCommandArray(commands, waitIn);

        if (myReturnCode != 0) {

            throw new CentrifugeException("Return code " + myReturnCode.toString()
                    + " executing command: \n" + exepathIn + formatted);
        }
        return myReturnCode;
    }

   private String[] getCommandArray(String command) {
      String[] tokens = DELIM_PATTERN.split(command);

      for (int i = 0; i < tokens.length; i++) {
         if (i == 0) {
            LOG.debug("cmd: " + tokens[i]);
         } else {
            LOG.debug(" p" + i + ": " + tokens[i]);
         }
      }
      return tokens;
   }

    private int execCommandArray(String[] commandIn, boolean waitIn) throws Exception {

        LOG.debug("*executing command: {}", () -> commandIn[0]);

        Runtime myRuntime = Runtime.getRuntime();
        Process myProcess = myRuntime.exec(commandIn);

        StreamGobbler errorGobbler = new StreamGobbler(myProcess.getErrorStream(), StreamType.DEBUG);
        StreamGobbler outGobbler = new StreamGobbler(myProcess.getInputStream(), StreamType.DEBUG);
        errorGobbler.start();
        outGobbler.start();
        return (waitIn) ? waitForResponse(myProcess) : 0;
    }

   private void preCommandWait() {
      try {
         Thread.sleep(Configuration.getInstance().getDataCacheConfig().getPreCommandWait());
      } catch (InterruptedException IGNORE) {}
   }

   private void postCommandWait() {
      try {
         Thread.sleep(Configuration.getInstance().getDataCacheConfig().getPostCommandWait());
      } catch (InterruptedException IGNORE) {}
   }

    private void installSqlFunctions() {

        for (int i = 0; _configuredCommandFiles.length > i; i++) {

            try {

                InputStream myStream = getConfiguredSql(_configuredCommandFiles[i]);

                if (null != myStream) {

                    executeRequest(myStream, _configuredCommandFiles[i]);
                }

            } catch (Exception myException) {

                LOG.error("Caught exception loading SQL command file "
                            + Format.value(_configuredCommandFiles[i]), myException);
            }
        }
    }

    private InputStream getConfiguredSql(String fileNameIn) throws InitializationException, FileNotFoundException {

        File myFile = new File("cachedb/ddl/" + fileNameIn);

        return myFile.exists() ? new FileInputStream(myFile) : null;
    }

   private void executeRequest(InputStream commandStreamIn, String fileNameIn) throws InitializationException {
      if (commandStreamIn != null) {
         try (Connection connection = CsiPersistenceManager.getCacheConnection()) {
            String sql = IOUtils.toString(commandStreamIn);

            try (Statement myStatement = connection.createStatement()) {
               myStatement.execute(sql);
            } catch (Exception myException) {
               SqlUtil.quietRollback(connection);
               throw new InitializationException("Failed executing " + Format.value(fileNameIn), myException);
            }
            connection.commit();
         } catch (Exception myException) {
            throw new InitializationException("Failed executing " + Format.value(fileNameIn), myException);
         } finally {
            IOUtils.closeQuietly(commandStreamIn);
         }
      }
   }

    private void waitForUserResponse() {

        try {

            System.in.read();

        } catch (Exception IGNORE) {}
    }

    // Delete directory marked for discard.
    private void finalizeDirectoryDelete(File directoryIn) {

        File myTarget = new File(directoryIn.getName() + "_DISCARD");

        if (myTarget.exists()) {

            try {

                FileUtils.deleteDirectory(myTarget);

            } catch (Exception IGNORE) {}
        }
    }

    private void deleteDirectory(File directoryIn) {

        if (directoryIn.exists()) {

            try {

                FileUtils.deleteDirectory(directoryIn);

            } catch (Exception myException) {

                if (directoryIn.exists()) {

                    try {

                        directoryIn.renameTo(new File(directoryIn.getName() + "_DISCARD"));

                    } catch (Exception IGNORE) { }
                }
            }
        }
    }

    // Used during upgrade
    private boolean canRename(File directoryIn) {

        boolean mySuccess = false;
        File myScratch = new File(directoryIn.getPath() + SCRATCH);

        try {

            if (myScratch.exists()) {
// TODO: ???
                FileUtils.deleteDirectory(directoryIn);
            }

        } catch (Exception IGNORE) {

            LOG.error("Unable to remove \"csi_scratch\" directory from \"cachedb\" directory.");
        }
        try {

            directoryIn.renameTo(myScratch);
            if (!directoryIn.exists()) {

                myScratch.renameTo(directoryIn);
                mySuccess = true;
            }
        } catch (Exception IGNORE) {}
        return mySuccess;
    }
}
