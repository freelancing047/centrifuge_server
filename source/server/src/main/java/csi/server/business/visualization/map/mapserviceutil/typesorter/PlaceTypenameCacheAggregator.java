package csi.server.business.visualization.map.mapserviceutil.typesorter;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.common.model.map.PlaceidTypenameDuple;

import java.util.Map;
import java.util.Set;

public class PlaceTypenameCacheAggregator {
    private MapCacheHandler mapCacheHandler;
    private MapNodeInfo mapNodeInfo;
    private Map<PlaceidTypenameDuple, Integer> typenameToId;
    private Set<PlaceidTypenameDuple> sortedTypeNames;
    private Map<Integer, PlaceidTypenameDuple> typeIdToName;
    private int typenameCacheId;

    PlaceTypenameCacheAggregator(MapCacheHandler mapCacheHandler, MapNodeInfo mapNodeInfo, PlaceDynamicTypeInfo dynamicTypeInfo) {
        this.mapCacheHandler = mapCacheHandler;
        this.mapNodeInfo = mapNodeInfo;
        typenameToId = dynamicTypeInfo.getTypenameToId();
    }

    public void aggregate() {
        init();
        populateTypenameCacheWithTypeIdToName();
        populateTypenameCacheWithSortedTypeNames();
    }

    private void init() {
        typeIdToName = Maps.newHashMap();
        sortedTypeNames = Sets.newTreeSet();
        populateTypeIdToNameAndSortedTypeNames();
        typenameCacheId = 0;
    }

    private void populateTypeIdToNameAndSortedTypeNames() {
        for (PlaceidTypenameDuple typename : getTypeNamesOfPlaces())
            populateEitherTypeIdToNameOrSortedTypeNames(typename);
    }

    private Set<PlaceidTypenameDuple> getTypeNamesOfPlaces() {
        Set<PlaceidTypenameDuple> typeNames = Sets.newTreeSet();
        for (int placeId = mapCacheHandler.getMapSettings().getPlaceSettings().size() - 1; placeId >= 0; placeId--)
            getTypeNamesOfPlace(typeNames, placeId);
        return typeNames;
    }

    private void getTypeNamesOfPlace(Set<PlaceidTypenameDuple> typeNames, int placeId) {
        Set<String> typenameSet = mapNodeInfo.getPlaceIdToTypeNames().get(placeId);
        if (typenameSet != null) {
            for (String typename : typenameSet) {
                typeNames.add(new PlaceidTypenameDuple(placeId, typename));
            }
        }
    }

    private void populateEitherTypeIdToNameOrSortedTypeNames(PlaceidTypenameDuple typename) {
        Integer typeId = typenameToId.get(typename);
        if (isInDynamicTypenameToId(typeId))
            typeIdToName.put(typeId, typename);
        else
            sortedTypeNames.add(typename);
    }

    private boolean isInDynamicTypenameToId(Integer typeId) {
        return typeId != null;
    }

    private void populateTypenameCacheWithTypeIdToName() {
        for (Integer typeId : mapCacheHandler.getSortedPlaceTypeIds(typeIdToName)) {
            PlaceidTypenameDuple key = typeIdToName.get(typeId);
            mapCacheHandler.applyTypenameToTypeInfo(mapNodeInfo, key, typenameCacheId);
            typenameCacheId++;
        }
    }

    private void populateTypenameCacheWithSortedTypeNames() {
        for (PlaceidTypenameDuple typename : sortedTypeNames) {
            mapCacheHandler.applyTypenameToTypeInfo(mapNodeInfo, typename, typenameCacheId);
            typenameCacheId++;
        }
    }
}