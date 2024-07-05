package csi.client.gwt.maplayer.editor;

import csi.server.common.model.map.Basemap;

public interface MapLayerListHolder {
	void populateBasemapNames();
	boolean basemapNameExists(String ownerName, String basemapName);
	void notifyBasemapDeleted(Basemap basemap);
}
