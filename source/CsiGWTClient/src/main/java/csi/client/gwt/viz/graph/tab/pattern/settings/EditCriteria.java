package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsView;
import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class EditCriteria extends AbstractPatternSettingsActivity {
    private HasPatternCriteria item;

    public EditCriteria(PatternSettingsImpl patternSettings, HasPatternCriteria item) {
        super(patternSettings);
        this.item = item;
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
        view.setEditing(item);
        CriteriaPanel cp = new CriteriaPanel(patternSettings, item);
        if (item instanceof PatternLink) {
            cp.allowAddCriteria(true);
        }
        else {
            cp.allowAddCriteria(true);
        }
        view.showCriteria(cp);
    }
}
