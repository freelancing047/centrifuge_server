package csi.server.common.model.visualization.viewer.field;

public class CountDistinctFieldLensDef extends AbstractFieldLensDef {

    private String displayName = "Count Distinct of Field";

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    @Override
    protected String getLensType() {
        return "CountDistinctField";
    }
}
