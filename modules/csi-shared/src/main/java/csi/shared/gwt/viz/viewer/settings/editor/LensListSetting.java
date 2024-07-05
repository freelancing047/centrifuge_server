package csi.shared.gwt.viz.viewer.settings.editor;

import java.util.ArrayList;
import java.util.List;

public class LensListSetting extends LensSettingsControl {
    String selectedValue;

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    List<String> values;
    public LensSettingsControl copy() {
        LensListSetting copy = new LensListSetting();
        copy.setLabel(getLabel());
        copy.setType(getType());
        copy.setValues(new ArrayList<>(values));
        copy.setSelectedValue(getSelectedValue());
        return copy;

    }
}
