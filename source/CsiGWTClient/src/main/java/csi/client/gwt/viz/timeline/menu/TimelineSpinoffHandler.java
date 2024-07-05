package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.SpinoffDialog;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;

public class TimelineSpinoffHandler extends AbstractTimelineMenuEventHandler{

    public TimelineSpinoffHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().saveViewStateToVisualizationDef();
        if (!getPresenter().hasSelection()) {
            ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spinoffHandler_ErrorTitle(), CentrifugeConstantsLocator.get().spinoffHandler_ErrorMessage());
            errorDialog.show();
            return;
        }
        
        VortexFuture<Void> saveSettings = getPresenter().saveSettings(false, false, false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                String dataviewUuid = getPresenter().getDataViewUuid();
                String uuid = getPresenter().getUuid();
                SpinoffDialog spinoffDialog = new SpinoffDialog(dataviewUuid, uuid, getPresenter().getVisualizationDef().getSelection());
                spinoffDialog.show();
            }
        });
    }

}
