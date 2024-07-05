package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class RefreshRequiredEvent extends BaseCsiEvent<RefreshRequiredEventHandler> {

    public static final GwtEvent.Type<RefreshRequiredEventHandler> type = new GwtEvent.Type<RefreshRequiredEventHandler>();

    public RefreshRequiredEvent() {
        
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<RefreshRequiredEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(RefreshRequiredEventHandler handler) {
        handler.onRefreshRequired(this);
    }
}
