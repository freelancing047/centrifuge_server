package csi.client.gwt.viz.map;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.fx.client.DragEndEvent.DragEndHandler;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.widget.core.client.ContentPanel;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.map.legend.MouseEventResponder;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;

public class MapActivityManager extends CSIActivityManager implements MouseEventResponder {
    private MapPresenter presenter;
    private VizChrome chrome;

    private Button resizeFullButton;
    private Button zoomInButton;
    private Button zoomOutButton;
    private Button selectModeButton;
    private Button searchModeButton;
    private Button backToBundleButton;
    private Button heatmapPanelButton;
    private Button pinMapButton;
    private Button selectionModeButton;
    private boolean mouseSensitive;

    public MapActivityManager(ActivityMapper mapper, EventBus eventBus, MapPresenter presenter) {
        super(mapper, eventBus);
        this.presenter = presenter;
    }

    @Override
    public void setDisplay(final AcceptsOneWidget display) {
        super.setDisplay(display);
        if (display instanceof VizChrome) {
            final VizChrome chrome = (VizChrome) display;
            this.chrome = chrome;

            resizeFullButton = addButton(IconType.RESIZE_FULL, presenter.getZoomHomeHandler());
            zoomInButton = addButton(IconType.ZOOM_IN, presenter.getZoomInHandler());
            zoomOutButton = addButton(IconType.ZOOM_OUT, presenter.getZoomOutHandler());
            selectModeButton = addButton(IconType.SCREENSHOT, presenter.getSelectModeHandler());
            searchModeButton = addButton(IconType.SEARCH, presenter.getSearchModeHandler());
            backToBundleButton = addButton(IconType.ARROW_LEFT, presenter.getBackToBundleHandler());
            presenter.getBackToBundleHandler().setVizChrome(chrome);
            heatmapPanelButton = addButton(IconType.TASKS, presenter.getHeatMapPanelHandler());
            pinMapButton = addButton(IconType.UNLOCK, presenter.getPinMapHandler());
            selectionModeButton = addButton(IconType.CHECK_EMPTY, presenter.getSelectionModeHandler());

            final ContentPanel legendAsWindow = presenter.getMapLegend().getLegendAsWindow();
            Draggable d = chrome.addWindowAndReturnDraggable(legendAsWindow);
            DragEndHandler h = (DragEndHandler) presenter.getMapLegend();
            d.addDragEndHandler(h);
            {
                // final Widget parent = legendAsWindow.getParent();

                // FIXME: I don't like this approach...
                //    			chrome.getEventBus().addHandler(RenderSizeChangeEvent.type, new RenderSizeChangeEventHandler() {
                //    						@Override
                //    						public void onAttach(RenderSizeChangeEvent event) {
                //    							((AbsolutePanel) parent).setWidgetPosition(legendAsWindow, parent.getElement().getOffsetWidth() - 64 - 25 - legendAsWindow.getOffsetWidth(), 25);
                //    						}
                //    					});
                Scheduler.get().scheduleFixedDelay(() -> {
                    chrome.getEventBus().addHandler(VizPanel.ChromeResizeEvent.TYPE, new VizPanel.ChromeResizeEventHandler() {
                        @Override
                        public void onChromeResize() {
                            if (presenter.getMapLegend().isVisible()) {
                                presenter.getMapLegend().positionLegend();
                                presenter.getView().checkNeedReload();
                            }
                        }
                    });
                    return false;
                }, 1000);
            }
        }
    }

    private Button addButton(IconType iconType, VizButtonHandler buttonHandler) {
        Button button = new Button();
        button.setTitle(buttonHandler.getTooltipText());
        button.setIcon(iconType);
        button.setType(ButtonType.DEFAULT);
        buttonHandler.bind(button);
        chrome.addButton(button);
        return button;
    }

    @Override
    public void sensitize() {
        if (!mouseSensitive) {
            setPointerEvents(resizeFullButton, "all");
            setPointerEvents(zoomInButton, "all");
            setPointerEvents(zoomOutButton, "all");
            setPointerEvents(zoomOutButton, "all");
            setPointerEvents(selectModeButton, "all");
            setPointerEvents(searchModeButton, "all");
            setPointerEvents(backToBundleButton, "all");
            setPointerEvents(heatmapPanelButton, "all");
            setPointerEvents(pinMapButton, "all");
            setPointerEvents(selectionModeButton, "all");
            mouseSensitive = true;
        }
    }

    private void setPointerEvents(Button button, String value) {
        com.google.gwt.dom.client.Style style = button.getElement().getStyle();
        style.setProperty("pointerEvents", value);
    }

    @Override
    public void desensitize() {
        if (mouseSensitive) {
            setPointerEvents(resizeFullButton, "none");
            setPointerEvents(zoomInButton, "none");
            setPointerEvents(zoomOutButton, "none");
            setPointerEvents(zoomOutButton, "none");
            setPointerEvents(selectModeButton, "none");
            setPointerEvents(searchModeButton, "none");
            setPointerEvents(backToBundleButton, "none");
            setPointerEvents(heatmapPanelButton, "none");
            setPointerEvents(pinMapButton, "none");
            setPointerEvents(selectionModeButton, "none");
            mouseSensitive = false;
        }
    }

}
