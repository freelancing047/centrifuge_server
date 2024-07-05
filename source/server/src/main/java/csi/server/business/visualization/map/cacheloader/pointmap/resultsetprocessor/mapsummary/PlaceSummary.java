package csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary;

import csi.server.common.model.map.PlaceidTypenameDuple;

public class PlaceSummary extends AbstractMapSummary {
   public PlaceSummary() {
   }

   public void setMapNodeSizes() {
      setupGates();
      setMapNodeSizes(new PlaceidTypenameDuple());
   }
}
