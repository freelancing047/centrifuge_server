package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

import csi.client.gwt.WebMain;
import csi.client.gwt.util.BasicPlace;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.tab.player.StepEvent;
import csi.client.gwt.viz.graph.tab.player.StepEventHandler;
import csi.client.gwt.viz.graph.tab.player.StopEvent;
import csi.client.gwt.viz.graph.tab.player.StopEventHandler;
import csi.config.advanced.graph.ControlBarConfig;

public class GraphControlBarImpl implements GraphControlBar {

    private final Graph graph;
    private final EventBus eventBus;
    private final PlaceController placeController;
    private final GraphControlBarActivityMapper activityMapper;
    private final CSIActivityManager activityManager;
    private final GraphControlBarModel model;
    private final GraphControlBarView view;

    public GraphControlBarImpl(Graph graph) {
        this.graph = graph;
        model = new GraphControlBarModel(this);
        view = new GraphControlBarView(this);
        eventBus = new SimpleEventBus();
        placeController = new PlaceController(eventBus, new PlaceController.DefaultDelegate());
        activityMapper = new GraphControlBarActivityMapper(this);
        activityManager = new CSIActivityManager(activityMapper, eventBus);
        placeController.goTo(BasicPlace.DEFAULT_PLACE);
        activityManager.setActivity(new ShowGraphPlayer(this));
        graph.getTimePlayer().addEventHandler(new StepEventHandler() {
            @Override
            public void onStepEvent(StepEvent event) {
                model.setCurrentTime(event.getCurrentTime());
                step();
            }
        }, StepEvent.TYPE);
        graph.getTimePlayer().addEventHandler(new StopEventHandler() {
            @Override
            public void onStopEvent(StopEvent stopEvent) {
                initialize();
            }
        },StopEvent.TYPE);
        graph.addGraphEventHandler(GraphEvents.GRAPH_LOAD_COMPLETE, new GraphEventHandler() {
            @Override
            public void onGraphEvent(GraphEvent event) {
                if(view != null && view.isPlaying())
                    stop();
            }
        });
    }


    @Override
    public ControlBarConfig getConfig() {
        return WebMain.getClientStartupInfo().getGraphAdvConfig().getPlayerConfig().getControlBarConfig();
    }

    @Override
    public GraphControlBarView getView() {
        return view;
    }

    @Override
    public GraphControlBarModel getModel() {
        return model;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }


    @Override
    public void play() {
        activityManager.setActivity(new PlayGraphPlayer(this));

    }

    @Override
    public void stop() {
        activityManager.setActivity(new StopGraphPlayer(this));
    }

    @Override
    public void pause() {
        activityManager.setActivity(new PauseGraphPlayer(this));
    }

    @Override
    public void scrub(long time) {
        activityManager.setActivity(new ScrubGraph(this));
    }

    public void scrubToPercent(double percent){
        view.setScrubberPercent(percent);
    }

    @Override
    public void setStart(long time) {
        getModel().setStartTime(time);
        activityManager.setActivity(new SetPlayerStart(this));

    }

    @Override
    public void setEnd(long time) {

    }

    @Override
    public void show(boolean show) {
        //FIXME:this should be used or deleted...
        if (show) {
            activityManager.setActivity(new ShowGraphPlayer(this));
        } else {
            activityManager.setActivity(new HideGraphPlayer(this));
        }
    }

    @Override
    public void step() {
        activityManager.setActivity(new StepGraphPlayer(this));
    }

    @Override
    public void editStart() {

    }

    @Override
    public void editEnd() {

    }

    @Override
    public void editCurrent() {

    }

    @Override
    public void initialize() {
        activityManager.setActivity(new ShowGraphPlayer(this));

    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public Widget getHbox(){
        if(view !=null) {
            return view.getHbox();
        }
        return null;
    }
}
