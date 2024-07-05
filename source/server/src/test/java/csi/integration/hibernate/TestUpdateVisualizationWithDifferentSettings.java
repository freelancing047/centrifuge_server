package csi.integration.hibernate;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.integration.hibernate.setup.InsertSimpleDataView;
import csi.integration.hibernate.setup.WrapInTransactionCommand;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.service.VisualizationActionsService;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSettings;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.dao.CsiPersistenceManager;

/**
 * Tests we can update a visualizationDef and that the
 * settings are not duplicated within the database.
 *
 * @author Centrifuge Systems, Inc.
 */
public class TestUpdateVisualizationWithDifferentSettings {

    private VisualizationActionsService visualizationActionsService = new VisualizationActionsService();

    @BeforeClass
    public static void setup(){
        InitializeCentrifuge.initialize();
    }

    @Test
    public void testAddAndRemoveVisualizationDeletesVisibleTableField() throws Exception{
        visualizationActionsService.setSelectionBroadcastCache(SelectionBroadcastCache.getInstance());
        new WrapInTransactionCommand(){
            @Override
            protected void withinTransaction() throws Exception {
                TestUpdateVisualizationWithDifferentSettings tmv = new TestUpdateVisualizationWithDifferentSettings();
                DataView dv = tmv.getInsertedDataView();
                TableViewDef vizDef = tmv.persistNewVisualization(dv);

                TableViewSettings tableViewSettings = new TableViewSettings();
                VisibleTableField vtf = addVisibleTableField(dv, tableViewSettings);

                vizDef.setTableViewSettings(tableViewSettings);
                visualizationActionsService.saveSettings(vizDef, dv.getUuid(), null);
                visualizationActionsService.deleteVisualization(dv.getUuid(), vizDef.getUuid());

                assertNull(CsiPersistenceManager.findObject(VisibleTableField.class, vtf.getUuid()));
            }
        }.execute();
    }

    private DataView getInsertedDataView() throws CentrifugeException {
        String dvUuid = InsertSimpleDataView.saveNewDataView("newDataView");
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        assertNotNull(dv);
        return dv;
    }

    private TableViewDef persistNewVisualization(DataView dv) throws CentrifugeException {
        TableViewDef tableViewDef = addVisualizationToDataView(dv);
        CsiPersistenceManager.flush();
        return tableViewDef;
    }

    private TableViewDef addVisualizationToDataView(DataView dv) {
        TableViewSettings tableViewSettings = createTableViewSettings(dv);
        TableViewDef tableViewDef = createTableViewDef(tableViewSettings);
        try {
            visualizationActionsService.addVisualization(tableViewDef, dv.getUuid(), getWorksheetUuid(dv));
            return tableViewDef;
            
        } catch (Exception myException) {
            
            return null;
        }
    }

    private String getWorksheetUuid(DataView dv) {
        return dv.getMeta().getModelDef().getWorksheets().get(0).getUuid();
    }

    private TableViewDef createTableViewDef(TableViewSettings tableViewSettings) {
        TableViewDef tableViewDef = new TableViewDef();
        tableViewDef.setType(VisualizationType.TABLE);
        tableViewDef.setTableViewSettings(tableViewSettings);
        return tableViewDef;
    }

    private TableViewSettings createTableViewSettings(DataView dv) {
        TableViewSettings tableViewSettings = new TableViewSettings();
        for (FieldDef fieldDef : dv.getMeta().getFieldList()) {
            VisibleTableField vtf = new VisibleTableField();
            vtf.setFieldDef(fieldDef);
            tableViewSettings.getVisibleFields().add(vtf);
        }
        return tableViewSettings;
    }

    private VisibleTableField addVisibleTableField(DataView dv, TableViewSettings tableViewSettings) {
        VisibleTableField vtf = new VisibleTableField();

        vtf.setFieldDef(dv.getMeta().getFieldList().get(0));
        tableViewSettings.setVisibleFields(Lists.newArrayList(vtf));
        return vtf;
    }

}
