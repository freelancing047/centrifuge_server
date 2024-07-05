package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class ExitAdminModeEventHandler extends BaseCsiEventHandler {

    public abstract void onExitAdminMode(ExitAdminModeEvent event);
}
