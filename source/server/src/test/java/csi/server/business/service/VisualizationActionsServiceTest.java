package csi.server.business.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.ModelHelper;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DrillDownChartViewDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.dao.CsiPersistenceManager;

public class VisualizationActionsServiceTest {

    private VisualizationActionsService vas;

    @Before
    public void setUp() throws Exception {
        InitializeCentrifuge.initialize();
        CsiPersistenceManager.begin();
        vas = new VisualizationActionsService();
    }

    @After
    public void tearDown() throws Exception {
        vas = null;
        CsiPersistenceManager.close();
    }

    @Test
    public void testAddVisualization() throws Exception {
        // create test dataview
        DataViewDef dvdef = createTestDataViewDef("test template", "template for unit testing");
        DataViewHelper dvHelper = new DataViewHelper();
        DataView dv = dvHelper.createDataView("Test 1", dvdef);

        // create a default worksheet
        WorksheetDef wsd = createTestWorksheetDef("Sheet1");

        dv.getMeta().getModelDef().addWorksheet(wsd);
        ModelHelper.save(dv);
        CsiPersistenceManager.flush();

        // create a tableviz
        String vizname = "Test Table";
        TableViewDef viz = (TableViewDef) createTestVisualizationDef(vizname, VisualizationType.TABLE);

        vas.addVisualization(viz, dv.getUuid(), wsd.getUuid());
        CsiPersistenceManager.flush();

        TableViewDef viz2 = (TableViewDef) CsiPersistenceManager.findObject(TableViewDef.class, viz.getUuid());
        assertEquals(vizname, viz2.getName());
    }

    @Test
    public void testAddVisualizations() throws Exception {
        // create test dataview
        DataViewDef dvdef = createTestDataViewDef("test template", "template for unit testing");
        DataViewHelper dvHelper = new DataViewHelper();
        DataView dv = dvHelper.createDataView("Test 1", dvdef);

        // create a default worksheet
        WorksheetDef wsd = createTestWorksheetDef("Sheet1");

        dv.getMeta().getModelDef().addWorksheet(wsd);
        ModelHelper.save(dv);
        CsiPersistenceManager.flush();

        // create 5 visualizations
        TableViewDef tableviz = (TableViewDef) createTestVisualizationDef("Test Table", VisualizationType.TABLE);
        RelGraphViewDef graphviz = (RelGraphViewDef) createTestVisualizationDef("Test Graph",
                VisualizationType.RELGRAPH_V2);
//        TimelineViewDef_V1 timelineviz = (TimelineViewDef_V1) createTestVisualizationDef("Test Timeline",
//                VisualizationType.TIMELINE);
        DrillDownChartViewDef drilldownviz = (DrillDownChartViewDef) createTestVisualizationDef("Test Drilldown",
                VisualizationType.BAR_CHART);

        ArrayList<VisualizationDef> vizs = new ArrayList<VisualizationDef>();
        vizs.add(tableviz);
        /*vizs.add(chartviz);*/
        vizs.add(graphviz);
       // vizs.add(timelineviz);
        vizs.add(drilldownviz);

        for (VisualizationDef viz : vizs) {
            vas.addVisualization(viz, dv.getUuid(), wsd.getUuid());
            CsiPersistenceManager.flush();
        }
        DataView dv2 = (DataView) CsiPersistenceManager.findObject(DataView.class, dv.getUuid());
        assertEquals(4, dv2.getMeta().getModelDef().getVisualizations().size());
        assertEquals(1, dv2.getMeta().getModelDef().getWorksheets().size());
    }



    @Test
    public void testAddWorksheet() throws Exception {
        // create a dataview
        DataViewDef dvdef = createTestDataViewDef("test template", "template for unit testing");
        DataViewHelper dvHelper = new DataViewHelper();
        DataView dv = dvHelper.createDataView("Test 2", dvdef);

        // create a worksheet
        WorksheetDef wsd = createTestWorksheetDef("Sheet2");

        ModelHelper.save(dv);
        CsiPersistenceManager.flush();

        vas.addWorksheet(wsd, dv.getUuid());
        CsiPersistenceManager.flush();

        // retrieve the worksheet just added
        DataView dv2 = (DataView) CsiPersistenceManager.findObject(DataView.class, dv.getUuid());
        List<WorksheetDef> wsds = dv2.getMeta().getModelDef().getWorksheets();
        assertEquals(1, wsds.size());
        assertEquals(wsd.getUuid(), wsds.get(0).getUuid());
        assertEquals(wsd.getWorksheetName(), wsds.get(0).getWorksheetName());
    }

    @Test
    public void testAddWorksheets() throws Exception {
        // create a dataview
        DataViewDef dvdef = createTestDataViewDef("test template", "template for unit testing");
        DataViewHelper dvHelper = new DataViewHelper();
        DataView dv = dvHelper.createDataView("Test 3", dvdef);

        // create 3 worksheets
        WorksheetDef wsd1 = createTestWorksheetDef("Sheet 1");
        WorksheetDef wsd2 = createTestWorksheetDef("Sheet 2");
        WorksheetDef wsd3 = createTestWorksheetDef("Sheet 3");

        ModelHelper.save(dv);
        CsiPersistenceManager.flush();

        ArrayList<WorksheetDef> wsds = new ArrayList<WorksheetDef>();
        wsds.add(wsd1);
        wsds.add(wsd2);
        wsds.add(wsd3);

        for (WorksheetDef d : wsds) {
            vas.addWorksheet(d, dv.getUuid());
        }
        CsiPersistenceManager.flush();
        // retrieve the worksheets just added
        DataView dv2 = (DataView) CsiPersistenceManager.findObject(DataView.class, dv.getUuid());
        assertEquals(3, dv2.getMeta().getModelDef().getWorksheets().size());
    }

/*    @Test
    public void testSaveSettings() throws Exception {
        // create test dataview
        DataViewDef dvdef = createTestDataViewDef("test template", "template for unit testing");
        DataViewHelper dvHelper = new DataViewHelper();
        DataView dv = dvHelper.createDataView("Test 1", dvdef);

        // create a chart viz
        String oldcharttype = "VerticalBarChart"; // vertical bar chart
        String oldvizname = "Test Chart";
        ChartViewDef viz = (ChartViewDef) createTestVisualizationDef(oldvizname, VisualizationType.CHART);
        viz.setChartType(oldcharttype);

        dv.getMeta().getModelDef().addVisualization(viz);
        ModelHelper.save(dv);
        CsiPersistenceManager.flush();

        // now update chart viz
        String newcharttype = "OlapChart"; // matrix chart
        String newvizname = "Test Chart - matrix";
        ChartViewDef viz2 = (ChartViewDef) CsiPersistenceManager.findObject(ChartViewDef.class, viz.getUuid());
        viz2.setSuppressNulls(false);
        viz2.setChartType(newcharttype);
        viz2.setName(newvizname);

        vas.saveSettings(viz2, dv.getUuid(), false);

        // find the chart viz after saved settings
        ChartViewDef viz3 = (ChartViewDef) CsiPersistenceManager.findObject(ChartViewDef.class, viz.getUuid());
        assertEquals(newvizname, viz3.getName());
        assertEquals(newcharttype, viz3.getChartType());
        assertFalse(viz3.isSuppressNulls());
    }*/

    /*
    @Test
    public void testGetFilterConstraints() {
    	fail("Not yet implemented");
    }
    */

/*    @Test
    public void testGetVisualization() throws Exception {
        // create test dataview
        DataViewDef dvdef = createTestDataViewDef("test template", "template for unit testing");
        DataViewHelper dvHelper = new DataViewHelper();
        DataView dv = dvHelper.createDataView("Test 1", dvdef);
        // create a chartviz
        String vizname = "Test Chart";
        ChartViewDef viz = (ChartViewDef) createTestVisualizationDef(vizname, VisualizationType.CHART);
        dv.getMeta().getModelDef().addVisualization(viz);
        ModelHelper.save(dv);
        CsiPersistenceManager.flush();

        ChartViewDef viz2 = (ChartViewDef) CsiPersistenceManager.findObject(ChartViewDef.class, viz.getUuid());
        assertEquals(vizname, viz2.getName());
        assertEquals(VisualizationType.CHART, viz2.getType());
    }*/

    @Test
    public void testSetWorksheetName() throws Exception {
        String oldname = "Monthly Sales Report";
        String newname = "Mondthly Sales Report 2";

        // create a dataview
        DataViewDef dvdef = createTestDataViewDef("test template", "template for unit testing");
        DataViewHelper dvHelper = new DataViewHelper();
        DataView dv = dvHelper.createDataView("Test 2", dvdef);

        // create a worksheet
        WorksheetDef wsd = createTestWorksheetDef(oldname);
        dv.getMeta().getModelDef().addWorksheet(wsd);
        ModelHelper.save(dv);
        CsiPersistenceManager.flush();

        // retrieve the worksheet just added
        WorksheetDef wsd2 = (WorksheetDef) CsiPersistenceManager.findObject(WorksheetDef.class, wsd.getUuid());
        assertEquals(oldname, wsd2.getWorksheetName());

        try {
            // now set a new name to the worksheet
            vas.setWorksheetName(dv.getUuid(), wsd.getUuid(), newname);
            CsiPersistenceManager.flush();
            
        } catch (Exception myException) {
            
        }

        WorksheetDef wsd3 = (WorksheetDef) CsiPersistenceManager.findObject(WorksheetDef.class, wsd.getUuid());
        assertEquals(newname, wsd3.getWorksheetName());
    }

    private DataViewDef createTestDataViewDef(String name, String remarks) {

        DataViewDef dvdef = new DataViewDef(ReleaseInfo.version);
        dvdef.setName(name);
        dvdef.setRemarks(remarks);
        dvdef.setTemplate(true);

        DataModelDef dataModelDef = new DataModelDef();
        dataModelDef.setWorksheets(new ArrayList<WorksheetDef>());
        dataModelDef.setVisualizations(new ArrayList<VisualizationDef>());
        dataModelDef.setFieldDefs(new ArrayList<FieldDef>());
        dvdef.setModelDef(dataModelDef);

        return dvdef;
    }

    private WorksheetDef createTestWorksheetDef(String name) {
        WorksheetDef wsd = new WorksheetDef();
        wsd.setWorksheetName(name);
        return wsd;
    }

    private VisualizationDef createTestVisualizationDef(String name, VisualizationType type) {

        VisualizationDef viz = null;
        switch (type) {
            case TABLE:
                viz = new TableViewDef();
                viz.setType(VisualizationType.TABLE);
                break;
/*            case CHART:
                viz = new ChartViewDef();
                viz.setType(VisualizationType.CHART);
                break;*/
            case RELGRAPH_V2:
                viz = new RelGraphViewDef();
                viz.setType(VisualizationType.RELGRAPH_V2);
                break;
//            case TIMELINE:
//                viz = new TimelineViewDef_V1();
//                viz.setType(VisualizationType.TIMELINE);
//                break;
            case BAR_CHART:
                viz = new DrillDownChartViewDef();
                viz.setType(VisualizationType.BAR_CHART);
                break;
        }
        viz.setName(name);
        return viz;
    }

    private ArrayList<String> createItems(String base, int count) {

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < count; i++)
            list.add(base + i);

        return list;
    }

}
