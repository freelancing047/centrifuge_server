package csi.server.common.model.dataview;

import static javax.persistence.CascadeType.ALL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.interfaces.FieldDefSource;
import csi.server.common.interfaces.ParameterListAccess;
import csi.server.common.interfaces.SecurityAccess;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.OrphanColumn;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.util.ParameterHelper;
import csi.server.common.util.ReadOnlyList;
import csi.server.common.util.SystemParameters;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 5/9/2017.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AdHocDataSource extends ModelObject implements FieldDefSource, DataContainer, DataDefinition, ParameterListAccess, SecurityAccess {

    @OneToOne(cascade = ALL, orphanRemoval = true)
    private DataSetOp dataTree;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "dataparent_uuid")
    private List<QueryParameterDef> dataSetParameters;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "dataparent_uuid")
    private List<DataSourceDef> dataSources;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "dataparent_uuid")
    private List<OrphanColumn> orphanColumns;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "dataparent_uuid")
    private List<FieldDef> fieldDefs;

    //The numbers used to name the DataSetOp in the DataSource Editor
    private int nextJoinNumber = 1;
    private int nextAppendNumber = 1;
    private boolean sorted = false;
    private boolean storageTypesFlag = false;
    private Integer rowLimit = null;

    @Transient
    private AclResourceType _resourceType = null;
    @Transient
    private FieldListAccess _fieldAccess = null;
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
    @Transient
    private Map<String, DataSourceDef> _cloneDataSourceMap;
    @Transient
    private Map<String, SqlTableDef> _cloneSqlTableMap;
    @Transient
    private Map<String, FieldDef> _cloneFieldMap;
    @Transient
    private Map<String, Filter> _cloneFilterMap;
    @Transient
    protected CapcoInfo _capcoInfo = null;
    @Transient
    protected SecurityTagsInfo _securityTagsInfo = null;


    public AdHocDataSource() {

    }

    public AdHocDataSource(AclResourceType resourceTypeIn, DataSourceDef sourceDefIn,
                           CapcoInfo capcoInfoIn, SecurityTagsInfo securityTagsInfoIn) {

        _resourceType = resourceTypeIn;
        initializeTables(sourceDefIn);
        _capcoInfo = capcoInfoIn;
        _securityTagsInfo = securityTagsInfoIn;
    }

    public void resetTransients() {

        _fieldAccess = null;
        _parameterIdMap = null;
        _parameterNameMap = null;
        systemParameters = null;
        _systemParameterNameMap = null;
        _systemParameterIdMap = null;
        _columnByKeyMap = null;
        _cloneDataSourceMap = null;
        _cloneSqlTableMap = null;
        _cloneFieldMap = null;
        _cloneFilterMap = null;
    }

    public void setResourceType(AclResourceType resourceTypeIn) {

        _resourceType = resourceTypeIn;
    }

    public AclResourceType getResourceType() {

        return _resourceType;
    }

    public DataDefinition getDataDefinition() {

        return this;
    }

    public List<DataSourceDef> getDataSources() {
        if (dataSources == null) {
            dataSources = new ArrayList<DataSourceDef>();
        }

        return dataSources;
    }

    public SecurityAccess getSecurityAccess() {

        return this;
    }

    public List<FieldDef> getFieldList() {

        return new ReadOnlyList<FieldDef>(getFieldListAccess().getFieldDefList());
    }

    public List<QueryParameterDef> getParameterList() {

        return getDataSetParameters();
    }

    public Map<String, QueryParameterDef> initializeParameterUse() {

        return ParameterHelper.initializeParameterUse(getParameterList(), getDataTree(),
                                                        getFieldListAccess().getOrderedCaculatedFieldDefs());
    }

    public String getName() {

        return null;
    }

    public void setDataSources(List<DataSourceDef> dataSetList) {
        dataSources = dataSetList;
    }

    public ParameterListAccess getParameterListAccess() {
        return this;
    }

    public FieldListAccess getFieldListAccess() {

        if (null == _fieldAccess) {

            _fieldAccess = new FieldListAccess(this, fieldDefs);
        }
        return _fieldAccess;
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

    public List<QueryParameterDef> getOrderedParameterList(CsiDataType dataTypeIn) {

        Collection<QueryParameterDef> myListIn = getParameterNameMap().values();
        List<QueryParameterDef> myListOut = new ArrayList<QueryParameterDef>();

        for (QueryParameterDef myParameter : myListIn) {

            if (dataTypeIn == myParameter.getType()) {

                myListOut.add(myParameter);
            }
        }

        return myListOut;
    }

    public List<QueryParameterDef> getOrderedSystemParameterList(CsiDataType dataTypeIn) {

        Collection<QueryParameterDef> myListIn = getSystemParameterNameMap().values();
        List<QueryParameterDef> myListOut = new ArrayList<QueryParameterDef>();

        for (QueryParameterDef myParameter : myListIn) {

            if (dataTypeIn == myParameter.getType()) {

                myListOut.add(myParameter);
            }
        }

        return myListOut;
    }

    public List<QueryParameterDef> getOrderedFullParameterList(CsiDataType dataTypeIn) {

        Collection<QueryParameterDef> myListIn1 = getParameterNameMap().values();
        Collection<QueryParameterDef> myListIn2 = getSystemParameterNameMap().values();
        List<QueryParameterDef> myListOut = new ArrayList<QueryParameterDef>();

        for (QueryParameterDef myParameter : myListIn1) {

            if (dataTypeIn == myParameter.getType()) {

                myListOut.add(myParameter);
            }
        }

        for (QueryParameterDef myParameter : myListIn2) {

            if (dataTypeIn == myParameter.getType()) {

                myListOut.add(myParameter);
            }
        }

        return myListOut;
    }

    public List<QueryParameterDef> getOrderedParameterList() {

        Collection<QueryParameterDef> myListIn = getParameterNameMap().values();
        List<QueryParameterDef> myListOut = new ArrayList<QueryParameterDef>();

        for (QueryParameterDef myParameter : myListIn) {

            myListOut.add(myParameter);
        }

        return myListOut;
    }

    public List<QueryParameterDef> getOrderedSystemParameterList() {

        Collection<QueryParameterDef> myListIn = getSystemParameterNameMap().values();
        List<QueryParameterDef> myListOut = new ArrayList<QueryParameterDef>();

        for (QueryParameterDef myParameter : myListIn) {

            myListOut.add(myParameter);
        }

        return myListOut;
    }

    public List<QueryParameterDef> getOrderedFullParameterList() {

        Collection<QueryParameterDef> myListIn1 = getParameterNameMap().values();
        Collection<QueryParameterDef> myListIn2 = getSystemParameterNameMap().values();
        List<QueryParameterDef> myListOut = new ArrayList<QueryParameterDef>();

        for (QueryParameterDef myParameter : myListIn1) {

            myListOut.add(myParameter);
        }

        for (QueryParameterDef myParameter : myListIn2) {

            myListOut.add(myParameter);
        }

        return myListOut;
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
      Set<String> dataSourceKeys = new TreeSet<String>();

      if ((dataSources != null) && !dataSources.isEmpty()) {
         for (DataSourceDef dataSource : dataSources) {
            String key = dataSource.getDataSourceKey();

            if (null != key) {
               dataSourceKeys.add(key);
            }
         }
      }
      return dataSourceKeys;
   }

    public boolean isSorted() {

        return sorted;
    }

    public void setSorted(boolean sortedIn) {

        sorted = sortedIn;
    }

    public List<FieldDef> getFieldDefs() {

        return fieldDefs;
    }

    public void setFieldDefs(List<FieldDef> listIn) {

        fieldDefs = listIn;
        if (null != _fieldAccess) {

            _fieldAccess.resetList(this, fieldDefs);
        }
    }

    public ColumnDef getColumnByKey(String columnKeyIn) {

        ColumnDef myColumn = null;
        Map<String, ColumnDef> myMap = getColumnByKeyMap();

        if (null != myMap) {

            myColumn = myMap.get(columnKeyIn);
        }
        return myColumn;
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

    @Override
    public AdHocDataSource clone() {

        _cloneFieldMap = new HashMap<String, FieldDef>();
        _cloneFilterMap = new HashMap<String, Filter>();

        return clone(_cloneFieldMap, _cloneFilterMap);
    }

    //
    // Upon entry, "fieldMapIn" is an empty hashmap, filterMapIn is complete
    //
    @Override
    public <T extends ModelObject, S extends ModelObject> AdHocDataSource clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {

        AdHocDataSource myClone = new AdHocDataSource();

        super.cloneComponents(myClone);

        myClone.setNextJoinNumber(getNextJoinNumber());
        myClone.setNextAppendNumber(getNextAppendNumber());
        myClone.setDataSetParameters(cloneDataSetParameters(myClone));
        myClone.setDataSources(cloneDataSources(myClone, _cloneDataSourceMap, _cloneSqlTableMap, _cloneFieldMap)); // loads values into "_cloneDataSourceMap, ", "_cloneSqlTableMap" and "_cloneFieldMap"
        myClone.setDataTree(getDataTree().clone(_cloneDataSourceMap, _cloneSqlTableMap));
        myClone.setRowLimit(getRowLimit());
        myClone.setStorageTypesFlag(getStorageTypesFlag());

        myClone.setFieldDefs(cloneFieldDefs(fieldMapIn)); // loads values into "fieldMapIn"
        myClone.setSorted(isSorted());

        return myClone;
    }

    private List<DataSourceDef> cloneDataSources(AdHocDataSource cloneIn, Map<String, DataSourceDef> sourceMapIn, Map<String, SqlTableDef> tableMapIn, Map<String, FieldDef> fieldMapIn) {

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

    @Override
    public String toString() {
        return Objects.toStringHelper(this) //
                .add("dataTree", getDataTree()) //
                .add("dataSetParameters", getDataSetParameters()) //
                .add("dataSources", getDataSources()) //
                .toString();
    }

    public SecurityTagsInfo getSecurityTagsInfo() {

        return _securityTagsInfo;
    }

    public void setSecurityTagsInfo(SecurityTagsInfo securityTagsInfoIn) {

        _securityTagsInfo = securityTagsInfoIn;
    }

    public CapcoInfo getCapcoInfo() {

        return _capcoInfo;
    }

    public void setCapcoInfo(CapcoInfo capcoInfoIn) {

        _capcoInfo = capcoInfoIn;
    }

    public String getSecurityBanner(String bannerPrefix, String bannerDelimiter,
                                    String bannerSubDelimiter, String bannerSuffix, String tagItemPrefix) {

        return null;
    }

    public String getSecurityBanner(String defaultBannerIn) {

        return null;
    }

    public String getSecurityBannerAbr(String defaultBannerIn) {

        return null;
    }

    public String getSecurityBanner(String defaultBannerIn, String tagBannerIn) {

        return null;
    }

    public String getSecurityBannerAbr(String defaultBannerIn, String tagBannerIn) {

        return null;
    }

    public String getSecurityPortion() {

        return null;
    }

    public boolean hasStorageTypes() {

        return storageTypesFlag;
    }

    public boolean getStorageTypesFlag() {

        return storageTypesFlag;
    }

    public void setStorageTypesFlag(boolean valueIn) {

        storageTypesFlag = valueIn;
    }

    public void setStorageTypesFlag() {

        storageTypesFlag = true;
    }

    public void resetStorageTypesFlag() {

        storageTypesFlag = false;
    }


    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        super.debugContents(bufferIn, indentIn);
        debugObject(bufferIn, nextJoinNumber, indentIn, "nextJoinNumber");
        debugObject(bufferIn, nextAppendNumber, indentIn, "nextAppendNumber");
        doDebug(dataTree, bufferIn, indentIn, "dataTree", "DataSetOp");
        debugList(bufferIn, dataSetParameters, indentIn, "dataSetParameters");
        debugList(bufferIn, dataSources, indentIn, "dataSources");
        debugList(bufferIn, fieldDefs, indentIn, "fieldDefs");
    }

    private List<QueryParameterDef> cloneDataSetParameters(AdHocDataSource cloneIn) {

        if (null != getDataSetParameters()) {

            List<QueryParameterDef>  myList = new ArrayList<QueryParameterDef>();

            for (QueryParameterDef myItem : getDataSetParameters()) {

                QueryParameterDef myParameter = myItem.clone();

                myParameter.setParent(null);
                myList.add(myParameter);
            }

            return myList;

        } else {

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends ModelObject> List<FieldDef> cloneFieldDefs(Map<String, T> fieldMapIn) {

        if (null != getFieldDefs()) {

            List<FieldDef>  myList = new ArrayList<FieldDef>();

            for (FieldDef myItem : getFieldDefs()) {

                myList.add((FieldDef)cloneFromOrToMap(fieldMapIn, (T)myItem, fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private void initializeTables(DataSourceDef sourceDefIn) {

        dataSetParameters = new ArrayList<QueryParameterDef>();
        dataSources = new ArrayList<DataSourceDef>();
        orphanColumns = new ArrayList<OrphanColumn>();
        fieldDefs = new ArrayList<FieldDef>();
        if (null != sourceDefIn) {

            dataSources.add(sourceDefIn);
        }
    }
}
