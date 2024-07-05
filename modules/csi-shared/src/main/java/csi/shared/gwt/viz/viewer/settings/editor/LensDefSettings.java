package csi.shared.gwt.viz.viewer.settings.editor;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;
import csi.shared.core.util.Native;

import java.util.ArrayList;
import java.util.List;

public class LensDefSettings implements IsSerializable {
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private int position;
    private String name;
    private String LensType;
    private ArrayList<LensSettingsControl> controls;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLensType() {
        return LensType;
    }

    public void setLensType(String lensType) {
        LensType = lensType;
    }

    public void setControls(List<LensSettingsControl> controls) {
        this.controls = Lists.newArrayList(controls);
    }


    public List<LensSettingsControl> getControls() {
        if (controls == null) {
            controls = Lists.newArrayList();
        }
        return controls;
    }


    public LensDefSettings copy() {
        LensDefSettings copy = new LensDefSettings();
        copy.setName(getName());
        copy.setLensType(getLensType());
        List<LensSettingsControl> controlsCopy = Lists.newArrayList();
        for (LensSettingsControl control : controls) {
            controlsCopy.add(control.copy());
        }
        copy.setControls(controlsCopy);
        return copy;
    }
}
