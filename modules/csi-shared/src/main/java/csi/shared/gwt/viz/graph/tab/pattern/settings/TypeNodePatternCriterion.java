package csi.shared.gwt.viz.graph.tab.pattern.settings;

public class TypeNodePatternCriterion implements NodePatternCriterion {

    private String name;
    private String value;


    private boolean showInResults;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return "Type";
    }

    @Override
    public NodePatternCriterion deepCopy() {
        TypeNodePatternCriterion typePatternCriterion = new TypeNodePatternCriterion();
        typePatternCriterion.name = name;
        typePatternCriterion.value = value;
        return typePatternCriterion;
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
