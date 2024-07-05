package csi.server.common.model.visualization.viewer.field;

public class ValueFieldLensDef extends AbstractFieldLensDef {

    private String displayName = "Value of Field";

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
        return "ValueField";
    }
}
