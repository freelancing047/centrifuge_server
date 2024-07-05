package csi.client.gwt.viz.graph.tab.pattern;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.tab.pattern.result.PatternResultWidgetImpl;
import csi.server.common.dto.graph.pattern.PatternResultSet;

public class AddResults extends AbstractPatternTabActivity {
    private final PatternTab patternTab;
    private final PatternResultSet result;

    public AddResults(PatternTab patternTab, PatternResultSet result) {
        this.patternTab = patternTab;
        this.result = result;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        PatternTab.PatternTabModel model = patternTab.getModel();
        model.setCurrentResults(result);
        PatternTab.PatternTabView view = patternTab.getView();
        view.bind(this);

        PatternResultWidgetImpl patternResultWidget = new PatternResultWidgetImpl(patternTab.getGraph(), patternTab, result);

        view.setResults(patternResultWidget, result);
        patternTab.show();
    }
}
