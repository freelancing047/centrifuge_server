package csi.client.gwt.viz.map.legend;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class MapLegendActivityMapper implements ActivityMapper {
	private MapLegend mapLegend;
	
	public MapLegendActivityMapper(MapLegend mapLegend) {
        this.mapLegend = mapLegend;
    }

	@Override
	public Activity getActivity(Place place) {
		return new ShowMapLegend(mapLegend);
	}
}
