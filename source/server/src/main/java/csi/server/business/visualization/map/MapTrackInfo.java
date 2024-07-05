package csi.server.business.visualization.map;

import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MapTrackInfo implements TrackTypeInfo, TrackidToTracknames {
    private ConcurrentHashMap<TrackidTracknameDuple, Set<AugmentedMapNode>> mapNodeByKey;
    private ConcurrentHashMap<Long, MapLink> mapLinkById;
    private ConcurrentHashMap<LinkGeometry, MapLink> mapLinkByGeometry;
    private ConcurrentHashMap<TrackidTracknameDuple, List<MapLink>> mapLinkByKey;
    private ConcurrentHashMap<TrackidTracknameDuple, Integer> trackkeyToId;
    private ConcurrentHashMap<Integer, TrackidTracknameDuple> trackidToKey;
    private ConcurrentHashMap<TrackidTracknameDuple, String> trackkeyToColor;
    private ConcurrentHashMap<TrackidTracknameDuple, String> trackkeyToShape;
    private ConcurrentHashMap<TrackidTracknameDuple, Integer> trackkeyToWidth;
    private ConcurrentHashMap<Integer, Set<String>> trackidToTracknames;

    public MapTrackInfo() {
        mapNodeByKey = new ConcurrentHashMap<>();
        mapLinkById = new ConcurrentHashMap<>();
        mapLinkByGeometry = new ConcurrentHashMap<>();
        mapLinkByKey = new ConcurrentHashMap<>();
        trackkeyToId = new ConcurrentHashMap<>();
        trackidToKey = new ConcurrentHashMap<>();
        trackkeyToColor = new ConcurrentHashMap<>();
        trackkeyToShape = new ConcurrentHashMap<>();
        trackkeyToWidth = new ConcurrentHashMap<>();
        trackidToTracknames = new ConcurrentHashMap<>();
    }

    public Map<TrackidTracknameDuple, Set<AugmentedMapNode>> getMapNodeByKey() {
        return mapNodeByKey;
    }

    public Map<Long, MapLink> getMapLinkById() {
        return mapLinkById;
    }

    public Map<LinkGeometry, MapLink> getMapLinkByGeometry() {
        return mapLinkByGeometry;
    }

    public Map<TrackidTracknameDuple, List<MapLink>> getMapLinkByKey() {
        return mapLinkByKey;
    }

    public Map<TrackidTracknameDuple, Integer> getTrackKeyToId() {
        return trackkeyToId;
    }

    public Map<Integer, TrackidTracknameDuple> getTrackIdToKey() {
        return trackidToKey;
    }

    public Map<TrackidTracknameDuple, String> getTrackkeyToColor() {
        return trackkeyToColor;
    }

    public Map<TrackidTracknameDuple, String> getTrackkeyToShape() {
        return trackkeyToShape;
    }

    public Map<TrackidTracknameDuple, Integer> getTrackkeyToWidth() {
        return trackkeyToWidth;
    }

    public Map<Integer, Set<String>> getTrackidToTracknames() {
        return trackidToTracknames;
    }

}
