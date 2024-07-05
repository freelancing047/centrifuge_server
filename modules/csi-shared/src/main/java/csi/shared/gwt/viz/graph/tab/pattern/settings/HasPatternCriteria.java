package csi.shared.gwt.viz.graph.tab.pattern.settings;

import com.google.common.collect.ImmutableList;

public interface HasPatternCriteria {
    void addCriterion(PatternCriterion criterion);

    ImmutableList<PatternCriterion> getCriteria();

    String getName();

    void setName(String name);

    void removeCriterion(PatternCriterion criterion);

    boolean appliesToType(String type);

    boolean showInResults();

    void setShowInResults(boolean value);
}
