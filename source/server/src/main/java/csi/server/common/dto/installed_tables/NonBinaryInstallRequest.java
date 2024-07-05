package csi.server.common.dto.installed_tables;

import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.tables.InstalledTable;

/**
 * Created by centrifuge on 11/18/2015.
 */
public class NonBinaryInstallRequest extends TableInstallRequest {

    private CsiEncoding _encoding;

    public NonBinaryInstallRequest() {

        super();
    }

    public NonBinaryInstallRequest(InstalledTable tableIn) {

        super(tableIn);
    }

    public NonBinaryInstallRequest(CsiFileType fileTypeIn, CsiEncoding encodingIn) {

        super(fileTypeIn);

        _encoding = encodingIn;
    }

    public NonBinaryInstallRequest(CsiFileType fileTypeIn, CsiEncoding encodingIn, TableParameters tableParametersIn) {

        super(fileTypeIn, tableParametersIn);

        _encoding = encodingIn;
    }

    public void setEncoding(CsiEncoding encodingIn) {

        _encoding = encodingIn;
    }

    public CsiEncoding getEncoding() {

        return _encoding;
    }
}
