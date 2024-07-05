package csi.server.common.model.map.map;

import java.io.Serializable;

import csi.server.common.model.map.Basemap;

public class MapLayerDTO implements Serializable {
	private Basemap basemap;
	private boolean canEdit;

	public MapLayerDTO() {
		super();
	}

	public Basemap getBasemap() {
		return basemap;
	}

	public void setBasemap(Basemap basemap) {
		this.basemap = basemap;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}
}
