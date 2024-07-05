package csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor;

import com.google.common.collect.Sets;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.ColorWheel;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.visualization.map.MapConstants;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

import java.util.Map;
import java.util.Set;

public class PlaceTypenameProcessor {
    public static PlaceidTypenameDuple process(MapSettingsDTO mapSettings, int placeId, PlaceSettingsDTO placeSettings, MapNodeInfo mapNodeInfo, MapTheme mapTheme, Map<String, PlaceStyle> typeNameToPlaceStyle, String typenameFromRS) {
        String typename = placeSettings.getTypeName();
        PlaceidTypenameDuple key = processTypename(mapSettings, placeSettings, mapNodeInfo, mapTheme, typeNameToPlaceStyle, placeId, typename, typenameFromRS);
        if (key != null)
            registerTypenameToPlaceId(placeId, mapNodeInfo, key);
        return key;
    }

    private static PlaceidTypenameDuple processTypename(MapSettingsDTO mapSettings, PlaceSettingsDTO placeSettings, MapNodeInfo mapNodeInfo, MapTheme mapTheme, Map<String, PlaceStyle> typeNameToPlaceStyle, int placeId, String typename, String typenameFromRS) {
        PlaceDynamicTypeInfo dynamicTypeInfo = MapServiceUtil.getPlaceDynamicTypeInfo(mapSettings.getUuid());
        PlaceidTypenameDuple key = null;
        if (typename == null) {
            mapSettings.setPlaceTypeFixed(false);
            typename = getTypenameFromRow(typenameFromRS, placeSettings);
            if (typename != null) {
                key = new PlaceidTypenameDuple(placeId, typename);
                if (!mapNodeInfo.getTypenameToColor().containsKey(key)) {
                    PlaceStyle placeStyle = getPlaceStyle(mapTheme, typeNameToPlaceStyle, typename);
                    processDynamicType(placeSettings, mapNodeInfo, mapTheme, key, placeStyle, dynamicTypeInfo);
                }
            }
        } else {
            key = new PlaceidTypenameDuple(placeId, typename);
            if (!mapNodeInfo.getTypenameToColor().containsKey(key)) {
                processFixedType(placeSettings, mapNodeInfo, key, dynamicTypeInfo);
            }
        }
        return key;
    }

    private static String getTypenameFromRow(String typename, PlaceSettingsDTO placeSettings) {
        if (typename == null) {
            if (!placeSettings.isIncludeNullType())
                return null;
            typename = MapConstants.NULL_TYPE_NAME;
        } else if (typename.trim().length() == 0) {
            typename = MapConstants.EMPTY_TYPE_NAME;
        } else {
            typename = typename.trim();
        }
        return typename;
    }

    public static PlaceStyle getPlaceStyle(MapTheme mapTheme, Map<String, PlaceStyle> typeNameToPlaceStyle, String typename) {
        PlaceStyle placeStyle1 = typeNameToPlaceStyle.get(typename);
        if (placeStyle1 != null) {
            return placeStyle1;
        }
        if (mapTheme != null) {
            PlaceStyle placeStyle2 = mapTheme.getPlaceStyleMap().get(typename);
            if (placeStyle2 != null) {
                typeNameToPlaceStyle.put(typename, placeStyle2);
                return placeStyle2;
            }
        }
        return null;
    }

    private static void processDynamicType(PlaceSettingsDTO placeSettings, MapNodeInfo mapNodeInfo, MapTheme mapTheme,
                                           PlaceidTypenameDuple key, PlaceStyle placeStyle, PlaceDynamicTypeInfo dynamicTypeInfo) {
        processIcon(placeSettings, mapNodeInfo, key, placeStyle, dynamicTypeInfo);
        processShape(placeSettings, mapNodeInfo, mapTheme, key, placeStyle, dynamicTypeInfo);
        processColor(placeSettings, mapNodeInfo, key, placeStyle, dynamicTypeInfo);
    }

    private static void processIcon(PlaceSettingsDTO placeSettings, MapNodeInfo mapNodeInfo, PlaceidTypenameDuple key,
                                    PlaceStyle placeStyle, PlaceDynamicTypeInfo dynamicTypeInfo) {
        String iconUri = null;
        if (placeSettings.isIconOverridden()) {
            iconUri = placeSettings.getIconUri();
        } else if (placeStyle != null && placeStyle.getIconId() != null) {
            iconUri = placeStyle.getIconId();
        }
        if (iconUri != null) {
            storeIconUriInfo(mapNodeInfo, key, dynamicTypeInfo, iconUri);
        }
    }

    private static void storeIconUriInfo(MapNodeInfo mapNodeInfo, PlaceidTypenameDuple key,
                                         PlaceDynamicTypeInfo dynamicTypeInfo, String iconUri) {
        if (iconUri != null) {
            mapNodeInfo.getTypenameToIconUrl().put(key, iconUri);
            if (!dynamicTypeInfo.getTypenameToIconUrl().containsKey(key))
                dynamicTypeInfo.getTypenameToIconUrl().put(key, iconUri);
        }
    }

    private static void processShape(PlaceSettingsDTO placeSettings, MapNodeInfo mapNodeInfo, MapTheme mapTheme,
                                     PlaceidTypenameDuple key, PlaceStyle placeStyle, PlaceDynamicTypeInfo dynamicTypeInfo) {
        String shapeString;
        ShapeType defaultShape = null;
        if (mapTheme != null) {
            defaultShape = mapTheme.getDefaultShape();
        }
        if (placeSettings.isShapeOverridden()) {
            shapeString = placeSettings.getShapeTypeString();
        } else if (placeStyle != null) {
            if (placeStyle.getShape() != null) {
                shapeString = placeStyle.getShape().toString();
            } else if (defaultShape != null) {
                shapeString = defaultShape.toString();
            } else {
                shapeString = getShapeFromDynamicTypeInfo(key, dynamicTypeInfo);
            }
        } else if (defaultShape != null) {
            shapeString = defaultShape.toString();
        } else {
            shapeString = getShapeFromDynamicTypeInfo(key, dynamicTypeInfo);
        }
        storeShapeStringInfo(mapNodeInfo, key, dynamicTypeInfo, shapeString);
    }

    private static void storeShapeStringInfo(MapNodeInfo mapNodeInfo, PlaceidTypenameDuple key,
                                             PlaceDynamicTypeInfo dynamicTypeInfo, String shapeString) {
        mapNodeInfo.getTypenameToShape().put(key, shapeString);
        if (!dynamicTypeInfo.getTypenameToShape().containsKey(key))
            dynamicTypeInfo.getTypenameToShape().put(key, shapeString);
    }

    private static String getShapeFromDynamicTypeInfo(PlaceidTypenameDuple key, PlaceDynamicTypeInfo dynamicTypeInfo) {
        String shapeString;
        if (dynamicTypeInfo.getTypenameToShape().containsKey(key)) {
            shapeString = dynamicTypeInfo.getTypenameToShape().get(key);
        } else {
            shapeString = MapServiceUtil.getNextShapeTypeString();
            dynamicTypeInfo.getTypenameToShape().put(key, shapeString);
        }
        return shapeString;
    }

    private static void processColor(PlaceSettingsDTO placeSettings, MapNodeInfo mapNodeInfo, PlaceidTypenameDuple key,
                                     PlaceStyle placeStyle, PlaceDynamicTypeInfo dynamicTypeInfo) {
        String colorString;
        if (placeSettings.isColorOverridden()) {
            colorString = placeSettings.getColorString();
        } else if (placeStyle != null) {
            colorString = ClientColorHelper.get().make(placeStyle.getColor()).toString();
        } else {
            colorString = getColorFromDynamicTypeInfo(key, dynamicTypeInfo);
        }
        storeColorInfo(mapNodeInfo, key, dynamicTypeInfo, colorString.replace("#", ""));
    }

    private static String getColorFromDynamicTypeInfo(PlaceidTypenameDuple key, PlaceDynamicTypeInfo dynamicTypeInfo) {
        String colorString;
        if (dynamicTypeInfo.getTypenameToColor().containsKey(key)) {
            colorString = dynamicTypeInfo.getTypenameToColor().get(key);
        } else {
            colorString = ClientColorHelper.get().make(ColorWheel.next()).toString();
            dynamicTypeInfo.getTypenameToColor().put(key, colorString);
        }
        return colorString;
    }

    private static void storeColorInfo(MapNodeInfo mapNodeInfo, PlaceidTypenameDuple key, PlaceDynamicTypeInfo dynamicTypeInfo, String color) {
        mapNodeInfo.getTypenameToColor().put(key, color);
        if (!dynamicTypeInfo.getTypenameToColor().containsKey(key))
            dynamicTypeInfo.getTypenameToColor().put(key, color);
    }

    private static void processFixedType(PlaceSettingsDTO placeSettings, MapNodeInfo mapNodeInfo,
                                         PlaceidTypenameDuple key, PlaceDynamicTypeInfo dynamicTypeInfo) {
        // if (placeSettings.getIconUri() != null) {
        storeIconUriInfo(mapNodeInfo, key, dynamicTypeInfo, placeSettings.getIconUri());
        // }
        storeShapeStringInfo(mapNodeInfo, key, dynamicTypeInfo, placeSettings.getShapeTypeString());
        storeColorInfo(mapNodeInfo, key, dynamicTypeInfo, placeSettings.getColorString().replace("#", ""));
    }

    private static void registerTypenameToPlaceId(int placeId, MapNodeInfo mapNodeInfo, PlaceidTypenameDuple key) {
        if (key != null) {
            Set<String> typeNames;
            if (mapNodeInfo.getPlaceIdToTypeNames().containsKey(placeId)) {
                typeNames = mapNodeInfo.getPlaceIdToTypeNames().get(placeId);
            } else {
                typeNames = Sets.newTreeSet();
                mapNodeInfo.getPlaceIdToTypeNames().put(placeId, typeNames);
            }
            String typename = key.getTypename();
            if (typename != null) {
                typeNames.add(typename);
            }
        }
    }

}
