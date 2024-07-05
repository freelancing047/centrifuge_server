package csi.shared.core.visualization.map;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Objects;

public class MapSummaryExtent implements Serializable, IsSerializable {
    private Double xMin;
    private Double yMin;
    private Double xMax;
    private Double yMax;

    public MapSummaryExtent() {
    }

    public MapSummaryExtent(Double xmin, Double ymin, Double xmax, Double ymax) {
        super();
        while (xmax < -180) {
            xmax = xmax + 360;
            xmin = xmin + 360;
        }
        this.xMin = xmin;
        this.yMin = ymin;
        this.xMax = xmax;
        this.yMax = ymax;
    }

    public Double getXMin() {
        return xMin;
    }

    public void setXMin(Double xMin) {
        this.xMin = xMin;
    }

    public Double getYMin() {
        return yMin;
    }

    public void setYMin(Double yMin) {
        this.yMin = yMin;
    }

    public Double getXMax() {
        return xMax;
    }

    public void setXMax(Double xMax) {
        this.xMax = xMax;
    }

    public Double getYMax() {
        return yMax;
    }

    public void setYMax(Double yMax) {
        this.yMax = yMax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapSummaryExtent that = (MapSummaryExtent) o;
        return Objects.equals(xMin, that.xMin) &&
                Objects.equals(yMin, that.yMin) &&
                Objects.equals(xMax, that.xMax) &&
                Objects.equals(yMax, that.yMax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xMin, yMin, xMax, yMax);
    }

    public String toString() {
        return xMin + "," + yMin + ";" + xMax + "," + yMax;
    }

    public boolean isPointInExtent(double x, double y) {
        return x > xMin && x < xMax && y > yMin && y < yMax;
    }
}
