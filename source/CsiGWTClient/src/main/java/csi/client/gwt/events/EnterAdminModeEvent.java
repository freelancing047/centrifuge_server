package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class EnterAdminModeEvent extends BaseCsiEvent<EnterAdminModeEventHandler> {

    public static final GwtEvent.Type<EnterAdminModeEventHandler> type = new GwtEvent.Type<EnterAdminModeEventHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<EnterAdminModeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(EnterAdminModeEventHandler handler) {
        handler.onEnterAdminMode(this);
    }
}
