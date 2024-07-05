package csi.server.business.visualization.graph.pattern;

import java.util.Iterator;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;

/**
 * Build data from graph needed for the pattern matching algorithm.
 *
 * @author Centrifuge Systems, Inc.
 */
public class GraphPatternData {

    private final Multimap<Integer, Integer> adjacencyMap;
    private final Multimap<String, Node> nodeTypesMap;

    public GraphPatternData(Graph graph) {
        adjacencyMap = buildConnections(graph);
        nodeTypesMap = buildNodeTypes(graph);
    }

    public Multimap<Integer, Integer> getAdjacencyMap() {
        return adjacencyMap;
    }

    public Multimap<String, Node> getNodeTypesMap() {
        return nodeTypesMap;
    }

    @SuppressWarnings("unchecked")
    private Multimap<Integer, Integer> buildConnections(Graph graph) {
        HashMultimap<Integer, Integer> connections = HashMultimap.create();
        Iterator<Edge> edges = graph.edges();

        while (edges.hasNext()) {
            Edge edge = edges.next();
            connections.put(edge.getSourceNode().getRow(), edge.getTargetNode().getRow());
            connections.put(edge.getTargetNode().getRow(), edge.getSourceNode().getRow());
        }
        return connections;
    }

    @SuppressWarnings("unchecked")
    private Multimap<String, Node> buildNodeTypes(Graph graph) {
        HashMultimap<String, Node> typeContainer = HashMultimap.create();
        Iterator<Node> nodes = graph.nodes();

        while (nodes.hasNext()) {
            Node node = nodes.next();
            NodeStore details = GraphManager.getNodeDetails(node);
            Map<String, Integer> types = details.getTypes();
            for (String type : types.keySet()) {
                typeContainer.put(type.trim(), node);
            }
        }

        return typeContainer;
    }

    public static String getTypeFromNode(Node node) {
        NodeStore details = GraphManager.getNodeDetails(node);
        return details.getType();
    }

}
