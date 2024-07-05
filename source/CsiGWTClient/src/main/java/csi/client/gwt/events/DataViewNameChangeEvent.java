package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;



public class DataViewNameChangeEvent extends BaseCsiEvent<DataViewNameChangeEventHandler> {

    public static final Type<DataViewNameChangeEventHandler> type = new GwtEvent.Type<DataViewNameChangeEventHandler>();
    private String newRemarks;
    private String newName;
    private String uuid;

    
    public DataViewNameChangeEvent(String uuidIn, String newNameIn, String newRemarksIn){

        uuid = uuidIn;
        newName = newNameIn;
        newRemarks = newRemarksIn;
    }
    
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<DataViewNameChangeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(DataViewNameChangeEventHandler handler) {
        handler.onDataViewNameChange(this);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return newName;
    }

    public String getRemarks() {
        return newRemarks;
    }
}
