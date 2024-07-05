package csi.client.gwt.viz.map.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.dialogs.ExtractTableDialog;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.model.visualization.selection.DetailMapSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.MapActionsServiceProtocol;

/**
 * Created by centrifuge on 5/9/2018.
 */
public class MapSpawnTableHandler extends AbstractMenuEventHandler {

    MapSpawnTableHandler(MapPresenter presenter, MapMenuManager menuManager) {
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
                boolean doSpawn = true;
                if (result instanceof DetailMapSelection) {
                    DetailMapSelection detailMapSelection = (DetailMapSelection) result;
                    if (detailMapSelection.getNodes().isEmpty() && detailMapSelection.getLinks().isEmpty()) {
                        doSpawn = false;
                    }
                } else {
                    doSpawn = false;
                }

                if (doSpawn) {
                    try {
                        String dataViewUuid = getPresenter().getDataViewUuid();
                        String uuid = getPresenter().getUuid();
                        ExtractTableDialog extractTableDialog = new ExtractTableDialog(dataViewUuid, uuid, result);
                        extractTableDialog.show();
                    } catch (Exception myException) {
                        Display.error("MapSpawnTableHandler", 1, myException);
                    }
                } else {
                    ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spawnTableHandler_ErrorTitle(), CentrifugeConstantsLocator.get().spawnTableHandler_ErrorMessage());
                    errorDialog.show();
                }
            }
        });
    }

}

