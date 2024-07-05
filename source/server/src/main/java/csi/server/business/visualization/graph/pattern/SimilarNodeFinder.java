package csi.server.business.visualization.graph.pattern;

import java.util.Collection;

import prefuse.data.Graph;
import prefuse.data.Node;
import csi.server.business.visualization.graph.pattern.model.PatternNode;

/**
 * Compute similar nodes from the graph.
 *
 * @author Centrifuge Systems, Inc.
 */
public interface SimilarNodeFinder {

    public Collection<Node> findSimilarNodes(Graph graph, PatternNode patternNode);
}
