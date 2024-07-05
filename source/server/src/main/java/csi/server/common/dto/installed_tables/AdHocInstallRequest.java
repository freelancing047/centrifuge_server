package csi.server.common.dto.installed_tables;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.model.dataview.AdHocDataSource;

/**
 * Created by centrifuge on 5/9/2017.
 */
public class AdHocInstallRequest extends TableInstallRequest {

    private AdHocDataSource _dataSource;

    public AdHocInstallRequest() {

        super(CsiFileType.ADHOC);
    }

    public AdHocInstallRequest(AdHocDataSource dataSourceIn) {

        super(CsiFileType.ADHOC);
        _dataSource = dataSourceIn;
    }

    public AdHocInstallRequest(AdHocDataSource dataSourceIn, TableParameters tableParametersIn) {

        super(CsiFileType.ADHOC, tableParametersIn);
        _dataSource = dataSourceIn;
    }

    public void setDataSourcen(AdHocDataSource dataSourceIn) {

        _dataSource = dataSourceIn;
    }

    public AdHocDataSource getDataSource() {

        return _dataSource;
    }
}
