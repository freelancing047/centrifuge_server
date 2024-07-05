package csi.client.gwt.viz.matrix.menu;

import csi.client.gwt.csiwizard.dialogs.ExtractTableDialog;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;

/**
 * Created by centrifuge on 5/9/2018.
 */
public class MatrixSpawnTableHandler extends AbstractMatrixMenuEventHandler {

    public MatrixSpawnTableHandler(MatrixPresenter presenter, MatrixMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        final MatrixPresenter matrixPresenter = getPresenter();
        matrixPresenter.saveViewStateToVisualizationDef();
        if (!matrixPresenter.hasSelection()) {
            ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spawnTableHandler_ErrorTitle(),
                                                        CentrifugeConstantsLocator.get().spawnTableHandler_ErrorMessage());
            errorDialog.show();
            return;
        }
        VortexFuture<Void> saveSettings = matrixPresenter.saveSettings(false, false, false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                try {

                    String dataviewUuid = matrixPresenter.getDataViewUuid();
                    String uuid = matrixPresenter.getUuid();
                    ExtractTableDialog extractTableDialog = new ExtractTableDialog(dataviewUuid, uuid, matrixPresenter.getVisualizationDef().getSelection());
                    extractTableDialog.show();

                } catch (Exception myException) {

                    Display.error("MatrixSpawnTableHandler", 1, myException);
                }
            }
        });
    }

}
