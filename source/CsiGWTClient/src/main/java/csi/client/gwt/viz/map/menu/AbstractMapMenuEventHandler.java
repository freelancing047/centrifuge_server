package csi.client.gwt.viz.map.menu;

import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;

public abstract class AbstractMapMenuEventHandler  extends
	AbstractMenuEventHandler<MapPresenter, MapMenuManager> {

	public AbstractMapMenuEventHandler(MapPresenter presenter, MapMenuManager menuManager) {
        super(presenter, menuManager);
    }
}
