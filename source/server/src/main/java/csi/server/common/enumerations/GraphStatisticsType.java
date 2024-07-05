package csi.server.common.enumerations;

import csi.server.common.util.HasOrdinal;
import csi.shared.core.util.HasLabel;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public enum GraphStatisticsType implements Serializable, HasLabel, HasOrdinal {

    VISIBLE("visible", "Visible", 0),
    TOTAL("total", "Total", 1)

    ;

    private static Map<String, GraphStatisticsType> codeToEnumMapping = null;

    private String _key;
    private String _label;
    private int _ordinal;


    private GraphStatisticsType(String keyIn, String labelIn, int ordinalIn) {
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
        return _ordinal;
    }

    @Override
    public String getLabel() {
        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    private static Map<String, GraphStatisticsType> getEnumMapping() {

        if (null == codeToEnumMapping) {

            codeToEnumMapping = new TreeMap<String, GraphStatisticsType>();

            for (GraphStatisticsType myItem : values()) {

                codeToEnumMapping.put(myItem._key, myItem);
            }
        }
        return codeToEnumMapping;
    }
}
