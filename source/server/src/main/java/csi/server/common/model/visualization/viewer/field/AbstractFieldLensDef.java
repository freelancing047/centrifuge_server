package csi.server.common.model.visualization.viewer.field;

import com.google.common.collect.Lists;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensFieldDefSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractFieldLensDef implements LensDef {
    private String value="";
    private String id="1";

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean handles(Objective objective) {
        return true;
    }

    @Override
    public abstract String getDisplayName();

    @Override
    public abstract void setDisplayName(String displayName);

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
            control.setValue(value);
            controls.add(control);
        }
        lds.setControls(controls);
        lds.setName(getDisplayName());
        lds.setLensType(getLensType());
        return lds;
    }

    protected abstract String getLensType();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setId(String id) {
        this.id = id;
    }
}
