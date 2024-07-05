package csi.server.business.visualization.map.mapsettings;

import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.model.visualization.map.MapPlace;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class PlaceSettingsDTOBuilder {
    private MapTheme mapTheme;
    private MapPlace mapPlace;
    private PlaceSettingsDTO placeSettings;
    private PlaceStyle style;

    PlaceSettingsDTOBuilder(MapTheme mapTheme, MapPlace mapPlace) {
        super();
        this.mapTheme = mapTheme;
        this.mapPlace = mapPlace;
    }

    public void build() {
        init();
        placeSettings.setName(mapPlace.getName());
        setupGeoInfo();
        setupLabelInfo();
        setupSizeInfo();
        setupTypeInfo();
        setupRenderInfo();
    }

    private void init() {
        placeSettings = new PlaceSettingsDTO();
    }

    private void setupGeoInfo() {
        placeSettings.setLatColumn(mapPlace.getLatField().getLocalId().replace("-", "_"));
        placeSettings.setLongColumn(mapPlace.getLongField().getLocalId().replace("-", "_"));
    }

    private void setupLabelInfo() {
        if (mapPlace.getLabelField() != null)
            placeSettings.setLabelColumn(mapPlace.getLabelField().getLocalId().replace("-", "_"));
    }

    private void setupSizeInfo() {
        placeSettings.setSize(mapPlace.getSize());
        if (mapPlace.isComputedSize() && mapPlace.getSizeField() != null) {
            switch (mapPlace.getAggregateFunction()) {
                case COUNT:
                    setupSizeInfo("COUNT");
                    placeSettings.setSizedByDynamicType(mapPlace.isPlaceSizeByDynamicType());
                    break;
                case COUNT_DISTINCT:
                    setupSizeInfo("COUNT_DIST");
                    placeSettings.setSizedByDynamicType(mapPlace.isPlaceSizeByDynamicType());
                    break;
                case MIN:
                    if (mapPlace.getSizeField().getValueType().isNumeric()) {
                        setupSizeInfo("MIN");
                        placeSettings.setSizedByDynamicType(mapPlace.isPlaceSizeByDynamicType());
                    }
                    break;
                case MAX:
                    if (mapPlace.getSizeField().getValueType().isNumeric()) {
                        setupSizeInfo("MAX");
                        placeSettings.setSizedByDynamicType(mapPlace.isPlaceSizeByDynamicType());
                    }
                    break;
                case SUM:
                    if (mapPlace.getSizeField().getValueType().isNumeric()) {
                        setupSizeInfo("SUM");
                        placeSettings.setSizedByDynamicType(mapPlace.isPlaceSizeByDynamicType());
                    }
                    break;
                case AVG:
                    if (mapPlace.getSizeField().getValueType().isNumeric()) {
                        setupSizeInfo("AVG");
                        placeSettings.setSizedByDynamicType(mapPlace.isPlaceSizeByDynamicType());
                    }
                    break;
                case ABS_AVG:
                    if (mapPlace.getSizeField().getValueType().isNumeric()) {
                        setupSizeInfo("ABS_AVG");
                        placeSettings.setSizedByDynamicType(mapPlace.isPlaceSizeByDynamicType());
                    }
                    break;
                case ABS_SUM:
                    if (mapPlace.getSizeField().getValueType().isNumeric()) {
                        setupSizeInfo("ABS_SUM");
                        placeSettings.setSizedByDynamicType(mapPlace.isPlaceSizeByDynamicType());
                    }
                    break;
                case NORMALIZE:
                default:
                    break;
            }
        }
    }

    private void setupSizeInfo(String functionString) {
        placeSettings.setSizeColumn(mapPlace.getSizeField().getLocalId().replace("-", "_"));
        placeSettings.setSizeColumnNumerical(mapPlace.getSizeField().getValueType().isNumeric());
        placeSettings.setSizeFunction(functionString);
    }

    private void setupTypeInfo() {
        if (mapPlace.isTypeFixed())
            placeSettings.setTypeName(getTypeName(mapPlace));
        else {
            placeSettings.setTypeColumn(mapPlace.getTypeField().getLocalId().replace("-", "_"));
            placeSettings.setIncludeNullType(mapPlace.isIncludeNullType());
        }
    }

    private String getTypeName(MapPlace mapPlace) {
        String typename = mapPlace.getTypeName();
        if (typename == null)
            typename = mapPlace.getName();
        return typename;
    }

    private void setupRenderInfo() {
        style = getPlaceStyle();
        setupIcon();
        setupShape();
        setupColor();
    }

    private PlaceStyle getPlaceStyle() {
        if (mapTheme != null && mapTheme.getPlaceStyles() != null) {
            return mapTheme.getPlaceStyleMap().get(getTypeName(mapPlace));
        }
        return null;
    }

    private void setupIcon() {
        if (mapPlace.isUseDefaultIconSetting()) {
            placeSettings.setIconOverridden(false);
            handleDefaultIconSetting();
        } else {
            placeSettings.setIconOverridden(true);
            iconSettingAsAppearedOnView();
        }
    }

    private void handleDefaultIconSetting() {
        if (style != null) {
            String iconId = style.getIconId();
            placeSettings.setIconUri(iconId);
        }
    }

    private void iconSettingAsAppearedOnView() {
        if (mapPlace.isIconFixed()) {
            String iconId = mapPlace.getIconId();
            placeSettings.setIconUri(iconId);
        } else {
            placeSettings.setIconColumn(mapPlace.getIconField().getLocalId().replace("-", "_"));
        }
    }

    private void setupShape() {
        if (mapPlace.isUseDefaultShapeSetting()) {
            placeSettings.setShapeOverridden(false);
            handleDefaultShapeSetting();
        } else {
            placeSettings.setShapeOverridden(true);
            shapeSettingsAsAppearedOnView();
        }
    }

    private void handleDefaultShapeSetting() {
        ShapeType defaultShape = null;
        if (mapTheme != null) {
            defaultShape = mapTheme.getDefaultShape();
        }
        if (style != null) {
            if (style.getShape() != null)
                placeSettings.setShapeTypeString(style.getShape().toString());
            else if (defaultShape != null)
                placeSettings.setShapeTypeString(defaultShape.toString());
            else
                shapeSettingsAsAppearedOnView();
        } else if (defaultShape != null) {
            placeSettings.setShapeTypeString(defaultShape.toString());
        } else {
            shapeSettingsAsAppearedOnView();
        }
    }

    private void shapeSettingsAsAppearedOnView() {
        placeSettings.setShapeTypeString(mapPlace.getShapeTypeString());
    }

    private void setupColor() {
        if (mapPlace.isUseDefaultColorSetting()) {
            placeSettings.setColorOverridden(false);
            handleDefaultColorSetting();
        } else {
            placeSettings.setColorOverridden(true);
            colorSettingsAsAppearedOnView();
        }
    }

    private void handleDefaultColorSetting() {
        if (style != null) {
            String color = ClientColorHelper.get().make(style.getColor()).toString();
            placeSettings.setColorString(color.replace("#", ""));
        } else {
            colorSettingsAsAppearedOnView();
        }
    }

    private void colorSettingsAsAppearedOnView() {
        placeSettings.setColorString(mapPlace.getColorString());
    }

    public PlaceSettingsDTO getPlaceSettings() {
        return placeSettings;
    }

}
