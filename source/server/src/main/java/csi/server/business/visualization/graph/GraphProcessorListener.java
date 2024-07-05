package csi.server.business.visualization.graph;

import prefuse.data.Edge;
import prefuse.data.Node;

public interface GraphProcessorListener {

    void handleNode(Node node, boolean created);

    void handleEdge(Edge edge, boolean created);
    
    boolean isNewNode(Node node);
    
    boolean isNewEdge(Edge edge);

}
