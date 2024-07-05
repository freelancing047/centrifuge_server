package csi.server.business.visualization.map.mapserviceutil.typesorter;

import com.google.common.collect.Sets;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;
import csi.server.business.visualization.map.TrackTypeInfo;
import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.Map;
import java.util.Set;

class TrackDynamicTypeInfoReconciler {
    private MapCacheHandler mapCacheHandler;
    private TrackTypeInfo mapTrackInfo;
    private TrackDynamicTypeInfo dynamicTypeInfo;
    private int maxId;
    private Set<TrackidTracknameDuple> typeNamesNotInMapNodeInfo;

    TrackDynamicTypeInfoReconciler(MapCacheHandler mapCacheHandler, TrackTypeInfo mapTrackInfo, TrackDynamicTypeInfo dynamicTypeInfo) {
        this.mapCacheHandler = mapCacheHandler;
        this.mapTrackInfo = mapTrackInfo;
        this.dynamicTypeInfo = dynamicTypeInfo;
    }

    void reconcile() {
        init();
        populateDynamicTypeInfoWithMapNodeInfo();
        populateDynamicTypeInfoWithTypeNamesNotInMapNodeInfo();
    }

    private void init() {
        maxId = 0;
        typeNamesNotInMapNodeInfo = createTypeNamesNotInMapTrackInfo();
        mapCacheHandler.clearTypeInfo(dynamicTypeInfo);
    }

    private Set<TrackidTracknameDuple> createTypeNamesNotInMapTrackInfo() {
        Set<TrackidTracknameDuple> dynamicTypeInfoTypeNames = Sets.newTreeSet();
        dynamicTypeInfoTypeNames.addAll(dynamicTypeInfo.getTrackKeyToId().keySet());
        dynamicTypeInfoTypeNames.removeAll(mapTrackInfo.getTrackKeyToId().keySet());
        return dynamicTypeInfoTypeNames;
    }

    private void populateDynamicTypeInfoWithMapNodeInfo() {
        for (Map.Entry<Integer, TrackidTracknameDuple> integerStringEntry : mapTrackInfo.getTrackIdToKey().entrySet()) {
            TrackidTracknameDuple key = integerStringEntry.getValue();
            int typeID = integerStringEntry.getKey();
            mapCacheHandler.applyTypenameToTypeInfo(dynamicTypeInfo, key, typeID);
            if (maxId < typeID)
                maxId = typeID;
        }
    }

    private void populateDynamicTypeInfoWithTypeNamesNotInMapNodeInfo() {
        int typeId = maxId + 1;
        for (TrackidTracknameDuple key : getSortedTypeNames(typeNamesNotInMapNodeInfo)) {
            mapCacheHandler.applyTypenameToTypeInfo(dynamicTypeInfo, key, typeId);
            typeId++;
        }
    }

    private Set<TrackidTracknameDuple> getSortedTypeNames(Set<TrackidTracknameDuple> keys) {
        return Sets.newTreeSet(keys);
    }
}