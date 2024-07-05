package csi.client.gwt.viz.matrix.menu;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.SpinoffDialog;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;

public class MatrixSpinoffHandler extends AbstractMatrixMenuEventHandler {

    public MatrixSpinoffHandler(MatrixPresenter presenter, MatrixMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        final MatrixPresenter matrixPresenter = getPresenter();
        matrixPresenter.saveViewStateToVisualizationDef();
        if (!matrixPresenter.hasSelection()) {
            ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spinoffHandler_ErrorTitle(), CentrifugeConstantsLocator.get().spinoffHandler_ErrorMessage());
            errorDialog.show();
            return;
        }
        VortexFuture<Void> saveSettings = matrixPresenter.saveSettings(false, false, false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                String dataviewUuid = matrixPresenter.getDataViewUuid();
                String uuid = matrixPresenter.getUuid();
                SpinoffDialog spinoffDialog = new SpinoffDialog(dataviewUuid, uuid, matrixPresenter.getVisualizationDef().getSelection());
                spinoffDialog.show();
            }
        });
    }

}
