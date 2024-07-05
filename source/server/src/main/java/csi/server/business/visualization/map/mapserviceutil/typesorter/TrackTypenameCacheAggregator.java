package csi.server.business.visualization.map.mapserviceutil.typesorter;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.TrackidToTracknames;
import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.Map;
import java.util.Set;

public class TrackTypenameCacheAggregator {
    private MapCacheHandler mapCacheHandler;
    private TrackidToTracknames trackidToTracknames;
    private Set<TrackidTracknameDuple> sortedTypeNames;
    private Map<TrackidTracknameDuple, Integer> typenameToId;
    private Map<Integer, TrackidTracknameDuple> typeIdToName;
    private int typenameCacheId;

    TrackTypenameCacheAggregator(MapCacheHandler mapCacheHandler, TrackidToTracknames trackidToTracknames, Map<TrackidTracknameDuple, Integer> typenameToId) {
        this.mapCacheHandler = mapCacheHandler;
        this.trackidToTracknames = trackidToTracknames;
        this.typenameToId = typenameToId;
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
        for (TrackidTracknameDuple typename : getTypeNamesOfTracks())
            populateEitherTypeIdToNameOrSortedTypeNames(typename);
    }

    private Set<TrackidTracknameDuple> getTypeNamesOfTracks() {
        Set<TrackidTracknameDuple> typeNames = Sets.newTreeSet();
        for (int trackId = mapCacheHandler.getMapSettings().getTrackSettings().size() - 1; trackId >= 0; trackId--)
            getTypeNamesOfTrack(typeNames, trackId);
        return typeNames;
    }

    private void getTypeNamesOfTrack(Set<TrackidTracknameDuple> typeNames, int trackId) {
        Set<String> typenameSet = trackidToTracknames.getTrackidToTracknames().get(trackId);
        if (typenameSet != null) {
            for (String typename : typenameSet) {
                typeNames.add(new TrackidTracknameDuple(trackId, typename));
            }
        }
    }

    private void populateEitherTypeIdToNameOrSortedTypeNames(TrackidTracknameDuple typename) {
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
        for (Integer typeId : mapCacheHandler.getSortedTrackTypeIds(typeIdToName)) {
            TrackidTracknameDuple key = typeIdToName.get(typeId);
            mapCacheHandler.applyTypenameToTypeInfo(trackidToTracknames, key, typenameCacheId);
            typenameCacheId++;
        }
    }

    private void populateTypenameCacheWithSortedTypeNames() {
        for (TrackidTracknameDuple typename : sortedTypeNames) {
            mapCacheHandler.applyTypenameToTypeInfo(trackidToTracknames, typename, typenameCacheId);
            typenameCacheId++;
        }
    }
}