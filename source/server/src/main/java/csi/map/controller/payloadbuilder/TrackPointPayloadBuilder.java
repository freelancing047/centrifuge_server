package csi.map.controller.payloadbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import csi.map.controller.model.AssociationSymbol;
import csi.map.controller.model.Payload;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapTrackInfo;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class TrackPointPayloadBuilder extends AbstractPayloadBuilder {
    public TrackPointPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent) {
        super(mapCacheHandler, extent);
    }

    public TrackPointPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
        super(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
    }

    @Override
    protected void decorateWithMapCache() {
        TrackDetailPointBuilder pointBuilder = new TrackDetailPointBuilder(payload, mapCacheHandler, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
        pointBuilder.build();
    }

    @Override
    protected void decorateWithOthers() {
        decoratePayloadWithPlaceInfo(payload);
        decoratePayloadWithTrackInfo(payload);
        payload.setUseMultitypeDecorator(mapCacheHandler.isMultiTypeDecoratorShown());
        payload.setUseLinkupDecorator(mapCacheHandler.isLinkupDecoratorShown());
    }

    private void decoratePayloadWithTrackInfo(Payload payload) {
        List<AssociationSymbol> associationSymbols = new ArrayList<AssociationSymbol>();
        MapTrackInfo mapTrackInfo = mapCacheHandler.getMapTrackInfo();
        Map<TrackidTracknameDuple, Integer> typenameToId = mapCacheHandler.getTrackTypenameToId();
        if ((mapTrackInfo != null) && (mapTrackInfo.getTrackIdToKey() != null) && (typenameToId != null)) {
            for (TrackidTracknameDuple typename : mapTrackInfo.getTrackkeyToColor().keySet()) {
                if (typenameToId.containsKey(typename)) {
                    AssociationSymbol associationSymbol = new AssociationSymbol();
                    associationSymbol.setId(typenameToId.get(typename));
                    associationSymbol.setLineStyle(mapTrackInfo.getTrackkeyToShape().get(typename));
                    associationSymbol.setWidth(mapTrackInfo.getTrackkeyToWidth().get(typename));
                    associationSymbol.setColor(mapTrackInfo.getTrackkeyToColor().get(typename));
                    associationSymbol.setShowDirection(true);
                    associationSymbols.add(associationSymbol);
                }
            }
        }
        payload.setAssociationSymbols(associationSymbols);
    }

    @Override
    protected void decorateWithMapSettings() {
        payload.setNodeTransparency(mapCacheHandler.getMapSettings().getNodeTransparency() / 100f);
    }
}
