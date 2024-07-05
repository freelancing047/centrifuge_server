package csi.startup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.common.dto.system.AbstractPurgeRequest;
import csi.server.common.dto.system.DataViewPurgeRequest;
import csi.server.common.dto.system.ReaperControl;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.SqlUtil;

import lombok.Synchronized;

/**
 * Created by centrifuge on 7/26/2016.
 */

public class CleanUpThread extends Thread {
    private static final Logger LOG = LogManager.getLogger(CleanUpThread.class);

    private static boolean _doDebug = LOG.isDebugEnabled();

    private static final long MILLISECONDS_IN_A_DAY = TimeUnit.HOURS.toMillis(24L);

    private static List<AbstractPurgeRequest> _requestList = new ArrayList<AbstractPurgeRequest>();
    private static CleanUpThread _thread = null;
    private static List<ReaperControl> _reaperControls = new ArrayList<ReaperControl>(0);
    private static long _oneSecond = 1000;
    private static boolean _shutdown = false;

    private static List<ValuePair<Class<? extends ModelObject>, String>> _modelObjectList
            = new ArrayList<ValuePair<Class<? extends ModelObject>, String>>();
    private static ThreadLocal<List<ValuePair<Class<? extends ModelObject>, String>>> _localObjectList
            = new ThreadLocal<List<ValuePair<Class<? extends ModelObject>, String>>>();
    private static List<Set<String>> _orphanSet;
    private static int _activeId = 0;
    private static boolean _purgeSamples = Configuration.getInstance().getApplicationConfig().isPurgeSamples();
    private static boolean _purgeOrphans = Configuration.getInstance().getApplicationConfig().isPurgeOrphanDataViews();
    private static boolean _purgeOldDataViews = Configuration.getInstance().getApplicationConfig().isPurgeOldDataViews();
    private static int _lingeringLimit = Configuration.getInstance().getApplicationConfig().getDataViewPurgeAge();
    private static long _reapingWait;
    private static long _lastReapingOccurence;

    public static void launch() throws CentrifugeException {

        if (null == _thread)  {

            _thread = new CleanUpThread();
            CsiSecurityManager.identifyAuthorizedThread(_thread.getId());
            _thread.start();
        }
    }

    public static void terminate() {

        try {
            getSetShutdown(Boolean.TRUE);
            _thread.interrupt();
            _thread.join();
            _thread = null;
            saveControlInfo();
        } catch (Exception ignoreException) {

            if (_doDebug) {
               LOG.debug("Caught exception terminating.", ignoreException);
            }
        }
    }

    public static <T extends ModelObject> void scheduleDelete(T modelObjectIn) {

        if (null != modelObjectIn) {

            try {

                scheduleDelete(modelObjectIn.getClass(), modelObjectIn.getUuid());

            } catch (Exception ignoreException) {

                if (_doDebug) {
                  LOG.debug("Caught exception scheduling delete for "
                           + Format.value(modelObjectIn), ignoreException);
               }
            }
        }
    }

    public static void scheduleDelete(Class<? extends ModelObject> objectClassIn, String objectIdIn) {

        if ((null != objectClassIn) && (null != objectIdIn) && (0 < objectIdIn.length())) {

            List<ValuePair<Class<? extends ModelObject>, String>> myList = ensureLocalList();

            if (null != myList) {

                try {

                    myList.add(new ValuePair<Class<? extends ModelObject>, String>(objectClassIn, objectIdIn));

                } catch (Exception ignoreException) {

                    if (_doDebug) {
                     LOG.debug("Caught exception scheduling delete for "
                                               + Format.value(objectClassIn) + " object , id = "
                                               + Format.value(objectIdIn), ignoreException);
                  }
                }
            }
        }
    }

    public static void finalizeDeletes() {

        List<ValuePair<Class<? extends ModelObject>, String>> myList = ensureLocalList();

        if ((myList != null) && !myList.isEmpty()) {

            try {

                grabOrUpdateList(myList);

            } catch (Exception ignoreException) {

                if (_doDebug) {
                  LOG.debug("Caught exception finalizing deletes.", ignoreException);
               }
            }
        }
    }

    public static <T extends Resource> boolean scheduleDelete(T resourceIn) {

        boolean mySuccess = false;

        try {

            AbstractPurgeRequest myRequest = AbstractPurgeRequest.createRequest(resourceIn);

            if ((null != myRequest) && CsiPersistenceManager.markForDelete(resourceIn)) {

                addRemoveRequest(myRequest);

            } else {

                // Delete right now if unable to schedule removal.
                CsiPersistenceManager.deleteObject(resourceIn);
            }
            mySuccess = true;

        } catch (Exception myException) {

            LOG.error("Caught exception marking Resource "
                    + Format.value(resourceIn.getName()) + " for deletion: " + Format.value(myException));
        }
        return mySuccess;
    }

    @Synchronized
    public static AbstractPurgeRequest addRemoveRequest(AbstractPurgeRequest requestIn) {

        AbstractPurgeRequest myRequest = null;

        if (null != requestIn) {

            _requestList.add(requestIn);

        } else if (!_requestList.isEmpty()) {

            myRequest = _requestList.remove(0);

        }
        return myRequest;
    }

    public CleanUpThread() throws CentrifugeException {

        if ((AclResourceType.DATAVIEW.ordinal() != 0) || (AclResourceType.TEMPLATE.ordinal() != 1)) {

            LOG.fatal("Resource type enum not as expected!");
            throw new CentrifugeException("Resource type enum not as expected!");
        }
        loadControlInfo();
    }

    @Override
   public void run() {

        EntityManager myEntityManager = CsiPersistenceManager.getMetaEntityManager();

        _orphanSet = new ArrayList<Set<String>>();
        _orphanSet.add(new TreeSet<String>());
        _orphanSet.add(new TreeSet<String>());

        _purgeSamples = Configuration.getInstance().getApplicationConfig().isPurgeSamples();
        _purgeOrphans = Configuration.getInstance().getApplicationConfig().isPurgeOrphanDataViews();
        _purgeOldDataViews = Configuration.getInstance().getApplicationConfig().isPurgeOldDataViews();
        _lingeringLimit = Configuration.getInstance().getApplicationConfig().getDataViewPurgeAge();
        _reapingWait = MILLISECONDS_IN_A_DAY / Configuration.getInstance().getApplicationConfig().getDailyReaperCount();

        while (null == myEntityManager) {

            oneSecondSleep();
            myEntityManager = CsiPersistenceManager.getMetaEntityManager();
        }
        loadOutstandingRequests();
        reapTargets();
        CsiPersistenceManager.close();

        while (!getSetShutdown(null)) {

            processDeletes();
            if (now() > _lastReapingOccurence) {

                reapTargets();
            }
            oneSecondSleep();
        }
    }

    private static void loadOutstandingRequests() {

        try {

            List<String> myUuidList = AclRequest.retrieveDataViewsMarkedForDelete();

            if ((myUuidList != null) && !myUuidList.isEmpty()) {

                scheduleDeletes(AclResourceType.DATAVIEW, myUuidList);
            }

        } catch (Exception myException) {

            LOG.error("Caught exception while initializing rescheduling oustanding DataView delete requests:\n"
                    + Format.value(myException));
        }
        try {

            List<String> myUuidList = AclRequest.retrieveTemplatesMarkedForDelete();

            if ((myUuidList != null) && !myUuidList.isEmpty()) {

                scheduleDeletes(AclResourceType.TEMPLATE, myUuidList);
            }

        } catch (Exception myException) {

            LOG.error("Caught exception while initializing rescheduling oustanding Template delete requests:\n"
                        + Format.value(myException));
        }
    }

    public static void reapResources(AclResourceType resourceTypeIn, String[] ownersIn, Date[] filterIn) {

        if (null != filterIn) {

            List<String> myUuidList = ((null != ownersIn) && (0 < ownersIn.length))
                    ? AclRequest.reapResources(resourceTypeIn, ownersIn, filterIn)
                    : AclRequest.reapResources(resourceTypeIn, filterIn);

            scheduleDeletes(resourceTypeIn, myUuidList);
        }
    }

    @Synchronized
    private static boolean getSetShutdown(Boolean shutdownIn) {

        if (null != shutdownIn) {

            _shutdown = shutdownIn;
        }
        return _shutdown;
    }

    @Synchronized
    private static List<ReaperControl> getSetReaperControls(List<ReaperControl> reaperControlsIn) {

        if (null != reaperControlsIn) {

            _reaperControls = reaperControlsIn;
            saveControlInfo();
            return null;

        } else {

            return _reaperControls;
        }
    }

    @Synchronized
    private static List<ValuePair<Class<? extends ModelObject>, String>>
    grabOrUpdateList(List<ValuePair<Class<? extends ModelObject>, String>> newItemsIn) {

        List<ValuePair<Class<? extends ModelObject>, String>> myList = null;

        if (null != newItemsIn) {

            _modelObjectList.addAll(newItemsIn);
            newItemsIn.clear();

        } else {

            myList = new ArrayList<ValuePair<Class<? extends ModelObject>, String>>();

            for (ValuePair<Class<? extends ModelObject>, String> myPair : _modelObjectList) {

                if (null != myPair) {

                    myList.add(myPair);
                }
            }
            _modelObjectList.clear();
        }
        return myList;
    }

    private static void loadControlInfo() {

    }

    private static void saveControlInfo() {

    }

    private void processDeletes() {

        List<ValuePair<Class<? extends ModelObject>, String>> myObjectList = grabOrUpdateList(null);
        List<AbstractPurgeRequest> myRequestList = new ArrayList<AbstractPurgeRequest>();

        while (!getSetShutdown(null)) {

            AbstractPurgeRequest myRequest = addRemoveRequest(null);
            if (null != myRequest) {

                boolean myWaitingOnCache = myRequest instanceof DataViewPurgeRequest;
                EntityManager myEntityManager = CsiPersistenceManager.getMetaEntityManager();
                String myMetaError = "Failed to obtain meta handle for background delete process.\n";
                String myCacheError = "Caught exception while obtaining cache handle for background delete process.\n";

                while (myWaitingOnCache || (null == myEntityManager)) {

                    myWaitingOnCache = myRequest instanceof DataViewPurgeRequest;
                    if (null != myEntityManager) {

                        if (myWaitingOnCache) {

                            try {

                                CsiPersistenceManager.getCacheConnection();
                                myWaitingOnCache = false;

                            } catch (Exception myException) {

                                CsiPersistenceManager.close();
                                myEntityManager = null;
                                if (null != myCacheError) {

                                    LOG.error(myCacheError + Format.value(myException));
                                    myCacheError = null;
                                }
                                oneSecondSleep();
                            }
                        }

                    } else {

                        if (null != myMetaError) {

                            LOG.error(myMetaError);
                            myMetaError = null;
                        }
                        oneSecondSleep();
                        myEntityManager = CsiPersistenceManager.getMetaEntityManager();
                    }
                }
                if (!myRequest.execute()) {

                    myRequestList.add(myRequest);
                }
                CsiPersistenceManager.releaseCacheConnection();
                CsiPersistenceManager.close();

            } else {

                finalizeDeletes();
                deleteModelObjects(myObjectList);
                break;
            }
        }
        if (!myRequestList.isEmpty()) {

            for (AbstractPurgeRequest myRequest : myRequestList) {

                addRemoveRequest(myRequest);
            }
        }
    }

    private void reapTargets() {

        _lastReapingOccurence = now();

        if (!getSetShutdown(null)) {

            List<ReaperControl> myReaperControls = getSetReaperControls(null);

            if (_purgeSamples) {

                List<String> myUuidList = AclRequest.findAdministratorsSampleTemplatesAvoidingSecurity();

                if ((myUuidList != null) && !myUuidList.isEmpty()) {

                    for (String mySampleId : myUuidList) {

                        AclRequest.removeAcl(mySampleId);
                    }
                    scheduleDeletes(AclResourceType.SAMPLE, myUuidList);
                }
            }
            if (_purgeOrphans) {

                List<String> myUuidList = AclRequest.findOrphanDataViewsAvoidingSecurity();

                if ((myUuidList != null) && !myUuidList.isEmpty()) {

                    processOrphans(myUuidList);
                }
            }
            if (_purgeOldDataViews) {

                List<String> myUuidList = AclRequest.findOldDataViewsAvoidingSecurity(_lingeringLimit);

                if ((myUuidList != null) && !myUuidList.isEmpty()) {

                    scheduleDeletes(AclResourceType.DATAVIEW, myUuidList);
                }
            }
            if ((myReaperControls != null) && !myReaperControls.isEmpty()) {

                for (ReaperControl myReaderControl : myReaperControls) {

                    reapResources(myReaderControl.getResourceType(),
                                    myReaderControl.getOwnersArray(), myReaderControl.getFilter());
                }
            }
        }
    }

   private static void scheduleDeletes(AclResourceType resourceTypeIn, List<String> uuidListIn) {
      if (uuidListIn != null) {
         for (String myUuid : uuidListIn) {
            Resource resource = retrieveResource(resourceTypeIn, myUuid);

            if (resource != null) {
               CleanUpThread.scheduleDelete(resource);
            }
         }
      }
   }

   private static void processOrphans(List<String> uuidListIn) {
      Set<String> myActiveSet;
      Set<String> myLoadingSet;

      if ((uuidListIn != null) && !uuidListIn.isEmpty()) {
         myActiveSet = _orphanSet.get(_activeId);
         _activeId = (_activeId + 1) % 2;
         myLoadingSet = _orphanSet.get(_activeId);

         for (String myUuid : uuidListIn) {
            if (!CsiPersistenceManager.isPrebuilt(myUuid)) {
               if (myActiveSet.contains(myUuid)) {
                  Resource resource = retrieveResource(AclResourceType.DATAVIEW, myUuid);

                  if (resource != null) {
                     CleanUpThread.scheduleDelete(resource);
                  }
               } else {
                  myLoadingSet.add(myUuid);
               }
            }
         }
         myActiveSet.clear();
      }
   }

    private static Resource retrieveResource(AclResourceType typeIn, String uuidIn) {

        switch (typeIn) {

            case DATAVIEW:

                return CsiPersistenceManager.findForSystem(DataView.class, uuidIn);

            case TEMPLATE:
            case SAMPLE:

                return CsiPersistenceManager.findForSystem(DataViewDef.class, uuidIn);

            default:

                return null;
        }
    }

    private void oneSecondSleep() {

        try {

            sleep(_oneSecond);

        } catch (InterruptedException ignore) {
        } catch (Exception ignoreException) {

            if (_doDebug) {
               LOG.debug("Caught exception while sleeping", ignoreException);
            }
        }
    }

    private static void deleteModelObjects(List<ValuePair<Class<? extends ModelObject>,String>> listIn) {
       if ((listIn != null) && !listIn.isEmpty()) {
          try (Connection connection = CsiPersistenceManager.getMetaConnection()) {
             try {
                if (connection != null) {
                   Map<Class<? extends ModelObject>,Set<String>> myDiscardMap = groupDiscards(listIn);

                   for (Map.Entry<Class<? extends ModelObject>,Set<String>> myClassDiscards : myDiscardMap.entrySet()) {
                      Class<? extends ModelObject> myClass = myClassDiscards.getKey();
                      Set<String> myDiscards = myClassDiscards.getValue();

                      if ((myDiscards != null) && !myDiscards.isEmpty()) {
                         if (FieldDef.class.equals(myClass)) {
                            deleteFieldDefs(connection, myDiscards);
                         }
                      }
                   }
                } else {
                   grabOrUpdateList(listIn);
                }
             } catch (Exception exception) {
                SqlUtil.quietRollback(connection);
             }
          } catch (Exception exception) {
          }
       }
    }

    private static void deleteFieldDefs(Connection connectionIn, Set<String> discardsIn) {

        if ((discardsIn != null) && !discardsIn.isEmpty()) {

            Set<String> myUnusedItems = filterDiscards(connectionIn, discardsIn, "attributedef", "fielddef_uuid");
            if (!myUnusedItems.isEmpty()) {

                myUnusedItems = filterDiscards(connectionIn, discardsIn, "attributedef", "tooltiplinkfeilddef_uuid");
            }
            if (!myUnusedItems.isEmpty()) {

                myUnusedItems = filterDiscards(connectionIn, discardsIn, "directiondef", "fielddef_uuid");
            }
            if (!myUnusedItems.isEmpty()) {

                deleteFilteredModelObjects(FieldDef.class, myUnusedItems);
            }
        }
    }

   private static void deleteFilteredModelObjects(Class<? extends ModelObject> classIn, Set<String> setIn) {
      if ((setIn != null) && !setIn.isEmpty()) {
         EntityManager myEntityManager = CsiPersistenceManager.getMetaEntityManager();

         for (String myId : setIn) {
            if (myId != null) {
               try {
                  if (myEntityManager == null) {
                     myEntityManager = CsiPersistenceManager.getMetaEntityManager();
                  }
                  CsiPersistenceManager.begin();
                  CsiPersistenceManager.deleteObject(classIn, myId);
                  CsiPersistenceManager.commit();
               } catch (Exception exception) {
                  CsiPersistenceManager.rollback();
                  CsiPersistenceManager.close();
                  myEntityManager = null;
                  LOG.error("Clean-up caught exception removing {}, id = {}",
                            () -> Format.value(classIn), () ->Format.value(myId));
               }
            }
         }
      }
   }

   private static List<ValuePair<Class<? extends ModelObject>, String>> ensureLocalList() {

        List<ValuePair<Class<? extends ModelObject>, String>> myList = null;

        try {

            myList = _localObjectList.get();

            if (null == myList) {

                myList = new ArrayList<ValuePair<Class<? extends ModelObject>, String>>();
                _localObjectList.set(myList);
            }

        } catch (Exception myException) {

            LOG.error("Clean-up: Unable to access local delete list.", myException);
        }
        return myList;
    }

    private static Map<Class<? extends ModelObject>, Set<String>>
    groupDiscards(List<ValuePair<Class<? extends ModelObject>, String>> listIn) {

        Map<Class<? extends ModelObject>, Set<String>> myClassMap = new HashMap<>();

        for (ValuePair<Class<? extends ModelObject>, String> myPair : listIn) {

            Class<? extends ModelObject> myClass = myPair.getValue1();
            String myId = myPair.getValue2();
            Set<String> mySet = myClassMap.get(myClass);

            if (null == mySet) {

                mySet = new HashSet<String>();
                myClassMap.put(myClass, mySet);
            }
            mySet.add(myId);
        }
        return myClassMap;
    }

   private static Set<String> filterDiscards(Connection connectionIn,
                                             Set<String> setIn, String tableIn, String fieldIn) {
      if ((setIn != null) && !setIn.isEmpty()) {
         String myQueryString = buildFilterRequest(setIn, tableIn, fieldIn);

         try (PreparedStatement myStatement = connectionIn.prepareStatement(myQueryString);
              ResultSet myResultSet = myStatement.executeQuery()) {
            while (myResultSet.next()) {
               setIn.remove(myResultSet.getString(1));
            }
         } catch (Exception ignore) {
            //setIn.clear();
         }
      }
      return setIn;
   }

   private static String buildFilterRequest(Set<String> setIn, String tableIn, String fieldIn) {
      return new StringBuilder("SELECT \"")
                       .append(fieldIn)
                       .append("\" FROM \"")
                       .append(tableIn)
                       .append("\" WHERE \"")
                       .append(fieldIn)
                       .append("\" IN (")
                       .append(setIn.stream().map(SqlUtil::singleQuoteWithEscape).collect(Collectors.joining(",")))
                       .append(")")
                       .toString();
   }

   private static long now() {
      return System.currentTimeMillis() / _reapingWait;
   }
}
