package csi.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.model.visualization.graph.GraphConstants;

/**
 * @author Centrifuge Systems, Inc.
 */
public class GraphBuilder {

    private final List<NodeDataForGraphBuilder> nodes = new LinkedList<NodeDataForGraphBuilder>();
    private final List<EdgeDataForGraphBuilder> edges = new LinkedList<EdgeDataForGraphBuilder>();
    private final Map<String, Node> nodeKeyToNode = new HashMap<String, Node>();
    private final Map<String, Edge> edgeKeyToEdge = new HashMap<String, Edge>();

    public void reset(){
        nodes.clear();
        edges.clear();
        nodeKeyToNode.clear();
        edgeKeyToEdge.clear();
    }

    public void addNode(String label, String type){
        NodeDataForGraphBuilder nodeData = new NodeDataForGraphBuilder(label, type);
        nodes.add(nodeData);
    }

    public void addEdge(String originKey, String destinationKey){
        EdgeDataForGraphBuilder edgeData = new EdgeDataForGraphBuilder(originKey, destinationKey);
        edges.add(edgeData);
    }

    public Graph build(){
        Graph graph = initializeNewGraph();
        addNodesToGraph(graph);
        addEdgesToGraph(graph);

        return graph;
    }

    private void addEdgesToGraph(Graph graph) {
        for (EdgeDataForGraphBuilder edgeData : edges) {
            Node origin = nodeKeyToNode.get(edgeData.getOriginKey());
            Node destination = nodeKeyToNode.get(edgeData.getDestinationKey());
            Edge edge = graph.addEdge(origin, destination);
            String edgeKey = edgeData.getOriginKey() + "+" + edgeData.getDestinationKey();
            edgeKeyToEdge.put(edgeKey, edge);
        }
        graph.putClientProperty(GraphManager.EDGE_HASH_TABLE, edgeKeyToEdge);
    }

    private void addNodesToGraph(Graph graph) {
        for (NodeDataForGraphBuilder nodeData : nodes) {
            Node node = graph.addNode();
            NodeStore details = createDetails(nodeData);
            GraphManager.setNodeDetails(node, details);
            nodeKeyToNode.put(nodeData.getKey(), node);
        }
        graph.putClientProperty(GraphManager.NODE_HASH_TABLE, nodeKeyToNode);
    }

    private NodeStore createDetails(NodeDataForGraphBuilder nodeData) {
        NodeStore details = new NodeStore();
        details.addLabel(nodeData.getLabel());
        details.addType(nodeData.getType());
        details.setKey(nodeData.getKey());
        return details;
    }

    private Graph initializeNewGraph() {
        Graph graph = new Graph();
        graph.addColumn(GraphConstants.NODE_DETAIL, NodeStore.class);
        graph.addColumn(GraphConstants.LINK_DETAIL, LinkStore.class);
        return graph;
    }

}
