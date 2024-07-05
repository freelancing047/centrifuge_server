package csi.map.controller.payloadbuilder;

import java.util.ArrayList;
import java.util.List;

import csi.map.controller.model.TypeSymbol;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;
import csi.server.business.visualization.map.TrackmapNodeInfo;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class TrackSummaryPointPayloadBuilder extends AbstractPayloadBuilder {
    public TrackSummaryPointPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent) {
        super(mapCacheHandler, extent);
    }

    public TrackSummaryPointPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent,
                                           MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd,
                                           Integer rangeSize) {
       super(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
    }

    @Override
    protected void decorateWithMapCache() {
        TrackSummaryPointBuilder pointBuilder = new TrackSummaryPointBuilder(payload, mapCacheHandler);
        pointBuilder.build();
    }

    @Override
    protected void decorateWithOthers() {
        List<TypeSymbol> typeSymbols = new ArrayList<TypeSymbol>();
        TrackmapNodeInfo mapNodeInfo = mapCacheHandler.getTrackmapNodeInfo();
        TrackDynamicTypeInfo dynamicTypeInfo = mapCacheHandler.getTrackDynamicTypeInfo();
        for (TrackidTracknameDuple key : mapNodeInfo.getTrackkeyToColor().keySet()) {
            if (dynamicTypeInfo.getTrackKeyToId().containsKey(key)) {
                Integer typeId = dynamicTypeInfo.getTrackKeyToId().get(key);
                TypeSymbol typeSymbol = new TypeSymbol();
                typeSymbol.setId(typeId);
                typeSymbol.setShape(mapNodeInfo.getTrackkeyToShape().get(key));
                typeSymbol.setColor(mapNodeInfo.getTrackkeyToColor().get(key));
                typeSymbols.add(typeSymbol);
            }
        }
        payload.setDefaultShapeString(mapCacheHandler.getMapSettings().getDefaultShapeString());
        payload.setTypeSymbols(typeSymbols);
        payload.setUseMultitypeDecorator(mapCacheHandler.isMultiTypeDecoratorShown());
        payload.setUseLinkupDecorator(mapCacheHandler.isLinkupDecoratorShown());
    }

    @Override
    protected void decorateWithMapSettings() {
    }
}
