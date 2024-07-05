package csi.integration.hibernate;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.integration.hibernate.setup.WrapInTransactionCommand;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.visualization.chart.ChartType;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TestListOrdering {

    final DrillChartViewDef drillChartViewDef = new DrillChartViewDef();
    final String myUuid = drillChartViewDef.getUuid();

    @BeforeClass
    public static void setup(){
        InitializeCentrifuge.initialize();
    }

    @Test
    public void testOrderingOfCategories(){
        ChartSettings chartSettings = buildChartSettingsWithAreaFirstLineSecond();
        drillChartViewDef.setChartSettings(chartSettings);
        assertAreaIsFirstAndLineIsSecond(drillChartViewDef);

        final DrillChartViewDef drillChartViewDef2 = buildChartWithAreaFirstLineSecondListPositionsReveresed(chartSettings);
        assertAreaIsFirstAndLineIsSecond(drillChartViewDef2);

        assertLineFirstAreaSecondFromHibernate();
    }

    private void assertLineFirstAreaSecondFromHibernate() {
        new WrapInTransactionCommand() {
            @Override
            protected void withinTransaction() throws Exception {
                DrillChartViewDef chart = CsiPersistenceManager.findObject(DrillChartViewDef.class, myUuid);
                assertEquals(ChartType.LINE, chart.getChartSettings().getCategoryDefinitions().get(0).getChartType());
                assertEquals(ChartType.AREA, chart.getChartSettings().getCategoryDefinitions().get(1).getChartType());
            }
        }.execute();
    }

    private void assertAreaIsFirstAndLineIsSecond(final DrillChartViewDef drillChartViewDef2) {
        new WrapInTransactionCommand() {
            @Override
            protected void withinTransaction() throws Exception {
                DrillChartViewDef chart = CsiPersistenceManager.merge(drillChartViewDef2);
                assertEquals(ChartType.AREA, chart.getChartSettings().getCategoryDefinitions().get(0).getChartType());
                assertEquals(ChartType.LINE, chart.getChartSettings().getCategoryDefinitions().get(1).getChartType());
            }
        }.execute();
    }

    private ChartSettings buildChartSettingsWithAreaFirstLineSecond() {
        CategoryDefinition categoryDefinitionArea = new CategoryDefinition();
        categoryDefinitionArea.setChartType(ChartType.AREA);
        categoryDefinitionArea.setListPosition(0);

        CategoryDefinition categoryDefinitionLine = new CategoryDefinition();
        categoryDefinitionLine.setChartType(ChartType.LINE);
        categoryDefinitionLine.setListPosition(1);

        ChartSettings chartSettings = new ChartSettings();
        chartSettings.getCategoryDefinitions().add(categoryDefinitionArea);
        chartSettings.getCategoryDefinitions().add(categoryDefinitionLine);
        return chartSettings;
    }

    private DrillChartViewDef buildChartWithAreaFirstLineSecondListPositionsReveresed(ChartSettings chartSettings) {
        CategoryDefinition categoryDefinitionArea2 = new CategoryDefinition();
        categoryDefinitionArea2.setChartType(ChartType.AREA);
        categoryDefinitionArea2.setListPosition(1);

        CategoryDefinition categoryDefinitionLine2 = new CategoryDefinition();
        categoryDefinitionLine2.setChartType(ChartType.LINE);
        categoryDefinitionLine2.setListPosition(0);

        ChartSettings chartSettings2 = new ChartSettings();
        chartSettings2.getCategoryDefinitions().add(categoryDefinitionArea2);
        chartSettings2.getCategoryDefinitions().add(categoryDefinitionLine2);
        chartSettings2.setUuid(chartSettings.getUuid());

        final DrillChartViewDef drillChartViewDef2 = new DrillChartViewDef();
        drillChartViewDef2.setChartSettings(chartSettings2);
        drillChartViewDef2.setUuid(myUuid);
        return drillChartViewDef2;
    }


}
