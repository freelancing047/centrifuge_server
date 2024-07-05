package csi.server.business.visualization.map;

import csi.server.common.model.map.Geometry;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class RangeChecker {
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;

    public RangeChecker(Geometry geometry) {
        int summaryLevel = geometry.getSummaryLevel();
        xMin = geometry.getX();
        xMax = xMin + 1 * Math.pow(10, -summaryLevel);
        yMin = geometry.getY();
        yMax = yMin + 1 * Math.pow(10, -summaryLevel);
    }

    RangeChecker(MapSummaryExtent mapSummaryExtent) {
        xMin = mapSummaryExtent.getXMin();
        yMin = mapSummaryExtent.getYMin();
        xMax = mapSummaryExtent.getXMax();
        yMax = mapSummaryExtent.getYMax();
    }

    public boolean isInRange(Geometry geometry) {
        double x = geometry.getX();
        double y = geometry.getY();
        return xMin <= x && x <= xMax && yMin <= y && y <= yMax;
    }
}
