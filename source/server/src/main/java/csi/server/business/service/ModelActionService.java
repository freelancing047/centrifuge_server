package csi.server.business.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.ACL;
import csi.security.AccessControlEntry;
import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.security.queries.Users;
import csi.server.business.helper.DeepCloner;
import csi.server.business.helper.ModelHelper;
import csi.server.business.helper.SecurityHelper;
import csi.server.business.helper.SharedDataSourceHelper;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.Service;
import csi.server.business.service.filemanager.UploadService;
import csi.server.business.service.theme.ThemeActionsService;
import csi.server.common.dto.AccessRights;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.user.RecentAccess;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.interfaces.MapByDataType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.SynchronizeChanges;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.FieldReferenceValidator;

@Service(path = "/actions/model")
public class ModelActionService extends AbstractService implements ModelActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(ModelActionService.class);

    @Inject
    DataViewActionsService dataViewActionsService;

    @Inject
    DataViewDefActionsService dataViewDefActionsService;

    @Inject
    ThemeActionsService themeActionsService;

    @Inject
    UploadService uploadService;

/*
    @Operation
    @Interruptable
    public ModelObject cloneObject(@QueryParam("uuid") String uuid) throws CentrifugeException {

        return ModelHelper.cloneObject(ModelObject.class, uuid);
    }
*/
    public Response<String,? extends Resource>
    createDataResource(String requestIdIn, String nameIn, String remarksIn, AclResourceType resourceTypeIn,
                       AdHocDataSource dataSourceIn, List<LaunchParam> parametersIn, List<AuthDO> credentialsIn,
                       CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn, boolean overwriteIn,
                       SharingInitializationRequest sharingRequestIn) {

        try {

            Users.validateUser();

            switch (resourceTypeIn) {

                case DATAVIEW:

                    return (new DataViewActionsService()).createDataView(requestIdIn, nameIn, remarksIn, dataSourceIn,
                                                                            parametersIn, credentialsIn, overwriteIn,
                                                                            sharingRequestIn);

                case TEMPLATE:

                    return (new DataViewDefActionsService()).createTemplate(requestIdIn, nameIn, remarksIn,
                                                                            dataSourceIn, overwriteIn, sharingRequestIn);

                case DATA_TABLE:

                    return SharedDataSourceHelper.createInstalledTable(requestIdIn, nameIn, remarksIn, dataSourceIn,
                                                                        parametersIn, credentialsIn, capcoInfoIn,
                                                                        tagInfoIn, overwriteIn, sharingRequestIn);
            }
            return new Response<String, Resource>(requestIdIn, ServerMessage.RESOURCE_NOT_SUPPORTED);

        } catch (Exception myException) {

            return new Response<String, Resource>(requestIdIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Operation
    public ModelObject save(@PayloadParam ModelObject obj) throws CentrifugeException {

        Users.validateUser();
        ModelObject modelObject = null;
        if((obj instanceof DataView) || (obj instanceof DataViewDef)){

            DataViewDef objectToValidate = null;

            if(obj instanceof DataView){
                objectToValidate = ((DataView) obj).getMeta();
            }
            else if(obj instanceof DataViewDef){
                objectToValidate = (DataViewDef) obj;
            }

            try {
                FieldReferenceValidator fieldReferenceValidator = new FieldReferenceValidator(objectToValidate);
                fieldReferenceValidator.validateModelReferences();
                return ModelHelper.save(obj);
            } catch (Exception e) {
                throw new CentrifugeException("The dataview or template you attempted to save is in an invalid state. This may be due to another user editing this dataview with conflicting changes. Please close and reopen this dataview to correct the problem.");
            }
        }
        else{
            //This is always vizlayoutstate
            try{
                modelObject = ModelHelper.save(obj);
                CsiPersistenceManager.commit();
            } catch(Exception exception){
                if(LOG.isDebugEnabled()) {
                  LOG.debug("VisualizationLayoutState failed to save", exception);
               }
            }
            return modelObject;
        }
    }
/*
    @Operation
    public void saveList(@PayloadParam List<ModelObject> objs) throws CentrifugeException {
        for (ModelObject obj : objs) {
            if(obj instanceof DataView || obj instanceof DataViewDef){

                DataViewDef objectToValidate = null;

                if(obj instanceof DataView){
                    objectToValidate = ((DataView) obj).getMeta();
                }
                else if(obj instanceof DataViewDef){
                    objectToValidate = (DataViewDef) obj;
                }

                try {
                    FieldReferenceValidator fieldReferenceValidator = new FieldReferenceValidator(objectToValidate);
                    fieldReferenceValidator.validateModelReferences();
                    CsiPersistenceManager.merge(obj);
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();
                    throw new CentrifugeException("The dataview or template you attempted to save is in an invalid state. This may be due to another user editing this dataview with conflicting changes. Please close and reopen this dataview to correct the problem.");
                }
            }
            else
            {
                CsiPersistenceManager.merge(obj);
            }
        }
    }

    @Operation
    public Response<String, Resource> saveAs(@QueryParam("uuid") String srcUuid, @QueryParam("name") String newName, @QueryParam("name") String newRemarks, @QueryParam("forceIn") boolean forceIn) {

        try {

            Resource myResource = CsiPersistenceManager.findObject(Resource.class, srcUuid);

            if(myResource instanceof DataView || myResource instanceof DataViewDef){
                try {

                    DataViewDef objectToValidate = null;

                    if(myResource instanceof DataView){
                        objectToValidate = ((DataView) myResource).getMeta();
                    }
                    else if(myResource instanceof DataViewDef){
                        objectToValidate = (DataViewDef) myResource;
                    }

                    FieldReferenceValidator fieldReferenceValidator = new FieldReferenceValidator(objectToValidate);
                    fieldReferenceValidator.validateModelReferences();
                } catch (Exception e) {

                    return new Response<String, Resource>(srcUuid, ServerMessage.INVALID_STATE);
                }
            }

            return new Response<String, Resource>(srcUuid, ModelHelper.saveAs(myResource, newName, newRemarks, forceIn));

        } catch (Exception myException) {

            return new Response<String, Resource>(srcUuid, ServerMessage.CAUGHT_EXCEPTION, Display.value(myException));
        }
    }
*/
    // Change operation to persist the dataview or template actually submitted with the new name
    // not to persist a possibly outdated copy -- why would anybody do that?
    @Operation
    public Response<String, Resource> saveCurrentAs(String newNameIn, String newRemarksIn,
                                                    Resource resourceIn, boolean forceIn) {

        try {

            boolean isDataView = resourceIn instanceof DataView;
            boolean isTemplate = resourceIn instanceof DataViewDef;

            Users.validateUser();

            if(isDataView || isTemplate) {

                Resource mySavedResource = null;
                try {
                    DataViewDef myObjectToValidate = null;

                    if(isDataView){
                        myObjectToValidate = ((DataView) resourceIn).getMeta();
                    }
                    else {
                        myObjectToValidate = (DataViewDef) resourceIn;
                    }

                    FieldReferenceValidator fieldReferenceValidator = new FieldReferenceValidator(myObjectToValidate);
                    fieldReferenceValidator.validateModelReferences();
                    TaskHelper.checkForCancel();

                    if(isDataView){
                       LOG.info("Save DataView " + Format.value(resourceIn.getName())
                                + ", id = " + resourceIn.getUuid() + " as " + Format.value(newNameIn));
                    }
                    else {
                       LOG.info("Create Template " + Format.value(newNameIn));
                    }
                    mySavedResource = ModelHelper.saveAs(resourceIn, newNameIn, newRemarksIn, forceIn);

                    TaskHelper.checkForCancel();

                    //FIXME: hack to clear prebuilt dataview when template is changed
                    if (isTemplate) {

                        /*DataView dataView = */CsiPersistenceManager.getPrebuiltDataView(mySavedResource.getName());
                    }

                    return new Response<String, Resource>(newNameIn, mySavedResource);

                } catch(TaskCancelledException tce){
                    if (mySavedResource != null){
                        CsiPersistenceManager.begin();
                        CsiPersistenceManager.deleteObject(mySavedResource);
                        CsiPersistenceManager.commit();
                    }

                    return new Response<String, Resource>(newNameIn, ServerMessage.SAVE_CANCELED);

                } catch (Exception e) {

                   LOG.error(e);

                    return new Response<String, Resource>(newNameIn, ServerMessage.INVALID_STATE);
                }
            }
            else
            {
                Resource mySavedResource = ModelHelper.saveAs(resourceIn, newNameIn, newRemarksIn, forceIn);
                return new Response<String, Resource>(newNameIn, mySavedResource);
            }

        } catch (Exception myException) {

            return new Response<String, Resource>(newNameIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    public List<ResourceBasics> listUserResourceBasics(AclResourceType resourceTypeIn)
            throws CsiSecurityException {

        return AclRequest.listUserResources(resourceTypeIn);
    }

    public List<ResourceBasics> listUserResourceBasics(AclResourceType resourceTypeIn, String ownerIn)
            throws CsiSecurityException {

        return AclRequest.listOwnerResources(resourceTypeIn, ownerIn);
    }

    public List<List<ResourceBasics>> getOverWriteControlLists(AclResourceType resourceTypeIn) throws CentrifugeException {

        switch (resourceTypeIn) {

            case DATAVIEW:

                return (new DataViewActionsService()).getDataViewOverWriteControlLists();

            case TEMPLATE:

                return (new DataViewDefActionsService()).getTemplateOverWriteControlLists();

            case THEME:

                return (new ThemeActionsService()).getThemeOverWriteControlLists();

            default:

                throw new CentrifugeException("Unrecognized Resource Type.");
        }
    }

    public List<ResourceBasics> getUnFilteredResourceList(AclResourceType resourceTypeIn, AclControlType permissionIn)
            throws CentrifugeException {

        return AclRequest.listAnyAuthorizedResources(resourceTypeIn, new AclControlType[]{permissionIn});
    }

    public List<ResourceBasics> getFilteredResourceList(AclResourceType resourceTypeIn,
                                                        ResourceFilter filterIn, AclControlType permissionIn)
            throws CentrifugeException {

        return (null != filterIn)
                ? AclRequest.filterResources(resourceTypeIn, filterIn, new AclControlType[]{permissionIn})
                : AclRequest.listAnyAuthorizedResources(resourceTypeIn, new AclControlType[]{permissionIn});
    }

    public List<List<String>> delete(AclResourceType resourceTypeIn, List<String> listIn) throws CentrifugeException {

        List<String> mySuccess = new ArrayList<String>();
        List<String> myFailure = new ArrayList<String>();
        List<List<String>> myResults = new ArrayList<List<String>>();

        Users.validateUser();

        myResults.add(mySuccess);
        myResults.add(myFailure);

        if (AclResourceType.DATAVIEW == resourceTypeIn) {

            for (String myUuid : listIn) {

                try {

                    if (null != (new DataViewActionsService()).deleteDataView(myUuid)) {

                        mySuccess.add(myUuid);

                    } else {

                        myFailure.add(myUuid);
                    }

                } catch (Exception myException) {

                    myFailure.add(myUuid);
                }
            }

        } else if (AclResourceType.TEMPLATE == resourceTypeIn) {

            for (String myUuid : listIn) {

                try {

                    if (null != (new DataViewDefActionsService()).delete(myUuid)) {

                        mySuccess.add(myUuid);

                    } else {

                        myFailure.add(myUuid);
                    }

                } catch (Exception myException) {

                    myFailure.add(myUuid);
                }
            }

        } else if (AclResourceType.DATA_TABLE == resourceTypeIn) {

            for (String myUuid : listIn) {

                try {

                    if (null != (new UploadService()).delete(myUuid)) {

                        mySuccess.add(myUuid);

                    } else {

                        myFailure.add(myUuid);
                    }

                } catch (Exception myException) {

                    myFailure.add(myUuid);
                }
            }

        } else {

            throw new CentrifugeException("Unrecognized Resource Type.");
        }
        return myResults;
    }

    public AccessRights verifyAccess(String uuidIn) {

        AccessRights myAccessRights = new AccessRights();

        try {

            String myOwner = CsiSecurityManager.getUserName();
            ACL myAcl = AclRequest.getAclAvoidingSecurity(uuidIn);
            List<AccessControlEntry> myEntries = myAcl.getEntries();

            myAccessRights.setIsOwner(myOwner.equals(myAcl.getOwner()));

            if (myEntries != null) {

                for (AccessControlEntry myAccess : myEntries) {

                    if (CsiSecurityManager.hasRole(myAccess.getRoleName())) {

                        if (AclControlType.READ == myAccess.getAccessType()) {

                            myAccessRights.setCanRead(true);

                        } else if (AclControlType.EDIT == myAccess.getAccessType()) {

                            myAccessRights.setCanWrite(true);

                        } else if (AclControlType.DELETE == myAccess.getAccessType()) {

                            myAccessRights.setCanDelete(true);
                        }
                    }
                }
            }

        } catch (Exception myException) {

           LOG.error(myException.getMessage(), myException);
        }
        return myAccessRights;
    }

    public ValuePair<String, Boolean> isAuthorized(String uuidIn, AclControlType permissionIn) {

        return isAuthorized(uuidIn, permissionIn, true);
    }

    public ValuePair<String, Boolean> isAuthorized(String uuidIn, AclControlType permissionIn, boolean doSecurityIn) {

        Boolean myAuthorizationFlag = null;

        try {

            myAuthorizationFlag = CsiSecurityManager.isAuthorized(uuidIn, permissionIn, doSecurityIn);

        } catch (Exception myException) {

        }
        return new ValuePair<String, Boolean>(uuidIn, myAuthorizationFlag);
    }

    public ValuePair<String, Boolean> isOwner(String uuidIn) {

        Boolean myOwnershipFlag = false;

        try {

            myOwnershipFlag = AclRequest.isOwner(uuidIn);

        } catch (Exception myException) {

        }
        return new ValuePair<String, Boolean>(uuidIn, myOwnershipFlag);
    }

    public Response<String, ValuePair<List<? extends MapByDataType>, ValuePair<CapcoInfo, SecurityTagsInfo>>>
    getSecurityInfo(String uuidIn, AclResourceType typeIn, AclControlType modeIn) {

        if (CsiSecurityManager.canChangeSecurity(uuidIn)) {

            Resource myResource = CsiPersistenceManager.findObjectAvoidingSecurity(typeIn.getObjectClass(), uuidIn);

            if (null != myResource) {

                CapcoInfo myCapco = null;
                SecurityTagsInfo myTags = null;
                List<? extends MapByDataType> myList = null;

                if (myResource instanceof DataView) {

                    myResource = ((DataView) myResource).getMeta();
                }
                myCapco = myResource.getCapcoInfo();
                myTags = myResource.getSecurityTagsInfo();
                myList = (myResource instanceof DataViewDef)
                        ? ((DataViewDef) myResource).getModelDef().getFieldDefs()
                        : ((myResource instanceof InstalledTable)
                        ? ((InstalledTable) myResource).genColumnParameters()
                        : null);

                return new Response(uuidIn, new ValuePair(myList, new ValuePair(myCapco, myTags)));
            }
            return new Response(uuidIn, ServerMessage.FAILED_SERVER_REQUEST);

        } else {

            return new Response(uuidIn, ServerMessage.USER_NOT_AUTHORIZED);
        }
    }

    public Response<String, ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>>
    classifyResource(String uuidIn, AclResourceType typeIn, CapcoInfo capcoIn, SecurityTagsInfo tagsIn) {

        if (CsiSecurityManager.canChangeSecurity(uuidIn)) {

            Resource myResource = CsiPersistenceManager.findObjectAvoidingSecurity(typeIn.getObjectClass(), uuidIn);

            if (null != myResource) {

                SynchronizeChanges.updateSecurity(myResource, capcoIn, tagsIn);
                CsiPersistenceManager.mergeForSecurity(myResource);

                if (SecurityHelper.updateSecurity(myResource).booleanValue()) {

                    Resource mySecurityBase = (myResource instanceof DataView) ? ((DataView)myResource).getMeta() : myResource;
                    ValuePair<CapcoInfo, SecurityTagsInfo> myResults = new ValuePair(mySecurityBase.getCapcoInfo(),
                                                                                    mySecurityBase.getSecurityTagsInfo());
                    ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>> myResponse
                            = new ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>(CsiSecurityManager.isAuthorized(uuidIn,
                                                                            new AclControlType[]{AclControlType.READ}), myResults);

                    CsiPersistenceManager.mergeForSecurity(myResource);
                    return new Response<String, ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>>(uuidIn, myResponse);
                }
            }
        }
        return new Response<String, ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>>(uuidIn,
                                                                                   ServerMessage.FAILED_SERVER_REQUEST);
    }

    public Response<String, ValuePair<String, String>> renameResource(String uuidIn, AclResourceType typeIn,
                                                                      String nameIn, String remarksIn) {

        try {

            if (CsiSecurityManager.isAuthorized(uuidIn, AclControlType.EDIT)) {

                if (AclRequest.renameResource(uuidIn, nameIn, remarksIn)) {

                    if (AclResourceType.DATAVIEW == typeIn) {

                        try {
                            AclRequest.recordAccess(new RecentAccess(CsiSecurityManager.getUserName(), uuidIn, nameIn));
                        } catch (Exception IGNORE) {}
                    }
                    return new Response<String, ValuePair<String, String>>(uuidIn,
                            new ValuePair<String, String>(nameIn, remarksIn));
                } else {

                    return new Response<String, ValuePair<String, String>>(uuidIn, ServerMessage.FAILED_SERVER_REQUEST);
                }

            } else {

                return new Response<String, ValuePair<String, String>>(uuidIn, ServerMessage.USER_NOT_AUTHORIZED);
            }

        } catch (Exception myException) {

            return new Response<String, ValuePair<String, String>>(uuidIn,
                    ServerMessage.CAUGHT_EXCEPTION,
                    Format.value(myException));
        }
    }

    public void cancelTask(String taskIdIn) {

        TaskHelper.taskController.cancelTask(taskIdIn);
    }

    @Override
    public Response<String, Resource> saveDataviewAsDataview(String uuidIn, String newNameIn, String newRemarksIn,
                                                             boolean forceIn) throws CentrifugeException {

        if(CsiSecurityManager.isAuthorized(uuidIn, AclControlType.READ)) {

            DataView myDataView = CsiPersistenceManager.findObject(DataView.class, uuidIn);

            if (null == myDataView) {

                throw new CentrifugeException("DataView not found");
            }
            return saveCurrentAs(newNameIn, newRemarksIn, myDataView, forceIn);

        }else{

            throw new CentrifugeException("Not authorized.");
        }
    }

    @Override
    public Response<String, Resource> saveTemplateAsTemplate(String uuidIn, String newNameIn, String newRemarksIn,
                                                             boolean forceIn) throws CentrifugeException {

        if(CsiSecurityManager.isAuthorized(uuidIn, AclControlType.READ)) {

            DataViewDef myTemplate = CsiPersistenceManager.findObject(DataViewDef.class, uuidIn);

            if (null == myTemplate) {

                throw new CentrifugeException("Template not found");
            }
            return saveCurrentAs(newNameIn, newRemarksIn, myTemplate, forceIn);

        }else{

            throw new CentrifugeException("Not authorized.");
        }
    }

    @Override
    public Response<String, Resource> saveDataviewAsTemplate(String uuidIn, String newNameIn, String newRemarksIn,
                                                             boolean forceIn)  throws CentrifugeException  {

        if(CsiSecurityManager.isAuthorized(uuidIn, AclControlType.READ)) {

            DataView mySource = CsiPersistenceManager.findObject(DataView.class, uuidIn);
            DataViewDef myTemplate = (null != mySource)
                                        ? DeepCloner.clone(mySource.getMeta(), DeepCloner.CloneType.NEW_ID) : null;

            if (null == myTemplate) {

                throw new CentrifugeException("Dataview not found");
            }
            myTemplate.resetSecurity();
            return saveCurrentAs(newNameIn, newRemarksIn, myTemplate, forceIn);

        }else{

            throw new CentrifugeException("Not authorized.");
        }
    }

    @Operation
    public List<List<ResourceBasics>> getResourceOverWriteControlLists(AclResourceType typeIn) throws CentrifugeException {

        List<ResourceBasics> mySelectionList = AclRequest.listAuthorizedUserResources(typeIn, new AclControlType[]{AclControlType.DELETE});
        List<ResourceBasics> myRejectionList = AclRequest.listUserResources(typeIn);
        List<ResourceBasics> myConflictList = AclRequest.listAuthorizedUserResources(typeIn, new AclControlType[]{AclControlType.DELETE});

        return ModelHelper.generateOverWriteControlLists(mySelectionList, myRejectionList, myConflictList);
    }

    @Operation
    public Map<String, List<ResourceBasics>> getAdminResourceLists(AclResourceType typeIn, Collection<String> userList)
            throws CentrifugeException {

        Map<String, List<ResourceBasics>> myListMap = new TreeMap<String, List<ResourceBasics>>();

        for (String myUser : userList) {

            List<ResourceBasics> myList = AclRequest.listOwnerResources(typeIn, myUser);
            if ((myList != null) && !myList.isEmpty()) {

                myListMap.put(myUser, myList);
            }
        }
        return myListMap;
    }
}
