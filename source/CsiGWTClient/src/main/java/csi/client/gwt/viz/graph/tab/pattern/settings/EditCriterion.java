package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsView;
import csi.client.gwt.viz.graph.tab.pattern.settings.criterion.CriterionPanel;
import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class EditCriterion extends AbstractPatternSettingsActivity {
    private HasPatternCriteria node;
    private PatternCriterion criterion;

    public EditCriterion(PatternSettings patternSettings, HasPatternCriteria node, PatternCriterion criterion) {
        super(patternSettings);
        this.node = node;
        this.criterion = criterion;
    }

    @Override
    public void hideNodeDetails() {
    }

    @Override
    public void pinDetails(PatternNode node) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        PatternSettingsView view = getView();
        view.bind(this);
        view.showCriterion(new CriterionPanel(patternSettings, node, criterion));
    }
}
