package csi.shared.gwt.viz.viewer.settings.editor;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LensSettingsControl implements IsSerializable {
    String label;
    String type;//FIXME: change to enum

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LensSettingsControl copy() {
        LensSettingsControl copy = new LensListSetting();
        copy.setLabel(getLabel());
        copy.setType(getType());
        return copy;
    }
}
