package csi.server.common.model.visualization.viewer.field;

public class MinFieldLensDef extends AbstractFieldLensDef {

    private String displayName = "Min of Field";

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
        return "MinField";
    }
}
