package csi.server.business.visualization.map;

import com.google.common.collect.Sets;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TrackmapNodeInfo implements TrackTypeInfo, TrackidToTracknames {
    private ConcurrentHashMap<Long, AugmentedMapNode> mapNodeById;
    private ConcurrentHashMap<Geometry, AugmentedMapNode> mapNodeByGeometry;
    private ConcurrentHashMap<TrackidTracknameDuple, Set<AugmentedMapNode>> mapNodeByKey;
    private ConcurrentHashMap<TrackidTracknameDuple, Integer> trackkeyToId;
    private ConcurrentHashMap<Integer, TrackidTracknameDuple> trackidToKey;
    private ConcurrentHashMap<TrackidTracknameDuple, String> trackkeyToColor;
    private ConcurrentHashMap<TrackidTracknameDuple, String> trackkeyToShape;
    private ConcurrentHashMap<Integer, Set<String>> trackidToTracknames;
    private Set<AugmentedMapNode> combinedMapNodes;
    private Set<AugmentedMapNode> newMapNodes;
    private Set<AugmentedMapNode> updatedMapNodes;

    public TrackmapNodeInfo() {
        mapNodeById = new ConcurrentHashMap<>();
        mapNodeByGeometry = new ConcurrentHashMap<>();
        mapNodeByKey = new ConcurrentHashMap<>();
        trackkeyToId = new ConcurrentHashMap<>();
        trackidToKey = new ConcurrentHashMap<>();
        trackkeyToColor = new ConcurrentHashMap<>();
        trackkeyToShape = new ConcurrentHashMap<>();
        trackidToTracknames = new ConcurrentHashMap<>();
        combinedMapNodes = Sets.newHashSet();
        newMapNodes = Sets.newHashSet();
        updatedMapNodes = Sets.newHashSet();
    }

    public Map<Long, AugmentedMapNode> getMapNodeById() {
        return mapNodeById;
    }

    public Map<Geometry, AugmentedMapNode> getMapNodeByGeometry() {
        return mapNodeByGeometry;
    }

    public Map<TrackidTracknameDuple, Set<AugmentedMapNode>> getMapNodeByKey() {
        return mapNodeByKey;
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

    public Map<Integer, Set<String>> getTrackidToTracknames() {
        return trackidToTracknames;
    }

    public Set<AugmentedMapNode> getCombinedMapNodes() {
        return combinedMapNodes;
    }

    public Set<AugmentedMapNode> getNewMapNodes() {
        return newMapNodes;
    }

    public Set<AugmentedMapNode> getUpdatedMapNodes() {
        return updatedMapNodes;
    }
}
