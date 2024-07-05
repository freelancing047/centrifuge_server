package csi.client.gwt.viz.map.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class HideMapLegendHandler extends AbstractMenuEventHandler<MapPresenter, MapMenuManager> {
	public HideMapLegendHandler(MapPresenter presenter, MapMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().setLegendShown(false);
        getPresenter().hideLegend();
    }
}
