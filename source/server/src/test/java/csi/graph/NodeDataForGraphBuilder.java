package csi.graph;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NodeDataForGraphBuilder {

    private final String label;
    private final String type;
    private final String key;

    public NodeDataForGraphBuilder(String label, String type) {
        this(label, type, label);
    }

    public NodeDataForGraphBuilder(String label, String type, String key) {
        this.label = label;
        this.type = type;
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeDataForGraphBuilder that = (NodeDataForGraphBuilder) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
