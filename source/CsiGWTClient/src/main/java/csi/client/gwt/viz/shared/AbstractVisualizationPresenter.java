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
package csi.client.gwt.viz.shared;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.base.Objects;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.etc.AbstractInfrastructureAwarePresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.viz.shared.chrome.panel.*;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.worksheet.layout.window.VisualizationWindow;
import csi.client.gwt.worksheet.layout.window.WindowBase;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;

import java.util.HashMap;

/**
 * @author Centrifuge Systems, Inc.
 */
public abstract class AbstractVisualizationPresenter<D extends VisualizationDef, W extends IsWidget> extends
        AbstractInfrastructureAwarePresenter implements Visualization {

    protected AbstractDataViewPresenter dvPresenter;
    protected D visualizationDef;
    protected VizChrome chrome;
    protected CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    protected boolean viewLoaded = false;
    protected boolean limitedMenu;
    protected boolean readOnly;
    // keeps track of the state of the icon in the right corner for the notification labels.
    private HashMap<NotificationLabel, Boolean> labelMap = new HashMap<>();
    private Button filterLabel = new Button();
    private AbstractMenuManager<? super Visualization> menuManager;
    private HandlerRegistration renderSizeHandlerRegistration;
    private HandlerRegistration resizeHandlerRegistration;
    private LayoutPanel loadPanel;
    private FlexTable loadTable;
    private W view;
    private Selection oldSelection;


    public AbstractVisualizationPresenter(AbstractDataViewPresenter dvPresenterIn, D visualizationDef) {
        this(visualizationDef);
        dvPresenter = dvPresenterIn;
        readOnly = visualizationDef.isReadOnly();
    }

    public AbstractVisualizationPresenter(D visualizationDef) {
        setupLabelButton(filterLabel, IconType.FILTER);

        this.visualizationDef = visualizationDef;
        readOnly = visualizationDef.isReadOnly();
//        this.setupNotificationLabel();
    }

    @Override
    public VisualizationType getType() {
        return getVisualizationDef().getType();
    }

    @Override
    public VizChrome getChrome() {
        return chrome;
    }

    @Override
    public void setChrome(VizChrome vizChrome) {
        chrome = vizChrome;
        chrome.setTabDrawerVisible(false);
        chrome.setControlLayerOpacity(0);
        chrome.setName(getName());

        setMenuManager(createMenuManager());
        getMenuManager().registerPreloadMenus(limitedMenu);

        addSizeChangeHandler();
        addResizeHandler();

        if (visualizationDef.isSuppressLoadAtStartup()) {
            showLoadPanel();
            viewLoaded = false;
        } else {
            handleViewLoad();
        }
        if (readOnly) {

            DeferredCommand.add(this::setReadOnly);
        }
    }

    @Override
    public void setLimitedMenu(boolean limitedMenu) {
        this.limitedMenu = limitedMenu;
    }

    public abstract <V extends Visualization> AbstractMenuManager<V> createMenuManager();

    public AbstractMenuManager<? super Visualization> getMenuManager() {
        return menuManager;
    }

    public void setMenuManager(AbstractMenuManager<? super Visualization> menuManager) {
        this.menuManager = menuManager;
    }

    private void addSizeChangeHandler() {
        if (renderSizeHandlerRegistration != null) {
            renderSizeHandlerRegistration.removeHandler();
        }
        renderSizeHandlerRegistration = chrome.getEventBus().addHandler(RenderSizeChangeEvent.type,
                new RenderSizeChangeEventHandler() {
                    @Override
                    public void onAttach(RenderSizeChangeEvent event) {
                        onRenderSizeChange(event.getRenderSize());
                    }
                }
        );
    }

    protected void addResizeHandler() {
        if (resizeHandlerRegistration != null) {
            resizeHandlerRegistration.removeHandler();
        }
        resizeHandlerRegistration = chrome.getEventBus().addHandler(VizPanelResizeEvent.type,
                new VizPanelResizeEventHandler() {
                    @Override
                    public void onResize(VizPanelResizeEvent event) {
                        if (loadPanel != null) {
                            positionVizNotLoadedMessage();
                        } else {
                            readjustAfterResize();
                        }
                    }
                }
        );
    }

    protected void onRenderSizeChange(RenderSize renderSize) {
        // noop
    }

    private void positionVizNotLoadedMessage() {
        int loadPanelHeight = loadPanel.getOffsetHeight();
        int ftHeight = loadTable.getOffsetHeight();
        int topPosition = (loadPanelHeight - ftHeight) / 2;

        loadPanel.setWidgetTopBottom(loadTable, topPosition, Unit.PX, topPosition, Unit.PX);

        int loadPanelWidth = loadPanel.getOffsetWidth();
        int ftWidth = loadTable.getOffsetWidth();
        int leftPosition = (loadPanelWidth - ftWidth) / 2;

        loadPanel.setWidgetLeftRight(loadTable, leftPosition, Unit.PX, leftPosition, Unit.PX);
    }

    public void readjustAfterResize() {
    }

    private void showLoadPanel() {
        loadPanel = new LayoutPanel();
        loadTable = new FlexTable();
        loadTable.setBorderWidth(0);
        if (visualizationDef.isBroadcastListener()) {
            getChrome().enableBroadcastListener();
        }
//        String listening = visualizationDef.isBroadcastListener() ? " Listening" : " Not listening";
        Label loadLabel = new Label(_constants.absVizPresenter_vizNotLoaded());


        loadLabel.setWidth("248px");
        loadTable.setWidget(0, 0, loadLabel);
        Button loadButton = new Button(_constants.absVizPresenter_loadViz());
        loadButton.addClickHandler(event -> handleViewLoad());
        loadTable.setWidget(1, 0, loadButton);
        loadTable.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
        loadTable.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
        loadPanel.add(loadTable);
        chrome.setWidget(loadPanel);

        getChrome().setTabDrawerVisible(false);

        Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
            private int count = 0;

            @Override
            public boolean execute() {

                if (viewLoaded) {
                    return false;
                }
                positionVizNotLoadedMessage();
                count++;
                //shouldn't need this, but good safety check
                return count > 20;
            }


        }, 200);
    }

    protected void handleViewLoad() {
        viewLoaded = true;
        getMenuManager().registerMenus(limitedMenu);
        hideLoadPanel();
        chrome.setWidget(getView());
        chrome.setControlLayerOpacity(1);
        appendBroadcastIcon();
        Scheduler.get().scheduleDeferred(() -> loadVisualizationAndCreateFilterLabel());
    }

    protected void loadVisualizationAndCreateFilterLabel() {
        loadVisualization();
        createFilterLabel();
        handleViewLoad2();
    }

    protected void handleViewLoad2() {
    }

    protected void revertToUnloaded() {
        viewLoaded = false;
        showLoadPanel();
//	    chrome.setWidget(getView());
//        chrome.setControlLayerOpacity(1);
//
//		Timer timer = new Timer() {
//			public void run() {
//				loadVisualization();
//				createFilterLabel();
//				handleViewLoad2();
//			}
//		};
//		timer.schedule(200);
    }

    protected void hideLoadPanel() {
        if (loadPanel != null) {
            if (loadPanel.isAttached()) {
                loadPanel.removeFromParent();
                loadPanel = null;
            }
        }
    }


    public W getView() {
        if (view == null) {
            view = createView();
        }
        return view;
    }

    public void setView(W view) {
        this.view = view;
    }

    /**
     * @return Subclasses should create the widget that represents the visualization's view and return it here.
     */
    public abstract W createView();

    @Override
    public String getUuid() {
        return getVisualizationDef().getUuid();
    }

    @Override
    public boolean isBroadcastListener() {
        return getVisualizationDef().isBroadcastListener();
    }

    @Override
    public void setBroadcastListener(boolean listener) {
        getVisualizationDef().setBroadcastListener(listener);
        saveSettings(true);
    }

    @Override
    public VortexFuture<Void> saveSettings(final boolean refreshOnSuccess) {
        return saveSettings(refreshOnSuccess, false);
    }

    @Override
    public void removeFromParent() {
        getView().asWidget().removeFromParent();
    }

    @Override
    public void delete() {
        if (getChrome() instanceof VizPanel) {
            VizPanel v = ((VizPanel) getChrome());
            if (v.getFrameProvider() instanceof VisualizationWindow) {
                VisualizationWindow frameProvider = (VisualizationWindow) v.getFrameProvider();
                if (frameProvider.isFullScreen()) {
                    frameProvider.restoreFullScreen();
                }

                v.getWorksheet().delete(this.getVisualizationDef());
            }
        }
        WebMain.injector.getEventBus().fireEvent(new DeleteVisualizationEvent(getUuid()));
    }

    @Override
    public D getVisualizationDef() {
        return visualizationDef;
    }

    public void setVisualizationDef(D visualizationDef) {
        this.visualizationDef = visualizationDef;
    }

    @Override
    public boolean isImagingCapable() {
        return true;
    }

    @Override
    public void load() {
        WebMain.injector.getVortex().execute((Callback<D>) result -> {
            visualizationDef = result;
            handleViewLoadOrLoadVisualization();
        }, VisualizationActionsServiceProtocol.class).getVisualization(dvPresenter.getUuid(), getUuid());
    }

    protected void handleViewLoadOrLoadVisualization() {
        if (!viewLoaded) {
            handleViewLoad();
        } else {
            loadVisualizationAndCreateFilterLabel();
        }
    }

    @Override
    public void reload() {
        WebMain.injector.getVortex().execute((Callback<D>) result -> {
            visualizationDef = result;
            if (visualizationDef.isSuppressLoadAtStartup()) {
                if (viewLoaded) {
                    loadVisualizationAndCreateFilterLabel();
                }
            } else {
                handleViewLoadOrLoadVisualization();
            }
            appendBroadcastIcon();
        }, VisualizationActionsServiceProtocol.class).getVisualization(dvPresenter.getUuid(), getUuid());
    }

    @Override
    public VortexFuture<Void> saveSettings(final boolean refreshOnSuccess, final boolean isStructural) {
        return saveSettings(refreshOnSuccess, isStructural, true);
    }

    @Override
    public VortexFuture<Void> saveSettings(final boolean refreshOnSuccess, final boolean isStructural, final boolean clearTransient) {
        saveViewStateToVisualizationDef();
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(VisualizationActionsServiceProtocol.class).saveSettings(getVisualizationDef(), getDataViewUuid(), isStructural, clearTransient);
        } catch (Exception myException) {
            Dialog.showException(myException);
        }
        VortexEventHandler<Void> loadViz = new AbstractVortexEventHandler<Void>() {

            @Override
            public boolean onError(Throwable myException) {

                Dialog.showException(myException);
                return true;
            }

            @Override
            public void onSuccess(Void v) {
                if (refreshOnSuccess) {
                    if (visualizationDef.isSuppressLoadAtStartup() && viewLoaded) {
                        loadVisualizationAndCreateFilterLabel();
                    } else {
                        handleViewLoadOrLoadVisualization();
                    }

                    getChrome().setName(getName());
                }

                appendFilterIcon();
                appendBroadcastIcon();
            }

        };
        vortexFuture.addEventHandler(loadViz);


        return vortexFuture;
    }

    @Override
    public String getDataViewUuid() {
        return DataViewRegistry.getInstance().dataViewPresenterForVisualization(getUuid()).getUuid();
    }

    @Override
    public boolean isViewLoaded() {
        return viewLoaded;
    }

    @Override
    public void saveOldSelection(Selection selection) {
        oldSelection = selection;
    }

    public boolean hasOldSelection() {
        return oldSelection != null;
    }

    public Selection popOldSelection() {
        Selection returnSelection = this.oldSelection;
        this.oldSelection = null;
        return returnSelection;
    }

    @Override
    public String getName() {
        return getVisualizationDef().getName();
    }

    @Override
    public void setName(String newName) {
        getVisualizationDef().setName(newName);
        chrome.setName(getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof Visualization)) {
            return false;
        } else {
            Visualization typed = (Visualization) obj;
            return Objects.equal(this.getUuid(), typed.getUuid());
        }
    }

    public void displayLoadingError(Throwable myException) {
        String myMessage = myException.getMessage();
        int myLocation = (null != myMessage) ? myMessage.indexOf(_constants.absVizPresenter_sqlErrorText()) : -1;

        if (0 <= myLocation) {
            String myTitle = _constants.visualizationLoadingError_TitleSuffix(getName());
            String myDisplay = myMessage.substring(myLocation) + _constants.visualizationLoadingError_suggestion();

            Dialog.showError(myTitle, myDisplay);
        } else {
            Dialog.showError(myMessage);
        }
    }

    public DataView getDataView() {
        return DataViewRegistry.getInstance().dataViewPresenterForVisualization(getUuid()).getDataView();
    }

    public DataModelDef getDataModel() {
        return (null != dvPresenter) ? dvPresenter.getDataView().getMeta().getModelDef() : null;
    }

    public void setReadOnly() {

        VizChrome myChrome = getChrome();
        readOnly = true;

        if ((null != myChrome) && (getChrome() instanceof VizPanel)) {

            ((VizPanel) myChrome).setReadOnly();
        }
    }


    /*
     *  NOTIFICATION ICON
     */

    /**
     * creates the filter label and adds it to the proper panel
     */
    protected void createFilterLabel() {
        VizPanel v = ((VizPanel) getChrome());
        WindowBase frameProvider = (WindowBase) v.getFrameProvider();
        if (frameProvider instanceof VisualizationWindow) {
            filterLabel.addStyleName("vizWindowHeaderButton");
            filterLabel.setSize(ButtonSize.SMALL);
            filterLabel.getElement().getStyle().setProperty("background", "none");
//        filterLabel.getElement().getStyle().setColor("#333");
            filterLabel.setEnabled(false);
            filterLabel.getElement().getStyle().setOpacity(1);
            frameProvider.getHeader().insertTool(filterLabel, 1);
            validateVisibility();
        }
    }

    /**
     * Sets up the icon, size, type, and disables the button.
     *
     * @param btn  -
     * @param icon - IconType representing the icon for this button
     */
    private void setupLabelButton(Button btn, IconType icon) {
        btn.setIcon(icon);
        btn.setSize(ButtonSize.SMALL);
//        btn.setType(ButtonType.LINK);
//        btn.setEnabled(false);
        btn.setToggle(false);
        // map of what notification is turned on.
        initLabelMap();
    }

    /**
     * init the states for the types of the different labels.
     */
    private void initLabelMap() {
        labelMap.put(NotificationLabel.SELECTION, false);
        labelMap.put(NotificationLabel.FILTER, false);
        labelMap.put(NotificationLabel.BROADCAST, false);
    }

    /**
     * @param lbl
     * @param visible
     */
    protected void appendNotificationText(NotificationLabel lbl, boolean visible) {
        // check if we still need this label, so we don't get dupes
        if (isLabelNeeded(lbl)) {
            labelMap.put(lbl, visible);
            if (visible) {
                updateFilterLabelTooltip(lbl);
            }
        } else {
            //remove the label text for this element
            if (!visible) {
                labelMap.put(lbl, false);
                removeFilterLabelTooltipText(lbl);
            }
        }

        validateVisibility();
    }

    /**
     * If any of the notification labels are live, display.
     */
    private void validateVisibility() {
        if (labelMap.get(NotificationLabel.SELECTION) || labelMap.get(NotificationLabel.FILTER) || labelMap.get(NotificationLabel.BROADCAST)) {
            showFilterLabel(true);
        } else {
            showFilterLabel(false);
        }
    }

    /**
     * Removes part of the notification text, this is used to removed items that were in the tooltip that are no longer true.
     */
    private void removeFilterLabelTooltipText(NotificationLabel lbl) {
        String existingTip = filterLabel.getTitle();
        filterLabel.setTitle(existingTip.replace("\n" + lbl.getText(), ""));
    }

    /**
     * adds the notification label to the tooltip
     */
    private void updateFilterLabelTooltip(NotificationLabel lbl) {
        String existingTip = filterLabel.getTitle();
        existingTip += "\n" + lbl.getText();
        filterLabel.setTitle(existingTip);
    }

    private boolean isLabelNeeded(NotificationLabel lbl) {
        return !labelMap.get(lbl);
    }

    protected void showFilterLabel(boolean show) {
        filterLabel.setVisible(show);
    }

    private void appendFilterIcon() {
        if (visualizationDef.getFilter() != null) {
            showFilterLabel(true);
        } else {
            showFilterLabel(false);
        }
    }

    /**
     * checks if the viz has a broadcast in order to display accurate label for it.
     */
    protected void appendBroadcastIcon() {
        DataViewRegistry.getInstance().dataViewPresenterForVisualization(getUuid()).getBroadcastManager().isBroadcast(this, result -> appendNotificationText(NotificationLabel.BROADCAST, result));
    }

    @Override
    public void clearBroadcastNotification() {
        appendNotificationText(NotificationLabel.BROADCAST, false);
    }

    /******** END NOTIFICATION ICON STUFF **********/


    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Houses the different strings used for different notifications
     */
    public enum NotificationLabel {

        SELECTION(CentrifugeConstantsLocator.get().absVizPresenter_filterClauseTooltipText()),
        FILTER(CentrifugeConstantsLocator.get().absVizPresenter_filterTooltipText()),
        BROADCAST(CentrifugeConstantsLocator.get().absVizPresenter_broadcastTooltipText());

        private String lblText;

        NotificationLabel(String lbl) {
            lblText = lbl;
        }

        public String getText() {
            return lblText;
        }

    }
}
