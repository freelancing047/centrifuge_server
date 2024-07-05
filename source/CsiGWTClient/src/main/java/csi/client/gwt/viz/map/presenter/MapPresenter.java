package csi.client.gwt.viz.map.presenter;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Container;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Image;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.dataview.directed.DirectedPresenter;
import csi.client.gwt.dataview.directed.SelectView;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.map.*;
import csi.client.gwt.viz.map.legend.MapLegend;
import csi.client.gwt.viz.map.legend.MapLegendImpl;
import csi.client.gwt.viz.map.legend.MouseEventResponder;
import csi.client.gwt.viz.map.menu.MapMenuManager;
import csi.client.gwt.viz.map.metrics.MapMetricsView;
import csi.client.gwt.viz.map.overview.OverviewPresenter;
import csi.client.gwt.viz.map.view.MapView;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.viz.shared.filter.FilterCapableVisualizationPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.viz.shared.menu.SelectionOnlyOnServer;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.map.MapToolsInfo;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.ExportActionsServiceProtocol;
import csi.server.common.service.api.MapActionsServiceProtocol;
import csi.server.common.service.api.ThemeActionsServiceProtocol;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.visualization.map.OverviewRequest;
import csi.shared.core.visualization.map.OverviewResponse;

import java.util.List;

public class MapPresenter extends AbstractVisualizationPresenter<MapViewDef, MapView>
        implements MapLegendContainer, FilterCapableVisualizationPresenter, SelectionOnlyOnServer {
    private static final int PROGRESS_BAR_DELAY_ON_LOAD = 500;
    private static final int OVERVIEW_CONTENT_MARGIN = 80 + (2 * 11);
    private MapMetricsView metrics = null;
    private Integer gsize = null;
    private MapActivityManager activityManager;
    private ZoomHomeHandler zoomHomeHandler;
    private ZoomInHandler zoomInHandler;
    private ZoomOutHandler zoomOutHandler;
    private SelectModeHandler selectModeHandler;
    private SearchModeHandler searchModeHandler;
    private BackToBundleHandler backToBundleHandler;
    private HeatmapPanelHandler heatmapPanelHandler;
    private SelectionModeHandler selectionModeHandler;
    private PinMapHandler pinMapHandler;
    private MapLegend mapLegend;
    private MapViewDef mapViewDef;
    private MapView mapView;
    private boolean hideProgressRequested;
    private NullableRepeatingCommand command;
    private Alert progressIndicator;
    private MapTheme mapTheme;
    private boolean handleViewLoad2HandlerAdded = false;
    private boolean useSummary = false;
    // Exporting mask, will go up once we send the req to the server for the zip
    // of png
    private MaskDialog mask;
    private Container spinnerContainer;
    private com.github.gwtbootstrap.client.ui.Icon spinnerIcon;
    private boolean hideLoadingSpinnerRequested;
    private boolean legendEnabled;
    private boolean legendShown;
    private boolean multitypeDecoratorEnabled;
    private boolean multitypeDecoratorShown;
    private boolean linkupDecoratorEnabled;
    //	private final OverviewPresenter overviewPresenter;
    private OverviewPresenter overviewPresenter;
    private boolean mapNeedsLoad = true;
    private boolean isLoadTrack = false;
    private boolean isReload2 = false;
    private boolean isApplySelection = false;
    //    private void redrawOverview(int width, int widthOfTheOverviewContainer, int numberOfCategoriesInData, int individualBinCount) {
    private int previousWidth = -1;
    private boolean loading = false;
    private boolean overviewScrollingCreated = false;

    public MapPresenter(AbstractDataViewPresenter dvPresenterIn, MapViewDef visualizationDef) {
        super(dvPresenterIn, visualizationDef);
        mapViewDef = visualizationDef;
        mapLegend = MapLegendImpl.create(this);
        updateTheme();
        zoomHomeHandler = new ZoomHomeHandler(this);
        zoomInHandler = new ZoomInHandler(this);
        zoomOutHandler = new ZoomOutHandler(this);
        selectModeHandler = new SelectModeHandler(this);
        searchModeHandler = new SearchModeHandler(this);
        backToBundleHandler = new BackToBundleHandler(this);
        heatmapPanelHandler = new HeatmapPanelHandler(this);
        selectionModeHandler = new SelectionModeHandler(this, dvPresenterIn.getUuid(), visualizationDef.getUuid());
        pinMapHandler = new PinMapHandler(this, dvPresenterIn.getUuid(), visualizationDef.getUuid());
        MapActivityMapper activityMapper = new MapActivityMapper();
        ResettableEventBus eventBus = new ResettableEventBus(new SimpleEventBus());
        activityManager = new MapActivityManager(activityMapper, eventBus, this);

        if (isUseTrackMap()) {
            overviewPresenter = new OverviewPresenter(this);
        }
    }

    @Override
    public boolean hasSelection() {
        return true;
    }

    @Override
    public void saveViewStateToVisualizationDef() {
        // TODO Auto-generated method stub
    }

    @Override
    public ImagingRequest getImagingRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    public void handleLinkup() {
        WebMain.injector.getVortex().execute(MapActionsServiceProtocol.class).handleLinkup(dvPresenter.getUuid(), getVisualizationDef().getUuid());
        reload();
    }

    @Override
    public void loadVisualization() {
        if (!loading) {
            loading = true;
            VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
            try {
                if (isInActiveWorksheet()) {
                    showLoadingSpinner();
                }
                vortexFuture.execute(MapActionsServiceProtocol.class).forceLoad(getDataViewUuid(), getVisualizationDef().getUuid());
                vortexFuture.addEventHandler(new VortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loading = false;
                        //        setMapNeedsLoad(true);
                        if (isUseTrackMap()) {
                            if (overviewPresenter != null && overviewPresenter.isLoaded()) {
                                overviewPresenter.resetWithoutRender();
                            }
                            loadTrack();
                        } else {
                            loadVisualization2();
                        }
                        appendNotificationText(NotificationLabel.FILTER, getVisualizationDef().getFilter() != null);
                        appendBroadcastIcon();
                    }

                    @Override
                    public boolean onError(Throwable t) {
                        hideLoadingSpinner();
                        loading = false;
                        mapView.tileServiceBad();
                        return false;
                    }

                    @Override
                    public void onUpdate(int taskProgress, String taskMessage) {
                    }

                    @Override
                    public void onCancel() {
                        hideLoadingSpinner();
                        loading = false;
                    }
                });
            } catch (Exception myException) {
                hideLoadingSpinner();
                loading = false;
                mapView.tileServiceBad();
            }
        }
    }

    private void loadVisualization2() {
        hideLoadingSpinner();
        showProgressIndicator();
        if (mapLegend != null)
            mapLegend.clear();
        mapView.clearInfoWindows();
        updateTheme();
        WebMain.injector.getVortex().execute(MapActionsServiceProtocol.class).setLinkupDecoratorShown(getDataViewUuid(), getVisualizationDef().getUuid(), false);
        WebMain.injector.getVortex().execute(MapActionsServiceProtocol.class).doDeselectAll(dvPresenter.getUuid(), getVisualizationDef().getUuid());
        mapView.buildDisplay();
    }

    public boolean isUseHeatMap() {
        return mapViewDef.getMapSettings().isUseHeatMap();
    }

    public void ensureLegendIsDisplayed(String number) {
        Integer sequenceNumber = Integer.parseInt(number);
        if (legendEnabled && legendShown) {
            mapLegend.setSequenceNumber(sequenceNumber);
            mapLegend.load();
            Scheduler.get().scheduleDeferred(() -> mapLegend.showAndPositionLegend());
        }
    }

    @Override
    public void applySelection(Selection selection) {
        if (isViewLoaded()) {
            if (isInActiveWorksheet()) {
                mapView.applySelection();
            } else {
                setApplySelection();
            }
        }
    }

    @Override
    public void broadcastNotify(String text) {
        if (isViewLoaded()) {
            mapView.broadcastNotify(text);
            appendBroadcastIcon();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Visualization> AbstractMenuManager<V> createMenuManager() {
        return (AbstractMenuManager<V>) new MapMenuManager(this);
    }

    @Override
    public MapView createView() {
        mapView = new MapView(this);
        mapView.setVisible(true);
        mapView.setHeight("100%");
        mapView.setWidth("100%");
        return mapView;
    }

    @Override
    public void reload() {
        if (isViewLoaded()) {
            if (!loading) {
                loading = true;
                VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
                try {
                    if (isInActiveWorksheet()) {
                        showLoadingSpinner();
                    }
                    vortexFuture.execute(MapActionsServiceProtocol.class).forceLoad(getDataViewUuid(), getVisualizationDef().getUuid());
                    vortexFuture.addEventHandler(new VortexEventHandler<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            hideLoadingSpinner();
                            loading = false;
                            if (isUseTrackMap()) {
                                loadTrack2();
                            } else {
                                loadMap();
                            }
                        }

                        @Override
                        public boolean onError(Throwable t) {
                            hideLoadingSpinner();
                            loading = false;
                            mapView.tileServiceBad();
                            return false;
                        }

                        @Override
                        public void onUpdate(int taskProgress, String taskMessage) {
                        }

                        @Override
                        public void onCancel() {
                            hideLoadingSpinner();
                            loading = false;
                        }
                    });
                } catch (Exception myException) {
                    hideLoadingSpinner();
                    loading = false;
                    mapView.tileServiceBad();
                }
            }
        }
    }

    private void loadTrack2() {
        if (isInActiveWorksheet()) {
            loadTrack();
        } else {
            setLoadTrack();
        }
    }

    private void loadMap() {
        if (isInActiveWorksheet()) {
            reload2();
        } else {
            setReload2();
        }
    }

    public boolean isMapNeedsLoad() {
        return mapNeedsLoad;
    }

    private void unsetMapNeedsLoad() {
        mapNeedsLoad = false;
        isLoadTrack = false;
        isReload2 = false;
        isApplySelection = false;
    }

    public boolean isLoadTrack() {
        return isLoadTrack;
    }

    private void setLoadTrack() {
        mapNeedsLoad = true;
        isLoadTrack = true;
        isReload2 = false;
        isApplySelection = false;
    }

    public boolean isReload2() {
        return isReload2;
    }

    private void setReload2() {
        mapNeedsLoad = true;
        isLoadTrack = false;
        isReload2 = true;
        isApplySelection = false;
    }

    public boolean isApplySelection() {
        return isApplySelection;
    }

    private void setApplySelection() {
        mapNeedsLoad = true;
        if (!isLoadTrack && !isReload2) {
            isApplySelection = true;
        }
    }

    private boolean isInActiveWorksheet() {
        if (this.dvPresenter instanceof DataViewPresenter) {
            DataViewPresenter dvPresenter = (DataViewPresenter) this.dvPresenter;
            WorksheetPresenter activeWorksheet = dvPresenter.getActiveWorksheet();
            for (Visualization vizDef : activeWorksheet.getVisualizations()) {
                if (vizDef.getUuid().equals(mapViewDef.getUuid())) {
                    return true;
                }
            }
            return false;
        } else if (dvPresenter instanceof DirectedPresenter) {
            DirectedPresenter var = (DirectedPresenter) dvPresenter;
            if (var.getView() instanceof SelectView) {
                SelectView view = (SelectView) var.getView();
                String uuid = view.getVizualization().getUuid();
                String uuid1 = mapViewDef.getUuid();
                return uuid.equals(uuid1);
            } else {
                return true;

            }
        }
        return true;
    }

    public void loadTrack() {
        overviewScrollingCreated = false;
        OverviewRequest request = new OverviewRequest();
        request.setDvUuid(getDataViewUuid());
        request.setVizUuid(getUuid());
        request.setCurrentWidth(getChrome().getMainLP().getOffsetWidth() - OVERVIEW_CONTENT_MARGIN);
        VortexFuture<OverviewResponse> vortexFuture = WebMain.injector.getVortex().createFuture();
        VortexEventHandler<OverviewResponse> handler = new AbstractVortexEventHandler<OverviewResponse>() {
            @Override
            public boolean onError(Throwable t) {
                hideLoadingSpinner();
                String msg = t.getMessage();
                if ("PlaceTypeLimitReached".equals(msg)) {
                    placeTypeLimitReached();
                } else {
                    trackTypeLimitReached();
                }
                return true;
            }

            public void onSuccess(OverviewResponse response) {
                hideLoadingSpinner();
                if (response != null) {
                    createOverviewScrolling(response);
                }
                reload2();
            }
        };
        vortexFuture.addEventHandler(handler);
        try {
            vortexFuture.execute(MapActionsServiceProtocol.class).getOverview(request);
        } catch (Exception myException) {
            hideLoadingSpinner();
            Dialog.showException(myException);
        }
    }

    public void reload2() {
        if (mapLegend != null)
            mapLegend.clear();
        mapView.clearInfoWindows();
        if (isInActiveWorksheet()) {
            mapView.reload();
        } else {
            mapView.onResize();
        }
        MapView.expireMetrics(getUuid());
    }

    private void createProgressIndicator() {
        ProgressBar progressBar;
        if (progressIndicator == null) {
            hideLoadingSpinner();
            progressIndicator = new Alert(_constants.progressBar_loading(), AlertType.INFO);
            getChrome().getMainLP().add(progressIndicator);
            progressBar = new ProgressBar(ProgressBarBase.Style.ANIMATED);
            progressIndicator.setClose(false);
            progressBar.setPercent(100);
            progressIndicator.add(progressBar);
            // FIXME: set using style name
            progressIndicator.setHeight("50px");// NON-NLS

            // Checking if this changed during creation
            if (hideProgressRequested)
                hideProgressIndicator();
        }
    }

    private boolean isHideProgressRequested() {
        return hideProgressRequested;
    }

    public void hideProgressIndicator() {
        // MapMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
        hideProgressRequested = true;
        if (progressIndicator != null && progressIndicator.isAttached()) {
            getChrome().getMainLP().remove(progressIndicator);
            progressIndicator.close();
            progressIndicator = null;
        }
    }

    public void showProgressIndicator() {
        hideProgressRequested = false;

        if (command != null) {
            // invalidates the command
            command.setRepeat(false);
            command = null;
        }
        // track previous command
        command = new NullableRepeatingCommand(this, (MapPresenter presenter, boolean isRepeat) -> {
            if (!presenter.isHideProgressRequested() && isRepeat)
                presenter.createProgressIndicator();
            return false;
        });

        Scheduler.get().scheduleFixedPeriod(command, PROGRESS_BAR_DELAY_ON_LOAD);
    }

    public void showLoadingSpinner() {
        hideLoadingSpinnerRequested = false;

        if (command != null) {
            // invalidates the command
            command.setRepeat(false);
            command = null;
        }
        // track previous command
        command = new NullableRepeatingCommand(this, (MapPresenter presenter, boolean isRepeat) -> {
            if (!presenter.isHideLoadingSpinnerRequested() && isRepeat) {
                presenter.createLoadingSpinner();
            }
            return false;
        });

        Scheduler.get().scheduleFixedPeriod(command, 50);
    }

    private void createLoadingSpinner() {
        if (spinnerIcon == null) {
            hideProgressIndicator();
            spinnerIcon = new com.github.gwtbootstrap.client.ui.Icon(IconType.SPINNER);
            spinnerIcon.setVisible(true);
            spinnerIcon.setIconSize(IconSize.FOUR_TIMES);
            spinnerIcon.setSpin(true);
            spinnerIcon.addStyleName("csi-icon-spinner"); //$NON-NLS-1$
            positionSpinner();

            // Checking if this changed during creation
            if (hideLoadingSpinnerRequested)
                hideLoadingSpinner();
        }
    }

    private void positionSpinner() {
        spinnerContainer = new Container();
        spinnerContainer.add(spinnerIcon);
        spinnerContainer.setHeight("50px");
        spinnerContainer.setWidth("50px");
        spinnerContainer.getElement().getStyle().setBackgroundColor("#fffff");
        spinnerContainer.getElement().getStyle().setMarginTop(20, Unit.PCT);
        getChrome().getMainLP().add(spinnerContainer);
    }

    private boolean isHideLoadingSpinnerRequested() {
        return hideLoadingSpinnerRequested;
    }

    public void hideLoadingSpinner() {
        hideLoadingSpinnerRequested = true;
        if (spinnerIcon != null && spinnerIcon.isAttached()) {
            spinnerIcon.setVisible(false);
            getChrome().getMainLP().remove(spinnerContainer);
            spinnerContainer.remove(spinnerIcon);
            spinnerIcon = null;
            spinnerContainer = null;
        }
    }

    public void showNoData() {
        if (isViewLoaded()) {
            mapView.showNoData();
            hideButtonGroup();
            disableLegend();
            unsetMapNeedsLoad();
        }
    }

    public void showData() {
        mapView.showData();
        showButtonGroup();
        adjustButtons();
        unsetMapNeedsLoad();
    }

    public void gatherMapToolsInfo() {
        Scheduler.get().scheduleEntry(() -> {
            if (loading) {
                return true;
            } else {
                WebMain.injector.getVortex().execute((MapToolsInfo mapToolsInfo) -> {
                    if (mapToolsInfo != null) {
                        legendEnabled = mapToolsInfo.isLegendEnabled();
                        legendShown = mapToolsInfo.isLegendShown();
                        handleLegendShowHideOrDisabled();
                        multitypeDecoratorEnabled = mapToolsInfo.isMultitypeDecoratorEnabled();
                        multitypeDecoratorShown = mapToolsInfo.isMultitypeDecoratorShown();
                        if (multitypeDecoratorEnabled)
                            if (multitypeDecoratorShown)
                                showMultiTypeDecorator();
                            else
                                hideMultiTypeDecorator();
                        else
                            disableMultiTypeDecorator();
                        linkupDecoratorEnabled = mapToolsInfo.isLinkupDecoratorEnabled();
                        if (linkupDecoratorEnabled)
                            enableLinkupDecorator();
                        else
                            disableLinkupDecorator();
                        mapView.showData();
                    } else {
                        disableLegend();
                        disableMultiTypeDecorator();
                        disableLinkupDecorator();
                    }
                }, MapActionsServiceProtocol.class).getMapToolsInfo(getDataView().getUuid(), getVisualizationDef().getUuid());
                return false;
            }
        });
    }

    public void pointLimitReached() {
        if (isViewLoaded()) {
            mapView.showPointLimitReached();
            hideButtonGroup();
            disableLegend();
            unsetMapNeedsLoad();
        }
    }

    public void placeTypeLimitReached() {
        if (isViewLoaded()) {
            mapView.showPlaceTypeLimitReached();
            hideButtonGroup();
            disableLegend();
            unsetMapNeedsLoad();
        }
    }

    public void trackTypeLimitReached() {
        if (isViewLoaded()) {
            mapView.showTrackTypeLimitReached();
            hideButtonGroup();
            disableLegend();
            unsetMapNeedsLoad();
        }
    }

    public void tooManyPoints(String number) {
        if (isViewLoaded()) {
            mapView.tooManyPoints(number);
            hideButtonGroup();
            disableLegend();
            unsetMapNeedsLoad();
        }
    }

    public void testTileService() {
        mapView.tileServiceGood();
        if (overviewPresenter != null) {
            mapView.addOverview(overviewPresenter.getOverviewView());
        }
    }

    public void tileServiceBad() {
        mapView.tileServiceBad();
    }

    public boolean isUseBundleMap() {
        MapSettings mapSettings = getVisualizationDef().getMapSettings();
        return mapSettings.isBundleUsed();
    }

    public void deselectAll() {
        if (isViewLoaded())
            mapView.deselectAll();
    }

    public void selectAll() {
        if (isViewLoaded())
            mapView.selectAll();
    }

    public void combinedPlaceClicked(String operation) {
        if (isViewLoaded())
            mapView.combinedPlaceClicked(operation);
    }

    public void newPlaceClicked(String operation) {
        if (isViewLoaded())
            mapView.newPlaceClicked(operation);
    }

    public void updatedPlaceClicked(String operation) {
        if (isViewLoaded())
            mapView.updatedPlaceClicked(operation);
    }

    public void associationClicked(String associationKey, String operation) {
        if (isViewLoaded())
            mapView.associationClicked(associationKey, operation);
    }

    public void trackClicked(int trackId, String trackName, String operation) {
        if (isViewLoaded())
            mapView.trackClicked(trackId, trackName, operation);
    }

    public void placeClicked(int placeId, String typename, String operation) {
        if (isViewLoaded())
            mapView.placeClicked(placeId, typename, operation);
    }

    public void f1() {
        if (mapViewDef.isSuppressLoadAtStartup()) {
            if (isViewLoaded()) {
                loadVisualizationAndCreateFilterLabel();
            }
        } else {
            handleViewLoadOrLoadVisualization();
        }
        getChrome().setName(getName());
    }

    @Override
    public MapLegend getMapLegend() {
        return mapLegend;
    }

    public void disableLegend() {
        mapLegend.hide();
        getMenuManager().disable(MenuKey.HIDE_LEGEND);
        getMenuManager().disable(MenuKey.SHOW_LEGEND);
        getMenuManager().disable(MenuKey.RESET_LEGEND);
        getMenuManager().hide(MenuKey.HIDE_LEGEND);
        getMenuManager().hide(MenuKey.SHOW_LEGEND);
        getMenuManager().hide(MenuKey.RESET_LEGEND);
    }

    public void sensitizeFloatingObjects() {
        if (mapLegend != null)
            ((MouseEventResponder) mapLegend).sensitize();
        if (metrics != null)
            ((MouseEventResponder) metrics).sensitize();
        ((MouseEventResponder) activityManager).sensitize();
    }

    public void desensitizeFloatingObjects() {
        if (mapLegend != null)
            ((MouseEventResponder) mapLegend).desensitize();
        if (metrics != null)
            ((MouseEventResponder) metrics).desensitize();
        ((MouseEventResponder) activityManager).desensitize();
    }

    private void disableMultiTypeDecorator() {
        getMenuManager().disable(MenuKey.HIDE_MULTITYPE_DECORATOR);
        getMenuManager().disable(MenuKey.SHOW_MULTITYPE_DECORATOR);
        getMenuManager().hide(MenuKey.HIDE_MULTITYPE_DECORATOR);
        getMenuManager().hide(MenuKey.SHOW_MULTITYPE_DECORATOR);
    }

    private void enableLinkupDecorator() {
        getMenuManager().enable(MenuKey.SHOW_LINKUP_HIGLIGHTS);
        getMenuManager().enable(MenuKey.CLEAR_MERGE_HIGHLIGHTS);
    }

    private void disableLinkupDecorator() {
        getMenuManager().disable(MenuKey.SHOW_LINKUP_HIGLIGHTS);
        getMenuManager().disable(MenuKey.CLEAR_MERGE_HIGHLIGHTS);
        getMenuManager().hide(MenuKey.SHOW_LINKUP_HIGLIGHTS);
        getMenuManager().hide(MenuKey.CLEAR_MERGE_HIGHLIGHTS);
    }

    @Override
    public void hideLegend() {
        mapLegend.hide();
        getMenuManager().hide(MenuKey.HIDE_LEGEND);
        getMenuManager().enable(MenuKey.SHOW_LEGEND);
        getMenuManager().enable(MenuKey.RESET_LEGEND);
    }

    @Override
    public void showLegend() {
        mapLegend.showAndPositionLegend();
        getMenuManager().hide(MenuKey.SHOW_LEGEND);
        getMenuManager().enable(MenuKey.HIDE_LEGEND);
        getMenuManager().enable(MenuKey.RESET_LEGEND);
    }

    private void hideMultiTypeDecorator() {
        getMenuManager().hide(MenuKey.HIDE_MULTITYPE_DECORATOR);
        getMenuManager().enable(MenuKey.SHOW_MULTITYPE_DECORATOR);
        mapLegend.updateCombinedPlaceIconStatus(false);
    }

    private void showMultiTypeDecorator() {
        getMenuManager().hide(MenuKey.SHOW_MULTITYPE_DECORATOR);
        getMenuManager().enable(MenuKey.HIDE_MULTITYPE_DECORATOR);
        mapLegend.updateCombinedPlaceIconStatus(true);
    }

    private void adjustButtons() {
        Scheduler.get().scheduleDeferred(() -> chrome.adjustWidgets());
    }

    @Override
    protected void handleViewLoad2() {
        if (viewLoaded && chrome != null) {
            if (!handleViewLoad2HandlerAdded) {
                activityManager.setDisplay(chrome);
                handleViewLoad2HandlerAdded = true;
            }

            hideLegend();
            gatherMapToolsInfo();
        }
    }

    private void showButtonGroup() {
        chrome.showButtonGroupContainer();
    }

    public void hideButtonGroup() {
        chrome.hideButtonGroupContainer();
    }

    public void applySelection() {
        mapView.applySelection();
    }

    public void legendUpdated() {
        mapView.legendUpdated();
    }

    @Override
    public void setBroadcastListener(boolean listener) {
        getVisualizationDef().setBroadcastListener(listener);
        saveSettingsOnly(false);
    }

    private MapTheme getMapTheme() {
        return mapTheme;
    }

    private void setMapTheme(MapTheme mapTheme) {
        this.mapTheme = mapTheme;
    }

    public void getImage(String iconId, ShapeType shape, int color, int size, double iconRatio, int strokeSize, final Image image) {
        try {
            WebMain.injector.getVortex().execute((Callback<String>) image::setUrl, MapActionsServiceProtocol.class).getNodeAsImageNew(iconId, true, shape, color, size, iconRatio, strokeSize, useSummary, mapViewDef.getUuid());
        } catch (CentrifugeException ignored) {
        }
    }

    public PlaceStyle findPlaceStyle(String typeName) {
        if (mapTheme != null && mapTheme.getPlaceStyles() != null) {
            for (PlaceStyle style : mapTheme.getPlaceStyles()) {
                if (style.getFieldNames() != null && style.getFieldNames().contains(typeName)) {
                    return style;
                }
            }
        }
        return null;
    }

    public void updateTheme() {
        if (getMapTheme() == null && getVisualizationDef().getMapSettings() != null
                && getVisualizationDef().getMapSettings().getThemeUuid() != null) {
            String themeUuid = getVisualizationDef().getMapSettings().getThemeUuid();
            WebMain.injector.getVortex().execute(this::setMapTheme, ThemeActionsServiceProtocol.class).findMapTheme(themeUuid);
        }
    }

    public VortexFuture<Void> saveSettingsOnly(final boolean refreshOnSuccess) {
        return saveSettings(refreshOnSuccess, false, false);
    }

    public void processResize() {
        mapView.processResize();
    }

    public ZoomHomeHandler getZoomHomeHandler() {
        return zoomHomeHandler;
    }

    public ZoomInHandler getZoomInHandler() {
        return zoomInHandler;
    }

    public ZoomOutHandler getZoomOutHandler() {
        return zoomOutHandler;
    }

    public SelectModeHandler getSelectModeHandler() {
        return selectModeHandler;
    }

    public SearchModeHandler getSearchModeHandler() {
        return searchModeHandler;
    }

    public BackToBundleHandler getBackToBundleHandler() {
        return backToBundleHandler;
    }

    public HeatmapPanelHandler getHeatMapPanelHandler() {
        return heatmapPanelHandler;
    }

    public PinMapHandler getPinMapHandler() {
        return pinMapHandler;
    }

    public MapLegend getMapLegendImpl() {
        return mapLegend;
    }

    public SelectionModeHandler getSelectionModeHandler() {
        return selectionModeHandler;
    }

    public void hideBackButton() {
        backToBundleHandler.hideButton();
    }

    public void showBackButton() {
        backToBundleHandler.showButton();
    }

    public void hideHeatMapPanelButton() {
        heatmapPanelHandler.hideButton();
    }

    public void showHeatMapPanelButton() {
        heatmapPanelHandler.showButton();
    }

    public void handleBreadcrumb(String breadcrumbAggregate, String showLeaves, String drilledToBottom) {
        boolean isShowLeaves = showLeaves.equals("true");
        boolean isDrilledToBottom = drilledToBottom.equals("true");
        mapView.setupBreadcrumb(breadcrumbAggregate, isShowLeaves);
        if (isShowLeaves || isDrilledToBottom) {
            WebMain.injector.getVortex().execute((Boolean isLegendShown) -> {
                if (isLegendShown != null) {
                    legendEnabled = true;
                    legendShown = isLegendShown;
                    handleLegendShowHideOrDisabled();
                } else {
                    disableLegend();
                    disableMultiTypeDecorator();
                    disableLinkupDecorator();
                }
            }, MapActionsServiceProtocol.class).isLegendShown(getDataView().getUuid(), getVisualizationDef().getUuid());
        } else {
            legendEnabled = false;
            handleLegendShowHideOrDisabled();
        }
    }

    private void handleLegendShowHideOrDisabled() {
        if (legendEnabled)
            if (legendShown)
                showLegend();
            else
                hideLegend();
        else
            disableLegend();
    }

    /**
     * calls iframe to kick off the export process. The iframe will call the
     * downloadExport when its done.
     */
    public void getMapPNGExport() {
        mapView.getMapAsBase64();
    }

    public String getExportName() {
        return getDataView().getName() + "_" + getName();
    }

    private void showExportMask() {
        mask = new MaskDialog("Exporting " + getExportName());
        mask.setTitle(CentrifugeConstantsLocator.get().exportInProgress());
        mask.show();
    }

    private void hideExportMask() {
        if (mask != null)
            mask.hide();
    }

    /**
     * Iframe will call this method.
     * <p>
     * This calls the exportZipPNG with a list n imaging requests.
     */
    public void downloadExport(List<ImagingRequest> req) {
        showExportMask();

        VortexFuture<String> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<String>() {
                @Override
                public void onSuccess(String result) {
                    hideExportMask();
                    DownloadHelper.download(getExportName(), ExportType.ZIP.getFileSuffix(), result);
                }

                @Override
                public boolean onError(Throwable t) {
                    hideExportMask();
                    return false;

                }
            });

            vortexFuture.execute(ExportActionsServiceProtocol.class).exportZipPNG(req);
        } catch (Exception myException) {
            hideExportMask();
        }
    }

    public boolean isUseSummary() {
        return useSummary;
    }

    public void setUseSummary(boolean useSummary) {
        this.useSummary = useSummary;
    }

    public void hideMenu() {
        chrome.getMenu().hideDropdown();
    }

    public MapMetricsView getMetrics() {
        return metrics;
    }

    public void showMetrics() {
        if (this.isViewLoaded() && progressIndicator == null) {
            if (metrics == null)
                metrics = new MapMetricsView(this);

            Scheduler.get().scheduleDeferred(() -> metrics.show());
        }
    }

    public boolean isUseTrackMap() {
        MapSettings mapSettings = getVisualizationDef().getMapSettings();
        return mapSettings.isUseTrack() && mapSettings.getMapTracks().size() > 0;
    }

    private void createOverviewScrolling(OverviewResponse response) {
        if (!overviewScrollingCreated) {
            int count = response.getTotalCategories();
            if (overviewPresenter == null) {
                overviewPresenter = new OverviewPresenter(this);
            }

            if (!overviewPresenter.isLoaded()) {
                initOverviewPresenter();
            }

            setNewOverviewValues(response);
            int endIndex = count - 1;
//        Range range = new Range(0, endIndex);

            boolean rangeSet = false;

            // TODO: JD-FIX gotta validate that the old range is valid instead of just using it
            // if (sizes.size() >= drillLocation && sizes.get(drillLocation) ==
            // data.size() && ranges.get(drillLocation) != null) {
            if (gsize != null && gsize == response.getTotalCategories()) {
                // This double checks and defaults to count if we screwed up
                if (endIndex >= count) {
                    endIndex = count - 1;
                }

                if (overviewPresenter.isRangeEmpty()) {
                    overviewPresenter.initViewPortMax(0, endIndex, true);
                } else {
                    overviewPresenter.initViewPortMax(0, response.getTotalCategories() - 1, true);
                }

                overviewPresenter.setScrollRange(0, endIndex, true);

                rangeSet = true;
            } else {

                gsize = -1;
            }

            if (!rangeSet) {
                overviewPresenter.setScrollRange(0, endIndex, false);
            }

            Scheduler.get().scheduleDeferred(() -> resizeOverview(getView().getOffsetWidth()));
//            overviewPresenter.doUpdateRange(overviewPresenter.range);
            overviewScrollingCreated = true;
        }
    }

    public boolean isOverviewScrollingCreated() {
        return overviewScrollingCreated;
    }

    private void initOverviewPresenter() {
        overviewPresenter.getOverviewView().setOverviewContent(null);
        overviewPresenter.build(getView().getOffsetWidth());
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
            overviewPresenter.setRange(newStartPosition, newEndPosition);
        }
    }

    public OverviewPresenter getOverview() {
        return overviewPresenter;
    }

    public void resizeOverview(int width) {
        int widthOfTheOverviewContainer = width - OVERVIEW_CONTENT_MARGIN;
        if (widthOfTheOverviewContainer <= 0) {
            return;
        }
//        int numberOfCategoriesInData = overviewPresenter.getNumberOfCategories();
//        int individualBinCount = overviewPresenter.getIndividualBinCount();
//        redrawOverview(width, widthOfTheOverviewContainer, numberOfCategoriesInData, individualBinCount);
        redrawOverview(width);
    }

    private void redrawOverview(int width) {
        if (width == previousWidth) {
            return;
        }
        //getOverview();
        if (overviewPresenter.resizeWidth(width - OVERVIEW_CONTENT_MARGIN, false)) {
            previousWidth = width;
        }
    }


    public void mapRenderComplete() {
        overviewPresenter.setDebounceRangeUpdate(false);
    }

    public void setLegendShown(boolean value) {
        legendShown = value;
        WebMain.injector.getVortex().execute(MapActionsServiceProtocol.class).setLegendShown(dvPresenter.getUuid(), getVisualizationDef().getUuid(), value);
    }

    public boolean isMapPinned() {
        return getPinMapHandler().isMapPinned();
    }

    public void setMapPinned(boolean value) {
        WebMain.injector.getVortex().execute(MapActionsServiceProtocol.class).setMapPinned(dvPresenter.getUuid(), getVisualizationDef().getUuid(), value);
    }

    public int getSelectionMode() { return getSelectionModeHandler().getSelectionMode();}

    public void setSelectionMode(int value) {
        WebMain.injector.getVortex().execute(MapActionsServiceProtocol.class).setSelectionMode(dvPresenter.getUuid(), getVisualizationDef().getUuid(), value);
    }

    public int[] getLegendPosition() {
        return mapLegend.getLegendPosition();
    }
}
