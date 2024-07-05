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

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.viz.chart.model.ChartModel;
import csi.client.gwt.viz.chart.overview.view.OverviewView;
import csi.client.gwt.viz.chart.presenter.BreadcrumLinkClickHandler;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.BroadcastAlert;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.DisplayFirst;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.service.api.ChartActionsServiceProtocol;
import csi.server.common.util.ValuePair;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.imaging.PNGImageComponent;
import csi.shared.core.imaging.SVGImageComponent;
import csi.shared.core.visualization.chart.HighchartPagingResponse;
import csi.shared.core.visualization.chart.HighchartResponse;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;

import java.util.List;
import java.util.Map;


/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartView extends ResizeComposite {

    public static final int CHART_TAB_INDEX = 0;
    private static final int TABLE_TAB_INDEX = 1;
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static Integer maxChartCategorySize;
    private final ChartPresenter chartPresenter;
    @UiField
    public LayoutPanel chartLayoutPanel;
    @UiField

    LayoutPanel mainLayoutPanel;
    @UiField
    CollapsibleBreadcrumbs drillBreadcrumb;
    @UiField
    CsiTabPanel tabPanel;
    @UiField
    Tab chartTab;
    @UiField
    BarChartTab barChartTab;
    @UiField
    Tab tableTab;
    @UiField
    ChartTableTab chartTableTab;
    @UiField
    HorizontalPanel overviewDiv;
    @UiField
    FlowPanel mainChartPanel;
    boolean isTableRendered = false;
    private ButtonGroup buttonGroup;
    private boolean isChartRendered = false;
    private ChartModel chartModel;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    // NO DATA/ERROR PANEL.
    private final String NO_DATA_MESSAGE = _constants.chartNoResultsMessage();
    private boolean renderBarChartTabDeferred = false;
    private HighchartResponse response;
    // initial value, if its -1 we will load based on default from settings -
    // WARNING: UNTIL THE VIEW IS TOGGLED THIS WILL STAY AT -1!!!! BUG
    private int currentTabIndex = -1;
    private int width = -1;
    private int height = -1;

    public ChartView(final ChartPresenter chartPresenter) {
        this.chartPresenter = chartPresenter;
        this.chartModel = chartPresenter.getChartModel();
        initWidget(uiBinder.createAndBindUi(this));
        initOverviewDiv();
        setupNormalLayout();
        tabPanel.selectTab(0);
        addBarChartTabHandler();
        chartTableTab.setChartView(this);
        addTableTabHandler();

        createControlButtons();
        Scheduler.get().scheduleDeferred(() -> tabPanel.hideLinks());
        mainChartPanel.setSize("100%", "100%");
//
        this.addAttachHandler(event -> {
//            tabPanel.getElement().getStyle().setOpacity(0);
            selectDefaultView();
//            Scheduler.get().scheduleFixedDelay(()->{
//                tabPanel.getElement().getStyle().setOpacity(1);
//                return false;
//            },200);

            setOverviewVisibility(!chartPresenter.getVisualizationDef().getHideOverview());
        });
    }

    static int getMaxChartCategorySize() {
        if (maxChartCategorySize == null) {
            maxChartCategorySize = WebMain.getClientStartupInfo().getChartMaxChartCategories();
        }
        return maxChartCategorySize;
    }

    // no more limit reached so this makes it easy.
    private void showNoData() {
        chartPresenter.getChrome().addFullScreenWindow(NO_DATA_MESSAGE, IconType.INFO_SIGN);
        setupBreadcrumbs();
        createControlButtons();
    }

    private void showDrilledIntoTheVoid() {
        chartPresenter.getChrome().addFullScreenWindow("No data at the current drill level", IconType.INFO_SIGN);
        setupBreadcrumbs();
        createControlButtons();


        drillBreadcrumb.getElement().getStyle().setZIndex(1);
        buttonGroup.getElement().getStyle().setZIndex(1);
        showBreadcrumbs();
        drillBreadcrumb.setVisible(true);
    }

    // restores the visibility of the rest of the layout panel
    private void hideNoData() {
        chartPresenter.getChrome().removeFullScreenWindow();
    }

    private void createControlButtons() {
        if (buttonGroup != null) {
            buttonGroup.removeFromParent();
        }
        buttonGroup = new ButtonGroup();
//        buttonGroup.getElement().getStyle().setOpacity(.65);
        buttonGroup.clear();
        {
            Button button = new Button();
            button.setIcon(IconType.RESIZE_FULL);
            button.setType(ButtonType.DEFAULT);
            button.addStyleName("overlay-clear");//NON-NLS
            button.addStyleName("rightControlButton");//NON-NLS
            buttonGroup.add(button);
            button.setTitle(_constants.chartView_fitToScreen());

            button.addClickHandler(event -> {
                chartPresenter.getOverviewPresenter().invisibleReset();
                chartPresenter.loadDrillCategories(chartPresenter.getChartModel().getDrillSelections());
                chartPresenter.setFirstLoad(false);
            });
        }
        {
            Button button = new Button();
            button.setIcon(IconType.SEARCH);
            button.setType(ButtonType.DEFAULT);
            button.addStyleName("overlay-clear");//NON-NLS
            button.addStyleName("rightControlButton");//NON-NLS
            buttonGroup.add(button);

            button.setTitle(_constants.chartView_search());
            button.addClickHandler(createSearchHandler());
        }
        {
            Button viewToggleButton = new Button();


            setCorrectIconForToggleView(viewToggleButton);

            viewToggleButton.setType(ButtonType.DEFAULT);
            viewToggleButton.addStyleName("overlay-clear");//NON-NLS
            viewToggleButton.addStyleName("rightControlButton");//NON-NLS
            buttonGroup.add(viewToggleButton);
            viewToggleButton.addClickHandler(event -> {
                chartPresenter.toggleDisplay();
                setCorrectIconForToggleView(viewToggleButton);
            });

        }
        if (chartPresenter.isDrillChart() && chartPresenter.getCurrentDrillLevel() != 0) {

            {
                Button button = new Button();
                button.setIcon(IconType.CHEVRON_UP);
                button.setTitle(_constants.chartView_drillUp());
                button.setType(ButtonType.DEFAULT);
                button.addStyleName("overlay-clear");//NON-NLS
                button.addStyleName("rightControlButton");//NON-NLS
                buttonGroup.add(button);
                button.addClickHandler(event -> {
                    chartPresenter.drillUpByLevel();
//                mainLayoutPanel.getWidgetContainerElement(mainLayoutPanel.getWidget(1));
                });
            }
        }

//        buttonGroup.getElement().getStyle().setOpacity(.7);

        Scheduler.get().scheduleDeferred(() -> {
            chartPresenter.getChrome().addButton(buttonGroup);
        });

    }

    private void setCorrectIconForToggleView(Button viewToggleButton) {
        switch (tabPanel.getSelectedTab()) {
            case CHART_TAB_INDEX:
                viewToggleButton.setIcon(IconType.TABLE);
                viewToggleButton.setTitle(_constants.chartView_showTable());
                break;
            case TABLE_TAB_INDEX:
                viewToggleButton.setIcon(IconType.BAR_CHART);
                viewToggleButton.setTitle(_constants.chartView_showChart());
                break;
            default:
                break;
        }
    }

    public void setViewsNotLoaded() {
        isChartRendered = false;
        isTableRendered = false;
        if (barChartTab.getChart() != null) {
            barChartTab.getChart().removeFromParent();
        }
    }

    public void clearTableTabClientSort() {
        chartTableTab.clearClientSort();
    }

    private void initOverviewDiv() {
        overviewDiv.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);

        OverviewView overview = chartPresenter.getOverviewView();
        overviewDiv.add(overview);
        overviewDiv.setCellVerticalAlignment(overview, HasVerticalAlignment.ALIGN_MIDDLE);
        overviewDiv.setCellHorizontalAlignment(overview, HasHorizontalAlignment.ALIGN_CENTER);

        overviewDiv.setVisible(false);
    }

    private ClickHandler createSearchHandler() {
        return event -> {
            List<String> categories = chartModel.getHighchartResponse().getCategoryNames();
            final SearchCategoryDialog dialog = new SearchCategoryDialog(categories);
            dialog.addClickHandler(event12 -> {
                if (dialog.isValid()) {
                    String category = dialog.getName();
                    chartPresenter.findCategory(category);
                    dialog.hide();
                }
            });

            dialog.addQueryHandler(event1 -> {
                String query = event1.getQuery();
//                if(dialog.isStoreEmpty()) {
                updateComboBoxStore(dialog, query, chartPresenter.getVisualizationDef().getUuid(), chartModel.getDrillSelections());
//                }
            });
            dialog.show();
        };
    }

    private void updateComboBoxStore(SearchCategoryDialog dialog, String query, String vizUuid, List<String> drills) {
        VortexFuture<List<String>> future = chartPresenter.getVortex().createFuture();

        future.addEventHandler(new AbstractVortexEventHandler<List<String>>() {
            @Override
            public void onSuccess(List<String> results) {
                dialog.updateStore(results);
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }
        });
        future.execute(ChartActionsServiceProtocol.class).getQueryCategories(vizUuid, query, drills);
    }

    private void setupNormalLayout() {
        layoutDrillsAndTabs();
    }

    private void layoutDrillsAndTabs() {
        drillBreadcrumb.setVisible(true);
//        chartLayoutPanel.setWidgetTopBottom(tabPanel, HEIGHT_OF_TABS, Unit.PX,
//                0, Unit.PX);
        tabPanel.showLinks();
    }

    private void addBarChartTabHandler() {
        chartTab.addClickHandler(event -> Scheduler.get().scheduleDeferred(() -> {
            if (renderBarChartTabDeferred) {
                renderBarChartTab();
                renderBarChartTabDeferred = false;
            }
            // TODO: remove?
            if (barChartTab.isLimitDisplayed()) {
                overviewDiv.setVisible(false);
            } else {
                overviewDiv.setVisible(true);
            }

            currentTabIndex = CHART_TAB_INDEX;
        }));
    }

    private void addTableTabHandler() {
        tableTab.addClickHandler(event -> Scheduler.get().scheduleDeferred(
                () -> {
                    renderChartTableTab();
                    if (chartTableTab.isLimitDisplayed()) {
                        overviewDiv.setVisible(false);
                    } else {
                        overviewDiv.setVisible(true);
                    }

                    currentTabIndex = TABLE_TAB_INDEX;
                }));
    }

    public void clearSelectedView() {
        currentTabIndex = -1;
    }

    public void render(boolean renderNow) {
        render(response, renderNow);
    }

    public void render(HighchartResponse response, boolean renderNow) {
        this.response = response;

        createControlButtons();
        chartPresenter.hideProgressIndicator();

        chartPresenter.setFirstLoad(true);
        setupBreadcrumbs();
        // should we check if the other series have data??
        if (response.getSeriesData().get(0).getData().size() == 0) {
            if (chartModel.getDrillSelections().size() > 0) {
                showDrilledIntoTheVoid();
            } else {
                showNoData();
            }
        } else {
            hideNoData();
        }

        Scheduler.get().scheduleDeferred(() -> {
            int i = selectDefaultView();
            switch (i) {
                case CHART_TAB_INDEX:
                    if (renderNow || !renderBarChartTabDeferred) {
                        renderBarChartTab();
                    }
                    renderBarChartTabDeferred = false;
                    isTableRendered = false;
                    break;

                case TABLE_TAB_INDEX:
                    notifyRenderBarChartDeferred();
                    renderChartTableTab();
                    break;
            }
        });
    }

    /**
     * Selects a tab based on the viz def.
     *
     * @return index of the tab selected.
     */
    private int selectDefaultView() {
        chartPresenter.getChartModel().setShowBreadcumb(chartPresenter.getVisualizationDef().getChartSettings().isShowBreadcrumbs());
        DisplayFirst isFirst = getDisplay();
        tabPanel.onResize();

        // don't select anything if we are already where we need to be.
        if (tabPanel.getSelectedTab() == CHART_TAB_INDEX && isFirst == DisplayFirst.CHART) {
            return tabPanel.getSelectedTab();
        }

        if (tabPanel.getSelectedTab() == TABLE_TAB_INDEX && isFirst == DisplayFirst.TABLE) {
            return tabPanel.getSelectedTab();
        }

        switch (isFirst) {
            case CHART:
                tabPanel.selectTab(CHART_TAB_INDEX);
                break;
            case TABLE:
                notifyRenderBarChartDeferred();
                tabPanel.selectTab(TABLE_TAB_INDEX);
                break;
        }

        return tabPanel.getSelectedTab();
    }

    private DisplayFirst getDisplay() {
        if (chartPresenter.getVisualizationDef().getChartSettings().getChartDisplay() == true)
            return DisplayFirst.CHART;
        else
            return DisplayFirst.TABLE;
    }

    public boolean isTableTabSelected() {
        return tabPanel.getSelectedTab() == TABLE_TAB_INDEX;
    }

    public int toggleViewVisibility() {
        if (response != null) {
            boolean rerender;
            if (tabPanel.getSelectedTab() == TABLE_TAB_INDEX) {
                tabPanel.selectTab(CHART_TAB_INDEX);
                currentTabIndex = CHART_TAB_INDEX;

                rerender = !isChartRendered;
            } else {
                tabPanel.selectTab(TABLE_TAB_INDEX);
                currentTabIndex = TABLE_TAB_INDEX;

                rerender = !isTableRendered;
            }

            if (rerender || renderBarChartTabDeferred) {
                render(response, true);
            }
        }
        return currentTabIndex;
    }

    private void renderBarChartTab() {
        BarChartTab.disableAnimation();
        isChartRendered = true;

        Scheduler.get().scheduleDeferred(() ->
        {
            if (response != null) {
                barChartTab.setResponse(response);
                barChartTab.setupChart(chartPresenter);
                int h = chartLayoutPanel.getOffsetHeight();
                int w = chartLayoutPanel.getOffsetWidth();

                barChartTab.setPixelSize(w - 20, h - 15);
                barChartTab.render(response);
                if (!chartPresenter.getVisualizationDef().getChartSettings().getQuickSortDef().isEmpty()) {
                    preserveChartLegendVisibility();
                }
            }
        });

        if (renderBarChartTabDeferred) {
            renderBarChartTabDeferred = false;
        }
        Scheduler.get().scheduleFixedDelay(() -> {
            BarChartTab.enableAnimation();
            return false;
        }, 500);
    }

    public Map<String, Boolean> getVisibility() {
        return barChartTab.getMeasureVisibility();
    }

    private void preserveChartLegendVisibility() {
        Map<String, Boolean> visibilityOnChart = chartPresenter.getVisiblityOnChart();
        CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
        for (Map.Entry<String, Boolean> entry : visibilityOnChart.entrySet()) {
            Series ourSeries = null;
            for (Series s : barChartTab.getChart().getSeries()) {
                String currSeriesName = i18n.menuDynamicToggleSortBy(s.getOptions().get("name").toString());
                currSeriesName = currSeriesName.replace("\"", "");
                if (currSeriesName.trim().equals(entry.getKey().trim())) {
                    ourSeries = s;
                    break;
                }
            }
            if (ourSeries != null) {
                if (entry.getValue()) {
                    ourSeries.show();
                } else {
                    ourSeries.hide();
                }
            }
        }
    }


    private void fixTableSize() {
        Scheduler.get().scheduleDeferred(() -> {
            if (drillBreadcrumb != null && tabPanel != null) {
//                if (drillBreadcrumb.isVisible()) {
//                    chartTableTab.setHeight("97%");
//                } else {
                chartTableTab.setHeight("100%");
//                }
            }
            chartTableTab.onResize();
        });
    }

    public void updateBreadcrumbs() {
        drillBreadcrumb.resetVisibility();
    }

    private void renderChartTableTab() {
        isTableRendered = true;
        if (response != null) {
            fixTableSize();
            if (response.getSeriesData().get(0).getData().size() != 0) {
                chartTableTab.setupGrid(chartPresenter, response);
                chartTableTab.render();
            }
        } else {
            // should we do something?
        }
    }

    public void setupBreadcrumbs() {
        if (!chartPresenter.isDrillChart()) {
            drillBreadcrumb.setVisible(false);
            return;
        }
        if (chartPresenter.getVisualizationDef().getChartSettings().isShowBreadcrumbs()) {
//            chartLayoutPanel.setWidgetTopBottom(chartLayoutPanel.getWidget(1), 25, Unit.PX, 0, Unit.PX );
            showBreadcrumbs();
        } else {
            // we don't need to breadcrumbs or the space allocated to them.
            drillBreadcrumb.hide();
            chartLayoutPanel.getElement().getParentElement().getStyle().setTop(0, Style.Unit.PX);
//            chartLayoutPanel.setWidgetTopBottom(chartLayoutPanel.getWidget(1), 0, Unit.PX, 0, Unit.PX );
        }
        onResize();
        fixTableSize();
    }

    private void showBreadcrumbs() {
        chartLayoutPanel.getElement().getParentElement().getStyle().setTop(25, Style.Unit.PX);
        drillBreadcrumb.show();

        trimBreadcrumbsToCorrectSize();
        // until we don't have to, go add them
        while (shouldAddBreadcrumbs(chartModel)) {
            addBreadcrumbs(chartModel);
        }
        addBreadcrumbHeader(chartModel);
        drillBreadcrumb.fitBreadcrumbs();
    }

    // this might go into my class
    private void trimBreadcrumbsToCorrectSize() {
        while (drillBreadcrumb.getWidgetCount() > chartModel.getDrillSelections().size()) {
            drillBreadcrumb.remove(drillBreadcrumb.getWidgetCount() - 1);
        }
        // i don't get this if..
        if (drillBreadcrumb.getWidgetCount() > 0) {
            drillBreadcrumb.remove(drillBreadcrumb.getWidgetCount() - 1);
        }
    }

    //keep
    private boolean shouldAddBreadcrumbs(ChartModel chartModel) {
        return drillBreadcrumb.getWidgetCount() < chartModel
                .getDrillSelections().size();
    }

    private void addBreadcrumbs(ChartModel chartModel) {
        int breadcrumbSize = drillBreadcrumb.getWidgetCount();

        String categoryName = chartPresenter.getVisualizationDef().getChartSettings().getCategoryDefinitions().get(breadcrumbSize).getComposedName();
        String crumbName = chartModel.getDrillSelections().get(breadcrumbSize);
        ClickHandler handler = new BreadcrumLinkClickHandler(chartPresenter.getDrillSelectionCallback(), chartModel, breadcrumbSize);

        Breadcrumb link = new Breadcrumb(categoryName, crumbName, handler);

        if (breadcrumbSize == 0) {
            link.disableRenderState(Breadcrumb.RenderState.MICRO);
            link.disableRenderState(Breadcrumb.RenderState.HIDDEN);
        }

        drillBreadcrumb.add(link);
    }

    private void addBreadcrumbHeader(ChartModel chartModel) {
        List<CategoryDefinition> categoryDefinitions = chartPresenter.getVisualizationDef().getChartSettings().getCategoryDefinitions();

        int headerIndex = drillBreadcrumb.getWidgetCount();
        String composedName = categoryDefinitions.get(headerIndex).getComposedName();
        Breadcrumb link;

        if (headerIndex + 1 < categoryDefinitions.size()) {
            // adds the » char to the end of the last crumb if there are more.
            link = new Breadcrumb(composedName + " \u00BB");
        } else {
            link = new Breadcrumb(composedName);
        }


        link.addClickHandler(new BreadcrumLinkClickHandler(chartPresenter.getDrillSelectionCallback(), chartModel, headerIndex));

        drillBreadcrumb.add(link);
    }

    public List<List<String>> getData() {
        return chartTableTab.getData();
    }

    /**
     * Sets the chart selection state and loads the data needed to render the next level into the views.
     *
     * @param drillCategories
     */
    void drillToSelection(final List<String> drillCategories) {
        if (chartPresenter.isNextDrillLevelAvailable(drillCategories)) {
            ChartSelectionState state = new ChartSelectionState();
            state.setDrillSelections(drillCategories);
            chartPresenter.getChartModel().setChartSelectionState(state);
            chartPresenter.loadDrillCategories(drillCategories);

        }
    }

    public void setOverviewVisibility(boolean visibility) {
        overviewDiv.setVisible(visibility);
        if (visibility) {
            resizeOverview();
            overviewDiv.getElement().getParentElement().getStyle().setBottom(-8, Style.Unit.PX);
            chartLayoutPanel.getElement().getParentElement().getStyle().setBottom(26, Style.Unit.PX);

        } else {
            overviewDiv.getElement().getParentElement().getStyle().setBottom(-34, Style.Unit.PX);
            chartLayoutPanel.getElement().getParentElement().getStyle().setBottom(0, Style.Unit.PX);

        }
        if (response !=   null && !response.getSeriesData().isEmpty()) {
            render(true);
        }
    }

    public ImagingRequest getImagingRequest() {
        Chart chart = barChartTab.getChart();
        Boolean enableSecurity = WebMain.injector.getMainPresenter().getUserInfo().getDoCapco();
        ImagingRequest request = new ImagingRequest();
        if (chart != null && tabPanel.getSelectedTab() == CHART_TAB_INDEX) {

            request.setWidth(chart.getOffsetWidth());
            request.setHeight(chart.getOffsetHeight());

            String text = SecurityBanner.getBannerText();

            if(text != null && enableSecurity){
                request.setHeight(chart.getOffsetHeight() + 50);
                Canvas tempCanvas = Canvas.createIfSupported();
                tempCanvas.setWidth(String.valueOf(request.getWidth()));
                tempCanvas.setCoordinateSpaceWidth(request.getWidth());
                tempCanvas.setHeight(25 + "px");
                tempCanvas.setCoordinateSpaceHeight(25);

                Context2d ctx = tempCanvas.getContext2d();
                ValuePair<String, String> colors = SecurityBanner.getColors(text, text);
                ctx.setFillStyle(colors.getValue2());
                ctx.fillRect(0,0, request.getWidth(), 25);
                ctx.setFillStyle(colors.getValue1());
                ctx.setFont("normal 400 13px Arial");
                ctx.setTextAlign(Context2d.TextAlign.CENTER);
                while(ctx.measureText(text).getWidth() >= request.getWidth()) {
                    createNewFont(ctx, ctx.getFont(), text, request.getWidth());
                }
                ctx.fillText(text, request.getWidth()/2, 17 );
                String dataUrl = tempCanvas.toDataUrl("image/png");

                PNGImageComponent topBanner = new PNGImageComponent();
                topBanner.setData(dataUrl);
                topBanner.setX(0);
                topBanner.setY(0);
                PNGImageComponent bottomBanner = new PNGImageComponent();
                bottomBanner.setData(dataUrl);
                bottomBanner.setY(getOffsetHeight() + 25);
                bottomBanner.setX(0);
                request.addComponent(topBanner);
                request.addComponent(bottomBanner);
            }

            SVGImageComponent chartSVG = new SVGImageComponent();
            chartSVG.setData(chart.getElement().getFirstChildElement().getInnerHTML());
            chartSVG.setX(0);
            if(enableSecurity) {
                chartSVG.setY(25);
            } else {
                chartSVG.setY(0);
            }
            chartSVG.setWidth(chart.getOffsetWidth());
            chartSVG.setHeight(chart.getOffsetHeight());

            request.addComponent(chartSVG);

        }
        return request;
    }

    private void createNewFont(Context2d ctx, String font, String text, int width) {
        String[] fontValues = font.split(" ");
        String fontSize = fontValues[2];
        String fontSizeValues[] = fontSize.split("p");
        int fontSizeInt = Integer.parseInt(fontSizeValues[0]);
        String newFont = fontValues[0] + " " + fontValues[1] + " " + fontSizeInt + "px"
                + " " + fontSizeValues[3];
        ctx.setFont(newFont);
        if(ctx.measureText(text).getWidth() >= width){
            createNewFont(ctx, ctx.getFont(), text, width);
        }
    }

    public void broadcastNotify(String text) {
        BroadcastAlert broadcastAlert = new BroadcastAlert(text);
        mainLayoutPanel.add(broadcastAlert);
    }

    void notifyRenderBarChartDeferred() {
        renderBarChartTabDeferred = true;
    }

    @Override
    public void onResize() {
        if (!this.isAttached()) {
            return;
        }

        int newWidth = getOffsetWidth();
        int newHeight = getOffsetHeight();

        if (newWidth == 0 && newHeight == 0) {
            return;
        }

        super.onResize();
        if (this.width == newWidth && this.height == newHeight) {
            selectDefaultView();
            return;
        }

        this.width = newWidth;
        this.height = newHeight;

        drillBreadcrumb.refresh();
        resizeOverview();


        tabPanel.hideLinks();
        if (response != null) {
            render(response, false);
        }

        tabPanel.onResize();
        barChartTab.onResize();
        if (chartPresenter.getMetrics() != null) {
            chartPresenter.getMetrics().positionLegend();
        }

    }

//    public void refresh(boolean render) {
//        drillBreadcrumb.refresh();
//        resizeOverview();
//        if (render) {
//            render(response, false);
//        }
//    }
//
//    public void refresh() {
//        drillBreadcrumb.refresh();
//        resizeOverview();
//        render(response, false);
//    }

    public HighchartResponse getResponse() {
        return response;
    }

    public void setResponse(HighchartPagingResponse currentResponse) {
        this.response = currentResponse;
    }

    private void resizeOverview() {
        int width = getOffsetWidth();
        chartPresenter.resizeOverview(width);
    }

    interface SpecificUiBinder extends UiBinder<Widget, ChartView> {
    }


}