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


public enum CapcoSection implements Serializable, HasLabel, HasOrdinal {

    CLASS("class", "Primary Classification", 0),
    SCI("sci", "Sensitive Compartmented Info", 1),
    SAP("sap", "Special Access Programs", 2),
    AEA("aea", "Atomic Energy Act", 3),
    FGI("fgi", "Foreign Government Info", 4),
    DISM("dism", "Dissemination Control", 5),
    NONIC("nonic", "Non-IC Control Markings", 6),
    OTHER("other", "Not Applicable", 7);

    private String _key;
    private String _label;
    private int _ordinal;
    private static List<CapcoSection> sortedSections = new ArrayList<CapcoSection>();
    private static List<CapcoSection> orderedSections = new ArrayList<CapcoSection>();

    private CapcoSection(String keyIn, String labelIn, int ordinalIn) {
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

    private static Map<String, CapcoSection> codeToEnumMapping = new HashMap<String, CapcoSection>();
    private static Map<String, CapcoSection> labelToEnumMapping = new HashMap<String, CapcoSection>();

    static {
        for (CapcoSection e : values()) {
            codeToEnumMapping.put(e._key, e);
            labelToEnumMapping.put(e._label, e);
            sortedSections.add(e);
            orderedSections.add(e);
        }
        Collections.sort(sortedSections, new Comparator<CapcoSection>() {

            @Override
            public int compare(CapcoSection o1, CapcoSection o2) {
                return o1.getLabel().compareTo(o2.getLabel());
    }
        });
    }

    public static CapcoSection getValue(String s) {
        if (s == null) {
            return null;
        }

        CapcoSection type = codeToEnumMapping.get(s.toLowerCase());

        if (type == null) {
            type = CapcoSection.OTHER;
        }

        return type;
    }

    public static CapcoSection getFromLabel(String s) {
        if (s == null) {
            return null;
        }

        CapcoSection type = labelToEnumMapping.get(s);

        if (type == null) {
            type = CapcoSection.OTHER;
        }

        return type;
    }

    public static String toString(JdbcDriverParameterValidationType type) {
        if (type == null) {
            return "";
        }
        return type.getLabel();
    }

    public static List<CapcoSection> getOrderedSections() {

        return orderedSections;
    }

    public static List<CapcoSection> sortedValuesByLabel() {
        return sortedSections;
    }
}
