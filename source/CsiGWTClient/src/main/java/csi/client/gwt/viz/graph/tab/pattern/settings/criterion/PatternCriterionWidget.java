package csi.client.gwt.viz.graph.tab.pattern.settings.criterion;

import com.google.gwt.user.client.ui.IsWidget;

import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public interface PatternCriterionWidget extends IsWidget {
    PatternCriterion getCriterion();

    void setCriterion(PatternCriterion criterion);
}
