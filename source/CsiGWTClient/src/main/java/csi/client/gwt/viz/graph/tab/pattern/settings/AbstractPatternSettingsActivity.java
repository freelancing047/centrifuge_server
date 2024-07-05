package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.List;

import com.google.common.collect.Lists;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsActivity;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.server.common.model.CsiUUID;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public abstract class AbstractPatternSettingsActivity implements PatternSettingsActivity {
    protected final PatternSettings patternSettings;

    public AbstractPatternSettingsActivity(PatternSettings patternSettings) {
        this.patternSettings = patternSettings;
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void close() {
        getModel().savePatterns();
        getView().hide();
    }

    @Override
    public void addToPattern(GraphType type) {
    }

    @Override
    public void showNodeDetails(DrawPatternNode node) {
    }

    protected PatternSettings.PatternSettingsModel getModel() {
        return patternSettings.getModel();
    }

    @Override
    public void search() {
        PatternSettings.PatternSettingsModel model = getModel();
        GraphPattern pattern = model.getEditPattern();
        VortexFuture<PatternResultSet> future = WebMain.injector.getVortex().createFuture();
        future.execute(GraphActionServiceProtocol.class).findPatterns(patternSettings.getGraph().getUuid(), pattern);
        PatternResultSetHandler handler = new PatternResultSetHandler(patternSettings.getTab());
        future.addEventHandler(handler);
        close();
        patternSettings.setLoading(future);
    }

    @Override
    public void addLink(PatternNode node1, PatternNode node2) {
    }

    @Override
    public void createNewPattern() {
        GraphPattern pattern = new GraphPattern(CsiUUID.randomUUID());
        List<String> patternNames = Lists.newArrayList();
        for (GraphPattern graphPattern : getModel().getPatterns()) {
            patternNames.add(graphPattern.getName());
        }
        String patternName = UniqueNameUtil.getDistinctName(patternNames, CentrifugeConstantsLocator.get().patternSettings_newPattern());
        pattern.setName(patternName);
        getModel().addPattern(pattern);
        getView().addPattern(pattern);
        patternSettings.editPattern(pattern);

    }

    @Override
    public void deletePattern() {
        GraphPattern pattern = getModel().getEditPattern();
        patternSettings.removePattern(pattern);
    }

    @Override
    public void setPatternName(String name) {
        GraphPattern editPattern = getModel().getEditPattern();
        if (editPattern != null) {
            editPattern.setName(name);
            getView().updatePattern(getModel().getEditPattern());
        }
    }

    @Override
    public void editPattern(GraphPattern pattern) {
        patternSettings.editPattern(pattern);
    }

    @Override
    public void editLink(PatternLink link) {

    }

    @Override
    public boolean allowLinkDrawing() {
        return false;
    }

    @Override
    public void copyPattern() {

    }

    @Override
    public void setRequireDistinctNodes(boolean value) {
        patternSettings.getModel().getEditPattern().setRequireDistinctNodes(value);
    }

    @Override
    public void setRequireDistinctLinks(boolean value) {
        patternSettings.getModel().getEditPattern().setRequireDistinctLinks(value);
    }

    protected PatternSettings.PatternSettingsView getView() {
        return patternSettings.getView();
    }

    @Override
    public void hideNodeDetails() {

    }

    @Override
    public void pinDetails(PatternNode var1) {

    }
}
