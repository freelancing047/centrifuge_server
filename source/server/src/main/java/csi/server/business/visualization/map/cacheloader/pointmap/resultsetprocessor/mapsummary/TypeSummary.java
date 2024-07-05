package csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary;

import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.common.model.map.PlaceidTypenameDuple;

public class TypeSummary extends AbstractMapSummary {
   private PlaceidTypenameDuple typename;

   public TypeSummary(PlaceidTypenameDuple typename) {
      this.typename = typename;
   }

   public void setMapNodeSizes() {
      setupGates();
      setMapNodeSizes(typename);
   }

   public void setMapNodeSizes(PlaceidTypenameDuple key) {
      for (AugmentedMapNode mapNode : getMapNodeList()) {
         mapNode.setSize(key, getSize(mapNode));
      }
   }
}
