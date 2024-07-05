package csi.server.common.enumerations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.server.common.util.HasOrdinal;
import csi.shared.core.util.HasLabel;


public enum JdbcDriverParameterType implements Serializable, HasLabel, HasOrdinal {

    SELECT_LIST("selectlist", "selectList", 0),
    PASSWORD("password", "password", 1),
    STRING("string", "string", 2),
    BOOLEAN("boolean", "boolean", 3),
    FILE("file", "file", 4),
    QUERY_COMMANDS("csiquerycommands", "csiQueryCommands", 5),
    PARAMETERS_TABLE("csiparmstable", "csiParmsTable", 6),
    XML_NAMESPACE("csinamespacetable", "csiNamespaceTable", 7),
    XML_COLUMNS_TABLE("csixmlcolumnstable", "csiXMLColumnsTable", 8),
    LEGACY_CONNECTION_STRING("csilegacyconnectionstring", "csiLegacyConnectionString", 9),
    UNSUPPORTED("unsupported", "Unsupported", 10);

    private String _key;
    private String _label;
    private int _ordinal;
    private static List<JdbcDriverParameterType> sortedDataTypes = new ArrayList<JdbcDriverParameterType>();

    private JdbcDriverParameterType(String keyIn, String labelIn, int ordinalIn) {
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

    private static Map<String, JdbcDriverParameterType> codeToEnumMapping = new HashMap<String, JdbcDriverParameterType>();

    static {
        for (JdbcDriverParameterType e : values()) {
            codeToEnumMapping.put(e._key, e);
            sortedDataTypes.add(e);
        }
        Collections.sort(sortedDataTypes, new Comparator<JdbcDriverParameterType>() {

            @Override
            public int compare(JdbcDriverParameterType o1, JdbcDriverParameterType o2) {
                return o1.getLabel().compareTo(o2.getLabel());
    }
        });
    }

    public static JdbcDriverParameterType getValue(String s) {
        if (s == null) {
            return null;
        }

        JdbcDriverParameterType type = codeToEnumMapping.get(s.toLowerCase());

        if (type == null) {
            type = JdbcDriverParameterType.UNSUPPORTED;
        }

        return type;
    }

    public static String toString(JdbcDriverParameterType type) {
        if (type == null) {
            return "";
        }
        return type.getLabel();
    }

    public static List<JdbcDriverParameterType> sortedValuesByLabel() {
        return sortedDataTypes;
    }
}
