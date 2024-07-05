package csi.shared.gwt.viz.graph.tab.pattern.settings;

public class NeighborNodePatternCriterion implements NodePatternCriterion {
    private String name;
    private String value;
    private boolean showInResults;
    private String minValue;
    private String maxValue;
    private String includeHidden;
    private String typeOfNeighbor;

    public String getTypeOfNeighbor() {
        return typeOfNeighbor;
    }

    public void setTypeOfNeighbor(String typeOfNeighbor) {
        this.typeOfNeighbor = typeOfNeighbor;
    }

    public String getIncludeHidden() {
        return includeHidden;
    }

    public void setIncludeHidden(String includeHidden) {
        this.includeHidden = includeHidden;
    }

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
        return "Number of Neighbors";
    }

    @Override
    public NodePatternCriterion deepCopy() {
        NeighborNodePatternCriterion copy = new NeighborNodePatternCriterion();
        copy.name = name;
        copy.value = value;
        copy.showInResults = showInResults;
        copy.minValue = minValue;
        copy.maxValue = maxValue;
        copy.includeHidden = includeHidden;
        copy.typeOfNeighbor = typeOfNeighbor;

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
}
