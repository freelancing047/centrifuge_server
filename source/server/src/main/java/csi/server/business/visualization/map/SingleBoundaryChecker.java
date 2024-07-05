package csi.server.business.visualization.map;

import csi.shared.core.visualization.map.MapSummaryExtent;

public class SingleBoundaryChecker extends AbstractBoundaryChecker {
   private MapSummaryExtent mapSummaryExtent;

   SingleBoundaryChecker(MapSummaryExtent mapSummaryExtent) {
      this.mapSummaryExtent = mapSummaryExtent;
   }

   @Override
   public boolean isPointInBoundary(double latitude, double longitude) {
      return mapSummaryExtent.isPointInExtent(longitude, latitude);
   }

//	public boolean isPointInBoundary(MapNode mapNode) {
//		double x = mapNode.getGeometry().getX();
//		double y = mapNode.getGeometry().getY();
//		return isPointInBoundary(y, x);
//	}
}
