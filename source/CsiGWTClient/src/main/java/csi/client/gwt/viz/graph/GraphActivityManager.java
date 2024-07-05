package csi.client.gwt.viz.graph;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.constants.ToggleType;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;

import csi.client.gwt.WebMain;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.viz.shared.chrome.panel.RenderSizeChangeEvent;
import csi.client.gwt.viz.shared.chrome.panel.RenderSizeChangeEventHandler;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;

//TODO: make non-pubic again later
public class GraphActivityManager extends CSIActivityManager {

    private Graph graph;
    private VizChrome chrome;
    private ButtonGroup dragTypeButtonGroup;

    @Override
    public void setDisplay(final AcceptsOneWidget display) {
        super.setDisplay(display);
        if (display instanceof VizChrome) {
            final VizChrome chrome = (VizChrome) display;
            this.chrome = chrome;
            chrome.addTab(graph.getNodesTab());
            chrome.addTab(graph.getLinksTab());
            chrome.addTab(graph.getPathTab());

//            if(WebMain)
            if(!graph.getVisualizationDef().isReadOnly()) {
                chrome.addTab(graph.getTimePlayerTab());
            }



            chrome.addTab(graph.getStatisticsTab());
            if(!graph.getVisualizationDef().isReadOnly()) {
                if (WebMain.getClientStartupInfo().getGraphAdvConfig().getPatternConfig().getEnabled()) {
                    chrome.addTab(graph.getPatternTab());
                }
            }
            if(!graph.getVisualizationDef().isReadOnly()) {
                addButton(IconType.CIRCLE, graph.getTransparencyHandler());
            }
            addButton(IconType.RESIZE_FULL, graph.getFitToSizeHandler());
            addButton(IconType.ZOOM_IN, graph.getZoomInHandler());
            //            addButton(IconType.FULLSCREEN, graph.getFullScreenHandler());
            addButton(IconType.ZOOM_OUT, graph.getZoomOutHandler());
            chrome.addButton(dragTypeButtonGroup);

            // FIXME:Should not be walking factories... Perhaps I should give the legend the chrome...
            final ContentPanel legendAsWindow = graph.getGraphLegend().getLegendAsWindow();
            chrome.addWindow(legendAsWindow);
            {
                final Widget parent = legendAsWindow.getParent();

                //FIXME: I don't like this approach...
                chrome.getEventBus().addHandler(RenderSizeChangeEvent.type, new RenderSizeChangeEventHandler() {
                    @Override
                    public void onAttach(RenderSizeChangeEvent event) {
                        ((AbsolutePanel) parent).setWidgetPosition(legendAsWindow, parent.getElement().getOffsetWidth() - 64 - 25 - legendAsWindow.getOffsetWidth(), 25);
                    }
                });
                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {

                        chrome.getEventBus().addHandler(VizPanel.ChromeResizeEvent.TYPE, new VizPanel.ChromeResizeEventHandler() {

                            @Override
                            public void onChromeResize() {

                                VBoxLayoutContainer buttonGroupContainer = ((VizPanel) chrome).getButtonGroupContainer();
                                if (buttonGroupContainer != null) {
                                        int parentOffsetWidth = parent.getOffsetWidth();
                                        int legendOffsetWidth = legendAsWindow.getOffsetWidth();
                                        int top = legendAsWindow.getOffsetHeight() - legendAsWindow.getOffsetHeight()/2;

                                        ((AbsolutePanel) parent).setWidgetPosition(legendAsWindow, parentOffsetWidth - 3*legendOffsetWidth/2, top);
                                    }
                                }
                        });
                        return false;
                    }
                }, 1000);
            }
            chrome.addWindow(graph.getTransparency().asWindow());
            chrome.setWidget(graph.getGraphSurface().getView());
            chrome.setTabDrawerVisible(true);
            chrome.getControlBar().add(graph.getGraphControlBar());
        }
    }


    private void createDragTypeButtonGroup() {
        dragTypeButtonGroup = new ButtonGroup();

        Button defaultButton = addToButtonGroup(IconType.LOCATION_ARROW, graph.getDefaultModeHandler());
        graph.getDefaultModeHandler().setButton(defaultButton);

        Button panButton = addToButtonGroup(IconType.MOVE, graph.getPanModeHandler());
        graph.getPanModeHandler().setButton(panButton);

        Button selectButton = addToButtonGroup(IconType.SCREENSHOT, graph.getSelectModeHandler());
        graph.getSelectModeHandler().setButton(selectButton);

        Button zoomButton = addToButtonGroup(IconType.SEARCH, graph.getZoomModeHandler());
        graph.getZoomModeHandler().setButton(zoomButton);

        dragTypeButtonGroup.setToggle(ToggleType.RADIO);
    }

    private Button addToButtonGroup(IconType iconType, VizButtonHandler buttonHandler) {
        Button button = new Button();
        button.setTitle(buttonHandler.getTooltipText());
        dragTypeButtonGroup.add(button);
        button.setIcon(iconType);
        button.setType(ButtonType.DEFAULT);
        buttonHandler.bind(button);
        button.addStyleName("overlay-clear");//NON-NLS
        button.addStyleName("rightControlButton");//NON-NLS
        return button;
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

    public GraphActivityManager(ActivityMapper mapper, EventBus eventBus, Graph graph) {
        super(mapper, eventBus);
        this.graph = graph;
        createDragTypeButtonGroup();
    }
}
