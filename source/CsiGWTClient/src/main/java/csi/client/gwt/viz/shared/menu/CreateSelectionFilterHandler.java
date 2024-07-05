package csi.client.gwt.viz.shared.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.CreateSelectionFilterDialog;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;

public class CreateSelectionFilterHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends AbstractMenuEventHandler<V, M> {
	private Visualization visualization;
    public CreateSelectionFilterHandler(V presenter, M menuManager) {
        super(presenter, menuManager);
        visualization = presenter;
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        visualization.saveViewStateToVisualizationDef();
        if (visualization instanceof SelectionOnlyOnServer) {
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

    	} else {
            if(visualization.hasSelection()){
                proceed();
            } else {
            	displayError();
            }
    	}
    }

	public void proceed() {
		VortexFuture<Void> saveSettings = visualization.saveSettings(false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                CreateSelectionFilterDialog CreateSelectionFilterDialog = new CreateSelectionFilterDialog(visualization);
                CreateSelectionFilterDialog.show();
            }
        });
	}

	private void displayError() {
		ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().createSelectionFilterHandler_ErrorTitle(), CentrifugeConstantsLocator.get().createSelectionFilterHandler_ErrorMessage());
		errorDialog.show();
	}
}
