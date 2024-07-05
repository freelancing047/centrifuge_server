package csi.server.business.visualization.map.mapsettings;

import csi.config.Configuration;
import csi.config.MapConfig;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTrack;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.visualization.map.TrackSettingsDTO;

import java.util.Optional;

public class TrackSettingsDTOBuilder {
    private MapTheme mapTheme;
    private MapTrack mapTrack;
    private MapSettings mapSettings;
    private TrackSettingsDTO trackSettings;
    private AssociationStyle style;
    private MapConfig mapConfig;

    TrackSettingsDTOBuilder(MapTheme mapTheme, MapTrack mapTrack, MapSettings mapSettings) {
        super();
        this.mapTheme = mapTheme;
        this.mapTrack = mapTrack;
        this.mapSettings = mapSettings;
        mapConfig = Configuration.getInstance().getMapConfig();
    }

    public void build() {
        init();
        trackSettings.setPlace(mapTrack.getPlace());
        setIdentity();
        setSequence();
        setupRenderInfo();
    }

    private void init() {
        trackSettings = new TrackSettingsDTO();
    }

    private void setIdentity() {
        if (mapTrack.isIdentityFixed()) {
            trackSettings.setIdentityName(getIdentityName());
        } else if (mapTrack.isIdentityDynamic()) {
            trackSettings.setIdentityColumn(mapTrack.getIdentityField().getLocalId().replace("-", "_"));
        } else if (mapTrack.isIdentityPlace()) {
            Optional<MapPlace> mapPlaceOptional = mapSettings.getMapPlaces().stream().filter(mapPlace -> mapPlace.getName().equals(mapTrack.getPlace())).findFirst();
            if (mapPlaceOptional.isPresent()) {
                MapPlace mapPlace = mapPlaceOptional.get();
                if (!mapPlace.isTypeFixed()) {
                    trackSettings.setIdentityColumn(mapPlace.getTypeField().getLocalId().replace("-", "_"));
                } else {
                    if (mapPlace.getTypeName() == null) {
                        trackSettings.setIdentityName(mapPlace.getName());
                    } else {
                        trackSettings.setIdentityName(mapPlace.getTypeName());
                    }
                }
            } else {
                //ERROR?
            }
        }
    }

    private String getIdentityName() {
        String identityName = mapTrack.getIdentityName();
        if (identityName == null)
            identityName = "";
        return identityName;
    }

    private void setSequence() {
        trackSettings.setSequenceColumn(mapTrack.getSequenceField().getLocalId().replace("-", "_"));
        trackSettings.setSequenceValueType(mapTrack.getSequenceField().getValueType().toString());
        trackSettings.setSequenceSortOrder(mapTrack.getSequenceSortOrder().toString());
    }

    private void setupRenderInfo() {
        trackSettings.setLineStyle(mapTrack.getStyleTypeString());
        style = getStyle();
        setWidth();
        setColor();
    }

    private AssociationStyle getStyle() {
        if (mapTrack.isIdentityFixed()) {
            String identityName = mapTrack.getIdentityName();
            if (mapTheme != null && identityName != null) {
                return mapTheme.getAssociationStyleMap().get(identityName);
            }
        }
        return null;
    }

    private void setWidth() {
        if (mapTrack.isUseDefaultWidthSetting()) {
            trackSettings.setWidthOverriden(false);
            if (style != null) {
                setWidth(style.getWidth().intValue());
            } else {
                setWidth(MapTrack.DEFAULT_TRACK_WIDTH);
            }
        } else {
            trackSettings.setWidthOverriden(true);
            setWidth(mapTrack.getWidth());
        }
    }

    private void setWidth(int width) {
        if (width > mapConfig.getMaxTrackWidth())
            width = mapConfig.getMaxTrackWidth();
        else if (width < mapConfig.getMinTrackWidth())
            width = mapConfig.getMinTrackWidth();
        trackSettings.setWidth(width);
    }

    private void setColor() {
        if (mapTrack.isUseDefaultColorSetting()) {
            trackSettings.setColorOverriden(false);
            handleDefaultColorSetting();
        } else {
            trackSettings.setColorOverriden(true);
            colorSettingAsAppearedOnView();
        }
    }

    private void handleDefaultColorSetting() {
        if (style != null)
            setColorUsingStyle();
        else
            colorSettingAsAppearedOnView();
    }

    private void setColorUsingStyle() {
        String color = ClientColorHelper.get().make(style.getColor()).toString();
        trackSettings.setColorString(color.replace("#", ""));
    }

    private void colorSettingAsAppearedOnView() {
        trackSettings.setColorString(mapTrack.getColorString().replace("#", ""));
    }

    public TrackSettingsDTO getTrackSettings() {
        return trackSettings;
    }
}
