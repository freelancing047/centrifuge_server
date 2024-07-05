package csi.server.business.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.security.queries.Users;
import csi.server.business.cachedb.DataViewCacheCopy;
import csi.server.business.helper.DeepCloner.CloneType;
import csi.server.business.service.DataViewActionsService;
import csi.server.business.service.DataViewDefActionsService;
import csi.server.business.service.annotation.Operation;
import csi.server.common.dto.Response;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.SelectionListData.SharingRequest;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.exception.AuthorizationException;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.dataview.DataViewType;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;

public class ModelHelper {
   private static final Logger LOG = LogManager.getLogger(ModelHelper.class);

    public static <T extends ModelObject> T cloneObject(Class<T> clz, String uuid) throws CentrifugeException {
        T obj = find(clz, uuid);
        if (obj == null) {
            throw new CentrifugeException("Source object not found: " + uuid);
        }

        T clone = DeepCloner.clone(obj, CloneType.NEW_ID);
        if (clone instanceof Resource) {
            ((Resource) clone).resetDates();
        }

        return clone;
    }

    public static <T extends ModelObject> T cloneObject(T obj) throws CentrifugeException {
        T clone = DeepCloner.clone(obj, CloneType.NEW_ID);
        if (clone instanceof Resource) {
            ((Resource) clone).resetDates();

            DataViewDef myMeta = (clone instanceof DataView)
                                    ? ((DataView)clone).getMeta() : (clone instanceof DataViewDef)
                                        ? (DataViewDef)clone : null;

            if (null != myMeta) {

                myMeta.resetTransients();
            }
        }

        return clone;
    }

    public static void commit() {
        CsiPersistenceManager.commit();
        CsiPersistenceManager.begin();
    }

    public static <T extends ModelObject> T find(Class<T> clz, String uuid) {
        return CsiPersistenceManager.findObject(clz, uuid);
    }

    public static void delete(Class<? extends ModelObject> clazz, String uuid) {
        CsiPersistenceManager.deleteObject(clazz, uuid);
    }

    public static void delete(ModelObject obj) {
        CsiPersistenceManager.deleteObject(obj);
    }

    public static <T extends ModelObject> T save(T mobj) {
        return CsiPersistenceManager.merge(mobj);
    }

    public static <T extends ModelObject> void persist(T mobj) {
        CsiPersistenceManager.persist(mobj);
    }

    public static <T extends Resource> T saveAs(Class<T> clzIn, String uuidIn, String newNameIn, String newRemarksIn, boolean forceIn) throws CentrifugeException {
        if (newNameIn == null) {
            throw new CentrifugeException("Failed to save a copy of the resource.  Name is missing.");
        }

        T obj = find(clzIn, uuidIn);
        return saveAs(obj, newNameIn, newRemarksIn, forceIn);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Resource> T beginSaveAs(T resourceIn, String newNameIn, String newRemarksIn, boolean forceIn) throws CentrifugeException, AuthorizationException {

        T myResult = (T) AclRequest.findOwnedResourceByName(resourceIn.getClass(), newNameIn);

        if (myResult != null) {
            if (resourceIn.getUuid().equals(myResult.getUuid())) {

                // Simply an update and save operation on the current resource
                resourceIn.setName(newNameIn);
                resourceIn.setRemarks(newRemarksIn);
                myResult = save(fixupPersistenceLinkage(resourceIn));
                commit();
                if (resourceIn instanceof DataViewDef) {

                    // Any prebuilts must be destroyed since they likely don't match the updated template
                    CsiPersistenceManager.discardPrebuiltDataView(myResult.getUuid());
                }

            } else if (!forceIn) {

                // User has not requested overwrite of existing resource
                throw new AuthorizationException("Resource by the name \"" + newNameIn + "\" already exists.");

            } else {

               LOG.info(String.format("Request to overwrite resource %s", myResult.getName()));
                if (!CsiSecurityManager.isAuthorized(myResult.getUuid(), new AclControlType[]{AclControlType.DELETE})) {
                    // Overwrite not authorized.
                    throw new AuthorizationException("Access denied.  Not authorized to overwrite object.");
                } else if (resourceIn instanceof DataViewDef) {
                    // Any prebuilts must be destroyed since they likely don't match the updated template
                    CsiPersistenceManager.discardPrebuiltDataView(myResult.getUuid());
                }
            }
        }

        return myResult;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Resource> T finalizeSaveAs(T resourceIn, T priorResourceIn) throws Exception {
        if (null != priorResourceIn) {
            if (priorResourceIn instanceof DataView) {
                (new DataViewActionsService()).deleteDataView(priorResourceIn.getUuid());
                commit();
            } else if (resourceIn instanceof DataViewDef) {
                (new DataViewDefActionsService()).delete(priorResourceIn.getUuid());
                defineSharing(resourceIn.getUuid(), null);
//                resourceIn.setUuid(priorResourceIn.getUuid());
//                (new DataViewActionsService()).deleteDataViewDef(priorResourceIn.getUuid());
//                commit();
//                persist(fixupPersistenceLinkage(resourceIn));
//                CsiPersistenceManager.merge(fixupPersistenceLinkage(resourceIn));
            }
        }
        CsiPersistenceManager.getMetaEntityManager().clear();
        persist(fixupPersistenceLinkage(resourceIn));
        commit();
        CsiPersistenceManager.getMetaEntityManager().clear();
        return (T)find(resourceIn.getClass(), resourceIn.getUuid());
    }

    public static <T extends Resource> T saveNew(T resourceIn, boolean forceIn) throws CentrifugeException {

        T myResult = null;

        try {

            myResult = beginSaveAs(resourceIn, resourceIn.getName(), resourceIn.getRemarks(), forceIn);

            if ((null == myResult) || !resourceIn.getUuid().equals(myResult.getUuid())) {

                myResult = finalizeSaveAs(resourceIn, myResult);
            }

        } catch (Exception myException) {
            CsiPersistenceManager.rollback();

            LOG.error("Caught exception saving resource under new name.", myException);
            if (myException instanceof CentrifugeException) {
                throw (CentrifugeException) myException;
            } else {
                throw new CentrifugeException("Caught exception saving resource under new name.", myException);
            }
        }
        return myResult;
    }

    public static <T extends Resource> T saveAs(T resourceIn, String newNameIn, String newRemarksIn, boolean forceIn) throws CentrifugeException {
       T myResult = null;

        try {

            myResult = beginSaveAs(resourceIn, newNameIn, newRemarksIn, forceIn);

            if ((null == myResult) || !resourceIn.getUuid().equals(myResult.getUuid())) {

                T myClone = cloneObject(resourceIn);

                myClone.setName(newNameIn);
                myClone.setRemarks(newRemarksIn);
                if (myClone instanceof DataView) {

                    DataView mySource = (DataView)resourceIn;
                    DataView myDataView = (DataView)myClone;

                    myDataView.setType(DataViewType.BASIC);
                    myDataView.setResourceType(AclResourceType.DATAVIEW);

                    (new DataViewCacheCopy(mySource, myDataView)).executeCopy();
                    myDataView.setNeedsRefresh(false);
                    myDataView.clearInstalledTables();

                } else if (myClone instanceof DataViewDef) {

                    DataViewDef myTemplate = ((DataViewDef)myClone);

                    myTemplate.setTemplate(true);
                    myTemplate.setResourceType(AclResourceType.TEMPLATE);

                    myTemplate.clearAllRuntimeValues();
                }
                myResult = finalizeSaveAs(myClone, myResult);
                SecurityHelper.cloneSecurity(myResult, resourceIn.getUuid());
            }
            commit();

        } catch (Exception myException) {
            CsiPersistenceManager.rollback();

            LOG.error("Caught exception saving resource under new name.", myException);
            if (myException instanceof CentrifugeException) {
                throw (CentrifugeException) myException;
            } else {
                throw new CentrifugeException("Caught exception saving resource under new name.", myException);
            }
        }
        return myResult;
    }
/*
    public static ResourceDO makeResourceInfo(Resource res) {
        ResourceDO info = new ResourceDO();
        info.uuid = res.getUuid();
        info.name = res.getName();
        info.remarks = res.getRemarks();
        info.createDate = res.getCreateDate();
        info.lastOpenDate = res.getLastOpenDate();
        info.lastUpdateDate = res.getLastUpdateDate();

        if (res instanceof DataView) {
            info.spinoff = ((DataView) res).isSpinoff();
        }

        info.permissions = CsiSecurityManager.listPermissions(res);

        return info;
    }
*/
    public static DataViewDef locateTemplate(String uuidIn, String templateNameIn, String templateOwnerIn) throws CentrifugeException {

        DataViewDef myTemplate = (null != uuidIn) ? CsiPersistenceManager.findObject(DataViewDef.class, uuidIn) : null;

        if (null == myTemplate) {

            myTemplate = AclRequest.findResourceByName(DataViewDef.class, templateNameIn, templateOwnerIn);
        }
        return myTemplate;
    }

    /*
        public static DataViewDef resolveToTemplate(String uuid, String templateName, String dvname) throws CentrifugeException {

            DataViewDef template = null;

            if (null != uuid) {
                Resource res = ModelHelper.find(Resource.class, uuid);
                if (res instanceof DataViewDef) {
                    template = (DataViewDef) res;
                } else  if (res instanceof DataView) {
                    template = ((DataView) res).getMeta();
                }
            }

            if ((null == template) && (null != templateName)) {
                template = AclRequest.findResourceByName(DataViewDef.class, templateName);
            }

            if (template == null && dvname != null) {
                DataView dv = AclRequest.findResourceByName(DataView.class, dvname);
                if (dv != null) {
                    template = dv.getMeta();
                }
            }

            if (template == null) {
                throw new CentrifugeException("Resource not found");
            }

            return template;
        }
    */
    private static <T extends Resource> T fixupPersistenceLinkage(T resourceIn) {
        if (resourceIn instanceof DataView) {
            return (T) DataViewHelper.fixupPersistenceLinkage((DataView) resourceIn);
        } else if (resourceIn instanceof DataViewDef) {
            return (T) DataViewHelper.fixupPersistenceLinkage((DataViewDef) resourceIn);
        } else {
            return resourceIn;
        }
    }

    public static <T extends SelectorBasics> List<List<T>> generateOverWriteControlLists(List<T> selectionListIn,
                                                                                          List<T> ownersListIn,
                                                                                          List<T> conflictListIn) {

        List<List<T>> myResults = new ArrayList<List<T>>();
        List<T> mySelectionList = (null != selectionListIn) ? selectionListIn : new ArrayList<T>();
        List<T> myConflictList = (null != conflictListIn) ? conflictListIn : new ArrayList<T>();
        List<T> myRejectionList = new ArrayList<T>();

        if ((null != ownersListIn) && !ownersListIn.isEmpty()) {

            if ((null != conflictListIn) && !conflictListIn.isEmpty()) {

                Map<String, Integer> myConflictMap = new HashMap<String, Integer>();

                for (T myConflict : conflictListIn) {

                    myConflictMap.put(myConflict.getKey(), 0);
                }
                for (T myResource : ownersListIn) {

                    if (!myConflictMap.containsKey(myResource.getKey())) {

                        myRejectionList.add(myResource);
                    }
                }

            } else {

                myRejectionList = ownersListIn;
            }
        }
        myResults.add(mySelectionList);
        myResults.add(myRejectionList);
        myResults.add(myConflictList);

        return myResults;
    }

    @Operation
    public static Response<String, List<SharingDisplay>> defineSharing(String keyIn, List<String> resourceListIn,
                                                                       SharingRequest sharingRequestIn) {

        try {

            Users.validateUser();

            if (null != sharingRequestIn) {

                AclRequest.setRolePermissions(resourceListIn, sharingRequestIn);
                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();
            }
            return new Response<String, List<SharingDisplay>>(keyIn,
                                                    AclRequest.getSharingNamesAvoidingSecurity(resourceListIn));

        } catch(Exception myException) {

            return new Response<String, List<SharingDisplay>>(keyIn, ServerMessage.CAUGHT_EXCEPTION,
                                                                Format.value(myException));
        }
    }

    @Operation
    public static boolean defineSharing(String resourceIn, SharingInitializationRequest sharingRequestIn) {

        try {

            Users.validateUser();

            if (null != sharingRequestIn) {

                AclRequest.setRolePermissions(resourceIn, sharingRequestIn);
                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();
            }

            return true;

        } catch(Exception myException) {

            return false;
        }
    }

    public static void resetSecurity(Resource resourceIn) {

        if (null != resourceIn) {

            resourceIn.resetSecurity();
        }
    }
}