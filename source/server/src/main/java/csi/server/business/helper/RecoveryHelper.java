package csi.server.business.helper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.cachedb.CacheDataConsumer;
import csi.server.business.cachedb.CacheDataProducer;
import csi.server.common.dto.DataPackage;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.util.Format;
import csi.server.common.util.ParameterHelper;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;

/**
 * Created by centrifuge on 10/4/2018.
 */
public class RecoveryHelper {
   private static final Logger LOG = LogManager.getLogger(DataCacheHelper.class);

    // TODO: Look over carefully for proper handling of installed tables

   public static DataPackage buildRecoveryPackage(DataView dataViewIn) throws SQLException, CentrifugeException {
      DataPackage myFullPackage = null;

      try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
         DataPackage myDataPackage = DataPackage.extractDataPackage(dataViewIn);
         String myPrefix = myDataPackage.getPrefix();
         String[] myTables = myDataPackage.getTables(0);
         String[] myLinkups = myDataPackage.getLinkups(0);
         String[] myViews = myDataPackage.getViews(0);

         protectCacheItems(myConnection, myTables, myLinkups, myViews, myPrefix);

         myTables = getRelatedTables(myConnection, dataViewIn);
         myLinkups = getRelatedLinkups(myConnection, dataViewIn);
         myViews = getRelatedViews(myConnection, dataViewIn);

         protectCacheItems(myConnection, myTables, myLinkups, myViews, myPrefix);

         myDataPackage.setTables(1, myTables);
         myDataPackage.setLinkups(1, myLinkups);
         myDataPackage.setViews(1, myViews);

         myFullPackage = myDataPackage;
      }
      return myFullPackage;
   }

   public static void recoverDataPackage(DataPackage dataPackageIn) throws SQLException, CentrifugeException {
      if (dataPackageIn != null) {
         try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
            String myPrefix = dataPackageIn.getPrefix();
            String[] myInstalledTableInfo = dataPackageIn.getInstalledTables();

            for (int i = 1; myInstalledTableInfo.length > i; i += 2) {
               QueryHelper.executeSQL(myConnection, myInstalledTableInfo[i], null);
            }
            for (int i = 0; i < 2; i++) {
               String[] myTables = dataPackageIn.getTables(i);
               String[] myLinkups = dataPackageIn.getLinkups(i);
               String[] myViews = dataPackageIn.getViews(i);

               restoreCacheItems(myConnection, myTables, myLinkups, myViews, myPrefix);
            }
         }
      }
   }

   public static void discardRecoveryPackage(DataPackage dataPackageIn) throws SQLException, CentrifugeException {
      if (dataPackageIn != null) {
         try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
            for (int i = 0; i < 2; i++) {
               String[] myTables = dataPackageIn.getTables(i);
               String[] myLinkups = dataPackageIn.getLinkups(i);
               String[] myViews = dataPackageIn.getViews(i);

               dropCacheItems(myConnection, myTables, myLinkups, myViews);
            }
         } catch (Exception IGNORE) {
         }
      }
   }

   public static long fixupDataView(DataView dataViewIn) {
      long rowCount = 0L;
      Connection conn = null;

      try {
         BigDecimal version = BigDecimal.valueOf(binaryVersion(dataViewIn.getVersion()));

         if (BigDecimal.valueOf(2.01).compareTo(version) == 0) {
            String oldTableName = dataViewIn.getTables();
            rowCount = -1L;

            LOG.info("Migrating DataView {} from {} to {}", () -> Format.value(dataViewIn.getName()),
                     () -> dataViewIn.getVersion(), () -> ReleaseInfo.version);

            if (oldTableName != null) {
               String databaseName = CsiPersistenceManager.getCacheDatabase();
               String newTableName = CacheUtil.getCacheTableName(dataViewIn.getUuid(), 0);
               conn = CsiPersistenceManager.getCacheConnection();
               String request;
               Map<String,CsiDataType> columnMap = DataCacheBuilder.getColumnMap(conn, oldTableName);

               for (String oldColumnName : columnMap.keySet()) {
                  if (oldColumnName != null) {
                     String uuid = CacheUtil.fromDbUuid(oldColumnName);
                     FieldListAccess model = dataViewIn.getMeta().getModelDef().getFieldListAccess();
                     FieldDef field = model.getFieldDefByUuid(uuid);

                     if (field != null) {
                        String newColumnName = CacheUtil.getColumnName(field);
                        request = "ALTER TABLE " + CacheUtil.quote(oldTableName) +
                                  " RENAME COLUMN " + CacheUtil.quote(oldColumnName) +
                                  " TO " + CacheUtil.quote(newColumnName);

                        QueryHelper.executeSQL(conn, request, null);
                        conn.commit();
                     }
                  }
               }
               request = "ALTER TABLE " + CacheUtil.quote(oldTableName) + " RENAME TO " + CacheUtil.quote(newTableName);

               QueryHelper.executeSQL(conn, request, null);
               conn.commit();

               DataCacheBuilder.createTopView(conn, dataViewIn);

               rowCount = QueryHelper.countRows(conn, databaseName, "public", CacheUtil.getCacheTableName(dataViewIn.getUuid()));
            }
         } else if (BigDecimal.valueOf(3.05).compareTo(version) == 0) {
            LOG.info("Migrating DataView {} from {} to {}", () -> Format.value(dataViewIn.getName()),
                     () -> dataViewIn.getVersion(), () -> ReleaseInfo.version);

            // Recreate the view for accessing the DataView
            DataCacheBuilder.applyChangesToCache(dataViewIn);
         }
         if ((3.050007 > version.doubleValue()) && (dataViewIn.getTables() == null)) {
            dataViewIn.setTables(CacheUtil.getCacheTableName(dataViewIn.getUuid(), 0));
         }
         if (3.060102 > version.doubleValue()) {
            DataViewDef meta = dataViewIn.getMeta();

            ParameterHelper.initializeParameterUse(meta.getParameterList(), meta.getDataTree(), meta.getFieldList());
         }
      } catch (Exception exception) {
         LOG.error("Unable to fix up data tables for DataView {}", () -> Format.value(dataViewIn));
      } finally {
         SqlUtil.quietCloseConnection(conn);
      }
      return rowCount;
   }

    public static long monitorTransfer(Connection connectionIn, CacheDataProducer producerIn,
                                       CacheDataConsumer consumerIn, boolean closeIn) throws Exception {

        long mySourceRowCount = 0L;
        long myTargetRowCount = 0L;

        try {

            while (consumerIn.isAlive()) {

                TaskHelper.checkForCancel();
                consumerIn.join(100);
            }

            mySourceRowCount = producerIn.getRowCount();
            myTargetRowCount = consumerIn.getRowCount();

            Exception producerError = producerIn.getException();
            Exception consumerError = consumerIn.getException();
            if (producerError != null) {

                if ((null != consumerError) && (producerError.getMessage().contains("Pipe closed"))) {

                    throw consumerError;
                }
                throw producerError;
            }
            if (consumerError != null) {

                throw consumerError;
            }
            if (mySourceRowCount > myTargetRowCount) {

                throw new Exception("Not all rows loaded.");
            }
            connectionIn.commit();

        } catch (TaskCancelledException myException) {

            producerIn.handleCancel(true);

            while (consumerIn.isAlive()) {

                try {

                    consumerIn.interrupt();
                    consumerIn.join(100);

                } catch (Exception myIgnore) {

                }
            }

            if (null != connectionIn) {

                try {

                    connectionIn.rollback();

                } catch(Exception myRollbackException) {

                }
            }
            throw myException;

        } catch (Exception myException) {

            if (null != connectionIn) {

                try {

                    connectionIn.rollback();

                } catch(Exception myRollbackException) {

                }
            }
            throw myException;

        } finally {

            if ((null != connectionIn) && closeIn) {

                try {

                    connectionIn.close();

                } catch(Exception myException) {

                    // IGNORE !!
                }
            }
        }
        return myTargetRowCount;
    }

    private static void protectCacheItems(Connection connectionIn, String[] tablesIn, String[] linkupsIn, String[] viewsIn, String prefixIn) {

        if (null != tablesIn) {

            for (int i = 0; tablesIn.length > i; i++) {

                String myTable = tablesIn[i];
                tablesIn[i] = prefixIn + myTable;

                try {

                    DataCacheBuilder.renameCacheTable(connectionIn, myTable, tablesIn[i]);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to rename cache table " + Format.value(myTable), myException);
                }
            }
        }

        if (null != linkupsIn) {

            for (int i = 0; linkupsIn.length > i; i++) {

                String myLinkup = linkupsIn[i];
                linkupsIn[i] = prefixIn + myLinkup;

                try {

                    DataCacheBuilder.renameCacheTable(connectionIn, myLinkup, linkupsIn[i]);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to rename linkup cache table " + Format.value(myLinkup), myException);
                }
            }
        }

        if (null != viewsIn) {

            for (int i = 0; viewsIn.length > i; i++) {

                String myView = viewsIn[i];
                viewsIn[i] = prefixIn + myView;

                try {

                    DataCacheBuilder.renameCacheView(connectionIn, myView, viewsIn[i]);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to rename cache view " + Format.value(myView), myException);
                }
            }
        }
    }

    private static void restoreCacheItems(Connection connectionIn, String[] tablesIn, String[] linkupsIn, String[] viewsIn, String prefixIn) {

        int myOffset = prefixIn.length();

        if (null != tablesIn) {

            for (int i = 0; tablesIn.length > i; i++) {

                String myTable = tablesIn[i];
                tablesIn[i] = myTable.substring(myOffset);

                try {

                    DataCacheBuilder.dropCacheTable(connectionIn, tablesIn[i]);
                    DataCacheBuilder.renameCacheTable(connectionIn, myTable, tablesIn[i]);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to rename cache table " + Format.value(myTable), myException);
                }
            }
        }

        if (null != linkupsIn) {

            for (int i = 0; linkupsIn.length > i; i++) {

                String myLinkup = linkupsIn[i];
                linkupsIn[i] = myLinkup.substring(myOffset);

                try {

                    DataCacheBuilder.dropCacheTable(connectionIn, linkupsIn[i]);
                    DataCacheBuilder.renameCacheTable(connectionIn, myLinkup, linkupsIn[i]);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to rename linkup cache table " + Format.value(myLinkup), myException);
                }
            }
        }

        if (null != viewsIn) {

            for (int i = 0; viewsIn.length > i; i++) {

                String myView = viewsIn[i];
                viewsIn[i] = myView.substring(myOffset);

                try {

                    DataCacheBuilder.dropCacheView(connectionIn, viewsIn[i]);
                    DataCacheBuilder.renameCacheView(connectionIn, myView, viewsIn[i]);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to rename cache view " + Format.value(myView), myException);
                }
            }
        }
    }

   public static void dropRelatedCacheItems(DataView dataviewIn) throws SQLException, CentrifugeException {
      String[] myTables = dataviewIn.clearTables();
      String[] myLinkups = dataviewIn.clearLinkups();
      String[] myViews = dataviewIn.clearViews();

      try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
         dropCacheItems(myConnection, myTables, myLinkups, myViews);

         myTables = getRelatedTables(myConnection, dataviewIn);
         myLinkups = getRelatedLinkups(myConnection, dataviewIn);
         myViews = getRelatedViews(myConnection, dataviewIn);

         dropCacheItems(myConnection, myTables, myLinkups, myViews);
      }
   }

   private static String[] getRelatedTables(Connection connectionIn, DataView dataviewIn) {
      List<String> myTables = new ArrayList<String>();
      String myPattern = CacheUtil.getCacheTablePattern(dataviewIn.getUuid());
      String myQuery = "select tablename from pg_tables where tablename like " + myPattern;

      try (Statement myStatement = connectionIn.createStatement();
           ResultSet myResults = QueryHelper.executeStatement(myStatement, myQuery)) {

         while (myResults.next()) {
            myTables.add(myResults.getString(1));
         }
      } catch (SQLException exception) {
         LOG.error("Problem identifying related cache tables for DataView " + Format.value(dataviewIn.getName()), exception);
      }
      return myTables.toArray(new String[0]);
   }

   private static String[] getRelatedLinkups(Connection connectionIn, DataView dataviewIn) {
      List<String> myTables = new ArrayList<String>();
      List<LinkupMapDef> myLinkupMaps = dataviewIn.getMeta().getLinkupDefinitions();
      String myPattern = CacheUtil.getCacheTablePattern(dataviewIn.getUuid());
      String myQuery = "select tablename from pg_tables where tablename like " + myPattern;

      for (LinkupMapDef myLinkup : myLinkupMaps) {
         try (Statement myStatement = connectionIn.createStatement();
              ResultSet myResults = QueryHelper.executeStatement(myStatement, myQuery)) {
            while (myResults.next()) {
               myTables.add(myResults.getString(1));
            }
         } catch (SQLException sqle) {
            LOG.error("Problem identifying related cache tables for Linkup " + Format.value(myLinkup.getLinkupName()), sqle);
         }
      }
      return myTables.toArray(new String[0]);
   }

    private static String[] getRelatedViews(Connection connectionIn, DataView dataviewIn) {

        return new String[0];
    }

    private static void dropCacheItems(Connection connectionIn, String[] tablesIn, String[] linkupsIn, String[] viewsIn) {

        if (null != tablesIn) {

            for (String myTable : tablesIn) {

                try {

                    DataCacheBuilder.dropCacheTable(connectionIn, myTable);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to delete cache table " + Format.value(myTable), myException);
                }
            }
        }

        if (null != linkupsIn) {

            for (String myLinkup : linkupsIn) {

                try {

                    DataCacheBuilder.dropCacheTable(connectionIn, myLinkup);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to delete linkup cache table " + Format.value(myLinkup), myException);
                }
            }
        }

        if (null != viewsIn) {

            for (String myView : viewsIn) {

                try {

                    DataCacheBuilder.dropCacheView(connectionIn, myView);

                } catch (Exception myException) {

                    SqlUtil.quietRollback(connectionIn);
                    LOG.error("Unable to delete cache view " + Format.value(myView), myException);
                }
            }
        }
    }

    private static double binaryVersion(String versionIn) {

        String[] myPortion = (null != versionIn) ? versionIn.trim().split("\\.") : new String[0];
        Double myValue = (0 < myPortion.length) ? Double.valueOf(myPortion[0]) : 0.0;

        for (int i = 1; myPortion.length > i; i++ ) {

            myValue = (myValue * 100.0) + Double.valueOf(myPortion[i]);
        }
        for (int i = 1; myPortion.length > i; i++ ) {

            myValue = myValue / 100.0;
        }
        return myValue;
    }
}
