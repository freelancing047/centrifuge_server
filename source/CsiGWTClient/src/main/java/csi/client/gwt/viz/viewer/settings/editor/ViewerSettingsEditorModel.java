package csi.client.gwt.viz.viewer.settings.editor;

import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;

import java.util.List;

public interface ViewerSettingsEditorModel {

    void addLensDefSetting(LensDefSettings lensDefSettings);

    List<LensDefSettings> getLensDefSettingsList();

    void setLensDefSetting(List<LensDefSettings> result);

    void removeLensDefSetting(LensDefSettings lensDefSettings);
}
