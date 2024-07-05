package csi.server.business.visualization.map;

import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.visualization.selection.AbstractMapSelection;

public class ConcurrentMapCacheInfo {
    private Integer mapCacheAt = null;
    private AbstractMapSelection mapSelection = null;
    private boolean multiTypeDecoratorShownStatus = false;
    private boolean linkupDecoratorShownStatus = false;
    private PlaceDynamicTypeInfo placeDynamicTypeInfo = null;
    private TrackDynamicTypeInfo trackDynamicTypeInfo = null;
    private Integer rangeStart = null;
    private Integer rangeEnd = null;
    private List<TrackMapSummaryGrid.SequenceSortValue> rangeSeriesValue = null;
    private Map<Geometry, Set<Integer>> geometryToRowIds = null;
    private Map<LinkGeometry, Set<Integer>> linkGeometryToRowIds = null;
    private Map<TrackidTracknameDuple, Set<Geometry>> trackTypeToGeometries = null;
    private Map<TrackidTracknameDuple, Set<LinkGeometry>> trackTypeToLinkGeometries = null;
    private Integer previousRangeEnd = null;
    private Integer previousRangeStart = null;
    private Integer previousRangeSize = null;

    ConcurrentMapCacheInfo() {
    }

    Integer getMapCacheAt() {
        return mapCacheAt;
    }

    void setMapCacheAt(Integer mapCacheAt) {
        this.mapCacheAt = mapCacheAt;
    }

    public AbstractMapSelection getMapSelection() {
        return mapSelection;
    }

    public void setMapSelection(AbstractMapSelection mapSelection) {
        this.mapSelection = mapSelection;
    }

    boolean getMultiTypeDecoratorShownStatus() {
        return multiTypeDecoratorShownStatus;
    }

    void setMultiTypeDecoratorShownStatus(boolean multiTypeDecoratorShownStatus) {
        this.multiTypeDecoratorShownStatus = multiTypeDecoratorShownStatus;
    }

    boolean getLinkupDecoratorShownStatus() {
        return linkupDecoratorShownStatus;
    }

    void setLinkupDecoratorShownStatus(boolean linkupDecoratorShownStatus) {
        this.linkupDecoratorShownStatus = linkupDecoratorShownStatus;
    }

    public PlaceDynamicTypeInfo getPlaceDynamicTypeInfo() {
        return placeDynamicTypeInfo;
    }

    void setPlaceDynamicTypeInfo(PlaceDynamicTypeInfo placeDynamicTypeInfo) {
        this.placeDynamicTypeInfo = placeDynamicTypeInfo;
    }

    public TrackDynamicTypeInfo getTrackDynamicTypeInfo() {
        return trackDynamicTypeInfo;
    }

    void setTrackDynamicTypeInfo(TrackDynamicTypeInfo trackDynamicTypeInfo) {
        this.trackDynamicTypeInfo = trackDynamicTypeInfo;
    }

    public Integer getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Integer rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Integer getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(Integer rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    List<TrackMapSummaryGrid.SequenceSortValue> getRangeSeriesValue() {
        return rangeSeriesValue;
    }

    void setRangeSeriesValue(List<TrackMapSummaryGrid.SequenceSortValue> rangeSeriesValue) {
        this.rangeSeriesValue = rangeSeriesValue;
    }

    Map<Geometry, Set<Integer>> getTrackGeometryToRowIds() {
        return geometryToRowIds;
    }

    void setTrackGeometryToRowIds(Map<Geometry, Set<Integer>> geometryToRowIds) {
        this.geometryToRowIds = geometryToRowIds;
    }

    Map<LinkGeometry, Set<Integer>> getLinkGeometryToRowIds() {
        return linkGeometryToRowIds;
    }

    void setTrackLinkGeometryToRowIds(Map<LinkGeometry, Set<Integer>> linkGeometryToRowIds) {
        this.linkGeometryToRowIds = linkGeometryToRowIds;
    }

    Map<TrackidTracknameDuple, Set<Geometry>> getTrackTypeToGeometries() {
        return trackTypeToGeometries;
    }

    void setTrackTypeToGeometries(Map<TrackidTracknameDuple, Set<Geometry>> trackTypeToGeometries) {
        this.trackTypeToGeometries = trackTypeToGeometries;
    }

    Map<TrackidTracknameDuple, Set<LinkGeometry>> getTrackTypeToLinkGeometries() {
        return trackTypeToLinkGeometries;
    }

    void setTrackTypeToLinkGeometries(Map<TrackidTracknameDuple, Set<LinkGeometry>> trackTypeToLinkGeometries) {
        this.trackTypeToLinkGeometries = trackTypeToLinkGeometries;
    }

    Integer getPreviousRangeEnd() {
        return previousRangeEnd;
    }

    void setPreviousRangeEnd(Integer previousRangeEnd) {
        this.previousRangeEnd = previousRangeEnd;
    }

    Integer getPreviousRangeStart() {
        return previousRangeStart;
    }

    void setPreviousRangeStart(Integer previousRangeStart) {
        this.previousRangeStart = previousRangeStart;
    }

    Integer getPreviousRangeSize() {
        return previousRangeSize;
    }

    void setPreviousRangeSize(Integer previousRangeSize) {
        this.previousRangeSize = previousRangeSize;
    }
}
