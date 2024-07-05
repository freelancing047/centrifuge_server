package csi.server.business.visualization.map;

import csi.server.business.visualization.map.mapserviceutil.MapBundleInfo;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.HeatMapInfo;
import csi.server.common.model.visualization.selection.Selection;

public class MapContext {
	public static ThreadLocal<MapContext> Current = new ThreadLocal<MapContext>();
	private boolean invalidated = false;
	private String dvUuid;
	private String vizUuid;
	private Selection selection;
	private boolean legendShown = true;
	private boolean multitypeDecoratorShown = false;
	private boolean linkupDecoratorShown = false;
	private HeatMapInfo heatMapInfo;
	private MapBundleInfo mapBundleInfo;

	public MapContext(String dvUuid, String vizUuid) {
		this.invalidated = false;
		this.dvUuid = dvUuid;
		this.vizUuid = vizUuid;
	}

	public boolean isInvalidated() {
		return invalidated;
	}

	public void setInvalidated(boolean invalidated) {
		this.invalidated = invalidated;
	}

	public String getDvUuid() {
		return dvUuid;
	}

	public String getVizUuid() {
		return vizUuid;
	}

	public Selection getSelection() {
		return selection;
	}

	public void setSelection(Selection selection) {
		this.selection = selection;
	}

	public boolean isLegendShown() {
		return legendShown;
	}

	public void setLegendShown(boolean legendShown) {
		this.legendShown = legendShown;
	}

	public boolean isMultitypeDecoratorShown() {
		return multitypeDecoratorShown;
	}

	public void setMultitypeDecoratorShown(boolean multitypeDecoratorShown) {
		this.multitypeDecoratorShown = multitypeDecoratorShown;
	}

	public boolean isLinkupDecoratorShown() {
		return linkupDecoratorShown;
	}

	public void setLinkupDecoratorShown(boolean linkupDecoratorShown) {
		this.linkupDecoratorShown = linkupDecoratorShown;
	}

	public HeatMapInfo getHeatMapInfo() {
		return heatMapInfo;
	}

	public void setHeatMapInfo(HeatMapInfo heatMapInfo) {
		this.heatMapInfo = heatMapInfo;
	}

	public MapBundleInfo getMapBundleInfo() {
		return mapBundleInfo;
	}

	public void setMapBundleInfo(MapBundleInfo mapBundleInfo) {
		this.mapBundleInfo = mapBundleInfo;
	}

}
