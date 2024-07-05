package csi.server.common.model.map;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Objects;

public class Geometry implements Comparable<Geometry>, IsSerializable, Serializable {
    private int summaryLevel;
    private double x;
    private double y;

    public Geometry() {
    }

    public Geometry(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, summaryLevel);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Geometry that = (Geometry) obj;
        return Objects.equals(summaryLevel, that.summaryLevel) &&
                Objects.equals(x, that.x) &&
                Objects.equals(y, that.y);
    }

    @Override
    public int compareTo(Geometry o) {
        if (o == null)
            throw new NullPointerException();
        if (this.equals(o)) {
            return 0;
        } else {
            if (summaryLevel < o.summaryLevel) {
                return 1;
            } else if (summaryLevel > o.summaryLevel) {
                return -1;
            } else {
                if (Objects.equals(x, o.x)) {
                    if (Objects.equals(y, o.y)) {
                        return 0;
                    } else if (y < o.y) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (x < o.x) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }

    public int getSummaryLevel() {
        return summaryLevel;
    }

    public void setSummaryLevel(int summaryLevel) {
        this.summaryLevel = summaryLevel;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String toString() {
        return "summaryLevel: " + summaryLevel + "; x: " + x + "; y: " + y;
    }

    public boolean contains(Geometry geometry) {
        double xmin = x;
        double xmax = x + 1 * Math.pow(10, -summaryLevel);
        double ymin = y;
        double ymax = y + 1 * Math.pow(10, -summaryLevel);
        return xmin <= geometry.x && geometry.x <= xmax && ymin <= geometry.y && geometry.y <= ymax;
    }
}
