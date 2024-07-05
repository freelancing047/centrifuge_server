package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * Created by centrifuge on 10/25/2016.
 */
public class TemplateNameChangeEvent extends BaseCsiEvent<TemplateNameChangeEventHandler> {

    public static final Type<TemplateNameChangeEventHandler> type = new GwtEvent.Type<TemplateNameChangeEventHandler>();
    private String newRemarks;
    private String newName;
    private String uuid;


    public TemplateNameChangeEvent(String uuidIn, String newNameIn, String newRemarksIn){

        uuid = uuidIn;
        newName = newNameIn;
        newRemarks = newRemarksIn;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<TemplateNameChangeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(TemplateNameChangeEventHandler handler) {
        handler.onTemplateNameChange(this);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return newName;
    }

    public String getRemarks() {
        return newRemarks;
    }
}
