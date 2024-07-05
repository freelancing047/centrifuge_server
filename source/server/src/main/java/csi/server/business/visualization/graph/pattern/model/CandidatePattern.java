package csi.server.business.visualization.graph.pattern.model;

import java.util.HashMap;
import java.util.Map;

/**
 * We construct these candidates while traversing a pattern in order to find a match.
 * The traversed pattern is known as the rootPattern.
 *
 * This object wraps a pattern and provides a mapping between the wrapper pattern and the rootPattern.
 * @author Centrifuge Systems, Inc.
 */
public final class CandidatePattern{

    private final Pattern pattern;
    private final Map<PatternNode, PatternNode> candidatePatternNodesToRootPatternNodes = new HashMap<PatternNode, PatternNode>();

    public CandidatePattern(Pattern pattern, PatternNode candidatePatternNode, PatternNode rootPatternNode) {
        this.pattern = pattern;
        candidatePatternNodesToRootPatternNodes.put(candidatePatternNode, rootPatternNode);
    }

    private CandidatePattern(Pattern pattern, Map<PatternNode, PatternNode> patternNodeMap) {
        this.pattern = pattern;
        candidatePatternNodesToRootPatternNodes.putAll(patternNodeMap);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void addPatternMapping(PatternNode patternNode, PatternNode currentPatternNode) {
        candidatePatternNodesToRootPatternNodes.put(patternNode, currentPatternNode);
    }

    public PatternNode getRootPatternNode(PatternNode candidatePatternNode){
        return candidatePatternNodesToRootPatternNodes.get(candidatePatternNode);
    }

    public CandidatePattern clonePattern(){
        Pattern clonePattern = getPattern().clonePattern();
        return new CandidatePattern(clonePattern, candidatePatternNodesToRootPatternNodes);
    }

   @Override
   public boolean equals(Object o) {
      return (this == o) ||
             ((o != null) &&
              (o instanceof CandidatePattern) &&
              (((pattern == null) && (((CandidatePattern) o).pattern == null)) ||
               pattern.getNodeRows().equals(((CandidatePattern) o).pattern.getNodeRows())));
   }

    @Override
    public int hashCode() {
        return pattern != null ? pattern.getNodeRows().hashCode() : 0;
    }
}
