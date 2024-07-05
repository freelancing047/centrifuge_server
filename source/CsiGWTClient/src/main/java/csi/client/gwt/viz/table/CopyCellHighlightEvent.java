package csi.client.gwt.viz.table;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.sencha.gxt.widget.core.client.event.GridEvent;

import csi.client.gwt.viz.table.CopyCellHighlightEvent.CopyCellHighlightHandler;


public class CopyCellHighlightEvent extends GridEvent<CopyCellHighlightHandler> {

    public interface HasCopyCellHighlightHandlers extends HasHandlers {
        HandlerRegistration addCopyCellHighlightHandler(CopyCellHighlightHandler handler);
    }

    public interface CopyCellHighlightHandler extends EventHandler {
        void onCellHighlight(CopyCellHighlightEvent event);
    }

    private static GwtEvent.Type<CopyCellHighlightHandler> TYPE;

    public static GwtEvent.Type<CopyCellHighlightHandler> getType() {
        if (TYPE == null) {
            TYPE = new GwtEvent.Type<CopyCellHighlightHandler>();
        }
        return TYPE;
    }

    private int startRowIndex;
    private int startCellIndex;
    private int endRowIndex;
    private int endCellIndex;

    public CopyCellHighlightEvent(int rowIndex, int cellIndex, int endRowIndex, int endCellIndex) {
        this.startRowIndex = rowIndex;
        this.startCellIndex = cellIndex;
        this.endRowIndex = endRowIndex;
        this.endCellIndex = endCellIndex;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GwtEvent.Type<CopyCellHighlightHandler> getAssociatedType() {
        return (GwtEvent.Type) TYPE;
    }

    @Override
    protected void dispatch(CopyCellHighlightHandler handler) {
        handler.onCellHighlight(this);
    }

    public int getStartRowIndex() {
        return startRowIndex;
    }

    public int getStartCellIndex() {
        return startCellIndex;
    }

    public int getEndRowIndex() {
        return endRowIndex;
    }

    public int getEndCellIndex() {
        return endCellIndex;
    }


}