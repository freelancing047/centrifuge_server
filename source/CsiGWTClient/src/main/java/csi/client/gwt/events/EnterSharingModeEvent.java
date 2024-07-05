package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class EnterSharingModeEvent extends BaseCsiEvent<EnterSharingModeEventHandler> {

    public static final GwtEvent.Type<EnterSharingModeEventHandler> type = new GwtEvent.Type<EnterSharingModeEventHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<EnterSharingModeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(EnterSharingModeEventHandler handler) {
        handler.onEnterSharingMode(this);
    }
}
