package csi.server.business.helper;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.business.cachedb.AbstractInlineProducer;
import csi.server.business.cachedb.CsvEncodingProducer;
import csi.server.business.cachedb.CsvProducer;
import csi.server.business.cachedb.NewExcelProducer;
import csi.server.business.helper.linkup.ParameterSetFactory;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.ResponseArgument;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.dto.installed_tables.CsvParameters;
import csi.server.common.dto.installed_tables.NewExcelInstallRequest;
import csi.server.common.dto.installed_tables.NewExcelParameters;
import csi.server.common.dto.installed_tables.NonBinaryInstallRequest;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.dto.installed_tables.TableParameters;
import csi.server.common.dto.installed_tables.TxtParameters;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.enumerations.ServerResponse;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.DataWrapper;
import csi.server.common.model.FieldDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;
import csi.server.common.util.uploader.zip.CsiZipEntry;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;
import csi.server.util.uploader.CsiFileInputStream;
import csi.server.util.uploader.CsiServerZipScanner;
import csi.server.util.uploader.XlsxReader;

/**
 * Created by centrifuge on 7/7/2017.
 */
public class SharedDataSourceHelper {
   private static final Logger LOG = LogManager.getLogger(SharedDataSourceHelper.class);

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final Integer _COMMA = (int) ',';
    private static final Integer _DOUBLE_QUOTE = (int) '"';

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Public Methods                                      //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

   public static void registerInstalledTableVersion(InstalledTable tableIn, String nameIn) {
      if ((tableIn != null) && (nameIn != null)) {
         int myOldRevision = Math.max(0, tableIn.getRevision());
         int myNewRevision = tableIn.registerInstalledTableVersion(nameIn);
         String myTableName = tableIn.getTableName(myOldRevision);

         if ((myNewRevision > myOldRevision) && (tableIn.revisionUseCount(myOldRevision) == 0)) {
            try (Connection myCacheConnection = CsiPersistenceManager.getCacheConnection()) {
               try {
                  DataCacheBuilder.dropCacheTable(myCacheConnection, myTableName);
               } catch (Exception myException) {
                  LOG.info("Failed discarding installed table revison table " + Format.value(myTableName)
                  + " data, revision " + Integer.toString(myOldRevision));
                  SqlUtil.quietRollback(myCacheConnection);
               }
            } catch (SQLException e) {
               LOG.info("Failed discarding installed table revison table " + Format.value(myTableName)
               + " data, revision " + Integer.toString(myOldRevision));
            } catch (CentrifugeException e) {
               LOG.info("Failed discarding installed table revison table " + Format.value(myTableName)
               + " data, revision " + Integer.toString(myOldRevision));
            }
         }
      }
   }

    public static String lockInstalledTable(InstalledTable tableIn) {

        String myNewLock = null;

        if (null != tableIn) {

            myNewLock = tableIn.lockRevision();
        }
        return myNewLock;
    }

    public static String incrementLock(String lockIn) {

        String myLock = null;

        if (null != lockIn) {

            String[] myLockPair = StringUtil.split(lockIn, ':');
            InstalledTable myTable = CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class, myLockPair[0]);

            myLock = myTable.incrementLock(lockIn);
        }
        return myLock;
    }

    public static void releaseInstalledTable(String lockIn) {

        if (null != lockIn) {

            String[] myLockPair = StringUtil.split(lockIn, ':');

            delete(myLockPair[0], Integer.parseInt(myLockPair[1]));
        }
    }

    public static void releaseInstalledTables(String[] installedListIn) {

        if (null != installedListIn) {

           LOG.debug("releaseInstalledTables( )");
            for (int i = 0; installedListIn.length > i; i += 2) {

                releaseInstalledTable(installedListIn[i]);
            }
        }
    }

   public static boolean delete(InstalledTable tableIn) {
      boolean mySuccess = false;

      LOG.debug("delete(" + Format.value(tableIn) + ")");

      try {
         tableIn.releaseOwnership();

         LOG.info("Delete view for installed table " + Format.value(tableIn.getTablePath()));

         try (Connection myCacheConnection = CsiPersistenceManager.getCacheConnection()) {
            try {
               DataCacheBuilder.dropCacheView(myCacheConnection, tableIn.getName());
            } catch (Exception myException) {
               LOG.error("Caught exception removing installed table view " + Format.value(tableIn.getName()));

               if (myCacheConnection != null) {
                  SqlUtil.quietRollback(myCacheConnection);
               }
            }
         }
         mySuccess = discardIfUnused(tableIn);
      } catch (Exception myException) {
         LOG.error("Caught exception deleting installed table " + Format.value(tableIn.getTablePath()));
         CsiPersistenceManager.rollback();
         CsiPersistenceManager.begin();
      }
      return mySuccess;
   }

    public static void delete(String uuid, int revisionIn) {

       LOG.debug("delete(" + Format.value(uuid) + ", " + Format.value(revisionIn) + ")");
        delete(CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class, uuid), revisionIn);
    }

   public static boolean delete(InstalledTable tableIn, int revisionIn) {
      LOG.debug("delete(" + Format.value(tableIn) + ", " + Format.value(revisionIn) + ")");

      boolean mySuccess = false;

      if (tableIn != null) {
         if (tableIn.releaseRevision(revisionIn) == 0) {
            if (tableIn.readyToDiscard() || (tableIn.getRevision() != revisionIn)) {
               LOG.info("Discard installed table " + Format.value(tableIn.getTablePath())
                        + " data, revision " + Integer.toString(revisionIn));

               try (Connection myCacheConnection = CsiPersistenceManager.getCacheConnection()) {
                  try {
                     DataCacheBuilder.dropCacheTable(myCacheConnection, tableIn.getTableName(revisionIn));
                  } catch (Exception myException) {
                     LOG.info("Failed discarding installed table " + Format.value(tableIn.getTablePath())
                              + " data, revision " + Integer.toString(revisionIn));
                     SqlUtil.quietRollback(myCacheConnection);
                  }
               } catch (SQLException sqle) {
                  LOG.info("Failed discarding installed table " + Format.value(tableIn.getTablePath())
                  + " data, revision " + Integer.toString(revisionIn));
               } catch (CentrifugeException ce) {
                  LOG.info("Failed discarding installed table " + Format.value(tableIn.getTablePath())
                  + " data, revision " + Integer.toString(revisionIn));
               }
            }
         }
         mySuccess = discardIfUnused(tableIn);
      }
      return mySuccess;
   }

   public static Response<String, ValuePair<Boolean, InstalledTable>> updateInstalledTable(InstalledTable tableIn,
                                                                                           List<AuthDO> credentialsIn,
                                                                                           List<LaunchParam> parametersIn)
          throws CentrifugeException, GeneralSecurityException {
      long myRowCount = 0L;
      Boolean myOverFlow = Boolean.FALSE;
      AdHocDataSource mySource = (tableIn == null) ? null : tableIn.getSourceDefinition();

       if (mySource != null) {
          ValuePair<Long, Boolean> myResults =
             DataCacheBuilder.initializeCache(tableIn, null,
                                              new ParameterSetFactory(mySource.getDataSetParameters(), parametersIn),
                                              credentialsIn, null, null, 0);

          CsiPersistenceManager.merge(tableIn);

          myRowCount = myResults.getValue1().longValue();
          myOverFlow = myResults.getValue2();

          try {
             SecurityHelper.applyInstalledTableSecurity(tableIn);
          } catch (Exception myException) {
             LOG.error("Caught exception applying security to installed table, "
                   + Format.value((tableIn == null) ? null : tableIn.getTablePath()) +
                   "\nApplying fall-back security.\n",
                   myException);
             SecurityHelper.invokeFallBackSecurity(tableIn);
          }
          if (tableIn != null) {
             tableIn.setSize(myRowCount);
             CsiPersistenceManager.merge(tableIn);
          }
       }
       return new Response<String, ValuePair<Boolean, InstalledTable>>((tableIn == null) ? null : tableIn.getUuid(),
             new ValuePair<Boolean, InstalledTable>(Boolean.TRUE, tableIn), myRowCount, myOverFlow.booleanValue());
   }

    public static Response<String, InstalledTable> createInstalledTable(String requestIdIn, String nameIn,
                                                                        String remarksIn, AdHocDataSource dataSourceIn,
                                                                        List<LaunchParam> parametersIn,
                                                                        List<AuthDO> credentialsIn,
                                                                        CapcoInfo capcoInfoIn,
                                                                        SecurityTagsInfo tagInfoIn, boolean overwriteIn,
                                                                        SharingInitializationRequest sharingRequestIn) {

        String myTableName = CacheUtil.generateInstalledTableName();
        String myUserName = CsiSecurityManager.getUserName();
        String myExposedName = InstalledTable.createTablePath(nameIn, myUserName, "dse");

        if (AclRequest.checkConflictAvoidingSecurity(myExposedName, AclResourceType.DATA_TABLE)) {

            return new Response<String, InstalledTable>(requestIdIn, ServerMessage.INSTALLED_TABLE_EXISTS, new ResponseArgument(myExposedName));

        } else {

            try {

                InstalledTable myTable = new InstalledTable(nameIn, myTableName, remarksIn,
                                                            dataSourceIn, capcoInfoIn, tagInfoIn, myUserName);

                ValuePair<Long, Boolean> myResults = DataCacheBuilder.initializeCache(myTable, null,
                                                            new ParameterSetFactory(dataSourceIn.getDataSetParameters(),
                                                                                    parametersIn),
                                                            credentialsIn, null, null, 0);
                CsiPersistenceManager.persist(myTable);
                try {

                    SecurityHelper.applyInstalledTableSecurity(myTable);

                } catch (Exception myException) {

                   LOG.error("Caught exception applying security to installed table, "
                                    + Format.value(myTable.getTablePath()) + "\nApplying fall-back security.\n",
                            myException);
                    SecurityHelper.invokeFallBackSecurity(myTable);
                }
                CsiPersistenceManager.merge(myTable);
                return new Response<String, InstalledTable>(requestIdIn, myTable, myResults.getValue1(), myResults.getValue2());

            } catch (Exception myException) {

                return new Response<String, InstalledTable>(requestIdIn, ServerMessage.CAUGHT_EXCEPTION,
                        Format.value(myException));
            }
        }
    }

   public static Response<Integer, TableInstallResponse> updateExcelFile(int handleIn, NewExcelInstallRequest requestIn) {
      try {
         String myTableId = requestIn.getTableId();
         InstalledTable myTable = CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class,
                                                                                   myTableId, AclControlType.EDIT);
         myTable = loadExcelData(myTable, requestIn);

         CsiPersistenceManager.merge(myTable);

         try {
            SecurityHelper.applyInstalledTableSecurity(myTable);
         } catch (Exception myException) {
            LOG.error("Caught exception applying security to installed table, "
                  + Format.value((myTable == null) ? "" : myTable.getTablePath()) + "\nApplying fall-back security.\n",
                  myException);
            SecurityHelper.invokeFallBackSecurity(myTable);
         }
         CsiPersistenceManager.merge(myTable);
         return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn), new TableInstallResponse(myTable));
      } catch (Exception myException) {
         return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn),
               new TableInstallResponse(ServerResponse.FAILED, "Caught exception installing Excel file!"),
               ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
      }
   }

    public static Response<Integer, TableInstallResponse> installExcelFile(int handleIn,
                                                                           TableInstallRequest installBlockIn,
                                                                           String tableNameIn) {

       LOG.debug("Install Excel file " + Format.value(installBlockIn.getFileName()));
        NewExcelInstallRequest myInstallRequest = (NewExcelInstallRequest) installBlockIn;
        Path myPath = Paths.get(FileHelper.buildUserFilePath(myInstallRequest.getFileName()));
        CsiServerZipScanner myZipStream = null;

        try (CsiFileInputStream myFileStream = new CsiFileInputStream(myPath)) {
//            FileOutputStream myDebugFile = getOutputFile("DEBUG", false);
            FileOutputStream myDebugFile = null;

            myZipStream = new CsiServerZipScanner(myFileStream, new Inflater(true));

            XlsxReader myReader = new XlsxReader(myZipStream, myInstallRequest.getStrings());
            NewExcelParameters myParameters = (NewExcelParameters) myInstallRequest.getTableParameters();
            ColumnParameters[] myColumns = myParameters.getColumnParameterArray();
            InstalledTable myTable = createTable(installBlockIn, tableNameIn);
            CsiZipEntry myEntry = myParameters.getEntry();
            int myDataStart = myParameters.getDataStart();

            if ((null != myColumns) && (0 < myColumns.length)) {

                AbstractInlineProducer myProducer = new NewExcelProducer(TaskHelper.getCurrentContext().getTaskId(),
                        myReader, myEntry, installBlockIn.getRowLimit(), myColumns, (myDataStart - 1), myDebugFile);
                long myTableRowCount = (myTable == null)
                                          ? 0L
                                          : DataCacheBuilder.createTableFromUpload(myTable, myTable.getColumns(), myProducer,
                                                            myInstallRequest.getFileType(), CsiEncoding.UTF_8, _COMMA,
                                                            _DOUBLE_QUOTE, _DOUBLE_QUOTE, null);

                DataCacheBuilder.createViewForInstalledTable(myTable);
                CsiPersistenceManager.persist(myTable);
                try {

                    SecurityHelper.applyInstalledTableSecurity(myTable);

                } catch (Exception myException) {

                   LOG.error("Caught exception applying security to installed table, "
                                    + Format.value(myTable.getTablePath()) + "\nApplying fall-back security.\n",
                            myException);
                    SecurityHelper.invokeFallBackSecurity(myTable);
                }
//                CsiPersistenceManager.merge(generateDataSource(myTable, installBlockIn));
                CsiPersistenceManager.merge(myTable);
                return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn), new TableInstallResponse(myTable));

            } else {

                return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn),
                        new TableInstallResponse(ServerResponse.FAILED, "Table had no data!"));
            }

        } catch (Exception myException) {

            return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn),
                    new TableInstallResponse(ServerResponse.FAILED, "Caught exception installing Excel file!"),
                    ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));

        } finally {

            if (null != myZipStream) {

                try {

                    myZipStream.close();

                } catch (Exception myException) {

                    // IGNORE!!
                }
            }
        }
    }

    public static Response<Integer, TableInstallResponse> updateCsvFile(int handleIn, NonBinaryInstallRequest requestIn) {

        try {

            String myTableId = requestIn.getTableId();
            InstalledTable myTable = CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class,
                    myTableId, AclControlType.EDIT);

            myTable = loadCsvData(myTable, requestIn);
            CsiPersistenceManager.merge(myTable);
            try {

                SecurityHelper.applyInstalledTableSecurity(myTable);

            } catch (Exception myException) {

               LOG.error("Caught exception applying security to installed table, "
                                + Format.value((myTable == null) ? "" : myTable.getTablePath()) +
                                "\nApplying fall-back security.\n",
                        myException);
                SecurityHelper.invokeFallBackSecurity(myTable);
            }
            CsiPersistenceManager.merge(myTable);
            return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn), new TableInstallResponse(myTable));

        } catch (Exception myException) {

            return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn),
                    new TableInstallResponse(ServerResponse.FAILED, "Caught exception installing Excel file!"),
                    ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    public static Response<Integer, TableInstallResponse> installCsvFile(int handleIn,
                                                                         TableInstallRequest installBlockIn,
                                                                         String tableNameIn) {

       LOG.debug("Install text/csv file " + Format.value(installBlockIn.getFileName()));
        NonBinaryInstallRequest myInstallRequest = (NonBinaryInstallRequest) installBlockIn;
        TableParameters myParameters = myInstallRequest.getTableParameters();
        ColumnParameters[] myColumns = myParameters.getColumnParameterArray();
        InstalledTable myTable = createTable(installBlockIn, tableNameIn);
        CsiFileType myFileType = myInstallRequest.getFileType();
        Path myPath = Paths.get(FileHelper.buildUserFilePath(myInstallRequest.getFileName()));

        try (CsiFileInputStream myFileStream = new CsiFileInputStream(myPath, myParameters.getFixNulls())) {
            if ((null != myColumns) && (0 < myColumns.length)) {

                Integer myDelimiter = null;
                Integer myQuote = null;
                Integer myEscape = null;
                int myDataStart = 0;
                String myNullValue = null;
                CsiEncoding myFinalEncoding = myInstallRequest.getEncoding();
                CsiEncoding myUnsupportedEncoding = null;
                AbstractInlineProducer myProducer = null;
                boolean myBackSlashFlag = false;

                if (CsiFileType.CSV == myFileType) {

                    myDelimiter = ((CsvParameters) myParameters).getDelimiter();
                    myDataStart = ((CsvParameters) myParameters).getDataStart();
                    myNullValue = ((CsvParameters) myParameters).getNullIndicator();
                    myQuote = ((CsvParameters) myParameters).getQuote();
                    myEscape = ((CsvParameters) myParameters).getEscape();
                    myBackSlashFlag = false;
                    if (null == myQuote) {

                        myQuote = _DOUBLE_QUOTE;
                    }

                } else if (CsiFileType.TEXT == myFileType) {

                    myDelimiter = ((TxtParameters) myParameters).getDelimiter();
                    myDataStart = ((TxtParameters) myParameters).getDataStart();
                    myNullValue = ((TxtParameters) myParameters).getNullIndicator();
                    myBackSlashFlag = true;
                }

                // Identify the appropriate producer
                //
                if ((CsiEncoding.UTF_16LE == myFinalEncoding) || (CsiEncoding.UTF_16BE == myFinalEncoding)) {

                    myUnsupportedEncoding = myFinalEncoding;
                    myFinalEncoding = CsiEncoding.UTF_8;
                    myProducer = new CsvEncodingProducer(TaskHelper.getCurrentContext().getTaskId(), myFileStream,
                            myUnsupportedEncoding, myDelimiter, myQuote, myEscape,
                            myNullValue, myColumns, myDataStart - 1, installBlockIn.getRowLimit(),
                            myBackSlashFlag, false);

                } else {

                    myProducer = new CsvProducer(TaskHelper.getCurrentContext().getTaskId(), myFileStream, myDelimiter,
                            myQuote, myEscape, myNullValue, myColumns, myDataStart - 1, installBlockIn.getRowLimit(),
                            myBackSlashFlag, false);
                }
                if (myTable != null) {
                   myTable.setSize(DataCacheBuilder.createTableFromUpload(myTable, myTable.getColumns(), myProducer,
                                                                          myInstallRequest.getFileType(), myFinalEncoding,
                                                                          myDelimiter, myQuote, myEscape, myNullValue));
                }
                DataCacheBuilder.createViewForInstalledTable(myTable);
                CsiPersistenceManager.persist(myTable);
                try {

                    SecurityHelper.applyInstalledTableSecurity(myTable);

                } catch (Exception myException) {

                   LOG.error("Caught exception applying security to installed table, "
                                    + Format.value((myTable == null) ? "" : myTable.getTablePath()) + "\nApplying fall-back security.\n",
                            myException);
                    SecurityHelper.invokeFallBackSecurity(myTable);
                }
//                CsiPersistenceManager.merge(generateDataSource(myTable, installBlockIn));
                CsiPersistenceManager.merge(myTable);
                return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn), new TableInstallResponse(myTable));

            } else {

                return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn),
                        new TableInstallResponse(ServerResponse.FAILED, "Table had no data!"),
                        ServerMessage.FILE_UPLOAD_ERROR, "Problem with column definitions!");
            }

        } catch (Exception myException) {

            return new Response<Integer, TableInstallResponse>(Integer.valueOf(handleIn),
                    new TableInstallResponse(ServerResponse.FAILED, "Caught exception installing CSV file!"),
                    ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    public static Response<String, TableInstallResponse> createTable(DataView dataViewIn, Selection selectionIn,
                                                                      VisualizationDef visualizationIn, String nameIn,
                                                                      String remarksIn, List<FieldDef> fieldListIn) {

        String myTableName = CacheUtil.generateInstalledTableName();
        String myUserName = CsiSecurityManager.getUserName();
        String myExposedName = InstalledTable.createTablePath(nameIn, myUserName, CsiFileType.DATAVIEW.getExtension());
        DataViewDef myMeta = dataViewIn.getMeta();

        if (AclRequest.checkConflictAvoidingSecurity(myExposedName, AclResourceType.DATA_TABLE)) {

            return new Response<String, TableInstallResponse>(dataViewIn.getUuid(),
                    new TableInstallResponse(ServerResponse.FAILED,
                            "Table exists!"),
                    ServerMessage.INSTALLED_TABLE_EXISTS, "");

        } else {

            try {

                InstalledTable myTable = new InstalledTable(nameIn, dataViewIn, visualizationIn.getName(),
                                                            myTableName, remarksIn, fieldListIn, myMeta.getCapcoInfo(),
                                                            myMeta.getSecurityTagsInfo(), myUserName);

                Set<Integer> mySelectionIds = DataViewHelper.prepareSelection(dataViewIn, visualizationIn, selectionIn);
                myTable.setSize(DataCacheBuilder.createCaptureCache(myTable, dataViewIn.getUuid(),
                                                                        mySelectionIds, myTable.getColumnMapping()));
                DataCacheBuilder.createViewForInstalledTable(myTable);
                CsiPersistenceManager.persist(myTable);
                try {

                    SecurityHelper.applyInstalledTableSecurity(myTable);

                } catch (Exception myException) {

                   LOG.error("Caught exception applying security to installed table, "
                                    + Format.value(myTable.getTablePath()) + "\nApplying fall-back security.\n",
                            myException);
                    SecurityHelper.invokeFallBackSecurity(myTable);
                }
                CsiPersistenceManager.merge(myTable);
                return new Response<String, TableInstallResponse>(dataViewIn.getUuid(), new TableInstallResponse(myTable));

            } catch (Exception myException) {

                return new Response<String, TableInstallResponse>(dataViewIn.getUuid(), ServerMessage.CAUGHT_EXCEPTION,
                        Format.value(myException));
            }
        }
    }

    public static Response<String, TableInstallResponse> updateTable(DataView dataViewIn,
                                                                     List<ValuePair<InstalledColumn, FieldDef>> pairedListIn,
                                                                     Selection selectionIn, String uuidIn,
                                                                     VisualizationDef visualizationIn) {
        try {
            InstalledTable myTable = CsiPersistenceManager.findObject(InstalledTable.class, uuidIn);
            Set<Integer> mySelectionIds = DataViewHelper.prepareSelection(dataViewIn, visualizationIn, selectionIn);
            myTable.setSize(DataCacheBuilder.createCaptureCache(myTable, dataViewIn.getUuid(), mySelectionIds, pairedListIn));
            DataCacheBuilder.createViewForInstalledTable(myTable);
            CsiPersistenceManager.persist(myTable);
            try {

                SecurityHelper.applyInstalledTableSecurity(myTable);

            } catch (Exception myException) {

               LOG.error("Caught exception applying security to installed table, "
                                + Format.value(myTable.getTablePath()) + "\nApplying fall-back security.\n",
                        myException);
                SecurityHelper.invokeFallBackSecurity(myTable);
            }
            CsiPersistenceManager.merge(myTable);
            return new Response<String, TableInstallResponse>(dataViewIn.getUuid(), new TableInstallResponse(myTable));

        } catch (Exception myException) {

            return new Response<String, TableInstallResponse>(dataViewIn.getUuid(), ServerMessage.CAUGHT_EXCEPTION,
                    Format.value(myException));
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static InstalledTable createTable(TableInstallRequest requestIn, String tableNameIn) {

        InstalledTable myTable = null;
        TableParameters myParameters = requestIn.getTableParameters();
        ColumnParameters[] myColumns = myParameters.getColumnParameterArray();
        String myRemarks = myParameters.getRemarks();

        if ((null != myColumns) && (0 < myColumns.length)) {

            CsiFileType myFileType = requestIn.getFileType();
            String myUserName = CsiSecurityManager.getUserName();
            String myOrigin = (CsiFileType.NEW_EXCEL == myFileType)
                                    ? ((NewExcelInstallRequest)requestIn).getStrings().getName()
                                    : null;

            LOG.info("Create Installed Table "
                    + Format.value(tableNameIn)
                    + " with view "
                    + Format.value(InstalledTable.createTablePath(myParameters.getTableName(),
                                                                    myUserName, myFileType.getExtension())));
            myTable = new InstalledTable(myParameters.getTableName(), myUserName, myOrigin, tableNameIn, myFileType,
                                            requestIn.getCapcoInfo(), requestIn.getTagsInfo(), myRemarks);

            for (int i = 0; myColumns.length > i; i++) {

                ColumnParameters myColumn = myColumns[i];

                if (myColumn.isIncluded()) {

                    myTable.addColumn(new InstalledColumn(myColumn.getName(),
                            myColumn.getLocalId(), myColumn.getDataType(), myColumn.isNullable()));
                }
            }
        }
        return myTable;
    }

   private static boolean discardIfUnused(InstalledTable tableIn) {
      boolean mySuccess = false;

      if ((tableIn != null) && tableIn.readyToDiscard()) {
         LOG.info("Remove default table " + Format.value(tableIn.getActiveTableName())
                  + " for installed table " + Format.value(tableIn.getName()));

         try (Connection myCacheConnection = CsiPersistenceManager.getCacheConnection()) {
            try {
               DataCacheBuilder.dropCacheTable(myCacheConnection, tableIn.getActiveTableName());
            } catch (Exception e) {
               LOG.error("Caught exception removing default table " + Format.value(tableIn.getActiveTableName())
                         + " for installed table " + Format.value(tableIn.getName()), e);
               SqlUtil.quietRollback(myCacheConnection);
            }
         } catch (Exception myException) {
            LOG.error("Caught exception removing default table " + Format.value(tableIn.getActiveTableName())
                      + " for installed table " + Format.value(tableIn.getName()), myException);
         }
         try {
            if (tableIn.readyToDiscard()) {
               LOG.info("Delete installed table " + Format.value(tableIn.getName()));
               CsiPersistenceManager.deleteObject(InstalledTable.class, tableIn.getUuid());
               mySuccess = true;
            } else {
               LOG.debug("Update usage for installed table " + Format.value(tableIn.getName()));
               CsiPersistenceManager.merge(tableIn);
            }
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();
         } catch (Exception myException) {
            LOG.error("Caught exception deleting installed table " + Format.value(tableIn.getName()));
            CsiPersistenceManager.rollback();
            CsiPersistenceManager.begin();
         }
      }
      return mySuccess;
   }

    private static InstalledTable loadExcelData(InstalledTable tableIn, NewExcelInstallRequest installRequestIn)
                    throws Exception {

        String myUuid = (null != tableIn) ? tableIn.getUuid() : null;

        if (null != myUuid) {

            String mySourceName = (null != installRequestIn) ? installRequestIn.getFileName() : tableIn.getName();
            Path myPath = Paths.get(FileHelper.buildUserFilePath(mySourceName));
            LOG.debug("Load Excel data from " + Format.value(myPath));
            CsiServerZipScanner myZipStream = null;

            try {

                NewExcelInstallRequest myInstallRequest =
                  (installRequestIn == null) ? new NewExcelInstallRequest(tableIn) : installRequestIn;
                CsiFileType myFileType = myInstallRequest.getFileType();
                Integer myRowLimit = myInstallRequest.getRowLimit();

                try (CsiFileInputStream myFileStream = new CsiFileInputStream(myPath)) {
                   myZipStream = new CsiServerZipScanner(myFileStream, new Inflater(true));
                   XlsxReader myReader = new XlsxReader(myZipStream, myInstallRequest.getStrings());
                   NewExcelParameters myParameters = (NewExcelParameters) myInstallRequest.getTableParameters();
                   ColumnParameters[] myColumns = myParameters.getColumnParameterArray();
                   List<InstalledColumn> myTableColumns = buildColumnList(tableIn, myColumns);
                   CsiZipEntry myEntry = myParameters.getEntry();
                   int myDataStart = myParameters.getDataStart();
                //            FileOutputStream myDebugFile = getOutputFile("DEBUG", false);
                   FileOutputStream myDebugFile = null;

                   if ((null != myColumns) && (0 < myColumns.length)) {
                      AbstractInlineProducer myProducer = new NewExcelProducer(TaskHelper.getCurrentContext().getTaskId(),
                                  myReader, myEntry, myRowLimit, myColumns, (myDataStart - 1), myDebugFile);
                      tableIn.setSize(DataCacheBuilder.createTableFromUpload(tableIn, myTableColumns,
                                                                             myProducer, myFileType, CsiEncoding.UTF_8,
                                                                             _COMMA, _DOUBLE_QUOTE, _DOUBLE_QUOTE, null));
                      DataCacheBuilder.createViewForInstalledTable(tableIn);
                   } else {
                      throw new CentrifugeException("No selected columns identified.");
                   }
                }
            } finally {

                if (null != myZipStream) {

                    try {

                        myZipStream.close();

                    } catch (Exception myException) {

                        // IGNORE!!
                    }
                }
            }
        }
        return tableIn;
    }

    private static InstalledTable loadCsvData(InstalledTable tableIn, NonBinaryInstallRequest installBlockIn)
            throws Exception {

        String myUuid = (null != tableIn) ? tableIn.getUuid() : null;

        if (null != myUuid) {

            String mySourceName = (null != installBlockIn) ? installBlockIn.getFileName() : tableIn.getName();
            Path myPath = Paths.get(FileHelper.buildUserFilePath(mySourceName));
            LOG.debug("Load text/csv data from " + Format.value(myPath));

            NonBinaryInstallRequest myInstallRequest =
               (installBlockIn == null) ? new NonBinaryInstallRequest(tableIn) : installBlockIn;
            CsiFileType myFileType = myInstallRequest.getFileType();
            Integer myRowLimit = myInstallRequest.getRowLimit();
            TableParameters myParameters = myInstallRequest.getTableParameters();
            ColumnParameters[] myColumns = myParameters.getColumnParameterArray();
            CsiEncoding myFinalEncoding = myInstallRequest.getEncoding();
            CsiEncoding myUnsupportedEncoding = null;

            if ((null != myColumns) && (0 < myColumns.length)) {
               Integer myDelimiter = null;
               Integer myQuote = null;
               Integer myEscape = null;
               int myDataStart = 0;
               String myNullValue = null;
               AbstractInlineProducer myProducer = null;
               boolean myBackSlashFlag = false;
               List<InstalledColumn> myTableColumns = buildColumnList(tableIn, myColumns);

               if (CsiFileType.CSV == myFileType) {
                  myDelimiter = ((CsvParameters) myParameters).getDelimiter();
                  myDataStart = ((CsvParameters) myParameters).getDataStart();
                  myNullValue = ((CsvParameters) myParameters).getNullIndicator();
                  myQuote = ((CsvParameters) myParameters).getQuote();
                  myEscape = ((CsvParameters) myParameters).getEscape();
                  myBackSlashFlag = false;

                  if (null == myQuote) {
                     myQuote = _DOUBLE_QUOTE;
                  }
               } else if (CsiFileType.TEXT == myFileType) {
                  myDelimiter = ((TxtParameters) myParameters).getDelimiter();
                  myDataStart = ((TxtParameters) myParameters).getDataStart();
                  myNullValue = ((TxtParameters) myParameters).getNullIndicator();
                  myBackSlashFlag = true;
               }
               try (CsiFileInputStream myFileStream = new CsiFileInputStream(myPath, myParameters.getFixNulls())) {
                  // Identify the appropriate producer
                  //
                  if ((CsiEncoding.UTF_16LE == myFinalEncoding) || (CsiEncoding.UTF_16BE == myFinalEncoding)) {
                     myUnsupportedEncoding = myFinalEncoding;
                     myFinalEncoding = CsiEncoding.UTF_8;
                     myProducer = new CsvEncodingProducer(TaskHelper.getCurrentContext().getTaskId(), myFileStream,
                                                               myUnsupportedEncoding, myDelimiter, myQuote, myEscape,
                                                               myNullValue, myColumns, myDataStart - 1, myRowLimit,
                                                               myBackSlashFlag, false);
                  } else {
                     myProducer = new CsvProducer(TaskHelper.getCurrentContext().getTaskId(), myFileStream, myDelimiter,
                                                       myQuote, myEscape, myNullValue, myColumns, myDataStart - 1, myRowLimit,
                                                       myBackSlashFlag, false);
                  }
                  tableIn.setSize(DataCacheBuilder.createTableFromUpload(tableIn, myTableColumns,
                                                                              myProducer, myFileType, myFinalEncoding,
                                                                              myDelimiter, myQuote, myEscape, myNullValue));
                  DataCacheBuilder.createViewForInstalledTable(tableIn);
               }
            }
        }
        return tableIn;
    }

    private static List<FieldDef> retrieveFieldList(DataWrapper sourceIn, List<String> fieldIdListIn)
            throws CentrifugeException {

        List<FieldDef> myListOut = new ArrayList<FieldDef>();
        List<FieldDef> myListIn = sourceIn.getFieldList();
        FieldListAccess myAccess = sourceIn.getDataDefinition().getFieldListAccess();

        if ((null != fieldIdListIn) && !fieldIdListIn.isEmpty()) {

            for (String myFieldId : fieldIdListIn) {

                FieldDef myField = myAccess.getFieldDefByLocalId(myFieldId);

                if (null == myField) {

                    throw new CentrifugeException("Failed retrieving field list for creating Installed Table.");
                }
                myListOut.add(myField);
            }

        } else {

            myListOut = myListIn;
        }
        return myListOut.isEmpty() ? null : myListOut;
    }

    private static List<FieldDef> retrieveMatchingFieldList(InstalledTable tableIn,
                                                            DataWrapper sourceIn, List<String> fieldIdListIn)
            throws CentrifugeException {

        List<String> myListIn = ((null != fieldIdListIn) && !fieldIdListIn.isEmpty())
                                            ? fieldIdListIn : tableIn.getFieldIdList();
        List<FieldDef> myListOut = retrieveFieldList(sourceIn, myListIn);

        List<InstalledColumn> myColumns = tableIn.getColumns();

        if ((null != myColumns) && (null != myListOut) && (myColumns.size() == myListOut.size())) {
           int howMany = myColumns.size();

            for (int i = 0; i < howMany; i++) {

                InstalledColumn myColumn = myColumns.get(i);
                FieldDef myField = myListOut.get(i);

                if (myColumn.getType() != myField.getDataType()) {

                    throw new CentrifugeException("Failed matching field list for updating Installed Table.");
                }
            }

        } else {

            throw new CentrifugeException("Failed retrieving field list for updating Installed Table.");
        }
        return myListOut;
    }
/*
                    FieldDef myField = myFields.get(i);
                    InstalledColumn myColumn = new InstalledColumn(myField.getFieldName(), myField.getLocalId(), myField.getValueType(), true, i);
                    columns.add(myColumn);

                ColumnParameters myColumn = myColumns[i];

                if (myColumn.isIncluded()) {

                    myTable.addColumn(new InstalledColumn(myColumn.getName(),
                            myColumn.getLocalId(), myColumn.getDataType(), myColumn.isNullable()));
 */
/*
    private static InstalledTable generateDataSource(InstalledTable tableIn, TableInstallRequest installBlockIn) {

        String myUserName = CsiSecurityManager.getUserName();
        DataSourceDef mySource = installBlockIn.generateDataSource(myUserName);
        ConnectionDef myConnection = mySource.getConnection();
        TableParameters myTableParameters = installBlockIn.getTableParameters();
        String myTableName = myTableParameters.getTableName();
        String myDsoName = "01 " + myTableName;
        List<ColumnDef> myColumnList = new ArrayList<ColumnDef>();
        List<FieldDef> myFieldList = new ArrayList<FieldDef>();
        String mySourceId = mySource.getLocalId();
        AdHocDataSource myMeta = new AdHocDataSource(AclResourceType.DATA_TABLE, mySource,
                                                        installBlockIn.getCapcoInfo(), installBlockIn.getTagsInfo());
        SqlTableDef myTable = new SqlTableDef(myUserName, installBlockIn.getFileType().getLabel().toLowerCase(),
                                                myTableName, null, "TABLE", null);
        String myTableId = myTable.getLocalId();
        ColumnParameters[] myParametersList = myTableParameters.getColumnParameterArray();
        List<InstalledColumn> myInstalledColumns = tableIn.getColumns();

        for (int i = 0, j = 0; myParametersList.length > i; i++) {

            ColumnParameters myParameters = myParametersList[i];

            String myColumnName = myParameters.getColumnName();
            CsiDataType myColumnType = myParameters.getColumnType();
            String myColumnId = myParameters.getLocalId();
            ColumnDef myColumn = new ColumnDef(myTable, myColumnName, myColumnType, null, i, true);

            if (myParameters.isIncluded()) {

                InstalledColumn myInstalledColumn = myInstalledColumns.get(j);
                String myName = myInstalledColumn.getFieldName();
                CsiDataType myType = myInstalledColumn.getType();
                FieldDef myField = new FieldDef(j, myName, FieldType.COLUMN_REF, myType, mySourceId, myTableId, myColumnId);
                myFieldList.add(myField);
            }
            myColumnList.add(myColumn);
        }
        myTable.setColumns(myColumnList);
        myTable.setSource(mySource);
        myTable.setDsoName(myDsoName);
        myTable.setDsoType(JdbcDriverType.extractValue(myConnection.getType()));
        myMeta.setDataTree(new DataSetOp(myDsoName, myTable));
        myMeta.setFieldDefs(myFieldList);
        tableIn.setSourceDefinition(myMeta);
        return tableIn;
    }
*/
    private static List<InstalledColumn> buildColumnList(InstalledTable tableIn, ColumnParameters[] parametersIn) {

        List<InstalledColumn> myList = null;

        if ((null != tableIn) &&(null != parametersIn)) {

            myList = new ArrayList<InstalledColumn>();

            for (ColumnParameters myParameters : parametersIn) {

                if ((null != myParameters) && myParameters.isIncluded()) {

                    String myKey = myParameters.getLocalId();

                    if (null != myKey) {

                        InstalledColumn myColumn = tableIn.getColumnByLocalId(myKey);

                        if (null != myColumn) {

                            myList.add(myColumn);
                            myParameters.setColumnType(myColumn.getType());
                            myParameters.setDataType(myColumn.getType());
                        }
                    }
                }
            }
        }
        return myList;
    }
}
