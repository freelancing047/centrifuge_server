package csi.integration.hibernate;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.integration.hibernate.setup.InsertSimpleDataView;
import csi.integration.hibernate.setup.WrapInTransactionCommand;
import csi.server.business.service.ModelActionService;
import csi.server.business.service.VisualizationActionsService;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.visualization.chart.ChartType;

/**
 * Insert a visualizationDef in a dataview and delete it.
 * Verifies that deletion cleans up a visualizationDef from the database.
 * @author Centrifuge Systems, Inc.
 */
public class TestInsertDeleteVisualization {

    private VisualizationActionsService service = new VisualizationActionsService();
    private ModelActionService modelActionService = new ModelActionService();

    @BeforeClass
    public static void setup(){
        InitializeCentrifuge.initialize();
    }

    @Test
    public void testAddAndRemoveVisualization() throws Exception{
        new WrapInTransactionCommand(){

            @Override
            protected void withinTransaction() throws Exception {
                DataView dv = getInsertedDataView();

                VisualizationDef visualizationDef = persistNewVisualization(dv);
                verifyVisualizationExists(visualizationDef.getUuid());

                deleteVisualization(dv.getUuid(), visualizationDef.getUuid());
                verifyVisualizationDoesNotExist(visualizationDef.getUuid());

                cleanupDataView(dv.getUuid());
            }
        }.execute();

    }

    private void cleanupDataView(String dvUuid) throws CentrifugeException {
        CsiPersistenceManager.deleteObject(DataView.class, dvUuid);
    }

    private DataView getInsertedDataView() throws CentrifugeException {
        String dvUuid = InsertSimpleDataView.saveNewDataView("newDataView");
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        assertNotNull(dv);
        return dv;
    }

    private void verifyVisualizationExists(String vizUuid) throws CentrifugeException {
        VisualizationDef viz = CsiPersistenceManager.findObject(VisualizationDef.class, vizUuid);
        assertNotNull(viz);
    }

    private void deleteVisualization(String dvUuid, String vizUuid) {
        try {
            service.deleteVisualization(dvUuid, vizUuid);
            CsiPersistenceManager.flush();
            
        } catch (Exception myException) {
            
        }
    }

    private void verifyVisualizationDoesNotExist(String vizUuid) throws CentrifugeException {
        VisualizationDef viz = CsiPersistenceManager.findObject(VisualizationDef.class, vizUuid);
        assertNull(viz);
    }


    private VisualizationDef persistNewVisualization(DataView dv) throws CentrifugeException {
        DrillChartViewDef tableViewDef = addVisualizationToDataView(dv);
        addVisualizationLayout(dv, tableViewDef);
        CsiPersistenceManager.flush();
        return tableViewDef;
    }

    private DrillChartViewDef addVisualizationToDataView(DataView dv) {
        ChartSettings tableViewSettings = createTableViewSettings(dv);
        DrillChartViewDef tableViewDef = createTableViewDef(tableViewSettings);
        try {
            service.addVisualization(tableViewDef, dv.getUuid(), getWorksheetUuid(dv));
            return tableViewDef;
            
        } catch (Exception myException) {
            
            return null;
        }
    }

    private void addVisualizationLayout(DataView dv, DrillChartViewDef tableViewDef) throws CentrifugeException {
        WorksheetDef worksheetDef = CsiPersistenceManager.findObject(WorksheetDef.class, getWorksheetUuid(dv));
        worksheetDef.getWorksheetScreenLayout().setActivatedVisualizationUuid(tableViewDef.getUuid());
        worksheetDef.getWorksheetScreenLayout().getLayout().getLayoutState(tableViewDef);
        modelActionService.save(worksheetDef);
    }

    private String getWorksheetUuid(DataView dv) {
        return dv.getMeta().getModelDef().getWorksheets().get(0).getUuid();
    }

    private DrillChartViewDef createTableViewDef(ChartSettings tableViewSettings) {
        DrillChartViewDef tableViewDef = new DrillChartViewDef();
        tableViewDef.setType(VisualizationType.DRILL_CHART);
        tableViewDef.setChartSettings(tableViewSettings);
        DrillCategory drillCategory = new DrillCategory();
        ArrayList<String> category = new ArrayList<String>();
        category.add("Test");
        drillCategory.setCategories(category);
        tableViewDef.getSelection().getSelectedItems().add(drillCategory);
        tableViewDef.setDrillSelection(drillCategory);
        return tableViewDef;
    }

    private ChartSettings createTableViewSettings(DataView dv) {
        ChartSettings tableViewSettings = new ChartSettings();
        int i = 0;
        for (FieldDef fieldDef : dv.getMeta().getFieldList()) {
            CategoryDefinition vtf = new CategoryDefinition();
            vtf.setChartType(ChartType.AREA);
            vtf.setFieldDef(fieldDef);

            tableViewSettings.getCategoryDefinitions().add(vtf);
        }
        return tableViewSettings;
    }

}
