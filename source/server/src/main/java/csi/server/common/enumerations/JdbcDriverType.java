package csi.server.common.enumerations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.server.common.util.HasOrdinal;
import csi.shared.core.util.HasLabel;
/* TODO: generate images for the following drivers
admintools
anx
cache
datafile
installedtabledriver
jsonfile
jsonWeb
remotetabledriver
sqlserver (jtds)
 */

public enum JdbcDriverType implements HasLabel, HasOrdinal {

    ACCESS("access", "selectList", 0),
    CUSTOM("legacy", "Custom JDBC", 1),
    EXCEL("excel", "Excel", 2),
    IMPALA("impala", "Impala", 3),
    LDAP("ldap", "LDAP", 4),
    ORACLE("oracle", "Oracle Thin", 5),
    POSTGRESS("postgresql", "PostgreSQL", 6),
    SQLSERVER("sqlserver", "SQL Server", 7), // needs to work with "sqlserver (jtds)"
    TEXT("text", "Text", 8),
    XML("xml", "XML", 9),
    MYSQL("mysql", "MySQL", 10),
    WEB("web", "Web Service", 11),
    OTHER("other", "Other", 12);

    private String _key;
    private String _label;
    private int _ordinal;
    private static List<JdbcDriverType> sortedDataTypes = new ArrayList<JdbcDriverType>();
    private static int _min;
    private static int _max;

    private JdbcDriverType(String keyIn, String labelIn, int ordinalIn) {
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

    private static Map<String, JdbcDriverType> codeToEnumMapping = new HashMap<String, JdbcDriverType>();

    static {
        _min = 100;
        _max = 0;
        for (JdbcDriverType e : values()) {
            _min = Math.min(_min, e._key.length());
            _max = Math.max(_max, e._key.length());
            codeToEnumMapping.put(e._key, e);
            sortedDataTypes.add(e);
        }
        Collections.sort(sortedDataTypes, new Comparator<JdbcDriverType>() {

            @Override
            public int compare(JdbcDriverType o1, JdbcDriverType o2) {
                return o1.getLabel().compareTo(o2.getLabel());
    }
        });
    }

    public static JdbcDriverType getValue(String s) {
        if (s == null) {
            return null;
        }

        JdbcDriverType type = codeToEnumMapping.get(s.toLowerCase());

        if (type == null) {
            type = JdbcDriverType.OTHER;
        }

        return type;
    }

    public static String toString(JdbcDriverType type) {
        if (type == null) {
            return "";
        }
        return type.getLabel();
    }

    public static List<JdbcDriverType> sortedValuesByLabel() {
        return sortedDataTypes;
    }

    public boolean isBaseFor(String stringIn) {

        return ((null != stringIn) && (_key.length() <= stringIn.length())
                && (_key.equalsIgnoreCase(stringIn.substring(0, _key.length()))));
    }

    public static JdbcDriverType extractValue(String stringIn) {

        JdbcDriverType type = null;

        if ((stringIn != null) && (0 < stringIn.length())) {

            String myInput = stringIn.toLowerCase();

            for (int i = Math.min(stringIn.length(), _max); _min <= i; i--) {

                type = codeToEnumMapping.get(myInput.substring(0, i));

                if (null != type) {

                    break;
                }
            }
        }

        return (type != null) ? type : JdbcDriverType.OTHER;
    }
}
