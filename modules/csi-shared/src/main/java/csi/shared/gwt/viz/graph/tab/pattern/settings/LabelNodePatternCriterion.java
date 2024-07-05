package csi.shared.gwt.viz.graph.tab.pattern.settings;

public class LabelNodePatternCriterion implements NodePatternCriterion {
    private String value;
    private String name;
    private boolean showInResults;

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
        return "Label";
    }

    @Override
    public NodePatternCriterion deepCopy() {
        LabelNodePatternCriterion copy = new LabelNodePatternCriterion();
        copy.name = name;
        copy.value = value;
        return copy;
    }

    @Override
    public boolean isShowInResults() {
        return showInResults;
    }

    @Override
    public void setShowInResults(boolean value) {
        showInResults = value;
    }
}
