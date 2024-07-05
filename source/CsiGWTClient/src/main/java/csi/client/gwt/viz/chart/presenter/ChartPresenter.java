/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.chart.presenter;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.Panel;
import com.sencha.gxt.core.client.dom.XDOM;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.chart.menu.ChartMenuManager;
import csi.client.gwt.viz.chart.model.ChartModel;
import csi.client.gwt.viz.chart.model.InitialChartData;
import csi.client.gwt.viz.chart.overview.OverviewPresenter;
import csi.client.gwt.viz.chart.overview.range.Range;
import csi.client.gwt.viz.chart.overview.range.RangeChangedEvent;
import csi.client.gwt.viz.chart.overview.range.RangeChangedEventHandler;
import csi.client.gwt.viz.chart.overview.view.OverviewView;
import csi.client.gwt.viz.chart.overview.view.content.CanvasOverview;
import csi.client.gwt.viz.chart.view.ChartMetricsView;
import csi.client.gwt.viz.chart.view.ChartView;
import csi.client.gwt.viz.matrix.ExpireMetrics;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.filter.FilterCapableVisualizationPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.chart.DisplayFirst;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.ChartActionsServiceProtocol;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.server.util.sql.api.AggregateFunction;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.visualization.chart.*;
import csi.shared.gwt.viz.chart.StatisticsHolder;
import csi.shared.gwt.viz.chart.StatisticsHolderImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logic for the chart. Handles setting up the chart and requesting the data.
 *
 * @author Centrifuge Systems, Inc.
 */
public class ChartPresenter extends AbstractVisualizationPresenter<DrillChartViewDef, ChartView> implements FilterCapableVisualizationPresenter {
    private static final int OVERVIEW_CONTENT_MARGIN = 30 + (2 * OverviewView.DRAG_BAR_WIDTH);
    private static final int DEFAULT_SEARCH_ZOOM = 5;
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final ChartModel chartModel = new ChartModel();
    private final OverviewPresenter overviewPresenter;
    private Alert progressIndicator;
    private ProgressBar progressBar;
    private HighchartPagingResponse currentResponse;
    private boolean sizeAdjusted = false;
    private StatisticsHolder statisticsHolder = new StatisticsHolderImpl();
    private DrillSelectionCallback drillSelectionCallback;
    private int currentDrillLevel = 0;
    private Map<Integer, Integer> sizes = new HashMap<Integer, Integer>();
    private Map<Integer, Range> ranges = new HashMap<Integer, Range>();
    private Map<String, Boolean> visiblityOnChart = new HashMap<String, Boolean>();
    public DisplayFirst displayFirst = null;
    private ChartMetricsView metrics;
    private boolean firstLoad = true;

    public ChartPresenter(AbstractDataViewPresenter dvPresenterIn, DrillChartViewDef visualizationDef) {
        super(dvPresenterIn, visualizationDef);

        overviewPresenter = new OverviewPresenter(this);
        drillSelectionCallback = createDrillSelectionCallback();
    }

    public void setVisibilityOnChart(Map<String, Boolean> v) {
        this.visiblityOnChart = v;
    }

    public Map<String, Boolean> getVisiblityOnChart() {
        return this.visiblityOnChart;
    }

    public boolean isFirstLoad() {
        return firstLoad;
    }

    public void setFirstLoad(boolean firstLoad) {
        this.firstLoad = firstLoad;
    }

    public void renderChart(RangeChangedEvent event) {
        int requestStart = Math.min(event.getRange().getStartIndex(), event.getRange().getEndIndex());
        int requestEnd = Math.max(event.getRange().getStartIndex(), event.getRange().getEndIndex());
        if (currentResponse != null) {
            if (requestStart >= currentResponse.getStart() && requestEnd < currentResponse.getCategorySize() + currentResponse.getStart()) {
                renderChart();
                ChartMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
                //hideProgressIndicator();
            } else {
                List<String> drillCategories = getChartModel().getDrillSelections();

                try {
                    HighchartPagingRequest request = createRequestForHighchartData(drillCategories);
                    request.setStart(requestStart);
                    request.setOverviewNeeded(false);

                    if (requestStart == requestEnd) {
                        request.setLimit(1);
                    } else {
                        request.setLimit(requestEnd - requestStart + 1);
                    }

                    // save the current drill range, this is referenced later to set the overview when we load back.
                    if (ranges.size() - 1 >= currentDrillLevel) {
                        ranges.remove(currentDrillLevel);
                    }

                    ranges.put(currentDrillLevel, overviewPresenter.getCategoryRange());
                    getHighchartPage(drillCategories, request);

                    getMenuManager().updateDynamicMenus();

                } catch (Exception exception) {
                    hideProgressIndicator();
                }
            }


        }
    }

    public void renderChart() {
        HighchartPagingResponse response = limitHighchartDataByOverviewRange(currentResponse.copy(), overviewPresenter.getCategoryRange());
        statisticsHolder.clearStatistics();
        populateStatisticsHolderWithHighchartResponse(response);
        getView().render(response, true);

    }

    private void populateStatisticsHolderWithHighchartResponse(HighchartPagingResponse response) {
        statisticsHolder.setCategoryNames(response.getCategoryNames());
        List<List<Number>> data = new ArrayList<List<Number>>();
        for (HighchartSeriesData highchartSeriesData : response.getSeriesData()) {
            data.add(highchartSeriesData.getData());
        }
        statisticsHolder.setData(data);
    }

    public List<ChartMetrics> getChartMetricsForView() {
        // this should get us whatever is in the view right now, meaning it should alwyas be filtered for the view data - accurate metrics...?
        HighchartPagingResponse highchartResponse = (HighchartPagingResponse) getView().getResponse();
        // so i forgot we have many measures here.
        List<ChartMetrics> metrics = new ArrayList<ChartMetrics>(highchartResponse.getSeriesData().size());
        int i = 0;
        Map<String, Boolean> visiblity = getView().getVisibility();
        for (HighchartSeriesData highchartSeriesData : highchartResponse.getSeriesData()) {
            Boolean visible = visiblity.get(highchartResponse.getSeriesInfos().get(i).getMetricName());
            if (visible == null) {
                visible = true;
            }

            Number min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            ChartMetrics seriesMetrics = new ChartMetrics();
            seriesMetrics.setSeriesName(highchartResponse.getSeriesInfos().get(i).getMetricName());
            i++;
            for (Number number : highchartSeriesData.getData()) {
                if (number != null) {
                    min = Math.min(min.doubleValue(), number.doubleValue());
                    max = Math.max(max.doubleValue(), number.doubleValue());
                }
            }

//			max = max.doubleValue() == Integer.MIN_VALUE ? 0 : max;
//			min = min.doubleValue() == Integer.MAX_VALUE ? 0 : min;

            if (visible) {
                seriesMetrics.setMax(max);
                seriesMetrics.setMin(min);
                seriesMetrics.setCategoryCount(highchartSeriesData.getData().size());
                metrics.add(seriesMetrics);
            }
        }


        return metrics;
    }

    public void getChartMeticsForAllChart() {

    }

    public ChartModel getChartModel() {
        return chartModel;
    }

    public void showProgressIndicator() {

        if (progressIndicator != null && progressIndicator.isAttached()) {
            return;
        }
        if (getChrome() != null) {
            createProgressIndicator();
        }
    }

    public void createProgressIndicator() {

        createProgressIndicator(getChrome().getMainLP());
    }

    public void createProgressIndicator(final Panel panel) {
        if (progressIndicator == null) {
            progressIndicator = new Alert(i18n.timelinePresenterLoading(), AlertType.INFO); //$NON-NLS-1$
            Scheduler.get().scheduleFixedDelay(() -> {
                        if (progressIndicator != null) {
                            panel.add(progressIndicator);
                            progressBar = new ProgressBar(ProgressBarBase.Style.ANIMATED);
                            progressBar.setPercent(100);
                            progressIndicator.setClose(false);
                            progressIndicator.add(progressBar);
                            progressIndicator.setHeight("50px");//FIXME: set using stylename
                            progressIndicator.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
                        }
                        return false;
                    }
                    , 1000);
        }
    }

    public void hideProgressIndicator() {
        if (progressIndicator != null) {
            if (progressIndicator.isAttached()) {
                getChrome().getMainLP().remove(progressIndicator);
                progressBar.clear();
                progressBar = null;
                progressIndicator.close();
            }
            progressIndicator = null;
        }
    }

    public HighchartPagingResponse limitHighchartDataByOverviewRange(HighchartPagingResponse response, Range range) {
        if (range.isEmpty()) {
            return response;
        }

        updateXYValuesOfRangeLimitedHighchartData(response, range);

        return response;
    }

    private void updateXYValuesOfRangeLimitedHighchartData(HighchartPagingResponse response, Range range) {
        //x values
        List<String> categoryNames = new ArrayList<String>();

        // sometimes we get weird start/end - where start is bigger, making this return an empty data, breaking the view.
        if (range.getStartIndex() > range.getEndIndex()) {
            int start = range.getStartIndex();
            int end = range.getEndIndex();
            range = new Range(end, start);
        }

        for (int i = range.getStartIndex() - response.getStart(); i <= range.getEndIndex() - response.getStart(); i++) {
            categoryNames.add(response.getCategoryNames().get(i));
        }

        //y values
        List<HighchartSeriesData> highchartSeriesDataList = new ArrayList<HighchartSeriesData>();
        List<AxisScale> axisYScales = new ArrayList<AxisScale>();

        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;

        for (HighchartSeriesData highchartSeriesData : response.getSeriesData()) {

            HighchartSeriesData newSeriesData = new HighchartSeriesData();
            List<Number> newData = new ArrayList<Number>();

            int arrayStart = range.getStartIndex() - response.getStart();
            for (int i = arrayStart; i <= range.getEndIndex() - response.getStart(); i++) {
                newData.add(highchartSeriesData.getData().get(i));
            }
            newSeriesData.setData(newData);
            highchartSeriesDataList.add(newSeriesData);
            if (getVisualizationDef().getChartSettings().isAlignAxes()) {
                minValue = Math.min(minimumOf(newData), minValue);
                maxValue = Math.max(maximumOf(newData), maxValue);
            } else {
                AxisScale newAxisScale = new AxisScale();
                newAxisScale.setMinValue(minimumOf(newData));
                newAxisScale.setMaxValue(maximumOf(newData));
                axisYScales.add(newAxisScale);
            }
        }

        if (getVisualizationDef().getChartSettings().isAlignAxes()) {
            List<HighchartSeriesData> seriesData = response.getSeriesData();
            for (int i = 0; i < seriesData.size(); i++) {
                AxisScale newAxisScale = new AxisScale();
                newAxisScale.setMinValue(minValue);
                newAxisScale.setMaxValue(maxValue);
                axisYScales.add(newAxisScale);
            }
        }
        response.setStart(response.getStart() + range.getStartIndex());
        //response.setLimit(categoryNames.size());
        response.setCategoryNames(categoryNames);

        response.setSeriesData(highchartSeriesDataList);
        response.setAxisScales(axisYScales);
    }

    private double minimumOf(List<Number> newData) {
        ArrayList<Number> numbers = Lists.newArrayList(newData);

        while (numbers.remove(null)) {
            numbers.remove(null);
        }

        if (numbers.size() == 0) {
            return 0;
        }

        double minValue = Doubles.min(Doubles.toArray(numbers));
        return minValue < 0 ? minValue : 0;
    }

    private double maximumOf(List<Number> newData) {
        ArrayList<Number> numbers = Lists.newArrayList(newData);
        while (numbers.remove(null)) {
            numbers.remove(null);
        }

        if (numbers.size() == 0) {
            return 0;
        }

        double maxValue = Doubles.max(Doubles.toArray(numbers));
        return maxValue < 0 ? 0 : maxValue;

    }

    /**
     * This created a handler allowing the drill on click for the series.
     *
     * @return
     */
    private DrillSelectionCallback createDrillSelectionCallback() {
        return new DrillSelectionCallback() {
            @Override
            public void drillCategorySelected(List<String> selectionCategories) {
                if (isNextDrillLevelAvailable(selectionCategories)) {
                    showProgressIndicator();
                    getVisualizationDef().getSelection().setFromSelection(NullSelection.instance);
                    getChartModel().setChartSelectionState(getVisualizationDef().getSelection());
                    getView().updateBreadcrumbs();
                    firstLoad = true;
                    load(selectionCategories);
                    getView().setViewsNotLoaded();
                }
            }
        };
    }

    /**
     * Returns the current drill order of the viz
     *
     * @return current drill level int
     */
    public int getCurrentDrillLevel() {
        return currentDrillLevel;
    }

    /**
     * This is just a public method for {@Link} load()
     *
     * @param drillCategories
     */
    public void loadDrillCategories(final List<String> drillCategories) {
        if (drillCategories != null) {
            firstLoad = true;
            load(drillCategories);
        }
    }

    public void loadDrillCategoriesAfterQuickSort(final List<String> drillCategories) {

        if (drillCategories != null) {
            overviewPresenter.reset();
            firstLoad = true;
            load(drillCategories);
        }

    }

    private void load(final List<String> drillCategories) {
//		firstLoad = false;
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                try {
                    HighchartPagingRequest request = createRequestForHighchartData(drillCategories);
                    InitialChartData initialChartData = InitialChartData.createInitialChartData(getVisualizationDef(), drillCategories);
                    getChartModel().setInitialChartData(initialChartData);
                    request.setOverviewNeeded(true);

                    //Only save drill going inwards, not outwards
                    if (currentResponse != null && drillCategories.size() >= currentDrillLevel) {
                        sizes.put(currentDrillLevel, currentResponse.getOverviewResponse().getTotalCategories());
                        // save the current drill range, this is referenced later to set the overview when we load back.
                        ranges.put(currentDrillLevel, overviewPresenter.getCategoryRange());

                    } else if (currentResponse != null) {
                        //Reversing the drill, invalidate current drill
                        ranges.put(currentDrillLevel, Range.EMPTY_RANGE);

                        //Reverse Drill, make sure to request the correct categoryRange
                        Range range = ranges.get(drillCategories.size());

                        //This can be null, if the sorting was changed, this gets reset
                        if (range != null) {
                            request.setStart(range.getStartIndex());
                            request.setLimit(range.getEndIndex() - range.getStartIndex() + 1);
                        }
                    }
                    getHighchartPage(drillCategories, request);
                    updateMenus();
                } catch (Exception exception) {
                    hideProgressIndicator();
                }
            }

        });

    }

    public void updateMenus() {
        getMenuManager().updateDynamicMenus();
    }

    public HighchartPagingRequest createRequestForHighchartData(List<String> drillCategories) {
        HighchartPagingRequest request = new HighchartPagingRequest();
        request.setLimit(-1);
        request.setStart(0);
        request.setDrillDimensions(drillCategories);
        request.setDvUuid(getDataViewUuid());
        request.setVizUuid(getUuid());
        request.setVizWidth(getChrome().getMainLP().getOffsetWidth());
        return request;
    }

    private void getHighchartPage(final List<String> drillCategories, HighchartPagingRequest request) {
        if (request.getLimit() == -1) {
            overviewPresenter.invalidateCurrentRange();
            overviewPresenter.resetMax();
        }
        // UGH
        getView().setViewsNotLoaded();
        VortexFuture<HighchartPagingResponse> future = getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<HighchartPagingResponse>() {
            @Override
            public void onSuccess(HighchartPagingResponse result) {
                List<SeriesInfo> seriesInfos = result.getSeriesInfos();
                seriesInfos.forEach(seriesInfo -> {
                    String metricName = seriesInfo.getMetricName();
                    if (metricName.startsWith("___MAGIC__")) {
                        metricName = metricName.replace("___MAGIC__","");
                        String[] split = metricName.split("___DELIM__");
                        String s0 = split[0];
                        String s1 = split[1];
                        AggregateFunction value = AggregateFunction.valueOf(s0);
                        s0  = value.getLabel();
                        metricName = s0 + " " +s1;
                    }
                    seriesInfo.setMetricName(metricName);
                });
                getChartModel().updateWith(result.copy(), drillCategories, getUuid());

                if (hasOldSelection()) {
                    getVisualizationDef().getSelection().setFromSelection(popOldSelection());
                    getChartModel().setChartSelectionState(getVisualizationDef().getSelection());
                }

                OverviewResponse oldOverview = null;
                if (currentResponse != null) {
                    oldOverview = currentResponse.getOverviewResponse();
                }

                currentResponse = result;
                populateStatisticsHolderWithHighchartResponse(currentResponse);

                getView().setResponse(currentResponse);

                //We either keep the old Overview, or make a new one based on data from the server if present
                if (result.getOverviewResponse() == null) {
                    result.setOverviewResponse(oldOverview);
                } else {
                    createOverviewScrolling(drillCategories, result.getOverviewResponse(), result.getLimit());
                }

                getView().render(currentResponse, false);
                ChartMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
                hideProgressIndicator();


            }

            @Override
            public boolean onError(Throwable t) {
                hideProgressIndicator();
                displayLoadingError(t);
                return false;
            }
        });
        future.execute(ChartActionsServiceProtocol.class).getChartPage(request);
    }

    @Override
    public boolean hasSelection() {
        return !getChartModel().getChartSelectionState().isCleared();
    }

    @Override
    public void saveViewStateToVisualizationDef() {
        saveDrillCategories();
        saveCurrentSelection();
    }

    private void saveDrillCategories() {
        List<String> list = getChartModel().getDrillSelections();
        DrillCategory drillCategory = new DrillCategory();
        drillCategory.setCategories(new ArrayList<String>(list));
        getVisualizationDef().setDrillSelection(drillCategory);
    }

    private void saveCurrentSelection() {
        getVisualizationDef().getSelection().setDrillSelections(getChartModel().getDrillSelections());
        getVisualizationDef().getSelection().makeSelectionStateForCategories(new ArrayList<DrillCategory>(getChartModel().getChartSelectionState().getSelectedItems()));


/*	    //Following code removes items not in the highchartresponse

	    List<String> categoryNames = chartModel.getHighchartResponse().getCategoryNames();
	    HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
	    List<DrillCategory> selection = chartModel.getChartSelectionState().getSelectedItems();
	    for(String categoryName: categoryNames){
	        categoryMap.put(categoryName, 0);
	    }

	    List<DrillCategory> narrowedSelection = new ArrayList<DrillCategory>();
	    boolean add;
	    for(DrillCategory category:selection){
	        DrillCategory narrowedCategory = new DrillCategory();
	        ArrayList<String> narrowedCategories = new ArrayList<String>();
	        List<String> selectedCategories = category.getCategories();
	        add = false;
	        for(String selectedCategory : selectedCategories){
	            if(categoryMap.get(selectedCategory) != null){
	                narrowedCategories.add(selectedCategory);
	                add=true;
	            }
	        }
	        if(add){
	            narrowedCategory.setCategories(narrowedCategories);
	            narrowedSelection.add(narrowedCategory);
	        }
	    getVisualizationDef().getSelection().makeSelectionStateForCategories(selection);
		}*/
    }

    @Override
    public ImagingRequest getImagingRequest() {
        return getView().getImagingRequest();
    }
    // little hack that will take care of the bug

    /**
     * Clears the drill selection and keeps the selected item state.
     * To not clear the drill selection, call load();
     */
    @Override
    public void loadVisualization() {
        showProgressIndicator();

        getChartModel().setDrillSelections(new ArrayList<String>());
        getVisualizationDef().getSelection().setFromSelection(NullSelection.instance);
        getChartModel().setChartSelectionState(getVisualizationDef().getSelection());
        firstLoad = true;
        load(getChartModel().getDrillSelections());

        this.appendNotificationText(NotificationLabel.FILTER, getVisualizationDef().getFilter() != null);
        this.appendNotificationText(NotificationLabel.SELECTION, getVisualizationDef().getChartSettings().getFilterCriteria().size() > 0);
        this.appendBroadcastIcon();
    }

    @Override
    protected void handleViewLoad2() {
        if (!sizeAdjusted) {
            this.getView().onResize();
            sizeAdjusted = true;
        }
    }

    @Override
    public void applySelection(Selection selection) {
        getView().setViewsNotLoaded();
        getVisualizationDef().getSelection().setFromSelection(selection);
        getChartModel().setChartSelectionState(getVisualizationDef().getSelection());

        hideProgressIndicator();

        //if the overview is tiny and we don't save the state it will get zoomed out.
        Range range = overviewPresenter.getCategoryRange();
        overviewPresenter.invalidateCurrentRange();
        overviewPresenter.setRange(range, true);
        updateMenus();
    }

    @Override
    public void broadcastNotify(String text) {
        getView().broadcastNotify(text);
        appendBroadcastIcon();
        ChartMetricsView.EVENT_BUS.fireEvent(new ExpireMetrics(getUuid()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Visualization> AbstractMenuManager<V> createMenuManager() {
        return (AbstractMenuManager<V>) new ChartMenuManager(this);
    }

    @Override
    public ChartView createView() {
        return new ChartView(this);
    }

    public StatisticsHolder getStatisticsHolder() {
        return statisticsHolder;
    }

    public DrillSelectionCallback getDrillSelectionCallback() {
        return drillSelectionCallback;
    }

    public void findCategory(String category) {
        VortexFuture<Integer> future = getVortex().createFuture();

        future.addEventHandler(new AbstractVortexEventHandler<Integer>() {
            @Override
            public void onSuccess(Integer position) {

                int rangeStart = position;
                int rangeEnd = position + DEFAULT_SEARCH_ZOOM;
                int endIndex = overviewPresenter.getNumberOfCategories() - 1;

                if (endIndex < DEFAULT_SEARCH_ZOOM) {
                    //No-op, they can use their eyes
                } else if (rangeStart + DEFAULT_SEARCH_ZOOM > endIndex) {
                    rangeStart = endIndex - DEFAULT_SEARCH_ZOOM;
                    rangeEnd = endIndex;
                } else {
                    rangeEnd = rangeStart + DEFAULT_SEARCH_ZOOM;
                }

                Range range = new Range(rangeStart, rangeEnd);

                overviewPresenter.setRange(range);
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }
        });
        future.execute(ChartActionsServiceProtocol.class).getCategoryRangeIndex(getVisualizationDef().getUuid(), getChartModel().getDrillSelections(), category);
    }

    public void f1() {
        getOverviewPresenter().reset();
        if (getVisualizationDef().isSuppressLoadAtStartup()) {
            if (isViewLoaded()) {
                getView().clearSelectedView();
                loadVisualization();
            }
        } else {
            handleViewLoadOrLoadVisualization();
        }
        getChrome().setName(getName());
    }

    /**
     * based on the number of category definition.
     *
     * @return
     */
    public boolean isDrillChart() {
        return getVisualizationDef().getChartSettings().getCategoryDefinitions().size() > 1;
    }

    public boolean isNextDrillLevelAvailable(List<String> drillCategories) {
        return drillCategories.size() < getVisualizationDef().getChartSettings().getCategoryDefinitions().size();
    }

    @Override
    public VortexFuture<Void> saveSettings(final boolean refreshOnSuccess, final boolean isStructural, final boolean clearTransient) {
        if (clearTransient) {
            resetRanges();
            if (null != overviewPresenter) {
//                overviewPresenter.reset();
            }
        }
        VortexFuture<Void> voidVortexFuture = super.saveSettings(refreshOnSuccess, isStructural, clearTransient);
        voidVortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                Boolean hideOverview = getVisualizationDef().getHideOverview();
                if (hideOverview) {
                    setOverviewVisibility(false);
                    getMenuManager().enable(MenuKey.SHOW_OVERVIEW);
                    getMenuManager().hide(MenuKey.HIDE_OVERVIEW);
                } else {
                    setOverviewVisibility(true);
                    getMenuManager().hide(MenuKey.SHOW_OVERVIEW);
                    getMenuManager().enable(MenuKey.HIDE_OVERVIEW);
                }
            }
        });

        return voidVortexFuture;
    }

    @Override
    public void reload() {
        getView().setViewsNotLoaded();
        WebMain.injector.getVortex().execute(new Callback<DrillChartViewDef>() {
            @Override
            public void onSuccess(DrillChartViewDef result) {
                setVisualizationDef(result);
                resetRanges();
                overviewPresenter.reset();

                if (getVisualizationDef().isSuppressLoadAtStartup()) {
                    if (isViewLoaded()) {
                        loadVisualization();
                    }
                } else {
                    handleViewLoadOrLoadVisualization();
                }
            }
        }, VisualizationActionsServiceProtocol.class).getVisualization(dvPresenter.getUuid(), getUuid());
    }

    /**
     * Experimental.
     */
    public void resetRanges() {
        this.ranges.clear();
    }

    public void save() {
        saveSettings(false, false, false);
    }

    public void selectAll() {
        showProgressIndicator();
        WebMain.injector.getVortex().execute(new Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> selection) {
                selectCategories(selection);
                getView().setViewsNotLoaded();
            }
        }, ChartActionsServiceProtocol.class).selectAll(getUuid(), chartModel.getDrillSelections());
    }

    public void selectCategories(List<String> keys) {

        ChartModel chartModel = getChartModel();
        List<String> drillSelections = chartModel.getDrillSelections();
        List<DrillCategory> selectedItems = new ArrayList<DrillCategory>();

        ArrayList<String> categories = new ArrayList<String>();
        for (String drillSelection : drillSelections) {
            categories.add(drillSelection);
        }

        for (String key : keys) {
            DrillCategory selectedItem = new DrillCategory();
            ArrayList<String> selectedCategories = Lists.newArrayList(categories);
            selectedCategories.add(key);
            selectedItem.setCategories(selectedCategories);
            selectedItems.add(selectedItem);
        }

        ChartSelectionState chartSelectionState = new ChartSelectionState();
        chartSelectionState.makeSelectionStateForCategories(selectedItems);
        chartSelectionState.setDrillSelections(chartModel.getDrillSelections());

        applySelection(chartSelectionState);
    }

    public void toggleDisplay() {
        int currentView = getView().toggleViewVisibility();

        DisplayFirst updatedView = currentView == getView().CHART_TAB_INDEX ? DisplayFirst.CHART : DisplayFirst.TABLE;

        // save onto viz def so we don't get out of sync when save settings is called.
        getVisualizationDef().getChartSettings().setCurrentView(updatedView);
        if (updatedView == DisplayFirst.CHART)
            getVisualizationDef().getChartSettings().setChartDisplay(true);
        else
            getVisualizationDef().getChartSettings().setChartDisplay(false);

        // update current view on server.
        VortexFuture<Boolean> vortexFuture = WebMain.injector.getVortex().createFuture();
        vortexFuture.execute(ChartActionsServiceProtocol.class).updateCurrentView(getUuid(), updatedView);
    }

    public void drillUpByLevel() {

        if (currentDrillLevel == 0 || !isDrillChart()) {
            return;
        } else {
            if (getChartModel().getDrillSelections().size() >= 1) {
                List<String> strings = new ArrayList<String>(getChartModel().getDrillSelections().subList(0, getChartModel().getDrillSelections().size() - 1));
                drillSelectionCallback.drillCategorySelected(strings);
            }
        }
    }

    public void showMetrics() {
        if (progressBar == null) {
            if (metrics == null) {
                metrics = new ChartMetricsView(this);
            }
            Scheduler.get().scheduleDeferred(() -> {
                metrics.show();
            });
        }
    }

    public void setOverviewVisibility(boolean visible) {
        getView().setOverviewVisibility(visible);
    }

    public void doToggleBreadcrumb() {
        boolean isOn = getChartModel().toggleBreadcumb();
        // will get saved automatically
        getVisualizationDef().getChartSettings().setShowBreadcrumbs(isOn);
        saveSettings(false, false, false);
        getView().setupBreadcrumbs();
    }

    public ChartMetricsView getMetrics() {
        return metrics;
    }

    public void createOverviewScrolling(final List<String> drillCategories, OverviewResponse result, int count) {
        if (!overviewPresenter.isLoaded()) {
            initOverviewPresenter();
        }

        setNewOverviewValues(result);
        int endIndex = count - 1;
        Range range = new Range(0, endIndex);

        boolean rangeSet = false;

        if (drillCategories != null) {
            final int drillLocation = drillCategories.size();
            currentDrillLevel = drillLocation;

            //TODO: JD-FIX gotta validate that the old range is valid instead of just using it
            //if (sizes.size() >= drillLocation && sizes.get(drillLocation) == data.size() && ranges.get(drillLocation) != null) {
            if (sizes.size() > drillLocation && sizes.get(drillLocation) == result.getTotalCategories() && ranges.get(drillLocation) != null) {

                if (ranges.get(drillLocation) != Range.EMPTY_RANGE) {
                    range = ranges.get(drillLocation);
                }

                //This double checks and defaults to count if we screwed up
                if (range.getEndIndex() - range.getStartIndex() < count) {

                } else {
                    range = new Range(0, count - 1);
                }

                if (overviewPresenter.getCategoryRange().equals(Range.EMPTY_RANGE)) {
                    overviewPresenter.initViewPortMax(0, endIndex, true);
                } else {
                    int chartMaxChartCategories = WebMain.getClientStartupInfo().getChartMaxChartCategories();
                    if (result.getTotalCategories() > chartMaxChartCategories) {
                        overviewPresenter.initViewPortMax(0, chartMaxChartCategories - 1, true);
                    } else {
                        overviewPresenter.initViewPortMax(0, result.getTotalCategories() - 1, true);
                    }
                }

                overviewPresenter.setScrollRange(range, true);

                rangeSet = true;
            } else {
                if (sizes.size() - 1 >= currentDrillLevel) {
                    sizes.remove(currentDrillLevel);
                }
                if (ranges.size() - 1 >= currentDrillLevel) {
                    ranges.remove(currentDrillLevel);
                }
                sizes.put(drillLocation, -1);
                ranges.put(drillLocation, range);
            }
        }

        if (!rangeSet) {
            overviewPresenter.setScrollRange(range, false);
        }

        Scheduler.get().scheduleDeferred(() -> resizeOverview(getView().getOffsetWidth()));
        overviewPresenter.forceFireRangeChangedEvent(overviewPresenter.categoryRange);
    }

    private OverviewPresenter initOverviewPresenter() {
        //overviewPresenter.getOverviewView().setOverviewContent(new ColoredOverview());
        overviewPresenter.getOverviewView().setOverviewContent(new CanvasOverview());
        overviewPresenter.build(getView().getOffsetWidth());
        overviewPresenter.addRangeChangedEventHandler(new RangeChangedEventHandler() {
            @Override
            public void onRangeChanged(RangeChangedEvent event) {
                showProgressIndicator();
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @Override
                    public void execute() {
                        overviewPresenter.resetDragState();
                        renderChart(event);
                        hideProgressIndicator();
//						}
                        //maybe pass the event it..

                    }
                });
            }
        });
        return overviewPresenter;
    }

    private void setNewOverviewValues(OverviewResponse result) {
        if (result != null && overviewPresenter.isLoaded()) {
            int oldIndividualBinCount = overviewPresenter.getIndividualBinCount();
            int oldStartIndex = overviewPresenter.getStartPosition() * oldIndividualBinCount;
            int oldEndPosition = overviewPresenter.getEndPosition();
            int oldWidth = overviewPresenter.getWidth();
            int oldEndIndex = 0;
            if (oldEndPosition < oldWidth) {
                oldEndIndex = overviewPresenter.getEndPosition() * oldIndividualBinCount;
            }

            overviewPresenter.setIndividualBinCount(result.getOverviewBinSize());

            overviewPresenter.setCategoryData(result.getTotalCategories(), result.getOverviewColors(), getView().getOffsetWidth() - OVERVIEW_CONTENT_MARGIN, false);

            int newIndividualBinCount = overviewPresenter.getIndividualBinCount();
            int newStartPosition = oldStartIndex / newIndividualBinCount;
            int newEndPosition = overviewPresenter.getWidth();
            if (oldEndIndex > 0) {
                newEndPosition = oldEndIndex / newIndividualBinCount;
                if (oldEndIndex % newIndividualBinCount > 0) {
                    newEndPosition++;
                }
            }

            Range range = new Range(newStartPosition, newEndPosition);

//            getView().setOverviewVisibility(true);

            overviewPresenter.setRange(range, false);

        }
    }

    public void resizeOverview(int width) {
        int widthOfTheOverviewContainer = width - OVERVIEW_CONTENT_MARGIN;
        if (widthOfTheOverviewContainer <= 0) {
            return;
        }
        int numberOfCategoriesInData = overviewPresenter.getNumberOfCategories();
        int individualBinCount = overviewPresenter.getIndividualBinCount();
        redrawOverview(width, widthOfTheOverviewContainer, numberOfCategoriesInData, individualBinCount);
    }

    private void redrawOverview(int width, int widthOfTheOverviewContainer, int numberOfCategoriesInData, int individualBinCount) {
        overviewPresenter.resizeWidth(width - OVERVIEW_CONTENT_MARGIN, false);

    }

    public OverviewPresenter getOverviewPresenter() {
        return overviewPresenter;
    }

    public OverviewView getOverviewView() {
        return overviewPresenter.getOverviewView();
    }

}
