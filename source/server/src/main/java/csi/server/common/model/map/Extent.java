package csi.server.common.model.map;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Objects;

public class Extent implements IsSerializable, Serializable {
    private static final double MIN_DIAMETER = 0.0072464;
    private static final double MIN_RADIUS = MIN_DIAMETER / 2;

    private double xmin = 181;
    private double ymin = 91;
    private double xmax = -181;
    private double ymax = -91;
    private SpatialReference spatialReference;
    private int zoom = -1;

    public Extent() {
    }


    public Extent(double xmin, double xmax, double ymin, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public static void expandExtentIfTooSmall(Extent extent) {
        expandXExtentIfTooSmall(extent);
        expandYExtentIfTooSmall(extent);
    }

    private static void expandXExtentIfTooSmall(Extent extent) {
        double deltaX = extent.getXmax() - extent.getXmin();
        if (deltaX < MIN_DIAMETER) {
            double centerX = (extent.getXmax() + extent.getXmin()) / 2;
            extent.setXmin(centerX - MIN_RADIUS);
            extent.setXmax(centerX + MIN_RADIUS);
        }
    }

    private static void expandYExtentIfTooSmall(Extent extent) {
        double deltaY = extent.getYmax() - extent.getYmin();
        if (deltaY < MIN_DIAMETER) {
            double centerY = (extent.getYmax() + extent.getYmin()) / 2;
            extent.setYmin(centerY - MIN_RADIUS);
            extent.setYmax(centerY + MIN_RADIUS);
        }
    }

    public void addPoint(Geometry geometry) {
        double x = geometry.getX();
        if (xmin > x) {
            xmin = x;
        }
        if (xmax < x) {
            xmax = x;
        }
        double y = geometry.getY();
        if (ymin > y) {
            ymin = y;
        }
        if (ymax < y) {
            ymax = y;
        }
    }

    public double getXmin() {
        return xmin;
    }

    public void setXmin(double xmin) {
        this.xmin = xmin;
    }

    public double getYmin() {
        return ymin;
    }

    public void setYmin(double ymin) {
        this.ymin = ymin;
    }

    public double getXmax() {
        return xmax;
    }

    public void setXmax(double xmax) {
        this.xmax = xmax;
    }

    public double getYmax() {
        return ymax;
    }

    public void setYmax(double ymax) {
        this.ymax = ymax;
    }

    public SpatialReference getSpatialReference() {
        return spatialReference;
    }

    public void setSpatialReference(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public int hashCode() {
        return Objects.hash(xmin, ymin, xmax, ymax);
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Extent other = (Extent) obj;
        if (Objects.hash(xmin) != Objects.hash(other.xmin))
            return false;
        if (Objects.hash(ymin) != Objects.hash(other.ymin))
            return false;
        if (Objects.hash(xmax) != Objects.hash(other.xmax))
            return false;
        return Objects.hash(ymax) == Objects.hash(other.ymax);
    }

    public boolean equals(Extent other) {
        return hashCode() == other.hashCode();
    }

    public String toString() {
        return "xmin: " + xmin + ", " +
                "ymin: " + ymin + ", " +
                "xmax: " + xmax + ", " +
                "ymax: " + ymax;
    }
}
