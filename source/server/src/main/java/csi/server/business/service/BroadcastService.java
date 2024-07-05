/*
 * @(#) BroadcastService.java,  31.05.2011
 *
 */
package csi.server.business.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import prefuse.data.Graph;

import csi.server.business.helper.DataCacheHelper;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.selection.operations.SelectionOperator;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.selection.torows.SelectionToRowsConverter;
import csi.server.business.selection.torows.SelectionToRowsCoverterFactory;
import csi.server.business.selection.toselection.RowsToSelectionConverter;
import csi.server.business.selection.toselection.RowsToSelectionConverterFactory;
import csi.server.business.selection.toselection.TableRowsToSelectionConverter;
import csi.server.business.service.annotation.Service;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.GraphHelper;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.broadcast.BroadcastRequest;
import csi.server.common.model.broadcast.BroadcastRequestType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.DetailMapSelection;
import csi.server.common.model.visualization.selection.GraphInternalIdSelection;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.model.visualization.selection.SummaryMapSelection;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.common.service.api.BroadcastServiceProtocol;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.exception.TaskAbortedException;

@Service(path = "/services/broadcast")
public class BroadcastService extends AbstractService implements BroadcastServiceProtocol {
    private static final Logger LOG = LogManager.getLogger(BroadcastService.class);

    @Autowired
    private SelectionBroadcastCache selectionBroadcastCache;


    @Autowired
    private VisualizationActionsService visualizationActionsService;

    @Override
    public void pinBroadcast(BroadcastRequest request) {

        DataView dv = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
        VisualizationDef broadcastingVizDef;
        if (request.getBroadcastingViz() != null) {
            broadcastingVizDef = request.getBroadcastingViz();
            verifySelection(request.getDataViewUuid(), broadcastingVizDef, broadcastingVizDef.getSelection());
        } else {
            broadcastingVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getBroadcasterVizUuid());
            setSelectionOnVizDef(request.getDataViewUuid(), broadcastingVizDef, request.getBroadcasterSelection());
        }
        Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
        TableRowsToSelectionConverter tableToRowsSelectionConverter = new TableRowsToSelectionConverter();
        IntegerRowsSelection rowsSelection = tableToRowsSelectionConverter.toRowsSelection(rowIds);
        selectionBroadcastCache.addSelection(request.getBroadcasterVizUuid(), rowsSelection);
    }

    @Override
    public void unpinBroadcast(BroadcastRequest request) {
        selectionBroadcastCache.removeSelection(request.getBroadcasterVizUuid());
    }

    @Override
    public Selection broadcastFilter(BroadcastRequest request) {
        if (request.getBroadcastRequestType() == BroadcastRequestType.FILTER_SET) {
            return doFilterSet(request);
        }
        DataView dv = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
        VisualizationDef listeningVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getListeningVizUuid());
        clearVizState(listeningVizDef);
        setSelectionOnVizDef(request.getDataViewUuid(), listeningVizDef, request.getListeningVizSelection());

        AbstractBroadcastStorageService.instance().clearBroadcast(request.getListeningVizUuid());

        //Convert listening selection to rows
        Set<Integer> selectionRowIds = convertVizSelectionIntoRows(dv, listeningVizDef, listeningVizDef.getSelection());
        if (!(listeningVizDef instanceof MapViewDef)) {
            visualizationActionsService.clearVizCache(listeningVizDef);
        }

        Selection cachedBroadcast = selectionBroadcastCache.getSelection(request.getBroadcasterVizUuid());
        //If there is no broadcast that was pinned, we can attempt to create a new one here
        cachedBroadcast = verifyAndUpdatePinnedBroadcast(request, cachedBroadcast);
        if (cachedBroadcast instanceof NullSelection) {
            return NullSelection.instance;
        }

        IntegerRowsSelection broadcast = (IntegerRowsSelection) cachedBroadcast;
        List<Integer> integers = DataCacheHelper.inverseRows(dv, (broadcast).getSelectedItems());

        selectionBroadcastCache.addSelection(request.getListeningVizUuid(), getSelectionFromVizDef(listeningVizDef));
        if (request.getBroadcastRequestType() == BroadcastRequestType.FILTER_HIDE) {
            AbstractBroadcastStorageService.instance().addBroadcast(request.getListeningVizUuid(), broadcast, true);
        } else {
            IntegerRowsSelection inverseSelection = new IntegerRowsSelection();
            inverseSelection.getSelectedItems().addAll(integers);
            AbstractBroadcastStorageService.instance().addBroadcast(request.getListeningVizUuid(), inverseSelection, true);
        }

        //selectionBroadcastCache.addMapSelection(request.getListeningVizUuid(), getSelectionFromVizDef(listeningVizDef));


        if (listeningVizDef instanceof RelGraphViewDef) {
            addGraphInternalSelection(listeningVizDef);
        }

        //Verify listening selection is still valid
        HashSet<Integer> rowHashSet;
        if ((selectionRowIds == null) || (broadcast.getSelectedItems() == null) || broadcast.getSelectedItems().isEmpty()) {
            selectionRowIds = new HashSet<>(0);
        } else {
            if (request.getBroadcastRequestType() == BroadcastRequestType.FILTER_HIDE) {
                rowHashSet = Sets.newHashSet(integers);
            } else {
                rowHashSet = Sets.newHashSet(broadcast.getSelectedItems());
            }
            selectionRowIds.retainAll(rowHashSet);
        }
        Selection selection;
        if (selectionRowIds.isEmpty()) {
            selection = NullSelection.instance;
        } else if (listeningVizDef instanceof MapViewDef) {
            if (MapServiceUtil.isUseTrack((listeningVizDef.getUuid()))) {
                selection = NullSelection.instance;
            } else {
                selection = handleMapVizSelectionAfterSelectionFilter(dv, listeningVizDef, selectionRowIds);
            }
        } else {
            //We create a new vizcache here so that we may create an appropriate selection to send back to client
            visualizationActionsService.createVizCache(listeningVizDef, dv);
            RowsToSelectionConverter rowsToSelectionConverter = new RowsToSelectionConverterFactory(dv, listeningVizDef).create();
            selection = rowsToSelectionConverter.toSelection(selectionRowIds);
        }
        if (listeningVizDef instanceof MapViewDef) {
            handleMapState(dv, (MapViewDef) listeningVizDef);
        }

        CsiPersistenceManager.getMetaEntityManager().clear();
        return selection;
    }

    private void handleMapState(DataView dv, MapViewDef listeningVizDef) {
        MapViewDef mapViewDef = listeningVizDef;
        String dvUuid = dv.getUuid();
        String vizUuid = mapViewDef.getUuid();
        MapSettings mapSettings = mapViewDef.getMapSettings();

        if (mapSettings.isBundleUsed().booleanValue()) {
            MapServiceUtil.addBreadcrumb(dvUuid, vizUuid, null);
            MapServiceUtil.setShowLeaves(dvUuid, vizUuid, false);
            MapCacheUtil.setCurrentExtentIfMapNotPinned(vizUuid, null);
        } else if (mapSettings.isUseTrack().booleanValue()) {
            MapCacheUtil.setCurrentExtentIfMapNotPinned(vizUuid, null);
            MapCacheUtil.removeSequenceBarInfo(vizUuid);
        }
        MapCacheUtil.invalidate(listeningVizDef.getUuid());
    }

    private void clearVizState(VisualizationDef listeningVizDef) {
        if (listeningVizDef instanceof TimelineViewDef) {
            TimelineViewDef timelineViewDef = (TimelineViewDef) listeningVizDef;
            if (timelineViewDef.getState() != null) {
                if (timelineViewDef.getState().getTrackStates() != null) {
                    timelineViewDef.getState().getTrackStates().clear();
                }
                timelineViewDef.getState().setFocusedTrack(null);
            }

        }
    }

    private Selection verifyAndUpdatePinnedBroadcast(BroadcastRequest request, Selection cachedBroadcast) {
        if ((cachedBroadcast instanceof NullSelection) || cachedBroadcast.isCleared()) {
            pinBroadcast(request);
            cachedBroadcast = selectionBroadcastCache.getSelection(request.getBroadcasterVizUuid());

        }
        return cachedBroadcast;
    }

    private Selection doFilterSet(BroadcastRequest request) {

        unpinBroadcast(request);
        DataView dv = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
        VisualizationDef listeningVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getListeningVizUuid());
        clearVizState(listeningVizDef);

        VisualizationDef broadcastingVizDef = null;
        if (request.getBroadcastingViz() != null) {
            broadcastingVizDef = request.getBroadcastingViz();
            verifySelection(request.getDataViewUuid(), broadcastingVizDef, broadcastingVizDef.getSelection());
        } else {
            broadcastingVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getBroadcasterVizUuid());
            setSelectionOnVizDef(request.getDataViewUuid(), broadcastingVizDef, request.getBroadcasterSelection());
        }
        setSelectionOnVizDef(request.getDataViewUuid(), listeningVizDef, request.getListeningVizSelection());
        TableRowsToSelectionConverter rowsToSelectionConverter = new TableRowsToSelectionConverter();
        BroadcastResult broadcastResult = AbstractBroadcastStorageService.instance().getBroadcast(request.getListeningVizUuid());
        IntegerRowsSelection selectionA = (IntegerRowsSelection) broadcastResult.getBroadcastFilter().copy();
        if ((selectionA == null) || selectionA.isCleared()) { //|| selectionA instanceof NullSelection) {
            selectionA = createSelectAllBoxed(dv, rowsToSelectionConverter);
        } else {

            List<Integer> inverseRows = DataCacheHelper.inverseRows(dv, selectionA.getSelectedItems());
            selectionA.clearSelection();
            selectionA.setSelectedItems(inverseRows);
        }
        SelectionOperator selectionOperatorA = new SelectionOperator(selectionA);

        Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
        Set<Integer> selectionRowIds = convertVizSelectionIntoRows(dv, listeningVizDef, listeningVizDef.getSelection());

        Selection selectionB = rowsToSelectionConverter.toRowsSelection(rowIds);

        Selection selectionOut = null;

        switch (request.getBroadcastSet()) {

            case A_OR_B:

                selectionOperatorA.operateOnSelection(BroadcastRequestType.SELECTION_ADD, selectionB);
                selectionOut = selectionOperatorA.getSelection();
                break;
            case B:
                selectionOperatorA.operateOnSelection(BroadcastRequestType.SELECTION_REPLACE, selectionB);
                selectionOut = selectionOperatorA.getSelection();
                break;
            case A_AND_NOT_B:
                selectionOperatorA.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selectionOut = selectionOperatorA.getSelection();
                break;
            case TRUE:
                selectionOut = createSelectAllBoxed(dv, rowsToSelectionConverter);
                break;
            case FALSE:
                selectionOut = rowsToSelectionConverter.toSelection(Sets.newConcurrentHashSet());
                break;
            case NEITHER_A_NOR_B: {
                Selection selectAll = createSelectAllBoxed(dv, rowsToSelectionConverter);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selectionOut = selectionOperator.getSelection();
            }
            break;
            case NOT_B: {
                SelectionOperator selectionOperator = new SelectionOperator(createSelectAllBoxed(dv, rowsToSelectionConverter));
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selectionOut = selectionOperator.getSelection();
            }
            break;
            case NOT_A: {
                SelectionOperator selectionOperator = new SelectionOperator(createSelectAllBoxed(dv, rowsToSelectionConverter));
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
                selectionOut = selectionOperator.getSelection();
            }
            break;
            case EITHER_A_OR_B_BUT_NOT_BOTH: {
                SelectionOperator selectionOperatorB = new SelectionOperator(selectionB.copy());
                selectionOperatorB.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
                selectionOperatorA.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selectionOperatorB.operateOnSelection(BroadcastRequestType.SELECTION_ADD, selectionOperatorA.getSelection());
                selectionOut = selectionOperatorB.getSelection();

            }
            break;
            case A_IF_AND_ONLY_IF_B: {
                Selection selectAll = createSelectAllBoxed(dv, rowsToSelectionConverter);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_ADD, getAndSelection(selectionA, selectionB));
                selectionOut = selectionOperator.getSelection();
            }
            break;
            case A:
                selectionOut = selectionA;
                break;
            case NOT_BOTH: {
                Selection selectAll = createSelectAllBoxed(dv, rowsToSelectionConverter);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, getAndSelection(selectionA, selectionB));
                selectionOut = selectionOperator.getSelection();
            }
            break;
            case IF_B_THEN_A: {
                Selection selectAll = createSelectAllBoxed(dv, rowsToSelectionConverter);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, getBNotASelection(selectionA, selectionB));
                selectionOut = selectionOperator.getSelection();
            }
            break;
            case IF_A_THEN_B: {
                Selection selectAll = createSelectAllBoxed(dv, rowsToSelectionConverter);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, getANotBSelection(selectionA, selectionB));
                selectionOut = selectionOperator.getSelection();
            }
            break;
            case B_AND_NOT_A:
                selectionOut = getBNotASelection(selectionA, selectionB);

                break;
            case AND:
                selectionOut = getAndSelection(selectionA, selectionB);
                break;
        }

        if (selectionOut instanceof IntPrimitiveSelection) {
            IntegerRowsSelection integerSelection = new IntegerRowsSelection();
            List<Integer> selectedItems = integerSelection.getSelectedItems();
            for (Integer integer : ((IntPrimitiveSelection) selectionOut).getSelectedItems()) {
                selectedItems.add(integer);
            }
            selectionOut = integerSelection;

        }
        List<Integer> displayedRows = null;
        boolean clearMapInfo = false;
        if (selectionOut instanceof IntegerRowsSelection) {
            displayedRows = ((IntegerRowsSelection) selectionOut).getSelectedItems();
            if (!selectionOut.isCleared()) {
                if (listeningVizDef instanceof MapViewDef) {
                    clearMapInfo = true;
                } else {
                    visualizationActionsService.clearVizCache(listeningVizDef);
                }
                List<Integer> integers = DataCacheHelper.inverseRows(dv, displayedRows);
                IntegerRowsSelection inverseSelection = new IntegerRowsSelection();
                inverseSelection.getSelectedItems().addAll(integers);
                AbstractBroadcastStorageService.instance().addBroadcast(request.getListeningVizUuid(), inverseSelection, true);
            } else {
                //TODO:notice to user
                return null;
                /*TaskHelper.reportProgress("Broadcast Aborted. No data.",100);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return request.getListeningVizSelection();*/
            }
        } else {
            displayedRows = new ArrayList<>();
        }
        selectionBroadcastCache.addSelection(request.getBroadcasterVizUuid(), getSelectionFromVizDef(broadcastingVizDef));
        selectionBroadcastCache.addSelection(request.getListeningVizUuid(), getSelectionFromVizDef(listeningVizDef));

        if (listeningVizDef instanceof RelGraphViewDef) {
            addGraphInternalSelection(listeningVizDef);
        }


        //Verify listening selection is still valid
        HashSet<Integer> validatedRows = Sets.newHashSet(displayedRows);
        validatedRows.retainAll(selectionRowIds);
        Selection selection;
        if (validatedRows.isEmpty()) {
            selection = NullSelection.instance;
        } else if (listeningVizDef instanceof MapViewDef) {
            if (MapServiceUtil.isUseTrack((listeningVizDef.getUuid()))) {
                selection = NullSelection.instance;
            } else {
                selection = handleMapVizSelectionAfterSelectionFilter(dv, listeningVizDef, selectionRowIds);
            }
        } else {
            //We create a new vizcache here so that we may create an appropriate selection to send back to client
            visualizationActionsService.createVizCache(listeningVizDef, dv);
            RowsToSelectionConverter rTS = new RowsToSelectionConverterFactory(dv, listeningVizDef).create();
            selection = rTS.toSelection(validatedRows);
        }
        if (clearMapInfo) {
            handleMapState(dv, (MapViewDef) listeningVizDef);
        }

        CsiPersistenceManager.getMetaEntityManager().clear();
        return selection;

    }

    private Selection handleMapVizSelectionAfterSelectionFilter(DataView dv, VisualizationDef listeningVizDef, Set<Integer> selectionRowIds) {
        visualizationActionsService.createVizCache(listeningVizDef, dv);
        RowsToSelectionConverter rTS = new RowsToSelectionConverterFactory(dv, listeningVizDef).create();
        Selection newSelection = rTS.toSelection(selectionRowIds);
        MapServiceUtil.addSelection(dv.getUuid(), listeningVizDef.getUuid(), newSelection);
        return NullSelection.instance;
    }

    private IntegerRowsSelection createSelectAllBoxed(DataView dv, TableRowsToSelectionConverter rowsToSelectionConverter) {
        IntegerRowsSelection selection;
        DataCacheHelper.verifyDataviewHasSize(dv);

        List<ValuePair<Long, Long>> ranges = dv.getInternalIdRanges();

        Set<Integer> allRows = Sets.newHashSetWithExpectedSize((int) (dv.getSize()));
        for (ValuePair<Long, Long> range : ranges) {
            int start = range.getValue1().intValue();
            int end = range.getValue2().intValue();
            ContiguousSet<Integer> rangeRows = ContiguousSet.create(Range.closed(start, end), DiscreteDomain.integers());
            allRows.addAll(rangeRows);
        }
        selection = rowsToSelectionConverter.toRowsSelection(allRows);
        return selection;

    }

    private void setSelectionOnVizDef(String dvUuid, VisualizationDef vizDef, Selection selection) {
        if (vizDef instanceof MapViewDef) {
            selection = MapServiceUtil.getSelection(dvUuid, vizDef.getUuid());
        }
        vizDef.getSelection().setFromSelection(selection);
    }

    private Selection verifySelection(String dvUuid, VisualizationDef vizDef, Selection selection) {
        if (vizDef instanceof MapViewDef) {
            selection = MapServiceUtil.getSelection(dvUuid, vizDef.getUuid());
            vizDef.getSelection().setFromSelection(selection);
        }
        return selection;
    }

    private Set<Integer> convertVizSelectionIntoRows(DataView dv, VisualizationDef vizDef, Selection selection) {
        SelectionToRowsConverter selectionToRowsConverter = new SelectionToRowsCoverterFactory(dv, vizDef).create();

        return selectionToRowsConverter.convertToRows(selection, false);
    }

    private Selection getSelectionFromVizDef(VisualizationDef visualizationDef) {
        if (visualizationDef instanceof RelGraphViewDef) {
            return getGraphSelection(visualizationDef.getUuid());
        }

        return visualizationDef.getSelection();
    }

    private Selection getGraphSelection(String graphUuid) {
        try {
            GraphContext gc = GraphServiceUtil.getGraphContext(graphUuid);
            if (gc != null) {
                synchronized (gc) {
                    return gc.getSelection(GraphManager.DEFAULT_SELECTION);
                }
            }
        } catch (TaskAbortedException exception) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(exception);
            }
        }
        return NullSelection.instance;
    }

    private void addGraphInternalSelection(VisualizationDef listeningVizDef) {
        GraphContext gc = GraphServiceUtil.getGraphContext(listeningVizDef.getUuid());
        if (gc == null) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Expected Graph Context. Found Null.");
            }
            return;
        }
        Graph graph = gc.getGraphData();

        SelectionModel selectionModel = (SelectionModel) getSelectionFromVizDef(listeningVizDef);
        GraphInternalIdSelection graphInternalIdSelection = GraphInternalIdSelection.createGraphInternalIdSelection(selectionModel, graph);
        selectionBroadcastCache.addSelection(listeningVizDef.getUuid(), graphInternalIdSelection);
    }

    @Override
    public Selection broadcastSelection(BroadcastRequest request) {
        if (request.getBroadcastRequestType() == BroadcastRequestType.SELECT_SET) {
            return doSelectionSet(request);
        }

        DataView dv = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
        VisualizationDef listeningVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getListeningVizUuid());
        clearVizState(listeningVizDef);

        setSelectionOnVizDef(request.getDataViewUuid(), listeningVizDef, request.getListeningVizSelection());

        //Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
        Selection cachedBroadcast = selectionBroadcastCache.getSelection(request.getBroadcasterVizUuid());
        //If there is no broadcast that was pinned, we can attempt to create a new one here
        cachedBroadcast = verifyAndUpdatePinnedBroadcast(request, cachedBroadcast);
        if (cachedBroadcast instanceof NullSelection) {
            return NullSelection.instance;
        }

        IntegerRowsSelection broadcast = (IntegerRowsSelection) cachedBroadcast;

        RowsToSelectionConverter rowsToSelectionConverter = new RowsToSelectionConverterFactory(dv, listeningVizDef).create();
        Selection selection = rowsToSelectionConverter.toSelection(Sets.newHashSet(broadcast.getSelectedItems()));
        Selection listeningSelection;

//TODO: both brances assign listeningSelection which is just assigned after if()
        //These are defaults in case of a bad pivot, on bad pivots, add and remove do nothing
        // while a replace returns an empty aka null selection
        if ((request.getBroadcastRequestType() == BroadcastRequestType.SELECTION_ADD) ||
                (request.getBroadcastRequestType() == BroadcastRequestType.SELECTION_REMOVE)) {
//            listeningSelection = request.getListeningVizSelection();
        } else {
//            listeningSelection = NullSelection.instance;
        }
        //Protect against a bad pivot
//		if(!selection.isCleared()){
        SelectionOperator operator = new SelectionOperator(getSelectionFromVizDef(listeningVizDef));
        operator.operateOnSelection(request.getBroadcastRequestType(), selection);
        listeningSelection = operator.getSelection();
        //addBroadcastingSelectionToCache(request, broadcastingVizDef);
        addListeningSelectionToCache(request, listeningVizDef, listeningSelection);
        if (listeningVizDef instanceof MapViewDef)
       {
         MapServiceUtil.addSelection(request.getDataViewUuid(), listeningVizDef.getUuid(), selectionBroadcastCache.getSelection(listeningVizDef.getUuid()));
//		}
      }


        if (listeningVizDef instanceof MapViewDef) {
         return NullSelection.instance;
      } else {
         return listeningSelection;
      }
    }

    private Selection doSelectionSet(BroadcastRequest request) {
        unpinBroadcast(request);
        Selection selection = null;
        DataView dv = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
        VisualizationDef listeningVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getListeningVizUuid());
        clearVizState(listeningVizDef);
        VisualizationDef broadcastingVizDef = null;
        if (request.getBroadcastingViz() != null) {
            broadcastingVizDef = request.getBroadcastingViz();
            verifySelection(request.getDataViewUuid(), broadcastingVizDef, broadcastingVizDef.getSelection());
        } else {
            broadcastingVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getBroadcasterVizUuid());
            setSelectionOnVizDef(request.getDataViewUuid(), broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
        }
        setSelectionOnVizDef(request.getDataViewUuid(), listeningVizDef, request.getListeningVizSelection());


        RowsToSelectionConverter rowsToSelectionConverter = new RowsToSelectionConverterFactory(dv, listeningVizDef).create();


        switch (request.getBroadcastSet()) {

            case A_OR_B:
                request.setBroadcastSet(null);
                request.setBroadcastRequestType(BroadcastRequestType.SELECTION_ADD);
                return broadcastSelection(request);
            case B:
                request.setBroadcastSet(null);
                request.setBroadcastRequestType(BroadcastRequestType.SELECTION_REPLACE);
                return broadcastSelection(request);
            case A_AND_NOT_B:
                request.setBroadcastSet(null);
                request.setBroadcastRequestType(BroadcastRequestType.SELECTION_REMOVE);
                return broadcastSelection(request);
            case TRUE:

                selection = createSelectAll(dv, rowsToSelectionConverter);

                break;
            case FALSE:

                selection = rowsToSelectionConverter.toSelection(Sets.newConcurrentHashSet());
                break;
            case NEITHER_A_NOR_B: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                Selection selectAll = createSelectAll(dv, rowsToSelectionConverter);
                Selection selectionA = getSelectionFromVizDef(listeningVizDef);
                Selection selectionB = rowsToSelectionConverter.toSelection(rowIds);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selection = selectionOperator.getSelection();
            }
            break;
            case NOT_B: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                Selection selectionB = rowsToSelectionConverter.toSelection(rowIds);
                SelectionOperator selectionOperator = new SelectionOperator(createSelectAll(dv, rowsToSelectionConverter));
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selection = selectionOperator.getSelection();
            }
            break;
            case NOT_A: {
                Selection selectionA = getSelectionFromVizDef(listeningVizDef);
                SelectionOperator selectionOperator = new SelectionOperator(createSelectAll(dv, rowsToSelectionConverter));
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
                selection = selectionOperator.getSelection();
            }
            break;
            case EITHER_A_OR_B_BUT_NOT_BOTH: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                Selection selectionA = getSelectionFromVizDef(listeningVizDef);
                Selection selectionB = rowsToSelectionConverter.toSelection(rowIds);
                SelectionOperator selectionOperatorA = new SelectionOperator(selectionA.copy());
                SelectionOperator selectionOperatorB = new SelectionOperator(selectionB.copy());
                selectionOperatorA.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selectionOperatorB.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
                selectionOperatorB.operateOnSelection(BroadcastRequestType.SELECTION_ADD, selectionOperatorA.getSelection());
                selection = selectionOperatorB.getSelection();

            }
            break;
            case A_IF_AND_ONLY_IF_B: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                Selection selectAll = createSelectAll(dv, rowsToSelectionConverter);
                Selection selectionA = getSelectionFromVizDef(listeningVizDef);
                Selection selectionB = rowsToSelectionConverter.toSelection(rowIds);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_ADD, getAndSelection(getSelectionFromVizDef(listeningVizDef), rowsToSelectionConverter.toSelection(rowIds)));
                selection = selectionOperator.getSelection();
            }
            break;
            case A:
                //Need copy here or else we clear selections by reference
                selection = getSelectionFromVizDef(listeningVizDef).copy();
                break;
            case NOT_BOTH: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                Selection selectAll = createSelectAll(dv, rowsToSelectionConverter);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, getAndSelection(getSelectionFromVizDef(listeningVizDef), rowsToSelectionConverter.toSelection(rowIds)));
                selection = selectionOperator.getSelection();
            }
            break;
            case IF_B_THEN_A: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                Selection selectAll = createSelectAll(dv, rowsToSelectionConverter);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, getBNotASelection(getSelectionFromVizDef(listeningVizDef), rowsToSelectionConverter.toSelection(rowIds)));
                selection = selectionOperator.getSelection();
            }
            break;
            case IF_A_THEN_B: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                Selection selectAll = createSelectAll(dv, rowsToSelectionConverter);
                SelectionOperator selectionOperator = new SelectionOperator(selectAll);
                selectionOperator.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, getANotBSelection(getSelectionFromVizDef(listeningVizDef), rowsToSelectionConverter.toSelection(rowIds)));
                selection = selectionOperator.getSelection();
            }
            break;
            case B_AND_NOT_A: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                selection = getBNotASelection(getSelectionFromVizDef(listeningVizDef), rowsToSelectionConverter.toSelection(rowIds));
            }
            break;
            case AND: {
                Set<Integer> rowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, getSelectionFromVizDef(broadcastingVizDef));
                selection = getAndSelection(getSelectionFromVizDef(listeningVizDef), rowsToSelectionConverter.toSelection(rowIds));
            }
            break;
        }

        Selection selectionA = getSelectionFromVizDef(listeningVizDef);
        SelectionOperator selectionOperatorA = new SelectionOperator(selectionA);
        selectionOperatorA.operateOnSelection(BroadcastRequestType.SELECTION_REPLACE, selection);
        selection = selectionOperatorA.getSelection();
//        addBroadcastingSelectionToCache(request, broadcastingVizDef);
        addListeningSelectionToCache(request, listeningVizDef, selection);
        if (listeningVizDef instanceof MapViewDef) {
         MapServiceUtil.addSelection(request.getDataViewUuid(), listeningVizDef.getUuid(), selectionBroadcastCache.getSelection(listeningVizDef.getUuid()));
      }

        return selection;

    }

    private Selection getBNotASelection(Selection selectionA, Selection selectionB) {
        Selection selection;
        SelectionOperator selectionOperatorB = new SelectionOperator(selectionB.copy());
        selectionOperatorB.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA);
        selection = selectionOperatorB.getSelection();
        return selection;
    }

    private Selection getANotBSelection(Selection selectionA, Selection selectionB) {
        Selection selection;
        SelectionOperator selectionOperatorA = new SelectionOperator(selectionA.copy());
        selectionOperatorA.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB);
        selection = selectionOperatorA.getSelection();
        return selection;
    }

    private Selection getAndSelection(Selection selectionA, Selection selectionB) {
        Selection selection;//TODO: might be easier way...

        SelectionOperator selectionOperatorA = new SelectionOperator(selectionA.copy());
        SelectionOperator selectionOperatorB = new SelectionOperator(selectionB.copy());
        SelectionOperator selectionOperatorC = new SelectionOperator(selectionB.copy());
        selectionOperatorA.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionB); //A!B
        selectionOperatorB.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionA); //B!A
        selectionOperatorC.operateOnSelection(BroadcastRequestType.SELECTION_ADD, selectionA); //B||A
        selectionOperatorC.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionOperatorA.getSelection()); //B||A&&!(A!B)
        selectionOperatorC.operateOnSelection(BroadcastRequestType.SELECTION_REMOVE, selectionOperatorB.getSelection()); //B||A&&!(A!B)&&!(B!A)
        selection = selectionOperatorC.getSelection();
        return selection;
    }

    private Selection createSelectAll(DataView dv, RowsToSelectionConverter rowsToSelectionConverter) {
        Selection selection;
        DiscreteDomain<Integer> integers = DiscreteDomain.integers();

        List<Integer> ids = new ArrayList<Integer>();
        DataCacheHelper.verifyDataviewHasSize(dv);

        List<ValuePair<Long, Long>> ranges = dv.getInternalIdRanges();
        for (ValuePair<Long, Long> valuePair : ranges) {

            Long value1 = valuePair.getValue1();
            Long value2 = valuePair.getValue2();

            Range<Integer> range = Range.closed(value1.intValue(), value2.intValue());
            ids.addAll(ContiguousSet.create(range, integers));
        }

        //Range<Integer> range = Range.openClosed(0, (int) dv.getSize());
        selection = rowsToSelectionConverter.toSelection(Sets.newHashSet(ids));
        return selection;
    }


    private void addListeningSelectionToCache(BroadcastRequest request, VisualizationDef listeningVizDef, Selection listeningSelection) {
        if (listeningVizDef instanceof RelGraphViewDef) {
            addGraphInternalSelection(listeningVizDef);
        } else if (listeningVizDef instanceof MapViewDef) {
            addMapSelection(request, listeningSelection);
        } else {
            selectionBroadcastCache.addSelection(request.getListeningVizUuid(), listeningSelection);
        }
    }

    private void addMapSelection(BroadcastRequest request, Selection listeningSelection) {
        if (listeningSelection instanceof DetailMapSelection) {
            AbstractMapSelection newListeningSelection = new SummaryMapSelection(request.getListeningVizUuid());
            DetailMapSelection detailMapSelection = (DetailMapSelection) listeningSelection;
            MapServiceUtil.selectionAddNodes(detailMapSelection.getNodes(), newListeningSelection);
            newListeningSelection.addLinks(detailMapSelection.getLinks());
            listeningSelection = newListeningSelection;
        }
        selectionBroadcastCache.addSelection(request.getListeningVizUuid(), listeningSelection);
    }

    @Override
    public void invalidateDataviewBroadcast(String uuid) {
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, uuid);

        DataModelDef model = dataView.getMeta().getModelDef();

        List<VisualizationDef> visualizations = model.getVisualizations();
        for (VisualizationDef viz : visualizations) {
            AbstractBroadcastStorageService.instance().clearBroadcast(viz.getUuid());
        }
    }

    @Override
    public void clearSelection(BroadcastRequest request) {
        VisualizationDef listeningVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getListeningVizUuid());
        clearVizState(listeningVizDef);

        if (listeningVizDef instanceof RelGraphViewDef) {
            GraphContext context = GraphServiceUtil.getGraphContext(request.getListeningVizUuid());
            if (context != null) {
                synchronized (context) {
                    SelectionModel selection = GraphHelper.getSelection(request.getListeningVizUuid(), "default.selection");
                    selection.reset();
                    context.updateDisplay();
                    selectionBroadcastCache.addSelection(request.getListeningVizUuid(), selection);
                }
            }
        } else {
            selectionBroadcastCache.clearSelection(listeningVizDef.getUuid());
        }

        if (listeningVizDef instanceof MapViewDef) {
         MapServiceUtil.addSelection(request.getDataViewUuid(), listeningVizDef.getUuid(), selectionBroadcastCache.getSelection(listeningVizDef.getUuid()));
      }
    }

    @Override
    public Selection clearBroadcast(BroadcastRequest request) {
        if (!broadcastExists(request.getBroadcasterVizUuid())) {
         return null;
      }

        DataView dv = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
//        VisualizationDef listeningVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getListeningVizUuid());
        VisualizationDef broadcastingVizDef = CsiPersistenceManager.findObject(VisualizationDef.class, request.getBroadcasterVizUuid());
        if (broadcastingVizDef instanceof MapViewDef) {
         setSelectionOnVizDef(request.getDataViewUuid(), broadcastingVizDef, request.getBroadcasterSelection());
      }

        Set<Integer> selectionRowIds = convertVizSelectionIntoRows(dv, broadcastingVizDef, request.getBroadcasterSelection());

        selectionBroadcastCache.addSelection(request.getBroadcasterVizUuid(), getSelectionFromVizDef(broadcastingVizDef));
        AbstractBroadcastStorageService.instance().clearBroadcast(request.getBroadcasterVizUuid());

        visualizationActionsService.clearVizCache(broadcastingVizDef);
        if (broadcastingVizDef instanceof MapViewDef) {
            handleMapState(dv, (MapViewDef) broadcastingVizDef);
        }

        Selection selection;
        if (selectionRowIds.isEmpty()) {
            selection = NullSelection.instance;
        } else if (broadcastingVizDef instanceof MapViewDef) {
            selection = request.getBroadcasterSelection();
        } else {
            //We create a new vizcache here so that we may create an appropriate selection to send back to client
            visualizationActionsService.createVizCache(broadcastingVizDef, dv);
            RowsToSelectionConverter rowsToSelectionConverter = new RowsToSelectionConverterFactory(dv, broadcastingVizDef).create();
            selection = rowsToSelectionConverter.toSelection(selectionRowIds);
        }

        return selection;
    }

    private boolean broadcastExists(String uuid) {
//        BroadcastResult broadcast = BroadcastStorageService.instance().getBroadcast(uuid);
//        if (broadcast.isEmpty()) {
//            return true;
//        }
        return AbstractBroadcastStorageService.instance().hasBroadcast(uuid);
    }

    public void setSelectionBroadcastCache(SelectionBroadcastCache selectionBroadcastCache) {
        this.selectionBroadcastCache = selectionBroadcastCache;
    }

    public boolean isBroadcast(String uuid) {
        return broadcastExists(uuid);
    }

}
