package csi.server.business.visualization.map.mapserviceutil.typesorter;


import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.shared.core.visualization.map.MapSettingsDTO;

import java.util.Set;

public class MapNodeTypeSorter {
    private MapNodeInfo mapNodeInfo;
    private PlaceDynamicTypeInfo dynamicTypeInfo;
    private MapSettingsDTO mapSettings;
    private MapCacheHandler mapCacheHandler;

    public MapNodeTypeSorter(MapCacheHandler mapCacheHandler, MapNodeInfo mapNodeInfo) {
        this.mapCacheHandler = mapCacheHandler;
        dynamicTypeInfo = mapCacheHandler.getPlaceDynamicTypeInfo();
        mapSettings = mapCacheHandler.getMapSettings();
        this.mapNodeInfo = mapNodeInfo;
    }

    public void sort() {
        if (mapSettings.isPlaceTypeFixed())
            populateTypenameCacheWithPlaceNames();
        else
            populateTypenameCacheWithDynamicTypes();
        reconcileDynamicTypeInfo();
    }

    private void populateTypenameCacheWithPlaceNames() {
        int id = 0;
        for (int placeId = mapSettings.getPlaceSettings().size() - 1; placeId >= 0; placeId--) {
            Set<String> typeNames = mapNodeInfo.getPlaceIdToTypeNames().get(placeId);
            if (typeNames != null)
                for (String typename : typeNames) {
                    PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
                    mapCacheHandler.applyTypenameToTypeInfo(mapNodeInfo, key, id);
                    id++;
                }
        }
    }

    private void populateTypenameCacheWithDynamicTypes() {
        PlaceTypenameCacheAggregator a = new PlaceTypenameCacheAggregator(mapCacheHandler, mapNodeInfo, dynamicTypeInfo);
        a.aggregate();
    }

    private void reconcileDynamicTypeInfo() {
        PlaceDynamicTypeInfoReconciler r = new PlaceDynamicTypeInfoReconciler(mapCacheHandler, mapNodeInfo, dynamicTypeInfo);
        r.reconcile();
    }
}