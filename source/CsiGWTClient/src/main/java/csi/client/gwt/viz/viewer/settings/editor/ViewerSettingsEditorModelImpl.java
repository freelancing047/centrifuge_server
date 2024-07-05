package csi.client.gwt.viz.viewer.settings.editor;

import com.google.common.collect.Lists;
import csi.client.gwt.viz.viewer.settings.ViewerSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;

import java.util.List;

public class ViewerSettingsEditorModelImpl implements ViewerSettingsEditorModel {
    private ViewerSettings viewerSettings;
    List<LensDefSettings> lensDefSettingsList;

    public ViewerSettingsEditorModelImpl(ViewerSettings viewerSettings) {
        super();
        this.viewerSettings = viewerSettings;
    }

    public ViewerSettings getViewerSettings() {
        return viewerSettings;
    }

    @Override
    public void addLensDefSetting(LensDefSettings lensDefSettings) {
        if (lensDefSettingsList == null) {
            //FIXME: should already be initialized.
            lensDefSettingsList = Lists.newArrayList();
        }
        lensDefSettingsList.add(lensDefSettings);
    }

    @Override
    public List<LensDefSettings> getLensDefSettingsList() {
        return lensDefSettingsList;
    }

    @Override
    public void setLensDefSetting(List<LensDefSettings> lensDefSettingsList) {
        this.lensDefSettingsList = lensDefSettingsList;
        int i = 0;
        for (LensDefSettings lensDefSettings : lensDefSettingsList) {
            lensDefSettings.setPosition(i++);
        }
    }

    @Override
    public void removeLensDefSetting(LensDefSettings lensDefSettings) {
        lensDefSettingsList.remove(lensDefSettings.getPosition());
    }
}
