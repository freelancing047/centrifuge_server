package csi.server.business.visualization.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import csi.config.Configuration;
import csi.server.business.visualization.map.cacheloader.MapNodeUtil;
import csi.server.business.visualization.map.mapcacheutil.CurrentInfo;
import csi.server.business.visualization.map.mapcacheutil.MapVizInfo;
import csi.server.business.visualization.map.mapserviceutil.typesorter.MapNodeTypeSorter;
import csi.server.business.visualization.map.mapserviceutil.typesorter.TrackSummaryNodeTypeSorter;
import csi.server.business.visualization.map.storage.OutOfBandResources;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.SpatialReference;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.TrackmapSelection;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.UBox;

public class MapCacheHandler {
    private static final int mapConfigDetailLevel;

    static {
        mapConfigDetailLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
    }

    //    private static final int MAX_ITERATION = 10;
    private final String dvUuid;
    private final String vizUuid;
    private final Integer sequenceNumber;
    private Integer cacheLocation;
    //    private int counter = 0;
    private MapVizInfo mapVizInfo = null;
    private MapSettingsDTO mapSettingsDTO = null;
    private CurrentInfo currentInfo = null;
    private boolean doIterate;

    public MapCacheHandler(String dvUuid, String vizUuid, Integer sequenceNumber) {
        this(dvUuid, vizUuid, sequenceNumber, true);
    }

    public MapCacheHandler(String dvUuid, String vizUuid, Integer sequenceNumber, boolean doIterate) {
        this.dvUuid = dvUuid;
        this.vizUuid = vizUuid;
        this.sequenceNumber = sequenceNumber;
        this.doIterate = doIterate;
        findCacheLocation(vizUuid);
        iterate();
    }

    /**** others ****/

    public static Set<Integer> getSortedTrackTypeIds(Map<Integer, TrackidTracknameDuple> typeIdToName) {
        return new TreeSet<Integer>(typeIdToName.keySet());
    }

    public static Set<Integer> getSortedPlaceTypeIds(Map<Integer, PlaceidTypenameDuple> typeIdToName) {
        return new TreeSet<Integer>(typeIdToName.keySet());
    }

    private void findCacheLocation(String vizUuid) {
        cacheLocation = MapCacheUtil.getCacheAt(vizUuid);
        if (cacheLocation == null) {
            cacheLocation = sequenceNumber;
        }
    }

    public String getDvUuid() {
        return dvUuid;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void iterate() {
        if (doIterate) {
//        counter++;
//        if (counter == MAX_ITERATION) {
//        if (usingLatestMapCache()) {
//                counter = 0;
//        } else {
//            throw new MapCacheStaleException();
//        }
//        }
            if (!usingLatestMapCache()) {
                throw new MapCacheStaleException();
            }
        }
    }

    /**** MapCacheUtil ****/

    public boolean usingLatestMapCache() {
        return MapCacheUtil.usingLatestMapCache(vizUuid, cacheLocation);
    }

    public AbstractMapSelection getMapSelection() {
        iterate();
        return MapCacheUtil.getMapSelection(vizUuid);
    }

    public Map<PlaceidTypenameDuple, Integer> getPlaceTypenameToId() {
        PlaceDynamicTypeInfo dynamicTypeInfo = getPlaceDynamicTypeInfo();
        if (dynamicTypeInfo == null) {
            return null;
        } else {
            return dynamicTypeInfo.getTypenameToId();
        }
    }

    public TrackDynamicTypeInfo getTrackDynamicTypeInfo() {
        iterate();
        return MapCacheUtil.getTrackDynamicTypeInfo(vizUuid);
    }

    public Integer getRangeStart() {
        iterate();
        return MapCacheUtil.getRangeStart(vizUuid);
    }

    public Integer getRangeEnd() {
        iterate();
        return MapCacheUtil.getRangeEnd(vizUuid);
    }

    public List<TrackMapSummaryGrid.SequenceSortValue> getSeriesValues() {
        iterate();
        return MapCacheUtil.getSeriesValues(vizUuid);
    }

    public boolean isMultiTypeDecoratorShown() {
        iterate();
        return MapCacheUtil.isMultiTypeDecoratorShown(vizUuid);
    }

    public boolean isLinkupDecoratorShown() {
        iterate();
        return MapCacheUtil.isLinkupDecoratorShown(vizUuid);
    }

    public void setTrackGeometryToRowIds(Map<Geometry, Set<Integer>> trackGeometryToRowIds) {
        iterate();
        MapCacheUtil.setTrackGeometryToRowIds(vizUuid, trackGeometryToRowIds);
    }

    public void setTrackLinkGeometryToRowIds(Map<LinkGeometry, Set<Integer>> trackLinkGeometryToRowIds) {
        iterate();
        MapCacheUtil.setTrackLinkGeometryToRowIds(vizUuid, trackLinkGeometryToRowIds);
    }

    public void setTrackTypeToGeometries(Map<TrackidTracknameDuple, Set<Geometry>> trackTypeToGeometries) {
        iterate();
        MapCacheUtil.setTrackTypeToGeometries(vizUuid, trackTypeToGeometries);
    }

    public void setTrackTypeToLinkGeometries(Map<TrackidTracknameDuple, Set<LinkGeometry>> trackTypeToLinkGeometries) {
        iterate();
        MapCacheUtil.setTrackTypeToLinkGeometries(vizUuid, trackTypeToLinkGeometries);
    }

    /**** OutOfBandResources ****/

    public void addOutOfBandResources(OutOfBandResources outOfBandResources) {
        iterate();
        MapCacheUtil.addOutOfBandResources(vizUuid, outOfBandResources);
    }

    public boolean outOfBandResourceBuilding() {
        iterate();
        return MapCacheUtil.outOfBandResourceBuilding(vizUuid);
    }

    public Map<Integer, Map<String, NodeSizeCalculator>> getNodeSizeCalculatorRegistry() {
        iterate();
        return MapCacheUtil.getNodeSizeCalculatorRegistry(vizUuid);
    }

    public MapSummaryGrid getMapSummaryGrid() {
        iterate();
        return MapCacheUtil.getMapSummaryGrid(vizUuid);
    }

    public TrackMapSummaryGrid getTrackMapSummaryGrid() {
        iterate();
        return MapCacheUtil.getTrackMapSummaryGrid(vizUuid);
    }

    public void invalidateOutOfBandResources() {
        iterate();
        MapCacheUtil.invalidateOutOfBandResources(vizUuid);
    }

    /**** MapServiceUtil ****/

    void registerMapContextIfNotSet() {
        iterate();
        MapContext mapContext = MapServiceUtil.getMapContext(dvUuid, vizUuid);
        if (mapContext == null) {
            mapContext = new MapContext(dvUuid, vizUuid);
            MapServiceUtil.setMapContext(mapContext);
        }
    }

    public TrackmapSelection getTrackMapSelection() {
        iterate();
        return MapServiceUtil.getTrackMapSelection(dvUuid, vizUuid);
    }

    public void applyTypenameToTypeInfo(MapTrackInfo mapTrackInfo, TrackidTracknameDuple key, int id) {
        iterate();
        MapServiceUtil.applyTypenameToTypeInfo(mapTrackInfo, key, id);
    }

    public void applyTypenameToTypeInfo(TrackTypeInfo typeInfo, TrackidTracknameDuple key, int id) {
        iterate();
        MapServiceUtil.applyTypenameToTypeInfo(typeInfo, key, id);
    }

    public Map<Integer, String> getMapLinkTypeIdToName(MapLinkInfo mapLinkInfo) {
        iterate();
        if (MapServiceUtil.isHandleBundle(dvUuid, vizUuid)) {
            return null;
        } else {
            return mapLinkInfo.getTypeidToName();
        }
    }

    public Map<Integer, PlaceidTypenameDuple> getMapNodeTypeIdToName(MapNodeInfo mapNodeInfo) {
        iterate();
        if (MapServiceUtil.isHandleBundle(dvUuid, vizUuid)) {
            return null;
        } else {
            return mapNodeInfo.getTypeIdToName();
        }
    }

    public Map<Integer, TrackidTracknameDuple> getMapTrackTypeIdToName(MapTrackInfo mapTrackInfo) {
        iterate();
        if (MapServiceUtil.isHandleBundle(dvUuid, vizUuid)) {
            return null;
        } else {
            return mapTrackInfo.getTrackIdToKey();
        }
    }

    public boolean isMapPinned() {
        return MapCacheUtil.isMapPinned(vizUuid);
    }

    public void invalidateExtentIfMapNotPinned() {
        iterate();
        MapCacheUtil.invalidateExtentIfMapNotPinned(vizUuid);
    }

    public List<Crumb> getBreadcrumb() {
        iterate();
        return MapServiceUtil.getBreadcrumb(dvUuid, vizUuid);
    }

    public boolean isCountChildren() {
        iterate();
        return MapServiceUtil.isCountChildren(dvUuid, vizUuid);
    }

    public void addBreadcrumb(List<Crumb> bundleLevel) {
        iterate();
        MapServiceUtil.addBreadcrumb(dvUuid, vizUuid, bundleLevel);
    }

    public void clearTypeInfo(TrackTypeInfo typeInfo) {
        iterate();
        MapServiceUtil.clearTypeInfo(typeInfo);
    }

    public void sortMapNodeType() {
        MapNodeInfo mapNodeInfo = getMapNodeInfo();
        clearTypeInfo(mapNodeInfo);
        PlaceDynamicTypeInfo dynamicTypeInfo = getPlaceDynamicTypeInfo();
        assignTypeIds(dynamicTypeInfo, mapNodeInfo);
    }

    public void clearTypeInfo(PlaceTypeInfo typeInfo) {
        iterate();
        MapServiceUtil.clearTypeInfo(typeInfo);
    }

    public PlaceDynamicTypeInfo getPlaceDynamicTypeInfo() {
        iterate();
        return MapCacheUtil.getPlaceDynamicTypeInfo(vizUuid);
    }

    private void assignTypeIds(PlaceDynamicTypeInfo dynamicTypeInfo, MapNodeInfo mapNodeInfo) {
        iterate();
        for (PlaceidTypenameDuple key : mapNodeInfo.getTypenameToColor().keySet()) {
            if (key != null) {
                Integer id = getIdFromDynamicTypeInfo(dynamicTypeInfo, key);
                applyTypenameToTypeInfo(mapNodeInfo, key, id);
            }
        }
    }

    private Integer getIdFromDynamicTypeInfo(PlaceDynamicTypeInfo dynamicTypeInfo, PlaceidTypenameDuple key) {
        iterate();
        Integer id = dynamicTypeInfo.getTypenameToId().get(key);
        if (id == null) {
            TreeSet<Integer> treeSet = new TreeSet<Integer>(dynamicTypeInfo.getTypeIdToName().keySet());
            if (!treeSet.isEmpty()) {
                id = treeSet.last() + 1;
            } else {
                id = 0;
            }
            applyTypenameToTypeInfo(dynamicTypeInfo, key, id);
        }
        return id;
    }

    public void applyTypenameToTypeInfo(PlaceTypeInfo typeInfo, PlaceidTypenameDuple key, int id) {
        iterate();
        MapServiceUtil.applyTypenameToTypeInfo(typeInfo, key, id);
    }

    public void sortCurrentMapTrackSummaryMapNodeType() {
        TrackmapNodeInfo mapNodeInfo = getTrackmapNodeInfo();
        TrackDynamicTypeInfo dynamicTypeInfo = MapServiceUtil.getTrackDynamicTypeInfo(vizUuid);
        if (dynamicTypeInfo == null) {
            TrackSummaryNodeTypeSorter sorter = new TrackSummaryNodeTypeSorter(this, mapNodeInfo);
            sorter.sort();
        } else {
            clearTypeInfo(mapNodeInfo);
            assignTypeIds(dynamicTypeInfo, mapNodeInfo);
        }
    }

    public void sortCurrentMapSummaryMapNodeType() {
        MapNodeInfo mapNodeInfo = getMapNodeInfo();
        if (isUseHome()) {
            MapNodeTypeSorter sorter = new MapNodeTypeSorter(this, mapNodeInfo);
            sorter.sort();
        } else {
            clearTypeInfo(mapNodeInfo);
            PlaceDynamicTypeInfo dynamicTypeInfo = MapServiceUtil.getPlaceDynamicTypeInfo(vizUuid);
            assignTypeIds(dynamicTypeInfo, mapNodeInfo);
        }
    }

    public void addBreadcrumbFromRequestString(String requestString) {
        iterate();
        Crumb crumb = extractCrumb(requestString);
        crumb.setPreviousInitialExtent(getMapVizInfo().getInitialExtent());
        List<Crumb> breadcrumb = getBreadcrumb();
        breadcrumb.add(crumb);
    }

    private Crumb extractCrumb(String requestString) {
        Crumb crumb = new Crumb();
        Extent extent = new Extent();
        String[] values = requestString.split("&");
        for (String value : values) {
            String[] subValues = value.split("=");
            switch (subValues[0]) {
                case "xmin":
                    extent.setXmin(Double.parseDouble(subValues[1]));
                    break;
                case "ymin":
                    extent.setYmin(Double.parseDouble(subValues[1]));
                    break;
                case "xmax":
                    extent.setXmax(Double.parseDouble(subValues[1]));
                    break;
                case "ymax":
                    extent.setYmax(Double.parseDouble(subValues[1]));
                    break;
                case "wkid":
                    SpatialReference spatialReference = new SpatialReference();
                    spatialReference.setWkid(Integer.parseInt(subValues[1]));
                    extent.setSpatialReference(spatialReference);
                    break;
                case "zoom":
                    extent.setZoom(Integer.parseInt(subValues[1]));
                    break;
                case "criterion":
                    Map<Long, AugmentedMapNode> mapNodeMap = getMapNodeByIdMap();
                    Long id = Long.parseLong(subValues[1]);
                    AugmentedMapNode mapNode = mapNodeMap.get(id);
                    BundleMapNode bundledMapNode = (BundleMapNode) mapNode;
                    crumb.setCriterion(bundledMapNode.getBundleValue());
                    break;
                default:
                    break;
            }
        }
        crumb.setPreviousExtent(extent);
        return crumb;
    }

    private void deselectAll() {
        iterate();
        MapServiceUtil.deselectAll(dvUuid, vizUuid);
    }

    public void setShowLeaves(boolean showLeaves) {
        iterate();
        MapServiceUtil.setShowLeaves(dvUuid, vizUuid, showLeaves);
    }

    public void setExtentIfMapNotPinned(Extent extent) {
        iterate();
        if (isUseTrackMap() || isBundleUsed()) {
            MapCacheUtil.setCurrentExtentIfMapNotPinned(vizUuid, extent);
        } else {
            MapCacheUtil.setCurrentExtentIfMapNotPinned(vizUuid, null);
        }
    }

    public void initializeRangeInfo() {
        List<TrackMapSummaryGrid.SequenceSortValue> sequenceSortValues = getSeriesValues();
        if (sequenceSortValues == null) {
            MapCacheUtil.removeRange(vizUuid);
        } else {
            MapCacheUtil.setRange(vizUuid, 0, sequenceSortValues.size() - 1);
        }
    }

    /**** MapVizInfo ****/

    private MapVizInfo getMapVizInfo() {
        iterate();
        if (mapVizInfo == null) {
            mapVizInfo = MapCacheUtil.getMapVizInfo(vizUuid);
            iterate();
        }
        return mapVizInfo;
    }

    public int getCoarsestMapSummaryPrecision() {
        return getMapVizInfo().getCoarsestMapSummaryPrecision();
    }

    public void setCoarsestMapSummaryPrecision(int coarsestPrecision) {
        getMapVizInfo().setCoarsestMapSummaryPrecision(coarsestPrecision);
    }

    public boolean isLinkLimitReached() {
        return getMapVizInfo().isLinkLimitReached();
    }

    public void setLinkLimitReached(boolean linkLimitReached) {
        getMapVizInfo().setLinkLimitReached(linkLimitReached);
    }

    public boolean isPointLimitReached() {
        return getMapVizInfo().isPointLimitReached();
    }

    public void setPointLimitReached(boolean pointLimitReached) {
        getMapVizInfo().setPointLimitReached(pointLimitReached);
    }

    void setLegendEnabled(boolean legendEnabled) {
        getMapVizInfo().setLegendEnabled(legendEnabled);
    }

    void setMultiTypeDecoratorEnabled(boolean multiTypeDecoratorEnabled) {
        getMapVizInfo().setMultiTypeDecoratorEnabled(multiTypeDecoratorEnabled);
    }

    public void setHomeStatus(MapVizInfo.HomeStatus homeStatus) {
        MapCacheUtil.setHomeStatus(vizUuid, homeStatus);
    }

    public boolean isPlaceLimitOrTrackTypeLimitReached() {
        return isPlaceTypeLimitReached() || isTrackTypeLimitReached();
    }

    public boolean isPlaceTypeLimitReached() {
        return getMapVizInfo().isPlaceTypeLimitReached();
    }

    public boolean isTrackTypeLimitReached() {
        return getMapVizInfo().isTrackTypeLimitReached();
    }

    private boolean isUseHome() {
        return getMapVizInfo().isUseHome(cacheLocation);
    }

    public void invalidateInitialExtent() {
        getMapVizInfo().invalidateInitialExtent();
    }

    void invalidateMapNodeInfoAndMapLinkInfo() {
        getMapVizInfo().invalidateMapNodeInfoAndMapLinkInfo(cacheLocation);
    }

    /**** CurrentInfo ****/

    private CurrentInfo getCurrentInfo() {
        iterate();
        if (currentInfo == null) {
            currentInfo = getMapVizInfo().getCurrentInfo(cacheLocation);
            iterate();
        }
        return currentInfo;
    }

    public void setTrackMapNodeInfo(TrackmapNodeInfo trackmapNodeInfo) {
        getCurrentInfo().setTrackmapNodeInfo(trackmapNodeInfo);
    }

    public Integer getItemsInViz() {
        Integer itemsInViz = getCurrentInfo().getItemsInViz();
        if (itemsInViz == null) {
            itemsInViz = calculateItemsInViz();
            getCurrentInfo().setItemsInViz(itemsInViz);
        }
        return itemsInViz;
    }

    private Integer calculateItemsInViz() {
        Integer itemsInViz = 0;
        itemsInViz += getNodeCount();
        itemsInViz += getLinkCount();
        return itemsInViz;
    }

    private Integer getNodeCount() {
        Map<Long, AugmentedMapNode> mapNodeMapById = getTheRightSummaryMapNodeMapById();
        if (mapNodeMapById == null) {
            return 0;
        } else {
            return mapNodeMapById.size();
        }
    }

    private Map<Long, AugmentedMapNode> getTheRightSummaryMapNodeMapById() {
        if (isUseTrackMap()) {
            return getTrackMapNodeMapById();
        } else {
            return getMapNodeByIdMap();
        }
    }

    private Map<Long, AugmentedMapNode> getTrackMapNodeMapById() {
        Map<Long, AugmentedMapNode> mapNodeMapById = null;
        if (isCurrentlyAtDetailLevel()) {
            MapNodeInfo mapNodeInfo = getMapNodeInfo();
            if (mapNodeInfo != null) {
                mapNodeMapById = mapNodeInfo.getMapById();
            }
        } else {
            mapNodeMapById = getTrackmapNodeByIdMap();
        }
        return mapNodeMapById;
    }

    public boolean isCurrentlyAtDetailLevel() {
        Integer currentMapSummaryPrecision = getCurrentMapSummaryPrecision();
        return currentMapSummaryPrecision == mapConfigDetailLevel;
    }

    public Integer getCurrentMapSummaryPrecision() {
        CurrentInfo currentInfo = getCurrentInfo();
        Integer mapSummaryPrecision = currentInfo.getMapSummaryPrecision();
        while (mapSummaryPrecision == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
                break;
            }
            iterate();
            mapSummaryPrecision = currentInfo.getMapSummaryPrecision();
        }
        return mapSummaryPrecision;
    }

    public void setCurrentMapSummaryPrecision(int currentMapPrecision) {
        getCurrentInfo().setMapSummaryPrecision(currentMapPrecision);
    }

    public MapNodeInfo getMapNodeInfo() {
        return getCurrentInfo().getMapNodeInfo();
    }

    public void setMapNodeInfo(MapNodeInfo mapNodeInfo) {
        getCurrentInfo().setMapNodeInfo(mapNodeInfo);
    }

    private Map<Long, AugmentedMapNode> getTrackmapNodeByIdMap() {
        TrackmapNodeInfo mapNodeInfo = getTrackmapNodeInfo();
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getMapNodeById();
        }
    }

    public Map<Geometry, AugmentedMapNode> getMapNodeByGeometryMap() {
        MapNodeInfo mapNodeInfo = getMapNodeInfo();
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getMapByGeometry();
        }
    }

    public Set<AugmentedMapNode> getCombinedMapNodesSet() {
        MapNodeInfo mapNodeInfo = getMapNodeInfo();
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getCombinedMapNodes();
        }
    }

    public TrackmapNodeInfo getTrackmapNodeInfo() {
        return getCurrentInfo().getTrackmapNodeInfo();
    }

    public Map<Long, AugmentedMapNode> getMapNodeByIdMap() {
        MapNodeInfo mapNodeInfo = getMapNodeInfo();
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getMapById();
        }
    }

    private Integer getLinkCount() {
        Map<Long, MapLink> mapLinkMapById = getMapLinkByIdMap();
        if (mapLinkMapById == null) {
            return 0;
        } else {
            return mapLinkMapById.size();
        }
    }

    private Map<Long, MapLink> getMapLinkByIdMap() {
        if (isUseTrackMap()) {
            MapTrackInfo mapTrackInfo = getMapTrackInfo();
            if (mapTrackInfo == null) {
                return null;
            } else {
                return mapTrackInfo.getMapLinkById();
            }
        } else {
            MapLinkInfo mapLinkInfo = getMapLinkInfo();
            if (mapLinkInfo == null) {
                return null;
            } else {
                return mapLinkInfo.getMapById();
            }
        }
    }

    public MapTrackInfo getMapTrackInfo() {
        return getCurrentInfo().getMapTrackInfo();
    }

    public MapLinkInfo getMapLinkInfo() {
        return getCurrentInfo().getMapLinkInfo();
    }

    public Map<TrackidTracknameDuple, List<MapLink>> getMapLinkByKey() {
        Map<TrackidTracknameDuple, List<MapLink>> mapLinkByKey = null;
        MapTrackInfo mapTrackInfo = getMapTrackInfo();
        if (mapTrackInfo != null) {
            mapLinkByKey = mapTrackInfo.getMapLinkByKey();
        }
        return mapLinkByKey;
    }

    public Map<TrackidTracknameDuple, Integer> getTrackTypenameToId() {
        TrackDynamicTypeInfo dynamicTypeInfo = getTrackDynamicTypeInfo();
        if (dynamicTypeInfo != null) {
            return dynamicTypeInfo.getTrackKeyToId();
        }
        return null;
    }

    public Map<String, List<MapLink>> getMapLinkByTypeMap() {
        MapLinkInfo mapLinkInfo = getCorrectMapLinkInfo();
        if (mapLinkInfo == null) {
            return null;
        } else {
            return mapLinkInfo.getMapByType();
        }
    }

    private MapLinkInfo getCorrectMapLinkInfo() {
        if (!isCurrentlyAtDetailLevel()) {
            return null;
        } else {
            return getMapLinkInfo();
        }
    }

    public Map<Long, AugmentedMapNode> getCurrentTrackMapSummaryMapNodeByIdMap() {
        return getTrackmapNodeByIdMap();
    }

    public void initializeMapNodeInfo() {
        MapNodeInfo mapNodeInfo = new MapNodeInfo();
        setMapNodeInfo(mapNodeInfo);
    }

    public void initializeMapNodeInfoAndMapTrackInfo() {
        initializeMapNodeInfo();
        initializeMapTrackInfo();
    }

    public void initializeMapNodeInfoAndMapLinkInfo() {
        initializeMapNodeInfo();
        MapLinkInfo mapLinkInfo = new MapLinkInfo();
        getCurrentInfo().setMapLinkInfo(mapLinkInfo);
    }

    private void initializeMapTrackInfo() {
        MapTrackInfo mapTrackInfo = new MapTrackInfo();
        getCurrentInfo().setMapTrackInfo(mapTrackInfo);
    }

    public boolean isMapCacheNotAvailable() {
        Map<Long, AugmentedMapNode> mapNodeMapById = getTheRightSummaryMapNodeMapById();
        return mapNodeMapById == null;
    }

    /**** MapSettingsDTO ****/
    public MapSettingsDTO getMapSettings() {
        iterate();
        if (mapSettingsDTO == null) {
            mapSettingsDTO = getMapVizInfo().getMapSettings();
            if (mapSettingsDTO == null) {
                iterate();
            }
            if (mapSettingsDTO == null) {
                throw new MapCacheStaleException();
            }
        }
        return mapSettingsDTO;
    }

    public boolean isUseHeatMap() {
        return getMapSettings().isUseHeatMap();
    }

    public boolean isUseTrackMap() {
        return getMapSettings().isUseTrackMap();
    }

    public boolean isBundleUsed() {
        return getMapSettings().isBundleUsed();
    }

    private void assignTypeIds(TrackDynamicTypeInfo dynamicTypeInfo, TrackmapNodeInfo mapNodeInfo) {
        iterate();
        for (TrackidTracknameDuple key : mapNodeInfo.getTrackkeyToColor().keySet()) {
            if (key != null) {
                Integer id = getIdFromDynamicTypeInfo(dynamicTypeInfo, key);
                applyTypenameToTypeInfo(mapNodeInfo, key, id);
            }
        }
    }

    private Integer getIdFromDynamicTypeInfo(TrackDynamicTypeInfo dynamicTypeInfo, TrackidTracknameDuple key) {
        iterate();
        Integer id = dynamicTypeInfo.getTrackKeyToId().get(key);
        if (id == null) {
            TreeSet<Integer> treeSet = new TreeSet<Integer>(dynamicTypeInfo.getTrackIdToKey().keySet());
            if (treeSet.isEmpty()) {
                id = 1;
            } else {
                id = treeSet.last() + 1;
            }
            applyTypenameToTypeInfo(dynamicTypeInfo, key, id);
        }
        return id;
    }

    /**** others ****/

    public boolean isSelected(AbstractMapSelection mapSelection, MapNode mapNode) {
        iterate();
        if (mapSelection == null) {
            return false;
        } else {
            return mapSelection.containsGeometry(mapNode.getGeometry());
        }
    }

    public boolean isSelected(AbstractMapSelection mapSelection, MapLink mapLink) {
        iterate();
        if (mapSelection == null) {
            return false;
        }
        return mapSelection.containsLink(mapLink.getLinkGeometry());
    }

    public void refreshMapState() {
        invalidateItemsInViz();
        setHomeStatus(MapVizInfo.HomeStatus.NOT_LOADED);
        invalidateMapNodeInfoAndMapLinkInfo();
        deselectAll();
    }

    private void invalidateItemsInViz() {
        getMapVizInfo().invalidateHomeItemsInViz();
        getCurrentInfo().invalidateItemsInViz();
    }

    public Extent getInitialExtent() {
        return getMapVizInfo().getInitialExtent();
    }

    public void setInitialExtent(Extent previousInitialExtent) {
        getMapVizInfo().setInitialExtent(previousInitialExtent);
    }

    /**** InitialExtentBuilder ****/

    public Map<Long, AugmentedMapNode> getHomeTrackMapNodeByIdMap() {
        mapVizInfo = getMapVizInfo();
        TrackmapNodeInfo mapNodeInfo = mapVizInfo.getHomeTrackmapNodeInfo();
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getMapNodeById();
        }
    }

    public List<MapSummaryExtent> calculateMapSummaryExtents(UBox uBox) {
        MapSummaryExtent mapSummaryExtent;
        if (uBox == null) {
            return new ArrayList<MapSummaryExtent>();
        } else {
            mapSummaryExtent = uBox.getMapSummaryExtent();
            return calculateMapSummaryExtents(mapSummaryExtent);
        }
    }

    public List<MapSummaryExtent> calculateMapSummaryExtents(MapSummaryExtent mapSummaryExtent) {
        List<MapSummaryExtent> mapSummaryExtents = new ArrayList<MapSummaryExtent>();
        if (mapSummaryExtent != null) {
            if (mapSummaryExtent.getXMin() < -180) {
                MapSummaryExtent fromNeg180 = new MapSummaryExtent(-180.0, mapSummaryExtent.getYMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMax());
                MapSummaryExtent toPos180 = new MapSummaryExtent(mapSummaryExtent.getXMin() + 360, mapSummaryExtent.getYMin(), 180.0, mapSummaryExtent.getYMax());
                mapSummaryExtents.add(fromNeg180);
                mapSummaryExtents.add(toPos180);
            } else {
                mapSummaryExtents.add(mapSummaryExtent);
            }
        }
        return mapSummaryExtents;
    }

    public String generateFilterString(DataView dataView, MapViewDef mapViewDef, List<MapSummaryExtent> mapSummaryExtents) {
        String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettingsDTO);
        String latLongFilterString = getLatLongFilterString(mapSettingsDTO.getPlaceSettings(), mapSummaryExtents);
        if (latLongFilterString != null) {
            if ((filterString == null) || (filterString.trim().length() == 0)) {
                filterString = latLongFilterString;
            } else {
                filterString += " AND " + latLongFilterString;
            }
        }
        return filterString;
    }

    private String getLatLongFilterString(List<PlaceSettingsDTO> placeSettings, List<MapSummaryExtent> mapSummaryExtents) {
        List<String> latLongFilterStringForPlaces = new ArrayList<String>();
        for (int placeId = 0; placeId < placeSettings.size(); placeId++) {
            String latLongFilterStringForPlaceId = MapNodeUtil.getLatLongFilterStringForPlaceId(placeSettings, placeId, mapSummaryExtents);
            latLongFilterStringForPlaces.add(latLongFilterStringForPlaceId);
        }
        if (latLongFilterStringForPlaces.isEmpty()) {
            return null;
        }
        return "(" + latLongFilterStringForPlaces.stream().collect(Collectors.joining(" OR ")) + ")";
    }

    public Extent getExtent() {
        return MapCacheUtil.getCurrentExtentOrCurrentExtentIfMapNotPinned(vizUuid);
    }
}
