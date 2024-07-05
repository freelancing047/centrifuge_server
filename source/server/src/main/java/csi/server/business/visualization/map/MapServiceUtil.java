package csi.server.business.visualization.map;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import csi.config.Configuration;
import csi.server.business.service.MapActionsService;
import csi.server.business.service.map.TypeLimitStatusChecker;
import csi.server.business.visualization.map.mapserviceutil.MapBundleInfo;
import csi.server.business.visualization.map.mapserviceutil.typesorter.AbstractPlaceTypeSorter;
import csi.server.business.visualization.map.mapserviceutil.typesorter.AssociationTypeSorter;
import csi.server.business.visualization.map.mapserviceutil.typesorter.SummaryPlaceTypeSorter;
import csi.server.business.visualization.map.mapserviceutil.typesorter.TrackTypeSorter;
import csi.server.business.visualization.map.mapsettings.MapSettingsDTOBuilder;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.HeatMapInfo;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.MapSelectionGrid;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SummaryMapSelection;
import csi.server.common.model.visualization.selection.TrackmapSelection;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskHelper;
import csi.server.task.api.TaskSession;
import csi.server.task.exception.TaskAbortedException;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class MapServiceUtil {
   private static final Logger LOG = LogManager.getLogger(MapActionsService.class);

    public static MapContext getMapContext(String dvUuid, String vizUuid) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        MapContext mapContext = getMapContext(taskSession, vizUuid);
        if (mapContext == null) {
            for (TaskSession oneTaskSession : TaskController.getInstance().getUserSessions()) {
                try {
                    mapContext = getMapContext(oneTaskSession, vizUuid);
                } catch (TaskAbortedException tae) {
                    mapContext = null;
                }
                if (mapContext != null) {
                    break;
                }
            }
        }
        MapContext.Current.set(mapContext);
        return mapContext;
    }

    public static PlaceDynamicTypeInfo getPlaceDynamicTypeInfo(String vizUuid) {

        TaskSession taskSession = TaskHelper.getCurrentSession();
        if (taskSession == null) {// NOTE: for calls from mapModule.js
            return ((Map<String, PlaceDynamicTypeInfo>) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(GraphConstants.KEY_CONTEXTS_MAP_PLACE_DYNAMICTYPEINFO)).get(vizUuid);
        }
        PlaceDynamicTypeInfo dynamicTypeInfo = getPlaceDynamicTypeInfo(taskSession, vizUuid);
        if (dynamicTypeInfo == null) {
            for (TaskSession oneTaskSession : TaskController.getInstance().getUserSessions()) {
                try {
                    dynamicTypeInfo = getPlaceDynamicTypeInfo(oneTaskSession, vizUuid);
                } catch (TaskAbortedException tae) {
                    dynamicTypeInfo = null;
                }
                if (dynamicTypeInfo != null) {
                    break;
                }
            }
        }
        return dynamicTypeInfo;
    }

    public static TrackDynamicTypeInfo getTrackDynamicTypeInfo(String vizUuid) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        if (taskSession == null) {// NOTE: for calls from mapModule.js
            return ((Map<String, TrackDynamicTypeInfo>) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(GraphConstants.KEY_CONTEXTS_MAP_TRACK_DYNAMICTYPEINFO)).get(vizUuid);
        }
        TrackDynamicTypeInfo dynamicTypeInfo = getTrackDynamicTypeInfo(taskSession, vizUuid);
        if (dynamicTypeInfo == null) {
            for (TaskSession oneTaskSession : TaskController.getInstance().getUserSessions()) {
                try {
                    dynamicTypeInfo = getTrackDynamicTypeInfo(oneTaskSession, vizUuid);
                } catch (TaskAbortedException tae) {
                    dynamicTypeInfo = null;
                }
                if (dynamicTypeInfo != null) {
                    break;
                }
            }
        }
        return dynamicTypeInfo;
    }

    public static void setMapContext(MapContext mapContext) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        setMapContext(taskSession, mapContext);
    }

    public static void setPlaceDynamicTypeInfo(PlaceDynamicTypeInfo dynamicTypeInfo, String vizUuid) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        setPlaceDynamicTypeInfo(taskSession, vizUuid, dynamicTypeInfo);
    }

    public static void setTrackDynamicTypeInfo(TrackDynamicTypeInfo dynamicTypeInfo, String vizUuid) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        setTrackDynamicTypeInfo(taskSession, vizUuid, dynamicTypeInfo);
    }

    public static MapContext removeMapContext(String vizUuid) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        MapContext mapContext = removeMapContext(taskSession, vizUuid);
        for (TaskSession onetaskSession : TaskController.getInstance().getUserSessions()) {
            removeMapContext(onetaskSession, vizUuid);
        }
        return mapContext;
    }

    public static PlaceDynamicTypeInfo removePlaceDynamicTypeInfo(String vizUuid) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        PlaceDynamicTypeInfo dynamicTypeInfo = removePlaceDynamicTypeInfo(taskSession, vizUuid);
        for (TaskSession oneTaskSession : TaskController.getInstance().getUserSessions()) {
            removePlaceDynamicTypeInfo(oneTaskSession, vizUuid);
        }
        return dynamicTypeInfo;
    }

    private static TrackDynamicTypeInfo removeTrackDynamicTypeInfo(String vizUuid) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        TrackDynamicTypeInfo dynamicTypeInfo = removeTrackDynamicTypeInfo(taskSession, vizUuid);
        for (TaskSession oneTaskSession : TaskController.getInstance().getUserSessions()) {
            removeTrackDynamicTypeInfo(oneTaskSession, vizUuid);
        }
        return dynamicTypeInfo;
    }

    //
    // base methods
    //

    private static MapContext getMapContext(TaskSession taskSession, String vizUuid) {
        Map<String, MapContext> mapContexts = getMapContextMap(taskSession);
        if (mapContexts == null) {
         return null;
      }

        synchronized (mapContexts) {
            MapContext mapContext = mapContexts.get(vizUuid);
            if ((mapContext != null) && mapContext.isInvalidated()) {
                // this throws a runtime exception
                mapContexts.remove(mapContext);
                TaskHelper.abortTask("Map context is invalidated");
            }
            return mapContext;
        }
    }

    private static PlaceDynamicTypeInfo getPlaceDynamicTypeInfo(TaskSession taskSession, String vizUuid) {
        Map<String, PlaceDynamicTypeInfo> placeDynamicTypeInfos = getPlaceDynamicTypeInfoMap(taskSession);
        if (placeDynamicTypeInfos == null) {
         return null;
      }

        synchronized (placeDynamicTypeInfos) {
            PlaceDynamicTypeInfo placeDynamicTypeInfo = placeDynamicTypeInfos.get(vizUuid);
            if ((placeDynamicTypeInfo != null) && placeDynamicTypeInfo.isInvalidated()) {
                placeDynamicTypeInfos.remove(placeDynamicTypeInfo);
                TaskHelper.abortTask("Map Place Dynamic Type Info is invalidated");
            }
            return placeDynamicTypeInfo;
        }
    }

    private static TrackDynamicTypeInfo getTrackDynamicTypeInfo(TaskSession taskSession, String vizUuid) {
        Map<String, TrackDynamicTypeInfo> trackDynamicTypeInfos = getTrackDynamicTypeInfoMap(taskSession);
        if (trackDynamicTypeInfos == null) {
         return null;
      }

        synchronized (trackDynamicTypeInfos) {
            TrackDynamicTypeInfo trackDynamicTypeInfo = trackDynamicTypeInfos.get(vizUuid);
            if ((trackDynamicTypeInfo != null) && trackDynamicTypeInfo.isInvalidated()) {
                trackDynamicTypeInfos.remove(trackDynamicTypeInfo);
                TaskHelper.abortTask("Map Track Dynamic Type Info is invalidated");
            }
            return trackDynamicTypeInfo;
        }
    }

   private static MapContext removeMapContext(TaskSession taskSession, String vizUuid) {
      MapContext mapContext = null;
      Map<String,MapContext> mapContexts = getMapContextMap(taskSession);

      if (mapContexts != null) {
         synchronized (mapContexts) {
            mapContext = mapContexts.get(vizUuid);

            if (mapContext != null) {
                mapContext.setInvalidated(true);

                mapContext = mapContexts.remove(vizUuid);
            }
         }
      }
      return mapContext;
   }

   private static PlaceDynamicTypeInfo removePlaceDynamicTypeInfo(TaskSession taskSession, String vizUuid) {
      PlaceDynamicTypeInfo dynamicTypeInfo = null;
      Map<String,PlaceDynamicTypeInfo> vizMaps = getPlaceDynamicTypeInfoMap(taskSession);

      if (vizMaps != null) {
         synchronized (vizMaps) {
            dynamicTypeInfo = vizMaps.get(vizUuid);

            if (dynamicTypeInfo != null) {
               dynamicTypeInfo.invalidate();
               dynamicTypeInfo = vizMaps.remove(vizUuid);
            }
         }
      }
      return dynamicTypeInfo;
   }

   private static TrackDynamicTypeInfo removeTrackDynamicTypeInfo(TaskSession taskSession, String vizUuid) {
      TrackDynamicTypeInfo dynamicTypeInfo = null;
      Map<String,TrackDynamicTypeInfo> vizMaps = getTrackDynamicTypeInfoMap(taskSession);

      if (vizMaps != null) {
         synchronized (vizMaps) {
            dynamicTypeInfo = vizMaps.get(vizUuid);

            if (dynamicTypeInfo != null) {
                dynamicTypeInfo.invalidate();
                dynamicTypeInfo = vizMaps.remove(vizUuid);
            }
         }
      }
      return dynamicTypeInfo;
   }

   private static void setMapContext(TaskSession taskSession, MapContext mapContext) {
      Map<String,MapContext> vizMaps = getMapContextMap(taskSession);

      if (vizMaps != null) {
         synchronized (vizMaps) {
            String vizUuid = mapContext.getVizUuid();

            vizMaps.put(vizUuid, mapContext);
         }
      }
   }

   private static void setPlaceDynamicTypeInfo(TaskSession taskSession, String vizUuid, PlaceDynamicTypeInfo dynamicTypeInfo) {
      Map<String,PlaceDynamicTypeInfo> vizMaps = getPlaceDynamicTypeInfoMap(taskSession);

      if (vizMaps != null) {
         synchronized (vizMaps) {
            vizMaps.put(vizUuid, dynamicTypeInfo);
         }
      }
   }

   private static void setTrackDynamicTypeInfo(TaskSession taskSession, String vizUuid, TrackDynamicTypeInfo dynamicTypeInfo) {
      Map<String,TrackDynamicTypeInfo> vizMaps = getTrackDynamicTypeInfoMap(taskSession);

      if (vizMaps != null) {
         synchronized (vizMaps) {
            vizMaps.put(vizUuid, dynamicTypeInfo);
         }
      }
   }

    private static Map<String, MapContext> getMapContextMap(TaskSession taskSession) {
        if (taskSession == null) {
         return null;
      }
        Map<String, MapContext> vizMaps = (Map<String, MapContext>) taskSession.getAttribute(GraphConstants.KEY_CONTEXTS_MAP);
        if (vizMaps == null) {
            vizMaps = taskSession.setAttributeIfAbsent(GraphConstants.KEY_CONTEXTS_MAP, Maps.newConcurrentMap());
        }
        return vizMaps;
    }

    private static Map<String, PlaceDynamicTypeInfo> getPlaceDynamicTypeInfoMap(TaskSession taskSession) {
        if (taskSession == null) {
         return null;
      }
        Map<String, PlaceDynamicTypeInfo> vizMaps = (Map<String, PlaceDynamicTypeInfo>) taskSession.getAttribute(GraphConstants.KEY_CONTEXTS_MAP_PLACE_DYNAMICTYPEINFO);
        if (vizMaps == null) {
            vizMaps = taskSession.setAttributeIfAbsent(GraphConstants.KEY_CONTEXTS_MAP_PLACE_DYNAMICTYPEINFO,
                    Maps.newConcurrentMap());
        }
        return vizMaps;
    }

    private static Map<String, TrackDynamicTypeInfo> getTrackDynamicTypeInfoMap(TaskSession taskSession) {
        if (taskSession == null) {
         return null;
      }
        Map<String, TrackDynamicTypeInfo> vizMaps = (Map<String, TrackDynamicTypeInfo>) taskSession.getAttribute(GraphConstants.KEY_CONTEXTS_MAP_TRACK_DYNAMICTYPEINFO);
        if (vizMaps == null) {
            vizMaps = taskSession.setAttributeIfAbsent(GraphConstants.KEY_CONTEXTS_MAP_TRACK_DYNAMICTYPEINFO,
                    Maps.newConcurrentMap());
        }
        return vizMaps;
    }

    public static Selection getSelection(String dvUuid, String mapUuid) {
        Selection selection = null;
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
                synchronized (mc) {
                    selection = mc.getSelection();
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
        if (selection == null) {
         selection = NullSelection.instance;
      }
        return selection;
    }

    public static void addSelection(String dvUuid, String mapUuid, Selection selection) {
        MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
        if (mc == null) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Expected Graph Context. Found Null.");
            }
            return;
        }
        synchronized (mc) {
            mc.setSelection(selection);
        }
    }

    public static TrackmapSelection getTrackMapSelection(String dvUuid, String mapUuid) {
        Selection selection = getSelection(dvUuid, mapUuid);
        TrackmapSelection trackmapSelection;
        if (selection instanceof TrackmapSelection) {
            trackmapSelection = (TrackmapSelection) selection;
        } else {
            trackmapSelection = new TrackmapSelection((mapUuid));
            addSelection(dvUuid, mapUuid, trackmapSelection);
        }
        return trackmapSelection;
    }

    public static AbstractMapSelection getMapSelection(String dvUuid, String mapUuid) {
        Selection selection = getSelection(dvUuid, mapUuid);
        AbstractMapSelection mapSelection;
        if (selection instanceof AbstractMapSelection) {
            mapSelection = (AbstractMapSelection) selection;
        } else {
            if (isUseTrack(dvUuid, mapUuid)) {
                mapSelection = new TrackmapSelection((mapUuid));
            } else {
                mapSelection = new SummaryMapSelection(mapUuid);
            }
            addSelection(dvUuid, mapUuid, mapSelection);
        }
        return mapSelection;
    }

    public static void selectNodes(String dvUuid, String mapUuid, String[] ids) {
        Map<Long, AugmentedMapNode> mapNodeMapById = MapCacheUtil.getTheRightSummaryMapNodeMapById(mapUuid);
        if (mapNodeMapById != null) {
            AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
            selectNode2(mapSelection, ids, mapNodeMapById);
        }
    }

    private static int selectNode2(AbstractMapSelection mapSelection, String[] nodeIds, Map<Long, AugmentedMapNode> mapNodeMap) {
        int retVal = 0;
        for (String nodeId : nodeIds) {
            AugmentedMapNode mapNode = mapNodeMap.get(Long.parseLong(nodeId));
            if (mapNode != null) {
                selectionAddNode(mapSelection, mapNode.getGeometry());
                retVal++;
            }
        }
        return retVal;
    }

    public static void selectionAddNode(AbstractMapSelection selection, Geometry geometry) {
        if (selection instanceof TrackmapSelection) {
            TrackMapSummaryGrid trackmapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid((selection.getMapViewDefUuid()));
            if (trackmapSummaryGrid != null) {
                if (geometry.getSummaryLevel() == Configuration.getInstance().getMapConfig().getDetailLevel()) {
                    selection.addLinks(trackmapSummaryGrid.getLinksComposedOf(geometry));
                } else {
                    selection.addLinks(trackmapSummaryGrid.getLinkDescendants(geometry));
                }
            }
        } else {
            if ((selection instanceof SummaryMapSelection) && (geometry.getSummaryLevel() != Configuration.getInstance().getMapConfig().getDetailLevel())) {
                MapSummaryGrid mapSummaryGrid = MapCacheUtil.getMapSummaryGrid(selection.getMapViewDefUuid());
                if (mapSummaryGrid != null) {
                    mapSummaryGrid.getDescendants(geometry).forEach(selection::addNode);
                }
            } else {
                selection.addNode(geometry);
            }
        }
    }

    public static void deselectNodes(String dvUuid, String mapUuid, String[] ids) {
        Selection selection = getSelection(dvUuid, mapUuid);
        Map<Long, AugmentedMapNode> mapNodeMapById = MapCacheUtil.getTheRightSummaryMapNodeMapById(mapUuid);
        if (mapNodeMapById != null) {
            if (selection instanceof AbstractMapSelection) {
                AbstractMapSelection mapSelection = (AbstractMapSelection) selection;
                deselectNodes(mapSelection, ids, mapNodeMapById);
            }
        }
    }

    private static int deselectNodes(AbstractMapSelection mapSelection, String[] nodeIds, Map<Long, AugmentedMapNode> mapNodeMap) {
        int retVal = 0;
        for (String nodeId : nodeIds) {
            AugmentedMapNode mapNode = mapNodeMap.get(Long.parseLong(nodeId));
            if (mapNode != null) {
                selectionRemoveNode(mapSelection, mapNode.getGeometry());
                retVal++;
            }
        }
        return retVal;
    }

    public static void selectionRemoveNode(AbstractMapSelection selection, Geometry geometry) {
        if (selection instanceof TrackmapSelection) {
            removeTrackSegmentsWithNode(selection, geometry);
        } else {
            if ((selection instanceof SummaryMapSelection) && (geometry.getSummaryLevel() != Configuration.getInstance().getMapConfig().getDetailLevel())) {
                SummaryMapSelection summaryMapSelection = (SummaryMapSelection) selection;
                MapSelectionGrid mapSelectionGrid = summaryMapSelection.getMapSelectionGrid();
                if (mapSelectionGrid.hasSmaller(geometry)) {
                    summaryMapSelection.removeSmaller(geometry);
                }
            } else {
                selection.removeNode(geometry);
            }
        }
    }

    private static void removeTrackSegmentsWithNode(AbstractMapSelection selection, Geometry geometry) {
        TrackMapSummaryGrid trackmapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid((selection.getMapViewDefUuid()));
        if (trackmapSummaryGrid != null) {
            if (geometry.getSummaryLevel() != Configuration.getInstance().getMapConfig().getDetailLevel()) {
                selection.removeLinks(trackmapSummaryGrid.getLinkDescendants(geometry));
            } else {
                selection.removeLinks(trackmapSummaryGrid.getLinksComposedOf(geometry));
            }
        }
    }

    public static void selectLinks(String dvUuid, String mapUuid, String[] linkIds) {
        Map<Long, MapLink> mapLinkMapById = null;
        if (!isHandleBundle(dvUuid, mapUuid)) {
            mapLinkMapById = getMapLinkByIdMap(mapUuid);
        }
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        if (isUseTrack(dvUuid, mapUuid)) {
            selectLinksForTracks(mapSelection, linkIds, mapLinkMapById);
        } else {
            selectLinks2(mapSelection, linkIds, mapLinkMapById);
        }
    }

    private static void selectLinksForTracks(AbstractMapSelection mapSelection, String[] linkIds, Map<Long, MapLink> mapLinkMap) {
        Arrays.stream(linkIds).map(nodeId -> mapLinkMap.get(Long.parseLong(nodeId))).filter(Objects::nonNull).map(MapLink::getLinkGeometry).forEach(mapSelection::addLink);
    }

    private static void selectLinks2(AbstractMapSelection mapSelection, String[] linkIds, Map<Long, MapLink> mapLinkMap) {
        Arrays.stream(linkIds).map(nodeId -> mapLinkMap.get(Long.parseLong(nodeId))).filter(Objects::nonNull).forEach(mapLink -> {
            mapLink.setSelected(true);
            mapSelection.addLink(getLinkGeometryPlus(mapLink));
        });
    }

    private static LinkGeometryPlus getLinkGeometryPlus(MapLink mapLink) {
        LinkGeometry linkGeometry = mapLink.getLinkGeometry();
        LinkGeometryPlus linkGeometryPlus = new LinkGeometryPlus(linkGeometry.getLinkType(), linkGeometry.getNode1Geometry(), linkGeometry.getNode2Geometry());
        mapLink.getRowIds().forEach(linkGeometryPlus::addRowId);
        return linkGeometryPlus;
    }

    public static String toggleNodeSelected(String dvUuid, String mapUuid, String[] ids) {
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        Map<Long, AugmentedMapNode> mapNodeMapById = MapCacheUtil.getTheRightSummaryMapNodeMapById(mapUuid);
        if (mapNodeMapById != null) {
            Long id = Long.parseLong(ids[0]);
            AugmentedMapNode mapNode = mapNodeMapById.get(id);
            // see if the first item is in the list
            if (isSelected(mapSelection, mapNode)) {
                // yes: remove all of the request from the list
                deselectNodes(mapSelection, ids, mapNodeMapById);
                return "unselected";
            } else {
                // no: add all of the request to the list
                selectNode2(mapSelection, ids, mapNodeMapById);
                return "selected";
            }
        }
        return "";
    }

    public static int toggleTrackNodes(String dvUuid, String mapUuid, String[] ids) {
        TrackmapSelection trackmapSelection = getTrackMapSelection(dvUuid, mapUuid);
        Map<Long, AugmentedMapNode> mapNodeMapById = MapCacheUtil.getTrackSummaryMapNodeMapById(mapUuid);
        if (mapNodeMapById != null) {
            Long id = Long.parseLong(ids[0]);
            AugmentedMapNode mapNode = mapNodeMapById.get(id);
            Geometry geometry = mapNode.getGeometry();
            Set<LinkGeometry> selected = trackmapSelection.getNumEqualGeometry(geometry);
            Set<LinkGeometry> incident = new TreeSet<LinkGeometry>();
            MapTrackInfo mapTrackInfo = MapCacheUtil.getMapTrackInfo(mapUuid);
            Map<TrackidTracknameDuple, List<MapLink>> mapLinkByKey;
            if (mapTrackInfo != null) {
                mapLinkByKey = mapTrackInfo.getMapLinkByKey();
                mapLinkByKey.values().forEach(mapLinks -> mapLinks.forEach(mapLink -> {
                    LinkGeometry linkGeometry = mapLink.getLinkGeometry();
                    if (linkGeometry.getNode1Geometry().equals(geometry) || linkGeometry.getNode2Geometry().equals(geometry)) {
                        incident.add(linkGeometry);
                    }
                }));
            }
            boolean doDeselect = selected.equals(incident);
            // see if the first item is in the list
            if (doDeselect) {
                // yes: remove all of the request from the list
                return deselectNodes(trackmapSelection, ids, mapNodeMapById);
            } else {
                // no: add all of the request to the list
                return selectNode2(trackmapSelection, ids, mapNodeMapById);
            }
        }
        return 0;
    }

    public static boolean toggleLinkSelected(String dvUuid, String mapUuid, String[] ids) {
        boolean changesMade = false;
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        if (ids.length >= 4) {
            Double x1 = Double.parseDouble(ids[0]);
            Double y1 = Double.parseDouble(ids[1]);
            Double x2 = Double.parseDouble(ids[2]);
            Double y2 = Double.parseDouble(ids[3]);
            LinkGeometry linkGeometry = getLinkGeometry(x1, y1, x2, y2);
            if (mapSelection.containsLink(linkGeometry)) {
               mapSelection.removeLink(linkGeometry);
            } else {
               mapSelection.addLink(linkGeometry);
            }
            changesMade = true;
        }
        return changesMade;
    }

    public static boolean selectLinks2(String dvUuid, String mapUuid, String[] ids) {
        boolean changesMade = false;
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        int i = 0;
        while ((i + 3) < ids.length) {
            Double x1 = Double.parseDouble(ids[i]);
            i++;
            Double y1 = Double.parseDouble(ids[i]);
            i++;
            Double x2 = Double.parseDouble(ids[i]);
            i++;
            Double y2 = Double.parseDouble(ids[i]);
            i++;
            LinkGeometry linkGeometry = getLinkGeometry(x1, y1, x2, y2);
            if (!mapSelection.containsLink(linkGeometry)) {
                mapSelection.addLink(linkGeometry);
                changesMade = true;
            }
        }
        return changesMade;
    }

    public static boolean deselectLinks2(String dvUuid, String mapUuid, String[] ids) {
        boolean changesMade = false;
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        int i = 0;
        while ((i + 3) < ids.length) {
            Double x1 = Double.parseDouble(ids[i]);
            i++;
            Double y1 = Double.parseDouble(ids[i]);
            i++;
            Double x2 = Double.parseDouble(ids[i]);
            i++;
            Double y2 = Double.parseDouble(ids[i]);
            i++;
            LinkGeometry linkGeometry = getLinkGeometry(x1, y1, x2, y2);
            if (mapSelection.containsLink(linkGeometry)) {
                mapSelection.removeLink(linkGeometry);
                changesMade = true;
            }
        }
        return changesMade;
    }

    private static LinkGeometry getLinkGeometry(Double x1, Double y1, Double x2, Double y2) {
        if (x1 < -180) {
            x1 += 360;
        }
        Geometry geometry1 = new Geometry(x1, y1);
        geometry1.setSummaryLevel(Configuration.getInstance().getMapConfig().getDetailLevel());
        if (x2 < -180) {
            x2 += 360;
        }
        Geometry geometry2 = new Geometry(x2, y2);
        geometry2.setSummaryLevel(Configuration.getInstance().getMapConfig().getDetailLevel());
        return new LinkGeometry(0, geometry1, geometry2);
    }

    public static void deselectAll(String dvUuid, String mapUuid) {
        if (isUseTrack(dvUuid, mapUuid)) {
            addSelection(dvUuid, mapUuid, new TrackmapSelection(mapUuid));
        } else {
            addSelection(dvUuid, mapUuid, new SummaryMapSelection(mapUuid));
        }
    }

    public static void selectAll(String dvUuid, String mapUuid) {
        AbstractMapSelection mapSelection;
        if (isUseTrack(dvUuid, mapUuid)) {
            mapSelection = new TrackmapSelection(mapUuid);
            TrackMapSummaryGrid trackMapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid(mapUuid);
            mapSelection.addLinks(trackMapSummaryGrid.getAllLinks());
        } else {
            mapSelection = new SummaryMapSelection(mapUuid);
            selectAllMapNodes(mapUuid, mapSelection);
            selectAllMapLinks(mapUuid, mapSelection);
        }
        addSelection(dvUuid, mapUuid, mapSelection);
    }

    private static void selectAllMapNodes(String mapViewDefUuid, AbstractMapSelection mapSelection) {
        MapSummaryGrid geoInfo = MapCacheUtil.getMapSummaryGrid(mapViewDefUuid);
        if (geoInfo == null) {
            Map<Long, AugmentedMapNode> mapNodeMapById = MapCacheUtil.getMapNodeByIdMap(mapViewDefUuid);
            mapNodeMapById.values().forEach(mapNode -> selectionAddNode(mapSelection, mapNode.getGeometry()));
        } else {
            MapSummaryExtent mapSummaryExtent = new MapSummaryExtent(-180.0, -90.0, 180.0, 90.0);
            geoInfo.getDescendants(mapSummaryExtent).forEach(geometry -> selectionAddNode(mapSelection, geometry));
        }
    }

    private static void selectAllMapLinks(String mapViewDefUuid, AbstractMapSelection mapSelection) {
        Set<LinkGeometryPlus> links = MapCacheUtil.getLinks(mapViewDefUuid);
        if (links == null) {
            Map<Long, MapLink> mapLinkMapById = getMapLinkByIdMap(mapViewDefUuid);
            if (mapLinkMapById != null) {
                mapLinkMapById.values().stream().map(MapServiceUtil::getLinkGeometryPlus).forEach(mapSelection::addLink);
            }
        } else {
            links.forEach(mapSelection::addLink);
        }
    }

    public static Map<Long, MapLink> getMapLinkByIdMap(String mapViewDefUuid) {
        if (isUseTrack(mapViewDefUuid)) {
            MapTrackInfo mapTrackInfo = MapCacheUtil.getMapTrackInfo(mapViewDefUuid);
            if (mapTrackInfo == null) {
                return null;
            } else {
                return mapTrackInfo.getMapLinkById();
            }
        } else {
            MapLinkInfo mapLinkInfo = MapCacheUtil.getMapLinkInfo(mapViewDefUuid);
            if (mapLinkInfo == null) {
                return null;
            } else {
                return mapLinkInfo.getMapById();
            }
        }
    }

    public static void handleNodeToggleOperations(String dvUuid, String mapUuid, String selectionOperation, Collection<AugmentedMapNode> mapNodes) {
        Set<Geometry> geometries = mapNodes.stream().map(MapNode::getGeometry).collect(Collectors.toCollection(Sets::newTreeSet));
        handleNodeToggleOperations(dvUuid, mapUuid, selectionOperation, geometries);
    }

    public static void handleNodeToggleOperations(String dvUuid, String mapUuid, String selectionOperation, Set<Geometry> geometries) {
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        geometries.forEach(geometry -> {
            if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                selectionRemoveNode(mapSelection, geometry);
            } else {
                selectionAddNode(mapSelection, geometry);
            }
        });
    }

    public static void handleLinkToggleOperations(String dvUuid, String mapUuid, String selectionOperation, Collection<MapLink> mapLinks) {
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        mapLinks.forEach(mapLink -> {
            if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                mapSelection.removeLink(mapLink.getLinkGeometry());
            } else {
                mapSelection.addLink(getLinkGeometryPlus(mapLink));
            }
        });
    }

    public static void handleLinkToggleOperationsForTrack(String dvUuid, String mapUuid, String selectionOperation, Set<LinkGeometry> linkGeometries) {
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        linkGeometries.forEach(linkGeometry -> {
            if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
               mapSelection.removeLink(linkGeometry);
            } else {
                mapSelection.addLink(linkGeometry);
            }
        });
    }

    public static void selectionAddNodes(Collection<Geometry> geometries, AbstractMapSelection toSelection) {
        if (geometries != null) {
            geometries.forEach(geometry -> selectionAddNode(toSelection, geometry));
        }
    }

    public static void selectionRemoveNodes(Collection<Geometry> geometries, AbstractMapSelection toSelection) {
        if (geometries != null) {
            geometries.forEach(geometry -> selectionRemoveNode(toSelection, geometry));
        }
    }

    public static void initializeMapViewState(String dvUuid, String mapUuid) {
        setLegendShown(dvUuid, mapUuid, true);
        setMultitypeDecoratorShown(dvUuid, mapUuid, false);
    }

    public static void setLegendShown(String dvUuid, String mapUuid, boolean value) {
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
                synchronized (mc) {
                    mc.setLegendShown(value);
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
    }

    public static boolean isLegendShown(String dvUuid, String mapUuid) {
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
                synchronized (mc) {
                    return mc.isLegendShown();
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
        return false;
    }

    public static void setMultitypeDecoratorShown(String dvUuid, String mapUuid, boolean value) {
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
                synchronized (mc) {
                    mc.setMultitypeDecoratorShown(value);
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
    }

    public static boolean isMultitypeDecoratorShown(String dvUuid, String mapUuid) {
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
                synchronized (mc) {
                    return mc.isMultitypeDecoratorShown();
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
        return false;
    }

    public static void setLinkupDecoratorShown(String dvUuid, String mapUuid, boolean value) {
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
               synchronized (mc) {
                    mc.setLinkupDecoratorShown(value);
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
    }

    public static boolean isLinkupDecoratorShown(String dvUuid, String mapUuid) {
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
               synchronized (mc) {
                    return mc.isLinkupDecoratorShown();
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
        return false;
    }

    public static void invalidatePlaceDynamicTypeInfo(String mapUuid) {
        removePlaceDynamicTypeInfo(mapUuid);
    }

    public static void invalidateTrackDynamicTypeInfo(String mapUuid) {
        removeTrackDynamicTypeInfo(mapUuid);
    }

    public static boolean isSelected(AbstractMapSelection mapSelection, MapNode mapNode) {
        if (mapSelection == null) {
         return false;
      }
        return mapSelection.containsGeometry(mapNode.getGeometry());
    }

    public static boolean isSelected(AbstractMapSelection mapSelection, MapLink mapLink) {
        if (mapSelection == null) {
         return false;
      }
        return mapSelection.containsLink(mapLink.getLinkGeometry());
    }

    public static void updatePlaceTypeOrder(String mapUuid, List<PlaceidTypenameDuple> placeNames) {
        AbstractPlaceTypeSorter sorter;
        sorter = new SummaryPlaceTypeSorter(mapUuid, placeNames);
        sorter.sort();
    }

    public static void updateAssociationTypeOrder(String mapViewDefUuid, List<String> associationNames) {
        AssociationTypeSorter sorter = new AssociationTypeSorter(mapViewDefUuid, associationNames);
        sorter.sort();
    }

    public static HeatMapInfo getHeatMapInfo(String dvUuid, String mapUuid) {
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
                synchronized (mc) {
                    HeatMapInfo heatMapInfo = mc.getHeatMapInfo();
                    if (heatMapInfo == null) {
                        heatMapInfo = new HeatMapInfo();
                        mc.setHeatMapInfo(heatMapInfo);
                    }
                    return heatMapInfo;
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
        return null;
    }

    private static MapBundleInfo getMapBundleInfo(String dvUuid, String mapUuid) {
        try {
            MapContext mc = MapServiceUtil.getMapContext(dvUuid, mapUuid);
            if (mc != null) {
                synchronized (mc) {
                    MapBundleInfo mapBundleInfo = mc.getMapBundleInfo();
                    if (mapBundleInfo == null) {
                        mapBundleInfo = new MapBundleInfo();
                        mc.setMapBundleInfo(mapBundleInfo);
                    }
                    return mapBundleInfo;
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
        return null;
    }

    public static boolean isHandleBundle(String dvUuid, String mapUuid) {
        boolean isHandleBundle;

        if (isBundleUsed(dvUuid, mapUuid)) {
            boolean isShowLeave = isShowLeaves(dvUuid, mapUuid);
            if (isShowLeave) {
               isHandleBundle = false;
            } else {
                List<Crumb> breadCrumb = getBreadcrumb(dvUuid, mapUuid);
                if (breadCrumb == null) {
                  isHandleBundle = true;
               } else {
                  isHandleBundle = breadCrumb.size() != getNumberOfBundleDefinitions(dvUuid, mapUuid);
               }
            }
        } else {
            isHandleBundle = false;
        }

        return isHandleBundle;
    }

    public static boolean isBundleUsed(String dvUuid, String mapUuid) {
        MapViewDef mapViewDef = CsiPersistenceManager.findObject(MapViewDef.class, mapUuid);
        MapSettings mapSettings = mapViewDef.getMapSettings();
        return mapSettings.isUseBundle() && !mapSettings.getMapBundleDefinitions().isEmpty();
    }

    public static boolean isUseTrack(String dvUuid, String mapUuid) {
        MapViewDef mapViewDef = CsiPersistenceManager.findObject(MapViewDef.class, mapUuid);
        MapSettings mapSettings = mapViewDef.getMapSettings();
        return mapSettings.isUseTrack();
    }

    public static boolean isShowLeaves(String dvUuid, String mapUuid) {
        MapBundleInfo mapBundleInfo = getMapBundleInfo(dvUuid, mapUuid);
        if (mapBundleInfo != null) {
         return mapBundleInfo.isShowLeaves();
      } else {
         return false;
      }
    }

    public static List<Crumb> getBreadcrumb(String dvUuid, String mapUuid) {
        MapBundleInfo mapBundleInfo = getMapBundleInfo(dvUuid, mapUuid);
        if (mapBundleInfo != null) {
         return mapBundleInfo.getBreadcrumb();
      } else {
         return null;
      }
    }

    public static int getNumberOfBundleDefinitions(String dvUuid, String mapUuid) {
        MapViewDef mapViewDef = CsiPersistenceManager.findObject(MapViewDef.class, mapUuid);
        MapSettings mapSettings = mapViewDef.getMapSettings();
        return mapSettings.getMapBundleDefinitions().size();
    }

   public static void setHeatmapBlurValue(String dvUuid, String mapUuid, Double blurValue) {
      HeatMapInfo heatMapInfo = getHeatMapInfo(dvUuid, mapUuid);

      if (heatMapInfo != null) {
         heatMapInfo.setBlurValue(blurValue);
      }
   }

   public static void setHeatmapMaxValue(String dvUuid, String mapUuid, Double maxValue) {
      HeatMapInfo heatMapInfo = getHeatMapInfo(dvUuid, mapUuid);

      if (heatMapInfo != null) {
         heatMapInfo.setMaxValue(maxValue);
      }
   }

   public static void setHeatmapMinValue(String dvUuid, String mapUuid, Double minValue) {
      HeatMapInfo heatMapInfo = getHeatMapInfo(dvUuid, mapUuid);

      if (heatMapInfo != null) {
         heatMapInfo.setMinValue(minValue);
      }
   }

    public static void setShowLeaves(String dvUuid, String mapUuid, boolean showLeaves) {
        MapBundleInfo mapBundleInfo = getMapBundleInfo(dvUuid, mapUuid);
        if (mapBundleInfo != null) {
         mapBundleInfo.setShowLeaves(showLeaves);
      }
    }

    public static void addBreadcrumb(String dvUuid, String mapUuid, List<Crumb> breadcrumb) {
        MapBundleInfo mapBundleInfo = getMapBundleInfo(dvUuid, mapUuid);
        if (mapBundleInfo != null) {
         mapBundleInfo.setBreadcrumb(breadcrumb);
      }
    }

    public static boolean isCountChildren(String dvUuid, String mapUuid) {
        List<Crumb> breadCrumb = getBreadcrumb(dvUuid, mapUuid);
        if (breadCrumb != null) {
         return (breadCrumb.size() + 1) < getNumberOfBundleDefinitions(dvUuid, mapUuid);
      } else {
         return 1 < getNumberOfBundleDefinitions(dvUuid, mapUuid);
      }
    }

    public static MapSettingsDTO createAndSaveMapSettingsDTO(String mapViewDefUuid, MapTheme mapTheme, DataView dataView, MapViewDef mapViewDef) {
        if (dataView == null) {
         return null;
      }
        if (mapViewDef == null) {
         return null;
      }
        MapSettingsDTOBuilder builder = new MapSettingsDTOBuilder(dataView, mapViewDef.getMapSettings(), mapTheme);
        builder.build();
        MapSettingsDTO mapSettingsDTO = builder.getMapSettingsDTO();
        mapSettingsDTO.setUuid(mapViewDefUuid);
        MapCacheUtil.addMapSettings(mapViewDefUuid, mapSettingsDTO);
        TypeLimitStatusChecker checker = new TypeLimitStatusChecker(mapSettingsDTO, dataView.getUuid(), dataView, mapViewDefUuid, mapViewDef);
        checker.check();
        return mapSettingsDTO;
    }

    public static void updateTrackTypeOrder(String mapViewDefUuid, List<TrackidTracknameDuple> trackNames) {
        TrackTypeSorter sorter = new TrackTypeSorter(mapViewDefUuid, trackNames);
        sorter.sort();
    }

    public static boolean selectTrackLinks(String dvUuid, String mapUuid, String[] nodes, String[] links) {
        boolean changesMade = false;
        AbstractMapSelection mapSelection = getMapSelection(dvUuid, mapUuid);
        Map<Long, AugmentedMapNode> mapNodeMapById = MapCacheUtil.getTheRightSummaryMapNodeMapById(mapUuid);
        if (mapNodeMapById != null) {
            TrackMapSummaryGrid trackmapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid(mapUuid);
            if (trackmapSummaryGrid != null) {
                if ((nodes != null) && (nodes.length > 0)) {
                    int i = 0;
                    while (i < nodes.length) {
                        String node = nodes[i];
                        AugmentedMapNode augmentedMapNode = mapNodeMapById.get(Long.parseLong(node));
                        if (augmentedMapNode != null) {
                            Geometry geometry = augmentedMapNode.getGeometry();
                            Set<LinkGeometry> linksOfNode;
                            if (geometry.getSummaryLevel() == Configuration.getInstance().getMapConfig().getDetailLevel()) {
                                linksOfNode = trackmapSummaryGrid.getLinksComposedOf(geometry);
                            } else {
                                linksOfNode = trackmapSummaryGrid.getLinkDescendants(geometry);
                            }
                            if ((linksOfNode != null) && !linksOfNode.isEmpty()) {
                                mapSelection.addLinks(linksOfNode);
                                changesMade = true;
                            }
                        }
                        i++;
                    }
                }
            }
        }
        int j = 0;
        while ((j + 3) < links.length) {
            Double x1 = Double.parseDouble(links[j]);
            j++;
            Double y1 = Double.parseDouble(links[j]);
            j++;
            Double x2 = Double.parseDouble(links[j]);
            j++;
            Double y2 = Double.parseDouble(links[j]);
            j++;
            LinkGeometry linkGeometry = getLinkGeometry(x1, y1, x2, y2);
            if (!mapSelection.containsLink(linkGeometry)) {
                mapSelection.addLink(linkGeometry);
                changesMade = true;
            }
        }
        return changesMade;
    }

    public static void applyTypenameToTypeInfo(PlaceTypeInfo typeInfo, PlaceidTypenameDuple key, int id) {
        typeInfo.getTypenameToId().put(key, id);
        typeInfo.getTypeIdToName().put(id, key);
    }

    public static void applyTypenameToTypeInfo(TrackTypeInfo typeInfo, TrackidTracknameDuple key, int id) {
        typeInfo.getTrackKeyToId().put(key, id);
        typeInfo.getTrackIdToKey().put(id, key);
    }

    public static void clearTypeInfo(PlaceTypeInfo typeInfo) {
        typeInfo.getTypenameToId().clear();
        typeInfo.getTypeIdToName().clear();
    }

    public static void clearTypeInfo(TrackTypeInfo typeInfo) {
        typeInfo.getTrackKeyToId().clear();
        typeInfo.getTrackIdToKey().clear();
    }

    public static String getNextShapeTypeString() {
        ShapeType shapeType = ShapeType.getNextNodeShape();
        while (shapeType == ShapeType.NONE) {
            shapeType = ShapeType.getNextNodeShape();
        }
        return shapeType.toString();
    }

    public static boolean isUseHeatmap(String dvUuid, String mapUuid) {
        MapViewDef mapViewDef = CsiPersistenceManager.findObject(MapViewDef.class, mapUuid);
        MapSettings mapSettings = mapViewDef.getMapSettings();
        return mapSettings.isUseHeatMap();
    }

    public static boolean isUseTrack(String mapViewDefUuid) {
        MapViewDef mapViewDef = CsiPersistenceManager.findObject(MapViewDef.class, mapViewDefUuid);
        MapSettings mapSettings = mapViewDef.getMapSettings();
        return mapSettings.isUseTrack();
    }

}