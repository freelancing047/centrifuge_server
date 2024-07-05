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
package csi.client.gwt.viz.chart.view;

import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.data.shared.ChartTableListStore;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.event.StoreSortEvent;
import com.sencha.gxt.data.shared.event.StoreSortEvent.StoreSortHandler;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.event.RowClickEvent;
import com.sencha.gxt.widget.core.client.grid.*;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.model.ChartModel;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.table.CellHoverEvent;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.RemoteLoadingGridComponentManager;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.common.service.api.ChartActionsServiceProtocol;
import csi.shared.core.visualization.chart.HighchartResponse;
import csi.shared.core.visualization.chart.HighchartSeriesData;
import csi.shared.core.visualization.chart.SeriesInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Centrifuge Systems, Inc.
 * <p>
 * Chart viz table tab
 */
public class ChartTableTab extends SimpleLayoutPanel {
    private static final String NO_MESSAGE_STYLE = "timeline-noresults";
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private ChartView chartView;
    private ChartModel chartModel;
    private HighchartResponse response;
    private List<SeriesInfo> highchartSeriesInfo;
    private List<HighchartSeriesData> highchartSeriesData;
    private ChartSelectionState chartSelectionState;
    private Grid<ChartGridRow> chartGrid;
    private RemoteLoadingGridComponentManager<ChartGridRow> gridComponentManager;
    private ChartGridRowProperty rowProperty = new ChartGridRowProperty();
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private ListStore<ChartGridRow> listStore;
    private ChartTableSelectionModel selectionModel;
    private HandlerRegistration selectionChangedHandlerRegistration;
    private GridContainer container;
    private boolean gridDone = false;
    private int currentDrillLevel;
    private boolean isLimitDisplayed = false;

    public ChartTableTab() {
        super();
        setSize("100%", "100%");

    }

    public boolean isGridDone() {
        return gridDone;
    }

    public void setChartView(ChartView chartView) {
        this.chartView = chartView;

    }

    public void setupGrid(ChartPresenter chartPresenter, HighchartResponse response) {
        chartModel = chartPresenter.getChartModel();
        chartSelectionState = chartModel.getChartSelectionState();
        currentDrillLevel = chartModel.getDrillSelections().size();
        this.response = response;
        highchartSeriesInfo = response.getSeriesInfos();
        highchartSeriesData = response.getSeriesData();
        List<Store.StoreSortInfo<ChartGridRow>> sortInfos = maybeSaveSortOrder();
        setupGridDataProvider();
        String header = chartPresenter.getVisualizationDef().getChartSettings().getCategoryDefinitions().get(currentDrillLevel).getComposedName() + " (" + response.getCategorySize() + ")";
        ColumnConfig<ChartGridRow, ?> dimensionColumn;
        ChartSettings chartSettings = chartPresenter.getVisualizationDef().getChartSettings();
        List<CategoryDefinition> categoryDefinitions = chartSettings.getCategoryDefinitions();
        CategoryDefinition categoryDefinition = categoryDefinitions.get(currentDrillLevel);
        CsiDataType categoryDefinitionDataType = categoryDefinition.getDerivedType();
        if (categoryDefinitionDataType == CsiDataType.Integer) {
            dimensionColumn = new ColumnConfig<ChartGridRow, Integer>(rowProperty.dimensionIntegerValue(), 200, header);
        } else if (categoryDefinitionDataType == CsiDataType.Number) {

            dimensionColumn = new ColumnConfig<ChartGridRow, String>(rowProperty.dimensionNumberValue(), 200, header);
        } else {

            dimensionColumn = new ColumnConfig<ChartGridRow, String>(rowProperty.dimensionStringValue(), 200, header);
        }
        dimensionColumn.setToolTip(header);

        dimensionColumn.setMenuDisabled(true);
        ColumnModel<ChartGridRow> cm = createColumnModel(dimensionColumn, response.getTotals());

        listStore = new ChartTableListStore(gridComponentManager.getStore());
        chartGrid = new ResizeableGrid<ChartGridRow>(listStore, cm);
        styleChartGrid(dimensionColumn);
        maybeRestoreSortOrder(sortInfos);
        setGridAsWidget();

        chartGrid.addHandler(new CellHoverEvent.CellHoverHandler() {
            @Override
            public void onCellHover(CellHoverEvent event) {
                Element cell = chartGrid.getView().getCell(event.getRowIndex(), event.getCellIndex());
                if (TextMetrics.get().getWidth(cell.getInnerText()) > cell.getClientWidth()) {
                    String msg = "";
                    msg = cell.getInnerText().replace("\n", "<br/>");
                    chartGrid.setToolTip(msg);
                } else {
                    chartGrid.hideToolTip();
                }
            }
        }, CellHoverEvent.getType());


        gridDone = true;
    }

    private List<Store.StoreSortInfo<ChartGridRow>> maybeSaveSortOrder() {
        if (chartGrid != null) {
            ListStore<ChartGridRow> store = chartGrid.getStore();
            if (store != null) {
                return store.getSortInfo();
            }
        }
        return null;
    }

    private void setupGridDataProvider() {
        GridComponentFactory componentFactory = WebMain.injector.getGridFactory();
        gridComponentManager = componentFactory.createRemoteLoading(rowProperty.key(), ChartActionsServiceProtocol.class, new ChartTableTabGridLoadCallback(response));
        // In the chart-overview window case, use local sort.
        gridComponentManager.getLoader().setRemoteSort(false);
    }

    private ColumnModel<ChartGridRow> createColumnModel(ColumnConfig<ChartGridRow, ?> dimensionColumn, List<Number> totalsIn) {
        List<ColumnConfig<ChartGridRow, ?>> columnConfigList = new ArrayList<ColumnConfig<ChartGridRow, ?>>();
        // Add the dimension column and then each of the metric column.
        columnConfigList.add(dimensionColumn);
        AggregationRowConfig<ChartGridRow> total = new AggregationRowConfig<ChartGridRow>();
        total.setRenderer(dimensionColumn, new AggregationSafeHtmlRenderer<ChartGridRow>(_constants.chartTableTab_Total()));
        ColumnModel<ChartGridRow> cm = addEachMetricToColumnModel(columnConfigList, total, totalsIn);
        cm.addAggregationRow(total);
        return cm;
    }

    private ColumnModel<ChartGridRow> addEachMetricToColumnModel(List<ColumnConfig<ChartGridRow, ?>> columnConfigList, AggregationRowConfig<ChartGridRow> total, List<Number> totalsIn) {
        // Add each of the metrics as a column. Each metric is represented as a data series.
        for (int index = 0; index < highchartSeriesData.size(); index++) {
            SeriesInfo seriesInfo = highchartSeriesInfo.get(index);
            String metricName = seriesInfo.getMetricName();
            ColumnConfig<ChartGridRow, Number> metricColumn =
                    new ColumnConfig<ChartGridRow, Number>(rowProperty.metricValue(index, metricName), 150, metricName);
            metricColumn.setToolTip(metricName);
            metricColumn.setHorizontalAlignment(HorizontalAlignmentConstant.endOf(Direction.LTR));
            columnConfigList.add(metricColumn);
            metricColumn.setHideable(false);
            metricColumn.setMenuDisabled(true);
            if ((null != totalsIn) && (totalsIn.size() > index)) {
                total.setRenderer(metricColumn, new AggregationSafeHtmlRenderer<ChartGridRow>(totalsIn.get(index).toString()));
            }
        }
        return new ColumnModel<ChartGridRow>(columnConfigList);
    }

    private void styleChartGrid(ColumnConfig<ChartGridRow, ?> dimensionColumn) {
        GridView<ChartGridRow> view = chartGrid.getView();
        if (view != null) {
            view.setAutoExpandColumn(dimensionColumn);
            view.setAutoExpandMax(2000);
            view.setAutoFill(true);
            view.setStripeRows(true);
            view.setColumnLines(false);
            view.setSortingEnabled(true);
        }
        chartGrid.setBorders(false);
        chartGrid.setLoadMask(true);
        selectionModel = new ChartTableSelectionModel();
        chartGrid.setSelectionModel(selectionModel);
        addSelectionChangedHandler();
        addSortHandler();
    }

    /**
     * Adds a RowClickEvent to the grid, however, will only trigger if the control key is down in the event.
     * allows the user to drill down into the data.
     */
    private void addDrillSelectionHandler() {

        chartGrid.addRowClickHandler(new RowClickEvent.RowClickHandler() {
            @Override
            public void onRowClick(RowClickEvent event) {
                if (!event.getEvent().getCtrlKey()) {
                    List<String> drillCategories = new ArrayList<String>();
                    List<DrillCategory> selectedItems = convertChartGridRowToDrillCategory(chartModel.getDrillSelections());
                    for (DrillCategory cat : selectedItems) {
                        drillCategories.addAll(cat.getCategories());
                    }

                    if (drillCategories.size() > 0) {
                        chartSelectionState.setDrillSelections(drillCategories);
                        chartView.drillToSelection(drillCategories);
                    }

                    chartView.setViewsNotLoaded();
                }
            }
        });
    }

    /**
     * Converts the list of ChartGridRows to List of DrillCategory
     *
     * @param drillSelections
     * @return List<DrillCategory> selectedItems - items that are selected in the grid converted to DrillCategorys
     */
    private List<DrillCategory> convertChartGridRowToDrillCategory(List<String> drillSelections) {
        List<ChartGridRow> selectedRows = selectionModel.getSelectedItems();
        List<DrillCategory> selectedItems = new ArrayList<DrillCategory>();

        for (ChartGridRow selectedRow : selectedRows) {
            DrillCategory selectedItem = new DrillCategory();
            ArrayList<String> categories = new ArrayList<String>();
            for (String drillSelection : drillSelections) {
                categories.add(drillSelection);
            }
            categories.add(selectedRow.getDimension());
            selectedItem.setCategories(categories);

            selectedItems.add(selectedItem);
        }

        return selectedItems;
    }

    private void addSelectionChangedHandler() {
        removeSelectionChangedHandler();

        selectionChangedHandlerRegistration = selectionModel.addSelectionChangedHandler(new SelectionChangedHandler<ChartGridRow>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<ChartGridRow> event) {
                List<DrillCategory> selectedItems = convertChartGridRowToDrillCategory(chartModel.getDrillSelections());
                chartSelectionState.makeSelectionStateForCategories(selectedItems);
                chartSelectionState.setDrillSelections(chartModel.getDrillSelections());
                chartView.notifyRenderBarChartDeferred();
            }
        });
    }

    private void removeSelectionChangedHandler() {
        if (selectionChangedHandlerRegistration != null) {
            selectionChangedHandlerRegistration.removeHandler();
            selectionChangedHandlerRegistration = null;
        }
    }

    private void addSortHandler() {
        listStore.addStoreSortHandler(new StoreSortHandler<ChartGridRow>() {
            @Override
            public void onSort(StoreSortEvent<ChartGridRow> event) {
                selectionModel.onSort();
            }
        });
    }

    private void maybeRestoreSortOrder(List<Store.StoreSortInfo<ChartGridRow>> sortInfos) {
        if (sortInfos != null) { // attempt to restore sort state
            for (Store.StoreSortInfo<ChartGridRow> sortInfo : sortInfos) {
                ListStore<ChartGridRow> store = chartGrid.getStore();
                if (store != null) {
                    store.addSortInfo(sortInfo);
                }
            }
        }
    }

    public void clearClientSort() {
        if (chartGrid != null && chartGrid.getStore() != null) {
            chartGrid.getStore().getSortInfo().clear();
        }
    }

    private void setGridAsWidget() {
        container = new GridContainer();
        chartGrid.getElement().getStyle().setOverflowY(Style.Overflow.HIDDEN);
        container.setSize("100%", "100%");
        container.setGrid(chartGrid);
        setWidget(container);

    }

    public void render() {
        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ChartGridRow>> loader = gridComponentManager.getLoader();
        if (loader != null) {
            Set<String> selectedCategoryNames = extractSelectedCategoryNames(chartSelectionState.getSelectedItems());
            List<ChartGridRow> selectedRows = computeSelectedRows(selectedCategoryNames);
            loadGrid(loader, selectedRows);
        }

    }

    private Set<String> extractSelectedCategoryNames(List<DrillCategory> selectedItems) {
        Set<String> selectedCategoryNames = Sets.newHashSet();

        for (DrillCategory drillCategory : selectedItems) {
            ArrayList<String> categories = drillCategory.getCategories();
            selectedCategoryNames.add(categories.get(currentDrillLevel));
        }

        return selectedCategoryNames;
    }

    private List<ChartGridRow> computeSelectedRows(Set<String> selectedCategoryNames) {
        List<ChartGridRow> selectedRows = new ArrayList<ChartGridRow>();

        List<String> categoryNames = response.getCategoryNames();

        for (int j = 0; j < categoryNames.size(); j++) {
            String categoryName = categoryNames.get(j);
            if (selectedCategoryNames.contains(categoryName)) {
                ChartGridRow row = new ChartGridRow();
                row.setDimension(categoryName);
                List<Number> metricValues = row.getMetricValues();
                for (HighchartSeriesData seriesData : highchartSeriesData) {
                    metricValues.add(seriesData.getData().get(j));
                }
                selectedRows.add(row);
            }
        }

        return selectedRows;

    }

    private void loadGrid(PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ChartGridRow>> loader, List<ChartGridRow> selectedRows) {
        removeSelectionChangedHandler();
        selectionModel.setSelection(selectedRows);
        loader.load();
        addSelectionChangedHandler();
        addDrillSelectionHandler();
    }

    public List<List<String>> getData() {
        List<List<String>> data = new ArrayList<List<String>>();

        for (ChartGridRow row : chartGrid.getStore().getAll()) {
            List<String> line = new ArrayList<String>();
            line.add(row.getDimension());

            for (Number measure : row.getMetricValues()) {
                line.add(measure.toString());
            }

            data.add(line);
        }

        return data;
    }

    public void selectRows(List<String> keys) {
        selectionModel.setSelected(keys);
    }

    public boolean isLimitDisplayed() {
        return isLimitDisplayed;
    }

    public void setLimitDisplayed(boolean limitDisplayed) {
        isLimitDisplayed = limitDisplayed;
    }


}