package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class EditTemplateEvent extends BaseCsiEvent<EditTemplateEventHandler> {

    public static final GwtEvent.Type<EditTemplateEventHandler> type = new GwtEvent.Type<EditTemplateEventHandler>();

    private String templateUUID;
    private boolean _silent = false;

    public EditTemplateEvent(String templateUUID) {
        this.templateUUID = templateUUID;
    }

    public EditTemplateEvent(String templateUUID, boolean silentIn) {
        this.templateUUID = templateUUID;
        _silent = silentIn;
    }

    public String getTemplateUUID() {
        return templateUUID;
    }

    public boolean isSilent() {
        return _silent;
    }

    @Override
    public Type<EditTemplateEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(EditTemplateEventHandler handler) {
        handler.onEditTemplate(this);
    }
}
