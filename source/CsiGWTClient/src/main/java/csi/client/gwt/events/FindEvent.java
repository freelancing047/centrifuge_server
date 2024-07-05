package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class FindEvent extends BaseCsiEvent<FindEventHandler> {

    public static final GwtEvent.Type<FindEventHandler> type = new GwtEvent.Type<FindEventHandler>();
    
    private int x;
    private int y;
    
    
    public FindEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    
    @Override
    public Type<FindEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(FindEventHandler handler) {
        handler.find(x, y);
    }

}
