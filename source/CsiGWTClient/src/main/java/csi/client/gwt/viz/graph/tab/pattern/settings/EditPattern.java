package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class EditPattern extends AbstractPatternSettingsActivity {
    private final GraphPattern pattern;

    public EditPattern(PatternSettings patternSettings, GraphPattern pattern) {
        super(patternSettings);
        this.pattern = pattern;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        getView().selectPattern(pattern);
        getModel().setEditPattern(pattern);
        PatternSettings.PatternSettingsView view = getView();
        {
            view.bind(this);
            view.clearPattern();
            view.setPatternName(pattern.getName());
            addPatternNodes();
            addPatternLinks();
            view.setRequireDistinctNodes(pattern.isRequireDistinctNodes());
            view.setRequireDistinctLinks(pattern.isRequireDistinctLinks());
        }
        patternSettings.show();
    }

    private void addPatternLinks() {
        PatternSettings.PatternSettingsView view = getView();
        for (PatternLink patternLink : pattern.getPatternLinks()) {
            view.addLinkToPattern(patternLink);
        }
    }

    private void addPatternNodes() {
        PatternSettings.PatternSettingsView view = getView();
        for (PatternNode patternNode : pattern.getPatternNodes()) {
            view.addNodeToPattern(patternNode);
        }
    }
}
