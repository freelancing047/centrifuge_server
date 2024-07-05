package csi.client.gwt.widget;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 8/23/2017.
 */
public class DataTypeMenu extends ContextMenuDisplay {

    int _row;
    DataTypeCallback _callback = null;
    MenuCallback _menuCallback = new MenuCallback() {
        @Override
        public void onSelectionMade(int menuItemIdIn) {

            if ((0 <= menuItemIdIn) && (7 > menuItemIdIn)) {

                _callback.onTypeSelection(CsiDataType.values()[menuItemIdIn], _row);
            }
        }
    };

    public DataTypeMenu(DataTypeCallback callbackIn, int rowIn) {

        super(
                new String[] {

                        CsiDataType.values()[0].getLabel(),
                        CsiDataType.values()[1].getLabel(),
                        CsiDataType.values()[2].getLabel(),
                        CsiDataType.values()[3].getLabel(),
                        CsiDataType.values()[4].getLabel(),
                        CsiDataType.values()[5].getLabel(),
                        CsiDataType.values()[6].getLabel()
                }
        );
        _callback = callbackIn;
        _row = rowIn;
    }

    public void showAt(int xIn, int yIn) {

        showMenuAt(xIn, yIn, _menuCallback);
    }
}
