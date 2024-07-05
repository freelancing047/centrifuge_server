package csi.client.gwt.widget;

import csi.server.common.enumerations.ComparingToken;

/**
 * Created by centrifuge on 8/23/2017.
 */
public class OperatorMenu extends ContextMenuDisplay {

    int _row;
    OperatorCallback _callback = null;
    MenuCallback _menuCallback = new MenuCallback() {
        @Override
        public void onSelectionMade(int menuItemIdIn) {

            if ((0 <= menuItemIdIn) && (7 > menuItemIdIn)) {

                _callback.onOperatorSelection(ComparingToken.values()[menuItemIdIn], _row);
            }
        }
    };

    public OperatorMenu(OperatorCallback callbackIn, int rowIn) {

        super(
                new String[] {

                        ComparingToken.values()[0].getLabel(),
                        ComparingToken.values()[1].getLabel(),
                        ComparingToken.values()[2].getLabel(),
                        ComparingToken.values()[3].getLabel(),
                        ComparingToken.values()[4].getLabel()
                }
        );
        _callback = callbackIn;
        _row = rowIn;
    }

    public void showAt(int xIn, int yIn) {

        showMenuAt(xIn, yIn, _menuCallback);
    }
}
