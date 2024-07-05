package csi.client.gwt.viz.map.menu;

import csi.client.gwt.viz.map.metrics.MapMetricsView;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.map.settings.MapSettingsPresenter;
import csi.client.gwt.viz.matrix.ExpireMetrics;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.server.common.model.visualization.map.MapViewDef;

public class MapSettingsHandler extends AbstractMapMenuEventHandler implements
	SettingsActionCallback<MapViewDef> {

	private MapPresenter mapPresenter;
	public MapSettingsHandler(MapPresenter presenter, MapMenuManager menuManager) {
		super(presenter, menuManager);
		mapPresenter = presenter;
	}

	@Override
	public void onMenuEvent(CsiMenuEvent event) {
		MapSettingsPresenter presenter = new MapSettingsPresenter(this);
        presenter.setDataView(getPresenter().getDataView());
        presenter.setVisualizationDef(getPresenter().getVisualizationDef());
        presenter.setVisualization(getPresenter());
        presenter.show();
	}

	@Override
	public void onSaveComplete(MapViewDef vizDef, boolean suppressLoadAfterSave) {
		if (!suppressLoadAfterSave) {
			mapPresenter.f1();
            MapMetricsView.EVENT_BUS.fireEvent(new ExpireMetrics(mapPresenter.getUuid()));
        }
    }

	@Override
	public void onCancel() {
		// Noop
	}

}
