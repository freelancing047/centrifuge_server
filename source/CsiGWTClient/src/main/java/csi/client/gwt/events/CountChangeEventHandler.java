package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class CountChangeEventHandler extends BaseCsiEventHandler {

    public abstract void onCountChange(CountChangeEvent event);
}
