package csi.server.common.enumerations;

/**
 * Created by centrifuge on 7/25/2016.
 */
public enum SystemParameter {

    USER("@USER", CsiDataType.String, "@USER"),
    CLIENT("@CLIENT", CsiDataType.String, "@CLIENT"),
    REMOTE_USER("@REMOTE_USER", CsiDataType.String, "@REMOTE_USER"),
    URL("@URL", CsiDataType.String, "@URL"),
    DN("@DN", CsiDataType.String, "@DN");

    private static String[] _i18nLabels = null;

    private String _name;
    private String _label;
    private CsiDataType _type;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    private SystemParameter(String nameIn, CsiDataType typeIn, String labelIn) {

        _name = nameIn;
        _label = labelIn;
        _type = typeIn;
    }

    public String getName() {

        return _name;
    }

    public CsiDataType getType() {

        return _type;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }
}
