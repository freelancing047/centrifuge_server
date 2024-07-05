package csi.shared.gwt.viz.viewer.settings.editor;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class LensMultiListSetting extends LensSettingsControl {
    public List<String> getSelectedValues() {
        return selectedValues;
    }

    public void setSelectedValues(List<String> selecteValues) {
        this.selectedValues = selecteValues;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    List<String> selectedValues;
    List<String> values;

    public LensSettingsControl copy() {
        LensMultiListSetting copy = new LensMultiListSetting();
        copy.setLabel(getLabel());
        copy.setType(getType());
        copy.setValues(new ArrayList<>(getValues()));
        copy.setSelectedValues(Lists.newArrayList(getSelectedValues()));
        return copy;
    }
}
