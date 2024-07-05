package csi.client.gwt.viz.chart.view;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.sencha.gxt.core.client.util.TextMetrics;
import csi.client.gwt.viz.chart.model.ChartModel;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.shared.core.color.ColorUtil;
import csi.shared.core.visualization.chart.*;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.YAxis;
import org.moxieapps.gwt.highcharts.client.plotOptions.PiePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import java.util.*;

/**
 * @author Centrifuge Systems, Inc.
 */
class HighchartConfigurer {
    private static final float saturation = .7f;//1.0 for brilliant, 0.0 for dull
    private static final float luminance = .45f; //1.0 for brighter, 0.0 for black
    private static final float golden = 0.618033988749895f;
    private static final int yAxisTitleMaxCharLimit = 50;
    private static final int yAxisTitleTextMetricLimit = 150;
    private final Random random;
    private ResizeableHighchart highchart;
    private ChartModel chartModel;
    private ChartPresenter chartPresenter;
    private ChartType chartType;
    private List<String> categoryNames;
    private List<SeriesInfo> seriesInfos;
    private List<HighchartSeriesData> seriesData;
    private List<AxisScale> axisScales;
    private float lastColorHue;

    HighchartConfigurer(ResizeableHighchart highchart, ChartPresenter chartPresenter, HighchartResponse response, Map<String, Double> pointPercentages) {
        this.highchart = highchart;
        this.chartPresenter = chartPresenter;
        chartModel = chartPresenter.getChartModel();
        chartType = chartModel.getChartType();
        categoryNames = response.getCategoryNames();
        seriesInfos = response.getSeriesInfos();
        seriesData = response.getSeriesData();
        axisScales = response.getAxisScales();
        setupLegend(pointPercentages);
        highchart.getXAxis().setAxisTitle(new AxisTitle().setText(null));
        random = new Random();
    }

    private void setupLegend(Map<String, Double> pointPercentages) {
        for (int col = 0; col < seriesData.size(); col++) {
            if (isPieOrDonutMeasure(col)) {
                double total = 0;
                HighchartSeriesData highchartSeriesData = seriesData.get(col);
                for (int row = 0; row < highchartSeriesData.getData().size(); row++) {
                    Number cellValue = highchartSeriesData.get(row);
                    if (cellValue != null) {
                        total += cellValue.doubleValue();
                    }
                }
                for (int row = 0; row < highchartSeriesData.getData().size(); row++) {
                    String pointName = categoryNames.get(row);
                    pointName = unescape(pointName);
                    Number cellValue = highchartSeriesData.get(row);

                    if (cellValue != null) {
                        double percentage = cellValue.doubleValue() / total;
                        pointPercentages.put(pointName, percentage);
                    }
                }
            }
        }
    }

    void configureChart() {
        highchart.setAnimation(false);
        highchart.removeAllSeries(false);
        highchart.getXAxis().setCategories(false, categoryNames.toArray(new String[]{}));
        setupYAxis();
        setupSeriesWithSelection();
    }

    private void setupYAxis() {
        int scaleCounter = 0;
        if (chartModel.getInitialChartData().isAlignAxes()) {
            while (axisScales.size() > 1) {
                axisScales.remove(1);
            }
        }
        for (int col = 0; col < axisScales.size(); col++) {
            if (!isPieOrDonutMeasure(col)) {
                SeriesInfo seriesInfo = seriesInfos.get(col);

                AxisScale axisScale = axisScales.get(col);
                YAxis yAxis = highchart.getYAxis(scaleCounter)
                        .setOpposite(scaleCounter % 2 == 1)
                        .setLineWidth(1.0).setShowEmpty(true)
                        //NOTE:Future highchart api has this option which might make more sense.
//                yAxis.setOption("softMin",axisScale.getMinValue());
//                yAxis.setOption("softMax",axisScale.getMaxValue());
                        .setOption("min", axisScale.getMinValue())
                        .setOption("max", axisScale.getMaxValue()).setMaxPadding(1);
                if (chartModel.getInitialChartData().isAlignAxes()) {
                    yAxis.setAxisTitle(new AxisTitle().setMargin(5).setText(""));
                } else {
                    String metricName = seriesInfo.getMetricName();
                    metricName = metricName.replace("&amp;amp;", "&");
                    metricName = metricName.replace("&amp;lt;", "<");
                    metricName = metricName.replace("&amp;gt;", ">");
                    metricName = metricName.replace("&amp;#39;", "'");
                    String truncatedMetricName = "";

                    if (TextMetrics.get().getWidth(metricName) > yAxisTitleTextMetricLimit) {
                        truncatedMetricName = metricName.substring(0, yAxisTitleMaxCharLimit) + "…";
                    }

                    if (truncatedMetricName.isEmpty()) {
                        AxisTitle title = new AxisTitle().setMargin(5).setText(metricName);
                        yAxis = yAxis.setLineColor(seriesInfo.getHexColor()).setAxisTitle(title);
                    } else {
                        AxisTitle title = new AxisTitle().setMargin(5).setText(truncatedMetricName);
                        yAxis = yAxis.setLineColor(seriesInfo.getHexColor()).setAxisTitle(title);

                        String vizuuid = chartPresenter.getUuid();
                        String formattedVizuuid = "main-layout-panel-Optional.of(" + vizuuid + ")";
                        String finalTruncatedMetricName = truncatedMetricName;
                        String finalMetricName = metricName;
                        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                            @Override
                            public boolean execute() {
                                NodeList<Element> element = Document.get().getElementById(formattedVizuuid).getElementsByTagName("text");
                                for (int i = 0; i < element.getLength(); i++) {
                                    String elementClassName = element.getItem(i).getAttribute("class");
                                    if (elementClassName.equals(" highcharts-yaxis-title")) {
                                        Element yAxisElem = element.getItem(i);
                                        String measureDisplayName = yAxisElem.getElementsByTagName("tspan").getItem(0).getInnerHTML();
                                        if (yAxisElem.getElementsByTagName("title").getLength() < 1 && measureDisplayName.equals(finalTruncatedMetricName)) {
                                            yAxisElem.setInnerHTML(yAxisElem.getInnerHTML() + "<title>" + finalMetricName + "</title>");
                                        }
                                    }
                                }
                                return false;
                            }
                        }, 500);
                    }
                }
                yAxis.setShowFirstLabel(true);
                yAxis.setShowLastLabel(false);
                if (chartType == ChartType.BAR) {
                    yAxis.setEndOnTick(true).setShowLastLabel(false);
                }

                scaleCounter++;
            }
        }

    }

    private void setupSeriesWithSelection() {
        int yAxisCounter = 0;

        Set<String> selectedCategories = new HashSet<>();
        for (DrillCategory selection : chartModel.getChartSelectionState().getSelectedItems()) {
            ArrayList<String> selectionCategories = selection.getCategories();
            selectedCategories.add(selectionCategories.get(chartModel.getDrillSelections().size()));
        }

        int size = seriesData.size();
        if (chartType.equals(ChartType.PIE)) {
            size = 1;
        }
        for (int col = 0; col < size; col++) {
            addSeries(col, yAxisCounter++, selectedCategories);
        }
    }

    private void addSeries(int col, int yAxisCounter, Set<String> selectedCategories) {
        Series series = highchart.createSeries();
        String seriesName = seriesInfos.get(col).getMetricName();
//        seriesName = HtmlEscapers.htmlEscaper().escape(seriesName);
        series.setName(seriesName);

        if (seriesInfos.get(col).getMeasureChartType() == MeasureChartType.DEFAULT && chartType.equals(ChartType.BAR)) {
            series.setIndex(seriesData.size() - col);
            series.setLegendIndex(col);
        } else {
            series.setIndex(col);
        }

        boolean isPieOrChart = isPieOrDonutMeasure(col);
        if (!isPieOrChart) {
            if (chartModel.getInitialChartData().isAlignAxes()) {
                series.setYAxis(0);
            } else {
                series.setYAxis(yAxisCounter);

            }
        }
        if (isPieOrChart) {
            lastColorHue = (float) ColorUtil.toHSL(seriesInfos.get(col).getHexColor()).getH();
        }

        setMeasureType(col, series);
        setSeriesDefaultColors(col, series);
        setSelectionOnSeries(selectedCategories, seriesData.get(col), series, isPieOrChart);
        highchart.addSeries(series, false, false);
    }

    private void setSelectionOnSeries(Set<String> selectedCategories, HighchartSeriesData highchartSeriesData, Series series, boolean isPieOrChart) {
        for (int row = 0; row < highchartSeriesData.getData().size(); row++) {
            Point p = createPoint(highchartSeriesData, isPieOrChart, row);

            series.addPoint(p, false, false, true);

            if (selectedCategories.contains(p.getName())) {
                p.setSliced(true);
                p.setSelected(true);
            }
        }
    }

    private Point createPoint(HighchartSeriesData highchartSeriesData, boolean isPieOrChart, int row) {
        String categoryName = categoryNames.get(row);
        categoryName = unescape(categoryName);
        Number value = highchartSeriesData.get(row);
        if (isPieOrChart) {
            return createPieOrDonutPoint(categoryName, value);
        } else {
            return new Point(categoryName, value);
        }
    }

    private Point createPieOrDonutPoint(String categoryName, Number number) {
        Point p = new Point(categoryName, number == null ? null : Math.abs(number.doubleValue()));
        lastColorHue = lastColorHue > 1f ? random.nextFloat() : lastColorHue;
        lastColorHue += golden;
        lastColorHue %= 1;

        String color = ColorUtil.toColorString(new ColorUtil.HSL(lastColorHue, saturation, luminance));

        p.setColor(color);
        if (number != null && number.doubleValue() < 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(HighchartConstants.NEGATIVE.toString(), JSONBoolean.getInstance(true));
            p.setUserData(jsonObject);
        }
        return p;
    }

    private void setSeriesDefaultColors(int col, Series series) {
        if (!isPieOrDonutMeasure(col)) {
            series.setPlotOptions(new SeriesPlotOptions().setColor(seriesInfos.get(col).getHexColor()));
        }
    }

    private void setMeasureType(int col, Series series) {
        switch (chartType) {
            case COLUMN:
            case LINE:
            case BAR:
            case AREA:
            case AREA_SPLINE:
                MeasureChartType measureChartType = seriesInfos.get(col).getMeasureChartType();
                switch (measureChartType) {
                    case AREA:
                        series.setType(Series.Type.AREA);
                        break;
                    case AREA_SPLINE:
                        series.setType(Series.Type.AREA_SPLINE);
                        break;
                    case COLUMN:
                        if (chartType.equals(ChartType.BAR)) {
                            break;
                        }
                        series.setType(Series.Type.COLUMN);
                        break;
                    case DEFAULT:
                        if (chartType.equals(ChartType.SPIDER)) {
                            series.setType(Series.Type.LINE);
                        }
                        // TODO fix the default for different types here
                        break;
                    case LINE:
                        series.setType(Series.Type.LINE);
                        break;
                    case DONUT:
                        PiePlotOptions plotOptions = new PiePlotOptions();
                        plotOptions.setInnerSize(HighchartBuilder.DONUT_INNER_SIZE);
                        series.setPlotOptions(plotOptions);
                        series.setType(Series.Type.PIE);
                        break;
                    case PIE:
                        series.setType(Series.Type.PIE);
                        break;
                }
                break;
            case POLAR:
            case SPIDER:
            case PIE:
            case DONUT:
                break;
        }
    }

    private boolean isPieOrDonutMeasure(int col) {
        MeasureChartType measureChartType = seriesInfos.get(col).getMeasureChartType();

        if (measureChartType == MeasureChartType.PIE || measureChartType == MeasureChartType.DONUT)
            return true;

        return measureChartType == MeasureChartType.DEFAULT && (chartType == ChartType.PIE || chartType == ChartType.DONUT);
    }

    String unescape(String s) {
        return s;
//        return s.replaceAll("<","&lt;").replaceAll(">","&gt;");
//        return s.replaceAll("'","\\'");
//        return s.replace("&quot;", "\"").replace("&#39;", "\'").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
    }
}
