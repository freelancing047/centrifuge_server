package csi.server.business.visualization.map.mapsettings;

import com.google.common.collect.Lists;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTrack;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;

import java.util.List;

public class TrackInfoAggregator {
    private MapSettings mapSettings;
    private MapTheme mapTheme;
    private List<TrackSettingsDTO> trackSettingsDTOs;

    public TrackInfoAggregator(MapSettings mapSettings, MapTheme mapTheme) {
        this.mapSettings = mapSettings;
        this.mapTheme = mapTheme;
    }

    public void aggregate() {
        init();
        mapSettings.getMapTracks().forEach(mapTrack -> aggregate(mapTrack));
    }

    private void init() {
        trackSettingsDTOs = Lists.newArrayList();
    }

    public void aggregate(MapTrack mapTrack) {
        TrackSettingsDTOBuilder builder = new TrackSettingsDTOBuilder(mapTheme, mapTrack, mapSettings);
        builder.build();
        trackSettingsDTOs.add(builder.getTrackSettings());
    }

    public void fillMapSettingsDTO(MapSettingsDTO mapSettingsDTO) {
        mapSettingsDTO.setTrackSettings(trackSettingsDTOs);
    }
}