package csi.server.business.visualization.map;

import com.google.common.collect.Maps;
import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrackDynamicTypeInfo implements TrackTypeInfo {
    private ConcurrentHashMap<TrackidTracknameDuple, Integer> trackKeyToId = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, TrackidTracknameDuple> trackIdToKey = new ConcurrentHashMap<>();
    private ConcurrentHashMap<TrackidTracknameDuple, String> trackKeyToSummaryShape = new ConcurrentHashMap<>();
    private ConcurrentHashMap<TrackidTracknameDuple, String> trackKeyToShape = new ConcurrentHashMap<>();
    private ConcurrentHashMap<TrackidTracknameDuple, String> trackKeyToColor = new ConcurrentHashMap<>();
    private ConcurrentHashMap<TrackidTracknameDuple, Integer> trackKeyToWidth = new ConcurrentHashMap<>();
    private boolean invalidated = false;

    public TrackDynamicTypeInfo() {
    }

    public Map<TrackidTracknameDuple, Integer> getTrackKeyToId() {
        return trackKeyToId;
    }

    public Map<Integer, TrackidTracknameDuple> getTrackIdToKey() {
        return trackIdToKey;
    }

    public Map<TrackidTracknameDuple, String> getTrackKeyToSummaryShape() {
        return trackKeyToSummaryShape;
    }

    public Map<TrackidTracknameDuple, String> getTrackKeyToShape() {
        return trackKeyToShape;
    }

    public Map<TrackidTracknameDuple, String> getTrackKeyToColor() {
        return trackKeyToColor;
    }

    public Map<TrackidTracknameDuple, Integer> getTrackKeyToWidth() {
        return trackKeyToWidth;
    }

    public void invalidate() {
        invalidated = true;
    }

    public boolean isInvalidated() {
        return invalidated;
    }
}
