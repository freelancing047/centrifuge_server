package csi.shared.gwt.viz.graph.tab.pattern.settings;

public class NullNodePatternCriterion implements PatternCriterion {
    private String name;
    private String value;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return "";
    }

    public PatternCriterion deepCopy() {
        NullNodePatternCriterion copy = new NullNodePatternCriterion();
        copy.name = name;
        copy.value = value;
        return copy;
    }

    @Override
    public boolean isShowInResults() {
        return false;
    }

    @Override
    public void setShowInResults(boolean value) {

    }
}
