package csi.server.common.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.query.QueryParameterDef;

/**
 * Created by centrifuge on 10/20/2014.
 */
public class SourceEditorSet implements IsSerializable {

    private String _dataViewId;
    private DataSetOp _dataTree;
    private List<DataSourceDef> _sourceList;
    private List<FieldDef> _fieldList;
    private List<QueryParameterDef> _parameterList;

    public void setDataViewId(String dataViewIdIn) {

        _dataViewId = dataViewIdIn;
    }

    public String getDataViewId() {

        return _dataViewId;
    }

    public void setDataTree(DataSetOp dataTreeIn) {

        _dataTree = dataTreeIn;
    }

    public DataSetOp getDataTree() {

        return _dataTree;
    }

    public void setSourceList(List<DataSourceDef> sourceListIn) {

        _sourceList = sourceListIn;
    }

    public List<DataSourceDef> getSourceList() {

        return _sourceList;
    }

    public void setFieldList(List<FieldDef> fieldListIn) {

        _fieldList = fieldListIn;
    }

    public List<FieldDef> getFieldList() {

        return _fieldList;
    }

    public void setParameterList(List<QueryParameterDef> parameterListIn) {

        _parameterList = parameterListIn;
    }

    public List<QueryParameterDef> getParameterList() {

        return _parameterList;
    }
}
