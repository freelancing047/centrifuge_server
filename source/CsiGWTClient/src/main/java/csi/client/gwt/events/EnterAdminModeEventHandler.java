package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class EnterAdminModeEventHandler extends BaseCsiEventHandler {

    public abstract void onEnterAdminMode(EnterAdminModeEvent event);
}
