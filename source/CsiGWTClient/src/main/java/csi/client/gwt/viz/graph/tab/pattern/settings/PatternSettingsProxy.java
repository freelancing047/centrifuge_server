package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.List;

import com.google.common.collect.Lists;

import csi.client.gwt.WebMain;
import csi.server.common.service.api.PatternActionsServiceProtocol;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;


public class PatternSettingsProxy
        implements PatternSettings.PatternSettingsModel {
    private List<GraphPattern> patterns;
    private List<GraphType> types;
    private GraphPattern editPattern;

    public PatternSettingsProxy() {
        this.patterns = Lists.newArrayList();
        this.types = Lists.newArrayList();
    }


    @Override
    public List<GraphPattern> getPatterns() {
        return patterns;
    }

    @Override
    public List<GraphType> getTypes() {
        return types;
    }

    public void add(GraphPattern pattern) {
        patterns.add(pattern);
    }

    @Override
    public void add(GraphType type) {
        types.add(type);
    }

    @Override
    public void addPattern(GraphPattern pattern) {
        patterns.add(pattern);
    }

    @Override
    public GraphPattern getEditPattern() {
        return editPattern;
    }

    @Override
    public void setEditPattern(GraphPattern editPattern) {
        this.editPattern = editPattern;
    }

    @Override
    public void removePattern(GraphPattern pattern) {
        patterns.remove(pattern);
    }

    @Override
    public void savePatterns() {
        String owner = WebMain.injector.getMainPresenter().getUserInfo().getName();
        WebMain.injector.getVortex().execute(PatternActionsServiceProtocol.class).saveGraphPatterns(owner, patterns);
    }
}
