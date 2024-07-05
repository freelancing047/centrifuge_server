package csi.server.common.enumerations;

import java.io.Serializable;

import csi.server.common.util.HasOrdinal;
import csi.shared.core.util.HasLabel;


public enum JdbcTableFilters implements Serializable, HasLabel, HasOrdinal {
    
    CATALOG("catalog", "Catalog", 0),
    SCHEMA("schema", "Schema", 1),
    TYPE("table_type", "TableType", 2),
    TABLE("table_name", "TableName", 3);

    private String _key;
    private String _label;
    private int _ordinal;

    private JdbcTableFilters(String keyIn, String labelIn, int ordinalIn) {
        _key = keyIn;
        _label = labelIn;
        _ordinal = ordinalIn;
    }

    public String getKey() {
        return _key;
    }

    public String getLabel() {
        return _label;
    }
    
    public int getOrdinal() {
        return _ordinal;
    }

    public static String toString(JdbcTableFilters type) {
        if (type == null) {
            return "";
        }
        return type.getLabel();
    }
}
