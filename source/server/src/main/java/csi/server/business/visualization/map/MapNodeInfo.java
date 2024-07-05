package csi.server.business.visualization.map;

import com.google.common.collect.Sets;
import csi.server.business.visualization.map.cacheloader.MapNodeUtil;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.PlaceidTypenameDuple;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MapNodeInfo implements PlaceTypeInfo {
    private ConcurrentHashMap<Long, AugmentedMapNode> mapById;
    private ConcurrentHashMap<Geometry, AugmentedMapNode> mapByGeometry;
    private ConcurrentHashMap<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapByType;
    private ConcurrentHashMap<PlaceidTypenameDuple, Integer> typenameToId;
    private ConcurrentHashMap<Integer, PlaceidTypenameDuple> typeIdToName;
    private ConcurrentHashMap<PlaceidTypenameDuple, String> typenameToColor;
    private ConcurrentHashMap<PlaceidTypenameDuple, String> typenameToShape;
    private ConcurrentHashMap<PlaceidTypenameDuple, String> typenameToIconUrl;
    private ConcurrentHashMap<Integer, Set<String>> placeIdToTypeNames;
    private Set<AugmentedMapNode> combinedMapNodes;
    private Set<AugmentedMapNode> newMapNodes;
    private Set<AugmentedMapNode> updatedMapNodes;

    public MapNodeInfo() {
        mapById = new ConcurrentHashMap<>();
        mapByGeometry = new ConcurrentHashMap<>();
        mapByType = new ConcurrentHashMap<>();
        typenameToId = new ConcurrentHashMap<>();
        typeIdToName = new ConcurrentHashMap<>();
        typenameToColor = new ConcurrentHashMap<>();
        typenameToShape = new ConcurrentHashMap<>();
        typenameToIconUrl = new ConcurrentHashMap<>();
        placeIdToTypeNames = new ConcurrentHashMap<>();
        combinedMapNodes = Sets.newTreeSet(MapNodeUtil.getMapNodeComparator());
        newMapNodes = Sets.newTreeSet(MapNodeUtil.getMapNodeComparator());
        updatedMapNodes = Sets.newTreeSet(MapNodeUtil.getMapNodeComparator());
    }

    public Map<Long, AugmentedMapNode> getMapById() {
        return mapById;
    }

    public Map<Geometry, AugmentedMapNode> getMapByGeometry() {
        return mapByGeometry;
    }

    public Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> getMapByType() {
        return mapByType;
    }

    public Map<PlaceidTypenameDuple, Integer> getTypenameToId() {
        return typenameToId;
    }

    public Map<Integer, PlaceidTypenameDuple> getTypeIdToName() {
        return typeIdToName;
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

    public Map<Integer, Set<String>> getPlaceIdToTypeNames() {
        return placeIdToTypeNames;
    }

    public Set<AugmentedMapNode> getCombinedMapNodes() {
        return combinedMapNodes;
    }

    public Set<AugmentedMapNode> getNewMapNodes() {
        return newMapNodes;
    }

    public Set<AugmentedMapNode> getUpdatedMapNodes() {
        return updatedMapNodes;
    }
}
