package csi.integration.broadcast;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.integration.hibernate.setup.InsertSimpleDataView;
import csi.integration.hibernate.setup.WrapInTransactionCommand;
import csi.server.business.service.BroadcastService;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.broadcast.BroadcastRequest;
import csi.server.common.model.broadcast.BroadcastRequestType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.selection.RowsSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSettings;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class BroadcastFilterIncludeTest {

    private final BroadcastService broadcastService = new BroadcastService();

    private VisualizationDef broadcaster;
    private VisualizationDef listener;
    private DataView dataView;

    @BeforeClass
    public static void setup(){
        InitializeCentrifuge.initialize();
    }

    @Test
    public void testBroadcastFilterInclude(){
        //FIXME: Trevor
//        insertVisualizationsWithSelection();
//        invokeBroadcastFilterInclude();
//
//        //Get the database version of the broadcaster
//        VisualizationDef broadcastingViz = CsiPersistenceManager.findObject(VisualizationDef.class, broadcaster.getUuid());
//        assertTrue(broadcastingViz.getBroadcastFilter().isCleared());
//        assertEquals(broadcastingViz.getBroadcastFilterType(), BroadcastRequestType.CLEAR);
//
//        //Get the database version of the listener
//        VisualizationDef listeningViz = CsiPersistenceManager.findObject(VisualizationDef.class, listener.getUuid());
//        assertEquals(((RowsSelection)(listeningViz.getBroadcastFilter())).getSelectedItems(), createBroadcastSelection().getSelectedItems());
//        assertEquals(listeningViz.getBroadcastFilterType(), BroadcastRequestType.FILTER_DISPLAY);
//        assertEquals(((RowsSelection)(listeningViz.getSelection())).getSelectedItems(), Lists.newArrayList("5"));
//
//        cleanupDataView();
    }

    @Test
    public void testBroadcastFilterThenClear(){
        //FIXME: Trevor
//        insertVisualizationsWithSelection();
//        invokeBroadcastFilterInclude();
//        invokeBroadcastClear();
//        invokeBroadcastAddSelection();
    }

    private void invokeBroadcastFilterInclude() {
        new WrapInTransactionCommand(){

            @Override
            protected void withinTransaction() throws Exception {
                BroadcastRequest broadcastRequest = createBroadcastIncludeRequest();
                broadcastService.broadcastFilter(broadcastRequest);
            }
        }.execute();

    }

    private void invokeBroadcastAddSelection() {
        new WrapInTransactionCommand(){
            @Override
            protected void withinTransaction() throws Exception {
                BroadcastRequest broadcastRequest = createBroadcastAddRequest();
                Selection selection = broadcastService.broadcastSelection(broadcastRequest);
                assertNotNull(selection);
                assertFalse(selection.isCleared());
            }
        }.execute();
    }

    private void invokeBroadcastClear() {
        new WrapInTransactionCommand(){

            @Override
            protected void withinTransaction() throws Exception {
                BroadcastRequest broadcastRequest = createBroadcastClearRequest();
                broadcastRequest.setBroadcastRequestType(BroadcastRequestType.CLEAR);
                broadcastService.clearBroadcast(broadcastRequest);
            }
        }.execute();
    }

    private void insertVisualizationsWithSelection() {
        new WrapInTransactionCommand(){

            @Override
            protected void withinTransaction() throws Exception {
                dataView = getInsertedDataView();
                broadcaster = addVisualizationToDataView();
                listener = addVisualizationToDataView();

            }
        }.execute();
    }

    private DataView getInsertedDataView() throws CentrifugeException {
        String dvUuid = InsertSimpleDataView.saveNewDataView("newDataView");
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        assertNotNull(dv);
        return dv;
    }

    private void cleanupDataView(){
        CsiPersistenceManager.deleteObject(DataView.class, dataView.getUuid());
    }

    private VisualizationDef addVisualizationToDataView() {
        TableViewDef tableViewDef = createTableViewDef(createTableViewSettings());
        dataView.getMeta().getModelDef().addVisualization(tableViewDef);
        return tableViewDef;
    }

    private TableViewDef createTableViewDef(TableViewSettings tableViewSettings) {
        TableViewDef tableViewDef = new TableViewDef();
        tableViewDef.setType(VisualizationType.TABLE);
        tableViewDef.setTableViewSettings(tableViewSettings);
        setSelectionOnVizDef(tableViewDef);

        return tableViewDef;
    }

    private void setSelectionOnVizDef(TableViewDef tableViewDef) {
        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.makeSelectionStateForRows(new ArrayList<Integer>(Arrays.asList(5, 10, 20)));
        tableViewDef.getSelection().setFromSelection(rowsSelection);
    }

    private TableViewSettings createTableViewSettings() {
        TableViewSettings tableViewSettings = new TableViewSettings();
        int i = 0;
        for (FieldDef fieldDef : dataView.getMeta().getFieldList()) {
            VisibleTableField vtf = new VisibleTableField();
            vtf.setFieldDef(fieldDef);
            tableViewSettings.getVisibleFields().add(vtf);
        }
        return tableViewSettings;
    }

    private BroadcastRequest createBroadcastClearRequest() {
        BroadcastRequest broadcastRequest = new BroadcastRequest();
        broadcastRequest.setBroadcasterVizUuid(listener.getUuid());
        broadcastRequest.setDataViewUuid(dataView.getUuid());
        broadcastRequest.setBroadcasterSelection(createBroadcastSelection());
        broadcastRequest.setBroadcastRequestType(BroadcastRequestType.CLEAR);
        return broadcastRequest;
    }

    private BroadcastRequest createBroadcastIncludeRequest() {
        BroadcastRequest broadcastRequest = new BroadcastRequest();
        broadcastRequest.setBroadcasterVizUuid(broadcaster.getUuid());
        broadcastRequest.setDataViewUuid(dataView.getUuid());
        broadcastRequest.setListeningVizUuid(listener.getUuid());
        broadcastRequest.setBroadcasterSelection(createBroadcastSelection());
        broadcastRequest.setListeningVizSelection(listener.getSelection());
        broadcastRequest.setBroadcastRequestType(BroadcastRequestType.FILTER_DISPLAY);
        return broadcastRequest;
    }

    private BroadcastRequest createBroadcastAddRequest() {
        BroadcastRequest broadcastRequest = new BroadcastRequest();
        broadcastRequest.setBroadcasterVizUuid(broadcaster.getUuid());
        broadcastRequest.setDataViewUuid(dataView.getUuid());
        broadcastRequest.setListeningVizUuid(listener.getUuid());
        broadcastRequest.setBroadcasterSelection(createBroadcastSelection());
        broadcastRequest.setListeningVizSelection(listener.getSelection());
        broadcastRequest.setBroadcastRequestType(BroadcastRequestType.SELECTION_ADD);
        return broadcastRequest;
    }

    private RowsSelection createBroadcastSelection() {
        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.makeSelectionStateForRows(new ArrayList<Integer>(Arrays.asList(1, 3, 5)));
        return rowsSelection;
    }
}
