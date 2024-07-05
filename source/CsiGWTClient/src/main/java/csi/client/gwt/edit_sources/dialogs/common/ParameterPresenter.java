package csi.client.gwt.edit_sources.dialogs.common;

import java.util.*;

import csi.client.gwt.dataview.DataSourceUtilities;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.ParameterHelper;

public class ParameterPresenter {

    List<DataSetOp> _dataList;
    List<QueryParameterDef> _parameterList;
    List<FieldDef> _fieldList;
    Map<String, QueryParameterDef> _nameMap;
    Map<String, QueryParameterDef> _idMap;

    public ParameterPresenter(List<QueryParameterDef> parametersIn,
                              List<DataSetOp> dataOpsIn, List<FieldDef> fieldListIn) {
        
        _dataList = dataOpsIn;
        _parameterList = getWorkingParameters(parametersIn);
        _fieldList = fieldListIn;
        
        buildParameterMaps();
    }
    
    public ParameterPresenter(ParameterPresenter presenterIn) {
        
        this(presenterIn.getParameters(), presenterIn.getDataList(), presenterIn.getFieldList());
    }
    
    public ParameterPresenter replaceAll(ParameterPresenter presenterIn) {
        
        _dataList = presenterIn.getDataList();
        _parameterList = presenterIn.getParameters();
        _fieldList = presenterIn.getFieldList();
        _nameMap = presenterIn.getNameMap();
        _idMap = presenterIn.getIdMap();
        
        return this;
    }

    public List<DataSetOp> getDataList()  {

        return _dataList;
    }

    public List<FieldDef> getFieldList()  {

        return _fieldList;
    }

    public List<QueryParameterDef> getParameters()  {
        
        return _parameterList;
    }
    
    public Map<String, QueryParameterDef> getNameMap()  {
        
        return _nameMap;
    }
    
    public Map<String, QueryParameterDef> getIdMap()  {
        
        return _idMap;
    }

    public QueryParameterDef getParameterByLocalId(String localIdIn) {
        
        return (null != localIdIn) ? _idMap.get(localIdIn) : null;
    }

    public void addTableItems(SqlTableDef tableIn) {

        if (null != tableIn) {

            ParameterHelper.addTableSources(_idMap, tableIn);
        }
    }

    public void removeTableItems(SqlTableDef tableIn) {

        if (null != tableIn) {

            ParameterHelper.removeTableSources(_idMap, tableIn);
        }
    }

    public void addSourceItem(String parameterIdIn) {

        QueryParameterDef myParameter = (null != parameterIdIn) ? getParameterByLocalId(parameterIdIn) : null;

        if (null != myParameter) {

            myParameter.addSourceItem();
        }
    }

    public void removeSourceItem(String parameterIdIn) {

        QueryParameterDef myParameter = (null != parameterIdIn) ? getParameterByLocalId(parameterIdIn) : null;

        if (null != myParameter) {

            myParameter.removeSourceItem();
        }
    }

    public void removeFilterItem(ColumnFilter filterIn) {

        String myParameterId = (null != filterIn) ? filterIn.getParamLocalId() : null;
        removeSourceItem(myParameterId);
    }

    public void addParameter(QueryParameterDef parameterIn) {
        
        _parameterList.add(parameterIn);
        _idMap.put(parameterIn.getLocalId(), parameterIn);
        _nameMap.put(parameterIn.getName(), parameterIn);
    }
    
    public void removeParameter(QueryParameterDef parameterIn) {
        
        String myId = parameterIn.getLocalId();
        String myName = parameterIn.getName();
        
        if (_idMap.containsKey(myId)) {
            
            _idMap.remove(myId);
        }
        
        if (_nameMap.containsKey(myName)) {
            
            _nameMap.remove(myName);
        }
        _parameterList.remove(parameterIn);
    }

    public boolean parameterHasNoUser(String localIdIn) {
        
        boolean myFlag = true;
        
        QueryParameterDef myParameter = getParameterByLocalId(localIdIn);
        
        if (null != myParameter) {
            
            myFlag = !myParameter.isInUse();
        }
        return myFlag;
    }
    
    public Map<String, QueryParameterDef> getCurrentNames() {
        
        return _nameMap;
    }
    

    private List<QueryParameterDef> getWorkingParameters(List<QueryParameterDef> listIn) {
        
        List<QueryParameterDef> myList = (null != listIn) ? listIn : new ArrayList<QueryParameterDef>();

        return DataSourceUtilities.duplicateList(myList);
    }

    private void buildParameterMaps() {
        
        _nameMap = new TreeMap<String, QueryParameterDef>();
        
        if (null == _parameterList) {
            
            _parameterList = new ArrayList<QueryParameterDef>();
        }
        if (0 < _parameterList.size()) {

            for (QueryParameterDef myParameter : _parameterList) {

                _nameMap.put(myParameter.getName(), myParameter);
            }
        }
        _idMap = ParameterHelper.initializeParameterUse(_parameterList, _dataList, _fieldList);
    }
}
