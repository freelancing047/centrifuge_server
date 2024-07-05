package csi.client.gwt.viz.map.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class ShowMapLegendHandler extends AbstractMenuEventHandler<MapPresenter, MapMenuManager> {
	public ShowMapLegendHandler(MapPresenter presenter, MapMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().setLegendShown(true);
        getPresenter().showLegend();
    }
}
