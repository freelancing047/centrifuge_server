package csi.server.business.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import csi.config.Configuration;
import csi.config.MapConfig;
import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.business.helper.ModelHelper;
import csi.server.business.helper.QueryHelper;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.business.service.map.BreadcrumbTrimmer;
import csi.server.business.service.map.ExtentInfoBuilder;
import csi.server.business.service.map.InitialExtentBuilder;
import csi.server.business.service.map.MapLegendBuilder;
import csi.server.business.service.map.NumPointCounter;
import csi.server.business.service.map.TooltipsRenderer;
import csi.server.business.service.theme.ThemeActionsService;
import csi.server.business.visualization.legend.MapLegendInfo;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.BundleMapNode;
import csi.server.business.visualization.map.CacheLoader;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.LinkGeometryPlus;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapCacheNotAvailable;
import csi.server.business.visualization.map.MapCacheStaleException;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapContext;
import csi.server.business.visualization.map.MapLink;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.business.visualization.map.PointSummaryCacheBuilder;
import csi.server.business.visualization.map.SummaryCacheBuilder;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.business.visualization.map.TrackSummaryCacheBuilder;
import csi.server.business.visualization.map.mapcacheutil.CurrentInfo;
import csi.server.business.visualization.map.mapcacheutil.MapVizInfo;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.MapMetricsType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.SortOrder;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.ExtentInfo;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.HeatMapInfo;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.MapLayerInfo;
import csi.server.common.model.map.MapToolsInfo;
import csi.server.common.model.map.PlaceSizeInfo;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.map.map.MapLayerDTO;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTileLayer;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.DetailMapSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SummaryMapSelection;
import csi.server.common.model.visualization.selection.TrackmapSelection;
import csi.server.common.service.api.MapActionsServiceProtocol;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.AssociationSettingsDTO;
import csi.shared.core.visualization.map.MapConfigDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MetricsDTO;
import csi.shared.core.visualization.map.OverviewRequest;
import csi.shared.core.visualization.map.OverviewResponse;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;
import csi.shared.core.visualization.map.UBox;
import csi.tools.GenerateBasemapsInitial;

@Service(path = "/services/map/actions")
public class MapActionsService extends AbstractService implements MapActionsServiceProtocol {
    private static final Logger LOG = LogManager.getLogger(MapActionsService.class);

    private static final String DV_UUID_PARAM = "dvuuid";
    private static final String VIZ_UUID_PARAM = "vduuid";
    private static final int BIN_SIZE_LIMIT = 10000;
    private static final int DEFAULT_WIDTH = 2000;
    private static int DEFAULT_WEIGHT = 3;
    private static List<String> colors = Arrays.asList(
            //rgb(250),
            rgb(240),
            //rgb(210),
            rgb(180),
            //rgb(150),
            rgb(120),
            //rgb(90),
            rgb(60),
            //rgb(30),
            rgb(0)
    );
    @Inject
    private ThemeActionsService themeActionsService;
    @Autowired
    private GraphActionsService graphActionsService;
    @Autowired
    private ModelActionService modelActionService;
    private MapConfigDTO mapConfigDTO = null;

    @Operation
    private static List<ResourceBasics> listAuthorizedUserBasemaps(AclControlType permissionsIn) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedUserBasemaps(new AclControlType[]{permissionsIn});
        return (null != myResults) ? myResults : new ArrayList<>();
    }

    /**
     * returns false on not authorized and if the object is null
     *
     * @param uuid
     * @return
     */
    private static boolean checkAuthorization(String uuid) {
        return CsiSecurityManager.isAuthorized(uuid, AclControlType.READ);
    }

    private static String rgb(int value) {
        return "rgb(" + value + "," + value + "," + value + ")";
    }

    private static void registerDynamicTypeInfoIfNotSet(String mapViewDefUuid) {
        PlaceDynamicTypeInfo placeDynamicTypeInfo = MapServiceUtil.getPlaceDynamicTypeInfo(mapViewDefUuid);
        if (placeDynamicTypeInfo == null) {
            placeDynamicTypeInfo = new PlaceDynamicTypeInfo();
            MapServiceUtil.setPlaceDynamicTypeInfo(placeDynamicTypeInfo, mapViewDefUuid);
        }
        TrackDynamicTypeInfo trackDynamicTypeInfo = MapServiceUtil.getTrackDynamicTypeInfo(mapViewDefUuid);
        if (trackDynamicTypeInfo == null) {
            trackDynamicTypeInfo = new TrackDynamicTypeInfo();
            MapServiceUtil.setTrackDynamicTypeInfo(trackDynamicTypeInfo, mapViewDefUuid);
        }
    }

    @Override
    public String loadMapCache(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber) {
        while (MapCacheUtil.isHomeLoading(mapViewDefUuid)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                break;
            }
        }
        String status;
        if (MapCacheUtil.isHomeLoaded(mapViewDefUuid)) {
            status = "success";
        } else {
            status = "failure";
            MapCacheUtil.setHomeStatus(mapViewDefUuid, MapVizInfo.HomeStatus.LOADING);
            MapCacheUtil.setMapCacheLocation(mapViewDefUuid, sequenceNumber);
            MapCacheUtil.setUseHome(mapViewDefUuid, true);
            DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
            if (dataView == null) {
                return status;
            }
            MapViewDef mapViewDef = (MapViewDef) dataView.getMeta().getModelDef().findVisualizationByUuid(mapViewDefUuid);
            MapContext mapContext = MapServiceUtil.getMapContext(dataViewUuid, mapViewDefUuid);
            if (mapContext == null) {
                mapContext = new MapContext(dataViewUuid, mapViewDefUuid);
                registerMapContext(mapContext);
            }
            registerDynamicTypeInfoIfNotSet(mapViewDefUuid);
            MapTheme mapTheme = getMapTheme(mapViewDef);
            MapSettingsDTO mapSettingsDTO = MapCacheUtil.getMapSettings(mapViewDefUuid);
            if (mapSettingsDTO == null) {
                mapSettingsDTO = MapServiceUtil.createAndSaveMapSettingsDTO(mapViewDefUuid, mapTheme, dataView, mapViewDef);
            }
            fillCacheWithMapContext(dataViewUuid, mapViewDefUuid);
            if ((mapSettingsDTO != null) && !MapCacheUtil.isPlaceLimitOrTrackTypeLimitReached(mapViewDefUuid)) {
                try {
                    MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber, false);
                    mapCacheHandler.invalidateOutOfBandResources();
                    mapCacheHandler.invalidateExtentIfMapNotPinned();
                    mapCacheHandler.invalidateInitialExtent();
                    CacheLoader loader = new CacheLoader(mapCacheHandler, dataView, mapViewDef, mapTheme, null);
                    loader.load();
                    status = loader.getStatus();
                    if (status.equals("success")) {
                        InitialExtentBuilder builder = new InitialExtentBuilder(mapCacheHandler);
                        builder.build();
                        Extent initialExtent = builder.getInitialExtent();
                        if (initialExtent != null) {
                           mapCacheHandler.setInitialExtent(initialExtent);
                        }
                        mapCacheHandler.setHomeStatus(MapVizInfo.HomeStatus.LOADED);
                    } else {
                        mapCacheHandler.setHomeStatus(MapVizInfo.HomeStatus.NOT_LOADED);
                    }
                } catch (MapCacheStaleException ignored) {
                    MapCacheUtil.setHomeStatus(mapViewDefUuid, MapVizInfo.HomeStatus.NOT_LOADED);
                }
            }
        }
        return status;
    }

    private void registerMapContext(MapContext mapContext) {
        MapServiceUtil.setMapContext(mapContext);
    }

    private MapTheme getMapTheme(MapViewDef mapViewDef) {
        String themeUuid = mapViewDef.getMapSettings().getThemeUuid();
        return getMapThemeByThemeUuid(themeUuid);
    }

    private MapTheme getMapThemeByThemeUuid(String themeUuid) {
        MapTheme mapTheme = null;
        if (themeUuid != null) {
            if (themeActionsService == null) {
               themeActionsService = new ThemeActionsService();
            }
            mapTheme = themeActionsService.findMapTheme(themeUuid);

        }
        return mapTheme;
    }

    @Override
    public String getTooltip(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, Long id) {
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
        MapViewDef mapViewDef = (MapViewDef) dataView.getMeta().getModelDef().findVisualizationByUuid(mapViewDefUuid);
        MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
        TooltipsRenderer renderer = new TooltipsRenderer(mapCacheHandler, dataView, mapViewDef, id);
        renderer.exec();
        return renderer.toString();
    }

    @Override
    public void doDeselectAll(String dataViewUuid, String mapViewDefUuid) {
        MapServiceUtil.deselectAll(dataViewUuid, mapViewDefUuid);
    }

    @Override
    public void doSelectAll(String dataViewUuid, String mapViewDefUuid) {
        MapServiceUtil.selectAll(dataViewUuid, mapViewDefUuid);
    }

    @Override
    public void forceLoad(String dataViewUuid, String mapViewDefUuid) {
        MapCacheUtil.invalidate(mapViewDefUuid);
        MapServiceUtil.initializeMapViewState(dataViewUuid, mapViewDefUuid);
        Integer mapSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if (mapSequenceNumber == null) {
            mapSequenceNumber = 1;
        }
        MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, mapSequenceNumber);
        loadMapCache(dataViewUuid, mapViewDefUuid, mapSequenceNumber);
    }

    @Override
    public MapConfigDTO getMapConfig() {
        if (mapConfigDTO == null) {
            mapConfigDTO = new MapConfigDTO();
            MapConfig mapConfig = Configuration.getInstance().getMapConfig();
            setBoundaryLayerIds(mapConfig);
            mapConfigDTO.setPointLimit(mapConfig.getPointLimit());
            mapConfigDTO.setLinkLimit(mapConfig.getLinkLimit());
            mapConfigDTO.setTypeLimit(mapConfig.getTypeLimit());
            mapConfigDTO.setDefaultBasemapOwner(mapConfig.getDefaultBasemapOwner());
            mapConfigDTO.setDefaultBasemapId(mapConfig.getDefaultBasemapId());
            mapConfigDTO.setFrontendToggleThreshold(mapConfig.getFrontendToggleThreshold());
            mapConfigDTO.setFrontendZoomThreshold(mapConfig.getFrontendZoomThreshold());
            mapConfigDTO.setMinPlaceSize(mapConfig.getMinPlaceSize());
            mapConfigDTO.setMaxPlaceSize(mapConfig.getMaxPlaceSize());
            mapConfigDTO.setMinTrackWidth(mapConfig.getMinTrackWidth());
            mapConfigDTO.setMaxTrackWidth(mapConfig.getMaxTrackWidth());
            mapConfigDTO.setDefaultThemeName(mapConfig.getDefaultThemeName());
            mapConfigDTO.setDetailLevel(mapConfig.getDetailLevel());
            mapConfigDTO.setLocatorUrl(mapConfig.getLocatorUrl());
        }
        return mapConfigDTO;
    }

    private void setBoundaryLayerIds(MapConfig mapConfig) {
        Map<String, String> boundaryLayers = mapConfig.getBoundaryLayers();
        List<String> boundaryLayerIds = new ArrayList<String>();
        boundaryLayerIds.addAll(boundaryLayers.keySet());
        mapConfigDTO.setBoundaryLayerIds(boundaryLayerIds);
    }

    @Override
    public void togglePlaceSelectionByType(String dataViewUuid, String mapViewDefUuid, Integer placeId, String typename, String selectionOperation) {
        AbstractMapSelection mapSelection = toggleSelectionPrep(dataViewUuid, mapViewDefUuid, selectionOperation);

        PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
        if (MapServiceUtil.isUseTrack(dataViewUuid, mapViewDefUuid)) {
            if (!MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid)) {
                TrackidTracknameDuple trackKey = new TrackidTracknameDuple(placeId, typename);
                handleNodeToggleOperationsForTrackmapSummary(mapViewDefUuid, selectionOperation, mapSelection, trackKey);
            } else {
                handleNodeToggleOperationsForTrackmapDetail(mapViewDefUuid, selectionOperation, mapSelection, key);
            }
        } else {
            handleNodeToggleOperationsForMapSummary(dataViewUuid, mapViewDefUuid, selectionOperation, key);
        }

        mapSelection.unlock();
    }

    private AbstractMapSelection toggleSelectionPrep(String dataViewUuid, String mapViewDefUuid, String selectionOperation) {
        AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid);
        mapSelection.lock();
        while (mapSelection.hasReaders()) {
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
      }

        if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_CLEAR)) {
         mapSelection.clearSelection();
      }
        return mapSelection;
    }

    private void handleNodeToggleOperationsForTrackmapSummary(String mapViewDefUuid, String selectionOperation, AbstractMapSelection mapSelection, TrackidTracknameDuple key) {
        TrackMapSummaryGrid trackMapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid(mapViewDefUuid);
        if (trackMapSummaryGrid != null) {
            Set<LinkGeometry> linkGeometries = trackMapSummaryGrid.getLinkGeometriesOfTrack(key);
            toggleTrackSelectionByType(selectionOperation, mapSelection, linkGeometries);
        }
    }

    private void handleNodeToggleOperationsForTrackmapDetail(String mapViewDefUuid, String selectionOperation, AbstractMapSelection mapSelection, PlaceidTypenameDuple key) {
        TrackMapSummaryGrid trackMapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid(mapViewDefUuid);
        if (trackMapSummaryGrid != null) {
            Set<LinkGeometry> linkGeometries = trackMapSummaryGrid.getLinkGeometriesOfType(key);
            toggleTrackSelectionByType(selectionOperation, mapSelection, linkGeometries);
        }
    }

    @Override
    public void toggleTrackSelectionByType(String dataViewUuid, String mapViewDefUuid, Integer trackId, String typename, String selectionOperation) {
        AbstractMapSelection mapSelection = toggleSelectionPrep(dataViewUuid, mapViewDefUuid, selectionOperation);

        TrackidTracknameDuple key = new TrackidTracknameDuple(trackId, typename);
        if (MapServiceUtil.isUseTrack(dataViewUuid, mapViewDefUuid)) {
            TrackMapSummaryGrid trackmapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid(mapViewDefUuid);
            if (trackmapSummaryGrid != null) {
                Set<LinkGeometry> linkGeometries = trackmapSummaryGrid.getLinkGeometriesOfTrack(key);
                toggleTrackSelectionByType(selectionOperation, mapSelection, linkGeometries);
            }
        }

        mapSelection.unlock();
    }

    private void toggleTrackSelectionByType(String selectionOperation, AbstractMapSelection mapSelection, Set<LinkGeometry> linkGeometries) {
        if (linkGeometries != null) {
            for (LinkGeometry linkGeometry : linkGeometries) {
                if (linkGeometry != null) {
                    if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                        mapSelection.removeLink(linkGeometry);
                    } else {
                        mapSelection.addLink(linkGeometry);
                    }
                }
            }
        }
    }

    private void handleNodeToggleOperationsForMapSummary(String dataViewUuid, String mapViewDefUuid, String selectionOperation, PlaceidTypenameDuple key) {
        AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid);
        Set<Geometry> geometries = MapCacheUtil.getGeometriesOfType(mapViewDefUuid, key);
        if (geometries != null) {
         for (Geometry geometry : geometries) {
            if (geometry != null) {
               if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                  MapServiceUtil.selectionRemoveNode(mapSelection, geometry);
               } else {
                  MapServiceUtil.selectionAddNode(mapSelection, geometry);
               }
            }
         }
      }
    }

    @Override
    public Boolean toggleCombinedPlaceSelection(String dataViewUuid, String mapViewDefUuid, String selectionOperation) {
        boolean retVal = false;
        AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid);
        mapSelection.lock();
        while (mapSelection.hasReaders()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        MapSummaryGrid mapSummaryGrid = MapCacheUtil.getMapSummaryGrid(mapViewDefUuid);
        if (MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid) && (mapSummaryGrid != null)) {
            if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_CLEAR)) {
                mapSelection.clearSelection();
            }
            Set<Geometry> geometries = mapSummaryGrid.getCombinedTypeGeometries();
            MapServiceUtil.handleNodeToggleOperations(dataViewUuid, mapViewDefUuid, selectionOperation, geometries);
            retVal = true;
        }

        mapSelection.unlock();
        return retVal;
    }

    @Override
    public void toggleAssociationSelectionByType(String dataViewUuid, String mapViewDefUuid, String associationKey, String selectionOperation) {
        AbstractMapSelection mapSelection = toggleSelectionPrep(dataViewUuid, mapViewDefUuid, selectionOperation);
        Set<LinkGeometryPlus> links = MapCacheUtil.getLinks(mapViewDefUuid);
        if (links != null) {
            int associationId = getAssociationId(mapViewDefUuid, associationKey);
            if (associationId != -1) {
                for (LinkGeometryPlus link : links) {
                    if (link.getLinkType() == associationId) {
                        if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                           mapSelection.removeLink(link);
                        } else {
                           mapSelection.addLink(link);
                        }
                    }
                }
            }
        } else {
            Collection<MapLink> mapLinks = MapCacheUtil.getMapLinksByType(mapViewDefUuid, associationKey);
            if (mapLinks != null) {
               MapServiceUtil.handleLinkToggleOperations(dataViewUuid, mapViewDefUuid, selectionOperation, mapLinks);
            }
        }

        mapSelection.unlock();
    }

    private int getAssociationId(String mapViewDefUuid, String associationName) {
        List<AssociationSettingsDTO> associationSettings = MapCacheUtil.getMapSettings(mapViewDefUuid).getAssociationSettings();

        for (int index = 0; index < associationSettings.size(); index++) {
            AssociationSettingsDTO associationSettingsDTO = associationSettings.get(index);
            if (associationSettingsDTO.getName().equals(associationName)) {
               return index;
            }
        }

        return -1;
    }

    @Override
    public Boolean toggleNewPlaceSelection(String dataViewUuid, String mapViewDefUuid, String selectionOperation) {
        boolean retVal = false;
        AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid);
        mapSelection.lock();
        while (mapSelection.hasReaders()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        MapSummaryGrid mapSummaryGrid = MapCacheUtil.getMapSummaryGrid(mapViewDefUuid);
        if (MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid) && (mapSummaryGrid != null)) {
            if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_CLEAR)) {
               mapSelection.clearSelection();
            }
            Set<Geometry> geometries = mapSummaryGrid.getNewTypeGeometries();
            MapServiceUtil.handleNodeToggleOperations(dataViewUuid, mapViewDefUuid, selectionOperation, geometries);
            retVal = true;
        }

        mapSelection.unlock();
        return retVal;
    }

    @Override
    public Boolean toggleUpdatedPlaceSelection(String dataViewUuid, String mapViewDefUuid, String selectionOperation) {
        boolean retVal = false;
        AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid);
        mapSelection.lock();
        while (mapSelection.hasReaders()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        MapSummaryGrid mapSummaryGrid = MapCacheUtil.getMapSummaryGrid(mapViewDefUuid);
        if (MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid) && (mapSummaryGrid != null)) {
            if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_CLEAR)) {
               mapSelection.clearSelection();
            }
            Set<Geometry> geometries = mapSummaryGrid.getUpdatedTypeGeometries();
            MapServiceUtil.handleNodeToggleOperations(dataViewUuid, mapViewDefUuid, selectionOperation, geometries);
            retVal = true;
        }

        mapSelection.unlock();
        return retVal;
    }

    @Override
    public Selection getSelection(String dataViewUuid, String mapViewDefUuid) {
        Selection selection = MapServiceUtil.getSelection(dataViewUuid, mapViewDefUuid);
        if (selection instanceof TrackmapSelection) {
            TrackmapSelection trackmapSelection = (TrackmapSelection) selection;
            DetailMapSelection detailMapSelection = new DetailMapSelection(mapViewDefUuid);
            detailMapSelection.addLinks(trackmapSelection.getLinks());
            return detailMapSelection;
        } else if (selection instanceof SummaryMapSelection) {
            SummaryMapSelection mapSummarySelection = (SummaryMapSelection) selection;
            DetailMapSelection detailMapSelection = new DetailMapSelection(mapViewDefUuid);
            MapServiceUtil.selectionAddNodes(mapSummarySelection.getNodes(), detailMapSelection);
            detailMapSelection.addLinks(mapSummarySelection.getLinks());
            return detailMapSelection;
        }
        return selection;
    }

    @Override
    public Integer getNumPoints(String dataViewUuid, String mapViewDefUuid, Long id) {
        NumPointCounter counter = new NumPointCounter(dataViewUuid, mapViewDefUuid, id);
        counter.calculate();
        return counter.getNumPoints();
    }

    @Operation
    public List<List<ResourceBasics>> getBasemapOverWriteControlLists() throws CentrifugeException {
        return ModelHelper.generateOverWriteControlLists(
                AclRequest.listAuthorizedUserBasemaps(new AclControlType[]{AclControlType.DELETE}),
                AclRequest.listUserBasemaps(),
                AclRequest.listAuthorizedUserBasemaps(new AclControlType[]{AclControlType.DELETE}));
    }

    @Override
    public void deleteBasemap(String uuid) {
        Basemap basemap = null;
        if (uuid != null) {
         basemap = CsiPersistenceManager.findObject(Basemap.class, uuid);
      }
        if (basemap != null) {
         CsiPersistenceManager.deleteObject(basemap);
      }
    }

    @Override
    public void saveBasemap(Basemap basemap) {
        Basemap existingBaseMap = CsiPersistenceManager.findObject(Basemap.class, basemap.getUuid());
        if (existingBaseMap != null) {
         CsiPersistenceManager.merge(basemap);
      } else {
         CsiPersistenceManager.persist(basemap);
      }
    }

    @Override
    public List<ResourceBasics> listBasemapResources() {
        List<ResourceBasics> resourceBasics;
        try {
            resourceBasics = listAuthorizedUserBasemaps(AclControlType.READ);
        } catch (CentrifugeException e) {
            resourceBasics = new ArrayList<ResourceBasics>();
        }
        return resourceBasics;
    }

    @Override
    public List<MapLayerDTO> listBasemaps() {
        List<MapLayerDTO> baseMapDTOs = new ArrayList<MapLayerDTO>();
        try {
            List<ResourceBasics> baseMapResources = listAuthorizedUserBasemaps(AclControlType.READ);
            for (ResourceBasics baseMapResource : baseMapResources) {
                Basemap basemap = findBasemap(baseMapResource.getUuid());
                if (basemap != null) {
                    MapLayerDTO baseMapDTO = new MapLayerDTO();
                    baseMapDTO.setBasemap(basemap);
                    ValuePair<String, Boolean> result = modelActionService.isAuthorized(basemap.getUuid(), AclControlType.EDIT);
                    boolean canEdit = result.getValue2();
                    baseMapDTO.setCanEdit(canEdit);
                    baseMapDTOs.add(baseMapDTO);
                }
            }
        } catch (CentrifugeException ignored) {
        }
        return baseMapDTOs;
    }

    @Override
    public Basemap findBasemap(String uuid) {
        if ((uuid == null) || uuid.isEmpty()) {
         return null;
      }

        if (checkAuthorization(uuid)) {
         return CsiPersistenceManager.findObject(Basemap.class, uuid);
      }

        return null;
    }

    @Override
    public void deleteBasemaps(List<String> myItemList) {
        for (String uuid : myItemList) {
         deleteBasemap(uuid);
      }
    }

    @Override
    public String buildSummaryCache(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, UBox uBox) {
        while (MapCacheUtil.isHomeLoading(mapViewDefUuid)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                break;
            }
        }
        MapCacheUtil.setMapCacheLocation(mapViewDefUuid, sequenceNumber);
        MapCacheUtil.setUseHome(mapViewDefUuid, false);
        try {
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
            MapViewDef mapViewDef = (MapViewDef) dataView.getMeta().getModelDef().findVisualizationByUuid(mapViewDefUuid);
            MapTheme mapTheme = getMapTheme(mapViewDef);
            SummaryCacheBuilder builder;
            if (mapCacheHandler.isUseTrackMap()) {
                builder = new TrackSummaryCacheBuilder(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
            } else {
                builder = new PointSummaryCacheBuilder(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
            }
            builder.build();
            return builder.getStatus();
        } catch (MapCacheNotAvailable e) {
            return "MapCacheNotAvailable";
        } catch (MapCacheStaleException e) {
            return "MapCacheStale";
        }
    }

    @Override
    public void drillOnBundle(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, String requestString) {
        try {
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            mapCacheHandler.addBreadcrumbFromRequestString(requestString);
            mapCacheHandler.invalidateExtentIfMapNotPinned();
            mapCacheHandler.invalidateInitialExtent();
            mapCacheHandler.refreshMapState();
        } catch (MapCacheStaleException ignored) {
        }
    }

    @Override
    public void trimBreadcrumb(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, String drillFieldName) {
        try {
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            BreadcrumbTrimmer trimmer = new BreadcrumbTrimmer(mapCacheHandler, drillFieldName);
            trimmer.trim();
            mapCacheHandler.refreshMapState();
        } catch (MapCacheStaleException ignored) {
        }
    }

    @Override
    public void showLeavesOnBundle(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, String requestString) {
        try {
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            mapCacheHandler.addBreadcrumbFromRequestString(requestString);
            mapCacheHandler.invalidateExtentIfMapNotPinned();
            mapCacheHandler.invalidateInitialExtent();
            mapCacheHandler.setShowLeaves(true);
            mapCacheHandler.refreshMapState();
        } catch (MapCacheStaleException ignored) {
        }
    }

    @Override
    public void dontShowLeavesOnBundle(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber) {
        try {
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            List<Crumb> breadcrumb = mapCacheHandler.getBreadcrumb();
            Crumb crumb = breadcrumb.get(breadcrumb.size() - 1);
            mapCacheHandler.setExtentIfMapNotPinned(crumb.getPreviousExtent());
            mapCacheHandler.setInitialExtent(crumb.getPreviousInitialExtent());
            breadcrumb.remove(crumb);
            mapCacheHandler.setShowLeaves(false);
            mapCacheHandler.refreshMapState();
        } catch (MapCacheStaleException ignored) {
        }

    }

    @Operation
    @Interruptable
    public MapLegendInfo legendData(@QueryParam(value = DV_UUID_PARAM) String dataViewUuid, @QueryParam(value = VIZ_UUID_PARAM) String mapViewDefUuid, Integer sequenceNumber) {
        MapLegendInfo mapLegendInfo;
        try {
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            MapLegendBuilder builder = new MapLegendBuilder(mapCacheHandler);
            builder.build();
            mapLegendInfo = builder.getMapLegendInfo();
        } catch (MapCacheStaleException e) {
            mapLegendInfo = new MapLegendInfo();
        }
        return mapLegendInfo;
    }

    public void updateLegend(String dataViewUuid, String mapViewDefUuid, List<PlaceidTypenameDuple> placeIdTypenameDuples, List<TrackidTracknameDuple> trackIdTrackNameDuples, List<String> associationNames) {
        if (MapServiceUtil.isUseTrack(dataViewUuid, mapViewDefUuid)) {
            if (trackIdTrackNameDuples.isEmpty()) {
                placeIdTypenameDuples.forEach(element -> trackIdTrackNameDuples.add(new TrackidTracknameDuple(element.getPlaceid(), element.getTypename())));
                MapServiceUtil.updateTrackTypeOrder(mapViewDefUuid, trackIdTrackNameDuples);
            } else {
                MapServiceUtil.updatePlaceTypeOrder(mapViewDefUuid, placeIdTypenameDuples);
                MapServiceUtil.updateTrackTypeOrder(mapViewDefUuid, trackIdTrackNameDuples);
            }
        } else {
            MapServiceUtil.updatePlaceTypeOrder(mapViewDefUuid, placeIdTypenameDuples);
            MapServiceUtil.updateAssociationTypeOrder(mapViewDefUuid, associationNames);
        }
    }

    public boolean isLegendEnabled(String mapViewDefUuid) {
        return MapCacheUtil.isLegendEnabled(mapViewDefUuid);
    }

    @Override
    public void setLegendShown(String dataViewUuid, String mapViewDefUuid, boolean value) {
        MapServiceUtil.setLegendShown(dataViewUuid, mapViewDefUuid, value);
    }

    @Override
    public boolean isLegendShown(String dataViewUuid, String mapViewDefUuid) {
        return MapServiceUtil.isLegendShown(dataViewUuid, mapViewDefUuid);
    }

    // i'm doing this until i'm done.

    /// problems: 1) random refreshes too often.

    @Override
    public boolean isMultitypeDecoratorEnabled(String mapViewDefUuid) {
        return MapCacheUtil.isMultiTypeDecoratorEnabled(mapViewDefUuid);
    }

    @Override
    public void setMultitypeDecoratorShown(String dataViewUuid, String mapViewDefUuid, boolean value) {
        MapServiceUtil.setMultitypeDecoratorShown(dataViewUuid, mapViewDefUuid, value);
    }

    @Override
    public boolean isMultitypeDecoratorShown(String dataViewUuid, String mapViewDefUuid) {
        return MapServiceUtil.isMultitypeDecoratorShown(dataViewUuid, mapViewDefUuid);
    }

    @Override
    public MapToolsInfo getMapToolsInfo(String dataViewUuid, String mapViewDefUuid) {
        MapToolsInfo mapToolsInfo = new MapToolsInfo();
        mapToolsInfo.setLegendEnabled(MapCacheUtil.isLegendEnabled(mapViewDefUuid));
        mapToolsInfo.setLegendShown(MapServiceUtil.isLegendShown(dataViewUuid, mapViewDefUuid));
        mapToolsInfo.setMultitypeDecoratorEnabled(MapCacheUtil.isMultiTypeDecoratorEnabled(mapViewDefUuid));
        mapToolsInfo.setMultitypeDecoratorShown(MapServiceUtil.isMultitypeDecoratorShown(dataViewUuid, mapViewDefUuid));
        mapToolsInfo.setLinkupDecoratorEnabled(isLinkupDecoratorEnabled(dataViewUuid));
        return mapToolsInfo;
    }

    private boolean isLinkupDecoratorEnabled(String dataViewUuid) {
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
        return dataView.getNextLinkupId() > 1;
    }

    @Override
    public MetricsDTO getViewMetrics(String mapViewDefUuid, String dvUuid) {
        Extent extent = getExtent(dvUuid, mapViewDefUuid);
        if (extent == null) {
            extent = MapCacheUtil.getInitialExtent(mapViewDefUuid);
        }
        return getMetricsInExtent(extent, dvUuid, mapViewDefUuid, false);
    }

    /**
     * @param extent - current map extent, or full extent.
     */
    private MetricsDTO getMetricsInExtent(Extent extent, String dvUuid, String mapViewDefUuid, boolean forceGlobal) {
        // lets check our extent
        MetricsDTO extentMetrics = new MetricsDTO();
        List<Extent> wrapAroundExtents = new ArrayList<>();
        if (extent == null) {
            return extentMetrics;
        }
        if (extent.getXmin() < -180) {
            double xMin = extent.getXmin() + 360;
            wrapAroundExtents.add(new Extent(-180, extent.getXmax(), extent.getYmin(), extent.getYmax()));
            wrapAroundExtents.add(new Extent(xMin, 180, extent.getYmin(), extent.getYmax()));
        } else if (extent.getXmax() > 180) {
            double xMax = extent.getXmax() - 360;
            wrapAroundExtents.add(new Extent(-180, xMax, extent.getYmin(), extent.getYmax()));
            wrapAroundExtents.add(new Extent(extent.getXmin(), 180, extent.getYmin(), extent.getYmax()));
        } else {
            wrapAroundExtents.add(extent);
        }

        // types are not in the same place if we are in summary/detail.
        boolean inSummary = !MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid);

        TreeMap<String, Integer> typeNameCount = new TreeMap<>();
        int pointCount = 0;

        // special case for detail level in a summary map, otherwise we will breaaaak;
        if (inSummary || forceGlobal) {
            MapSummaryGrid grid = MapCacheUtil.getMapSummaryGrid(mapViewDefUuid);
            if (grid == null) {
                // logger.error("Cache miss");
                return extentMetrics;
            }
            Set<PlaceidTypenameDuple> types = grid.getTypes();

            for (PlaceidTypenameDuple t : types) {
                Set<Geometry> geometriesOfType = grid.getGeometriesOfType(t);
                Set<Geometry> filtered = new HashSet<>();
                geometriesOfType.forEach(geometry -> {
                    double x = geometry.getX();
                    double y = geometry.getY();
                    for (Extent ex : wrapAroundExtents) {
                        if ((x >= ex.getXmin()) && (x <= ex.getXmax()) && (y >= ex.getYmin()) && (y <= ex.getYmax())) {
                            filtered.add(geometry);
                        }
                    }
                });

                // increment points
                pointCount += filtered.size();

                // String typename = t.getTrackname() + "(" + t.getPlaceid() +
                // ")";
                String typename = t.getTypename();
                if (typeNameCount.containsKey(typename)) {
                    typeNameCount.put(typename, typeNameCount.get(typename) + filtered.size());
                } else {
                    typeNameCount.put(typename, filtered.size());
                }
            }

        } else {
            MapNodeInfo mapNodeInfo = MapCacheUtil.getMapNodeInfo(mapViewDefUuid);

            if (mapNodeInfo == null) {
                // logger.error("Cache miss");
                return extentMetrics;
                // return extentMetrics;
            }

            // if heatmap
            if (MapCacheUtil.getMapSettings(mapViewDefUuid).isUseHeatMap()) {
                Map<Geometry, AugmentedMapNode> mapByGeometry = mapNodeInfo.getMapByGeometry();
                Set<AugmentedMapNode> filtered = new HashSet<>();
                mapByGeometry.values().forEach(augmentedMapNode -> {
                    Geometry geometry = augmentedMapNode.getGeometry();
                    double x = geometry.getX();
                    double y = geometry.getY();
                    for (Extent ex : wrapAroundExtents) {
                        if ((x >= ex.getXmin()) && (x <= ex.getXmax()) && (y >= ex.getYmin()) && (y <= ex.getYmax())) {
                            filtered.add(augmentedMapNode);
                        }
                    }
                });
                // increment points
                pointCount += filtered.size();
            } else if (MapServiceUtil.isBundleUsed(dvUuid, mapViewDefUuid)) {
                // need breadcrumbs to figure out iif we are on the lowest lvl
                List<Crumb> breadcrumb = MapServiceUtil.getBreadcrumb(dvUuid, mapViewDefUuid);
                // this is most detailed level
                if ((breadcrumb != null)
                        && (breadcrumb.size() == MapServiceUtil.getNumberOfBundleDefinitions(dvUuid, mapViewDefUuid))) {
                    // load normal

                    pointCount = getPointCount(wrapAroundExtents, typeNameCount, pointCount, mapNodeInfo);
                } else {

                    Set<AugmentedMapNode> filtered = new HashSet<>();
                    mapNodeInfo.getMapByGeometry().values().forEach(augmentedMapNode -> {
                        Geometry geometry = augmentedMapNode.getGeometry();
                        double x = geometry.getX();
                        double y = geometry.getY();
                        for (Extent ex : wrapAroundExtents) {
                            if ((x >= ex.getXmin()) && (x <= ex.getXmax()) && (y >= ex.getYmin()) && (y <= ex.getYmax())) {
                                filtered.add(augmentedMapNode);
                            }
                        }
                    });

                    // we need to extract the types and find their counts from somewehre else
                    for (AugmentedMapNode nd : filtered) {
                        if (nd instanceof BundleMapNode) {
                            BundleMapNode node = (BundleMapNode) nd;
                            pointCount++;
                            int val;
                            if (MapServiceUtil.isCountChildren(dvUuid, mapViewDefUuid)) {
                                val = node.getChildrenCount();
                            } else {
                                val = (int) node.getHits();
                            }

                            if (typeNameCount.containsKey(node.getLabel())) {
                                typeNameCount.put(node.getLabel(), typeNameCount.get(node.getLabel()) + val);
                            } else {
                                typeNameCount.put(node.getLabel(), val);
                            }

                        }
                    }

                }
            } else {
                pointCount = getPointCount(wrapAroundExtents, typeNameCount, pointCount, mapNodeInfo);
            }

        }

        extentMetrics.add(MapMetricsType.PLACES.getLabel(), pointCount);

        if (typeNameCount.size() > 0) {
            typeNameCount.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> extentMetrics.add(e.getKey(), e.getValue()));
        }
        return extentMetrics;
    }

    private int getPointCount(List<Extent> wrapAroundExtents, TreeMap<String, Integer> typeNameCount, int pointCount, MapNodeInfo mapNodeInfo) {
        Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapByType = mapNodeInfo.getMapByType();
        for (Map.Entry<PlaceidTypenameDuple, Set<AugmentedMapNode>> entry : mapByType.entrySet()) {
            PlaceidTypenameDuple key = entry.getKey();
            Set<AugmentedMapNode> augmentedMapNodes = entry.getValue();
            Set<AugmentedMapNode> filtered = new HashSet<>();

            augmentedMapNodes.forEach(augmentedMapNode -> {
                Geometry geometry = augmentedMapNode.getGeometry();
                double x = geometry.getX();
                double y = geometry.getY();
                for (Extent ex : wrapAroundExtents) {
                    if ((x >= ex.getXmin()) && (x <= ex.getXmax()) && (y >= ex.getYmin()) && (y <= ex.getYmax())) {
                        filtered.add(augmentedMapNode);
                    }
                }
            });

            // increment points
            pointCount += filtered.size();

            String typename = key.getTypename();
            if (typeNameCount.containsKey(typename)) {
                typeNameCount.put(typename, typeNameCount.get(typename) + filtered.size());
            } else {
                typeNameCount.put(typename, filtered.size());
            }

        }
        return pointCount;
    }

    @Override
    public MetricsDTO getMapTotalMetrics(String mapViewDefUuid, String dvUuid) {
        // this is max extent possible.
        Extent extent = new Extent(-180, 180, -90, 90);

        return getMetricsInExtent(extent, dvUuid, mapViewDefUuid, true);
    }

    @Override
    public String getNodeAsImageNew(String iconId, boolean isMap, ShapeType shape, int color, int size, double iconScale, int strokeSize, boolean useSummary, String mapViewDefUuid) throws CentrifugeException {
        if (isMap && useSummary && ((shape == ShapeType.NONE) || (shape == null))) {
            MapTheme mapTheme = getMapTheme(CsiPersistenceManager.findObject(MapViewDef.class, mapViewDefUuid));
            if (mapTheme != null) {
                shape = mapTheme.getDefaultShape();
                // Summary defaults to circle when there is no shape, so we have
                // to default to circle here
                if ((shape == ShapeType.NONE) || (shape == null)) {
                    shape = ShapeType.CIRCLE;
                }
            } else {
                shape = ShapeType.CIRCLE;
            }
        }
        String value = graphActionsService.getNodeAsImageNew(iconId, isMap, shape, color, 1.0f, false, false, false, size, iconScale, strokeSize, useSummary, false, false);
        if (value.equals(GraphActionsService.BROKEN_IMAGE)) {
         value = GraphActionsService.NO_SHAPE_ICON;
      }
        return value;
    }

    @Override
    public void selectNodes(String dataViewUuid, String mapViewDefUuid, String[] ids) {
        MapServiceUtil.selectNodes(dataViewUuid, mapViewDefUuid, ids);
        MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
    }

    @Override
    public void selectLinks(String dataViewUuid, String mapViewDefUuid, String[] ids) {
        MapServiceUtil.selectLinks(dataViewUuid, mapViewDefUuid, ids);
        MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
    }

    @Override
    public Boolean selectTrackLinks(String dataViewUuid, String mapViewDefUuid, String[] nodes, String[] links) {
        boolean changesMade = MapServiceUtil.selectTrackLinks(dataViewUuid, mapViewDefUuid, nodes, links);
        if (changesMade) {
            MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
        }
        return changesMade;
    }

    @Override
    public void deselectNodes(String dataViewUuid, String mapViewDefUuid, String[] ids) {
        MapServiceUtil.deselectNodes(dataViewUuid, mapViewDefUuid, ids);
        MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
    }

    @Override
    public String toggleSelectedNodes(String dataViewUuid, String mapViewDefUuid, String[] ids) {
        String retVal = MapServiceUtil.toggleNodeSelected(dataViewUuid, mapViewDefUuid, ids);
        if (!retVal.isEmpty()) {
         MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
      }
        return retVal;
    }

    @Override
    public Boolean toggleTrackNodes(String dataViewUuid, String mapViewDefUuid, String[] ids) {
        int numChanged = MapServiceUtil.toggleTrackNodes(dataViewUuid, mapViewDefUuid, ids);
        if (numChanged > 0) {
            MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
            return true;
        }
        return false;
    }

    @Override
    public Boolean toggleSelectedLinks(String dataViewUuid, String mapViewDefUuid, String[] ids) {
        boolean changesMade = MapServiceUtil.toggleLinkSelected(dataViewUuid, mapViewDefUuid, ids);
        if (changesMade) {
            MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
        }
        return changesMade;
    }

    @Override
    public Boolean selectLinks2(String dataViewUuid, String mapViewDefUuid, String[] ids) {
        boolean changesMade = MapServiceUtil.selectLinks2(dataViewUuid, mapViewDefUuid, ids);
        if (changesMade) {
            MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
        }
        return changesMade;
    }

    @Override
    public Boolean deselectLinks2(String dataViewUuid, String mapViewDefUuid, String[] ids) {
        boolean changesMade = MapServiceUtil.deselectLinks2(dataViewUuid, mapViewDefUuid, ids);
        if (changesMade) {
            MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
        }
        return changesMade;
    }

    @Override
    public void selectAll(String dataViewUuid, String mapViewDefUuid) {
        MapServiceUtil.selectAll(dataViewUuid, mapViewDefUuid);
    }

    @Override
    public void deselectAll(String dataViewUuid, String mapViewDefUuid) {
        MapServiceUtil.deselectAll(dataViewUuid, mapViewDefUuid);
    }

    @Override
    public String[] getSelectedNodes(String dataViewUuid, String mapViewDefUuid) {
        List<Long> objectIDList = new ArrayList<Long>();
        AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid);
        Map<Long, AugmentedMapNode> mapNodeMapById = MapCacheUtil.getTheRightSummaryMapNodeMapById(mapViewDefUuid);
        if (mapNodeMapById != null) {
            mapNodeMapById.values().stream().filter(mapNode -> (mapNode != null) && MapServiceUtil.isSelected(mapSelection, mapNode)).forEach(mapNode -> objectIDList.add(mapNode.getNodeId()));
        }
        return ListToArray(objectIDList);
    }

    private String[] ListToArray(List<Long> objectIDList) {
        String[] objectIDArray = null;
        if (objectIDList != null) {
            objectIDArray = new String[objectIDList.size()];
            for (int i = 0; i < objectIDList.size(); i++) {
               objectIDArray[i] = objectIDList.get(i).toString();
            }
        }
        return objectIDArray;
    }

    public void fillCacheWithMapContext(String dataViewUuid, String mapViewDefUuid) {
        MapCacheUtil.addMapSelection(mapViewDefUuid, MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid));
        MapCacheUtil.setMultiTypeDecoratorShown(mapViewDefUuid, MapServiceUtil.isMultitypeDecoratorShown(dataViewUuid, mapViewDefUuid));
        MapCacheUtil.setLinkupDecoratorShown(mapViewDefUuid, MapServiceUtil.isLinkupDecoratorShown(dataViewUuid, mapViewDefUuid));
        MapCacheUtil.setPlaceDynamicTypeInfo(mapViewDefUuid, MapServiceUtil.getPlaceDynamicTypeInfo(mapViewDefUuid));
        MapCacheUtil.setTrackDynamicTypeInfo(mapViewDefUuid, MapServiceUtil.getTrackDynamicTypeInfo(mapViewDefUuid));
    }

    @Override
    public Extent getExtent(String dataViewUuid, String mapViewDefUuid) {
        return MapCacheUtil.getCurrentExtentOrCurrentExtentIfMapNotPinned(mapViewDefUuid);
    }

    @Override
    public Extent getCurrentExtent(String dataViewUuid, String mapViewDefUuid) {
        return MapCacheUtil.getCurrentExtent(mapViewDefUuid);
    }

    @Override
    public void setExtent(String dataViewUuid, String mapViewDefUuid, Extent extent) {
        MapCacheUtil.setCurrentExtent(mapViewDefUuid, extent);
        if (MapCacheUtil.isUseTrack(mapViewDefUuid) || MapCacheUtil.isBundleUsed(mapViewDefUuid)) {
            MapCacheUtil.setCurrentExtentIfMapNotPinned(mapViewDefUuid, extent);
        }
    }

    @Override
    public void setHeatmapBlurValue(String dataViewUuid, String mapViewDefUuid, Double blurValue) {
        MapServiceUtil.setHeatmapBlurValue(dataViewUuid, mapViewDefUuid, blurValue);
    }

    @Override
    public void setHeatmapMaxValue(String dataViewUuid, String mapViewDefUuid, Double maxValue) {
        MapServiceUtil.setHeatmapMaxValue(dataViewUuid, mapViewDefUuid, maxValue);
    }

    @Override
    public void setHeatmapMinValue(String dataViewUuid, String mapViewDefUuid, Double minValue) {
        MapServiceUtil.setHeatmapMinValue(dataViewUuid, mapViewDefUuid, minValue);
    }

    @Override
    public void setHeatmapValues(String dataViewUuid, String mapViewDefUuid, Double blurValue, Double maxValue, Double minValue) {
        MapServiceUtil.setHeatmapBlurValue(dataViewUuid, mapViewDefUuid, blurValue);
        MapServiceUtil.setHeatmapMaxValue(dataViewUuid, mapViewDefUuid, maxValue);
        MapServiceUtil.setHeatmapMinValue(dataViewUuid, mapViewDefUuid, minValue);
    }

    @Override
    public List<Crumb> getBreadcrumb(String dataViewUuid, String mapViewDefUuid) {
        return MapServiceUtil.getBreadcrumb(dataViewUuid, mapViewDefUuid);
    }

    @Override
    public HeatMapInfo getHeatMapInfo(String dataViewUuid, String mapViewDefUuid) {
        return MapServiceUtil.getHeatMapInfo(dataViewUuid, mapViewDefUuid);
    }

    @Override
    public boolean isShowLeaves(String dataViewUuid, String mapViewDefUuid) {
        return MapServiceUtil.isShowLeaves(dataViewUuid, mapViewDefUuid);
    }

    @Override
    public void setLinkupDecoratorShown(String dataViewUuid, String mapViewDefUuid, boolean value) {
        MapServiceUtil.setLinkupDecoratorShown(dataViewUuid, mapViewDefUuid, value);
        MapCacheUtil.setLinkupDecoratorShown(mapViewDefUuid, value);
    }

    @Override
    public void handleLinkup(String dataViewUuid, String mapViewDefUuid) {
        setLinkupDecoratorShown(dataViewUuid, mapViewDefUuid, true);
        MapCacheUtil.invalidateMapNodeInfoAndMapLinkInfo(mapViewDefUuid);
        MapCacheUtil.invalidateInitialExtent(mapViewDefUuid);
        MapCacheUtil.invalidateExtentIfMapNotPinned(mapViewDefUuid);
        if (MapServiceUtil.isUseTrack(dataViewUuid, mapViewDefUuid)) {
            MapCacheUtil.invalidateMapNodeInfoAndMapTrackInfo(mapViewDefUuid);
            if (MapCacheUtil.getTrackGeometryToRowIds(mapViewDefUuid) != null) {
                MapCacheUtil.removeSequenceBarInfo(mapViewDefUuid);
            }
        }
    }

    @Override
    public List<MapLayerInfo> getMapLayerInfos(String mapViewDefUuid) {
        CsiPersistenceManager.begin();
        List<MapLayerInfo> mapLayerInfos = new ArrayList<MapLayerInfo>();
        MapViewDef mapViewDef = CsiPersistenceManager.findObject(MapViewDef.class, mapViewDefUuid);
        MapSettings mapSettings = mapViewDef.getMapSettings();
        for (MapTileLayer mapTileLayer : mapSettings.getTileLayers()) {
            if (mapTileLayer != null) {
                String baseMapId = mapTileLayer.getLayerId();
                if (baseMapId != null) {
                    Basemap basemap = CsiPersistenceManager.findObject(Basemap.class, baseMapId);
                    if (basemap != null) {
                        String url = basemap.getUrl();
                        if (url != null) {
                            MapLayerInfo mapLayerInfo = new MapLayerInfo();
                            mapLayerInfo.setId(basemap.getName());
                            if (url.startsWith("http")) {
                                mapLayerInfo.setKey("");
                                mapLayerInfo.setUrl(url);
                            } else if (GenerateBasemapsInitial.hasUrl(url)) {
                                mapLayerInfo.setKey(url);
                                mapLayerInfo.setUrl(GenerateBasemapsInitial.getUrl(url));
                            }
                            mapLayerInfo.setType(basemap.getType());
                            mapLayerInfo.setLayername(basemap.getLayername());
                            mapLayerInfo.setVisible(mapTileLayer.isVisible());
                            mapLayerInfo.setOpacity(mapTileLayer.getOpacity());
                            mapLayerInfos.add(mapLayerInfo);
                        }
                    }
                }
            }
        }
        CsiPersistenceManager.close();
        return mapLayerInfos;
    }

    @Override
    public PlaceSizeInfo getPlaceSizeInfo(String mapViewDefUuid) {
        CsiPersistenceManager.begin();
        PlaceSizeInfo placeSizeInfo = new PlaceSizeInfo();
        MapViewDef mapViewDef = CsiPersistenceManager.findObject(MapViewDef.class, mapViewDefUuid);
        MapSettings mapSettings = mapViewDef.getMapSettings();
        placeSizeInfo.setMax(mapSettings.getMaxPlaceSize());
        placeSizeInfo.setMin(mapSettings.getMinPlaceSize());
        CsiPersistenceManager.close();
        return placeSizeInfo;
    }

    @Override
    public OverviewResponse getOverview(OverviewRequest overviewRequest) {
        return calculateBinnedOverview(overviewRequest);
    }

    private OverviewResponse calculateBinnedOverview(OverviewRequest overviewRequest) {
        if (overviewRequest == null) {
            return null;
        }
        String dvUuid = overviewRequest.getDvUuid();
        if (dvUuid == null) {
            return null;
        }
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        if (dataView == null) {
            return null;
        }
        String vizUuid = overviewRequest.getVizUuid();
        if (vizUuid == null) {
            return null;
        }
        VisualizationDef vis = dataView.getMeta().getModelDef().findVisualizationByUuid(vizUuid);
        if (!(vis instanceof MapViewDef)) {
            return null;
        }
        MapViewDef mapVis = (MapViewDef) vis;
        MapSettingsDTO mapSettingsDTO = MapCacheUtil.getMapSettings(vizUuid);
        if (mapSettingsDTO == null) {
            MapTheme mapTheme = getMapTheme(mapVis);
            mapSettingsDTO = MapServiceUtil.createAndSaveMapSettingsDTO(vizUuid, mapTheme, dataView, mapVis);
        }
        if (mapSettingsDTO == null) {
            return null;
        }
        if (MapCacheUtil.isPlaceLimitOrTrackTypeLimitReached(vizUuid)) {
            String msg = "";
            if (MapCacheUtil.isPlaceTypeLimitReached(vizUuid)) {
                msg = "PlaceTypeLimitReached";
            } else if (MapCacheUtil.isTrackTypeLimitReached(vizUuid)) {
                msg = "TrackTypeLimitReached";
            }
            TaskHelper.reportError(msg, new Throwable());
        }

        //NOTE: Now have a map that we want to make overview for.

        TrackSettingsDTO trackSettingsDTO = mapSettingsDTO.getTrackSettings().get(0);
        String sequenceColumn = "\"" + trackSettingsDTO.getSequenceColumn() + "\"";
        String selectClause = "SELECT COUNT(*), \"" + trackSettingsDTO.getSequenceColumn() + "\" ";
        String fromClause = "FROM " + CacheUtil.getQuotedCacheTableName(dvUuid);

        /*{
            DataView dataView = (DataView) dvUuid;
            MapViewDef mapViewDef = (MapViewDef) vizUuid;
            String mapViewDefUuid = mapViewDef.getUuid();
            String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapViewDefUuid, mapSettingsDTO);
            String latLongFilterString = getLatLongFilter(placeSettingsDTO);
            if (filterString == null || filterString.trim().length() == 0)
                return "WHERE " + latLongFilterString;
            else
                return "WHERE " + filterString + " AND " + latLongFilterString;

        }*/
        String query;
        {
            String whereClause = "WHERE " + sequenceColumn + " IS NOT NULL";
            if (trackSettingsDTO.getIdentityColumn() != null) {
                String identityColumn = "\"" + trackSettingsDTO.getIdentityColumn() + "\"";
                whereClause += " AND " + identityColumn + " IS NOT NULL";
            }
            String groupByClause = "GROUP BY " + sequenceColumn;
            String orderByClause = "ORDER BY " + sequenceColumn;
            if (trackSettingsDTO.getSequenceSortOrder().equals(SortOrder.DESC.toString())) {
                orderByClause += " DESC";
            }

            whereClause += " AND " + getWhereClauseForPlace(dataView, mapVis, mapSettingsDTO);
            query = selectClause + " " + fromClause + " " + whereClause + " " + groupByClause + " " + orderByClause;
        }

        List<Integer> seriesData = new ArrayList<Integer>();
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = new ArrayList<TrackMapSummaryGrid.SequenceSortValue>();

        try (Connection conn = CsiPersistenceManager.getCacheConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = QueryHelper.executeStatement(stmt, query)) {
           while (rs.next()) {
              int count = rs.getInt(1);
              Comparable value = (Comparable) rs.getObject(2);

              seriesData.add(count);

              if (count > maxValue) {
                 maxValue = count;
              }
              if (count < minValue) {
                 minValue = count;
              }
              seriesValues.add(new TrackMapSummaryGrid.SequenceSortValue(value, 0));
           }
           MapCacheUtil.setSeriesValues(vizUuid, seriesValues);
        } catch (CentrifugeException | SQLException ce) {
           if (LOG != null) {
              LOG.error(ce);
              LOG.error("query: " + query);
           }
        } finally {
           CsiPersistenceManager.releaseCacheConnection();
        }
        OverviewResponse overviewResponse = new OverviewResponse();

        if (seriesData.isEmpty()) {
            return null;
        }
        int currentWidth = overviewRequest.getCurrentWidth();

        int binSize = ((seriesData.size() - 1) / (currentWidth < 1 ? DEFAULT_WIDTH : currentWidth)) + 1;

        overviewResponse.setOverviewBinSize(binSize);
        int binMin = Integer.MAX_VALUE;
        int binMax = Integer.MIN_VALUE;
        List<Integer> binValues = new ArrayList<Integer>();
        try {
            if (minValue == maxValue) {//Shortcut if all sequence values have same counts
                for (int i = 0; i < seriesData.size(); i += binSize) {
                    overviewResponse.getOverviewColors().add(DEFAULT_WEIGHT);
                }
            } else {
                for (int i = 0; i < seriesData.size(); i += binSize) {
                    int binValue = 0;
                    for (int j = 0; j < binSize; j++) {
                        if ((i + j) >= seriesData.size()) {
                            break;
                        }
                        Integer value = seriesData.get(i + j);
                        if (value > binValue) {
                            binValue += value;
                        }
                    }
                    binValues.add(binValue);
                    if (binValue < binMin) {
                        binMin = binValue;
                    }
                    if (binValue > binMax) {
                        binMax = binValue;
                    }
                }
            }
            {
                for (Integer binValue : binValues) {

                    double colorBinSize = (binMax - binMin) / (double) colors.size();
                    int index = 0;
                    while ((binValue - minValue) > ((index + 1) * colorBinSize)) {
                        index++;
                    }
                    while (index >= colors.size()) {
                        index--;
                    }
                    overviewResponse.getOverviewColors().add(index);
                }
            }
            overviewResponse.setTotalCategories(seriesData.size());
        } catch (Exception error) {
            if (LOG != null) {
                LOG.error(error);
            }
            return null;
        }
        return overviewResponse;
    }

    private String getWhereClauseForPlace(DataView dataView, MapViewDef mapViewDef, MapSettingsDTO mapSettingsDTO) {
        int trackId = 0;
        TrackSettingsDTO trackSettings = mapSettingsDTO.getTrackSettings().get(trackId);
        if (trackSettings == null) {
            return "";
        }
        PlaceSettingsDTO trackPlaceSettings = null;

        String trackPlaceName = trackSettings.getPlace();
        int placeId = 0;

        for (PlaceSettingsDTO placeSetting : mapSettingsDTO.getPlaceSettings()) {
            String placeName = placeSetting.getName();
            if (placeName != null) {
                if (placeName.equals(trackPlaceName)) {
                    trackPlaceSettings = placeSetting;
                    break;
                }
            }
        }
        if (trackPlaceSettings == null) {
            return "";
        }
        while (placeId < mapSettingsDTO.getPlaceSettings().size()) {

            placeId++;
        }
        String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettingsDTO);
        String latLongFilterString = FilterStringGenerator.getLatLongFilter(trackPlaceSettings);
        if ((filterString == null) || (filterString.trim().length() == 0)) {
         return latLongFilterString;
      } else {
         return filterString + " AND " + latLongFilterString;
      }
    }

    @Override
    public boolean updateRange(String dataViewUuid, String mapViewDefUuid, int start, int end) {
        boolean refreshMap = false;
        Integer oldStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
        Integer oldEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);
        if ((oldStart == null) || (oldEnd == null)) {
            refreshMap = true;
            MapCacheUtil.setRange(mapViewDefUuid, start, end);
//            MapCacheUtil.invalidateItemsInViz(mapViewDefUuid);
//            MapCacheUtil.invalidateMapNodeInfoAndMapTrackInfo(mapViewDefUuid);
        } else if ((oldStart != start) || (oldEnd != end)) {
            refreshMap = true;
            MapCacheUtil.setRange(mapViewDefUuid, start, end);
//            MapCacheUtil.invalidateItemsInViz(mapViewDefUuid);
//            MapCacheUtil.invalidateMapNodeInfoAndMapTrackInfo(mapViewDefUuid);
        }

        if (refreshMap) {
            MapCacheUtil.invalidateExtentIfMapNotPinned(mapViewDefUuid);
        }

        return refreshMap;
    }

    @Override
    public void setRangeHome(String dataViewUuid, String mapViewDefUuid) {
        List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = MapCacheUtil.getSeriesValues(mapViewDefUuid);
        MapCacheUtil.setRange(mapViewDefUuid, 0, seriesValues.size() - 1);
    }

    @Override
    public void selectFirstTrack(boolean deselect, String mapViewDefUuid, String dataViewUuid) {
        AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid);
        Integer rangeStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
        Integer rangeEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);
        List<LinkGeometry> linkGeometries = MapCacheUtil.getTrackMapSummaryGrid(mapViewDefUuid).getSequenceFirstSegmentsAfter(rangeStart, rangeEnd);
        if (linkGeometries != null) {
            for (LinkGeometry linkGeometry : linkGeometries) {
                if (deselect) {
                    mapSelection.removeLink(linkGeometry);
                } else {
                    mapSelection.addLink(linkGeometry);
                }
            }
        }
    }

    @Override
    public void selectLastTrack(boolean deselect, String mapViewDefUuid, String dataViewUuid) {
        AbstractMapSelection mapSelection = MapServiceUtil.getMapSelection(dataViewUuid, mapViewDefUuid);
        Integer rangeEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);
        Integer rangeStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
        List<LinkGeometry> linkGeometries = MapCacheUtil.getTrackMapSummaryGrid(mapViewDefUuid).getSequenceLastSegmentBefore(rangeStart, rangeEnd);
        if (linkGeometries != null) {
            for (LinkGeometry linkGeometry : linkGeometries) {
                if (deselect) {
                    mapSelection.removeLink(linkGeometry);
                } else {
                    mapSelection.addLink(linkGeometry);
                }
            }
        }
    }

    @Override
    public String getStartAndEnd(String mapViewDefUuid, String dataViewUuid) {
        String s = "\n";
        Integer rangeStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
        Integer rangeEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);

        TrackMapSummaryGrid trackMapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid(mapViewDefUuid);
        if ((trackMapSummaryGrid == null) || (rangeStart == null) || (rangeEnd == null)) {
            return s;
        }

        TrackMapSummaryGrid.SequenceSortValue startSequenceValue = trackMapSummaryGrid.getSequenceValue(rangeStart);
        if (startSequenceValue == null) {
            return s;
        }
        String startValue = startSequenceValue.getSequenceValue().toString();
        TrackMapSummaryGrid.SequenceSortValue endSequenceValue = trackMapSummaryGrid.getSequenceValue(rangeEnd);
        if (endSequenceValue == null) {
            return s;
        }
        String endValue = endSequenceValue.getSequenceValue().toString();
        if ((startValue == null) || (endValue == null)) {
            return s;
        }
        {
            List<String> strings = new ArrayList<String>(Arrays.asList(Strings.split(endValue, '\n')));
            if (!strings.isEmpty()) {
                endValue = strings.get(0);
            }
        }
        {
            List<String> strings = new ArrayList<String>(Arrays.asList(Strings.split(startValue, '\n')));
            if (!strings.isEmpty()) {
                startValue = strings.get(0);
            }
        }
        return startValue + "\n" + endValue;
    }

    @Override
    public ExtentInfo getExtentInfo(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber) {
        MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
        return ExtentInfoBuilder.getExtentInfo(mapCacheHandler);
    }

    @Override
    public boolean isMetricsReady(String dataViewUuid, String mapViewDefUuid) {
        CurrentInfo currentInfo = MapCacheUtil.getCurrentInfo(mapViewDefUuid);
        if (currentInfo != null) {
            Integer mapSummaryPrecision = currentInfo.getMapSummaryPrecision();
            return mapSummaryPrecision != null;
        }
        return false;
    }

    @Override
    public void setMapPinned(String dataViewUuid, String mapViewDefUuid, boolean value) {
        MapCacheUtil.setMapPinned(mapViewDefUuid, value);
    }

    @Override
    public boolean isMapPinned(String dataViewUuid, String mapViewDefUuid) {
        return MapCacheUtil.isMapPinned(mapViewDefUuid);
    }

    @Override
    public void setSelectionMode(String dataViewUuid, String mapViewDefUuid, int value) {
        MapCacheUtil.setSelectionMode(mapViewDefUuid, value);
    }

    @Override
    public Integer getSelectionMode(String dataViewUuid, String mapViewDefUuid) {
        return MapCacheUtil.getSelectionMode(mapViewDefUuid);
    }
}
