package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
import csi.client.gwt.etc.BaseCsiEvent;


public class EnterSourceEditModeEvent extends BaseCsiEvent<EnterSourceEditModeEventHandler> {

    public static final GwtEvent.Type<EnterSourceEditModeEventHandler> type = new GwtEvent.Type<EnterSourceEditModeEventHandler>();

    private DataSourceEditorPresenter _presenter = null;
    private SourceEditDialog _parentDialog = null;

    public EnterSourceEditModeEvent(DataSourceEditorPresenter presenterIn, SourceEditDialog parentDialogIn) {

        super();
        _presenter = presenterIn;
        _parentDialog = parentDialogIn;
    }

    public DataSourceEditorPresenter getPresenter() {

        return _presenter;
    }

    public SourceEditDialog getParentDialog() {

        return _parentDialog;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<EnterSourceEditModeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(EnterSourceEditModeEventHandler handler) {
        handler.onEnterSourceEditMode(this);
    }
}
