package csi.server.common.model.visualization.viewer;

import com.google.common.collect.Lists;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;

import java.util.ArrayList;
import java.util.Collection;

public class ContextImageLensDef implements LensDef {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean handles(Objective objective) {
        return false;
    }

    @Override
    public String getDisplayName() {
        return "ContextImage";
    }

    @Override
    public void setDisplayName(String displayName) {

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
        lds.setControls(Lists.newArrayList());
        lds.setName(getDisplayName());
        lds.setLensType("ContextImage");
        return lds;
    }
}
