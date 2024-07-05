package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class DataChangeEventHandler extends BaseCsiEventHandler {

    public abstract void onDataChange(DataChangeEvent event);
}
