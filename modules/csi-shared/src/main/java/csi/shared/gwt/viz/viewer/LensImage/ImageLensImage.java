package csi.shared.gwt.viz.viewer.LensImage;

public class ImageLensImage implements LensImage {
    private String value;

    @Override
    public String getLensDef() {
        return null;
    }

    @Override
    public void setLensDef(String id) {

    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setLabel(String s) {

    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
