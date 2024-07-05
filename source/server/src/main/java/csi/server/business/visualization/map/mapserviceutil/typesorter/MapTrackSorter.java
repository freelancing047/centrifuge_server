package csi.server.business.visualization.map.mapserviceutil.typesorter;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapTrackInfo;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.shared.core.visualization.map.MapSettingsDTO;

import java.util.Set;

public class MapTrackSorter {
    private MapCacheHandler mapCacheHandler;
    private MapTrackInfo mapTrackInfo;
    private TrackDynamicTypeInfo dynamicTypeInfo;
    private MapSettingsDTO mapSettings;

    public MapTrackSorter(MapCacheHandler mapCacheHandler) {
        this.mapCacheHandler = mapCacheHandler;
        dynamicTypeInfo = mapCacheHandler.getTrackDynamicTypeInfo();
        mapSettings = mapCacheHandler.getMapSettings();
        mapTrackInfo = mapCacheHandler.getMapTrackInfo();
    }

    public void sort() {
        if (mapSettings.isTrackTypeFixed())
            populateTypenameCacheWithTrackNames();
        else
            populateTypenameCacheWithDynamicTypes();
        reconcileDynamicTypeInfo();
    }

    private void populateTypenameCacheWithTrackNames() {
        int id = 0;
        for (int trackId = mapSettings.getTrackSettings().size() - 1; trackId >= 0; trackId--) {
            Set<String> typeNames = mapTrackInfo.getTrackidToTracknames().get(trackId);
            if (typeNames != null)
                for (String typename : typeNames) {
                    TrackidTracknameDuple key = new TrackidTracknameDuple(trackId, typename);
                    mapCacheHandler.applyTypenameToTypeInfo(mapTrackInfo, key, id);
                    id++;
                }
        }
    }

    private void populateTypenameCacheWithDynamicTypes() {
        TrackTypenameCacheAggregator a = new TrackTypenameCacheAggregator(mapCacheHandler, mapTrackInfo, dynamicTypeInfo.getTrackKeyToId());
        a.aggregate();
    }

    private void reconcileDynamicTypeInfo() {
        TrackDynamicTypeInfoReconciler r = new TrackDynamicTypeInfoReconciler(mapCacheHandler, mapTrackInfo, dynamicTypeInfo);
        r.reconcile();
    }
}