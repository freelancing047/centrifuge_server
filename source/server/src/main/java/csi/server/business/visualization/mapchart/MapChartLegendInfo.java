package csi.server.business.visualization.mapchart;

import java.util.List;

import csi.server.business.visualization.legend.ChartLegendItem;

/*
 * Structure to wrap the data that we send down for the Mapchart Legend
 */

public class MapChartLegendInfo {

    public boolean hasHeatMap;
    public boolean hasPlotMap;

    // For the heat map
    public String color_min;
    public String color_max;
    public String color_unspecified;
    public String label;
    public double valueMin;
    public double valueMax;

    public boolean hasBubbleMap;
    public double bubbleMin;
    public double bubbleMax;
    public String bubbleLabel;

    // For the plot map
    public List<ChartLegendItem> legendItems;

}
