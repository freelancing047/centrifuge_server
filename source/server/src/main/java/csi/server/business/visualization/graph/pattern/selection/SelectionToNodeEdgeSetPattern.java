package csi.server.business.visualization.graph.pattern.selection;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import csi.server.business.visualization.graph.pattern.GraphPatternData;
import csi.server.business.visualization.graph.pattern.model.NodeEdgeSetPattern;
import csi.server.business.visualization.graph.pattern.model.Pattern;
import csi.server.business.visualization.graph.pattern.model.PatternEdge;
import csi.server.business.visualization.graph.pattern.model.PatternNode;
import csi.server.common.model.visualization.selection.SelectionModel;

/**
 * Converts selection into a NodeEdgeSetPattern.
 *
 * @author Centrifuge Systems, Inc.
 */
public class SelectionToNodeEdgeSetPattern implements SelectionToPattern {

    @Override
    public Pattern createPattern(SelectionModel selection, Graph graph) {
        NodeEdgeSetPattern pattern = new NodeEdgeSetPattern();
        for (int nodeRow : selection.nodes) {
            Node node = graph.getNode(nodeRow);
            pattern.addNode(createPatternNode(node));
        }

        for (int edgeRow : selection.links) {
            Edge edge = graph.getEdge(edgeRow);
            pattern.addEdge(createPatternEdge(edge));
        }

        return pattern;
    }

    private PatternEdge createPatternEdge(Edge edge) {
        return new PatternEdge(createPatternNode(edge.getSourceNode()), createPatternNode(edge.getTargetNode()));
    }

    private PatternNode createPatternNode(Node node) {
        return new PatternNode(node.getRow(), GraphPatternData.getTypeFromNode(node));
    }
}
