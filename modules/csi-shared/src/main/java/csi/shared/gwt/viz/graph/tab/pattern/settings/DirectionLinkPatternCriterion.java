package csi.shared.gwt.viz.graph.tab.pattern.settings;

public class DirectionLinkPatternCriterion implements LinkPatternCriterion {

    private String name;
    private String value;
    private boolean showInResults;
    private boolean bidirectional;
    private boolean undirected;
    private boolean reverse;
    private boolean forward;

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
        return "Direction";
    }

    @Override
    public LinkPatternCriterion deepCopy() {
        DirectionLinkPatternCriterion copy;
        copy = new DirectionLinkPatternCriterion();
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
        this.showInResults = value;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public boolean isUndirected() {
        return undirected;
    }

    public void setUndirected(boolean undirected) {
        this.undirected = undirected;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }
}
