package csi.client.gwt.viz.viewer.settings.editor;

public interface ViewerSettingsEditorView {
    void addSettingsContainer(LensSettingsContainer settingsContainer);

    void addAvailableLens(AvailableLensControl control);

    void showDialog();

    void hide();
}
