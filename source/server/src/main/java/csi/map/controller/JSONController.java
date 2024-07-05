package csi.map.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import csi.config.Configuration;
import csi.map.controller.model.Association;
import csi.map.controller.model.Payload;
import csi.map.controller.payloadbuilder.AbstractPayloadBuilder;
import csi.map.controller.payloadbuilder.BuildSummaryCacheCancelled;
import csi.map.controller.payloadbuilder.BundlePayloadLoader;
import csi.map.controller.payloadbuilder.DeferToNewCachePayloadBuilder;
import csi.map.controller.payloadbuilder.EmptyPayloadBuilder;
import csi.map.controller.payloadbuilder.HeatMapPayloadBuilder;
import csi.map.controller.payloadbuilder.PointPayloadBuilder;
import csi.map.controller.payloadbuilder.SummaryPointPayloadBuilder;
import csi.map.controller.payloadbuilder.TrackPointPayloadBuilder;
import csi.map.controller.payloadbuilder.TrackSummaryPointPayloadBuilder;
import csi.server.business.service.MapActionsService;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.BundleMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapCacheNotAvailable;
import csi.server.business.visualization.map.MapCacheStaleException;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapNode;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.common.model.UUID;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.ExtentInfo;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.HeatMapInfo;
import csi.server.common.model.map.SpatialReference;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.task.TaskConstants;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskSession;
import csi.server.task.core.TaskGroupId;
import csi.server.ws.async.HttpTaskSession;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.UBox;

@Controller
@RequestMapping("/map")
public class JSONController {
   private static final Logger LOG = LogManager.getLogger(JSONController.class);

    private Payload getPayload(String dataViewUuid, String mapViewDefUuid, Integer rangeStart, Integer rangeEnd, Integer rangeSize, Integer sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Payload payload;

        fillCacheWithMapContext(dataViewUuid, mapViewDefUuid, request, response);
        try {
            Extent extent = getExtent(dataViewUuid, mapViewDefUuid, request, response);
            boolean mapCacheNotAvailable = loadMapCacheIfNeeded4(dataViewUuid, mapViewDefUuid, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response, extent);
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            payload = buildPayload(mapCacheHandler, rangeStart, rangeEnd, rangeSize, request, response, extent, mapCacheNotAvailable);
        } catch (BuildSummaryCacheCancelled e) {
            payload = null;
        } catch (MapCacheStaleException e) {
            payload = new Payload();
            payload.setDeferToNewCache();
        }

        return payload;
    }

    private Payload getPayload3(String dataViewUuid, String mapViewDefUuid, Integer rangeStart, Integer rangeEnd, Integer rangeSize, Integer sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Payload payload;

        fillCacheWithMapContext(dataViewUuid, mapViewDefUuid, request, response);
        try {
            Extent extent = getCurrentExtent(dataViewUuid, mapViewDefUuid, request, response);
            boolean mapCacheNotAvailable = loadMapCacheIfNeeded4(dataViewUuid, mapViewDefUuid, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response, extent);
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            payload = buildPayload(mapCacheHandler, rangeStart, rangeEnd, rangeSize, request, response, extent, mapCacheNotAvailable);
        } catch (BuildSummaryCacheCancelled e) {
            payload = null;
        } catch (MapCacheStaleException e) {
            payload = new Payload();
            payload.setDeferToNewCache();
        }

        return payload;
    }

    private boolean loadMapCacheIfNeeded4(String dataViewUuid, String mapViewDefUuid, Integer rangeStart, Integer rangeEnd, Integer rangeSize, Integer sequenceNumber, HttpServletRequest request, HttpServletResponse response, Extent extent) {
        boolean mapCacheNotAvailable;
        try {
            loadMapCacheIfNeeded2(dataViewUuid, mapViewDefUuid, extent, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            if (mapCacheHandler.isPlaceLimitOrTrackTypeLimitReached()) {
                mapCacheNotAvailable = true;
            } else {
                mapCacheNotAvailable = mapCacheHandler.isMapCacheNotAvailable();
            }
        } catch (MapCacheNotAvailable | MapCacheStaleException e) {
            mapCacheNotAvailable = true;
        }
        return mapCacheNotAvailable;
    }

    private Payload buildPayload(MapCacheHandler mapCacheHandler, Integer rangeStart, Integer rangeEnd, Integer rangeSize, HttpServletRequest request, HttpServletResponse response, Extent extent, boolean mapCacheNotAvailable) {
        Payload payload;

        try {
            AbstractPayloadBuilder payloadBuilder = getPayloadBuilder(mapCacheHandler, extent, rangeStart, rangeEnd, rangeSize, mapCacheNotAvailable, request, response);
            payloadBuilder.build();
            payload = payloadBuilder.getPayload();
        } catch (MapCacheStaleException e) {
            payload = new Payload();
            payload.setDeferToNewCache();
        }

        return payload;
    }

    private Payload getPayload2(String dataViewUuid, String mapViewDefUuid, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize, Integer sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Payload payload;

        fillCacheWithMapContext(dataViewUuid, mapViewDefUuid, request, response);
        try {
            Extent extent = getExtent(dataViewUuid, mapViewDefUuid, request, response);
            boolean mapCacheNotAvailable = loadMapCacheIfNeeded5(dataViewUuid, mapViewDefUuid, mapSummaryExtent, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            payload = buildPayload(mapCacheHandler, mapSummaryExtent, rangeStart, rangeEnd, rangeSize, request, response, extent, mapCacheNotAvailable);
        } catch (BuildSummaryCacheCancelled e) {
            payload = null;
        } catch (MapCacheStaleException e) {
            payload = new Payload();
            payload.setDeferToNewCache();
        }

        return payload;
    }

    private boolean loadMapCacheIfNeeded5(String dataViewUuid, String mapViewDefUuid, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize, Integer sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        boolean mapCacheNotAvailable;
        try {
            loadMapCacheIfNeeded3(dataViewUuid, mapViewDefUuid, mapSummaryExtent, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
            MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
            if (mapCacheHandler.isPlaceLimitOrTrackTypeLimitReached()) {
                mapCacheNotAvailable = true;
            } else {
                mapCacheNotAvailable = mapCacheHandler.isMapCacheNotAvailable();
            }
        } catch (MapCacheNotAvailable | MapCacheStaleException e) {
            mapCacheNotAvailable = true;
        }
        return mapCacheNotAvailable;
    }

    private Payload buildPayload(MapCacheHandler mapCacheHandler, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize, HttpServletRequest request, HttpServletResponse response, Extent extent, boolean mapCacheNotAvailable) {
        Payload payload;

        try {
            AbstractPayloadBuilder payloadBuilder = getPayloadBuilder(mapCacheHandler, extent, rangeStart, rangeEnd, rangeSize, mapSummaryExtent, mapCacheNotAvailable, request, response);
            payloadBuilder.build();
            payload = payloadBuilder.getPayload();
        } catch (MapCacheStaleException e) {
            payload = new Payload();
            payload.setDeferToNewCache();
        }


        return payload;
    }

    private AbstractPayloadBuilder getPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent, Integer rangeStart, Integer rangeEnd, Integer rangeSize, boolean mapCacheNotAvailable, HttpServletRequest request, HttpServletResponse response) {
        AbstractPayloadBuilder payloadBuilder;

        if (!mapCacheHandler.usingLatestMapCache()) {
            payloadBuilder = new DeferToNewCachePayloadBuilder();
        } else if (mapCacheNotAvailable) {
            if ((extent != null) && mapCacheHandler.isCurrentlyAtDetailLevel()) {
                MapSummaryExtent mapSummaryExtent = new MapSummaryExtent(extent.getXmin(), extent.getYmin(), extent.getXmax(), extent.getYmax());
                payloadBuilder = new EmptyPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            } else {
               payloadBuilder = new EmptyPayloadBuilder(mapCacheHandler, extent);
            }
        } else if (mapCacheHandler.isUseTrackMap()) {
            if (mapCacheHandler.isCurrentlyAtDetailLevel()) {
                if (extent != null) {
                    MapSummaryExtent mapSummaryExtent = new MapSummaryExtent(extent.getXmin(), extent.getYmin(), extent.getXmax(), extent.getYmax());
                    payloadBuilder = new TrackPointPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
                } else {
                    payloadBuilder = new TrackPointPayloadBuilder(mapCacheHandler, null);
                }
            } else {
                payloadBuilder = new TrackSummaryPointPayloadBuilder(mapCacheHandler, extent);
            }
        } else if (mapCacheHandler.isBundleUsed()) {
            BundlePayloadLoader bundlePayloadLoader = new BundlePayloadLoader(mapCacheHandler, extent);
            payloadBuilder = getBundlePayloadBuilder(mapCacheHandler.getDvUuid(), mapCacheHandler.getVizUuid(), request, response, bundlePayloadLoader);
        } else if (mapCacheHandler.isUseHeatMap()) {
            HeatMapPayloadBuilder heatmapPayloadBuilder = new HeatMapPayloadBuilder(mapCacheHandler, extent);
            HeatMapInfo heatMapInfo = getHeatMapInfo(mapCacheHandler.getDvUuid(), mapCacheHandler.getVizUuid(), request, response);
            heatmapPayloadBuilder.setHeatMapInfo(heatMapInfo);
            payloadBuilder = heatmapPayloadBuilder;
        } else {
            if ((extent != null) && mapCacheHandler.isCurrentlyAtDetailLevel()) {
                MapSummaryExtent mapSummaryExtent = new MapSummaryExtent(extent.getXmin(), extent.getYmin(), extent.getXmax(), extent.getYmax());
                payloadBuilder = new PointPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            } else {
                payloadBuilder = new SummaryPointPayloadBuilder(mapCacheHandler, extent);
            }
        }
        return payloadBuilder;
    }

    private AbstractPayloadBuilder getBundlePayloadBuilder(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response, BundlePayloadLoader bundlePayloadLoader) {
        AbstractPayloadBuilder payloadBuilder;
        List<Crumb> breadcrumb = getBreadcrumb(dataViewUuid, mapViewDefUuid, request, response);
        bundlePayloadLoader.setBreadcrumb(breadcrumb);
        boolean showLeaves = isShowLeaves(dataViewUuid, mapViewDefUuid, request, response);
        bundlePayloadLoader.setShowLeaves(showLeaves);
        payloadBuilder = bundlePayloadLoader;
        return payloadBuilder;
    }

    private AbstractPayloadBuilder getPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent, Integer rangeStart, Integer rangeEnd, Integer rangeSize, MapSummaryExtent mapSummaryExtent, boolean mapCacheNotAvailable, HttpServletRequest request, HttpServletResponse response) {
        AbstractPayloadBuilder payloadBuilder;
        if (!mapCacheHandler.usingLatestMapCache()) {
            payloadBuilder = new DeferToNewCachePayloadBuilder();
        } else if (mapCacheNotAvailable) {
            payloadBuilder = new EmptyPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
        } else if (mapCacheHandler.isUseTrackMap()) {
            if (mapCacheHandler.isCurrentlyAtDetailLevel()) {
                payloadBuilder = new TrackPointPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            } else {
                payloadBuilder = new TrackSummaryPointPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            }
        } else if (mapCacheHandler.isBundleUsed()) {
            BundlePayloadLoader bundlePayloadLoader = new BundlePayloadLoader(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            payloadBuilder = getBundlePayloadBuilder(mapCacheHandler.getDvUuid(), mapCacheHandler.getVizUuid(), request, response, bundlePayloadLoader);
        } else if (mapCacheHandler.isUseHeatMap()) {
            HeatMapPayloadBuilder heatmapPayloadBuilder = new HeatMapPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            HeatMapInfo heatMapInfo = getHeatMapInfo(mapCacheHandler.getDvUuid(), mapCacheHandler.getVizUuid(), request, response);
            heatmapPayloadBuilder.setHeatMapInfo(heatMapInfo);
            payloadBuilder = heatmapPayloadBuilder;
        } else {
            if (mapCacheHandler.isCurrentlyAtDetailLevel()) {
               payloadBuilder = new PointPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            } else {
                payloadBuilder = new SummaryPointPayloadBuilder(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            }
        }
        return payloadBuilder;
    }

    private Extent getExtent(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        Extent extent = null;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("getExtent", String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            extent = (Extent) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("getExtent", e);
        }
        return extent;
    }

    private Extent getCurrentExtent(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        Extent extent = null;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("getCurrentExtent", String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            extent = (Extent) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("getExtent", e);
        }
        return extent;
    }

    private HeatMapInfo getHeatMapInfo(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        HeatMapInfo heatMapInfo = null;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("getHeatMapInfo", String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            heatMapInfo = (HeatMapInfo) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("getHeatMapInfo", e);
        }
        return heatMapInfo;
    }

    private void fillCacheWithMapContext(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("fillCacheWithMapContext", String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("fillCacheWithMapContext", e);
        }
    }

    private void loadMapCacheIfNeeded(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Extent extent = getExtent(dataViewUuid, mapViewDefUuid, request, response);
        Integer rangeStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
        Integer rangeEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);
        List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = MapCacheUtil.getSeriesValues(mapViewDefUuid);
        Integer rangeSize = null;
        if (seriesValues != null) {
            rangeSize = seriesValues.size();
        }
        loadMapCacheIfNeeded2(dataViewUuid, mapViewDefUuid, extent, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
    }

    private void loadMapCacheIfNeeded2(String dataViewUuid, String mapViewDefUuid, Extent extent, Integer rangeStart, Integer rangeEnd, Integer rangeSize, Integer sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        if (extent == null) {
         loadMapCacheIfNeeded3(dataViewUuid, mapViewDefUuid, null, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
      } else {
            MapSummaryExtent mapSummaryExtent = new MapSummaryExtent(extent.getXmin(), extent.getYmin(), extent.getXmax(), extent.getYmax());
            loadMapCacheIfNeeded3(dataViewUuid, mapViewDefUuid, mapSummaryExtent, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
        }
    }

    private void loadMapCacheIfNeeded3(String dataViewUuid, String mapViewDefUuid, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize, Integer sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        boolean homeLoaded = MapCacheUtil.isHomeLoaded(mapViewDefUuid);
        MapSettingsDTO mapSettings = MapCacheUtil.getMapSettings(mapViewDefUuid);
        boolean mapSettingsNotNull = mapSettings != null;
        if (homeLoaded && mapSettingsNotNull && mapSettings.isUseHeatMap()) {
            MapCacheUtil.setUseHome(mapViewDefUuid, true);
        } else if (homeLoaded && mapSettingsNotNull && !mapSettings.isBundleUsed()) {
            boolean isMapSummaryAlreadyBuilt = mapSummaryAlreadyBuilt(mapViewDefUuid, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            if (!isMapSummaryAlreadyBuilt) {
                if (mapSummaryExtent == null) {
                  MapCacheUtil.invalidateMapSummaryExtent(mapViewDefUuid);
               } else {
                  MapCacheUtil.addMapSummaryExtent(mapViewDefUuid, mapSummaryExtent);
               }
                UBox uBox = new UBox(mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
                buildSummaryCache(dataViewUuid, mapViewDefUuid, sequenceNumber, uBox, request, response);
            }
        } else {
            loadMapCache(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
            try {
                MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
                mapCacheHandler.initializeRangeInfo();
            } catch (MapCacheStaleException ignored) {
            }
        }
    }

    private boolean mapSummaryAlreadyBuilt(String mapViewDefUuid, MapSummaryExtent newMapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
        if (shouldUseHome(newMapSummaryExtent, mapViewDefUuid, rangeStart, rangeEnd, rangeSize)) {
            Integer coarsestMapSummaryPrecision = MapCacheUtil.getCoarsestMapSummaryPrecision(mapViewDefUuid);
            MapCacheUtil.setCurrentMapSummaryPrecision(mapViewDefUuid, coarsestMapSummaryPrecision);
            MapCacheUtil.setUseHome(mapViewDefUuid, true);
            return true;
        } else if (isRangeChanged(mapViewDefUuid, rangeStart, rangeEnd, rangeSize)) {
            return false;
        } else {
            MapSummaryExtent oldMapSummaryExtent = MapCacheUtil.getMapSummaryExtent(mapViewDefUuid);
            if (oldMapSummaryExtent == null) {
                return false;
            } else {
                return oldMapSummaryExtent.equals(newMapSummaryExtent);
            }
        }
    }

    private boolean shouldUseHome(MapSummaryExtent newMapSummaryExtent, String mapViewDefUuid, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
        if (newMapSummaryExtent == null) {
            return true;
        } else {
            Extent initialExtent = MapCacheUtil.getInitialExtent(mapViewDefUuid);
            if (initialExtent != null) {
                double initialXMin = initialExtent.getXmin();
                double initialXMax = initialExtent.getXmax();
                double initialYMin = initialExtent.getYmin();
                double initialYMax = initialExtent.getYmax();
                double newXMin = newMapSummaryExtent.getXMin();
                double newXMax = newMapSummaryExtent.getXMax();
                double newYMin = newMapSummaryExtent.getYMin();
                double newYMax = newMapSummaryExtent.getYMax();
                boolean newContainsInitialOnX = (newXMin <= initialXMin) && (initialXMax <= newXMax);
                boolean newSpansWorldOnX = Math.abs(newXMax - newXMin - 360) < 0.00001;
                boolean newContainsInitialOnY = (newYMin <= initialYMin) && (initialYMax <= newYMax);
                return (newContainsInitialOnX || newSpansWorldOnX) && newContainsInitialOnY && ((rangeStart == null) || (rangeEnd == null) || (rangeSize == null) || ((rangeStart == 0) && (rangeEnd == (rangeSize - 1))));
            }
        }
        return false;
    }

    private void loadMapCache(String dataViewUuid, String mapViewDefUuid, int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        TaskContext taskContext = createTaskContext(request, response);
        taskContext.setExecutionPoolId(dataViewUuid);
        try {
            taskContext.setMethod(MapActionsService.class.getMethod("loadMapCache", String.class, String.class, Integer.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, sequenceNumber};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (NoSuchMethodException e) {
            LOG.debug("loadMapCache", e);
        } catch (Exception e) {
            LOG.debug(e.getMessage());
        }
    }

    private TaskContext createTaskContext(HttpServletRequest request, HttpServletResponse response) {
        TaskSession taskSession = new HttpTaskSession(request.getSession());

        String clientId = UUID.randomUUID();
        String taskId = UUID.randomUUID();

        // Create and set the TaskContext
        TaskContext taskContext = new TaskContext(clientId, taskId, new TaskGroupId(), taskSession);
        taskContext.setAdminTask(false);
        taskContext.setInterruptable(true);
        taskContext.setServicePath(null);
        taskContext.setCodec(null);
        taskContext.setType(TaskConstants.TASK_TYPE_REST);
        taskContext.setGwtService(false);
        taskContext.setServiceClass(MapActionsService.class);
        taskContext.setSynchronous(true);
        taskContext.setSynchronousHttpResponse(response);
        return taskContext;
    }

    private void buildSummaryCache(String dataViewUuid, String mapViewDefUuid, Integer sequenceNumber, UBox uBox, HttpServletRequest request, HttpServletResponse response) {
        TaskContext taskContext = createTaskContext(request, response);
        taskContext.setExecutionPoolId(dataViewUuid);
        try {
            taskContext.setMethod(MapActionsService.class.getMethod("buildSummaryCache", String.class, String.class, Integer.class, UBox.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, sequenceNumber, uBox};
            taskContext.setMethodArgs(parameters);
            TaskController taskController = TaskController.getInstance();
            taskController.submitTask(taskContext);
            String status = (String) taskContext.getStatus().getResultData();
            if (status.equals("cancelled")) {
                throw new BuildSummaryCacheCancelled();
            } else if (status.equals("MapCacheNotAvailable")) {
                throw new MapCacheNotAvailable();
            }
        } catch (NoSuchMethodException e) {
            LOG.debug("buildSummaryCache", e);
        } catch (Exception e) {
            LOG.debug(e.getMessage());
        }
    }

    @RequestMapping(value = "getUpdate/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.GET)
    public @ResponseBody
    Payload getLayerUpdateInJSON(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Payload payload = null;

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                Integer rangeStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
                Integer rangeEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);
                List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = MapCacheUtil.getSeriesValues(mapViewDefUuid);
                Integer rangeSize = null;
                if (seriesValues != null) {
                  rangeSize = seriesValues.size();
               }
                payload = getPayload(dataViewUuid, mapViewDefUuid, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
                if (payload != null) {
                  payload.setSequenceNumber(sequenceNumber);
               }
            } catch (Exception e) {
                LOG.debug("getLayerUpdateInJSON", e);
            }
        }

        return payload;
    }

    @RequestMapping(value = "getUpdate2/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    Payload getLayerUpdate2InJSON(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Payload payload = null;

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                String[] doubleStrings = om.readValue(request.getInputStream(), String[].class);
                List<Double> doubles = new ArrayList<Double>();
                try {
                    for (String doubleString : doubleStrings) {
                        Double value = Double.parseDouble(doubleString);
                        if (value.isNaN()) {
                            LOG.debug("getLayerUpdate2InJSON value NaN");
                            doubles.clear();
                            break;
                        }
                        doubles.add(value);
                    }
                } catch (NullPointerException npe) {
                    LOG.debug("getLayerUpdate2InJSON", npe);
                    doubles.clear();
                }

                Integer rangeStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
                Integer rangeEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);
                List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = MapCacheUtil.getSeriesValues(mapViewDefUuid);
                Integer rangeSize = null;
                if (seriesValues != null) {
                  rangeSize = seriesValues.size();
               }

                if (doubles.size() == 4) {
                    MapSummaryExtent mapSummaryExtent = new MapSummaryExtent(doubles.get(0), doubles.get(1), doubles.get(2), doubles.get(3));
                    payload = getPayload2(dataViewUuid, mapViewDefUuid, mapSummaryExtent, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
                } else {
                  payload = getPayload(dataViewUuid, mapViewDefUuid, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
               }
            } catch (Exception e) {
                LOG.debug("getLayerUpdateInJSON", e);
            }
            if (payload != null) {
               payload.setSequenceNumber(sequenceNumber);
            }
        }

        return payload;
    }

    @RequestMapping(value = "getUpdate3/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.GET)
    public @ResponseBody
    Payload getLayerUpdateInJSON3(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Payload payload = null;

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                Integer rangeStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
                Integer rangeEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);
                List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = MapCacheUtil.getSeriesValues(mapViewDefUuid);
                Integer rangeSize = null;
                if (seriesValues != null) {
                  rangeSize = seriesValues.size();
               }
                payload = getPayload3(dataViewUuid, mapViewDefUuid, rangeStart, rangeEnd, rangeSize, sequenceNumber, request, response);
                if (payload != null) {
                  payload.setSequenceNumber(sequenceNumber);
               }
            } catch (Exception e) {
                LOG.debug("getLayerUpdateInJSON", e);
            }
        }

        return payload;
    }

    @RequestMapping(value = "setSelected/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    SelectionResponse setSelected(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        SelectionResponse selectionResponse = new SelectionResponse();

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                NodesAndLinks nodesAndLinks = om.readValue(request.getInputStream(), NodesAndLinks.class);
                String[] nodes = nodesAndLinks.getNodes();
                String[] links = nodesAndLinks.getLinks();

                if (MapCacheUtil.isUseTrack(mapViewDefUuid)) {
                    Boolean changesMade = selectTrackLinks(dataViewUuid, mapViewDefUuid, nodes, links, request, response);

                    if ((changesMade != null) && changesMade.booleanValue()) {
                        if (MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid)) {
                            populateLinkSelectionResponse(mapViewDefUuid, selectionResponse);
                        } else {
                            selectionResponse.setNumNodeChanged(nodes.length);
                        }
                    }
                } else {
                    if (nodes.length > 0) {
                        selectNodes(dataViewUuid, mapViewDefUuid, nodes, request, response);
                        selectionResponse.setNumNodeChanged(nodes.length);
                    }
                    if (links.length > 0) {
                        Boolean changesMade = selectLinks2(dataViewUuid, mapViewDefUuid, links, request, response);

                        if ((changesMade != null) && changesMade.booleanValue()) {
                            populateLinkSelectionResponse(mapViewDefUuid, selectionResponse);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.debug("setSelected", e);
            }
        }

        return selectionResponse;
    }

    private Boolean selectTrackLinks(String dataViewUuid, String mapViewDefUuid, String[] nodes, String[] links, HttpServletRequest request, HttpServletResponse response) {
        Boolean retVal = false;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("selectTrackLinks", String.class, String.class, String[].class, String[].class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, nodes, links};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            retVal = (Boolean) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("selectLinks2", e);
        }
        return retVal;
    }

    private void populateLinkSelectionResponse(@PathVariable String mapViewDefUuid, SelectionResponse selectionResponse) {
        Association selected = new Association(-1);
        collectSelectedLinks(mapViewDefUuid, selected);
        selectionResponse.setLinkResponse(selected);
    }

    private void selectNodes(String dataViewUuid, String mapViewDefUuid, String[] ids, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("selectNodes", String.class, String.class, String[].class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, ids};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("selectNodes", e);
        }
    }

    @RequestMapping(value = "setDeselected/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    SelectionResponse setDeselected(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        SelectionResponse selectionResponse = new SelectionResponse();

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                NodesAndLinks nodesAndLinks = om.readValue(request.getInputStream(), NodesAndLinks.class);
                String[] nodes = nodesAndLinks.getNodes();
                if (nodes.length > 0) {
                    deselectNodes(dataViewUuid, mapViewDefUuid, nodes, request, response);
                    selectionResponse.setNumNodeChanged(nodes.length);
                }
                String[] links = nodesAndLinks.getLinks();

                if (links.length > 0) {
                    Boolean changesMade = deselectLinks2(dataViewUuid, mapViewDefUuid, nodesAndLinks.getLinks(), request, response);

                    if ((changesMade != null) && changesMade.booleanValue()) {
                        populateLinkSelectionResponse(mapViewDefUuid, selectionResponse);
                    }
                } else if (MapCacheUtil.isUseTrack(mapViewDefUuid) && (nodes.length > 0)) {
                    if (MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid)) {
                        populateLinkSelectionResponse(mapViewDefUuid, selectionResponse);
                    } else {
                        selectionResponse.setNumNodeChanged(nodes.length);
                    }
                }
            } catch (Exception e) {
                LOG.debug("setDeselected", e);
            }
        }

        return selectionResponse;
    }

    private void deselectNodes(String dataViewUuid, String mapViewDefUuid, String[] ids, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("deselectNodes", String.class, String.class, String[].class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, ids};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("deselectNodes", e);
        }
    }

    @RequestMapping(value = "setNodeSelected/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String setNodeSelected(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                String[] ids = om.readValue(request.getInputStream(), String[].class);
                selectNodes(dataViewUuid, mapViewDefUuid, ids, request, response);
                status = Integer.toString(ids.length);
            } catch (Exception e) {
                LOG.debug("setNodeSelected", e);
            }
        }

        return status;
    }

    @RequestMapping(value = "toggleSelected/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    SelectionResponse toggleSelected(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        SelectionResponse selectionResponse = new SelectionResponse();

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                NodesAndLinks nodesAndLinks = om.readValue(request.getInputStream(), NodesAndLinks.class);
                String[] nodes = nodesAndLinks.getNodes();
                String[] links = nodesAndLinks.getLinks();
                if (MapCacheUtil.isUseTrack(mapViewDefUuid)) {
                    if (nodes.length > 0) {
                        if (toggleTrackNodes(dataViewUuid, mapViewDefUuid, nodes, request, response).booleanValue()) {
                            if (MapCacheUtil.isCurrentlyAtDetailLevel(mapViewDefUuid)) {
                                populateLinkSelectionResponse(mapViewDefUuid, selectionResponse);
                            } else {
                                selectionResponse.setNumNodeChanged(nodes.length);
                                selectionResponse.setNodeChangeType("reload");
                            }
                        }
                    } else if (links.length > 0) {
                        if (toggleSelectedLinks(dataViewUuid, mapViewDefUuid, links, request, response).booleanValue()) {
                            populateLinkSelectionResponse(mapViewDefUuid, selectionResponse);
                        }
                    }
                } else {
                    if (nodes.length > 0) {
                        String status = toggleSelectedNodes(dataViewUuid, mapViewDefUuid, nodes, request, response);
                        if (!status.isEmpty()) {
                            selectionResponse.setNumNodeChanged(nodes.length);
                            selectionResponse.setNodeChangeType(status);
                        }
                    } else if (links.length > 0) {
                        Boolean changesMade = toggleSelectedLinks(dataViewUuid, mapViewDefUuid, links, request, response);

                        if ((changesMade != null) && changesMade.booleanValue()) {
                            populateLinkSelectionResponse(mapViewDefUuid, selectionResponse);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.debug("toggleSelected2", e);
            }
        }

        return selectionResponse;
    }

    private String toggleSelectedNodes(String dataViewUuid, String mapViewDefUuid, String[] ids, HttpServletRequest request, HttpServletResponse response) {
        String retVal = "";
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("toggleSelectedNodes", String.class, String.class, String[].class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, ids};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            retVal = (String) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("toggleSelectedNodes", e);
        }
        return retVal;
    }

    private Boolean toggleTrackNodes(String dataViewUuid, String mapViewDefUuid, String[] ids, HttpServletRequest request, HttpServletResponse response) {
        Boolean retVal = false;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("toggleTrackNodes", String.class, String.class, String[].class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, ids};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            retVal = (Boolean) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("toggleSelectedNodes", e);
        }
        return retVal;
    }

    private void collectSelectedLinks(@PathVariable String mapViewDefUuid, Association selected) {
        AbstractMapSelection mapSelection = MapCacheUtil.getMapSelection(mapViewDefUuid);
        if (mapSelection != null) {
            Map<Geometry, AugmentedMapNode> mapNodeByGeometryMap = MapCacheUtil.getMapNodeByGeometryMap(mapViewDefUuid);
            if (mapNodeByGeometryMap != null) {
                mapSelection.getLinks().forEach(linkGeometry -> {
                    long sourceNodeId = getNodeId(mapNodeByGeometryMap, linkGeometry.getNode1Geometry());
                    long destinationNodeId = getNodeId(mapNodeByGeometryMap, linkGeometry.getNode2Geometry());
                    if ((sourceNodeId != -1) && (destinationNodeId != -1)) {
                     selected.addSegment(sourceNodeId, destinationNodeId);
                  }
                });
            }
        }
    }

    private Boolean toggleSelectedLinks(String dataViewUuid, String mapViewDefUuid, String[] ids, HttpServletRequest request, HttpServletResponse response) {
        Boolean retVal = false;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("toggleSelectedLinks", String.class, String.class, String[].class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, ids};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            retVal = (Boolean) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("toggleSelectedLinks", e);
        }
        return retVal;
    }

    @RequestMapping(value = "selectLinks/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    Association selectLinks(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        final Association selected = new Association(-1);
        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                String[] ids = om.readValue(request.getInputStream(), String[].class);
                Boolean changesMade = selectLinks2(dataViewUuid, mapViewDefUuid, ids, request, response);

                if ((changesMade != null) && changesMade.booleanValue()) {
                    collectSelectedLinks(mapViewDefUuid, selected);
                }
            } catch (Exception e) {
                LOG.debug("selectLinks", e);
            }
        }
        return selected;
    }

    private Boolean selectLinks2(String dataViewUuid, String mapViewDefUuid, String[] ids, HttpServletRequest request, HttpServletResponse response) {
        Boolean retVal = false;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("selectLinks2", String.class, String.class, String[].class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, ids};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            retVal = (Boolean) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("selectLinks2", e);
        }
        return retVal;
    }

    @RequestMapping(value = "deselectLinks/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    Association deselectLinks(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        final Association selected = new Association(-1);
        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                String[] ids = om.readValue(request.getInputStream(), String[].class);
                Boolean changesMade = deselectLinks2(dataViewUuid, mapViewDefUuid, ids, request, response);

                if ((changesMade != null) && changesMade.booleanValue()) {
                    collectSelectedLinks(mapViewDefUuid, selected);
                }
            } catch (Exception e) {
                LOG.debug("deselectLinks", e);
            }
        }
        return selected;
    }

    private Boolean deselectLinks2(String dataViewUuid, String mapViewDefUuid, String[] ids, HttpServletRequest request, HttpServletResponse response) {
        Boolean retVal = false;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("deselectLinks2", String.class, String.class, String[].class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, ids};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            retVal = (Boolean) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("deselectLinks2", e);
        }
        return retVal;
    }

    private long getNodeId(Map<Geometry, AugmentedMapNode> mapNodeByGeometryMap, Geometry geometry) {
        MapNode mapNode = mapNodeByGeometryMap.get(geometry);
        if (mapNode == null) {
            return -1;
        } else {
            return mapNode.getNodeId();
        }
    }

    @RequestMapping(value = "setNodeDeselected/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String setNodeDeselected(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                String[] ids = om.readValue(request.getInputStream(), String[].class);
                deselectNodes(dataViewUuid, mapViewDefUuid, ids, request, response);
                status = Integer.toString(ids.length);
            } catch (Exception e) {
                LOG.debug("setNodeDeselected", e);
            }
        }

        return status;
    }

    @RequestMapping(value = "getTooltip/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}/{strId}", method = RequestMethod.GET)
    public @ResponseBody
    String getTooltip(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, @PathVariable String strId, HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        String tooltip = "{}";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                long id = Long.parseLong(strId);
                TaskContext taskContext = createTaskContext(request, response);
                taskContext.setMethod(MapActionsService.class.getMethod("getTooltip", String.class, String.class, Integer.class, Long.class));
                Object[] parameters = {dataViewUuid, mapViewDefUuid, sequenceNumber, id};
                taskContext.setMethodArgs(parameters);
                TaskController.getInstance().submitTask(taskContext);
                if (taskContext.getStatus().getResultData() instanceof String) {
                  tooltip = (String) taskContext.getStatus().getResultData();
               }
            } catch (Exception e) {
                LOG.debug("getTooltip", e);
            }
        }

        return tooltip;
    }

    @RequestMapping(value = "setExtent/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String setExtent(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                Extent initialExtent = MapCacheUtil.getInitialExtent(mapViewDefUuid);
                if (initialExtent != null) {
                    String requestString = getRequestString(request);
                    Extent extent = extractExtent(requestString);
                    LOG.info("setExtent: extent: " + extent);
                    setExtent(dataViewUuid, mapViewDefUuid, extent, request, response);
                    status = "success";
                }
            } catch (Exception e) {
                LOG.debug("setExtent", e);
            }
        }

        return status;
    }

    private void setExtent(String dataViewUuid, String mapViewDefUuid, Extent extent, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("setExtent", String.class, String.class, Extent.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, extent};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("setExtent", e);
        }
    }

    private Extent extractExtent(String requestString) {
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
                default:
                    break;
            }
        }
        return extent;
    }

    private String getRequestString(HttpServletRequest request) throws IOException {
        return IOUtils.toString(request.getReader());
    }

    @RequestMapping(value = "deselectAll/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String deselectAll(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                deselectAll(dataViewUuid, mapViewDefUuid, request, response);
                status = "success";
            } catch (Exception e) {
                LOG.debug("deselectAll", e);
            }
        }

        return status;
    }

    private void deselectAll(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("deselectAll", String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("deselectAll", e);
        }
    }

    @RequestMapping(value = "selectAll/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String selectAll(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                selectAll(dataViewUuid, mapViewDefUuid, request, response);
                status = "success";
            } catch (Exception e) {
                LOG.debug("selectAll", e);
            }
        }

        return status;
    }

    private void selectAll(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("selectAll", String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("selectAll", e);
        }
    }

    @RequestMapping(value = "combinedPlaceClicked/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}/{operation}", method = RequestMethod.POST)
    public @ResponseBody
    String combinedPlaceClicked(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, @PathVariable String operation, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                toggleCombinedPlaceSelection(dataViewUuid, mapViewDefUuid, operation, request, response);
                status = "success";
            } catch (Exception e) {
                LOG.debug("combinedPlaceClicked", e);
            }
        }

        return status;
    }

    private void toggleCombinedPlaceSelection(String dataViewUuid, String mapViewDefUuid, String operation, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("toggleCombinedPlaceSelection", String.class, String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, operation};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("toggleCombinedPlaceSelection", e);
        }
    }

    @RequestMapping(value = "newPlaceClicked/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}/{operation}", method = RequestMethod.POST)
    public @ResponseBody
    String newPlaceClicked(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, @PathVariable String operation, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                toggleNewPlaceSelection(dataViewUuid, mapViewDefUuid, operation, request, response);
                status = "success";
            } catch (Exception e) {
                LOG.debug("newPlaceClicked", e);
            }
        }

        return status;
    }

    private void toggleNewPlaceSelection(String dataViewUuid, String mapViewDefUuid, String operation, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("toggleNewPlaceSelection", String.class, String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, operation};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("toggleNewPlaceSelection", e);
        }
    }

    @RequestMapping(value = "updatedPlaceClicked/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}/{operation}", method = RequestMethod.POST)
    public @ResponseBody
    String updatedPlaceClicked(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, @PathVariable String operation, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                toggleUpdatedPlaceSelection(dataViewUuid, mapViewDefUuid, operation, request, response);
                status = "success";
            } catch (Exception e) {
                LOG.debug("updatedPlaceClicked", e);
            }
        }

        return status;
    }

    private void toggleUpdatedPlaceSelection(String dataViewUuid, String mapViewDefUuid, String operation, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("toggleUpdatedPlaceSelection", String.class, String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, operation};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("toggleUpdatedPlaceSelection", e);
        }
    }

    @RequestMapping(value = "associationClicked/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String associationClicked(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                SelectOperation selectOperation = om.readValue(request.getInputStream(), SelectOperation.class);
                toggleAssociationSelectionByType(dataViewUuid, mapViewDefUuid, selectOperation.getTypename(), selectOperation.getOperation(), request, response);
                status = "success";
            } catch (Exception e) {
                LOG.debug("associationClicked", e);
            }
        }

        return status;
    }

    private void toggleAssociationSelectionByType(String dataViewUuid, String mapViewDefUuid, String associationKey, String operation, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("toggleAssociationSelectionByType", String.class, String.class, String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, associationKey, operation};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("toggleAssociationSelectionByType", e);
        }
    }

    @RequestMapping(value = "placeClicked/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String placeClicked(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                SelectOperation selectOperation = om.readValue(request.getInputStream(), SelectOperation.class);
                togglePlaceSelectionByType(dataViewUuid, mapViewDefUuid, selectOperation.getId(), selectOperation.getTypename(), selectOperation.getOperation(), request, response);
                status = "success";
            } catch (Exception e) {
                LOG.debug("placeClicked", e);
            }
        }

        return status;
    }

    @RequestMapping(value = "trackClicked/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String trackClicked(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                ObjectMapper om = new ObjectMapper();
                SelectOperation selectOperation = om.readValue(request.getInputStream(), SelectOperation.class);
                toggleTrackSelectionByType(dataViewUuid, mapViewDefUuid, selectOperation.getId(), selectOperation.getTypename(), selectOperation.getOperation(), request, response);
                status = "success";
            } catch (Exception e) {
                LOG.debug("placeClicked", e);
            }
        }

        return status;
    }

    private void togglePlaceSelectionByType(String dataViewUuid, String mapViewDefUuid, int placeId, String typename, String operation, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("togglePlaceSelectionByType", String.class, String.class, Integer.class, String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, placeId, typename, operation};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("togglePlaceSelectionByType", e);
        }
    }

    private void toggleTrackSelectionByType(String dataViewUuid, String mapViewDefUuid, int trackId, String typename, String operation, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("toggleTrackSelectionByType", String.class, String.class, Integer.class, String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, trackId, typename, operation};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("toggleTrackSelectionByType", e);
        }
    }

    private String[] getSelectedNodes(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        String[] objectIDArray = null;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("getSelectedNodes", String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            objectIDArray = (String[]) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("getSelectedNodes", e);
        }
        return objectIDArray;
    }

    @RequestMapping(value = "setHeatmapBlurValue/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String setHeatmapBlur(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        updateMapSequenceNumber(mapViewDefUuid, sequenceNumber);

        try {
            loadMapCacheIfNeeded(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
            String requestString = getRequestString(request);
            setHeatmapBlurValue(dataViewUuid, mapViewDefUuid, Double.parseDouble(requestString), request, response);
            status = "success";
        } catch (Exception e) {
            LOG.debug("setHeatmapBlurValue", e);
        }

        return status;
    }

    private void updateMapSequenceNumber(@PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber) {
        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
        }
    }

    private void setHeatmapBlurValue(String dataViewUuid, String mapViewDefUuid, Double blurValue, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("setHeatmapBlurValue", String.class, String.class, Double.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, blurValue};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("setHeatmapBlurValue", e);
        }
    }

    @RequestMapping(value = "setHeatmapMaxValue/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String setHeatmapMaxValue(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        updateMapSequenceNumber(mapViewDefUuid, sequenceNumber);

        try {
            loadMapCacheIfNeeded(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
            String requestString = getRequestString(request);
            setHeatmapMaxValue(dataViewUuid, mapViewDefUuid, Double.parseDouble(requestString), request, response);
            status = "success";
        } catch (Exception e) {
            LOG.debug("setHeatmapMaxValue", e);
        }

        return status;
    }

    private void setHeatmapMaxValue(String dataViewUuid, String mapViewDefUuid, Double maxValue, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("setHeatmapMaxValue", String.class, String.class, Double.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, maxValue};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("setHeatmapMaxValue", e);
        }
    }

    @RequestMapping(value = "setHeatmapMinValue/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String setHeatmapMinValue(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        updateMapSequenceNumber(mapViewDefUuid, sequenceNumber);

        try {
            loadMapCacheIfNeeded(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
            String requestString = getRequestString(request);
            setHeatmapMinValue(dataViewUuid, mapViewDefUuid, Double.parseDouble(requestString), request, response);
            status = "success";
        } catch (Exception e) {
            LOG.debug("setHeatmapMinValue", e);
        }

        return status;
    }

    private void setHeatmapMinValue(String dataViewUuid, String mapViewDefUuid, Double minValue, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("setHeatmapMinValue", String.class, String.class, Double.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, minValue};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("setHeatmapMinValue", e);
        }
    }

    @RequestMapping(value = "setHeatmapValues/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String setHeatmapValues(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        updateMapSequenceNumber(mapViewDefUuid, sequenceNumber);

        try {
            loadMapCacheIfNeeded(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
            String requestString = getRequestString(request);
            Map<String, Double> heatmapValues = extractHeatmapValues(requestString);
            setHeatmapValues(dataViewUuid, mapViewDefUuid, heatmapValues.get("blur"), heatmapValues.get("max"), heatmapValues.get("min"), request, response);
            status = "success";
        } catch (Exception e) {
            LOG.debug("setHeatmapBlurValue", e);
        }

        return status;
    }

    private void setHeatmapValues(String dataViewUuid, String mapViewDefUuid, Double blurValue, Double maxValue, Double minValue, HttpServletRequest request, HttpServletResponse response) {
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("setHeatmapValues", String.class, String.class, Double.class, Double.class, Double.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, blurValue, maxValue, minValue};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
        } catch (Exception e) {
            LOG.debug("setHeatmapValues", e);
        }
    }

    private Map<String, Double> extractHeatmapValues(String requestString) {
        Map<String, Double> heatmapValues = new HashMap<String, Double>();
        String[] values = requestString.split("&");
        for (String value : values) {
            String[] subvalues = value.split("=");
            switch (subvalues[0]) {
                case "blur":
                    heatmapValues.put("blur", Double.parseDouble(subvalues[1]));
                    break;
                case "max":
                    heatmapValues.put("max", Double.parseDouble(subvalues[1]));
                    break;
                case "min":
                    heatmapValues.put("min", Double.parseDouble(subvalues[1]));
                    break;
                default:
                    break;
            }
        }
        return heatmapValues;
    }

    @RequestMapping(value = "drillOnBundle/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String drillOnBundle(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setExecutionPoolId(dataViewUuid);
            try {
                status = callMapActionsService(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response, taskContext, "drillOnBundle");
            } catch (Exception e) {
                LOG.debug("drillOnBundle", e);
            }
        }

        return status;
    }

    private String callMapActionsService(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response, TaskContext taskContext, String functionName) throws IOException, NoSuchMethodException {
        loadMapCacheIfNeeded(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
        String requestString = getRequestString(request);
        taskContext.setMethod(MapActionsService.class.getMethod(functionName, String.class, String.class, Integer.class, String.class));
        Object[] parameters = {dataViewUuid, mapViewDefUuid, sequenceNumber, requestString};
        taskContext.setMethodArgs(parameters);
        TaskController.getInstance().submitTask(taskContext);
        return "success";
    }

    @RequestMapping(value = "trimBreadcrumb/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String trimBreadcrumb(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setExecutionPoolId(dataViewUuid);
            try {
                status = callMapActionsService(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response, taskContext, "trimBreadcrumb");
            } catch (Exception e) {
                LOG.debug("trimBreadcrumb", e);
            }
        }

        return status;
    }

    @RequestMapping(value = "showLeavesOnBundle/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String showLeavesOnBundle(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setExecutionPoolId(dataViewUuid);
            try {
                status = callMapActionsService(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response, taskContext, "showLeavesOnBundle");
            } catch (Exception e) {
                LOG.debug("showLeavesOnBundle", e);
            }
        }

        return status;
    }

    @RequestMapping(value = "dontShowLeavesOnBundle/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.POST)
    public @ResponseBody
    String dontShowLeavesOnBundle(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        String status = "failure";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setExecutionPoolId(dataViewUuid);
            try {
                loadMapCacheIfNeeded(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
                taskContext.setMethod(MapActionsService.class.getMethod("dontShowLeavesOnBundle", String.class, String.class, Integer.class));
                Object[] parameters = {dataViewUuid, mapViewDefUuid, sequenceNumber};
                taskContext.setMethodArgs(parameters);
                TaskController.getInstance().submitTask(taskContext);
                status = "success";
            } catch (Exception e) {
                LOG.debug("dontShowLeavesOnBundle", e);
            }
        }

        return status;
    }

    @RequestMapping(value = "getExtentInfo/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.GET)
    public @ResponseBody
    ExtentInfo getExtentInfo(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        ExtentInfo extentInfo = null;

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                loadMapCacheIfNeeded(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
                MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
                if (mapCacheHandler.isPlaceLimitOrTrackTypeLimitReached()) {
                    extentInfo = new ExtentInfo();
                    if (mapCacheHandler.isPlaceTypeLimitReached()) {
                     extentInfo.setPlaceTypeLimitReached(true);
                  } else if (mapCacheHandler.isTrackTypeLimitReached()) {
                     extentInfo.setTrackTypeLimitReached(true);
                  }
                } else {
                    extentInfo = getExtentInfo(mapCacheHandler, request, response);
                }
            } catch (BuildSummaryCacheCancelled e) {
                LOG.debug("BuildSummaryCacheCancelled");
            } catch (MapCacheStaleException e) {
                LOG.debug("MapCacheStaleException");
            } catch (MapCacheNotAvailable e) {
                LOG.debug("MapCacheNotAvailable");
            }
        }

        return extentInfo;
    }

    @RequestMapping(value = "getSquisherExtent/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.GET)
    public @ResponseBody
    Extent getSquisherExtent(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Extent extent = null;

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                Integer rangeStart = MapCacheUtil.getRangeStart(mapViewDefUuid);
                Integer rangeEnd = MapCacheUtil.getRangeEnd(mapViewDefUuid);
                List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = MapCacheUtil.getSeriesValues(mapViewDefUuid);
                Integer rangeSize = null;
                if (seriesValues != null) {
                    rangeSize = seriesValues.size();
                }
                MapCacheUtil.invalidateMapSummaryExtent(mapViewDefUuid);
                UBox uBox = new UBox(null, rangeStart, rangeEnd, rangeSize);
                buildSummaryCache(dataViewUuid, mapViewDefUuid, sequenceNumber, uBox, request, response);
                extent = MapCacheUtil.getCurrentExtentOrCurrentExtentIfMapNotPinned(mapViewDefUuid);
            } catch (BuildSummaryCacheCancelled e) {
                LOG.debug("BuildSummaryCacheCancelled");
            } catch (MapCacheStaleException e) {
                LOG.debug("MapCacheStaleException");
            } catch (MapCacheNotAvailable e) {
                LOG.debug("MapCacheNotAvailable");
            }
        }
        return extent;
    }

    @RequestMapping(value = "getItemsInViz/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.GET)
    public @ResponseBody
    int getItemsInViz(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Integer itemsInViz = -1;

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                loadMapCacheIfNeeded(dataViewUuid, mapViewDefUuid, sequenceNumber, request, response);
                MapCacheHandler mapCacheHandler = new MapCacheHandler(dataViewUuid, mapViewDefUuid, sequenceNumber);
                itemsInViz = mapCacheHandler.getItemsInViz();
            } catch (BuildSummaryCacheCancelled | MapCacheNotAvailable | MapCacheStaleException ignored) {
            }
        }

        return itemsInViz;
    }

    private ExtentInfo getExtentInfo(MapCacheHandler mapCacheHandler, HttpServletRequest request, HttpServletResponse response) {
        ExtentInfo extentInfo = null;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("getExtentInfo", String.class, String.class, Integer.class));
            Object[] parameters = {mapCacheHandler.getDvUuid(), mapCacheHandler.getVizUuid(), mapCacheHandler.getSequenceNumber()};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            extentInfo = (ExtentInfo) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("getExtentInfo", e);
        }
        return extentInfo;
    }

    private Integer getNumPoints(String dataViewUuid, String mapViewDefUuid, String strId, HttpServletRequest request, HttpServletResponse response) {
        Integer numPoints = null;

        long id = Long.parseLong(strId);
        TaskContext taskContext = createTaskContext(request, response);
        taskContext.setExecutionPoolId(dataViewUuid);
        try {
            taskContext.setMethod(MapActionsService.class.getMethod("getNumPoints", String.class, String.class, Long.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid, id};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            if (taskContext.getStatus().getResultData() instanceof Integer) {
               numPoints = (Integer) taskContext.getStatus().getResultData();
            }
        } catch (NoSuchMethodException e) {
            LOG.debug("getNumPoints", e);
        }

        return numPoints;
    }

    @RequestMapping(value = "isUnderlyingPointsWithinLimits/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}/{strId}", method = RequestMethod.GET)
    public @ResponseBody
    String isUnderlyingPointsWithinLimits(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, @PathVariable String strId, HttpServletRequest request, HttpServletResponse response) {
        String retVal = "true";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            Integer numPoints = getNumPoints(dataViewUuid, mapViewDefUuid, strId, request, response);
            if ((numPoints != null) && ((numPoints <= 0) || (numPoints > Configuration.getInstance().getMapConfig().getPointLimit()))) {
               retVal = numPoints + "";
            }
        }

        return retVal;
    }

    @RequestMapping(value = "isDrillable/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}/{strId}", method = RequestMethod.GET)
    public @ResponseBody
    String isDrillable(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, @PathVariable String strId, HttpServletRequest request, HttpServletResponse response) {
        String retVal = "true";

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            MapSettingsDTO mapSettings = MapCacheUtil.getMapSettings(mapViewDefUuid);
            List<Crumb> breadCrumb = getBreadcrumb(dataViewUuid, mapViewDefUuid, request, response);

            if ((breadCrumb.size() + 1) < mapSettings.getMapBundleDefinitions().size()) {
                Long id = Long.parseLong(strId);
                Map<Long, AugmentedMapNode> mapNodeMap = MapCacheUtil.getMapNodeByIdMap(mapViewDefUuid);
                if (mapNodeMap != null) {
                    AugmentedMapNode mapNode = mapNodeMap.get(id);
                    if (mapNode != null) {
                        BundleMapNode bundleMapNode = (BundleMapNode) mapNode;
                        int numChildren = bundleMapNode.getChildrenCount();
                        if ((numChildren <= 0) || (numChildren > Configuration.getInstance().getMapConfig().getPointLimit())) {
                           retVal = numChildren + "";
                        }
                    }
                }
            } else {
                Integer numPoints = getNumPoints(dataViewUuid, mapViewDefUuid, strId, request, response);
                if ((numPoints != null) && ((numPoints <= 0) || (numPoints > Configuration.getInstance().getMapConfig().getPointLimit()))) {
                  retVal = numPoints + "";
               }
            }
        }

        return retVal;
    }

   private List<Crumb> getBreadcrumb(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request,
                                     HttpServletResponse response) {
      List<Crumb> breadcrumb = Collections.emptyList();

      try {
         TaskContext taskContext = createTaskContext(request, response);

         taskContext.setMethod(MapActionsService.class.getMethod("getBreadcrumb", String.class, String.class));

         Object[] parameters = { dataViewUuid, mapViewDefUuid };

         taskContext.setMethodArgs(parameters);
         TaskController.getInstance().submitTask(taskContext);

         breadcrumb = (List<Crumb>) taskContext.getStatus().getResultData();
      } catch (Exception e) {
         LOG.debug("getBreadcrumb", e);
      }
      return breadcrumb;
   }

    private boolean isShowLeaves(String dataViewUuid, String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        boolean showLeave = false;
        try {
            TaskContext taskContext = createTaskContext(request, response);
            taskContext.setMethod(MapActionsService.class.getMethod("isShowLeaves", String.class, String.class));
            Object[] parameters = {dataViewUuid, mapViewDefUuid};
            taskContext.setMethodArgs(parameters);
            TaskController.getInstance().submitTask(taskContext);
            showLeave = (Boolean) taskContext.getStatus().getResultData();
        } catch (Exception e) {
            LOG.debug("isShowLeaves", e);
        }
        return showLeave;
    }

    @RequestMapping(value = "getSequenceNumber/{dataViewUuid}/{mapViewDefUuid}", method = RequestMethod.GET)
    public @ResponseBody
    String getSequenceNumber(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, HttpServletRequest request, HttpServletResponse response) {
        return MapCacheUtil.getSequenceNumber(mapViewDefUuid);
    }

    private boolean isRangeChanged(String mapViewDefUuid, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
        Integer previousRangeStart = MapCacheUtil.getPreviousRangeStart(mapViewDefUuid);
        Integer previousRangeEnd = MapCacheUtil.getPreviousRangeEnd(mapViewDefUuid);
        Integer previousRangeSize = MapCacheUtil.getPreviousRangeSize(mapViewDefUuid);

        boolean rangeStartChanged = true;
        if ((previousRangeStart == null) && (rangeStart == null)) {
            rangeStartChanged = false;
        } else if (previousRangeStart == null) {
            if (rangeStart == 0) {
                rangeStartChanged = false;
            }
        } else if (rangeStart == null) {
            if (previousRangeStart == 0) {
                rangeStartChanged = false;
            }
        } else {
            rangeStartChanged = !previousRangeStart.equals(rangeStart);
        }
        if (rangeStartChanged) {
            MapCacheUtil.setPreviousRangeStart(mapViewDefUuid, rangeStart);
        }

        boolean rangeSizeChanged = !isEqual(previousRangeSize, rangeSize);
        if (rangeSizeChanged) {
            MapCacheUtil.setPreviousRangeSize(mapViewDefUuid, rangeSize);
        }

        boolean rangeEndChanged = true;
        if ((previousRangeEnd == null) && (rangeEnd == null)) {
            rangeEndChanged = false;
        } else if (previousRangeEnd == null) {
            if ((rangeSize != null) && (rangeEnd == (rangeSize - 1))) {
                rangeEndChanged = false;
            }
        } else if (rangeEnd == null) {
            if ((previousRangeSize != null) && (previousRangeEnd == (previousRangeSize - 1))) {
                rangeEndChanged = false;
                MapCacheUtil.setPreviousRangeEnd(mapViewDefUuid, null);
            }
        } else {
            rangeEndChanged = !previousRangeEnd.equals(rangeEnd);
        }
        if (rangeEndChanged) {
            MapCacheUtil.setPreviousRangeEnd(mapViewDefUuid, rangeEnd);
        }

        return rangeStartChanged || rangeSizeChanged || rangeEndChanged;
    }

    private boolean isEqual(Integer a, Integer b) {
        if ((a == null) && (b == null)) {
            return true;
        } else if (a == null) {
            return false;
        } else if (b == null) {
            return false;
        } else {
            return a.equals(b);
        }
    }

    @RequestMapping(value = "getSelected/{dataViewUuid}/{mapViewDefUuid}/{sequenceNumber}", method = RequestMethod.GET)
    public @ResponseBody
    Selections getSelected(@PathVariable String dataViewUuid, @PathVariable String mapViewDefUuid, @PathVariable int sequenceNumber, HttpServletRequest request, HttpServletResponse response) {
        Selections selected = null;

        Integer oldSequenceNumber = MapCacheUtil.getMapSequenceNumber(mapViewDefUuid);
        if ((oldSequenceNumber != null) && (oldSequenceNumber < sequenceNumber)) {
            MapCacheUtil.setMapSequenceNumber(mapViewDefUuid, sequenceNumber);
            try {
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                MapSettingsDTO mapSettings = MapCacheUtil.getMapSettings(mapViewDefUuid);
                if (mapSettings == null) {
                  return null;
               }

                String[] nodeSelection = getSelectedNodes(dataViewUuid, mapViewDefUuid, request, response);
                Association linkSelection = new Association(-1);
                collectSelectedLinks(mapViewDefUuid, linkSelection);
                selected = new Selections(nodeSelection, linkSelection);
            } catch (Exception e) {
                LOG.debug("getSelected", e);
            }
        }

        return selected;
    }

    static class Selections {
        private String[] nodeSelection;
        private Association linkSelection;

        Selections(String[] nodeSelection, Association linkSelection) {
            this.nodeSelection = nodeSelection;
            this.linkSelection = linkSelection;
        }

        public String[] getNodeSelection() {
            return nodeSelection;
        }

        public void setNodeSelection(String[] nodeSelection) {
            this.nodeSelection = nodeSelection;
        }

        public Association getLinkSelection() {
            return linkSelection;
        }

        public void setLinkSelection(Association linkSelection) {
            this.linkSelection = linkSelection;
        }
    }

    static class SelectionResponse {
        private int numNodeChanged = 0;
        private String nodeChangeType = "";
        private Association linkResponse = null;

        SelectionResponse() {
        }

        public int getNumNodeChanged() {
            return numNodeChanged;
        }

        void setNumNodeChanged(int numNodeChanged) {
            this.numNodeChanged = numNodeChanged;
        }

        public String getNodeChangeType() {
            return nodeChangeType;
        }

        void setNodeChangeType(String nodeChangeType) {
            this.nodeChangeType = nodeChangeType;
        }

        public Association getLinkResponse() {
            return linkResponse;
        }

        void setLinkResponse(Association linkResponse) {
            this.linkResponse = linkResponse;
        }
    }
}