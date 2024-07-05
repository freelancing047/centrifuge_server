package csi.server.common.enumerations;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by centrifuge on 7/31/2015.
 */
public enum CsiFileType implements Serializable {

    NEW_EXCEL(InstallationType.NEW_EXCEL, "xlsx", false, true, true, false),
    OLD_EXCEL(InstallationType.OLD_EXCEL, "xls", false, true, false, false),
    CSV(InstallationType.CSV, "csv", true, false, true, true),
    TEXT(InstallationType.TEXT, "txt", true, false, true, false),
    DUMP(InstallationType.DUMP, "bcp", true, false, false, false),
    XML(InstallationType.XML, "xml", false, true, false, false),
    JSON(InstallationType.JSON, "json", false, true, false, false),
    ADHOC(InstallationType.ADHOC, "dse", false, false, false, false),
    DATAVIEW(InstallationType.DATAVIEW, "capture", false, false, false, false);

    private static final Map<String, CsiFileType> _fileTypeMap;
    static {

        _fileTypeMap = new TreeMap<String, CsiFileType>();

        _fileTypeMap.put("xlsx", NEW_EXCEL);
        _fileTypeMap.put("xls", OLD_EXCEL);
        _fileTypeMap.put("csv", CSV);
        _fileTypeMap.put("txt", TEXT);
        _fileTypeMap.put("bcp", DUMP);
        _fileTypeMap.put("xml", XML);
        _fileTypeMap.put("json", JSON);
        _fileTypeMap.put("dse", ADHOC);
        _fileTypeMap.put("capture", DATAVIEW);
    }

    private static CsiFileType _default = null;

    private InstallationType _type;
    private String _extension;
    private boolean _doClientProcessing;
    private boolean _hasMultipleTables;
    private boolean _isSupported;
    private boolean _defaultChoice;

    public static CsiFileType getFileType(String stringIn) {

        return (null != stringIn) ? _fileTypeMap.get(stringIn.toLowerCase()) : null;
    }

    public boolean isDefaultChoice() {

        return _defaultChoice;
    }

    public boolean isSupported() {

        return _isSupported;
    }

    public boolean hasMultipleTables() {

        return _hasMultipleTables;
    }

    public boolean doClientProcessing() {

        return _doClientProcessing;
    }

    public String getExtension() {

        return _extension;
    }

    public String getSuffix() {

        return "." + _extension;
    }

    public InstallationType getInstallationType() {

        return _type;
    }

    public String getLabel() {

        return _type.getLabel();
    }

    public String getDescription() {

        return _type.getDescription();
    }

    public static CsiFileType getDefault() {

        if (null == _default) {

            for (CsiFileType myType : values()) {

                if (myType.isDefaultChoice()) {

                    _default = myType;
                }
            }
        }

        return _default;
    }

    private CsiFileType(InstallationType typeIn, String extensionIn, boolean doClientProcessingIn,
                        boolean hasMultipleTablesIn, boolean isSupportedIn, boolean defaultChoiceIn) {

        _type = typeIn;
        _extension = extensionIn;
        _doClientProcessing = doClientProcessingIn;
        _hasMultipleTables = hasMultipleTablesIn;
        _isSupported = isSupportedIn;
        _defaultChoice = defaultChoiceIn;
    }
}
