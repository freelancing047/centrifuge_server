//package csi.client.gwt.dataview.directed.visualization;
//
//import static com.google.common.base.Preconditions.checkNotNull;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.github.gwtbootstrap.client.ui.Button;
//import com.github.gwtbootstrap.client.ui.ButtonGroup;
//import com.github.gwtbootstrap.client.ui.Navbar;
//import com.github.gwtbootstrap.client.ui.Tab;
//import com.github.gwtbootstrap.client.ui.Tooltip;
//import com.github.gwtbootstrap.client.ui.constants.IconType;
//import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
//import com.google.common.base.Optional;
//import com.google.gwt.core.client.GWT;
//import com.google.gwt.core.client.Scheduler;
//import com.google.gwt.dom.client.Style.Overflow;
//import com.google.gwt.dom.client.Style.Unit;
//import com.google.gwt.event.dom.client.ClickEvent;
//import com.google.gwt.event.dom.client.ClickHandler;
//import com.google.gwt.event.shared.EventBus;
//import com.google.gwt.event.shared.SimpleEventBus;
//import com.google.gwt.uibinder.client.UiBinder;
//import com.google.gwt.uibinder.client.UiField;
//import com.google.gwt.uibinder.client.UiHandler;
//import com.google.gwt.user.client.ui.AbsolutePanel;
//import com.google.gwt.user.client.ui.DockLayoutPanel;
//import com.google.gwt.user.client.ui.HTMLPanel;
//import com.google.gwt.user.client.ui.IsWidget;
//import com.google.gwt.user.client.ui.LayoutPanel;
//import com.google.gwt.user.client.ui.ResizeComposite;
//import com.google.gwt.user.client.ui.ResizeLayoutPanel;
//import com.google.gwt.user.client.ui.SimpleLayoutPanel;
//import com.google.gwt.user.client.ui.SplitLayoutPanel;
//import com.google.gwt.user.client.ui.Widget;
//import com.google.web.bindery.event.shared.Event;
//import com.sencha.gxt.core.client.dom.XDOM;
//import com.sencha.gxt.fx.client.Draggable;
//import com.sencha.gxt.widget.core.client.ContentPanel;
//import com.sencha.gxt.widget.core.client.Resizable;
//import com.sencha.gxt.widget.core.client.Window;
//import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
//import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
//import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
//
//import csi.client.gwt.dataview.DataViewWidget;
//import csi.client.gwt.dataview.directed.DirectedPresenter;
//import csi.client.gwt.events.CsiEvent;
//import csi.client.gwt.events.CsiEventCommander;
//import csi.client.gwt.events.CsiEventHandler;
//import csi.client.gwt.events.CsiEventHeader;
//import csi.client.gwt.viz.Visualization;
//import csi.client.gwt.viz.VizMessageArea;
//import csi.client.gwt.viz.shared.chrome.VizChrome;
//import csi.client.gwt.viz.shared.chrome.panel.RenderSize;
//import csi.client.gwt.viz.shared.chrome.panel.RenderSizeChangeEvent;
//import csi.client.gwt.viz.shared.chrome.panel.VizPanelFrameProvider;
//import csi.client.gwt.viz.shared.menu.CsiMenuNav;
//import csi.client.gwt.viz.shared.menu.MenuKey;
//import csi.client.gwt.widget.boot.AbstractCsiTab;
//import csi.client.gwt.widget.boot.CsiTabPanel;
//import csi.shared.core.Constants;
//
//public class DirectedVizPanel extends ResizeComposite implements VizChrome {
//
//	private static final double TABPANEL_RESIZE_FACTOR = .8;
//    private static final String BROADCAST_TOOLTIP = "Currently Listening";
//    private DirectedPresenter presenter;
//
//    interface MyUiBinder extends UiBinder<Widget, DirectedVizPanel> {
//    }
//
//    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
//
//    @Override
//    public LayoutPanel getMainLP() {
//        return mainLP;
//    }
//
//    @UiField
//    AbsolutePanel abso;
//    @UiField
//    VBoxLayoutContainer buttonGroupContainer;
//    @UiField
//    ButtonGroup buttonGroup;
//    @UiField
//    SimpleLayoutPanel mainPanel;
//    @UiField(provided = true)
//    SplitLayoutPanel controlsLayer;
//    @UiField
//    CsiMenuNav csiMenuNav;
//    @UiField
//    Navbar menuNavBar;
//    
//    @UiField
//    HTMLPanel notificationTray;
//    //
//    String name;
//    @UiField
//    CsiTabPanel tabpanel;
//    @UiField
//    ResizeLayoutPanel tabpanelWrapper;
//    @UiField
//    DockLayoutPanel dockLayoutPanel;
//
//    private Button broadcastListener;
//
//    @UiField
//    LayoutPanel mainLP;
//
//    @UiField
//    Button restoreTabDrawerPanelButton;
//    @UiField
//    Button minimizeTabDrawerPanelButton;
//    @UiField
//    Button popoutTabDrawerPanelButton;
//
//    @UiField
//    LayoutPanel tabPanelLayoutPanel;
//    @UiField
//    SimpleLayoutPanel controlBar;
//
//    private Visualization visualization;
//    private VizMessageArea messageArea;
//    private boolean isFullscreen;
//    private boolean tabDrawerVisible;
//    private VizPanelFrameProvider frameProvider;
//
//    protected boolean isHidden = true;
//
//    private RenderSize currentRenderSize;
//    private EventBus eventBus = new SimpleEventBus();
//    private Optional<String> vizUuid;
//
//    private int tabDrawerLastFullSize = 250;
//
//    @Override
//    public SplitLayoutPanel getControlsLayer() {
//        return controlsLayer;
//    }
//
//    public DirectedVizPanel(DirectedPresenter presenter) {
//    	this.presenter = presenter;
//        controlsLayer = new SplitLayoutPanel(4) {
//
//            @Override
//            public void onResize() {
//                super.onResize();
//
//                Map<String, String> headerMap = new HashMap<String, String>();
//                headerMap.put("tab", "resize");
//
//                new CsiEvent(headerMap, new HashMap<String, String>()).fire();
//            }
//        };
//        broadcastListener = new Button();
//        broadcastListener.setIcon(IconType.HEADPHONES);
//        broadcastListener.setTitle(BROADCAST_TOOLTIP);
//        broadcastListener.setSize(ButtonSize.SMALL);
//        broadcastListener.setVisible(false);
//        broadcastListener.addStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_HEADER_BUTTON);
//        broadcastListener.addClickHandler(new ClickHandler() {
//
//            @Override
//            public void onClick(ClickEvent event) {
//                getVisualization().setBroadcastListener(false);
//                getVisualization().getChrome().getMenu().unCheckedMenuItem(MenuKey.LISTEN_FOR_BROADCAST);
//                disableBroadcastListener();
//            }
//        });
//
//        initWidget(uiBinder.createAndBindUi(this));
//        dockLayoutPanel.getElement().setId("vizChrome");
//        controlsLayer.setWidgetToggleDisplayAllowed(tabpanelWrapper, true);
//        tabpanelWrapper.getElement().getStyle().setOverflow(Overflow.VISIBLE);
//        // controlsLayer.setWidgetMinSize(tabpanelWrapper, 0);
//        menuNavBar.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
//
//        // toggleTabDrawerPanelButton.addClickHandler(new ToggleTabDrawerClickHandler(controlsLayer.getElement(),
//        // toggleTabDrawerPanelButton));
//    }
//
//    @UiHandler("restoreTabDrawerPanelButton")
//    void restoreTabDrawer(ClickEvent event) {
//        if (isHidden) {
//            if(tabDrawerLastFullSize > this.getOffsetHeight() && this.getOffsetHeight() > 200){
//                //We force this to not be greater than 80% of the total height
//                controlsLayer.setWidgetSize(tabpanelWrapper, (int) (this.getOffsetHeight() * TABPANEL_RESIZE_FACTOR));
//            } else {
//                controlsLayer.setWidgetSize(tabpanelWrapper, tabDrawerLastFullSize);
//            }
//            controlsLayer.animate(300);
//        }
//        isHidden = !isHidden;
//    }
//
//
//    @UiHandler("minimizeTabDrawerPanelButton")
//    void minimizeTabDrawer(ClickEvent event) {
//        if (isHidden == false) {
//            tabDrawerLastFullSize = tabpanel.getOffsetHeight();
//            controlsLayer.setWidgetSize(tabpanelWrapper, 30);
//            controlsLayer.animate(300);
//        }
//        isHidden = !isHidden;
//    }
//
//    private void addTabNameUpDownActionOnClick(final Tab tab) {
//        tab.asTabLink().addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                if (isHidden) {
//                    restoreTabDrawer(null);
//                } else if (tabpanel.getSelectedTab() == tabpanel.getLinks().indexOf(tab.asTabLink())) {
//                    minimizeTabDrawer(null);
//                }
//            }
//        });
//    }
//
//    public void enablePopoutButton(boolean isEnabel){
//
//        popoutTabDrawerPanelButton.setEnabled(isEnabel);
//    }
//
//
//    @UiHandler("popoutTabDrawerPanelButton")
//    void onPopoutTabDrawer(ClickEvent event) {
//        int offsetHeight = tabPanelLayoutPanel.getOffsetHeight();
//        int offsetWidth = tabPanelLayoutPanel.getOffsetWidth();
//        int selectedTab = tabpanel.getSelectedTab();
//        floatingTabWindow = new Window() {
//
//            @Override
//            public void onResize() {
//                // no-op
//            }
//        };
//        floatingTabWindow.add(tabPanelLayoutPanel);
//        floatingTabWindow.setWidth(offsetWidth);
//        floatingTabWindow.setHeight(offsetHeight);
//        floatingTabWindow.setHeading(visualization.getVisualizationDef().getName());
//        DirectedVizPanel.this.presenter.add(floatingTabWindow);
//        floatingTabWindow.show();
//        floatingTabWindow.getDraggable().setUpdateZIndex(true);
//        floatingTabWindow.setMinWidth(350);
//        floatingTabWindow.setHeight(275);
//        // controlsLayer.remove(tabpanelWrapper);
//
//        // controlsLayer.setWidgetSize(tabpanelWrapper, -10);
//        // tabpanelWrapper.setVisible(false);
//        controlsLayer.setWidgetHidden(tabpanelWrapper, true);
//        controlsLayer.forceLayout();
//        popoutTabDrawerPanelButton.setVisible(false);
//        restoreTabDrawerPanelButton.setVisible(false);
//        minimizeTabDrawerPanelButton.setVisible(false);
//        floatingTabWindow.addBeforeHideHandler(new BeforeHideHandler() {
//
//            @Override
//            public void onBeforeHide(BeforeHideEvent event) {
//                int selectedTab = tabpanel.getSelectedTab();
//                tabpanelWrapper.add(tabPanelLayoutPanel);
//                // tabpanelWrapper.setVisible(true);
//                controlsLayer.setWidgetHidden(tabpanelWrapper, false);
//                controlsLayer.setWidgetSize(tabpanelWrapper, 30);
//                isHidden = true;
//                // NOTE: need to clear these values so it will go back to taking 100%
//                tabPanelLayoutPanel.getElement().getStyle().setProperty("height", null);
//                tabPanelLayoutPanel.getElement().getStyle().setProperty("width", null);
//                // controlsLayer.insertSouth(tabpanelWrapper, 30, null);
//                controlsLayer.forceLayout();
//                popoutTabDrawerPanelButton.setVisible(true);
//                restoreTabDrawerPanelButton.setVisible(true);
//                minimizeTabDrawerPanelButton.setVisible(true);
//                tabDrawerVisible = false;
//                tabpanel.selectTab(selectedTab);
//                tabpanel.selectTab(selectedTab);
//                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
//                    @Override
//                    public void execute() {
//                        abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 23, abso.getOffsetHeight() / 2 - buttonGroupContainer.getOffsetHeight() / 2);
//                    }
//                });
//            }
//        });
//        floatingTabWindow.getDraggable().setContainer(DirectedVizPanel.this.presenter.getView().asWidget());
//        abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 25, 0);
//        tabpanel.selectTab(selectedTab);
//        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
//            @Override
//            public void execute() {
//                abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 23, abso.getOffsetHeight() / 2 - buttonGroupContainer.getOffsetHeight() / 2);
//            }
//        });
//    }
//
//    public void setFrameProvider(VizPanelFrameProvider frameProvider) {
//        this.frameProvider = frameProvider;
//        frameProvider.addButton(broadcastListener);
//    }
//
//    private void setupFullscreenHandler() {
//        mainLP.getElement().setId("main-layout-panel-" + vizUuid);
//
//        Map<String, String> headerMap = new HashMap<String, String>();
//        headerMap.put("viz", "fullscreenEvent");
//        headerMap.put("vizId", vizUuid.get());
//        CsiEventHeader header = new CsiEventHeader(headerMap);
//        CsiEventCommander.getInstance().addHandler(header, new CsiEventHandler() {
//
//            @Override
//            public void onCsiEvent(CsiEvent event) {
//                Map<String, String> payload = event.getPayload();
//                String enabled = payload.get("enabled");
//                if ("true".equals(enabled)) {
//                    isFullscreen = true;
//                } else {
//                    isFullscreen = false;
//                }
//            }
//        });
//    }
//
//    @Override
//    public EventBus getEventBus() {
//        return eventBus;
//    }
//
//    @Override
//    public void addTab(final Tab tab) {
//        tabpanel.add(tab);
//        addTabNameUpDownActionOnClick(tab);
//    }
//
//
//
//    @Override
//    public void requestFullScreen() {
//        if (vizUuid.isPresent()) {
//            requestFullscreenNative(vizUuid.get());
//        }
//    }
//
//    public static void requestFullscreenNative(String vizUuid){
//        DataViewWidget.showFullscreen("main-layout-panel-" + vizUuid);
//    }
//
//    @Override
//    public void addWindow(final ContentPanel display) {
//        display.setHeight(150);
//        display.setWidth(150);
//        display.addStyleName("overlay-clear");
//        display.setBodyStyle("pad-text");
//        display.setCollapsible(true);
//        abso.add(display);
//        // Create Draggable around Window
//        final Draggable d = new Draggable(display, display.getHeader());
//        d.setUseProxy(false);
//        d.setContainer(abso);
//        new Resizable(display);
//    }
//
//    @Override
//    public CsiMenuNav getMenu() {
//        return csiMenuNav;
//    }
//
//    @Override
//    public Visualization getVisualization() {
//        return visualization;
//    }
//
//    @Override
//    public void setVisualization(Visualization v) {
//        checkNotNull(v);
//        visualization = v;
//        vizUuid = Optional.of(v.getUuid());
//        // adopt
//        v.setChrome(this);
//        setupFullscreenHandler();
//    }
//
//    @Override
//    public void removeVisualization() {
//        visualization.removeFromParent();
//        visualization = null;
//        vizUuid = Optional.absent();
//        mainPanel.clear();
//        eventBus = new SimpleEventBus();
//    }
//
//    @Override
//    public VizMessageArea getMessageArea() {
//        return messageArea;
//    }
//
//    @Override
//    public void selectTab(AbstractCsiTab nodesTab) {
//        int widgetIndex = tabpanel.getWidgetIndex(nodesTab);
//        tabpanel.selectTab(widgetIndex);
//    }
//
//    @Override
//    public void setWidget(IsWidget w) {
//        checkNotNull(w);
//        mainPanel.clear();
//        mainPanel.setWidget(w);
//    }
//
//    @Override
//    public void addButton(IsWidget w) {
//        if (w instanceof Tooltip) {
//            Tooltip t = (Tooltip) w;
//            t.getWidget().addStyleName("rightControlButton");
//            buttonGroup.add(w.asWidget());
//            return;
//        }
//        if (w instanceof Button) {
//            w.asWidget().addStyleName("rightControlButton");
//            buttonGroup.add(w);
//            return;
//        }
//        if (w instanceof ButtonGroup) {
//            ((ButtonGroup) w).setVertical(true);
//            ((ButtonGroup) w).getElement().getStyle().setPaddingTop(2, Unit.PX);
//            w.asWidget().addStyleName("dragTypeButtonGroup");
//            buttonGroupContainer.add(w);
//            return;
//        }
//    }
//
//    @Override
//    public void removeTab(AbstractCsiTab tab) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void removeWindow(ContentPanel panel) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void removeButton(Widget widget) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void onResize() {
//        super.onResize();
//        RenderSize renderSize = RenderSize.forSize(asWidget().getOffsetWidth());
//        if (renderSize != currentRenderSize) {
//            if (tabDrawerVisible) {
//                controlsLayer.asWidget().setVisible(renderSize.showButtons());
//            }
//            dockLayoutPanel.setWidgetHidden(menuNavBar, renderSize == RenderSize.MICRO);
//            currentRenderSize = renderSize;
//            getEventBus().fireEvent(new RenderSizeChangeEvent(currentRenderSize));
//        }
//        getEventBus().fireEvent(new ChromeResizeEvent(asWidget().getOffsetWidth(), asWidget().getOffsetHeight()));
//        abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 23, abso.getOffsetHeight() / 2 - buttonGroupContainer.getOffsetHeight() / 2);
//        getMenu().onResize();
//
//        if(tabpanelWrapper.getOffsetHeight() > this.getOffsetHeight() && this.getOffsetHeight() > 200){
//            //We force this to not be greater than 80% of the total height
//            controlsLayer.setWidgetSize(tabpanelWrapper, (int) (this.getOffsetHeight() * TABPANEL_RESIZE_FACTOR));
//        }
//    }
//
//    public VBoxLayoutContainer getButtonGroupContainer() {
//        return buttonGroupContainer;
//    }
//
//    @Override
//    public void hideControlLayer() {
//        controlsLayer.getElement().getParentElement().removeFromParent();
//    }
//
//    @Override
//    public void setTabDrawerVisible(boolean visible) {
//        tabDrawerVisible = visible;
//        controlsLayer.asWidget().setVisible(visible);
//    }
//
//    @Override
//    public void setName(String name) {
//        frameProvider.setName(name);
//    }
//
//    @Override
//    public void enableBroadcastListener() {
//        broadcastListener.setVisible(true);
//    }
//
//    @Override
//    public void disableBroadcastListener() {
//        broadcastListener.setVisible(false);
//    }
//
//    @Override
//    public SimpleLayoutPanel getControlBar() {
//        return controlBar;
//    }
//
//    @Override
//    public void bringFloatingTabDrawerToFront() {
//        if (floatingTabWindow != null) {
//            floatingTabWindow.setZIndex(XDOM.getTopZIndex());
//        }
//    }
//
//    Window floatingTabWindow;
//
//    public VizPanelFrameProvider getFrameProvider() {
//        return frameProvider;
//    }
//
//    public static abstract class ChromeResizeEventHandler {
//        abstract public void onChromeResize();
//    }
//
//    public static class ChromeResizeEvent extends Event<ChromeResizeEventHandler> {
//        public static final Type<ChromeResizeEventHandler> TYPE = new Type<ChromeResizeEventHandler>();
//        private final int width;
//        private final int height;
//
//        public ChromeResizeEvent(int width, int height) {
//            this.width = width;
//            this.height = height;
//        }
//
//        @Override
//        public Type<ChromeResizeEventHandler> getAssociatedType() {
//            return TYPE;
//        }
//
//        @Override
//        protected void dispatch(ChromeResizeEventHandler handler) {
//            handler.onChromeResize();
//        }
//
//        public int getHeight() {
//            return height;
//        }
//
//        public int getWidth() {
//            return width;
//        }
//    }
//
//    public HTMLPanel getNotificationTray() {
//        return notificationTray;
//    }
//
//}
