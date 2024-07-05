package csi.client.gwt.viz.graph.events;

import java.util.EnumSet;
import java.util.Set;

import com.google.gwt.event.shared.EventHandler;

public abstract class GraphEventHandler implements EventHandler {

    private Set<GraphEvents> subtypes = EnumSet.noneOf(GraphEvents.class);


    public abstract void onGraphEvent(GraphEvent event);


    public void subTypeCheck(GraphEvent event) {
        if (subtypes.contains(event.getSubType())) {
            onGraphEvent(event);
        }
    }


    public void addSubType(GraphEvents graphEvents) {
        subtypes.add(graphEvents);
    }


    public void removeSubType(GraphEvents graphEvents) {
        subtypes.remove(graphEvents);
    }
}
