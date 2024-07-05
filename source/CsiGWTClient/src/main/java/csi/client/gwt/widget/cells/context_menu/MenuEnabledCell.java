package csi.client.gwt.widget.cells.context_menu;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import csi.client.gwt.widget.boot.Dialog;
import com.google.gwt.cell.client.Cell;

import java.util.List;
import java.util.Set;

/**
 * Created by centrifuge on 1/24/2018.
 */
public class MenuEnabledCell<T> implements Cell<T> {

    CellMenuCallback<T> _callback;
    String[] _menuEntries;
    Cell _cell;

    public MenuEnabledCell(Cell cellIn, CellMenuCallback<T> callbackIn, String[] menuEntriesIn) {

        _cell = cellIn;
        _callback = callbackIn;
        _menuEntries = menuEntriesIn;
    }

    @Override
    public void onBrowserEvent(Context contextIn, Element parentIn, T valueIn,
                               NativeEvent eventIn, ValueUpdater<T> valueUpdaterIn) {
        try {

            String eventType = eventIn.getType();
            if ((null != _callback) && BrowserEvents.CONTEXTMENU.equals(eventType)) {

                (new CellContextMenu<T>(_menuEntries, _callback, contextIn, valueIn)).showAt(eventIn.getClientX(),
                        eventIn.getClientY());
            } else {

                _cell.onBrowserEvent(contextIn, parentIn, valueIn, eventIn, valueUpdaterIn);
            }

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    public boolean dependsOnSelection() {

        return _cell.dependsOnSelection();
    }

    public Set<String> getConsumedEvents() {

        return _cell.getConsumedEvents();
    }

    public boolean handlesSelection() {

        return _cell.handlesSelection();
    }

    public boolean isEditing(Context contextIn, Element parentIn, T valueIn) {

        return _cell.isEditing(contextIn, parentIn, valueIn);
    }

    public void render(Context contextIn, T valueIn, SafeHtmlBuilder htmlBuilderIn) {

        _cell.render(contextIn, valueIn, htmlBuilderIn);
    }

    public boolean resetFocus(Context contextIn, Element parentIn, T valueIn) {

        return _cell.resetFocus(contextIn, parentIn, valueIn);
    }

    public void setValue(Context contextIn, Element parentIn, T valueIn) {

        _cell.setValue(contextIn, parentIn, valueIn);
    }
}
