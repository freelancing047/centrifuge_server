package csi.server.business.visualization.map;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import csi.config.Configuration;
import csi.server.business.visualization.map.cacheloader.outofbandresources.AbstractOutOfBandResourcesLoader;
import csi.server.business.visualization.map.mapcacheutil.CurrentInfo;
import csi.server.business.visualization.map.mapcacheutil.MapCache;
import csi.server.business.visualization.map.mapcacheutil.MapVizInfo;
import csi.server.business.visualization.map.storage.AbstractMapStorageService;
import csi.server.business.visualization.map.storage.OutOfBandResources;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class MapCacheUtil {
    private static final ConcurrentHashMap<String, ConcurrentMapCacheInfo> concurrentMapCacheInfos = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> mapSequenceNumber = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> mapPinned = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> selectionType = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Extent> currentExtent = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Extent> currentExtentIfMapNotPinned = new ConcurrentHashMap<>();

    private static ConcurrentMapCacheInfo getConcurrentMapCacheInfo(String mapViewDefUuid) {
        concurrentMapCacheInfos.putIfAbsent(mapViewDefUuid, new ConcurrentMapCacheInfo());
        return concurrentMapCacheInfos.get(mapViewDefUuid);
    }

    public static Integer getMapSequenceNumber(String mapViewDefUuid) {
        Integer sequenceNumber = mapSequenceNumber.get(mapViewDefUuid);
        return sequenceNumber;
    }

    public static String getSequenceNumber(String mapViewDefUuid) {
        Integer sequenceNumber = mapSequenceNumber.get(mapViewDefUuid);
        if (sequenceNumber == null) {
            sequenceNumber = 1;
        } else {
            sequenceNumber++;
        }
        mapSequenceNumber.put(mapViewDefUuid, sequenceNumber);
        return sequenceNumber.toString();
    }

    public static void setMapSequenceNumber(String mapViewDefUuid, int sequenceNumber) {
        mapSequenceNumber.put(mapViewDefUuid, sequenceNumber);
    }

    public static void removeMapCacheInfo(String mapViewDefUuid) {
        concurrentMapCacheInfos.remove(mapViewDefUuid);
    }

    /**** JSONController ****/
    public static Integer getPreviousRangeEnd(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getPreviousRangeEnd();
    }

    public static Integer getPreviousRangeStart(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getPreviousRangeStart();
    }

    public static Integer getPreviousRangeSize(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getPreviousRangeSize();
    }

    public static void setPreviousRangeEnd(String mapViewDefUuid, Integer value) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setPreviousRangeEnd(value);
    }

    public static void setPreviousRangeStart(String mapViewDefUuid, Integer value) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setPreviousRangeStart(value);
    }

    public static void setPreviousRangeSize(String mapViewDefUuid, Integer value) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setPreviousRangeSize(value);
    }

    public static void setMapCacheLocation(String mapViewDefUuid, Integer sequenceNumber) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setMapCacheAt(sequenceNumber);
        MapVizInfo mapVizInfo = getMapVizInfo(mapViewDefUuid);
        mapVizInfo.clearStaleCaches(sequenceNumber);
    }

    public static MapSummaryExtent getMapSummaryExtent(String mapViewDefUuid) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);
        if (currentInfo == null) {
            return null;
        } else {
            return currentInfo.getMapSummaryExtent();
        }
    }

    public static void invalidateMapSummaryExtent(String mapViewDefUuid) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);

        if (currentInfo != null) {
           currentInfo.invalidateMapSummaryExtent();
        }
    }

    public static void addMapSummaryExtent(String mapViewDefUuid, MapSummaryExtent mapSummaryExtent) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);

        if (currentInfo != null) {
           currentInfo.setMapSummaryExtent(mapSummaryExtent);
        }
    }

    /**** MapActionsService ****/

    public static void addMapSelection(String mapViewDefUuid, AbstractMapSelection selection) {
        if (selection.isCleared()) {
            getConcurrentMapCacheInfo(mapViewDefUuid).setMapSelection(null);
        } else {
            getConcurrentMapCacheInfo(mapViewDefUuid).setMapSelection(selection);
        }
    }

    public static void setMultiTypeDecoratorShown(String mapViewDefUuid, boolean value) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setMultiTypeDecoratorShownStatus(value);
    }

    public static void setLinkupDecoratorShown(String mapViewDefUuid, boolean value) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setLinkupDecoratorShownStatus(value);
    }

    public static void setPlaceDynamicTypeInfo(String mapViewDefUuid, PlaceDynamicTypeInfo value) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setPlaceDynamicTypeInfo(value);
    }

    public static void setTrackDynamicTypeInfo(String mapViewDefUuid, TrackDynamicTypeInfo value) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setTrackDynamicTypeInfo(value);
    }

    public static void setRange(String mapViewDefUuid, int start, int end) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setRangeStart(start);
        getConcurrentMapCacheInfo(mapViewDefUuid).setRangeEnd(end);
    }

    public static List<TrackMapSummaryGrid.SequenceSortValue> getSeriesValues(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getRangeSeriesValue();
    }

    public static void setSeriesValues(String mapViewDefUuid, List<TrackMapSummaryGrid.SequenceSortValue> seriesValues) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setRangeSeriesValue(seriesValues);
    }

    public static Collection<AugmentedMapNode> getMapNodesByType(String mapViewDefUuid, PlaceidTypenameDuple key) {
        Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType = getMapNodeByTypeMap(mapViewDefUuid);
        if (mapNodeByType == null) {
            return null;
        } else {
            return mapNodeByType.get(key);
        }
    }

    private static Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> getMapNodeByTypeMap(String mapViewDefUuid) {
        MapNodeInfo mapNodeInfo = getMapNodeInfo(mapViewDefUuid);
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getMapByType();
        }
    }

    public static Collection<MapLink> getMapLinksByType(String mapViewDefUuid, String typename) {
        Map<String, List<MapLink>> mapLinkByType = getMapLinkByTypeMap(mapViewDefUuid);
        if (mapLinkByType == null) {
            return null;
        } else {
            return mapLinkByType.get(typename);
        }
    }

    public static void invalidateMapNodeInfoAndMapTrackInfo(String mapViewDefUuid) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);
        if (currentInfo != null) {
            currentInfo.invalidateMapNodeInfo();
            currentInfo.invalidateMapTrackInfo();
        }
    }

    public static CurrentInfo getCurrentInfo(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        Integer currentCacheLocation = getCacheAt(mapViewDefUuid);
        if (currentCacheLocation == null) {
            return null;
        } else {
            return mapVizInfo.getCurrentInfo(currentCacheLocation);
        }
    }

    public static boolean isLegendEnabled(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.isLegendEnabled();
    }

    public static boolean isMultiTypeDecoratorEnabled(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.isMultiTypeDecoratorEnabled();
    }

    public static Set<Geometry> getGeometriesOfType(String mapViewDefUuid, PlaceidTypenameDuple key) {
        MapSummaryGrid geoInfo = getMapSummaryGrid(mapViewDefUuid);
        if (geoInfo == null) {
            return null;
        } else {
            return geoInfo.getGeometriesOfType(key);
        }
    }

    /**** MapServiceUtil ****/

    static void addMapSettings(String mapViewDefUuid, MapSettingsDTO mapSettings) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        mapVizInfo.setMapSettings(mapSettings);
    }

    static Map<Long, AugmentedMapNode> getTrackSummaryMapNodeMapById(String mapViewDefUuid) {
        MapSettingsDTO mapSettingsDTO = getMapSettings(mapViewDefUuid);
        if (mapSettingsDTO == null) {
            return null;
        } else {
            return getTrackMapNodeMapById(mapViewDefUuid);
        }
    }

    /**** TypeLimitStatusChecker ****/

    public static void setPlaceTypeLimitReached(String mapViewDefUuid, boolean limitReached) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        mapVizInfo.setPlaceTypeLimitReached(limitReached);
    }

    public static void setTrackTypeLimitReached(String mapViewDefUuid, boolean limitReached) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        mapVizInfo.setTrackTypeLimitReached(limitReached);
    }

    /**** MapSelectionToRowsConverter ****/

    public static Map<LinkGeometry, Set<Integer>> getTrackLinkGeometryToRowIds(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getLinkGeometryToRowIds();
    }

    /**** MapCacheHandler ****/

    public static boolean isLinkupDecoratorShown(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getLinkupDecoratorShownStatus();
    }

    public static PlaceDynamicTypeInfo getPlaceDynamicTypeInfo(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getPlaceDynamicTypeInfo();
    }

    public static TrackDynamicTypeInfo getTrackDynamicTypeInfo(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getTrackDynamicTypeInfo();
    }

    static void setTrackGeometryToRowIds(String mapViewDefUuid, Map<Geometry, Set<Integer>> geometryToRowId) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setTrackGeometryToRowIds(geometryToRowId);
    }

    static void setTrackLinkGeometryToRowIds(String mapViewDefUuid, Map<LinkGeometry, Set<Integer>> linkGeometryToRowId) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setTrackLinkGeometryToRowIds(linkGeometryToRowId);
    }

    static void setTrackTypeToGeometries(String mapViewDefUuid, Map<TrackidTracknameDuple, Set<Geometry>> rowIdToLinkGeometry) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setTrackTypeToGeometries(rowIdToLinkGeometry);
    }

    static void setTrackTypeToLinkGeometries(String mapViewDefUuid, Map<TrackidTracknameDuple, Set<LinkGeometry>> rowIdToLinkGeometry) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setTrackTypeToLinkGeometries(rowIdToLinkGeometry);
    }

    static Integer getCacheAt(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getMapCacheAt();
    }

    static boolean usingLatestMapCache(String vizUuid, Integer cacheLocation) {
        Integer currentCacheLocation = getCacheAt(vizUuid);
        if (currentCacheLocation == null) {
            return true;
        } else {
            return currentCacheLocation.equals(currentCacheLocation);
        }
    }

    static boolean isMultiTypeDecoratorShown(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getMultiTypeDecoratorShownStatus();
    }

    /**** self and MapCacheHandler ****/

    private static Map<String, List<MapLink>> getMapLinkByTypeMap(String mapViewDefUuid) {
        MapLinkInfo mapLinkInfo = getCorrectMapLinkInfo(mapViewDefUuid);
        if (mapLinkInfo == null) {
            return null;
        } else {
            return mapLinkInfo.getMapByType();
        }
    }

    private static Map<Long, AugmentedMapNode> getTrackmapNodeByIdMap(String mapViewDefUuid) {
        TrackmapNodeInfo mapNodeInfo = getTrackmapNodeInfo(mapViewDefUuid);
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getMapNodeById();
        }
    }

    static MapVizInfo getMapVizInfo(String mapViewDefUuid) {
        return MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
    }

    /**** Multiple ****/

    public static AbstractMapSelection getMapSelection(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getMapSelection();
    }

    public static Integer getRangeStart(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getRangeStart();
    }

    public static Integer getRangeEnd(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getRangeEnd();
    }

    public static void removeSequenceBarInfo(String mapViewDefUuid) {
        removeRange(mapViewDefUuid);
        removeSeriesValues(mapViewDefUuid);
        removeRowIdInfo(mapViewDefUuid);
        removeTrackTypeToLinkGeometries(mapViewDefUuid);
    }

    static void removeRange(String mapViewDefUuid) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setRangeStart(null);
        getConcurrentMapCacheInfo(mapViewDefUuid).setRangeEnd(null);
    }

    private static void removeSeriesValues(String mapViewDefUuid) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setRangeSeriesValue(null);
    }

    private static void removeRowIdInfo(String mapViewDefUuid) {
        removeTrackGeometryToRowIds(mapViewDefUuid);
        removeTrackLinkGeometryToRowIds(mapViewDefUuid);
    }

    private static void removeTrackGeometryToRowIds(String mapViewDefUuid) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setTrackGeometryToRowIds(null);
    }

    private static void removeTrackLinkGeometryToRowIds(String mapViewDefUuid) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setTrackLinkGeometryToRowIds(null);
    }

    private static void removeTrackTypeToLinkGeometries(String mapViewDefUuid) {
        getConcurrentMapCacheInfo(mapViewDefUuid).setTrackTypeToLinkGeometries(null);
    }

    public static Map<Geometry, Set<Integer>> getTrackGeometryToRowIds(String mapViewDefUuid) {
        return getConcurrentMapCacheInfo(mapViewDefUuid).getTrackGeometryToRowIds();
    }

    public static MapSettingsDTO getMapSettings(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.getMapSettings();
    }

    public static boolean isUseTrack(String mapViewDefUuid) {
        MapSettingsDTO mapSettingsDTO = getMapSettings(mapViewDefUuid);
        return mapSettingsDTO.isUseTrackMap();
    }

    public static MapNodeInfo getMapNodeInfo(String mapViewDefUuid) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);
        if (currentInfo == null) {
            return null;
        } else {
            return currentInfo.getMapNodeInfo();
        }
    }

    public static Map<Long, AugmentedMapNode> getMapNodeByIdMap(String mapViewDefUuid) {
        MapNodeInfo mapNodeInfo = getMapNodeInfo(mapViewDefUuid);
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getMapById();
        }
    }

    public static void invalidate(String mapViewDefUuid) {
        MapCache.getInstance().invalidate(mapViewDefUuid);
        AbstractMapStorageService.instance().invalidate(mapViewDefUuid);
    }

    public static MapLinkInfo getCorrectMapLinkInfo(String mapViewDefUuid) {
        if (!isCurrentlyAtDetailLevel(mapViewDefUuid)) {
            return null;
        } else {
            return getMapLinkInfo(mapViewDefUuid);
        }
    }

    public static TrackmapNodeInfo getTrackmapNodeInfo(String mapViewDefUuid) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);
        if (currentInfo == null) {
            return null;
        } else {
            return currentInfo.getTrackmapNodeInfo();
        }
    }

    public static Extent getInitialExtent(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.getInitialExtent();
    }

    public static void invalidateInitialExtent(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = getMapVizInfo(mapViewDefUuid);
        mapVizInfo.invalidateInitialExtent();
    }

    public static void invalidateMapNodeInfoAndMapLinkInfo(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = getMapVizInfo(mapViewDefUuid);
        if (isHomeLoaded(mapViewDefUuid) || isHomeLoading(mapViewDefUuid)) {
            setHomeStatus(mapViewDefUuid, MapVizInfo.HomeStatus.NOT_LOADED);
            int currentCacheLocation = getCacheAt(mapViewDefUuid);
            mapVizInfo.invalidateMapNodeInfoAndMapLinkInfo(currentCacheLocation);
        }
    }

    public static void setCurrentMapSummaryPrecision(String mapViewDefUuid, int precision) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);
        if (currentInfo != null) {
            currentInfo.setMapSummaryPrecision(precision);
        }
    }

    public static Integer getCoarsestMapSummaryPrecision(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.getCoarsestMapSummaryPrecision();
    }

    public static Map<Geometry, AugmentedMapNode> getMapNodeByGeometryMap(String mapViewDefUuid) {
        MapNodeInfo mapNodeInfo = getMapNodeInfo(mapViewDefUuid);
        if (mapNodeInfo == null) {
            return null;
        } else {
            return mapNodeInfo.getMapByGeometry();
        }
    }

    public static boolean isPlaceLimitOrTrackTypeLimitReached(String mapViewDefUuid) {
        return isPlaceTypeLimitReached(mapViewDefUuid) || isTrackTypeLimitReached(mapViewDefUuid);
    }

    public static boolean isPlaceTypeLimitReached(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.isPlaceTypeLimitReached();
    }

    public static boolean isTrackTypeLimitReached(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.isTrackTypeLimitReached();
    }

    public static Set<LinkGeometryPlus> getLinks(String mapViewDefUuid) {
        OutOfBandResources outOfBandResources = getOutOfBandResources(mapViewDefUuid);
        if (outOfBandResources == null) {
            return null;
        } else {
            return outOfBandResources.getLinks();
        }
    }

    public static Map<Long, AugmentedMapNode> getTheRightSummaryMapNodeMapById(String mapViewDefUuid) {
        MapSettingsDTO mapSettingsDTO = getMapSettings(mapViewDefUuid);
        if (mapSettingsDTO == null) {
            return null;
        } else {
            if (mapSettingsDTO.isUseTrackMap()) {
                return getTrackMapNodeMapById(mapViewDefUuid);
            } else {
                return getMapNodeByIdMap(mapViewDefUuid);
            }
        }
    }

    private static Map<Long, AugmentedMapNode> getTrackMapNodeMapById(String mapViewDefUuid) {
        Map<Long, AugmentedMapNode> mapNodeMapById = null;
        if (isCurrentlyAtDetailLevel(mapViewDefUuid)) {
            MapNodeInfo mapNodeInfo = getMapNodeInfo(mapViewDefUuid);
            if (mapNodeInfo != null) {
                mapNodeMapById = mapNodeInfo.getMapById();
            }
        } else {
            mapNodeMapById = getTrackmapNodeByIdMap(mapViewDefUuid);
        }
        return mapNodeMapById;
    }

    public static MapTrackInfo getMapTrackInfo(String mapViewDefUuid) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);
        if (currentInfo == null) {
            return null;
        } else {
            return currentInfo.getMapTrackInfo();
        }
    }

    static MapLinkInfo getMapLinkInfo(String mapViewDefUuid) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);
        if (currentInfo == null) {
            return null;
        } else {
            return currentInfo.getMapLinkInfo();
        }
    }

    public static boolean isCurrentlyAtDetailLevel(String mapViewDefUuid) {
        CurrentInfo currentInfo = getCurrentInfo(mapViewDefUuid);
        if (currentInfo == null) {
            /// TODO: need to look into this more
            return false;
        } else {
            Integer mapSummaryPrecision = currentInfo.getMapSummaryPrecision();
            int detailLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
            if (mapSummaryPrecision == null) {
                return false;
            } else {
                return mapSummaryPrecision == detailLevel;
            }
        }
    }

    public static void setUseHome(String mapViewDefUuid, boolean useHome) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        int currentCacheLocation = getCacheAt(mapViewDefUuid);
        mapVizInfo.setUseHome(currentCacheLocation, useHome);
    }

    /**** Out Of Band ****/
    static void addOutOfBandResources(String mapViewDefUuid, OutOfBandResources outOfBandResources) {
        AbstractMapStorageService.instance().addOutOfBandResources(mapViewDefUuid, outOfBandResources);
    }

    static boolean outOfBandResourceBuilding(String mapViewDefUuid) {
        return AbstractOutOfBandResourcesLoader.saveJobs.containsKey(mapViewDefUuid);
    }

    private static OutOfBandResources getOutOfBandResources(String mapViewDefUuid) {
        Semaphore semaphore = AbstractOutOfBandResourcesLoader.acquireSemaphore(mapViewDefUuid);
        semaphore.release();
        return AbstractMapStorageService.instance().getOutOfBandResources(mapViewDefUuid);
    }

    static Map<Integer, Map<String, NodeSizeCalculator>> getNodeSizeCalculatorRegistry(String mapViewDefUuid) {
        OutOfBandResources outOfBandResources = getOutOfBandResources(mapViewDefUuid);
        if (outOfBandResources == null) {
            return null;
        }
        return outOfBandResources.getRegistry();
    }

    public static MapSummaryGrid getMapSummaryGrid(String mapViewDefUuid) {
        OutOfBandResources outOfBandResources = getOutOfBandResources(mapViewDefUuid);
        if (outOfBandResources == null) {
            return null;
        }
        return outOfBandResources.getMapSummaryGrid();
    }

    public static TrackMapSummaryGrid getTrackMapSummaryGrid(String mapViewDefUuid) {
        OutOfBandResources outOfBandResources = getOutOfBandResources(mapViewDefUuid);
        if (outOfBandResources == null) {
            return null;
        }
        MapSummaryGrid mapSummaryGrid = outOfBandResources.getMapSummaryGrid();
        if (mapSummaryGrid instanceof TrackMapSummaryGrid) {
            return (TrackMapSummaryGrid) mapSummaryGrid;
        } else {
            return null;
        }
    }

    static void invalidateOutOfBandResources(String mapViewDefUuid) {
        Semaphore semaphore = AbstractOutOfBandResourcesLoader.acquireSemaphore(mapViewDefUuid);
        semaphore.release();
        AbstractMapStorageService.instance().invalidateOutOfBandResources(mapViewDefUuid);
    }

    public static boolean isHomeLoaded(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.isHomeLoaded();
    }

    public static boolean isHomeLoading(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        return mapVizInfo.isHomeLoading();
    }

    public static void setHomeStatus(String mapViewDefUuid, MapVizInfo.HomeStatus homeStatus) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        mapVizInfo.setHomeStatus(homeStatus);
    }

    public static boolean isBundleUsed(String mapViewDefUuid) {
        MapVizInfo mapVizInfo = MapCache.getInstance().getMapVizInfo(mapViewDefUuid);
        MapSettingsDTO mapSettings = mapVizInfo.getMapSettings();
        return mapSettings.isBundleUsed();
    }

    public static boolean isMapPinned(String mapViewDefUuid) {
        Boolean isMapPinned = mapPinned.get(mapViewDefUuid);
        if (isMapPinned == null) {
            isMapPinned = false;
        }
        return isMapPinned;
    }

    public static void setMapPinned(String mapViewDefUuid, Boolean value) {
        if (value == null) {
            mapPinned.remove(mapViewDefUuid);
        } else {
            mapPinned.put(mapViewDefUuid, value);
        }
    }

    public static Integer getSelectionMode(String mapViewDefUuid) {
        Integer getSelectionType = selectionType.get(mapViewDefUuid);
        if (getSelectionType == null) {
            getSelectionType = 0;
        }
        return getSelectionType;
    }

    public static void setSelectionMode(String mapViewDefUuid, Integer value) {
        if (value == null) {
            selectionType.remove(mapViewDefUuid);
        } else {
            selectionType.put(mapViewDefUuid, value);
        }
    }

    public static Extent getCurrentExtentOrCurrentExtentIfMapNotPinned(String mapViewDefUuid) {
        if (isMapPinned(mapViewDefUuid) || !currentExtentIfMapNotPinned.containsKey(mapViewDefUuid)) {
            return currentExtent.get(mapViewDefUuid);
        } else {
            return getCurrentExtentIfMapNotPinned(mapViewDefUuid);
        }
    }

    public static Extent getCurrentExtent(String mapViewDefUuid) {
        return currentExtent.get(mapViewDefUuid);
    }

    public static void setCurrentExtent(String mapViewDefUuid, Extent extent) {
        if (extent == null) {
            currentExtent.remove(mapViewDefUuid);
        } else {
            currentExtent.put(mapViewDefUuid, extent);
        }
    }

    private static Extent getCurrentExtentIfMapNotPinned(String mapViewDefUuid) {
        return currentExtentIfMapNotPinned.get(mapViewDefUuid);
    }

    public static void setCurrentExtentIfMapNotPinned(String mapViewDefUuid, Extent extent) {
        if (extent == null) {
            currentExtentIfMapNotPinned.remove(mapViewDefUuid);
        } else {
            currentExtentIfMapNotPinned.put(mapViewDefUuid, extent);
        }
    }

    private static void invalidateExtent(String mapViewDefUuid) {
        setCurrentExtent(mapViewDefUuid, null);
    }

    public static void invalidateExtentIfMapNotPinned(String mapViewDefUuid) {
        if (!isMapPinned(mapViewDefUuid)) {
            invalidateExtent(mapViewDefUuid);
        }
    }
}
