package csi.client.gwt.viz.viewer.settings.editor;

import csi.client.gwt.viz.viewer.settings.ViewerSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

public interface ViewerSettingsEditor {
    ViewerSettingsEditorModel getModel();

    ViewerSettingsEditorView getView();

    VSEControl buildControl(LensSettingsControl control);

    void addAvailableLens(LensDefSettings lensDefSettings);

    void addLens(LensDefSettings lensDefSettings);

    void updateModel();

    void removeLens(LensDefSettings lensDefSettings);
}
