package csi.server.common.dto.installed_tables;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 12/28/2015.
 */
public class SheetFormat implements IsSerializable {

    Integer _columnNamesRow;
    Integer _firstDataRow;
    CsiDataType[] _dataTypeList;

    public SheetFormat() {
    
    }

    public SheetFormat(Integer columnNamesRowIn, Integer firstDataRowIn, CsiDataType[] dataTypeListIn) {
        
        _columnNamesRow = columnNamesRowIn;
        _firstDataRow = firstDataRowIn;
        _dataTypeList = dataTypeListIn;
    }

    public void setColumnNamesRow(Integer columnNamesRowIn) {

        _columnNamesRow = columnNamesRowIn;
    }

    public Integer getColumnNamesRow() {

        return _columnNamesRow;
    }

    public void setFirstDataRow(Integer firstDataRowIn) {

        _firstDataRow = firstDataRowIn;
    }

    public Integer getFirstDataRow() {

        return _firstDataRow;
    }

    public void setDataTypeList(CsiDataType[] dataTypeListIn) {

        _dataTypeList = dataTypeListIn;
    }

    public CsiDataType[] getDataTypeList() {

        return _dataTypeList;
    }
}
