package csi.server.common.model.visualization.chart;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import csi.shared.core.util.HasLabel;

/**
 * Created by Ivan on 5/10/2017.
 */
public enum DisplayFirst implements Serializable, HasLabel {
    CHART("Chart"), //
    TABLE("Table");


    private String label;
    private static String[] _i18nLabels = null;


    private DisplayFirst(String label) {
        this.label = label;
    }

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    private static Map<String, DisplayFirst> _codeToEnumMapping = new HashMap<String, DisplayFirst>();

    static {
        for (DisplayFirst fc : values()) {
            _codeToEnumMapping.put(fc.label, fc);
        }
    }

    public static DisplayFirst forCode(String value) {
        return _codeToEnumMapping.get(value);
    }

    @Override
    public String getLabel() {
        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : label;
        return (null != myLabel) ? myLabel : label;
    }


}
