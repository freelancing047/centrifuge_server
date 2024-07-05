package csi.server.business.visualization.graph.pattern.model;

import java.util.Set;

/**
 * A pattern is a single component graph.
 * @author Centrifuge Systems, Inc.
 */
public interface Pattern {

    public void addNode(PatternNode patternNode);
    public void addEdge(PatternEdge patternEdge);

    public Set<PatternNode> getPatternNodes();
    public Set<PatternEdge> getPatternEdges();

    public Set<Integer> getNodeRows();

    public Pattern clonePattern();

}
