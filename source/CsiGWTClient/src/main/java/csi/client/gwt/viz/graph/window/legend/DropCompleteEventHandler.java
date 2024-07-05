package csi.client.gwt.viz.graph.window.legend;

import com.google.gwt.event.shared.EventHandler;

import csi.client.gwt.etc.BaseCsiEventHandler;

public abstract class DropCompleteEventHandler extends BaseCsiEventHandler implements EventHandler {
    
    public abstract void onDropComplete(DropCompleteEvent event);

}
