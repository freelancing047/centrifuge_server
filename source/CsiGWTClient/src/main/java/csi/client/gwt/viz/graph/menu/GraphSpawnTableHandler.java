package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.dialogs.ExtractTableDialog;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;

/**
 * Created by centrifuge on 5/8/2018.
 */
public class GraphSpawnTableHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public GraphSpawnTableHandler(Graph presenter, GraphMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        Visualization visualization = getPresenter();
        WebMain.injector.getVortex().execute(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean hasSelection) {
                if (hasSelection) {
                    proceed();
                } else {
                    displayError();
                }
            }
        }, VisualizationActionsServiceProtocol.class).isSelectionAvailable(visualization.getDataViewUuid(), visualization.getVisualizationDef());
    }

    private void proceed() {
        VortexFuture<SelectionModel> selectionModel = getPresenter().getModel().getSelectionModel();
        selectionModel.addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {
            @Override
            public void onSuccess(SelectionModel result) {
                Visualization visualization = getPresenter();
                if (visualization instanceof Graph) {

                    try {

                        Graph graph = (Graph) visualization;
                        String dataviewUuid = graph.getDataviewUuid();
                        String uuid = graph.getUuid();
                        ExtractTableDialog extractTableDialog = new ExtractTableDialog(dataviewUuid, uuid, null);
                        extractTableDialog.show();

                    } catch (Exception myException) {

                        Display.error("GraphSpawnTableHandler", 1, myException);
                    }
                }
            }
        });
    }

    private void displayError() {
        ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spawnTableHandler_ErrorTitle(),
                                                    CentrifugeConstantsLocator.get().spawnTableHandler_ErrorMessage());
        errorDialog.show();
    }

}
