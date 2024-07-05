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


public enum JdbcDriverParameterValidationType implements Serializable, HasLabel, HasOrdinal {

    MAXCHARS("maxchars", "maxChars", 0),
    MINCHARS("minchars", "minChars", 1),
    MINVALUE("minvalue", "minValue", 2),
    MAXVALUE("maxvalue", "maxValue", 3),
    ISNUMBER("isnumber", "isNumber", 4),
    ISVALUE("isvalue", "isValue", 5),
    REGEX("regex", "regEx", 6),
    MAXITEMS("maxitems", "maxItems", 7),
    MINITEMS("minitems", "minItems", 8),
    FORMAT("format", "format", 9),
    FILETYPE("filetype", "fileType", 10),
    UNSUPPORTED("unsupported", "Unsupported", 11);

    private String _key;
    private String _label;
    private int _ordinal;
    private static List<JdbcDriverParameterValidationType> sortedDataTypes = new ArrayList<JdbcDriverParameterValidationType>();

    private JdbcDriverParameterValidationType(String keyIn, String labelIn, int ordinalIn) {
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

    private static Map<String, JdbcDriverParameterValidationType> codeToEnumMapping = new HashMap<String, JdbcDriverParameterValidationType>();

    static {
        for (JdbcDriverParameterValidationType e : values()) {
            codeToEnumMapping.put(e._key, e);
            sortedDataTypes.add(e);
        }
        Collections.sort(sortedDataTypes, new Comparator<JdbcDriverParameterValidationType>() {

            @Override
            public int compare(JdbcDriverParameterValidationType o1, JdbcDriverParameterValidationType o2) {
                return o1.getLabel().compareTo(o2.getLabel());
    }
        });
    }

    public static JdbcDriverParameterValidationType getValue(String s) {
        if (s == null) {
            return null;
        }

        JdbcDriverParameterValidationType type = codeToEnumMapping.get(s.toLowerCase());

        if (type == null) {
            type = JdbcDriverParameterValidationType.UNSUPPORTED;
        }

        return type;
    }

    public static String toString(JdbcDriverParameterValidationType type) {
        if (type == null) {
            return "";
        }
        return type.getLabel();
    }

    public static List<JdbcDriverParameterValidationType> sortedValuesByLabel() {
        return sortedDataTypes;
    }
}
