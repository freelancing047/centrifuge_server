package csi.client.gwt.widget.cells.context_menu;

import com.google.gwt.cell.client.Cell.Context;
import csi.client.gwt.widget.ContextMenuDisplay;
import csi.client.gwt.widget.MenuCallback;

/**
 * Created by centrifuge on 1/24/2018.
 */
public class CellContextMenu<T> extends ContextMenuDisplay {

    CellMenuCallback<T> _callback;
    String[] _menuItems;
    Context _context;
    T _value;
    MenuCallback _menuCallback = new MenuCallback() {

        @Override
        public void onSelectionMade(int menuItemIn) {

            if ((0 <= menuItemIn) && (_menuItems.length > menuItemIn)) {

                _callback.onSelection(_value, _context.getIndex(), _context.getColumn(), menuItemIn);
            }
        }
    };

    public CellContextMenu(String[] menuItemsIn, CellMenuCallback<T> callbackIn, Context contextIn, T valueIn) {
        super(menuItemsIn);
        _menuItems = menuItemsIn;
        _callback = callbackIn;
        _value = valueIn;
        _context = contextIn;
    }

    public void showAt(int xIn, int yIn) {

        showMenuAt(xIn, yIn, _menuCallback);
    }
}
