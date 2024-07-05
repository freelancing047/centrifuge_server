package csi.map.controller.payloadbuilder;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.map.Extent;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class PointPayloadBuilder extends AbstractPayloadBuilder {
   public PointPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent, MapSummaryExtent mapSummaryExtent,
                              Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
      super(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
   }

   @Override
   protected void decorateWithMapCache() {
      DetailPointBuilder pointBuilder = new DetailPointBuilder(payload, mapCacheHandler, mapSummaryExtent, rangeStart,
            rangeEnd, rangeSize);
      pointBuilder.build();
   }

   @Override
   protected void decorateWithOthers() {
      decoratePayloadWithPlaceInfo(payload);
      decoratePayloadWithAssociationInfo(payload);
      payload.setUseMultitypeDecorator(mapCacheHandler.isMultiTypeDecoratorShown());
      payload.setUseLinkupDecorator(mapCacheHandler.isLinkupDecoratorShown());
   }

   @Override
   protected void decorateWithMapSettings() {
   }
}
