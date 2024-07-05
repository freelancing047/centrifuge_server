package csi.tools;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import csi.server.business.cachedb.PGCacheDataConsumer;
import csi.server.business.cachedb.PGCacheDataDump;
import csi.server.business.helper.DataCacheBuilder;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.QueryHelper;
import csi.server.business.helper.RecoveryHelper;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiConnection;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 5/21/2018.
 */
public class DataMover implements CustomTaskChange {
   private static final Logger LOG = LogManager.getLogger(DataMover.class);

   private static boolean _doDebug = LOG.isDebugEnabled();

   private static final String TABLE_PREFIX = "cache_";
   private static final String LINKUP_STRING =
      "linkup_[A-Fa-f0-9]{8}_[A-Fa-f0-9]{4}_[A-Fa-f0-9]{4}_[A-Fa-f0-9]{4}_[A-Fa-f0-9]{12}";
   private static final String UUID_STRING =
      "([A-Fa-f0-9]{8})-([A-Fa-f0-9]{4})-([A-Fa-f0-9]{4})-([A-Fa-f0-9]{4})-([A-Fa-f0-9]{12})";
   private static final Pattern LINKUP_PATTERN = Pattern.compile(LINKUP_STRING);
   private static final Pattern UUID_PATTERN = Pattern.compile(UUID_STRING);

   private class IgnoreException extends CentrifugeException {
      public IgnoreException(String messageIn) {
         super(messageIn);
      }

      public IgnoreException(String messageIn, Exception myExceptionIn) {
         super(messageIn);
      }
   }

   private static boolean _needTableViews = false;
   private static boolean _needTopViews = false;

   private Connection _metaConnection = null;
   private CsiConnection _cacheConnection = null;
   private String _metaName;
   private String _cacheName;

   public DataMover() {
   }

   public static boolean requiresCleanup() {
      return _needTableViews || _needTopViews;
   }

   public static void cleanupMigration() {
      if (requiresCleanup()) {
         try (Connection metaConnection = CsiPersistenceManager.getMetaConnection();
              CsiConnection cacheConnection = CsiPersistenceManager.getCacheConnection()) {
            if (_needTableViews) {
               createInstalledTableViews(metaConnection, cacheConnection);
            }
            if (_needTopViews) {
               createDataViewTopViews(metaConnection, cacheConnection);
            }
         } catch (Exception exception) {
            LOG.fatal("Caught fatal exception while creating missing views!", exception);
         }
      }
   }

   @Override
   public void execute(Database database) throws CustomChangeException {
      try {
         _metaConnection = CsiPersistenceManager.getMetaConnection();
         _cacheConnection = CsiPersistenceManager.getCacheConnection();
         _metaName = DataCacheHelper.getMetaName();
         _cacheName = DataCacheHelper.getCacheName();

         if ((null != _metaConnection) && (null != _cacheConnection) && (null != _metaName) && (null != _cacheName)
               && (!_metaName.equals(_cacheName))) {

            moveInstalledTableData();
            moveDataViewData();
         }

      } catch (Exception myException) {

         LOG.fatal("Caught fatal exception while migrating data!", myException);
         throw new CustomChangeException();

      } finally {

         _cacheConnection = (CsiConnection) close(_cacheConnection);
         _metaConnection = close(_metaConnection);
      }
   }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }

   private void moveInstalledTableData() throws CustomChangeException {
      String queryString = "SELECT id, tablename, viewname FROM waitingtables";
      boolean retryFlag = true;
      boolean activeFlag = false;

      while (retryFlag) {
         try (Connection connection = CsiPersistenceManager.getMetaConnection();
              Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
              ResultSet results = statement.executeQuery(queryString)) {
            retryFlag = false;

            try {
               while (results.next()) {
                  processInstalledTable(results);

                  activeFlag = true;
               }
            } catch (Exception exception) {
               LOG.error("Caught an exception accessing next item in InstalledTable resultset.\n"
                         + exception.getMessage());

               if (!activeFlag) {
                  throw new CustomChangeException();
               }
               retryFlag = activeFlag;
            }
         } catch (CustomChangeException cce) {
            throw cce;
         } catch (Exception exception) {
            LOG.error("Caught an exception retrieving list of InstalledTables for moving data to CacheDB database.\n"
                       + exception.getMessage());
            throw new CustomChangeException();
         }
      }
   }

   private void moveDataViewData() throws CustomChangeException {
      String queryString = "SELECT id, tablename, viewname, metaid, linkups FROM waitingdataviews";
      boolean retryFlag = true;
      boolean activeFlag = false;

      while (retryFlag) {
         try (Connection connection = CsiPersistenceManager.getMetaConnection();
              Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
              ResultSet results = statement.executeQuery(queryString)) {
            retryFlag = false;

            try {
               while (results.next()) {
                  processDataView(results);

                  activeFlag = true;
               }
            } catch (Exception myException) {
               LOG.error("Caught an exception accessing next item in DataView resultset.\n" + myException.getMessage());

               if (!activeFlag) {
                  throw new CustomChangeException();
               }
               retryFlag = activeFlag;
            }
         } catch (CustomChangeException cce) {
            throw cce;
         } catch (Exception exception) {
            LOG.error("Caught an exception retrieving list of DataViews for moving data to CacheDB database.\n"
                      + exception.getMessage());
            throw new CustomChangeException();
         }
      }
   }

   private void processInstalledTable(ResultSet resultsIn) {
      try {
         String myId = resultsIn.getString(1);

         logTransfer(myId, 6);

         String myTableName = resultsIn.getString(2);
         String myViewName = resultsIn.getString(3);
         String[] myTableList = new String[] { myTableName };
         String myCommandString = "DELETE FROM \"waitingtables\" WHERE \"id\" = '" + myId + "'";
         long myRowCount = copyTables(myId, myTableList, null);

         LOG.info("Transfered " + Long.toString(myRowCount) + " row(s).");

         if (createView(myViewName)) {
            clearOldData(_metaConnection, myTableList, myViewName);
            QueryHelper.executeSQL(_metaConnection, myCommandString, null);
            _metaConnection.commit();
         } else {
            rollback(_metaConnection);
            recordMissingTableView(myId, myTableList, myViewName);
            throw new CentrifugeException("Unable to create view " + quote(myViewName));
         }
      } catch (Exception exception) {
         rollback(_metaConnection);
         LOG.error("Caught an exception moving Installed Table data to CacheDB database.\n"
                   + exception.getMessage());
      }
   }

    private void processDataView(ResultSet resultsIn) {

        try {

            String myId = resultsIn.getString(1);
            String myNameIn = resultsIn.getString(2);
            String myViewIn = resultsIn.getString(3);
            String myTableName = (null != myNameIn) ? myNameIn : TABLE_PREFIX + myId.replace('-', '_') + "_0";

            String myViewName = (null != myViewIn) ? myViewIn : TABLE_PREFIX + myId.replace('-', '_');

            if (verifyTable(myTableName)) {

                logTransfer(myId, 0);

                String myFixup = resultsIn.getString(5);
                String[] myFirstSplit = ((null != myFixup) && (70 < myFixup.length()))
                        ? StringUtil.split(myFixup, '|') : null;
                String[] myTableList = ((null != myFirstSplit) && (0 < myFirstSplit.length))
                        ? new String[myFirstSplit.length + 1] : new String[1];
                String myCommandString = "DELETE FROM \"waitingdataviews\" WHERE \"id\" = '" + myId + "'";

                myTableList[0] = myTableName;
                if (null != myFirstSplit) {

                    for (int i = 0; myFirstSplit.length > i; i++) {

                        String[] myPair = StringUtil.split(myFirstSplit[i], ',');

                        myTableList[i + 1] = myPair[1];
                    }
                }
                if ((null == myNameIn) || (null == myViewIn)) {

                    updateDataView(myId, myTableName, myViewName);
                }
                long myRowCount = copyTables(myId, myTableList, myViewName);
                LOG.info("Transfered " + Long.toString(myRowCount) + " row(s).");
                if (createView(myViewName)) {

                    clearOldData(_metaConnection, myTableList, myViewName);
                    QueryHelper.executeSQL(_metaConnection, myCommandString, null);
                    _metaConnection.commit();

                } else {

                    rollback(_metaConnection);
                    if (0L < myRowCount) {

                        recordMissingTopView(myId, myTableList, myViewName);
                    }
                    LOG.error("Unable to create view " + quote(myViewName));
                }

            } else {

                rollback(_metaConnection);
                LOG.error("Unable to verify existence of table " + quote(myTableName));
            }

        } catch (IgnoreException myException) {

            LOG.info(myException.getMessage());

        } catch (Exception myException) {

            LOG.error("Caught an exception moving DataView data to CacheDB database.\n"
                    + myException.getMessage());
        }
    }

    private static void clearOldData(Connection metaConnectionIn, String[] tableListIn, String viewNameIn) {

        try {

            QueryHelper.executeSQL(metaConnectionIn, "DROP VIEW IF EXISTS \"" + viewNameIn + "\"", null);
            metaConnectionIn.commit();

        } catch (Exception IGNORE) {

            rollback(metaConnectionIn);
        }
        for (String myTableName : tableListIn) {

            try {

                QueryHelper.executeSQL(metaConnectionIn, "DROP TABLE IF EXISTS \"" + myTableName + "\"", null);
                metaConnectionIn.commit();

            } catch (Exception IGNORE) {

                rollback(metaConnectionIn);
            }
        }
    }

   private long copyTables(String id, String[] tableList, String baseName) throws Exception {
      long rowCount = 0L;

      if (tableList != null) {
         for (int i = 0; i < tableList.length; i++) {
            String source = tableList[i];

            if (StringUtils.isNotEmpty(source)) {
               String target = source;

               if (baseName != null) {
                  target = source.replace(LINKUP_STRING, baseName);
               }
               List<ValuePair<String,String>> columnList = createTable(source, target);

               if ((columnList != null) && !columnList.isEmpty()) {
                  rowCount += copyData(source, target, columnList);
               }
            }
         }
      }
      return rowCount;
   }

   private List<ValuePair<String,String>> createTable(String source, String target)
         throws CentrifugeException {
      List<ValuePair<String,String>> columnList = Collections.emptyList();

      try {
         columnList = DataCacheHelper.getColumnList(_metaConnection, source);

         if ((columnList != null) && !columnList.isEmpty()) {
            String dropCommand = "DROP TABLE IF EXISTS \"" + target + "\"";
            String createCommand = createTableCommand(target, columnList);

            createObject(dropCommand, createCommand);
         }
      } catch (Exception myException) {
         String basicMessage = "Caught an exception creating the table \""
                                 + target + "\" from \"" + source + "\"";

         LOG.error(basicMessage + "\n" + myException.getMessage());
         throw new CentrifugeException(basicMessage, myException);
      }
      return columnList;
   }

   private static String createTableCommand(String tableNameIn, List<ValuePair<String,String>> columnListIn) {
      StringBuilder buffer = new StringBuilder("CREATE TABLE \"").append(tableNameIn).append("\" (");
      boolean first = true;

      for (ValuePair<String,String> myPair : columnListIn) {
         if (first) {
            first = false;
         } else {
            buffer.append(", ");
         }
         buffer.append("\"").append(myPair.getValue1()).append("\" ").append(myPair.getValue2());
      }
      return buffer.append(", PRIMARY KEY (\"internal_id\"))").toString();
   }

    private long copyData(String source, String target, List<ValuePair<String, String>> columnList)
            throws CentrifugeException {
        long rowCount = 0L;
        String columnNames = formatColumnNames(columnList);

        try (PipedInputStream reader = new PipedInputStream();
             PipedOutputStream writer = new PipedOutputStream(reader)) {
            Connection connection = _cacheConnection.getConnection();
            PGCacheDataDump producer = new PGCacheDataDump(writer, _metaConnection, source, columnNames);
            PGCacheDataConsumer consumer = new PGCacheDataConsumer(reader, connection, target, columnNames);

            consumer.start();
            producer.start();

            rowCount = RecoveryHelper.monitorTransfer(_cacheConnection, producer, consumer, false);
        } catch (Exception exception) {
            String myBasicMessage = "Caught an exception while copying data from \""
                    + source + "\" to \"" + target + "\"";
            LOG.error(myBasicMessage + "\n" + exception.getMessage());
            throw new CentrifugeException(myBasicMessage, exception);
        }
        return rowCount;
    }

    private boolean createView(String viewNameIn) {

        boolean mySuccess = false;
        String myCreateCommand = null;
        String myDropCommand = "DROP VIEW IF EXISTS \"" + viewNameIn + "\"";

        try {

            myCreateCommand = createViewCommand(viewNameIn);
            if (null != myCreateCommand) {

                createObject(myDropCommand, myCreateCommand);
                mySuccess = true;
            }

        } catch (Exception myException) {

            String myBasicMessage = "Caught an exception creating view \"" + viewNameIn + "\"";

            LOG.error(myBasicMessage + "\n" + myException.getMessage());

            if (null != myCreateCommand) {

                try {

                    myCreateCommand = fixCreateCommand(myCreateCommand, viewNameIn);
                    createObject(myDropCommand, myCreateCommand);
                    mySuccess = true;

                } catch (Exception myNextException) {

                    myBasicMessage = "Caught an exception on second attempt at creating view \"" + viewNameIn + "\"";
                   LOG.error(myBasicMessage + "\n" + myException.getMessage());
                }
            }
        }
        return mySuccess;
    }

   private static String fixCreateCommand(String commandIn, String viewNameIn) {
      String myFormattedCommand = UUID_PATTERN.matcher(commandIn).replaceAll("$1_$2_$3_$4_$5");

      return LINKUP_PATTERN.matcher(myFormattedCommand).replaceAll(viewNameIn);
   }

    private String createViewCommand(String viewName) throws CentrifugeException, SQLException {
       StringBuilder buffer = new StringBuilder();
       String viewRequest =
          new StringBuilder("SELECT definition from pg_catalog.pg_views where viewowner = 'csiserver' and viewname = '")
                    .append(viewName).append("'").toString();

       try (ResultSet results = QueryHelper.executeSingleQuery(_metaConnection, viewRequest, null)) {
          if (results.next()) {
             String viewCommand = results.getString(1);

             if (StringUtils.isNotEmpty(viewCommand)) {
                buffer.append("CREATE VIEW \"").append(viewName).append("\" AS ").append(viewCommand);
             }
          }
       }
       return (buffer.length() > 0) ? buffer.toString() : null;
    }

    private void createObject(String dropCommandIn, String createCommandIn) throws CentrifugeException, SQLException {

        if (0 < createCommandIn.length()) {

            try {

                if (_doDebug) {
                  LOG.debug(dropCommandIn);
               }
                QueryHelper.executeSQL(_cacheConnection, dropCommandIn, null);
                if (_doDebug) {
                  LOG.debug(createCommandIn);
               }
                QueryHelper.executeSQL(_cacheConnection, createCommandIn, null);
                _cacheConnection.commit();

            } catch (Exception myException) {

                _cacheConnection.rollback();
                throw myException;
            }
        }
    }

   private static String formatColumnNames(List<ValuePair<String, String>> columnListIn) {
      StringBuilder buffer = new StringBuilder();
      boolean first = true;

      for (ValuePair<String, String> myPair : columnListIn) {
         if (first) {
            first = false;
         } else {
            buffer.append(",");
         }
         buffer.append("\"").append(myPair.getValue1()).append("\"");
      }
      return buffer.toString();
   }

   private boolean verifyTable(String tableName) {
      boolean verified = false;
      String viewRequest =
         new StringBuilder("SELECT COUNT(1) from pg_catalog.pg_tables where tableowner = 'csiserver' and tablename = '")
                   .append(tableName).append("'").toString();

      try (ResultSet results = QueryHelper.executeSingleQuery(_metaConnection, viewRequest, null)) {
         verified = results.next();
      } catch (Exception IGNORE) {
      }
      return verified;
   }

   private void updateDataView(String idIn, String tableNameIn, String viewNameIn) {
      String sql = new StringBuilder("UPDATE dataview SET(tables, views) = ('")
                             .append(tableNameIn).append("','").append(viewNameIn)
                             .append("') WHERE uuid = '").append(idIn).append("'").toString();

      try {
         QueryHelper.executeSQL(_metaConnection, sql, null);
         _metaConnection.commit();
      } catch (Exception myException) {
         rollback(_metaConnection);
         LOG.error("Caught an exception updating DataView definition.\n" + myException.getMessage());
      }
   }

   private void logTransfer(String idIn, int typeIn) throws Exception {
      String queryString =
         new StringBuilder("SELECT resourcetype, name, owner FROM modelresource WHERE uuid = '")
                   .append(idIn).append("'").toString();

      try (ResultSet results = QueryHelper.executeSingleQuery(_metaConnection, queryString, null)) {
         if (results.next()) {
            Integer type = (results.getObject(1) != null) ? results.getInt(1) : null;
            String name = results.getString(2);
            String owner = results.getString(3);

            if (results.getString(2) == null) {
               throw new CentrifugeException("No name returned for Resource with id \"" + idIn + "\"");
            } else if ((type == null) || (type != typeIn)) {
               Integer foundOrdinal = (Integer) results.getObject(1);
               AclResourceType myFoundType = ((foundOrdinal != null) && (0 <= foundOrdinal)
                     && (AclResourceType.values().length > foundOrdinal)) ? AclResourceType.values()[foundOrdinal]
                            : null;
                AclResourceType myDesiredType = ((0 <= typeIn) && (AclResourceType.values().length > typeIn))
                      ? AclResourceType.values()[typeIn]
                      : null;
                String myFoundLabel = (null != myFoundType) ? myFoundType.getDescriptor()
                      : ((null != foundOrdinal) ? Integer.toString(foundOrdinal) : "");
                String myDesiredLabel = (null != myDesiredType) ? myDesiredType.getDescriptor()
                      : Integer.toString(typeIn);

                throw new CentrifugeException(
                      "Requested type \"" + myDesiredLabel + "\" does not match returned type \"" + myFoundLabel
                            + "\" for Resource " + quote(name) + "with id \"" + idIn + "\"");
             } else if (owner == null) {
                String myMessage = "No Data wil be transfered for DataView " + quote(name) + " which has no owner.";

                if (typeIn == 0) {
                   throw new IgnoreException(myMessage);
                } else {
                   throw new CentrifugeException(myMessage);
                }
             } else {
                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append("Move data for ");

                if (type == 0) {
                   myBuffer.append("Dataview ");
                } else if (type == 6) {
                   myBuffer.append("Installed Table ");
                }
                if (name == null) {
                   myBuffer.append("<null>");
                } else {
                   myBuffer.append('"');
                   myBuffer.append(name);
                   myBuffer.append('"');
                }
                myBuffer.append(" owned by ");
                myBuffer.append('"');
                myBuffer.append(owner);
                myBuffer.append('"');
                myBuffer.append(" to new CacheDB database.");
                LOG.info(myBuffer.toString());
             }
          } else {
             throw new CentrifugeException("No results returned for Resource with id \"" + idIn + "\"");
          }
       }
    }

    private static String quote(String valueIn) {

        if (null != valueIn) {

            return "\"" + valueIn + "\"";

        } else {

            return "<null>";
        }
    }

    private static Connection close(Connection connectionIn) {

        try {

            if (null != connectionIn) {

                connectionIn.close();
            }

        } catch (Exception IGNORE) {}
        return null;
    }

    private static void rollback(Connection connectionIn) {

        try {

            connectionIn.rollback();

        } catch (Exception IGNORE) {}
    }

    private void recordMissingTableView(String installedTableIdIn, String[] tableListIn, String viewNameIn) {

        String myTableList = StringUtil.concatInput(tableListIn);
        String myCommand = "INSERT INTO \"missingtableviews\" (\"id\", \"tablestring\", \"viewname\") VALUES ('"
                             + installedTableIdIn + "', '" + myTableList + "', '" + viewNameIn + "')";

        try {

            QueryHelper.executeSQL(_metaConnection, myCommand, null);
            _metaConnection.commit();
            _needTableViews = true;

        } catch (Exception myException) {

            LOG.error("CaughtException recording missing InstalledTable view.\n" + myException.getMessage());
        }
    }

    private void recordMissingTopView(String dataViewIdIn, String[] tableListIn, String viewNameIn) {

        String myTableList = StringUtil.concatInput(tableListIn);
        String myCommand = "INSERT INTO \"missingtopviews\" (\"id\", \"tablestring\", \"viewname\") VALUES ('"
                            + dataViewIdIn + "', '" + myTableList + "', '" + viewNameIn + "')";

        try {

            QueryHelper.executeSQL(_metaConnection, myCommand, null);
            _metaConnection.commit();
            _needTopViews = true;

        } catch (Exception myException) {

            LOG.error("CaughtException recording missing DataView top view.\n" + myException.getMessage());
        }
    }

   private static void createInstalledTableViews(Connection metaConnection, CsiConnection cacheConnection) {
      String queryString = "SELECT id, tablestring, viewname FROM missingtableviews";
      boolean retryFlag = true;
      boolean activeFlag = false;

      while (retryFlag) {
         try (Connection connection = CsiPersistenceManager.getMetaConnection();
              Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
              ResultSet results = statement.executeQuery(queryString)) {
            retryFlag = false;

            try {
               while (results.next()) {
                  createTableView(metaConnection, results);

                  activeFlag = true;
               }
            } catch (Exception myException) {
               LOG.error("Caught an exception accessing next item in missingtableviews resultset.\n"
                     + myException.getMessage());

               if (!activeFlag) {
                  throw new CustomChangeException();
               }
               retryFlag = activeFlag;
            }
         } catch (Exception exception) {
            LOG.error("Caught an exception retrieving missingtableviews resultset.\n"
                      + exception.getMessage());
         }
      }
   }

   private static void createDataViewTopViews(Connection metaConnection, CsiConnection cacheConnection) {
      String queryString = "SELECT id, tablestring, viewname FROM missingtopviews";
      boolean retryFlag = true;
      boolean activeFlag = false;

      while (retryFlag) {
         try (Connection connection = CsiPersistenceManager.getMetaConnection();
              Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
              ResultSet results = statement.executeQuery(queryString)) {
            retryFlag = false;

            try {
               while (results.next()) {
                  createTopView(metaConnection, cacheConnection, results);

                  activeFlag = true;
               }
            } catch (Exception myException) {
               LOG.error("Caught an exception accessing next item in missingtopviews resultset.\n"
                         + myException.getMessage());

               if (!activeFlag) {
                  throw new CustomChangeException();
               }
               retryFlag = activeFlag;
            }
         } catch (Exception myException) {
            LOG.error("Caught an exception retrieving missingtopviews resultset.\n"
                      + myException.getMessage());
         }
      }
   }
/*
        } catch (Exception myException) {

            LOG.error("Caught exception accessing missing top view list for DataViews.\n" + myException.getMessage());
        }
    }
*/
    private static void createTableView(Connection metaConnectionIn, ResultSet resultsIn)  {

        InstalledTable myInstalledTable = null;
        String myId = null;

        try {

            myId = resultsIn.getString(1);

            if (null != myId) {

                myInstalledTable = CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class, myId);

                if (null != myInstalledTable) {

                    DataCacheBuilder.createViewForInstalledTable(myInstalledTable);
                    LOG.info("Successfully created top view for " + Format.value(myInstalledTable));
                    String myQueryString = "DELETE FROM \"missingtableviews\" WHERE \"id\" = '" + myId + "'";
                    QueryHelper.executeSQL(metaConnectionIn, myQueryString, null);
                    metaConnectionIn.commit();
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught exception creating view for "
                    + Format.value(myInstalledTable) + " (id:" + Format.value(myId) + ")");
        }
    }

    private static void createTopView(Connection metaConnectionIn, CsiConnection cacheConnectionIn, ResultSet resultsIn) {

        DataView myDataView = null;
        String myId = null;

        try {

            myId = resultsIn.getString(1);

            if (null != myId) {

                myDataView = CsiPersistenceManager.findObjectAvoidingSecurity(DataView.class, myId);

                if (null != myDataView) {

                    DataCacheBuilder.createTopView(cacheConnectionIn, myDataView);
                    myDataView.setNeedsRefresh(false);
                    CsiPersistenceManager.merge(myDataView);
                    LOG.info("Successfully created top view for " + Format.value(myDataView));
                    String myQueryString = "DELETE FROM \"missingtopviews\" WHERE \"id\" = '" + myId + "'";
                    QueryHelper.executeSQL(metaConnectionIn, myQueryString, null);
                    metaConnectionIn.commit();
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught exception creating top view for "
                    + Format.value(myDataView) + " (id:" + myId + ")");
        }
    }
}
