package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.server.common.model.dataview.DataView;

public class OpenDataViewEvent extends BaseCsiEvent<OpenDataViewEventHandler> {

    public static final Type<OpenDataViewEventHandler> type = new Type<OpenDataViewEventHandler>();

    private DataView _dataView;

    private String _dataViewName = null;
    private String _dataViewUuid = null;
    private String _dataViewOwner = null;
    private String _workSheetUuid = null;
    private long _rowCount = 0L;
    private boolean _moreData = false;

    public OpenDataViewEvent(DataView dataViewIn) {

        _dataView = dataViewIn;
    }

    public OpenDataViewEvent(String dataViewUuidIn) {

        _dataViewUuid = dataViewUuidIn;
    }

    public OpenDataViewEvent(String dataViewNameIn, String dataViewUuidIn) {

        _dataViewName = dataViewNameIn;
        _dataViewUuid = dataViewUuidIn;
    }

    public OpenDataViewEvent(DataView dataView, long rowCount, boolean moreData) {

        _dataView = dataView;
        _rowCount = rowCount;
        _moreData = moreData;
    }

    public OpenDataViewEvent(String dataViewUuidIn, long rowCount, boolean moreData) {

        _dataViewUuid = dataViewUuidIn;
        _rowCount = rowCount;
        _moreData = moreData;
    }

    public OpenDataViewEvent(String dataViewNameIn, String dataViewUuidIn, long rowCount, boolean moreData) {

        _dataViewName = dataViewNameIn;
        _dataViewUuid = dataViewUuidIn;
        _rowCount = rowCount;
        _moreData = moreData;
    }

    public OpenDataViewEvent(String dataViewNameIn, String dataViewUuidIn, String dataViewOwnerIn) {

        _dataViewName = dataViewNameIn;
        _dataViewUuid = dataViewUuidIn;
        _dataViewOwner = dataViewOwnerIn;
    }

    public OpenDataViewEvent(String dataViewNameIn, String dataViewUuidIn, String dataViewOwnerIn, String workSheetUuidIn) {

        _dataViewName = dataViewNameIn;
        _dataViewUuid = dataViewUuidIn;
        _dataViewOwner = dataViewOwnerIn;
        _workSheetUuid = workSheetUuidIn;
    }

    public DataView getDataView() {

        return _dataView;
    }

    public String getDataViewName() {

        return (null != _dataViewName) ? _dataViewName : ((null != _dataView) ? _dataView.getName() : null);
    }

    public String getDataViewUuid() {

        return (null != _dataViewUuid) ? _dataViewUuid : ((null != _dataView) ? _dataView.getUuid() : null);
    }

    public String getDataViewOwner() {

        return (null != _dataViewOwner) ? _dataViewOwner : ((null != _dataView) ? _dataView.getOwner() : null);
    }

    public String getActiveWorkSheet() {

        return _workSheetUuid;
    }

    public long getRowCount() {

        return _rowCount;
    }

    public boolean hasMoreData() {

        return _moreData;
    }

    @Override
    public Type<OpenDataViewEventHandler> getAssociatedType() {

        return type;
    }

    @Override
    protected void dispatch(OpenDataViewEventHandler handler) {
        handler.onDataViewOpen(this);
    }
}
