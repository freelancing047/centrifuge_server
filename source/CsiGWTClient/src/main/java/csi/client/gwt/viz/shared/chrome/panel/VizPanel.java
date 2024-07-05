package csi.client.gwt.viz.shared.chrome.panel;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.Event;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Resizable;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
import csi.client.gwt.dataview.broadcast.BroadcastManager;
import csi.client.gwt.dataview.directed.DirectedPresenter;
import csi.client.gwt.dataview.directed.visualization.DirectedWindow;
import csi.client.gwt.events.CsiEvent;
import csi.client.gwt.events.CsiEventCommander;
import csi.client.gwt.events.CsiEventHandler;
import csi.client.gwt.events.CsiEventHeader;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.VizMessageArea;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.viz.shared.menu.CsiMenuNav;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.widget.InfoPanel;
import csi.client.gwt.widget.boot.AbstractCsiTab;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.client.gwt.worksheet.layout.window.VisualizationWindow;
import csi.shared.core.Constants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class VizPanel extends ResizeComposite implements VizChrome {

    public static final int DEFAULT_WINDOW_SIZE = 150;
    public static final int MENU_HIDE_DELAY = 2000;
    private static final double TABPANEL_RESIZE_FACTOR = .8;
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    protected boolean isHidden = true;
    @UiField
    AbsolutePanel abso;
    @UiField
    VBoxLayoutContainer buttonGroupContainer;
    @UiField
    ButtonGroup buttonGroup;
    @UiField
    SimpleLayoutPanel mainPanel;
    @UiField(provided = true)
    SplitLayoutPanel controlsLayer;
    CsiMenuNav csiMenuNav;
    Navbar menuNavBar;
    //
    String name;
    @UiField
    CsiTabPanel tabpanel;
    @UiField
    ResizeLayoutPanel tabpanelWrapper;
    @UiField
    DockLayoutPanel dockLayoutPanel;
    @UiField
    LayoutPanel mainLP;
    @UiField
    Button restoreTabDrawerPanelButton;
    @UiField
    Button minimizeTabDrawerPanelButton;
    @UiField
    Button popoutTabDrawerPanelButton;
    @UiField
    LayoutPanel tabPanelLayoutPanel;
    @UiField
    SimpleLayoutPanel controlBar;
    long closeAfter = Long.MIN_VALUE;
    Window floatingTabWindow;
    private String BROADCAST_TOOLTIP = CentrifugeConstantsLocator.get().chrome_vizPanel_broadcast_tooltip();
    private SimpleLayoutPanel sendToPanel;
    private SimpleLayoutPanel instructionPanel;
    private Button broadcastListener;
    private Visualization visualization;
    private VizMessageArea messageArea;
    private boolean isFullscreen;
    private boolean tabDrawerVisible;
    private VizPanelFrameProvider frameProvider;
    private static VizPanelFrameProvider activeFrameProvider;

    private WorksheetPresenter worksheet;
    private DirectedPresenter directedPresenter;
    private RenderSize currentRenderSize;
    private EventBus eventBus = new SimpleEventBus();
    private Optional<String> vizUuid;
    private int tabDrawerLastFullSize = 300;
    private boolean readOnly = false;
    private FluidContainer headerContainer;
    private FluidRow nameRow;
    private InlineLabel vizName;
    private InfoPanel fullScreenWindow;
    private CenterLayoutContainer spinnerContainer;


    public VizPanel(DirectedPresenter presenter) {
        this();
        this.directedPresenter = presenter;
    }

    public VizPanel() {
        controlsLayer = new SplitLayoutPanel(4) {

            @Override
            public void onResize() {
                super.onResize();

                Map<String, String> headerMap = new HashMap<String, String>();
                headerMap.put("tab", "resize");

                //   resizeTabs();
                new CsiEvent(headerMap, new HashMap<String, String>()).fire();
                //TODO:redraw canvas for broadcast venn diagram??
            }


        };
        broadcastListener = new Button();
        broadcastListener.setIcon(IconType.HEADPHONES);
        broadcastListener.setTitle(BROADCAST_TOOLTIP);
        broadcastListener.setSize(ButtonSize.SMALL);
        broadcastListener.setVisible(false);
        broadcastListener.addStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_HEADER_BUTTON);
        broadcastListener.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getVisualization().setBroadcastListener(false);
                getVisualization().getChrome().getMenu().unCheckedMenuItem(MenuKey.LISTEN_FOR_BROADCAST);
                disableBroadcastListener();
            }
        });


        initWidget(uiBinder.createAndBindUi(this));
        dockLayoutPanel.getElement().setId("vizChrome");
        controlsLayer.setWidgetToggleDisplayAllowed(tabpanelWrapper, true);
        tabpanelWrapper.getElement().getStyle().setOverflow(Overflow.VISIBLE);
        tabpanel.addStyleName("viz-tab-drawer");

        // controlsLayer.setWidgetMinSize(tabpanelWrapper, 0);

        menuNavBar = new Navbar();
        menuNavBar.addStyleName("visualizationMenu");
        csiMenuNav = new CsiMenuNav();
        menuNavBar.add(csiMenuNav);

        tabpanelWrapper.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                resizeButtonGroupContainer();
            }
        });
        createSendTo();
        hideSendTo();

    }

//    private void resizeTabs() {
//        Scheduler.get().scheduleDeferred(new ScheduledCommand(){
//
//            @Override
//            public void execute() {
//                for(TabLink link: tabpanel.getLinks()){
//                    if(tabpanel.getOffsetWidth() <= 615){
//                        link.setText("");
//                    }
//                }
//            }});
//
//    }

    public VizPanel(WorksheetPresenter worksheet) {
        this();
        this.worksheet = worksheet;
    }

    public WorksheetPresenter getWorksheet() {
        return worksheet;
    }

    @Override
    public LayoutPanel getMainLP() {
        return mainLP;
    }

    @Override
    public SplitLayoutPanel getControlsLayer() {
        return controlsLayer;
    }

    public void resizeButtonGroupContainer() {
        buttonGroupContainer.forceLayout();
        abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 25, abso.getOffsetHeight() / 2 - buttonGroupContainer.getOffsetHeight() / 2);
    }

    @UiHandler("restoreTabDrawerPanelButton")
    void restoreTabDrawer(ClickEvent event) {
        if (isHidden) {
            if (tabDrawerLastFullSize > this.getOffsetHeight() && this.getOffsetHeight() > 200) {
                //We force this to not be greater than 80% of the total height
                controlsLayer.setWidgetSize(tabpanelWrapper, (int) (this.getOffsetHeight() * TABPANEL_RESIZE_FACTOR));
            } else {
                controlsLayer.setWidgetSize(tabpanelWrapper, tabDrawerLastFullSize);
            }
            controlsLayer.animate(300);
        }
        isHidden = !isHidden;
    }

    @UiHandler("minimizeTabDrawerPanelButton")
    void minimizeTabDrawer(ClickEvent event) {
        if (isHidden == false) {
            tabDrawerLastFullSize = tabpanel.getOffsetHeight();
            controlsLayer.setWidgetSize(tabpanelWrapper, 30);
            controlsLayer.animate(300);
        }
        isHidden = !isHidden;
    }

    private void addTabNameUpDownActionOnClick(final Tab tab) {
        tab.asTabLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isHidden) {
                    restoreTabDrawer(null);
                } else if (tabpanel.getSelectedTab() == tabpanel.getLinks().indexOf(tab.asTabLink())) {
                    minimizeTabDrawer(null);
                }
            }
        });
    }

    public void enablePopoutButton(boolean isEnabel) {

        popoutTabDrawerPanelButton.setEnabled(isEnabel);
    }

    @UiHandler("popoutTabDrawerPanelButton")
    void onPopoutTabDrawer(ClickEvent event) {
        int offsetHeight = tabPanelLayoutPanel.getOffsetHeight();
        int offsetWidth = tabPanelLayoutPanel.getOffsetWidth();
        int selectedTab = tabpanel.getSelectedTab();
        floatingTabWindow = new Window() {

            @Override
            public void onResize() {
                // no-op
            }
        };
        floatingTabWindow.add(tabPanelLayoutPanel);
        floatingTabWindow.setWidth(offsetWidth);
        floatingTabWindow.setHeight(offsetHeight);
        floatingTabWindow.setHeading(visualization.getVisualizationDef().getName());
        if (VizPanel.this.worksheet != null) {
            VizPanel.this.worksheet.add(floatingTabWindow);
        } else {
            VizPanel.this.directedPresenter.add(floatingTabWindow);
        }
        floatingTabWindow.show();
        floatingTabWindow.getDraggable().setUpdateZIndex(true);
        floatingTabWindow.setMinWidth(350);
        floatingTabWindow.setHeight(275);
        controlsLayer.setWidgetHidden(tabpanelWrapper, true);
        controlsLayer.forceLayout();
        popoutTabDrawerPanelButton.setVisible(false);
        restoreTabDrawerPanelButton.setVisible(false);
        minimizeTabDrawerPanelButton.setVisible(false);
        floatingTabWindow.addBeforeHideHandler(new BeforeHideHandler() {

            @Override
            public void onBeforeHide(BeforeHideEvent event) {
                int selectedTab = tabpanel.getSelectedTab();
                tabpanelWrapper.add(tabPanelLayoutPanel);
                // tabpanelWrapper.setVisible(true);
                controlsLayer.setWidgetHidden(tabpanelWrapper, false);
                controlsLayer.setWidgetSize(tabpanelWrapper, 30);
                isHidden = true;
                // NOTE: need to clear these values so it will go back to taking 100%
                tabPanelLayoutPanel.getElement().getStyle().setProperty("height", null);
                tabPanelLayoutPanel.getElement().getStyle().setProperty("width", null);
                // controlsLayer.insertSouth(tabpanelWrapper, 30, null);
                controlsLayer.forceLayout();
                popoutTabDrawerPanelButton.setVisible(true);
                restoreTabDrawerPanelButton.setVisible(true);
                minimizeTabDrawerPanelButton.setVisible(true);
                tabDrawerVisible = false;
                tabpanel.selectTab(selectedTab);
                tabpanel.selectTab(selectedTab);
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 25, abso.getOffsetHeight() / 2 - buttonGroupContainer.getOffsetHeight() / 2);
                    }
                });
            }
        });

        floatingTabWindow.getDraggable().setContainer(getViewWidget());
        abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 25, 0);
        tabpanel.selectTab(selectedTab);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 25, abso.getOffsetHeight() / 2 - buttonGroupContainer.getOffsetHeight() / 2);
            }
        });
    }

    public Widget getViewWidget() {
        if (this.worksheet == null) {
            return directedPresenter.getView();
        } else {
            return worksheet.getView().asWidget();
        }
    }

    public void setFrameProvider(VizPanelFrameProvider frameProvider, String visualizationName) {
        this.frameProvider = frameProvider;
        frameProvider.addButton(broadcastListener);
        headerContainer = new FluidContainer();
        {
            Style style = headerContainer.getElement().getStyle();
            style.setHeight(16, Unit.PX);
            style.setMargin(0, Unit.PX);
        }
        {
            nameRow = new FluidRow();
            headerContainer.add(nameRow);

            vizName = new InlineLabel(visualizationName);
            nameRow.add(vizName);
            Style nameRowStyle = nameRow.getElement().getStyle();
            nameRowStyle.setHeight(15, Unit.PX);
            vizName.getElement().getStyle().setLineHeight(10, Unit.PX);
            vizName.getElement().getStyle().setMarginLeft(-15.0, Unit.PX);
            vizName.getElement().getStyle().setFontSize(12.0, Unit.PX);

            RootPanel.get().add(menuNavBar);
            hideMenu();

            MouseOutHandler mouseOutHandler = event -> {
                closeAfter = new Date().getTime() + MENU_HIDE_DELAY;
                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {
                        if (!csiMenuNav.isDropdownVisible()) {
                            if (!(frameProvider).isInFocus()) {
                                if (closeAfter < new Date().getTime() + 20) {
                                    csiMenuNav.hideDropdown();
                                    nameRow.getElement().getStyle().setDisplay(Style.Display.INITIAL);
                                    vizName.getElement().getStyle().setLineHeight(10, Unit.PX);
                                    if (frameProvider instanceof VisualizationWindow && ((VisualizationWindow) frameProvider).isFullScreen()) {
                                        vizName.getElement().getStyle().setLineHeight(16, Unit.PX);
                                    }
                                    hideMenu();
                                }
                            }
                        }
                        return false;
                    }
                }, MENU_HIDE_DELAY);
            };


            menuNavBar.addBitlessDomHandler(mouseOutHandler, MouseOutEvent.getType());
            headerContainer.addBitlessDomHandler(mouseOutHandler, MouseOutEvent.getType());

            MouseDownHandler mouseDownHandler = event -> {
                handleMenuMouseDown(frameProvider);
            };
            if (frameProvider instanceof VisualizationWindow) {
                ((VisualizationWindow) frameProvider).addBitlessDomHandler(mouseDownHandler, MouseDownEvent.getType());
            } else if (frameProvider instanceof DirectedWindow) {
                ((DirectedWindow) frameProvider).addBitlessDomHandler(mouseDownHandler, MouseDownEvent.getType());
            }

            menuNavBar.addBitlessDomHandler(event -> closeAfter = new Date().getTime() + MENU_HIDE_DELAY, MouseOverEvent.getType());

            MouseOverHandler mouseOverHandler = event -> {
                if (!readOnly) {
                    showMenu();
                    String vizZIndex = "";
                    if (frameProvider instanceof VisualizationWindow) {
                        Style style = menuNavBar.getElement().getStyle();
                        vizZIndex = ((VisualizationWindow) frameProvider).getElement().getStyle().getZIndex();
                        style.setZIndex(Integer.parseInt(vizZIndex));
                        if (((VisualizationWindow) frameProvider).isFullScreen()) {
                            style.setZIndex(XDOM.getTopZIndex());
                            menuNavBar.getElement().getFirstChildElement().getStyle().setProperty("minHeight", "15px");
                            menuNavBar.getElement().getFirstChildElement().getStyle().setHeight(15, Unit.PX);
                            style.setTop(0, Unit.PX);

                        }
                    } else if (frameProvider instanceof DirectedWindow) {
                        Style style = menuNavBar.getElement().getStyle();
                        style.setZIndex(XDOM.getTopZIndex());
                    }


                }
            };

            csiMenuNav.addBitlessDomHandler(mouseOverHandler, MouseOverEvent.getType());
            headerContainer.addBitlessDomHandler(mouseOverHandler, MouseOverEvent.getType());
            menuNavBar.addBitlessDomHandler(mouseOverHandler, MouseOverEvent.getType());

        }
        frameProvider.addButton(headerContainer);
        headerContainer.getElement().getParentElement().getStyle().setWidth(20000, Unit.PX);
    }

    public void handleMenuMouseDown(VizPanelFrameProvider frameProvider) {
        if (activeFrameProvider != null && activeFrameProvider instanceof VisualizationWindow) {
            ((VisualizationWindow) activeFrameProvider).getVisualizationPanel().hideMenu();
            ((VisualizationWindow) activeFrameProvider).getVisualizationPanel().showNameRow();
            activeFrameProvider.setInFocus(false);
        } else if (activeFrameProvider != null && activeFrameProvider instanceof DirectedWindow) {
            ((DirectedWindow) activeFrameProvider).getVisualizationPanel().hideMenu();
            ((DirectedWindow) activeFrameProvider).getVisualizationPanel().showNameRow();
            activeFrameProvider.setInFocus(false);
        }
        showMenu();
        if (frameProvider instanceof VisualizationWindow) {
            ((VisualizationWindow) frameProvider).bringToFrontIfNotActive();
            frameProvider.setInFocus(true);
            if (((VisualizationWindow) frameProvider).isFullScreen()) {
                menuNavBar.getElement().getStyle().setTop(0, Unit.PX);
            }
        } else if (frameProvider instanceof DirectedWindow) {
            ((DirectedWindow) frameProvider).bringToFrontIfNotActive();
            frameProvider.setInFocus(true);
        }
        this.activeFrameProvider = frameProvider;
    }

    public Navbar getNavBar() {
        return menuNavBar;
    }

    public InlineLabel getVizName() {
        return vizName;
    }

    public FluidContainer getHeaderContainer() {
        return headerContainer;
    }

    public void showMenu() {
        menuNavBar.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        nameRow.getElement().getStyle().setDisplay(Style.Display.NONE);
        Style style = menuNavBar.getElement().getStyle();
        style.setDisplay(Style.Display.INITIAL);
        style.setLeft(headerContainer.getAbsoluteLeft(), Unit.PX);
        style.setTop(headerContainer.getAbsoluteTop() - 5, Unit.PX);
        style.setPosition(Style.Position.ABSOLUTE);
        style.setHeight(12, Unit.PX);

        int headerClientWidth = getHeaderContainer().getElement().getClientWidth();
        csiMenuNav.setHeaderTotalOffsetWidth(headerClientWidth);
        csiMenuNav.onResize();

        menuNavBar.getElement().getFirstChildElement().getStyle().setPadding(0, Unit.PX);
    }

    public void hideMenu() {
        menuNavBar.getElement().getStyle().setDisplay(Style.Display.NONE);
    }

    public void showNameRow() {
        nameRow.getElement().getStyle().setDisplay(Style.Display.INITIAL);
    }

    private void setupFullscreenHandler() {
        mainLP.getElement().setId("main-layout-panel-" + vizUuid);

        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("viz", "fullscreenEvent");
        headerMap.put("vizId", vizUuid.get());
        CsiEventHeader header = new CsiEventHeader(headerMap);
        CsiEventCommander.getInstance().addHandler(header, new CsiEventHandler() {

            @Override
            public void onCsiEvent(CsiEvent event) {
                Map<String, String> payload = event.getPayload();
                String enabled = payload.get("enabled");
                isFullscreen = "true".equals(enabled);
            }
        });
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public void addTab(final Tab tab) {
        tabpanel.add(tab);
        addTabNameUpDownActionOnClick(tab);
    }

    @Override
    public void addFullScreenWindow(String message, IconType iconType) {
        removeFullScreenWindow();
        fullScreenWindow = new InfoPanel(message, iconType);
        fullScreenWindow.setHeight("100%");
        fullScreenWindow.setWidth("100%");
        //fullScreenWindow.setWidth(this.getOffsetWidth());
        //fullScreenWindow.setHeight(this.getOffsetHeight());
        mainLP.insert(fullScreenWindow, mainLP.getWidgetIndex(sendToPanel));
    }

    @Override
    public void removeFullScreenWindow() {
        if (fullScreenWindow != null) {
            fullScreenWindow.setVisible(false);
            mainLP.remove(fullScreenWindow);
            fullScreenWindow.removeFromParent();
            fullScreenWindow = null;
        }
    }

    @Override
    public void showLoadingSpinner() {
        if (spinnerContainer == null) {
            com.github.gwtbootstrap.client.ui.Icon spinnerIcon = new com.github.gwtbootstrap.client.ui.Icon(IconType.SPINNER);
            spinnerIcon.setIconSize(IconSize.FOUR_TIMES);
            spinnerIcon.setSpin(true);
            spinnerIcon.addStyleName("csi-icon-spinner"); //$NON-NLS-1$
            spinnerIcon.getElement().getStyle().setProperty("margin", "auto");


            spinnerContainer = new CenterLayoutContainer();
            spinnerContainer.setVisible(true);//
            spinnerContainer.add(spinnerIcon);
            spinnerContainer.setWidth("50px");
//            layoutPanel.add(spinnerIcon);
            spinnerContainer.getElement().getStyle().setBackgroundColor("#fffff");
            spinnerContainer.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
            spinnerContainer.getElement().getStyle().setTextAlign(Style.TextAlign.CENTER);

            mainLP.add(spinnerContainer);
        }

    }

    @Override
    public void hideLoadingSpinner() {
        if (spinnerContainer != null) {
            mainLP.remove(spinnerContainer);
            spinnerContainer.setVisible(false);
            spinnerContainer.removeFromParent();
            spinnerContainer = null;
        }
    }


    @Override
    public Draggable addWindowAndReturnDraggable(final ContentPanel display) {
        display.setHeight(DEFAULT_WINDOW_SIZE);
        display.setWidth(DEFAULT_WINDOW_SIZE);
        display.addStyleName("overlay-clear");
        display.setBodyStyle("pad-text");
        display.setCollapsible(true);
        abso.add(display);
        // Create Draggable around Window
        final Draggable d = new Draggable(display, display.getHeader());
        d.setUseProxy(false);
        d.setContainer(abso);
        new Resizable(display);
        return d;
    }

    @Override
    public void addWindow(final ContentPanel display) {
        addWindowAndReturnDraggable(display);
    }

    @Override
    public CsiMenuNav getMenu() {
        return csiMenuNav;
    }

    @Override
    public Visualization getVisualization() {
        return visualization;
    }

    @Override
    public void setVisualization(Visualization v) {
        checkNotNull(v);
        visualization = v;
        vizUuid = Optional.of(v.getUuid());
        // adopt
        v.setLimitedMenu(directedPresenter != null);
        v.setChrome(this);
        setupFullscreenHandler();
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        hideMenu();
    }

    @Override
    public void removeVisualization() {
        visualization.removeFromParent();
        visualization = null;
        vizUuid = Optional.absent();
        mainPanel.clear();
        eventBus = new SimpleEventBus();
    }

    @Override
    public VizMessageArea getMessageArea() {
        return messageArea;
    }

    @Override
    public void selectTab(AbstractCsiTab nodesTab) {
        int widgetIndex = tabpanel.getWidgetIndex(nodesTab);
        tabpanel.selectTab(widgetIndex);
    }

    @Override
    public void setWidget(IsWidget w) {
        checkNotNull(w);
        mainPanel.clear();
        mainPanel.setWidget(w);
    }

    @Override
    public void addButton(IsWidget w) {
        if (w instanceof Tooltip) {
            Tooltip t = (Tooltip) w;
            t.getWidget().addStyleName("rightControlButton");
            buttonGroup.add(w.asWidget());
            buttonGroupContainer.setStyleName("rightControlButtonBar");
        } else if (w instanceof Button) {
            w.asWidget().addStyleName("rightControlButton");
            buttonGroup.add(w);
            buttonGroupContainer.setStyleName("rightControlButtonBar");
        } else if (w instanceof ButtonGroup) {
            ((ButtonGroup) w).setVertical(true);
//            ((ButtonGroup) w).getElement().getStyle().setPaddingTop(2, Unit.PX);
            w.asWidget().addStyleName("dragTypeButtonGroup");
            buttonGroupContainer.add(w);
            buttonGroupContainer.setStyleName("rightControlButtonBar");
        }
        resizeButtonGroupContainer();
    }

    @Override
    public void removeTab(AbstractCsiTab tab) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeWindow(ContentPanel panel) {
        abso.remove(panel);
    }

    @Override
    public void removeButton(Widget widget) {
        // TODO Auto-generated method stub

    }

    public void adjustWidgets() {

        RenderSize renderSize = RenderSize.forSize(asWidget().getOffsetWidth());
        if (readOnly || (renderSize != currentRenderSize)) {
            if (tabDrawerVisible) {
                controlsLayer.asWidget().setVisible(renderSize.showButtons());
            }

            csiMenuNav.setVisible(!(readOnly || renderSize == RenderSize.MICRO));
//            menuNavBar.setWidgetHidden(menuNavBar, (readOnly || renderSize == RenderSize.MICRO));
            getEventBus().fireEvent(new RenderSizeChangeEvent(renderSize));
        }
        getEventBus().fireEvent(new ChromeResizeEvent(asWidget().getOffsetWidth(), asWidget().getOffsetHeight()));
        buttonGroupContainer.forceLayout();
        abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 25, abso.getOffsetHeight() / 2 - buttonGroupContainer.getOffsetHeight() / 2);
        getMenu().onResize();

        if (tabpanelWrapper.getOffsetHeight() > this.getOffsetHeight() && this.getOffsetHeight() > 200) {
            //We force this to not be greater than 80% of the total height
            controlsLayer.setWidgetSize(tabpanelWrapper, (int) (this.getOffsetHeight() * TABPANEL_RESIZE_FACTOR));
        }
        getEventBus().fireEvent(new VizPanelResizeEvent());
        if (tabpanel.getLinks().isEmpty() == false) {
            if (tabpanel.getSelectedTab() == -1) {
                tabpanel.selectTab(0);
            }
            tabpanel.onResize();
        }

        sendToPanel.onResize();

    }

    @Override
    public void onResize() {
        super.onResize();
        RenderSize renderSize = RenderSize.forSize(asWidget().getOffsetWidth());
        if (readOnly || (renderSize != currentRenderSize)) {
            if (tabDrawerVisible) {
                controlsLayer.asWidget().setVisible(renderSize.showButtons());
            }

            csiMenuNav.setVisible(!(readOnly || renderSize == RenderSize.MICRO));
//            menuNavBar.setWidgetHidden(menuNavBar, (readOnly || renderSize == RenderSize.MICRO));
            currentRenderSize = renderSize;
            getEventBus().fireEvent(new RenderSizeChangeEvent(currentRenderSize));
        }
        getEventBus().fireEvent(new ChromeResizeEvent(asWidget().getOffsetWidth(), asWidget().getOffsetHeight()));
        buttonGroupContainer.forceLayout();
        abso.setWidgetPosition(buttonGroupContainer, abso.getOffsetWidth() - 25, abso.getOffsetHeight() / 2 - buttonGroupContainer.getOffsetHeight() / 2);
        getMenu().onResize();

        if (tabpanelWrapper.getOffsetHeight() > this.getOffsetHeight() && this.getOffsetHeight() > 200) {
            //We force this to not be greater than 80% of the total height
            controlsLayer.setWidgetSize(tabpanelWrapper, (int) (this.getOffsetHeight() * TABPANEL_RESIZE_FACTOR));
        }
        getEventBus().fireEvent(new VizPanelResizeEvent());
        if (tabpanel.getLinks().isEmpty() == false) {
            if (tabpanel.getSelectedTab() == -1) {
                tabpanel.selectTab(0);
            }
            tabpanel.onResize();
        }

        sendToPanel.onResize();
        if (visualization instanceof MapPresenter) {
            MapPresenter mapPresenter = (MapPresenter) this.visualization;
            if (mapPresenter.isMapNeedsLoad()) {
                if (mapPresenter.isLoadTrack())
                    mapPresenter.loadTrack();
                else if (mapPresenter.isReload2()) {
                    mapPresenter.reload2();
                } else {
                    mapPresenter.reload();
                }
            }
        }
    }

    @Override
    public void hideButtonGroupContainer() {
        buttonGroupContainer.hide();
    }

    @Override
    public int getButtonContainerHeight() {
        return buttonGroupContainer.getOffsetHeight();
    }

    @Override
    public void showButtonGroupContainer() {
        buttonGroupContainer.show();
    }

    public VBoxLayoutContainer getButtonGroupContainer() {
        return buttonGroupContainer;
    }

    @Override
    public void hideControlLayer() {
        controlsLayer.getElement().getParentElement().removeFromParent();
    }

    @Override
    public void setTabDrawerVisible(boolean visible) {
        tabDrawerVisible = visible;
        tabpanelWrapper.asWidget().setVisible(visible);
        controlsLayer.setWidgetHidden(tabpanelWrapper, !visible);
        //controlsLayer.asWidget().setVisible(visible);
    }

    @Override
    public void setName(String name) {
        if (vizName != null) {
            vizName.setText(name);
        } else {
            frameProvider.setName(name);
        }
    }

    @Override
    public void enableBroadcastListener() {
        if (!visualization.getVisualizationDef().isReadOnly()) {
            broadcastListener.setVisible(true);
        }
    }

    @Override
    public void disableBroadcastListener() {
        broadcastListener.setVisible(false);
    }

    @Override
    public SimpleLayoutPanel getControlBar() {
        return controlBar;
    }

    @Override
    public void bringFloatingTabDrawerToFront() {
        if (floatingTabWindow != null) {
            floatingTabWindow.setZIndex(XDOM.getTopZIndex());
        }
    }

    @Override
    public void removeFloatingTab() {
        if (floatingTabWindow != null) {
            floatingTabWindow.removeFromParent();
            floatingTabWindow.hide();
        }
    }

    public VizPanelFrameProvider getFrameProvider() {
        return frameProvider;
    }

    public DirectedPresenter getDirectedPresenter() {
        return directedPresenter;
    }

    public void setDirectedPresenter(DirectedPresenter directedPresenter) {
        this.directedPresenter = directedPresenter;
    }

    @Override
    public void addSearchBox(final Panel searchPanel) {

        mainLP.add(searchPanel);
        searchPanel.addStyleName("overlay-clear");
        searchPanel.addStyleName("searchBox");
        //searchPanel.getElement().getStyle().setRight(20, Unit.PX);
        //display.setBodyStyle("pad-text");
        //display.setCollapsible(true);
//        Scheduler.get().scheduleDeferred(new ScheduledCommand(){
//
//            @Override
//            public void execute() {
//                searchPanel.getElement().getStyle().setTop(0, Unit.PX);
//                searchPanel.getElement().getStyle().setRight(searchPanel.getOffsetWidth(), Unit.PX);
//            }});
    }

    @Override
    public void setControlLayerOpacity(double d) {
        controlsLayer.getElement().getStyle().setOpacity(d);
    }

    @Override
    public void sendTo(final BroadcastManager broadcastManager, final Visualization senderViz) {

        createSendTo();

        showSendTo(broadcastManager, senderViz);
    }

    private FluidContainer createVennDiagram(final BroadcastManager broadcastManager, final Visualization senderViz) {
        VennDiagramContainer fc = new VennDiagramContainer(broadcastManager, senderViz, getVisualization());

        return fc;
    }

    private void createSendTo() {
        //Hack so we can re-order the dom elements to show sendto on top
        if (fullScreenWindow != null) {
            removeSendTo();
            mainLP.remove(sendToPanel);
            sendToPanel = null;
        }
        if (sendToPanel == null) {
            sendToPanel = new SimpleLayoutPanel();
            sendToPanel.addStyleName("overlay-clear");
            sendToPanel.addStyleName("sendTo");
            dockLayoutPanel.addStyleName("overlay");


            mainLP.add(sendToPanel);
        }
    }

    public void displayInstruction(String text) {
        createInstructionPanel();
        instructionPanel.clear();
        InlineLabel label = new InlineLabel(text);
        instructionPanel.add(label);
    }

    public void hideInstructions() {
        if (instructionPanel != null) {

            instructionPanel.clear();

            instructionPanel.removeStyleName("sendTo");

            instructionPanel.setVisible(false);
        }
    }

    private void createInstructionPanel() {
        //Hack so we can re-order the dom elements to show sendto on top
        if (fullScreenWindow != null) {
            removeSendTo();
            mainLP.remove(instructionPanel);
            instructionPanel = null;
        }
        if (instructionPanel == null) {
            instructionPanel = new SimpleLayoutPanel();
            //instructionPanel.addStyleName("overlay-clear");
            instructionPanel.addStyleName("sendTo");
            //dockLayoutPanel.addStyleName("overlay");


            mainLP.add(sendToPanel);
        }
    }

    @Override
    public void removeSendTo() {
        hideSendTo();
        //sendToPanel = null;
    }

    private void hideSendTo() {
        if (sendToPanel != null) {

            sendToPanel.clear();

            sendToPanel.removeStyleName("overlay-clear");
            sendToPanel.removeStyleName("sendTo");
            dockLayoutPanel.removeStyleName("overlay");

            sendToPanel.setVisible(false);
        }
    }

    private void showSendTo(BroadcastManager broadcastManager, Visualization senderViz) {
        if (sendToPanel != null) {
            sendToPanel.clear();
            sendToPanel.add(createVennDiagram(broadcastManager, senderViz));
            sendToPanel.setVisible(true);
            sendToPanel.addStyleName("overlay-clear");
            sendToPanel.addStyleName("sendTo");
            dockLayoutPanel.addStyleName("overlay");
            sendToPanel.onResize();
        }
    }

    public void setReadOnly() {
        readOnly = true;
        onResize();
    }

    interface MyUiBinder extends UiBinder<Widget, VizPanel> {
    }

    public static abstract class ChromeResizeEventHandler {
        abstract public void onChromeResize();
    }

    public static class ChromeResizeEvent extends Event<ChromeResizeEventHandler> {
        public static final Type<ChromeResizeEventHandler> TYPE = new Type<ChromeResizeEventHandler>();
        private final int width;
        private final int height;

        public ChromeResizeEvent(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public Type<ChromeResizeEventHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ChromeResizeEventHandler handler) {
            handler.onChromeResize();
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }
    }

}
