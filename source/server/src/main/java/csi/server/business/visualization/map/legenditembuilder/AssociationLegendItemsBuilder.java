package csi.server.business.visualization.map.legenditembuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import csi.server.business.visualization.legend.AssociationLegendItem;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapLinkInfo;

public class AssociationLegendItemsBuilder {
   private MapCacheHandler mapCacheHandler;
   private List<AssociationLegendItem> legendItems;
   private Map<Integer,String> typeIdToName;
   private Map<String,String> typenameToColor;
   private Map<String,String> typenameToShape;
   private Set<Integer> typeIds;

   public AssociationLegendItemsBuilder(MapCacheHandler mapCacheHandler) {
      this.mapCacheHandler = mapCacheHandler;
      legendItems = new ArrayList<AssociationLegendItem>();
   }

   public void build() {
      initControlVariables();
      buildLegendItems();
   }

   private void initControlVariables() {
      typeIds = new TreeSet<Integer>();
      MapLinkInfo mapLinkInfo = mapCacheHandler.getMapLinkInfo();

      if (mapLinkInfo != null) {
         typeIdToName = mapCacheHandler.getMapLinkTypeIdToName(mapLinkInfo);

         if (typeIdToName != null) {
            typeIds.addAll(typeIdToName.keySet());
            typenameToColor = mapLinkInfo.getTypenameToColor();
            typenameToShape = mapLinkInfo.getTypenameToShape();
         }
      }
   }

   private void buildLegendItems() {
      for (Integer typeId : typeIds) {
         addToLegendItems(typeId);
      }
   }

   private void addToLegendItems(Integer typeId) {
      String typename = typeIdToName.get(typeId);
      AssociationLegendItem legendItem = getLegendItem(typename);

      if (legendItem != null) {
         legendItems.add(legendItem);
      }
   }

   private AssociationLegendItem getLegendItem(String typename) {
      return canCreateLegendItem(typename) ? createAndInitializeLegendItem(typename) : null;
   }

   private boolean canCreateLegendItem(String typename) {
      return typenameToColor.get(typename) != null;
   }

   private AssociationLegendItem createAndInitializeLegendItem(String typename) {
      AssociationLegendItem legendItem = new AssociationLegendItem();

      legendItem.key = typename;
      legendItem.typeName = typename;
      legendItem.shape = typenameToShape.get(typename);
      legendItem.color = Integer.decode("0x" + typenameToColor.get(typename).replace("#", ""));
      return legendItem;
   }

   public List<AssociationLegendItem> getLegendItems() {
      return legendItems;
   }
}
