package csi.server.business.visualization.graph.pattern.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Pattern defined by a set of nodes and edges.
 * @author Centrifuge Systems, Inc.
 */
public class NodeEdgeSetPattern implements Pattern, IsSerializable {

    private Set<PatternNode> patternNodes = new HashSet<PatternNode>();
    private Set<PatternEdge> patternEdges = new HashSet<PatternEdge>();
    private Set<Integer> nodeRows = new HashSet<Integer>();

    public Set<PatternNode> getPatternNodes() {
        return Collections.unmodifiableSet(patternNodes);
    }

    public Set<PatternEdge> getPatternEdges() {
        return Collections.unmodifiableSet(patternEdges);
    }

    public Set<Integer> getNodeRows() {
        return new HashSet<Integer>(nodeRows);
    }

    public void addNode(PatternNode patternNode) {
        patternNodes.add(patternNode);
        nodeRows.add(patternNode.getRow());
    }

    public void addEdge(PatternEdge patternEdge) {
        patternEdges.add(patternEdge);
    }

    public String toString(){
        return "Node Rows: " + getNodeRows().toString();
    }

    public NodeEdgeSetPattern clonePattern(){
        NodeEdgeSetPattern nodeEdgeSetPattern = new NodeEdgeSetPattern();

        for (PatternNode patternNode : patternNodes) {
            nodeEdgeSetPattern.addNode(new PatternNode(patternNode.getRow(), patternNode.getType()));

        }
        for(PatternEdge patternEdge : patternEdges){
            nodeEdgeSetPattern.addEdge(new PatternEdge(patternEdge.getSource(), patternEdge.getTarget()));
        }
        return nodeEdgeSetPattern;

    }
}
