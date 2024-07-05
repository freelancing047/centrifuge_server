package csi.client.gwt.viz.map.menu;

import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;

public class SaveMenuHandler extends AbstractMapMenuEventHandler {
	public SaveMenuHandler(MapPresenter presenter, MapMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
    	getPresenter().saveSettingsOnly(false);
    }
}
