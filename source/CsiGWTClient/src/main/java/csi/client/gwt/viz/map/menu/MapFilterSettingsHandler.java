package csi.client.gwt.viz.map.menu;

import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;

import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.filter.FilterSettingsHandler;
import csi.client.gwt.viz.shared.filter.ManageFilterDialog;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.filter.Filter;

public class MapFilterSettingsHandler extends FilterSettingsHandler<MapPresenter> {
	private MapPresenter mapPresenter;

	public MapFilterSettingsHandler(MapPresenter presenter, AbstractMenuManager<MapPresenter> menuManager) {
		super(presenter, menuManager);
		mapPresenter = presenter;
	}

	@Override
    public void onMenuEvent(CsiMenuEvent event) {
        Filter filter = mapPresenter.getVisualizationDef().getFilter();
        String dataViewUuid = mapPresenter.getDataViewUuid();
        final ManageFilterDialog dialog = new ManageFilterDialog(filter, dataViewUuid);
        dialog.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent hideEvent) {
                mapPresenter.getVisualizationDef().setFilter(dialog.getSelectedFilter());
                VortexFuture<Void> future = getPresenter().saveSettings(false);
                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
					@Override
					public void onSuccess(Void result) {
						mapPresenter.f1();
					}
                });
            }
        });
        dialog.show();
    }
}
