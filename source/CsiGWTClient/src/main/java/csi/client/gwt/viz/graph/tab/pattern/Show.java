package csi.client.gwt.viz.graph.tab.pattern;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.tab.pattern.PatternTab.PatternTabView;

public class Show extends AbstractPatternTabActivity {
    private PatternTab patternTab;

    public Show(PatternTab patternTab) {
        this.patternTab = patternTab;
    }

    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        PatternTabView view = patternTab.getView();
        view.bind(this);
    }

}
