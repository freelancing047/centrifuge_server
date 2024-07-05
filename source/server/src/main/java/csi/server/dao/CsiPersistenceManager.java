package csi.server.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;

import csi.config.Configuration;
import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.common.dto.user.RecentAccess;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.JdbcDriverParameterKey;
import csi.server.common.exception.AuthorizationException;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.model.themes.Theme;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.util.JndiUtil;
import csi.server.util.SqlUtil;

import lombok.Synchronized;

// TODO:
// 1. move model object related methods to ModelHelper
// 2. move cache connection methods to ConnectionFactoryManager
// Unsigned comment by hnguyen on 3/11/2010

public class CsiPersistenceManager {
   private static final Logger LOG = LogManager.getLogger(CsiPersistenceManager.class);

    public static final String PRIMARY_KEY_NAME = "uuid";

    private static final String CACHE_USER_NAME = "java:comp/env/jdbc/user";

    private static final String CACHE_NAME = "java:comp/env/jdbc/cache";


    private static final String METADB_RESOURCE_URL = "java:comp/env/jdbc/MetaDB";

    public static final String META_UNIT = "meta";

    @Autowired
    private BeanFactory beanFactory;

    private static EntityManagerFactory metaFactory;

    private static Integer incValue = 1;
    private static ThreadLocal<Integer> threadTag = new ThreadLocal<Integer>();

    private static ThreadLocal<EntityManager> localEntityManager = new ThreadLocal<EntityManager>();
    private static ThreadLocal<CsiConnection> cacheConnection = new ThreadLocal<CsiConnection>();
    private static ThreadLocal<CsiConnection> userConnection = new ThreadLocal<CsiConnection>();

    private static Map<String, List<DataView>> cachedDataViews = Maps.newConcurrentMap();
    private static Set<String> cachedDataViewUuids = new TreeSet<>();

    private static String[] iconManagementRoles = null;

    private static boolean _doDebug = LOG.isDebugEnabled();

    public static void logEntry(String taskIn, boolean forceCloseIn) {

        if (_doDebug) {
            if (null != localEntityManager.get()) {
                LOG.debug("+++++++++++ >>>>>>>>>>>>>> ENTER (" + taskIn + ") " + getThreadID().toString()
                        + " -- found connection open !!");
                if (forceCloseIn) {
                    LOG.debug("                                 (" + taskIn + ") " + getThreadID().toString()
                            + " -- forcing connection closed.");
                    close();
                }
            } else {
                LOG.debug("+++++++++++ >>>>>>>>>>>>>> ENTER (" + taskIn + ") " + getThreadID().toString());
            }
        } else {
            statEntry(taskIn, forceCloseIn);
        }
    }

    public static void logExit(String taskIn, boolean forceCloseIn) {

        if (_doDebug) {
            if (null != localEntityManager.get()) {
                LOG.debug("+++++++++++ <<<<<<<<<<<<<< EXIT (" + taskIn + ") " + getThreadID().toString()
                        + " -- left connection open !!");
                if (forceCloseIn) {
                    LOG.debug("                                (" + taskIn + ") " + getThreadID().toString()
                            + " -- forcing connection closed.");
                    close();
                }
            } else {
                LOG.debug("+++++++++++ <<<<<<<<<<<<<< EXIT (" + taskIn + ") " + getThreadID().toString());
            }
        } else {
            statExit(taskIn, forceCloseIn);
        }
    }

    public static boolean isActive() {

        return (null != localEntityManager.get());
    }

    public static void statEntry(String taskIn, boolean forceCloseIn) {

        if (null != localEntityManager.get()) {
            if (forceCloseIn) {
                LOG.error(taskIn + " found connection open -- forcing closed !!");
                close();
            } else {
                LOG.error(taskIn + " found connection open !!");
            }
        }
    }

    public static boolean markForDelete(Resource resourceIn) {

        boolean mySuccess = false;

        try {

            if ((resourceIn instanceof DataView) || (resourceIn instanceof DataViewDef)) {
                //Mark as deleted so it is never seen again
                resourceIn.markForDelete();
                resourceIn.resetTransients();
                getMetaEntityManager().merge(stripCredentials(resourceIn));
                commit();
                begin();
                mySuccess = true;
            }

        } catch (Exception ignore) { }

        return mySuccess;
    }

    public static void statExit(String taskIn, boolean forceCloseIn) {

        if (null != localEntityManager.get()) {
            if (forceCloseIn) {
                LOG.error(taskIn + " left connection open -- forcing closed !!");
                close();
            } else {
                LOG.error(taskIn + " left connection open !!");
            }
        }
    }

    public static EntityManager getMetaEntityManager() {
        EntityManager em = localEntityManager.get();

        if (em == null) {
            em = createMetaEntityManager();
            localEntityManager.set(em);
        }

        return em;
    }

    public static boolean hasEntityManager() {
        EntityManager em = localEntityManager.get();

        return (em != null);
    }

    public static void setFactory(EntityManagerFactory factory) {
        metaFactory = factory;
    }

    public static EntityManager createMetaEntityManager() {
        if (_doDebug) {
         LOG.debug("+++++++++++ >>>>>>>>>>>>>> openConnection(" + getThreadID().toString() + ")");
      }
        if (metaFactory == null) {
            metaFactory = Persistence.createEntityManagerFactory(META_UNIT);
        }
        return metaFactory.createEntityManager();
    }

    public static boolean isTransactionActive() {

        return getMetaEntityManager().getTransaction().isActive();
    }

    public static void begin() {
        EntityManager em = getMetaEntityManager();
        if (!em.getTransaction().isActive()) {
            if (_doDebug) {
               LOG.debug("                                 (" + getThreadID().toString() + ") begin()");
            }
            em.getTransaction().begin();
        }
    }

    /**
     * Also attempts to roll back exception
     */
    public static void close() {
        //Seems redundant checking has then using get, but get will always create a new instance if null,
        //that we just end up closing anyways
        if(!hasEntityManager()){
            return;
        }
        EntityManager em = getMetaEntityManager();
        if (em != null) {
            if (_doDebug) {
               LOG.debug("+++++++++++ <<<<<<<<<<<<<< closeConnection(" + getThreadID().toString() + ")");
            }
            localEntityManager.remove();
            try {
                if (em.getTransaction().isActive()) {
                    if (_doDebug) {
                     LOG.debug("                                 (" + getThreadID().toString() + ") rollback()");
                  }
                    em.getTransaction().rollback();
                }
            } finally {
                if (_doDebug) {
                  LOG.debug("                                 (" + getThreadID().toString() + ") close()");
               }
                em.close();
            }
        }
    }

    public static void flush() {

        if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") flush()");
      }
        getMetaEntityManager().flush();
    }

    public static <T extends Object> void persist(Collection<T> collection) {
        for (T object : collection) {
            persist(object);
        }
    }

    public static <T extends Object> T persist(T obj) {

		if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") persist()");
      }
        getMetaEntityManager().persist(obj);
        return obj;
    }

    public static <T extends Object> T merge(T obj) {
        if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") merge()");
      }
        T myItem = getMetaEntityManager().merge(obj);
        return myItem;
    }

    public static <T extends ModelObject> T merge(T obj) {
        obj.resetTransients();
        if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") merge()");
      }
        T myItem = getMetaEntityManager().merge(obj);
        return myItem;
    }

    public static <T extends Resource> T merge(T resourceIn) {
        // Record update timestamp
        resourceIn.updateLastUpdate();
        // Only treat DataViews and DataViewDefs as controlled resources
        if ((resourceIn instanceof DataView) || (resourceIn instanceof DataViewDef)
                || (resourceIn instanceof Theme) || (resourceIn instanceof Basemap)) {
            if (!CsiSecurityManager.isAuthorized(resourceIn.getUuid(), AclControlType.EDIT)) {
                String myName = resourceIn.getName();
                throw new AuthorizationException("Edit access to object " + Format.value(myName) + " denied.");
            }
            if (resourceIn instanceof DataView) {
                try {
                    AclRequest.recordAccess(new RecentAccess(CsiSecurityManager.getUserName(),
                            resourceIn.getUuid(), resourceIn.getName()));
                } catch (Exception IGNORE) {}
            }
        }
        resourceIn.resetTransients();
        if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") merge()");
      }
        getMetaEntityManager().merge(stripCredentials(resourceIn));

        return resourceIn;
    }

    public static <T extends Resource> T mergeForSecurity(T resourceIn) {
        // Record update timestamp
        resourceIn.updateLastUpdate();
        // Only treat DataViews and DataViewDefs as controlled resources
        if ((resourceIn instanceof DataView) || (resourceIn instanceof DataViewDef)
                || (resourceIn instanceof Theme) || (resourceIn instanceof Basemap)
                || (resourceIn instanceof InstalledTable)) {
            if (!CsiSecurityManager.canChangeSecurity(resourceIn.getUuid())) {
                String myName = resourceIn.getName();
                throw new AuthorizationException("Not authorized to change security for object " + Format.value(myName) + ".");
            }
            if (resourceIn instanceof DataView) {
                try {
                    AclRequest.recordAccess(new RecentAccess(CsiSecurityManager.getUserName(),
                            resourceIn.getUuid(), resourceIn.getName()));
                } catch (Exception IGNORE) {}
            }
        }
        resourceIn.resetTransients();
        if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") merge()");
      }
        getMetaEntityManager().merge(stripCredentials(resourceIn));

        return resourceIn;
    }

    public static void rollback() {
        EntityManager em = getMetaEntityManager();
        if (em.getTransaction().isActive()) {
            if (_doDebug) {
               LOG.debug("                                 (" + getThreadID().toString() + ") rollback()");
            }
            em.getTransaction().rollback();
        }
    }

    public static void markForRollback() {
        EntityManager em = getMetaEntityManager();
        EntityTransaction trans = em.getTransaction();
        if (trans.isActive()) {
            trans.setRollbackOnly();
        }
    }

    public static boolean isRollbackOnly() {
        EntityManager em = getMetaEntityManager();
        EntityTransaction trans = em.getTransaction();

        if (trans.isActive()) {
            return trans.getRollbackOnly();
        }

        return false;
    }

    public static void commit() {
        EntityManager em = getMetaEntityManager();
        if (em.getTransaction().isActive()) {
            if (_doDebug) {
               LOG.debug("                                 (" + getThreadID().toString() + ") commit()");
            }
            em.getTransaction().commit();
        }
    }

    public static void deleteObject(Class<? extends Object> clazz, Object id) {
        if (ModelObject.class != clazz) {
            Object obj = findForDelete(clazz, getNormalizedId(clazz, id));
            if (obj == null) {
                return;
            }
            deleteObject(obj);
        } else {
            LOG.error("Encountered unsupported request to find \"ModelObject\" by id.");
        }
    }

    public static void refreshObject(Object obj) {
        if ((obj instanceof DataView) || (obj instanceof DataViewDef)) {
            if (!(CsiSecurityManager.isAuthorized(((ModelObject)obj).getUuid(), AclControlType.READ))) {
                String myName = ((Resource)obj).getName();
                throw new AuthorizationException("Read access to object " + Format.value(myName) + " denied.");
            }
            if (obj instanceof DataView) {
                try {
                    AclRequest.recordAccess(new RecentAccess(CsiSecurityManager.getUserName(),
                                                            ((Resource)obj).getUuid(), ((Resource)obj).getName()));
                } catch (Exception IGNORE) {}
            }
        }
        if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") refresh()");
      }
        getMetaEntityManager().refresh(obj);
    }

    public static Object getNormalizedId(Class<?> clazz, Object id) {
        if (ModelObject.class.isAssignableFrom(clazz) && (id instanceof String)) {
            return new CsiUUID((String) id);
        }

        return id;
    }

    public static <T> boolean objectExists(Class<T> clazz, Object id) {
        try {
            return (findObject(clazz, getNormalizedId(clazz, id)) != null);
        } catch (AuthorizationException e) {
            return false;
        }
    }

    public static <T> void deleteForSystem(T obj) {

        EntityManager em = getMetaEntityManager();
        if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") remove()");
      }
        em.remove(obj);
    }

    public static <T> void deleteObject(T obj) {
        //if (obj instanceof Resource) {
        // Only treat DataViews and DataViewDefs as controlled resources
        if ((obj instanceof DataView) || (obj instanceof DataViewDef)
                || (obj instanceof InstalledTable) || (obj instanceof Theme) || (obj instanceof Basemap)) {
            if (!CsiSecurityManager.isAuthorized(((Resource) obj).getUuid(), AclControlType.DELETE)) {
                String myName = ((Resource)obj).getName();
                throw new AuthorizationException("Not authorized to delete object " + Format.value(myName) + ".");
            }
        }

        EntityManager em = getMetaEntityManager();
        if (_doDebug) {
         LOG.debug("                                 (" + getThreadID().toString() + ") remove()");
      }
        em.remove(obj);
    }

    public static <T> void deleteObjects(List<T> list) {
        for (T obj : list) {
            //if (obj instanceof Resource) {
            // Only treat DataViews and DataViewDefs as controlled resources
            if ((obj instanceof DataView) || (obj instanceof DataViewDef)) {
                if (!CsiSecurityManager.isAuthorized(((Resource) obj).getUuid(), AclControlType.DELETE)) {
                    String myName = ((Resource)obj).getName();
                    throw new AuthorizationException("Not authorized to delete object " + Format.value(myName) + ".");
                }
            }

            EntityManager em = getMetaEntityManager();
            if (_doDebug) {
               LOG.debug("                                 (" + getThreadID().toString() + ") remove()");
            }
            em.remove(obj);
        }
    }

    public static <T> T findObject(Class<T> clazz, Object id) {
        return findObject(clazz, id, AclControlType.READ);
    }

    /**
     * Detach an object, so that changes to it don't affect DB or cache.
     * Object's that have joined sub objects(like lists), must be walked or eagerly fetched
     * or else those values will be null.
     *
     * @param object
     */
    public static <T extends ModelObject> void detachEntity(T object){
        getMetaEntityManager().detach(object);
    }

    public static <T> T findObject(Class<T> clazz, Object id, AclControlType modeIn, boolean doSecurityIn) {
        return findObject(clazz, id, modeIn, doSecurityIn, true);
    }

    public static <T> T findObject(Class<T> clazz, Object id, AclControlType modeIn, boolean doSecurityIn, boolean recordAccess) {
        T objectFound = null;
        if (ModelObject.class != clazz) {
            Object normalizedId = getNormalizedId(clazz, id);
            //if (Resource.class.isAssignableFrom(clazz)) {
            // Only treat DataViews and DataViewDefs as controlled resources
            if ((DataView.class == clazz) || (DataViewDef.class == clazz) || (InstalledTable.class == clazz)) {
                if (!(CsiSecurityManager.isAuthorized(id.toString(), modeIn, doSecurityIn))) {

                    if (null != findItem(clazz, normalizedId)) {
                        String myName = null;
                        try {
                            myName = AclRequest.getResourceName((String) id);
                        } catch (Exception IGNORE) {}
                        throw new AuthorizationException(modeIn.getLabel() + " access to object " + Format.value(myName) + " denied.");
                    } else {
                        return null;
                    }
                }
            }
            objectFound = findItem(clazz, normalizedId);
            if (objectFound == null) {
                if (_doDebug) {
                  LOG.debug("Can't find object of type " + clazz.getCanonicalName() + " with UUID: " + normalizedId.toString());
               }
            } else if (objectFound instanceof Resource) {
                ((Resource)objectFound).resetTransients();
                if (recordAccess) {
                    if (objectFound instanceof DataView) {

                        try {
                            AclRequest.recordAccess(new RecentAccess(CsiSecurityManager.getUserName(),
                                    ((Resource) objectFound).getUuid(),
                                    ((Resource) objectFound).getName()));
                        } catch (Exception IGNORE) {
                        }
                    }
                }
            }
        } else {
            LOG.error("Encountered unsupported request to find \"ModelObject\" by id.");
        }
        return objectFound;
    }

    public static <T> T findObject(Class<T> clazz, Object id, AclControlType modeIn) {

        return findObject( clazz, id, modeIn, true) ;
    }

    public static <T> T findObjectAvoidingSecurity(Class<T> clazz, Object id, AclControlType modeIn) {

        return findObject( clazz, id, modeIn, false) ;
    }

    public static <T> T findObjectAvoidingSecurity(Class<T> clazz, Object id) {
        T objectFound = null;
        if (ModelObject.class != clazz) {
            Object normalizedId = getNormalizedId(clazz, id);
            objectFound = findItem(clazz, normalizedId);
            if (objectFound == null) {
                if (_doDebug) {
                  LOG.debug("Can't find object of type " + clazz.getCanonicalName() + " with UUID: " + normalizedId.toString());
               }
            } else if (objectFound instanceof Resource) {
                ((Resource)objectFound).resetTransients();
            }
        } else {
            LOG.error("Encountered unsupported request to find \"ModelObject\" by id.");
        }
        return objectFound;
    }

    /**
     * This method does not use security, please confirm acls of uuids before using this method
     * Reason being is, for resources, we don't want to check ACL's twice.
     * @param clazz
     * @param ids
     * @return
     */
    public static <T extends ModelObject> List<T> findObjects(Class<T> clazz, Set<String> ids) {
        List<T> objectsFound = null;
        if (ModelObject.class != clazz) {

            List<Object> normalizedIds = new ArrayList<Object>();
            for(Object id: ids){
                normalizedIds.add(getNormalizedId(clazz, id));
            }

            objectsFound = retrieveItems(clazz, normalizedIds);
            if (objectsFound == null) {
                if (_doDebug) {
                  LOG.debug("Can't find objects of type " + clazz.getCanonicalName());
               }
            } else if (!objectsFound.isEmpty() && (objectsFound.get(0) instanceof Resource)) {
//                for(Object objectFound: objectsFound){
//                    ((Resource)objectFound).resetTransients();
//                }
            }
        } else {
            LOG.error("Encountered unsupported request to find \"ModelObject\" by id.");
        }
        return objectsFound;
    }

    public static <T> T findForDelete(Class<T> clazz, Object id) {
        T objectFound = null;
        if (ModelObject.class != clazz) {
            Object normalizedId = getNormalizedId(clazz, id);
            objectFound = findItem(clazz, normalizedId);
            if (objectFound == null) {
                if (_doDebug) {
                  LOG.debug("Can't find object of type " + clazz.getCanonicalName() + " with UUID: " + normalizedId.toString());
               }
            } else if (objectFound instanceof Resource) {
                ((Resource)objectFound).resetTransients();
            }
        } else {
            LOG.error("Encountered unsupported request to find \"ModelObject\" by id.");
        }
        return objectFound;
    }

    public static <T> T findForSystem(Class<T> clazz, Object id) {
        T objectFound = null;
        if (CsiSecurityManager.isAuthorizedThread()) {
            if (ModelObject.class != clazz) {
                EntityManager em = getMetaEntityManager();
                Object normalizedId = getNormalizedId(clazz, id);
                if (_doDebug) {
                  LOG.debug("                                 (" + getThreadID().toString() + ") find()");
               }
                objectFound = em.find(clazz, normalizedId);
                if (objectFound == null) {
                    if (_doDebug) {
                     LOG.debug("Can't find object of type " + clazz.getCanonicalName() + " with UUID: " + normalizedId.toString());
                  }
                } else if (objectFound instanceof Resource) {
                    ((Resource) objectFound).resetTransients();
                }
            } else {
                LOG.error("Encountered unsupported request to find \"ModelObject\" by id.");
            }
        } else {
            LOG.error("Unauthorized thread requested special access to a resource.");
        }
        return objectFound;
    }

    public static DataSource getCacheDataSource() throws CentrifugeException {
        return JndiUtil.lookupResource(DataSource.class, CsiPersistenceManager.METADB_RESOURCE_URL);
    }
/*
    public static Connection getCacheConnection() throws CentrifugeException {
        if (_doDebug) LOG.debug("+++++++++++ >>>>>>>>>>>>>> getCacheConnection(" + getThreadID().toString() + ")");
        try {
            return new CsiConnection(getCacheDataSource().getConnection());
        } catch (Exception e) {
            LOG.error("Failed to get connection to data cache", e);
            throw new CentrifugeException("Failed to get connection to data cache");
        }
    }

    public static Connection getCacheConnection() throws CentrifugeException {

        DataSource ds = JndiUtil.lookupResource(DataSource.class, CACHE_NAME);
        Connection conn = null;
        try {
            conn = ds.getConnection();

            return new CsiConnection(conn);
//            return conn;
        } catch (Exception e) {
            SqlUtil.quietRollback(conn);
            LOG.error("Failed to get connection to data cache", e);
            throw new CentrifugeException("Failed to get connection to data cache");
        } finally {
        }
    }
*/
   public static Connection getMetaConnection() throws CentrifugeException {
      Connection connection = null;
      DataSource metaSource = JndiUtil.lookupResource(DataSource.class, METADB_RESOURCE_URL);

      try {
         connection = metaSource.getConnection();
      } catch (Exception e) {
         if (CsiSecurityManager.getUserName() != null) {
            LOG.error("Failed to get connection to Meta Data", e);
         }
         throw new CentrifugeException("Failed to get connection to Meta Data");
      }
      return connection;
   }

    public static CsiConnection getCacheConnection() throws CentrifugeException {

        if (null == cacheConnection.get()) {

            DataSource ds = JndiUtil.lookupResource(DataSource.class, CACHE_NAME);
            try {

                Connection myConnection = ds.getConnection();
                CsiConnection mySharedConnection = new CsiConnection(myConnection);

                cacheConnection.set(mySharedConnection);

            } catch (Exception e) {

                SqlUtil.quietRollback(cacheConnection.get());
                if (null != CsiSecurityManager.getUserName()) {

                    LOG.error("Failed to get connection to data cache", e);
                }
                throw new CentrifugeException("Failed to get connection to data cache");

            } finally {
            }
        }
        return cacheConnection.get();
    }

    public static CsiConnection getUserConnection() throws CentrifugeException {

        if (null == userConnection.get()) {

            DataSource ds = JndiUtil.lookupResource(DataSource.class, CACHE_USER_NAME);
            try {

                Connection myConnection = ds.getConnection();
                CsiConnection mySharedConnection = new CsiConnection(myConnection);

                userConnection.set(mySharedConnection);

            } catch (Exception e) {

                SqlUtil.quietRollback(userConnection.get());
                if (null != CsiSecurityManager.getUserName()) {

                    LOG.error("Failed to get restricted connection to data cache", e);
                }
                throw new CentrifugeException("Failed to get restricted connection to data cache");

            } finally {
            }
        }
        return userConnection.get();
    }

    public static CsiConnection getInstalledTableConnection() throws CentrifugeException {

        return getCacheConnection();
    }

    public static void releaseCacheConnection() {

        CsiConnection myCacheConnection = cacheConnection.get();
        if (null != myCacheConnection) {

            try {

                myCacheConnection.release();

            } catch (Exception myException) {

                LOG.error(Format.value(myException));
            }
            cacheConnection.set(null);
        }
    }

    public static void releaseUserConnection() {

        CsiConnection myUserConnection = userConnection.get();
        if (null != myUserConnection) {

            try {

                myUserConnection.release();

            } catch (Exception myException) {

                LOG.error(Format.value(myException));
            }
            userConnection.set(null);
        }
    }

    public static String getCacheDatabase() throws CentrifugeException {

        return ConnectionFactoryManager.cacheDatabase;
    }

    // called at shutdown
    public static void closeEntityManagerFactories() {
        if (metaFactory == null) {
            return;
        }

        try {
            metaFactory.close();
            metaFactory = null;
        } catch (Exception e) {
            LOG.error("Failed to close entity factories.", e);
        }
    }

    // called at startup
    public static void initializeEntityManagerFactories() {
        if (metaFactory == null) {
            metaFactory = Persistence.createEntityManagerFactory(META_UNIT);
        }
    }

    public static <T extends Resource>  T persist(T resourceIn) {

        T myResource = null;
        List<? extends Resource> myResourceList = null;

        try {

            if (resourceIn instanceof DataViewDef) {
                myResourceList = AclRequest.listUserTemplateConflicts(resourceIn.getName());
            } else if (resourceIn instanceof DataView) {
                myResourceList = AclRequest.listUserDvConflicts(resourceIn.getName());
            }

            resourceIn.resetTransients();
            myResource = persistResource(resourceIn, myResourceList);

        } catch (Exception myException) {

            LOG.error("Caught exception persisting resource " + Format.value(resourceIn.getName()) + " as simple object: " + myException.getMessage());
        }
        return myResource;
    }

    public static <T extends Resource> T persistResource(T newResourceIn, List<? extends Resource> oldResourceListIn) throws CentrifugeException {

        if (null != oldResourceListIn) {

            for (Resource myOldResource : oldResourceListIn) {

                deleteObject(myOldResource);
            }
        }
        newResourceIn.resetTransients();
        newResourceIn.resetDates();
        getMetaEntityManager().persist(newResourceIn);
        if (newResourceIn instanceof DataView) {
            try {
                AclRequest.recordAccess(new RecentAccess(CsiSecurityManager.getUserName(),
                                                            newResourceIn.getUuid(), newResourceIn.getName()));
            } catch (Exception IGNORE) {}
        }
        return newResourceIn;
    }


    private static synchronized Integer getThreadID() {
        Integer myValue = threadTag.get();
        if (null == myValue) {
            myValue = incValue++;
            threadTag.set(myValue);
        }
        return myValue;
    }

    private static <T> T stripCredentials(T objectIn) {

        ConnectionDef myConnection = null;

        if (objectIn instanceof DataSourceDef) {

            myConnection = ((DataSourceDef)objectIn).getConnection();

        } else if (objectIn instanceof ConnectionDef) {

            myConnection = (ConnectionDef)objectIn;
        }

        if (null != myConnection) {

            Map<String, String> myLocalMap = myConnection.getProperties().getPropertiesMap();

            if ((myLocalMap.containsKey(JdbcDriverParameterKey.RUNTIME_USERNAME.getKey()))
                    || (myLocalMap.containsKey(JdbcDriverParameterKey.RUNTIME_PASSWORD.getKey()))) {

                myLocalMap.remove(JdbcDriverParameterKey.RUNTIME_USERNAME.getKey());
                myLocalMap.remove(JdbcDriverParameterKey.RUNTIME_PASSWORD.getKey());
                myConnection.getProperties().refreshProperties();
            }
        } else {

            return stripAllCredentials(objectIn);
        }
        return objectIn;
    }

    private static <T> T stripAllCredentials(T objectIn) {

        List<DataSourceDef> myList = null;

        if (objectIn instanceof DataView) {

            myList = ((DataView)objectIn).getMeta().getDataSources();

        } else if (objectIn instanceof DataViewDef) {

            myList = ((DataViewDef)objectIn).getDataSources();
        }
        if (null != myList) {

            for (DataSourceDef mySource : myList) {

                Map<String, String> myLocalMap = mySource.getConnection().getProperties().getPropertiesMap();

                if ((myLocalMap.containsKey(JdbcDriverParameterKey.RUNTIME_USERNAME.getKey()))
                        || (myLocalMap.containsKey(JdbcDriverParameterKey.RUNTIME_PASSWORD.getKey()))) {

                    myLocalMap.remove(JdbcDriverParameterKey.RUNTIME_USERNAME.getKey());
                    myLocalMap.remove(JdbcDriverParameterKey.RUNTIME_PASSWORD.getKey());
                    mySource.getConnection().getProperties().refreshProperties();
                }
            }
        }
        return objectIn;
    }

    private enum PrebuiltAction {

        CHECK, ADD, REMOVE
    }
	public static void addPrebuiltDataView(Object idIn, DataView dataViewIn) {

        if (null != dataViewIn) {

            managePrebuiltDataView(PrebuiltAction.ADD, idIn, dataViewIn);
        }
	}

	public static DataView getPrebuiltDataView(Object idIn) {

        return managePrebuiltDataView(PrebuiltAction.REMOVE, idIn, null);
    }

    public static void discardPrebuiltDataView(Object idIn) {

        while (null != getPrebuiltDataView(idIn)) {

      }
    }

    public static boolean isPrebuilt(Object idIn) {

        return (null != managePrebuiltDataView(PrebuiltAction.CHECK, idIn, null));
    }

    @Synchronized
    private static DataView managePrebuiltDataView(PrebuiltAction requestIn, Object idIn, DataView dataViewIn) {

        DataView myDataView = null;

        if (null != idIn) {

            if (requestIn == PrebuiltAction.ADD) {

                List<DataView> dataViews = cachedDataViews.get(idIn);

                if (dataViews == null) {
                    dataViews = new ArrayList<DataView>();
                }
                dataViews.add(dataViewIn);
                cachedDataViews.put(idIn.toString(), dataViews);
                cachedDataViewUuids.add(dataViewIn.getUuid());

            } else {

                List<DataView> dataViews = cachedDataViews.get(idIn);
                if ((dataViews != null) && !dataViews.isEmpty()) {
                    if (requestIn == PrebuiltAction.REMOVE) {

                        myDataView = dataViews.remove(0);
                        cachedDataViewUuids.remove(myDataView.getUuid());
                        if (dataViews.isEmpty()) {
                            cachedDataViews.remove(idIn);
                        }
                    } else {

                        myDataView = dataViews.get(0);
                    }
                }
            }
        }
        return myDataView;
    }

    public static void clear() {
        EntityManager em = getMetaEntityManager();
        if (em != null) {
            try {
                if (em.getTransaction().isActive()) {
                    if (_doDebug) {
                     LOG.debug("                                 (" + getThreadID().toString() + ") rollback()");
                  }
                    em.getTransaction().rollback();
                }
            } finally {
                if (_doDebug) {
                  LOG.debug("                                 (" + getThreadID().toString() + ") clear()");
               }
                em.clear();
            }
        }
    }

    private static <T> T findItem(Class<T> clazzIn, Object idIn) {

        T myItem = null;

        if (null != idIn) {

            EntityManager myManager = getMetaEntityManager();

            if (null != myManager) {

                if (_doDebug) {
                  LOG.debug("                                 (" + getThreadID().toString() + ") find()");
               }
                myItem = myManager.find(clazzIn, getNormalizedId(clazzIn, idIn));

                if ((null != myItem) && (myItem instanceof Resource)) {

                    if (AclResourceType.DISCARDED == ((Resource) myItem).getResourceType()) {

                        myItem = null;
                    }
                }
            }
        }
        return myItem;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> findItems(Class<T> clazzIn, List<Object> idsIn) {

        List<T> myItems = null;
        EntityManager myManager = getMetaEntityManager();

        if (null != myManager) {

            if (_doDebug) {
               LOG.debug("                                 (" + getThreadID().toString() + ") find()");
            }
            Session session = myManager.unwrap(Session.class);
            Criteria criteria = session.createCriteria(clazzIn).add(Restrictions.in(PRIMARY_KEY_NAME, idsIn));
            //No writing to cache for this, just getting and invalidating
            criteria.setCacheMode(CacheMode.GET);
//            criteria.setCacheable(false);
//            criteria.setFetchSize(50);
            myItems = criteria.list();

            if ((null != myItems) && !myItems.isEmpty() && (myItems.get(0) instanceof Resource)) {

                for(T item : myItems){
                    if (AclResourceType.DISCARDED == ((Resource)item).getResourceType()) {
                        return null;
                    }
                }
            }
        }
        return myItems;
    }
//    EntityManager em = CsiPersistenceManager.getMetaEntityManager();
//    CriteriaBuilder cb = em.getCriteriaBuilder();
//    CriteriaQuery<clazzIn> q = cb.createQuery(clazzIn);
//    Root<clazzIn> c = q.from(PersistedPattern.class);
//    ParameterExpression<String> p = cb.parameter(String.class);
//    q.select(c).where(clazzIn.equal(c.get("owner"), p));
//    TypedQuery<clazzIn> query = em.createQuery(q);
//    query.setParameter(p, owner);
//    List<clazzIn> results = query.getResultList();

    public static <T extends ModelObject> List<T> retrieveItems(Class<T> clazzIn, List<Object> idsIn) {
        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> q = cb.createQuery(clazzIn);
        Root<T> root = q.from(clazzIn);

        ParameterExpression<List> p = cb.parameter(List.class);

        q.select(root).where(root.get(PRIMARY_KEY_NAME).in(p));

        TypedQuery<T> query = em.createQuery(q);
        query.setParameter(p, idsIn);

        List<T> results = query.getResultList();
        return results;
    }

    public static List<String> findDistinctColumnValues(String tableName, String columnName){
        EntityManager myManager = getMetaEntityManager();

        if (null != myManager) {
            if (_doDebug) {
               LOG.debug("                                 (" + getThreadID().toString() + ") find()");
            }
            Session session = myManager.unwrap(Session.class);
            String hql = "select distinct i." + columnName + " from " + tableName + " i";
            Query query = session.createQuery(hql);
            query.setCacheable(false);
            query.setCacheMode(CacheMode.GET);
            return query.list();
        }

        return null;
    }

    public static boolean hasIconManagementAccess(){
        //Since this is defined via config, we only need to parse once
        if(iconManagementRoles == null){
            String accessUsers = Configuration.getInstance().getApplicationConfig().getIconManagementAccess();
            String delimiter = Configuration.getInstance().getApplicationConfig().getIconManagementAccessDelimiter();

            if((delimiter != null) && !delimiter.isEmpty()){
                iconManagementRoles = accessUsers.split(delimiter);
            } else {
                iconManagementRoles = new String[1];
                iconManagementRoles[0] = accessUsers;
            }
        }

        return CsiSecurityManager.hasAnyRole(iconManagementRoles);

    }
}
