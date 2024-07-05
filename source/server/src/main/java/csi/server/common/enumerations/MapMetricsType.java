package csi.server.common.enumerations;

import csi.shared.core.util.HasLabel;

import java.util.HashMap;
import java.util.Map;

public enum MapMetricsType implements HasLabel {
    PLACE("Place"),
    PLACES("Places");


    private String label;
    private static String[] _i18nLabels = null;

    MapMetricsType(String label) {
        this.label = label;
    }

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    private static Map<String, MapMetricsType> _codeToEnumMapping = new HashMap<String, MapMetricsType>();

    static {
        for (MapMetricsType fc : values()) {
            _codeToEnumMapping.put(fc.label, fc);
        }
    }

    public static MapMetricsType forCode(String value) {
        return _codeToEnumMapping.get(value);
    }

    @Override
    public String getLabel() {
        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : label;
        return (null != myLabel) ? myLabel : label;
    }
}
