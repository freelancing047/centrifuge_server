package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.mainapp.MainView.Mode;


public class ExitAdminModeEvent extends BaseCsiEvent<ExitAdminModeEventHandler> {

    public static final GwtEvent.Type<ExitAdminModeEventHandler> type = new GwtEvent.Type<ExitAdminModeEventHandler>();

    public ExitAdminModeEvent() {
        
        super();
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ExitAdminModeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(ExitAdminModeEventHandler handler) {
        handler.onExitAdminMode(this);
    }
}
