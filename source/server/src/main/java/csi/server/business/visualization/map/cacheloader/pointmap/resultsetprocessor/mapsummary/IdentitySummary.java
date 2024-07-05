package csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary;

import csi.server.common.model.map.TrackidTracknameDuple;

public class IdentitySummary extends AbstractMapSummary {
   private TrackidTracknameDuple identityname;

   public IdentitySummary(TrackidTracknameDuple identityname) {
      this.identityname = identityname;
   }

   public void setMapNodeSizes() {
      setupGates();
      setMapNodeSizes(identityname);
   }
}
