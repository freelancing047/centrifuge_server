package csi.server.business.visualization.map;

import csi.server.common.model.map.PlaceidTypenameDuple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PlaceDynamicTypeInfo implements PlaceTypeInfo {
    private ConcurrentHashMap<PlaceidTypenameDuple, Integer> typenameToId;
    private ConcurrentHashMap<Integer, PlaceidTypenameDuple> typeidToName;
    private ConcurrentHashMap<PlaceidTypenameDuple, String> typenameToColor;
    private ConcurrentHashMap<PlaceidTypenameDuple, String> typenameToShape;
    private ConcurrentHashMap<PlaceidTypenameDuple, String> typenameToIconUrl;
    private boolean invalidated;

    public PlaceDynamicTypeInfo() {
        typenameToId = new ConcurrentHashMap<>();
        typeidToName = new ConcurrentHashMap<>();
        typenameToColor = new ConcurrentHashMap<>();
        typenameToShape = new ConcurrentHashMap<>();
        typenameToIconUrl = new ConcurrentHashMap<>();
        invalidated = false;
    }

    public Map<PlaceidTypenameDuple, Integer> getTypenameToId() {
        return typenameToId;
    }

    public Map<Integer, PlaceidTypenameDuple> getTypeIdToName() {
        return typeidToName;
    }

    public Map<PlaceidTypenameDuple, String> getTypenameToColor() {
        return typenameToColor;
    }

    public Map<PlaceidTypenameDuple, String> getTypenameToShape() {
        return typenameToShape;
    }

    public Map<PlaceidTypenameDuple, String> getTypenameToIconUrl() {
        return typenameToIconUrl;
    }

    public void invalidate() {
        invalidated = true;
    }

    boolean isInvalidated() {
        return invalidated;
    }
}
