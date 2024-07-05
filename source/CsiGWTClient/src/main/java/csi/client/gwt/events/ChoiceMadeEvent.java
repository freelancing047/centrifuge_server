package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class ChoiceMadeEvent extends BaseCsiEvent<ChoiceMadeEventHandler> {

    public static final GwtEvent.Type<ChoiceMadeEventHandler> type = new GwtEvent.Type<ChoiceMadeEventHandler>();

    private int _choice;
    private Object _data;

    public ChoiceMadeEvent(int choiceIn) {

        _choice = choiceIn;
    }

    public ChoiceMadeEvent(int choiceIn, Object dataIn) {

        _choice = choiceIn;
        _data = dataIn;
    }

    public int getChoice() {

        return _choice;
    }

    public Object getData() {

        return _data;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ChoiceMadeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(ChoiceMadeEventHandler handler) {
        handler.onChoiceMade(this);
    }
}
