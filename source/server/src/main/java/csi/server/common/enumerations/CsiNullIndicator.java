package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 7/30/2015.
 */
public enum CsiNullIndicator implements Serializable {

    EMPTY_STRING("", "empty value - ' '"),
    SLASH_N("\\N", "slash-N - ' \\N '"),
    UPPER_NULL("NULL", "upper null - ' NULL '"),
    LOWER_NULL("null", "lower null - ' null '"),
    MIXED_NULL("Null", "capped null - ' Null '");

    private String _string;
    private String _label;

    private static String[] _i18nLabels = null;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public String getString() {

        return _string;
    }

    private CsiNullIndicator(String stringIn, String labelIn) {

        _string = stringIn;
        _label = labelIn;
    }
}
