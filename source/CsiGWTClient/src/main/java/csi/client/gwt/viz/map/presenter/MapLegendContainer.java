package csi.client.gwt.viz.map.presenter;

import csi.client.gwt.viz.map.legend.MapLegend;

public interface MapLegendContainer {
	MapLegend getMapLegend();
	void hideLegend();
	void showLegend();
}
