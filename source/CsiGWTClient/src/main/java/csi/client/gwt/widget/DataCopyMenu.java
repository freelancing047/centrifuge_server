package csi.client.gwt.widget;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 8/23/2017.
 */
public class DataCopyMenu<T> extends ContextMenuDisplay {

    T _value;
    int _row;
    DataCopyCallback _callback = null;

    MenuCallback _menuCallback = new MenuCallback() {
        @Override
        public void onSelectionMade(int menuItemIdIn) {

            switch (menuItemIdIn) {

                case 0:

                    _callback.onCellSelection(_value);
                    break;

                case 1:

                    _callback.onRowSelection(_row);
                    break;

                default:

                    break;
            }
        }
    };

    public DataCopyMenu(DataCopyCallback callbackIn, int rowIn, T valueIn) {

        super(
                new String[] {

                        "Copy Cell Data",
                        "Copy Menu Data"
                }
        );
        _callback = callbackIn;
        _row = rowIn;
        _value = valueIn;
    }

    public void showAt(int xIn, int yIn) {

        showMenuAt(xIn, yIn, _menuCallback);
    }
}
