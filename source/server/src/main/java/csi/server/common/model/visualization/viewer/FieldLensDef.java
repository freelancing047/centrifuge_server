package csi.server.common.model.visualization.viewer;

import com.google.common.collect.Lists;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;

import java.util.Collection;
import java.util.List;

public class FieldLensDef implements LensDef {
    public List<String> localIds = Lists.newArrayList();

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
        return null;
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    @Override
    public Collection<String> getGroups() {
        return null;
    }

    @Override
    public LensDefSettings getSettings() {
        return null;
    }
}
