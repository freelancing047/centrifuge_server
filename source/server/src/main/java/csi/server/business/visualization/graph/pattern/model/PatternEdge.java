package csi.server.business.visualization.graph.pattern.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains information regarding edges relevant to pattern matching.
 *
 * @author Centrifuge Systems, Inc.
 */
public class PatternEdge implements IsSerializable {
    private PatternNode source;
    private PatternNode target;

    //Note: when changing this from a hardcode, regenerate the equals and hashcode methods.
    private boolean directed = false;

    public PatternEdge() {

    }

    public PatternEdge(PatternNode source, PatternNode target) {
        this.source = source;
        this.target = target;
    }

    public PatternNode getSource() {
        return source;
    }

    public PatternNode getTarget() {
        return target;
    }

    public boolean isDirected() {
        return directed;
    }

   @Override
   public boolean equals(Object o) {
      return (this == o) ||
             ((o != null) &&
              (o instanceof PatternEdge) &&
              (((source == null) && (((PatternEdge) o).source == null)) ||
               source.equals(((PatternEdge) o).source)) &&
              (((target == null) && (((PatternEdge) o).target == null)) ||
               target.equals(((PatternEdge) o).target)));
 }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = (31 * result) + (target != null ? target.hashCode() : 0);
        return result;
    }
}
