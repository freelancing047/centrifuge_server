package csi.client.gwt.viz.map.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.SpinoffDialog;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.model.visualization.selection.DetailMapSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class MapSpinOffHandler extends AbstractMenuEventHandler {

    MapSpinOffHandler(MapPresenter presenter, MapMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        // do some magic here.
        VortexFuture<Selection> vortexFuture = WebMain.injector.getVortex().createFuture();
        vortexFuture.execute(MapActionsServiceProtocol.class).getSelection(getPresenter().getDataViewUuid(), getPresenter().getUuid());
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<Selection>() {
            @Override
            public void onSuccess(Selection result) {
                boolean doSpinOff = true;
                if (result instanceof DetailMapSelection) {
                    DetailMapSelection detailMapSelection = (DetailMapSelection) result;
                    if (detailMapSelection.getNodes().isEmpty() && detailMapSelection.getLinks().isEmpty()) {
                        doSpinOff = false;
                    }
                } else {
                    doSpinOff = false;
                }

                if (doSpinOff) {
                    String dataViewUuid = getPresenter().getDataViewUuid();
                    String uuid = getPresenter().getUuid();
                    SpinoffDialog spinoffDialog = new SpinoffDialog(dataViewUuid, uuid, result);
                    spinoffDialog.show();
                } else {
                    ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spinoffHandler_ErrorTitle(), CentrifugeConstantsLocator.get().spinoffHandler_ErrorMessage());
                    errorDialog.show();
                }
            }
        });
    }

}

