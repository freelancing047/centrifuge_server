package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class CarriageReturnEvent extends BaseCsiEvent<CarriageReturnEventHandler> {

    private boolean _validFlag = false;
    private Object _selection = null;

    public static final GwtEvent.Type<CarriageReturnEventHandler> type = new GwtEvent.Type<CarriageReturnEventHandler>();
    
    public CarriageReturnEvent(boolean validFlagIn) {
        
        _validFlag = validFlagIn;
    }
    
    public CarriageReturnEvent(boolean validFlagIn, Object selectionIn) {
        
        _validFlag = validFlagIn;
        _selection = selectionIn;
    }

    public boolean getValidFlag() {
        
        return _validFlag;
    }

    public Object getSelection() {
        
        return _selection;
    }
    
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<CarriageReturnEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(CarriageReturnEventHandler handler) {
        handler.onCarriageReturn(this);
    }
}
