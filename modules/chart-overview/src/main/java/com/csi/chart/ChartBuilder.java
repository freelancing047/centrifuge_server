package com.csi.chart;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import com.csi.chart.data.DataService;
import com.csi.chart.dto.BaseRequest;
import com.csi.chart.renderer.ColorPaintScale;

public class ChartBuilder {

    private DataService dataService;
    private boolean absolute;
    private String chartType;
    private Dataset dataset;
    private String resourceId;

    private BaseRequest request;

    public ChartBuilder withDataService(DataService service) {
        this.dataService = service;
        return this;
    }

    public ChartBuilder withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }
    
    public ChartBuilder withAbsoluteRepresentation(boolean absolute) {
        this.absolute = absolute;
        return this;
    }

    public ChartBuilder withChartType(String chartType) {
        this.chartType = chartType;
        return this;
    }

    public ChartBuilder withDataset(Dataset data) {
        this.dataset = data;
        return this;
    }

    public JFreeChart build() throws Exception {
        if (dataset != null) {
            return buildWithDataset();
        }

        JFreeChart chart = null;
        boolean categorical = dataService.isCategoryChart();
        if (dataService.isHeatMap()) {
            XYDataset xyData = dataService.getXYData(request);
            chart = buildAndConfigure(xyData);
        } else if (categorical) {
            dataService.sortByValue(true);
            CategoryDataset categoryData = dataService.getCategoryData(request);
            chart = buildAndConfigure(categoryData);
        } else {
            XYDataset xyData = dataService.getXYData(request);
            chart = buildAndConfigure(xyData);
        }
        return chart;
    }

    private JFreeChart buildWithDataset() {
        return buildAndConfigure();
    }

    private JFreeChart buildAndConfigure() {
        if (dataset instanceof XYDataset) {
            XYDataset xyData = (XYDataset) dataset;
            return buildAndConfigure(xyData);
        } else {
            CategoryDataset catData = (CategoryDataset) dataset;
            return buildAndConfigure(catData);
        }
    }

    private JFreeChart buildAndConfigure(CategoryDataset categoryData) {
        CategoryAxis domain = new CategoryAxis();
        domain.setVisible(false);

        NumberAxis range = new NumberAxis();
        range.setVisible(false);

        CategoryItemRenderer renderer = buildRenderer(categoryData);
        CategoryPlot plot = new CategoryPlot(categoryData, domain, range, renderer);

        JFreeChart chart = new JFreeChart(plot);

        configurePlot((CategoryPlot) plot);
        chart.setAntiAlias(true);
        return chart;
    }

    private JFreeChart buildAndConfigure(XYDataset xyData) {
        NumberAxis domain = new NumberAxis();
        domain.setVisible(false);
        domain.setAxisLineVisible(false);

        NumberAxis range = new NumberAxis();
        range.setVisible(false);
        range.setAxisLineVisible(false);

        XYItemRenderer renderer = buildXYPlotRenderer(xyData);
        XYPlot plot = new XYPlot(xyData, domain, range, renderer);
        configurePlot(plot);

        JFreeChart chart = new JFreeChart(plot);
        return chart;
    }

    private void configurePlot(XYPlot plot) {
        plot.setInsets(RectangleInsets.ZERO_INSETS);
        plot.setOutlineVisible(false);
        // plot.setBackgroundPaint(new Color(0x00ffffff, true));
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setRangeCrosshairVisible(false);
    }

    private XYItemRenderer buildXYPlotRenderer(XYDataset xyData) {
        XYItemRenderer renderer = null;
        if (xyData instanceof MatrixSeriesCollection) {
            MatrixSeriesCollection msc = (MatrixSeriesCollection) xyData;
            MatrixSeries series = msc.getSeries(0);
            long n = series.getRowCount() * series.getColumnsCount();
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (int i = 0; i < n; i++) {
                Number z = series.getItem(i);
                if (z == null) {
                    continue;
                }

                double zz = z.doubleValue();
                if (zz < min) {
                    min = zz;
                }

                if (zz > max) {
                    max = zz;
                }
            }

            XYBlockRenderer br = new XYBlockRenderer();
            ColorPaintScale scale = new ColorPaintScale(min, max);
            scale.setColor(Color.blue.getRGB());
            br.setPaintScale(scale);
            renderer = br;
        }

        return renderer;
    }

    private CategoryItemRenderer buildRenderer(CategoryDataset categoryData) {
        validateChartType();
        CategoryItemRenderer renderer = null;
        boolean is1D = categoryData.getRowCount() == 1;
        if (isBarChart() || isPieChart()) {
            BarRenderer br = (is1D) ? new BarRenderer() : new StackedBarRenderer(!this.absolute);
            br.setBasePaint(Color.blue);
            br.setDrawBarOutline(false);
            br.setShadowVisible(false);
            br.setBarPainter(new StandardBarPainter());
            renderer = br;
        } else if (isLineChart()) {
            LineAndShapeRenderer lsr = new LineAndShapeRenderer(true, false);
            renderer = lsr;
        }
        return renderer;
    }

    private void configurePlot(CategoryPlot plot) {
        plot.setInsets(RectangleInsets.ZERO_INSETS);
        plot.setOutlineVisible(false);
        plot.setBackgroundPaint(new Color(0x00ffffff, true));
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);

        CategoryAxis axis = plot.getDomainAxis();
        axis.setVisible(false);

        ValueAxis valueAxis = plot.getRangeAxis();
        valueAxis.setVisible(false);
    }

    private void validateChartType() {
        chartType = (chartType == null) ? "bar" : chartType.toLowerCase().trim();
    }

    private boolean isBarChart() {
        return chartType.indexOf(chartType) != -1;
    }

    private boolean isLineChart() {
        return chartType.indexOf("line") != -1;
    }

    private boolean isPieChart() {
        return chartType.indexOf("pie") != -1;
    }

    public void withRequest(BaseRequest request) {
        this.request = request;
    }

}
