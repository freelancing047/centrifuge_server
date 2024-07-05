package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class ChoiceMadeEventHandler extends BaseCsiEventHandler {

    public abstract void onChoiceMade(ChoiceMadeEvent event);
}
