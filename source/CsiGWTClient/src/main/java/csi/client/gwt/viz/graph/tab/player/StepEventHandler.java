package csi.client.gwt.viz.graph.tab.player;

import com.google.gwt.event.shared.EventHandler;

public abstract class StepEventHandler implements EventHandler {
    public abstract void onStepEvent(StepEvent event);
}
