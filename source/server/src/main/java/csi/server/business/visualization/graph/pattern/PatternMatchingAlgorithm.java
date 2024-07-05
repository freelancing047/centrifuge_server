package csi.server.business.visualization.graph.pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import prefuse.data.Graph;
import prefuse.data.Node;
import csi.server.business.visualization.graph.pattern.model.CandidatePattern;
import csi.server.business.visualization.graph.pattern.model.NodeEdgeSetPattern;
import csi.server.business.visualization.graph.pattern.model.Pattern;
import csi.server.business.visualization.graph.pattern.model.PatternEdge;
import csi.server.business.visualization.graph.pattern.model.PatternNode;

/**
 * Algorithm traverses through the pattern that is passed in as a parameter and builds
 * a candidatePatterns list.
 *
 * @author Centrifuge Systems, Inc.
 */
public class PatternMatchingAlgorithm {

    private static final int MATCH_LIMIT = 1000;

    private Graph graph;
    private GraphPatternData graphPatternData;
    private SimilarNodeFinder similarNodeFinder;

    private Pattern rootPattern;
    private Set<CandidatePattern> candidatePatterns = new HashSet<CandidatePattern>();
    private Set<PatternNode> patternNodesTouchedByTraversal = new HashSet<PatternNode>();

    public List<Pattern> findMatches(Graph graph, Pattern pattern) {
        this.graph = graph;
        this.graphPatternData = new GraphPatternData(graph);
        this.similarNodeFinder = new SimilarNodesByType(graphPatternData);
        this.rootPattern = pattern;

        findMatchingPatterns();

        return extractMatchingPatternsFromCandidates();
    }

    private List<Pattern> extractMatchingPatternsFromCandidates() {
        List<Pattern> matchingPatterns = new LinkedList<Pattern>();

        for (CandidatePattern candidatePattern : candidatePatterns) {
            matchingPatterns.add(candidatePattern.getPattern());
        }
        return matchingPatterns;
    }

    private void findMatchingPatterns() {
        PatternNode patternNode = findNodeWithLeastNumberOfConnectionsWithinRootPattern();
        traverse(patternNode);
    }

    private PatternNode findNodeWithLeastNumberOfConnectionsWithinRootPattern() {
        int minCount = 0;
        PatternNode minPatternNode = null;

        for (PatternNode patternNode : rootPattern.getPatternNodes()) {
            int count = findIncidentNodesOfRootPattern(patternNode).size();
            if (minPatternNode == null) {
                minPatternNode = patternNode;
                minCount = count;
            } else if (count < minCount) {
                minPatternNode = patternNode;
                minCount = count;
            }
        }

        if (minCount == 0) {
            throw new IllegalArgumentException("A pattern must be a connected component with at least one link.");
        }
        return minPatternNode;
    }

    private void traverse(PatternNode currentRootPatternNode) {
        if (patternNodesTouchedByTraversal.contains(currentRootPatternNode)) {
            return;
        }
        patternNodesTouchedByTraversal.add(currentRootPatternNode);

        Collection<Node> nodes = findSimilarNodes(currentRootPatternNode);
        multiplyCandidateResults(nodes, currentRootPatternNode);

        for (PatternNode childrenPatternNode : findIncidentNodesOfRootPattern(currentRootPatternNode)) {
            traverse(childrenPatternNode);
        }
    }

    private Collection<Node> findSimilarNodes(PatternNode patternNode) {
        Collection<Node> similarNodes = new ArrayList<Node>();
        Collection<Node> nodesOfRootType = similarNodeFinder.findSimilarNodes(graph, patternNode);

        for (Node node : nodesOfRootType) {
            if (isValidBasedOnTypeMatchOfChildren(node, patternNode)) {
                similarNodes.add(node);
            }
        }

        return similarNodes;
    }

    private void multiplyCandidateResults(Collection<Node> nodesToMultiply, PatternNode currentPatternNode) {
        if (candidatePatterns.isEmpty()) {
            addNodesAsCandidates(nodesToMultiply, currentPatternNode);
            return;
        }

        Set<CandidatePattern> newCandidates = new HashSet<CandidatePattern>();
        for (CandidatePattern candidatePattern : candidatePatterns) {
            for (Node potentialNodeToAdd : nodesToMultiply) {
                addNodeToCandidatePattern(currentPatternNode, newCandidates, candidatePattern, potentialNodeToAdd);

                if(newCandidates.size() >= MATCH_LIMIT && patternNodesTouchedByTraversal.size() == rootPattern.getNodeRows().size()){
                    candidatePatterns = newCandidates;
                    return;
                }
            }
        }

        candidatePatterns = newCandidates;
    }

    private void addNodeToCandidatePattern(PatternNode currentRootPatternNode, Set<CandidatePattern> newCandidates, CandidatePattern candidatePattern, Node potentialNodeToAdd) {
        if (nodeDoesNotAlreadyExistInCandidatePattern(candidatePattern, potentialNodeToAdd)) {
            if (addingTheNodeDoesNotCreateADuplicateNodeWithinTheCandidate(candidatePattern, potentialNodeToAdd)) {
                CandidatePattern cloneCandidate = addLegToCandidate(candidatePattern, potentialNodeToAdd, currentRootPatternNode);
                if (cloneCandidate != null) {
                    newCandidates.add(cloneCandidate);
                }
            }
        }
    }

    private boolean addingTheNodeDoesNotCreateADuplicateNodeWithinTheCandidate(CandidatePattern candidatePattern, Node potentialNodeToAdd) {
        Set<Integer> candidateRowSet = candidatePattern.getPattern().getNodeRows();
        return candidateRowSet.add(potentialNodeToAdd.getRow());
    }

    private boolean nodeDoesNotAlreadyExistInCandidatePattern(CandidatePattern candidatePattern, Node node) {
        return !(candidatePattern.getPattern().getNodeRows().contains(node.getRow()));
    }

    private CandidatePattern addLegToCandidate(CandidatePattern candidatePattern, Node node, PatternNode currentPatternNode) {
        CandidatePattern clonedPattern = candidatePattern.clonePattern();
        Set<PatternNode> attachableNodes = findAttachableNodes(clonedPattern, node.getRow(), currentPatternNode);

        if (attachableNodes.isEmpty())
            return null;

        PatternNode patternNode = new PatternNode(node.getRow(), GraphPatternData.getTypeFromNode(node));
        clonedPattern.getPattern().addNode(patternNode);
        clonedPattern.addPatternMapping(patternNode, currentPatternNode);

        for (PatternNode attachableNode : attachableNodes) {
            PatternEdge patternEdge = new PatternEdge(attachableNode, patternNode);
            clonedPattern.getPattern().addEdge(patternEdge);
        }

        return clonedPattern;
    }

    private Set<PatternNode> findAttachableNodes(CandidatePattern clonedPattern, int row, PatternNode currentPatternNode) {
        int countOfAttachmentsInRootPattern = findNumberOfAttachments(currentPatternNode);
        Set<PatternNode> attachablePatternNodes = new HashSet<PatternNode>();

        int numberOfIncidentPatternNodes = 0;
        for (PatternNode patternNode : clonedPattern.getPattern().getPatternNodes()) {
            if (patternNodeIsEligibleToBeAttached(clonedPattern, patternNode)) {
                Collection<Integer> incidentRows = graphPatternData.getAdjacencyMap().get(patternNode.getRow());
                if (incidentRows.contains(row)) {
                    attachablePatternNodes.add(patternNode);
                    if (++numberOfIncidentPatternNodes == countOfAttachmentsInRootPattern) {
                        break;
                    }
                }
            }
        }

        if (numberOfIncidentPatternNodes < countOfAttachmentsInRootPattern)
            attachablePatternNodes.clear();

        return attachablePatternNodes;
    }

    private boolean patternNodeIsEligibleToBeAttached(CandidatePattern clonedPattern, PatternNode patternNode) {
        int numberOfUsedEdgesInCandidate = countNumberOfIncidentEdges(clonedPattern.getPattern(), patternNode);
        PatternNode rootPatternNode = clonedPattern.getRootPatternNode(patternNode);
        int numberOfUsedEdgesInRootPattern = countNumberOfIncidentEdges(rootPattern, rootPatternNode);

        return numberOfUsedEdgesInCandidate < numberOfUsedEdgesInRootPattern;
    }

    private int countNumberOfIncidentEdges(Pattern pattern, PatternNode patternNode) {
        int count = 0;
        for (PatternEdge patternEdge : pattern.getPatternEdges()) {
            if (patternEdge.getSource().equals(patternNode) || patternEdge.getTarget().equals(patternNode))
                count++;
        }
        return count;
    }

    private int findNumberOfAttachments(PatternNode currentPatternNode) {
        int attachmentCount = 0;
        for (PatternNode patternNode : patternNodesTouchedByTraversal) {
            if (edgeBetweenInPattern(patternNode, currentPatternNode))
                attachmentCount++;
        }
        return attachmentCount;
    }

    private boolean edgeBetweenInPattern(PatternNode patternNode, PatternNode currentPatternNode) {
        for (PatternEdge patternEdge : rootPattern.getPatternEdges()) {
            if ((patternEdge.getSource().getRow() == patternNode.getRow() && patternEdge.getTarget().getRow() == currentPatternNode.getRow()) ||
                    (patternEdge.getSource().getRow() == currentPatternNode.getRow() && patternEdge.getTarget().getRow() == patternNode.getRow())) {
                return true;
            }
        }
        return false;
    }

    private void addNodesAsCandidates(Collection<Node> nodesToMultiply, PatternNode currentPatternNodeOfRootPattern) {
        for (Node node : nodesToMultiply) {
            PatternNode candidatePatternNode = new PatternNode(node.getRow(), GraphPatternData.getTypeFromNode(node));

            Pattern singleNodePattern = new NodeEdgeSetPattern();
            singleNodePattern.addNode(candidatePatternNode);

            CandidatePattern candidatePattern = new CandidatePattern(singleNodePattern, candidatePatternNode, currentPatternNodeOfRootPattern);
            candidatePatterns.add(candidatePattern);
        }
    }

    private boolean isValidBasedOnTypeMatchOfChildren(Node nodeOfType, PatternNode patternNode) {
        List<String> incidentTypes = getIncidentTypesOfNode(nodeOfType);

        for (Integer incidentNodeRow : getIncidentRowsOfRootPattern(patternNode)) {
            Node childNode = graph.getNode(incidentNodeRow);
            String typeFromNode = GraphPatternData.getTypeFromNode(childNode);
            if (!incidentTypes.contains(typeFromNode)) {
                return false;
            }
            incidentTypes.remove(typeFromNode);
        }
        return true;
    }

    private Set<PatternNode> findIncidentNodesOfRootPattern(PatternNode currentRootPatternNode) {
        Set<PatternNode> patternNodes = new HashSet<PatternNode>();

        for (PatternEdge patternEdge : rootPattern.getPatternEdges()) {
            PatternNode foundNode = findIncidentNode(currentRootPatternNode, patternEdge);
            if (foundNode != null)
                patternNodes.add(foundNode);
        }

        return patternNodes;
    }

    private PatternNode findIncidentNode(PatternNode patternNode, PatternEdge patternEdge) {
        if (patternEdge.getSource().equals(patternNode)) {
            return patternEdge.getTarget();
        }
        if (patternEdge.getTarget().equals(patternNode)) {
            return patternEdge.getSource();
        }
        return null;
    }

    private Set<Integer> getIncidentRowsOfRootPattern(PatternNode currentRootPatternNode) {
        Set<Integer> incidentRows = new HashSet<Integer>();
        for (PatternNode patternNode : findIncidentNodesOfRootPattern(currentRootPatternNode)) {
            incidentRows.add(patternNode.getRow());
        }
        return incidentRows;
    }

    private List<String> getIncidentTypesOfNode(Node nodeOfType) {
        Collection<Integer> incidentNodeRows = graphPatternData.getAdjacencyMap().get(nodeOfType.getRow());
        List<String> incidentTypes = new ArrayList<String>();

        for (Integer incidentNodeRow : incidentNodeRows) {
            Node childNode = graph.getNode(incidentNodeRow);
            incidentTypes.add(GraphPatternData.getTypeFromNode(childNode));
        }
        return incidentTypes;
    }

}
