package csi.shared.gwt.viz.viewer.settings.editor;

public class LensFieldDefSetting extends LensSettingsControl {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LensSettingsControl copy() {
        LensFieldDefSetting copy = new LensFieldDefSetting();
        copy.setLabel(getLabel());
        copy.setType(getType());
        copy.setValue(value);
        return copy;
    }

}
