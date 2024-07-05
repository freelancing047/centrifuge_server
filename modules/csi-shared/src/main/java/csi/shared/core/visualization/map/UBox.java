package csi.shared.core.visualization.map;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Objects;

public class UBox implements Serializable, IsSerializable {
    private MapSummaryExtent mapSummaryExtent;
    private Integer rangeStart;
    private Integer rangeEnd;
    private Integer rangeSize;

    public UBox() {
    }

    public UBox(MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSieze) {
        this.mapSummaryExtent = mapSummaryExtent;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.rangeSize = rangeSieze;
    }

    public MapSummaryExtent getMapSummaryExtent() {
        return mapSummaryExtent;
    }

    public Integer getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Integer rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Integer getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(Integer rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public Integer getRangeSize() {
        return rangeSize;
    }

    public void setRangeSize(Integer rangeSize) {
        this.rangeSize = rangeSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UBox uBox = (UBox) o;
        return Objects.equals(mapSummaryExtent, uBox.mapSummaryExtent) &&
                Objects.equals(rangeStart, uBox.rangeStart) &&
                Objects.equals(rangeEnd, uBox.rangeEnd) &&
                Objects.equals(rangeSize, uBox.rangeSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
    }
}
