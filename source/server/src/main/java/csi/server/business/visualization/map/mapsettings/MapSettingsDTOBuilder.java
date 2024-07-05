package csi.server.business.visualization.map.mapsettings;

import java.util.ArrayList;
import java.util.List;

import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapBundleDefinition;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTooltipField;
import csi.shared.core.visualization.map.MapBundleDefinitionDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapTooltipFieldDTO;

public class MapSettingsDTOBuilder {
   private DataView dataView;
   private MapSettings mapSettings;
   private MapTheme mapTheme;
   private MapSettingsDTO mapSettingsDTO;

   public MapSettingsDTOBuilder(DataView dataView, MapSettings mapSettings, MapTheme mapTheme) {
      this.dataView = dataView;
      this.mapSettings = mapSettings;
      this.mapTheme = mapTheme;
   }

   public void build() {
      init();
      populateMapSettingsValues();
      populateHeatmapInfo();
      populateBundlemapInfo();
      populateTrackmapInfo();
      populatePlaceInfo();
      populateAssociationInfo();
      populateTrackInfo();
   }

   private void init() {
      mapSettingsDTO = new MapSettingsDTO();
   }

   private void populateMapSettingsValues() {
      if (mapSettings.getWeightField() != null) {
         mapSettingsDTO.setWeightColumn(mapSettings.getWeightField().getLocalId().replace("-", "_"));
      }
      setPlaceTooltipFields();
   }

   private void setPlaceTooltipFields() {
      mapSettingsDTO.setPlaceTooltipFields(createPlaceTooltipFields());
   }

   private List<List<MapTooltipFieldDTO>> createPlaceTooltipFields() {
      List<List<MapTooltipFieldDTO>> placeTooltipFields = new ArrayList<List<MapTooltipFieldDTO>>();

      mapSettings.getMapPlaces().forEach(mapPlace -> placeTooltipFields.add(getPlaceTooltipFieldsFromMapPlace(mapPlace)));
      return placeTooltipFields;
   }

   private List<MapTooltipFieldDTO> getPlaceTooltipFieldsFromMapPlace(MapPlace mapPlace) {
      List<MapTooltipFieldDTO> tooltipFieldDTOs = new ArrayList<MapTooltipFieldDTO>();

      for (MapTooltipField tooltipField : mapPlace.getTooltipFields()) {
         FieldDef fieldDef = tooltipField.getFieldDef(dataView.getMeta().getModelDef());

         tooltipFieldDTOs.add(createPlaceTooltipFieldFromFieldDef(fieldDef));
      }
      return tooltipFieldDTOs;
   }

   private MapTooltipFieldDTO createPlaceTooltipFieldFromFieldDef(FieldDef fieldDef) {
      MapTooltipFieldDTO mapTooltipFieldDTO = new MapTooltipFieldDTO();

      mapTooltipFieldDTO.setFieldColumn(fieldDef.getLocalId().replace("-", "_"));
      mapTooltipFieldDTO.setFieldName(fieldDef.getFieldName());
      return mapTooltipFieldDTO;
   }

   private void populateHeatmapInfo() {
      mapSettingsDTO.setUseHeatMap(mapSettings.isUseHeatMap());
      mapSettingsDTO.setHeatmapColors(getHeatmapColors());
   }

   private List<String> getHeatmapColors() {
      List<String> heatmapColors = new ArrayList<String>();

      heatmapColors.add(mapSettings.getColorModel().getColor(0, 0, 1));
      heatmapColors.add(mapSettings.getColorModel().getColor(.25, 0, 1));
      heatmapColors.add(mapSettings.getColorModel().getColor(.5, 0, 1));
      heatmapColors.add(mapSettings.getColorModel().getColor(.75, 0, 1));
      heatmapColors.add(mapSettings.getColorModel().getColor(1, 0, 1));
      return heatmapColors;
   }

   private void populateBundlemapInfo() {
      mapSettingsDTO.setBundleUsed(mapSettings.isBundleUsed());
      setMapBundleDefinitions(mapSettingsDTO, mapSettings);
   }

   private void setMapBundleDefinitions(MapSettingsDTO mapSettingsDTO, MapSettings mapSettings) {
      List<MapBundleDefinitionDTO> mapBundleDefinitionDTOs = new ArrayList<MapBundleDefinitionDTO>();

      mapSettings.getMapBundleDefinitions()
                 .forEach(mapBundleDefinition -> mapBundleDefinitionDTOs.add(createMapBundleDefinitionDTO(mapBundleDefinition)));
      mapSettingsDTO.setMapBundleDefinitions(mapBundleDefinitionDTOs);
   }

   private MapBundleDefinitionDTO createMapBundleDefinitionDTO(MapBundleDefinition mapBundleDefinition) {
      MapBundleDefinitionDTO mapBundleDefinitionDTO = new MapBundleDefinitionDTO();

      mapBundleDefinitionDTO.setFieldName(mapBundleDefinition.getFieldDef().getFieldName());
      mapBundleDefinitionDTO.setFieldColumn(mapBundleDefinition.getFieldDef().getLocalId().replace("-", "_"));
      mapBundleDefinitionDTO.setShapeString(mapBundleDefinition.getShapeString());
      mapBundleDefinitionDTO.setColorString(mapBundleDefinition.getColorString());
      mapBundleDefinitionDTO.setShowLabel(mapBundleDefinition.getShowLabel().booleanValue());
      mapBundleDefinitionDTO.setAllowNulls(mapBundleDefinition.isAllowNulls());
      return mapBundleDefinitionDTO;
   }

   private void populateTrackmapInfo() {
      mapSettingsDTO.setUseTrackMap(mapSettings.isUseTrack());
      mapSettingsDTO.setNodeTransparency(mapSettings.getNodeTransparency());

      if (mapSettings.isUseTrack() && mapSettings.getMapTracks().get(0).isUseDefaultOpacity()) {
         mapSettingsDTO.setNodeTransparency(MapSettings.DEFAULT_PLACE_OPACITY);
      }
   }

   private void populatePlaceInfo() {
      setDefaultShapeString();

      PlaceInfoAggregator aggregator = new PlaceInfoAggregator(mapSettings, mapTheme);

      aggregator.aggregate();
      aggregator.fillMapSettingsDTO(mapSettingsDTO);
   }

   private void setDefaultShapeString() {
      ShapeType shapeType = null;

      if (mapTheme != null) {
         shapeType = mapTheme.getDefaultShape();
      }
      if ((shapeType != null) && (shapeType != ShapeType.NONE)) {
         mapSettingsDTO.setDefaultShapeString(shapeType.toString());
      } else {
         mapSettingsDTO.setDefaultShapeString(ShapeType.CIRCLE.toString());
      }
   }

   private void populateAssociationInfo() {
      AssociationInfoAggregator aggregator = new AssociationInfoAggregator(mapSettings, mapTheme);

      aggregator.aggregate();
      aggregator.fillMapSettingsDTO(mapSettingsDTO);
   }

   private void populateTrackInfo() {
      TrackInfoAggregator aggregator = new TrackInfoAggregator(mapSettings, mapTheme);

      aggregator.aggregate();
      aggregator.fillMapSettingsDTO(mapSettingsDTO);
   }

   public MapSettingsDTO getMapSettingsDTO() {
      return mapSettingsDTO;
   }
}
