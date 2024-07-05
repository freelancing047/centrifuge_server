package csi.server.common.model.visualization.viewer;

import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;

import java.util.Collection;

public interface LensDef{
    String getId();

    boolean handles(Objective objective);

    String getDisplayName();

    void setDisplayName(String displayName);

    Collection<String> getGroups();

    LensDefSettings getSettings();
}
