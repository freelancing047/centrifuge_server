package csi.client.gwt.widget.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.sencha.gxt.data.shared.ListStore;
import csi.client.gwt.events.GridCellClick;
import csi.client.gwt.events.GridCellClickHandler;
import csi.client.gwt.widget.IsSelectable;

/**
 * Created by centrifuge on 4/24/2019.
 */
public abstract class ClickableGridCell<T> extends AbstractCell<T> {

    GridCellClickHandler _handler = null;
    ListStore<? extends IsSelectable> _store = null;

    public ClickableGridCell(GridCellClickHandler handlerIn) {

        super("click"); //$NON-NLS-1$
        _handler = handlerIn;
    }

    public ClickableGridCell(GridCellClickHandler handlerIn, ListStore<? extends IsSelectable> storeIn) {

       this(handlerIn);
        _store = storeIn;
    }

    public void addStore(ListStore<? extends IsSelectable> storeIn) {

        _store = storeIn;
    }

    @Override
    public void onBrowserEvent(Cell.Context contextIn, Element parentIn,
                               T valueIn, NativeEvent eventIn, ValueUpdater<T> valueUpdaterIn) {

        String myEventType = eventIn.getType();
        int myRow = contextIn.getIndex();

        if ((null != _handler) && BrowserEvents.CLICK.equals(myEventType)
            && ((null == _store) || ((null != _store) && (0 <= myRow)
                && (_store.size() > myRow) && _store.get(myRow).getSelected()))) {

            _handler.onGridCellClick(new GridCellClick(myRow, contextIn.getColumn()));

        } else {

            super.onBrowserEvent(contextIn, parentIn, valueIn, eventIn, valueUpdaterIn);
        }
    }
}
