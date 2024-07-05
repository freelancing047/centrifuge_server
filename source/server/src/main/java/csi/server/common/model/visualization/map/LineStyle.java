package csi.server.common.model.visualization.map;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.shared.core.util.HasLabel;

public enum LineStyle implements IsSerializable, HasLabel {

    DASH("Dash"),
    DOT("Dot"),
    SOLID("Solid"),
    NONE("None");

    private String label;
    private static String[] _i18nLabels = null;

    public static LineStyle[] lineStyleWheel = { DASH, DOT, SOLID, NONE };

    LineStyle(String label) {
        this.label = label;
    }

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }


    @Override
    public String getLabel() {
        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : label;
        return (null != myLabel) ? myLabel : label;
    }

    public static LineStyle getShape(String shape) {
        if(shape == null)
            return NONE;
        if (shape.equals("Dash")) {
            return DASH;
        }
        if (shape.equals("Dot")) {
            return DOT;
        }
        if (shape.equals("Solid")) {
            return SOLID;
        }
        return NONE;
    }
}
