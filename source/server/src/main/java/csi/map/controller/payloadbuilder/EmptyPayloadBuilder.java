package csi.map.controller.payloadbuilder;

import csi.config.Configuration;
import csi.map.controller.model.Payload;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.map.Extent;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class EmptyPayloadBuilder extends AbstractPayloadBuilder {
   public EmptyPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent) {
      super(mapCacheHandler, extent);
   }

   public EmptyPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent, MapSummaryExtent mapSummaryExtent,
                              Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
      super(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
   }

   @Override
   protected void decorateWithMapCache() {
      payload.setItemsInViz(0);
      payload.setUseSummary();
      payload.setSummaryLevel(Configuration.getInstance().getMapConfig().getDetailLevel());
      if (mapCacheHandler.isPlaceLimitOrTrackTypeLimitReached()) {
         if (mapCacheHandler.isPlaceTypeLimitReached()) {
            payload.setPlaceTypeLimitReached();
         } else {
            payload.setTrackTypeLimitReached();
         }
      } else {
         payload.setCacheNotAvailable();
      }
   }

   @Override
   protected void decorateWithOthers() {
   }

   @Override
   protected void decoratePayloadWithPlaceInfo(Payload payload) {
   }

   @Override
   protected void decoratePayloadWithAssociationInfo(Payload payload) {
   }

   @Override
   protected void decorateWithExtentInfo() {
   }

   @Override
   protected void decorateWithMapSettings() {
   }
}
