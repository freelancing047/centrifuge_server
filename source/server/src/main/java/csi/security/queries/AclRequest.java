package csi.security.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.ACL;
import csi.security.AccessControlEntry;
import csi.security.Authorization;
import csi.security.CapcoSecurityTag;
import csi.security.CsiSecurityManager;
import csi.security.GenericSecurityTag;
import csi.security.SecurityMask;
import csi.security.SourceAclEntry;
import csi.security.jaas.JAASRole;
import csi.security.queries.Constants.MatchingMode;
import csi.security.queries.Constants.QueryMode;
import csi.server.common.dto.DataPair;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.SelectionListData.SharingRequest;
import csi.server.common.dto.system.UserFunction;
import csi.server.common.dto.user.RecentAccess;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.enumerations.ResourceSortMode;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.model.Resource;
import csi.server.common.model.UUID;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.model.themes.Theme;
import csi.server.common.util.Format;
import csi.server.common.util.SynchronizeChanges;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CsiUtil;

import lombok.Synchronized;

// Unsecured requests should include "AvoidingSecurity" within the entry point name

public class AclRequest extends ControlledQueries {
    protected static final Logger LOG = LogManager.getLogger(AclRequest.class);

    protected static boolean _doDebug = LOG.isDebugEnabled();
    protected static QueryMode _mode = QueryMode.JAVA;

    public static List<ResourceBasics> getRecentDataViews(String userIn, int limitIn) {

        List<ResourceBasics> myFinalList = new ArrayList<ResourceBasics>();
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        List<Object[]> myResultList = null;
        String myUser = userIn.toLowerCase();

        try {
            Query myQuery = myManager.createQuery("SELECT r.uuid.uuid, r.name, r.remarks, a.lastAccess, a.logonId, r.size "
                                                    + "FROM ModelResource r, RecentAccess a WHERE r.resourceType = "
                                                    + ":type AND a.logonId = :user AND a.resourceId = r.uuid.uuid "
                                                    + "ORDER BY a.lastAccess DESC");

            myQuery.setParameter("user", myUser);
            myQuery.setParameter("type", AclResourceType.DATAVIEW);
            if (0 < limitIn) {

                myQuery.setMaxResults(limitIn);
            }
            myResultList = myQuery.getResultList();

            if ((null != myResultList) && !myResultList.isEmpty()) {

                for (Object[] myResult : myResultList) {

                    myFinalList.add(new ResourceBasics(myResult, myUser));
                }
            }

        } catch(Exception myException) {

            LOG.error("Caught exception retrieving recent DataViews:\n" + Format.value(myException));
        }
        return myFinalList;
    }

    public static void recordAccess(String uuidIn) {

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        try {

            Query myQuery =null;

            myManager.getTransaction().commit();
            myManager.getTransaction().begin();

            myQuery = myManager.createQuery("UPDATE ModelResource SET lastOpenDate = :date WHERE uuid.uuid = :key");
            myQuery.setParameter("key", uuidIn);
            myQuery.setParameter("date", new Date());
            myQuery.executeUpdate();
            myManager.getTransaction().commit();
            myManager.getTransaction().begin();

        } catch(Exception myException) {

            myManager.getTransaction().rollback();
            myManager.getTransaction().begin();
            LOG.error("Caught exception retrieving recent DataViews:\n" + Format.value(myException));
        }
    }

    public static void recordAccess(RecentAccess infoIn) {

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        try {
            Query myQuery = null;

            myManager.getTransaction().commit();
            myManager.getTransaction().begin();

            if (null != infoIn.getName()) {

                myQuery = myManager.createQuery("SELECT count(*) FROM RecentAccess WHERE key = :key");

                myQuery.setParameter("key", infoIn.getKey());
                Long myCount = (Long)myQuery.getSingleResult();
                if ((null == myCount) || (0 == myCount)) {

                    myQuery = myManager.createNativeQuery("INSERT INTO RecentAccess(key, logonId, resourceId, "
                            + "name, lastAccess) VALUES(:key, :user, :resource, :name, :date)");

                } else {

                    myQuery = myManager.createNativeQuery("UPDATE RecentAccess SET logonId = :user, resourceId = "
                            + ":resource, name = :name, lastAccess = :date WHERE key = :key");
                }
                myQuery.setParameter("name", infoIn.getName());

            } else {

                myQuery = myManager.createNativeQuery("UPDATE RecentAccess SET logonId = :user, resourceId = "
                                            + ":resource, lastAccess = :date WHERE key = :key");
            }
            myQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedEntityClass(RecentAccess.class);
            myQuery.setParameter("key", infoIn.getKey());
            myQuery.setParameter("user", infoIn.getLogonId());
            myQuery.setParameter("resource", infoIn.getResourceId());
            myQuery.setParameter("date", infoIn.getLastAccess());
            myQuery.executeUpdate();
            myManager.getTransaction().commit();
            myManager.getTransaction().begin();

        } catch(Exception myException) {

            myManager.getTransaction().rollback();
            myManager.getTransaction().begin();
            LOG.error("Caught exception retrieving recent DataViews:\n" + Format.value(myException));
        }
    }

    public static List<UserFunction> getUserFunctionList() {

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        List<UserFunction> myList = null;

        try {
            Query myQuery = myManager.createQuery("SELECT uf FROM UserFunction uf");

            myList = myQuery.getResultList();

        } catch(Exception IGNORE) {
        }
        return (null != myList) ? myList : new ArrayList<UserFunction>();
    }

    public static List<Basemap> getBasemapsAvoidingSecurity() {

        List<Basemap> myMaps = null;
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery("SELECT b FROM Basemap b");
        try {

            myMaps = myQuery.getResultList();

        } catch (Exception IGNORE) {
        }
        return myMaps;
    }

    public static boolean checkConflictAvoidingSecurity(String uuidIn) {

        boolean myResult = false;
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery("SELECT COUNT(*) FROM ModelResource r WHERE uuid.uuid = :uuid");
        try {

            myQuery.setParameter("uuid", uuidIn);
            myResult = (0L < (Long)myQuery.getSingleResult());

        } catch (Exception IGNORE) {
        }
        return myResult;
    }

    public static boolean checkConflictAvoidingSecurity(String nameIn, AclResourceType typeIn) {

        return checkConflictAvoidingSecurity(nameIn, null, typeIn);
    }

    public static boolean checkConflictAvoidingSecurity(String nameIn, String ownerIn, AclResourceType typeIn) {

        boolean myResult = false;
        String myOwner = (null != ownerIn) ? ownerIn : CsiSecurityManager.getUserName();
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery("SELECT COUNT(*) FROM ModelResource r WHERE r.resourceType = "
                + ":type AND r.name = :name AND r.owner = :owner");
        try {

            myQuery.setParameter("name", nameIn);
            myQuery.setParameter("type", typeIn);
            myQuery.setParameter("owner", myOwner);
            myResult = (0L < (Long)myQuery.getSingleResult());

        } catch (Exception IGNORE) {
        }
        return myResult;
    }

    public static String getUuidFromOwnedResource(String nameIn, String ownerIn, AclResourceType typeIn) {

        String myResult = null;
        String myOwner = (null != ownerIn) ? ownerIn : CsiSecurityManager.getUserName();
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery("SELECT uuid.uuid FROM ModelResource r WHERE r.resourceType = "
                + ":type AND r.name = :name AND r.owner = :owner");
        try {

            myQuery.setParameter("name", nameIn);
            myQuery.setParameter("type", typeIn);
            myQuery.setParameter("owner", myOwner);
            myResult = (String)myQuery.getSingleResult();

        } catch (Exception IGNORE) {
        }
        return myResult;
    }

    public static Resource getOwnedResourceByNameAvoidingSecurity(String nameIn, AclResourceType typeIn) {

        return getOwnedResourceByNameAvoidingSecurity(nameIn, CsiSecurityManager.getUserName(), typeIn);
    }

    public static Resource getOwnedResourceByNameAvoidingSecurity(String nameIn, String ownerIn, AclResourceType typeIn) {

        Resource myResult = null;
        String myOwner = (null != ownerIn) ? ownerIn : CsiSecurityManager.getUserName();
        AclResource myResource = resourceTypeMap.get(typeIn);
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery("SELECT d FROM " + myResource.getJavaName()
                                                + " WHERE d.resourceType = :type AND"
                                                + " d.name = :name AND d.owner = :owner");
        try {

            myQuery.setParameter("name", nameIn);
            myQuery.setParameter("type", typeIn);
            myQuery.setParameter("owner", myOwner);
            myResult = (Resource)myQuery.getSingleResult();

        } catch (Exception IGNORE) {
        }
        return myResult;
    }

    public static Resource getResourceByUuidAvoidingSecurity(String uuidIn, AclResourceType typeIn) {

        Resource myResult = null;
        AclResource myResource = resourceTypeMap.get(typeIn);
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery("SELECT d FROM " + myResource.getJavaName()
                                                + " WHERE d.uuid.uuid = :uuid");
        try {

            myQuery.setParameter("uuid", uuidIn);
            myResult = (Resource)myQuery.getSingleResult();

        } catch (Exception IGNORE) {
        }
        return myResult;
    }

    public static List<InstalledColumn> linkInstalledColumns(InstalledTable tableIn, List<String> columnListIn) {

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        for (String myColumnName : columnListIn) {

            try {

                Query myQuery = myManager.createQuery("SELECT ic FROM InstalledColumn ic WHERE columnName = '"
                        + myColumnName + "'");

                List<InstalledColumn> myList = myQuery.getResultList();

                if (!myList.isEmpty()) {

                    InstalledColumn myColumn = myList.get(0);

                    myColumn.setLocalId(UUID.randomUUID());
                    myColumn.setInstalledTable(tableIn);
                    tableIn.addColumn(myColumn);
                }

            } catch(Exception IGNORE) {
            }
        }
        myManager.merge(tableIn);
        return tableIn.getColumns();
    }

    public static boolean isOwner(String uuidIn) {

        boolean myResult = false;

        if ((null != uuidIn) && (0 < uuidIn.length())) {

            try {

                Query myQuery = createJavaQuery(AclFunction.COUNT_ACL, AclScope.OWNED, AclResource.ACL,
                        AclCondition.ACL_MATCH, new String[]{uuidIn}, SecurityMask.getNoSecurityMask());

                if (null != myQuery) {

                    myResult = (0L < (Long) myQuery.getSingleResult());
                }

            } catch(Exception IGNORE) {
            }
        }
        return myResult;
    }

    public static boolean hasACL(String uuidIn) {

        return (null != getResourceACL(uuidIn));
    }

    public static List<ACL> getUserAclList(String roleIn) {

        List<ACL> myList = null;
        String myRole = (null != roleIn) ? roleIn.trim().toLowerCase() : null;

        if ((null != myRole) && (0 < myRole.length())) {

            try {

                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                Query myQuery = myManager.createQuery(AclCommands.GET_USER_RESOURCE_ACL_LIST.getJava());

                myQuery.setParameter("role", myRole);

                myList = myQuery.getResultList();

            } catch(Exception IGNORE) {
            }
        }
        return myList;
    }

    public static void replaceAllOwnership(String oldOwnerIn, String newOwnerIn) {

        String myOldOwner = (null != oldOwnerIn) ? oldOwnerIn.trim().toLowerCase() : null;
        String myNewOwner = (null != newOwnerIn) ? newOwnerIn.trim().toLowerCase() : null;
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery(AclCommands.CHANGE_ALL_OWNERSHIP_ACL.getJava());
        myQuery.setParameter("oldowner", myOldOwner);
        myQuery.setParameter("newowner", myNewOwner);
        myQuery.executeUpdate();
        myQuery = myManager.createQuery(AclCommands.CHANGE_ALL_OWNERSHIP_RESOURCE.getJava());
        myQuery.setParameter("oldowner", myOldOwner);
        myQuery.setParameter("newowner", myNewOwner);
        myQuery.executeUpdate();
    }

    public static void removeRoleFromAllACLs(String roleIn) {

        List<ACL> myAclList = getUserAclList(roleIn);

        removeAclRolePermissions(myAclList, roleIn);
    }

    public static void migrateSecurity(Resource targetResource, String sourceUuidIn, String targetUuidIn) {

        ACL mySourceACL = getResourceACL(sourceUuidIn);

        if (null != mySourceACL) {

            ACL myTargetACL =  lockAcl(targetResource, targetUuidIn);

            if (null != myTargetACL) {

                myTargetACL.getSourceEntries().addAll(mySourceACL.getSourceEntries());
                myTargetACL.getLinkupEntries().addAll(mySourceACL.getLinkupEntries());
                myTargetACL.getCapcoTags().addAll(mySourceACL.getCapcoTags());
                myTargetACL.getGenericTags().addAll(mySourceACL.getGenericTags());
                unlockAcl(myTargetACL);
            }
        }
    }

    public static void migrateACL(Resource targetResourceIn, String sourceUuidIn) {

        ACL mySourceACL = getResourceACL(sourceUuidIn);

        if ((null != mySourceACL) && (null != targetResourceIn)) {

            String myTargetUuid = targetResourceIn.getUuid();
            ACL myTestAcl = getResourceACL(myTargetUuid);
            ACL myTargetACL = (null != myTestAcl) ? myTestAcl : new ACL();
            Authorization myAuthorization = CsiSecurityManager.getAuthorization();
            String myOwner = (null != myAuthorization) ? myAuthorization.getName() : null;
            List<AccessControlEntry> mySourceList = mySourceACL.getEntries();
            List<AccessControlEntry> myTargetList = mySourceACL.getEntries();

            targetResourceIn.setOwner(myOwner);
            myTargetACL.setOwner(myOwner);
            myTargetACL.setUuid(mySourceACL.getUuid());
            myTargetList.clear();
            for (AccessControlEntry myEntry :mySourceList) {

                myTargetList.add(myEntry.clone());
            }

            if (null != myTestAcl) {

                CsiPersistenceManager.merge(myTargetACL);

            } else {

                CsiPersistenceManager.persist(myTargetACL);
            }
            CsiPersistenceManager.merge(targetResourceIn);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();
        }
    }

    public static ACL captureAcl(String uuidIn) {

        try {

            List<ACL> myList = getResourceAclList(uuidIn);
            ACL myCurrentACL = ((null != myList) && !myList.isEmpty()) ? myList.get(0) : null;

            return (null != myCurrentACL) ? myCurrentACL.clone() : null;

        } catch (Exception IGNORE) {

            return null;
        }
    }

    public static void replaceAcl(String uuidIn, ACL aclIn) {

        if ((null != uuidIn) && (null != aclIn)) {

            try {

                removeAcl(uuidIn);
                aclIn.setUuid(uuidIn);
                CsiPersistenceManager.merge(aclIn);
                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();

            } catch (Exception IGNORE) {
            }
        }
    }

    public static boolean removeAcl(String uuidIn) {

        try {
            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            List<ACL> myList = getResourceAclList(uuidIn);

            if ((null != myList) && !myList.isEmpty()) {

                for (ACL myACL : myList) {

                    myACL.getSourceEntries().clear();
                    myACL.getLinkupEntries().clear();
                    myACL.getCapcoTags().clear();
                    myACL.getGenericTags().clear();

                    myManager.remove(myACL);
                }
            }
            return true;

        } catch (Exception IGNORE) {

            return false;
        }
    }

    public static List<CapcoSecurityTag> getKnownCapcoTags() {

        List<CapcoSecurityTag> myList = null;

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query myQuery = myManager.createQuery(AclCommands.GET_CAPCO_TAGS.getJava());

            myList = myQuery.getResultList();

        } catch(Exception IGNORE) {
        }
        return myList;
    }

    public static List<GenericSecurityTag> getKnownGenericTags() {

        List<GenericSecurityTag> myList = null;

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query myQuery = myManager.createQuery(AclCommands.GET_GENERIC_TAGS.getJava());

            myList = myQuery.getResultList();

        } catch(Exception IGNORE) {
        }
        return myList;
    }

    public static List<SourceAclEntry> getKnownSourceControls() {

        List<SourceAclEntry> myList = null;

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query myQuery = myManager.createQuery(AclCommands.GET_SOURCE_CONTROLS.getJava());

            myList = myQuery.getResultList();

        } catch(Exception myException) {

            LOG.error("Caught exception\n" + Format.value(myException));
        }
        return myList;
    }

    @SuppressWarnings("unchecked")
    public static AclResourceType getResourceType(String uuidIn) {

        Resource myResult = getResource(uuidIn);

        return (null != myResult) ? myResult.getResourceType() : null;
    }

    @SuppressWarnings("unchecked")
    public static Resource getResource(String uuidIn) {

        Resource myResult = null;
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery(AclCommands.GET_RESOURCE.getJava());
        myQuery.setParameter("uuid", uuidIn);

        try {

            myResult = (Resource)myQuery.getSingleResult();

        } catch (NoResultException IGNORE) {
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static String getResourceName(String uuidIn) {

        String myResult = null;
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery(AclCommands.GET_RESOURCE_NAME.getJava());
        myQuery.setParameter("uuid", uuidIn);

        try {

            myResult = (String)myQuery.getSingleResult();

        } catch (NoResultException IGNORE) {
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static boolean renameResource(String uuidIn, String nameIn, String remarksIn) {

        boolean myResult = false;

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query myQuery = myManager.createQuery(AclCommands.RENAME_RESOURCE.getJava());
            myQuery.setParameter("uuid", uuidIn);
            myQuery.setParameter("name", nameIn);
            myQuery.setParameter("remarks", remarksIn);
            myQuery.executeUpdate();
            myResult = true;

        } catch (Exception myException) {

            LOG.error("Caught exception renaming resource:\n" + Format.value(myException));
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static AclResourceType findOwnedResourceType(String uuidIn) throws CsiSecurityException {

        AclResourceType myResult = null;
        Query myQuery = createJavaQuery(AclFunction.LIST_PRIOR_TYPE, AclScope.OWNED, AclResource.UNKNOWN_RESOURCE,
                                            AclCondition.UUID_MATCH, new String[]{uuidIn});

        if (null != myQuery) {

            try {

                myResult = (AclResourceType)myQuery.getSingleResult();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static String getUuidFromIdAvoidingSecurity(Class<?> objectTypeIn, String IdIn) {

        String myResult = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            try {

                String myQueryString = "SELECT d.uuid FROM " + myTable.getJavaName() + " WHERE d.id = :id";
                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                Query myQuery = myManager.createQuery(myQueryString);
                myQuery.setParameter("id", IdIn);

                myResult = (String)myQuery.getSingleResult();

            } catch (Exception IGNORE) {
            }
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findOwnedResourceByName(Class<T> objectTypeIn, String nameIn)
            throws CsiSecurityException {

        T myResult = null;
        Query myQuery = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.OWNED, myTable, AclCondition.NAME_MATCH,
                    new String[]{nameIn});
        }

        if (null != myQuery) {

            try {

                myResult = (T) myQuery.getSingleResult();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findOwnedResourceByName(Class<T> objectTypeIn, String nameIn,
                                                                 AclControlType accessModeIn)
            throws CsiSecurityException {

        T myResult = null;
        Query myQuery = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.OWNED_AUTHORIZED, myTable,
                                            AclCondition.NAME_MATCH,
                    new String[]{nameIn}, new AclControlType[]{accessModeIn});
        }

        if (null != myQuery) {

            try {

                myResult = (T) myQuery.getSingleResult();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findOwnedResourceByName(Class<T> objectTypeIn, String nameIn, String ownerIn)
            throws CsiSecurityException {

        T myResult = null;

        if (null != ownerIn) {

            Query myQuery = null;
            AclResource myTable = identifyTable(objectTypeIn);

            if (null != myTable) {

                Set<String> myRoleSet = new HashSet<String>(Arrays.asList(ownerIn));
                List<Set<String>> myArguments = new ArrayList<Set<String>>();
                myArguments.add(myRoleSet);

                myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.OWNED_BY_OTHER, myTable,
                        AclCondition.NAME_MATCH, new String[]{nameIn}, myArguments);
            }

            if (null != myQuery) {

                try {

                    myResult = (T) myQuery.getSingleResult();

                } catch (NoResultException IGNORE) {
                }
            }
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findOwnedResourceByName(Class<T> objectTypeIn, String nameIn, String ownerIn,
                                                                 AclControlType accessModeIn)
            throws CsiSecurityException {

        T myResult = null;

        if (null != ownerIn) {

            Query myQuery = null;
            AclResource myTable = identifyTable(objectTypeIn);

            if (null != myTable) {

                Set<String> myRoleSet = new HashSet<String>(Arrays.asList(ownerIn));
                List<Set<String>> myArguments = new ArrayList<Set<String>>();
                myArguments.add(myRoleSet);

                myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.checkOwnerRequirement(accessModeIn),
                                                myTable, AclCondition.NAME_MATCH, new String[]{nameIn},
                                                new AclControlType[]{accessModeIn}, myArguments);
            }

            if (null != myQuery) {

                try {

                    myResult = (T) myQuery.getSingleResult();

                } catch (NoResultException IGNORE) {
                }
            }
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findFirstResourceByName(Class<T> objectTypeIn, String nameIn,
                                                                 AclControlType accessModeIn)
            throws CsiSecurityException {

        T myResult = null;
        List<T> myResults = findResourcesByName(objectTypeIn, nameIn, accessModeIn);

        if ((null != myResults) && !myResults.isEmpty()) {

            myResult = myResults.get(0);
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findFirstResourceByName(Class<T> objectTypeIn, String nameIn)
            throws CsiSecurityException {

        T myResult = null;
        List<T> myResults = findResourcesByName(objectTypeIn, nameIn);

        if ((null != myResults) && !myResults.isEmpty()) {

            myResult = myResults.get(0);
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findResourceByName(Class<T> objectTypeIn, String nameIn)
            throws CsiSecurityException {

        T myResult = findOwnedResourceByName(objectTypeIn, nameIn);

        if (null == myResult) {

            myResult = findFirstResourceByName(objectTypeIn, nameIn);
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findResourceByName(Class<T> objectTypeIn, String nameIn,
                                                            AclControlType accessModeIn) throws CsiSecurityException {

        T myResult = findOwnedResourceByName(objectTypeIn, nameIn, accessModeIn);

        if (null == myResult) {

            myResult = findFirstResourceByName(objectTypeIn, nameIn, accessModeIn);
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findResourceByName(Class<T> objectTypeIn, String nameIn, String ownerIn,
                                                            AclControlType accessModeIn) throws CsiSecurityException {

        T myResult = findOwnedResourceByName(objectTypeIn, nameIn, ownerIn, accessModeIn);

        if (null == myResult) {

            myResult = findFirstResourceByName(objectTypeIn, nameIn, accessModeIn);
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findResourceByName(Class<T> objectTypeIn, String nameIn, String ownerIn)
            throws CsiSecurityException {

        T myResult = findOwnedResourceByName(objectTypeIn, nameIn, ownerIn);

        if (null == myResult) {

            myResult = findFirstResourceByName(objectTypeIn, nameIn);
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findResourceById(Class<T> objectTypeIn, String uuidIn)
            throws CsiSecurityException {

        T myResult = null;
        Query myQuery = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.ANY, myTable, AclCondition.UUID_MATCH,
                                            new String[]{uuidIn});
        }

        if (null != myQuery) {

            try {

                myResult = (T) myQuery.getSingleResult();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T findResourceById(Class<T> objectTypeIn, String uuidIn,
                                                          AclControlType accessModeIn) throws CsiSecurityException {

        T myResult = null;

        Query myQuery = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.checkRequirement(accessModeIn), myTable,
                    AclCondition.UUID_MATCH, new String[]{uuidIn}, new AclControlType[]{accessModeIn});
        }

        if (null != myQuery) {

            try {

                myResult = (T) myQuery.getSingleResult();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResult;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> List<T> listAllAuthorizedResources(Class<T> objectTypeIn,
                                                                          AclControlType[] permissionsIn)
            throws CsiSecurityException {

        List<T> myResults = null;
        Query myQuery = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.AUTHORIZED, myTable, permissionsIn,
                                            new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});
        }

        if (null != myQuery) {

            try {

                myResults = myQuery.getResultList();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static List<String> listInstalledTableOwners() throws CsiSecurityException {

        List<String> myResults = null;
        Query myQuery = createJavaQuery(AclFunction.DISTINCT_TABLE_OWNERS, AclScope.ANY, AclResource.EVERY_TABLE);

        if (null != myQuery) {

            try {

                myResults = myQuery.getResultList();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static List<String> listInstalledTableSchemas() throws CsiSecurityException {

        List<String> myResults = null;
        Query myQuery = createJavaQuery(AclFunction.DISTINCT_TABLE_SCHEMAS, AclScope.ANY, AclResource.EVERY_TABLE);

        if (null != myQuery) {

            try {

                myResults = myQuery.getResultList();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static List<String> listInstalledTableTypes() throws CsiSecurityException {

        List<String> myResults = null;
        Query myQuery = createJavaQuery(AclFunction.DISTINCT_TABLE_TYPES, AclScope.ANY, AclResource.EVERY_TABLE);

        if (null != myQuery) {

            try {

                myResults = myQuery.getResultList();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static List<InstalledTable> listAuthorizedInstalledTable(String topLevelIn, String midLevelIn,
                                                                    String lowLevelIn, AclControlType[] permissionsIn)
            throws CsiSecurityException {

        List<InstalledTable> myResults = null;
        Query myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.AUTHORIZED, AclResource.TABLE,
                                            AclCondition.TOP_MID_MATCH, new String[]{ topLevelIn, midLevelIn},
                                            permissionsIn, new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});

        if (null != myQuery) {

            try {

                myResults = myQuery.getResultList();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> List<T> findResourcesByName(Class<T> objectTypeIn, String nameIn)
            throws CsiSecurityException {

        List<T> myResults = null;
        Query myQuery = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.ANY, myTable,
                    AclCondition.NAME_MATCH, new String[]{nameIn});
        }

        if (null != myQuery) {

            try {

                myResults = myQuery.getResultList();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> List<T> findResourcesByName(Class<T> objectTypeIn, String nameIn,
                                                                   AclControlType accessModeIn)
            throws CsiSecurityException {

        List<T> myResults = null;
        Query myQuery = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.checkRequirement(accessModeIn), myTable,
                                            AclCondition.NAME_MATCH, new String[]{nameIn},
                                            new AclControlType[]{accessModeIn});
        }

        if (null != myQuery) {

            try {

                myResults = myQuery.getResultList();

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    public static <T extends Resource> boolean resourceNameExists(Class<T> objectTypeIn, String nameIn)
            throws CsiSecurityException {

        boolean myResult = false;
        Query myQuery = null;
        AclResource myTable = identifyTable(objectTypeIn);

        if (null != myTable) {

            myQuery = createJavaQuery(AclFunction.COUNT, AclScope.ANY, myTable, AclCondition.NAME_MATCH,
                                            new String[]{nameIn});
        }

        if (null != myQuery) {

            try {

                Long myCount = (Long) myQuery.getSingleResult();

                myResult = ((null != myCount) && (0 < myCount));

            } catch (NoResultException IGNORE) {
            }
        }
        return myResult;
    }

    public static boolean isAuthorized(String uuidIn, AclControlType[] permissionsIn) throws CsiSecurityException {

        return isAuthorized(uuidIn, permissionsIn, true);
    }

    public static boolean isAuthorizedAll(String uuidIn, AclControlType[] permissionsIn, boolean doSecurityIn)
            throws CsiSecurityException {

        for (int i = 0; permissionsIn.length > i; i++) {

            if (!isAuthorized(uuidIn, new AclControlType[]{permissionsIn[i]}, doSecurityIn)) {

                return false;
            }
        }
        return true;
    }

    public static boolean isAuthorized(String uuidIn, AclControlType[] permissionsIn, boolean doSecurityIn)
            throws CsiSecurityException {

        boolean myResult = false;

        if (CsiSecurityManager.getUserName().equalsIgnoreCase(JAASRole.ADMIN_USER_NAME)) {

            myResult = true;
            if ((null != permissionsIn) && (0 < permissionsIn.length)) {

                for (AclControlType myPermission : permissionsIn) {

                    if ((AclControlType.SHARE != myPermission) && (AclControlType.DELETE != myPermission) && (AclControlType.TRANSFER != myPermission)) {

                        myResult = false;
                        break;
                    }
                }
            }

        } else if (CsiSecurityManager.getUserName().equalsIgnoreCase(JAASRole.SECURITY_USER_NAME)) {

            myResult = true;
            if ((null != permissionsIn) && (0 < permissionsIn.length)) {

                for (AclControlType myPermission : permissionsIn) {

                    if (AclControlType.CLASSIFY != myPermission) {

                        myResult = false;
                        break;
                    }
                }
            }

        } else {

            boolean myIsDelete = ((null != permissionsIn) && (1 == permissionsIn.length) && (AclControlType.DELETE == permissionsIn[0]));
            SecurityMask mySecurityEnforced = myIsDelete ? SecurityMask.getNoSecurityMask() : new SecurityMask(true);
            AclControlType[] myPermissions = null;
            AclScope myScope = null;
            Query myQuery = null;

            if (null != permissionsIn) {

                List<AclControlType> myScratchPermissions = new ArrayList<AclControlType>(permissionsIn.length);

                myScope = AclScope.AUTHORIZED;
                for (AclControlType myPermission : permissionsIn) {

                    if (AclControlType.SOURCE_EDIT == myPermission) {

                        myScratchPermissions.add(AclControlType.EDIT);
                        if (mySecurityEnforced.hasSecurity()) {

                            mySecurityEnforced.addConfiguredSourceEditRestrictions();
                        }

                    } else if (AclControlType.EXPORT == myPermission) {

                        myScratchPermissions.add(AclControlType.READ);
                        if (mySecurityEnforced.hasSecurity()) {

                            mySecurityEnforced.addConfiguredExportRestrictions();
                        }

                    } else {

                        myScratchPermissions.add(myPermission);
                    }
                }
                myPermissions = myScratchPermissions.isEmpty()
                                   ? new AclControlType[] { AclControlType.READ }
                                   : myScratchPermissions.toArray(new AclControlType[0]);
            }
            if (doSecurityIn && mySecurityEnforced.hasSecurity()) {

                myQuery = createJavaQuery(AclFunction.COUNT_ACL, myScope, AclResource.ACL,
                                                AclCondition.ACL_MATCH, new String[]{uuidIn},
                                                myPermissions, mySecurityEnforced);

                if (null != myQuery) {

                    myResult = (0L < (Long) myQuery.getSingleResult());

                    if ((!myResult) && mySecurityEnforced.hasSecurity()) {

                        // Verify the item exists (assuming it has an ACL) -- if not, we are authorized
                        myQuery = createJavaQuery(AclFunction.COUNT_ACL, myScope, AclResource.ACL,
                                                        AclCondition.ACL_MATCH, new String[]{uuidIn}, myPermissions,
                                                        new SecurityMask(false));

                        if (null != myQuery) {

                            if (0L != (Long) myQuery.getSingleResult()) {

                                // Log blocked for security reasons
                                LOG.error("User " + Format.value(CsiSecurityManager.getUserName())
                                            + " blocked by security from accessing resource with UUID "
                                            + Format.value(uuidIn));
/*
                                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                                String myQueryString = "SELECT a FROM ACL a WHERE a.uuid = :parm1";
                                Query myDebugQuery = myManager.createQuery(myQueryString);
                                myDebugQuery.setParameter("parm1", uuidIn);
                                List<ACL> myAclList = myDebugQuery.getResultList();
                                LOG.info("hello");
*/
                            }
                        }
                    }
                }

            } else {

                myQuery = createJavaQuery(AclFunction.COUNT_ACL, myScope, AclResource.ACL,
                                                AclCondition.ACL_MATCH, new String[]{uuidIn}, myPermissions,
                                                new SecurityMask(false));
                if (null != myQuery) {

                    myResult = (0L < (Long) myQuery.getSingleResult());
                }
            }
        }
        return myResult;
    }

    public static List<ACL> getResourceAcl(List<String> resourceIdsIn) throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_ACL, AclScope.ANY, AclResource.ACL,
                AclCondition.ACL_LIST, new Object[]{resourceIdsIn});

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

    public static List<ResourceBasics> listProtectedMatchingTablesAvoidingSecurity(String patternIn)
            throws CsiSecurityException {

        ResourceFilter myFilter = new ResourceFilter();
        String myUserName = CsiSecurityManager.getUserName();
        AclResource myResource = resourceTypeMap.get(AclResourceType.DATA_TABLE);

        myFilter.setMatchDisplayPattern(patternIn);
        myFilter.setEditRejectString(myUserName);
        Query myQuery = buildFilterQuery(AclFunction.LIST_BASICS, myResource,
                                            myFilter, null, null, null, null);

        return retrieveResourceBasicsList(myQuery);
    }

    public static List<ResourceBasics> listOwnedMatchingTablesAvoidingSecurity(String patternIn, AclControlType[] permissionsIn)
            throws CsiSecurityException {

        return filterOwnedResourcesBasedOnStringAvoidingSecurity(AclResourceType.DATA_TABLE, patternIn, permissionsIn);
    }

    public static List<ResourceBasics> listOwnedMatchingTables(String patternIn, AclControlType[] permissionsIn)
            throws CsiSecurityException {

        return filterOwnedResourcesBasedOnString(AclResourceType.DATA_TABLE, patternIn, permissionsIn);
    }

    public static List<ResourceBasics> listMatchingTablesAvoidingSecurity(String patternIn, AclControlType[] permissionsIn)
            throws CsiSecurityException {

        return filterResourcesBasedOnStringAvoidingSecurity(AclResourceType.DATA_TABLE, patternIn, permissionsIn);
    }

    public static List<ResourceBasics> listMatchingTables(String patternIn, AclControlType[] permissionsIn)
            throws CsiSecurityException {

        return filterResourcesBasedOnString(AclResourceType.DATA_TABLE, patternIn, permissionsIn);
    }

    public static List<ResourceBasics> filterResourcesBasedOnString(AclResourceType resourceTypeIn, String matchingIn,
                                                                    AclControlType[] permissionsIn)
            throws CsiSecurityException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        Query myQuery = buildFilterQuery(AclFunction.LIST_BASICS, myResource, null,
                                            matchingIn, null, permissionsIn, new SecurityMask(true));

        return retrieveResourceBasicsList(myQuery);
    }

    public static List<ResourceBasics> filterOwnedResourcesBasedOnString(AclResourceType resourceTypeIn,
                                                                         String matchingIn,
                                                                         AclControlType[] permissionsIn)
            throws CsiSecurityException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        String myOwner = CsiSecurityManager.getUserName();
        Query myQuery = buildFilterQuery(AclFunction.LIST_BASICS, myResource, null,
                                            matchingIn, myOwner, permissionsIn, new SecurityMask(true));

        return retrieveResourceBasicsList(myQuery);
    }

    public static List<ResourceBasics> filterResourcesBasedOnStringAvoidingSecurity(AclResourceType resourceTypeIn,
                                                                                    String matchingIn,
                                                                                    AclControlType[] permissionsIn)
            throws CsiSecurityException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        Query myQuery = buildFilterQuery(AclFunction.LIST_BASICS, myResource,
                                            null, matchingIn, null, permissionsIn, null);

        return retrieveResourceBasicsList(myQuery);
    }

    public static List<ResourceBasics> filterOwnedResourcesBasedOnStringAvoidingSecurity(AclResourceType resourceTypeIn,
                                                                                         String matchingIn,
                                                                                         AclControlType[] permissionsIn)
            throws CsiSecurityException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        String myOwner = CsiSecurityManager.getUserName();
        Query myQuery = buildFilterQuery(AclFunction.LIST_BASICS, myResource,
                                            null, matchingIn, myOwner, permissionsIn, null);

        return retrieveResourceBasicsList(myQuery);
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> filterResources(AclResourceType resourceTypeIn,
                                                       ResourceFilter filterIn, AclControlType[] permissionsIn)
            throws CsiSecurityException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        Query myQuery = buildFilterQuery(AclFunction.LIST_BASICS, myResource, filterIn,
                                            null, null, permissionsIn, new SecurityMask(true));

        return retrieveResourceBasicsList(myQuery);
    }

    @SuppressWarnings("unchecked")
    public static List<String> retrieveDataViewsMarkedForDelete() throws CsiSecurityException {

        List<String> myResults = null;
        Query myQuery = createJavaQuery(AclFunction.LIST_ANY_UUID, AclScope.ANY, AclResource.DELETED_DATAVIEW,
                new SecurityMask(false));

        if (null != myQuery) {

            try {

                // Retrieve resource list
                myResults = myQuery.getResultList();
                // Mark resources for delete

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static List<String> retrieveTemplatesMarkedForDelete() throws CsiSecurityException {

        List<String> myResults = null;
        Query myQuery = createJavaQuery(AclFunction.LIST_ANY_UUID, AclScope.ANY, AclResource.DELETED_TEMPLATE,
                                            new SecurityMask(false));

        if (null != myQuery) {

            try {

                // Retrieve resource list
                myResults = myQuery.getResultList();
                // Mark resources for delete

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static List<String> reapResources(AclResourceType typeIn, Date[] filterIn) {

        return reapResources(typeIn, null, filterIn);
    }

    @SuppressWarnings("unchecked")
    public static List<String> reapResources(AclResourceType typeIn, String[] ownersIn, Date[] filterIn) {

        List<String> myResults = null;
        Query myQuery = buildReaperQuery(AclFunction.LIST_UUID, typeIn, ownersIn, filterIn);

        if (null != myQuery) {

            try {

                // Retrieve resource list
                myResults = myQuery.getResultList();
                // Mark resources for delete

            } catch (NoResultException IGNORE) {
            }
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static Query buildReaperQuery(AclFunction functionIn, AclResourceType typeIn, String[] ownersIn,
                                         Date[] filterIn) {

       /*
        Query myQuery = null;
        AclResource myResource = resourceTypeMap.get(typeIn);
        List<Set<String>> myArguments = new ArrayList<Set<String>>();
        List<AclControlType> myPermissionsList = new ArrayList<AclControlType>();
        String[] myOwners = ((null != ownersIn) && (0 < ownersIn.length)) ? ownersIn : null;
        AclScope myScope = (null != myOwners)
                                ? defineScope(myOwners, null, myArguments, myPermissionsList) : AclScope.ANY;

        if (null != myResource) {

            myQuery = createJavaQuery(functionIn, myScope, myResource, null, null, null, myArguments,
                                            new SecurityMask(false), filterIn);
        }
        return myQuery;
        */
       return null;
    }

    @SuppressWarnings("unchecked")
    public static List<String> listUserTablePaths(CsiFileType fileTypeIn) {

        String myUser = CsiSecurityManager.getUserName();
        StringBuilder myQueryBuffer = new StringBuilder();

        myQueryBuffer.append("SELECT d.name from InstalledTable d where d.topLevel = :topLevel AND d.midLevel = :midLevel AND d.useCount = 1");

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery(myQueryBuffer.toString());

        myQuery.setParameter("topLevel", myUser);
        myQuery.setParameter("midLevel", fileTypeIn.getExtension());

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

    @SuppressWarnings("unchecked")
    public static List<String> listUserTableNames(CsiFileType fileTypeIn) {

        String myUser = CsiSecurityManager.getUserName();
        String myQueryString = "SELECT d.baseName from InstalledTable d where d.topLevel = :topLevel AND d.midLevel = :midLevel AND d.useCount = 1";
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery(myQueryString);

        myQuery.setParameter("topLevel", myUser);
        myQuery.setParameter("midLevel", fileTypeIn.getExtension());

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

   public static boolean resourceExists(AclResourceType resourceTypeIn, String nameIn) throws CsiSecurityException {
      boolean result = false;
      Query query = createJavaQuery(AclFunction.COUNT, AclScope.OWNED, resourceTypeMap.get(resourceTypeIn),
                                    AclCondition.NAME_MATCH, new String[]{nameIn});

      if (query != null) {
         Long count = (Long) query.getSingleResult();

         result = ((count != null) && (count.intValue() > 0));
      }
      return result;
   }

    @SuppressWarnings("unchecked")
    public static List<String> listAllResourceNames(AclResourceType resourceTypeIn) throws CsiSecurityException {
        Query myQuery = createJavaQuery(AclFunction.LIST_NAMES, AclScope.ANY, resourceTypeMap.get(resourceTypeIn),
                                            new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

    @SuppressWarnings("unchecked")
    public static List<String> listUserResourceNames(AclResourceType resourceTypeIn) throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_NAMES, AclScope.OWNED, resourceTypeMap.get(resourceTypeIn),
                new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

    @SuppressWarnings("unchecked")
    public static List<String> listUserResourceNames(AclResourceType resourceTypeIn, String ownerIn)
            throws CsiSecurityException {

        Query myQuery =null;
        Set<String> myRoleSet = new HashSet<String>(Arrays.asList(ownerIn.trim().toLowerCase()));
        List<Set<String>> myArguments = new ArrayList<Set<String>>();
        myArguments.add(myRoleSet);

        myQuery = createJavaQuery(AclFunction.LIST_NAMES, AclScope.OWNED_BY_OTHER,
                                        resourceTypeMap.get(resourceTypeIn), new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC},
                                        myArguments);

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

   @SuppressWarnings("unchecked")
   public static Long countResource(AclResourceType resourceTypeIn, AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Long result = Long.valueOf(0L);
      Query query = createJavaQuery(AclFunction.COUNT, AclScope.AUTHORIZED,
                                    resourceTypeMap.get(resourceTypeIn), permissionsIn, null);

      if (query != null) {
         List<Long> results = query.getResultList();

         if (results != null) {
            result = results.get(0);
         }
      }
      return result;
   }

    @SuppressWarnings("unchecked")
    public static List<String> listAuthorizedResourceNames(AclResourceType resourceTypeIn,
                                                           AclControlType[] permissionsIn)
            throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_NAMES, AclScope.AUTHORIZED,
                                            resourceTypeMap.get(resourceTypeIn), permissionsIn,
                                            new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listUserResources(AclResourceType resourceTypeIn)
            throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED,
                resourceTypeMap.get(resourceTypeIn),
                new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC}, new SecurityMask(false));

        return retrieveResourceBasicsList(myQuery);
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listUserResources(AclResourceType resourceTypeIn, String patternIn)
            throws CsiSecurityException {

        String myFilter = " LIKE %" + patternIn;

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, myFilter, AclScope.OWNED,
                resourceTypeMap.get(resourceTypeIn),
                new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC}, new SecurityMask(false));

        return retrieveResourceBasicsList(myQuery);
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listOwnerResources(AclResourceType resourceTypeIn, String ownerIn)
            throws CsiSecurityException {

        String myOwner = (null != ownerIn) ? ownerIn.trim().toLowerCase() : null;

        if ((null != myOwner) && (0 < myOwner.length())) {

            Set<String> myRoleSet = new HashSet<String>(Arrays.asList(ownerIn));
            List<Set<String>> myArguments = new ArrayList<Set<String>>();
            myArguments.add(myRoleSet);

            Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED_BY_OTHER,
                    resourceTypeMap.get(resourceTypeIn),
                    new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC}, myArguments, new SecurityMask(false));

            return retrieveResourceBasicsList(myQuery);

        } else {

            return listUserResources(resourceTypeIn);
        }
    }

@SuppressWarnings("unchecked")
public static List<ResourceBasics> listAuthorizedUserResources(AclResourceType resourceTypeIn,
                                                               AclControlType[] permissionsIn)
        throws CsiSecurityException {

    Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED_AUTHORIZED,
                                        resourceTypeMap.get(resourceTypeIn), permissionsIn,
                                        new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});

    return retrieveResourceBasicsList(myQuery);
}
    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listAuthorizedUserResources(AclResourceType resourceTypeIn,
                                                                   AclControlType[] permissionsIn, String patternIn)
            throws CsiSecurityException {

        String myFilter = " LIKE %" + patternIn;

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, myFilter, AclScope.OWNED_AUTHORIZED,
                                            resourceTypeMap.get(resourceTypeIn), permissionsIn,
                                            new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});

        return retrieveResourceBasicsList(myQuery);
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listAuthorizedResourceExports(AclResourceType resourceTypeIn)
            throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED,
                                            resourceTypeMap.get(resourceTypeIn),
                                            new AclControlType[] {AclControlType.READ},
                                            new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC},
                                            SecurityMask.getExportSecurityMask());

        return retrieveResourceBasicsList(myQuery);
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listAnyAuthorizedResources(AclResourceType resourceTypeIn,
                                                                  AclControlType[] permissionsIn)
            throws CsiSecurityException {

        return listAnyAuthorizedResources(resourceTypeIn, permissionsIn, SecurityMask.getDefaultSecurityMask());
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listAnyAuthorizedResources(AclResourceType resourceTypeIn,
                                                                  AclControlType[] permissionsIn,
                                                                  SecurityMask enforceSecurityIn)
            throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED,
                resourceTypeMap.get(resourceTypeIn), permissionsIn,
                new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC}, enforceSecurityIn);

        return retrieveResourceBasicsList(myQuery);
    }

    @SuppressWarnings("unchecked")
    public static List<String> listUserDvNames() throws CsiSecurityException {

        return listUserResourceNames(AclResourceType.DATAVIEW);
    }

    @SuppressWarnings("unchecked")
    public static List<String> listAuthorizedDvNames(AclControlType[] permissionsIn) throws CsiSecurityException {

        return listAuthorizedResourceNames(AclResourceType.DATAVIEW, permissionsIn);
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listUserDvs() throws CsiSecurityException {

        return listUserResources(AclResourceType.DATAVIEW);
    }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listUserThemes() throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED, AclResource.THEME,
                                    new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC }, new SecurityMask(false));

      return ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                                        (query == null) ? null : query.getResultList(),
                                        new ArrayList<ResourceBasics>());
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listUserBasemaps() throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED, AclResource.MAP_BASEMAP,
                                    new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC }, new SecurityMask(false));

      return ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                                        (query == null) ? null : query.getResultList(),
                                        new ArrayList<ResourceBasics>());
   }

    @SuppressWarnings("unchecked")
    public static List<Resource> listUserResourceConflicts(AclResourceType typeIn, String nameIn) throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.OWNED, resourceTypeMap.get(typeIn),
                AclCondition.NAME_MATCH, new String[]{nameIn}, new SecurityMask(false));

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

    @SuppressWarnings("unchecked")
    public static List<DataView> listUserDvConflicts(String nameIn) throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.OWNED, AclResource.DATAVIEW,
                AclCondition.NAME_MATCH, new String[]{nameIn}, new SecurityMask(false));

        return (null != myQuery) ? myQuery.getResultList() : null;
    }

   @SuppressWarnings("unchecked")
   public static List<Theme> listUserThemeConflicts(String nameIn) throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.OWNED, AclResource.THEME,
                                    AclCondition.NAME_MATCH, new String[]{ nameIn }, new SecurityMask(false));

      return (query == null) ? null : query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public static List<Basemap> listUserBasemapConflicts(String nameIn) throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.OWNED, AclResource.MAP_BASEMAP,
                                    AclCondition.NAME_MATCH, new String[]{ nameIn }, new SecurityMask(false));

      return (query == null) ? null : query.getResultList();
   }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listAuthorizedUserDvs(AclControlType[] permissionsIn)
            throws CsiSecurityException {

        return listAuthorizedUserResources(AclResourceType.DATAVIEW, permissionsIn);
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listAuthorizedDvExports() throws CsiSecurityException {

        return listAuthorizedResourceExports(AclResourceType.DATAVIEW);
    }

   @SuppressWarnings("unchecked")
   public static List<String> listAuthorizedThemeUuids(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_UUID, AclScope.AUTHORIZED, AclResource.THEME,
                                    permissionsIn, new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return (query == null) ? null : query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedUserThemes(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED, AclResource.THEME,
                                    permissionsIn, new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                                        (query == null) ? null : query.getResultList(),
                                        new ArrayList<ResourceBasics>());
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedUserGraphThemes(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED, AclResource.GRAPH_THEME,
                                    permissionsIn, new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                                        (query == null) ? null : query.getResultList(),
                                        new ArrayList<ResourceBasics>());
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedUserMapThemes(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED, AclResource.MAP_THEME,
                                    permissionsIn, new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                                        (query == null) ? null : query.getResultList(),
                                        new ArrayList<ResourceBasics>());
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedUserBasemaps(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED, AclResource.MAP_BASEMAP,
                                    permissionsIn, new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                                        (query == null) ? null : query.getResultList(),
                                        new ArrayList<ResourceBasics>());
   }

   @SuppressWarnings("unchecked")
   public static List<String> listAuthorizedIconUuids(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_UUID, AclScope.AUTHORIZED, AclResource.ICON, permissionsIn, null);

      return (query == null) ? null : query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedUserIcons(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED, AclResource.ICON, permissionsIn, null);

      return ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                                        (query == null) ? null : query.getResultList(),
                                        new ArrayList<ResourceBasics>());
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedUserIcons(AclControlType[] permissionsIn, Integer start, Integer end)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, new AclScope[]{ AclScope.AUTHORIZED },
                                    AclResource.ICON, permissionsIn, null, start, end);

      return ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                                        (query == null) ? null : query.getResultList(),
                                        new ArrayList<ResourceBasics>());
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAnyAuthorizedDvs(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      return listAnyAuthorizedResources(AclResourceType.DATAVIEW, permissionsIn);
   }

   @SuppressWarnings("unchecked")
   public static List<String> listAllTemplateNames() throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_NAMES, AclScope.ANY, AclResource.TEMPLATE_RESOURCE,
                                    new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return (query == null) ? null : query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public static List<String> listUserTemplateNames() throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_NAMES, AclScope.OWNED, AclResource.TEMPLATE_RESOURCE,
                                    new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return (query == null) ? null : query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public static List<String> listAuthorizedTemplateNames(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_NAMES, AclScope.AUTHORIZED, AclResource.TEMPLATE_RESOURCE,
                                    permissionsIn, new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return (query == null) ? null : query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listUserTemplates() throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED, AclResource.TEMPLATE_RESOURCE,
                                    new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC }, new SecurityMask(false));

      return retrieveResourceBasicsList(query);
   }

   @SuppressWarnings("unchecked")
   public static List<DataViewDef> listUserTemplateConflicts(String nameIn) throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_OBJECT, AclScope.OWNED, AclResource.TEMPLATE,
                                    AclCondition.NAME_MATCH, new String[]{ nameIn }, new SecurityMask(false));

      return (query == null) ? null : query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedUserTemplates(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      Query query = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED_AUTHORIZED,
                                    AclResource.TEMPLATE_RESOURCE, permissionsIn,
                                    new ResourceSortMode[]{ ResourceSortMode.NAME_ALPHA_ASC });

      return retrieveResourceBasicsList(query);
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedTemplates(AclControlType[] permissionsIn)
         throws CsiSecurityException {
      return listAnyAuthorizedResources(AclResourceType.TEMPLATE, permissionsIn);
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedTemplatesForEdit() throws CsiSecurityException {
      return listAnyAuthorizedResources(AclResourceType.TEMPLATE, new AclControlType[]{AclControlType.EDIT},
                                        SecurityMask.getSourceEditSecurityMask());
   }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> listAuthorizedTemplateExports() throws CsiSecurityException {
      return listAuthorizedResourceExports(AclResourceType.TEMPLATE);
   }

    @SuppressWarnings("unchecked")
    public static List<String> listAuthorizedSampleNames(AclControlType[] permissionsIn) throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_NAMES, AclScope.AUTHORIZED, AclResource.ADMIN_TOOL_RESOURCE,
                                            permissionsIn, new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});
        List<String> myList = (null != myQuery) ? myQuery.getResultList() : new ArrayList<String>();

        myQuery = createJavaQuery(AclFunction.LIST_NAMES, AclScope.AUTHORIZED, AclResource.SAMPLE_RESOURCE,
                                        permissionsIn, new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});
        if (null != myQuery) {

            List<String> mySampleList = myQuery.getResultList();
            if (null != mySampleList) {

                myList.addAll(mySampleList);
            }
        }
        return myList.isEmpty() ? null : myList;
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listUserSamples() throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED, AclResource.ADMIN_TOOL_RESOURCE,
                                            new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC}, new SecurityMask(false));
        List<ResourceBasics> myList = (null != myQuery)
                                            ? retrieveResourceBasicsList(myQuery)
                                            : new ArrayList<ResourceBasics>();

        myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED, AclResource.SAMPLE_RESOURCE,
                                        new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC}, new SecurityMask(false));
        if (null != myQuery) {

            List<ResourceBasics> mySampleList = retrieveResourceBasicsList(myQuery);
            if (null != mySampleList) {

                myList.addAll(mySampleList);
            }
        }
        return myList.isEmpty() ? null : myList;
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listAuthorizedUserSamples(AclControlType[] permissionsIn)
            throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED_AUTHORIZED,
                                            AclResource.ADMIN_TOOL_RESOURCE, permissionsIn,
                                            new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});
        List<ResourceBasics> myList = (null != myQuery)
                ? retrieveResourceBasicsList(myQuery)
                : new ArrayList<ResourceBasics>();

        myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.OWNED_AUTHORIZED,
                                        AclResource.SAMPLE_RESOURCE, permissionsIn,
                                        new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});
        if (null != myQuery) {

            List<ResourceBasics> mySampleList = retrieveResourceBasicsList(myQuery);
            if (null != mySampleList) {

                myList.addAll(mySampleList);
            }
        }
        return myList.isEmpty() ? null : myList;
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listAuthorizedSamples(AclControlType[] permissionsIn)
            throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED,
                                            AclResource.ADMIN_TOOL_RESOURCE, permissionsIn,
                                            new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});
        List<ResourceBasics> myList = (null != myQuery)
                ? retrieveResourceBasicsList(myQuery)
                : new ArrayList<ResourceBasics>();

        myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED,
                                        AclResource.SAMPLE_RESOURCE, permissionsIn,
                                        new ResourceSortMode[]{ResourceSortMode.NAME_ALPHA_ASC});
        if (null != myQuery) {

            List<ResourceBasics> mySampleList = retrieveResourceBasicsList(myQuery);
            if (null != mySampleList) {

                myList.addAll(mySampleList);
            }
        }
        return myList.isEmpty() ? null : myList;
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceBasics> listRecentlyOpenedDataViews(AclControlType[] permissionsIn)
            throws CsiSecurityException {

        Query myQuery = createJavaQuery(AclFunction.LIST_BASICS, AclScope.AUTHORIZED, AclResource.DATAVIEW_RESOURCE,
                                            permissionsIn, new ResourceSortMode[]{ResourceSortMode.ACCESS_DATE_DESC,
                                            ResourceSortMode.NAME_ALPHA_ASC});

        return retrieveResourceBasicsList(myQuery);
    }

    public static List<AccessControlEntry> getResourceUsersAvoidingSecurity(List<String> uuidListIn) {

        List<AccessControlEntry> myList = null;

        if (null != uuidListIn) {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query myQuery = myManager.createQuery(AclCommands.GET_ACL_ENTRIES.getJava());
            myQuery.setParameter("uuids", uuidListIn);

            myList = myQuery.getResultList();
        }
        return myList;
    }

    public static List<AccessControlEntry> getResourceUsersAvoidingSecurity(String uuidIn) {

        if (null != uuidIn) {

            List<String> myUuidList = new ArrayList<String>(1);

            myUuidList.add(uuidIn);

            return getResourceUsersAvoidingSecurity(myUuidList);
        }
        return null;
    }

    public static ACL getAclAvoidingSecurity(String uuidIn) {

        ACL myResult = null;

        if (null != uuidIn) {

            List<ACL> myList = getResourceAclList(uuidIn);

            if ((null != myList) && !myList.isEmpty()) {

                myResult = myList.get(0);
            }
        }
        return myResult;
    }

    public static String getResourceOwnerAvoidingSecurity(String uuidIn) {

        String myResult = null;

        if (null != uuidIn) {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query myQuery = myManager.createQuery(AclCommands.GET_OWNER.getJava());
            myQuery.setParameter("uuid", uuidIn);

            myResult = (String)myQuery.getSingleResult();
        }
        return myResult;
    }

    public static List<AccessControlEntry> getResourceUsersAvoidingSecurity(Resource resourceIn) {

        return (null != resourceIn) ? getResourceUsersAvoidingSecurity(resourceIn.getUuid()) : null;
    }

    public static String getResourceOwnerAvoidingSecurity(Resource resourceIn) {

        return (null != resourceIn) ? getResourceOwnerAvoidingSecurity(resourceIn.getUuid()) : null;
    }

    public static List<SharingDisplay> getAclInfoAvoidingSecurity(List<String> uuidListIn)
            throws CsiSecurityException {

        List<SharingDisplay> myResultList = new ArrayList<SharingDisplay>();

        if (null != uuidListIn) {

            List<ACL> myList = getAclList(uuidListIn);

            if (null != myList) {

                for (ACL myAcl : myList) {

                    CsiPersistenceManager.refreshObject(myAcl);

                    myResultList.add(new SharingDisplay(myAcl.getUuid(), null,
                            null, null, null, null, null, null, null,
                            myAcl.getOwner(),myAcl.getEntries()));
                }
            }
        }
        return myResultList;
    }

    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:

    public static List<SharingDisplay> getSharingNamesAvoidingSecurity(AclResourceType resourceTypeIn,
                                                                       ResourceFilter filterIn, String patternIn,
                                                                       String ownerIn) throws CsiSecurityException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        List<SharingDisplay> myResultList = new ArrayList<SharingDisplay>();
        List<Object[]> myResults = null;
        Query myQuery = buildFilterQuery(AclFunction.LIST_RESOURCE, myResource,
                                            filterIn, patternIn, ownerIn, null, null);

        if (null != myQuery) {

            myResults = myQuery.getResultList();
        }
        return loadSharingResults(myResults, myResultList, false);
    }

    public static SharingDisplay getSingleSharingNameAvoidingSecurity(AclResourceType resourceTypeIn, String nameIn,
                                                                      String ownerIn) throws CsiSecurityException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        SharingDisplay myResults = null;
        List<Object[]> myResultList = null;
        Query myQuery = buildFilterQuery(AclFunction.LIST_RESOURCE_ACL, myResource, null, nameIn, ownerIn, null, null);

        if (null != myQuery) {

            myResultList = myQuery.getResultList();
            if ((null != myResultList) && !myResultList.isEmpty()) {

                myResults = loadSharingResults(myResultList.get(0), true);
            }
        }
        return myResults;
    }

    public static List<SharingDisplay> getSharingNamesAvoidingSecurity(List<String> uuidListIn)
            throws CsiSecurityException {

        List<SharingDisplay> myResultList = new ArrayList<SharingDisplay>();
        List<Object[]> myResults = null;
        String myQueryString = AclFunction.LIST_RESOURCE_ACL.getJava()
                + " FROM ModelResource d, ACL a WHERE d.uuid.uuid IN (:ids) AND d.uuid.uuid = a.uuid";
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery(myQueryString);

        if (null != myQuery) {

            myQuery.setParameter("ids", uuidListIn);
            myResults = myQuery.getResultList();
        }
        return loadSharingResults(myResults, myResultList, true);
    }

    @Synchronized
    public static ACL lockAcl(Resource resourceIn, String uuidIn) {

        ACL myAcl = null;

        if (null != uuidIn) {

            myAcl = getResourceACL(uuidIn);

            if (null == myAcl) {

                Authorization myAuthorization = CsiSecurityManager.getAuthorization();
                if (myAuthorization != null) {

                    String myOwner = myAuthorization.getName();
                    myAcl = createACL(resourceIn, myOwner);
                }
            }
        }
        if (null != myAcl) {

            myAcl.setLocked(true);
            CsiPersistenceManager.merge(myAcl);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();
        }
        return myAcl;
    }

    @Synchronized
    public static void unlockAcl(ACL aclIn) {

        aclIn.setLocked(false);
        CsiPersistenceManager.merge(aclIn);
        CsiPersistenceManager.commit();
        CsiPersistenceManager.begin();
    }

    public static void setRolePermissions(String resourceIn, SharingInitializationRequest sharingRequestIn) {

        ACL myAcl = getResourceACL(resourceIn);
        Resource myResource = getResource(resourceIn);
        List<AccessControlEntry> myEntryList = new ArrayList<AccessControlEntry>();
        AclResourceType myResourceType = (null != myResource) ? myResource.getResourceType() : null;
        String myType = (null != myResourceType) ? myResourceType.getLabel() : "";
        String myResourceName = getResourceName(myResource);
        String myDisplayName =  getDisplayName(myResourceName, resourceIn);

        if (null != sharingRequestIn) {

            addAclRoles(myEntryList, sharingRequestIn.getReadList(), AclControlType.READ);
            addAclRoles(myEntryList, sharingRequestIn.getEditList(), AclControlType.EDIT);
            addAclRoles(myEntryList, sharingRequestIn.getDeleteList(), AclControlType.DELETE);
        }

        if (null != myAcl) {

            LOG.info("Remove all ACL entries for " + myType + " " + myDisplayName
                    + " with owner " + Format.value(myAcl.getOwner()) + ".");
            myAcl.getEntries().clear();
            CsiPersistenceManager.merge(myAcl);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();
            logAclEntryAdd(myEntryList, myResourceName, myType);
            if (!myEntryList.isEmpty()) {

                myAcl.getEntries().addAll(myEntryList);
            }
            CsiPersistenceManager.merge(myAcl);

        } else {

            Authorization myAuthorization = CsiSecurityManager.getAuthorization();
            String myOwner = (null != myAuthorization) ? myAuthorization.getName().toLowerCase() : null;

            LOG.info("Create ACL for " + myType + " " + myDisplayName
                    + " with owner " + Format.value(myOwner) + ".");
            myAcl = new ACL(myOwner, resourceIn);
            logAclEntryAdd(myEntryList, myResourceName, myType);
            if (!myEntryList.isEmpty()) {

                myAcl.getEntries().addAll(myEntryList);
            }
            if (myResource != null) {
               myResource.setOwner(myOwner);
            }
            CsiPersistenceManager.persist(myAcl);
        }
    }

    public static void setRolePermissions(List<String> resourceListIn, SharingRequest sharingRequestIn) {

        if (null != sharingRequestIn) {

            for (String myResourceId : resourceListIn) {

                ACL myAcl = getResourceACL(myResourceId);

                if (null != myAcl) {

                    Map<String, AccessControlEntry> myEntryMap = mapSharing(myAcl);

                    myAcl.getEntries().clear();

                    removeAclRoles(myEntryMap, sharingRequestIn.getReadList().get(1), AclControlType.READ);
                    removeAclRoles(myEntryMap, sharingRequestIn.getEditList().get(1), AclControlType.EDIT);
                    removeAclRoles(myEntryMap, sharingRequestIn.getDeleteList().get(1), AclControlType.DELETE);

                    addAclRoles(myEntryMap, sharingRequestIn.getReadList().get(0), AclControlType.READ);
                    addAclRoles(myEntryMap, sharingRequestIn.getEditList().get(0), AclControlType.EDIT);
                    addAclRoles(myEntryMap, sharingRequestIn.getDeleteList().get(0), AclControlType.DELETE);

                    if (!myEntryMap.isEmpty()) {

                        myAcl.getEntries().addAll(myEntryMap.values());
                    }
                    CsiPersistenceManager.merge(myAcl);
                }
            }
        }
    }

    private static void removeAclRoles(Map<String, AccessControlEntry> mapIn, List<String> roleListIn, AclControlType modeIn) {

        String myBase = modeIn.name() + ":";

        for (String myRole : roleListIn) {

            String myRoleName = myRole.toLowerCase();
            String myKey = myBase + myRoleName;

            if (mapIn.containsKey(myKey)) {

                mapIn.remove(myKey);
            }
        }
    }

    private static void addAclRoles(Map<String, AccessControlEntry> mapIn, List<String> roleListIn, AclControlType modeIn) {

        String myBase = modeIn.name() + ":";

        for (String myRole : roleListIn) {

            String myRoleName = myRole.toLowerCase();
            String myKey = myBase + myRoleName;

            if (!mapIn.containsKey(myKey)) {

                mapIn.put(myKey, new AccessControlEntry(modeIn, myRoleName));
            }
        }
    }

    private static Map<String, AccessControlEntry> mapSharing(ACL aclIn) {

        Map<String, AccessControlEntry> myEntryMap = new HashMap<String, AccessControlEntry>();

        if (null != aclIn) {

            for (AccessControlEntry myEntry : aclIn.getEntries()) {

                myEntryMap.put((myEntry.getAccessType().name() + ":" + myEntry.getRoleName()), myEntry);
            }
        }
        return myEntryMap;
    }

    public static ACL createACL(Resource resourceIn, String ownerIn,
                                 List<ValuePair<AclControlType, String>> listIn) {

        ACL myAcl = createACL(resourceIn, ownerIn);

        if ((null != myAcl) && (null != listIn) && !listIn.isEmpty()) {

            List<AccessControlEntry> myAclList = myAcl.getEntries();
            AclResourceType myResourceType = (null != resourceIn) ? resourceIn.getResourceType() : null;
            String myType = (null != myResourceType) ? myResourceType.getLabel() : "";
            String myDisplayName = getResourceDisplayName(resourceIn, resourceIn.getUuid());
            String myOwner = myAcl.getOwner();

            for (ValuePair<AclControlType, String> myPair : listIn) {

                if (null != myPair) {

                    AclControlType myPermission = myPair.getValue1();
                    String myRoleIn = (null != myPair.getValue2()) ? myPair.getValue2().trim() : null;
                    String myRole = ((null != myRoleIn) && (0 < myRoleIn.length())) ? myRoleIn.toLowerCase() : null;

                    if ((null != myPermission) && (null != myRole) && (!myRole.equals(myOwner))) {

                        LOG.info("Grant " + myType + " " + myDisplayName + " "
                                + myPermission.getLabel() + " access to " + Format.value(myRoleIn) + ".");
                        myAclList.add(new AccessControlEntry(myPermission, myRole));
                    }
                }
            }
        }
        return myAcl;
    }

   private static ACL createACL(Resource resourceIn, String ownerIn) {
      String myUuid = resourceIn.getUuid();
      String myOwner = (ownerIn == null)
                          ? (resourceIn.getOwner() == null)
                                ? null
                                : resourceIn.getOwner().trim().toLowerCase()
                          : ownerIn.trim().toLowerCase();
      AclResourceType myResourceType = (null != resourceIn) ? resourceIn.getResourceType() : null;
      String myType = (null != myResourceType) ? myResourceType.getLabel() : "";
      String myDisplayName = getResourceDisplayName(resourceIn, myUuid);
      boolean myIncludeAdmin = !JAASRole.ADMIN_ROLE_NAME.equals(myOwner);

      LOG.info("Create ACL for " + myType + " " + myDisplayName + " with owner " + Format.value(myOwner) + ".");

      ACL myAcl = new ACL(myOwner, myUuid);
      List<AccessControlEntry> myAclList = myAcl.getEntries();

      // Grant all privileges to the owner and selected privileges to CSO and Admin
      if ((myOwner != null) && (myOwner.length() > 0)) {
         LOG.info("Grant " + myType + " " + myDisplayName + " "
               + AclControlType.READ.getLabel() + " access to " + Format.value(myOwner) + ".");
         myAclList.add(new AccessControlEntry(AclControlType.READ, myOwner));
         LOG.info("Grant " + myType + " " + myDisplayName + " "
               + AclControlType.EDIT.getLabel() + " access to " + Format.value(myOwner) + ".");
         myAclList.add(new AccessControlEntry(AclControlType.EDIT, myOwner));
         LOG.info("Grant " + myType + " " + myDisplayName + " "
               + AclControlType.DELETE.getLabel() + " access to " + Format.value(myOwner) + ".");
         myAclList.add(new AccessControlEntry(AclControlType.DELETE, myOwner));
      }
      if (myIncludeAdmin) {
         LOG.info("Grant " + myType + " " + myDisplayName + " "
               + AclControlType.DELETE.getLabel() + " access to " + Format.value(JAASRole.ADMIN_ROLE_NAME) + ".");
         myAclList.add(new AccessControlEntry(AclControlType.DELETE, JAASRole.ADMIN_ROLE_NAME));
      }
      CsiPersistenceManager.persist(myAcl);

      if (ownerIn != null) {
         resourceIn.setAclId(myAcl.getId());
         resourceIn.setOwner(myOwner);
         CsiPersistenceManager.merge(resourceIn);
      }
      return myAcl;
   }

    private static Query buildFilterQuery(AclFunction requestIn, AclResource resourceTypeIn, ResourceFilter filterIn,
                                          String matchPatternIn, String ownerIn, AclControlType[] permissionsIn,
                                          SecurityMask securityMaskIn)
            throws CsiSecurityException {

        List<String> myOwnerMatchList = (null != ownerIn) ? Arrays.asList(ownerIn) : null;
        List<String> myOwnerRejectList = null;
        List<String> myReadMatchList = null;
        List<String> myReadRejectList = null;
        List<String> myEditMatchList = null;
        List<String> myEditRejectList = null;
        List<String> myDeleteMatchList = null;
        List<String> myDeleteRejectList = null;
        String myMatchPattern = ((null != matchPatternIn) && (0 < matchPatternIn.length())) ? matchPatternIn : null;
        String myRejectPattern = null;
        boolean myTestName = true;
        boolean myTestRemarks = true;
        Date[] myTemporalParameters = null;
        ResourceSortMode[] mySortingRequest = null;
        MatchingMode myMatchingMode = null;

        if (null != filterIn) {

            myOwnerMatchList = filterIn.getOwnerMatchForAcl();
            myOwnerRejectList = filterIn.getOwnerRejectForAcl();
            myReadMatchList = filterIn.getAccessMatchForAcl();
            myReadRejectList = filterIn.getAccessRejectForAcl();
            myEditMatchList = filterIn.getEditMatchForAcl();
            myEditRejectList = filterIn.getEditRejectForAcl();
            myDeleteMatchList = filterIn.getDeleteMatchForAcl();
            myDeleteRejectList = filterIn.getDeleteRejectForAcl();
            if (null == myMatchPattern) {

                myMatchPattern = filterIn.getMatchPattern();
                myRejectPattern = filterIn.getRejectPattern();
                myTestName = filterIn.getTestName();
                myTestRemarks = filterIn.getTestRemarks();
            }
            myTemporalParameters = filterIn.getTemporalValuesForQuery();
            mySortingRequest = filterIn.getSortingRequest();
        }
        myMatchingMode = MatchingMode.determineMode(myMatchPattern, myRejectPattern, myTestName, myTestRemarks);
        return buildFilterQuery(requestIn, resourceTypeIn, myMatchPattern, myRejectPattern,
                myOwnerMatchList, myOwnerRejectList, myReadMatchList, myReadRejectList,
                myEditMatchList, myEditRejectList, myDeleteMatchList, myDeleteRejectList,
                myTemporalParameters, permissionsIn, mySortingRequest, myMatchingMode, securityMaskIn);
    }

    private static Query buildFilterQuery(AclFunction requestIn, AclResource resourceTypeIn,
                                          String matchPatternIn, String rejectPatternIn,
                                          List<String> ownerMatchIn, List<String> ownerRejectIn,
                                          List<String> readMatchIn, List<String> readRejectIn,
                                          List<String> editMatchIn, List<String> editRejectIn,
                                          List<String> deleteMatchIn, List<String> deleteRejectIn,
                                          Date[] temporalFiltersIn, AclControlType[] permissionsIn,
                                          ResourceSortMode[] sortModeIn, MatchingMode matchingModeIn,
                                          SecurityMask securityMaskIn)
            throws CsiSecurityException {

        Query query = null;
        SecurityMask mySecurityMask = (null != securityMaskIn)
                ? securityMaskIn
                : SecurityMask.getNoSecurityMask();
        String[] myPatterns = new String[]{matchPatternIn, rejectPatternIn};
        List<AclScope> myScope = new ArrayList<AclScope>();
        String myFilter = null;
        ResourceSortMode[] mySortMode = (null != sortModeIn)
                ? sortModeIn
                : new ResourceSortMode[] {ResourceSortMode.NAME_ALPHA_ASC, ResourceSortMode.OWNER_ALPHA_ASC};

        if ((null != permissionsIn) && (0 < permissionsIn.length)) {

            myScope.add(AclScope.AUTHORIZED);
        }
        if ((null != readMatchIn) && !readMatchIn.isEmpty()) {

            myScope.add(AclScope.READER_FILTER);
        }
        if ((null != readRejectIn) && !readRejectIn.isEmpty()) {

            myScope.add(AclScope.NOT_READER_FILTER);
        }
        if ((null != editMatchIn) && !editMatchIn.isEmpty()) {

            myScope.add(AclScope.EDITER_FILTER);
        }
        if ((null != editRejectIn) && !editRejectIn.isEmpty()) {

            myScope.add(AclScope.NOT_EDITER_FILTER);
        }
        if ((null != deleteMatchIn) && !deleteMatchIn.isEmpty()) {

            myScope.add(AclScope.DELETER_FILTER);
        }
        if ((null != deleteRejectIn) && !deleteRejectIn.isEmpty()) {

            myScope.add(AclScope.NOT_DELETER_FILTER);
        }
        if (myScope.isEmpty()) {
           if ((AclFunction.LIST_BASICS == requestIn) || CsiSecurityManager.isSpecialAccess()) {

              if ((null != ownerMatchIn) && !ownerMatchIn.isEmpty()) {

                  if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                      myFilter = AclScope.RESOURCE_OWNER_FILTER.getJava() + Constants.AND_MODIFIER
                              + AclScope.RESOURCE_NOT_OWNER_FILTER.getJava();

                  } else {

                      myFilter = AclScope.RESOURCE_OWNER_FILTER.getJava();
                  }

              } else  if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                  myFilter = AclScope.RESOURCE_NOT_OWNER_FILTER.getJava();
              }

          } else {

              myFilter = AclScope.RESOURCE_OWNED.getJava();
          }
        } else {
/*
        RESOURCE_OWNER_FILTER("(d.owner IN (:owners)) AND ", false, false, false),
        RESOURCE_NOT_OWNER_FILTER("(d.owner NOT IN (:notOwners)) AND ", false, false, false);

 */
            if ((AclFunction.LIST_BASICS == requestIn) || CsiSecurityManager.isSpecialAccess()) {

                if ((null != ownerMatchIn) && !ownerMatchIn.isEmpty()) {

                    myScope.add(AclScope.OWNER_FILTER);
                }
                if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                    myScope.add(AclScope.NOT_OWNER_FILTER);
                }

            } else {

                myScope.add(AclScope.OWNED);
            }
        }

        query = createJavaQuery(requestIn, myFilter, myScope.toArray(new AclScope[0]), resourceTypeIn, null, null,
                                    permissionsIn, mySortMode, myPatterns, null, mySecurityMask, temporalFiltersIn,
                                    null, null, matchingModeIn);

        if (query != null) {
           if (myScope.isEmpty()) {
              if ((AclFunction.LIST_BASICS == requestIn) || CsiSecurityManager.isSpecialAccess()) {

                 if ((null != ownerMatchIn) && !ownerMatchIn.isEmpty()) {

                    query.setParameter("owners", ownerMatchIn);
                 }
                 if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                    query.setParameter("notOwners", ownerRejectIn);
                 }

              } else {

                 query.setParameter("roles", CsiSecurityManager.getUserName());
              }
           } else {

              if ((null != readMatchIn) && !readMatchIn.isEmpty()) {

                 query.setParameter("readers", readMatchIn);
              }
              if ((null != readRejectIn) && !readRejectIn.isEmpty()) {

                 query.setParameter("notReaders", readRejectIn);
              }
              if ((null != editMatchIn) && !editMatchIn.isEmpty()) {

                 query.setParameter("editors", editMatchIn);
              }
              if ((null != editRejectIn) && !editRejectIn.isEmpty()) {

                 query.setParameter("notEditors", editRejectIn);
              }
              if ((null != deleteMatchIn) && !deleteMatchIn.isEmpty()) {

                 query.setParameter("deleters", deleteMatchIn);
              }
              if ((null != deleteRejectIn) && !deleteRejectIn.isEmpty()) {

                 query.setParameter("notDeleters", deleteRejectIn);
              }
              if ((null != ownerMatchIn) && !ownerMatchIn.isEmpty()) {

                 query.setParameter("owners", ownerMatchIn);
              }
              if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                 query.setParameter("notOwners", ownerRejectIn);
              }
           }
        }
        return query;
    }

    public static void setRolePermissions(List<String> resourcesIn, String roleIn, List<AclControlType> permissionsIn) {

        List<ACL> myList = getAclList(resourcesIn);
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        for (ACL myAcl : myList) {

            String myUuid = myAcl.getUuid();
            Resource myResource = getResource(myUuid);
            AclResourceType myResourceType = (null != myResource) ? myResource.getResourceType() : null;
            String myType = (null != myResourceType) ? myResourceType.getLabel() : "";
            String myDisplayName = (null != myResource) ? getResourceDisplayName(myResource) : Format.value((String)null);
            List<AccessControlEntry> myEntryList = removeRolePermissions(myAcl, roleIn);

            if (myEntryList != null) {
               for (AclControlType myPermission : permissionsIn) {
                  LOG.info("Grant " + myType + " " + myDisplayName + " "
                        + myPermission.getLabel() + " access to " + Format.value(roleIn) + ".");
                  myEntryList.add(new AccessControlEntry(myPermission, roleIn));
               }
            }
            myManager.merge(myAcl);
        }
    }

    public static void removeRolePermissions(List<String> resourcesIn, String roleIn) {

        List<ACL> myList = getAclList(resourcesIn);

        removeAclRolePermissions(myList, roleIn);
    }

    private static void removeAclRolePermissions(List<ACL> listIn, String roleIn) {

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        for (ACL myAcl : listIn) {

            try {

                removeRolePermissions(myAcl, roleIn);
                myManager.merge(myAcl);

            } catch (Exception myException) {

                LOG.error("Caught exception trying to remove " + Format.value(roleIn)
                            + " from ACL for resource with uuid " + Format.value(myAcl.getUuid()) + ".");
            }
        }
    }

    private static void addAclRoles(List<AccessControlEntry> entryListIn,
                                    List<String> roleListIn, AclControlType permissionIn) {

        if (null != roleListIn) {

            for (String myRole : roleListIn) {

                entryListIn.add(new AccessControlEntry(permissionIn, myRole.toLowerCase()));
            }
        }
    }

    private static List<AccessControlEntry> removeRolePermissions(ACL aclIn, String roleIn) {

        List<AccessControlEntry> myActualList = null;
        String myRole = (null != roleIn) ? roleIn.trim().toLowerCase() : null;

        if ((null != myRole) && (0 < myRole.length()) && (null != aclIn)) {

            String myUuid = aclIn.getUuid();
            Resource myResource = getResource(myUuid);
            AclResourceType myResourceType = (null != myResource) ? myResource.getResourceType() : null;
            String myType = (null != myResourceType) ? myResourceType.getLabel() : "";
            String myDisplayName = getResourceDisplayName(myResource, myUuid);

            myActualList = aclIn.getEntries();

            if ((null != myActualList) && !myActualList.isEmpty()) {

                List<AccessControlEntry> myWorkingList = new ArrayList<AccessControlEntry>(myActualList);

                for (int i = myWorkingList.size() - 1; 0 <= i; i--) {

                    AccessControlEntry myEntry = myWorkingList.get(i);

                    if (myRole.equals(myEntry.getRoleName())) {

                        LOG.info("Remove " + myType + " " + myDisplayName + " "
                                + myEntry.getAccessType().getLabel() + " access from " + Format.value(roleIn) + ".");
                        myActualList.remove(i);
                    }
                }
            }
        }
        return myActualList;
    }

    public static List<DataPair<String, String>> setResourceOwner(List<String> resourcesIn,
                                                                  String roleIn, Map<String, Object> conflictListIn) {

        String myRole = (null != roleIn) ? roleIn.trim().toLowerCase() : null;
        List<DataPair<String, String>> myChanges = new ArrayList<DataPair<String, String>>();

        if ((null != myRole) && (0 < myRole.length()) && (null != resourcesIn) && !resourcesIn.isEmpty()) {

            List<String> myList = new ArrayList<String>();

            for (String myUuid : resourcesIn) {
                String currentOwner = AclRequest.getResourceOwnerAvoidingSecurity(myUuid);

                if ((currentOwner == null) || !myRole.equalsIgnoreCase(currentOwner.trim())) {
                    LOG.info(Format.value(myRole) + " != " + ((currentOwner == null) ? "<null>" : Format.value(currentOwner.trim().toLowerCase())));
                    Resource myResource = getResource(myUuid);
                    AclResourceType myResourceType = (null != myResource) ? myResource.getResourceType() : null;
                    String myType = (null != myResourceType) ? myResourceType.getLabel() : "";
                    String myResourceName = getResourceName(myResource);
                    String myDisplayName = getDisplayName(myResourceName, myUuid);
                    String myNewName = SynchronizeChanges.guaranteeResourceName(myResourceName, conflictListIn);

                    if (!myNewName.equals(myResourceName)) {

                        myChanges.add(new DataPair<String, String>(myResourceName, myNewName));
                        LOG.info("Change owner for " + myType + " " + myDisplayName
                                + " from " + Format.value(currentOwner) + " to " + Format.value(roleIn)
                                + ", with new resource name " + Format.value(myNewName));

                        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                        Query myQuery = myManager.createQuery(AclCommands.CHANGE_NAME.getJava());
                        myQuery.setParameter("uuid", myUuid);
                        myQuery.setParameter("name", myNewName);
                        myQuery.executeUpdate();

                        conflictListIn.put(myNewName, null);

                    } else {

                        LOG.info("Change owner for " + myType + " " + Format.value(myDisplayName)
                                + " from " + Format.value(currentOwner) + " to " + Format.value(roleIn) + ".");
                    }
                    myList.add(myUuid);
                }
            }
            if (!myList.isEmpty()) {
                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                Query myQuery = myManager.createQuery(AclCommands.CHANGE_OWNER_ACL.getJava());
                myQuery.setParameter("resources", myList);
                myQuery.setParameter("owner", myRole);
                myQuery.executeUpdate();
                myQuery = myManager.createQuery(AclCommands.CHANGE_OWNER_RESOURCE.getJava());
                myQuery.setParameter("resources", myList);
                myQuery.setParameter("owner", myRole);
                myQuery.executeUpdate();
            }
        }
        return myChanges.isEmpty() ? null : myChanges;
    }

    public static List<String> findAdministratorsSampleTemplatesAvoidingSecurity() {

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        String myQueryString = "SELECT d.uuid.uuid from ModelResource d WHERE"
                                + " ((d.resourceType = 10) AND (d.owner = 'administrators'))";

        // System.out.println(sb.toString());
        Query myQuery = myManager.createQuery(myQueryString);

        try {

            return myQuery.getResultList();

        } catch(NoResultException myException) {

            return null;
        }
    }

    public static List<DataViewDef> loadAllOriginalSampleTemplatesAvoidingSecurity() {

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        String myQueryString = "SELECT d FROM DataViewDef d WHERE ((d.resourceType = 19) OR (d.resourceType = 10))"
                                + " AND ((d.owner = 'metaviewers') OR (d.owner = 'administrators')"
                                + " OR (d.owner = 'MetaViewers') OR (d.owner = 'Administrators') OR (d.owner IS NULL))";

        // System.out.println(sb.toString());
        Query myQuery = myManager.createQuery(myQueryString);

        try {

            return myQuery.getResultList();

        } catch(NoResultException myException) {

            return null;
        }
    }

    public static List<String> findOrphanDataViewsAvoidingSecurity() {

        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        StringBuilder sb = new StringBuilder();
        sb.append(AclFunction.LIST_UUID.getJava());
        sb.append(" FROM ");
        sb.append(AclResource.DATAVIEW.getJavaName());
        sb.append(" WHERE ");
        sb.append(AclScope.NO_ACL.getJava());

        // System.out.println(sb.toString());
        Query myQuery = myManager.createQuery(sb.toString());

        try {

            return myQuery.getResultList();

        } catch(NoResultException myException) {

            return null;
        }
    }

    public static List<String> findOldDataViewsAvoidingSecurity(int ageInDaysIn) {
/*
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

        StringBuilder sb = new StringBuilder();
        sb.append(AclFunction.LIST_UUID.getJava());
        sb.append(" FROM ");
        sb.append(AclResource.DATAVIEW.getJavaName());

        // System.out.println(sb.toString());
        Query myQuery = myManager.createQuery(sb.toString());

        try {

            return (List<String>) myQuery.getResultList();

        } catch(NoResultException myException) {

            return null;
        }
*/
        return null;
    }

    private static void logAclEntryRemove(List<AccessControlEntry> listIn, String resourceIn, String typeIn) {

        for (AccessControlEntry myEntry : listIn) {

            LOG.info("Remove " + typeIn + " " + Format.value(resourceIn) + " "
                    + myEntry.getAccessType().getLabel() + " access to " + Format.value(myEntry.getRoleName()) + ".");
        }
    }

    private static void logAclEntryAdd(List<AccessControlEntry> listIn, String resourceIn, String typeIn) {

        for (AccessControlEntry myEntry : listIn) {

            LOG.info("Grant " + typeIn + " " + Format.value(resourceIn) + " "
                    + myEntry.getAccessType().getLabel() + " access to " + Format.value(myEntry.getRoleName()) + ".");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ACL> getAclList(List<String> resourcesIn) {

        List<ACL> myResults = null;
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery(AclCommands.GET_MULTIPLE_RESOURCE_ACL_LIST.getJava());
        myQuery.setParameter("resources", resourcesIn);

        try {

            myResults = myQuery.getResultList();

        } catch (Exception IGNORE) {
        }
        return myResults;
    }

    public static ACL getResourceACL(String uuidIn) {

        List<ACL> myList = getResourceAclList(uuidIn);

        return ((null != myList) && !myList.isEmpty()) ? myList.get(0) : null;
    }

    private static List<ACL> getResourceAclList(String uuidIn) {

        List<ACL> myResults = null;
        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
        Query myQuery = myManager.createQuery(AclCommands.GET_SINGLE_RESOURCE_ACL_LIST.getJava());

        myQuery.setParameter("uuid", uuidIn);

        try {

            myResults = myQuery.getResultList();

        } catch (Exception IGNORE) {
        }
        return myResults;
    }

    @SuppressWarnings("unchecked")
    public static String makeUniqueResourceName(Class<? extends Resource> clz, String name) {
        String jpql = "select res.name from " + clz.getName() + " res where res.name = :name or res.name like '"
                        + name.replace("'", "''") + "%'";
        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
        Query q = em.createQuery(jpql);
        q.setParameter("name", name);
        List<String> nameList = q.getResultList();
        Set<String> nameset = new HashSet<String>();
        for (String n : nameList) {
            nameset.add(n.toLowerCase());
        }
        return CsiUtil.getDistinctName2(nameset, name);
    }

    public static boolean isUniqueResourceName(Class<? extends Resource> clz, String name) {
        /* CTWO-6750 -- using case-sensitive search for name */
        // used to call lower() function on the names for case-insenitive search.
        String jpql = "select count(res.name) from " + clz.getName() + " res where res.name = :name";
        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
        Query q = em.createQuery(jpql);
        q.setParameter("name", name);
        try {
            long result = (Long) q.getSingleResult();
            return result == 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            SecurityMask enforceSecurityIn) throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, null, null, null, null,
                                enforceSecurityIn, null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, null, null, null, null,
                                    new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            ResourceSortMode[] orderByIn) throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, null, orderByIn, null, null,
                                new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            ResourceSortMode[] orderByIn, List<Set<String>> othersIn) throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, null, orderByIn, null, othersIn,
                                new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, String filterIn, AclScope scopeIn,
                                            AclResource tableIn, ResourceSortMode[] orderByIn, SecurityMask enforceSecurityIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, filterIn, scopeIn, tableIn, null, null, null, orderByIn, null, null,
                                enforceSecurityIn, null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            ResourceSortMode[] orderByIn, SecurityMask enforceSecurityIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, null, orderByIn, null, null,
                                enforceSecurityIn, null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            ResourceSortMode[] orderByIn, List<Set<String>> otherIn,
                                            SecurityMask enforceSecurityIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, null, orderByIn, null, otherIn,
                                enforceSecurityIn, null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclCondition conditionIn, Object[] parametersIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, conditionIn, parametersIn, null, null, null, null,
                                    new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclCondition conditionIn, Object[] parametersIn,
                                            SecurityMask enforceSecurityIn) throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, conditionIn, parametersIn, null, null, null, null,
                                    enforceSecurityIn, null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclCondition conditionIn, Object[] parametersIn,
                                            AclControlType[] permissionsIn) throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, conditionIn, parametersIn, permissionsIn, null,
                                    null, null, new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclCondition conditionIn, Object[] parametersIn,
                                            AclControlType[] permissionsIn, SecurityMask enforceSecurityIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, conditionIn, parametersIn, permissionsIn, null,
                                    null, null, enforceSecurityIn, null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, String filterIn, AclScope scopeIn,
                                            AclResource tableIn, AclControlType[] permissionsIn, ResourceSortMode[] orderByIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, filterIn, scopeIn, tableIn, null, null, permissionsIn, orderByIn, null, null,
                                new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclControlType[] permissionsIn, ResourceSortMode[] orderByIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, permissionsIn, orderByIn, null, null,
                                new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope[] scopeIn, AclResource tableIn,
                                            AclControlType[] permissionsIn, ResourceSortMode[] orderByIn,
                                            Integer start, Integer end)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, permissionsIn, orderByIn, null, null,
                new SecurityMask(true), null, start, end, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclControlType[] permissionsIn, ResourceSortMode[] orderByIn,
                                            SecurityMask enforceSecurityIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, permissionsIn, orderByIn, null, null,
                                enforceSecurityIn, null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclCondition conditionIn, Object[] parametersIn,
                                            List<Set<String>> otherIn) throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, conditionIn, parametersIn, null, null, null,
                                    otherIn, new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclCondition conditionIn, Object[] parametersIn,
                                            AclControlType[] permissionsIn, List<Set<String>> otherIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, conditionIn, parametersIn, permissionsIn, null,
                                    null, otherIn, new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclCondition conditionIn, Object[] parametersIn,
                                            AclControlType[] permissionsIn, ResourceSortMode[] orderByIn)
            throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, conditionIn, parametersIn, permissionsIn,
                                    orderByIn, null, null, new SecurityMask(true), null, null);
    }

    private static Query createJavaQuery(AclFunction functionIn, AclScope scopeIn, AclResource tableIn,
                                            AclControlType[] permissionsIn, ResourceSortMode[] orderByIn, String patternIn,
                                            Date[] temporalParmsIn, MatchingMode matchingModeIn) throws CsiSecurityException {

        return createJavaQuery(functionIn, null, scopeIn, tableIn, null, null, permissionsIn, orderByIn, patternIn,
                                    null, new SecurityMask(true), temporalParmsIn, matchingModeIn);
    }

    private static Query createJavaQuery(AclFunction functionIn, String filterIn, AclScope scopeIn,
                                            AclResource tableIn, AclCondition conditionIn, Object[] parametersIn,
                                            AclControlType[] permissionsIn, ResourceSortMode[] orderByIn, String patternIn,
                                            List<Set<String>> otherIn, SecurityMask enforceSecurityIn,
                                            Date[] temporalParmsIn, MatchingMode matchingModeIn) throws CsiSecurityException {

        return createJavaQuery(functionIn, filterIn, new AclScope[]{scopeIn},
                                    tableIn, conditionIn, parametersIn, permissionsIn, orderByIn,
                                    new String[]{patternIn}, otherIn, enforceSecurityIn, temporalParmsIn, null, null, matchingModeIn);
    }

    private static Query createJavaQuery(AclFunction functionIn, String filterIn, AclScope[] scopeIn,
                                              AclResource tableIn, AclCondition conditionIn, Object[] parametersIn,
                                              AclControlType[] permissionsIn, ResourceSortMode[] orderByIn, String[] patternIn,
                                              List<Set<String>> otherIn, SecurityMask enforceSecurityIn,
                                              Date[] temporalParmsIn, Integer start, Integer end, MatchingMode matchingModeIn)
            throws CsiSecurityException {

        AclControlType[] myPermissions = expandPermissions(tableIn, permissionsIn);
        QueryBlock myQueryBlock = loadQueryBlock(new QueryBlock(), functionIn, filterIn, scopeIn, tableIn, patternIn,
                (null != enforceSecurityIn) ? enforceSecurityIn : new SecurityMask(true), matchingModeIn, _mode);

        String myQueryString = createQueryString(myQueryBlock, functionIn, filterIn,
                                                    scopeIn, tableIn, conditionIn, temporalParmsIn, orderByIn, _mode);
        LOG.debug("QUERY:\n" + myQueryString);

        try {

            // LOG.info("EXECUTE QUERY: " + Display.value(myQueryString));

            Query query = createQuery(myQueryBlock, myQueryString, parametersIn, myPermissions, patternIn, otherIn,
                    temporalParmsIn);

            if((start != null) && (end != null)){
                query.setFirstResult(start);
                query.setMaxResults(Math.abs(end-start));
            }
            return query;

        } catch (Exception myException) {

            LOG.error("Caught exception:\n" + Format.value(myException)
                    + "\n while building query:\n" + Format.value(myQueryString));
        }
        return null;
    }

    protected static Query createQuery(QueryBlock queryBlockIn, String queryStringIn,
                                       Object[] parametersIn, AclControlType[] permissionsIn,
                                       String[] patternIn, List<Set<String>> otherIn, Date[] temporalParmsIn) {
        Query myQuery = null;

        if (null != queryStringIn) {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            myQuery = myManager.createQuery(queryStringIn);

            if (_doDebug) {
               LOG.debug("Query String: [ " + queryStringIn + " ]");
            }

            if (queryBlockIn.applyScope() || queryBlockIn.enforceSecurity().hasSecurity()) {

                if (0 <= queryStringIn.indexOf(":roles")) {

                    if (_doDebug) {

                        LOG.debug("parameter \"roles\": [ " + CsiSecurityManager.getUserRoles() + " ]");
                    }
                    myQuery.setParameter("roles", CsiSecurityManager.getUserRoles());
                }
            }

            if (queryBlockIn.useOther()) {
               int howMany = otherIn.size();

                for (int i = 0; i < howMany; i++) {


                    if (_doDebug) {

                        LOG.debug("parameter \"other" + Integer.toString(i + 1) + "\": [ " + otherIn.get(i) + " ]");
                    }
                    myQuery.setParameter("other" + Integer.toString(i + 1), otherIn.get(i));
                }
            }

            if (null != queryBlockIn.getMatchingMode()) {

                if (0 <= queryStringIn.indexOf(":match")) {


                    if (_doDebug) {

                        LOG.debug("parameter \"match\": [ " + patternIn[0] + " ]");
                    }
                    myQuery.setParameter("match", patternIn[0]);
                }
                if (0 <= queryStringIn.indexOf(":reject")) {


                    if (_doDebug) {

                        LOG.debug("parameter \"reject\": [ " + patternIn[1] + " ]");
                    }
                    myQuery.setParameter("reject", patternIn[1]);
                }
            }

            if (queryBlockIn.requiresPermissions()) {

                if (0 <= queryStringIn.indexOf(":permissions")) {


                    if (_doDebug) {

                        LOG.debug("parameter \"permissions\": [ " + permissionsIn + " ]");
                    }
                    myQuery.setParameter("permissions", Arrays.asList(permissionsIn));
                }
            }

            if ((null != parametersIn) && (0 < parametersIn.length)) {

                for (int i = 0; parametersIn.length > i; i++) {


                    if (_doDebug) {

                        LOG.debug("parameter \"parm" + Integer.toString(i + 1) + "\": [ " + parametersIn[i] + " ]");
                    }
                    myQuery.setParameter("parm" + Integer.toString(i + 1), parametersIn[i]);
                }
            }

            if (null != temporalParmsIn) {

                for (int i = 0; temporalParmsIn.length > i; i++) {

                    if (Constants.JAVA_TIMESTAMP_TEST.length <= i) {

                        break;
                    }
                    if (null != temporalParmsIn[i]) {

                        if (_doDebug) {

                            LOG.debug("parameter \"date" + Integer.toString(i + 1) + "\": [ "
                                    + temporalParmsIn[i] + " ]");
                        }
                        myQuery.setParameter("date" + Integer.toString(i + 1), temporalParmsIn[i]);
                    }
                }
            }
        }
        return myQuery;
    }

    private static List<ResourceBasics> retrieveResourceBasicsList(Query queryIn) {

        return (null != queryIn) ? ResourceBasics.loadResults(CsiSecurityManager.getUserName(),
                queryIn.getResultList(),
                new ArrayList<ResourceBasics>()) : null;
    }

    private static List<SharingDisplay> loadSharingResults(List<Object[]> listIn,
                                                             List<SharingDisplay> listOutIn, boolean hasAclIn)
            throws CsiSecurityException {

        List<SharingDisplay> myList = (null != listOutIn) ? listOutIn : new ArrayList<SharingDisplay>();

        if (null != listIn) {

            for (Object[] myResults : listIn) {

                SharingDisplay myItem = loadSharingResults(myResults, hasAclIn);

                if (null != myResults) {

                    myList.add(myItem);
                }
            }
        }
        return myList;
    }

    private static SharingDisplay loadSharingResults(Object[] rowIn, boolean hasAclIn)
            throws CsiSecurityException {

        SharingDisplay myResult = null;

        if (null != rowIn) {

            ACL myAcl = hasAclIn ? (ACL)rowIn[9] : null;
            String myOwner = !hasAclIn ? (String)rowIn[9] : null;
            List<AccessControlEntry> myAclList = null;

            if (null != myAcl) {

                CsiPersistenceManager.refreshObject(myAcl);
                myOwner = myAcl.getOwner();
                myAclList = myAcl.getEntries();
            }

            myResult = new SharingDisplay((String)rowIn[0], (String)rowIn[1], (String)rowIn[2],
                    (Date)rowIn[3], (Date)rowIn[4], (Date)rowIn[5], (Long)rowIn[6],
                    (Integer)rowIn[7], (Integer)rowIn[8], myOwner, myAclList);
        }
        return myResult;
    }
}
