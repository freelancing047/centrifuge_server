package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;
import com.sencha.gxt.widget.core.client.grid.Grid;
import csi.client.gwt.etc.BaseCsiEvent;

/**
 * Created by centrifuge on 8/2/2017.
 */
public class GridClickEvent extends BaseCsiEvent<GridClickEventHandler> {

    public static final GwtEvent.Type<GridClickEventHandler> type = new GwtEvent.Type<GridClickEventHandler>();

    private Object _key;
    private int _row;
    private int _column;

    public GridClickEvent(Object keyIn, int rowIn, int columnIn) {

        _key = keyIn;
        _row = rowIn;
        _column = columnIn;
    }

    public GridClickEvent(int rowIn, int columnIn) {

        _row = rowIn;
        _column = columnIn;
    }

    public GridClickEvent(Object keyIn) {

        _key = keyIn;
    }

    public Object getKey() {

        return _key;
    }

    public int getRow() {

        return _row;
    }

    public int getColumn() {

        return _column;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<GridClickEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(GridClickEventHandler handler) {
        handler.onGridClick(this);
    }
}
