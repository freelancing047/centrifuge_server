package csi.server.business.visualization.map.mapcacheutil;

import csi.server.business.visualization.map.MapLinkInfo;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.MapTrackInfo;
import csi.server.business.visualization.map.TrackmapNodeInfo;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class CurrentInfo {
    private Integer mapSummaryPrecision;
    private MapNodeInfo mapNodeInfo;
    private TrackmapNodeInfo trackmapNodeInfo;
    private Integer itemsInViz;
    private MapLinkInfo mapLinkInfo;
    private MapTrackInfo mapTrackInfo;
    private MapSummaryExtent mapSummaryExtent;

    public Integer getMapSummaryPrecision() {
        return mapSummaryPrecision;
    }

    public void setMapSummaryPrecision(Integer mapSummaryPrecision) {
        this.mapSummaryPrecision = mapSummaryPrecision;
    }

    public MapNodeInfo getMapNodeInfo() {
        return mapNodeInfo;
    }

    public void setMapNodeInfo(MapNodeInfo mapNodeInfo) {
        this.mapNodeInfo = mapNodeInfo;
    }

    public void invalidateMapNodeInfo() {
        this.mapNodeInfo = null;
    }

    public TrackmapNodeInfo getTrackmapNodeInfo() {
        return trackmapNodeInfo;
    }

    public void setTrackmapNodeInfo(TrackmapNodeInfo trackmapNodeInfo) {
        this.trackmapNodeInfo = trackmapNodeInfo;
    }

    public Integer getItemsInViz() {
        return itemsInViz;
    }

    public void setItemsInViz(Integer itemsInViz) {
        this.itemsInViz = itemsInViz;
    }

    public void invalidateItemsInViz() {
        this.itemsInViz = null;
    }

    public MapLinkInfo getMapLinkInfo() {
        return mapLinkInfo;
    }

    public void setMapLinkInfo(MapLinkInfo mapLinkInfo) {
        this.mapLinkInfo = mapLinkInfo;
    }

    public MapTrackInfo getMapTrackInfo() {
        return mapTrackInfo;
    }

    public void setMapTrackInfo(MapTrackInfo mapTrackInfo) {
        this.mapTrackInfo = mapTrackInfo;
    }

    public void invalidateMapTrackInfo() {
        this.mapTrackInfo = null;
    }

    public MapSummaryExtent getMapSummaryExtent() {
        return mapSummaryExtent;
    }

    public void setMapSummaryExtent(MapSummaryExtent mapSummaryExtent) {
        this.mapSummaryExtent = mapSummaryExtent;
    }

    public void invalidateMapSummaryExtent() {
        this.mapSummaryExtent = null;
    }
}
