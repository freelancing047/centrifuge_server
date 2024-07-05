package csi.server.business.visualization.graph.pattern;

import java.util.ArrayList;
import java.util.List;

import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.pattern.model.Pattern;
import csi.server.business.visualization.graph.pattern.model.PatternEdge;
import csi.server.business.visualization.graph.pattern.model.PatternMeta;
import csi.server.business.visualization.graph.pattern.model.PatternNode;
import csi.server.business.visualization.graph.pattern.selection.SelectionToNodeEdgeSetPattern;
import csi.server.business.visualization.graph.pattern.selection.SelectionToPattern;
import csi.server.common.model.visualization.selection.SelectionModel;

/**
 * Runs pattern matching algorithm against the current graph selection.
 *
 * @author Centrifuge Systems, Inc.
 */
public class FindPatternsFromSelection {

    private final Graph graph;

    public FindPatternsFromSelection(Graph graph) {
        this.graph = graph;
    }

    public List<PatternMeta> find(String uuid) {
        Pattern rootPattern = createPatternFromSelection(uuid);
        PatternMatchingAlgorithm matchingAlgorithm = new PatternMatchingAlgorithm();

        List<Pattern> results = matchingAlgorithm.findMatches(graph, rootPattern);
        return convertCandidatesToOutputResult(results);
    }

    private List<PatternMeta> convertCandidatesToOutputResult(List<Pattern> results) {
        List<PatternMeta> metaResults = new ArrayList<PatternMeta>();
//        int i = 0;
//        for (Pattern candidatePattern : results) {
//            PatternMeta patternMeta = new PatternMeta();
//            patternMeta.setPattern((NodeEdgeSetPattern) candidatePattern);
//            patternMeta.setIndex(i++);
//            patternMeta.setPatternNodes(buildString(candidatePattern));
//            metaResults.add(patternMeta);
//        }
        return metaResults;
    }

    private String buildString(Pattern candidatePattern) {
        StringBuilder sb = new StringBuilder();
        for (PatternEdge patternEdge : candidatePattern.getPatternEdges()) {
            PatternNode sourceNode = patternEdge.getSource();
            PatternNode targetNode = patternEdge.getTarget();

            Node source = graph.getNode(sourceNode.getRow());
            Node target = graph.getNode(targetNode.getRow());
            NodeStore sourceDetails = GraphManager.getNodeDetails(source);
            NodeStore targetDetails = GraphManager.getNodeDetails(target);

            sb.append(sourceDetails.getLabel());
            sb.append("--");
            sb.append(targetDetails.getLabel());
            sb.append(" ");

        }
        return sb.toString();
    }

    private Pattern createPatternFromSelection(String uuid) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);
        SelectionModel selection = graphContext.getSelection(GraphManager.DEFAULT_SELECTION);
        SelectionToPattern selectionToPattern = new SelectionToNodeEdgeSetPattern();
        return selectionToPattern.createPattern(selection, graph);
    }

}
