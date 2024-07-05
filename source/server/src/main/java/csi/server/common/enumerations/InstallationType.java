package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 7/6/2015.
 */
public enum InstallationType implements Serializable {

    NEW_EXCEL("File Type \"xlsx\"", "Excel File (xlsx)"),
    OLD_EXCEL("File Type \"xls\"", "Excel File (xls)"),
    CSV("File Type \"csv\"", "Excel Export File (csv)"),
    TEXT("File Type \"txt\"", "Text File (txt)"),
    DUMP("File Type \"bcp\"", "SqlServer export File (bcp)"),
    XML("File Type \"xml\"", "XML File (xml)"),
    JSON("File Type \"json\"", "JSON Output File"),
    WRAPPER("Foreign Table Wrapper", "Foreign Table Wrapper"),
    DATAVIEW("DataView Data Source", "DataView Data Source"),
    ADHOC("Ad Hoc Data Source", "Ad Hoc Data Source");

    String _label;
    String _description;

    private static String[] _i18nLabels = null;
    private static String[] _i18nDescriptions = null;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    public static void setI18nDescriptions(String[] i18nDescriptionsIn) {

        _i18nDescriptions = i18nDescriptionsIn;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public String getDescription() {

        String myDescription = (null != _i18nDescriptions) ? _i18nDescriptions[ordinal()] : _description;
        return (null != myDescription) ? myDescription : _description;
    }

    public int getMask() {

        return 1 << ordinal();
    }

    private InstallationType(String labelIn, String descriptionIn) {

        _label = labelIn;
        _description = descriptionIn;
    }
}
