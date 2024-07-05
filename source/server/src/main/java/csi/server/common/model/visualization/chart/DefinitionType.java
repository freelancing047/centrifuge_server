package csi.server.common.model.visualization.chart;

import csi.shared.core.util.HasLabel;

import java.util.HashMap;
import java.util.Map;

public enum DefinitionType implements HasLabel {
    FIELD_NAME("Field name"), //
    CATEGORY("[category]");


    private String label;
    private static String[] _i18nLabels = null;

    DefinitionType(String label) {
        this.label = label;
    }

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    private static Map<String, DefinitionType> _codeToEnumMapping = new HashMap<String, DefinitionType>();

    static {
        for (DefinitionType fc : values()) {
            _codeToEnumMapping.put(fc.label, fc);
        }
    }

    public static DefinitionType forCode(String value) {
        return _codeToEnumMapping.get(value);
    }

    @Override
    public String getLabel() {
        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : label;
        return (null != myLabel) ? myLabel : label;
    }
}
