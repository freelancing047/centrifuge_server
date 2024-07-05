package csi.server.business.visualization.map.storage;

import csi.server.business.visualization.map.LinkGeometryPlus;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.business.visualization.map.NodeSizeCalculator;

import java.util.Map;
import java.util.Set;

public interface OutOfBandResources {
    Map<Integer, Map<String, NodeSizeCalculator>> getRegistry();

    MapSummaryGrid getMapSummaryGrid();

    Set<LinkGeometryPlus> getLinks();
}
