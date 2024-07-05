package csi.server.common.dto.installed_tables;

import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.uploader.zip.CsiZipEntry;

/**
 * Created by centrifuge on 11/18/2015.
 */
public class NewExcelInstallRequest extends TableInstallRequest {

    private CsiZipEntry _strings;

    public NewExcelInstallRequest() {

        super();
    }

    public NewExcelInstallRequest(InstalledTable tableIn) {

        super(tableIn);
    }

    public NewExcelInstallRequest(CsiZipEntry stringsIn) {

        super(CsiFileType.NEW_EXCEL);

        _strings = stringsIn;
    }

    public NewExcelInstallRequest(CsiZipEntry stringsIn, TableParameters tableParametersIn) {

        super(CsiFileType.NEW_EXCEL, tableParametersIn);

        _strings = stringsIn;
    }

    public CsiZipEntry getStrings() {

        return _strings;
    }

    public void setStrings(CsiZipEntry stringsIn) {

        _strings = stringsIn;
    }
}
