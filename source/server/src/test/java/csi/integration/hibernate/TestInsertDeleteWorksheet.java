package csi.integration.hibernate;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.integration.hibernate.setup.InsertSimpleDataView;
import csi.integration.hibernate.setup.WrapInTransactionCommand;
import csi.server.business.service.VisualizationActionsService;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSettings;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.dao.CsiPersistenceManager;

/**
 * Insert a worksheetDef in a dataview and delete it.
 * Verifies that deletion cleans up a worksheetDef from the database, including visualizationDefs that exists within it.
 * @author Centrifuge Systems, Inc.
 */
public class TestInsertDeleteWorksheet {

    private VisualizationActionsService service = new VisualizationActionsService();

    @BeforeClass
    public static void setup(){
        InitializeCentrifuge.initialize();
    }

    @Test
    public void testAddAndRemoveWorksheet() throws Exception{
        new WrapInTransactionCommand(){

            @Override
            protected void withinTransaction() throws Exception {
                DataView dv = getInsertedDataView();
                String wksUuid = persistNewWorksheet(dv);
                verifyWorksheetExists(wksUuid);

                deleteWorksheet(dv.getUuid(), wksUuid);
                verifyWorksheetDoesNotExist(wksUuid);
            }
        }.execute();
    }

    @Test
    public void testAddAndRemoveWorksheetWithVisualization() throws Exception{
        new WrapInTransactionCommand(){
            @Override
            protected void withinTransaction() throws Exception {
                DataView dv = getInsertedDataView();
                String wksUuid = persistNewWorksheet(dv);
                verifyWorksheetExists(wksUuid);

                VisualizationDef vizDef = addVisualizationToWorksheet(dv, wksUuid);

                deleteWorksheet(dv.getUuid(), wksUuid);
                verifyWorksheetDoesNotExist(wksUuid);
                verifyVisualizationDoesNotExist(vizDef.getUuid());
            }
        }.execute();

    }

    private VisualizationDef addVisualizationToWorksheet(DataView dv, String wksUuid) throws CentrifugeException {
        TableViewSettings tableViewSettings = createTableViewSettings(dv);
        TableViewDef tableViewDef = createTableViewDef(tableViewSettings);
        service.addVisualization(tableViewDef, dv.getUuid(), wksUuid);
        CsiPersistenceManager.flush();
        addVisualizationLayout(dv,tableViewDef,wksUuid);
        return tableViewDef;
    }

    private String persistNewWorksheet(DataView dv) {
        WorksheetDef worksheetDef = new WorksheetDef();
        worksheetDef.setWorksheetName("Test");
        try {
            service.addWorksheet(worksheetDef, dv.getUuid());
            CsiPersistenceManager.flush();
            return worksheetDef.getUuid();
            
        } catch (Exception myException) {
            
            return null;
        }
    }

    private DataView getInsertedDataView() throws CentrifugeException {
        String dvUuid = InsertSimpleDataView.saveNewDataView("newDataView");
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        assertNotNull(dv);
        return dv;
    }

    private void verifyWorksheetExists(String wksUuid) throws CentrifugeException {
        WorksheetDef wks = CsiPersistenceManager.findObject(WorksheetDef.class, wksUuid);
        assertNotNull(wks);
    }


    private void verifyVisualizationDoesNotExist(String uuid) throws CentrifugeException {
        VisualizationDef viz = CsiPersistenceManager.findObject(VisualizationDef.class, uuid);
        assertNull(viz);

    }

    private void deleteWorksheet(String dvUuid, String wksUuid) throws CentrifugeException {
        WorksheetDef wks = CsiPersistenceManager.findObject(WorksheetDef.class, wksUuid);
        service.removeWorksheet(wks, dvUuid);
        CsiPersistenceManager.flush();
    }

    private void verifyWorksheetDoesNotExist(String wksUuid) throws CentrifugeException {
        WorksheetDef wks = CsiPersistenceManager.findObject(WorksheetDef.class, wksUuid);
        assertNull(wks);
    }

    private void addVisualizationLayout(DataView dv, TableViewDef tableViewDef, String wksUuid) throws CentrifugeException {
        WorksheetDef worksheetDef = CsiPersistenceManager.findObject(WorksheetDef.class, wksUuid);
        worksheetDef.getWorksheetScreenLayout().setActivatedVisualizationUuid(tableViewDef.getUuid());
        worksheetDef.getWorksheetScreenLayout().getLayout().getLayoutState(tableViewDef);
        CsiPersistenceManager.merge(worksheetDef);
        CsiPersistenceManager.flush();
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
}
