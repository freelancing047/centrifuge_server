package csi.graph;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import csi.server.business.visualization.graph.GraphManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TestGraphBuilder {

    @Test
    public void testGraphBuilder(){
        Graph graph = createSampleGraph();

        assertEquals(8, graph.getNodeCount());
        assertEquals(10, graph.getEdgeCount());

        Map<String, Node> nodeMap = (Map<String, Node>)graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        Map<String, Edge> edgeMap = (Map<String, Edge>)graph.getClientProperty(GraphManager.EDGE_HASH_TABLE);
        assertEquals(8, nodeMap.size());
        assertEquals(10, edgeMap.size());

    }

    public static Graph createSampleGraph() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.addNode("0", "BLACK");
        graphBuilder.addNode("1", "BLACK");
        graphBuilder.addNode("2", "GREEN");
        graphBuilder.addNode("3", "GREEN");
        graphBuilder.addNode("4", "GREEN");
        graphBuilder.addNode("5", "BLUE");
        graphBuilder.addNode("6", "BLUE");
        graphBuilder.addNode("7", "BLACK");

        graphBuilder.addEdge("0", "2");
        graphBuilder.addEdge("0", "3");
        graphBuilder.addEdge("1", "2");
        graphBuilder.addEdge("1", "3");
        graphBuilder.addEdge("1", "4");
        graphBuilder.addEdge("2", "5");
        graphBuilder.addEdge("3", "5");
        graphBuilder.addEdge("4", "5");
        graphBuilder.addEdge("4", "6");
        graphBuilder.addEdge("4", "7");

        return graphBuilder.build();
    }


}
