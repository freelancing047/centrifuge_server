package csi.server.business.visualization.map;

import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.Map;

public interface TrackTypeInfo {
    Map<TrackidTracknameDuple, Integer> getTrackKeyToId();

    Map<Integer, TrackidTracknameDuple> getTrackIdToKey();
}
