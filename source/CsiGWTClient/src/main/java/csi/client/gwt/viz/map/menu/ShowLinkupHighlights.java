package csi.client.gwt.viz.map.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class ShowLinkupHighlights extends AbstractMenuEventHandler<MapPresenter, MapMenuManager> {
	public ShowLinkupHighlights(MapPresenter presenter, MapMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
		VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
		vortexFuture.execute(MapActionsServiceProtocol.class).setLinkupDecoratorShown(getPresenter().getDataViewUuid(), getPresenter().getVisualizationDef().getUuid(), true);
		vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
			@Override
			public void onSuccess(Void result) {
				getPresenter().applySelection();
			}
		});
    }
}
