package csi.client.gwt.viz.map.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.map.presenter.MapLegendContainer;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class ResetMapLegendHandler extends AbstractMenuEventHandler<MapPresenter, MapMenuManager> {
	private MapLegendContainer map;

	public ResetMapLegendHandler(MapPresenter presenter, MapMenuManager menuManager) {
		super(presenter, menuManager);
		map = presenter;

	}

	@Override
	public void onMenuEvent(CsiMenuEvent event) {
    	WebMain.injector.getVortex().execute(MapActionsServiceProtocol.class).setLegendShown(getPresenter().getDataViewUuid(), getPresenter().getVisualizationDef().getUuid(), true);
		map.getMapLegend().setLegendPositionAnchored(true);
		map.showLegend();
	}
}
