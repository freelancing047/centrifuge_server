package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.mainapp.MainView.Mode;


public class ExitSharingModeEvent extends BaseCsiEvent<ExitSharingModeEventHandler> {

    public static final GwtEvent.Type<ExitSharingModeEventHandler> type = new GwtEvent.Type<ExitSharingModeEventHandler>();

    public ExitSharingModeEvent() {
        
        super();
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ExitSharingModeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(ExitSharingModeEventHandler handler) {
        handler.onExitSharingMode(this);
    }
}
