package csi.server.common.model.visualization.viewer;

import com.google.common.collect.Lists;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensFieldDefSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

import java.util.ArrayList;
import java.util.Collection;

public class HyperlinkLensDef implements LensDef {
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    String value;
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean handles(Objective objective) {
        return false;
    }

    private String displayName = "Hyperlink";

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    @Override
    public Collection<String> getGroups() {
        ArrayList<String> strings = Lists.newArrayList();
        strings.add("Graph");
        return strings;
    }

    @Override
    public LensDefSettings getSettings() {
        LensDefSettings lds = new LensDefSettings();
        ArrayList<LensSettingsControl> controls = Lists.newArrayList();
        {
            LensFieldDefSetting control = new LensFieldDefSetting();
            control.setLabel("Field");
            control.setType("field");
            control.setValue("");
            controls.add(control);
        }
        lds.setControls(controls);
        lds.setName(getDisplayName());
        lds.setLensType("hyperlink");
        return lds;
    }
}
