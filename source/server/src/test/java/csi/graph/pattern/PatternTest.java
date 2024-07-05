package csi.graph.pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import csi.graph.TestGraphBuilder;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.pattern.PatternMatchingAlgorithm;
import csi.server.business.visualization.graph.pattern.model.Pattern;
import csi.server.business.visualization.graph.pattern.selection.SelectionToNodeEdgeSetPattern;
import csi.server.common.model.visualization.selection.SelectionModel;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PatternTest {

    @Test
    public void testTwoNodesPattern(){
        Graph sampleGraph = TestGraphBuilder.createSampleGraph();
        Pattern pattern = getPattern(sampleGraph, createTwoNodeSelectionModel(sampleGraph));

        PatternMatchingAlgorithm matchingAlgorithm = new PatternMatchingAlgorithm();
        List<Pattern> results = matchingAlgorithm.findMatches(sampleGraph, pattern);

        assertEquals(results.size(), 6);
        assertContains(results, 1, 4);
        assertContains(results, 1, 3);
        assertContains(results, 1, 2);
        assertContains(results, 4, 7);
        assertContains(results, 0, 3);
        assertContains(results, 0, 2);
    }

    private SelectionModel createTwoNodeSelectionModel(Graph graph) {
        return createSelectionModel(graph, varargs(0,2), varargs("0+2"));
    }

    @Test
    public void testThreeNodesPattern(){
        Graph sampleGraph = TestGraphBuilder.createSampleGraph();
        Pattern pattern = getPattern(sampleGraph, createThreeNodeSelectionModel(sampleGraph));

        PatternMatchingAlgorithm matchingAlgorithm = new PatternMatchingAlgorithm();

        List<Pattern> results = matchingAlgorithm.findMatches(sampleGraph, pattern);

        assertEquals(results.size(), 4);
        assertContains(results, 0, 2, 3);
        assertContains(results, 1, 2, 4);
        assertContains(results, 1, 2, 3);
        assertContains(results, 1, 3, 4);

    }

    private SelectionModel createThreeNodeSelectionModel(Graph graph) {
        return createSelectionModel(graph, varargs(0,2,3), varargs("0+2", "0+3"));
    }

    @Test
    public void testSquarePattern(){
        Graph sampleGraph = TestGraphBuilder.createSampleGraph();
        Pattern pattern = getPattern(sampleGraph, createSquareSelectionModel(sampleGraph));

        PatternMatchingAlgorithm matchingAlgorithm = new PatternMatchingAlgorithm();

        List<Pattern> results = matchingAlgorithm.findMatches(sampleGraph, pattern);

        assertEquals(results.size(), 4);
        assertContains(results, 0, 2, 3, 5);
        assertContains(results, 1, 2, 3, 5);
        assertContains(results, 1, 2, 4, 5);
        assertContains(results, 1, 3, 4, 5);

    }

    private SelectionModel createSquareSelectionModel(Graph graph) {
        return createSelectionModel(graph, varargs(0,2,3,5), varargs("0+2","0+3","2+5","3+5"));
    }

    @Test
    public void testPathPattern(){
        Graph sampleGraph = TestGraphBuilder.createSampleGraph();
        Pattern pattern = getPattern(sampleGraph, createPathPatternSelectionModel(sampleGraph));

        PatternMatchingAlgorithm matchingAlgorithm = new PatternMatchingAlgorithm();

        List<Pattern> results = matchingAlgorithm.findMatches(sampleGraph, pattern);

        assertEquals(results.size(), 6);
        assertContains(results, 0, 2, 3, 5);
        assertContains(results, 1, 2, 3, 5);
        assertContains(results, 1, 2, 4, 5);
        assertContains(results, 1, 2, 4, 6);
        assertContains(results, 1, 3, 4, 5);
        assertContains(results, 1, 3, 4, 6);

    }

    private SelectionModel createPathPatternSelectionModel(Graph graph) {
        return createSelectionModel(graph, varargs(0,2,3,5), varargs("0+2","0+3","3+5"));
    }

    @Test
    public void testLongPathPattern(){
        Graph sampleGraph = TestGraphBuilder.createSampleGraph();
        Pattern pattern = getPattern(sampleGraph, createLongPathPatternSelectionModel(sampleGraph));

        PatternMatchingAlgorithm matchingAlgorithm = new PatternMatchingAlgorithm();

        List<Pattern> results = matchingAlgorithm.findMatches(sampleGraph, pattern);

        assertEquals(results.size(), 3);
        assertContains(results, 0, 1, 2, 3, 4, 5);
        assertContains(results, 1, 2, 3, 4, 5, 7);
        assertContains(results, 0, 2, 3, 4, 5, 7);

    }

    private SelectionModel createLongPathPatternSelectionModel(Graph graph) {
        return createSelectionModel(graph, varargs(7,4,5,2,1,3), varargs("4+7","4+5","2+5","1+2","1+3"));
    }


    private Pattern getPattern(Graph graph, SelectionModel selectionModel) {
        SelectionToNodeEdgeSetPattern selectionToNodeEdgeSetPattern = new SelectionToNodeEdgeSetPattern();
        return selectionToNodeEdgeSetPattern.createPattern(selectionModel, graph);
    }

    @SuppressWarnings("unchecked")
    private SelectionModel createSelectionModel(Graph graph, Set<Integer> nodes, Set<String> edgeKeys) {
        Map<String, Node> nodeMap = (Map<String, Node>)graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        Map<String, Edge> edgeMap = (Map<String, Edge>)graph.getClientProperty(GraphManager.EDGE_HASH_TABLE);

        SelectionModel selectionModel = new SelectionModel();
        for(Integer nodeKey : nodes){
            selectionModel.nodes.add(nodeMap.get(String.valueOf(nodeKey)).getRow());
        }
        for(String edgeKey: edgeKeys){
            selectionModel.links.add(edgeMap.get(String.valueOf(edgeKey)).getRow());
        }

        return selectionModel;
    }

    private void assertContains(List<Pattern> results, Integer ... rows) {
        boolean found = false;
        for(Pattern p : results){
            Set<Integer> rowsAsSet = varargs(rows);
            if(p.getNodeRows().equals(rowsAsSet))
                found = true;
        }
        assertTrue(found);
    }

    private <T> Set<T> varargs(T... rows) {
        Set<T> rowsAsSet = new HashSet<T>();
        rowsAsSet.addAll(Arrays.asList(rows));
        return rowsAsSet;
    }
}
