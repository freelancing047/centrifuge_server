package csi.server.business.visualization.map.mapsettings;

import com.google.common.collect.Lists;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapAssociation;
import csi.server.common.model.visualization.map.MapSettings;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.visualization.map.AssociationSettingsDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;

import java.util.List;

public class AssociationInfoAggregator {
    private MapSettings mapSettings;
    private MapTheme mapTheme;
    private List<AssociationSettingsDTO> associationSettings;

    AssociationInfoAggregator(MapSettings mapSettings, MapTheme mapTheme) {
        this.mapSettings = mapSettings;
        this.mapTheme = mapTheme;
    }

    public void aggregate() {
        init();
        for (MapAssociation mapAssociation : mapSettings.getMapAssociations()) {
            AssociationSettingsDTO associationSettingsDTO = new AssociationSettingsDTO();
            associationSettingsDTO.setName(mapAssociation.getName());
            associationSettingsDTO.setSource(mapAssociation.getSource());
            associationSettingsDTO.setDestination(mapAssociation.getDestination());
            associationSettingsDTO.setLineStyle(mapAssociation.getStyleTypeString());
            AssociationStyle style = getStyle(mapAssociation);
            setWidth(mapAssociation, style, associationSettingsDTO);
            setColor(mapAssociation, style, associationSettingsDTO);
            associationSettingsDTO.setShowDirection(mapAssociation.isShowDirection());
            associationSettings.add(associationSettingsDTO);
        }
    }

    private void init() {
        associationSettings = Lists.newArrayList();
    }

    private AssociationStyle getStyle(MapAssociation mapAssociation) {
        if (mapTheme != null) {
            return mapTheme.getAssociationStyleMap().get(mapAssociation.getName());
        } else {
            return null;
        }
    }

    private void setWidth(MapAssociation mapAssociation, AssociationStyle style,
                          AssociationSettingsDTO associationSettings) {
        if (mapAssociation.isUseDefaultWidthSetting() && style != null)
            setWidthUsingStyle(style, associationSettings);
        else
            associationSettings.setWidth(mapAssociation.getWidth());
    }

    private void setWidthUsingStyle(AssociationStyle style, AssociationSettingsDTO associationSettings) {
        int width = style.getWidth().intValue();
        if (width > 5)
            width = 5;
        else if (width < 1)
            width = 1;
        associationSettings.setWidth(width);
    }

    private void setColor(MapAssociation mapAssociation, AssociationStyle style, AssociationSettingsDTO associationSettings) {
        if (mapAssociation.isUseDefaultColorSetting() && style != null)
            setColorUsingStyle(style, associationSettings);
        else
            associationSettings.setColorString(mapAssociation.getColorString().replace("#", ""));
    }

    private void setColorUsingStyle(AssociationStyle style, AssociationSettingsDTO associationSettings) {
        String color = ClientColorHelper.get().make(style.getColor()).toString();
        associationSettings.setColorString(color.replace("#", ""));
    }

    void fillMapSettingsDTO(MapSettingsDTO mapSettingsDTO) {
        mapSettingsDTO.setAssociationSettings(associationSettings);
    }
}