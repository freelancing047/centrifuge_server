package csi.server.business.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.regex.Pattern;

import csi.config.ApplicationConfig;
import csi.config.Configuration;
import csi.config.FeatureToggleConfiguration;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.util.io.IOLib;

import csi.security.CsiSecurityManager;
import csi.server.business.cachedb.AbstractInlineProducer;
import csi.server.business.cachedb.PGCacheDataConsumer;
import csi.server.business.cachedb.PGCacheDataProducer;
import csi.server.business.cachedb.PGRawDataProducer;
import csi.server.business.cachedb.dataset.DataSetProcessor;
import csi.server.business.cachedb.dataset.DataSetUtil;
import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.cachedb.script.CsiScriptRunner;
import csi.server.business.cachedb.script.ecma.EcmaScriptRunner;
import csi.server.business.helper.field.FieldCycleDetector;
import csi.server.business.helper.linkup.ParameterSetFactory;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.interfaces.DataWrapper;
import csi.server.common.interfaces.SqlTokenValueCallback;
import csi.server.common.interfaces.TokenExecutionValue;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.LogicalQuery;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.SqlTokenTreeItem;
import csi.server.common.model.UUID;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.linkup.LooseMapping;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.AuthorizationObject;
import csi.server.common.util.ConnectorSupport;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.connector.jdbc.CacheConnectionFactory;
import csi.server.dao.CsiConnection;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;
import csi.server.util.sql.CacheCommands;
import csi.server.util.sql.CacheTokens;

/**
 * Created by centrifuge on 10/4/2018.
 */
public class DataCacheBuilder {
   private static final Logger LOG = LogManager.getLogger(DataCacheHelper.class);

   private static final Pattern SQL_FROM_PATTERN = Pattern.compile(" FROM ");
   private static final Pattern SQL_COLUMN_SEPARATOR_PATTERN = Pattern.compile(", ");
   private static final Pattern SQL_AS_PATTERN = Pattern.compile(" AS ");

   private static boolean _doDebug = LOG.isDebugEnabled();

    private static boolean unixPlatform;

    private static long processWaitTimeout = 30000; // milliseconds

    private static File tempDirectory = new File("./temp");


//    private static Platform platform = null;

    static {
/*
        platform = PlatformFactory.createNewPlatformInstance("PostgreSql");
        platform.setDelimitedIdentifierModeOn(true);
        PlatformInfo myPlatformInfo = platform.getPlatformInfo();
        myPlatformInfo.setMaxIdentifierLength(63);
        myPlatformInfo.setDefaultSize(Types.NUMERIC, 38);
        myPlatformInfo.setDefaultSize(Types.DECIMAL, 38);
        myPlatformInfo.setHasSize(java.sql.Types.VARCHAR, false);
        myPlatformInfo.addNativeTypeMapping(java.sql.Types.VARCHAR, "TEXT");
*/
        String myOS = System.getProperty("os.name");
        if (myOS == null) {
            unixPlatform = false;
        } else {
            unixPlatform = myOS.contains("nux") || myOS.contains("nix");
        }
    }


    public static void clearCache(DataView dataviewIn) throws CentrifugeException {

        try {

            List<LinkupMapDef> myLinkupList = dataviewIn.getMeta().getLinkupDefinitions();

            if (null != myLinkupList) {

                for (LinkupMapDef myLinkup : myLinkupList) {

                    myLinkup.resetUseCount();
                }
            }
            RecoveryHelper.dropRelatedCacheItems(dataviewIn);
            SecurityHelper.discardLinkupSecurity(dataviewIn);

        } catch (Exception myException) {

        }
    }

   public static long discardExcessRows(String tableIn, long limitIn, long countIn) {
      long result = countIn;

      try (CsiConnection myCacheConnection = CsiPersistenceManager.getCacheConnection()) {
         String myRequest = "DELETE FROM " + CacheUtil.quote(tableIn) + " WHERE "
                            + CacheUtil.quote(CacheTokens.CSI_ROW_ID) + " > " + Long.toString(limitIn);

         QueryHelper.executeSQL(myCacheConnection, myRequest, null);
         myCacheConnection.commit();
         result = limitIn;
      } catch (Exception myException) {
         LOG.error("Caught exception removing extra rows from limited data request.");
      }
      return result;
   }

    public static ValuePair<Long, Boolean> initializeCache(DataWrapper sourceIn, ConnectionDef connectionIn,
                                                           ParameterSetFactory parameterValuesIn, List<AuthDO> credentialsIn,
                                                           DataView targetIn, LinkupMapDef linkupIn, int generationIn)
            throws CentrifugeException, GeneralSecurityException {

        if (_doDebug) {
         LOG.debug("Initializing Cache");
      }

        boolean myLinkupActive = (null != linkupIn) && (null != targetIn);
        DataView mySpinOff = myLinkupActive && (0 == generationIn) ? targetIn : null;
        DataView myDataView = ((null != sourceIn) && (sourceIn instanceof DataView) && (null == linkupIn))
                ? (DataView) sourceIn : null;
        long myRowStart = (myLinkupActive) ? targetIn.getNextLinkupRowId() : 1L;
        CsiConnection myCacheConnection = null;
        DataDefinition myMeta = sourceIn.getDataDefinition();
        DataSetOp mySourceTree = myMeta.getDataTree();
        List<DataSourceDef> mySourceList = myMeta.getDataSources();
        Integer myRowLimit = ((null != myMeta) && (null != myMeta.getRowLimit())) ? myMeta.getRowLimit() : null;
        Integer rowLimit = Configuration.getInstance().getApplicationConfig().getDefaultRowLimit();
        if(Configuration.getInstance().getFeatureToggleConfig().isUseNewLogoutPage()) {
            Integer tempRowLimit = myRowLimit;
            myRowLimit = (tempRowLimit > rowLimit) ? rowLimit : tempRowLimit;
        }
        Integer myRequestLimit = (null != myRowLimit) ? myRowLimit + 1 : null;
        FieldListAccess myModel = myMeta.getFieldListAccess();
        DataDefinition myTargetMeta = (null != targetIn) ? targetIn.getDataDefinition() : null;
        FieldListAccess myTargetModel = (null != myTargetMeta) ? myTargetMeta.getFieldListAccess() : myModel;
        List<QueryParameterDef> myDataSetParameters = parameterValuesIn.getParameterList();
        Map<String, FieldDef> myFieldDefMapping = (myLinkupActive)
                ? generateTargetSourceMapping(myModel, linkupIn) : null;
        boolean myReturnAll = (linkupIn == null) || linkupIn.getReturnAll();
        InstalledTable myInstalledTable = null;
        String myTopView = null;
        long myRowCount = 0L;
        boolean myMoreData = false;
        String myTableLock = null;
        List<String> myTempLocks = new ArrayList<String>();
        List<String> myTempTables = new ArrayList<String>();
        List<String> myTempViews = new ArrayList<String>();
        List<ValuePair<FieldDef, FieldDef>> myFieldDefs = null;
        List<String> myCapcoColumnNames = null;
        List<String> myTagColumnNames = null;
        DataView myTargetDataView = (null != targetIn)
                ? targetIn
                : (sourceIn instanceof DataView) ? (DataView) sourceIn : null;

        identifySourceTypes(myMeta);
        identifySourceTypes(myTargetMeta);

//        DataSyncContext mySyncContext = buildDataSyncContext(sourceIn);

        try {

            if (myLinkupActive) {

                Map<String, FieldDef> myCapcoColumnMap = sourceIn.getCapcoColumnMap();
                Map<String, FieldDef> myTagColumnMap = sourceIn.getTagColumnMap();

                myFieldDefs = matchFieldDefs(DataSetUtil.getResultColumns(myTargetMeta.getDataTree(), null),
                        myTargetModel, myCapcoColumnMap, myTagColumnMap, myFieldDefMapping);
                myCapcoColumnNames = getColumnNames(myCapcoColumnMap);
                myTagColumnNames = getColumnNames(myTagColumnMap);

            } else {

                myFieldDefs = matchFieldDefs(DataSetUtil.getResultColumns(myMeta.getDataTree(), null), myModel);
                myCapcoColumnNames = sourceIn.getCapcoColumnNames();
                myTagColumnNames = sourceIn.getTagColumnNames();
            }
            DataSetProcessor myProcessor
                    = new DataSetProcessor().evaluateDataSet(mySourceTree, mySourceList, parameterValuesIn, myModel,
                    ((myLinkupActive) ? getSourceColumns(myFieldDefs) : null), myReturnAll);
            Map<String, String> myAliasMap = myProcessor.getAliasMap();
            List<LogicalQuery> myQueries = myProcessor.getLogicalQueries();
            List<Map<String, CsiDataType>> myDataTypeMaps = myProcessor.getDataTypeMaps();
            SqlTableDef mySourceTable = mySourceTree.getTableDef();
            boolean myPreCacheFlag = addPrecalculatedFieldDefs(myTargetModel, myFieldDefs);
            String myInstalledViewQuery = null;
            String myDatabaseName = CsiPersistenceManager.getCacheDatabase();
            LogicalQuery mySimpleQuery = ((null != myQueries) && (1 == myQueries.size())) ? myQueries.get(0) : null;
            ConnectionDef myConnectionDef = connectionIn;
            boolean mySimpleSource = false;
            String myTableName = getTableName(sourceIn, targetIn, generationIn);

            TaskHelper.checkForCancel();
            // Recognize the ability to optimize simple requests
            if ((null != mySimpleQuery) && (null == myConnectionDef)) {

                DataSourceDef myDataSource = (null != mySimpleQuery) ? mySimpleQuery.source : null;

                if (null != myDataSource) {

                    myConnectionDef = myDataSource.getConnection();
                    if (null != myConnectionDef) {

                        ConnectionFactory myFactory
                                = ConnectionFactoryManager.getInstance().getConnectionFactory(myConnectionDef);

                        if (null != myFactory) {

                            mySimpleSource = ConnectionFactoryManager.isAdvancedFactory(myFactory)
                                    && (!isCallProcWithUnion(myQueries.get(0).sqlText))
                                    && (!CacheCommands.INSTALLED_TABLE_KEY.equals(myConnectionDef.getType()));
                        }
                    }
                }
            }
            AuthorizationObject.updateConnectionCredentials(sourceIn,
                    AuthorizationObject.buildCredentialsMap(credentialsIn));
            myCacheConnection = CsiPersistenceManager.getCacheConnection();
            if (null != myDataView) {

                // Drop conflicting tables and views
                try {

                    deleteTopView(myCacheConnection, myDataView);
                    dropCacheTable(myCacheConnection, myTableName);
                    dropCacheView(myCacheConnection, myTableName);
                    SecurityHelper.discardLinkupSecurity(myDataView);

                } catch (Exception IGNORE) {
                }
                myDataView.setTopView(false);
            }
            if ((null != myDataView) && (1 == myQueries.size()) && (null != myConnectionDef)) {

                String myInstalledTableId = null;

                if ((!myPreCacheFlag) && (!myLinkupActive) && (null != mySourceTable)
                        && (!mySourceTable.getIsCustom()) && (!mySourceTable.hasFilters())
                        && CacheCommands.INSTALLED_TABLE_KEY.equals(myConnectionDef.getType())
                        && dataTypesMatch(myMeta)) {

                    try {

                        myInstalledTableId = mySourceTable.getReferenceId();
                        myInstalledTable = (null != myInstalledTableId)
                                ? CsiPersistenceManager.findObject(InstalledTable.class,
                                myInstalledTableId,
                                AclControlType.READ)
                                : null;
                        if (null != myInstalledTable) {

                            myTableLock = SharedDataSourceHelper.lockInstalledTable(myInstalledTable);
                            String myInstalledQuery = (null != myTableLock)
                                    ? myProcessor.createViewQuery(myInstalledTable, myTableLock)
                                    : null;
                            if (null != myRowLimit) {

                                String myCurrentTable = myInstalledTable.getActiveTableName();

                                if (QueryHelper.countRows(myCacheConnection, myDatabaseName, "public", myCurrentTable)
                                        > myRowLimit) {

                                    myInstalledQuery = null;
                                }
                            }
                            if (null != myInstalledQuery) {

                                StringBuilder myBuffer = new StringBuilder();

                                myBuffer.append("CREATE OR REPLACE VIEW ");
                                myBuffer.append(CacheUtil.quote(myTableName));
                                myBuffer.append(" AS ");
                                myBuffer.append(myInstalledQuery);
                                myInstalledViewQuery = myBuffer.toString();
                                if (_doDebug) {

                                    LOG.debug("\n\nCreate a view of the installed table using the following command . . ."
                                            + "\n[" + myInstalledViewQuery + "]\n");
                                }
                                QueryHelper.executeSQL(myCacheConnection, myInstalledViewQuery, null);
                                myCacheConnection.commit();
                                myDataView.addInstalledTable(myTableLock);
                                myDataView.addInstalledTable(myInstalledViewQuery);
                                myDataView.setTopView(true);
                                myTopView = myTableName;

                            } else {

                                if (null != myTableLock) {

                                    // Release lock on current installed table version
                                    SharedDataSourceHelper.releaseInstalledTable(myTableLock);
                                }
                                myInstalledTable = null;
                            }
                        }

                    } catch (Exception myException) {

                        LOG.error("Caught exception trying to link to Installed Table. Will attempt to load from it.", myException);
                        if (null != myTableLock) {

                            SharedDataSourceHelper.releaseInstalledTable(myTableLock);
                        }
                        myInstalledTable = null;
                    }
                }
            }
            if ((null == myInstalledTable) && !myQueries.isEmpty()) {

                createDataTable(myCacheConnection, myTableName, null, myFieldDefs, myRowStart);
                // Single data source
                if (mySimpleSource) {

                    String myString = (null != mySimpleQuery) ? mySimpleQuery.sqlText : null;

                    if (null != myString) {

                        myTopView = loadSourceData(myCacheConnection, sourceIn, myRequestLimit, myAliasMap,
                                myTableName, mySimpleQuery, myDataSetParameters,
                                myTargetMeta, linkupIn, myFieldDefs);
                    }
                    // Multiple data source or one that can't do appends and outer joins
                } else {

                    queryOriginalSources(myCacheConnection, sourceIn, myTempTables, myTempViews,
                            myTempLocks, myQueries, myDataTypeMaps, myDataSetParameters);

                    if (null != myCacheConnection) {

                        String myQuery = myProcessor.getLocalQuery().toString();
                        if (_doDebug) {

                            LOG.debug("And combine the data with the following query . . ."
                                    + "\n[" + Format.value(myQuery) + "]");
                        }
//                        if ((null != myTempViews) && !myTempViews.isEmpty()) {

                        CsiConnection myConnection = CsiPersistenceManager.getUserConnection();

                        myCacheConnection.commit();
                        grantWriteAccess(myCacheConnection, myTableName);
                        myTopView = combineQueryResults(myConnection, sourceIn, myTableName, myAliasMap,
                                myDataSetParameters, linkupIn, myFieldDefs, myQuery, myRequestLimit);
                        revokeWriteAccess(myCacheConnection, myTableName);
/*
                        } else {

                            myTopView = combineQueryResults(myCacheConnection, sourceIn, myTableName,
                                    myAliasMap, myDataTypeMap, myDataSetParameters,
                                    linkupIn, myFieldDefs, myQuery, myRowLimit);
                        }*/
                    }
                }
                LOG.debug("DataCacheHelper.initializeCache: loadPrecachedFields(myCacheConnection, targetIn, generationIn);");
                loadPrecachedFields(myCacheConnection, myTargetDataView, generationIn);
            }

            LOG.debug("DataCacheHelper.initializeCache: TaskHelper.checkForCancel();");
            TaskHelper.checkForCancel();
            myRowCount = DataCacheHelper.getRowCount(myTopView);
            if (sourceIn instanceof InstalledTable) {

                grantReadAccess(myCacheConnection, myTableName);
                SharedDataSourceHelper.registerInstalledTableVersion((InstalledTable) sourceIn, myTableName);
                createViewForInstalledTable((InstalledTable) sourceIn);

            } else {

                if ((null != myRowLimit) && (myRowLimit < myRowCount)) {

                    myRowCount = discardExcessRows(myTopView, myRowLimit, myRowCount);
                    myMoreData = true;
                }

                if (0 == generationIn) {
                    if (null != myDataView) {

                        myDataView.setNeedsRefresh(false);
                        // Create a top level view of the cache including dynamic fields
                        myTopView = createTopView(myCacheConnection, myDataView);

                        // Handle ACL security and banners for DataView
                        if (null != myInstalledTable) {

                            SecurityHelper.migrateSecurityInfo(myDataView, myInstalledTable);
                            myDataView.addView(myTableName);
                            CsiPersistenceManager.merge(myInstalledTable);
                            CsiPersistenceManager.merge(myDataView);

                        } else {

                            SecurityHelper.updateSecurityData(myCacheConnection, sourceIn, myTopView,
                                    null, myCapcoColumnNames, myTagColumnNames);
                        }

                    } else if (null != mySpinOff) {

                        // Create a top level view of the cache including dynamic fields
                        myTopView = createTopView(myCacheConnection, mySpinOff);

                        // Handle ACL security and banners for DataView
                        SecurityHelper.updateSecurityData(myCacheConnection, sourceIn, myTopView, targetIn,
                                myCapcoColumnNames, myTagColumnNames);
                    }

                } else {

                    // Handle ACL security and banner update for new Linkup
                    LOG.debug("DataCacheHelper.initializeCache: updateSecurityData(myCacheConnection,"
                            + " dataViewIn, myTopView, targetIn, myReverseMapping);");
                    SecurityHelper.updateSecurityData(myCacheConnection, sourceIn, myTopView, targetIn,
                            myCapcoColumnNames, myTagColumnNames);
                    SecurityHelper.recordLinkupSecurity(targetIn.getSecurityAccess(),
                            ((DataView) sourceIn).getSecurityAccess(),
                            myCapcoColumnNames, myTagColumnNames);
                }
            }


        } catch (TaskCancelledException myException) {

            if (null != myCacheConnection) {

                SqlUtil.quietRollback(myCacheConnection);
            }
            throw myException;

        } catch (GeneralSecurityException myException) {

            if (null != myCacheConnection) {

                SqlUtil.quietRollback(myCacheConnection);
            }
            throw myException;

        } catch (Exception myException) {


            String myError = "Failed to initialize DataView cache.\n" + Format.value(myException);


            if (null != myCacheConnection) {

                SqlUtil.quietRollback(myCacheConnection);
            }
            LOG.error(myError, myException);
            throw new CentrifugeException(myError, myException);

        } finally {

            if (!myTempLocks.isEmpty()) {

                releaseTempLocks(myTempLocks);
            }
            if (null != myCacheConnection) {

                if (!myTempTables.isEmpty()) {

                    // remove the temporary tables
                    dropTempTables(myCacheConnection, myTempTables);
                }
                if (!myTempViews.isEmpty()) {

                    // remove the temporary tables
                    dropTempViews(myCacheConnection, myTempViews);
                }

                SqlUtil.quietCloseConnection(myCacheConnection);
            }
//            notifyListenersEndSync(mySyncContext, myExceptionFlag);
        }
        return new ValuePair<Long, Boolean>(myRowCount, myMoreData);
    }

    /*
        This entry point is used for installing data files as database tables for use as data sources
     */
   public static long createTableFromUpload(InstalledTable tableIn, List<InstalledColumn> columnListIn,
                                            AbstractInlineProducer producerIn, CsiFileType fileTypeIn,
                                            CsiEncoding encodingIn, Integer delimiterIn,
                                            Integer quoteIn, Integer escapeIn, String nullIn)
         throws CentrifugeException {
      long myTableRowCount = 0L;
      String myTableAlias = tableIn.getNextActiveTableName();
      PGCacheDataConsumer myConsumer = null;
      CsiFileType myPseudoType = (CsiFileType.TEXT == fileTypeIn) ? CsiFileType.TEXT : CsiFileType.CSV;
      String myCopyCommand = buildCopyCommand(myTableAlias, columnListIn, myPseudoType,
                                              encodingIn, delimiterIn, quoteIn, escapeIn, nullIn);

      CsiConnection myConnection = CsiPersistenceManager.getCacheConnection();
      Connection conn = myConnection.getConnection();

      try {
         List<ValuePair<FieldDef, FieldDef>> myFieldDefs = tableIn.getPhysicalPairs();

         if (_doDebug) {
            LOG.debug("Load Installed Table data using:\n" + Format.value(myCopyCommand));
         }
         try (InputStream myInputStream = producerIn.getStreamHandle()) {
            myConsumer = new PGCacheDataConsumer(myInputStream, conn, myCopyCommand, true);

            // Create table and identity control
            createDataTable(myConnection, myTableAlias, CacheTokens.CSI_ORIGIN, myFieldDefs, 1L);
            myConsumer.usePipeDelimiter(false);
            myConsumer.start();
            producerIn.start();

            myTableRowCount = RecoveryHelper.monitorTransfer(myConnection, producerIn, myConsumer, true);

            grantReadAccess(myConnection, myTableAlias);
            SharedDataSourceHelper.registerInstalledTableVersion(tableIn, myTableAlias);
         }
      } catch (Exception myException) {
         throw new CentrifugeException("Failed creating table from upload:\n"
                                       + CacheUtil.replaceNames(Format.value(myException),
                                       CacheUtil.getTableNamePair(tableIn),
                                       CacheUtil.getColumnNameMap(columnListIn)));
      }
      return myTableRowCount;
   }

    public static void renameCacheView(Connection connectionIn, String oldNameIn, String newNameIn) {

        try {

            renameCacheView(connectionIn, oldNameIn, newNameIn, true);

        } catch(Exception myException) {

            LOG.error("Caught exception renaming cache view " + Format.value(oldNameIn), myException);
        }
    }

    public static void renameCacheTable(Connection connectionIn, String oldNameIn, String newNameIn) {

        try {

            renameCacheTable(connectionIn, oldNameIn, newNameIn, true);

        } catch(Exception myException) {

            LOG.error("Caught exception renaming cache table " + Format.value(oldNameIn), myException);
        }
    }

   public static void renameCacheTable(Connection connectionIn, String oldNameIn, String newNameIn, boolean commitIn) throws Exception {
      String myIdentityTable = CacheUtil.getGeneratorNameForCacheTable(newNameIn);
      String sql = String.format(CacheCommands.RENAME_CACHE_TABLE, CacheUtil.quote(oldNameIn), CacheUtil.quote(newNameIn));
      String sql2 =
         DataCacheHelper.tableExists(connectionIn, myIdentityTable)
            ? String.format(CacheCommands.RENAME_CACHE_IDENTITY, CacheUtil.quote(CacheUtil.getGeneratorNameForCacheTable(oldNameIn)), CacheUtil.quote(myIdentityTable))
            : null;

      try (Statement stmt = connectionIn.createStatement()) {
         stmt.addBatch(sql);

         if (sql2 != null) {
            stmt.addBatch(sql2);
         }
         stmt.executeBatch();

         if (commitIn) {
            connectionIn.commit();
         }
      } catch(Exception myException) {
         SqlUtil.quietRollback(connectionIn);
         throw myException;
      }
   }

   public static String discardLinkupData(DataView dataViewIn) {
      String myTopView = null;

      try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
         String[] myLinkupTables = dataViewIn.clearLinkups();
         List<String> myCapcoColumnNames = dataViewIn.getCapcoColumnNames();
         List<String> myTagColumnNames = dataViewIn.getTagColumnNames();

         deleteTopView(myConnection, dataViewIn);

         for (int i = 0; i < myLinkupTables.length; i++) {
            dropCacheTable(myConnection, myLinkupTables[i]);
         }
         myTopView = createTopView(myConnection, dataViewIn);

         SecurityHelper.discardLinkupSecurity(dataViewIn);
         SecurityHelper.updateSecurityData(myConnection, dataViewIn, myTopView, null,
                                           myCapcoColumnNames, myTagColumnNames);
      } catch (Exception myException) {
         LOG.error("Caught exception trying to drop linkup tables.", myException);
      }
      return myTopView;
   }

// TODO: CASTING AND COLUMN ORDER

   public static long initializeSpinOffCache(DataView oldDataViewIn, List<FieldDef> fieldListIn,
                                             DataWrapper newResourceIn, Set<Integer> selectionIdsIn)
         throws CentrifugeException {
      long myRowCount = -1;
      String myNewTableName = null;
      String myBaseLoadString = "";

      try (CsiConnection myConnection = CsiPersistenceManager.getCacheConnection()) {
         try {
            InstalledTable myTable = (newResourceIn instanceof InstalledTable) ? (InstalledTable)newResourceIn : null;

            identifySourceTypes(oldDataViewIn.getDataDefinition());
            identifySourceTypes(newResourceIn.getDataDefinition());

            String myInterimView = createColumnLevelView(myConnection, oldDataViewIn, fieldListIn);

            TaskHelper.checkForCancel();

            List<ValuePair<FieldDef, FieldDef>> myFieldDefs = newResourceIn.getPhysicalPairs();
            myNewTableName = getTableName(newResourceIn, null, 0);

            // Create table and identity control
            createDataTable(myConnection, myNewTableName, CacheTokens.CSI_ORIGIN, myFieldDefs, 1L);
            // construct and execute insert into statement to copy rows
            // from interim view to spinoff table
            myBaseLoadString = buildSpinoffInsertRequest(myNewTableName, myFieldDefs, myInterimView);
            myRowCount = loadIdListData(myConnection, myBaseLoadString, selectionIdsIn);

            if (myTable != null) {
               grantReadAccess(myConnection, myNewTableName);
               SharedDataSourceHelper.registerInstalledTableVersion(myTable, myNewTableName);
            }
         } catch (Exception myException) {
            SqlUtil.quietRollback(myConnection);

            if (myNewTableName != null) {
               dropCacheTable(myConnection, myNewTableName);
            }
            throw new CentrifugeException("Failed to initialize spinoff cache using "
                    + ((null != selectionIdsIn) ? Integer.toString(selectionIdsIn.size()) : "<null>")
                    + " ids and the base command "
                    + ((null != myBaseLoadString) ? "\n" + myBaseLoadString : "<null>"), myException);
        }
      } catch (Exception exception) {
         throw new CentrifugeException("Failed to initialize spinoff cache using "
                  + ((null != selectionIdsIn) ? Integer.toString(selectionIdsIn.size()) : "<null>")
                  + " ids and the base command "
                  + ((null != myBaseLoadString) ? "\n" + myBaseLoadString : "<null>"), exception);
      }
      return myRowCount;
   }

   public static long createCaptureCache(InstalledTable tableIn, String dataViewIdIn, Set<Integer> selectionIdsIn,
                                         List<ValuePair<InstalledColumn, FieldDef>> pairedListIn)
         throws CentrifugeException {
      long myRowCount = -1;

      try (CsiConnection myConnection = CsiPersistenceManager.getCacheConnection()) {
         try {
            String mySourceViewName = CacheUtil.getCacheTableName(dataViewIdIn);
            String myNewTableName = tableIn.getNextActiveTableName();

            TaskHelper.checkForCancel();

            String myBaseLoadString = buildCaptureInsertRequest(myNewTableName, mySourceViewName, pairedListIn);
            List<ValuePair<FieldDef, FieldDef>> myFieldDefs = tableIn.getPhysicalPairs();

            // Create table and identity control
            createDataTable(myConnection, myNewTableName, CacheTokens.CSI_ORIGIN, myFieldDefs, 1L);
            // construct and execute insert into statement to copy rows
            // from interim view to spinoff table

            myRowCount = loadIdListData(myConnection, myBaseLoadString, selectionIdsIn);

            grantReadAccess(myConnection, myNewTableName);
            SharedDataSourceHelper.registerInstalledTableVersion(tableIn, myNewTableName);
         } catch (Exception exception) {
            SqlUtil.quietRollback(myConnection);
            throw new CentrifugeException("Failed to initialize spinoff cache", exception);
         }
      } catch (Exception exception) {
         throw new CentrifugeException("Failed to initialize spinoff cache", exception);
      }
      return myRowCount;
   }

   // Called when updating field list with precalculated fields
   public static void applyChangesToCache(DataView dataViewIn, Map<String,CsiDataType> oldPreCalculatedMapIn,
                                          Map<String,CsiDataType> newPreCalculatedMapIn, Map<String,CsiDataType> retypeMapIn)
         throws Exception {
      try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
         deleteTopView(myConnection, dataViewIn);

         try {
            if (dataViewIn.isTopView()) {
               convertBaseViewToBaseTable(myConnection, dataViewIn);
            }
            restructureCacheTable(myConnection, dataViewIn, oldPreCalculatedMapIn, newPreCalculatedMapIn, retypeMapIn);
            myConnection.commit();
            identifySourceTypes(dataViewIn.getMeta());
         } finally {
            createTopView(myConnection, dataViewIn);
         }
      }
   }

   // Called when executing creating, deleting, or modifying a linkup definition
   // or when updating field list without precalculated fields
   public static void applyChangesToCache(DataView dataViewIn) throws CentrifugeException, SQLException {
      try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
         deleteTopView(myConnection, dataViewIn);
         createTopView(myConnection, dataViewIn);
      } catch (Exception exception) {
         LOG.error(exception);
      }
   }

    public static void loadPrecachedFields(Connection connectionIn, DataView dataViewIn, int generationIn)
            throws CentrifugeException, SQLException {

        DataViewDef myMeta = (null != dataViewIn) ? dataViewIn.getMeta() : null;
        FieldListAccess myModel = (null != myMeta) ? myMeta.refreshFieldListAccess() : null;

        if ((null != myModel) && myModel.hasPrecalculatedFields()) {

            String myDataViewId = dataViewIn.getUuid();
            String myTableName = CacheUtil.getCacheTableName(myDataViewId, generationIn);
            String myViewName = CacheUtil.getColumnViewName(myDataViewId, generationIn);
            List<ValuePair<String, String>> myViews = new ArrayList<ValuePair<String, String>>();
            List<FieldDef> myOrderedFieldList = (null != myModel) ? myModel.getOrderedFieldDefList() : null;

            myViews.add(new ValuePair<String, String>(myTableName, myViewName));
            createPartialView(connectionIn, myViewName, myTableName, myOrderedFieldList);
            myModel.setDirtyFlags();
            loadScriptedColumnData(connectionIn, myMeta, myViews);
            loadDerivedColumnData(connectionIn, myMeta, myViews);
            myModel.clearDirtyFlags();
            dropCacheView(connectionIn, myViewName);
        }
    }

   private static String loadSourceData(CsiConnection cacheConnectionIn, DataWrapper wrapperIn, Integer rowLimitIn,
                                         Map<String,String> aliasMapIn, String tableNameIn, LogicalQuery queryIn,
                                         List<QueryParameterDef> dataSetParametersIn, DataDefinition metaIn, LinkupMapDef linkupIn,
                                         List<ValuePair<FieldDef,FieldDef>> fieldDefListIn)
          throws CentrifugeException, GeneralSecurityException, SQLException {
      DataSourceDef myDataSource = queryIn.source;
      ConnectionDef myConnectionDef = myDataSource.getConnection();
      ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(myConnectionDef);

      try (Connection mySourceConnection = myFactory.getConnection(myConnectionDef)) {
         if (_doDebug) {
            LOG.debug("Executing external query operation: " + queryIn.baseOp.getName());
         }
         TaskHelper.checkForCancel();

         if ((queryIn != null) && (queryIn.preSql != null)) {  //TODO: queryIn cannot be null here
            QueryHelper.executeSQL(mySourceConnection, queryIn.preSql, dataSetParametersIn);
         }
         String myQuerySql = queryIn.sqlText;

         if (_doDebug) {
            LOG.debug("Load source data using query:\n" + myQuerySql + "\n");
         }
         try (ResultSet myResults = QueryHelper.executeSingleQuery(mySourceConnection, myQuerySql, rowLimitIn, dataSetParametersIn)) {
            List<ValuePair<FieldDef,Integer>> myCacheFields =
               prepareToLoadCache(wrapperIn, aliasMapIn, buildColumnIndexMap(myResults), fieldDefListIn);

            if (_doDebug) {
               LOG.debug("Redirection Map -----------------------------------------------");

               for (ValuePair<FieldDef,Integer> myPair : myCacheFields) {
                  FieldDef myField = myPair.getValue1();
                  Integer myIndex = myPair.getValue2();

                  if ((null != myField) && (0 <= myIndex)) {
                     LOG.debug("\t" + Format.value(myField.getFieldName() + " :: " + CacheUtil.getColumnName(myField)
                               + " :: " + myField.tryStorageType().getSqlType() + " :: " + Integer.toString(myIndex)));
                  }
               }
               LOG.debug("---------------------------------------------------------------");
            }
            if ((null == linkupIn) && (wrapperIn instanceof DataView)) {
               ((DataView) wrapperIn).addTable(tableNameIn);
            }
            if (null != myCacheFields) {
               TaskHelper.checkForCancel();

               try {
                  bulkPopulateCache(cacheConnectionIn, myResults, tableNameIn, myCacheFields, rowLimitIn);
                  cacheConnectionIn.commit();
               } catch (Exception myException) {
                  throw new CentrifugeException("initializeCache:loadSourceData:Error loading data: " + Format.value(myException));
               }
            } else {
               throw new CentrifugeException("This dataview's structure is corrupted and it cannot be opened.");
            }
         }
         if ((queryIn != null) && (queryIn.postSql != null)) {  //TODO: queryIn cannot be null here
            QueryHelper.executeSQL(mySourceConnection, queryIn.postSql, dataSetParametersIn);
         }
      } catch (TaskCancelledException myException) {
         throw myException;
      } catch (Exception e) {
         ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

         if ((null != mySupport) && mySupport.isRestricted(myConnectionDef.getType())) {
            throw new CentrifugeException("Query execution error.");
         }
         throw e;
      }
      return tableNameIn;
   }

   private static void queryOriginalSources(CsiConnection cacheConnectionIn, DataWrapper wrapperIn,
                                            List<String> tableListIn, List<String> viewListIn,
                                            List<String> lockListIn, List<LogicalQuery> queryListIn,
                                            List<Map<String, CsiDataType>> dataTypeMapsIn,
                                            List<QueryParameterDef> dataSetParametersIn)
         throws CentrifugeException, GeneralSecurityException, SQLException {
      int howMany = queryListIn.size();

      for (int i = 0; i < howMany; i++) {
         LogicalQuery query = queryListIn.get(i);

         if (query != null) {
            Map<String, CsiDataType> myDataTypeMap = dataTypeMapsIn.get(i);
            DataView myDataView = (wrapperIn instanceof DataView) ? (DataView) wrapperIn : null;
            DataSourceDef myDataSource = query.source;
            ConnectionDef myConnectionDef = (myDataSource != null) ? myDataSource.getConnection() : null;
            String myTableName = (myDataSource != null) ? CacheUtil.toDbUuid(query.baseOp.getUuid()) : null;

            if (myTableName != null) {
               if (((myDataSource != null) && myDataSource.isInPlace()) ||
                   ((myConnectionDef != null) && CacheCommands.INSTALLED_TABLE_KEY.equals(myConnectionDef.getType()))) {
                  // NEED TO LOCK INSTALLED TABLE AND RELEASE IN initializeCache
                  if (query.referenceId != null) {
                     InstalledTable myInstalledTable =
                        CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class, query.referenceId);

                     lockListIn.add(SharedDataSourceHelper.lockInstalledTable(myInstalledTable));
                  }
                  String myRequest = "CREATE OR REPLACE VIEW " + CacheUtil.quote(myTableName) + " AS " + query.sqlText;

                  QueryHelper.executeSQL(cacheConnectionIn, myRequest, null);
                  viewListIn.add(myTableName);
                  cacheConnectionIn.commit();
                  grantReadAccess(cacheConnectionIn, myTableName);
//                getColumnMap(cacheConnectionIn, myTableName, dataTypeMapIn);
               } else {
                  ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(myConnectionDef);
                  TaskHelper.checkForCancel();

                  try (Connection mySourceConnection = myFactory.getConnection(myConnectionDef)) {
                     if (_doDebug) {
                        LOG.debug("Executing external query operation: " + query.baseOp.getName());
                     }
                     if (query.preSql != null) {
                        QueryHelper.executeSQL(mySourceConnection, query.preSql, dataSetParametersIn);
                     }
                     createAndLoadCacheTable(mySourceConnection, cacheConnectionIn, query, myFactory,
                                             dataSetParametersIn, myTableName, myDataTypeMap, query.rowLimit);

                     if ((myDataView != null) && (myDataView.isTopView())) {
                        myDataView.addTable(myTableName);
                     } else {
                        tableListIn.add(myTableName);
                     }
                     if (query.postSql != null) {
                        QueryHelper.executeSQL(mySourceConnection, query.postSql, dataSetParametersIn);
                        mySourceConnection.commit();
                     }
                     cacheConnectionIn.commit();
                     grantReadAccess(cacheConnectionIn, myTableName);
                  } catch (TaskCancelledException myException) {
                     throw myException;
                  }
               }
            }
         }
      }
   }

    private static String combineQueryResults(CsiConnection cacheConnectionIn, DataWrapper wrapperIn, String tableNameIn,
                                       Map<String, String> aliasMapIn, List<QueryParameterDef> dataSetParametersIn,
                                       LinkupMapDef linkupIn, List<ValuePair<FieldDef, FieldDef>> fieldDefListIn,
                                       String queryIn, Integer rowLimitIn)
            throws Exception {

        // TODO: NEED TO RELEASE INSTALLED TABLE FROM queryOriginalSources

        // TODO: NEED TO RELEASE INSTALLED TABLE FROM queryOriginalSources

        // TODO: NEED TO RELEASE INSTALLED TABLE FROM queryOriginalSources

        DataDefinition myDataDef = (null != wrapperIn) ? wrapperIn.getDataDefinition() : null;
        String myLoadingSql = createLoadingQuery(myDataDef, tableNameIn, queryIn, rowLimitIn, fieldDefListIn, aliasMapIn);

        if ((null == linkupIn) && (wrapperIn instanceof DataView)) {

            ((DataView)wrapperIn).addTable(tableNameIn);
        }
        TaskHelper.checkForCancel();
        if (_doDebug) {
         LOG.debug("Combine data from all sources into final table using command\n" + myLoadingSql + "\n");
      }
        QueryHelper.executeSqlUpdate(cacheConnectionIn, myLoadingSql, dataSetParametersIn);
        cacheConnectionIn.commit();

        return tableNameIn;
    }

    //  COPY %s (%s) FROM STDIN WITH DELIMITER AS '|' CSV
    private static String buildCopyCommand(String tableNameIn, List<InstalledColumn> columnListIn,
                                           CsiFileType fileTypeIn, CsiEncoding encodingIn,
                                           Integer delimiterIn, Integer quoteIn,
                                           Integer escapeIn, String nullIn) {

        StringBuilder myBuffer = new StringBuilder();
        int howMany = columnListIn.size();

        myBuffer.append("COPY ");
        myBuffer.append(CacheUtil.quote(tableNameIn));
        myBuffer.append(" (");

        for (int i = 0; i < howMany; i++) {
            if (i > 0) {
                myBuffer.append(", ");
            }
            myBuffer.append(CacheUtil.quote(columnListIn.get(i).getColumnName()));
        }
        myBuffer.append(") FROM STDIN WITH");

        if ((null != encodingIn) && (CsiEncoding.UTF_16BE != encodingIn)
                && (CsiEncoding.UTF_16LE != encodingIn)) {

            myBuffer.append(" ENCODING ");
            CacheUtil.singleQuote(myBuffer, encodingIn.getLabel());
        }

        if (null != delimiterIn) {

            myBuffer.append(" DELIMITER AS ");
            CacheUtil.singleQuote(myBuffer, (char)(int)delimiterIn);
        }

        if (null != nullIn) {

            myBuffer.append(" NULL AS ");
            CacheUtil.singleQuote(myBuffer, nullIn);
        }

        if (null != quoteIn) {

            myBuffer.append(" QUOTE AS ");
            CacheUtil.singleQuote(myBuffer, (char)(int)quoteIn);
        }

        if (null != escapeIn) {

            myBuffer.append(" ESCAPE AS ");
            CacheUtil.singleQuote(myBuffer, (char)(int)escapeIn);
        }

        if (CsiFileType.CSV == fileTypeIn) {

            myBuffer.append(" CSV");
        }

        return myBuffer.toString();
    }

    private static String createLoadingQuery(DataDefinition dataDefinitionIn, String tableNameIn, String queryIn,
                                             Integer rowLimitIn, List<ValuePair<FieldDef, FieldDef>> fieldListIn,
                                             Map<String, String> aliasMapIn) {

        StringBuilder myBuffer = new StringBuilder();
        CacheConnectionFactory myFactory = new CacheConnectionFactory();
        String myPrefix = " (";

        myBuffer.append("INSERT INTO ");
        myBuffer.append(CacheUtil.quote(tableNameIn));

        if (!fieldListIn.isEmpty()) {
            for (ValuePair<FieldDef, FieldDef> myPair : fieldListIn) {

                FieldDef mySourceField = myPair.getValue2();

                // TODO: BROKEN ???
                if (null != mySourceField) {

                    FieldDef myTargetField = myPair.getValue1();

                    myBuffer.append(myPrefix);
                    myBuffer.append(CacheUtil.getQuotedColumnName(myTargetField));
                }
                myPrefix = ", ";
            }
            myPrefix = (") SELECT ");

            for (ValuePair<FieldDef, FieldDef> myPair : fieldListIn) {

                FieldDef mySourceField = myPair.getValue2();

                if (null != mySourceField) {

                    CsiDataType myCast = null;
                    FieldDef myTargetField = myPair.getValue1();
                    String mySourceColumnId = mySourceField.getColumnLocalId();
                    String myColumnName = ((null != aliasMapIn) && (null != mySourceColumnId))
                            ? aliasMapIn.get(mySourceColumnId)
                            : null;

                    if (null != myColumnName) {

                        if (null != myTargetField) {

                            String myKey = mySourceField.getColumnKey();
                            ColumnDef mySourceColumn = ((null != dataDefinitionIn) && (null != myKey))
                                    ? dataDefinitionIn.getColumnByKey(myKey) : null;
                            CsiDataType mySourceType = (null != mySourceColumn) ? mySourceColumn.getCsiType() : null;
                            CsiDataType myTargetType = myTargetField.tryStorageType();

                            myCast = ((myTargetType == null) || (myTargetType == mySourceType)) ? null : myTargetType;
                        }
                        myBuffer.append(myPrefix);
                        if (null != myCast) {

                            myBuffer.append(myFactory.castExpression("\"q1\"." + CacheUtil.quote(myColumnName), myCast));

                        } else {

                            myBuffer.append("\"q1\".");
                            myBuffer.append(CacheUtil.quote(myColumnName));
                        }
                        myPrefix = ", ";
                    }
                }
            }

        } else {

            myBuffer.append(" SELECT *");
        }
        myBuffer.append(" FROM (");
        myBuffer.append(queryIn);
        myBuffer.append(") \"q1\"");
        if (null != rowLimitIn) {

            myBuffer.append(" LIMIT ");
            myBuffer.append(rowLimitIn.toString());
        }

        return myBuffer.toString();
    }

    private static long loadIdListData(Connection connectionIn, String baseCommandIn, Set<Integer> idsIn)
            throws SQLException, CentrifugeException {

        long myRowCount = 0L;
        int myIdCount = (idsIn == null) ? 0 : idsIn.size();

        try {
            StringBuilder myBuffer = new StringBuilder();
            int myIdNum = 0;
            StopWatch stopWatch = new StopWatch();

            stopWatch.start();

            try (Statement statement = connectionIn.createStatement()) {
               myBuffer.append(baseCommandIn);

               if (myIdCount > 0) {
                  myBuffer.append(" WHERE ");
                  myBuffer.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));
                  myBuffer.append(" IN (");

                  for (Integer myId : idsIn) {
                     myIdNum++;
                     myBuffer.append(myId.toString());

                     if ((myIdCount > myIdNum) && (QueryHelper.SQL_MAX_LENGTH > myBuffer.length())) {
                        myBuffer.append(",");
                     } else {
                        myBuffer.append(")");

                        if (_doDebug) {
                           LOG.debug("Load Data using command:\n" + myBuffer.toString() + "\n");
                        }
                        myRowCount += statement.executeUpdate(myBuffer.toString());

                        if (myIdCount > myIdNum) {
                           myBuffer.setLength(0);
                           myBuffer.append(baseCommandIn);
                           myBuffer.append(" WHERE ");
                           myBuffer.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));
                           myBuffer.append(" IN (");
                        } else {
                           break;
                        }
                     }
                  }
               }
            }
            stopWatch.stop();

            if (myRowCount > 0) {
               connectionIn.commit();

               if (_doDebug) {
                  LOG.debug("time to spinoff cache data: " + stopWatch.getTime());
               }
            } else {
                throw new CentrifugeException("No data was captured from the DataView.");
            }
        } catch (Exception myException) {
            SqlUtil.quietRollback(connectionIn);
            throw myException;
        }
        return myRowCount;
    }

   private static String buildSpinoffInsertRequest(String tableNameIn, List<ValuePair<FieldDef, FieldDef>> fieldListIn, String interimViewIn) {
      StringBuilder myBuffer = new StringBuilder();
      boolean first = true;

      myBuffer.append("INSERT INTO ");
      myBuffer.append(CacheUtil.quote(tableNameIn));
      myBuffer.append( "(");
      myBuffer.append(CacheTokens.CSI_ORIGIN);
      myBuffer.append(", ");

      for (ValuePair<FieldDef, FieldDef> myPair : fieldListIn) {
         FieldDef myField = myPair.getValue1();

         if (first) {
            first = false;
         } else {
            myBuffer.append(", ");
         }
         myBuffer.append(CacheUtil.getQuotedColumnName(myField));
      }
      myBuffer.append(") SELECT ");
      myBuffer.append(CacheTokens.CSI_GROUP_ID);
      myBuffer.append(", ");
      first = true;

      for (ValuePair<FieldDef, FieldDef> myPair : fieldListIn) {
         FieldDef myField = myPair.getValue1();

         if (first) {
            first = false;
         } else {
            myBuffer.append(", ");
         }
         myBuffer.append(CacheUtil.getQuotedColumnName(myField));
      }
      myBuffer.append(" FROM ");
      myBuffer.append(CacheUtil.quote(interimViewIn));
      return myBuffer.toString();
   }

    private static String buildCaptureInsertRequest(String tableNameIn, String sourceNameIn,
                                             List<ValuePair<InstalledColumn, FieldDef>> pairedListIn) {

        StringBuilder myBuffer = new StringBuilder();
        CacheConnectionFactory myFactory = new CacheConnectionFactory();

        myBuffer.append("INSERT INTO ");
        myBuffer.append(CacheUtil.quote(tableNameIn));
        myBuffer.append( "(");
        boolean first = true;

        for (ValuePair<InstalledColumn, FieldDef> myPair : pairedListIn) {
            InstalledColumn myColumn = myPair.getValue1();

            if (first) {
               first = false;
            } else {
               myBuffer.append(", ");
            }
            myBuffer.append(CacheUtil.quote(myColumn.getColumnName()));
        }
        myBuffer.append(") SELECT ");
        first = true;

        for (ValuePair<InstalledColumn, FieldDef> myPair : pairedListIn) {
            InstalledColumn myColumn = myPair.getValue1();
            FieldDef myField = myPair.getValue2();
            CsiDataType myTargetType = myColumn.getType();

            if (first) {
               first = false;
            } else {
               myBuffer.append(", ");
            }
            if (myField == null) {
               myBuffer.append(myFactory.castNull(myTargetType));
            } else {
                String myQuotedSourceColumn = CacheUtil.getQuotedColumnName(myField);

                if (myField.getValueType() == myTargetType) {

                    myBuffer.append(myQuotedSourceColumn);

                } else {

                    myBuffer.append(myFactory.castExpression(myQuotedSourceColumn, myTargetType));
                }
            }
        }
        myBuffer.append(" FROM ");
        myBuffer.append(CacheUtil.quote(sourceNameIn));

        return myBuffer.toString();
    }

    private static List<ValuePair<FieldDef, Integer>> prepareToLoadCache(DataWrapper wrapperIn,
                                                                         Map<String, String> aliasMapIn,
                                                                         Map<String, Integer> columnIndexMapIn,
                                                                         List<ValuePair<FieldDef, FieldDef>> fieldDefsIn)
            throws SQLException, CentrifugeException {

        boolean myErrorFlag = false;
        List<ValuePair<FieldDef, Integer>> myCacheFields = new ArrayList<ValuePair<FieldDef, Integer>>();

        if (_doDebug) {

            LOG.debug("           -- AliasMap -------------------------------------------------------");

            for (Map.Entry<String, String> myPair : aliasMapIn.entrySet()) {


                LOG.debug("             ---  " + myPair.getKey() + " :: " + myPair.getValue());
            }
            LOG.debug("           --------------------------------------------------------------------");
        }
        LOG.debug("           -- FieldMap -------------------------------------------------------");

        for (ValuePair<FieldDef, FieldDef> myPair: fieldDefsIn) {

            FieldDef myTarget = myPair.getValue1();
            FieldDef mySource = myPair.getValue2();

            // TODO: BROKEN ???
            if ((null != mySource) && ((FieldType.COLUMN_REF == mySource.getFieldType())
                    || (FieldType.LINKUP_REF == mySource.getFieldType()))) {

                String myColumnId = mySource.getColumnLocalId();
                String myColumnAlias = aliasMapIn.get(myColumnId);
                Integer myIndex = (myColumnAlias != null) ? columnIndexMapIn.get(myColumnAlias) : null;

                if (myColumnAlias == null) {
                    LOG.error("This dataview's structure is corrupted and it cannot be opened.");

                    if (_doDebug) {

                        myErrorFlag = true;
                        continue;

                    } else {

                        wrapperIn.setNeedsSource(true);
                        throw new CentrifugeException(
                                "This dataview's structure is corrupted and it cannot be opened.");
                    }
                }
                if (myIndex == null) {

                    myIndex = columnIndexMapIn.get(myColumnAlias.toLowerCase());
                }
                // if still not found, search index by fieldname (needed when SELECT * is the query
                if ((myIndex == null) && (mySource.getName() != null)) {

                    myIndex = columnIndexMapIn.get(mySource.getName());
                }
                if ((myIndex == null) && (mySource.mapKey() != null)) {

                    myIndex = columnIndexMapIn.get(mySource.mapKey());
                }
                if (null != myIndex) {

                    LOG.debug("             ---  " + myTarget + " :: " + Integer.toString(myIndex));
                    myCacheFields.add(new ValuePair<FieldDef, Integer>(myTarget, myIndex));
                }

            } else if (myTarget.getFieldType() == FieldType.DERIVED) {
                LOG.debug("             ---  " + myTarget + " :: " + Integer.toString(-1));
                myCacheFields.add(new ValuePair<FieldDef, Integer>(null, -1));
            } else if (myTarget.getFieldType() == FieldType.SCRIPTED) {
                LOG.debug("             ---  " + myTarget + " :: " + Integer.toString(-1));
                myCacheFields.add(new ValuePair<FieldDef, Integer>(null, -1));
            }
        }
        LOG.debug("           -------------------------------------------------------------------");
        return myErrorFlag ? null : myCacheFields;
    }

    private static long bulkPopulateCache(CsiConnection cacheConnectionIn, ResultSet resultsIn, String tableName,
                                          List<ValuePair<FieldDef, Integer>> fieldListIn, Integer rowLimitIn)
            throws CentrifugeException {

        String columnNames = CacheUtil.makeColumnNameStringFromFields(fieldListIn);

        if (LOG.isDebugEnabled()) {

            char myDelim = ':';
            StringBuilder myBuffer = new StringBuilder();
            for (ValuePair<FieldDef, Integer> myPair : fieldListIn) {

                if (null != myPair) {

                    FieldDef myField = myPair.getValue1();

                    if (null != myField) {

                        myBuffer.append(myDelim);
                        myDelim = ',';
                        myBuffer.append(myField.getName());
                    }
                }
            }
            LOG.debug("bulkPopulateCache.processFields" + myBuffer.toString());
            LOG.debug("bulkPopulateCache.columnNames:" + columnNames);
        }
        try {
            PipedInputStream reader = new PipedInputStream();
            PipedOutputStream writer = new PipedOutputStream(reader);

            PGCacheDataProducer producer = new PGCacheDataProducer(writer, resultsIn, fieldListIn, rowLimitIn);
            producer.setDaemon(true);

            PGCacheDataConsumer consumer = new PGCacheDataConsumer(reader, cacheConnectionIn.getConnection(),
                    tableName, columnNames, true);
            consumer.setDaemon(true);

            consumer.start();
            producer.start();

            return RecoveryHelper.monitorTransfer(cacheConnectionIn, producer, consumer, true);
        } catch (Exception myException) {
            throw new CentrifugeException("Failed bulk populating cache:\n" + Format.value(myException));
        }
    }

    private static long createAndLoadCacheTable(Connection sourceConnectionIn, CsiConnection cacheConnectionIn,
                                                LogicalQuery queryIn, ConnectionFactory factoryIn,
                                                List<QueryParameterDef> dataSetParametersIn, String tableNameIn,
                                                Map<String, CsiDataType> dataTypeMapIn, Integer rowLimitIn)
            throws CentrifugeException, SQLException {

        long myRowCount = 0L;
        /*String myDatabaseName = */CsiPersistenceManager.getCacheDatabase();
        DataSetOp mySourceOp = queryIn.parentOp;
        String myDataSource = (null != mySourceOp) ? mySourceOp.getName() : null;

        if (_doDebug) {
         LOG.debug("Load data for cache table " + Format.value(tableNameIn)
                   + " using query: " + queryIn.sqlText + " LIMIT 0");
      }

        if (ConnectionFactoryManager.isStreaming(factoryIn)) {

            File myLocalPipe = null;

         // Execute query interested in metadata
            try (ResultSet myResults = QueryHelper.executeSingleQuery(sourceConnectionIn, queryIn.sqlText + " LIMIT 0",
                                                                      dataSetParametersIn)) {
               // Create empty data view cache table
               String myColumnNames = createStagingTable(cacheConnectionIn, tableNameIn,
                       myResults.getMetaData(), dataTypeMapIn);

                myLocalPipe = createPipeFile();

                if (null == myLocalPipe) {
                    throw new CentrifugeException("Cannot create local pipe !");
                }
                String myPipeLocation = myLocalPipe.getCanonicalPath();

                // Execute the streaming query which starts producing data into pipe
                String myOptions = "REMOTESOURCE 'JDBC'";
                String myStreamQuery = String.format("CREATE EXTERNAL TABLE '%1$s' USING ( %2$s ) AS %3$s",
                        myPipeLocation, myOptions, queryIn.sqlText);
                ExternalDataExporter externalDataExporter = new ExternalDataExporter(sourceConnectionIn, myStreamQuery,
                        dataSetParametersIn);
                if (unixPlatform) {

                    // FIFO pipe based, exporter should be executed in paralel
                    new Thread(externalDataExporter).start();
                } else {

                    // Non FIFO pipe based, exporter should be executed in sequence
                    externalDataExporter.run();
                }

                if (_doDebug) {
                  LOG.debug("Create and load stream table: " + Format.value(tableNameIn));
               }

                // Transfer data from pipe to cache table
                myRowCount = loadFromPipe(cacheConnectionIn, tableNameIn, myColumnNames,
                        myResults, myPipeLocation, rowLimitIn);

            } catch (IOException e) {
                throw new CentrifugeException(e);
            } finally {
                if (null != myLocalPipe) {
                    myLocalPipe.delete();
                }
            }
        } else if (ConnectionFactoryManager.isAdvancedFactory(factoryIn) && !isCallProcWithUnion(queryIn.sqlText)) {
            try (ResultSet myResults = QueryHelper.executeSingleQuery(sourceConnectionIn, queryIn.sqlText,
                                                                      rowLimitIn, dataSetParametersIn)) {
               if (_doDebug) {
                  LOG.debug("Create and load advanced table: " + Format.value(tableNameIn));
               }

               String myColumnNames = createStagingTable(cacheConnectionIn, tableNameIn,
                     myResults.getMetaData(), dataTypeMapIn);
               myRowCount = loadRawTable(cacheConnectionIn, tableNameIn, myColumnNames, myResults,
                                         factoryIn, rowLimitIn, myDataSource);
            } catch (Exception e) {
            }
        } else {
            // hack for outlier connection types so they can handle unions
            String[] mySubqueries = CacheCommands.UNION_PATTERN.split(queryIn.sqlText);
            String myTable = null;
            String myColumnNames = null;

            for (String mySubquery : mySubqueries) {
               try (ResultSet myResults = QueryHelper.executeSingleQuery(sourceConnectionIn, mySubquery,
                                                                    rowLimitIn, dataSetParametersIn)) {
                  if (myTable == null) {
                     if (_doDebug) {
                        LOG.debug("Create and load table: " + Format.value(tableNameIn));
                     }
                     myTable = tableNameIn;
                     myColumnNames = createStagingTable(cacheConnectionIn, tableNameIn,
                                                        myResults.getMetaData(), dataTypeMapIn);
                  }
                  myRowCount = loadRawTable(cacheConnectionIn, tableNameIn, myColumnNames, myResults,
                                            factoryIn, rowLimitIn, myDataSource);
               } catch (Exception e) {
               }
            }
        }
        return myRowCount;
    }

    private static long loadRawTable(CsiConnection cacheConnectionIn, String tableNameIn, String columnNamesIn,
                                     ResultSet resultSetIn, ConnectionFactory factoryIn, Integer rowLimitIn,
                                     String dataSourceIn)
            throws CentrifugeException {

        try {
            // create list of fielddefs in the proper processing order
            PipedInputStream reader = new PipedInputStream();
            PipedOutputStream writer = new PipedOutputStream(reader);

            PGRawDataProducer producer = new PGRawDataProducer(writer, resultSetIn, factoryIn,
                    "UTF8", rowLimitIn, dataSourceIn);
            producer.setDaemon(true);

            PGCacheDataConsumer consumer = new PGCacheDataConsumer(reader, cacheConnectionIn.getConnection(),
                    tableNameIn, columnNamesIn);
            consumer.setDaemon(true);

            consumer.start();
            producer.start();

            return RecoveryHelper.monitorTransfer(cacheConnectionIn, producer, consumer, true);
        } catch (Exception myException) {
            throw new CentrifugeException("Failed loading raw table:\n" + Format.value(myException));
        }
    }

    private static long loadFromPipe(CsiConnection cacheConnectionIn, String tableNameIn, String columnNamesIn,
                                     ResultSet resultSetIn, String tempLocation, Integer rowLimitIn)
            throws CentrifugeException {
        try (InputStream tempInputStream = new FileInputStream(new File(tempLocation))) {
            PGCacheDataConsumer consumer = new PGCacheDataConsumer(tempInputStream, cacheConnectionIn.getConnection(),
                    tableNameIn, columnNamesIn);
            consumer.usePipeDelimiter(true);
            consumer.setDaemon(true);

            consumer.start();
            consumer.join();

            TaskHelper.checkForCancel();
            Throwable consumerError = consumer.getException();
            if (consumerError != null) {
                if (consumerError instanceof RuntimeException) {
                    throw (RuntimeException) consumerError;
                } else {
                    throw new CentrifugeException("loadFromPipe:Error writing data\n" + consumerError.getMessage(), consumerError);
                }
            }
            return consumer.getRowCount();
        } catch (Exception myException) {
            throw new CentrifugeException("Failed loading from pipe:\n" + Format.value(myException));
        }
    }

    public static Map<String, CsiDataType> getColumnMap(Connection connectionIn, String tableNameIn) {

        return getColumnMap(connectionIn, tableNameIn, null);
    }

    private static Map<String, CsiDataType> getColumnMap(Connection connectionIn, String tableNameIn,
                                                         Map<String, CsiDataType> mapIn) {

       Map<String, CsiDataType> myMap = (null != mapIn) ? mapIn : new HashMap<String, CsiDataType>();

        try (ResultSet results = DataCacheHelper.getOrderedColumnList(connectionIn, tableNameIn)) {

            while (SqlUtil.hasMoreRows(results)) {

                String myColumnName = results.getString(1);
                String myDbType = results.getString(2);
                myMap.put(myColumnName, CsiDataType.getMatchingType(myDbType));
            }

        } catch(Exception myException) {

            LOG.error("Caught exception retrieving column names.", myException);
        }

        return myMap;
    }

    private static Map<String, Integer> buildColumnIndexMap(ResultSet resultsIn) throws SQLException {

        Map<String, Integer> myColumnIndexMap = new HashMap<String, Integer>();
        ResultSetMetaData myResultSetMetaData = resultsIn.getMetaData();
        int myColumnCount = myResultSetMetaData.getColumnCount();

        LOG.debug("Map data columns ---------------------------------------------------");

        for (int i = 1; i <= myColumnCount; i++) {

            myColumnIndexMap.put(SqlUtil.getColumnName(myResultSetMetaData, i).toLowerCase(), i);

            LOG.debug("  ---  (" + Format.value(i) + ") " + SqlUtil.getColumnName(myResultSetMetaData, i).toLowerCase());
        }
        LOG.debug("--------------------------------------------------------------------");

        return myColumnIndexMap;
    }

    /*
        Map from target (DataView) field local id to source (Template) field local id list

        Used in creating actual linkup tables
     */
    private static Map<String, FieldDef> generateTargetSourceMapping(FieldListAccess modelIn, LinkupMapDef linkupIn) {

        if (null != linkupIn) {

            Map<String,FieldDef> myMapping = new HashMap<String, FieldDef>();
            List<LooseMapping> myList = linkupIn.getFieldsMap();

            for (LooseMapping myGrouping : myList) {

                String myTarget = myGrouping.getMappedLocalId();
                String mySource = myGrouping.getMappingLocalId();
                FieldDef myField = ((null != myTarget) && (null != mySource))
                        ? modelIn.getFieldDefByLocalId(mySource) : null;

                if (null != myField) {

                    myMapping.put(myTarget, myField);
                }
            }
            return myMapping;

        } else {

            return null;
        }
    }

    private static Map<String,String> getQuotedColumnMap(Connection connectionIn, String tableNameIn) {
       Map<String,String> myMap = new HashMap<String,String>();

       try (ResultSet results = DataCacheHelper.getOrderedColumnList(connectionIn, tableNameIn)) {
          while (SqlUtil.hasMoreRows(results)) {
             String myColumnName = results.getString(1);
             String myDbType = results.getString(2);

             myMap.put(CacheUtil.quote(myColumnName), myDbType);
          }
       } catch (Exception myException) {
          SqlUtil.quietRollback(connectionIn);
          LOG.error("Caught exception retrieving column names.", myException);
       }
       return myMap;
    }

    private static void identifySourceTypes(DataDefinition metaIn) {

        if ((null != metaIn) && (!metaIn.hasStorageTypes())) {

            DataSetOp myTree = metaIn.getDataTree();
            Map<String, CsiDataType> myMap = myTree.buildUnionCastingMap();

            for (FieldDef myField : metaIn.getFieldListAccess().getFieldDefList()) {

                if (null == myField.getStorageType()) {

                    ColumnDef myColumn = metaIn.getColumnByKey(myField.getColumnKey());

                    if (null != myColumn) {

                        CsiDataType myDataType = myMap.get(myColumn.getColumnKey());

                        myField.forceStorageType((null != myDataType) ? myDataType : myColumn.getCsiType());

                    } else {

                        myField.forceStorageType(myField.getValueType());
                    }
                }
            }
            metaIn.setStorageTypesFlag();
        }
    }

    private static List<ValuePair<FieldDef, FieldDef>> matchFieldDefs(List<ColumnDef> columnListIn,
                                                                      FieldListAccess modelIn) {

        List<ValuePair<FieldDef, FieldDef>> myList = new ArrayList<ValuePair<FieldDef, FieldDef>>();

        for (ColumnDef myColumn : columnListIn) {

            String myColumnKey = myColumn.getColumnKey();
            FieldDef myField = (null != myColumnKey) ? modelIn.getFieldDefByColumnKey(myColumnKey) : null;

            if (null != myField) {

                myList.add(new ValuePair<FieldDef, FieldDef>(myField, myField));
            }
        }
        return myList;
    }

    private static List<ValuePair<FieldDef, FieldDef>> matchFieldDefs(List<ColumnDef> columnListIn,
                                                                      FieldListAccess modelIn,
                                                                      Map<String, FieldDef> capcoColumnMapIn,
                                                                      Map<String, FieldDef> tagColumnMapIn,
                                                                      Map<String, FieldDef> targetToSourceMapIn) {

        List<ValuePair<FieldDef, FieldDef>> myList = new ArrayList<ValuePair<FieldDef, FieldDef>>();

        for (Map.Entry<String, FieldDef> myEntry : targetToSourceMapIn.entrySet()) {

            FieldDef mySource = myEntry.getValue();
            FieldDef myTarget = modelIn.getFieldDefByLocalId(myEntry.getKey());

            if ((null != mySource) && (null != myTarget)) {

                myList.add(new ValuePair<FieldDef, FieldDef>(myTarget, mySource));
                if ((null != capcoColumnMapIn) && capcoColumnMapIn.containsKey(mySource.getLocalId())) {

                    capcoColumnMapIn.remove(mySource.getLocalId());
                    capcoColumnMapIn.put(myTarget.getLocalId(), myTarget);
                }
                if ((null != tagColumnMapIn) && tagColumnMapIn.containsKey(mySource.getLocalId())) {

                    tagColumnMapIn.remove(mySource.getLocalId());
                    tagColumnMapIn.put(myTarget.getLocalId(), myTarget);
                }
            }
        }
        if ((null != capcoColumnMapIn) && !capcoColumnMapIn.isEmpty()) {

            for (Map.Entry<String, FieldDef> myEntry : capcoColumnMapIn.entrySet()) {

                if (!targetToSourceMapIn.containsKey(myEntry.getKey())) {

                    myList.add(new ValuePair<FieldDef, FieldDef>(myEntry.getValue(), myEntry.getValue()));
                }
            }
        }
        if ((null != tagColumnMapIn) && !tagColumnMapIn.isEmpty()) {

            for (Map.Entry<String, FieldDef> myEntry : tagColumnMapIn.entrySet()) {

                if (!targetToSourceMapIn.containsKey(myEntry.getKey())) {

                    myList.add(new ValuePair<FieldDef, FieldDef>(myEntry.getValue(), myEntry.getValue()));
                }
            }
        }
        return myList;
    }

    public static List<String> getColumnNames(Map<String, FieldDef> mapIn) {

        List<String> myList = new ArrayList<String>();

        if (mapIn != null) {

            for (FieldDef myField : mapIn.values()) {

                myList.add(CacheUtil.getColumnName(myField));
            }
        }
        return myList.isEmpty() ? null : myList;
    }

    private static Set<String> getSourceColumns(List<ValuePair<FieldDef, FieldDef>> listIn) {

        Set<String> myList = new HashSet<String>();

        for (ValuePair<FieldDef, FieldDef> myPair : listIn) {

            FieldDef mySource = myPair.getValue2();

            if (null != mySource) {

                myList.add(mySource.getColumnKey());
            }
        }
        return myList;
    }

    private static boolean addPrecalculatedFieldDefs(FieldListAccess modelIn,
                                                     List<ValuePair<FieldDef, FieldDef>> fieldListIn) {

        boolean myChange = false;
        List<ValuePair<FieldDef, FieldDef>> myFieldList = (null != fieldListIn)
                ? fieldListIn
                : new ArrayList<ValuePair<FieldDef, FieldDef>>();
        List<FieldDef> myAdditions = modelIn.getOrderedPreCaculatedFieldDefs();

        if ((null != myAdditions) && !myAdditions.isEmpty()) {

            myChange = true;
            for (FieldDef myField : myAdditions) {

                myFieldList.add(new ValuePair<FieldDef, FieldDef>(myField, null));
            }
        }

        return myChange;
    }

   private static String getTableName(DataWrapper sourceIn, DataWrapper targetIn, int generationIn) {
      String result = null;

      if (generationIn == 0) {
         if (sourceIn != null) {
            if (sourceIn instanceof InstalledTable) {
               result = ((InstalledTable) sourceIn).getNextActiveTableName();
            } else if (targetIn != null) {
               result = CacheUtil.getCacheTableName(targetIn.getUuid(), 0);
            } else {
               result = CacheUtil.getCacheTableName(sourceIn.getUuid(), 0);
            }
         }
      } else {
         result = CacheUtil.getCacheTableName(targetIn.getUuid(), generationIn);
      }
      return result;
   }

    private static boolean isCallProcWithUnion(String sql) {
        return (CacheCommands.UNION_PATTERN.matcher(sql).find() && CacheCommands.CALL_PATTERN.matcher(sql).find());
    }

    private static void releaseTempLocks(List<String> lockListIn) {
        for (String myLock : lockListIn) {

            if (null != myLock) {

                // Release lock on current installed table version
                SharedDataSourceHelper.releaseInstalledTable(myLock);
            }
        }
    }

    private static void dropTempTables(Connection connectionIn, List<String> tableNamesIn) {
        for (String myTable : tableNamesIn) {

            dropCacheTable(connectionIn, myTable);
        }
    }

    private static void dropTempViews(Connection connectionIn, List<String> viewNamesIn) {
        for (String myView : viewNamesIn) {

            dropCacheView(connectionIn, myView);
        }
    }

    public static void deleteTopView(Connection connectionIn, DataView dataViewIn)
            throws CentrifugeException, SQLException {

        String myDynamicView = CacheUtil.getCacheTableName(dataViewIn.getUuid());
        String myQueryString = "DROP VIEW IF EXISTS " + CacheUtil.quote(myDynamicView);
        QueryHelper.executeSQL(connectionIn, myQueryString, null);
        dataViewIn.removeView(myDynamicView);
        connectionIn.commit();
    }

    public static void renameCacheView(Connection connectionIn, String oldNameIn, String newNameIn, boolean commitIn) throws Exception {
        String sql = String.format(CacheCommands.RENAME_CACHE_VIEW, CacheUtil.quote(oldNameIn), CacheUtil.quote(newNameIn));

        try (Statement stmt = connectionIn.createStatement()) {
            stmt.addBatch(sql);
            stmt.executeBatch();

            if (commitIn) {
               connectionIn.commit();
            }
        } catch(Exception myException) {
            SqlUtil.quietRollback(connectionIn);
            throw myException;
        }
    }

    public static void dropCacheTable(Connection connectionIn, String tableNameIn) {

        try {

            dropCacheTable(connectionIn, tableNameIn, true);

        } catch(Exception myException) {

            LOG.error("Caught exception dropping cache table " + Format.value(tableNameIn), myException);
        }
    }

    public static void dropCacheView(Connection connectionIn, String viewNameIn) {

        try {

            dropCacheView(connectionIn, viewNameIn, true);

        } catch(Exception myException) {

            LOG.error("Caught exception dropping cache view " + Format.value(viewNameIn), myException);
        }
    }

    public static void dropCacheTable(Connection connectionIn, String tableNameIn, boolean commitIn) throws Exception {

        String myTableCommand = String.format(CacheCommands.DROP_CACHE_TABLE, CacheUtil.quote(tableNameIn));
        String mySequenceCommand = String.format(CacheCommands.DROP_CACHE_IDENTITY,
                CacheUtil.quote(CacheUtil.getGeneratorNameForCacheTable(tableNameIn)));

        for (int i = 0; 2 > i; i++) {

            try {

                QueryHelper.executeSQL(connectionIn, myTableCommand, null);
                QueryHelper.executeSQL(connectionIn, mySequenceCommand, null);
                if (commitIn) {

                    connectionIn.commit();
                }
                break;

            } catch(Exception myException) {

                SqlUtil.quietRollback(connectionIn);
            }
        }
    }

    public static void dropCacheView(Connection connectionIn, String viewNameIn, boolean commitIn) throws Exception {

        String myCommand = String.format(CacheCommands.DROP_CACHE_VIEW, CacheUtil.quote(viewNameIn));

        for (int i = 0; 2 > i; i++) {

            try {

                QueryHelper.executeSQL(connectionIn, myCommand, null);
                if (commitIn) {

                    connectionIn.commit();
                }
                break;

            } catch(Exception myException) {

                SqlUtil.quietRollback(connectionIn);
            }
        }
    }

    private static boolean dataTypesMatch(DataDefinition metaIn) {

        boolean mySuccess = true;
        List<FieldDef> myFieldList = metaIn.getFieldListAccess().getFieldDefList();

        for (FieldDef myField : myFieldList) {

            String myColumnKey = myField.getColumnKey();

            if (FieldType.COLUMN_REF == myField.getFieldType()) {

                ColumnDef myColumn = metaIn.getColumnByKey(myColumnKey);

                if ((null == myColumn) || (myColumn.getCsiType() != myField.getStorageType())) {

                    mySuccess = false;
                    break;
                }

            } else if ((FieldType.SCRIPTED == myField.getFieldType())
                    || ((FieldType.DERIVED == myField.getFieldType()) && myField.isPreCalculated())) {

                mySuccess = false;
                break;
            }
        }
        return mySuccess;
    }

    private static void createDataTable(CsiConnection connectionIn, String tableNameIn, String originIn,
                                        List<ValuePair<FieldDef, FieldDef>> fieldListIn, long rowStartIn)
            throws Exception {

        if ((null != fieldListIn) && !fieldListIn.isEmpty()) {

            String myIdentity = CacheUtil.quote(CacheTokens.CSI_ROW_ID);
            String mySequenceCommand = buildCreateSequenceCommand(tableNameIn, rowStartIn);
            String myTableCommand = addDataColumns(beginCreateTableRequest(tableNameIn, myIdentity, originIn, null, null),
                                                    myIdentity, fieldListIn);

            // Create table and identity control
            dropCacheTable(connectionIn, tableNameIn);
            QueryHelper.executeSQL(connectionIn, mySequenceCommand, null);
            if (_doDebug) {
               LOG.debug("Create Data Table using command:\n" + myTableCommand + "\n");
            }
            QueryHelper.executeSQL(connectionIn, myTableCommand, null);
            connectionIn.commit();

        } else {

            throw new CentrifugeException("Attempting to create a data table with no columns!");
        }
    }

    private static String createStagingTable(CsiConnection connectionIn, String tableNameIn, ResultSetMetaData metaIn,
                                             Map<String, CsiDataType> dataTypeMapIn)
            throws CentrifugeException, SQLException {

        StringBuilder myBuffer = beginCreateTableRequest(tableNameIn, null, null, null, null);
        String myColumnNames = addStagingColumns(myBuffer, metaIn, dataTypeMapIn);

        if ((null != myColumnNames) && (0 < myColumnNames.length())) {

            String myTableCommand = myBuffer.toString();

            // Create table and identity control
            dropCacheTable(connectionIn, tableNameIn);
            if (_doDebug) {
               LOG.debug("Create Data Table using command:\n" + myTableCommand + "\n");
            }
            QueryHelper.executeSQL(connectionIn, myTableCommand, null);
            connectionIn.commit();

            return myColumnNames;

        } else {

            throw new CentrifugeException("Attempting to create a staging table with no columns!");
        }
    }

    private static String buildCreateSequenceCommand(String tableNameIn, long rowStartIn) {

        return "CREATE SEQUENCE "
                + CacheUtil.quote(CacheUtil.getGeneratorNameForCacheTable(tableNameIn))
                + " START " + Long.toString(rowStartIn);
    }

    private static StringBuilder beginCreateTableRequest(String tableNameIn, String identityIn,
                                                         String originIn, String capcoIn, String tagsIn) {

        StringBuilder myBuffer = new StringBuilder();

        myBuffer.append("CREATE TABLE ");
        myBuffer.append(CacheUtil.quote(tableNameIn));
        myBuffer.append(" (");
        if (null != identityIn) {

            myBuffer.append(identityIn);
            myBuffer.append(" INTEGER UNIQUE DEFAULT nextval('");
            myBuffer.append(CacheUtil.getGeneratorNameForCacheTable(tableNameIn));
            myBuffer.append("'), ");
        }
        if (null != originIn) {

            myBuffer.append(originIn);
            myBuffer.append(" INTEGER, ");
        }
        if (null != capcoIn) {

            myBuffer.append(capcoIn);
            myBuffer.append(" SMALLINT, ");
        }
        if (null != tagsIn) {

            myBuffer.append(tagsIn);
            myBuffer.append(" SMALLINT, ");
        }
        return myBuffer;
    }

    private static String addInstalledColumns(StringBuilder bufferIn, String identityIn,
                                              List<InstalledColumn> columnListIn) {

        for (InstalledColumn myColumn : columnListIn) {

            bufferIn.append(CacheUtil.quote(myColumn.getColumnName()));
            bufferIn.append(" ");
            bufferIn.append(myColumn.getType().getSqlType());
            bufferIn.append(", ");
        }
        bufferIn.append("PRIMARY KEY (");
        bufferIn.append(identityIn);
        bufferIn.append("))");

        return bufferIn.toString();
    }

    private static String addDataColumns(StringBuilder bufferIn, String identityIn,
                                         List<ValuePair<FieldDef, FieldDef>> fieldListIn) {

        for (ValuePair<FieldDef, FieldDef> myPair : fieldListIn) {

            FieldDef myField = myPair.getValue1();

            if ((FieldType.SCRIPTED == myField.getFieldType())
                    || (FieldType.DERIVED == myField.getFieldType())) {

                myField.setStorageType(myField.getValueType());
            }
            bufferIn.append(CacheUtil.getQuotedColumnName(myField));
            bufferIn.append(" ");
            bufferIn.append(myField.tryStorageType().getSqlType());
            bufferIn.append(", ");
        }
        bufferIn.append("PRIMARY KEY (");
        bufferIn.append(identityIn);
        bufferIn.append("))");

        return bufferIn.toString();
    }

    private static String addStagingColumns(StringBuilder bufferIn, ResultSetMetaData metaIn,
                                            Map<String,CsiDataType> dataTypeMapIn)
          throws SQLException {
       String myColumnString = null;
       StringBuilder myBuffer = new StringBuilder();
       int myColumnCount = metaIn.getColumnCount();
       boolean first = true;

       for (int myColumnIndex = 1; myColumnCount >= myColumnIndex; myColumnIndex++) {
          String myColumnName = SqlUtil.getColumnName(metaIn, myColumnIndex).toLowerCase();
          CsiDataType myCsiType = dataTypeMapIn.get(myColumnName);

          if (first) {
             first = false;
          } else {
             myBuffer.append(", ");
             bufferIn.append(", ");
          }
          myBuffer.append(CacheUtil.quote(myColumnName));

          bufferIn.append(CacheUtil.quote(myColumnName));
          bufferIn.append(" ");
          bufferIn.append(myCsiType.getSqlType());
       }
       if (myBuffer.length() > 0) {
          bufferIn.append(')');
          myColumnString = myBuffer.toString();
       }
       return myColumnString;
    }

   public static void grantReadAccess(Connection connectionIn, String tableIn) {
      String mySelectCommand = String.format(CacheCommands.GRANT_SELECT_PERMISSION, CacheUtil.quote(tableIn));

      try (Statement statement = connectionIn.createStatement()) {
         statement.execute(mySelectCommand);
         connectionIn.commit();
      } catch (Exception myException) {
         LOG.error("Unable to grant read access to 'csiuser'");
      }
   }

   public static void grantWriteAccess(Connection connectionIn, String tableIn) {
      String myGenerator = CacheUtil.getGeneratorNameForCacheTable(tableIn);
      String mySelectCommand1 = String.format(CacheCommands.GRANT_SELECT_PERMISSION, CacheUtil.quote(tableIn));
      String myInsertCommand1 = String.format(CacheCommands.GRANT_INSERT_PERMISSION, CacheUtil.quote(tableIn));
      String myUpdateCommand1 = String.format(CacheCommands.GRANT_UPDATE_PERMISSION, CacheUtil.quote(tableIn));
      String mySelectCommand2 = String.format(CacheCommands.GRANT_SELECT_PERMISSION, CacheUtil.quote(myGenerator));
      String myInsertCommand2 = String.format(CacheCommands.GRANT_INSERT_PERMISSION, CacheUtil.quote(myGenerator));
      String myUpdateCommand2 = String.format(CacheCommands.GRANT_UPDATE_PERMISSION, CacheUtil.quote(myGenerator));

      try (Statement statement = connectionIn.createStatement()) {
         statement.execute(mySelectCommand1);
         statement.execute(myInsertCommand1);
         statement.execute(myUpdateCommand1);
         statement.execute(mySelectCommand2);
         statement.execute(myInsertCommand2);
         statement.execute(myUpdateCommand2);
         connectionIn.commit();
      } catch (Exception myException) {
         LOG.error("Unable to grant write access to 'csiuser'");
      }
   }

   public static void revokeReadAccess(Connection connectionIn, String tableIn) {
      String mySelectCommand = String.format(CacheCommands.REVOKE_SELECT_PERMISSION, CacheUtil.quote(tableIn));

      try (Statement statement = connectionIn.createStatement()) {
         statement.execute(mySelectCommand);
         connectionIn.commit();
      } catch(Exception myException) {
         LOG.error("Unable to revoke read access from 'csiuser'");
      }
   }

   public static void revokeWriteAccess(Connection connectionIn, String tableIn) {
      String myGenerator = CacheUtil.getGeneratorNameForCacheTable(tableIn);
      String mySelectCommand1 = String.format(CacheCommands.REVOKE_SELECT_PERMISSION, CacheUtil.quote(tableIn));
      String myInsertCommand1 = String.format(CacheCommands.REVOKE_INSERT_PERMISSION, CacheUtil.quote(tableIn));
      String myUpdateCommand1 = String.format(CacheCommands.REVOKE_UPDATE_PERMISSION, CacheUtil.quote(tableIn));
      String mySelectCommand2 = String.format(CacheCommands.REVOKE_SELECT_PERMISSION, CacheUtil.quote(myGenerator));
      String myInsertCommand2 = String.format(CacheCommands.REVOKE_INSERT_PERMISSION, CacheUtil.quote(myGenerator));
      String myUpdateCommand2 = String.format(CacheCommands.REVOKE_UPDATE_PERMISSION, CacheUtil.quote(myGenerator));

      try (Statement statement = connectionIn.createStatement()) {
         statement.execute(mySelectCommand1);
         statement.execute(myInsertCommand1);
         statement.execute(myUpdateCommand1);
         statement.execute(mySelectCommand2);
         statement.execute(myInsertCommand2);
         statement.execute(myUpdateCommand2);
         connectionIn.commit();
      } catch (Exception myException) {
         LOG.error("Unable to revoke write permission from 'csiuser'");
      }
   }

    // TODO: Fixup Security for SPIN-OFF

   public static void finalizeSpinOff(DataView dataViewIn) throws CentrifugeException, SQLException {
     try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
        String myTableName = CacheUtil.getCacheTableName(dataViewIn.getUuid(), 0);

        // Create a top level view of the cache including dynamic fields
        createTopView(myConnection, dataViewIn);

        // Handle ACL security and banners
        SecurityHelper.finalizeSpinoffSecurityData(myConnection, dataViewIn, myTableName);

        myConnection.commit();
     } catch (Exception exception) {
        LOG.error(exception);
     }
   }

    public static String createTopView(Connection connectionIn, DataView dataViewIn)
            throws CentrifugeException, SQLException {

        identifySourceTypes(dataViewIn.getMeta());
        FieldListAccess myModel = dataViewIn.getMeta().refreshFieldListAccess();
        String myDynamicView = CacheUtil.getCacheTableName(dataViewIn.getUuid());
        List<FieldDef> myOrderedFieldList = (null != myModel) ? myModel.getOrderedFieldDefList() : null;
        String myQueryString = buildFinalViewQueryString(connectionIn, dataViewIn, myDynamicView, myOrderedFieldList);
        if (_doDebug) {
         LOG.debug("Create Top View using:\n" + myQueryString + "\n");
      }
        QueryHelper.executeSQL(connectionIn, myQueryString, null);
        dataViewIn.addView(myDynamicView);
        connectionIn.commit();

        return myDynamicView;
    }

    public static String createColumnLevelView(Connection connectionIn, DataView dataViewIn, List<FieldDef> fieldListIn)
            throws CentrifugeException, SQLException {

        StringBuilder myBuffer = new StringBuilder();
        String myDynamicView = CacheUtil.getColumnViewName(dataViewIn.getUuid());
        DataViewDef myMeta = dataViewIn.getMeta();
        List<FieldDef> myFieldList = (null != fieldListIn)
                ? fieldListIn
                : getSortedFields(myMeta.getFieldList());

        myBuffer.append("CREATE OR REPLACE VIEW ");
        myBuffer.append(CacheUtil.quote(myDynamicView));
        myBuffer.append(" (");
        myBuffer.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));
        myBuffer.append(", ");
        myBuffer.append(CacheUtil.quote(CacheTokens.CSI_GROUP_ID));

        for (FieldDef myFieldDef : myFieldList) {

            FieldType myFieldType = myFieldDef.getFieldType();

            if ((FieldType.COLUMN_REF == myFieldType)
                    || (FieldType.LINKUP_REF == myFieldType) || myFieldDef.isPreCalculated()) {

                myBuffer.append(", ");
                myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));
            }
        }
        myBuffer.append( ") AS ");
        buildStaticViewQueryString(myBuffer, connectionIn, dataViewIn, myFieldList);
        if (_doDebug) {
         LOG.debug("Create Column View using:\n" + myBuffer.toString() + "\n");
      }
        QueryHelper.executeSQL(connectionIn, myBuffer.toString(), null);
        connectionIn.commit();

        return myDynamicView;
    }

   private static List<ValuePair<String, String>> genTempViews(Connection connectionIn, DataView dataViewIn)
         throws CentrifugeException, SQLException {
      List<ValuePair<String, String>> myViewList = new ArrayList<>();

      if (dataViewIn != null) {
         DataViewDef myMeta = dataViewIn.getMeta();
         FieldListAccess myModel = (myMeta != null) ? myMeta.refreshFieldListAccess() : null;

         if ((myModel != null) && myModel.hasPrecalculatedFields()) {
            String[] myTables = dataViewIn.getTableList();
            String[] myLinkups = dataViewIn.getLinkupList();
            List<FieldDef> myOrderedFieldList = myModel.getOrderedFieldDefList();

            for (int i = 0; i < 2; i++) {
               if ((myTables != null) && (myTables.length > 0)) {
                  for (int j = 0; j < myTables.length; j++) {
                     String myTable = myTables[j];
                     String myView = CacheUtil.getColumnViewNameFromTable(myTable);

                     if (myView != null) {
                        createPartialView(connectionIn, myView, myTable, myOrderedFieldList);
                        myViewList.add(new ValuePair<String, String>(myTable, myView));
                     }
                  }
               }
               myTables = myLinkups;
            }
         }
      }
      return myViewList;
   }

    public static void createPartialView(Connection connectionIn, String viewNameIn,
                                         String tableNameIn, List<FieldDef> fieldListIn)
            throws CentrifugeException, SQLException {

        dropCacheView(connectionIn, viewNameIn);
        StringBuilder myBuffer = new StringBuilder();
        Map<String, CsiDataType> myColumnMap = getColumnMap(connectionIn, tableNameIn);

        myBuffer.append("CREATE OR REPLACE VIEW ");
        myBuffer.append(CacheUtil.quote(viewNameIn));
        myBuffer.append("AS ");
        addLinkupToQuery(myBuffer, fieldListIn, tableNameIn, myColumnMap);
        QueryHelper.executeSQL(connectionIn, myBuffer.toString(), null);
        connectionIn.commit();
    }

   public static void createViewForInstalledTable(InstalledTable tableIn)
         throws CentrifugeException {
      List<InstalledColumn> myColumnList = tableIn.getColumns();

      try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
         try {
            String myViewName = tableIn.getTablePath();
            String myTableName = tableIn.getActiveTableName();
            String myViewCommand = buildCacheViewCommand(myViewName, myTableName, myColumnList);

            // Create view
            dropCacheView(myConnection, myViewName, true);

            if (_doDebug) {
               LOG.debug("Create View for Installed Table using command:\n" + myViewCommand + "\n");
            }
            QueryHelper.executeSQL(myConnection, myViewCommand, null);
            myConnection.commit();
         } catch (Exception exception) {
            if (null != myConnection) {
               try {
                  myConnection.rollback();
               } catch(Exception myRollbackException) {
               }
            }
            throw new CentrifugeException("Failed creating view from upload:\n"
                                          + CacheUtil.replaceNames(Format.value(exception),
                                          CacheUtil.getTableNamePair(tableIn),
                                          CacheUtil.getColumnNameMap(myColumnList)));
         }
      } catch (Exception exception) {
         throw new CentrifugeException("Failed creating view from upload:\n"
                                       + CacheUtil.replaceNames(Format.value(exception),
                                       CacheUtil.getTableNamePair(tableIn),
                                       CacheUtil.getColumnNameMap(myColumnList)));
      }
   }

    private static String buildFinalViewQueryString(Connection connectionIn, DataView dataViewIn,
                                                    String viewNameIn, List<FieldDef> orderedFieldListIn) {

        CacheConnectionFactory myFactory = new CacheConnectionFactory();
        StringBuilder myBuffer = new StringBuilder();
        DataViewDef myMeta = dataViewIn.getMeta();
        FieldListAccess myModel = myMeta.getFieldListAccess();
        int myLevelLimit = FieldCycleDetector.markUncachedFields(myModel);

        myBuffer.append("CREATE OR REPLACE VIEW ");
        myBuffer.append(CacheUtil.quote(viewNameIn));
        myBuffer.append(" (");
        myBuffer.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));
        myBuffer.append(", ");
        myBuffer.append(CacheUtil.quote(CacheTokens.CSI_GROUP_ID));

        for (FieldDef myFieldDef : orderedFieldListIn) {

            myBuffer.append(", ");
            myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));
        }
        myBuffer.append( ") AS ");

        for (int i = 0; myLevelLimit > i; i++) {

            String myPrefix = "q" + Integer.toString(i);

            myBuffer.append( " SELECT ");
            myBuffer.append(CacheUtil.quotePrefix(myPrefix));
            myBuffer.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));
            myBuffer.append(" AS ");
            myBuffer.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));
            myBuffer.append(", ");
            myBuffer.append(CacheUtil.quotePrefix(myPrefix));
            myBuffer.append(CacheUtil.quote(CacheTokens.CSI_GROUP_ID));
            myBuffer.append(" AS ");
            myBuffer.append(CacheUtil.quote(CacheTokens.CSI_GROUP_ID));

            for (FieldDef myFieldDef : orderedFieldListIn) {

                FieldType myFieldType = myFieldDef.getFieldType();

                if ((FieldType.COLUMN_REF == myFieldType)
                        || (FieldType.LINKUP_REF == myFieldType) || myFieldDef.isPreCalculated()) {

                    myBuffer.append(", ");
                    if ((0 == i) && (myFieldDef.getValueType() != myFieldDef.tryStorageType())) {

                        myBuffer.append(myFactory.castExpression(CacheUtil.getQuotedColumnName(myFieldDef, myPrefix),
                                myFieldDef.getValueType()));

                    } else  {

                        myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef, myPrefix));
                    }
                    myBuffer.append(" AS ");
                    myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));

                } else if (FieldType.STATIC == myFieldType) {

                    if (0 == i) {

                        myBuffer.append(", ");
                        myBuffer.append(myFactory.castExpression(formatConstant(myFieldDef), myFieldDef.getValueType()));
                        myBuffer.append(" AS ");
                        myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));
                    }

                } else if ((FieldType.DERIVED == myFieldType) && (myFieldDef.getLevel() >= i)) {

                    myBuffer.append(", ");
                    if (myFieldDef.getLevel() == i) {

                        String myExpression = formatExpression(myMeta, myFieldDef, myPrefix);
                        myBuffer.append((null != myExpression) ? myExpression : CsiDataType.getCacheNull(myFieldDef.getValueType()));

                    } else {

                        myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef, myPrefix));
                    }
                    myBuffer.append(" AS ");
                    myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));
                }
            }
            myBuffer.append(" FROM (");
        }
        buildStaticViewQueryString(myBuffer, connectionIn, dataViewIn, orderedFieldListIn);

        for (int i = myLevelLimit - 1; 0 <= i; i--) {

            myBuffer.append(") q");
            myBuffer.append(Integer.toString(i));
        }

        return myBuffer.toString();
    }

    private static StringBuilder buildStaticViewQueryString(StringBuilder bufferIn, Connection connectionIn,
                                                            DataView dataViewIn, List<FieldDef> fieldListIn) {

        CacheConnectionFactory myFactory = new CacheConnectionFactory();
        String myTableName = CacheUtil.getCacheTableName(dataViewIn.getUuid(), 0);
        StringBuilder myBuffer = (null != bufferIn) ? bufferIn : new StringBuilder();
        String[] myLinkups = dataViewIn.getLinkupList();
        Map<String, CsiDataType> myColumnMap = getColumnMap(connectionIn, myTableName);

        if ((null != myColumnMap) && !myColumnMap.isEmpty()) {

            myBuffer.append(" SELECT ");
            myBuffer.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));
            myBuffer.append(", ");
            myBuffer.append("0::INTEGER AS " + CacheUtil.quote(CacheTokens.CSI_GROUP_ID));

            for (FieldDef myFieldDef : fieldListIn) {

                String myColumnName = CacheUtil.getColumnName(myFieldDef);
                CsiDataType mySourceType = myColumnMap.get(myColumnName);
                CsiDataType myTargetType = myFieldDef.tryStorageType();
                FieldType myFieldType = myFieldDef.getFieldType();

                if ((FieldType.COLUMN_REF == myFieldType)
                        || (FieldType.LINKUP_REF == myFieldType) || myFieldDef.isPreCalculated()) {

                    myBuffer.append(", ");
                    if (null != mySourceType) {

                        if ((null == myTargetType) || (myTargetType == mySourceType)) {

                            myBuffer.append(CacheUtil.quote(myColumnName));

                        } else {

                            myBuffer.append(myFactory.castExpression(CacheUtil.quote(myColumnName), myTargetType));
                        }

                    } else if (null != myTargetType) {

                        myBuffer.append(CsiDataType.getCacheNull(myTargetType));

                    } else {

                        myBuffer.append("null");
                    }
                    myBuffer.append(" AS ");
                    myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));
                }
            }
            myBuffer.append(" FROM ");
            myBuffer.append(CacheUtil.quote(myTableName));

            if ((null != myLinkups) && (0 < myLinkups.length)) {

                for (int i = 0; myLinkups.length > i; i++) {

                    myBuffer.append( " UNION ALL ");
                    addLinkupToQuery(myBuffer, fieldListIn, myLinkups[i], getColumnMap(connectionIn, myLinkups[i]));
                }
            }
        }
        return myBuffer;
    }

    private static StringBuilder addLinkupToQuery(StringBuilder bufferIn, List<FieldDef> fieldListIn,
                                                  String linkupTableNameIn, Map<String, CsiDataType> columnMapIn) {

        CacheConnectionFactory myFactory = new CacheConnectionFactory();
        StringBuilder myBuffer = (null != bufferIn) ? bufferIn : new StringBuilder();

        if ((null != columnMapIn) && !columnMapIn.isEmpty()) {

            myBuffer.append( " SELECT ");
            myBuffer.append(CacheUtil.quote(linkupTableNameIn));
            myBuffer.append(".");
            myBuffer.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));
            myBuffer.append(", ");
            myBuffer.append(linkupTableNameIn.substring(1 + linkupTableNameIn.lastIndexOf('_')));
            myBuffer.append("::INTEGER AS " + CacheTokens.CSI_GROUP_ID);

            for (FieldDef myFieldDef : fieldListIn) {

                String myColumnName = CacheUtil.getColumnName(myFieldDef);
                CsiDataType mySourceType = columnMapIn.get(myColumnName);
                CsiDataType myTargetType = myFieldDef.tryStorageType();
                FieldType myFieldType = myFieldDef.getFieldType();

                if ((FieldType.COLUMN_REF == myFieldType)
                        || (FieldType.LINKUP_REF == myFieldType) || myFieldDef.isPreCalculated()) {

                    myBuffer.append(", ");
                    if (null != mySourceType) {

                        if ((null == myTargetType) || (myTargetType == mySourceType)) {

                            myBuffer.append(CacheUtil.quote(myColumnName));

                        } else {

                            myBuffer.append(myFactory.castExpression(CacheUtil.quote(myColumnName), myTargetType));
                        }

                    } else if (null != myTargetType) {

                        myBuffer.append(CsiDataType.getCacheNull(myTargetType));

                    } else {

                        myBuffer.append("null");
                    }
                    myBuffer.append(" AS ");
                    myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));
                }
            }
            myBuffer.append(" FROM ");
            myBuffer.append(CacheUtil.quote(linkupTableNameIn));
        }
        return myBuffer;
    }

    private static String buildCacheViewCommand(String viewNameIn, String tableNameIn,
                                                List<InstalledColumn> columnListIn) {
        StringBuilder myBuffer = new StringBuilder();
        int howMany = columnListIn.size();

        myBuffer.append(" CREATE OR REPLACE VIEW ");
        myBuffer.append(CacheUtil.quoteAndEscape(viewNameIn));
        myBuffer.append(" (\"" + CacheTokens.CSI_ROW_ID + "\"");

        for (int i = 0; i < howMany; i++) {
            myBuffer.append(", ");
            myBuffer.append(CacheUtil.quoteAndEscape(columnListIn.get(i).getFieldName()));
        }
        myBuffer.append(") AS SELECT \"v1\".\"" + CacheTokens.CSI_ROW_ID + "\", \"v1\".");

        for (int i = 0; i < howMany; i++) {
            if (i > 0) {
                myBuffer.append(", \"v1\".");
            }
            myBuffer.append(CacheUtil.quote(columnListIn.get(i).getColumnName()));
            myBuffer.append(" AS ");
            myBuffer.append(CacheUtil.quoteAndEscape(columnListIn.get(i).getFieldName()));
        }
        myBuffer.append(" FROM ");
        myBuffer.append(CacheUtil.quote(tableNameIn));
        myBuffer.append(" \"v1\"");
        return myBuffer.toString();
    }

    private static void loadDerivedColumnData(Connection connectionIn, DataDefinition metaDataIn,
                                              List<ValuePair<String, String>> viewListIn)
            throws CentrifugeException, SQLException {

        FieldListAccess myModel = metaDataIn.getFieldListAccess();
        List<FieldDef> mySqlDerivedFields = myModel.getOrderedDirtyRestrictedFieldDefs();

        if ((null != viewListIn) && !viewListIn.isEmpty()) {

            for (ValuePair<String, String> myView : viewListIn) {

                String myQueryString
                        = buildDerivedSqlFieldsTableUpdateQuery(metaDataIn, myView.getValue1(), myView.getValue2(),
                                                                mySqlDerivedFields, myModel.getColumnMap());

                if ((null != myQueryString) && (0 < myQueryString.length())) {

                    QueryHelper.executeSQL(connectionIn, myQueryString, null);
                    connectionIn.commit();
                }
            }
        }
    }

    private static void definePreCalculatedStorageTypes(FieldListAccess fieldAccessIn) {

        for (FieldDef myFieldDef : fieldAccessIn.getOrderedPreCaculatedFieldDefs()) {

            myFieldDef.setStorageType(myFieldDef.getValueType());
        }
    }

    private static void replaceRetypedColumnData(Connection connectionIn, DataView dataViewIn,
                                                 String[] tableListIn, String[] linkupListIn)
            throws CentrifugeException, SQLException {

        List<FieldDef> myList = dataViewIn.getMeta().getModelDef().getFieldListAccess().getOrderedDirtyRetypedFieldDefs();

        if ((null != myList) && !myList.isEmpty()) {

            if ((null != tableListIn) && (0 < tableListIn.length)) {

                for (String myTable : tableListIn) {

                    retypeDataColumns(connectionIn, myTable, myList);
                }
            }
            if ((null != linkupListIn) && (0 < linkupListIn.length)) {

                for (String myTable : linkupListIn) {

                    retypeDataColumns(connectionIn, myTable, myList);
                }
            }
            for (FieldDef myField : myList) {

                myField.setStorageType(myField.getValueType());
                myField.setPreCalculated(false);
                myField.setDirty(false);
            }
        }
    }

    private static String buildDerivedSqlFieldsTableUpdateQuery(DataDefinition metaDataIn, String tableNameIn,
                                                                String viewNameIn, List<FieldDef> derivedFieldsIn,
                                                                Map<String, String> columnMapIn) {

        StringBuilder myBuffer = new StringBuilder();

        if ((null != derivedFieldsIn) && !derivedFieldsIn.isEmpty()) {

            Map<String, String> myStaticMap = metaDataIn.getFieldListAccess().getStaticFieldMap();

            myBuffer.append("UPDATE ");
            myBuffer.append(CacheUtil.quote(tableNameIn));

            if (1 < derivedFieldsIn.size()) {

                boolean myFirst = true;

                myBuffer.append(" SET ");
                for (FieldDef myFieldDef : derivedFieldsIn) {

                    if (myFirst) {

                        myFirst = false;
                        myBuffer.append('(');

                    } else {

                        myBuffer.append(", ");
                    }
                    myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));
                }
                myBuffer.append(')');

                myFirst = true;

                myBuffer.append(" = ");
                for (FieldDef myFieldDef : derivedFieldsIn) {

                    if (myFirst) {

                        myFirst = false;
                        myBuffer.append('(');

                    } else {

                        myBuffer.append(", ");
                    }
                    String myExpression = formatExpression(metaDataIn, myFieldDef, myStaticMap, columnMapIn);
                    myBuffer.append((null != myExpression) ? myExpression : "null");
                }
                myBuffer.append(')');

            } else {

                FieldDef myFieldDef = derivedFieldsIn.get(0);

                myBuffer.append(" SET ");
                myBuffer.append(CacheUtil.getQuotedColumnName(myFieldDef));
                myBuffer.append(" = ");

                String myExpression = formatExpression(metaDataIn, myFieldDef, myStaticMap, columnMapIn);
                myBuffer.append((null != myExpression) ? myExpression : "null");
            }
        }
        return myBuffer.toString();
    }

    private static List<FieldDef> filterList(List<FieldDef> listIn, Set<String> columnNamesIn) {

        List<FieldDef> myList = new ArrayList<FieldDef>();

        for (FieldDef myField : listIn) {

            String myColumnName = CacheUtil.getColumnName(myField);

            if (columnNamesIn.contains(myColumnName)) {

                myList.add(myField);
            }
        }
        return myList;
    }

    private static void loadScriptedColumnData(Connection connectionIn, DataDefinition metaIn,
                                               List<ValuePair<String, String>> viewListIn)
            throws CentrifugeException, SQLException {

        FieldListAccess myModel = (null != metaIn) ? metaIn.getFieldListAccess() : null;
        FieldCycleDetector myDependencyCheck = (null != myModel) ? new FieldCycleDetector(myModel) : null;
        List<FieldDef> myList = (null != myDependencyCheck) ? myDependencyCheck.getOrderedDirtyScriptedFieldDefs() : null;

        // Update unstructured JavaScript fields
        if ((null != myList) && !myList.isEmpty()) {

            if ((null != viewListIn) && !viewListIn.isEmpty()) {

                for (ValuePair<String, String> myPair : viewListIn) {

                    updateScriptedFields(connectionIn, myPair.getValue1(), myPair.getValue2(), metaIn, myList);
                }
            }
        }
    }

    public static void retypeDataColumns(Connection connectionIn,
                                         String tableNameIn, List<FieldDef> listIn)
            throws SQLException, CentrifugeException {

        if ((null != listIn) && !listIn.isEmpty()) {

            StringBuilder myBuffer = new StringBuilder();
            CacheConnectionFactory myFactory = new CacheConnectionFactory();
            boolean first = true;

            myBuffer.append("UPDATE ");
            myBuffer.append(CacheUtil.quote(tableNameIn));
            myBuffer.append(" SET (");

            for (FieldDef myField : listIn) {
                String myColumnName = CacheUtil.getColumnName(myField) + "_new";
                String myType = myField.getValueType().getSqlType();

                if (first) {
                   first = false;
                } else {
                   myBuffer.append(", ");
                }
                myBuffer.append(CacheUtil.quote(myColumnName));
                addCacheField(connectionIn, tableNameIn, myColumnName, myType);
            }
            myBuffer.append(") = (");
            first = true;

            for (FieldDef myField : listIn) {
                String myColumnName = CacheUtil.getColumnName(myField);

                if (first) {
                   first = false;
                } else {
                   myBuffer.append(", ");
                }
                myBuffer.append(myFactory.castExpression(CacheUtil.quote(myColumnName), myField.getValueType()));
            }
            myBuffer.append(")");
            QueryHelper.executeSQL(connectionIn, myBuffer.toString(), null);
            connectionIn.commit();
            for (FieldDef myField : listIn) {

                String myColumnName = CacheUtil.getColumnName(myField);

                removeCacheField(connectionIn, tableNameIn, myColumnName);
            }
            for (FieldDef myField : listIn) {

                String myOldColumnName = CacheUtil.getColumnName(myField) + "_new";
                String myNewColumnName = CacheUtil.getColumnName(myField);

                renameCacheField(connectionIn, tableNameIn, myOldColumnName, myNewColumnName);
            }
            connectionIn.commit();
        }
    }

    public static void updateScriptedFields(Connection conn, String tableNameIn, String viewNameIn,
                                            DataDefinition metaIn, List<FieldDef> updateFields)
         throws CentrifugeException, SQLException {

       if ((updateFields != null) && !updateFields.isEmpty()) {
          try (ResultSet rs = DataCacheHelper.getCacheData(viewNameIn, conn, null, false)) {
             CacheRowSet rowSet = new CacheRowSet(metaIn.getFieldListAccess().getFieldDefList(), rs);
             String UPDATE_SQL = "UPDATE %1$s SET %2$s WHERE \"" + CacheTokens.CSI_ROW_ID + "\" = ? ";
             StringBuilder buf = new StringBuilder();
             int i = 0;

             for (FieldDef f : updateFields) {
                // we only support updating scripted fields right now
                if (FieldType.SCRIPTED == f.getFieldType()) {
                   if (i > 0) {
                      buf = buf.append(", ");
                   }
                   buf.append(CacheUtil.getQuotedColumnName(f) + " = ? ");
                   i++;
                }
             }
             String setItems = buf.toString();
             String updateSql = String.format(UPDATE_SQL, CacheUtil.quote(tableNameIn), setItems);
             CsiScriptRunner runner = new EcmaScriptRunner();

             try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                int stmcnt = 0;
                boolean myErrorFlag = false;

                while (rowSet.nextRow()) {
                   stmt.clearParameters();

                   int idx = 1;

                   for (FieldDef changed : updateFields) {
                      if (FieldType.SCRIPTED == changed.getFieldType()) {
                         int paramType = changed.getValueType().getPgType();
                         Object value = null;

                         try {
                            value = runner.evalScriptedField(metaIn.getFieldListAccess(), changed, rowSet);
                         } catch (Exception e) {
                            if (!myErrorFlag) {
                               // just LOG once
                               myErrorFlag = true;
                               LOG.warn("Failed to evaluate scripted field '" + changed.getFieldName() + "'.", e);
                            }
                         }
                         rowSet.update(changed, value);

                         if (value == null) {
                            stmt.setNull(idx, paramType);
                         } else {
                            stmt.setObject(idx, value, paramType);
                         }
                         idx++;
                      }
                   }
                   stmt.setObject(idx, rs.getObject(CacheTokens.CSI_ROW_ID));
                   stmt.addBatch();
                   stmcnt++;

                   if ((stmcnt % CacheCommands.BATCH_INSERT_SIZE) == 0) {
                      stmcnt = 0;
                      stmt.executeBatch();
                      stmt.clearBatch();
                   }
                }
                if (stmcnt > 0) {
                   stmt.executeBatch();
                }
             }
          }
       }
        // conn.commit();
    }

    private static void renameCacheField(Connection connectionIn, String tableNameIn,
                                         String oldColumnNameIn, String newColumnNameIn)
            throws SQLException {

        String mySql = String.format(CacheCommands.RENAME_CACHE_COLUMN, CacheUtil.quote(tableNameIn),
                CacheUtil.quote(oldColumnNameIn), CacheUtil.quote(newColumnNameIn));

        try (Statement statement = connectionIn.createStatement()) {
           statement.executeUpdate(mySql);
        }
        connectionIn.commit();
    }

    private static void addCacheField(Connection connectionIn, String tableNameIn, String columnNameIn, String typeIn)
            throws SQLException {

        String mySql = String.format(CacheCommands.ADD_CACHE_FIELD, CacheUtil.quote(tableNameIn), CacheUtil.quote(columnNameIn), typeIn);

        try (Statement statement = connectionIn.createStatement()) {
           statement.executeUpdate(mySql);
        }
        connectionIn.commit();
    }

    private static void removeCacheField(Connection connectionIn, String tableNameIn, String columnNameIn)
            throws SQLException {
       String mySql = String.format(CacheCommands.DROP_CACHE_FIELD, CacheUtil.quote(tableNameIn), CacheUtil.quote(columnNameIn));

       try (Statement statement = connectionIn.createStatement()) {
          statement.executeUpdate(mySql);
       }
       connectionIn.commit();
    }

    // Format expression for returning data from a view column
    public static String formatExpression(final DataViewDef metaDataIn, FieldDef fieldDefIn, final String prefixIn) {

        String myExpression = "null";
        String myCoercion = CsiDataType.getCoercion(fieldDefIn.getValueType());

        try {

            final Map<String, FieldDef> myMap = metaDataIn.getModelDef().getFieldListAccess().getFieldMapByLocalId();

            SqlTokenTreeItem myTop = fieldDefIn.getSqlExpression().get(0);

            if (0 == myTop.getDataTypeMask()) {

                myTop.setRequiredDataType(fieldDefIn.getValueType().getMask());
            }

            myExpression = myTop.format(new SqlTokenValueCallback() {

                @Override
                public String getFieldDisplayValue(String valueIn) {
                    return null;
                }

                @Override
                public TokenExecutionValue getFieldExecutionValue(String valueIn) {

                    TokenExecutionValue myResult = null;
                    FieldDef myField = myMap.get(valueIn);

					if (FieldType.STATIC == myField.getFieldType()) {

                        myResult = new TokenExecutionValue(myField.getValueType(), true, myField.escapeStaticText());

					} else {

						CsiDataType myDataType = CacheUtil.getColumnType(metaDataIn, myField);

						if (null != myDataType) {

                            myResult = new TokenExecutionValue(myDataType, false,
                                    CacheUtil.getQuotedColumnName(myField, prefixIn));
                        }
                    }
                    return myResult;
                }

                @Override
                public String getParameterDisplayValue(String valueIn) {
                    return null;
                }

                @Override
                public TokenExecutionValue getParameterExecutionValue(String valueIn) {

                    TokenExecutionValue myResult = null;
                    QueryParameterDef myParameter = metaDataIn.getParameterById(valueIn);

                    if (null != myParameter) {

                        myResult = new TokenExecutionValue(myParameter.getType(), true, myParameter.getValue());
                    }
                    return myResult;
                }

            }, true);

            if (null != myCoercion) {

                myExpression = myCoercion + "(" + myExpression + ")";
            }

        } catch (Exception myException) {

        }

        return myExpression;
    }

    private static File createPipeFile() throws CentrifugeException {
        try {
            File pipeFile = null;

            if (unixPlatform) {
                String pipeFileName = UUID.randomUUID();
                ProcessBuilder pb = new ProcessBuilder();
                pb.directory(tempDirectory);
                pb.command("mkfifo", pipeFileName);
                Process process = pb.start();
                Timer timer = new Timer();
                timer.schedule(new ThreadInterrupter(Thread.currentThread()), processWaitTimeout);
                int returnCode = -1;
                try {
                    returnCode = process.waitFor();
                } catch (InterruptedException e) {
                    process.destroy();
                    throw new CentrifugeException("Pipe creator process did not return after " + processWaitTimeout
                            + " milliseconds");
                } finally {
                    timer.cancel();
                }

                if (returnCode != 0) {
                    String stderr = IOLib.readAsString(process.getErrorStream());
                    if (LOG.isDebugEnabled()) {
                        String stdout = IOLib.readAsString(process.getInputStream());
                        if (_doDebug) {
                           LOG.debug("Created a pipe failed with code: " + Integer.toString(returnCode) + " \nErrors: "
                                    + stderr + "\nstdout: " + stdout);
                        }
                    }
                    throw new CentrifugeException("Failure in pipe creator process . Return code:" + returnCode
                            + " Error mesage:" + stderr);
                }

                pipeFile = new File(tempDirectory, pipeFileName);
            } else {
                pipeFile = File.createTempFile("csi", ".dat");
            }
            if (LOG.isDebugEnabled()) {
                if (_doDebug) {
                  LOG.debug("Created pipeFile: " + pipeFile.getCanonicalPath());
               }
            }
            return pipeFile;
        } catch (Throwable t) {
            throw new CentrifugeException(t);
        }
    }

    private static List<FieldDef>
    getSortedFields(List<FieldDef> fullListIn) {

        Map<Integer, FieldDef> myMap = new TreeMap<Integer, FieldDef>();

        for (FieldDef myFieldDef : fullListIn) {

            myMap.put(myFieldDef.getOrdinal(), myFieldDef);
        }
        return new ArrayList(myMap.values());
    }

    private static void restructureCacheTable(Connection connectionIn, DataView dataViewIn,
                                              Map<String, CsiDataType> oldPreCalculatedMapIn,
                                              Map<String, CsiDataType> newPreCalculatedMapIn,
                                              Map<String, CsiDataType> retypeMapIn)
            throws CentrifugeException, SQLException {

        List<ValuePair<String, String>> myViews;
        String[] myTables = dataViewIn.getTableList();
        String[] myLinkups = dataViewIn.getLinkupList();
        DataViewDef myMetaData = dataViewIn.getMeta();

        definePreCalculatedStorageTypes(myMetaData.getFieldListAccess());
        removeDiscardedColumns(connectionIn, myTables, myLinkups, oldPreCalculatedMapIn, newPreCalculatedMapIn);
        addNewColumns(connectionIn, myTables, myLinkups, oldPreCalculatedMapIn, newPreCalculatedMapIn);
        replaceRetypedColumnData(connectionIn, dataViewIn, myTables, myLinkups);
        myViews = genTempViews(connectionIn, dataViewIn);
        loadScriptedColumnData(connectionIn, myMetaData, myViews);
        loadDerivedColumnData(connectionIn, myMetaData, myViews);
        for (ValuePair<String, String> myView : myViews) {

            dropCacheView(connectionIn, myView.getValue2());
        }
    }

    private static void convertBaseViewToBaseTable(Connection connectionIn, DataView dataViewIn)
            throws Exception{

        String myTableName = CacheUtil.getCacheTableName(dataViewIn.getUuid(), 0);
        String[] myTableInfo = dataViewIn.clearInstalledTables();
        String myViewQuery = myTableInfo[1];

        dataViewIn.removeView(myTableName);

        try {

            String[] myLevelOne = SQL_FROM_PATTERN.split(myViewQuery);
            String[] myLevelTwo = SQL_COLUMN_SEPARATOR_PATTERN.split(myLevelOne[0]);
            String myDataTableName = myLevelOne[1];
            Map<String, String> myColumnDataTypeMap = getQuotedColumnMap(connectionIn, myDataTableName);
            String mySequenceCommand = buildCreateSequenceCommand(myTableName, 1L);
            String myCreateCommand = null;
            String myLoadCommand = null;
            StringBuilder myCreateBuffer = beginCreateTableRequest(myTableName, CacheTokens.CSI_ROW_ID, null, null, null);
            StringBuilder myLoadBuffer = new StringBuilder();
            StringBuilder myLoadBufferTwo = new StringBuilder();

            myLoadBuffer.append("INSERT INTO ");
            myLoadBuffer.append(CacheUtil.quote(myTableName));
            myLoadBuffer.append("(\"" + CacheTokens.CSI_ROW_ID + "\",");
            myLoadBufferTwo.append("\"" + CacheTokens.CSI_ROW_ID + "\",");

            for (int i = 1; myLevelTwo.length > i; i++) {

                String[] myFieldPair = SQL_AS_PATTERN.split(myLevelTwo[i]);

                myCreateBuffer.append(myFieldPair[1]);
                myCreateBuffer.append(" ");
                myCreateBuffer.append(myColumnDataTypeMap.get(myFieldPair[0]));
                myCreateBuffer.append(", ");

                myLoadBuffer.append(myFieldPair[1]);
                myLoadBuffer.append(", ");

                myLoadBufferTwo.append(myFieldPair[0]);
                myLoadBufferTwo.append(", ");
            }
            myCreateBuffer.append("PRIMARY KEY (");
            myCreateBuffer.append(CacheTokens.CSI_ROW_ID);
            myCreateBuffer.append("))");
            myLoadBuffer.setLength(myLoadBuffer.length() - 2);
            myLoadBuffer.append(") SELECT ");
            myLoadBuffer.append(myLoadBufferTwo);
            myLoadBuffer.setLength(myLoadBuffer.length() - 2);
            myLoadBuffer.append(" FROM ");
            myLoadBuffer.append(myDataTableName);
            myCreateCommand = myCreateBuffer.toString();
            myLoadCommand = myLoadBuffer.toString();

            // Remove original source view
            dropCacheView(connectionIn, myTableName, true);
            dataViewIn.removeView(myTableName);

            // Create and load an original source table
            LOG.debug("CREATE SEQUENCE: " + mySequenceCommand);
            QueryHelper.executeSQL(connectionIn, mySequenceCommand, null);
            LOG.debug("CREATE DATA TABLE: " + myCreateCommand);
            QueryHelper.executeSQL(connectionIn, myCreateCommand, null);
            connectionIn.commit();
            LOG.debug("LOAD DATA TABLE: " + myLoadCommand);
            QueryHelper.executeSQL(connectionIn, myLoadCommand, null);

            dataViewIn.addTable(myTableName);
            dataViewIn.setTopView(false);

            // Release lock on current installed table version
            SharedDataSourceHelper.releaseInstalledTables(myTableInfo);

        } catch (Exception myException) {

            StringBuilder myBuffer = new StringBuilder();

//            myBuffer.append("CREATE OR REPLACE VIEW ");
//            myBuffer.append(CacheUtil.quote(myTableName));
//            myBuffer.append(" AS ");
            myBuffer.append(myViewQuery);

            LOG.error("Caught exception converting basic data view into a basic data table.", myException);
            if (_doDebug) {

                LOG.debug("\n\nCreate a view of the installed table using the following command . . ."
                        + "\n[" + myBuffer.toString() + "]\n");
            }
            QueryHelper.executeSQL(connectionIn, myBuffer.toString(), null);
            dataViewIn.addView(myTableName);
            dataViewIn.setInstalledTables(myTableInfo);

            throw myException;

        } finally {

            try {

                connectionIn.commit();

            } catch (Exception IGNORE) {}
        }
    }

    private static void removeDiscardedColumns(Connection connectionIn, String[] tableListIn, String[] linkupListIn,
                                               Map<String, CsiDataType> oldPreCalculatedMapIn,
                                               Map<String, CsiDataType> newPreCalculatedMapIn) throws SQLException {
/*
        if ((null != tableListIn) && (null != oldPreCalculatedMapIn) && (null != newPreCalculatedMapIn)) {

            List<String> myRemovalList = new ArrayList<String>();

            for (Map.Entry<String, CsiDataType> myOldEntry : oldPreCalculatedMapIn.entrySet()) {

                String myColumnId = myOldEntry.getKey();
                CsiDataType myOldDataType = myOldEntry.getValue();

                if (newPreCalculatedMapIn.containsKey(myColumnId)) {

                    CsiDataType myNewDataType = newPreCalculatedMapIn.get(myColumnId);

                    if (!myOldDataType.equals(myNewDataType)) {

                        myRemovalList.add(myColumnId);
                    }

                } else {

                    myRemovalList.add(myColumnId);
                }
            }
*/
        if ((null != tableListIn) && (null != oldPreCalculatedMapIn)) {

            List<String> myRemovalList = new ArrayList<String>();

            for (Map.Entry<String, CsiDataType> myOldEntry : oldPreCalculatedMapIn.entrySet()) {

                myRemovalList.add(myOldEntry.getKey());
            }
            if (!myRemovalList.isEmpty()) {
               for (int i = 0; i < tableListIn.length; i++) {
                  String myTableName = tableListIn[i];

                  for (String myEntry : myRemovalList) {
                     String myColumnName = CacheUtil.toDbUuid(myEntry);
                     removeCacheField(connectionIn, myTableName, myColumnName);
                  }
               }

                if (null != linkupListIn) {

                    for (int i = 0; i < linkupListIn.length; i++) {

                        String myTableName = linkupListIn[i];

                        for (String myEntry : myRemovalList) {

                            String myColumnName = CacheUtil.toDbUuid(myEntry);

                            removeCacheField(connectionIn, myTableName, myColumnName);
                        }
                    }
                }
            }
        }
    }

    private static void addNewColumns(Connection connectionIn, String[] tableListIn, String[] linkupListIn,
                                      Map<String, CsiDataType> oldPreCalculatedMapIn,
                                      Map<String, CsiDataType> newPreCalculatedMapIn) throws SQLException {
/*
        if ((null != tableListIn) && (null != oldPreCalculatedMapIn) && (null != newPreCalculatedMapIn)) {

            List<ValuePair<String, CsiDataType>> myNewColumnList = new ArrayList<ValuePair<String, CsiDataType>>();

            for (Map.Entry<String, CsiDataType> myNewEntry : newPreCalculatedMapIn.entrySet()) {

                String myColumnId = myNewEntry.getKey();
                CsiDataType myNewDataType = myNewEntry.getValue();

                if (oldPreCalculatedMapIn.containsKey(myColumnId)) {

                    CsiDataType myOldDataType = oldPreCalculatedMapIn.get(myColumnId);

                    if (!myNewDataType.equals(myOldDataType)) {

                        myNewColumnList.add(new ValuePair<String, CsiDataType>(myColumnId, myNewDataType));
                    }

                } else {

                    myNewColumnList.add(new ValuePair<String, CsiDataType>(myColumnId, myNewDataType));
                }
            }
*/
        if ((null != tableListIn) && (null != oldPreCalculatedMapIn) && (null != newPreCalculatedMapIn)) {

            List<ValuePair<String, CsiDataType>> myNewColumnList = new ArrayList<ValuePair<String, CsiDataType>>();

            for (Map.Entry<String, CsiDataType> myNewEntry : newPreCalculatedMapIn.entrySet()) {

                String myColumnId = myNewEntry.getKey();
                CsiDataType myNewDataType = myNewEntry.getValue();

                myNewColumnList.add(new ValuePair<String, CsiDataType>(myColumnId, myNewDataType));
            }
            if (!myNewColumnList.isEmpty()) {
               for (int i = 0; i < tableListIn.length; i++) {
                  String myTableName = tableListIn[i];

                  addNewColumns(connectionIn, myTableName, myNewColumnList);
               }

                if (null != linkupListIn) {

                    for (int i = 0; i < linkupListIn.length; i++) {

                        String myTableName = linkupListIn[i];

                        addNewColumns(connectionIn, myTableName, myNewColumnList);
                    }
                }
            }
        }
    }

    private static void addNewColumns(Connection connectionIn, String tableNameIn,
                                      Map<String, CsiDataType> preCalculatedMapIn) throws SQLException {

        if ((null != tableNameIn) && (null != preCalculatedMapIn)) {

            List<ValuePair<String, CsiDataType>> myNewColumnList = new ArrayList<ValuePair<String, CsiDataType>>();

            for (Map.Entry<String, CsiDataType> myEntry : preCalculatedMapIn.entrySet()) {

                String myColumnId = myEntry.getKey();
                CsiDataType myDataType = myEntry.getValue();


                myNewColumnList.add(new ValuePair<String, CsiDataType>(myColumnId, myDataType));
            }

            addNewColumns(connectionIn, tableNameIn, myNewColumnList);
        }
    }

    private static void addNewColumns(Connection connectionIn, String tableNameIn,
                                      List<ValuePair<String, CsiDataType>> newColumnListIn) throws SQLException {


        if ((null != tableNameIn) && (null != newColumnListIn) && !newColumnListIn.isEmpty()) {

            for (ValuePair<String, CsiDataType> myEntry : newColumnListIn) {

                String myColumnName = CacheUtil.toDbUuid(myEntry.getValue1());
                String myColumnType = myEntry.getValue2().getSqlType();

                addCacheField(connectionIn, tableNameIn, myColumnName, myColumnType);
            }
            connectionIn.commit();
        }
    }

    // Format expression for filling a table column
    private static String formatExpression(final DataDefinition metaDataIn, FieldDef fieldDefIn,
                                           final Map<String, String> staticMapIn,
                                           final Map<String, String> columnMapIn) {

        String myCoercion = CsiDataType.getCoercion(fieldDefIn.tryStorageType());
        String myExpression = "null";

        try {

            final Map<String, FieldDef> myMap = metaDataIn.getFieldListAccess().getFieldMapByLocalId();
            SqlTokenTreeItem myTop = fieldDefIn.getSqlExpression().get(0);
//            CsiDataType mytokenDataType = myTop.getToken().getType();

            if (0 == myTop.getDataTypeMask()) {

                myTop.setRequiredDataType(fieldDefIn.tryStorageType().getMask());
            }

            myExpression = myTop.format(new SqlTokenValueCallback() {

                @Override
                public String getFieldDisplayValue(String valueIn) {
                    return null;
                }

                @Override
                public TokenExecutionValue getFieldExecutionValue(String valueIn) {

                    TokenExecutionValue myResult = null;
                    FieldDef myField = myMap.get(valueIn);

                    if (null != myField) {

                        ColumnDef myColumnDef = (null != myField.getColumnLocalId())
                                ? metaDataIn.getColumnByKey(myField.getColumnKey())
                                : null;

							if (FieldType.STATIC == myField.getFieldType()) {

							    myResult = new TokenExecutionValue(myField.getValueType(), true, myField.escapeStaticText());

                            } else {

							CsiDataType myDataType = (null != myColumnDef) ? myColumnDef.getCsiType() : myField.tryStorageType();

							if (null != myDataType) {

                                String myColumn = (null != columnMapIn) ? columnMapIn.get(valueIn) : null;

                                myResult = new TokenExecutionValue(myDataType, false, CacheUtil.quote(CacheUtil.toDbUuid(myColumn)));
                            }
                        }
                    }
                    return myResult;
                }

                @Override
                public String getParameterDisplayValue(String valueIn) {
                    return null;
                }

                @Override
                public TokenExecutionValue getParameterExecutionValue(String valueIn) {

                    TokenExecutionValue myResult = null;
                    QueryParameterDef myParameter = metaDataIn.getParameterListAccess().getParameterById(valueIn);

                    if (null != myParameter) {


                        myResult = new TokenExecutionValue(myParameter.getType(), true, myParameter.getValue());
                    }
                    return myResult;
                }

            }, true);

            if (null != myCoercion) {

                myExpression = myCoercion + "(" + myExpression + ")";
            }

        } catch (Exception myException) {

        }

        return myExpression;
    }

    public static String formatConstant(FieldDef fieldDefIn) {

        String myValue = "null";

        switch (fieldDefIn.getValueType()) {

            case Integer :
            case Number :

                myValue = fieldDefIn.getStaticText();
                break;

            case String :
            case Boolean :
            case DateTime :
            case Date :
            case Time :

                myValue = "'" + fieldDefIn.escapeStaticText() + "'";
                break;
        }
        return myValue;
    }
}
