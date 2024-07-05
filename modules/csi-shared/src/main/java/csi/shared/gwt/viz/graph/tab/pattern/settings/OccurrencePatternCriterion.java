package csi.shared.gwt.viz.graph.tab.pattern.settings;

public class OccurrencePatternCriterion implements NodePatternCriterion {
    private String name;
    private String value;
    private boolean showInResults;
    private String minValue;
    private String maxValue;

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
        return "Occurrence";
    }

    @Override
    public boolean isShowInResults() {
        return showInResults;
    }

    @Override
    public void setShowInResults(boolean value) {
        showInResults = value;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public NodePatternCriterion deepCopy() {
        OccurrencePatternCriterion copy = new OccurrencePatternCriterion();
        copy.name = name;
        copy.value = value;
        copy.showInResults = showInResults;
        copy.minValue = minValue;
        copy.maxValue = maxValue;
        return copy;
    }
}
