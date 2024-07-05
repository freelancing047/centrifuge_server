package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.SqlTableDef;

public class TableSelectionItemDTO implements IsSerializable {

    private String catalog;
    private String schema;
    private String type;
    private SqlTableDef tableDef;

    public TableSelectionItemDTO(){
        
    }
    
    public TableSelectionItemDTO(String catalogIn, String schemaIn, String typeIn, SqlTableDef tableDefIn) {
        super();
        this.catalog = catalogIn;
        this.schema = schemaIn;
        this.type = typeIn;
        this.tableDef = tableDefIn;
    }
    
    public TableSelectionItemDTO( SqlTableDef tableDefIn){
        tableDef = tableDefIn;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getType() {
        return type;
    }

    public SqlTableDef getTableDef() {
        return tableDef;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTableDef(SqlTableDef tableDef) {
        this.tableDef = tableDef;
    }

    public  String getName() {
        
        return tableDef.getTableName();
    }

}
