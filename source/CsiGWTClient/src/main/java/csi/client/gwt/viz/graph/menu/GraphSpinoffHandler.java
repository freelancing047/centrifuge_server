package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.SpinoffDialog;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;

public class GraphSpinoffHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public GraphSpinoffHandler(Graph presenter, GraphMenuManager menuManager) {
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
		            Graph graph = (Graph) visualization;
		            String dataviewUuid = graph.getDataviewUuid();
		            String uuid = graph.getUuid();
		            SpinoffDialog spinoffDialog = new SpinoffDialog(dataviewUuid, uuid, null);
		            spinoffDialog.show();
		        }
		    }
		});
	}

	private void displayError() {
		ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spinoffHandler_ErrorTitle(), CentrifugeConstantsLocator.get().spinoffHandler_ErrorMessage());
		errorDialog.show();
	}
	
}
