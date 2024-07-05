package csi.shared.gwt.viz.viewer.LensImage;

import java.util.List;

public class LinkLensImage implements LensImage {
    private String label;
    private List<String> types;
    private String count;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    private String lensDef;
    @Override
    public String getLensDef() {
        return lensDef;
    }

    public void setLensDef(String lensDef) {
        this.lensDef = lensDef;
    }
    public enum LinkLensTokens{
        TYPE,
        LABEL
    }
}
