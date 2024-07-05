package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.node.settings.NodeSettingsActivityMapper;
import csi.client.gwt.viz.graph.tab.pattern.PatternTab;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class PatternSettingsImpl
        implements PatternSettings
{
    private final EventBus eventBus;
    private final CSIActivityManager activityManager;
    private final PatternSettingsModel model;
    private final PatternSettingsView view;
    private final Graph graph;
    private final PatternTab patternTab;

    public PatternSettingsImpl(final PatternTab patternTab)
    {
        this.patternTab = patternTab;
        graph = patternTab.getGraph();
        model = new PatternSettingsProxy();
        view = new PatternSettingsDialog(this);
        eventBus = new SimpleEventBus();
        //FIXME: what is NodeSettingsActivityMapper doing here?
        activityManager = new CSIActivityManager(new NodeSettingsActivityMapper(), eventBus);
        activityManager.setActivity(new Initialize(this));
    }

    @Override
    public void show()
    {
        activityManager.setActivity(new Show(this));
    }

    @Override
    public PatternSettingsView getView()
    {
        return view;
    }

    @Override
    public PatternSettingsModel getModel()
    {
        return model;
    }

    @Override
    public Graph getGraph()
    {
        return graph;
    }

    @Override
    public void editCriteria(final HasPatternCriteria node)
    {
        activityManager.setActivity(new EditCriteria(this, node));
    }

    @Override
    public void hideCriteria()
    {
        activityManager.setActivity(new Show(this));
    }

    @Override
    public void editCriterion(final HasPatternCriteria node, final PatternCriterion criterion)
    {
        activityManager.setActivity(new EditCriterion(this, node, criterion));
    }

    @Override
    public void editPattern(final GraphPattern pattern) {
        activityManager.setActivity(new EditPattern(this, pattern));
    }

    @Override
    public void removePattern(final GraphPattern pattern) {
        activityManager.setActivity(new RemovePattern(this, pattern));
    }

    @Override
    public void addPattern(final GraphPattern pattern) {
        activityManager.setActivity(new AddPattern(this, pattern));
    }

    @Override
    public void removeFromPattern(final HasPatternCriteria item) {
        activityManager.setActivity(new RemoveItem(this, item));
    }

    @Override
    public void removeCriterion(final HasPatternCriteria node, final PatternCriterion criterion) {
        node.removeCriterion(criterion);
        activityManager.setActivity(new EditCriteria(this, node));
    }

    @Override
    public void setResults(final PatternResultSet result) {
        patternTab.setResults(result);
    }

    @Override
    public void setLoading(VortexFuture future) {
        patternTab.setMostRecentPattern(model.getEditPattern());
        patternTab.setLoading(future);
    }

    @Override
    public PatternTab getTab() {
        return patternTab;
    }

    @Override
    public void setEditPattern(GraphPattern pattern) {
        model.setEditPattern(pattern);
    }
}
