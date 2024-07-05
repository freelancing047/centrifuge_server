package csi.server.business.visualization.map.storage;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import csi.server.business.visualization.map.LinkGeometryPlus;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.business.visualization.map.NodeSizeCalculator;
import csi.server.business.visualization.map.PointMapSummaryGrid;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class PointmapOutOfBandResources implements Serializable, OutOfBandResources {
    private Map<Integer, Map<String, NodeSizeCalculator>> registry;
    private PointMapSummaryGrid mapSummaryGrid;
    private Set<LinkGeometryPlus> links;

    public PointmapOutOfBandResources() {
        registry = Maps.newHashMap();
        mapSummaryGrid = new PointMapSummaryGrid();
        links = Sets.newHashSet();
    }

    public Map<Integer, Map<String, NodeSizeCalculator>> getRegistry() {
        return registry;
    }

    public MapSummaryGrid getMapSummaryGrid() {
        return mapSummaryGrid;
    }

    public Set<LinkGeometryPlus> getLinks() {
        return links;
    }
}
