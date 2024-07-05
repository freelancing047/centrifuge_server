package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;
import csi.client.gwt.etc.BaseCsiEvent;

/**
 * Created by centrifuge on 4/24/2019.
 */
public class GridCellClick extends BaseCsiEvent<GridCellClickHandler> {

    private int _row;
    private int _column;

    public static final GwtEvent.Type<GridCellClickHandler> type = new GwtEvent.Type<GridCellClickHandler>();

    public GridCellClick(int rowIn, int columnIn) {

        _row = rowIn;
        _column = columnIn;
    }

    public int getRow() {

        return _row;
    }

    public int getColumn() {

        return _column;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<GridCellClickHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(GridCellClickHandler handlerIn) {
        handlerIn.onGridCellClick(this);
    }
}
