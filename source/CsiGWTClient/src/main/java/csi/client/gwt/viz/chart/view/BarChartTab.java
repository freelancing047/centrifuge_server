package csi.client.gwt.viz.chart.view;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.SimplePanel;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.model.ChartModel;
import csi.client.gwt.viz.chart.model.CsiChartLoadHandler;
import csi.client.gwt.viz.chart.model.InitialChartData;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.chart.presenter.DrillSelectionCallback;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.viz.shared.menu.CsiMenuNav;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.shared.core.visualization.chart.ChartType;
import csi.shared.core.visualization.chart.HighchartResponse;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.events.*;

import java.util.*;

public class BarChartTab extends FullSizeLayoutPanel {
    private ChartPresenter chartPresenter;
    private ChartModel chartModel;

    private SimplePanel chartPanel;
    private HighchartResponse response;

    private List<String> drillSelections;
    private ChartSelectionState chartSelectionState;

    private ResizeableHighchart highchart;
    private PieLegendShowPercentageCallback pieLegendShowPercentageCallback = new PieLegendShowPercentageCallback();

    private String chartTitle;
    private String chartDescription;
    private SeriesShowEventHandler showHandler = new SeriesShowEventHandler() {
        @Override
        public boolean onShow(SeriesShowEvent seriesShowEvent) {
            CsiMenuNav myMenu = chartPresenter.getChrome().getMenu();
            myMenu.setDynamicItemVisible(CentrifugeConstantsLocator.get().menuDynamicToggleSortBy(seriesShowEvent.getSeriesName()), true);
            Series enableMouseTracking = highchart.getSeries(seriesShowEvent.getSeriesId()).setOption("enableMouseTracking", null);
            highchart.getSeries(seriesShowEvent.getSeriesId()).update(enableMouseTracking, true);


            Scheduler.get().scheduleFixedDelay(() -> {
                ChartMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(chartPresenter.getUuid()));
                // check if we have more than one measure..

                List<String> categoryNames = chartPresenter.getView().getResponse().getCategoryNames();

                HashSet<String> selectedCategories = Sets.newHashSet();

                int drillDepth = chartSelectionState.getDrillSelections().size();
                for (DrillCategory selectedItem : chartSelectionState.getSelectedItems()) {
                    selectedCategories.add(selectedItem.getCategories().get(drillDepth));
                }

                if (chartPresenter.getVisualizationDef().getChartSettings().getMeasureDefinitions().size() > 1) {
                    for (String categoryName : categoryNames) {
                        fixMultiMeasureSelection(categoryName, selectedCategories.contains(categoryName), seriesShowEvent.getSeriesId());
                    }
                }

                if (highchart != null) {
                    highchart.redraw();
                }
                return false;
            }, 50);
            return true;
        }
    };
    private Map<String, Boolean> seriesVisibilityMap = new HashMap<>();

    public BarChartTab() {
        super();
        Style style = getElement().getStyle();
        style.setProperty("webkitTouchCallout", "none");
        style.setProperty("webkitUserSelect", "none");
        style.setProperty("htmlUserSelect", "none");
        style.setProperty("mozUserSelect", "none");
        style.setProperty("msUserSelect", "none");
        style.setProperty("userSelect", "none");
        style.setWidth(100, Style.Unit.PCT);
        style.setHeight(100, Style.Unit.PCT);

        chartPanel = new SimplePanel();
        add(chartPanel);
        chartPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        chartPanel.setSize("100%", "100%");
//		setWidgetBottomHeight(chartPanel, 0, Unit.PX, 100, Unit.PCT);


        setChartTitle();
    }

    public static native void disableAnimation() /*-{
        $wnd.$.fx.off = true;
    }-*/;

    public static native void enableAnimation() /*-{
        $wnd.$.fx.off = false;
    }-*/;

    public static native void setPointSelected(JavaScriptObject obj, boolean select, boolean shouldSetState) /*-{
        if (select) {
            obj.selected = true;
            obj.setState('select');
        } else {
            obj.selected = false;
            obj.setState('');
        }
    }-*/;

    private void setChartTitle() {
        if (highchart != null && chartTitle != null && chartDescription != null) {
            highchart.setTitle(chartTitle, chartDescription, chartModel.getInitialChartData().getSeriesCount() > 1 && !chartPresenter.getVisualizationDef().getChartSettings().isUseCountStarForMeasure() ?
                    ChartTitle.Align.LEFT : ChartTitle.Align.CENTER);
        }
    }


    void setupChart(ChartPresenter chartPresenter) {
        this.chartPresenter = chartPresenter;
        chartModel = chartPresenter.getChartModel();
        drillSelections = chartModel.getDrillSelections();
        chartSelectionState = chartModel.getChartSelectionState();

        if (highchart != null) {
            preserveHiddenMeasures();
            highchart.setAnimation(false);
            highchart.removeAllSeries(false);
            highchart.removeFromParent();
        }

        createHighchart(chartPresenter::hideProgressIndicator);

        highchart.redraw();
    }

    private void createHighchart(CsiChartLoadHandler csiChartLoadHandler) {
        InitialChartData initialChartData = chartModel.getInitialChartData();
        chartTitle = initialChartData.getChartTitle();
        chartDescription = initialChartData.getChartDescription();
        String horizontalAxisName = initialChartData.getHorizontalAxisName();
        int seriesCount = initialChartData.getSeriesCount();

        DrillSelectionCallback drillSelectionCallback = chartPresenter.getDrillSelectionCallback();
        HorizontalAxisLabelsFormatter horizontalAxisLabelsFormatter = new HorizontalAxisLabelsFormatter(chartPresenter);

        boolean pieLabelEnabled = initialChartData.isPieLabelEnabled();
        boolean pieLabelShowPercentage = initialChartData.isPieLabelShowPercentage();
        boolean pieLabelShowValue = initialChartData.isPieLabelShowValue();
        double pieLabelPercentageThreshold = initialChartData.getPieLabelPercentageThreshold();

        boolean pieLegendEnabled = initialChartData.isPieLegendEnabled();
        boolean pieLegendShowPercentage = initialChartData.isPieLegendShowPercentage();
        boolean pieLegendShowValue = initialChartData.isPieLegendShowValue();

        String strHorizontalAxisName = horizontalAxisName;
        if (response != null) {
            strHorizontalAxisName += " (" + response.getCategorySize() + ")";
            if (chartModel.getChartType() == ChartType.PIE || chartModel.getChartType() == ChartType.DONUT) {
                String metricName = chartModel.getHighchartResponse().getSeriesInfos().get(0).getMetricName();
                strHorizontalAxisName = initialChartData.getHorizontalAxisName() + " (" + response.getCategorySize() + ")" + " \u2014 " + metricName;

            }
            if (chartModel.getChartType() == ChartType.POLAR || chartModel.getChartType() == ChartType.SPIDER) {
                int size = chartModel.getDrillSelections().size();
                String metricName = chartModel.getHighchartResponse().getSeriesInfos().get(0).getMetricName();
                strHorizontalAxisName = chartPresenter.getVisualizationDef().getChartSettings().getCategoryDefinitions().get(size).getComposedName() + " (" + response.getCategorySize() + ")" + " \u2014 " + metricName;

            }
        }

        HighchartBuilder highchartBuilder = new HighchartBuilder();

        highchart = highchartBuilder
                .titleDescription(chartTitle, strHorizontalAxisName)
                .xAxisTitle(horizontalAxisName)
                .chartPresenter(chartPresenter)
                .seriesCount(seriesCount)
                .drillSelectionCallback(drillSelectionCallback)
                .axisLabelsFormatter(horizontalAxisLabelsFormatter)
                .pieLabelEnabled(pieLabelEnabled)
                .pieLabelShowPercentage(pieLabelShowPercentage)
                .pieLabelShowValue(pieLabelShowValue)
                .pieLabelPercentageThreshold(pieLabelPercentageThreshold)
                .pieLegendEnabled(pieLegendEnabled)
                .pieLegendShowPercentage(pieLegendShowPercentage)
                .pieLegendShowValue(pieLegendShowValue)
                .setPointSelectEventHandler(getPointSelectEventHandler())
                .setPointUnselectEventHandler(getPointUnselectEventHandler())
                .setSeriesHideEvent(getSeriesHideHandler())
                .setSeriesShowEvent(getSeriesShowHandler())
                .build(pieLegendShowPercentageCallback, csiChartLoadHandler);

        // adds custom logic to event listeners

//
        highchart.setOption("/plotOptions/column/minPointLength", 3);
        highchart.setOption("/plotOptions/bar/minPointLength", 3);//
        highchart.setOption("/plotOptions/series/turboThreshold", Integer.toString(ChartView.getMaxChartCategorySize() + 1));
        setSelectionDecorators();

        highchart.setHeight("100%");
        highchart.setWidth100();
    }

    private void setSelectionDecorators() {
        highchart.setOption("/chart/animation", false);
        highchart.setOption("/plotOptions/series/animation", false);


        highchart.setOption("/plotOptions/series/allowPointSelect", true);
        highchart.setOption("/plotOptions/series/marker/enabled", true); // mb

        highchart.setOption("/plotOptions/series/states/hover/enabled", false);  //hover looks bad, disable.

        highchart.setOption("/plotOptions/series/states/select/enabled", true);
        highchart.setOption("/plotOptions/series/states/select/color", null); // disable color replacement( defaults to gray)
        highchart.setOption("/plotOptions/series/states/select/borderColor", "rgb(255,133,10)");
        highchart.setOption("/plotOptions/series/states/select/color", "rgb(255,133,10)");
        highchart.setOption("/plotOptions/series/states/select/lineColor", "rgb(255,133,10)");
        highchart.setOption("/plotOptions/series/states/select/radius", 5);


        highchart.setOption("/plotOptions/series/marker/states/select/borderColor", "rgb(255,133,10)");
        highchart.setOption("/plotOptions/series/marker/states/select/lineColor", "rgb(255,133,10)");
        highchart.setOption("/plotOptions/series/marker/states/select/radius", 3);
        highchart.setOption("/plotOptions/series/marker/states/select/enabled", true);

        highchart.setOption("/plotOptions/series/bar/marker/enabled", true);

        highchart.setOption("/plotOptions/line/marker/enabled", true); // always show point markers
        highchart.setOption("/plotOptions/line/marker/fillColor", null); // always show point markers
        highchart.setOption("/plotOptions/area/marker/enabled", true); // always show point markers
        highchart.setOption("/plotOptions/area/marker/fillColor", null); // always show point markers
    }

    private SeriesHideEventHandler getSeriesHideHandler() {
        return seriesHideEvent -> {
            CsiMenuNav myMenu = chartPresenter.getChrome().getMenu();
            myMenu.setDynamicItemVisible(CentrifugeConstantsLocator.get().menuDynamicToggleSortBy(seriesHideEvent.getSeriesName()), false);

            Series enableMouseTracking = highchart.getSeries(seriesHideEvent.getSeriesId()).setOption("enableMouseTracking", false);
            highchart.getSeries(seriesHideEvent.getSeriesId()).update(enableMouseTracking, true);

            ChartMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(chartPresenter.getUuid()));

            return true;
        };
    }

    private SeriesShowEventHandler getSeriesShowHandler() {
        return showHandler;
    }

    private PointSelectEventHandler getPointSelectEventHandler() {
        return pointSelectEvent -> {
            ArrayList<String> drillCategories = new ArrayList<>(drillSelections);
            drillCategories.add(pointSelectEvent.getPointName());
            DrillCategory drillCategory = new DrillCategory();
            drillCategory.setCategories(drillCategories);

            chartSelectionState.getSelectedItems().add(drillCategory);

            fixMultiMeasureSelection(pointSelectEvent.getPointName(), true);
            chartPresenter.getView().isTableRendered = false;
            return true;
        };
    }

    private PointUnselectEventHandler getPointUnselectEventHandler() {
        return pointUnselectEvent -> {
            ArrayList<String> drillCategories = new ArrayList<>(drillSelections);
            drillCategories.add(pointUnselectEvent.getPointName());
            DrillCategory toRemove = null;
            for (DrillCategory drillCategory : chartSelectionState.getSelectedItems()) {
                if (drillCategory.getCategories().equals(drillCategories)) {
                    toRemove = drillCategory;
                }
            }

            chartSelectionState.getSelectedItems().remove(toRemove);
            fixMultiMeasureSelection(pointUnselectEvent.getPointName(), false);

            return true;
        };
    }

    private void fixMultiMeasureSelection(String categoryName, boolean select) {
        Series[] allSeries = highchart.getSeries();
        // if we have just one measure don't bother.
        if (allSeries.length == 1) {
            return;
        }

        for (Series series : allSeries) {
            for (Point point : series.getPoints()) {
                if (point.getName().equals(categoryName)) {
                    setPointSelected(point.getNativePoint(), select, series.isVisible());
                }
            }
        }
        highchart.redraw();
    }

    private void fixMultiMeasureSelection(String categoryName, boolean select, String seriesId) {
        // if we have just one measure don't bother.

        Series series = highchart.getSeries(seriesId);
        if (series != null) {

            for (Point point : series.getPoints()) {
                if (point.getName().equals(categoryName)) {
                    setPointSelected(point.getNativePoint(), select, series.isVisible());
                }
            }
        }
        highchart.redraw();
    }


    private void preserveHiddenMeasures() {
        if (highchart != null) {
            if (!highchart.isRendered()) {
                return;
            }
            if (highchart.getSeries().length > 0) {
                for (Series series : highchart.getSeries()) {
                    seriesVisibilityMap.put(series.getName(), series.isVisible());
                }
            }
        }
    }

    public void render(HighchartResponse response) {
        this.response = response;

        this.setSize("100%", "100%");
        highchart.setSize("100%", "100%");
        configureChart();
        Scheduler.get().scheduleDeferred(() -> {
            chartPanel.setWidget(highchart);
            highchart.getElement().getStyle().setProperty("maxHeight", getOffsetHeight() + "px");
            highchart.getElement().getStyle().setProperty("maxWidth", getOffsetWidth() + "px");
            correctMeasureVisibility();
            highchart.reflow();
            highchart.redraw();
        });
    }


    Map<String, Boolean> getMeasureVisibility() {
        preserveHiddenMeasures();
        return seriesVisibilityMap;
    }

    private void correctMeasureVisibility() {
        if (seriesVisibilityMap.keySet().size() == 0) {
            return;
        }
        for (Series series : highchart.getSeries()) {
            Boolean visible = seriesVisibilityMap.get(series.getName());
            if (visible != null) {
                if (series.isVisible() == visible) {
                    // noop
                } else {
                    series.setVisible(visible);
                }
            }
        }
    }

//    private String getXAxisName() {
//        String strHorizontalAxisName = horizontalAxisName;
//        strHorizontalAxisName += " (" + response.getCategorySize() + ")";
//        return strHorizontalAxisName;
//    }

    private void configureChart() {
        Map<String, Double> pointPercentages = new TreeMap<>();
        HighchartConfigurer highchartConfigurer = new HighchartConfigurer(highchart, chartPresenter, response, pointPercentages);
        pieLegendShowPercentageCallback.setPointPercentages(pointPercentages);
        highchartConfigurer.configureChart();
    }

    public Chart getChart() {
        return ((Chart) chartPanel.getWidget());
    }

    boolean isLimitDisplayed() {
        //todo remove
        return false;
    }

    @Override
    public void onResize() {
        super.onResize();

        // full screen the chart.
        if (highchart != null) {
            highchart.setHeight100();
            highchart.setWidth100();

            highchart.getElement().getStyle().setProperty("maxHeight", getOffsetHeight() + "px");
            highchart.getElement().getStyle().setProperty("maxWidth", getOffsetWidth() + "px");

            highchart.reflow();
            highchart.redraw();
        }

    }

    public void setResponse(HighchartResponse response) {
        this.response = response;
    }
}
