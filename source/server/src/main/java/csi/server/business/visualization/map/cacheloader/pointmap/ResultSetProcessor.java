package csi.server.business.visualization.map.cacheloader.pointmap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import csi.config.Configuration;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapLink;
import csi.server.business.visualization.map.MapLinkInfo;
import csi.server.business.visualization.map.MapNode;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.UUIDUtil;
import csi.server.business.visualization.map.cacheloader.MapNodeUtil;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.MapNodeStatisticsCalculator;
import csi.server.business.visualization.map.mapserviceutil.typesorter.MapLinkSorter;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.AssociationSettingsDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;

class ResultSetProcessor {
    private MapCacheHandler mapCacheHandler;
    private DataView dataView;
    private MapSettingsDTO mapSettings;
    private MapTheme mapTheme;
    private ResultSet rs;
    private MapNodeInfo mapNodeInfo;
    private MapLinkInfo mapLinkInfo;
    private Map<String, Integer> mapPlaceNameNodeId;
    private boolean linkLimitReached;
    private boolean cancelled = false;
    private int linkCount = 0;
    private MapNodeUtil mapNodeUtil;
    private boolean waitForRegistry;

    ResultSetProcessor(MapCacheHandler mapCacheHandler, DataView dataView, MapSettingsDTO mapSettings, MapTheme mapTheme, ResultSet rs, boolean waitForRegistry) {
        this.mapCacheHandler = mapCacheHandler;
        this.dataView = dataView;
        this.mapSettings = mapSettings;
        this.mapTheme = mapTheme;
        this.rs = rs;
        this.waitForRegistry = waitForRegistry;
    }

    public void process() throws SQLException {
        if (canProcess()) {
            init();
            processResultSet();
            if (mapCacheHandler.getItemsInViz() == 0)
                clear();
            else {
                calculateMapNodeStatistics(mapNodeInfo.getMapByType(), mapNodeInfo.getPlaceIdToTypeNames());
                mapCacheHandler.sortMapNodeType();
                if (linkLimitReached) {
                    clearLinks();
                } else {
                    MapLinkSorter sorter = new MapLinkSorter(mapCacheHandler);
                    sorter.sort();
                }
            }
            mapCacheHandler.setLinkLimitReached(linkLimitReached);
        }
    }

    private boolean canProcess() {
        return rs != null;
    }

    private void init() {
        mapNodeInfo = mapCacheHandler.getMapNodeInfo();
        mapLinkInfo = mapCacheHandler.getMapLinkInfo();
        Map<String, PlaceStyle> typeNameToPlaceStyle = Maps.newHashMap();
        mapPlaceNameNodeId = Maps.newHashMap();
        for (int placeId = 0; placeId < mapSettings.getPlaceSettings().size(); placeId++)
            mapPlaceNameNodeId.put(mapSettings.getPlaceSettings().get(placeId).getName(), placeId);
        mapNodeUtil = new MapNodeUtil(dataView, mapSettings, mapNodeInfo, mapTheme, typeNameToPlaceStyle);
    }

    private void processResultSet() throws SQLException {
        while (rs.next()) {
            if (cancelled)
                return;
            processRow();
        }
    }

    private void processRow() throws SQLException {
        Map<Integer, MapNode> mapNodes = Maps.newHashMap();
        Integer id = getId();
        Integer internalStateId = getInternalStateId();
        populateMapNodes(mapNodes, id);
        populateMapLinks(mapNodes, id, internalStateId);
    }

    private Integer getId() throws SQLException {
        String strId = rs.getString(CacheUtil.INTERNAL_ID_NAME);
        return Integer.parseInt(strId);
    }

    private Integer getInternalStateId() throws SQLException {
        String strId = rs.getString(CacheUtil.INTERNAL_STATEID);
        return Integer.parseInt(strId);
    }

    private void populateMapNodes(Map<Integer, MapNode> mapNodes, Integer id)
            throws SQLException {
        for (int placeId = 0; placeId < mapSettings.getPlaceSettings().size(); placeId++)
            populateMapNodesByPlace(mapNodes, id, placeId);
    }

    private void populateMapNodesByPlace(Map<Integer, MapNode> mapNodes, Integer id, int placeId) throws SQLException {
        double latitude = rs.getDouble(mapSettings.getPlaceSettings().get(placeId).getLatColumn());
        if (rs.wasNull() || latitude < -90 || latitude > 90)
            return;
        double longitude = rs.getDouble(mapSettings.getPlaceSettings().get(placeId).getLongColumn());
        if (rs.wasNull() || longitude < -180 || longitude > 180)
            return;

        if (cancelled)
            return;

        AugmentedMapNode mapNode = mapNodeUtil.processMapNode(rs, id, placeId, latitude, longitude);
        if (mapNode != null)
            mapNodes.put(placeId, mapNode);
        // }
    }

    private void populateMapLinks(Map<Integer, MapNode> mapNodes, Integer id, Integer internalStateId) {
        for (int associationId = 0; associationId < mapSettings.getAssociationSettings().size(); associationId++) {
            AssociationSettingsDTO associationSettings = mapSettings.getAssociationSettings().get(associationId);
            populateMapLinksByAssociation(mapNodes, id, internalStateId, associationId, associationSettings);
            if (linkLimitReached)
                break;
        }
    }

    private void populateMapLinksByAssociation(Map<Integer, MapNode> mapNodes, Integer id, Integer internalStateId, int associationId, AssociationSettingsDTO associationSettings) {
        MapNode sourceMapNode = getNodeForLink(mapNodes, associationSettings.getSource());
        if (sourceMapNode == null)
            return;
        MapNode destinationMapNode = getNodeForLink(mapNodes, associationSettings.getDestination());
        if (destinationMapNode == null)
            return;

        String typename = processAssociationTypename(associationSettings, mapLinkInfo);
        MapLink mapLink = getMapLink(associationId, sourceMapNode, destinationMapNode);
        if (linkLimitReached)
            return;
        updateMapLink(id, internalStateId, associationId, typename, mapLink);
    }

    private MapNode getNodeForLink(Map<Integer, MapNode> mapNodes, String name) {
        int index = mapPlaceNameNodeId.get(name);
        return mapNodes.get(index);
    }

    private String processAssociationTypename(AssociationSettingsDTO associationSettings, MapLinkInfo mapLinkInfo) {
        String typename = associationSettings.getName();
        if (!mapLinkInfo.getTypenameToColor().containsKey(typename)) {
            mapLinkInfo.getTypenameToColor().put(typename, associationSettings.getColorString());
            mapLinkInfo.getTypenameToShape().put(typename, associationSettings.getLineStyle());
            mapLinkInfo.getTypenameToWidth().put(typename, associationSettings.getWidth());
            mapLinkInfo.getTypenameToShowDirection().put(typename, associationSettings.isShowDirection());
        }
        return typename;
    }

    private MapLink getMapLink(int associationId, MapNode sourceMapNode, MapNode destinationMapNode) {
        LinkGeometry linkGeometry = new LinkGeometry(associationId, sourceMapNode.getGeometry(),
                destinationMapNode.getGeometry());
        MapLink mapLink;
        if (mapLinkInfo.getMapByGeometry().containsKey(linkGeometry))
            mapLink = retrieveMapLink(mapLinkInfo.getMapByGeometry(), linkGeometry);
        else {
            linkCount++;
            if (linkCount > Configuration.getInstance().getMapConfig().getLinkLimit()) {
                mapCacheHandler.setPointLimitReached(true);
                linkLimitReached = true;
            }
            mapLink = createMapLink(mapLinkInfo.getMapById(), mapLinkInfo.getMapByGeometry(), UUIDUtil.getUUIDLong(), sourceMapNode, destinationMapNode, linkGeometry);
        }
        return mapLink;
    }

    private MapLink retrieveMapLink(Map<LinkGeometry, MapLink> mapLinkMapByGeom, LinkGeometry linkGeometry) {
        MapLink mapLink = mapLinkMapByGeom.get(linkGeometry);
        mapLink.incrementHits();
        return mapLink;
    }

    private MapLink createMapLink(Map<Long, MapLink> mapLinkMapById, Map<LinkGeometry, MapLink> mapLinkMapByGeom, Long linkId, MapNode sourceMapNode, MapNode destinationMapNode, LinkGeometry linkGeometry) {
        MapLink mapLink = new MapLink(linkId);
        mapLink.setSourceNode(sourceMapNode);
        mapLink.setDestinationNode(destinationMapNode);
        mapLinkMapByGeom.put(linkGeometry, mapLink);
        mapLinkMapById.put(mapLink.getLinkId(), mapLink);
        return mapLink;
    }

    private void updateMapLink(Integer id, Integer internalStateId, int associationId, String typename, MapLink mapLink) {
        if (!mapLink.getRowIds().contains(id))
            updateMapLink(id, internalStateId, mapLink, associationId, typename, mapLinkInfo.getMapByType());
    }

    private void updateMapLink(Integer id, Integer internalStateId, MapLink mapLink, int typeId, String typeName, Map<String, List<MapLink>> mapLinkByType) {
        if (!mapLinkByType.keySet().contains(typeName))
            mapLinkByType.put(typeName, Lists.newArrayList());
        mapLinkByType.get(typeName).add(mapLink);
        mapLink.addInternalstateid(internalStateId);
        mapLink.setTypeId(typeId);
        mapLink.getRowIds().add(id);
        mapLink.incrementHits();
    }

    private void clear() {
        clearNodes();
        clearLinks();
    }

    private void clearNodes() {
        mapNodeInfo.getMapById().clear();
        mapNodeInfo.getMapByType().clear();
        mapNodeInfo.getCombinedMapNodes().clear();
    }

    private void clearLinks() {
        mapLinkInfo.getMapById().clear();
        mapLinkInfo.getMapByType().clear();
    }

    private void calculateMapNodeStatistics(Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> placeIdToTypeNames) {
        if (!mapNodeUtil.isMaxPlaceSizeEqualsMinPlaceSize()) {
            MapNodeStatisticsCalculator calculator = new MapNodeStatisticsCalculator(mapCacheHandler, mapNodeByType, placeIdToTypeNames, waitForRegistry);
            calculator.calculate();
        }
    }

    public void cancel() {
        cancelled = true;
    }
}