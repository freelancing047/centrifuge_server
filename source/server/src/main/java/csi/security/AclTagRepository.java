package csi.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import csi.security.queries.AclRequest;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.interfaces.KeyRetrieval;
import csi.server.common.model.Resource;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.dao.CsiPersistenceManager;

import lombok.Synchronized;

/**
 * Created by centrifuge on 10/1/2016.
 */
public class AclTagRepository {
    private static boolean _initialized;
    private static Map<String, CapcoSecurityTag> _capcoTags;
    private static Map<String, GenericSecurityTag> _genericTags;
    private static Map<String, SourceAclEntry> _sourceControl;
    private static int _sourceControlMask;

    public AclTagRepository() {
    }

    public static void establishResourceSecurity(Resource resourceIn, String uuidIn, Set<String> capcoStringsIn,
                                                    List<Set<String>> distributionTagsIn, Set<String> sourceKeysIn)
            throws CsiSecurityException {

        if (!_initialized) {

            initializeStaticData();
        }
        ACL myAcl = AclRequest.lockAcl(resourceIn, uuidIn);

        if (null != myAcl) {

            try {

                myAcl.getLinkupEntries().clear();
                replaceList(myAcl.getCapcoTags(), _capcoTags, capcoStringsIn,
                            /*template tag -- never persisted*/new CapcoSecurityTag("", true));
                forceAclUpdate(myAcl);
                replaceList(myAcl.getGenericTags(), _genericTags,
                        replaceDistribution(myAcl.getDistributionTags(), distributionTagsIn, myAcl),
                        /*template tag -- never persisted*/new GenericSecurityTag("", true));
                forceAclUpdate(myAcl);
                replaceList(myAcl.getSourceEntries(), _sourceControl, sourceKeysIn, null);
                AclRequest.unlockAcl(myAcl);

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                CsiPersistenceManager.begin();

                throw new CsiSecurityException("Caught exception while establishing Resource security:\n" + Format.value(myException));
            }

        } else {

            throw new CsiSecurityException("Unable to obtain ACL lock!");
        }
    }

    public static void augmentResourceSecurity(Resource resourceIn, String uuidIn, Set<String> capcoStringsIn,
                                                  List<Set<String>> distributionTagsIn, Set<String> sourceKeysIn)
            throws CsiSecurityException {

        if (!_initialized) {

            initializeStaticData();
        }
        ACL myAcl = AclRequest.lockAcl(resourceIn, uuidIn);

        if (null != myAcl) {

            try {

                augmentList(myAcl.getCapcoTags(), _capcoTags, capcoStringsIn,
                            /*template tag -- never persisted*/new CapcoSecurityTag("", true));
                forceAclUpdate(myAcl);
                replaceList(myAcl.getGenericTags(), _genericTags,
                        replaceDistribution(myAcl.getDistributionTags(), distributionTagsIn, myAcl),
                        /*template tag -- never persisted*/new GenericSecurityTag("", true));
                forceAclUpdate(myAcl);
                augmentList(myAcl.getLinkupEntries(), createMap(_sourceControl, myAcl.getSourceEntries()), sourceKeysIn, null);
                AclRequest.unlockAcl(myAcl);

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                CsiPersistenceManager.begin();

                throw new CsiSecurityException("Caught exception while establishing Resource security:\n" + Format.value(myException));
            }

        } else {

            throw new CsiSecurityException("Unable to obtain ACL lock!");
        }
    }

   @Synchronized
   public static void initializeStaticData() throws CsiSecurityException {
      if (!_initialized) {
         boolean activeFlag = CsiPersistenceManager.isTransactionActive();

         if (!activeFlag) {
            CsiPersistenceManager.begin();
         }
         loadTableData();

         if (updateConfiguredSecurity() && !_sourceControl.isEmpty()) {
            CsiPersistenceManager.commit();

            if (activeFlag) {
               CsiPersistenceManager.begin();
            }
            for (SourceAclEntry myEntry : _sourceControl.values()) {
               _sourceControlMask |= myEntry.getTagMask();
            }
            _initialized = true;
         }
      }
   }

    public static boolean hasConfiguredSecurity() throws CsiSecurityException {

        if (!_initialized) {

            initializeStaticData();
        }
        return 0 != _sourceControlMask;
    }

    public static int getConfiguredSecurityMask() throws CsiSecurityException {

        if (!_initialized) {

            initializeStaticData();
        }
        return _sourceControlMask;
    }

    private static boolean updateConfiguredSecurity() throws CsiSecurityException {

        boolean mySuccess = false;
        Set<String> myFactoryKeySet = ConnectionFactoryManager.getInstance().getTypeNames();

        if ((null != myFactoryKeySet) && !myFactoryKeySet.isEmpty()) {

            mySuccess = true;
            for (String myTag : myFactoryKeySet) {

                try {

                    String myKey = myTag.toLowerCase();
                    ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getFactoryForType(myKey);
                    SourceAclEntry myEntry = _sourceControl.get(myKey);

                    if (null != myEntry) {

                        myEntry.setDataAccessRole(myFactory.getDriverAccessRole());
                        myEntry.setSourceEditRole(myFactory.getSourceEditRole());
                        myEntry.setConnectionEditRole(myFactory.getConnectionEditRole());
                        CsiPersistenceManager.merge(myEntry);

                    } else {

                        myEntry = new SourceAclEntry(myKey, myFactory.getDriverAccessRole(),
                                myFactory.getSourceEditRole(), myFactory.getConnectionEditRole());

                        CsiPersistenceManager.persist(myEntry);
                        _sourceControl.put(myKey, myEntry);
                    }

                } catch (Exception myException) {

                    throw new CsiSecurityException("Caught an exception while updating Configured Security:\n" + Format.value(myException));
                }
            }
        }
        return mySuccess;
    }

    private static void loadTableData() throws CsiSecurityException {

        if (!_initialized) {

            try {

            List<CapcoSecurityTag> myCapcoTagList = AclRequest.getKnownCapcoTags();
            List<GenericSecurityTag> myGenericTagList = AclRequest.getKnownGenericTags();
            List<SourceAclEntry> mySourceControlList = AclRequest.getKnownSourceControls();

            _capcoTags = createMap(myCapcoTagList);
            _genericTags = createMap(myGenericTagList);
            _sourceControl = createMap(mySourceControlList);

            } catch (Exception myException) {

                throw new CsiSecurityException("Caught an exception while loading security tables:\n" + Format.value(myException));
            }
        }
    }

    private static <T extends KeyRetrieval> Map<String, T> createMap(List<T> listIn) {

        Map<String, T> myMap = new TreeMap<String, T>();

        if (null != listIn) {

            for (T myItem : listIn) {

                myMap.put(myItem.getKey(), myItem);
            }
        }
        return myMap;
    }

    private static Set<String> replaceDistribution(List<DistributionTag> listIn, List<Set<String>> distributionTagsIn, ACL aclIn) {

        Set<String> myList = new TreeSet<String>();

        forceAclUpdate(aclIn);
        listIn.clear();
        forceAclUpdate(aclIn);
        if ((null != distributionTagsIn) && !distributionTagsIn.isEmpty()) {

            for (Set<String> mySet : distributionTagsIn) {

                if ((null != mySet) && !mySet.isEmpty()) {

                    if (1 == mySet.size()) {

                        myList.add(mySet.iterator().next());

                    } else {

                        listIn.add(new DistributionTag(replaceList(new ArrayList<GenericSecurityTag>(), _genericTags, mySet,
                                                        /*template tag -- never persisted*/new GenericSecurityTag("", true))));
                    }
                }
            }
        }
        forceAclUpdate(aclIn);
        return myList;
    }

    private static <T extends KeyRetrieval> Map<String, T> createMap(Map<String, T> baseMapIn, List<T> listIn) {

        if ((null != listIn) && !listIn.isEmpty()) {

            Map<String, T> myMap = new TreeMap<String, T>();

            for (T myItem : listIn) {

                myMap.put(myItem.getKey(), myItem);
            }
            for (T myItem : baseMapIn.values()) {

                String myKey = myItem.getKey();

                if (!myMap.containsKey(myKey)) {

                    myMap.put(myKey, myItem);
                }
            }
            return myMap;
        }
        return baseMapIn;
    }

    private static <T extends KeyRetrieval> void augmentList(Collection<T> listIn, Map<String, T> baseMapIn,
                                                             Set<String> keySetIn, T sampleIn) {

        if ((null != keySetIn) && !keySetIn.isEmpty()) {

            Set<String> myCurrentSet = new TreeSet<String>();

            for (T myItem : listIn) {

                myCurrentSet.add(myItem.getKey());
            }
            for (String myItem : keySetIn) {

                myCurrentSet.add(myItem.toLowerCase());
            }
            replaceList(listIn, baseMapIn, keySetIn, sampleIn);
        }
    }

    private static <T extends KeyRetrieval> Collection<T> replaceList(Collection<T> listIn, Map<String, T> baseMapIn,
                                                             Set<String> keySetIn, T sampleIn) {
        listIn.clear();

        if ((null != keySetIn) && !keySetIn.isEmpty()) {

            for (String myKey : keySetIn) {

                T myTag = baseMapIn.get(myKey);

                if ((null == myTag)&& (null != sampleIn)) {

                    if (sampleIn instanceof CapcoSecurityTag) {

                        myTag = getOrAdd((T)new CapcoSecurityTag(myKey, ((CapcoSecurityTag)sampleIn).getEnforce()), baseMapIn);

                    } else if (sampleIn instanceof GenericSecurityTag) {

                        myTag = getOrAdd((T)new GenericSecurityTag(myKey, ((GenericSecurityTag)sampleIn).getEnforce()), baseMapIn);
                    }
                }
                if (null != myTag) {

                    listIn.add(myTag);
                }
            }
        }
        return listIn;
    }

    @Synchronized
    private static <T extends KeyRetrieval> T getOrAdd(T tagIn, Map<String, T> baseMapIn) {

        String myKey = tagIn.getKey();
        T myTag = baseMapIn.get(myKey);

        if (null == myTag) {

            myTag = tagIn;
            baseMapIn.put(myKey, myTag);
            CsiPersistenceManager.persist(myTag);
        }
        return myTag;
    }

    private static void forceAclUpdate(ACL aclIn) {
        CsiPersistenceManager.merge(aclIn);
        CsiPersistenceManager.commit();
        CsiPersistenceManager.begin();
    }
}
