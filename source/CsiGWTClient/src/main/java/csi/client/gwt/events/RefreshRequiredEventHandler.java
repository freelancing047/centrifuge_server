package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


public abstract class RefreshRequiredEventHandler extends BaseCsiEventHandler {

    public abstract void onRefreshRequired(RefreshRequiredEvent event);
}
