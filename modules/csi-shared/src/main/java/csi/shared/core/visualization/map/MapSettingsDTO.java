package csi.shared.core.visualization.map;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MapSettingsDTO {
    private boolean useHeatMap;
    private List<String> heatmapColors = Lists.newArrayList();
    private boolean bundleUsed;
    private List<MapBundleDefinitionDTO> mapBundleDefinitions;
    private String weightColumn = null;
    private boolean useTrackMap;

    private Set<String> placeNameSet;
    private String defaultShapeString;
    private List<List<MapTooltipFieldDTO>> placeTooltipFields;
    private List<PlaceSettingsDTO> placeSettings;
    private List<AssociationSettingsDTO> associationSettings;
    private List<TrackSettingsDTO> trackSettings;

    private boolean placeTypeFixed = true;
    private boolean trackTypeFixed = true;
    private String uuid;

    private float nodeTransparency = 1.0f;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isUseHeatMap() {
        return useHeatMap;
    }

    public void setUseHeatMap(boolean useHeatMap) {
        this.useHeatMap = useHeatMap;
    }

    public List<String> getHeatmapColors() {
        return heatmapColors;
    }

    public void setHeatmapColors(List<String> heatmapColors) {
        this.heatmapColors = heatmapColors;
    }

    public boolean isBundleUsed() {
        return bundleUsed;
    }

    public void setBundleUsed(boolean bundleUsed) {
        this.bundleUsed = bundleUsed;
    }

    public List<MapBundleDefinitionDTO> getMapBundleDefinitions() {
        return mapBundleDefinitions;
    }

    public void setMapBundleDefinitions(List<MapBundleDefinitionDTO> mapBundleDefinitions) {
        this.mapBundleDefinitions = mapBundleDefinitions;
    }

    public String getWeightColumn() {
        return weightColumn;
    }

    public void setWeightColumn(String weightColumn) {
        this.weightColumn = weightColumn;
    }

    public boolean isUseTrackMap() {
        return useTrackMap && !trackSettings.isEmpty();
    }

    public void setUseTrackMap(boolean useTrackMap) {
        this.useTrackMap = useTrackMap;
    }

    public boolean isPlaceName(String typeName) {
        return placeNameSet.contains(typeName);
    }

    public List<List<MapTooltipFieldDTO>> getTooltipFields() {
        return placeTooltipFields;
    }

    public void setPlaceTooltipFields(List<List<MapTooltipFieldDTO>> tooltipFields) {
        this.placeTooltipFields = tooltipFields;
    }

    public String getDefaultShapeString() {
        return defaultShapeString;
    }

    public void setDefaultShapeString(String defaultShapeTypeString) {
        this.defaultShapeString = defaultShapeTypeString;
    }

    public List<PlaceSettingsDTO> getPlaceSettings() {
        return placeSettings;
    }

    public void setPlaceSettings(List<PlaceSettingsDTO> placeSettings) {
        this.placeSettings = placeSettings;
        placeNameSet = new TreeSet<>();
        for (PlaceSettingsDTO placeSetting : placeSettings)
            placeNameSet.add(placeSetting.getName());
    }

    public List<AssociationSettingsDTO> getAssociationSettings() {
        return associationSettings;
    }

    public void setAssociationSettings(List<AssociationSettingsDTO> associationSettings) {
        this.associationSettings = associationSettings;
    }

    public List<TrackSettingsDTO> getTrackSettings() {
        return trackSettings;
    }

    public void setTrackSettings(List<TrackSettingsDTO> trackSettings) {
        this.trackSettings = trackSettings;
    }

    public boolean isPlaceTypeFixed() {
        return placeTypeFixed;
    }

    public void setPlaceTypeFixed(boolean value) {
        placeTypeFixed = value;
    }

    public boolean isTrackTypeFixed() {
        return trackTypeFixed;
    }

    public void setTrackTypeFixed(boolean value) {
        trackTypeFixed = value;
    }

    public float getNodeTransparency() {
        return nodeTransparency;
    }

    public void setNodeTransparency(float nodeTransparency) {
        this.nodeTransparency = nodeTransparency;
    }
}
