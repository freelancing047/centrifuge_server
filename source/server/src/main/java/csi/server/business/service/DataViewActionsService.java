package csi.server.business.service;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;

import csi.config.Configuration;
import csi.config.DBConfig;
import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.security.queries.Users;
import csi.server.business.cachedb.script.CsiScriptRunner;
import csi.server.business.cachedb.script.MapRow;
import csi.server.business.cachedb.script.ecma.EcmaScriptRunner;
import csi.server.business.helper.DataCacheBuilder;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.DataViewFactory;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.ModelHelper;
import csi.server.business.helper.RecoveryHelper;
import csi.server.business.helper.SecurityHelper;
import csi.server.business.helper.SharedDataSourceHelper;
import csi.server.business.helper.linkup.ParameterSetFactory;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.selection.torows.SelectionToRowsConverter;
import csi.server.business.selection.torows.SelectionToRowsCoverterFactory;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.CompilationResult;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.DataPackage;
import csi.server.common.dto.DataviewRequest;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.LaunchRequest;
import csi.server.common.dto.Response;
import csi.server.common.dto.SpinoffRequestV2;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.config.connection.DriverConfigInfo;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.dto.user.RecentAccess;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.LinkupMode;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.exception.AuthorizationException;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.linkup.LinkupDataTransfer;
import csi.server.common.linkup.LinkupHelper;
import csi.server.common.linkup.LinkupRequest;
import csi.server.common.linkup.LinkupResponse;
import csi.server.common.linkup.LinkupValidationReport;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.Resource;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.AnnotationCardDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.FilterDefinition;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.MultiValueDefinition;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.ParameterHelper;
import csi.server.common.util.SynchronizeChanges;
import csi.server.common.util.ValuePair;
import csi.server.connector.config.DriverList;
import csi.server.connector.config.JdbcDriver;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.CsiTypeUtil;
import csi.server.util.DataModelDefHelper;
import csi.server.util.FieldReferenceValidator;
import csi.server.util.TemplateCacheUtil;
import csi.server.util.sql.SQLFactory;
import csi.server.ws.actions.TaskCsiSessionHelper;
import csi.shared.gwt.exception.SecurityException;
import csi.startup.CleanUpThread;

@SuppressWarnings("unused")
@Service(path = "/actions/dataview")
public class DataViewActionsService extends AbstractService implements DataViewActionServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(DataViewActionsService.class);

    private static final String LINKUP_OPERATION_NO_DATA = "No data is returned from the linkup operation";
    public static final String SESSION_ID = "sessionID";

    private static boolean _doDebug = LOG.isDebugEnabled();

    private final ConcurrentHashMap<String, DataView> prebuildToTemplateQueue = new ConcurrentHashMap<String, DataView>(20);

    @Autowired
    SelectionBroadcastCache selectionBroadcastCache;

    @Autowired
    VisualizationActionsService visualizationActionsService;

    public DataViewActionsService() {
        super();
    }

    @Override
   @Operation
    public List<ResourceBasics> listRecentlyOpenedDataViews2() throws CentrifugeException {

        return AclRequest.listRecentlyOpenedDataViews(new AclControlType[] { AclControlType.READ });
    }

    @Override
   @Operation
    public List<ResourceBasics> listUserResourceBasics() throws CsiSecurityException {

        List<ResourceBasics> dataViews = AclRequest.listUserDvs();
        List<ResourceBasics> templates = AclRequest.listUserTemplates();

        List<ResourceBasics> selectorBasics = new ArrayList<ResourceBasics>();
        if (dataViews != null) {
         selectorBasics.addAll(dataViews);
      }
        if (templates != null) {
         selectorBasics.addAll(templates);
      }

        return selectorBasics;
    }

    @Operation
    public List<ResourceBasics> listUserDataViewBasics() throws CsiSecurityException {

        return AclRequest.listUserDvs();
    }

    @Override
   @Operation
    public List<ResourceBasics> listDataViewBasics(AclControlType accessModeIn) throws CentrifugeException {

        List<ResourceBasics> myResults = AclRequest.listAnyAuthorizedDvs(new AclControlType[]{accessModeIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Override
   @Operation
    public List<ResourceBasics> listDataViewExportBasics() throws CentrifugeException {

        List<ResourceBasics> myResults = AclRequest.listAuthorizedDvExports();

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Override
   @Operation
    public List<List<ResourceBasics>> getDataViewOverWriteControlLists() throws CentrifugeException {

        List<ResourceBasics> mySelectionList = AclRequest.listAuthorizedUserDvs(new AclControlType[]{AclControlType.DELETE});
        List<ResourceBasics> myRejectionList = AclRequest.listUserDvs();
        List<ResourceBasics> myConflictList = AclRequest.listAuthorizedUserDvs(new AclControlType[]{AclControlType.DELETE});

        return ModelHelper.generateOverWriteControlLists(mySelectionList, myRejectionList, myConflictList);
    }

    @Override
   @Operation
    public List<String> listDataViewNames(AclControlType accessModeIn) throws CentrifugeException {

        return AclRequest.listAuthorizedDvNames(new AclControlType[]{accessModeIn});

    }

    @Override
   @Operation
    @Interruptable
    public Boolean dataviewNameExists(@QueryParam(value = "name") String resName) throws CentrifugeException {

        return AclRequest.resourceNameExists(DataView.class, resName);
    }

    @Override
   @Operation
    public Response<String, DataView> getDataview(String dataViewIdIn, AclControlType modeIn) {

        if (null != dataViewIdIn) {

            if (!CsiSecurityManager.isAuthorized(dataViewIdIn, modeIn)) {

                return new Response<String, DataView>(dataViewIdIn, ServerMessage.DATAVIEW_EDIT_ERROR);
            }

            try {
                DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dataViewIdIn);

                if (null != myDataView) {

                    return new Response<String, DataView>(dataViewIdIn, myDataView);

                } else {

                    return new Response<String, DataView>(dataViewIdIn, ServerMessage.DATAVIEW_LOCATE_ERROR, Format.value(dataViewIdIn) + ".");
                }

            } catch (Exception myException) {

                LOG.error(myException.getMessage(), myException);

                return new Response<String, DataView>(dataViewIdIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
            }

        } else {

            return new Response<String, DataView>(dataViewIdIn, ServerMessage.DATAVIEW_LOCATE_ERROR, "<null>.");
        }
    }

   @Override
   @Operation
   public Response<String,DataView> editDataview(DataView dataViewIn) {
      if (dataViewIn != null) {
         String myId = dataViewIn.getUuid();

         if (myId != null) {
            if (!CsiSecurityManager.isAuthorized(myId, AclControlType.EDIT)) {
               return new Response<String,DataView>(myId, ServerMessage.DATAVIEW_EDIT_ERROR);
            }
            try {
               DataView myOldDataView = CsiPersistenceManager.findObject(DataView.class, myId);
               DataViewDef myOldMeta = myOldDataView.getMeta();
               DataViewDef myNewMeta = dataViewIn.getMeta();
               DataModelDef myOldDataModel = myOldMeta.getModelDef();
               DataModelDef myNewDataModel = myNewMeta.getModelDef();
               DataSetOp myOldDataTree = myOldMeta.getDataTree();
               DataSetOp myNewDataTree = myNewMeta.getDataTree();

               try {
                  FieldReferenceValidator fieldReferenceValidator = new FieldReferenceValidator(dataViewIn.getMeta());

                  fieldReferenceValidator.isValid();
               } catch (Exception myException) {
                  LOG.error(myException.getMessage(), myException);

                  return new Response<String,DataView>(myId, ServerMessage.DATAVIEW_VALIDATION_ERROR,
                        Format.value(dataViewIn.getName()) + "\"\n" + Format.value(myException));
               }

               try {
                  DataViewHelper.fixupPersistenceLinkage(dataViewIn);

                  myNewMeta.setDataTree(null);
                  CsiPersistenceManager.merge(dataViewIn);
                  CsiPersistenceManager.commit();
                  CsiPersistenceManager.begin();
                  if (null != myOldDataTree) {

                     CsiPersistenceManager.deleteObject(DataSetOp.class, myOldDataTree.getUuid());
                     CsiPersistenceManager.commit();
                     CsiPersistenceManager.begin();
                     myOldDataTree = null;
                  }
                  myNewMeta.setDataTree(myNewDataTree);
                  dataViewIn.setNeedsSource(false);
                  DataView mySavedDataView = CsiPersistenceManager.merge(dataViewIn);

                  if (_doDebug) {
                     LOG.debug("\nDone Saving DataView \"" + dataViewIn.getName() + "\"\n");
                  }

                  return new Response<String,DataView>(myId, mySavedDataView);

               } catch (Exception myException) {
                  LOG.error("Retrying after exception saving DataView\n" + Format.value(myException));

                  try {
                     myNewMeta.setDataTree(null);
                     myNewMeta.setModelDef(null);
                     CsiPersistenceManager.merge(dataViewIn);
                     CsiPersistenceManager.commit();
                     CsiPersistenceManager.begin();
                     if (null != myOldDataTree) {

                        CsiPersistenceManager.deleteObject(DataSetOp.class, myOldDataTree.getUuid());
                        CsiPersistenceManager.commit();
                        CsiPersistenceManager.begin();
                     }
                     if (null != myOldDataModel) {

                        CsiPersistenceManager.deleteObject(DataSetOp.class, myOldDataModel.getUuid());
                        CsiPersistenceManager.commit();
                        CsiPersistenceManager.begin();
                     }
                     myNewMeta.setDataTree(myNewDataTree);
                     myNewMeta.setModelDef(myNewDataModel);
                     dataViewIn.setNeedsSource(false);
                     DataView mySavedDataView = CsiPersistenceManager.merge(dataViewIn);

                     if (_doDebug) {
                        LOG.debug("\nDone Saving DataView \"" + dataViewIn.getName() + "\"\n");
                     }
                     return new Response<String,DataView>(myId, mySavedDataView);
                  } catch (Exception mySecondException) {
                     LOG.error("Failed after retrying DataView save\n" + Format.value(myException));

                     CsiPersistenceManager.merge(myOldDataView);

                     return new Response<String,DataView>(myId, ServerMessage.DATAVIEW_SAVE_ERROR,
                           Format.value(dataViewIn.getName()) + "\n" + Format.value(myException));
                  }
               }
            } catch (Exception myException) {
               LOG.error(myException.getMessage(), myException);

               return new Response<String,DataView>(myId, ServerMessage.DATAVIEW_SAVE_ERROR,
                     Format.value(dataViewIn.getName()) + "\n" + Format.value(myException));
            }
         } else {
            return new Response<String,DataView>(myId, ServerMessage.DATAVIEW_UUID_ERROR);
         }
      } else {
         return new Response<String,DataView>(null, ServerMessage.DATAVIEW_EDIT_ERROR);
      }
   }

    @Override
   @Operation
    public DataView save(@PayloadParam DataView view) throws CentrifugeException {
        if (!CsiSecurityManager.isAuthorized(view.getUuid(), AclControlType.EDIT)) {
            throw new AuthorizationException("Access denied.  Not authorized to edit dataview.");
        }

        try {
            if (_doDebug) {
                LOG.debug("\nBegin Saving DataView \"" + view.getName() + "\"\n");
                LOG.debug(view.debug("  "));
            }

            DataViewHelper.fixupPersistenceLinkage(view);

            FieldReferenceValidator fieldReferenceValidator = new FieldReferenceValidator(view.getMeta());
//            fieldReferenceValidator.validateModelReferences();
            fieldReferenceValidator.isValid();
            DataView myDataView = ModelHelper.save(view);

            if (_doDebug) {
               LOG.debug("\nDone Saving DataView \"" + view.getName() + "\"\n");
            }

            return myDataView;
        } catch (Exception e) {
            CentrifugeException myException = new CentrifugeException(e.getMessage());
            myException.setStackTrace(e.getStackTrace());
            LOG.error(e.getMessage(), e);
//            throw new CentrifugeException(
            //                  "The dataview you attempted to save is in an invalid state. This may be due to another user editing this dataview with conflicting changes. Please close and reopen this dataview to correct the problem.", e);
            TaskHelper.reportError(e.getMessage(), myException);
            throw myException;
        }
    }

    @Override
   public String deleteDataView(String uuidIn) {

        DataView myDataView = CsiPersistenceManager.findForDelete(DataView.class, uuidIn);

        if (null != myDataView) {

            if (!CsiSecurityManager.isAuthorized(uuidIn, AclControlType.DELETE)) {

                throw Throwables.propagate(new SecurityException("Not authorized to delete the Dataview with uuid "
                                                                    + Format.value(uuidIn)));
            }
            LOG.info("Delete DataView " + Format.value(myDataView.getName()) + ", id = " + uuidIn);
            TaskCsiSessionHelper mySessionHelper = new TaskCsiSessionHelper(TaskHelper.getCurrentContext());
            mySessionHelper.cleanUp(myDataView);

            if (!CleanUpThread.scheduleDelete(myDataView)) {

                throw Throwables.propagate(new SecurityException("Unable to mark for delete the Dataview with uuid "
                                                                    + Format.value(uuidIn)));
            }
        }
        return uuidIn;
    }

    @Override
   public Response<String, DataView> createSimpleDataView(String nameIn,
                                                           String remarksIn, SqlTableDef tableIn,
                                                           List<QueryParameterDef> dataSetParametersIn,
                                                           List<LaunchParam> parametersIn, List<AuthDO> credentialsIn,
                                                           CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn,
                                                           boolean overwriteIn,
                                                           SharingInitializationRequest sharingRequestIn) {

        DataView myDataView = null;

        TaskHelper.reportTaskID();
        try {

            Users.validateUser();
            LOG.info("Create new DataView " + Format.value(nameIn) + " from scratch.");
            myDataView = DataViewFactory.createDataView(nameIn, remarksIn, tableIn,
                                                        dataSetParametersIn, capcoInfoIn, tagInfoIn, overwriteIn);

        } catch (GeneralSecurityException myException) {

            try {

                return new Response<String, DataView>(
                        DataViewHelper.listAuthorizationsRequired(tableIn, credentialsIn));

            } catch (Exception myNewException) {

                return new Response<String, DataView>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myNewException));
            }

        } catch (Exception myException) {

            return new Response<String, DataView>(ServerMessage.DATAVIEW_CREATE_EXCEPTION, Format.value(myException));
        }

        if (null != myDataView) {

            try {

                DataView myExistingDataView = AclRequest.findOwnedResourceByName(DataView.class, nameIn);

                if (null != myExistingDataView) {

                    deleteDataView(myExistingDataView.getUuid());
                }
                myDataView.setNeedsSource(null == tableIn);
                ModelHelper.defineSharing(myDataView.getUuid(), sharingRequestIn);
                CsiPersistenceManager.persist(DataViewHelper.fixupPersistenceLinkage(myDataView));
                // myDataView =
                // CsiPersistenceManager.merge(DataViewHelper.fixupPersistenceLinkage(myDataView));

            } catch (Exception myException) {

                return new Response<String, DataView>(ServerMessage.DATAVIEW_DELETE_EXCEPTION, Format.value(myException));
            }

        } else {

            return new Response<String, DataView>(ServerMessage.DATAVIEW_CREATE_ERROR);
        }

        if (null != tableIn) {

            try {

                DataSourceDef mySource = tableIn.getSource();

                if (null != mySource) {

                    return launch(null, myDataView, parametersIn, credentialsIn, null);
                }

            } catch (GeneralSecurityException myException) {

                try {

                    return new Response<String, DataView>(DataViewHelper.listAuthorizationsRequired(myDataView.getMeta().getDataSources(), credentialsIn));

                } catch (Exception myNewException) {

                    return new Response<String, DataView>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myNewException));
                }

            } catch (Exception myException) {

                return new Response<String, DataView>(ServerMessage.DATAVIEW_LOAD_EXCEPTION, Format.value(myException));
            }
        }

        return new Response<String, DataView>(myDataView);
    }

    @Override
   public Response<String, DataView> createDataView(String requestIdIn, String nameIn, String commentsIn,
                                                     AdHocDataSource dataSourceIn, List<LaunchParam> parametersIn,
                                                     List<AuthDO> credentialsIn, boolean overwriteIn,
                                                     SharingInitializationRequest sharingRequestIn) {

        DataView myDataView = null;

        TaskHelper.reportTaskID();
        try {

            LOG.info("Create new DataView " + Format.value(nameIn) + " from scratch.");
            myDataView = DataViewFactory.createDataView(nameIn, commentsIn, dataSourceIn, overwriteIn);

        } catch (GeneralSecurityException myException) {

            try {

                return new Response<String, DataView>(
                        DataViewHelper.listAuthorizationsRequired(dataSourceIn.getDataSources(), credentialsIn));

            } catch (Exception myNewException) {

                return new Response<String, DataView>(requestIdIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myNewException));
            }

        } catch (Exception myException) {

            return new Response<String, DataView>(requestIdIn, ServerMessage.DATAVIEW_CREATE_EXCEPTION, Format.value(myException));
        }

        if (null != myDataView) {

            try {

                DataView myExistingDataView = AclRequest.findOwnedResourceByName(DataView.class, nameIn);

                if (null != myExistingDataView) {

                    deleteDataView(myExistingDataView.getUuid());
                }
                myDataView.setNeedsSource(false);
                ModelHelper.defineSharing(myDataView.getUuid(), sharingRequestIn);
                CsiPersistenceManager.persist(DataViewHelper.fixupPersistenceLinkage(myDataView));
                // myDataView =
                // CsiPersistenceManager.merge(DataViewHelper.fixupPersistenceLinkage(myDataView));

            } catch (Exception myException) {

                return new Response<String, DataView>(requestIdIn, ServerMessage.DATAVIEW_DELETE_EXCEPTION, Format.value(myException));
            }

        } else {

            return new Response<String, DataView>(requestIdIn, ServerMessage.DATAVIEW_CREATE_ERROR);
        }

        try {

            return launch(requestIdIn, myDataView, parametersIn, credentialsIn, null);

        } catch (GeneralSecurityException myException) {

            try {

                return new Response<String, DataView>(requestIdIn, DataViewHelper.listAuthorizationsRequired(myDataView.getMeta().getDataSources(), credentialsIn));

            } catch (Exception myNewException) {

                return new Response<String, DataView>(requestIdIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myNewException));
            }

        } catch (Exception myException) {

            return new Response<String, DataView>(requestIdIn, ServerMessage.DATAVIEW_LOAD_EXCEPTION, Format.value(myException));
        }
    }

    /**
     * This method initializes the DataView's field cache.  It doesn't actually launch a DataView.
     */
    private Response<String, DataView> launch(String requestIdIn, DataView dataviewIn, List<LaunchParam> parametersIn,
                            List<AuthDO> credentialsIn, List<List<ArrayList<String>>> parameterValuesIn)
            throws CentrifugeException, GeneralSecurityException {

        try {

            String myUserName = Users.validateUser();

            LOG.info(String.format("Request by user %s to open dataview %s, uuid %s", Format.value(myUserName),
                                    Format.value(dataviewIn.getName()), Format.value(dataviewIn.getUuid())));

            if (!CsiSecurityManager.isAuthorized(dataviewIn.getUuid(), AclControlType.EDIT)) {

                throw new CentrifugeException(String.format("User %s Not authorized to refresh data for dataview %s",
                                                            Format.value(myUserName),
                                                            Format.value(dataviewIn.getName())));
            }

            DataView openedView = null;
            Response<String, DataView> myResponse = null;
            try {

                myResponse = (new DataViewHelper(visualizationActionsService)).openDataView(requestIdIn, dataviewIn,
                                                                                            parametersIn, credentialsIn);
                openedView = myResponse.getResult();
                if (openedView == null) {

                    throw new CentrifugeException(String.format("Dataview %s contains no data.",
                                                                Format.value(dataviewIn.getName())));
                }
                LOG.info(String.format("User %s successfully initialized dataview %s, uuid %s", Format.value(myUserName),
                                        Format.value(dataviewIn.getName()), Format.value(dataviewIn.getUuid())));

            } catch (CentrifugeException e) {

                if (null != parameterValuesIn) {

                    throw new CentrifugeException(LINKUP_OPERATION_NO_DATA);

                } else {

                    throw (e);
                }
            }
            openedView.getMeta().getModelDef().getVisualizations();
            return myResponse;

        } catch (TaskCancelledException e) {

            (new DataViewHelper(visualizationActionsService)).deleteCacheData(dataviewIn);

            // TODO: the client should not save the dataview in a separate transaction.
            // This is a hack...we need to do special processing here to delete
            // the cancelled dataview since the dataview was not
            // created in the current transaction so rolling back changes
            // won't actually remove the dataview.
            //

            // roll back anything we may have change in the current transaction
            CsiPersistenceManager.rollback();

            // begin and new transaction and delete the object. We must commit here
            // otherwise throwing the exception will cause the new transaction to be
            // rolled back
            CsiPersistenceManager.begin();
            CsiPersistenceManager.deleteObject(DataView.class, dataviewIn.getUuid());
            CsiPersistenceManager.commit();
            throw e;
        }
    }

    @Override
   @Operation
    public void closeDataview(@QueryParam(value = "uuid") String uuid) {

        try {
            AclRequest.recordAccess(uuid);
            AclRequest.recordAccess(new RecentAccess(CsiSecurityManager.getUserName(), uuid, null));
        } catch (Exception IGNORE) {}
        TaskCsiSessionHelper helper = new TaskCsiSessionHelper(TaskHelper.getCurrentContext());
        helper.cleanUp(uuid);
    }

    @Override
   @Operation
    @Interruptable
    public Response<String, DataView> spinoff(SpinoffRequestV2 request, String spinoffName) {

        String dvUuid = request.getDataViewUuid();

        try {

            VisualizationDef vizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getVisualizationUuid());

            TaskHelper.reportTaskID();
            return new Response<String, DataView>(dvUuid, createSpinoffDv(dvUuid, spinoffName, vizDef, request.getSelection()));

        } catch (Exception myException) {

            return new Response<String, DataView>(dvUuid, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Override
   public Response<String, TableInstallResponse> spawnTable(String nameIn, String remarksIn, String dataViewIdIn,
                                                             String visualizationIdIn, Selection selectionIn,
                                                             List<FieldDef> fieldListIn) {

        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dataViewIdIn);
        VisualizationDef myVisualization = CsiPersistenceManager.findObject(VisualizationDef.class, visualizationIdIn);

        return SharedDataSourceHelper.createTable(myDataView, selectionIn, myVisualization,
                                                    nameIn, remarksIn, fieldListIn);
    }

    @Override
   public Response<String, TableInstallResponse> updateTable(String uuidIn, String dataViewIdIn,
                                                             String visualizationIdIn, Selection selectionIn,
                                                             List<ValuePair<InstalledColumn, FieldDef>> pairedListIn) {

        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dataViewIdIn);
        VisualizationDef myVisualization = CsiPersistenceManager.findObject(VisualizationDef.class, visualizationIdIn);

        return SharedDataSourceHelper.updateTable(myDataView, pairedListIn, selectionIn, uuidIn, myVisualization);
    }

    @Override
   public List<ResourceBasics> getRecentDataViews(int limitIn) throws CentrifugeException {

        String myUser = Users.validateUser();

        return AclRequest.getRecentDataViews(myUser, limitIn);
    }

    @Override
   public List<ResourceBasics> getRecentDataViews(String userIn, int limitIn) throws CentrifugeException, SecurityException {

        if (CsiSecurityManager.isSecurity()) {

            return AclRequest.getRecentDataViews(userIn, limitIn);

        } else {

            throw new SecurityException("CSO authorization required!");
        }
    }

    @Override
   public List<FieldDef> getFullFieldList(String dataViewIdIn) {

        DataView myDataView = CsiPersistenceManager.findObjectAvoidingSecurity(DataView.class, dataViewIdIn);

        return (null != myDataView) ? new ArrayList<FieldDef>(myDataView.getFieldList()) : null;
    }

    private DataView createSpinoffDv(String dvUuid, String spinoffName, VisualizationDef vizDef, Selection selection)
            throws CentrifugeException {

        try {
            DataView srcdv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
            if (srcdv == null) {
                throw new CentrifugeException("Spin off source dataview not found: " + dvUuid);
            }
/*
            if(!verifySelection(vizDef, srcdv, selection)){
                return "-1";
            }
*/

            DataView spinDv = DataViewHelper.createSpinOff(srcdv, spinoffName, vizDef, selection);

            if (spinDv == null) {
                throw new CentrifugeException("Unable to create spinoff");
            }

            DataViewHelper.removeAllSelectionFilter(spinDv);

            return spinDv;
        } catch (CentrifugeException myException) {
            LOG.error("Caught exception during Spinoff", myException);
            throw myException;
        } catch (Throwable myException) {
            LOG.error("Caught exception during Spinoff", myException);
            throw new CentrifugeException(myException);
        }
    }
/*
    private boolean verifySelection(VisualizationDef vizDef, DataView dv, Selection selection) {
        BroadcastResult broadcast = selectionBroadcastCache.getBroadcast(vizDef.getUuid());
        if(selection == null){
            selection = getSelectionFromVizDef(vizDef);
        } else {
            vizDef.getSelection().setFromSelection(selection);
        }
        if(selection.isCleared()){
            return false;
        }
        if(broadcast == BroadcastResult.EMPTY_BROADCAST_RESULT || broadcast.getBroadcastFilter() == null || broadcast.isExcludeRows()){
            return true;
        }

        SelectionToRowsConverter selectionToRowsConverter = new SelectionToRowsCoverterFactory(dv, vizDef).create();
        Set<Integer> rows = selectionToRowsConverter.convertToRows(selection, true);
        RowsSelection vizSelection = new TableRowsToSelectionConverter().toSelection(rows);

        for(Integer rowId: broadcast.getBroadcastFilter().getSelectedItems()){
            if(vizSelection.getSelectedItems().contains(rowId)){
                return true;
            }
        }

        return false;
    }
*/
    private Selection getSelectionFromVizDef(VisualizationDef visualizationDef) {
        if (visualizationDef instanceof RelGraphViewDef) {
            return getGraphSelection(visualizationDef.getUuid());
        }

        return visualizationDef.getSelection();
    }

    private Selection getGraphSelection(String graphUuid) {
        GraphContext gc = GraphServiceUtil.getGraphContext(graphUuid);
        if (gc != null) {
            synchronized (gc) {
                return gc.getSelection(GraphManager.DEFAULT_SELECTION);
            }
        }
        return NullSelection.instance;
    }

    @Operation
    public Response<String, LinkupValidationReport> validateLinkupDefinition(LinkupMapDef linkupMappingIn) {

        String myLinkupId = (null != linkupMappingIn) ? linkupMappingIn.getUuid() : null;
        DataViewHelper myHelper = new DataViewHelper(visualizationActionsService);

        return new Response<String, LinkupValidationReport>(myLinkupId, myHelper.validateLinkupRequest(linkupMappingIn));
    }

    @Override
   @Operation
    public Response<String, LinkupResponse> executeLinkup(LinkupRequest requestIn) {
        String mySessionId = requestIn.getSessionId();
        String myLinkupUuid = requestIn.getLinkupUuid();
        LinkupMapDef myLinkupMapping = CsiPersistenceManager.findObject(LinkupMapDef.class, myLinkupUuid);
        String myVizUuid = requestIn.getVisualizationId();
        String myNewDataviewName = requestIn.getNewDataViewName();
        List<LaunchParam> myParamValues = requestIn.getParameterValues();
        List<AuthDO> authorizationList = requestIn.getAuthorizationList();
        LinkupMode myMode = requestIn.getMode();
        boolean discardNulls = requestIn.isDiscardNulls();
        List<LinkupHelper> myParameterBuilderList = requestIn.getParameterBuilderList();

        if ((null == myParameterBuilderList) || myParameterBuilderList.isEmpty() ||
              (null == myParameterBuilderList.get(0))) {
            return new Response<String, LinkupResponse>(null, "No linkup parameters found");
        }

        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, mySessionId);
        if (myDataView == null) {
            return new Response<String, LinkupResponse>(null, "Source dataview not found");
        }
        VisualizationDef myVizDef = ModelHelper.find(VisualizationDef.class, myVizUuid);
        if (myVizDef == null) {
            return new Response<String, LinkupResponse>(null, "Source visualization not found");
        }
        VisualizationType myVizType = myVizDef.getType();
        Set<Integer> mySelectionList = null;

        TaskHelper.reportTaskID();
        if ((VisualizationType.DRILL_CHART == myVizType) ||
                (VisualizationType.MATRIX == myVizType) ||
                (VisualizationType.CHRONOS == myVizType) ||
                (VisualizationType.TABLE == myVizType)){

            try {

                mySelectionList = getSelectionRows(myDataView, myVizDef, requestIn.getSelection());

            } catch (Exception myException) {

                return new Response<String, LinkupResponse>(null, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
            }
        }

        if(VisualizationType.GEOSPATIAL_V2 == myVizType) {
            try {

				AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(myDataView.getUuid(), myVizUuid);
                mySelectionList = getSelectionRows(myDataView, myVizDef, mapSelection);

            } catch (Exception myException) {

                return new Response<String, LinkupResponse>(null, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
            }
        }

        DataViewHelper myHelper = new DataViewHelper(visualizationActionsService);
        myHelper.setVizActionsService(visualizationActionsService);
        Response<String, LinkupResponse> myResponse = myHelper.executeLinkup(myDataView, myVizDef, myLinkupMapping,
                                                                            myParameterBuilderList, myNewDataviewName,
                                                                            mySelectionList, myParamValues,
                                                                            authorizationList, myMode, discardNulls);

        LinkupResponse myLinkupResponse = myResponse.getResult();
        if (null != myLinkupResponse) {

            myLinkupResponse.setLinkupRequest(requestIn);
        }
        return myResponse;
    }

    private Set<Integer> getSelectionRows(DataView dataViewIn, VisualizationDef vizDefIn, Selection selectionIn)
            throws CentrifugeException {

        SelectionToRowsCoverterFactory coverterFactory = new SelectionToRowsCoverterFactory(dataViewIn, vizDefIn);
        SelectionToRowsConverter converter = coverterFactory.create();

        Set<Integer> rows = converter.convertToRows(selectionIn, false);
        return rows;
    }

    @Override
   @Operation
    public Response<String, DataView> relaunch(String dataViewUuidIn, List<LaunchParam> parameterDataIn,
                                               List<AuthDO> credentialsIn) {

        DataView myDataView = null;
        DataPackage myRecoveryPackage = null;
        Long myRowCount = 0L;
        Boolean myMissingData = false;

        TaskHelper.reportTaskID();
        try {

            List<QueryParameterDef> myParameters = null;
            String myUser = Users.validateUser();

            if (!CsiSecurityManager.isAuthorized(dataViewUuidIn, (AclControlType)null)) {
                throw new CentrifugeException(String.format("User %s Not authorized to refresh data for dataview %s",
                        Format.value(myUser),
                        Format.value(AclRequest.getResourceName(dataViewUuidIn))));
            }

            closeDataview(dataViewUuidIn);

            myDataView = retrieveDataView(dataViewUuidIn);
            myParameters = myDataView.getMeta().getDataSetParameters();
            DataViewHelper.removeAllSelectionFilter(myDataView);
            DataViewHelper.clearVisualizationCaches(myDataView, visualizationActionsService);

            if (myDataView == null) {
                throw new CentrifugeException(String.format("Could not launch dataview UUID %s",
                                                            Format.value(dataViewUuidIn)));
            }
            if (myDataView.isSpinoff()) {

                DataViewHelper.trimDataView(myDataView);

            } else {

                if (!myDataView.getNeedsRefresh()) {

                    myRecoveryPackage = RecoveryHelper.buildRecoveryPackage(myDataView);
                }
                ValuePair<Long, Boolean> myResults = DataViewHelper.launchDataView(myDataView, parameterDataIn, credentialsIn,
                                                                    new ParameterSetFactory(myParameters, parameterDataIn));
                myRowCount = myResults.getValue1();
                myMissingData = myResults.getValue2();
                DataViewHelper.mergeParameterData(myParameters, parameterDataIn);
            }
            myDataView.setNeedsRefresh(false);
            save(myDataView);
            String userName = CsiSecurityManager.getUserName();

            LOG.info(String.format("User %s successfully reloaded dataview %s, uuid %s",
                    Format.value(userName), Format.value(myDataView.getName()), Format.value(myDataView.getUuid())));
            if (null != myRecoveryPackage) {
                SharedDataSourceHelper.releaseInstalledTables(myRecoveryPackage.getInstalledTables());
            }
            RecoveryHelper.discardRecoveryPackage(myRecoveryPackage);
            return new Response<String, DataView>(dataViewUuidIn, myDataView, myRowCount, myMissingData);

        } catch (GeneralSecurityException myException) {

            SharedDataSourceHelper.releaseInstalledTables(myDataView.clearInstalledTables());
            restoreDataView(dataViewUuidIn, myRecoveryPackage);

            try {

                return new Response<String, DataView>(dataViewUuidIn,
                        DataViewHelper.listAuthorizationsRequired(myDataView.getMeta().getDataSources(), credentialsIn));

            } catch (Exception myNewException) {

                LOG.error("Caught an Exception:", myNewException);
                return new Response<String, DataView>(dataViewUuidIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myNewException));
            }

        } catch (Exception myException) {

            LOG.error("Caught an Exception:", myException);
            restoreDataView(dataViewUuidIn, myRecoveryPackage);

            return new Response<String, DataView>(dataViewUuidIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    /**
     * Extracts the dataview from hibernate.
     *
     * @param uuid
     * @return
     * @throws CentrifugeException
     */
    private DataView retrieveDataView(String uuid) throws CentrifugeException {
        String userName = CsiSecurityManager.getUserName();
        String resourceName = AclRequest.getResourceName(uuid);
        if (!CsiSecurityManager.isAuthorized(uuid, AclControlType.READ)) {

            if (null != resourceName) {

                throw new CentrifugeException(String.format("User %s Not authorized to open dataview %s",
                                                            Format.value(userName), Format.value(resourceName)));

            } else {

                throw new CentrifugeException(String.format("User %s Not authorized to open dataview with uuid %s",
                                                            Format.value(userName), Format.value(uuid)));
            }
        }

        DataView dv = ModelHelper.find(DataView.class, uuid);
        String myCurrentVersion = ReleaseInfo.version;
        if (dv == null) {
            throw new CentrifugeException("No dataview found with uuid: " + Format.value(uuid));
        }
        if (!myCurrentVersion.equals(dv.getVersion())) {

            long myDeltaRowCount = RecoveryHelper.fixupDataView(dv);

            if (0 <= myDeltaRowCount){

                if (CsiSecurityManager.isAuthorized(uuid, AclControlType.EDIT)) {

                    dv.setVersion(myCurrentVersion);
                    CsiPersistenceManager.merge(dv);
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();
                }

            } else {

                dv.setNeedsRefresh(true);
            }
        }
        return dv;
    }

    private DataView restoreDataView(String dataViewUuidIn, DataPackage recoveryPackageIn) {

        DataView myDataView = null;
//        String[] myInstalledTables = (null != recoveryPackageIn) ? recoveryPackageIn.getInstalledTables() : null;

        try {

            RecoveryHelper.recoverDataPackage(recoveryPackageIn);

        } catch (Exception myNewException) {

            // IGNORE
        }

        // roll back anything we may have change in the current transaction
        CsiPersistenceManager.rollback();

        // begin and new transaction and delete the object. We must commit here
        // otherwise throwing the exception will cause the new transaction to be
        // rolled back
        CsiPersistenceManager.begin();

        return myDataView;
    }

    @Override
   @Operation
    public List<String> testFieldReferences(String dvUuidIn, List<String> fieldUuidsIn) {

        return DataViewHelper.testDataViewFieldReferences(dvUuidIn, fieldUuidsIn);
    }

    @Override
   @Operation
    @Interruptable
    public CsiMap<String, List<String>> listFieldReferencesMulti(@PayloadParam List<String> fieldIds,
                                                                 @QueryParam(value = SESSION_ID) String dvUuid) {
        if ((dvUuid == null) || dvUuid.isEmpty()) {
            throw new RuntimeException("Missing required parameter sessionID");
        }

        Resource rs = CsiPersistenceManager.findObject(Resource.class, dvUuid);

        TaskHelper.checkForCancel();

        FieldListAccess modelDef = null;
        if (rs instanceof DataView) {
            modelDef = ((DataView) rs).getMeta().getFieldListAccess();
        } else if (rs instanceof DataViewDef) {
            modelDef = ((DataViewDef) rs).getModelDef().getFieldListAccess();
        } else {
            throw new RuntimeException("List references not valid for resource of type: " + rs.getName());
        }

        CsiMap<String, List<String>> referenceMap = new CsiMap<String, List<String>>();
        for (String fieldUuid : fieldIds) {
            TaskHelper.checkForCancel();

            FieldDef f = modelDef.findFieldDefByUuid(fieldUuid);
            if (f == null) {
                throw new RuntimeException("Field not found: " + fieldUuid);
            }

            List<String> references = null;
            try {
                references = DataModelDefHelper.listFieldReferences(modelDef, f);
            } catch (CentrifugeException e) {
                e.printStackTrace();
            }
            referenceMap.put(fieldUuid, references);
        }

        return referenceMap;
    }

    @Override
   @Operation
    @Interruptable
    public CompilationResult testScript(@QueryParam("sessionID") String dvUuid, @PayloadParam FieldDef scriptedField)
            throws CentrifugeException {
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        FieldListAccess modelDef = dv.getMeta().getFieldListAccess();

        Random rand = new Random();
        int strcnt = 0;
        MapRow dummyRow = new MapRow();
        for (FieldDef f : modelDef.getFieldDefList()) {
            TaskHelper.checkForCancel();

            if (!f.getUuid().equals(scriptedField.getUuid()) && (f.getValueType() != null) && (f.getFieldName() != null)) {

                switch (f.getValueType()) {

                    case String:

                        dummyRow.set(f, "sample" + (strcnt++));
                        break;

                    case Integer:

                        dummyRow.set(f, Integer.valueOf(rand.nextInt()));
                        break;

                    case Number:

                        dummyRow.set(f, Double.valueOf(rand.nextFloat()));
                        break;

                    case DateTime: {

                        java.sql.Timestamp d = new java.sql.Timestamp(System.currentTimeMillis());
                        d.setTime(System.currentTimeMillis());
                        dummyRow.set(f, d);
                        break;
                    }

                    case Date: {

                        java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
                        d.setTime(0);
                        dummyRow.set(f, d);
                        break;
                    }

                    case Time: {

                        java.sql.Time t = new java.sql.Time(System.currentTimeMillis());
                        dummyRow.set(f, t);
                        break;
                    }

                    default:

                        break;
                }
            }
        }

        TaskHelper.checkForCancel();

        CompilationResult result = new CompilationResult();
        CsiScriptRunner runner = new EcmaScriptRunner();
        try {
            Object sampleVal = runner.evalScriptedField(modelDef, scriptedField, dummyRow);
            result.success = true;
            result.sampleValue = CsiTypeUtil.coerceString(sampleVal, scriptedField.getDisplayFormat());
        } catch (Exception e) {
            result.success = false;
            result.errorMsg = e.getMessage();
            if (e.getCause() != null) {
                result.cause = e.getCause().getMessage();
            }
        }
        return result;
    }

    @Override
   @Operation
    public Response<String, Boolean> share(String resourceIn, SharingInitializationRequest sharingRequestIn) {

        Boolean mySuccess = false;

        if (null != sharingRequestIn) {

            mySuccess = ModelHelper.defineSharing(resourceIn, sharingRequestIn);
        }
        return new Response<String, Boolean>(resourceIn, mySuccess);
    }

    @Override
   @Operation
    public Response<String, DataView> launchUrlTemplate(@PayloadParam LaunchRequest launchRequest) {

        return launchTemplate(launchRequest, null);
    }

    @Override
   @Operation
    public Response<String, DataView> launchTemplate(@PayloadParam LaunchRequest launchRequest) {

        return launchTemplate(launchRequest, null);
    }

    @Override
   @Operation
    public Response<String, DataView> launchTemplate(LaunchRequest launchRequestIn,
                                                     SharingInitializationRequest sharingRequestIn) {
        String sourceTemplateName = launchRequestIn.getName();
        String myUuid = launchRequestIn.getTargetUuid();
        String myRequestedName = launchRequestIn.getTargetDvName();
        boolean myOverWriteFlag = launchRequestIn.getForceOverwrite();
        String myBaseName = ((null != myRequestedName) && (0 < myRequestedName.length()))
                                    ? myRequestedName
                                    : sourceTemplateName;
        String myRemarks = launchRequestIn.getRemarks();
        List<LaunchParam> myParameterData = launchRequestIn.getParams();
        List<AuthDO> auths = launchRequestIn.getAuths();

        DataView myDataview;
        DataViewDef myTemplate = null;
        Long myRowCount;
        Boolean myMissingData = false;

        try {

            Users.validateUser();

            if (null != myUuid) {

                myTemplate = ModelHelper.find(DataViewDef.class, myUuid);
            }

            if ((null == myTemplate) && (null != sourceTemplateName)) {

                myTemplate = AclRequest.findResourceByName(DataViewDef.class, sourceTemplateName, AclControlType.READ);
            }

            if (myTemplate == null) {

                return new Response<String, DataView>(myUuid, ServerMessage.TEMPLATE_NOT_FOUND,
                                            Format.value((null != sourceTemplateName) ? sourceTemplateName : myUuid));
            }
            myUuid = myTemplate.getUuid();

        } catch (Exception myException) {

            return new Response<String, DataView>(myUuid, ServerMessage.TEMPLATE_ACCESS_EXCEPTION, Format.value(myException));
        }

        boolean cached = false;
        DataView cachedDataView;
        try {
            List<String> userDvNames = AclRequest.listUserDvNames();
            if (userDvNames != null) {
                userDvNames = userDvNames.stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            String myDataViewName = (myOverWriteFlag)
                    ? myRequestedName
                    : SynchronizeChanges.guaranteeResourceName(myBaseName, userDvNames);
            List<DataView> conflicts = AclRequest.listUserDvConflicts(myDataViewName);
            DataViewDef myMetaData;

            // Record accessing the template
            AclRequest.recordAccess(myUuid);
            //attempt to retrieve cached dataview
            cachedDataView = CsiPersistenceManager.getPrebuiltDataView(myUuid);

            LOG.info("Create DataView " + Format.value(myDataViewName)
                    + " from template "  + Format.value(sourceTemplateName) + ", id = " + Format.value(myUuid));

            if (cachedDataView == null) {
                myMetaData = ModelHelper.cloneObject(myTemplate);
                myMetaData.setTemplate(false);
                GraphActionsService.augmentRelGraphViewDef(myMetaData);
                myDataview = new DataView(myMetaData, myDataViewName, myRemarks);
            } else {
                cached = true;
                myDataview = cachedDataView;
                myDataview.setName(myDataViewName);
                myDataview.setRemarks(myRemarks);
                AclRequest.createACL(myDataview, CsiSecurityManager.getUserName(), null);
                CsiMap<String, String> properties = myDataview.getClientProperties();
                if (properties != null) {
                    properties.put(TemplateCacheUtil.PREBUILD_PROPERTY, Boolean.FALSE.toString());
                }
            }
			for(DataView conflict: conflicts){
				if(myOverWriteFlag){
					ModelHelper.delete(conflict);
				} else {
					//TODO: consider verifying this pass in integration testing
					return new Response<String, DataView>(myUuid, ServerMessage.DATAVIEW_SAVE_EXCEPTION, "Name in use");
				}
			}
            {
                //repopulate cached DataView
                CacheDataViewRunnable runnable = new CacheDataViewRunnable(myTemplate);
                Executors.newSingleThreadExecutor().execute(runnable);
            }

        } catch (Exception myException) {

            return new Response<String, DataView>(myUuid, ServerMessage.TEMPLATE_XFER_EXCEPTION, Format.value(myException));
        }

        try {
            if (!cached) {
                DataViewHelper.fixupPersistenceLinkage(myDataview);
                ModelHelper.saveNew(myDataview, launchRequestIn.getForceOverwrite());
                SecurityHelper.resetSecurityInfo(myDataview);
            }
        } catch (Exception myException) {
            return new Response<String, DataView>(myUuid, ServerMessage.DATAVIEW_SAVE_EXCEPTION, Format.value(myException));
        }

        try {

            myDataview.getMeta().setOwner(null);
            ModelHelper.defineSharing(myDataview.getUuid(), sharingRequestIn);
            ValuePair<Long, Boolean> myResults = DataViewHelper.launchDataView(myDataview, myParameterData, auths,
                    new ParameterSetFactory(myDataview.getMeta().getDataSetParameters(), myParameterData));
            myRowCount = myResults.getValue1();
            myMissingData = myResults.getValue2();
            DataViewHelper.mergeParameterData(myDataview.getParameterList(), myParameterData);
            myDataview.setNeedsRefresh(false);

        } catch (GeneralSecurityException myException) {
            try {
                return new Response<String, DataView>(myUuid,
                        DataViewHelper.listAuthorizationsRequired(myDataview.getMeta().getDataSources(), auths));

            } catch (Exception myNewException) {

                return new Response<String, DataView>(myUuid, ServerMessage.CAUGHT_EXCEPTION, Format.value(myNewException));
            }

        } catch (Exception myException) {

            return new Response<String, DataView>(myUuid, ServerMessage.DATAVIEW_LOAD_EXCEPTION, Format.value(myException));
        }
        try {
            if (!cached) {
                CsiPersistenceManager.merge(myDataview);
            }
            else{

                //TODO: Currently keeping prebuilds that have not yet been fully
                // persisted inside a concurrent map so we can return accurate dataviews to client without
                // having to wait for persistence. Level 3 cache basically.
                prebuildToTemplateQueue.put(myDataview.getUuid(), myDataview);
                final DataView mergeMe = myDataview;
                {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try{
                            CsiPersistenceManager.begin();

                            CsiPersistenceManager.getMetaEntityManager().merge(mergeMe);
                            CsiPersistenceManager.commit();
                            CsiPersistenceManager.close();
                            prebuildToTemplateQueue.remove(mergeMe.getUuid());
                            } catch(Exception exception){
                                LOG.error(exception);

                            }
                        }
                    };
                    Executors.newSingleThreadExecutor().execute(runnable);
                }
            }
        } catch (Exception myException) {
            return new Response<String, DataView>(myUuid, ServerMessage.DATAVIEW_PERSIST_EXCEPTION, Format.value(myException));
        }

        DataViewHelper.removeAllSelectionFilter(myDataview);

        if (launchRequestIn.getMigrateACL()) {

            AclRequest.migrateACL(myDataview, myTemplate.getUuid());
        }
        return new Response<String, DataView>(myUuid, myDataview, myRowCount, myMissingData);
    }

    @Override
    public Response<String, DataView> openDataView(String uuid) {
        Response<String, DataView> response;
        try {
            DataView dv;

            Users.validateUser();

            //TODO: Currently keeping prebuilds that have not yet been fully
            // persisted inside a map so we can return accurate dataviews to client without
            // having to wait for persistence. Level 3 cache basically.
            //will throw NPE if uuid is null
            if(prebuildToTemplateQueue.containsKey(uuid)){
                dv = prebuildToTemplateQueue.get(uuid);
            }
            else {
                dv = retrieveDataView(uuid);
                // **** Cancellation check point ****
                TaskHelper.checkForCancel();
                //restoreSelectionsFromCache(dv);
                DataViewDef meta = dv.getMeta();
                ParameterHelper.initializeParameterUse(meta.getParameterList(), meta.getDataTree(), meta.getFieldList());
                prepareDataViewForSerialisation(dv);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("User %s started to open dataview %s, uuid %s",
                                        Format.value(CsiSecurityManager.getUserName()),
                                        Format.value(dv.getName()), Format.value(dv.getUuid())));
            }
            response = new Response<String, DataView>(uuid, dv);
        } catch (Exception myException) {
            response = new Response<String, DataView>(uuid, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Dataview %s, uuid %s, sent to the front end by User %s",
                    Format.value(response.getResult().getName()),
                    Format.value(response.getResult().getUuid()),
                    Format.value(CsiSecurityManager.getUserName())));
        }
        return response;
    }

    private void prepareDataViewForSerialisation(DataView dv) throws CentrifugeException {
        if (DataCacheHelper.cacheExists(dv.getUuid())) {

            CsiMap<String, String> props = dv.getClientProperties();
            if (props == null) {
                props = new CsiMap<String, String>();
                dv.setClientProperties(props);
            }
            TaskHelper.checkForCancel();

            dv.setLastOpenDate(new Date());
        } else {
            dv.setNeedsRefresh(true);
            SecurityHelper.resetSecurityInfo(dv);
        }
    }

    @Override
   @Operation
    public Response<String, DriverConfigInfo> getConnectionUIConfigByKey(String keyIn) {

        try {

            if ((keyIn == null) || keyIn.equals("")) {
                return new Response<String, DriverConfigInfo>(keyIn, ServerMessage.MISSING_DRIVER_KEY);
            }

            DBConfig dbConfig = Configuration.getInstance().getDbConfig();
            DriverList driversList = dbConfig.getDrivers();

            for (JdbcDriver driver : driversList.getDrivers()) {
                String driverKeyLower = driver.getKey().toLowerCase();
                String keyInLower = keyIn.toLowerCase();
                if ((driverKeyLower != null) && driverKeyLower.equals(keyInLower)) {
                    return new Response<String, DriverConfigInfo>(keyIn, driver.getUiConnectionConfig());
                }
            }

            return new Response<String, DriverConfigInfo>(keyIn, ServerMessage.DRIVER_LOCATE_ERROR, Format.value(keyIn) + ".");

        } catch (Exception myException) {

            return new Response<String, DriverConfigInfo>(keyIn, ServerMessage.CONNECTOR_CONFIG_ERROR, Format.value(myException));
        }
    }

    @Override
   @Operation
    public LinkupDataTransfer addLinkupInformation(LinkupDataTransfer linkupDataIn) throws CentrifugeException {

        List<FieldDef> myNewFields = linkupDataIn.getNewFields();

        try {

            DataView myDataView = validateLinkupChange("ADD", linkupDataIn);
            DataViewDef myDataViewDef = myDataView.getMeta();

            SynchronizeChanges.addToLinkupList(myDataViewDef, linkupDataIn.getObjectOfInterest(), myNewFields);

            //CsiPersistenceManager.merge(DataViewHelper.fixupPersistenceLinkage(myDataView));
            CsiPersistenceManager.merge(myDataView);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();

            DataCacheBuilder.applyChangesToCache(myDataView);

        } catch (Exception myException) {

            LOG.error("Caught exception adding Linkup", myException);

            throw new CentrifugeException(myException);
        }

        return linkupDataIn;
    }

    private DataView validateLinkupChange(String activityIn, LinkupDataTransfer linkupDataIn)
            throws CentrifugeException {

        DataView myDataView = null;

        if (null != linkupDataIn) {

            LinkupMapDef myLinkupMap = linkupDataIn.getObjectOfInterest();

            if (null != myLinkupMap) {

                String myDataViewId = linkupDataIn.getDataViewUuid();

                if (null == myDataViewId) {

                    throw new CentrifugeException("Dataview ID not provided!");
                }

                if (!CsiSecurityManager.isAuthorized(myDataViewId, AclControlType.EDIT)) {
                    throw new CentrifugeException("Not authorized to edit this Dataview");
                }

                myDataView = CsiPersistenceManager.findObject(DataView.class, myDataViewId);

                if (null == myDataView) {

                    throw new CentrifugeException("Dataview not found: " + myDataViewId);
                }

                if (_doDebug) {
                  LOG.debug("-- -- --  " + activityIn + " linkup definition for DataView -- " + myDataViewId);
               }

                DataViewDef myDataViewDef = myDataView.getMeta();

                if (null == myDataViewDef) {

                    throw new CentrifugeException("Dataview broken: " + myDataViewId);
                }
            } else {

                throw new CentrifugeException("No linkup map passed in.");
            }
        } else {

            throw new CentrifugeException("No linkup data passed in.");
        }

        return myDataView;
    }

    @Override
   @Operation
    public LinkupDataTransfer updateLinkupInformation(LinkupDataTransfer linkupDataIn) throws CentrifugeException {

        List<FieldDef> myNewFields = linkupDataIn.getNewFields();
        List<FieldDef> myDiscardedFields = linkupDataIn.getDeletedFields();

        try {

            DataView myDataView = validateLinkupChange("UPDATE", linkupDataIn);

            SynchronizeChanges.removeFromLinkupList(myDataView.getMeta(), linkupDataIn.getObjectOfInterest(), myDiscardedFields);
//            myDataView = CsiPersistenceManager.merge(myDataView);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();

            SynchronizeChanges.addToLinkupList(myDataView.getMeta(), linkupDataIn.getObjectOfInterest(), myNewFields);
//            myDataView = CsiPersistenceManager.merge(myDataView);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();

            DataCacheBuilder.applyChangesToCache(myDataView);

        } catch (Exception myException) {

            LOG.error("Caught exception updating Linkup", myException);

            throw new CentrifugeException(myException);
        }

        return linkupDataIn;
    }

    @Override
   @Operation
    public LinkupDataTransfer removeLinkupInformation(LinkupDataTransfer linkupDataIn) throws CentrifugeException {

        List<FieldDef> myDiscardedFields = linkupDataIn.getDeletedFields();

        try {

            DataView myDataView = validateLinkupChange("REMOVE", linkupDataIn);
            DataViewDef myDataViewDef = myDataView.getMeta();

            SynchronizeChanges.removeFromLinkupList(myDataViewDef, linkupDataIn.getObjectOfInterest(), myDiscardedFields);

            //CsiPersistenceManager.merge(DataViewHelper.fixupPersistenceLinkage(myDataView));
            CsiPersistenceManager.merge(myDataView);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();

            DataCacheBuilder.applyChangesToCache(myDataView);

        } catch (Exception myException) {

            LOG.error("Caught exception removing Linkup", myException);

            throw new CentrifugeException(myException);
        }

        return linkupDataIn;
    }

    /**
     * A shadow copy is used a clone of the existing DV used to handle reverts on the client side.
     *
     * @param uuid
     * @return
     * @throws CentrifugeException
     */
    @Override
   @Operation
    @Interruptable
    public DataView getShadowCopyOfDataView(@QueryParam(value = "uuid") String uuid) throws CentrifugeException {

        DataView dv = retrieveDataView(uuid);

        // **** Cancellation check point ****
        TaskHelper.checkForCancel();

        prepareDataViewForSerialisation(dv);

        LOG.info(String.format("User %s retrived shadow copy of %s, uuid %s",
                                Format.value(CsiSecurityManager.getUserName()),
                                Format.value(dv.getName()), Format.value(dv.getUuid())));

        return dv;
    }

    @Override
    public void saveFilters(String dvUuid, Set<Filter> filters) throws CentrifugeException {
        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {
            throw new CentrifugeException("Not authorized to edit dataview");
        }
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);

        Iterator<Filter> filtersIterator = filters.iterator();

        DataViewDef dataViewDef = dataView.getMeta();
        List<Filter> dvFilters = dataViewDef.getFilters();
        //We create a threadsafe list that will then be used for filters
        CopyOnWriteArrayList<Filter> safeList = new CopyOnWriteArrayList<Filter>();
        safeList.addAll(dvFilters);

        for (Filter filter : safeList) {
            if(!filters.contains(filter)){
                dvFilters.remove(filter);
            }
        }

        while(filtersIterator.hasNext()){
            Filter filter = filtersIterator.next();
            if (dvFilters.contains(filter)) {
                //Merge filters to update current session
                CsiPersistenceManager.merge(filter);
            }
            else{
                dvFilters.add(filter);
            }
        }

        if((dataView.getMeta().getModelDef() != null) && (dataView.getMeta().getModelDef().getVisualizations() != null)){
            for(VisualizationDef viz: dataView.getMeta().getModelDef().getVisualizations()){
                if((viz.getFilter() != null) && !dvFilters.contains(viz.getFilter())){
                    viz.setFilter(null);
                    //Merge viz's that changed in current session
                    CsiPersistenceManager.merge(viz);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Operation
    public Response<String, Map<String, DataView>> getDataviewsByName(DataviewRequest request) throws CentrifugeException {

        List<DataView> dataViews = AclRequest.findResourcesByName(DataView.class, request.getDataviewName(), request.getAccessMode());
        Response<String, Map<String, DataView>> response = new Response<String, Map<String, DataView>>();

        Map<String, DataView> dataViewOwnerMap = new HashMap<String, DataView>();
        for(DataView dataView: dataViews){

            String owner = CsiSecurityManager.retrieveOwner(dataView.getUuid());

            if((request.getUserName() == null) || request.getUserName().isEmpty()){
                dataViewOwnerMap.put(owner, dataView);
            } else if(owner.equals(request.getUserName())){
                dataViewOwnerMap.clear();
                dataViewOwnerMap.put(owner, dataView);
                break;
            }

        }

        response.setResult(dataViewOwnerMap);
        response.setKey(request.getDataviewName());

        response.setSuccess(true);

        return response;
    }

    @Override
    public DataModelDef reorderWorksheet(int newIndex, int oldIndex, String dvUuid) throws CentrifugeException {

        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }
        List<WorksheetDef> worksheets = null;
        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);

        if (null != myDataView) {

            worksheets = myDataView.getMeta().getModelDef().getWorksheets();
            CopyOnWriteArrayList<WorksheetDef> safeList = new CopyOnWriteArrayList<WorksheetDef>();

            safeList.addAll(worksheets);

            WorksheetDef reorderedWorksheet = worksheets.get(oldIndex);

            safeList.remove(reorderedWorksheet);

            safeList.add(newIndex, reorderedWorksheet);

            List<WorksheetDef> finalList = new ArrayList<WorksheetDef>();
            finalList.addAll(safeList);
            myDataView.getMeta().getModelDef().setWorksheets(finalList);

            return CsiPersistenceManager.merge(myDataView.getMeta().getModelDef());
        }

        return null;
    }

    @Override
   public Response<String, List<FieldDef>> updateFieldList(String dataViewIdIn, List<FieldDef> newListIn,
                                                            List<FieldDef> changeListIn, List<FieldDef> discardListIn,
                                                            List<String> fieldOrderIn) {

        if (null != dataViewIdIn) {

           if (!CsiSecurityManager.isAuthorized(dataViewIdIn, AclControlType.EDIT)) {

                return new Response<String, List<FieldDef>>(dataViewIdIn, ServerMessage.DATAVIEW_EDIT_ERROR);
            }

            try {

                DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dataViewIdIn);

                if (null != myDataView) {

                    DataViewDef myMeta = myDataView.getMeta();
                    FieldListAccess myModel = myMeta.getFieldListAccess();
                    Map<String, FieldDef> myFullMap = new TreeMap<String, FieldDef>();
                    Map<String, CsiDataType> myOldPreCalculatedMap = myModel.getPreCaculatedColumnMap();
                    Map<String, CsiDataType> myNewPreCalculatedMap = null;
                    Map<String, CsiDataType> myRetypeMap = null;

                    myMeta.clearStorageTypesFlag();

//                    myRecoveryPackage = DataCacheHelper.buildRecoveryPackage(myDataView);

                    for (FieldDef myField : myModel.getDependentFieldDefs()) {

                        myFullMap.put(myField.getLocalId(), myField);
                    }
                    for (FieldDef myField : newListIn) {

                        myFullMap.put(myField.getLocalId(), myField);
                    }
                    for (FieldDef myField : changeListIn) {

                        myFullMap.put(myField.getLocalId(), myField);
                    }
                    for (FieldDef myField : discardListIn) {

                        myFullMap.remove(myField.getLocalId());
                    }

                    DataViewHelper.handleChangingNames(myModel, newListIn, changeListIn, discardListIn);

                    if (null != discardListIn) {

                        for (FieldDef myItem : discardListIn) {

                            DataViewHelper.deleteFieldDef(myDataView, myItem);
                        }
                    }

                    if (null != changeListIn) {

                        myRetypeMap = new HashMap<String, CsiDataType>();
                        for (FieldDef myItem : changeListIn) {

                            if (DataViewHelper.updateFieldDef(myDataView, myItem)) {

                                myRetypeMap.put(myItem.getLocalId(), myItem.getValueType());
                            }
                        }
                    }

                    if (null != newListIn) {

                        for (FieldDef myItem : newListIn) {

                            DataViewHelper.addFieldDef(myDataView, myItem);
                        }
                    }

                    DataViewHelper.checkForCycles(myModel, myFullMap.values());
                    DataViewHelper.updateFieldOrder(myModel, fieldOrderIn);

                    myNewPreCalculatedMap = myModel.getPreCaculatedColumnMap();

                    DataViewHelper.invalidateAffectedVisualizations(myDataView, changeListIn,
                                                                    visualizationActionsService);

                    if (((null != myRetypeMap) && !myRetypeMap.isEmpty())
                            || ((null != myOldPreCalculatedMap) && !myOldPreCalculatedMap.isEmpty())
                            || ((null != myNewPreCalculatedMap) && !myNewPreCalculatedMap.isEmpty())) {

                        DataCacheBuilder.applyChangesToCache(myDataView, myOldPreCalculatedMap,
                                                            myNewPreCalculatedMap, myRetypeMap);


                    } else {

                        DataCacheBuilder.applyChangesToCache(myDataView);

                    }

                    myModel.clearDirtyFlags();
                    myDataView.updateLastUpdate();
                    CsiPersistenceManager.merge(myDataView);
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();

                    myDataView = CsiPersistenceManager.findObject(DataView.class, dataViewIdIn);
//                    DataCacheHelper.discardRecoveryPackage(myRecoveryPackage);

                    return new Response<String, List<FieldDef>>(dataViewIdIn, new ArrayList<FieldDef>(myDataView.getFieldList()));

                } else {

                    return new Response<String, List<FieldDef>>(dataViewIdIn, ServerMessage.DATAVIEW_LOCATE_ERROR, Format.value(dataViewIdIn) + ".");
                }

            } catch (Exception myException) {

//                restoreDataView(dataViewIdIn, myRecoveryPackage, false);
                LOG.error(myException.getMessage(), myException);

                return new Response<String, List<FieldDef>>(dataViewIdIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
            }

        } else {

            return new Response<String, List<FieldDef>>(dataViewIdIn, ServerMessage.DATAVIEW_LOCATE_ERROR, "<null>.");
        }
    }

    public void clearPrebuiltDataView(String uuid) throws CentrifugeException {
        if(CsiPersistenceManager.isPrebuilt(uuid)) {
            CsiPersistenceManager.discardPrebuiltDataView(uuid);
        }

    }

    @Override
   public Filter createSelectionFilter(String dataViewUuid, String vizUuid, String filterName, Selection newSelection) throws CentrifugeException {
        Filter filter = new Filter();
        filter.setName(filterName);

        FilterDefinition filterDefinition = new FilterDefinition();

        DataView dv = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
        VisualizationDef vizDef = ModelHelper.find(VisualizationDef.class, vizUuid);

        if (vizDef instanceof MapViewDef) {
			newSelection = MapServiceUtil.getMapSelection(dataViewUuid, vizUuid);
    	}
        vizDef.getSelection().setFromSelection(newSelection);
        AbstractBroadcastStorageService.instance().clearBroadcast(vizDef.getUuid());
        selectionBroadcastCache.addSelection(vizDef.getUuid(), getSelectionFromVizDef(vizDef));

        SelectionToRowsCoverterFactory selectionToRowsCoverterFactory 	= new SelectionToRowsCoverterFactory(dv, vizDef);

        Selection selection;
        if (vizDef instanceof RelGraphViewDef) {
            selection = getGraphSelection(vizDef.getUuid());
        } else {
            selection = selectionBroadcastCache.getSelection(vizUuid);
        }

        Set<Integer> rows = selectionToRowsCoverterFactory.create().convertToRows(selection, false);
        List<Object> values = new ArrayList<Object>();
        for (Integer row : rows) {
        	values.add(row);
        }

        FilterExpression filterExpression = new FilterExpression();
        filterExpression.setOrdinal(0);
        filterExpression.setExpressionId(1);
        filterExpression.setNegated(false);
        filterExpression.setOperator(RelationalOperator.INCLUDED);
        MultiValueDefinition valueDefinition = new MultiValueDefinition();
        valueDefinition.setValues(values);
        valueDefinition.setDataType(CsiDataType.Integer);
        filterExpression.setValueDefinition(valueDefinition);
        filterExpression.setSelectionFilter(true);

        filterDefinition.getFilterExpressions().add(filterExpression);

        filter.setFilterDefinition(filterDefinition);

        List<Filter> filters = dv.getMeta().getFilters();
        filters.add(filter);

        return filter;
    }

   @Override
   public Boolean filterNameExists(String dataViewUuid, String filterName) throws CentrifugeException {
      Boolean result = Boolean.FALSE;
      DataView dataview = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
      List<Filter> filters = dataview.getMeta().getFilters();

      for (Filter filter : filters) {
         if (filter.getName().equals(filterName)) {
            result = Boolean.TRUE;
            break;
         }
      }
      return result;
   }

    /*
    @Operation
    public List<ResourceBasics> listUserDataViewBasics() throws CentrifugeException {

        List<ResourceBasics> myResults = AclRequest.listUserDvs();

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<ResourceBasics> listUserDataViewBasics(AclControlType accessModeIn) throws CentrifugeException {

        List<ResourceBasics> myResults = AclRequest.listAuthorizedUserDvs(new AclControlType[]{accessModeIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }
     */
    /*
    @Operation
    @Interruptable
    public String getUniqueDataviewName(@QueryParam(value = "dvName") String dvName) throws CentrifugeException {
        return AclRequest.makeUniqueResourceName(DataView.class, dvName);
    }
     */
    /*
    @Operation
    public Response<String, DataView> editDataview(String dataViewIdIn, DataSetOp dataTreeIn, List<DataSourceDef> sourceListIn, List<FieldDef> fieldListIn, List<QueryParameterDef> parameterListIn) {

        if (!CsiSecurityManager.isAuthorized(dataViewIdIn, AclControlType.getFullDataViewAccess())) {

            return new Response<String, DataView>(dataViewIdIn, "Access denied.  Not authorized to edit dataview.");
        }

        try {
            DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dataViewIdIn);
            DataViewDef myMeta = myDataView.getMeta();
            List<DataSetOp> myOpDiscardList = listDataSetOps(myDataView);
            List<FieldDef> myFieldDiscardList = new ArrayList<FieldDef>();
            List<QueryParameterDef> myParameterDiscardList = new ArrayList<QueryParameterDef>();
            List<DataSourceDef> mySourceDiscardList = new ArrayList<DataSourceDef>();

            dataTreeIn = dataTreeIn.finalizeForUpload();

            if  (null != parameterListIn) {

                for (QueryParameterDef myParameter : parameterListIn) {

                    myParameter.regenerateUuid();
                }
            }

            if (_doDebug) {
                LOG.debug("\nBegin Saving DataView \"" + myDataView.getName() + "\"\n");
                LOG.debug(myDataView.debug("  "));
            }

            FieldReferenceValidator fieldReferenceValidator = new FieldReferenceValidator(myMeta);
            fieldReferenceValidator.isValid();

            myMeta.getModelDef().setFieldDefs(Update.createOrUpdateList(myMeta.getModelDef().getFieldDefs(), fieldListIn, myFieldDiscardList));
            myMeta.setDataSetParameters(Update.createOrUpdateList(myMeta.getDataSetParameters(), parameterListIn, myParameterDiscardList));
            myMeta.setDataSources(Update.createOrUpdateList(myMeta.getDataSources(), sourceListIn, mySourceDiscardList));

            myDataView = CsiPersistenceManager.merge(myDataView);

            if (_doDebug) LOG.debug("\nDone Saving DataView \"" + myDataView.getName() + "\"\n");

            return new Response<String, DataView>(dataViewIdIn, myDataView);
        } catch (Exception myException) {

            LOG.error(myException.getMessage(), myException);

            return new Response<String, DataView>(dataViewIdIn, ServerMessage.CAUGHT_EXCEPTION, Display.value(myException));
        }
    }
     */
    /*
    @Operation
    public Response<String, LinkupResponse> mergeLinkup(String dataViewUuidIn, String linkupUuidIn) {

        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuidIn);
        DataView myLinkupDataView = CsiPersistenceManager.findObject(DataView.class, linkupUuidIn);
        DataViewHelper myHelper = new DataViewHelper(visualizationActionsService);
        return myHelper.mergeLinkup(myDataView, myLinkupDataView);
    }

    @Operation
    public void discardLinkup(String dataViewUuidIn, String linkupUuidIn) {

    }
     */
    private static final String ASSET_ID = "assetID";

    private static final String TABLE_REQUEST = "0";

    private static final String GRAPH_REQUEST = "1";

    private static final String CHART_REQUEST = "2";

    private static final String TIMELINE_REQUEST = "3";

    private static final String DRILL_REQUEST = "drill";

    private static final String ROW_LIST_PATTERN = "\\s*+\\d++\\s*+(,\\s*+\\d++\\s*+)*+";

    private static final String REQUEST_DATA = "requestData";

    private static final String KEY_RESOURCE_CONNECTION = "KeyResourceConnection";

    private static String[] AdminRoles = {"administrators"};

    @Autowired
    private BeanFactory beanFactory;

    @Inject
    private FilterActionsService filterActionsService;

    public FilterActionsService getFilterActionsService() {
        return filterActionsService;
    }

    public void setFilterActionsService(FilterActionsService filterActionsService) {
        this.filterActionsService = filterActionsService;
    }

    @Inject
    private SQLFactory sqlFactory;

    private SQLFactory getSqlFactory() {
        return sqlFactory;
    }

//    @Override
//    public void initMarshaller(XStream xstream) {
//        xstream.alias("pageInfo", PagingInfo.class);
//        xstream.alias("spinoffRequest", SpinoffRequest.class);
//        xstream.alias("field", SpinoffField.class);
//        xstream.alias("tuple", SpinoffTuple.class);
//        xstream.alias("rows", RowDataSet.class);
//        xstream.alias("row", Map.class);
//    }

//    private void restoreSelectionsFromCache(DataView dv) {
//        for (VisualizationDef visualizationDef : dv.getMeta().getModelDef().getVisualizations()) {
//            visualizationDef.getSelection().setFromSelection(selectionBroadcastCache.getSelection(visualizationDef.getUuid()));
//        }
//    }

    @Override
   public List<FieldDef> getLinkupDiscardedFields(String dataViewIdIn) {
        DataView myDataView = CsiPersistenceManager.findObjectAvoidingSecurity(DataView.class, dataViewIdIn);
        if (myDataView == null) {
            return null;
        } else {
            Map<String, FieldDef> discardFieldCandidates = myDataView.getFieldList().stream().filter(fieldDef -> fieldDef.getFieldType() == FieldType.LINKUP_REF).collect(Collectors.toMap(fieldDef -> fieldDef.getUuid(), fieldDef -> fieldDef));
            List<String> fieldsUsed = DataViewHelper.testFieldReferences(myDataView.getMeta(), discardFieldCandidates.keySet().stream().collect(Collectors.toList()));
            return discardFieldCandidates.keySet().stream().filter(key -> !fieldsUsed.contains(key)).map(key -> discardFieldCandidates.get(key)).collect(Collectors.toList());
        }
    }

    @Override
   public void removeLinkup(LinkupDataTransfer linkupDataIn) throws CentrifugeException {
        try {
            DataView myDataView = validateLinkupChange("REMOVE", linkupDataIn);
            DataViewDef myDataViewDef = myDataView.getMeta();
            SynchronizeChanges.removeLinkup(myDataViewDef, linkupDataIn.getObjectOfInterest());
            //CsiPersistenceManager.merge(DataViewHelper.fixupPersistenceLinkage(myDataView));
            CsiPersistenceManager.merge(myDataView);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();
            DataCacheBuilder.applyChangesToCache(myDataView);
        } catch (Exception myException) {
            LOG.error("Caught exception removing Linkup", myException);
            throw new CentrifugeException(myException);
        }
    }

    @Override
   public void removeLinkupDiscardedFields(LinkupDataTransfer linkupDataIn) throws CentrifugeException {
        List<FieldDef> myDiscardedFields = linkupDataIn.getDeletedFields();
        try {
            DataView myDataView = validateLinkupChange("REMOVE", linkupDataIn);
            DataViewDef myDataViewDef = myDataView.getMeta();
            SynchronizeChanges.removeLinkupDiscardedFields(myDataViewDef, myDiscardedFields);
            //CsiPersistenceManager.merge(DataViewHelper.fixupPersistenceLinkage(myDataView));
            CsiPersistenceManager.merge(myDataView);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();
            DataCacheBuilder.applyChangesToCache(myDataView);
        } catch (Exception myException) {
            LOG.error("Caught exception removing Linkup", myException);
            throw new CentrifugeException(myException);
        }
    }

    @Override
    public long getNewAnnotationCount(String dvUuid, Date date) {
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid, AclControlType.READ, true, false);
        List<AnnotationCardDef> annotationCardDefs = dataView.getMeta().getModelDef().getAnnotationCardDefs();
        long newAnnotationCount = annotationCardDefs.stream().filter(annotationCardDef -> !annotationCardDef.getCreatorUserName().equals(CsiSecurityManager.getUserName())).filter(annotationCardDef -> annotationCardDef.getCreateTime().after(date)).count();
        return newAnnotationCount;
    }
}
