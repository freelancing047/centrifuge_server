package csi.client.gwt.viz.map.view;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Frame;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.directed.visualization.DirectedWindow;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.viz.map.metrics.MapMetricsView;
import csi.client.gwt.viz.map.overview.view.OverviewView;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.map.settings.MapConfigProxy;
import csi.client.gwt.viz.matrix.ExpireMetrics;
import csi.client.gwt.viz.matrix.HideMetricsEvent;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.viz.shared.BroadcastAlert;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.chrome.panel.VizPanelFrameProvider;
import csi.client.gwt.viz.shared.export.LegendExporter;
import csi.client.gwt.widget.InfoPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.worksheet.layout.window.VisualizationWindow;
import csi.client.gwt.worksheet.layout.window.WindowBase;
import csi.server.common.util.ValuePair;
import csi.shared.core.imaging.ImageComponent;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.imaging.PNGImageComponent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MapView extends ResizeComposite {
    private static MapViewUiBinder uiBinder = GWT.create(MapViewUiBinder.class);
    private static Map<String, MapPresenter> mapPresenterCache;

    static {
        mapPresenterCache = new TreeMap<>();
        registerMethod();
    }

    @UiField
    LayoutPanel layoutPanel;
    private Element breadcrumbDiv;
    private HorizontalPanel overviewPanel;
    private InfoPanel infoWindow;
    private Element iframe;
    private MapPresenter presenter;
    private boolean needResize = false;
    private String mapWidth = null;
    private String mapHeight = null;
    private boolean isInfoWindowOn = false;
    private long rangeChangeTest = 0;

    public MapView(MapPresenter presenter) {
        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }

    private static native void registerMethod() /*-{
        $wnd.mapViewShow = $entry(@csi.client.gwt.viz.map.view.MapView::show(Ljava/lang/String;));
        $wnd.mapViewShowLoading = $entry(@csi.client.gwt.viz.map.view.MapView::showLoading(Ljava/lang/String;));
        $wnd.mapViewHideLoading = $entry(@csi.client.gwt.viz.map.view.MapView::hideLoading(Ljava/lang/String;));
        $wnd.noData = $entry(@csi.client.gwt.viz.map.view.MapView::noData(Ljava/lang/String;));
        $wnd.hasData = $entry(@csi.client.gwt.viz.map.view.MapView::hasData(Ljava/lang/String;));
        $wnd.mapRenderComplete = $entry(@csi.client.gwt.viz.map.view.MapView::mapRenderComplete(Ljava/lang/String;));
        $wnd.pointLimitReached = $entry(@csi.client.gwt.viz.map.view.MapView::pointLimitReached(Ljava/lang/String;));
        $wnd.placeTypeLimitReached = $entry(@csi.client.gwt.viz.map.view.MapView::placeTypeLimitReached(Ljava/lang/String;));
        $wnd.trackTypeLimitReached = $entry(@csi.client.gwt.viz.map.view.MapView::trackTypeLimitReached(Ljava/lang/String;));
        $wnd.tileServiceBad = $entry(@csi.client.gwt.viz.map.view.MapView::tileServiceBad(Ljava/lang/String;));
        $wnd.loadLegend = $entry(@csi.client.gwt.viz.map.view.MapView::loadLegend(Ljava/lang/String;Ljava/lang/String;));
        $wnd.processResize = $entry(@csi.client.gwt.viz.map.view.MapView::processResize(Ljava/lang/String;));
        $wnd.exportPNG = $entry(@csi.client.gwt.viz.map.view.MapView::exportPNG(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
        $wnd.showBackButton = $entry(@csi.client.gwt.viz.map.view.MapView::showBackButton(Ljava/lang/String;));
        $wnd.hideBackButton = $entry(@csi.client.gwt.viz.map.view.MapView::hideBackButton(Ljava/lang/String;));
        $wnd.handleBreadcrumb = $entry(@csi.client.gwt.viz.map.view.MapView::handleBreadcrumb(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
        $wnd.tooManyPoints = $entry(@csi.client.gwt.viz.map.view.MapView::tooManyPoints(Ljava/lang/String;Ljava/lang/String;));
        $wnd.showSpinner = $entry(@csi.client.gwt.viz.map.view.MapView::showLoadingSpinner(Ljava/lang/String;));
        $wnd.hideSpinner = $entry(@csi.client.gwt.viz.map.view.MapView::hideLoadingSpinner(Ljava/lang/String;));
        $wnd.setUseSummary = $entry(@csi.client.gwt.viz.map.view.MapView::setUseSummary(Ljava/lang/String;Ljava/lang/Boolean;));
        $wnd.hideMenu = $entry(@csi.client.gwt.viz.map.view.MapView::hideMenu(Ljava/lang/String;));
        $wnd.refreshMetrics = $entry(@csi.client.gwt.viz.map.view.MapView::refreshMetrics(Ljava/lang/String;));
        $wnd.expireMetrics = $entry(@csi.client.gwt.viz.map.view.MapView::expireMetrics(Ljava/lang/String;));
        $wnd.refreshMetricsForSummary = $entry(@csi.client.gwt.viz.map.view.MapView::refreshMetricsForSummary(Ljava/lang/String;));
        $wnd.disableLegend = $entry(@csi.client.gwt.viz.map.view.MapView::disableLegend(Ljava/lang/String;));
        $wnd.sensitizeFloatingObjects = $entry(@csi.client.gwt.viz.map.view.MapView::sensitizeFloatingObjects(Ljava/lang/String;));
        $wnd.desensitizeFloatingObjects = $entry(@csi.client.gwt.viz.map.view.MapView::desensitizeFloatingObjects(Ljava/lang/String;));
        $wnd.isMapPinned = $entry(@csi.client.gwt.viz.map.view.MapView::isMapPinned(Ljava/lang/String;));
        $wnd.getSelectionMode = $entry(@csi.client.gwt.viz.map.view.MapView::getSelectionMode(Ljava/lang/String;));
        $wnd.getLegendPosition = $entry(@csi.client.gwt.viz.map.view.MapView::getLegendPosition(Ljava/lang/String;));
        $wnd.showHeader =  $entry(@csi.client.gwt.viz.map.view.MapView::showHeader(Ljava/lang/String;));
    }-*/;

    public static void showHeader(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        VizPanel vizPanel = (VizPanel) mapPresenter.getChrome();
        VizPanelFrameProvider frameProvider = vizPanel.getFrameProvider();
        vizPanel.handleMenuMouseDown(frameProvider);

        if (frameProvider instanceof VisualizationWindow) {
            ((VisualizationWindow) frameProvider).bringToFrontIfNotActive();
        } else if (frameProvider instanceof DirectedWindow) {
            ((DirectedWindow) frameProvider).bringToFrontIfNotActive();
        }
        vizPanel.showMenu();
    }


    public static void show(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        VizPanel vizPanel = (VizPanel) mapPresenter.getChrome();
        VizPanelFrameProvider frameProvider = vizPanel.getFrameProvider();
        WindowBase windowBase = (WindowBase) frameProvider;
        windowBase.bringToFrontIfNotActive();
    }

    public static void showLoading(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.showProgressIndicator();
    }

    public static void hideLoading(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.hideProgressIndicator();
    }

    public static void disableLegend(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.disableLegend();
    }

    public static void sensitizeFloatingObjects(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.sensitizeFloatingObjects();
    }

    public static void desensitizeFloatingObjects(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.desensitizeFloatingObjects();
    }

    public static boolean isMapPinned(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        return mapPresenter.isMapPinned();
    }

    public static int getSelectionMode(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        return mapPresenter.getSelectionMode();
    }

    public static int[] getLegendPosition(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        return mapPresenter.getLegendPosition();
    }

    public static void noData(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.showNoData();
    }

    public static void hasData(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.showData();
    }

    public static void mapRenderComplete(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.mapRenderComplete();
    }

    public static void pointLimitReached(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.pointLimitReached();
    }

    public static void placeTypeLimitReached(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.placeTypeLimitReached();
    }

    public static void trackTypeLimitReached(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.trackTypeLimitReached();
    }

    public static void tileServiceBad(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.tileServiceBad();
    }

    public static void tooManyPoints(String key, String number) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.tooManyPoints(number);
    }

    public static void loadLegend(String key, String number) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.ensureLegendIsDisplayed(number);
    }

    public static void showBackButton(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.showBackButton();
    }

    public static void hideBackButton(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.hideBackButton();
    }

    public static void handleBreadcrumb(String key, String breadcrumbAggregate, String showLeaves, String drilledToBottom) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.handleBreadcrumb(breadcrumbAggregate, showLeaves, drilledToBottom);
    }

    public static void processResize(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.processResize();
    }

    public static void showLoadingSpinner(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.showLoadingSpinner();
    }

    public static void hideLoadingSpinner(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.hideLoadingSpinner();
    }

    public static void setUseSummary(String key, Boolean value) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.setUseSummary(value);
    }

    public static void expireMetrics(String uuid) {
        MapMetricsView.EVENT_BUS.fireEvent(new ExpireMetrics(uuid));
    }

    public static void refreshMetrics(String uuid) {
        MapMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(uuid));
    }

    public static void refreshMetricsForSummary(String uuid) {
        MapMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(uuid, true));
    }

    public static void hideMenu(String key) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        mapPresenter.hideMenu();
    }

    private static native void callIframe(Element iframe, String message, String domain) /*-{
        if (iframe != null && iframe.contentWindow != null) {
            iframe.contentWindow.postMessage(message, domain);
        }
    }-*/;

    private static native boolean isContentWindowNull(Element iframe) /*-{
        return iframe.contentWindow == null;
    }-*/;

    /**
     * This will get called when the main png is done. We will then build the
     * legend png and ask the presenter to send it up to wrap it.
     *
     * @param key
     * @param base64
     * @param width
     * @param height
     */
    public static void exportPNG(String key, String base64, String width, String height) {
        MapPresenter mapPresenter = mapPresenterCache.get(key);
        String name = mapPresenter.getExportName();

        // ImagingRequest for the map
        ImagingRequest map = new ImagingRequest();
        ImageComponent comp = new PNGImageComponent();
        comp.setData(base64);
        comp.setX(0);
        map.setName(name + "_Map");
        map.setHeight(Integer.parseInt(height));
        map.setWidth(Integer.parseInt(width));

        Boolean enableSecurity = WebMain.injector.getMainPresenter().getUserInfo().getDoCapco();
        String text = SecurityBanner.getBannerText();
        if(text != null && enableSecurity) {
            map.setHeight(map.getHeight() + 50);
            com.google.gwt.canvas.client.Canvas tempCanvas = Canvas.createIfSupported();
            tempCanvas.setWidth(String.valueOf(map.getWidth()));
            tempCanvas.setCoordinateSpaceWidth(map.getWidth());
            tempCanvas.setHeight(25 + "px");
            tempCanvas.setCoordinateSpaceHeight(25);

            Context2d ctx = tempCanvas.getContext2d();
            ValuePair<String, String> colors = SecurityBanner.getColors(text, text);
            ctx.setFillStyle(colors.getValue2());
            ctx.fillRect(0,0, map.getWidth(), 25);
            ctx.setFillStyle(colors.getValue1());
            ctx.setFont("normal 400 13px Arial");
            ctx.setTextAlign(Context2d.TextAlign.CENTER);
            while(ctx.measureText(text).getWidth() >= map.getWidth()) {
                createNewFont(ctx, ctx.getFont(), text, map.getWidth());
            }
            ctx.fillText(text, map.getWidth()/2, 17 );
            String dataUrl = tempCanvas.toDataUrl("image/png");
            PNGImageComponent topBanner = new PNGImageComponent();
            topBanner.setData(dataUrl);
            topBanner.setX(0);
            topBanner.setY(0);
            PNGImageComponent bottomBanner = new PNGImageComponent();
            bottomBanner.setData(dataUrl);
            bottomBanner.setY(mapPresenter.getView().getOffsetHeight() + 25);
            bottomBanner.setX(0);
            map.addComponent(topBanner);
            map.addComponent(bottomBanner);
            comp.setY(25);
        }
        map.addComponent(comp);

        // ImagingRequest for the Legend
        ImagingRequest legend = new ImagingRequest();
        Map<String, String> visItems = mapPresenter.getMapLegend().getVisItems();
        int[] legendSize = LegendExporter.getLegendSize(visItems);
        PNGImageComponent leg = new PNGImageComponent();
        legend.setName(name + "_Legend");
        legend.setWidth(legendSize[0]);
        legend.setHeight(legendSize[1]);
        leg.setData(LegendExporter.getLegendAsBase64(visItems, "Map Legend:", legendSize));
        legend.addComponent(leg);

        List<ImagingRequest> exportRequest = new ArrayList<>();
        exportRequest.add(map);
        exportRequest.add(legend);

        mapPresenter.downloadExport(exportRequest);
    }

    private static void createNewFont(Context2d ctx, String font, String text, int width) {
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

    public void getMapAsBase64() {
        callIframe(iframe, "getBase64", getDomain());
    }

    public void buildDisplay() {
        presenter.testTileService();
    }

    public void tileServiceBad() {
        presenter.hideProgressIndicator();
        showErrorWindow(CentrifugeConstantsLocator.get().mapViewCannotReachTileService());
        presenter.hideButtonGroup();
        presenter.disableLegend();
    }

    public void tileServiceGood() {
        String srcString = "";
        if (isUseHeatmap()) {
            srcString += getHostLocation() + "heatmap2.jsp?dv_uuid=" + presenter.getDataViewUuid() + "&viz_uuid=" + presenter.getUuid();
        } else if (isUseBundlemap()) {
            srcString += getHostLocation() + "bundlemap2.jsp?dv_uuid=" + presenter.getDataViewUuid() + "&viz_uuid=" + presenter.getUuid();
        } else if (isUseTrackmap()) {
            srcString += getHostLocation() + "trackmap.jsp?dv_uuid=" + presenter.getDataViewUuid() + "&viz_uuid=" + presenter.getUuid();
        } else {
            srcString += getHostLocation() + "map2.jsp?dv_uuid=" + presenter.getDataViewUuid() + "&viz_uuid=" + presenter.getUuid();
        }
        Frame frame = new Frame(srcString);
        iframe = frame.getElement();
        iframe.setAttribute("tabIndex", "-1");
        iframe.setAttribute("style", "position: absolute;");
        needResize = true;
        calculateMapWidth();
        calculateMapHeight();
        setIframeSize();
        iframe.setAttribute("frameborder", "0");
        iframe.setAttribute("marginheight", "0");
        iframe.setAttribute("marginwidth", "0");
        // iframe.setAttribute("src", srcString);

        layoutPanel.clear();
        if (isUseBundlemap()) {
            HorizontalPanel breadcrumbPanel = new HorizontalPanel();
            breadcrumbPanel.setWidth("100%");
            breadcrumbDiv = breadcrumbPanel.getElement();
            layoutPanel.add(breadcrumbPanel);
            layoutPanel.add(frame);
            layoutPanel.setWidgetTopHeight(breadcrumbPanel, 0, Unit.PX, 20, Unit.PX);
            layoutPanel.setWidgetTopBottom(frame, 20, Unit.PX, 0, Unit.PX);
        } else if (isUseTrackmap()) {
            overviewPanel = new HorizontalPanel();
            overviewPanel.setWidth("100%");
            layoutPanel.add(frame);
            layoutPanel.add(overviewPanel);
            layoutPanel.setWidgetTopBottom(frame, 0, Unit.PX, 34, Unit.PX);
            layoutPanel.setWidgetBottomHeight(overviewPanel, -8, Unit.PX, 34, Unit.PX);
        } else {
            layoutPanel.add(frame);
            presenter.hideBackButton();
        }
        if (isUseHeatmap()) {
            presenter.showHeatMapPanelButton();
        } else {
            presenter.hideHeatMapPanelButton();
        }
        mapPresenterCache.put(presenter.getUuid(), presenter);
        MapMetricsView.EVENT_BUS.fireEvent(new ExpireMetrics(presenter.getUuid()));
        presenter.showLoadingSpinner();
    }

    public void addOverview(OverviewView overview) {
        overviewPanel.add(overview);
        overviewPanel.setCellVerticalAlignment(overview, HasVerticalAlignment.ALIGN_MIDDLE);
        overviewPanel.setCellHorizontalAlignment(overview, HasHorizontalAlignment.ALIGN_CENTER);
    }

    private String getHostLocation() {
        return GWT.getHostPageBaseURL();
    }

    public void reload() {
        presenter.testTileService();
    }

    public void rangeUpdate() {
        if (iframe != null && !isContentWindowNull(iframe)) {
            callIframe(iframe, "applySelection", getDomain());
        } else {
            presenter.testTileService();
        }
    }

    private String getDomain() {
        return "*";
    }

    public void applySelection() {
        if (iframe != null) {
            callIframe(iframe, "applySelection", getDomain());
        }
    }

    public void deselectAll() {
        if (iframe != null) {
            callIframe(iframe, "deselectAll", getDomain());
        }
    }

    public void selectAll() {
        if (iframe != null) {
            callIframe(iframe, "selectAll", getDomain());
        }
    }

    public void combinedPlaceClicked(String operation) {
        if (iframe != null) {
            callIframe(iframe, "combinedPlaceClicked;" + operation, getDomain());
        }
    }

    public void newPlaceClicked(String operation) {
        if (iframe != null) {
            callIframe(iframe, "newPlaceClicked;" + operation, getDomain());
        }
    }

    public void updatedPlaceClicked(String operation) {
        if (iframe != null) {
            callIframe(iframe, "updatedPlaceClicked;" + operation, getDomain());
        }
    }

    public void associationClicked(String associationKey, String operation) {
        if (iframe != null) {
            callIframe(iframe, "associationClicked;" + associationKey + ";" + operation, getDomain());
        }
    }

    public void trackClicked(int trackId, String trackName, String operation) {
        if (iframe != null) {
            callIframe(iframe, "trackClicked;" + trackId + ";" + trackName + ";" + operation, getDomain());
        }
    }

    public void placeClicked(int placeId, String typename, String operation) {
        if (iframe != null) {
            if (typename == null) {
                typename = "";
            }
            callIframe(iframe, "placeClicked;" + placeId + ";" + typename + ";" + operation, getDomain());
        }
    }

    public void zoomHome() {
        if (iframe != null) {
            callIframe(iframe, "zoomHome", getDomain());
        }
    }

    public void zoomIn() {
        if (iframe != null) {
            callIframe(iframe, "zoomIn", getDomain());
        }
    }

    public void zoomOut() {
        if (iframe != null) {
            callIframe(iframe, "zoomOut", getDomain());
        }
    }

    public void rangeChange() {
        long time = new Date().getTime();
        rangeChangeTest = time;
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            private long flag;

            {
                flag = time;
            }

            @Override
            public boolean execute() {
                if (flag == rangeChangeTest) {
                    if (iframe != null) {
                        callIframe(iframe, "rangeChange", getDomain());
                    }
                }
                return false;
            }
        }, 200);
    }

    public void toggleSelection() {
        if (iframe != null) {
            callIframe(iframe, "toggleSelection", getDomain());
        }
    }

    public void toggleSearch() {
        if (iframe != null) {
            callIframe(iframe, "toggleSearch", getDomain());
        }
    }

    public void backToBundle() {
        if (iframe != null) {
            callIframe(iframe, "backToBundle", getDomain());
        }
    }

    public void heatmapPanel() {
        if (iframe != null) {
            callIframe(iframe, "heatmapPanel", getDomain());
        }
    }

    public void legendUpdated() {
        if (iframe != null) {
            callIframe(iframe, "legendUpdated", getDomain());
        }
    }

    public void setSelectionModeCircle() {
        if (iframe != null) {
            callIframe(iframe, "setSelectionModeCircle", getDomain());
        }
    }

    public void setSelectionModeRectangle() {
        if (iframe != null) {
            callIframe(iframe, "setSelectionModeRectangle", getDomain());
        }
    }

    public void setSelectionModePolygon() {
        if (iframe != null) {
            callIframe(iframe, "setSelectionModePolygon", getDomain());
        }
    }

    public void setSelectionModePan() {
        if (iframe != null) {
            callIframe(iframe, "setSelectionModePan", getDomain());
        }
    }

    public void broadcastNotify(String text) {
        BroadcastAlert broadcastAlert = new BroadcastAlert(text);
        layoutPanel.add(broadcastAlert);
        if (CentrifugeConstantsLocator.get().broadcastAlert_broadcastReceived().equals(text)) {
            MapMetricsView.EVENT_BUS.fireEvent(new ExpireMetrics(presenter.getUuid()));
        }
    }

    public void showNoData() {
        showErrorWindow(CentrifugeConstantsLocator.get().mapViewNoData());
    }

    public void showData() {
        clearInfoWindows();
    }

    public void showPointLimitReached() {
        showErrorWindow(CentrifugeConstantsLocator.get().mapPointLimit());
    }

    public void showPlaceTypeLimitReached() {
        showErrorWindow(CentrifugeConstantsLocator.get().mapPlaceTypeLimit());
    }

    public void showTrackTypeLimitReached() {
        showErrorWindow(CentrifugeConstantsLocator.get().mapTrackTypeLimit());
    }

    public void tooManyPoints(String number) {
        Dialog.showInfo(CentrifugeConstantsLocator.get().mapPointLimit(),
                CentrifugeConstantsLocator.get().mapTooManyPoints(MapConfigProxy.instance().getPointLimit(), number));
    }

    @Override
    public void onResize() {
        super.onResize();
        String oldMapWidth = mapWidth;
        String oldMapHeight = mapHeight;
        calculateMapWidth();
        calculateMapHeight();
        if (iframe != null && !(mapWidth.equals(oldMapWidth) && mapHeight.equals(oldMapHeight))) {
            if (isInfoWindowOn) {
                needResize = true;
                callIframe(iframe, "resizeHappened", getDomain());
            } else {
                callIframe(iframe, "mapContainerResizing", getDomain());
                setIframeSize();
                callIframe(iframe, "mapContainerResized", getDomain());
            }
        }
        if (presenter.getMetrics() != null) {
            presenter.getMetrics().positionLegend();
        }
        if (isUseTrackmap()) {
            resizeOverview();
        }
    }

    private void calculateMapWidth() {
        mapWidth = String.valueOf(layoutPanel.getOffsetWidth());
    }

    private void calculateMapHeight() {
        int height = layoutPanel.getOffsetHeight();
        if (isUseBundlemap()) {
            height -= 20;
        } else if (isUseTrackmap()) {
            height -= 34;
        }
        mapHeight = String.valueOf(height);
    }

    private void showErrorWindow(String message) {
        layoutPanel.clear();
        iframe = null;
        infoWindow = new InfoPanel(message);
        layoutPanel.add(infoWindow);
        isInfoWindowOn = true;
        MapMetricsView.EVENT_BUS.fireEvent(new HideMetricsEvent(presenter.getUuid()));
    }

    public void processResize() {
        if (needResize) {
            callIframe(iframe, "mapContainerResizing", getDomain());
            setIframeSize();
            callIframe(iframe, "mapContainerResized", getDomain());
        }
    }

    public void clearInfoWindows() {
//		layoutPanel.clear();
        if (infoWindow != null) {
            infoWindow.removeFromParent();
        }
        isInfoWindowOn = false;
    }

    private void setIframeSize() {
        iframe.setAttribute("width", mapWidth);
        iframe.setAttribute("height", mapHeight);
        needResize = false;
    }

    private boolean isUseHeatmap() {
        return presenter.isUseHeatMap();
    }

    private boolean isUseBundlemap() {
        return presenter.isUseBundleMap();
    }

    private boolean isUseTrackmap() {
        return presenter.isUseTrackMap();
    }

    public void setupBreadcrumb(String breadcrumbAggregate, boolean removeLink) {
        breadcrumbDiv.removeAllChildren();
        String[] crumbs = breadcrumbAggregate.split(";");
        int index = 0;
        while (index < crumbs.length) {
            if (index > 0) {
                SpanElement spanElement = Document.get().createSpanElement();
                spanElement.setInnerHTML(" > ");
                breadcrumbDiv.appendChild(spanElement);
            }
            String value = crumbs[index];
            if (removeLink || !value.contains("(")) {
                SpanElement spanElement = Document.get().createSpanElement();
                spanElement.setInnerHTML(value);
                breadcrumbDiv.appendChild(spanElement);
            } else {
                AnchorElement anchorElement = Document.get().createAnchorElement();
                anchorElement.setInnerHTML(value);
                final String columnName = value.substring(0, value.indexOf("("));
                Anchor a = Anchor.wrap(anchorElement);
                a.addClickHandler((ClickEvent event) -> callIframe(iframe, "trimBreadcrumb;" + columnName, getDomain()));
                breadcrumbDiv.appendChild(anchorElement);
            }
            index++;
        }
    }

    public void checkNeedReload() {
        callIframe(iframe, "checkNeedReload", getDomain());
    }

    private void resizeOverview() {
        int width = getOffsetWidth();
        presenter.resizeOverview(width);
    }

    interface MapViewUiBinder extends UiBinder<Widget, MapView> {
    }
}
