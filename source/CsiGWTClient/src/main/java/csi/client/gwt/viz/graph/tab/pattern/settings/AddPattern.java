package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;

public class AddPattern extends AbstractPatternSettingsActivity {
    private GraphPattern pattern;

    public AddPattern(PatternSettings patternSettings, GraphPattern pattern) {

        this(patternSettings);
        this.pattern = pattern;
    }

    public AddPattern(PatternSettings patternSettings) {
        super(patternSettings);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        getModel().addPattern(pattern);
        getView().addPattern(pattern);
        patternSettings.editPattern(pattern);
    }
}
