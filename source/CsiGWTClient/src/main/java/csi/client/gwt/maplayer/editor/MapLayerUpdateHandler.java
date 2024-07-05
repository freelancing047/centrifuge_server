package csi.client.gwt.maplayer.editor;

import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.widget.boot.Dialog;

public class MapLayerUpdateHandler extends AbstractVortexEventHandler<Void> {
	private MapLayerListHolder listHolder;
	public MapLayerUpdateHandler(MapLayerListHolder listHolder) {
		this.listHolder = listHolder;
	}
	
	@Override
	public boolean onError(Throwable t) {
		Dialog.showException(t);
		return true;
	}

	@Override
	public void onSuccess(Void v) {
		listHolder.populateBasemapNames();
	}
}