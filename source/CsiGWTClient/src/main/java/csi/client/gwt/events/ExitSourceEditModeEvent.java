package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.mainapp.MainView;
import csi.client.gwt.mainapp.MainView.Mode;
import csi.server.common.interfaces.DataContainer;


public class ExitSourceEditModeEvent extends BaseCsiEvent<ExitSourceEditModeEventHandler> {

    public static final GwtEvent.Type<ExitSourceEditModeEventHandler> type = new GwtEvent.Type<ExitSourceEditModeEventHandler>();

    private String _titleBar = null;
    private boolean _refresh = false;

    public ExitSourceEditModeEvent(String titleBarIn, boolean refreshIn) {

        super();
        _titleBar = titleBarIn;
        _refresh = refreshIn;
    }

    public String getTitleBar() {

        return _titleBar;
    }

    public boolean getRefresh() {

        return _refresh;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ExitSourceEditModeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(ExitSourceEditModeEventHandler handler) {
        handler.onExitSourceEditMode(this);
    }
}
