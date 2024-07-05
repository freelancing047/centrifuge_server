package csi.client.gwt.viz.graph.surface;

import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import csi.client.gwt.events.CsiEvent;
import csi.client.gwt.util.BasicPlace;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.surface.menu.ContextMenuPresenter;
import csi.client.gwt.viz.graph.surface.menu.ShowContextMenu;
import csi.client.gwt.viz.graph.surface.multitypes.MultiTypePresenter;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTipManager;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.shared.core.visualization.graph.GraphLayout;
import csi.shared.gwt.viz.graph.MultiTypeInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphSurfaceImpl implements GraphSurface {

    private CSIActivityManager activityManager;
    private ClickMode clickMode = ClickMode.NONE;
    private DragMode dragMode = DragMode.PAN;
    private DragMode explicitDragMode = DragMode.DEFAULT;
    private Graph graph;
    private View graphSurface;
    private KeyHandler keyHandler;
    private Model model;
    private MouseHandler mouseHandler;
    private Presenter presenter;
    @SuppressWarnings("rawtypes")
    private VortexEventHandler refreshLater = new AbstractVortexEventHandler() {

        @SuppressWarnings("unused")
        @Override
        public void onSuccess(Object result) {
            refresh();
            graph.hideProgressIndicator();
        }

        @SuppressWarnings("unused")
        @Override
        public boolean onError(Throwable t) {
            refresh();
            return true;
        }
    };
    private ContextMenuPresenter contextMenuPresenter;
    private ToolTipManager toolTipManager;


    private GraphSurfaceImpl(Graph graph) {
        this.graph = graph;
        mouseHandler = new MouseHandler(this);
        // contextMenuHandler = new GraphSurfaceContextMenuHandler(this);
        keyHandler = new KeyHandler(this);
        EventBus eventBus = new SimpleEventBus();
        graphSurface = new ViewImpl(this);
        PlaceController placeController = new PlaceController(eventBus);
        GraphSurfaceActivityMapper activityMapper = new GraphSurfaceActivityMapper(this);
        activityManager = new CSIActivityManager(activityMapper, eventBus);
        placeController.goTo(BasicPlace.DEFAULT_PLACE);
        //NON-NLS
        //NON-NLS
        GraphEventHandler onLoadCallback = new GraphEventHandler() {

            @SuppressWarnings("unused")
            @Override
            public void onGraphEvent(GraphEvent event) {
                refresh();
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("graph", "loaded");//NON-NLS
                headerMap.put("vizId", getVizUuid());//NON-NLS
                new CsiEvent(headerMap, new HashMap<>()).fire();
            }
        };
        graph.addGraphEventHandler(GraphEvents.GRAPH_LOAD_COMPLETE, onLoadCallback);
        contextMenuPresenter = new ShowContextMenu(this);
        toolTipManager = new ToolTipManager(this);
    }

    // @Override
    // public void setCurrentSelectionModel(SelectionModel model) {
    // currentSelectionModel = model;
    // Map<String, String> headerMap = new HashMap<String, String>();
    // headerMap.put("currentSelection", "updated");
    // new CsiEvent(headerMap, new HashMap<String, String>()).fire();
    // }

    public static GraphSurfaceImpl create(Graph graph) {
        return new GraphSurfaceImpl(graph);
    }

    @Override
    public CSIActivityManager getActivityManager() {
        return activityManager;
    }

    @Override
    public FillStrokeStyle getBackgroundColor() {
        return graph.getBackgroundColor();
    }

    @Override
    public int getBackgroundColorInt() {
        return graph.getModel().getBackgroundColorInt();
    }

    @Override
    public ClickMode getClickMode() {
        return clickMode;
    }

    @Override
    public void setClickMode(ClickMode clickMode) {
        this.clickMode = clickMode;
    }

    @Override
    public DragMode getDragMode(boolean useExplicit) {
        return dragMode;
    }

    @Override
    public DragMode getExplicitDragMode() {
        return explicitDragMode;
    }

    @Override
    public void setExplicitDragMode(DragMode explicitDragMode) {
        this.explicitDragMode = explicitDragMode;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    public View getGraphSurface() {
        return graphSurface;
    }

    @Override
    public KeyHandler getKeyHandler() {
        return keyHandler;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        mouseHandler.setPresenter(presenter);
    }

    @Override
    public View getView() {
        return graphSurface;
    }

    @Override
    public String getVizUuid() {
        return graph.getUuid();
    }

    @Override
    public void setDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
    }

    @Override
    public void show() {
        activityManager.setActivity(new ShowGraph(this));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void refresh(VortexFuture future) {
        refresh(future, null);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void refreshWithNewLayout(final VortexFuture vortexFuture, final GraphLayout oldLayout) {
//    	refresh(vortexFuture, null);
        vortexFuture.addEventHandler(new AbstractVortexEventHandler() {
            @Override
            public void onSuccess(Object result) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUpdate(int taskProgress, String taskMessage) {
                String message = taskMessage + "\n" + taskProgress;
                graph.setProgressIndicatorText(message);
            }

            @Override
            public void onCancel() {
                graph.respondToCancel();
                graph.getModel().applyLayoutBeforeLoad(oldLayout);
                graph.getModel().checkLayout(oldLayout);
            }

        });
        refresh(vortexFuture, event -> {
            graph.setProgressIndicatorText("Cancelling");
            vortexFuture.cancel(new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSuccess(Void result) {
                    // TODO Auto-generated method stub

                }

            });
        });
    }

    private void refresh(VortexFuture future, ClickHandler cancelHandler) {
        graph.showProgressIndicator(future, cancelHandler);
        future.addEventHandler(refreshLater);
    }

    @Override
    public void refresh() {
        if (presenter instanceof RefreshGraph) {
            RefreshGraph refreshGraph = (RefreshGraph) presenter;
            refreshGraph.again();
        } else {
            activityManager.setActivity(new RefreshGraph(this));
        }
    }

    @Override
    public MouseHandler getMouseHandler() {
        return mouseHandler;
    }

    @Override
    public Widget asWidget() {
        return graphSurface.asWidget();
    }

    @Override
    public void zoomToFit() {
        if (model != null) {
            refresh(model.zoomToFit(), null);
            toolTipManager.removeAllToolTips();
        }
    }

    @Override
    public ContextMenuPresenter getContextMenuPresenter() {
        return contextMenuPresenter;
    }

    @Override
    public ToolTipManager getToolTipManager() {
        return toolTipManager;
    }

    public void setReadOnly() {

        if (null != contextMenuPresenter) {
            contextMenuPresenter.setReadOnly();
        }
    }

    @Override
    public void showMultiTypes(MultiTypeInfo result) {
        MultiTypePresenter presenter = new MultiTypePresenter();
        List<LegendItemProxy> items = getGraph().getLegend().getMatchingItems(result.getTypes(), result.isNode());
        presenter.showTypes(graphSurface, result, items);
        graphSurface.addMultiTypePreview(presenter.getView(), (int) result.getX(), (int) result.getY());
    }
}
