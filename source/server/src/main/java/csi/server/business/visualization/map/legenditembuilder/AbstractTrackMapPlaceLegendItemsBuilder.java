package csi.server.business.visualization.map.legenditembuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.server.business.visualization.legend.PlaceLegendItem;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.shared.core.visualization.map.MapSettingsDTO;

public abstract class AbstractTrackMapPlaceLegendItemsBuilder {
   protected MapSettingsDTO mapSettings;
   protected List<PlaceLegendItem> legendItems;
   protected Map<TrackidTracknameDuple,String> typenameToShape;
   protected Map<TrackidTracknameDuple,String> typenameToColor;
   MapCacheHandler mapCacheHandler;
   Set<Integer> typeIds;
   Map<Integer,TrackidTracknameDuple> typeIdToName;

   public AbstractTrackMapPlaceLegendItemsBuilder(MapCacheHandler mapCacheHandler) {
      this.mapCacheHandler = mapCacheHandler;
      legendItems = new ArrayList<PlaceLegendItem>();
      mapSettings = mapCacheHandler.getMapSettings();
   }

   public abstract void build();

   public List<PlaceLegendItem> getLegendItems() {
      return legendItems;
   }

   void buildLegendItems() {
      for (Integer typeId : typeIds) {
         addToLegendItems(typeId);
      }
   }

   private void addToLegendItems(Integer typeId) {
      TrackidTracknameDuple key = typeIdToName.get(typeId);
      if (typenameToShape.containsKey(key)) {
         PlaceLegendItem legendItem = getLegendItem(key);
         legendItems.add(legendItem);
      }
   }

   private PlaceLegendItem getLegendItem(TrackidTracknameDuple key) {
      return createAndInitializeLegendItem(key);
   }

   private PlaceLegendItem createAndInitializeLegendItem(TrackidTracknameDuple key) {
      PlaceLegendItem legendItem = new PlaceLegendItem();
      legendItem.key = key.getTrackname() + "::" + key.getTrackid();
      legendItem.placeId = key.getTrackid();
      legendItem.placeName = mapSettings.getTrackSettings().get(key.getTrackid()).getIdentityName();
      legendItem.typeName = key.getTrackname();
      legendItem.shape = typenameToShape.get(key);
      String color = typenameToColor.get(key);
      if (color.startsWith("#")) {
         color = color.substring(1);
      }
      legendItem.color = Integer.decode("0x" + color);
      return legendItem;
   }
}
