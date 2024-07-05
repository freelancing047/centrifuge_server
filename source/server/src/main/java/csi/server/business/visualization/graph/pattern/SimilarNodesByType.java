package csi.server.business.visualization.graph.pattern;

import java.util.Collection;

import prefuse.data.Graph;
import prefuse.data.Node;
import csi.server.business.visualization.graph.pattern.model.PatternNode;

/**
 * Find similar nodes by type.
 *
 * @author Centrifuge Systems, Inc.
 */
public class SimilarNodesByType implements SimilarNodeFinder {

    private final GraphPatternData graphPatternData;

    public SimilarNodesByType(GraphPatternData graphPatternData) {
        this.graphPatternData = graphPatternData;
    }

    @Override
    public Collection<Node> findSimilarNodes(Graph graph, PatternNode patternNode) {
        return graphPatternData.getNodeTypesMap().get(patternNode.getType());
    }
}
