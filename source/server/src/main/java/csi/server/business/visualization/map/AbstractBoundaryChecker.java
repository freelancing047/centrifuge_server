package csi.server.business.visualization.map;

import csi.shared.core.visualization.map.MapSummaryExtent;

public abstract class AbstractBoundaryChecker {
   public static AbstractBoundaryChecker get(MapSummaryExtent mapSummaryExtent) {
      if (mapSummaryExtent == null) {
         return NoBoundaryChecker.getInstance();
      }
      if (mapSummaryExtent.getXMin() >= -180) {
         return new SingleBoundaryChecker(mapSummaryExtent);
      }
      MultipleBoundaryChecker multipleBoundaryChecker = new MultipleBoundaryChecker();
      MapSummaryExtent fromNeg180 = new MapSummaryExtent(-180.0, mapSummaryExtent.getYMin(), mapSummaryExtent.getXMax(),
                                                         mapSummaryExtent.getYMax());
      MapSummaryExtent toPos180 = new MapSummaryExtent(mapSummaryExtent.getXMin() + 360, mapSummaryExtent.getYMin(),
                                                       180.0, mapSummaryExtent.getYMax());
      multipleBoundaryChecker.addMapSummaryExtent(fromNeg180);
      multipleBoundaryChecker.addMapSummaryExtent(toPos180);
      return multipleBoundaryChecker;
   }

   public abstract boolean isPointInBoundary(double latitude, double longitude);
//	abstract boolean isPointInBoundary(MapNode mapNode);
}
