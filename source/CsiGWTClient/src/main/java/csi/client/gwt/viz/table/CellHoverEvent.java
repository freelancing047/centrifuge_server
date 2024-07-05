package csi.client.gwt.viz.table;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.widget.core.client.event.GridEvent;

import csi.client.gwt.viz.table.CellHoverEvent.CellHoverHandler;

public final class CellHoverEvent extends GridEvent<CellHoverHandler> {

    public interface HasCellHoverHandlers extends HasHandlers {
        HandlerRegistration addCellClickHandler(CellHoverEvent handler);
    }

    public interface CellHoverHandler extends EventHandler {
        void onCellHover(CellHoverEvent event);
    }

    private static GwtEvent.Type<CellHoverHandler> TYPE;

    public static GwtEvent.Type<CellHoverHandler> getType() {
        if (TYPE == null) {
            TYPE = new GwtEvent.Type<CellHoverHandler>();
        }
        return TYPE;
    }

    private int rowIndex;
    private int cellIndex;
    private Event event;

    public CellHoverEvent(int rowIndex, int cellIndex, Event event) {
        this.rowIndex = rowIndex;
        this.cellIndex = cellIndex;
        this.event = event;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GwtEvent.Type<CellHoverHandler> getAssociatedType() {
        return (GwtEvent.Type) TYPE;
    }

    public int getCellIndex() {
        return cellIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    protected void dispatch(CellHoverHandler handler) {
        handler.onCellHover(this);
    }
}