package csi.client.gwt.viz.timeline.events;

import csi.client.gwt.etc.BaseCsiEventHandler;

public abstract class SelectionChangeEventHandler extends BaseCsiEventHandler{

	public abstract void onSelect(SelectionChangeEvent event);
	
}
