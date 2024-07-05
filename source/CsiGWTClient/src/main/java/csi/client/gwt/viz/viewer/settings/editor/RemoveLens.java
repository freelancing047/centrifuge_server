package csi.client.gwt.viz.viewer.settings.editor;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

public class RemoveLens implements Activity {
    private ViewerSettingsEditorImpl viewerSettingsEditor;
    private LensDefSettings lensDefSettings;

    public RemoveLens(ViewerSettingsEditorImpl viewerSettingsEditor, LensDefSettings lensDefSettings) {
        this.viewerSettingsEditor = viewerSettingsEditor;
        this.lensDefSettings = lensDefSettings;
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        lensDefSettings = lensDefSettings.copy();
        viewerSettingsEditor.getModel().removeLensDefSetting(lensDefSettings);
    }
}
