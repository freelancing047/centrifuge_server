package csi.integration.broadcast;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.integration.hibernate.setup.InsertSimpleDataView;
import csi.integration.hibernate.setup.WrapInTransactionCommand;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.BroadcastService;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.broadcast.BroadcastRequest;
import csi.server.common.model.broadcast.BroadcastRequestType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSettings;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ClearBroadcastTest {

    private VisualizationDef broadcaster;
    private VisualizationDef listener;
    private DataView dataView;

    @BeforeClass
    public static void setup(){
        InitializeCentrifuge.initialize();
    }

    @Test
    public void testClearBroadcastFilter(){

        insertVisualizations();

        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.getSelectedItems().addAll(new ArrayList<Integer>(Arrays.asList(5, 10, 20)));
        AbstractBroadcastStorageService.instance().addBroadcast(broadcaster.getUuid(), rowsSelection, false);
        AbstractBroadcastStorageService.instance().addBroadcast(listener.getUuid(), rowsSelection, true);

        invokeClearBroadcast();

        verifyBroadcasterIsCleared();
        verifyListenerIsNotCleared();

        cleanupDataView();
    }

    private void verifyListenerIsNotCleared() {
        BroadcastResult broadcastResult = AbstractBroadcastStorageService.instance().getBroadcast(listener.getUuid());
        assertFalse(broadcastResult.getBroadcastFilter().isCleared());
        assertTrue(broadcastResult.isExcludeRows());
    }

    private void verifyBroadcasterIsCleared() {
        BroadcastResult broadcastResult = AbstractBroadcastStorageService.instance().getBroadcast(broadcaster.getUuid());
        assertTrue(broadcastResult.getBroadcastFilter().isCleared());
        assertFalse(broadcastResult.isExcludeRows());
    }

    private void invokeClearBroadcast() {
        new WrapInTransactionCommand(){

            @Override
            protected void withinTransaction() throws Exception {
                BroadcastService broadcastService = new BroadcastService();
                broadcastService.setSelectionBroadcastCache(SelectionBroadcastCache.getInstance());
                BroadcastRequest broadcastRequest = createClearBroadcastRequest();
                broadcastService.clearBroadcast(broadcastRequest);
            }
        }.execute();
    }

    private void insertVisualizations() {
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

        return tableViewDef;
    }

    private TableViewSettings createTableViewSettings() {
        TableViewSettings tableViewSettings = new TableViewSettings();
        for (FieldDef fieldDef : dataView.getMeta().getFieldList()) {
            VisibleTableField vtf = new VisibleTableField();
            vtf.setFieldDef(fieldDef);
            tableViewSettings.getVisibleFields().add(vtf);
        }
        return tableViewSettings;
    }

    private BroadcastRequest createClearBroadcastRequest() {
        BroadcastRequest broadcastRequest = new BroadcastRequest();
        broadcastRequest.setBroadcasterVizUuid(broadcaster.getUuid());
        broadcastRequest.setDataViewUuid(dataView.getUuid());
        broadcastRequest.setListeningVizUuid(listener.getUuid());
        broadcastRequest.setBroadcasterSelection(new IntegerRowsSelection());
        broadcastRequest.setListeningVizSelection(new IntegerRowsSelection());
        broadcastRequest.setBroadcastRequestType(BroadcastRequestType.CLEAR);
        return broadcastRequest;
    }
}
