package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.csiwizard.dialogs.ExtractTableDialog;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;

/**
 * Created by centrifuge on 5/9/2018.
 */
public class TimelineSpawnTableHandler extends AbstractTimelineMenuEventHandler{

    public TimelineSpawnTableHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().saveViewStateToVisualizationDef();
        if (!getPresenter().hasSelection()) {
            ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spawnTableHandler_ErrorTitle(), CentrifugeConstantsLocator.get().spawnTableHandler_ErrorMessage());
            errorDialog.show();
            return;
        }

        VortexFuture<Void> saveSettings = getPresenter().saveSettings(false, false, false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                try {

                    String dataviewUuid = getPresenter().getDataViewUuid();
                    String uuid = getPresenter().getUuid();
                    ExtractTableDialog spinoffDialog = new ExtractTableDialog(dataviewUuid, uuid, getPresenter().getVisualizationDef().getSelection());
                    spinoffDialog.show();

                } catch (Exception myException) {

                    Display.error("TimelineSpawnTableHandler", 1, myException);
                }
            }
        });
    }

}
