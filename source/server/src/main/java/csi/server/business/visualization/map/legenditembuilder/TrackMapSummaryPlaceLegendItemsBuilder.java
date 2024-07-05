package csi.server.business.visualization.map.legenditembuilder;

import java.util.TreeSet;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;

public class TrackMapSummaryPlaceLegendItemsBuilder extends AbstractTrackMapPlaceLegendItemsBuilder {
    public TrackMapSummaryPlaceLegendItemsBuilder(MapCacheHandler mapCacheHandler) {
        super(mapCacheHandler);
    }

    public void build() {
        initControlVariables();
        buildLegendItems();
    }

    private void initControlVariables() {
        typeIds = new TreeSet<Integer>();

        TrackDynamicTypeInfo dynamicTypeInfo = mapCacheHandler.getTrackDynamicTypeInfo();
        typeIdToName = dynamicTypeInfo.getTrackIdToKey();
//        removeUnusedTypes();

        if (typeIdToName != null) {
            typeIds.addAll(typeIdToName.keySet());
            typenameToShape = dynamicTypeInfo.getTrackKeyToSummaryShape();
            typenameToColor = dynamicTypeInfo.getTrackKeyToColor();
        }
    }

//    private void removeUnusedTypes() {
//        MapSummaryGrid grid = mapCacheHandler.getMapSummaryGrid();
//        if (grid != null) {
//			Set<TrackidTracknameDuple> types = grid.getTypes();
//			HashMap<Integer, TrackidTracknameDuple> isMap = Maps.newHashMap();
//
//			isMap.putAll(typeIdToName);
//			for (Map.Entry<Integer, TrackidTracknameDuple> integerStringEntry : isMap.entrySet()) {
//				if (!types.contains(integerStringEntry.getValue())) {
//					typeIdToName.remove(integerStringEntry.getKey());
//				}
//			}
//        }
//    }
}
