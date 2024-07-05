package csi.server.business.visualization.map.cacheloader.trackmap;

import java.util.HashMap;
import java.util.Map;

import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;
import csi.server.business.visualization.map.TrackmapNodeInfo;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.PlaceTypenameProcessor;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.ColorWheel;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;

public class TrackSummaryTypenameProcessor {
    private TrackmapNodeInfo mapNodeInfo;
    private MapTheme mapTheme;
    private Map<String,PlaceStyle> typeNameToPlaceStyle;
    private Map<String,AssociationStyle> trackNameToAssociationStyle;
    private TrackDynamicTypeInfo dynamicTypeInfo;

    TrackSummaryTypenameProcessor(TrackmapNodeInfo mapNodeInfo, MapTheme mapTheme, MapSettingsDTO mapSettings) {
        this.mapNodeInfo = mapNodeInfo;
        this.mapTheme = mapTheme;
        dynamicTypeInfo = MapServiceUtil.getTrackDynamicTypeInfo(mapSettings.getUuid());
        typeNameToPlaceStyle = new HashMap<String,PlaceStyle>();
        trackNameToAssociationStyle = new HashMap<String,AssociationStyle>();
    }

    public TrackidTracknameDuple process(TrackSettingsDTO trackSettings, int trackId, String trackName) {
        TrackidTracknameDuple key = processTrackName(trackSettings, trackId, trackName);
        if (key != null) {
         registerTrackKeyToId(trackId, key);
      }
        return key;
    }

    private TrackidTracknameDuple processTrackName(TrackSettingsDTO trackSettings, int trackId, String trackName) {
        TrackidTracknameDuple key = null;
        if (trackName != null) {
            key = getTrackIdTrackNameDuple(trackSettings, trackId, trackName);
        }
        return key;
    }

    private TrackidTracknameDuple getTrackIdTrackNameDuple(TrackSettingsDTO trackSettings, int trackId, String trackName) {
        TrackidTracknameDuple key;
        key = new TrackidTracknameDuple(trackId, trackName);
        if (!mapNodeInfo.getTrackkeyToColor().containsKey(key)) {
            PlaceStyle placeStyle = getPlaceStyle(trackName);
            AssociationStyle associationStyle = getAssociationStyle(trackName);
            processShape(key, placeStyle);
            processColor(trackSettings, key, associationStyle);
        }
        return key;
    }

    private PlaceStyle getPlaceStyle(String typename) {
        return PlaceTypenameProcessor.getPlaceStyle(mapTheme, typeNameToPlaceStyle, typename);
    }

    private AssociationStyle getAssociationStyle(String typename) {
        AssociationStyle associationStyle1 = trackNameToAssociationStyle.get(typename);
        if (associationStyle1 != null) {
            return associationStyle1;
        }
        if (mapTheme != null) {
            AssociationStyle associationStyle2 = mapTheme.getAssociationStyleMap().get(typename);
            if (associationStyle2 != null) {
                trackNameToAssociationStyle.put(typename, associationStyle2);
                return associationStyle2;
            }
        }
        return null;
    }

    private void processShape(TrackidTracknameDuple key, PlaceStyle placeStyle) {
        String shapeString;
        ShapeType defaultShape = null;
        if (mapTheme != null) {
            defaultShape = mapTheme.getDefaultShape();
        }
        if (placeStyle != null) {
            if (placeStyle.getShape() != null) {
                shapeString = placeStyle.getShape().toString();
            } else if (defaultShape != null) {
                shapeString = defaultShape.toString();
            } else {
                shapeString = getShapeFromDynamicTypeInfo(key);
            }
        } else if (defaultShape != null) {
            shapeString = defaultShape.toString();
        } else {
            shapeString = getShapeFromDynamicTypeInfo(key);
        }
        storeShapeStringInfo(key, shapeString);
    }

    private String getShapeFromDynamicTypeInfo(TrackidTracknameDuple key) {
        String shapeString;
        if (dynamicTypeInfo.getTrackKeyToSummaryShape().containsKey(key)) {
            shapeString = dynamicTypeInfo.getTrackKeyToSummaryShape().get(key);
        } else {
            shapeString = MapServiceUtil.getNextShapeTypeString();
            dynamicTypeInfo.getTrackKeyToSummaryShape().put(key, shapeString);
        }
        return shapeString;
    }

    private void storeShapeStringInfo(TrackidTracknameDuple key, String shapeString) {
        mapNodeInfo.getTrackkeyToShape().put(key, shapeString);
        if (!dynamicTypeInfo.getTrackKeyToSummaryShape().containsKey(key)) {
         dynamicTypeInfo.getTrackKeyToSummaryShape().put(key, shapeString);
      }
    }

    private void processColor(TrackSettingsDTO trackSettings, TrackidTracknameDuple key, AssociationStyle associationStyle) {
        String colorString;
        if (trackSettings.isColorOverriden()) {
            colorString = trackSettings.getColorString();
        } else if (associationStyle != null) {
            colorString = ClientColorHelper.get().make(associationStyle.getColor()).toString();
        } else {
            colorString = getColorFromDynamicTypeInfo(key);
        }
        storeColorInfo(key, colorString.replace("#", ""));
    }

    private String getColorFromDynamicTypeInfo(TrackidTracknameDuple key) {
        String colorString;
        if (dynamicTypeInfo.getTrackKeyToColor().containsKey(key)) {
            colorString = dynamicTypeInfo.getTrackKeyToColor().get(key);
        } else {
            colorString = ClientColorHelper.get().make(ColorWheel.next()).toString();
            dynamicTypeInfo.getTrackKeyToColor().put(key, colorString);
        }
        return colorString;
    }

    private void storeColorInfo(TrackidTracknameDuple key, String color) {
        mapNodeInfo.getTrackkeyToColor().put(key, color);
        if (!dynamicTypeInfo.getTrackKeyToColor().containsKey(key)) {
         dynamicTypeInfo.getTrackKeyToColor().put(key, color);
      }
    }

    private void registerTrackKeyToId(int trackId, TrackidTracknameDuple key) {
        if (key != null) {
            AbstractTrackDetailPointCacheLoader.registerTypenameToTrackId(trackId, key, mapNodeInfo.getTrackidToTracknames());
        }
    }
}
