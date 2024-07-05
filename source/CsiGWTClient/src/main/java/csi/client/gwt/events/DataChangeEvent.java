package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.server.common.enumerations.DataOperation;

public class DataChangeEvent extends BaseCsiEvent<DataChangeEventHandler> {

    public static final GwtEvent.Type<DataChangeEventHandler> type = new GwtEvent.Type<DataChangeEventHandler>();

    private Object _object;
    private DataOperation _request;
    private Throwable _error;    
    private boolean _success = true;
    
    public DataChangeEvent(Object objectIn) {
        _object = objectIn;
        _request = DataOperation.UNSPECIFIED;
        _success = true;
    }
    
    public DataChangeEvent(Object objectIn, DataOperation requestIn) {
        _object = objectIn;
        _request = requestIn;
        _success = true;
    }
    
    public DataChangeEvent(Object objectIn, Throwable errorIn) {
        _object = objectIn;
        _success = false;
        _error = errorIn;
    }
    
    public Object getData() {
        return _object;
    }
    
    public String getDataString() {
        if (_object instanceof String) {
            return (String)_object;
        } else {
            return _object.toString();
        }
    }

    public DataOperation getRequest() {
        return _request;
    }

    public String getRequestString() {
        return _request.getLabel();
    }

    public Throwable getError() {
        return _error;
    }
    
    public boolean isError() {
        return !_success;
    }
    
    public boolean isSuccess() {
        return _success;
    }
    
    public boolean isNew() {
        return (DataOperation.CREATE == _request);
    }
    
    public boolean isChange() {
        return (DataOperation.UPDATE == _request);
    }
    
    public boolean isDelete() {
        return (DataOperation.DELETE == _request);
    }
    

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<DataChangeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(DataChangeEventHandler handler) {
        handler.onDataChange(this);
    }
}
