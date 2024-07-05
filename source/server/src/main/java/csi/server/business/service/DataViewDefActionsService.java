package csi.server.business.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;

import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.security.queries.Users;
import csi.server.business.helper.DataViewFactory;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.DeepCloner;
import csi.server.business.helper.ModelHelper;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.Response;
import csi.server.common.dto.SpinoffRequest;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.ResourceChoiceCriteria;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.linkup.TemplateResponse;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SpinoffField;
import csi.server.common.model.SpinoffTuple;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.linkup.LooseMapping;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.util.FieldReferenceValidator;
import csi.server.ws.actions.PagingInfo;
import csi.shared.gwt.exception.SecurityException;
import csi.startup.CleanUpThread;

@Service(path = "/actions/dataviewdef")
public class DataViewDefActionsService extends AbstractService implements DataViewDefActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(DataViewDefActionsService.class);

   private static boolean _doDebug = LOG.isDebugEnabled();

    @Autowired
    private DataViewActionsService dataivewActionsService;

    public DataViewDefActionsService() {
        super();
    }

    @Override
    public void initMarshaller(XStream xstream) {
        xstream.alias("pageInfo", PagingInfo.class);
        xstream.alias("spinoffRequest", SpinoffRequest.class);
        xstream.alias("field", SpinoffField.class);
        xstream.alias("tuple", SpinoffTuple.class);
    }

    @Operation
    public List<String> listAllTemplateNames() throws CentrifugeException {
        List<String> myResults = AclRequest.listAllTemplateNames();

        return (null != myResults) ? myResults : new ArrayList<String>();
    }

    @Operation
    public List<String> listUserTemplateNames() throws CentrifugeException {
        List<String> myResults = AclRequest.listUserTemplateNames();

        return (null != myResults) ? myResults : new ArrayList<String>();
    }

    @Operation
    public List<ResourceBasics> listUserTemplateBasics() throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listUserTemplates();

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<ResourceBasics> listUserTemplateBasics(AclControlType accessModeIn) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedUserTemplates(new AclControlType[]{accessModeIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<ResourceBasics> listTemplateBasics(AclControlType accessModeIn) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedTemplates(new AclControlType[]{accessModeIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<ResourceBasics> listTemplateExportBasics() throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedTemplateExports();

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<ResourceBasics> listLinkupTemplateBasics() throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedTemplates(new AclControlType[]{AclControlType.READ});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<ResourceBasics> listTemplateEditBasics() throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedTemplatesForEdit();

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<List<ResourceBasics>> getTemplateOverWriteControlLists() throws CentrifugeException {

        List<ResourceBasics> mySelectionList = AclRequest.listAuthorizedUserTemplates(new AclControlType[]{AclControlType.DELETE});
        List<ResourceBasics> myRejectionList = AclRequest.listUserTemplates();
        List<ResourceBasics> myConflictList = AclRequest.listAuthorizedUserTemplates(new AclControlType[]{AclControlType.DELETE});

        return ModelHelper.generateOverWriteControlLists(mySelectionList, myRejectionList, myConflictList);
    }

    @Operation
    public List<String> listTemplateNames(AclControlType accessModeIn) throws CentrifugeException {

        return AclRequest.listAuthorizedTemplateNames(new AclControlType[]{accessModeIn});

    }

    @Operation
    public List<ResourceBasics> listSampleBasics(AclControlType accessModeIn) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedSamples(new AclControlType[]{accessModeIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<List<ResourceBasics>> getSampleOverWriteControlLists() throws CentrifugeException {

        List<ResourceBasics> mySelectionList = AclRequest.listAuthorizedUserSamples(new AclControlType[]{AclControlType.DELETE});
        List<ResourceBasics> myRejectionList = AclRequest.listUserSamples();
        List<ResourceBasics> myConflictList = AclRequest.listAuthorizedUserSamples(new AclControlType[]{AclControlType.DELETE});

        return ModelHelper.generateOverWriteControlLists(mySelectionList, myRejectionList, myConflictList);
    }

    @Operation
    public List<String> listSampleNames(AclControlType accessModeIn) throws CentrifugeException {

        return AclRequest.listAuthorizedSampleNames(new AclControlType[]{accessModeIn});
    }

    @Operation
    @Interruptable
    public List<FieldDef> listFieldDefs(@QueryParam(value = "uuid") String uuid) throws CentrifugeException {
        DataViewDef def = CsiPersistenceManager.findObject(DataViewDef.class, uuid);
        if (def == null) {
            throw new CentrifugeException("Dataview definition does not exist. uuid=" + uuid);
        }
        return new ArrayList<FieldDef>(def.getFieldList());
    }

    @Operation
    @Interruptable
    public Response<String, TemplateResponse> getLinkupTemplate(String templateIdIn) {

        return getTemplate(templateIdIn, AclControlType.EMBEDDED);
    }

    @Operation
    @Interruptable
    public Response<String, TemplateResponse> getLinkupTemplate(LinkupMapDef linkupIn) {

        TemplateResponse myResponse = null;

        String myUuid = linkupIn.getTemplateUuid();
        String myName = linkupIn.getTemplateName();
        String myOwner = linkupIn.getTemplateOwner();
        List<LooseMapping> myFieldList = linkupIn.getFieldsMap();

        try {

            myResponse = locateTemplate(myUuid, myName, myOwner, myFieldList, AclControlType.EMBEDDED);

        } catch (Exception myException) {

        }
        return new Response(myUuid, myResponse);
    }

    @Operation
    @Interruptable
    public Response<String, TemplateResponse> getTemplate(String templateIdIn, AclControlType modeIn) {


        try {

            TemplateResponse myTemplateResponse = locateTemplate(templateIdIn, null, null, null, modeIn);

            if (null != myTemplateResponse) {

                AclRequest.recordAccess(templateIdIn);
                return new Response<String, TemplateResponse>(templateIdIn, myTemplateResponse);

            } else {

                return new Response<String, TemplateResponse>(templateIdIn,
                        ServerMessage.TEMPLATE_ACCESS_ERROR, "???" + ",\n UUID: " + Format.value(templateIdIn));
            }

        } catch (Exception myException) {

            return new Response<String, TemplateResponse>(templateIdIn, ServerMessage.CAUGHT_EXCEPTION,
                    Format.value(myException));
        }
    }

    @Operation
    public DataViewDef save(@PayloadParam DataViewDef def) throws CentrifugeException {

        Users.validateUser();
        authorizeDataSources(def.getDataSources());

        String uuid = def.getUuid();
        uuid = (uuid != null) ? uuid.trim() : null;

        if (CsiPersistenceManager.objectExists(DataViewDef.class, uuid)) {
            LOG.info(String.format("Request to save data view def with uuid %s", uuid));

            if (!CsiSecurityManager.isAuthorized(uuid, AclControlType.EDIT)) {
                throw new CentrifugeException("Not authorized to edit Dataview");
            }
        } else {
            LOG.info("Request to save new Dataview with name '" + def.getName() + "'");
        }

        TaskHelper.checkForCancel();
        def.clearAllRuntimeValues();

        DataViewHelper.fixupPersistenceLinkage(def);
        return CsiPersistenceManager.merge(def);
    }

    @Operation
    public Response<String, DataViewDef> editTemplate(DataViewDef templateIn) {

        String myId = (null != templateIn) ? templateIn.getUuid() : null;

        if (null != myId) {

            try {

                DataViewDef myOldTemplate = CsiPersistenceManager.findObject(DataViewDef.class, templateIn.getUuid());

                if (!CsiSecurityManager.isAuthorized(myId, AclControlType.EDIT)) {

                    return new Response<String, DataViewDef>(myId, ServerMessage.TEMPLATE_EDIT_ERROR);
                }

                try {

                    FieldReferenceValidator fieldReferenceValidator = new FieldReferenceValidator(templateIn);
                    fieldReferenceValidator.isValid();

                } catch (Exception myException) {

                    LOG.error(myException.getMessage(), myException);

                    return new Response<String, DataViewDef>(myId, ServerMessage.TEMPLATE_VALIDATION_ERROR,
                            Format.value(templateIn.getName()) +"\"\n" + Format.value(myException));
                }

                try {

                    dataivewActionsService.clearPrebuiltDataView(myId);


                    DataViewDef myOldMeta = myOldTemplate;
                    DataViewDef myNewMeta = templateIn;
                    DataSetOp myOldDataTree = myOldMeta.getDataTree();
                    DataSetOp myNewDataTree = myNewMeta.getDataTree();

                    DataViewHelper.fixupPersistenceLinkage(myNewMeta);

                    myNewMeta.setDataTree(null);
                    CsiPersistenceManager.merge(myNewMeta);
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();
                    if (null != myOldDataTree) {

                        CsiPersistenceManager.deleteObject(DataSetOp.class, myOldDataTree.getUuid());
                        CsiPersistenceManager.commit();
                        CsiPersistenceManager.begin();
                    }
                    myNewMeta.setDataTree(myNewDataTree);
                    DataViewDef mySavedTemplate = CsiPersistenceManager.merge(myNewMeta);

                    {
                        //Create a cache of newly edited template
                        CacheDataViewRunnable runnable = new CacheDataViewRunnable(mySavedTemplate);
                        Executors.newSingleThreadExecutor().execute(runnable);
                    }

                    if (_doDebug) {
                     LOG.debug("\nDone Saving Template \"" + templateIn.getName() + "\"\n");
                  }

                    return new Response<String, DataViewDef>(myId, mySavedTemplate);

                } catch (Exception myException) {

                    LOG.error(myException.getMessage(), myException);

                    CsiPersistenceManager.merge(myOldTemplate);

                    return new Response<String, DataViewDef>(myId, ServerMessage.TEMPLATE_SAVE_ERROR,
                            Format.value(templateIn.getName()) + "\n" + Format.value(myException));
                }
            } catch (Exception myException) {


                LOG.error(myException.getMessage(), myException);

                return new Response<String, DataViewDef>(myId, ServerMessage.TEMPLATE_SAVE_ERROR,
                        Format.value(templateIn.getName()) + "\n" + Format.value(myException));
            }

        } else {

            return new Response<String, DataViewDef>(myId, ServerMessage.TEMPLATE_UUID_ERROR);
        }
    }

    @Operation
    @Interruptable
    public DataViewDef cloneObject(@QueryParam(value = "uuid") String uuid) throws CentrifugeException, IOException {
        return ModelHelper.cloneObject(DataViewDef.class, uuid);
    }

    public DataViewDef cloneDataViewDef(String uuid) throws CentrifugeException, IOException {
        return cloneObject(uuid);
    }

    @Operation
    public DataViewDef saveAs(@QueryParam(value = "uuid") String uuid, @QueryParam(value = "newName") String newName) throws CentrifugeException {

        LOG.info(String.format("Saving template with uuid %s as %s", uuid, newName));

        Users.validateUser();

        DataViewHelper helper = new DataViewHelper();
        DataViewDef snapdef = CsiPersistenceManager.findObject(DataViewDef.class, uuid);
        authorizeDataSources(snapdef.getDataSources());

        TaskHelper.checkForCancel();

        DataViewDef cloned = helper.cloneModelObject(snapdef, DeepCloner.CloneType.NEW_ID);
        cloned.resetDates();
        cloned.setTemplate(true);
        cloned.setName(newName);

        TaskHelper.checkForCancel();

        DataViewDef olddef = AclRequest.findOwnedResourceByName(DataViewDef.class, newName);
        if (olddef != null) {
            LOG.info(String.format("Request to overwrite data view def with uuid %s", uuid));

            if (!CsiSecurityManager.isAuthorized(olddef.getUuid(), AclControlType.DELETE)) {
                throw new CentrifugeException("Not authorized to edit Dataview");
            }

            cloned.setUuid(olddef.getUuid());
            DataViewHelper.fixupPersistenceLinkage(cloned);
            cloned = CsiPersistenceManager.merge(cloned);
        } else {
            DataViewHelper.fixupPersistenceLinkage(cloned);
            CsiPersistenceManager.persist(cloned);
        }

        return cloned;
    }

    public DataViewDef saveDataViewDefAs(String uuid, String newName) throws CentrifugeException {
        return saveAs(uuid, newName);
    }

   public void authorizeDataSources(List<DataSourceDef> defs) throws CentrifugeException {
      for (DataSourceDef def : defs) {
         if (!canCreateConnectionType(def.getConnection()).booleanValue()) {
            throw new CentrifugeException("Not authorized to save datasource with connection type: " + def.getConnection().getType());
         }
      }
   }

   @Operation
   public Boolean canCreateConnectionType(ConnectionDef def) {
      // check to make sure current user is allowed
      // to create a dataviewdef with this connection type
      ConnectionFactory factory = null;

      try {
         factory = ConnectionFactoryManager.getInstance().getConnectionFactory(def);
      } catch (CentrifugeException e) {
         e.printStackTrace();
         return Boolean.FALSE;
      }
      if ((factory != null) && (factory.getDriverAccessRole() != null)) {
         return CsiSecurityManager.hasRole(factory.getDriverAccessRole());
      }
      return Boolean.TRUE;
   }

    @Operation
    public String delete(@QueryParam(value = "uuid") String uuidIn) throws CentrifugeException {

        DataViewDef myTemplate = CsiPersistenceManager.findForDelete(DataViewDef.class, uuidIn);

        if (null != myTemplate) {

            if (!CsiSecurityManager.isAuthorized(uuidIn, AclControlType.DELETE)) {

                throw Throwables.propagate(new SecurityException("Not authorized to delete the Template with uuid "
                        + Format.value(uuidIn)));
            }
            LOG.info("Delete Template " + Format.value(myTemplate.getName()) + ", id = " + uuidIn);
            if (!CleanUpThread.scheduleDelete(myTemplate)) {

                throw Throwables.propagate(new SecurityException("Unable to mark for delete the Template with uuid "
                        + Format.value(uuidIn)));
            }
        }
        return uuidIn;
    }

    public String deleteDataViewDef(String uuid) throws CentrifugeException {
        return delete(uuid);
    }

    @Operation
    public DataView createDataViewFromTemplate(@QueryParam(value = "uuid") String templateUUID, @QueryParam(value = "name") String name) throws CentrifugeException {

        Users.validateUser();

        DataViewHelper helper = new DataViewHelper();
        name = helper.makeUniqueDataViewName(name);

        TaskHelper.checkForCancel();

        TaskHelper.reportTaskID();
        return helper.createDataViewfromTemplate(name, templateUUID);
    }

    @Operation
    public List<String> testCoreFieldReferences(String templateUuidIn) {

        return DataViewHelper.testTemplateCoreFieldReferences(templateUuidIn);
    }

    @Operation
    public List<String> testFieldReferences(String templateUuidIn, List<String> fieldUuidsIn) {

        return DataViewHelper.testTemplateFieldReferences(templateUuidIn, fieldUuidsIn);
    }

    @Operation
    public List<String> testFieldReferences(String templateUuidIn) {

        return DataViewHelper.testTemplateFieldReferences(templateUuidIn);
    }

    @Operation
    public List<String> testFieldReferenceAndReturnViz(String templateUuidIn, String fieldUuidIn) {
        return DataViewHelper.testTemplateFieldReferencesAndReturnVis(templateUuidIn, fieldUuidIn);
    }

    public Response<String, DataViewDef> createTemplate(String requestIdIn, String nameIn, String commentsIn,
                                                       AdHocDataSource dataSourceIn, boolean overwriteIn,
                                                       SharingInitializationRequest sharingRequestIn) {


        try {

            Users.validateUser();
            DataViewDef myTemplate = DataViewFactory.createTemplate(nameIn, commentsIn, dataSourceIn, overwriteIn);
            if (null != myTemplate) {

                try {

                    DataViewDef myExistingTemplate = AclRequest.findOwnedResourceByName(DataViewDef.class, nameIn);

                    if (null != myExistingTemplate) {

                        delete(myExistingTemplate.getUuid());
                    }
                    ModelHelper.defineSharing(myTemplate.getUuid(), sharingRequestIn);
                    CsiPersistenceManager.persist(DataViewHelper.fixupPersistenceLinkage(myTemplate));
                    return new Response<String, DataViewDef>(requestIdIn, myTemplate);

                } catch (Exception myException) {

                    return new Response<String, DataViewDef>(requestIdIn, ServerMessage.TEMPLATE_SAVE_ERROR, Format.value(myException));
                }

            } else {

                return new Response<String, DataViewDef>(requestIdIn, ServerMessage.TEMPLATE_CREATE_ERROR);
            }

        } catch (Exception myException) {

            return new Response<String, DataViewDef>(requestIdIn, ServerMessage.DATAVIEW_CREATE_EXCEPTION, Format.value(myException));
        }
    }

    private TemplateResponse locateTemplate(String templateIdIn, String templateNameIn, String ownerIn,
                                            List<LooseMapping> fieldListIn, AclControlType modeIn)
            throws CsiSecurityException {

        if (null != templateIdIn) {

            DataViewDef myTemplate = AclRequest.findResourceById(DataViewDef.class, templateIdIn, modeIn);

            if (null != myTemplate) {

                int myCriteriaMask = validateFieldList(myTemplate, fieldListIn, ResourceChoiceCriteria.UUID.setBit());

                if (0 < myCriteriaMask) {

                    if ((null != templateNameIn) && myTemplate.getName().equals(templateNameIn)) {

                        myCriteriaMask = ResourceChoiceCriteria.NAME.setBit(myCriteriaMask);
                    }
                    if ((null != ownerIn) && (null != myTemplate.getOwner()) && myTemplate.getOwner().equals(ownerIn)) {

                        myCriteriaMask = ResourceChoiceCriteria.OWNER.setBit(myCriteriaMask);
                    }
                    return new TemplateResponse(myTemplate, myCriteriaMask);
                }
            }
        }
        if (null != templateNameIn) {

            DataViewDef mySelection = null;
            int mySelectionCriteria = 0;
            List<DataViewDef> myTemplates = AclRequest.findResourcesByName(DataViewDef.class, templateNameIn, modeIn);
            TemplateResponse myResponse = new TemplateResponse();

            if ((myTemplates != null) && !myTemplates.isEmpty()) {

                for (DataViewDef myTemplate : myTemplates) {

                    int myCriteriaMask = validateFieldList(myTemplate, fieldListIn, ResourceChoiceCriteria.NAME.setBit());

                    if (0 < myCriteriaMask) {

                        if ((null != ownerIn) && (null != myTemplate.getOwner()) && myTemplate.getOwner().equals(ownerIn)) {

                            myCriteriaMask = ResourceChoiceCriteria.NAME.setBit(myCriteriaMask);
                            mySelection = myTemplate;
                            mySelectionCriteria = myCriteriaMask;

                        } else if (null == mySelection) {

                            mySelection = myTemplate;
                            mySelectionCriteria = myCriteriaMask;
                        }
                        myResponse.addToList(myTemplate, myCriteriaMask);
                    }
                }
                if (null != mySelection) {

                    myResponse.setResult(mySelection, mySelectionCriteria);
                    return myResponse;
                }
            }
        }
        return new TemplateResponse();
    }

    private int validateFieldList(DataViewDef templateIn, List<LooseMapping> fieldListIn, int criteriaMaskIn) {

        int myCriteriaMask = criteriaMaskIn;

        if ((fieldListIn != null) && !fieldListIn.isEmpty()) {

            FieldListAccess myAccess = templateIn.getFieldListAccess();

            myCriteriaMask = ResourceChoiceCriteria.ALL_LIST_IDS.setBit(myCriteriaMask);
            myCriteriaMask = ResourceChoiceCriteria.ALL_LIST_NAMES.setBit(myCriteriaMask);

            for (LooseMapping myMapping : fieldListIn) {

                boolean myFoundFlag = false;

                if (myAccess.containsFieldLocalId(myMapping.getMappingLocalId())) {

                    myFoundFlag = true;
                    myCriteriaMask = ResourceChoiceCriteria.SOME_LIST_IDS.setBit(myCriteriaMask);

                } else {

                    myCriteriaMask = ResourceChoiceCriteria.ALL_LIST_IDS.clearBit(myCriteriaMask);
                }
                if (myAccess.containsFieldName(myMapping.getMappingName())) {

                    myFoundFlag = true;
                    myCriteriaMask = ResourceChoiceCriteria.SOME_LIST_NAMES.setBit(myCriteriaMask);

                } else {

                    myCriteriaMask = ResourceChoiceCriteria.ALL_LIST_NAMES.clearBit(myCriteriaMask);
                }
                if (!myFoundFlag) {

                    return 0;
                }
            }
            if (ResourceChoiceCriteria.ALL_LIST_IDS.isSet(myCriteriaMask)) {

                myCriteriaMask = ResourceChoiceCriteria.SOME_LIST_IDS.clearBit(myCriteriaMask);
            }
            if (ResourceChoiceCriteria.ALL_LIST_NAMES.isSet(myCriteriaMask)) {

                myCriteriaMask = ResourceChoiceCriteria.SOME_LIST_NAMES.clearBit(myCriteriaMask);
            }
        }
        return myCriteriaMask;
    }
}
