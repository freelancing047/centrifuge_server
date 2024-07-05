package csi.server.business.visualization.map.mapsettings;

import java.util.List;

import com.google.common.collect.Lists;

import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapSettings;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class PlaceInfoAggregator {
	private MapSettings mapSettings;
	private MapTheme mapTheme;
	private List<PlaceSettingsDTO> placeSettings;

	public PlaceInfoAggregator(MapSettings mapSettings, MapTheme mapTheme) {
		this.mapSettings = mapSettings;
		this.mapTheme = mapTheme;
	}

	public void aggregate() {
		init();
		for (MapPlace mapPlace : mapSettings.getMapPlaces())
			aggregate(mapPlace);
	}

	private void init() {
		placeSettings = Lists.newArrayList();
	}

	private void aggregate(MapPlace mapPlace) {
		PlaceSettingsDTOBuilder builder = new PlaceSettingsDTOBuilder(mapTheme, mapPlace);
		builder.build();
		placeSettings.add(builder.getPlaceSettings());
	}

	public void fillMapSettingsDTO(MapSettingsDTO mapSettingsDTO) {
		mapSettingsDTO.setPlaceSettings(placeSettings);
	}
}