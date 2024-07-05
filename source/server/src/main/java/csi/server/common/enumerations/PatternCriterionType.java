package csi.server.common.enumerations;

import csi.server.common.util.HasOrdinal;
import csi.shared.core.util.HasLabel;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public enum PatternCriterionType implements Serializable, HasLabel, HasOrdinal {

    TYPE("type", "Type", 0),
    LABEL("label", "Label", 1),
    NUMBER_OF_NEIGHBORS("number of neighbors", "Number of Neighbors", 2),
    OCCURRENCE("occurrence", "Occurrence", 3),
    FIELD_VALUE("field value", "Field Value", 4),
    ;

    private static Map<String, PatternCriterionType> codeToEnumMapping = null;

    private String _key;
    private String _label;
    private int _ordinal;


    private PatternCriterionType(String keyIn, String labelIn, int ordinalIn) {
        _key = keyIn;
        _label = labelIn;
        _ordinal = ordinalIn;
    }

    private static String[] _i18nLabels = null;

    public static void setI18nLabels(String[] i18nLabelsIn) {
        _i18nLabels = i18nLabelsIn;
    }

    @Override
    public int getOrdinal() {
        return 0;
    }

    @Override
    public String getLabel() {
        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    private static Map<String, PatternCriterionType> getEnumMapping() {

        if (null == codeToEnumMapping) {

            codeToEnumMapping = new TreeMap<String, PatternCriterionType>();

            for (PatternCriterionType myItem : values()) {

                codeToEnumMapping.put(myItem._key, myItem);
            }
        }
        return codeToEnumMapping;
    }
}
