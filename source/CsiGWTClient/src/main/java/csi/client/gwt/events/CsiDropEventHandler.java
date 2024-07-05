package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;

/**
 * Created by centrifuge on 3/23/2016.
 */
public abstract class CsiDropEventHandler extends BaseCsiEventHandler {

    public abstract void onDrop(CsiDropEvent eventIn);
}
