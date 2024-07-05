package csi.server.business.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;

import csi.security.CsiSecurityManager;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.ModelHelper;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.business.service.chart.ChartActionsService;
import csi.server.business.service.chronos.ChronosActionsService;
import csi.server.business.service.matrix.MatrixActionsService;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.data.GraphDataManager;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;
import csi.server.common.dto.FieldConstraints;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.FilterConstraintsRequest;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.model.worksheet.WorksheetScreenLayout;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.util.IntCollection;
import csi.shared.gwt.exception.CsiClientException;

@Service(path = "/actions/viz")
public class VisualizationActionsService extends AbstractService implements VisualizationActionsServiceProtocol {
    private static final Logger LOG = LogManager.getLogger(VisualizationActionsService.class);

    @Autowired
    private SelectionBroadcastCache selectionBroadcastCache;

    @Autowired
    private ChartActionsService chartActionsService;

    @Autowired
    private ChronosActionsService chronosActionsService;

    @Autowired
    private GraphActionsService graphActionsService;

    @Autowired
    private MatrixActionsService matrixActionsService;

    /*
     * This gets called when the user clicks on 'Add Visualization'. The client
     * creates the uuid. Right now, we add this to both the dataview and the
     * worksheet (both ids must be passed in). The reason for this is so that we
     * can support the case of visualizations appearing on multiple worksheets
     * down the road if desired.
     *
     */
    @Operation
    public void addVisualization(@PayloadParam VisualizationDef viz, @QueryParam(value = "dvUuid") String dvUuid,
                                 @QueryParam(value = "worksheetUuid") String worksheetUuid) throws CentrifugeException {

        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }
        // VisualizationDef managedViz = CsiPersistenceManager.persist(viz);

        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);

        for (WorksheetDef worksheetDef : dv.getMeta().getModelDef().getWorksheets()) {
            if (worksheetDef.getUuid().equals(worksheetUuid)) {
                worksheetDef.addVisualization(viz);
            }
        }
        dv.getMeta().getModelDef().addVisualization(viz);
        ModelHelper.save(dv);

        if (viz instanceof MapViewDef) {
            MapViewDef mapViewDef = (MapViewDef) viz;
            String mapViewDefUuid = mapViewDef.getUuid();
            MapServiceUtil.initializeMapViewState(dvUuid, mapViewDefUuid);

            MapServiceUtil.invalidatePlaceDynamicTypeInfo(mapViewDefUuid);
            PlaceDynamicTypeInfo placeDynamicTypeInfo = new PlaceDynamicTypeInfo();
            MapServiceUtil.setPlaceDynamicTypeInfo(placeDynamicTypeInfo, mapViewDefUuid);
            MapServiceUtil.invalidateTrackDynamicTypeInfo(mapViewDefUuid);
            TrackDynamicTypeInfo trackDynamicTypeInfo = new TrackDynamicTypeInfo();
            MapServiceUtil.setTrackDynamicTypeInfo(trackDynamicTypeInfo, mapViewDefUuid);
        }

        LOG.debug("Added visualization: " + viz.getUuid());
    }

    @Operation
    public void addWorksheet(@PayloadParam WorksheetDef worksheet, @QueryParam(value = "dvUuid") String dvUuid)
            throws CentrifugeException {

        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        dv.getMeta().getModelDef().addWorksheet(worksheet);
        ModelHelper.save(dv);
        LOG.debug("Added worksheet: " + worksheet.getUuid());
    }

    /**
     * Deletes the worksheet but does not delete the visualizations within it.
     *
     * @param worksheet
     * @param dvUuid
     */
    @Operation
    public void removeWorksheet(@PayloadParam WorksheetDef worksheet, @QueryParam(value = "dvUuid") String dvUuid)
            throws CentrifugeException {

        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }

        // FIXME: This is a hibernate bug HHH-1268. When using an order index
        // and removing items
        // you get in trouble when trying to remove an item from the list.
        // so now I unattach all worksheets, save, and make a new list minus the
        // deleted one.
        // This is only safe now because we are single-threaded on a dataview.
        DataView dv = ModelHelper.find(DataView.class, dvUuid);
        WorksheetDef sheet = CsiPersistenceManager.findObject(WorksheetDef.class, worksheet.getUuid());
        List<WorksheetDef> worksheets = dv.getMeta().getModelDef().getWorksheets();
        List<String> uuids = new ArrayList<String>();
        for (WorksheetDef worksheetDef : worksheets) {
            if (!worksheetDef.getUuid().equals(worksheet.getUuid())) {
               uuids.add(worksheetDef.getUuid());
            }
        }
        deleteVizDefsOnlyInWorksheet(dv, sheet);
        dv.getMeta().getModelDef().setWorksheets(null);
        ModelHelper.save(dv);
        CsiPersistenceManager.commit();
        CsiPersistenceManager.close();

        CsiPersistenceManager.begin();
        worksheets = new ArrayList<WorksheetDef>();
        for (String uuid : uuids) {
            WorksheetDef worksheetDef = CsiPersistenceManager.findObject(WorksheetDef.class, uuid);
            worksheets.add(worksheetDef);
        }
        dv.getMeta().getModelDef().setWorksheets(worksheets);
        ModelHelper.save(dv);

        sheet.getWorksheetScreenLayout().setWorksheetDef(null);
        sheet = CsiPersistenceManager.findObject(WorksheetDef.class, worksheet.getUuid());
        CsiPersistenceManager.deleteObject(sheet);
    }

    private void deleteVizDefsOnlyInWorksheet(DataView dv, WorksheetDef sheet) throws CentrifugeException {

        if (!CsiSecurityManager.isAuthorized(dv.getUuid(), AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }
        CopyOnWriteArrayList<VisualizationDef> visualizationDefs = new CopyOnWriteArrayList<VisualizationDef>(
                sheet.getVisualizations());
        for (VisualizationDef visualizationDef : visualizationDefs) {
            if (!existsInMultipleWorksheets(dv, visualizationDef)) {
                dv.getMeta().getModelDef().removeVisualization(visualizationDef);
                removeFromWorksheet(visualizationDef, sheet);
                CsiPersistenceManager.deleteObject(visualizationDef);
            }
        }
    }

    private boolean existsInMultipleWorksheets(DataView dv, VisualizationDef visualizationDef) {
        int count = 0;
        for (WorksheetDef worksheetDef : dv.getMeta().getModelDef().getWorksheets()) {
            for (VisualizationDef vizDef : worksheetDef.getVisualizations()) {
                if (vizDef.equals(visualizationDef)) {
                    count++;
                }
            }
        }
        return count > 1;
    }

    /**
     * Deletes visualization from the dataview, removing it from all worksheets.
     *
     * @param dvUuid  the uuid of the data view
     * @param vizUuid the uuid of the visualization.
     */
    @Override
    @Operation
    public void deleteVisualization(String dvUuid, String vizUuid) throws CentrifugeException {

        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }

        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        VisualizationDef visualizationDef = CsiPersistenceManager.findObject(VisualizationDef.class, vizUuid);

        if (visualizationDef != null) {
            DataModelDef modelDef = dataView.getMeta().getModelDef();
            modelDef.removeVisualization(visualizationDef);

            removeFromWorksheets(modelDef, visualizationDef);

            AbstractBroadcastStorageService.instance().clearBroadcast(visualizationDef.getUuid());
            clearVizCache(visualizationDef);
            visualizationDef = CsiPersistenceManager.merge(visualizationDef);
            CsiPersistenceManager.deleteObject(visualizationDef);
            if (visualizationDef instanceof RelGraphViewDef) {
                GraphServiceUtil.removeGraphContext(visualizationDef.getUuid());
                GraphDataManager.deleteContext(visualizationDef.getUuid());
            } else if (visualizationDef instanceof MapViewDef) {
                MapServiceUtil.removeMapContext(visualizationDef.getUuid());
                MapCacheUtil.removeMapCacheInfo(visualizationDef.getUuid());
                MapCacheUtil.invalidate(visualizationDef.getUuid());
            }

        }
        ModelHelper.save(dataView);
    }

    public VisualizationDef moveVisualization(String dvUuid, String vizUuid, String worksheetUuid)
            throws CentrifugeException {
        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        VisualizationDef visualizationDef = CsiPersistenceManager.findObject(VisualizationDef.class, vizUuid);

        DataModelDef modelDef = dataView.getMeta().getModelDef();

        CopyOnWriteArrayList<WorksheetDef> worksheets = new CopyOnWriteArrayList<WorksheetDef>(
                modelDef.getWorksheets());
        for (WorksheetDef worksheetDef : worksheets) {
            if (!worksheetDef.getUuid().equals(worksheetUuid)) {
                removeFromWorksheet(visualizationDef, worksheetDef);

                CsiPersistenceManager.commit();
            }
        }

        WorksheetDef worksheet = CsiPersistenceManager.findObject(WorksheetDef.class, worksheetUuid);

        worksheet.addVisualization(visualizationDef);
        ModelHelper.save(dataView);

        return visualizationDef;

    }

    public VisualizationDef copyVisualization(String dvUuid, String vizUuid, String worksheetUuid, boolean isMove)
            throws CentrifugeException {
        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }

        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);

        DataModelDef modelDef = dataView.getMeta().getModelDef();
        WorksheetDef worksheet = null;
        VisualizationDef visualizationDef = copyVisualization(modelDef, dvUuid, vizUuid, isMove);

        if (visualizationDef != null) {
           // Persist to add new one
           CsiPersistenceManager.persist(visualizationDef);
           dataView.getMeta().getModelDef().addVisualization(visualizationDef);

           // Copy the broadcasts
           AbstractBroadcastStorageService.instance().copy(vizUuid, visualizationDef.getUuid());

           for (WorksheetDef worksheetDef : modelDef.getWorksheets()) {
              if (worksheetDef.getUuid().equals(worksheetUuid)) {
                 worksheet = worksheetDef;
              }
           }
        }
        if (worksheet == null) {
           throw new CentrifugeException("Could not find worksheet");
        }
        worksheet.addVisualization(visualizationDef);
        ModelHelper.save(dataView);

        if (LOG.isDebugEnabled() && (visualizationDef != null)) {
            LOG.debug("Copied visualization: " + visualizationDef.getUuid());
        }
        return visualizationDef;
    }

    private VisualizationDef copyVisualization(DataModelDef modelDef, String dvUuid, String vizUuid, boolean isMove) {
        Map<String, Filter> filterMap = new HashMap<String, Filter>();
        List<String> nameList = new ArrayList<String>();
        VisualizationDef copy = null;
        FieldListAccess myFieldAccess = modelDef.getFieldListAccess();

        for (VisualizationDef myItem : modelDef.getVisualizations()) {
            nameList.add(myItem.getName());
            if (myItem.getUuid().equals(vizUuid)) {
                if (myItem.getFilter() != null) {
                    filterMap.put(myItem.getFilterUuid(), myItem.getFilter().copy());
                }
                copy = myItem.copy(myFieldAccess.getFieldMapByName(), filterMap);

                if (myItem instanceof MapViewDef) {
                    Selection selection = MapServiceUtil.getMapSelection(dvUuid, myItem.getUuid());
                    MapServiceUtil.addSelection(dvUuid, copy.getUuid(), selection.copy());
                } else {
                    copy.getSelection().setFromSelection(myItem.getSelection());
                }
            }
        }

        if (!isMove && (copy != null)) {
            copy.setName("Copy of - " + copy.getName());

            String name = copy.getName();
            int count = 1;
            while (nameList.contains(copy.getName())) {
                copy.setName(name + " (" + count + ")");
                count++;
            }
        }
        return copy;
    }

    private void removeFromWorksheets(DataModelDef modelDef, VisualizationDef visualizationDef) {
        CopyOnWriteArrayList<WorksheetDef> worksheets = new CopyOnWriteArrayList<WorksheetDef>(
                modelDef.getWorksheets());
        for (WorksheetDef worksheetDef : worksheets) {
            removeFromWorksheet(visualizationDef, worksheetDef);
        }
    }

    private void removeFromWorksheet(VisualizationDef visualizationDef, WorksheetDef worksheetDef) {
        worksheetDef.removeVisualization(visualizationDef);
        WorksheetScreenLayout worksheetScreenLayout = worksheetDef.getWorksheetScreenLayout();
        if (worksheetScreenLayout.getActivatedVisualizationUuid() != null) {
            if (worksheetScreenLayout.getActivatedVisualizationUuid().equals(visualizationDef.getUuid())) {
                worksheetDef.getWorksheetScreenLayout().setActivatedVisualizationUuid(null);
            }
        }
    }

    @Operation
    public void saveSettings(VisualizationDef viz, String dvuuid, Boolean isStructural) throws CentrifugeException {
        saveSettings(viz, dvuuid, isStructural, true);
    }

    @Operation
    public void saveSettings(VisualizationDef viz, String dvuuid, Boolean isStructural, Boolean clearTransient)
            throws CentrifugeException {

        if (!CsiSecurityManager.isAuthorized(dvuuid, AclControlType.EDIT)) {
            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }
        if (clearTransient.booleanValue()) {
            clearVizCache(viz);
        }
        VisualizationDef merged = CsiPersistenceManager.merge(viz);

        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvuuid);
        dv.setLastUpdateDate(new Date());
        CsiPersistenceManager.flush();

        // treat null as true so it behaves the same as it did prior to the new parameter
        boolean hasStructuralChanges = ((isStructural == null) || isStructural.booleanValue());

        if (hasStructuralChanges) {
            if (viz instanceof RelGraphViewDef) {
                GraphDataManager.deleteContext(merged.getUuid());
            } else if (viz instanceof MapViewDef) {
                MapViewDef mapViewDef = (MapViewDef) viz;
                String mapViewDefUuid = mapViewDef.getUuid();
                MapServiceUtil.initializeMapViewState(dvuuid, mapViewDefUuid);

                MapServiceUtil.removeMapContext(mapViewDefUuid);

                MapServiceUtil.invalidatePlaceDynamicTypeInfo(mapViewDefUuid);
                PlaceDynamicTypeInfo placeDynamicTypeInfo = new PlaceDynamicTypeInfo();
                MapServiceUtil.setPlaceDynamicTypeInfo(placeDynamicTypeInfo, mapViewDefUuid);
                MapServiceUtil.invalidateTrackDynamicTypeInfo(mapViewDefUuid);
                TrackDynamicTypeInfo trackDynamicTypeInfo = new TrackDynamicTypeInfo();
                MapServiceUtil.setTrackDynamicTypeInfo(trackDynamicTypeInfo, mapViewDefUuid);

                MapCacheUtil.invalidateInitialExtent(mapViewDefUuid);
                MapCacheUtil.removeSequenceBarInfo(mapViewDefUuid);
            }
        }

        LOG.debug(String.format("Settings saved for viz '%s'", merged.getName()));

    }

    public void clearVizCache(String vizUuid) {
        try {
            VisualizationDef viz = CsiPersistenceManager.findObject(VisualizationDef.class, vizUuid);
            clearVizCache(viz);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public void clearVizCache(VisualizationDef viz) {
        if (viz instanceof MapViewDef) {
            MapCacheUtil.invalidate(viz.getUuid());
        } else if (viz instanceof MatrixViewDef) {
            matrixActionsService.invalidateCache(viz.getUuid());
        } else if (viz instanceof DrillChartViewDef) {
            chartActionsService.clearCache(viz);
        } else if (viz instanceof TimelineViewDef) {
            chronosActionsService.clearCache(viz);
        } else if (viz instanceof RelGraphViewDef) {
            graphActionsService.clearCache(viz.getUuid());
        } else if (!(viz instanceof RelGraphViewDef)) {
            // selectionBroadcastCache.addMapSelection(viz.getUuid(),
            // viz.getSelection());
        }
    }

    public void createVizCache(VisualizationDef viz, DataView dataView) {
        if (viz instanceof MatrixViewDef) {
            // matrixActionsService.invalidateCache(viz.getUuid());
        } else if (viz instanceof DrillChartViewDef) {
            chartActionsService.createCache(viz, dataView);
        } else if (viz instanceof TimelineViewDef) {
            chronosActionsService.createCache(viz, dataView);
        } else if (viz instanceof MatrixViewDef) {
            matrixActionsService.createCache(viz, dataView);
        }
    }

   @Operation
   @Interruptable
   public List<FieldConstraints> getFilterConstraints(@PayloadParam FilterConstraintsRequest request,
                                                      FieldDef selectedItem) throws CentrifugeException {
      List<FieldConstraints> constraintsList = new ArrayList<FieldConstraints>();

      if (selectedItem != null) {
         String dvUuid = request.dvUuid;
         boolean caseSensitive = request.caseSensitive;
         int limit = request.limit;
         FieldConstraints constraints = DataCacheHelper.getFieldConstraints(dvUuid, selectedItem, caseSensitive, limit);

         constraintsList.add(constraints);
      }
      return constraintsList;
   }

    @Operation
    @Interruptable
    public VisualizationDef getVisualization(@QueryParam(value = "dvUuid") String dvUuid, @QueryParam(value = "uuid") String vizUuid) {
        VisualizationDef viz = ModelHelper.find(VisualizationDef.class, vizUuid.toLowerCase());
        if (viz == null) {
            throw Throwables.propagate(new CsiClientException("Visualization not found. uuid = " + vizUuid));
        }

        if (viz instanceof MapViewDef) {
            // MapCache.getInstance().addMapSelection(vizUuid,
            // selectionBroadcastCache.getSelection(vizUuid));
            MapViewDef mapViewDef = (MapViewDef) viz;
            MapSettings mapSettings = mapViewDef.getMapSettings();

            if (mapSettings.isBundleUsed().booleanValue()) {
                MapServiceUtil.addBreadcrumb(dvUuid, vizUuid, null);
                MapServiceUtil.setShowLeaves(dvUuid, vizUuid, false);
            } else if (mapSettings.isUseTrack().booleanValue()) {
                if (MapCacheUtil.getTrackGeometryToRowIds(vizUuid) != null) {
                    MapCacheUtil.removeSequenceBarInfo(vizUuid);
                }
            }
            MapCacheUtil.invalidateInitialExtent(vizUuid);
            MapCacheUtil.invalidateExtentIfMapNotPinned(vizUuid);
        } else {
            // viz.getSelection().setFromSelection(selectionBroadcastCache.getSelection(vizUuid));
        }

        return viz;
    }

    @Operation
    @Interruptable
    public void setWorksheetName(String dvUuid, @QueryParam(value = "uuid") String uuid,
                                 @QueryParam(value = "name") String newName) throws CentrifugeException {

        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {

            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }
        WorksheetDef w = CsiPersistenceManager.findObject(WorksheetDef.class, uuid);
        if (w == null) {
            throw Throwables.propagate(new CsiClientException(String.format("No worksheet found with id '%s'", uuid)));
        }
        w.setWorksheetName(newName);
    }

    @Operation
    @Interruptable
    public void setWorksheetColor(String dvUuid, @QueryParam(value = "uuid") String uuid, Integer color) throws CentrifugeException {
        if (!CsiSecurityManager.isAuthorized(dvUuid, AclControlType.EDIT)) {
            throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
        }

        WorksheetDef w = CsiPersistenceManager.findObject(WorksheetDef.class, uuid);
        if (w == null) {
            throw Throwables.propagate(new CsiClientException(String.format("No worksheet found with id '%s'", uuid)));
        }
        w.setWorksheetColor(color);
    }

    public SelectionBroadcastCache getSelectionBroadcastCache() {
        return selectionBroadcastCache;
    }

    public void setSelectionBroadcastCache(SelectionBroadcastCache selectionBroadcastCache) {
        this.selectionBroadcastCache = selectionBroadcastCache;
    }

   @Override
   public Boolean isSelectionAvailable(String dvUuid, VisualizationDef visualizationDef) {
      Boolean retVal = Boolean.FALSE;
      Selection selection;

      if (visualizationDef instanceof RelGraphViewDef) {
         GraphContext gc = GraphServiceUtil.getGraphContext(visualizationDef.getUuid());

         if (gc != null) {
            synchronized (gc) {
               selection = gc.getSelection(GraphManager.DEFAULT_SELECTION);

               if (selection != null) {
                  retVal = Boolean.valueOf(!selection.isCleared());
               }
               if (retVal.booleanValue()) {
                  SelectionModel selection1 = (SelectionModel) selection;
                  boolean hasNonPlunkedNode = false;

                  for (Integer node : selection1.nodes) {
                     NodeStore nodeDetails = GraphManager.getNodeDetails(gc.getVisibleGraph().getNode(node));

                     if (!nodeDetails.isPlunked()) {
                        hasNonPlunkedNode = true;
                        break;
                     }
                  }
                  if (!hasNonPlunkedNode) {
                     boolean hasNonPlunkedLink = false;

                     for (Integer link : selection1.links) {
                        LinkStore linkDetails = GraphManager.getEdgeDetails(gc.getVisibleGraph().getEdge(link));

                        if (!linkDetails.isPlunked()) {
                           hasNonPlunkedLink = true;
                           break;
                        }
                     }
                     if (!hasNonPlunkedLink) {
                        retVal = Boolean.FALSE;
                     }
                  }
               }
            }
         }
      } else if (visualizationDef instanceof MapViewDef) {
         selection = MapServiceUtil.getMapSelection(dvUuid, visualizationDef.getUuid());
         retVal = Boolean.valueOf(!selection.isCleared());
      } else if (visualizationDef instanceof TimelineViewDef) {
         TimelineEventSelection timelineSelection = (TimelineEventSelection) visualizationDef.getSelection();
         TimelineViewDef timelineViewDef = (TimelineViewDef) visualizationDef;
         IntCollection eventIds = timelineSelection.getSelectedItems();

         if ((eventIds != null) && !eventIds.isEmpty()) {
            Set<Integer> selectionHash =
               chronosActionsService.validateVisibleEvents(timelineViewDef, timelineSelection.getSelectedItems());

            if (!selectionHash.isEmpty()) {
               retVal = Boolean.TRUE;
            }
         }
      }
      return retVal;
   }
}
