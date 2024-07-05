package csi.client.gwt.viz.map.menu;

import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.CreateSelectionFilterDialog;
import csi.client.gwt.viz.shared.menu.CreateSelectionFilterHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;

public class MapCreateSelectionFilterHandler extends CreateSelectionFilterHandler<MapPresenter, MapMenuManager>{

	public MapCreateSelectionFilterHandler(MapPresenter presenter, MapMenuManager menuManager) {
		super(presenter, menuManager);
	}
	
	@Override
	public void proceed() {
		VortexFuture<Void> saveSettings = getPresenter().saveSettingsOnly(false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                CreateSelectionFilterDialog CreateSelectionFilterDialog = new CreateSelectionFilterDialog(getPresenter());
                CreateSelectionFilterDialog.show();
            }
        });
	}

}
