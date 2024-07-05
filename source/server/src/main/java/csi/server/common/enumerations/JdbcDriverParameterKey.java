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


public enum JdbcDriverParameterKey implements Serializable, HasLabel, HasOrdinal {

    HOSTNAME("csi.hostname", "csi.hostname", 0),
    PORT("csi.port", "csi.port", 1),
    RUNTIME_USERNAME("csi.runtime.username", "csi.runtime.username", 2),
    RUNTIME_PASSWORD("csi.runtime.password", "csi.runtime.password", 3),
    USERNAME("csi.username", "csi.username", 4),
    PASSWORD("csi.password", "csi.password", 5),
    INSTANCENAME("csi.instancename", "csi.instanceName", 6),
    DATABASENAME("csi.databasename", "csi.databaseName", 7),
    PARAMS_PREFIX("csi.params", "csi.params", 8),
    SCHEMA_USEEXISTING("csi.schema.useexisting", "csi.schema.useExisting", 9),
    SCHEMA_HASHEADERS("csi.schema.firstrowheaders", "csi.schema.firstRowHeaders", 10),
    SCHEMA_ROWDELIM("csi.schema.rowdelim", "csi.schema.rowDelim", 11),
    SCHEMA_CELLDELIM("csi.schema.celldelim", "csi.schema.cellDelim", 12),
    SCHEMA_TABLENAME("csi.schema.tablename", "csi.schema.tableName", 13),
    SCHEMA_XPATH("csi.schema.xpath", "csi.schema.xpath", 14),
    SCHEMA_COLUMNS("csi.schema.columns", "csi.schema.columns", 15),
    SCHEMA_CHARSET("csi.schema.charset", "csi.schema.charset", 16),
    SCHEMA_DATE_FORMAT("csi.schema.dateformat", "csi.schema.dateFormat", 17),
    SCHEMA_NAMESPACE_PREFIX("csi.schema.namespace", "csi.schema.namespace", 18),
    FILETOKEN("csi.filetoken", "csi.filetoken", 19),
    LOCALFILEPATH("csi.localFilepath", "csi.localFilePath", 20),
    REMOTEFILEPATH("csi.remotefilepath", "csi.remoteFilePath", 21),
    FILEPATH("csi.filepath", "csi.filePath", 22),
    QUERY_TABLE_NAME("query.tablename", "query.tableName", 23),
    PRE_QUERY("prequery", "preQuery", 24),
    POST_QUERY("postquery", "postQuery", 25),
    LEGACY_CONNECTION_STRING("csilegacyconnectionstring", "csiLegacyConnectionString", 26),
    ESCAPED_TEXT("escapedtext", "escapedText", 27),
    INPLACE("inplace", "inPlace", 28),
    DISTINCT_SOURCES("distinctsources", "distinctSources", 29),
    UNSUPPORTED("unsupported", "Unsupported", 30),
    SIMPLE_LOADER("simpleloader", "simpleLoader", 31);

    private String _key;
    private String _label;
    private int _ordinal;
    private static List<JdbcDriverParameterKey> sortedDataTypes = new ArrayList<JdbcDriverParameterKey>();

    private JdbcDriverParameterKey(String keyIn, String labelIn, int ordinalIn) {
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

    private static Map<String, JdbcDriverParameterKey> codeToEnumMapping = new HashMap<String, JdbcDriverParameterKey>();

    static {
        for (JdbcDriverParameterKey e : values()) {
            codeToEnumMapping.put(e._key, e);
            sortedDataTypes.add(e);
        }
        Collections.sort(sortedDataTypes, new Comparator<JdbcDriverParameterKey>() {

            @Override
            public int compare(JdbcDriverParameterKey o1, JdbcDriverParameterKey o2) {
                return o1.getLabel().compareTo(o2.getLabel());
    }
        });
    }

    public static JdbcDriverParameterKey getValue(String s) {
        if (s == null) {
            return null;
        }

        JdbcDriverParameterKey type = codeToEnumMapping.get(s.toLowerCase());

        if (type == null) {
            type = JdbcDriverParameterKey.UNSUPPORTED;
        }

        return type;
    }

    public static String toString(JdbcDriverParameterKey type) {
        if (type == null) {
            return "";
        }
        return type.getLabel();
    }

    public static List<JdbcDriverParameterKey> sortedValuesByLabel() {
        return sortedDataTypes;
    }
}
