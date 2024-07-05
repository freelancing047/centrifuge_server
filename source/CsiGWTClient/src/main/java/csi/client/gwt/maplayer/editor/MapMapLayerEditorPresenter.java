package csi.client.gwt.maplayer.editor;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.model.map.Basemap;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class MapMapLayerEditorPresenter {
	private MapLayerListHolder listHolder;
	private EditMapLayerDialog dialog;
	private Basemap basemap;

	public MapMapLayerEditorPresenter(MapLayerListHolder listHolder) {
		this.listHolder = listHolder;
	}

	public void edit(Basemap newBasemap) {
		basemap = newBasemap;

		if (dialog == null) {
			dialog = new EditMapLayerDialog(this);
		}

		dialog.display(basemap);
		dialog.show();
	}

	public void saveModel(String owner, String name, String url, String type, String layername) {
		basemap.setOwner(owner);
		basemap.setName(name);
		basemap.setUrl(url);
		basemap.setType(type);
		basemap.setLayername(layername);

		VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
		try {
			vortexFuture.execute(MapActionsServiceProtocol.class).saveBasemap(basemap);
		} catch (Exception myException) {
			Dialog.showException(myException);
		}

		vortexFuture.addEventHandler(new MapLayerUpdateHandler(listHolder));
	}
	
	public boolean basemapNameExists(String ownerName, String basemapName) {
		return listHolder.basemapNameExists(ownerName, basemapName);
	}
	
	public void deleteBasemap() {
		listHolder.notifyBasemapDeleted(basemap);
	}
}
