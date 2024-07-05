package csi.client.gwt.viz.viewer.settings.editor;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.ViewerActionServiceProtocol;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

import java.util.List;
import java.util.Map;

public class Show implements Activity {
    private ViewerSettingsEditorImpl vse;

    public Show(ViewerSettingsEditorImpl viewerSettingsEditor) {

        vse = viewerSettingsEditor;
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
        loadAvailableLenses();
        loadActiveLenses();
        vse.getView().showDialog();
    }

    public void loadAvailableLenses() {
        VortexFuture<Map<String, List<LensDefSettings>>> future = WebMain.injector.getVortex().createFuture();
        future.execute(ViewerActionServiceProtocol.class).getAvailableLenses();
        future.addEventHandler(new AbstractVortexEventHandler<Map<String, List<LensDefSettings>>>() {
            @Override
            public void onSuccess(Map<String, List<LensDefSettings>> result) {
                for (Map.Entry<String, List<LensDefSettings>> stringListEntry : result.entrySet()) {
                    List<LensDefSettings> value = stringListEntry.getValue();
                    String key = stringListEntry.getKey();
                    for (LensDefSettings lensDefSettings : value) {
                        vse.addAvailableLens(lensDefSettings);
                    }
                }
            }
        });
    }

    public void loadActiveLenses() {

        VortexFuture<List<LensDefSettings>> future = WebMain.injector.getVortex().createFuture();
        String dvuuid = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getUuid();
        future.execute(ViewerActionServiceProtocol.class).getLensConfiguration(dvuuid);
        future.addEventHandler(new AbstractVortexEventHandler<List<LensDefSettings>>() {
            @Override
            public void onSuccess(List<LensDefSettings> result) {
                vse.getModel().setLensDefSetting(result);
                for (LensDefSettings lensDefSettings : result) {
                    LensSettingsContainer lensSettingsContainer = new LensSettingsContainer(lensDefSettings, vse);
                    vse.getView().addSettingsContainer(lensSettingsContainer);
                    for (LensSettingsControl control : lensDefSettings.getControls()) {
                        VSEControl controlWidget = vse.buildControl(control);
                        lensSettingsContainer.add(controlWidget);
                    }
                }
            }
        });
    }
}
