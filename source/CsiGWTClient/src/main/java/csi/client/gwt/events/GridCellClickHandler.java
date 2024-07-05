package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;

/**
 * Created by centrifuge on 4/24/2019.
 */

public abstract class GridCellClickHandler extends BaseCsiEventHandler {

    public abstract void onGridCellClick(GridCellClick eventIn);
}
