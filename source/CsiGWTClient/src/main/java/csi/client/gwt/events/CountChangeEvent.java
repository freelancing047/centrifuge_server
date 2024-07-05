package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class CountChangeEvent extends BaseCsiEvent<CountChangeEventHandler> {

    public static final GwtEvent.Type<CountChangeEventHandler> type = new GwtEvent.Type<CountChangeEventHandler>();

    long _count;
    
    public CountChangeEvent(long countIn) {
        _count = countIn;
    }
    
    public long getCount() {
        
        return _count;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<CountChangeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(CountChangeEventHandler handler) {
        handler.onCountChange(this);
    }
}
