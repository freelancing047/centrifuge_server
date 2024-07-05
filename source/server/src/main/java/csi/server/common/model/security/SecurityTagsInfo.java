package csi.server.common.model.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.CapcoSource;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.util.StringUtil;

/**
 * Created by centrifuge on 9/6/2016.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SecurityTagsInfo extends ModelObject {

    private String baseTagString = null;
    private String delimiterString = null;
    private String columnString = null;
    private String ignoredTagString = null;
    private CapcoSource mode = CapcoSource.USE_DEFAULT;
    private String identifiedTagString = null;
    private String restrictionBitString = null;
    private Long mappingBit;
    private boolean orTags = false;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private SecurityTagsInfo next = null;

    @Transient
    private List<Set<String>> restrictionTree = null;
    @Transient
    private List<String> identifiedTagList = null;
    @Transient
    private Map<String, Long> nameMap = null;
    @Transient
    private Map<Long, String> idMap = null;

    public static SecurityTagsInfo genFallBack(String defaultIn) {

        return new SecurityTagsInfo(CapcoSource.USER_ONLY, defaultIn, null, null, false);
    }

    public SecurityTagsInfo() {

    }

    public SecurityTagsInfo(CapcoSource modeIn, String baseTagStringIn, String columnStringIn,
                            String delimiterStringIn, boolean orTagsIn) {

        mode = modeIn;
        baseTagString = baseTagStringIn;
        columnString = columnStringIn;
        delimiterString = delimiterStringIn;
        orTags = orTagsIn;
    }

    @Override
    public void resetTransients() {

        restrictionTree = null;
        identifiedTagList = null;
        nameMap = new TreeMap<String, Long>();
        idMap = new TreeMap<Long, String>();
    }

    public void reset() {

        identifiedTagString = null;
        restrictionBitString = null;
        restrictionTree = null;
        identifiedTagList = null;
        nameMap = new TreeMap<String, Long>();
        idMap = new TreeMap<Long, String>();
        mappingBit = 0L;
        if (null != next) {

            next.reset();
        }
    }

    public void setMappingBit(Long mappingBitIn) {

        mappingBit = mappingBitIn;
    }

    public Long getMappingBit() {

        return mappingBit;
    }

    public void setOrTags(boolean orTagsIn) {

        orTags = orTagsIn;
    }

    public boolean getOrTags() {

        return orTags;
    }

    public CapcoSource getMode() {

        return mode;
    }

    public void setMode(CapcoSource modeIn) {

        mode = modeIn;
    }

    public String getBaseTagString() {

        return baseTagString;
    }

    public void setBaseTagString(String baseTagStringIn) {

        baseTagString = baseTagStringIn;
        restrictionTree = null;
    }

    public String getDelimiterString() {

        return delimiterString;
    }

    public void setDelimiterString(String delimiterStringIn) {

        delimiterString = delimiterStringIn;
    }

    public String getColumnString() {

        return columnString;
    }

    public void setColumnString(String columnStringIn) {

        columnString = columnStringIn;
    }

    public String getIdentifiedTagString() {

        return identifiedTagString;
    }

    public void setIdentifiedTagString(String identifiedTagStringIn) {

        identifiedTagString = identifiedTagStringIn;
    }

    public String getRestrictionBitString() {

        return restrictionBitString;
    }

    public void setRestrictionBitString(String restrictionBitStringIn) {

        restrictionBitString = restrictionBitStringIn;
        restrictionTree = null;
    }

    public String getIgnoredTagString() {

        return ignoredTagString;
    }

    public void setIgnoredTagString(String ignoredTagStringIn) {

        ignoredTagString = ignoredTagStringIn;
    }

    public SecurityTagsInfo getNext() {

        return next;
    }

    public void setNext(SecurityTagsInfo nextIn) {

        next = nextIn;
    }

    public char[] getDelimiterArray() {

        return (null != delimiterString) ? delimiterString.toCharArray() : null;
    }

    public void setDelimiters(char[] delimitersIn) {

        delimiterString = new String(delimitersIn);
    }

    public void setDelimiters(String delimitersIn) {

        delimiterString = delimitersIn;
    }

    public String[] getColumnArray() {

        return getArray(columnString);
    }

    public List<String> getColumnList() {

        return getList(columnString);
    }

    public Set<String> getColumnSet() {

        return getSet(columnString);
    }

    public void setColumns(String[] columnsIn) {

        columnString = StringUtil.concatUniqueInput(columnsIn);
    }

    public void setColumns(Collection<String> columnsIn) {

        columnString = StringUtil.concatUniqueInput(columnsIn);
    }

    public void setColumns(Set<String> columnsIn) {

        columnString = StringUtil.concatInput(columnsIn);
    }

    public boolean isTagField(FieldDef fieldIn) {

        Set<String> mySet = getColumnSet();

        if ((null != fieldIn) && (null != mySet) && !mySet.isEmpty()) {

            String myKey = fieldIn.getColumnLocalId();

            if ((null != myKey) && (mySet.contains(myKey))) {

                return true;

            } else {

                myKey = fieldIn.getLocalId();

                if ((null != myKey) && (mySet.contains(myKey))) {

                    return true;
                }
            }
        }
        return false;
    }

    public Map<String, Integer> getIdentifiedTagMap() {

        return StringUtil.genImplicitMap(identifiedTagString);
    }

    public List<String> getIdentifiedTagList() {

        if (null == identifiedTagList) {

            identifiedTagList = getOrderedList(identifiedTagString);
        }
        return identifiedTagList;
    }

    public String getIdentifiedTag(int indexIn) {

        getIdentifiedTagList();
        return (identifiedTagList.size() > indexIn) ? identifiedTagList.get(indexIn) : null;
    }

    public void setIdentifiedTags(Collection<String> identifiedTagsIn) {

        identifiedTagString = StringUtil.concatInput(identifiedTagsIn);
    }

    public void setRestrictionBits(List<Long> ListIn) {

        restrictionBitString = StringUtil.concatValues(ListIn);
        restrictionTree = null;
    }

    public List<Long> getRestrictionBits() {

        return (null != restrictionBitString) ? getValueList(restrictionBitString) : new ArrayList<Long>();
    }

    public String[] getIgnoredTagsArray() {

        return getArray(ignoredTagString);
    }

    public List<String> getIgnoredTagsList() {

        return getList(ignoredTagString);
    }

    public Set<String> getIgnoredTagsSet() {

        return getSet(ignoredTagString);
    }

    public void setIgnoredTagString(String[] ignoredTagStringIn) {

        ignoredTagString = StringUtil.concatUniqueInput(ignoredTagStringIn);
    }

    public void setIgnoredTagString(Collection<String> ignoredTagStringIn) {

        ignoredTagString = StringUtil.concatUniqueInput(ignoredTagStringIn);
    }

    public void setIgnoredTagString(Set<String> ignoredTagStringIn) {

        ignoredTagString = StringUtil.concatUniqueInput(ignoredTagStringIn);
    }

   public boolean doBaseTags() {
      return ((CapcoSource.USER_ONLY == mode) || (CapcoSource.USER_AND_DATA == mode));
   }

   public boolean doDataScan() {
      return ((columnString != null) && (columnString.length() > 0)
            && ((CapcoSource.DATA_ONLY == mode) || (CapcoSource.USER_AND_DATA == mode)));
   }

    private String[] getArray(String stringIn) {

        return getArray(stringIn, null);
    }

    private List<Long> getValueList(String stringIn) {

        return getValueList(stringIn, null);
    }

    private List<String> getList(String stringIn) {

        return getList(stringIn, null);
    }

    private List<String> getOrderedList(String stringIn) {

        return getOrderedList(stringIn, null);
    }

    private Set<String> getSet(String stringIn) {

        return getSet(stringIn, null);
    }

    private String[] getArray(String stringIn, Set<String> setIn) {

        if ((null != stringIn) && (0 < stringIn.length())) {

            Set<String> mySet = (null != setIn) ? setIn : new TreeSet<String>();
            StringUtil.extractInto(mySet, stringIn);
            return mySet.isEmpty() ? null : mySet.toArray(new String[0]);
        }
        return null;
    }

    private List<String> getOrderedList(String stringIn, List<String> listIn) {

        if ((null != stringIn) && (0 < stringIn.length())) {

            List<String> myList = (null != listIn) ? listIn : new ArrayList<String>();
            StringUtil.extractInto(myList, stringIn);
            return myList.isEmpty() ? null : new ArrayList(myList);
        }
        return null;
    }

    private List<String> getList(String stringIn, Set<String> setIn) {

        if ((null != stringIn) && (0 < stringIn.length())) {

            Set<String> mySet = (null != setIn) ? setIn : new TreeSet<String>();
            StringUtil.extractInto(mySet, stringIn);
            return mySet.isEmpty() ? null : new ArrayList(mySet);
        }
        return null;
    }

    private List<Long> getValueList(String stringIn, Set<Long> setIn) {

        if ((null != stringIn) && (0 < stringIn.length())) {

            Set<Long> mySet = (null != setIn) ? setIn : new TreeSet<Long>();
            StringUtil.extractValuesInto(mySet, stringIn);
            return mySet.isEmpty() ? null : new ArrayList(mySet);
        }
        return null;
    }

    private Set<String> getSet(String stringIn, Set<String> setIn) {

        if ((null != stringIn) && (0 < stringIn.length())) {

            Set<String> mySet = (null != setIn) ? setIn : new TreeSet<String>();
            StringUtil.extractInto(mySet, stringIn);
            return mySet.isEmpty() ? null : mySet;
        }
        return null;
    }

    public SecurityTagsInfo clone() {

        SecurityTagsInfo myClone = new SecurityTagsInfo();

        cloneComponents(myClone);

        return myClone;
    }

    public SecurityTagsInfo fullClone() {

        SecurityTagsInfo myClone = new SecurityTagsInfo();

        fullCloneComponents(myClone);

        return myClone;
    }

    protected void cloneComponents(SecurityTagsInfo cloneIn) {

        super.cloneComponents(cloneIn);
        cloneLocalComponents(cloneIn);
    }

    protected void fullCloneComponents(SecurityTagsInfo cloneIn) {

        super.fullCloneComponents(cloneIn);
        cloneLocalComponents(cloneIn);
    }

    protected void cloneLocalComponents(SecurityTagsInfo cloneIn) {

        cloneIn.resetTransients();
        cloneIn.setBaseTagString(getBaseTagString());
        cloneIn.setDelimiterString(getDelimiterString());
        cloneIn.setColumnString(getColumnString());
        cloneIn.setIgnoredTagString(getIgnoredTagString());
        cloneIn.setMode(getMode());
        cloneIn.setIdentifiedTagString(getIdentifiedTagString());
        cloneIn.setRestrictionBitString(getRestrictionBitString());
        cloneIn.setMappingBit(getMappingBit());
        cloneIn.setOrTags(getOrTags());
    }

    public void updateInPlace(SecurityTagsInfo sourceIn) {

        sourceIn.fullCloneComponents(this);
    }

    public boolean hasTags() {

        return ((null != restrictionBitString) && (0 < restrictionBitString.length()));
    }

    public String getBanner(String bannerPrefixIn, String tagDelimiterIn,
                            String subTagDelimiterIn, String bannerSuffixIn, String tagListPrefixIn) {

        String myLabel = null;

        restrictionTree = null;
        getRestrictionTree();

        if ((null != restrictionTree) && !restrictionTree.isEmpty()) {

            boolean doBannerPrefix = ((null != bannerPrefixIn) && (0 < bannerPrefixIn.length()));
            boolean doTagListPrefix = ((null != tagListPrefixIn) && (0 < tagListPrefixIn.length()));
            boolean doTagDelimiter = ((null != tagDelimiterIn) && (0 < tagDelimiterIn.length()));
            boolean doSubTagDelimiter = ((null != subTagDelimiterIn) && (0 < subTagDelimiterIn.length()));
            StringBuilder myBuffer = new StringBuilder();

            if (doBannerPrefix) {

                myBuffer.append(bannerPrefixIn);
            }
            for (Set<String> myTagSet : restrictionTree) {

                if (doTagListPrefix) {

                    myBuffer.append(tagListPrefixIn);
                }
                for (String myTag : myTagSet) {

                    myBuffer.append(myTag.toUpperCase());
                    if (doSubTagDelimiter) {

                        myBuffer.append(subTagDelimiterIn);
                    }
                }
                if (doSubTagDelimiter) {

                    myBuffer.setLength(myBuffer.length() - subTagDelimiterIn.length());
                }
                if (doTagDelimiter) {

                    myBuffer.append(tagDelimiterIn);
                }
            }
            if (doTagDelimiter) {

                myBuffer.setLength(myBuffer.length() - tagDelimiterIn.length());
            }
            if ((null != bannerSuffixIn) && (0 < bannerSuffixIn.length())) {

                myBuffer.append(bannerSuffixIn);
            }
            myLabel = myBuffer.toString();
        }
        return myLabel;
    }

    public List<Set<String>> getRestrictionTree() {

        if (null == restrictionTree) {

            restrictionTree = new ArrayList<Set<String>>();

            if ((null != restrictionBitString) && (0 < restrictionBitString.length())) {

                Set<Long> myValueSet = new TreeSet<Long>();

                StringUtil.extractValuesInto(myValueSet, restrictionBitString);

                if (!myValueSet.isEmpty()) {

                    for (Long myValue : myValueSet) {

                        Set<String> mySubSet = new TreeSet<String>();

                        for (int i = 0; 64 > i; i++) {

                            long myMask = 1L << i;

                            if (myMask > myValue) {

                                break;
                            }
                            if (0 != (myMask & myValue)) {

                                String myTag = getIdentifiedTag(i);

                                if (null != myTag) {

                                    mySubSet.add(myTag);
                                }
                            }
                        }
                        restrictionTree.add(mySubSet);
                    }
                }
            }
        }
        return restrictionTree;
    }

    public List<Long> setDefaultTags() {

        List<Long> myBitMaps = new ArrayList<Long>();

        reset();

        if ((null != baseTagString) && (0 < baseTagString.length())) {

            List<Set<String>> myList = new ArrayList<Set<String>>();
            Collection<String> myGroups = StringUtil.extractInto(new ArrayList<String>(), baseTagString.toLowerCase(), '|');

            for (String myGroup : myGroups) {

                Set<String> myTags = (Set<String>)StringUtil.extractInto(new TreeSet<String>(), myGroup, ',');

                if ((null != myTags) && !myTags.isEmpty()) {

                    myList.add(myTags);
                }
            }
            myBitMaps = processTags(myList, new ArrayList<Long>());
        }
        return myBitMaps;
    }

    public void initializeTags(Collection<String> tagSetsIn, boolean orTagsIn) throws CsiSecurityException {

        if (orTagsIn) {

            initializeOrTags(tagSetsIn);

        } else {

            initializeAndTags(tagSetsIn);
        }
    }

    public void initializeAndTags(Collection<String> tagSetsIn) {

        if ((null != tagSetsIn) && !tagSetsIn.isEmpty()) {

            List<Set<String>> myList = new ArrayList<Set<String>>();
            TreeSet<String> myResultSet = new TreeSet<String>();
            char[] myDelimiters = getDelimiterArray();
            int myDelimiterCount = (null != myDelimiters) ? myDelimiters.length : 0;

            for (String myString : tagSetsIn) {

                myResultSet.add(myString.trim().toLowerCase());
            }

            if (0 < myDelimiterCount) {

                for (char myDelimeter : myDelimiters) {

                    TreeSet<String> myWorkSet = new TreeSet<String>();

                    for (String myResult : myResultSet) {

                        StringUtil.extractInto(myWorkSet, myResult, myDelimeter);
                    }
                    myResultSet = myWorkSet;
                }
            }
            for (String myTag : myResultSet) {

                Set<String> mySet = new TreeSet<String>();

                mySet.add(myTag);
                myList.add(mySet);
            }
            processTags(myList, setDefaultTags());
        }
    }

    public void initializeOrTags(Collection<String> tagSetsIn) throws CsiSecurityException {

        char[] myDelimiters = getDelimiterArray();

        if ((null != myDelimiters) && (0 < myDelimiters.length)) {

            if (!tagSetsIn.isEmpty()) {

                List<Set<String>> myList = new ArrayList<Set<String>>();

                for (String myResult : tagSetsIn) {

                    TreeSet<String> myFullSet = new TreeSet<String>();

                    myFullSet.add(myResult);

                    for (char myDelimeter : myDelimiters) {

                        TreeSet<String> myWorkSet = new TreeSet<String>();

                        for (String myItem : myFullSet) {

                            StringUtil.extractInto(myWorkSet, myItem, myDelimeter);
                        }
                        myFullSet = myWorkSet;
                    }
                    myList.add(myFullSet);
                }
                processTags(myList, setDefaultTags());
            }

        } else {

            throw new CsiSecurityException("No delimeter defined for separating ORed Security Tags.");
        }
    }

    public List<Set<String>> augmentTags(List<Set<String>> distributionListIn) {

        restrictionTree = null;

        processTags(distributionListIn, getRestrictionBits());

        return getRestrictionTree();
    }

    private List<Long> processTags(List<Set<String>> distributionListIn, List<Long> bitMapsIn) {

        if ((null != distributionListIn) && !distributionListIn.isEmpty()) {

            buildMaps();

            for (Set<String> myGroup : distributionListIn) {

                if ((null != myGroup) && !myGroup.isEmpty()) {

                    long myMask = 0L;
                    boolean myDiscard = false;

                    for (String myTag : myGroup) {

                        Long myId = nameMap.get(myTag);

                        if (null == myId) {

                            myId = mappingBit++;
                            nameMap.put(myTag, myId);
                            idMap.put(myId, myTag);
                        }
                        myMask |= 1L << myId;
                    }
                    if (0L != myMask) {

                        for (int i = bitMapsIn.size() - 1; 0 <= i; i--) {

                            long myTest = bitMapsIn.get(i);

                            if ((myMask & myTest) == myTest) {

                                myDiscard = true;
                                break;

                            } else if ((myMask & myTest) == myMask) {

                                bitMapsIn.remove(i);
                            }
                        }
                        if (!myDiscard) {

                            bitMapsIn.add(myMask);
                        }
                    }
                }
            }
            setIdentifiedTags(idMap.values());
            setRestrictionBits(bitMapsIn);
        }
        return bitMapsIn;
    }

    private Map<String, Long> getNameMap() {

        if (null == nameMap) {

            buildMaps();
        }
        return nameMap;
    }

    private Map<Long, String> getIdMap() {

        if (null == idMap) {

            buildMaps();
        }
        return idMap;
    }

    private void buildMaps() {

        nameMap = new TreeMap<String, Long>();
        idMap = new TreeMap<Long, String>();

        if (null == mappingBit) {

            mappingBit = 0L;
        }

        if ((null != identifiedTagString) && (0 < identifiedTagString.length())) {

            String[] myTags = StringUtil.split(identifiedTagString);

            for (int i = 0; myTags.length > i; i++) {

                String myTag = myTags[i];

                if ((null != myTag) && (0 < myTag.length())) {

                    nameMap.put(myTag, (long)i);
                    idMap.put((long)i, myTag);
                }
            }
        }
    }

    public void removeAllLinkupInfo() {

        if (null != next) {

            next.removeAllLinkupInfo();
            next = null;
        }
    }

    public SecurityTagsInfo addLinkupInfo(SecurityTagsInfo tagsIn) {

        if (null != next) {

            return next.addLinkupInfo(tagsIn);

        } else {

            next = tagsIn;
        }
        return next;
    }

    public SecurityTagsInfo genForLinkup(List<String> fieldListIn) {

        SecurityTagsInfo myClone = clone();

        myClone.reset();
        myClone.setColumns(fieldListIn);
        return myClone;
    }

    public List<Set<String>> mergeResults() {

        return (null != next) ? augmentTags(next.mergeResults()) : getRestrictionTree();
    }

    public void lockResults() {

        baseTagString = buildBaseString(mergeResults());
        columnString = null;
        delimiterString = null;
        next = null;
        mode = CapcoSource.USER_ONLY;
    }

   private String buildBaseString(List<Set<String>> treeIn) {
      StringBuilder myBuffer = new StringBuilder();
      List<Set<String>> myTree = (treeIn == null) ? getRestrictionTree() : treeIn;

      if ((myTree != null) && !myTree.isEmpty()) {
         boolean firstTree = true;

         for (Set<String> mySet : myTree) {
            if ((mySet != null) && !mySet.isEmpty()) {
               boolean firstSet = true;

               if (firstTree) {
                  firstTree = false;
               } else {
                  myBuffer.append('|');
               }
               for (String myTag : mySet) {
                  if ((myTag != null) && (myTag.length() > 0)) {
                     if (firstSet) {
                        firstSet = false;
                     } else {
                        myBuffer.append(',');
                     }
                     myBuffer.append(myTag);
                  }
               }
            }
         }
      }
      return myBuffer.toString();
   }
}
