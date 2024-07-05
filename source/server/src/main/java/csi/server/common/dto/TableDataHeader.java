package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class TableDataHeader implements IsSerializable {

    private String colId;

    private String colName;

    public TableDataHeader() {

    }

    public TableDataHeader(String colId, String colName) {
        super();
        this.colId = colId;
        this.colName = colName;
    }

    public String getColId() {
        return colId;
    }

    public void setColId(String colId) {
        this.colId = colId;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }
}
