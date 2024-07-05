package csi.server.business.visualization.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.shared.gwt.viz.graph.LinkDirection;

public class PrefuseToJungTransformer implements Function<Graph, edu.uci.ics.jung.graph.Graph<String, String>> {

    @Override
    public edu.uci.ics.jung.graph.Graph<String, String> apply(Graph prefuseGraph) {
        DirectedSparseMultigraph<String, String> graph = new DirectedSparseMultigraph<String, String>();
//        Map<String, Node> nodeKeyMap = (Map<String, Node>) prefuseGraph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        Set<Edge> pendingEdges = new HashSet<Edge>();

        for (Iterator<Node> nodes = prefuseGraph.nodes(); nodes.hasNext();) {
           Node node = nodes.next();

           if (GraphContext.Predicates.IsNodeDisplayable.test(node) && GraphContext.Predicates.IsNodeVisualized.test(node)) {
              NodeStore details = GraphContext.Functions.GetNodeDetails.apply(node);
              String nodeKey = details.getKey();

              graph.addVertex(nodeKey);

              for (Iterator<Edge> edges = node.edges(); edges.hasNext();) {
                 pendingEdges.add(edges.next());
              }
           }
        }
        Map<String, Edge> edgeKeyMap = (Map<String, Edge>) prefuseGraph.getClientProperty(GraphManager.EDGE_HASH_TABLE);
        boolean requiresEdgeIndex = false;

        if (edgeKeyMap == null) {
            edgeKeyMap = new HashMap<String, Edge>();
            prefuseGraph.putClientProperty(GraphManager.EDGE_HASH_TABLE, edgeKeyMap);

            requiresEdgeIndex = true;
        }

//        Predicate<Node> isDisplayable = GraphContext.Predicates.IsNodeDisplayable;
        for (Edge edge : pendingEdges) {
            LinkStore details = GraphManager.getEdgeDetails(edge);
            if (!details.isDisplayable()) {
                // skipping edge -- one of the nodes is bundled or hidden
                continue;
            }

            LinkDirection direction = details.getDirection();
            if (direction == LinkDirection.FORWARD) {
                graph.addEdge(details.getKey(), new Pair(details.getFirstEndpoint().getKey(), details.getSecondEndpoint().getKey()), EdgeType.DIRECTED);
            } else if (direction == LinkDirection.REVERSE) {
                // Reverse
                graph.addEdge(GraphManager.invertLinkID(details.getKey()), new Pair(details.getSecondEndpoint().getKey(), details.getFirstEndpoint().getKey()), EdgeType.DIRECTED);
            } else {
                // None or both
                graph.addEdge(details.getKey(), new Pair(details.getFirstEndpoint().getKey(), details.getSecondEndpoint().getKey()), EdgeType.DIRECTED);
                graph.addEdge(GraphManager.invertLinkID(details.getKey()), new Pair(details.getSecondEndpoint().getKey(), details.getFirstEndpoint().getKey()), EdgeType.DIRECTED);
            }

            if (requiresEdgeIndex || !edgeKeyMap.containsKey(details.getKey())) {
                edgeKeyMap.put(details.getKey(), edge);
            }
        }

        return graph;
    }
}
