package csi.server.business.visualization.map.legenditembuilder;

import java.util.Set;

import csi.server.business.visualization.legend.CombinedPlaceLegendItem;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapSummaryGrid;

public class CombinedMapNodeLegendItemBuilder {
   private MapCacheHandler mapCacheHandler;
   private Set<AugmentedMapNode> combinedMapNodes;
   private CombinedPlaceLegendItem legendItem;

   public CombinedMapNodeLegendItemBuilder(MapCacheHandler mapCacheHandler) {
      this.mapCacheHandler = mapCacheHandler;
      legendItem = null;
   }

   public void build() {
      init();

      if (canCreateLegendItem()) {
         createLegendItem();
      }
   }

   private void init() {
      combinedMapNodes = mapCacheHandler.getCombinedMapNodesSet();
   }

   private boolean canCreateLegendItem() {
      boolean combinedMapNodesAvailable = (combinedMapNodes != null) && !combinedMapNodes.isEmpty();

      if (!combinedMapNodesAvailable && mapCacheHandler.isCurrentlyAtDetailLevel()) {
         MapSummaryGrid mapSummaryGrid = mapCacheHandler.getMapSummaryGrid();

         if (mapSummaryGrid != null) {
            combinedMapNodesAvailable = mapSummaryGrid.hasCombinedType();
         }
      }
      return combinedMapNodesAvailable;
   }

   private void createLegendItem() {
      legendItem = new CombinedPlaceLegendItem();

      if (combinedMapNodes == null) {
         legendItem.count = 1;
      } else {
         legendItem.count = combinedMapNodes.size();
      }
      legendItem.visible = mapCacheHandler.isMultiTypeDecoratorShown();
      legendItem.clickable = mapCacheHandler.isCurrentlyAtDetailLevel();
   }

   public CombinedPlaceLegendItem getLegendItem() {
      return legendItem;
   }
}
