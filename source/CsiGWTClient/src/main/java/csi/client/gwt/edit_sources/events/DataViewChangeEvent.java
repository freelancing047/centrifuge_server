package csi.client.gwt.edit_sources.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * Event is generated when a user selects table to add to the DSE canvas.
 */
public class DataViewChangeEvent extends BaseCsiEvent<DataViewChangeEventHandler> {

    public static final GwtEvent.Type<DataViewChangeEventHandler> type = new GwtEvent.Type<DataViewChangeEventHandler>();

    public DataViewChangeEvent() {
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<DataViewChangeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(DataViewChangeEventHandler handler) {
        handler.onChange(this);
    }

}
