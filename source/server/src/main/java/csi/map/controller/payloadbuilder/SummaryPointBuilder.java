package csi.map.controller.payloadbuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.map.controller.model.Feature;
import csi.map.controller.model.Layer;
import csi.map.controller.model.Payload;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class SummaryPointBuilder extends PointBuilder {
   private List<Feature> features;
   private Map<Integer,List<Feature>> selectedUncombined = new HashMap<Integer,List<Feature>>();

   public SummaryPointBuilder(Payload payload, MapCacheHandler mapCacheHandler, MapSummaryExtent mapSummaryExtent,
                              Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
      super(mapCacheHandler, mapSummaryExtent, rangeStart, rangeEnd, rangeSize, payload);
      init();
   }

   private void init() {
      mapNodeMapById = mapCacheHandler.getMapNodeByIdMap();
      Layer layer = payload.getLayer();
      features = layer.getFeatures();
      placeTypenameToId = mapCacheHandler.getPlaceDynamicTypeInfo().getTypenameToId();
   }

   public void build() {
      processNodesAndLinks();
      payload.setItemsInViz(mapCacheHandler.getItemsInViz());
      AbstractPayloadBuilder.incorporateInto(features, selectedUncombined, unselectedUncombined, mapCacheHandler);
      payload.setUseSummary();
      payload.setSummaryLevel(mapCacheHandler.getCurrentMapSummaryPrecision());
   }
}
