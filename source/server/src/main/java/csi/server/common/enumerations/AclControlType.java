package csi.server.common.enumerations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.server.common.util.HasOrdinal;
import csi.shared.core.util.HasLabel;


public enum AclControlType implements Serializable, HasLabel, HasOrdinal {

    CREATE("create", "Create", 0),
    READ("read", "Read", 1),
    EDIT("edit", "Edit", 2),
    DELETE("delete", "Delete", 3),
    CLASSIFY("classify", "Classify", 4),
    DECLASSIFY("declassify", "Declassify", 5),
    FIND("find", "Find", 6),
    ACCESS("access", "Access", 7),
    NEED("need", "Need", 8),
    SHARE("share", "Share", 9),
    TRANSFER("transfer", "Transfer", 10),
    EXPORT("export", "Export", 11),
    SOURCE_EDIT("source_edit", "Source Edit", 12),
    UNSUPPORTED("unsupported", "Unsupported", 13),
    EMBEDDED("embedded", "Embedded Use", 14);

    private static Map<String, AclControlType> codeToEnumMapping = null;

    private String _key;
    private String _label;
    private int _ordinal;

    private AclControlType(String keyIn, String labelIn, int ordinalIn) {
        _key = keyIn;
        _label = labelIn;
        _ordinal = ordinalIn;
    }

    private static String[] _i18nLabels = null;

    public static void setI18nLabels(String[] i18nLabelsIn) {
        _i18nLabels = i18nLabelsIn;
    }

    public String getKey() {
        return _key;
    }

    public String getLabel() {
        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }
    
    public int getOrdinal() {
        return _ordinal;
    }

    public static AclControlType getValue(String s) {
        if (s == null) {
            return null;
        }

        AclControlType type = getEnumMapping().get(s.toLowerCase());

        if (type == null) {
            type = AclControlType.UNSUPPORTED;
        }

        return type;
    }

    public static List<AclControlType> sortedValuesByLabel() {

        Collection<AclControlType> myValues = getEnumMapping().values();
        List<AclControlType> myList = new ArrayList<AclControlType>(myValues.size());

        for (AclControlType myItem : myValues){

            myList.add(myItem);
        }

        return myList;
    }

    private static Map<String, AclControlType> getEnumMapping() {

        if (null == codeToEnumMapping) {

            codeToEnumMapping = new TreeMap<String, AclControlType>();

            for (AclControlType myItem : values()) {

                codeToEnumMapping.put(myItem._key, myItem);
            }
        }
        return codeToEnumMapping;
    }
}
