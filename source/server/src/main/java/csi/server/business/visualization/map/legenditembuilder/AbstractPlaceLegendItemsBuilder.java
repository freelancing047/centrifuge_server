package csi.server.business.visualization.map.legenditembuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.server.business.visualization.legend.PlaceLegendItem;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.shared.core.visualization.map.MapSettingsDTO;

public abstract class AbstractPlaceLegendItemsBuilder {
   protected MapSettingsDTO mapSettings;
   protected List<PlaceLegendItem> legendItems;
   protected Map<PlaceidTypenameDuple,String> typenameToShape;
   protected Map<PlaceidTypenameDuple,String> typenameToColor;
   MapCacheHandler mapCacheHandler;
   Set<Integer> typeIds;
   Map<Integer,PlaceidTypenameDuple> typeIdToName;
   Map<PlaceidTypenameDuple,String> typenameToIconUrl;

   public AbstractPlaceLegendItemsBuilder(MapCacheHandler mapCacheHandler) {
      this.mapCacheHandler = mapCacheHandler;
      legendItems = new ArrayList<PlaceLegendItem>();
      mapSettings = mapCacheHandler.getMapSettings();
   }

   public abstract void build();

   public List<PlaceLegendItem> getLegendItems() {
      return legendItems;
   }

   void buildLegendItems() {
      typeIds.forEach(this::addToLegendItems);
   }

   private void addToLegendItems(Integer typeId) {
      PlaceidTypenameDuple key = typeIdToName.get(typeId);
      if (typenameToColor.containsKey(key)) {
         PlaceLegendItem legendItem = getLegendItem(key);

         legendItems.add(legendItem);
      }
   }

   private PlaceLegendItem getLegendItem(PlaceidTypenameDuple key) {
      return createAndInitializeLegendItem(key);
   }

   private PlaceLegendItem createAndInitializeLegendItem(PlaceidTypenameDuple key) {
      PlaceLegendItem legendItem = new PlaceLegendItem();
      legendItem.key = key.getTypename() + "::" + key.getPlaceid();
      legendItem.placeId = key.getPlaceid();
      legendItem.placeName = mapSettings.getPlaceSettings().get(key.getPlaceid()).getName();
      legendItem.typeName = key.getTypename();
      legendItem.shape = typenameToShape.get(key);
      String color = typenameToColor.get(key);

      if (color.startsWith("#")) {
         color = color.substring(1);
      }
      legendItem.color = Integer.decode("0x" + color);

      if (typenameToIconUrl.get(key) != null) {
         legendItem.iconURI = typenameToIconUrl.get(key);
      }
      return legendItem;
   }
}
