package csi.client.gwt.viz.graph.tab.pattern;

import java.util.List;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.Tab;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.node.settings.NodeSettingsActivityMapper;
import csi.client.gwt.viz.graph.tab.pattern.result.PatternResultWidget;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternResultSetHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.PatternHighlightRequest;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;

public class PatternTabImpl implements PatternTab {
    private final Graph graph;
    private final EventBus eventBus;
    private final CSIActivityManager activityManager;
    private final PatternTabView view;
    private final PatternTabModel model;
    private GraphPattern mostRecentPattern;
    private Set<PatternResultSetHandler> pendingResultHandler = Sets.newHashSet();


    public PatternTabImpl(GraphImpl graph) {
        this.graph = graph;
        this.view = new PatternTabViewImpl(graph, this);
        this.eventBus = new SimpleEventBus();
        //FIXME: what is NodeSettingsActivityMapper doing here?
        this.activityManager = new CSIActivityManager(new NodeSettingsActivityMapper(), eventBus);
        activityManager.setActivity(new Show(this));
        model = new PatternTabModelImpl();
        graph.addGraphEventHandler(GraphEvents.GRAPH_LOAD_COMPLETE, new GraphEventHandler() {
            @Override
            public void onGraphEvent(GraphEvent event) {
                refresh();
            }
        });
    }

    @Override
    public PatternTabView getView() {
        return view;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public void setResults(PatternResultSet result) {
        activityManager.setActivity(new AddResults(this, result));
    }

    @Override
    public void show() {
        activityManager.setActivity(new Show(this));
    }

    @Override
    public void setLoading(VortexFuture future) {
        view.setLoading(future);
    }

    @Override
    public PatternTabModel getModel() {
        return model;
    }

    @Override
    public GraphPattern getMostRecentPattern() {
        return mostRecentPattern;
    }

    @Override
    public void setMostRecentPattern(GraphPattern pattern) {

        mostRecentPattern = pattern;
    }

    @Override
    public List<PatternHighlightRequest> getHighlightRequests() {
        List<PatternHighlightRequest> patternHighlightRequests = Lists.newArrayList();
        for (PatternResultWidget patternResultWidget : view.getPatternResultWidgets()) {
            patternHighlightRequests.add(patternResultWidget.getPatternHighlightRequest());
        }
        return patternHighlightRequests;
    }

    @Override
    public void refresh() {
        for (PatternResultSetHandler handler : pendingResultHandler) {
            handler.abort();
        }
        view.clearResults();
    }

    @Override
    public void removePendingPatternResult(PatternResultSetHandler handler) {
        pendingResultHandler.remove(handler);
    }

    @Override
    public void addPendingPatternResult(PatternResultSetHandler handler) {
        pendingResultHandler.add(handler);
    }

    @Override
    public Tab getTab() {
        return view.getTab();
    }
}
