package csi.client.gwt.viz.graph.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.viz.graph.Graph;

public class GraphEvent extends GwtEvent<GraphEventHandler> {

    public static final Type<GraphEventHandler> TYPE = new Type<GraphEventHandler>();
    private final GraphEvents subType;
    private final Graph graph;


    public GraphEvent(Graph graph, GraphEvents subType) {
        this.graph = graph;
        this.subType = subType;
    }


    @Override
    protected void dispatch(GraphEventHandler handler) {
        handler.subTypeCheck(this);
    }


    @Override
    public Type<GraphEventHandler> getAssociatedType() {
        return TYPE;
    }


    public Graph getGraph() {
        return graph;
    }


    public GraphEvents getSubType() {
        return subType;
    }
}