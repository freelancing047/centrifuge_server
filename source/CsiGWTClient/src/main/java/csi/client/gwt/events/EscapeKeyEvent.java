package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class EscapeKeyEvent extends BaseCsiEvent<EscapeKeyEventHandler> {

    public static final GwtEvent.Type<EscapeKeyEventHandler> type = new GwtEvent.Type<EscapeKeyEventHandler>();

    public EscapeKeyEvent() {

    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<EscapeKeyEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(EscapeKeyEventHandler handler) {
        handler.onEscapeKeyRecognized(this);
    }
}
