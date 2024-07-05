package csi.client.gwt.viz.timeline.events;

import csi.client.gwt.etc.BaseCsiEventHandler;

public abstract class CrumbRemovedEventHandler extends BaseCsiEventHandler{

	public abstract void onRemove(CrumbRemovedEvent event);
	
}
