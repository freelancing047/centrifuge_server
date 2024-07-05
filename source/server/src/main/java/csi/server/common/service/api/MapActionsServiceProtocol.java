/**
 * Copyright (c) 2008-2013 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.server.common.service.api;

import csi.server.business.visualization.legend.MapLegendInfo;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.map.*;
import csi.server.common.model.map.map.MapLayerDTO;
import csi.server.common.model.visualization.selection.Selection;
import csi.shared.core.visualization.map.*;
import csi.shared.gwt.vortex.VortexService;

import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface MapActionsServiceProtocol extends VortexService {
    void doDeselectAll(String dataViewUuid, String mapViewDefUuid);

    void doSelectAll(String dataViewUuid, String mapViewDefUuid);

    void forceLoad(String dataViewUuid, String mapViewDefUuid);

    MapConfigDTO getMapConfig();

    Selection getSelection(String dataViewUuid, String mapViewDefUuid);

    void togglePlaceSelectionByType(String dataViewUuid, String mapViewDefUuid, Integer placeId, String typename, String selectionOperation);

    void toggleTrackSelectionByType(String dataViewUuid, String mapViewDefUuid, Integer trackId, String typename, String selectionOperation);

    Boolean toggleCombinedPlaceSelection(String dataViewUuid, String mapViewDefUuid, String selectionOperation);

    void toggleAssociationSelectionByType(String dataViewUuid, String mapViewDefUuid, String associationKey, String selectionOperation);

    Boolean toggleNewPlaceSelection(String dataViewUuid, String mapViewDefUuid, String selectionOperation);

    Boolean toggleUpdatedPlaceSelection(String dataViewUuid, String mapViewDefUuid, String selectionOperation);

    void deleteBasemap(String uuid);

    void saveBasemap(Basemap basemap);

    List<ResourceBasics> listBasemapResources();

    List<MapLayerDTO> listBasemaps();

    Basemap findBasemap(String uuid);

    void deleteBasemaps(List<String> myItemList) throws CentrifugeException;

    void updateLegend(String dataViewUuid, String mapViewDefUuid, List<PlaceidTypenameDuple> placeIdTypenameDuple, List<TrackidTracknameDuple> trackIdTracknameDuple, List<String> associationNames);

    boolean isLegendEnabled(String mapViewDefUuid);

    void setLegendShown(String dataViewUuid, String mapViewDefUuid, boolean value);

    boolean isLegendShown(String dataViewUuid, String mapViewDefUuid);

    boolean isMultitypeDecoratorEnabled(String mapViewDefUuid);

    void setMultitypeDecoratorShown(String dataViewUuid, String mapViewDefUuid, boolean value);

    boolean isMultitypeDecoratorShown(String dataViewUuid, String mapViewDefUuid);

    MapToolsInfo getMapToolsInfo(String dataViewUuid, String mapViewDefUuid);

    MetricsDTO getViewMetrics(String mapViewDefUuid, String dvUuid);

    // does this need dv?
    MetricsDTO getMapTotalMetrics(String mapViewDefUuid, String dvUuid);

    String getNodeAsImageNew(String iconId, boolean isMap, ShapeType shape, int color, int size, double iconScale, int strokeSize, boolean useSummary, String mapViewDefUuid) throws CentrifugeException;

    void fillCacheWithMapContext(String dataViewUuid, String mapViewDefUuid);

    String loadMapCache(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber);

    String buildSummaryCache(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, UBox uBox);

    void selectNodes(String dataViewUuid, String mapViewDefUuid, String[] ids);

    void selectLinks(String dataViewUuid, String mapViewDefUuid, String[] ids);

    Boolean selectTrackLinks(String dataViewUuid, String mapViewDefUuid, String[] nodes, String[] links);

    void deselectNodes(String dataViewUuid, String mapViewDefUuid, String[] ids);

    String toggleSelectedNodes(String dataViewUuid, String mapViewDefUuid, String[] ids);

    Boolean toggleTrackNodes(String dataViewUuid, String mapViewDefUuid, String[] ids);

    Boolean toggleSelectedLinks(String dataViewUuid, String mapViewDefUuid, String[] ids);

    Boolean selectLinks2(String dataViewUuid, String mapViewDefUuid, String[] ids);

    Boolean deselectLinks2(String dataViewUuid, String mapViewDefUuid, String[] ids);

    String getTooltip(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, Long id);

    void selectAll(String dataViewUuid, String mapViewDefUuid);

    void deselectAll(String dataViewUuid, String mapViewDefUuid);

    String[] getSelectedNodes(String dataViewUuid, String mapViewDefUuid);

    void drillOnBundle(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, String requestString);

    void trimBreadcrumb(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, String drillFieldName);

    void showLeavesOnBundle(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, String requestString);

    void dontShowLeavesOnBundle(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber);

    Integer getNumPoints(String dataViewUuid, String mapViewDefUuid, Long id);

    MapLegendInfo legendData(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber) throws CentrifugeException;

    Extent getExtent(String dataViewUuid, String mapViewDefUuid);

    Extent getCurrentExtent(String dataViewUuid, String mapViewDefUuid);

    void setExtent(String dataViewUuid, String mapViewDefUuid, Extent extent);

    ExtentInfo getExtentInfo(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber);

    void setHeatmapBlurValue(String dataViewUuid, String mapViewDefUuid, Double blurValue);

    void setHeatmapMaxValue(String dataViewUuid, String mapViewDefUuid, Double maxValue);

    void setHeatmapMinValue(String dataViewUuid, String mapViewDefUuid, Double minValue);

    void setHeatmapValues(String dataViewUuid, String mapViewDefUuid, Double blurValue, Double maxValue, Double minValue);

    List<Crumb> getBreadcrumb(String dataViewUuid, String mapViewDefUuid);

    HeatMapInfo getHeatMapInfo(String dataViewUuid, String mapViewDefUuid);

    boolean isShowLeaves(String dataViewUuid, String mapViewDefUuid);

    void setLinkupDecoratorShown(String dataViewUuid, String mapViewDefUuid, boolean value);

    void handleLinkup(String dataViewUuid, String mapViewDefUuid);

    List<MapLayerInfo> getMapLayerInfos(String mapViewDefUuid);

    PlaceSizeInfo getPlaceSizeInfo(String mapViewDefUuid);

    OverviewResponse getOverview(OverviewRequest overviewRequest);

    boolean updateRange(String dataViewUuid, String mapViewDefUuid, int start, int end);

    void setRangeHome(String dataViewUuid, String mapViewDefUuid);

    void selectFirstTrack(boolean deselect, String mapViewDefUuid, String dataViewUuid);

    void selectLastTrack(boolean deselect, String mapViewDefUuid, String dataViewUuid);

    String getStartAndEnd(String uuid, String dataViewUuid);

    boolean isMetricsReady(String dataViewUuid, String mapViewDefUuid);

    void setMapPinned(String dataViewUuid, String mapViewDefUuid, boolean value);

    boolean isMapPinned(String dataViewUuid, String mapViewDefUuid);

    void setSelectionMode(String dataViewUuid, String mapViewDefUuid, int value);

    Integer getSelectionMode(String dataViewUuid, String mapViewDefUuid);
}