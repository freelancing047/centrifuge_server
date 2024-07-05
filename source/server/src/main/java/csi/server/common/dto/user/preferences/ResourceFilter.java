package csi.server.common.dto.user.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.SelectionListData.OptionBasics;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.ResourceSortMode;
import csi.server.common.util.StringUtil;

/**
 * Created by centrifuge on 3/1/2016.
 */
@Entity
@Table(
        name="resourcefilter",
        indexes = {
                @Index(columnList = "logonid", name = "idx_resourcefilter_logonid", unique = false)
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ResourceFilter implements IsSerializable {
   private static final long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = null;
    private String logonId;
    private String name;

    private String matchPattern;
    private String rejectPattern;
    private boolean testName;
    private boolean testRemarks;

    private Date createOnOrAfter;
    private Date createBefore;
    private Date modifyOnOrAfter;
    private Date modifyBefore;
    private Date accessOnOrAfter;
    private Date accessBefore;

    private Integer createOnOrAfterDelta;
    private Integer createBeforeDelta;
    private Integer modifyOnOrAfterDelta;
    private Integer modifyBeforeDelta;
    private Integer accessOnOrAfterDelta;
    private Integer accessBeforeDelta;

    private ResourceSortMode firstSort;
    private ResourceSortMode secondSort;
    private ResourceSortMode thirdSort;
    private ResourceSortMode fourthSort;

    private String ownerMatchString;
    private String ownerRejectString;
    private String accessMatchString;
    private String accessRejectString;
    private String editMatchString;
    private String editRejectString;
    private String deleteMatchString;
    private String deleteRejectString;

    private String remarks;
    private boolean defaultFilter;

    public ResourceFilter() {

    }

    public ResourceFilter(boolean testNameIn) {

        this();
        testName = testNameIn;
    }

    public ResourceFilter(ResourceFilter sourceFilterIn) {

        copy(sourceFilterIn);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long filterIdIn) {

        id = filterIdIn;
    }

    public void setLogonId(String logonIdIn) {

        logonId = logonIdIn;
    }

    public String getLogonId() {

        return logonId;
    }

    public void setName(String nameIn) {

        name = nameIn;
    }

    public String getName() {

        return name;
    }

    public ResourceSortMode[] getSortingRequest() {

        List<ResourceSortMode> mySortingRequest = new ArrayList<ResourceSortMode>(4);

        if (null != firstSort) {

            mySortingRequest.add(firstSort);
        }
        if (null != secondSort) {

            mySortingRequest.add(secondSort);
        }
        if (null != thirdSort) {

            mySortingRequest.add(thirdSort);
        }
        if (null != fourthSort) {

            mySortingRequest.add(fourthSort);
        }
        return mySortingRequest.isEmpty() ? null : mySortingRequest.toArray(new ResourceSortMode[0]);
    }

    public void setMatchDisplayPattern(String patternIn) {

        matchPattern = StringUtil.patternToSql(patternIn);
    }

    public String getMatchDisplayPattern() {

        return ((null != matchPattern) && (0 < matchPattern.length())) ? StringUtil.patternFromSql(matchPattern) : null;
    }

    public void setMatchPattern(String patternIn) {

        matchPattern = patternIn;
    }

    public String getMatchPattern() {

        return matchPattern;
    }

    public void setRejectDisplayPattern(String patternIn) {

        rejectPattern = StringUtil.patternToSql(patternIn);
    }

    public String getRejectDisplayPattern() {

        return ((null != rejectPattern) && (0 < rejectPattern.length())) ? StringUtil.patternFromSql(rejectPattern) : null;
    }

    public void setRejectPattern(String patternIn) {

        rejectPattern = patternIn;
    }

    public String getRejectPattern() {

        return rejectPattern;
    }

    public void setTestName(boolean testNameIn) {

        testName = testNameIn;
    }

    public boolean getTestName() {

        return testName;
    }

    public void setTestRemarks(boolean testRemarksIn) {

        testRemarks = testRemarksIn;
    }

    public boolean getTestRemarks() {

        return testRemarks;
    }

    public void setOwnerMatchString(String ownerStringIn) {

        ownerMatchString = ownerStringIn;
    }

    public String getOwnerMatchString() {

        return ownerMatchString;
    }

    public void setOwnerRejectString(String ownerStringIn) {

        ownerRejectString = ownerStringIn;
    }

    public String getOwnerRejectString() {

        return ownerRejectString;
    }

    public void setAccessMatchString(String accessStringIn) {

        accessMatchString = accessStringIn;
    }

    public String getAccessMatchString() {

        return accessMatchString;
    }

    public void setAccessRejectString(String accessStringIn) {

        accessRejectString = accessStringIn;
    }

    public String getAccessRejectString() {

        return accessRejectString;
    }

    public void setEditMatchString(String editStringIn) {

        editMatchString = editStringIn;
    }

    public String getEditMatchString() {

        return editMatchString;
    }

    public void setEditRejectString(String editStringIn) {

        editRejectString = editStringIn;
    }

    public String getEditRejectString() {

        return editRejectString;
    }

    public void setDeleteMatchString(String deleteStringIn) {

        deleteMatchString = deleteStringIn;
    }

    public String getDeleteMatchString() {

        return deleteMatchString;
    }

    public void setDeleteRejectString(String deleteStringIn) {

        deleteRejectString = deleteStringIn;
    }

    public String getDeleteRejectString() {

        return deleteRejectString;
    }

    public void setCreateOnOrAfter(Date createOnOrAfterIn) {

        createOnOrAfter = createOnOrAfterIn;
    }

    public Date getCreateOnOrAfter() {

        return createOnOrAfter;
    }

    public void setCreateBefore(Date createBeforeIn) {

        createBefore = createBeforeIn;
    }

    public Date getCreateBefore() {

        return createBefore;
    }

    public void setModifyOnOrAfter(Date modifyOnOrAfterIn) {

        modifyOnOrAfter = modifyOnOrAfterIn;
    }

    public Date getModifyOnOrAfter() {

        return modifyOnOrAfter;
    }

    public void setModifyBefore(Date modifyBeforeIn) {

        modifyBefore = modifyBeforeIn;
    }

    public Date getModifyBefore() {

        return modifyBefore;
    }

    public void setAccessOnOrAfter(Date accessOnOrAfterIn) {

        accessOnOrAfter = accessOnOrAfterIn;
    }

    public Date getAccessOnOrAfter() {

        return accessOnOrAfter;
    }

    public void setAccessBefore(Date accessBeforeIn) {

        accessBefore = accessBeforeIn;
    }

    public Date getAccessBefore() {

        return accessBefore;
    }

    public void setRemarks(String remarksIn) {

        remarks = remarksIn;
    }

    public String getRemarks() {

        return remarks;
    }

    public void setDefaultFilter(boolean defaultFilterIn) {

        defaultFilter = defaultFilterIn;
    }

    public boolean getDefaultFilter() {

        return defaultFilter;
    }

    public Date[] getTemporalAbsolutes() {

        return new Date[] {createOnOrAfter, createBefore, modifyOnOrAfter,
                modifyBefore, accessOnOrAfter, accessBefore};
    }

    public void setTemporalAbsolutes(Date[] temporalAbsolutesIn) {

        if (null != temporalAbsolutesIn) {

            int myCount = temporalAbsolutesIn.length;

            switch (myCount) {

                case 0:

                    createOnOrAfter = null;

                case 1:

                    createBefore = null;

                case 2:

                    modifyOnOrAfter = null;

                case 3:

                    modifyBefore = null;

                case 4:

                    accessOnOrAfter = null;

                case 5:

                    accessBefore = null;

                default:
            }

            switch (myCount) {

                case 6:

                    accessBefore = temporalAbsolutesIn[5];

                case 5:

                    accessOnOrAfter = temporalAbsolutesIn[4];

                case 4:

                    modifyBefore = temporalAbsolutesIn[3];

                case 3:

                    modifyOnOrAfter = temporalAbsolutesIn[2];

                case 2:

                    createBefore = temporalAbsolutesIn[1];

                case 1:

                    createOnOrAfter = temporalAbsolutesIn[0];

                default:
            }

        } else {

            clearAclList();
        }
    }

    public void clearTemporalAbsoluteList() {

        createOnOrAfter = null;
        createBefore = null;
        modifyOnOrAfter = null;
        modifyBefore = null;
        accessOnOrAfter = null;
        accessBefore = null;
    }

    public void setCreateOnOrAfter(Integer createOnOrAfterDeltaIn) {

        createOnOrAfterDelta = createOnOrAfterDeltaIn;
    }

    public Integer getCreateOnOrAfterDelta() {

        return createOnOrAfterDelta;
    }

    public void setCreateBefore(Integer createBeforeDeltaIn) {

        createBeforeDelta = createBeforeDeltaIn;
    }

    public Integer getCreateBeforeDelta() {

        return createBeforeDelta;
    }

    public void setModifyOnOrAfterDelta(Integer modifyOnOrAfterDeltaIn) {

        modifyOnOrAfterDelta = modifyOnOrAfterDeltaIn;
    }

    public Integer getModifyOnOrAfterDelta() {

        return modifyOnOrAfterDelta;
    }

    public void setModifyBeforeDelta(Integer modifyBeforeDeltaIn) {

        modifyBeforeDelta = modifyBeforeDeltaIn;
    }

    public Integer getModifyBeforeDelta() {

        return modifyBeforeDelta;
    }

    public void setAccessOnOrAfterDelta(Integer accessOnOrAfterDeltaIn) {

        accessOnOrAfterDelta = accessOnOrAfterDeltaIn;
    }

    public Integer getAccessOnOrAfterDelta() {

        return accessOnOrAfterDelta;
    }

    public void setAccessBeforeDelta(Integer accessBeforeDeltaIn) {

        accessBeforeDelta = accessBeforeDeltaIn;
    }

    public Integer getAccessBeforeDelta() {

        return accessBeforeDelta;
    }

    public Integer[] getTemporalDeltas() {

        return new Integer[] {createOnOrAfterDelta, createBeforeDelta, modifyOnOrAfterDelta,
                                modifyBeforeDelta, accessOnOrAfterDelta, accessBeforeDelta};
    }

    public void setTemporalDeltas(Integer[] temporalDeltasIn) {

        if (null != temporalDeltasIn) {

            int myCount = temporalDeltasIn.length;

            switch (myCount) {

                case 0:

                    createOnOrAfterDelta = null;

                case 1:

                    createBeforeDelta = null;

                case 2:

                    modifyOnOrAfterDelta = null;

                case 3:

                    modifyBeforeDelta = null;

                case 4:

                    accessOnOrAfterDelta = null;

                case 5:

                    accessBeforeDelta = null;

                default:
            }

            switch (myCount) {

                case 6:

                    accessBeforeDelta = temporalDeltasIn[5];

                case 5:

                    accessOnOrAfterDelta = temporalDeltasIn[4];

                case 4:

                    modifyBeforeDelta = temporalDeltasIn[3];

                case 3:

                    modifyOnOrAfterDelta = temporalDeltasIn[2];

                case 2:

                    createBeforeDelta = temporalDeltasIn[1];

                case 1:

                    createOnOrAfterDelta = temporalDeltasIn[0];

                default:
            }

        } else {

            clearAclList();
        }
    }

    public void clearTemporalDeltaList() {

        createOnOrAfterDelta = null;
        createBeforeDelta = null;
        modifyOnOrAfterDelta = null;
        modifyBeforeDelta = null;
        accessOnOrAfterDelta = null;
        accessBeforeDelta = null;
    }

    public Date[] getTemporalValuesForQuery() {

        Date[] myDateArray = new Date[6];

        myDateArray[0] = (null != createOnOrAfter) ? createOnOrAfter :  makeDate(createOnOrAfterDelta, 0);
        myDateArray[1] = (null != createBefore) ? createBefore :  makeDate(createBeforeDelta, 1);
        myDateArray[2] = (null != modifyOnOrAfter) ? modifyOnOrAfter :  makeDate(modifyOnOrAfterDelta, 0);
        myDateArray[3] = (null != modifyBefore) ? modifyBefore :  makeDate(modifyBeforeDelta, 1);
        myDateArray[4] = (null != accessOnOrAfter) ? accessOnOrAfter :  makeDate(accessOnOrAfterDelta, 0);
        myDateArray[5] = (null != accessBefore) ? accessBefore :  makeDate(accessBeforeDelta, 1);

        return myDateArray;
    }

    public void setFirstSort(ResourceSortMode firstSortIn) {

        firstSort = firstSortIn;
    }

    public ResourceSortMode getFirstSort() {

        return firstSort;
    }

    public void setSecondSort(ResourceSortMode secondSortIn) {

        secondSort = secondSortIn;
    }

    public ResourceSortMode getSecondSort() {

        return secondSort;
    }

    public void setThirdSort(ResourceSortMode thirdSortIn) {

        thirdSort = thirdSortIn;
    }

    public ResourceSortMode getThirdSort() {

        return thirdSort;
    }

    public void setFourthSort(ResourceSortMode fourthSortIn) {

        fourthSort = fourthSortIn;
    }

    public ResourceSortMode getFourthSort() {

        return fourthSort;
    }

    public void setOwnerMatchList(Collection<StringEntry> ownerListIn) {

        ownerMatchString = StringUtil.concatDisplay(ownerListIn);
    }

    public List<StringEntry> getOwnerMatchListForDisplay() {

        return (List<StringEntry>)StringUtil.extractForDisplay(new ArrayList<StringEntry>(), ownerMatchString, '|');
    }

    public List<String> getOwnerMatchListAsList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), ownerMatchString, '|');
    }

    public List<String> getOwnerMatchForAcl() {

        return (List<String>)StringUtil.extractForAcl(new ArrayList<String>(), ownerMatchString, '|');
    }

    public void setOwnerRejectList(Collection<StringEntry> ownerListIn) {

        ownerRejectString = StringUtil.concatDisplay(ownerListIn);
    }

    public List<StringEntry> getOwnerRejectForDisplay() {

        return (List<StringEntry>)StringUtil.extractForDisplay(new ArrayList<StringEntry>(), ownerRejectString, '|');
    }

    public List<String> getOwnerRejectListAsList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), ownerRejectString, '|');
    }

    public List<String> getOwnerRejectForAcl() {

        return (List<String>)StringUtil.extractForAcl(new ArrayList<String>(), ownerRejectString, '|');
    }



    public void setAccessMatchList(Collection<StringEntry> accessListIn) {

        accessMatchString = StringUtil.concatDisplay(accessListIn);
    }

    public List<StringEntry> getAccessMatchForDisplay() {

        return (List<StringEntry>)StringUtil.extractForDisplay(new ArrayList<StringEntry>(), accessMatchString, '|');
    }

    public List<String> getAccessMatchListAsList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), accessMatchString, '|');
    }

    public List<String> getAccessMatchForAcl() {

        return (List<String>)StringUtil.extractForAcl(new ArrayList<String>(), accessMatchString, '|');
    }

    public void setAccessRejectList(Collection<StringEntry> accessListIn) {

        accessRejectString = StringUtil.concatDisplay(accessListIn);
    }

    public List<StringEntry> getAccessRejectForDisplay() {

        return (List<StringEntry>)StringUtil.extractForDisplay(new ArrayList<StringEntry>(), accessRejectString, '|');
    }

    public List<String> getAccessRejectListAsList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), accessRejectString, '|');
    }

    public List<String> getAccessRejectForAcl() {

        return (List<String>)StringUtil.extractForAcl(new ArrayList<String>(), accessRejectString, '|');
    }



    public void setEditMatchList(Collection<StringEntry> editListIn) {

        editMatchString = StringUtil.concatDisplay(editListIn);
    }

    public List<StringEntry> getEditMatchForDisplay() {

        return (List<StringEntry>)StringUtil.extractForDisplay(new ArrayList<StringEntry>(), editMatchString, '|');
    }

    public List<String> getEditMatchListAsList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), editMatchString, '|');
    }

    public List<String> getEditMatchForAcl() {

        return (List<String>)StringUtil.extractForAcl(new ArrayList<String>(), editMatchString, '|');
    }

    public void setEditRejectList(Collection<StringEntry> editListIn) {

        editRejectString = StringUtil.concatDisplay(editListIn);
    }

    public List<StringEntry> getEditRejectForDisplay() {

        return (List<StringEntry>)StringUtil.extractForDisplay(new ArrayList<StringEntry>(), editRejectString, '|');
    }

    public List<String> getEditRejectListAsList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), editRejectString, '|');
    }

    public List<String> getEditRejectForAcl() {

        return (List<String>)StringUtil.extractForAcl(new ArrayList<String>(), editRejectString, '|');
    }



    public void setDeleteMatchList(Collection<StringEntry> deleteListIn) {

        deleteMatchString = StringUtil.concatDisplay(deleteListIn);
    }

    public List<StringEntry> getDeleteMatchForDisplay() {

        return (List<StringEntry>)StringUtil.extractForDisplay(new ArrayList<StringEntry>(), deleteMatchString, '|');
    }

    public List<String> getDeleteMatchListAsList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), deleteMatchString, '|');
    }

    public List<String> getDeleteMatchForAcl() {

        return (List<String>)StringUtil.extractForAcl(new ArrayList<String>(), deleteMatchString, '|');
    }

    public void setDeleteRejectList(Collection<StringEntry> deleteListIn) {

        deleteRejectString = StringUtil.concatDisplay(deleteListIn);
    }

    public List<StringEntry> getDeleteRejectForDisplay() {

        return (List<StringEntry>)StringUtil.extractForDisplay(new ArrayList<StringEntry>(), deleteRejectString, '|');
    }

    public List<String> getDeleteRejectListAsList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), deleteRejectString, '|');
    }

    public List<String> getDeleteRejectForAcl() {

        return (List<String>)StringUtil.extractForAcl(new ArrayList<String>(), deleteRejectString, '|');
    }

    public void clearAclList() {

        accessRejectString = null;
        accessMatchString = null;
        editRejectString = null;
        editMatchString = null;
        deleteRejectString = null;
        deleteMatchString = null;
        ownerRejectString = null;
        ownerMatchString = null;
    }

    public void setListOfAclLists(List<List<List<StringEntry>>> aclListIn) {

        if (null != aclListIn) {

            int myCount = aclListIn.size();

            // Clear strings that are not included in input data
            switch (myCount) {

                case 0:

                    ownerMatchString = null;
                    ownerRejectString = null;

                case 1:

                    accessMatchString = null;
                    accessRejectString = null;

                case 2:

                    editMatchString = null;
                    editRejectString = null;

                case 3:

                    deleteMatchString = null;
                    deleteRejectString = null;

                default:
            }

            // Set strings that are included as part of the input data
            switch (myCount) {

                case 4:

                    setDeleteRejectList(aclListIn.get(3).get(1));
                    setDeleteMatchList(aclListIn.get(3).get(0));

                case 3:

                    setEditRejectList(aclListIn.get(2).get(1));
                    setEditMatchList(aclListIn.get(2).get(0));

                case 2:

                    setAccessRejectList(aclListIn.get(1).get(1));
                    setAccessMatchList(aclListIn.get(1).get(0));

                case 1:

                    setOwnerRejectList(aclListIn.get(0).get(1));
                    setOwnerMatchList(aclListIn.get(0).get(0));

                default:
            }

        } else {

            clearAclList();
        }
    }

    public List<List<Collection<StringEntry>>> getListOfAclDisplayLists() {

        List<List<Collection<StringEntry>>> myFinalList = new ArrayList<List<Collection<StringEntry>>>();
        List<Collection<StringEntry>> myOwnerList = new ArrayList<Collection<StringEntry>>();
        List<Collection<StringEntry>> myReadList = new ArrayList<Collection<StringEntry>>();
        List<Collection<StringEntry>> myEditList = new ArrayList<Collection<StringEntry>>();
        List<Collection<StringEntry>> myDeleteList = new ArrayList<Collection<StringEntry>>();

        myOwnerList.add(getOwnerMatchListForDisplay());
        myOwnerList.add(getOwnerRejectForDisplay());
        myFinalList.add(myOwnerList);

        myReadList.add(getAccessMatchForDisplay());
        myReadList.add(getAccessRejectForDisplay());
        myFinalList.add(myReadList);

        myEditList.add(getEditMatchForDisplay());
        myEditList.add(getEditRejectForDisplay());
        myFinalList.add(myEditList);

        myDeleteList.add(getDeleteMatchForDisplay());
        myDeleteList.add(getDeleteRejectForDisplay());
        myFinalList.add(myDeleteList);

        return myFinalList;
    }

    public String getSortingName() {

        return name.toLowerCase();
    }

    public String getDisplayName() {

        return name;
    }

    public ResourceFilter clone() {

        return (new ResourceFilter()).copy(this).finalizeClone(id);
    }

    public ResourceFilter copy(ResourceFilter sourceFilterIn) {

        name = sourceFilterIn.getName();
        logonId = sourceFilterIn.getLogonId();
        matchPattern = sourceFilterIn.getMatchPattern();
        rejectPattern = sourceFilterIn.getRejectPattern();
        testName = sourceFilterIn.getTestName();
        testRemarks = sourceFilterIn.getTestRemarks();
        createOnOrAfter = sourceFilterIn.getCreateOnOrAfter();
        createBefore = sourceFilterIn.getCreateBefore();
        modifyOnOrAfter = sourceFilterIn.getModifyOnOrAfter();
        modifyBefore = sourceFilterIn.getModifyBefore();
        accessOnOrAfter = sourceFilterIn.getAccessOnOrAfter();
        accessBefore = sourceFilterIn.getAccessBefore();
        createOnOrAfterDelta = sourceFilterIn.getCreateOnOrAfterDelta();
        createBeforeDelta = sourceFilterIn.getCreateBeforeDelta();
        modifyOnOrAfterDelta = sourceFilterIn.getModifyOnOrAfterDelta();
        modifyBeforeDelta = sourceFilterIn.getModifyBeforeDelta();
        accessOnOrAfterDelta = sourceFilterIn.getAccessOnOrAfterDelta();
        accessBeforeDelta = sourceFilterIn.getAccessBeforeDelta();
        firstSort = sourceFilterIn.getFirstSort();
        secondSort = sourceFilterIn.getSecondSort();
        thirdSort = sourceFilterIn.getThirdSort();
        fourthSort = sourceFilterIn.getFourthSort();
        ownerMatchString = sourceFilterIn.getOwnerMatchString();
        ownerRejectString = sourceFilterIn.getOwnerRejectString();
        accessMatchString = sourceFilterIn.getAccessMatchString();
        accessRejectString = sourceFilterIn.getAccessRejectString();
        editMatchString = sourceFilterIn.getEditMatchString();
        editRejectString = sourceFilterIn.getEditRejectString();
        deleteMatchString = sourceFilterIn.getDeleteMatchString();
        deleteRejectString = sourceFilterIn.getDeleteRejectString();
        remarks = sourceFilterIn.getRemarks();
        defaultFilter = sourceFilterIn.getDefaultFilter();

        return this;
    }

   public OptionBasics getOptionBasics() {
      return new OptionBasics(Long.toString(id), name, remarks, defaultFilter);
   }

   private ResourceFilter finalizeClone(Long idIn) {
      id = idIn;
      return this;
   }

   private Date makeDate(Integer delta, int adjustment) {
      Date myDate = null;

      if (delta != null) {
         Date myToday = new Date();
         long myBase = myToday.getTime() / MILLIS_PER_DAY;
         Date adjustedDate = new Date(((myBase - delta.intValue()) + adjustment) * MILLIS_PER_DAY);
         myDate = new Date(adjustedDate.getTime() + (adjustedDate.getTimezoneOffset() * 60 * 1000));

//         myDate = Date.from(LocalDateTime.now().minusDays(delta.intValue())
//                                               .plusDays(adjustment)
//                                               .atZone(ZoneId.systemDefault()).toInstant());
      }
      return myDate;
   }
}
