package csi.server.common.model.visualization.viewer.field;

public class VarianceFieldLensDef extends AbstractFieldLensDef {

    private String displayName = "Variance of Field";

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
        return "VarianceField";
    }
}