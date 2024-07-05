package csi.server.business.visualization.map.mapserviceutil.typesorter;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;
import csi.server.business.visualization.map.TrackmapNodeInfo;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.shared.core.visualization.map.MapSettingsDTO;

import java.util.Set;

public class TrackSummaryNodeTypeSorter {
    private MapCacheHandler mapCacheHandler;
    private TrackmapNodeInfo mapNodeInfo;
    private TrackDynamicTypeInfo dynamicTypeInfo;
    private MapSettingsDTO mapSettings;

    public TrackSummaryNodeTypeSorter(MapCacheHandler mapCacheHandler, TrackmapNodeInfo mapNodeInfo) {
        this.mapCacheHandler = mapCacheHandler;
        dynamicTypeInfo = mapCacheHandler.getTrackDynamicTypeInfo();
        mapSettings = mapCacheHandler.getMapSettings();
        this.mapNodeInfo = mapNodeInfo;
    }

    public void sort() {
        if (mapSettings.isPlaceTypeFixed())
            populateTypenameCacheWithPlacenames();
        else
            populateTypenameCacheWithDynamicTypes();
        reconcileDynamicTypeInfo();
    }

    private void populateTypenameCacheWithPlacenames() {
        int id = 0;
        for (int trackid = mapSettings.getTrackSettings().size() - 1; trackid >= 0; trackid--) {
            Set<String> typeNames = mapNodeInfo.getTrackidToTracknames().get(trackid);
            if (typeNames != null)
                for (String typename : typeNames) {
                    TrackidTracknameDuple key = new TrackidTracknameDuple(trackid, typename);
                    mapCacheHandler.applyTypenameToTypeInfo(mapNodeInfo, key, id);
                    id++;
                }
        }
    }

    private void populateTypenameCacheWithDynamicTypes() {
        TrackTypenameCacheAggregator a = new TrackTypenameCacheAggregator(mapCacheHandler, mapNodeInfo, dynamicTypeInfo.getTrackKeyToId());
        a.aggregate();
    }

    private void reconcileDynamicTypeInfo() {
        TrackDynamicTypeInfoReconciler r = new TrackDynamicTypeInfoReconciler(mapCacheHandler, mapNodeInfo, dynamicTypeInfo);
        r.reconcile();
    }
}