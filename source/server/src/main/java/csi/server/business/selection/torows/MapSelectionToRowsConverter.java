package csi.server.business.selection.torows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.server.business.service.FilterActionsService;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.BundleMapNode;
import csi.server.business.visualization.map.LinkGeometryPlus;
import csi.server.business.visualization.map.MapBundleQueryBuilder;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapNode;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.util.sql.SQLFactory;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MapSelectionToRowsConverter implements SelectionToRowsConverter {
    private final DataView dataView;
    private final MapViewDef visualizationDef;
    private final SQLFactory sqlFactory;
    private final FilterActionsService filterActionsService;

    MapSelectionToRowsConverter(DataView dataView, MapViewDef visualizationDef, SQLFactory sqlFactory,
                                FilterActionsService filterActionsService) {
        this.dataView = dataView;
        this.visualizationDef = visualizationDef;
        this.sqlFactory = sqlFactory;
        this.filterActionsService = filterActionsService;
    }

    @Override
    public Set<Integer> convertToRows(Selection selection, boolean excludeBroadcast) {
        Set<Integer> rowIds = new HashSet<Integer>();

        if (selection instanceof AbstractMapSelection) {
            AbstractMapSelection mapSelection = (AbstractMapSelection) selection;

            if (MapServiceUtil.isHandleBundle(dataView.getUuid(), visualizationDef.getUuid())) {
                MapBundleQueryBuilder mapQueryBuilder = new MapBundleQueryBuilder();
                mapQueryBuilder.setDataView(dataView);
                mapQueryBuilder.setViewDef(visualizationDef);
                mapQueryBuilder.setSqlFactory(sqlFactory);
                mapQueryBuilder.setFilterActionsService(filterActionsService);

                List<String> nodeCriteria = new ArrayList<String>();
                Map<Geometry, AugmentedMapNode> mapNodeByGeometryMap = MapCacheUtil.getMapNodeByGeometryMap(visualizationDef.getUuid());
                if (null != mapNodeByGeometryMap) {
                    for (Geometry nodeGeometry : mapSelection.getNodes()) {
                        MapNode mapNode = mapNodeByGeometryMap.get(nodeGeometry);
                        if (mapNode != null) {
                            BundleMapNode bundleMapNode = (BundleMapNode) mapNode;
                            nodeCriteria.add(bundleMapNode.getBundleValue());
                        }
                    }
                }
                if (!nodeCriteria.isEmpty()) {
                    rowIds = mapQueryBuilder.selectionValuesToRows(nodeCriteria, excludeBroadcast);
                }
            } else if (MapServiceUtil.isUseHeatmap(dataView.getUuid(), visualizationDef.getUuid())) {
                Map<Geometry, AugmentedMapNode> mapNodeByGeometryMap = MapCacheUtil.getMapNodeByGeometryMap(visualizationDef.getUuid());
                if (null != mapNodeByGeometryMap) {
                    for (Geometry nodeGeometry : mapSelection.getNodes()) {
                        MapNode mapNode = mapNodeByGeometryMap.get(nodeGeometry);
                        if (mapNode != null) {
                            rowIds.addAll(mapNode.getRowIds());
                        }
                    }
                }
            } else if (MapServiceUtil.isUseTrack(dataView.getUuid(), visualizationDef.getUuid())) {
                TrackMapSummaryGrid trackMapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid(visualizationDef.getUuid());
                if (trackMapSummaryGrid != null) {
                    for (LinkGeometry linkGeometry : mapSelection.getLinks()) {
                        rowIds.addAll(trackMapSummaryGrid.getRowIds(linkGeometry));
                    }
                } else {
                    for (LinkGeometry linkGeometry : mapSelection.getLinks()) {
                        Map<LinkGeometry, Set<Integer>> trackLinkGeometryToRowIds = MapCacheUtil.getTrackLinkGeometryToRowIds(visualizationDef.getUuid());
                        if (null != trackLinkGeometryToRowIds) {
                            Set<Integer> ids = trackLinkGeometryToRowIds.get(linkGeometry);
                            if (ids != null) {
                                rowIds.addAll(ids);
                            }
                        }
                    }
                }
            } else {
                MapSummaryGrid mapSummaryGrid = MapCacheUtil.getMapSummaryGrid(visualizationDef.getUuid());
                if (mapSummaryGrid != null) {
                    for (Geometry geometry : mapSelection.getNodes()) {
                        rowIds.addAll(mapSummaryGrid.getRowIds(geometry));
                    }
                    for (LinkGeometry linkGeometry : mapSelection.getLinks()) {
                        if (linkGeometry instanceof LinkGeometryPlus) {
                            LinkGeometryPlus linkGeometryPlus = (LinkGeometryPlus) linkGeometry;
                            rowIds.addAll(linkGeometryPlus.getRowIds());
                        }
                    }
                }
            }
        }

        return rowIds;
    }
}
