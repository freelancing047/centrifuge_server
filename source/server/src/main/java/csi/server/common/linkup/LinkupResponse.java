package csi.server.common.linkup;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.util.SynchronizeChanges;

/**
 * Created by centrifuge on 4/1/2015.
 */
public class LinkupResponse implements IsSerializable {

    private long _rowCount;
    private int _nextLinkupId;
    private String _linkupTables;
    private String _linkupMapUuid;
    private int _useCount;
    private CapcoInfo _capcoInfo;
    private SecurityTagsInfo _securityTagsInfo;
    private LinkupRequest _linkupRequest;
    private LinkupValidationReport _validationReport;
    private DataView _newDataView;

    public LinkupResponse() {

    }

    public LinkupResponse(LinkupValidationReport validationReportIn) {

        _validationReport = validationReportIn;
    }

    public LinkupResponse(DataView newDataViewIn, long rowCountIn, int nextLinkupIdIn, String linkupTablesIn,
                          String linkupMapUuidIn, int useCountIn, CapcoInfo capcoInfoIn,
                          SecurityTagsInfo securityTagsInfoIn) {

        _newDataView = newDataViewIn;
        _validationReport = new LinkupValidationReport();
        _rowCount = rowCountIn;
        _nextLinkupId = nextLinkupIdIn;
        _linkupTables = linkupTablesIn;
        _linkupMapUuid = linkupMapUuidIn;
        _useCount = useCountIn;
        _capcoInfo = capcoInfoIn;
        _securityTagsInfo = securityTagsInfoIn;
    }

    public void setNewDataView(DataView newDataViewIn) {

        _newDataView = newDataViewIn;
    }

    public DataView getNewDataView() {

        return _newDataView;
    }

    public void setRowCount(long rowCountIn) {

        _rowCount = rowCountIn;
    }

    public long getRowCount() {

        return _rowCount;
    }

    public void setNextLinkupId(int nextLinkupIdIn) {

        _nextLinkupId = nextLinkupIdIn;
    }

    public int getNextLinkupId() {

        return _nextLinkupId;
    }

    public void setLinkupTables(String linkupTablesIn) {

        _linkupTables = linkupTablesIn;
    }

    public String getLinkupTables() {

        return _linkupTables;
    }

    public void setLinkupMapUuid(String linkupMapUuidIn) {

        _linkupMapUuid = linkupMapUuidIn;
    }

    public String getLinkupMapUuid() {

        return _linkupMapUuid;
    }

    public void setUseCount(int useCountIn) {

        _useCount = useCountIn;
    }

    public int getUseCount() {

        return _useCount;
    }

    public void setCapcoInfo(CapcoInfo capcoInfoIn) {

        _capcoInfo = capcoInfoIn;
    }

    public CapcoInfo getCapcoInfo() {

        return _capcoInfo;
    }

    public void setValidationReport(LinkupValidationReport validationReportIn) {

        _validationReport = validationReportIn;
    }

    public LinkupValidationReport getValidationReport() {

        return _validationReport;
    }

    public void setLinkupRequest(LinkupRequest linkupRequestIn) {

        _linkupRequest = linkupRequestIn;
    }

    public LinkupRequest getLinkupRequest() {

        return _linkupRequest;
    }

    public void setSecurityTagsInfo(SecurityTagsInfo securityTagsInfoIn) {

        _securityTagsInfo = securityTagsInfoIn;
    }

    public SecurityTagsInfo getSecurityTagsInfo() {

        return _securityTagsInfo;
    }

    public DataView updateDataView(DataView dataViewIn) {

        DataViewDef myMetaData = dataViewIn.getMeta();
        LinkupMapDef myLinkupMap = myMetaData.getLinkup(_linkupMapUuid);
        CapcoInfo myCapcoInfo = myMetaData.getCapcoInfo();
        SecurityTagsInfo mySecurityTagsInfo = myMetaData.getSecurityTagsInfo();

        myLinkupMap.setUseCount(_useCount);
        dataViewIn.incrementNextLinkupRowId(_rowCount);
        dataViewIn.setNextLinkupId(_nextLinkupId);
        dataViewIn.setLinkups(_linkupTables);

        SynchronizeChanges.setCapcoInfo(myMetaData, _capcoInfo, _securityTagsInfo, myCapcoInfo, mySecurityTagsInfo);

        return dataViewIn;
    }
}
