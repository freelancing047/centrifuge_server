package csi.server.business.visualization.map;

import com.google.common.collect.Sets;
import csi.server.common.model.map.Geometry;

import java.util.Set;

public class MapNode {
    protected String vizUuid;
    final private Long nodeId;
    private Geometry geometry;
    private Set<Integer> rowIds = Sets.newTreeSet();
    private double hits = 0;

    public MapNode(String vizUuid, Long nodeId) {
        super();
        this.vizUuid = vizUuid;
        this.nodeId = nodeId;
    }

    public long getNodeId() {
        return nodeId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Set<Integer> getRowIds() {
        return rowIds;
    }

    public void incrementHits() {
        incrementHits(1);
    }

    public void incrementHits(double increment) {
        hits += increment;
    }

    public double getHits() {
        return hits;
    }

    public void setHits(Double hits) {
        this.hits = hits;
    }

    public void incorporate(MapNode other) {
        incorporateHits(other);
        incorporateRowIds(other);
    }

    private void incorporateHits(MapNode other) {
        hits += other.hits;
    }

    private void incorporateRowIds(MapNode other) {
        rowIds.addAll(other.rowIds);
    }

}
