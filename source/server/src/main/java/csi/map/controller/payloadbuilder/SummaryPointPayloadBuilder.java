package csi.map.controller.payloadbuilder;

import java.util.ArrayList;
import java.util.List;

import csi.map.controller.model.TypeSymbol;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.common.model.map.Extent;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class SummaryPointPayloadBuilder extends AbstractPayloadBuilder {
   public SummaryPointPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent) {
      super(mapCacheHandler, extent);
   }

   public SummaryPointPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent, MapSummaryExtent mapSummaryExtent,
                                     Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
      super(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
   }

   @Override
   protected void decorateWithMapCache() {
      SummaryPointBuilder pointBuilder = new SummaryPointBuilder(payload, mapCacheHandler, mapSummaryExtent, rangeStart,
            rangeEnd, rangeSize);
      pointBuilder.build();
   }

   @Override
   protected void decorateWithOthers() {
      if (mapCacheHandler.usingLatestMapCache()) {
         List<TypeSymbol> typeSymbols = new ArrayList<TypeSymbol>();
         MapNodeInfo mapNodeInfo = mapCacheHandler.getMapNodeInfo();
         AbstractPayloadBuilder.addTypeSymbol(typeSymbols, mapNodeInfo, mapCacheHandler);
         payload.setDefaultShapeString(mapCacheHandler.getMapSettings().getDefaultShapeString());
         payload.setTypeSymbols(typeSymbols);
         payload.setUseMultitypeDecorator(mapCacheHandler.isMultiTypeDecoratorShown());
         payload.setUseLinkupDecorator(mapCacheHandler.isLinkupDecoratorShown());
      }
   }

   @Override
   protected void decorateWithMapSettings() {
   }
}
