package csi.client.gwt.viz.chart.model;

import java.util.List;

import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.shared.core.visualization.chart.ChartType;

/**
 * @author Centrifuge Systems, Inc.
 */
public class InitialChartData {

    private final String chartTitle;
    private final String chartDescription;
    private final String horizontalAxisName;
    private final ChartType chartType;
    private final int seriesCount;
    
    private final boolean pieLabelEnabled;
    private final boolean pieLabelShowPercentage;
    private final boolean pieLabelShowValue;
    private final double pieLabelPercentageThreshold;
    private final boolean pieLegendEnabled;
    private final boolean pieLegendShowPercentage;
    private final boolean pieLegendShowValue;

    private final boolean alignAxes;

    private InitialChartData(String chartTitle, String chartDescription, String xAxisName, ChartType chartType, int seriesCount, boolean pieLabelEnabled, boolean pieLabelShowPercentage, boolean pieLabelShowValue, double pieLabelPercentageThreshold, boolean pieLegendEnabled, boolean pieLegendShowPercentage, boolean pieLegendShowValue, boolean alignAxes) {
        this.chartTitle = chartTitle;
        this.chartDescription = chartDescription;
        this.horizontalAxisName = xAxisName;
        this.chartType = chartType;
        this.seriesCount = seriesCount;
        this.pieLabelEnabled = pieLabelEnabled;
        this.pieLabelShowPercentage = pieLabelShowPercentage;
        this.pieLabelShowValue = pieLabelShowValue;
        this.pieLabelPercentageThreshold = pieLabelPercentageThreshold;
        this.pieLegendEnabled = pieLegendEnabled;
        this.pieLegendShowValue = pieLegendShowValue;
        this.pieLegendShowPercentage = pieLegendShowPercentage;
        this.alignAxes = alignAxes;
    }

    public boolean isPieLabelEnabled() {
		return pieLabelEnabled;
	}

    public boolean isPieLabelShowPercentage() {
		return pieLabelShowPercentage;
	}

    public boolean isPieLabelShowValue() {
		return pieLabelShowValue;
	}
    
	public double getPieLabelPercentageThreshold() {
		return pieLabelPercentageThreshold;
	}

    public boolean isPieLegendEnabled() {
		return pieLegendEnabled;
	}

	public boolean isPieLegendShowPercentage() {
		return pieLegendShowPercentage;
	}

	public boolean isPieLegendShowValue() {
		return pieLegendShowValue;
	}

	public String getChartTitle() {
        return chartTitle;
    }

    public String getChartDescription() {
        return chartDescription;
    }

    public String getHorizontalAxisName() {
        return horizontalAxisName;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public int getSeriesCount() {
        return seriesCount;
    }

    public static InitialChartData createInitialChartData(DrillChartViewDef visualizationDef, List<String> drillCategories) {
        List<CategoryDefinition> categoryDefinitions = visualizationDef.getChartSettings().getCategoryDefinitions();
        
        ChartType chartType = getChartType(drillCategories, categoryDefinitions);
        
        String chartTitle = visualizationDef.getName();

        String chartDescription = "";
        if (chartType == ChartType.PIE || chartType == ChartType.DONUT || chartType == ChartType.POLAR || chartType == ChartType.SPIDER) {
        	chartDescription = categoryDefinitions.get(drillCategories.size()).getComposedName();
        } 

        String xAxisName = categoryDefinitions.get(drillCategories.size()).getComposedName();
        if (chartType == ChartType.POLAR || chartType == ChartType.SPIDER) {
        	xAxisName = "";
        }
        
        int seriesCount = createSeriesCount(visualizationDef);

        ChartSettings chartSettings = visualizationDef.getChartSettings();
        
        boolean pieLabelEnabled = chartSettings.isPieLabelEnabled();
        boolean pieLabelShowPercentage = chartSettings.isPieLabelShowPercentage();
        boolean pieLabelShowValue = chartSettings.isPieLabelShowValue();
        double pieLabelPercentageThreshold = chartSettings.getPieLabelPercentageThreshold();
        
        boolean pieLegendEnabled = chartSettings.isPieLegendEnabled();
        boolean pieLegendShowPercentage = chartSettings.isPieLegendShowPercentage();
        boolean pieLegendShowValue = chartSettings.isPieLegendShowValue();

        boolean alignAxes = chartSettings.isAlignAxes();

        return new InitialChartData(chartTitle, chartDescription, xAxisName, chartType, seriesCount,
        		pieLabelEnabled, pieLabelShowPercentage, pieLabelShowValue, pieLabelPercentageThreshold, 
        		pieLegendEnabled, pieLegendShowPercentage, pieLegendShowValue, alignAxes);
    }

	private static ChartType getChartType(List<String> drillCategories, List<CategoryDefinition> categoryDefinitions) {
        return categoryDefinitions.get(drillCategories.size()).getChartType();
    }

    private static int createSeriesCount(DrillChartViewDef visualizationDef) {
        int seriesCount = visualizationDef.getChartSettings().getMeasureDefinitions().size();
        if(visualizationDef.getChartSettings().isUseCountStarForMeasure()){
            return 1;
        }
        if(seriesCount == 0){
            seriesCount++;
        }
        return seriesCount;
    }
    public boolean isAlignAxes() {
        return alignAxes;
    }
}
