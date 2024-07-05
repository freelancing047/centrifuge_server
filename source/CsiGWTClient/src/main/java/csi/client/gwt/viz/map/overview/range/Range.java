package csi.client.gwt.viz.map.overview.range;

/**
 * @author Centrifuge Systems, Inc.
 */
public class Range {
    public static Range EMPTY_RANGE = new Range(-1,-1);
    private final int startIndex;
    private final int endIndex;

    public Range(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public boolean isEmpty(){
        return equals(EMPTY_RANGE);
    }

    public int getDifference() {
        return this.endIndex - this.startIndex+1;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public String toString(){
        return startIndex + " - " + endIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;
        return endIndex == range.endIndex && startIndex == range.startIndex;
    }

    @Override
    public int hashCode() {
        int result = startIndex;
        result = 31 * result + endIndex;
        return result;
    }

}
