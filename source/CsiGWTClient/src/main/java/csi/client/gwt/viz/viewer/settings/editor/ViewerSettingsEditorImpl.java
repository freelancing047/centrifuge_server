package csi.client.gwt.viz.viewer.settings.editor;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import csi.client.gwt.WebMain;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.viewer.settings.ViewerSettings;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.ViewerActionServiceProtocol;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

public class ViewerSettingsEditorImpl implements ViewerSettingsEditor {

    private final EventBus eventBus;
    private final ViewerSettingsEditorView view;
    private final CSIActivityManager activityManager;
    private ViewerSettingsEditorModel model;


    public ViewerSettingsEditorImpl(ViewerSettings viewerSettings) {
        view = new ViewerSettingsEditorViewImpl(this);
        this.eventBus = new SimpleEventBus();
        this.activityManager = new CSIActivityManager(new ViewerSettingsActivityMapper(), eventBus);
        model = new ViewerSettingsEditorModelImpl(viewerSettings);
        activityManager.setActivity(new Show(this));

    }

    @Override
    public ViewerSettingsEditorModel getModel() {
        return model;
    }

    @Override
    public ViewerSettingsEditorView getView() {
        return view;
    }

    @Override
    public VSEControl buildControl(LensSettingsControl control) {
        switch (control.getType()) {
            case "list":
                return new VSEListControl(control);
            case "multilist":
                return new VSEMultiListControl(control);
            case "field":
                return new VseFdcbControl(control);
            default:
                break;
        }
        return null;
    }

    @Override
    public void addAvailableLens(LensDefSettings lensDefSettings) {
        AvailableLensControl control = new AvailableLensControl(lensDefSettings);
        control.setVse(this);
        view.addAvailableLens(control);
    }

    @Override
    public void addLens(LensDefSettings lensDefSettings) {
        activityManager.setActivity(new AddLens(this, lensDefSettings));
    }

    @Override
    public void updateModel() {
        //FIXME:
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        String dvuuid = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getUuid();

        //FIXME: server side validation of settings?
        future.execute(ViewerActionServiceProtocol.class).updateSettings(model.getLensDefSettingsList(), dvuuid);
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                view.hide();
            }
        });
    }

    @Override
    public void removeLens(LensDefSettings lensDefSettings) {
        activityManager.setActivity(new RemoveLens(this, lensDefSettings));
    }

    private static class ViewerSettingsActivityMapper implements ActivityMapper {
        @Override
        public Activity getActivity(Place place) {
            return null;
        }
    }
}
