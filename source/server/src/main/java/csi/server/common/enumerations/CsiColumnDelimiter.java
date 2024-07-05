package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 7/30/2015.
 */
public enum CsiColumnDelimiter implements Serializable {

    COMMA(',', "comma - ' , '"),
    TAB('\t', "tab (control code)"),
    BAR('|', "bar - ' | '"),
    COLON(':', "colon - ' : '"),
    SEMI(';', "semi-colon - ' ; '");

    private char _character;
    private String _label;

    private static String[] _i18nLabels = null;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public int getValue() {

        return (int)_character;
    }

    public char getCharacter() {

        return _character;
    }

    private CsiColumnDelimiter(char characterIn, String labelIn) {

        _character = characterIn;
        _label = labelIn;
    }
}
