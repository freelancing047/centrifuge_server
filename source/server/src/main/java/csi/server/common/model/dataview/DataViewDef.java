package csi.server.common.model.dataview;

import static javax.persistence.CascadeType.ALL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PreRemove;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;

import csi.security.monitors.ResourceACLMonitor;
import csi.server.common.dto.CreateKmlRequest;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.interfaces.ParameterListAccess;
import csi.server.common.interfaces.SecurityAccess;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.Resource;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.OrphanColumn;
import csi.server.common.model.extension.ExtensionData;
import csi.server.common.model.extension.SimpleExtension;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.ParameterHelper;
import csi.server.common.util.ReadOnlyList;
import csi.server.common.util.StringUtil;
import csi.server.common.util.SystemParameters;
import csi.server.common.util.ValuePair;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners({ResourceACLMonitor.class})
public class DataViewDef extends Resource implements DataDefinition, ParameterListAccess, SecurityAccess, DataContainer {

    private String version = null;

    @OneToOne(cascade = ALL, orphanRemoval = true)
    private DataSetOp dataTree;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parent")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    private List<QueryParameterDef> dataSetParameters;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "parent_uuid")
    private List<LinkupMapDef> linkupDefinitions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "parent_uuid")
    private List<DataSourceDef> dataSources;

    //TODO: ordinal should not be needed
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    private List<Filter> filters;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DataModelDef modelDef;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "parent_uuid")
    private List<SimpleExtension> extensionConfigs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "parent_uuid")
    private List<ExtensionData> extensionData;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "parent_uuid")
    private List<OrphanColumn> orphanColumns;

    //The numbers used to name the DataSetOp in the DataSource Editor
    private int nextJoinNumber = 1;
    private int nextAppendNumber = 1;
    private boolean storageTypesFlag = false;
    private Integer rowLimit = null;
    @Column(columnDefinition = "TEXT")
    protected String dataKeys = null;           // list of data source keys to be returned as source data keys

    @Transient
    private Map<String, DataSourceDef> _cloneDataSourceMap;

    @Transient
    private Map<String, SqlTableDef> _cloneSqlTableMap;

    @Transient
    private Map<String, FieldDef> _cloneFieldMap;

    @Transient
    private Map<String, Filter> _cloneFilterMap;

    @Transient
    private Map<String, LinkupMapDef> _linkupMap;

    @Transient
    private Map<String, QueryParameterDef> _parameterIdMap;

    @Transient
    private Map<String, QueryParameterDef> _parameterNameMap;

    @Transient
    private List<QueryParameterDef> systemParameters;

    @Transient
    private Map<String, QueryParameterDef> _systemParameterNameMap;

    @Transient
    private Map<String, QueryParameterDef> _systemParameterIdMap;

    @Transient
    private Map<String, ColumnDef> _columnByKeyMap;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private CreateKmlRequest kmlRequest;

    public DataViewDef() {
        super(AclResourceType.TEMPLATE);
        version = ReleaseInfo.version;
        this.filters = new ArrayList<Filter>();
    }

    public DataViewDef(String versionIn) {
        this();
        version = versionIn;
    }

    public List<String> getCoreFieldIds() {

        return getFieldListAccess().getCoreFieldIds();
    }

    public List<String> getFieldIds() {

        return getFieldListAccess().getFieldIds();
    }

   public List<String> getCapcoColumnNames() {
      List<String> columnNames = Collections.emptyList();

      if (capcoInfo != null) {
         columnNames = new ArrayList<>();
         List<String> keys = capcoInfo.getSecurityFields();

         if ((null != keys) && !keys.isEmpty()) {
            FieldListAccess fieldAccess = getFieldListAccess();

            for (String key : keys) {
               FieldDef field = fieldAccess.getFieldDefByAnyKey(key);

               if (field != null) {
                  columnNames.add(field.getLocalId().replace('-', '_'));
               }
            }
         }
      }
      return columnNames.isEmpty() ? null : columnNames;
   }

   public List<String> getTagColumnNames() {
      List<String> columnNames = Collections.emptyList();

      if (securityTagsInfo != null) {
         columnNames = new ArrayList<>();
         List<String> keys = securityTagsInfo.getColumnList();

         if ((keys != null) && !keys.isEmpty()) {
            FieldListAccess myFieldAccess = getFieldListAccess();

            for (String key : keys) {
               FieldDef field = myFieldAccess.getFieldDefByAnyKey(key);

               if (field != null) {
                  columnNames.add(field.getLocalId().replace('-', '_'));
               }
            }
         }
      }
      return columnNames.isEmpty() ? null : columnNames;
   }

   public Map<String,FieldDef> getCapcoColumnMap() {
      Map<String,FieldDef> capcoColumnMap = Collections.emptyMap();
      List<String> securityFields = (capcoInfo == null) ? null : capcoInfo.getSecurityFields();

      if ((securityFields != null) && !securityFields.isEmpty()) {
         capcoColumnMap = new HashMap<String,FieldDef>();
         Set<String> uniqueSecurityFields = new HashSet<String>(securityFields);
         FieldListAccess fieldAccess = getFieldListAccess();

         for (String key : uniqueSecurityFields) {
            FieldDef field = fieldAccess.getFieldDefByAnyKey(key);

            if (field != null) {
               capcoColumnMap.put(field.getLocalId(), field);
            }
         }
      }
      return capcoColumnMap.isEmpty() ? null : capcoColumnMap;
   }

   public Map<String,FieldDef> getTagColumnMap() {
      Map<String,FieldDef> tagColumnMap = Collections.emptyMap();
      Set<String> columnSet = (securityTagsInfo == null) ? null : securityTagsInfo.getColumnSet();

      if ((columnSet != null) && !columnSet.isEmpty()) {
         tagColumnMap = new HashMap<String,FieldDef>();
         FieldListAccess fieldAccess = getFieldListAccess();

         for (String key : columnSet) {
            FieldDef field = fieldAccess.getFieldDefByAnyKey(key);

            if (field != null) {
               tagColumnMap.put(field.getLocalId(), field);
            }
         }
      }
      return tagColumnMap.isEmpty() ? null : tagColumnMap;
   }

    public List<ValuePair<FieldDef, FieldDef>> getPhysicalPairs() {

        return getFieldListAccess().getPhysicalPairs();
    }

    public DataDefinition getDataDefinition() {

        return this;
    }

    public SecurityAccess getSecurityAccess() {

        return this;
    }

    public List<FieldDef> getFieldList() {

        return new ReadOnlyList<FieldDef>(modelDef.getFieldList());
    }

    public List<QueryParameterDef> getParameterList() {

        return getParameterListAccess().getDataSetParameters();
    }

    public Map<String, QueryParameterDef> initializeParameterUse() {

        return ParameterHelper.initializeParameterUse(getParameterList(), getDataTree(),
                                                        getFieldListAccess().getOrderedCaculatedFieldDefs());
    }

    public void resetTransients() {

        _cloneDataSourceMap = null;
        _cloneSqlTableMap = null;
        _cloneFieldMap = null;
        _cloneFilterMap = null;
        _linkupMap = null;
        _parameterIdMap = null;
        _parameterNameMap = null;
        systemParameters = null;
        _systemParameterNameMap = null;
        _systemParameterIdMap = null;
        _columnByKeyMap = null;

        if (null != modelDef) {

            modelDef.resetTransients();
        }
    }

    public boolean isValidVersion() {

        return ReleaseInfo.version.equals(version);
    }

    public void setVersion(String versionIn) {

        version = versionIn;
    }

    public String getVersion() {

        return version;
    }

    public List<SimpleExtension> getExtensionConfigs() {
        if (extensionConfigs == null) {
            extensionConfigs = new ArrayList<SimpleExtension>();
        }
        return extensionConfigs;
    }

    public void setExtensionConfigs(List<SimpleExtension> extensions) {
        extensionConfigs = extensions;
    }

    public List<ExtensionData> getExtensionData() {
        if (extensionData == null) {
            extensionData = new ArrayList<ExtensionData>();
        }
        return extensionData;
    }

    public void setExtensionData(List<ExtensionData> extensionData) {
        this.extensionData = extensionData;
    }

    public List<DataSourceDef> getDataSources() {
        if (dataSources == null) {
            dataSources = new ArrayList<DataSourceDef>();
        }

        return dataSources;
    }

    public void setDataSources(List<DataSourceDef> dataSetList) {
        dataSources = dataSetList;
    }

    public ParameterListAccess getParameterListAccess() {
        return this;
    }

    public FieldListAccess getFieldListAccess() {

        return (null != modelDef) ? modelDef.getFieldListAccess() : null;
    }

    public FieldListAccess refreshFieldListAccess() {

        return (null != modelDef) ? modelDef.refreshFieldListAccess() : null;
    }

    public DataModelDef getModelDef() {
        return modelDef;
    }

    public void setModelDef(DataModelDef modelDef) {
        this.modelDef = modelDef;
    }

    public void clearTransientValues() {
        clearRuntimeValues(true);
    }

    public void clearAllRuntimeValues() {
        clearRuntimeValues(false);
    }

    public void clearRuntimeValues(boolean transientOnly) {
        for (DataSourceDef dsdef : getDataSources()) {
            ConnectionDef conn = dsdef.getConnection();
            conn.clearRuntimeValues();
        }
        for (QueryParameterDef myParameter : dataSetParameters) {
            myParameter.getValues().clear();
        }
        for (LinkupMapDef myLinkup : linkupDefinitions) {
            myLinkup.setUseCount(0);
        }
    }

    public DataSetOp getDataTree() {

        return dataTree;
    }

    public void setDataTree(DataSetOp dataTreeIn) {
        this.dataTree = dataTreeIn;
    }

    public Integer getRowLimit() {

        return rowLimit;
    }

    public void setRowLimit(Integer rowLimitIn) {

        rowLimit = rowLimitIn;
    }

    public List<QueryParameterDef> getDataSetParameters() {
        if (dataSetParameters == null) {
            dataSetParameters = new ArrayList<QueryParameterDef>();
        }

            return dataSetParameters;
    }

   public List<QueryParameterDef> getSystemParameters() {
      if (systemParameters == null) {
         systemParameters = SystemParameters.getList();
      }
      return systemParameters;
   }

    public void setDataSetParameters(List<QueryParameterDef> dataSetParametersIn) {

        dataSetParameters = new ArrayList<QueryParameterDef>();

        if (null != dataSetParametersIn) {

            for (QueryParameterDef myParameter : dataSetParametersIn) {

                if (!myParameter.isSystemParam()) {

                    dataSetParameters.add(myParameter);
                }
            }
        }
        _parameterIdMap = null;
        _parameterNameMap = null;
    }

    @PreRemove
    public void cleanupRefs() {
        getDataSources().clear();
        getDataSetParameters().clear();
        dataTree.clear();
    }

    public List<LinkupMapDef> getLinkupDefinitions() {
        if (linkupDefinitions == null) {
            linkupDefinitions = new ArrayList<LinkupMapDef>();
        }
        return linkupDefinitions;
    }

    public void setLinkupDefinitions(List<LinkupMapDef> linkupDefinitions) {
        this.linkupDefinitions = linkupDefinitions;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public List<OrphanColumn> getOrphanColumns() {
        return orphanColumns;
    }

    public void setOrphanColumns(List<OrphanColumn> orphanColumnsIn) {
        orphanColumns = orphanColumnsIn;
    }


    public int getNextJoinNumber() {
        return nextJoinNumber;
    }

    public void setNextJoinNumber(int nextJoinNumberIn) {
        this.nextJoinNumber = nextJoinNumberIn;
    }

    public int getNextAppendNumber() {
        return nextAppendNumber;
    }

    public void setNextAppendNumber(int nextAppendNumberIn) {
        this.nextAppendNumber = nextAppendNumberIn;
    }

    public int getAndIncrementNextJoinNumber() {
        return nextJoinNumber++;
    }

    public int getAndIncrementNextAppendNumber() {
        return nextAppendNumber++;
    }

   public List<ValuePair<FieldDef,ColumnDef>> getColumnReferenceFieldDefs() {
      List<ValuePair<FieldDef,ColumnDef>> list = new ArrayList<ValuePair<FieldDef,ColumnDef>>();
      Map<String,ColumnDef> myMap = getColumnByKeyMap();

      for (FieldDef myField : getFieldList()) {
         if (FieldType.COLUMN_REF == myField.getFieldType()) {
            String myKey = myField.getColumnKey();
            ColumnDef myColumn = (myKey == null) ? null : myMap.get(myKey);

            list.add(new ValuePair<FieldDef,ColumnDef>(myField, myColumn));
         }
      }
      return list;
   }

    public Map<String, ColumnDef> getColumnByKeyMap() {

        if (null == _columnByKeyMap) {

            _columnByKeyMap = new HashMap<String, ColumnDef>();

            for (DataSetOp myDso = dataTree.getFirstOp(); null != myDso; myDso = myDso.getNextOp()) {

                SqlTableDef myTable = myDso.getTableDef();

                if (null != myTable) {

                    for (ColumnDef myColumn : myTable.getColumns()) {

                        _columnByKeyMap.put(myColumn.getColumnKey(), myColumn);
                    }
                }
            }
        }
        return _columnByKeyMap;
    }

    public void resetColumnByKeyMap() {

        _columnByKeyMap = null;
    }

    public ColumnDef getColumnByKey(String columnKeyIn) {

        ColumnDef myColumn = null;
        Map<String, ColumnDef> myMap = getColumnByKeyMap();

        if (null != myMap) {

            myColumn = myMap.get(columnKeyIn);
        }
        return myColumn;
    }

    //
    // Clones all the components except for the linkups
    //
    @Override
    public DataViewDef clone() {

        DataViewDef myClone = new DataViewDef();

        super.cloneComponents(myClone);

        _cloneDataSourceMap = new HashMap<String, DataSourceDef>();
        _cloneSqlTableMap = new HashMap<String, SqlTableDef>();
        _cloneFieldMap = new HashMap<String, FieldDef>();
        _cloneFilterMap = new HashMap<String, Filter>();

        myClone.setVersion(getVersion());
        myClone.setNextJoinNumber(getNextJoinNumber());
        myClone.setNextAppendNumber(getNextAppendNumber());
        myClone.setDataSetParameters(cloneDataSetParameters(myClone));
        myClone.setExtensionConfigs(cloneExtensionConfigs(_cloneFieldMap));
        myClone.setExtensionData(cloneExtensionData());
        myClone.setFilters(cloneFilters(_cloneFilterMap, _cloneFieldMap)); // loads values into "_cloneFilterMap"

        if (null != getModelDef()) {
            myClone.setModelDef(getModelDef().clone(_cloneFieldMap, _cloneFilterMap));
        }
        myClone.setDataSources(cloneDataSources(myClone, _cloneDataSourceMap, _cloneSqlTableMap, _cloneFieldMap)); // loads values into "_cloneDataSourceMap, ", "_cloneSqlTableMap" and "_cloneFieldMap"
        myClone.setDataTree(getDataTree().clone(_cloneDataSourceMap, _cloneSqlTableMap));
        myClone.setRowLimit(getRowLimit());

        return myClone;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("dataTree", getDataTree()) //
                .add("dataSetParameters", getDataSetParameters()) //
                .add("dataSources", getDataSources()) //
                .add("template", isTemplate()) //
                .toString();
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        super.debugContents(bufferIn, indentIn);
        debugObject(bufferIn, version, indentIn, "version");
        debugObject(bufferIn, nextJoinNumber, indentIn, "nextJoinNumber");
        debugObject(bufferIn, nextAppendNumber, indentIn, "nextAppendNumber");
        doDebug(modelDef, bufferIn, indentIn, "modelDef", "DataModelDef");
        doDebug(dataTree, bufferIn, indentIn, "dataTree", "DataSetOp");
        debugList(bufferIn, dataSetParameters, indentIn, "dataSetParameters");
        debugList(bufferIn, linkupDefinitions, indentIn, "linkupDefinitions");
        debugList(bufferIn, dataSources, indentIn, "dataSources");
        debugList(bufferIn, extensionConfigs, indentIn, "extensionConfigs");
        debugList(bufferIn, extensionData, indentIn, "extensionData");
    }

    private List<QueryParameterDef> cloneDataSetParameters(DataViewDef cloneIn) {

        if (null != getDataSetParameters()) {

            List<QueryParameterDef>  myList = new ArrayList<QueryParameterDef>();

            for (QueryParameterDef myItem : getDataSetParameters()) {

                QueryParameterDef myParameter = myItem.clone();

                myParameter.setParent(cloneIn);
                myList.add(myParameter);
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<DataSourceDef> cloneDataSources(DataViewDef cloneIn, Map<String, DataSourceDef> sourceMapIn, Map<String, SqlTableDef> tableMapIn, Map<String, FieldDef> fieldMapIn) {

        if (null != getDataSources()) {

            List<DataSourceDef>  myList = new ArrayList<DataSourceDef>();

            for (DataSourceDef myItem : getDataSources()) {

                DataSourceDef mySource = cloneFromOrToMap(sourceMapIn, myItem, sourceMapIn, tableMapIn, fieldMapIn);

                myList.add(mySource);
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<SimpleExtension> cloneExtensionConfigs(Map<String, FieldDef> fieldMapIn) {

        if (null != getExtensionConfigs()) {

            List<SimpleExtension>  myList = new ArrayList<SimpleExtension>();

            for (SimpleExtension myItem : getExtensionConfigs()) {

                myList.add(myItem.clone(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<ExtensionData> cloneExtensionData() {

        if (null != getExtensionData()) {

            List<ExtensionData>  myList = new ArrayList<ExtensionData>();

            for (ExtensionData myItem : getExtensionData()) {

                myList.add(myItem.clone());
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<Filter> cloneFilters(Map<String, Filter> filterMapIn,  Map<String, FieldDef> fieldMapIn) {

        if (null != getFilters()) {

            List<Filter>  myList = new ArrayList<Filter>();

            for (Filter myItem : getFilters()) {

                myList.add(cloneFromOrToMap(filterMapIn, myItem, fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    public void setLastKmlRequest(CreateKmlRequest request) {
        this.kmlRequest = request;
    }

    public CreateKmlRequest getKmlRequest() {
        return kmlRequest;
    }

    public void setKmlRequest(CreateKmlRequest kmlRequest) {
        this.kmlRequest = kmlRequest;
    }

    public void removeLinkup(LinkupMapDef linkupIn) {

        if (null != linkupIn) {

            removeLinkup(getLinkupMap(), linkupIn);
        }
    }

    public void addLinkup(LinkupMapDef linkupIn) {

        if (null != linkupIn) {

            Map<String, LinkupMapDef> myMap = getLinkupMap();

            removeLinkup(myMap, linkupIn);
            linkupDefinitions.add(linkupIn);
            myMap.put(linkupIn.getUuid(), linkupIn);
        }
    }

    public LinkupMapDef getLinkup(String uuidIn) {

        return getLinkupMap().get(uuidIn);
    }

    public Map<String, LinkupMapDef> getLinkupMap() {

        if (null == _linkupMap) {

            _linkupMap = new TreeMap<String, LinkupMapDef>();

            for (LinkupMapDef item : linkupDefinitions) {

                _linkupMap.put(item.getUuid(), item);
            }
        }
        return _linkupMap;
    }

    public void addParameter(QueryParameterDef parameterIn) {

        Map<String, QueryParameterDef> myIdMap = getParameterIdMap();
        String myKey = parameterIn.getLocalId();

        if (myIdMap.containsKey(myKey)) {

            updateParameter(parameterIn);

        } else {

            dataSetParameters.add(parameterIn);
            myIdMap.put(myKey, parameterIn);

            if (null != _parameterNameMap) {

                _parameterNameMap.put(parameterIn.getName(), parameterIn);
            }
        }
    }

    public void deleteParameter(QueryParameterDef parameterIn) {

        Map<String, QueryParameterDef> myIdMap = getParameterIdMap();
        String myKey = parameterIn.getLocalId();

        if (myIdMap.containsKey(myKey)) {

            dataSetParameters.remove(parameterIn);
            myIdMap.remove(myKey);

            if (null != _parameterNameMap) {

                _parameterNameMap.remove(parameterIn.getName());
            }
        }
    }

    public void updateParameter(QueryParameterDef parameterIn) {

        Map<String, QueryParameterDef> myIdMap = getParameterIdMap();
        String myKey = parameterIn.getLocalId();

        if (myIdMap.containsKey(parameterIn.getLocalId())) {

            QueryParameterDef myParameter = myIdMap.get(myKey);

            if ((null != _parameterNameMap) && (!myParameter.getName().equals(parameterIn.getName()))) {

                _parameterNameMap.remove(myParameter.getName());
                _parameterNameMap.put(parameterIn.getName(), parameterIn);
            }

            parameterIn.updateInPlace(myParameter);
        }
    }

   public List<QueryParameterDef> getOrderedParameterList(CsiDataType dataType) {
      List<QueryParameterDef> results = new ArrayList<QueryParameterDef>();

      for (Map.Entry<String,QueryParameterDef> entry : getParameterNameMap().entrySet()) {
         QueryParameterDef paramDef = entry.getValue();

         if (dataType == paramDef.getType()) {
            results.add(paramDef);
         }
      }
      return results;
   }

   public List<QueryParameterDef> getOrderedSystemParameterList(CsiDataType dataType) {
      List<QueryParameterDef> results = new ArrayList<QueryParameterDef>();

      for (Map.Entry<String,QueryParameterDef> entry : getSystemParameterNameMap().entrySet()) {
         QueryParameterDef paramDef = entry.getValue();

         if (dataType == paramDef.getType()) {
            results.add(paramDef);
         }
      }
      return results;
   }

   public List<QueryParameterDef> getOrderedFullParameterList(CsiDataType dataType) {
      List<QueryParameterDef> results = new ArrayList<QueryParameterDef>();

      for (Map.Entry<String,QueryParameterDef> entry : getParameterNameMap().entrySet()) {
         QueryParameterDef paramDef = entry.getValue();

         if (dataType == paramDef.getType()) {
            results.add(paramDef);
         }
      }
      for (Map.Entry<String,QueryParameterDef> entry : getSystemParameterNameMap().entrySet()) {
         QueryParameterDef paramDef = entry.getValue();

         if (dataType == paramDef.getType()) {
            results.add(paramDef);
         }
      }
      return results;
   }

   public List<QueryParameterDef> getOrderedParameterList() {
      List<QueryParameterDef> results = new ArrayList<QueryParameterDef>();

      for (Map.Entry<String,QueryParameterDef> entry : getParameterNameMap().entrySet()) {
         results.add(entry.getValue());
      }
      return results;
   }

   public List<QueryParameterDef> getOrderedSystemParameterList() {
      List<QueryParameterDef> results = new ArrayList<QueryParameterDef>();

      for (Map.Entry<String,QueryParameterDef> entry : getSystemParameterNameMap().entrySet()) {
         results.add(entry.getValue());
      }
      return results;
   }

   public List<QueryParameterDef> getOrderedFullParameterList() {
      List<QueryParameterDef> results = new ArrayList<QueryParameterDef>();

      for (Map.Entry<String,QueryParameterDef> entry : getParameterNameMap().entrySet()) {
         results.add(entry.getValue());
      }
      for (Map.Entry<String,QueryParameterDef> entry : getSystemParameterNameMap().entrySet()) {
         results.add(entry.getValue());
      }
      return results;
   }

    public void refreshParameters() {

        _parameterIdMap = null;
        _parameterNameMap = null;
    }

    public Map<String, QueryParameterDef> getParameterIdMap() {

        if (null == _parameterIdMap) {

            List<QueryParameterDef> myList = getDataSetParameters();

            _parameterIdMap = new TreeMap<String, QueryParameterDef>();

            if (null != myList) {

                for (QueryParameterDef myParameter : myList) {

                    _parameterIdMap.put(myParameter.getLocalId(), myParameter);
                }
            }
        }
        return _parameterIdMap;
    }

    public Map<String, QueryParameterDef> getParameterNameMap() {

        if (null == _parameterNameMap) {

            List<QueryParameterDef> myList = getDataSetParameters();

            _parameterNameMap = new TreeMap<String, QueryParameterDef>();

            if (null != myList) {

                for (QueryParameterDef myParameter : myList) {

                    _parameterNameMap.put(myParameter.getName(), myParameter);
                }
            }
        }
        return _parameterNameMap;
    }

    public Map<String, QueryParameterDef> getSystemParameterNameMap() {

        if (null == _systemParameterNameMap) {

            List<QueryParameterDef> myList = getSystemParameters();

            _systemParameterNameMap = new TreeMap<String, QueryParameterDef>();

            if (null != myList) {

                for (QueryParameterDef myParameter : myList) {

                    _systemParameterNameMap.put(myParameter.getName(), myParameter);
                }
            }
        }
        return _systemParameterNameMap;
    }

    public Map<String, QueryParameterDef> getSystemParameterIdMap() {

        if (null == _systemParameterIdMap) {

            List<QueryParameterDef> myList = getSystemParameters();

            _systemParameterIdMap = new TreeMap<String, QueryParameterDef>();

            if (null != myList) {

                for (QueryParameterDef myParameter : myList) {

                    _systemParameterIdMap.put(myParameter.getLocalId(), myParameter);
                }
            }
        }
        return _systemParameterIdMap;
    }

    public List<QueryParameterDef> getRequiredParameters() {

        return ParameterHelper.filter(getDataSetParameters());
    }

    public QueryParameterDef getParameterById(String idIn) {

        QueryParameterDef myParameter = getParameterIdMap().get(idIn);

        if (null == myParameter) {

            myParameter = getSystemParameterIdMap().get(idIn);
        }

        return myParameter;
    }

    public QueryParameterDef getParameterByName(String nameIn) {

        QueryParameterDef myParameter = getParameterNameMap().get(nameIn);

        if (null == myParameter) {

            myParameter = getSystemParameterNameMap().get(nameIn);
        }

        return myParameter;
    }

    @Override
    public Set<String> getDataSourceKeySet() {

        Set<String> myKeySet = new TreeSet<String>();

        if (null != dataKeys) {

            myKeySet.addAll(Arrays.asList(StringUtil.split(dataKeys, '\t')));

        } else {

            if (dataSources != null) {

                for (DataSourceDef mySource : dataSources) {

                    String myKey = mySource.getDataSourceKey();

                    if (null != myKey) {

                        myKeySet.add(myKey);
                    }
                }
            }
        }
        return myKeySet;
    }

    public void lockDataKeys(Set<String> keySetIn) {

        dataKeys = StringUtil.concatInput(keySetIn, '\t');
    }

    public boolean hasStorageTypes() {

        return storageTypesFlag;
    }

    public boolean getStorageTypesFlag() {

        return storageTypesFlag;
    }

    public void clearStorageTypesFlag() {

        storageTypesFlag = false;
    }

    public void setStorageTypesFlag() {

        storageTypesFlag = true;
    }

    private void removeLinkup(Map<String, LinkupMapDef> mapIn, LinkupMapDef linkupIn) {

        LinkupMapDef myItem = mapIn.get(linkupIn.getUuid());

        if (null != myItem) {

            linkupDefinitions.remove(myItem);
            mapIn.remove(myItem.getUuid());
        }
    }
}
