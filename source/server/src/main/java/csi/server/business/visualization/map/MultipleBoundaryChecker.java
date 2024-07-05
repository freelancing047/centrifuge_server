package csi.server.business.visualization.map;

import java.util.ArrayList;
import java.util.List;

import csi.shared.core.visualization.map.MapSummaryExtent;

public class MultipleBoundaryChecker extends AbstractBoundaryChecker {
   private List<MapSummaryExtent> mapSummaryExtents;

   MultipleBoundaryChecker() {
      mapSummaryExtents = new ArrayList<MapSummaryExtent>();
   }

   void addMapSummaryExtent(MapSummaryExtent mapSummaryExtent) {
      mapSummaryExtents.add(mapSummaryExtent);
   }

   @Override
   public boolean isPointInBoundary(double latitude, double longitude) {
      boolean result = false;

      for (MapSummaryExtent mapSummaryExtent : mapSummaryExtents) {
         if (mapSummaryExtent.isPointInExtent(longitude, latitude)) {
            result = true;
            break;
         }
      }
      return result;
   }

//	@Override
//	public boolean isPointInBoundary(MapNode mapNode) {
//		double x = mapNode.getGeometry().getX();
//		double y = mapNode.getGeometry().getY();
//		return isPointInBoundary(y, x);
//	}
}
