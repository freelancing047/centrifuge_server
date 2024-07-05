package csi.shared.gwt.viz.graph.tab.pattern.settings;

public class FieldDefNodePatternCriterion implements NodePatternCriterion {
    private String name;
    private String value;
    private String fieldName;
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
    public String getType() { return "Field Value"; }

    @Override
    public NodePatternCriterion deepCopy() {
        FieldDefNodePatternCriterion copy = new FieldDefNodePatternCriterion();
        copy.name = name;
        copy.value = value;
        copy.fieldName = fieldName;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
