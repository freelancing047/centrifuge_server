package csi.server.common.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.FieldDef;
import csi.server.util.CacheUtil;

public class TableDataSet implements IsSerializable {

    private List<TableDataHeader> headers = new ArrayList<TableDataHeader>();
    private List<List<?>> rows = new ArrayList<List<?>>();

    public List<TableDataHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<TableDataHeader> headers) {
        this.headers = headers;
    }

    public List<List<?>> getRows() {
        return rows;
    }

    public void setRows(List<List<?>> rows) {
        this.rows = rows;
    }

    public void addRow(List<?> row) {
        rows.add(row);
    }

    public void addHeader(String col) {
        headers.add(new TableDataHeader(col, col));
    }

    public void addHeader(FieldDef field) {
        headers.add(new TableDataHeader(field.getUuid(), field.getFieldName()));
    }
}
