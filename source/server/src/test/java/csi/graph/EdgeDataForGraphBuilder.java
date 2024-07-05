package csi.graph;

/**
 * @author Centrifuge Systems, Inc.
 */
public class EdgeDataForGraphBuilder {

    private final String originKey;
    private final String destinationKey;

    public EdgeDataForGraphBuilder(String originKey, String destinationKey) {
        this.originKey = originKey;
        this.destinationKey = destinationKey;
    }

    public String getOriginKey() {
        return originKey;
    }

    public String getDestinationKey() {
        return destinationKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EdgeDataForGraphBuilder that = (EdgeDataForGraphBuilder) o;

        if (!destinationKey.equals(that.destinationKey)) return false;
        if (!originKey.equals(that.originKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = originKey.hashCode();
        result = 31 * result + destinationKey.hashCode();
        return result;
    }
}

