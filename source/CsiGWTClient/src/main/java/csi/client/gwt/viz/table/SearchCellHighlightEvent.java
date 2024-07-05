package csi.client.gwt.viz.table;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.sencha.gxt.widget.core.client.event.GridEvent;

import csi.client.gwt.viz.table.SearchCellHighlightEvent.SearchCellHighlightHandler;


public class SearchCellHighlightEvent extends GridEvent<SearchCellHighlightHandler> {

    public interface HasSearchCellHighlightHandlers extends HasHandlers {
        HandlerRegistration addSearchCellHighlightHandler(SearchCellHighlightHandler handler);
    }

    public interface SearchCellHighlightHandler extends EventHandler {
        void onCellHighlight(SearchCellHighlightEvent event);
    }

    private static GwtEvent.Type<SearchCellHighlightHandler> TYPE;

    public static GwtEvent.Type<SearchCellHighlightHandler> getType() {
        if (TYPE == null) {
            TYPE = new GwtEvent.Type<SearchCellHighlightHandler>();
        }
        return TYPE;
    }

    private int rowIndex;
    private int cellIndex;
    private int rowId;

    public SearchCellHighlightEvent(int rowIndex, int cellIndex, int rowId) {
        this.rowIndex = rowIndex;
        this.cellIndex = cellIndex;
        this.setRowId(rowId);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GwtEvent.Type<SearchCellHighlightHandler> getAssociatedType() {
        return (GwtEvent.Type) TYPE;
    }

    public int getCellIndex() {
        return cellIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }
    @Override
    protected void dispatch(SearchCellHighlightHandler handler) {
        handler.onCellHighlight(this);
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }
}