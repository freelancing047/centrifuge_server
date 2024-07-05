package csi.server.common.dto.graph;



import prefuse.data.Graph;

import com.google.gwt.user.client.rpc.IsSerializable;


public class EdgeListing implements IsSerializable {

    private final Graph graph;

    public EdgeListing(Graph graph) {
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }

}
