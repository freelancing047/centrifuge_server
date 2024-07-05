package csi.server.business.visualization.map;

import java.util.Map;
import java.util.Set;

public interface TrackidToTracknames extends TrackTypeInfo {
    Map<Integer, Set<String>> getTrackidToTracknames();
}
