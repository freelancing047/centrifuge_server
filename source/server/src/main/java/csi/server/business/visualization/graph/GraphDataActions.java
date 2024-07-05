package csi.server.business.visualization.graph;

import java.util.Iterator;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;

public class GraphDataActions {

    public static final String NODE_TYPES_LIST = "types.nodes";
    public static final String LINK_TYPES_LIST = "types.links";

    public Multimap<String, Edge> getOrCreateLinkTypes(Graph graph) {
        Multimap<String, Edge> linkTypes = buildEdgeTypes(graph);
        graph.putClientProperty(LINK_TYPES_LIST, linkTypes);
        return linkTypes;
    }

    private Multimap<String, Edge> buildEdgeTypes(Graph graph) {
        HashMultimap<String, Edge> typeContainer = HashMultimap.create();
        Iterator<Edge> edges = graph.edges();
        while (edges.hasNext()) {
            Edge edge = edges.next();
            LinkStore details = GraphManager.getEdgeDetails(edge);
            Map<String, Integer> types = details.getTypes();
            for (String type : types.keySet()) {
                typeContainer.put(type.trim(), edge);
            }
        }
        return typeContainer;
    }

    public Multimap<String, Node> buildNodeTypes(Graph graph) {

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

}
