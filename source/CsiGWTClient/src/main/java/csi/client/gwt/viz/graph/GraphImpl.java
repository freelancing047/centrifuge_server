package csi.client.gwt.viz.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.base.Strings;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.event.ActivateEvent;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.dataview.DataViewWidget;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.button.DefaultModeHandler;
import csi.client.gwt.viz.graph.button.FitToSizeHandler;
import csi.client.gwt.viz.graph.button.PanModeHandler;
import csi.client.gwt.viz.graph.button.SelectModeHandler;
import csi.client.gwt.viz.graph.button.TransparencyHandler;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.graph.button.ZoomInHandler;
import csi.client.gwt.viz.graph.button.ZoomModeHandler;
import csi.client.gwt.viz.graph.button.ZoomOutHandler;
import csi.client.gwt.viz.graph.controlbar.GraphControlBar;
import csi.client.gwt.viz.graph.controlbar.GraphControlBarImpl;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.menu.GraphMenuManager;
import csi.client.gwt.viz.graph.menu.ResetLegendHandler;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.GraphSurfaceImpl;
import csi.client.gwt.viz.graph.tab.GraphTab;
import csi.client.gwt.viz.graph.tab.link.LinksTabImpl;
import csi.client.gwt.viz.graph.tab.node.NodesTabImpl;
import csi.client.gwt.viz.graph.tab.path.PathTabView;
import csi.client.gwt.viz.graph.tab.pattern.PatternTab;
import csi.client.gwt.viz.graph.tab.pattern.PatternTabImpl;
import csi.client.gwt.viz.graph.tab.player.TimePlayer;
import csi.client.gwt.viz.graph.tab.statistics.StatisticsTab;
import csi.client.gwt.viz.graph.window.annotation.GraphAnnotation;
import csi.client.gwt.viz.graph.window.annotation.GraphAnnotationCallback;
import csi.client.gwt.viz.graph.window.legend.GraphLegend;
import csi.client.gwt.viz.graph.window.legend.GraphLegendImpl;
import csi.client.gwt.viz.graph.window.transparency.TransparencySettings;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.filter.FilterCapableVisualizationPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.viz.shared.menu.SelectionOnlyOnServer;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.InfoPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.client.gwt.worksheet.layout.window.VisualizationWindow;
import csi.client.gwt.worksheet.layout.window.WindowBase;
import csi.client.gwt.worksheet.layout.window.WindowLayout;
import csi.client.gwt.worksheet.layout.window.events.VisualizationBarSelectionEvent;
import csi.client.gwt.worksheet.layout.window.events.VisualizationBarSelectionEventHandler;
import csi.client.gwt.worksheet.tab.WorksheetTabPanel;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.worksheet.VisualizationLayoutState;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.server.common.service.api.ThemeActionsServiceProtocol;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.shared.core.imaging.ImagingRequest;

public class GraphImpl extends AbstractVisualizationPresenter<RelGraphViewDef, csi.client.gwt.viz.graph.surface.ViewImpl> implements Graph, FilterCapableVisualizationPresenter, SelectionOnlyOnServer {
    private static final int PROGRESS_BAR_DELAY_ON_LOAD = 2000;
    private GraphControlBar graphControlBar;
    private Button hiddenItemBtn;

    public static Graph create(VisualizationDef vizDef) {
        return new GraphImpl(vizDef);
    }

    public static Image getRenderedIcon(String iconId, ShapeType shape, int color, int size, double iconScale) {
        final Image image = new Image();
        getRenderedIcon(iconId, shape, color, size, iconScale, image);
        return image;
    }

    public static void getRenderedIcon(String iconId, ShapeType shape, int color, int size, double iconScale,
            final Image image) {
        final VortexFuture<String> futureTask = WebMain.injector.getVortex().createFuture();
        try {
            futureTask.execute(GraphActionServiceProtocol.class).getNodeAsImageNew(iconId, shape, color, size, iconScale);
        } catch (CentrifugeException e) {
            
        }
        futureTask.addEventHandler(new AbstractVortexEventHandler<String>() {

            @Override
            public void onSuccess(String result) {
                image.setUrl(result);
            }
            
            @Override
            public boolean onError(Throwable t){
                return true;
            }
        });
    }
    
    public static void getBundleIcon(int size, double iconScale,
            final Image image) {
        final VortexFuture<String> futureTask = WebMain.injector.getVortex().createFuture();
        try {
            futureTask.execute(GraphActionServiceProtocol.class).getBundleIcon(size, iconScale);
        } catch (CentrifugeException e) {
            
        }
        futureTask.addEventHandler(new AbstractVortexEventHandler<String>() {

            @Override
            public void onSuccess(String result) {
                image.setUrl(result);
            }
            
            @Override
            public boolean onError(Throwable t){
                return true;
            }
        });
    }

    private GraphActivityManager activityManager;
    private ResettableEventBus eventBus = new ResettableEventBus(new SimpleEventBus());
    private FitToSizeHandler fitToSizeHandler;
    private GraphLegend graphLegend;
    private GraphAnnotation graphAnnotation;
    private boolean firstAnnotationLoad = true;
    private GraphSurface graphSurface;
    private LinksTabImpl linksTab;
    private Model model;
    private NodesTabImpl nodesTab;
    private PatternTab patternTab;
    private PanModeHandler panModeHandler;
    private DefaultModeHandler defaultModeHandler;
    private PathTabView pathTab;
    private Presenter presenter;
    private StatisticsTab statisticsTab;
    private SelectModeHandler selectModeHandler;
    private TimePlayer timePlayerTab;
    private VizButtonHandler zoomInHandler;
    private ZoomModeHandler zoomModeHandler;
    private VizButtonHandler zoomOutHandler;

    private String vizUuid;

    private boolean loaded = false;
    private TransparencySettings transparencySettings;
    private VizButtonHandler transparencyHandler;

    private Alert progressIndicator;
    private boolean hideProgressRequested;
    private NullableRepeatingCommand command;

    private int totalTabsOffset = 0;
    private boolean isLoadedOnce = false;

    public GraphImpl(VisualizationDef vizDef) {
        super((RelGraphViewDef) vizDef);
        theme = null;
        setVizUuid(vizDef.getUuid());
        model = new GraphProxy(this, vizDef);
        // tabs
        nodesTab = new NodesTabImpl(this);
        linksTab = new LinksTabImpl(this);
        pathTab = new PathTabView(this);

        if(!readOnly) {
            patternTab = new PatternTabImpl(this);
        }
        // searchTab = new SearchTab(this);
        statisticsTab = new StatisticsTab(this);
        if(!readOnly) {
            timePlayerTab = new TimePlayer(this);
        }
        // widget
        graphSurface = GraphSurfaceImpl.create(this);
        // windows
        graphLegend = GraphLegendImpl.create(this, ((RelGraphViewDef) vizDef).getState());

        transparencySettings = new TransparencySettings(this);
        // buttons
        zoomInHandler = new ZoomInHandler(this);
        zoomOutHandler = new ZoomOutHandler(this);
        fitToSizeHandler = new FitToSizeHandler(this);
        panModeHandler = new PanModeHandler(this);
        defaultModeHandler = new DefaultModeHandler(this);
        zoomModeHandler = new ZoomModeHandler(this);
        selectModeHandler = new SelectModeHandler(this);
        GraphActivityMapper activityMapper = new GraphActivityMapper(this);
        activityManager = new GraphActivityManager(activityMapper, eventBus, this);

        if(!readOnly) {
            graphControlBar = new GraphControlBarImpl(this);
            showControlBar(false);
        }
        
        updateTheme();
    }

    @Override
    public void addGraphEventHandler(GraphEvents type, GraphEventHandler handler) {
        handler.addSubType(type);
        eventBus.addHandler(GraphEvent.TYPE, handler);
    }

    @Override
    public <V extends Visualization> AbstractMenuManager<V> createMenuManager() {
        return (AbstractMenuManager<V>) new GraphMenuManager(this);
    }

    @Override
    public csi.client.gwt.viz.graph.surface.ViewImpl createView() {
        return (csi.client.gwt.viz.graph.surface.ViewImpl) graphSurface.getView();
    }

    @Override
    public void fireEvent(GraphEvents event) {
        eventBus.fireEvent(new GraphEvent(this, event));
    }

    @Override
    public GraphActivityManager getActivityManager() {
        return activityManager;
    }

    @Override
    public FillStrokeStyle getBackgroundColor() {
        return model.getBackgroundColor();
    }

    @Override
    public VizChrome getChrome() {
        return chrome;
    }

    @Override
    public AbstractDataViewPresenter getDataview() {
        return DataViewRegistry.getInstance().dataViewPresenterForVisualization(getUuid());
    }

    @Override
    public String getDataviewUuid() {
        return DataViewRegistry.getInstance().dataViewPresenterForVisualization(getUuid()).getUuid();
    }

    @Override
    public FitToSizeHandler getFitToSizeHandler() {
        return fitToSizeHandler;
    }

    @Override
    public GraphLegend getGraphLegend() {
        return graphLegend;
    }

    @Override
    public GraphSurface getGraphSurface() {
        return graphSurface;
    }

    @Override
    public ImagingRequest getImagingRequest() {
        return null;
    }

    @Override
    public GraphLegend getLegend() {
        return graphLegend.getLegend();
    }

    @Override
    public Tab getLinksTab() {
        return linksTab.getTab();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public <T> void refreshTabs(VortexFuture<T> vortexFuture) {
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<T>() {
            @Override
            public void onSuccess(T result) {
                nodesTab.refresh();
                linksTab.refresh();
                //NOTE: current strategy is to only clear results on graph load.
                //patternTab.refresh();
            }
        });
    }

    @Override
    public Tab getNodesTab() {
        return nodesTab.getTab();
    }

    @Override
    public PanModeHandler getPanModeHandler() {
        return panModeHandler;
    }

    @Override
    public Tab getPathTab() {
        return pathTab.getTab();
    }

    @Override
    public GraphPlayerSettings getPlayerSettings() {
        return model.getPlayerSettings();
    }

    @Override
    public SelectModeHandler getSelectModeHandler() {
        return selectModeHandler;
    }

    public Tab getStatisticsTab() {
        return statisticsTab.getTab();
    }

    @Override
    public Tab getPatternTab() {
        return patternTab.getTab();
    }

    @Override
    public Map<String, String> getVisItems() {

        return graphLegend.getVisItems();
    }

    @Override
    public void setHiddenItemIndicator(Boolean hiddenItems) {
        hiddenItemBtn.getElement().getStyle().setOpacity(hiddenItems ? 1:0);
    }

    @Override
    public Tab getTimePlayerTab() {
        return timePlayerTab.getTab();
    }

    @Override
    public VisualizationType getType() {
        return VisualizationType.RELGRAPH_V2;
    }

    @Override
    public String getUuid() {
        return vizUuid;
    }

    @Override
    public Visualization getVisualization() {
        return this;
    }

    @Override
    public VizButtonHandler getZoomInHandler() {
        return zoomInHandler;
    }

    @Override
    public ZoomModeHandler getZoomModeHandler() {
        return zoomModeHandler;
    }

    @Override
    public VizButtonHandler getZoomOutHandler() {
        return zoomOutHandler;
    }

    @Override
    public boolean hasBundleSpecification() {
        List<BundleDef> bundleDefs = model.getBundleDefs();
        if (bundleDefs == null) {
            return false;
        }
        if (bundleDefs.size() < 1) {
            return false;
        }
        BundleDef bundleDef = bundleDefs.get(0);
        if (bundleDef == null) {
            return false;
        }
        List<BundleOp> operations = bundleDef.getOperations();
        if (operations == null) {
            return false;
        }
        if (operations.size() < 1) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSelection() {
        return true;
    }

    @Override
    public void hideLegend() {
        graphLegend.hide();
        getMenuManager().hide(MenuKey.HIDE_LEGEND);
        getMenuManager().enable(MenuKey.SHOW_LEGEND);

    }

    @Override
    public boolean isBroadcastListener() {
        return model.getRelGraphViewDef().isBroadcastListener();
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }
    
    @Override
    public boolean isLoadedOnce() {
        return isLoadedOnce;
    }

    @Override
    public Widget getGraphControlBar() {
        return graphControlBar.asWidget();
    }

    public GraphControlBar getGraphControlBarAsGraphControlBar(){ return graphControlBar; }


    @Override
    public void load() {
    	if (isViewLoaded()) {
	        // TODO: this should be another presenter/action.
	        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
	        try {
	            vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        isLoading = false;
                        updateTheme();
                    }
                    
                    @Override
                    public boolean onError(Throwable t){
                        isLoading = false;
                        return isLoading;
                    }
                });
	            
	            double modelHeight = graphSurface.asWidget().getOffsetHeight();
	            double modelWidth = graphSurface.asWidget().getOffsetWidth();
	            
	            AbstractDataViewPresenter dataviewPresenter = getDataview();
	            if(dataviewPresenter instanceof DataViewPresenter) {
	                if(modelHeight < VizPanel.DEFAULT_WINDOW_SIZE || modelWidth < VizPanel.DEFAULT_WINDOW_SIZE) {
    	                VisualizationLayoutState state = ((DataViewPresenter)dataviewPresenter).getActiveWorksheet().getWorksheet().getWorksheetScreenLayout().getLayout().getLayoutState(visualizationDef);
    	                modelHeight = state.getHeight();
    	                modelWidth = state.getWidth();
	                }
	            }
	            

                modelHeight = modelHeight < VizPanel.DEFAULT_WINDOW_SIZE ? 400 : modelHeight;
                modelWidth = modelWidth < VizPanel.DEFAULT_WINDOW_SIZE ? 400 : modelWidth ;
	            vortexFuture.execute(GraphActionServiceProtocol.class).loadGraph(getUuid(), getDataviewUuid(),
	                    (int) modelWidth, (int) modelHeight);
	        } catch (CentrifugeException e) {
	            isLoading = false;
	        }
            //custom handle load without calling super.. hence why we need weird filter lbl mng
	        handleLoad(vortexFuture);
    	} else {
    		handleViewLoad();
    	}
    }

    private void handleLoad(final VortexFuture<Void> vortexFuture) {
        theme = null;
        showProgressIndicator(vortexFuture, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				progressIndicator.setText("Cancelling");
				vortexFuture.cancel(new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						isLoading = false;
					}

					@Override
					public void onSuccess(Void result) {
						// TODO Auto-generated method stub
						isLoading = false;
					}
		    		
		    	});
			}
        });
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
            private DataViewPresenter dataViewPresenter;
            private WorksheetPresenter worksheet;
            private HandlerRegistration handlerRegistration;

            @Override
            public void onSuccess(Void result) {
                AbstractDataViewPresenter abstractDataViewPresenter = getDataview();
                if (abstractDataViewPresenter instanceof DataViewPresenter) {
                    dataViewPresenter = (DataViewPresenter) abstractDataViewPresenter;
                    VizPanel vizPanel = (VizPanel) getChrome();
                    worksheet = vizPanel.getWorksheet();
                    String parentWorksheetName = worksheet.getName();
                    String activeWorksheetName = dataViewPresenter.getActiveWorksheet().getName();
                    if (parentWorksheetName.equals(activeWorksheetName)) {
                        setLoaded(true);
                        hideProgressIndicator();
                        if(hasOldSelection())
                            applySelection(popOldSelection());
                    } else {
                        DataViewWidget dataViewWidget = (DataViewWidget) dataViewPresenter.getView();
                        if(handlerRegistration != null) {
                            handlerRegistration.removeHandler();
                        }
                        handlerRegistration = dataViewWidget.addSelectionHandler(new SelectionHandler() {

                            @Override
                            public void onSelection(SelectionEvent event) {
                                WorksheetTabPanel source = (WorksheetTabPanel) event.getSource();
                                WindowLayout selectedItem = (WindowLayout) event.getSelectedItem();
                                String parentWorksheetName = worksheet.getName();
                                String activeWorksheetName = selectedItem.getName();
                                if (parentWorksheetName.equals(activeWorksheetName)) {
                                    hideProgressIndicator();
                                    handlerRegistration.removeHandler();
                                    load();
                                }

                                if(hasOldSelection())
                                    applySelection(popOldSelection());
                            }

                        });
                    }
                } else {
                    setLoaded(true);
                    if(hasOldSelection())
                        applySelection(popOldSelection());
                    hideProgressIndicator();
                    
                }

				createFilterLabel();
                createHiddenItemIndicator();
				handleViewLoad2();
            }

            @Override
            public void onUpdate(int taskProgess, String taskMessage) {
                String message = taskMessage + "\n" + taskProgess;
                if (progressIndicator != null) {
                	setProgressIndicatorText(message);
                }
            }

            @Override
            public boolean onError(Throwable t) {
                try{
                    isLoading = false;
                    loaded = false;
                    hideProgressIndicator();

                    if(t.getMessage().equals("TOO_MANY_TYPES")){

                        showInfoPanel(_constants.graph_tooManyTypes());
                    }else {
                        handleViewLoad2();
                        displayLoadingError(t);
                    }
                }catch(Exception exception){}
                return true;
            }
            
            
            @Override 
            public void onCancel() {
            	if (handlerRegistration != null) {
            		handlerRegistration.removeHandler();
            	}
				respondToCancel();
            }
        });
    }

    private void createHiddenItemIndicator() {
        if (hiddenItemBtn == null) {
            VizPanel v = ((VizPanel) getChrome());
            WindowBase frameProvider = (WindowBase) v.getFrameProvider();
            if (frameProvider instanceof VisualizationWindow) {
                hiddenItemBtn = new Button();
                hiddenItemBtn.addStyleName("vizWindowHeaderButton");
                hiddenItemBtn.setSize(ButtonSize.SMALL);
                hiddenItemBtn.getElement().getStyle().setProperty("background", "none");
                hiddenItemBtn.setEnabled(false);
                hiddenItemBtn.setIcon(IconType.EYE_CLOSE);
                hiddenItemBtn.getElement().getStyle().setOpacity(0);
                hiddenItemBtn.setTitle(_constants.hiddenItemIndicator());
                frameProvider.getHeader().insertTool(hiddenItemBtn, 1);
            }
        }
    }
    @Override
	public void respondToCancel() {
    	hideProgressIndicator();
		revertToUnloaded();
		setLoaded(false);
    }
    
    @Override
    protected void revertToUnloaded() {
        super.revertToUnloaded();
        isLoading = false;
    	getGraphSurface().getToolTipManager().removeAllToolTips();
		graphLegend.hide();
        graphAnnotation.setForcedClosed(true);
        graphAnnotation.hide();
//		showFilterLabel(false);
        if (chrome != null) {
            chrome.setTabDrawerVisible(false);
            chrome.hideButtonGroupContainer();
        }
        getMenuManager().hideMenus(limitedMenu);
        csi.client.gwt.viz.graph.surface.ViewImpl view = (csi.client.gwt.viz.graph.surface.ViewImpl) graphSurface.getView();
        view.drawBackground();
        view.clearMainCanvas();
        view.clearForeground();
        removeFromParent();
    }
    

    @Override
    public void hideProgressIndicator() {

        hideProgressRequested = true;
        if (progressIndicator != null) {
            if (progressIndicator.isAttached()) {
                getChrome().getMainLP().remove(progressIndicator);
                progressIndicator.close();
            }
            progressIndicator = null;
        }
    }

    @Override
    public void showProgressIndicator(VortexFuture vortexFuture, ClickHandler cancelHandler) {
        hideProgressRequested = false;

        if (command != null) {
            //invalidates the command
            command.setRepeat(false);
            command = null;
        }
        //track previous command
        command = new NullableRepeatingCommand(this, vortexFuture, cancelHandler);

        Scheduler.get().scheduleFixedPeriod(command, PROGRESS_BAR_DELAY_ON_LOAD);
    }

    public void createProgressIndicator(final VortexFuture vortexFuture, final ClickHandler cancelHandler) {
        ProgressBar progressBar;
        if (progressIndicator == null) {
            progressIndicator = new Alert(_constants.progressBar_loading(), AlertType.INFO);
            getChrome().getMainLP().add(progressIndicator);
            progressBar = new ProgressBar(ProgressBarBase.Style.ANIMATED);
            progressIndicator.setClose(false);
            progressBar.setPercent(100);
            if (cancelHandler != null) {
	            FlexTable ft = new FlexTable();            
            	progressBar.getElement().getStyle().setProperty("margin", "0 auto");
            	ft.setWidget(0, 0, progressBar);
            	ft.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
            	Button cancelButton = new Button("Cancel");
                cancelButton.getElement().getStyle().setProperty("margin", "0 auto");
                cancelButton.getElement().getStyle().setProperty("pointerEvents", "all");
                cancelButton.addClickHandler(cancelHandler);
                ft.setWidget(1, 0, cancelButton);
                ft.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
            	ft.setWidth("100%");
            	progressIndicator.add(ft);
            	progressIndicator.setHeight("65px");//NON-NLS
            } else {
	            progressIndicator.add(progressBar);
    	        //FIXME: set using style name
        	    progressIndicator.setHeight("50px");//NON-NLS
			}

            //Checking if this changed during creation
            if (hideProgressRequested) {
                hideProgressIndicator();
            }
        }
    }

    @Override
    public void setProgressIndicatorText(String message) {
        if (progressIndicator != null) {
        	progressIndicator.setText(message);
        }
    }
    
    @Override
    public void loadVisualization() {
        hideInfoPanel();
        try{
            this.appendNotificationText(NotificationLabel.FILTER, getVisualizationDef().getFilter() != null);
            appendBroadcastIcon();
            if(getVisualizationDef().getLayout() != null && getModel() != null){
                getModel().checkLayout(getVisualizationDef().getLayout());
            }
            if (getGraphSurface().asWidget().isAttached()) {
                load();
            } else {
                getGraphSurface().asWidget().addAttachHandler(new AttachEvent.Handler() {
                    @Override
                    public void onAttachOrDetach(AttachEvent event) {
                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                if (!isLoaded()) {
                                    load();
                                } else {
                                    isLoading = false;
                                }
                            }
                        });
                    }
                });
            }

        } catch(Exception exception){
            isLoading = false;
        }
    }

    @Override
    public void removeFromParent() {
        getGraphSurface().asWidget().removeFromParent();
    }

    @Override
    public VortexFuture<Void> saveSettings(boolean refreshOnSuccess) {

        this.appendNotificationText(NotificationLabel.FILTER, getVisualizationDef().getFilter() != null);
        appendBroadcastIcon();
//        if ( getVisualizationDef().getFilter() != null) {
//            showFilterLabel(true);
//        } else {
//            showFilterLabel(false);
//        }
        //for the graph saving the settings is probably not neccessary, but we may want to save state.
        //FIXME: need to spend time to think about what to save when...
        return model.save();
    }

    @Override
    public void saveViewStateToVisualizationDef() {
        model.setCurrentSelectionAsOldSelection();
    }


    @Override
    public void setBroadcastListener(boolean listener) {
        model.getRelGraphViewDef().setBroadcastListener(listener);
        model.saveSettings(false);
    }

    @Override
    public void applySelection(Selection selection) {
        if(getGraphSurface() != null && model != null)
            getGraphSurface().refresh(model.setShadowSelection());
    }

    @Override
    public void setChrome(VizChrome _chrome) {
        checkNotNull(_chrome);
        super.setChrome(_chrome);
        chrome.setTabDrawerVisible(true);
    }
    
    @Override
    protected void handleViewLoad() {
	    viewLoaded = true;
        getMenuManager().registerMenus(limitedMenu);
    	hideLoadPanel();
	    chrome.setWidget(getView());
        csi.client.gwt.viz.graph.surface.ViewImpl view = (csi.client.gwt.viz.graph.surface.ViewImpl) graphSurface.getView();
        view.drawBackground();
        view.clearMainCanvas();
        view.clearForeground();
        chrome.setControlLayerOpacity(1);


        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                loadVisualization();
            }
        });
    }
    
    private boolean handleViewLoad2HandlerAdded = false;
    protected boolean isLoading = false;
    private RepeatingCommand nextLoadCommand;
    private GraphTheme theme;

    private InfoPanel noData;

    @Override
    protected void handleViewLoad2() {
    	if (viewLoaded) {
        	if (chrome != null) {
                adjustButtons();
                //            graphSurface.refresh();
                chrome.setName(model.getName());
                if (!handleViewLoad2HandlerAdded) {
                    activityManager.setDisplay(chrome);
                    if (((VizPanel) chrome).getWorksheet() != null) {
                        ((VizPanel) chrome).getWorksheet().getView().getEventBus().addHandler(VisualizationBarSelectionEvent.type, new VisualizationBarSelectionEventHandler() {
                            @Override
                            public void onSelect(Visualization visualization) {
                                if (visualization.getUuid().equals(getUuid())) {
                                    getChrome().bringFloatingTabDrawerToFront();
                                }
                            }
                        });
                    } else {
                        ((VizPanel) chrome).getDirectedPresenter().getEventBus().addHandler(VisualizationBarSelectionEvent.type, new VisualizationBarSelectionEventHandler() {
                            @Override
                            public void onSelect(Visualization visualization) {
                                if (visualization.getUuid().equals(getUuid())) {
                                    getChrome().bringFloatingTabDrawerToFront();
                                }
                            }
                        });
                    }
                    ((WindowBase) ((VizPanel) chrome).getFrameProvider()).addActivateHandler(new ActivateEvent.ActivateHandler<WindowBase>() {
                        @Override
                        public void onActivate(ActivateEvent<WindowBase> event) {
                            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    getChrome().bringFloatingTabDrawerToFront();
                                }
                            });

                        }
                    });
                	handleViewLoad2HandlerAdded = true;
                }

               	chrome.setTabDrawerVisible(true);
               	chrome.showButtonGroupContainer();
//               	getChrome().bringFloatingTabDrawerToFront();
//                showControlBar(false);

        		totalTabsOffset = getNodesTab().asWidget().getOffsetWidth();
        		totalTabsOffset++;
        		totalTabsOffset += getLinksTab().asWidget().getOffsetWidth();
        		totalTabsOffset++;
        		totalTabsOffset += getPathTab().asWidget().getOffsetWidth();
        		totalTabsOffset++;
        		if(!readOnly) {
                    totalTabsOffset += getTimePlayerTab().asWidget().getOffsetWidth();
                    totalTabsOffset++;
                }
        		totalTabsOffset += getStatisticsTab().asWidget().getOffsetWidth();
        		totalTabsOffset++;
                if(!readOnly) {
                    totalTabsOffset += getPatternTab().asWidget().getOffsetWidth();
                    totalTabsOffset++;
                }
        		totalTabsOffset += 75;
        	}


            hideIfEmpty();

        }
    }

    private void adjustButtons() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                chrome.adjustWidgets();
            }
        });
    }

    @Override
    public void hideIfEmpty() {
        VortexFuture<Boolean> vortexFuture = WebMain.injector.getVortex().createFuture();
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if(result) {
                    showInfoPanel(CentrifugeConstantsLocator.get().mapViewNoData());
                }else{
                    hideInfoPanel();
                }
            }

            @Override
            public boolean onError(Throwable t){
                return true;
            }
        });

        vortexFuture.execute(GraphActionServiceProtocol.class).isEmpty(graphSurface.getVizUuid());
    }

    private void hideInfoPanel(){
        graphSurface.getView().asWidget().getElement().getStyle().setProperty("pointerEvents",  "auto");
        VizChrome chrome = getChrome();
        ensureLegendIsDisplayed();
        if(chrome != null) {
            chrome.removeFullScreenWindow();
            chrome.showButtonGroupContainer();
            chrome.setTabDrawerVisible(true);
        }
    }

    private void showInfoPanel(String msg){
        graphSurface.getView().asWidget().getElement().getStyle().setProperty("pointerEvents",  "none");
        getChrome().addFullScreenWindow(msg , IconType.INFO_SIGN);
        hideLegend();
        getChrome().hideButtonGroupContainer();
        getChrome().setTabDrawerVisible(false);
    }


    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
        // FIXME: should be handler on graph load event.
        if (loaded) {
        	isLoadedOnce = true;
            graphLegend.load();
            eventBus.fireEvent(new GraphEvent(this, GraphEvents.GRAPH_LOAD_COMPLETE));
            //TODO: tooltip manager should be able to respond to the event fired.
            getGraphSurface().getToolTipManager().removeAllToolTips();
            getChrome().setName(getName());
            ensureLegendIsDisplayed();
        }
    }

    private void ensureLegendIsDisplayed() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                new ResetLegendHandler(GraphImpl.this, null).onMenuEvent(null);
                showAnnotationIfPopulated();
            }
        });
    }

    @Override
    public void setPlayerSettings(GraphPlayerSettings playerSettings) {
        model.setPlayerSettings(playerSettings);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setVizUuid(String vizUuid) {
        checkArgument(!Strings.isNullOrEmpty(vizUuid));
        this.vizUuid = vizUuid;
    }

    @Override
    public void showLegend() {
        graphLegend.show();
        getMenuManager().hide(MenuKey.SHOW_LEGEND);
        getMenuManager().enable(MenuKey.HIDE_LEGEND);
    }

    @Override
    public void showTransparencyWindow() {
        transparencySettings.show();
    }

    @Override
    public VizButtonHandler getTransparencyHandler() {
        if (transparencyHandler == null) {
            transparencyHandler = new TransparencyHandler(this);
        }
        return transparencyHandler;
    }

    @Override
    public TransparencySettings getTransparency() {
        return transparencySettings;
    }

    @Override
    public RelGraphViewDef getVisualizationDef() {
        return getModel().getRelGraphViewDef();
    }

    @Override
    public void setVisualizationDef(RelGraphViewDef visualizationDef) {
        model = new GraphProxy(this, visualizationDef);
    }

    public void setNextLoadCommand(RepeatingCommand command){
        this.nextLoadCommand = command;
    }
    @Override
    public void reload() {
        
        
        this.nextLoadCommand = new RepeatingCommand() {

            @Override
            public boolean execute() {
                if(nextLoadCommand != this){
                    return false;
                }
                if(isLoading){
                    
                    return true;
                }

                isLoading = true;
                //Kill this command if another is waiting
                
                
                try {
                    VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {

                            @Override
                            public void onSuccess(Void result) {
                                if (isViewLoaded()) {
                                    loadVisualization();
                                } else {
                                    isLoading = false;
                                }
                            }
                            
                            @Override
                            public boolean onError(Throwable t){
                                isLoading = false;
                                //Dialog.showException(t);
                                return true;
                            }
                        });
                        
                    vortexFuture.execute(VisualizationActionsServiceProtocol.class).saveSettings(getVisualizationDef(), getDataviewUuid(), true);

                } catch (Exception myException) {
                    isLoading = false;
                    Dialog.showException(myException);
                }
                return false;
            }
        };
        
        Scheduler.get().scheduleFixedPeriod(nextLoadCommand, 100);

    }

    @Override
    public TimePlayer getTimePlayer() {
        return timePlayerTab;
    }

    @Override
    public void showControlBar(boolean b) {
        if(graphControlBar != null) {
            if (b) {
                graphControlBar.getView().getHbox().getElement().getStyle().setTop(0, Style.Unit.PX);
                graphControlBar.getView().getElement().getStyle().setOpacity(1);
                graphControlBar.getView().getElement().getStyle().clearZIndex();
            } else {
                graphControlBar.getView().getHbox().getElement().getStyle().setTop(110, Style.Unit.PX);
                graphControlBar.getView().getElement().getStyle().setOpacity(0);
                graphControlBar.getView().getElement().getStyle().setZIndex(-1);
            }
        }
    }

    @Override
    public void showNodesTabSnaColumns() {
        nodesTab.showSnaColumns();
    }

    @Override
    public void broadcastNotify(String text) {
        ((csi.client.gwt.viz.graph.surface.ViewImpl) graphSurface.getView()).broadcastNotify(text);
        appendBroadcastIcon();
    }

    public GraphAnnotation getGraphAnnotation() {
        return graphAnnotation;
    }

    public void setGraphAnnotation(GraphAnnotation graphAnnotation) {
        this.graphAnnotation = graphAnnotation;
    }

    @Override
    public void hideAnnotation() {
        graphAnnotation.setForcedClosed(true);
        graphAnnotation.hide();
        //getMenuManager().disable(MenuKey.HIDE_ANNOTATION);
        getMenuManager().hide(MenuKey.HIDE_ANNOTATION);
        getMenuManager().enable(MenuKey.SHOW_ANNOTATION);
    }

    public void showAnnotationIfPopulated() {
        if (graphAnnotation == null) {
            graphAnnotation = GraphAnnotation.create(this);
            chrome.addWindow(graphAnnotation.getDisplay());
            graphAnnotation.addLoadCallback(createAnnotationCallback());
            graphAnnotation.load();
        }

    }

    private GraphAnnotationCallback createAnnotationCallback() {
        return new GraphAnnotationCallback() {


            @Override
            public void callback() {
                if (firstAnnotationLoad && (graphAnnotation.getHtml() == null || !graphAnnotation.getHtml().isEmpty())) {
                    firstAnnotationLoad = false;
                    graphAnnotation.hide();

                    getMenuManager().enable(MenuKey.SHOW_ANNOTATION);
                    getMenuManager().hide(MenuKey.HIDE_ANNOTATION);
                } else if (graphAnnotation.isForcedClosed()) {
                    graphAnnotation.hide();

                    getMenuManager().enable(MenuKey.SHOW_ANNOTATION);
                    getMenuManager().hide(MenuKey.HIDE_ANNOTATION);
                } else {
                    graphAnnotation.show();
                    getMenuManager().hide(MenuKey.SHOW_ANNOTATION);
                    getMenuManager().enable(MenuKey.HIDE_ANNOTATION);
                }
            }
        };
    }

    @Override
    public void showAnnotation() {
        showAnnotationIfPopulated();

        graphAnnotation.setForcedClosed(false);
        graphAnnotation.show();
        //getMenuManager().disable(MenuKey.SHOW_ANNOTATION);
        getMenuManager().hide(MenuKey.SHOW_ANNOTATION);
        getMenuManager().enable(MenuKey.HIDE_ANNOTATION);
    }

    @Override
    public DefaultModeHandler getDefaultModeHandler() {
        return defaultModeHandler;
    }

    public boolean isHideProgressRequested() {
        return hideProgressRequested;
    }

    public void setHideProgressRequested(boolean hideProgressRequested) {
        this.hideProgressRequested = hideProgressRequested;
    }

    @Override
    public void readjustAfterResize() {
    	if (totalTabsOffset > 0) {
        	if (getGraphSurface().asWidget().getOffsetWidth() < totalTabsOffset) {
        		hideTabTitle(getNodesTab());
        		hideTabTitle(getLinksTab());
        		hideTabTitle(getPathTab());
        		hideTabTitle(getTimePlayerTab());
        		hideTabTitle(getStatisticsTab());
        		hideTabTitle(getPatternTab());
        	} else {
        		showTabTitle(getNodesTab());
        		showTabTitle(getLinksTab());
        		showTabTitle(getPathTab());
        		showTabTitle(getTimePlayerTab());
        		showTabTitle(getStatisticsTab());
        		showTabTitle(getPatternTab());
        	}
    	}
    }

	private void hideTabTitle(Tab tab) {
		if (tab instanceof GraphTab) {
			GraphTab graphTab = (GraphTab)tab;
			graphTab.hideTabTitle();
		}
	}

	private void showTabTitle(Tab tab) {
		if (tab instanceof GraphTab) {
			GraphTab graphTab = (GraphTab)tab;
			graphTab.showTabTitle();
		}
	}

    @Override
    public void setReadOnly() {

        super.setReadOnly();
        graphSurface.setReadOnly();
    }

    private Map<String, NodeStyle> styleMap = new HashMap<String, NodeStyle>();
    
    @Override
    public NodeStyle findNodeStyle(String typeName) {
        if(theme == null){
            return null;
        }
        
        if(styleMap.isEmpty()){
            if(theme.getNodeStyles() != null){
                for(NodeStyle nodeStyle: theme.getNodeStyles()){
                    if(nodeStyle.getFieldNames() != null){
                        for(String field: nodeStyle.getFieldNames()){
                            styleMap.put(field, nodeStyle);
                        }
                    }
                }
            }
        }
        
        return styleMap.get(typeName);
        
    }
    
    public void clearStyles(){
        styleMap.clear();
    }

    @Override
    public void setTheme(GraphTheme result) {
        this.theme = result;
    }

    @Override
    public GraphTheme getTheme() {
        return this.theme;
    }

    private void updateTheme() {
        VortexFuture<GraphTheme> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {
            @Override
            public void onSuccess(GraphTheme result) {
                setTheme(result);
                clearStyles();
            }
        });

        future.execute(ThemeActionsServiceProtocol.class).findGraphTheme(getModel().getTheme());
    }



    @Override
    public void delete() {
        super.delete();
        getChrome().removeFloatingTab();
    }

    @Override
    public void refresh() {
        if(graphSurface != null && graphLegend != null && graphLegend.asWidget().isAttached()) {
            //graphSurface.refresh();
        }
    }


}
