package csi.server.business.visualization.graph.pattern.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains information regarding nodes relevant to pattern matching.
 * @author Centrifuge Systems, Inc.
 */
public class PatternNode implements IsSerializable {

    private int row;
    private String type;

    public PatternNode(){
    }

    public PatternNode(int row, String type) {
        this.row = row;
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatternNode that = (PatternNode) o;

        return row == that.row && !(type != null ? !type.equals(that.type) : that.type != null);
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
