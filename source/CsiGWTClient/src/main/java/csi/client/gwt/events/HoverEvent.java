package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.etc.BaseCsiEvent;


public class HoverEvent extends BaseCsiEvent<HoverEventHandler> {

    private boolean _isOver = false;
    private String _displayMessage = null;
    private Widget _sourceObject = null;

    public static final GwtEvent.Type<HoverEventHandler> type = new GwtEvent.Type<HoverEventHandler>();
    
    public HoverEvent(Widget sourceObjectIn, String displayMessageIn, boolean isOverIn) {
        
        _sourceObject = sourceObjectIn;
        _displayMessage = displayMessageIn;
        _isOver = isOverIn;
    }

    public Widget getSourceObject() {
        
        return _sourceObject;
    }

    public String getDisplayMessage() {
        
        return _displayMessage;
    }
    
    public boolean isOver() {
        
        return _isOver;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<HoverEventHandler> getAssociatedType() {
        
        return type;
    }

    @Override
    protected void dispatch(HoverEventHandler handler) {
        
        handler.onHoverChange(this);
    }
}
