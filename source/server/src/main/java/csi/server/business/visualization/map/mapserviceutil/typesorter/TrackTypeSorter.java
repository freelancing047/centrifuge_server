package csi.server.business.visualization.map.mapserviceutil.typesorter;

import com.google.common.collect.Sets;
import csi.server.business.visualization.map.*;
import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TrackTypeSorter {
    protected int id;
    private Set<TrackidTracknameDuple> dynamicTypeInfoTypeNames;
    private List<TrackidTracknameDuple> keys;
    private TrackDynamicTypeInfo dynamicTypeInfo;
    private MapTrackInfo mapTrackInfo;
    private TrackmapNodeInfo mapNodeInfo;
    private Set<TrackidTracknameDuple> trackInfoTypeNames;

    public TrackTypeSorter(String mapViewDefUuid, List<TrackidTracknameDuple> keys) {
        this.keys = keys;
        if (MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid)) {
            mapTrackInfo = MapCacheUtil.getMapTrackInfo(mapViewDefUuid);
        } else {
            mapNodeInfo = MapCacheUtil.getTrackmapNodeInfo(mapViewDefUuid);
        }
        dynamicTypeInfo = MapServiceUtil.getTrackDynamicTypeInfo(mapViewDefUuid);
    }

    public void sort() {
        init();
        applyToTypenameCache(keys.iterator());
        applyToTypenameCache(trackInfoTypeNames.iterator());
        applyToDynamicTypenameCache(dynamicTypeInfoTypeNames.iterator());
    }

    private void init() {
        initControlVariables();
        clearTypenameCache();
    }

    private void initControlVariables() {
        trackInfoTypeNames = Sets.newTreeSet();
        dynamicTypeInfoTypeNames = Sets.newTreeSet();
        for (TrackidTracknameDuple key : dynamicTypeInfo.getTrackKeyToId().keySet()) {
            if (key != null)
                dynamicTypeInfoTypeNames.add(key);
        }
        if (mapTrackInfo != null) {
            mapTrackInfo.getTrackkeyToColor().keySet().forEach(getTrackIdTrackNameDupleConsumer());
        } else {
            mapNodeInfo.getTrackkeyToColor().keySet().forEach(getTrackIdTrackNameDupleConsumer());
        }
        trackInfoTypeNames.removeAll(keys);
        id = 0;
    }

    private Consumer<TrackidTracknameDuple> getTrackIdTrackNameDupleConsumer() {
        return trackIdTrackNameDuple -> {
            if (trackIdTrackNameDuple != null) {
                trackInfoTypeNames.add(trackIdTrackNameDuple);
                dynamicTypeInfoTypeNames.remove(trackIdTrackNameDuple);
            }
        };
    }

    private void clearTypenameCache() {
        if (mapTrackInfo != null) {
            MapServiceUtil.clearTypeInfo(mapTrackInfo);
        } else {
            MapServiceUtil.clearTypeInfo(mapNodeInfo);
        }
        MapServiceUtil.clearTypeInfo(dynamicTypeInfo);
    }

    private void applyToTypenameCache(Iterator<TrackidTracknameDuple> keys) {
        while (keys.hasNext()) {
            TrackidTracknameDuple key = keys.next();
            if (mapTrackInfo != null) {
                MapServiceUtil.applyTypenameToTypeInfo(mapTrackInfo, key, id);
            } else {
                MapServiceUtil.applyTypenameToTypeInfo(mapNodeInfo, key, id);
            }
            MapServiceUtil.applyTypenameToTypeInfo(dynamicTypeInfo, key, id);
            id++;
        }
    }

    private void applyToDynamicTypenameCache(Iterator<TrackidTracknameDuple> keys) {
        while (keys.hasNext()) {
            TrackidTracknameDuple key = keys.next();
            MapServiceUtil.applyTypenameToTypeInfo(dynamicTypeInfo, key, id);
            id++;
        }
    }
}