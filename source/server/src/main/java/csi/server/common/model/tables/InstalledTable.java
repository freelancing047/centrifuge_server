package csi.server.common.model.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.security.monitors.ResourceACLMonitor;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.enumerations.InstallationType;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.interfaces.DataWrapper;
import csi.server.common.interfaces.SecurityAccess;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.Resource;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 7/7/2015.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(ResourceACLMonitor.class)
public class InstalledTable extends Resource implements DataWrapper, SecurityAccess {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String EXAMPLE_UUID = CsiUUID.randomUUID();
    private static final int UUID_STRING_LENGTH = EXAMPLE_UUID.length();
    private static final String INSTALLED_TABLE_PREFIX = "file_";
    private static final String INSTALLED_TABLE_BASE_PATTERN = "file_xxxxxxxx_xxxx_xxxx_xxxx_xxxxxxxxxxxx";
    private static final int INSTALLED_TABLE_PREFIX_SIZE = INSTALLED_TABLE_PREFIX.length();
    private static final int INSTALLED_TABLE_BASE_PATTERN_SIZE = INSTALLED_TABLE_BASE_PATTERN.length();

    private String version;
    private Boolean needsSource = false;

    private String origin;
    private String driver;          // Driver key
    private String tableName;
    private String topLevel;        // ie: Catalog Name
    private String midLevel;        // ie: Schema Name
    private String lowLevel;        // ie: Table Type
    private String baseName;        // ie: Table Name
    @Enumerated(value = EnumType.STRING)
    private InstallationType uploadType;
    private long firstRow;
    private boolean discardFlag;
    private int revision = -1;
    private String revisionList;    // Concatenated list of existing revisions and use counts
    private boolean rowLevelCapco = false;
    private boolean rowLevelTags = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "installedtable_uuid")
    private List<InstalledColumn> columns;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AdHocDataSource sourceDefinition;
    @Column(columnDefinition = "TEXT")
    protected String dataKeys = null;           // list of data source keys from linkups

    @Transient
    private Map<String, InstalledColumn> _fieldNameMap = null;
    @Transient
    private Map<String, InstalledColumn> _localIdMap = null;
    @Transient
    private Map<Integer, InstalledColumn> _ordinalMap = null;
    @Transient
    private Map<Integer, Integer> _revisionMap = null;
    @Transient
    private List<ValuePair<InstalledColumn, FieldDef>> _columnMapping = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Static Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static String createTablePath(String baseNameIn, String topLevelIn, String midLevelIn) {

        return (Format.identifyNull(topLevelIn)
                + "." + Format.identifyNull(midLevelIn)
                + "." + Format.identifyNull(baseNameIn));
    }

    public static String generateInstalledTableName() {
        return INSTALLED_TABLE_PREFIX + CsiUUID.randomUUID().replace('-', '_');
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public InstalledTable() {

        super(AclResourceType.DATA_TABLE);
    }

    public InstalledTable(InstallationType uploadTypeIn) {

        super(AclResourceType.DATA_TABLE);
        uploadType = uploadTypeIn;
        super.setFlags(1 << uploadType.ordinal());
    }

    public InstalledTable(String baseNameIn, DataView dataViewIn, String visualizationIn, String tableNameIn,
                          String remarksIn,  List<FieldDef> fieldListIn, CapcoInfo capcoInfoIn,
                          SecurityTagsInfo tagInfoIn, String userIn) {

        this(CsiFileType.DATAVIEW.getInstallationType());

        version = ReleaseInfo.version;
        lockDataKeys(dataViewIn.getDataSourceKeySet());
        columns = new ArrayList<InstalledColumn>();
        sourceDefinition = null;
        origin = visualizationIn;
        driver = dataViewIn.getUuid();
        tableName = tableNameIn;
        topLevel = userIn;
        midLevel = CsiFileType.DATAVIEW.getExtension();
        baseName = baseNameIn;
        capcoInfo = (null != capcoInfoIn) ? capcoInfoIn.clone() : null;
        securityTagsInfo = (null != tagInfoIn) ? tagInfoIn.clone() : null;
        firstRow = 0;
        size = 0;
        discardFlag = false;
        useCount = 0;
        setName(createTablePath(baseNameIn, topLevel, midLevel));
        setRemarks(remarksIn);
        setUseCount(1);

        _columnMapping = new ArrayList<>();
        int howMany = fieldListIn.size();

        for (int i = 0; i < howMany; i++) {
            FieldDef myField = fieldListIn.get(i);

            InstalledColumn myColumn = new InstalledColumn(myField.getFieldName(), CsiUUID.randomUUID(),
                                                            myField.getValueType(), true, i);
            columns.add(myColumn);
            _columnMapping.add(new ValuePair<InstalledColumn, FieldDef>(myColumn, myField));
        }
        migrateFieldLists(_columnMapping, capcoInfoIn, tagInfoIn);
    }

    public InstalledTable(String baseNameIn, String tableNameIn, String remarksIn, DataDefinition resourceIn,
                          CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn, String userIn) {

        this(InstallationType.ADHOC);

        version = ReleaseInfo.version;
        columns = new ArrayList<InstalledColumn>();
        sourceDefinition = (AdHocDataSource) ((resourceIn instanceof AdHocDataSource) ? resourceIn : null);
        origin = null;
        driver = null;
        tableName = tableNameIn;
        topLevel = userIn;
        midLevel = CsiFileType.ADHOC.getExtension();
        baseName = baseNameIn;
        capcoInfo = (null != capcoInfoIn) ? capcoInfoIn.clone() : null;
        securityTagsInfo = (null != tagInfoIn) ? tagInfoIn.clone() : null;
        firstRow = 0;
        size = 0;
        discardFlag = false;
        useCount = 0;
        setName(createTablePath(baseNameIn, topLevel, midLevel));
        setRemarks(remarksIn);
        setUseCount(1);

        // Create column list
        if (null != sourceDefinition) {

            List<FieldDef> myFields = sourceDefinition.getFieldDefs();

            if (null != myFields) {
               int howMany = myFields.size();

                for (int i = 0; i < howMany; i++) {
                    FieldDef myField = myFields.get(i);
                    InstalledColumn myColumn = new InstalledColumn(myField.getFieldName(), myField.getLocalId(),
                                                                   myField.getValueType(), true, i);
                    columns.add(myColumn);
                }
            }
        }
    }

    public InstalledTable(String baseNameIn, String userNameIn, String originIn, String tableNameIn,
                          CsiFileType fileTypeIn, CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn, String remarksIn) {

        this(fileTypeIn.getInstallationType());

        version = ReleaseInfo.version;
        columns = new ArrayList<InstalledColumn>();
        origin = originIn;
        driver = "@@" + fileTypeIn.getExtension() + "@@";
        tableName = tableNameIn;
        topLevel = userNameIn;
        midLevel = fileTypeIn.getExtension();
        baseName = baseNameIn;
        capcoInfo = (null != capcoInfoIn) ? capcoInfoIn.clone() : null;
        securityTagsInfo = (null != tagInfoIn) ? tagInfoIn.clone() : null;
        firstRow = 0;
        size = 0;
        discardFlag = false;
        useCount = 0;
        setName(createTablePath(baseNameIn, topLevel, midLevel));
        setRemarks(remarksIn);
        setUseCount(1);
    }

    public List<ValuePair<InstalledColumn, FieldDef>> getColumnMapping() {

        return _columnMapping;
    }

   public void migrateFieldLists(List<ValuePair<InstalledColumn, FieldDef>> pairedListIn,
                                 CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn) {
      boolean mySecurityFlag = false;

      if (capcoInfo != null) {
         capcoInfo.setFieldString(null);
         mySecurityFlag = true;
      }
      if (securityTagsInfo != null) {
         securityTagsInfo.setColumnString(null);
         mySecurityFlag = true;
      }
      // Create column list
      if ((pairedListIn != null) && mySecurityFlag) {
         StringJoiner myCapcoBuffer = new StringJoiner("|");
         StringJoiner myTagBuffer = new StringJoiner("|");
         int howMany = pairedListIn.size();

         for (int i = 0; i < howMany; i++) {
            ValuePair<InstalledColumn, FieldDef> myPair = pairedListIn.get(i);
            InstalledColumn myColumn = myPair.getValue1();
            FieldDef myField = myPair.getValue2();

            if (myField != null) {
               if (capcoInfoIn.isCapcoField(myField)) {
                  myCapcoBuffer.add(myColumn.getLocalId());
               }
               if (tagInfoIn.isTagField(myField)) {
                  myTagBuffer.add(myColumn.getLocalId());
               }
            }
         }
         if ((capcoInfo != null) && (myCapcoBuffer.length() > 0)) {
            capcoInfo.setFieldString(myCapcoBuffer.toString());
         }
         if ((securityTagsInfo != null) && (myTagBuffer.length() > 0)) {
            securityTagsInfo.setColumnString(myTagBuffer.toString());
         }
      }
   }

    public void setDataKeys(String dataKeysIn) {

        dataKeys = dataKeysIn;
    }

    public String getDataKeys() {

        return dataKeys;
    }

    public void discardDataKeys() {

        dataKeys = null;
    }

    public void addDataKeys(Collection<String> keySetIn) {

        if (null != keySetIn) {

            String myNewKeys = StringUtil.concatUniqueInput(keySetIn, '\t');

            if (null == dataKeys) {

                dataKeys = myNewKeys;

            } else {

                dataKeys = dataKeys + "\n" + myNewKeys;
            }
        }
    }

    public void lockDataKeys() {

        lockDataKeys(getDataSourceKeySet());
    }

    public void lockDataKeys(Set<String> keySetIn) {

        dataKeys = StringUtil.concatInput(keySetIn, '\t');
    }

    public Set<String> getDataSourceKeySet() {

        Set<String> myKeySet = new TreeSet<String>();

        if (null != dataKeys) {

            String[] myKeySets = StringUtil.split(dataKeys, '\n');

            for (int i = 0; myKeySets.length > i; i++) {

                String[] myKeys = StringUtil.split(myKeySets[i], '\t');

                myKeySet.addAll(Arrays.asList(myKeys));
            }

        } else if (null != sourceDefinition) {

            List<DataSourceDef> myDataSources = sourceDefinition.getDataSources();

            if (myDataSources != null) {

                for (DataSourceDef mySource : myDataSources) {

                    String myKey = mySource.getDataSourceKey();

                    if (null != myKey) {

                        myKeySet.add(myKey);
                    }
                }
            }
        }
        return myKeySet;
    }

    public List<String> getFieldIdList() {

        List<String> myIdList = new ArrayList<String>();

        for (InstalledColumn myColumn : columns) {

            myIdList.add(myColumn.getLocalId());
        }
        return myIdList;
    }

    public CsiFileType getFileType() {

        return CsiFileType.getFileType(midLevel);
    }

   public Map<String, FieldDef> getCapcoColumnMap() {
      Map<String, FieldDef> myResult = new HashMap<String, FieldDef>();
      List<String> myList = (capcoInfo == null) ? null : capcoInfo.getSecurityFields();

      if ((myList != null) && !myList.isEmpty()) {
         Set<String> mySet = new HashSet<String>(myList);

         if (!mySet.isEmpty()) {
            FieldListAccess myFieldAccess = getFieldListAccess();

            if (myFieldAccess != null) {
               for (String myKey : mySet) {
                  FieldDef myField = myFieldAccess.getFieldDefByAnyKey(myKey);

                  if (myField != null) {
                     myResult.put(myField.getLocalId(), myField);
                  }
               }
            }
         }
      }
      return myResult.isEmpty() ? null : myResult;
   }

   public Map<String, FieldDef> getTagColumnMap() {
      Map<String, FieldDef> myResult = new HashMap<String, FieldDef>();
      Set<String> mySet = (securityTagsInfo == null) ? null : securityTagsInfo.getColumnSet();

      if ((mySet != null) && !mySet.isEmpty()) {
         FieldListAccess myFieldAccess = getFieldListAccess();

         if (myFieldAccess != null) {
            for (String myKey : mySet) {
               FieldDef myField = myFieldAccess.getFieldDefByAnyKey(myKey);

               if (myField != null) {
                  myResult.put(myField.getLocalId(), myField);
               }
            }
         }
      }
      return myResult.isEmpty() ? null : myResult;
   }

    public List<ValuePair<FieldDef, FieldDef>> getPhysicalPairs() {

        FieldListAccess myFieldAccess = getFieldListAccess();

        return (null != myFieldAccess) ? myFieldAccess.getPhysicalPairs() : buildPhysicalPairs();
    }

    public List<String> getCapcoColumnNames() {

        List<String> myColumnNames = new ArrayList<>();

        if (null != capcoInfo) {

            Map<String, InstalledColumn> myMap = getLocalIdMap();
            List<String> myKeys = capcoInfo.getSecurityFields();

            if (myKeys != null) {

                for (String myKey : myKeys) {

                    InstalledColumn myColumn = myMap.get(myKey);

                    if (null != myColumn) {

                        myColumnNames.add(myColumn.getColumnName());
                    }
                }
            }
        }
        return myColumnNames.isEmpty() ? null : myColumnNames;
    }

    public List<String> getTagColumnNames() {

        List<String> myColumnNames = new ArrayList<>();

        if (null != securityTagsInfo) {

            Map<String, InstalledColumn> myMap = getLocalIdMap();
            List<String> myKeys = securityTagsInfo.getColumnList();

            if (myKeys != null) {

                for (String myKey : myKeys) {

                    InstalledColumn myColumn = myMap.get(myKey);

                    if (null != myColumn) {

                        myColumnNames.add(myColumn.getColumnName());
                    }
                }
            }
        }
        return myColumnNames.isEmpty() ? null : myColumnNames;
    }

    public void installCapco(CapcoInfo capcoIn) {

        capcoInfo = (null != capcoIn) ? capcoIn.clone() : null;
    }

    public void installSecurityTags(SecurityTagsInfo tagsIn) {

        securityTagsInfo = (null != tagsIn) ? tagsIn.clone() : null;
    }

    public String getInstalledTableName() {

        return INSTALLED_TABLE_PREFIX + getUuid().replace('-', '_')
                + ((0 < revision) ? "_" + Integer.toString(revision) : "");
    }

    public List<ColumnParameters> genColumnParameters() {

        List<ColumnParameters> myList = new ArrayList<ColumnParameters>();

        for (InstalledColumn myColumn : columns) {

            myList.add(new ColumnParameters(tableName, myColumn));
        }
        return myList;
    }

    public void resetTransients() {

        if (null != sourceDefinition) {

            sourceDefinition.resetTransients();
        }
        _fieldNameMap = null;
        _localIdMap = null;
        _ordinalMap = null;
        _revisionMap = null;
    }

    public boolean isOrphan() {

        return (0 == useCount) && (null == baseName);
    }

    public void releaseOwnership() {

        setUseCount(0);
        setBaseName(null);
    }

    public boolean readyToDiscard() {

        return (0 == getUseCount()) && ((null == revisionList) || (0 == revisionList.length()));
    }

    public int registerInstalledTableVersion(String tableNameIn) {

        revision = Math.max(revision, getTableRevision(tableNameIn));

        return revision;
    }

    public String lockTableName(String tableNameIn) {

        updateUsage(getTableRevision(tableNameIn), 1);

        return getVersionString();
    }

    public String lockRevision() {

        updateUsage(revision, 1);

        return getVersionString();
    }

    public String incrementLock(String lockIn) {

        String myLock = null;
        String[] myLockSet = StringUtil.split(lockIn, ':');

        if (uuid.getUuid().equals(myLockSet[0])) {

            updateUsage(Integer.decode(myLockSet[1]), 1);

            myLock = getVersionString();
        }
        return myLock;
    }

    public int revisionUseCount(final int revisionIn) {

        return updateUsage(revisionIn, 0);
    }

    public int releaseRevision(final int revisionIn) {

        return updateUsage(revisionIn, -1);
    }

    public String releaseLock(final String lockIn) {

        String[] myLockSet = StringUtil.split(lockIn, ':');

        if (uuid.equals(myLockSet[0])) {

            updateUsage(extractRevisionId(lockIn), -1);
        }

        return null;
    }

    public DataDefinition getDataDefinition() {

        return sourceDefinition;
    }

    public SecurityAccess getSecurityAccess() {

        return this;
    }

    public List<DataSourceDef> getDataSources() {

        return (null != sourceDefinition) ? sourceDefinition.getDataSources() : null;
    }

    public List<FieldDef> getFieldList() {

        return (null != sourceDefinition) ? sourceDefinition.getFieldList() : null;
    }

    public List<QueryParameterDef> getParameterList() {

        return (null != sourceDefinition) ? sourceDefinition.getParameterList() : null;
    }

    public String getTablePath() {

        return name;
    }

    public void setOrigin(String originIn) {

        origin = originIn;
    }

    public String getOrigin() {

        return origin;
    }

    public void setDriver(String driverIn) {

        driver = driverIn;
    }

    public String getDriver() {

        return driver;
    }

    public void setTableName(String tableNameIn) {

        tableName = tableNameIn;
    }

    public String getTableName() {

        return tableName;
    }

    public String getActiveTableName() {

        return (0 < revision) ? (tableName + "_" + Integer.toString(revision)) : tableName;
    }

    public String getNextActiveTableName() {

        return (-1 < revision) ? tableName + "_" + Integer.toString(revision + 1) : tableName;
    }

    public String getTableName(int revisionIn) {

        return (0 < revisionIn) ? (tableName + "_" + Integer.toString(revisionIn)) : tableName;
    }

    public String getTableName(String revisionIn) {

        int myRevision = extractRevisionId(revisionIn);

        return (0 < myRevision) ? (tableName + "_" + Integer.toString(myRevision)) : tableName;
    }

    public void setTopLevel(String topLevelIn) {

        topLevel = topLevelIn;
    }

    public String getTopLevel() {

        return topLevel;
    }

    public void setMidLevel(String midLevelIn) {

        midLevel = midLevelIn;
    }

    public String getMidLevel() {

        return midLevel;
    }

    public void setLowLevel(String lowLevelIn) {

        lowLevel = lowLevelIn;
    }

    public String getLowLevel() {

        return lowLevel;
    }

    public void setBaseName(String baseNameIn) {

        baseName = baseNameIn;
    }

    public String getBaseName() {

        return baseName;
    }

    public void setSourceDefinition(AdHocDataSource sourceDefinitionIn) {

        sourceDefinition = sourceDefinitionIn;
    }

    public AdHocDataSource getSourceDefinition() {

        if ((null != sourceDefinition) && (null == sourceDefinition.getResourceType())) {

            // Fill in implicit values.
            sourceDefinition.setResourceType(AclResourceType.DATA_TABLE);
            sourceDefinition.setCapcoInfo(getCapcoInfo());
            sourceDefinition.setSecurityTagsInfo(getSecurityTagsInfo());
        }

        return sourceDefinition;
    }

    public void setUploadType(InstallationType uploadTypeIn) {

        uploadType = uploadTypeIn;
    }

    public InstallationType getUploadType() {

        return uploadType;
    }

    public String getCapcoPortion() {

        CapcoInfo myCapcoInfo = getCapcoInfo();

        if (null != myCapcoInfo) {

            return myCapcoInfo.getPortion();

        } else {

            return null;
        }
    }

    public String getCapcoColumn() {

        return null;
    }

    public void setFirstRow(long firstRowIn) {

        firstRow = firstRowIn;
    }

    public long getFirstRow() {

        return firstRow;
    }

    public void setDiscardFlag(boolean discardFlagIn) {

        discardFlag = discardFlagIn;
    }

    public boolean getDiscardFlag() {

        return discardFlag;
    }

    public void setRevision(int revisionIn) {

        revision = revisionIn;
    }

    public void incrementRevision() {

        revision++;
    }

    public int getRevision() {

        return revision;
    }

    public void setRevisionList(String revisionListIn) {

        revisionList = revisionListIn;
    }

    public String getRevisionList() {

        return revisionList;
    }

    public void setColumns(List<InstalledColumn> columnListIn) {

        columns = (null != columnListIn) ? columnListIn : new ArrayList<InstalledColumn>();
    }

    public List<InstalledColumn> getColumns() {

        return columns;
    }

    public void addColumn(InstalledColumn columnIn) {

        columnIn.setOrdinal(columns.size());
        columns.add(columnIn);
    }

    public void acquire() {

        useCount++;
    }

    public boolean release() {

        return release(false);
    }

    public boolean release(boolean forceIn) {

        return ((0 >= --useCount) && (discardFlag || forceIn));
    }

    public String getColumnNameByLocalId(String localIdIn) {

        InstalledColumn myColumn = getLocalIdMap().get(localIdIn);

        return (null != myColumn) ? myColumn.getColumnName() : null;
    }

    public String getFieldNameByLocalId(String localIdIn) {

        InstalledColumn myColumn = getLocalIdMap().get(localIdIn);

        return (null != myColumn) ? myColumn.getFieldName() : null;
    }

    public InstalledColumn getColumnByLocalId(String localIdIn) {

        return (null != localIdIn) ? getLocalIdMap().get(localIdIn) : null;
    }

    public CsiDataType getDataTypeByFieldName(String nameIn) {

        InstalledColumn myColumn = (null != nameIn) ? getFieldNameMap().get(nameIn) : null;

        return (null != myColumn) ? myColumn. getType() : null;
    }

    public InstalledColumn getColumnByFieldName(String nameIn) {

        return (null != nameIn) ? getFieldNameMap().get(nameIn) : null;
    }

    public Collection<InstalledColumn> getOrderedColumns() {

        return getOrdinalMap().values();
    }

    public Resource getResource() {

        return this;
    }

    public long getNextRowId() {

        return 0L;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String versionIn) {
        version = versionIn;
    }

    public boolean getNeedsSource() {
        if (needsSource == null) {
            needsSource = false;
        }
        return needsSource;
    }

    public void setNeedsSource(boolean needsSourceIn) {
        needsSource = needsSourceIn;
    }

    public void setRowLevelCapco(boolean rowLevelCapcoIn) {

        rowLevelCapco = rowLevelCapcoIn;
    }

    public boolean getRowLevelCapco() {

        return rowLevelCapco;
    }

    public void setRowLevelTags(boolean rowLevelTagsIn) {

        rowLevelTags = rowLevelTagsIn;
    }

    public boolean getRowLevelTags() {

        return rowLevelTags;
    }
/*
    public boolean getOrderedMapping() {

        return orderedMapping;
    }

    public void setOrderedMapping(boolean orderedMappingIn) {
        orderedMapping = orderedMappingIn;
    }

    public String getFieldMap() {

        return fieldMap;
    }

    public void setFieldMap(String fieldMapIn) {

        fieldMap = fieldMapIn;
    }

    public String[][] getMappingArray() {

        String[][] myFieldMap = null;
        String[] myFirstPass = StringUtil.split(fieldMap, '\n');

        if (null != myFirstPass) {

            myFieldMap = new String[myFirstPass.length][];
            for (int i = 0; myFirstPass.length > i; i++) {

                myFieldMap[i] = StringUtil.split(myFirstPass[i]);
            }
        }
        return myFieldMap;
    }

    public List<String[]> getMappingList() {

        List<String[]> myFieldMap = null;
        String[] myFirstPass = StringUtil.split(fieldMap, '\n');

        if (null != myFirstPass) {

            myFieldMap = new ArrayList<String[]>(myFirstPass.length);
            for (int i = 0; myFirstPass.length > i; i++) {

                myFieldMap.add(StringUtil.split(myFirstPass[i]));
            }
        }
        return myFieldMap;
    }

    public void setFieldMap(String[][] fieldMapIn) {

        StringBuilder myBuffer = new StringBuilder();

        if ((null != fieldMapIn) && (0 < fieldMapIn.length)) {

            for (int i = 0; fieldMapIn.length > i; i++) {

                String[] myMapping = fieldMapIn[i];

                myBuffer.append(myMapping[0]);
                myBuffer.append('|');
                myBuffer.append(myMapping[1]);
                myBuffer.append('\n');
            }
            myBuffer.setLength(myBuffer.length() - 1);
        }
        fieldMap = (0 < myBuffer.length()) ? myBuffer.toString() : null;
    }

    public void setFieldMap(List<String[]> fieldMapIn) {

        StringBuilder myBuffer = new StringBuilder();

        if ((null != fieldMapIn) && !fieldMapIn.isEmpty()) {

            for (int i = 0; fieldMapIn.size() > i; i++) {

                String[] myMapping = fieldMapIn.get(i);

                myBuffer.append(myMapping[0]);
                myBuffer.append('|');
                myBuffer.append(myMapping[1]);
                myBuffer.append('\n');
            }
            myBuffer.setLength(myBuffer.length() - 1);
        }
        fieldMap = (0 < myBuffer.length()) ? myBuffer.toString() : null;
    }
*/
    private Integer getTableRevision(String tableNameIn) {

        if ((tableName.length() + 1) < tableNameIn.length()) {

            String myRevisionString = tableNameIn.substring(tableName.length() + 1);
            return Integer.decode(myRevisionString);

        } else if (tableName.equals(tableNameIn)) {

            return 0;
        }
        return null;
    }

    private String getVersionString() {

        return getUuid() + ":" + Integer.toString(revision);
    }

    private int extractRevisionId(String versionStringIn) {

        String[] myParts = (null != versionStringIn) ? StringUtil.split(versionStringIn, ':') : null;

        return (myParts == null) ? Integer.valueOf(-1) : Integer.decode(myParts[1]);
    }

    private Map<String, InstalledColumn> getFieldNameMap() {

        if (null == _fieldNameMap) {

            _fieldNameMap = new TreeMap<String, InstalledColumn>();

            for (InstalledColumn myColumn : getColumns()) {

                _fieldNameMap.put(myColumn.getFieldName(), myColumn);
            }
        }
        return _fieldNameMap;
    }

    private Map<String, InstalledColumn> getLocalIdMap() {

        if (null == _localIdMap) {

            _localIdMap = new TreeMap<String, InstalledColumn>();

            for (InstalledColumn myColumn : getColumns()) {

                _localIdMap.put(myColumn.getLocalId(), myColumn);
            }
        }
        return _localIdMap;
    }

    private Map<Integer, InstalledColumn> getOrdinalMap() {

        if (null == _ordinalMap) {

            _ordinalMap = new TreeMap<Integer, InstalledColumn>();

            for (InstalledColumn myColumn : getColumns()) {

                _ordinalMap.put(myColumn.getOrdinal(), myColumn);
            }
        }
        return _ordinalMap;
    }

    private Map<Integer, Integer> getRevisionMap() {

        if (null == _revisionMap) {

            _revisionMap = new TreeMap<Integer, Integer>();

            if ((null != revisionList) && (0 < revisionList.length())) {

                String[] myList = StringUtil.split(revisionList, '|');

                for (String myEntry : myList) {

                    String[] myPair = StringUtil.split(myEntry, ':');

                    _revisionMap.put(Integer.decode(myPair[0]), Integer.decode(myPair[1]));
                }
            }
        }
        return _revisionMap;
    }

    private synchronized int updateUsage(Integer keyIn, int deltaIn) {

        int myUseCount = 0;

        if (null != keyIn) {

            revision = Math.max(revision, keyIn);
            getRevisionMap();

            Integer myOldCount = _revisionMap.get(keyIn);
            myUseCount = (null != myOldCount)
                                ? Math.max(0, (myOldCount + deltaIn))
                                : Math.max(0, deltaIn);

            if (0 != deltaIn) {

                if (0 < myUseCount) {

                    _revisionMap.put(keyIn, myUseCount);

                } else {

                    _revisionMap.remove(keyIn);
                }
                generateRevisionString();
            }
        }
        return myUseCount;
    }

   private void generateRevisionString() {
      StringJoiner revisions = new StringJoiner("|");
      StringBuilder buffer = new StringBuilder();

      for (Map.Entry<Integer, Integer> entry : _revisionMap.entrySet()) {
         if (entry.getValue() > 0) {
            buffer.setLength(0);
            revisions.add(buffer.append(entry.getKey()).append(":").append(entry.getValue()).toString());
         }
      }
      revisionList = revisions.toString();
   }

    private List<ValuePair<FieldDef, FieldDef>> buildPhysicalPairs() {

        List<ValuePair<FieldDef, FieldDef>> myList = new ArrayList<>();
        Collection<InstalledColumn> myColumns = getOrderedColumns();

        for (InstalledColumn myColumn : myColumns) {

            CsiDataType myType = myColumn.getType();
            FieldDef myField = new FieldDef(myColumn.getFieldName(), FieldType.COLUMN_REF, myType);

            myField.setStorageType(myType);
            myField.setLocalId(myColumn.getColumnName().replace('_', '-'));
            myList.add(new ValuePair<FieldDef, FieldDef>(myField, myField));
        }
        return myList;
    }

    private FieldListAccess getFieldListAccess() {

        return (null != sourceDefinition) ? sourceDefinition.getFieldListAccess() : null;
    }
}
