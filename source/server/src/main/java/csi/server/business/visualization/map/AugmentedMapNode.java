package csi.server.business.visualization.map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.TypeSizeValue;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class AugmentedMapNode extends MapNode {
    private Set<Integer> internalStateIds = Sets.newTreeSet();
    private boolean isNew = false;
    private boolean isUpdated = false;

    private Set<Integer> placeIds = Sets.newTreeSet();
    private Map<String, Map<String, Integer>> descriptions = Maps.newTreeMap();
    private Set<String> labels = Sets.newTreeSet();

    private Map<Integer, TypeSizeValue> placeSizeValueMap = Maps.newTreeMap();
    private Map<PlaceidTypenameDuple, TypeSizeValue> typeSizeValueMap = Maps.newTreeMap();
    private Map<PlaceidTypenameDuple, Integer> typeNodeCounts = Maps.newTreeMap();
    private Map<PlaceidTypenameDuple, String> typeIconUriMap = Maps.newTreeMap();
    private Map<PlaceidTypenameDuple, Integer> typeSizeMap = Maps.newTreeMap();

//    private Set<Integer> trackIds = Sets.newTreeSet();
    private Map<TrackidTracknameDuple, TypeSizeValue> identitySizeValueMap = Maps.newTreeMap();
    private Map<TrackidTracknameDuple, Integer> identityNodeCounts = Maps.newTreeMap();
    private Map<TrackidTracknameDuple, Integer> identitySizeMap = Maps.newTreeMap();
    private Map<TrackidTracknameDuple, PlaceidTypenameDuple> placeTypeForTrack = Maps.newTreeMap();

    public AugmentedMapNode(String vizUuid, Long nodeId) {
        super(vizUuid, nodeId);
    }

    public TreeSet<Integer> getTypeIds(Map<PlaceidTypenameDuple, Integer> typenameToId) {
        TreeSet<Integer> typeIds = new TreeSet<>();
        Map<Integer, PlaceidTypenameDuple> typeIdToName = Maps.newHashMap();
        aggregateTypeInfo(typeIds, typeIdToName, typenameToId);
        return typeIds;
    }

    public TreeSet<Integer> getIdentityIds(Map<TrackidTracknameDuple, Integer> typenameToId) {
        TreeSet<Integer> typeIds = new TreeSet<>();
        Map<Integer, TrackidTracknameDuple> typeIdToName = Maps.newHashMap();
        aggregateIdentityInfo(typeIds, typeIdToName, typenameToId);
        return typeIds;
    }

    private void aggregateTypeInfo(TreeSet<Integer> typeIds, Map<Integer, PlaceidTypenameDuple> typeIdToName, Map<PlaceidTypenameDuple, Integer> typenameToId) {
        if (typenameToId == null)
            throw new MapCacheNotAvailable();
        for (PlaceidTypenameDuple key : typeNodeCounts.keySet()) {
            Integer typeId = typenameToId.get(key);
            if (typeId != null && !typeIds.contains(typeId)) {
                typeIds.add(typeId);
                typeIdToName.put(typeId, key);
            }
        }
    }

    private void aggregateIdentityInfo(TreeSet<Integer> typeIds, Map<Integer, TrackidTracknameDuple> typeIdToName, Map<TrackidTracknameDuple, Integer> identityNameToId) {
        if (identityNameToId == null)
            throw new MapCacheNotAvailable();
        for (TrackidTracknameDuple key : identityNodeCounts.keySet()) {
            Integer typeId = identityNameToId.get(key);
            if (typeId != null && !typeIds.contains(typeId)) {
                typeIds.add(typeId);
                typeIdToName.put(typeId, key);
            }
        }
    }

    public PlaceidTypenameDuple getTypeName(Map<PlaceidTypenameDuple, Integer> typenameToId) {
        TreeSet<Integer> typeIds = new TreeSet<>();
        Map<Integer, PlaceidTypenameDuple> typeIdToName = Maps.newHashMap();
        aggregateTypeInfo(typeIds, typeIdToName, typenameToId);
        int id = typeIds.first();
        return typeIdToName.get(id);
    }

    private TrackidTracknameDuple getIdentityName(Map<TrackidTracknameDuple, Integer> identityNameToId) {
        TreeSet<Integer> typeIds = new TreeSet<>();
        Map<Integer, TrackidTracknameDuple> typeIdToName = Maps.newHashMap();
        aggregateIdentityInfo(typeIds, typeIdToName, identityNameToId);
        int id = typeIds.first();
        return typeIdToName.get(id);
    }

    public void addTypeName(PlaceidTypenameDuple key) {
        if (typeNodeCounts.containsKey(key)) {
            int count = typeNodeCounts.get(key);
            count++;
            typeNodeCounts.put(key, count);
        } else {
            typeNodeCounts.put(key, 1);
        }
    }

    public void addIdentityName(TrackidTracknameDuple key) {
        if (identityNodeCounts.containsKey(key)) {
            int count = identityNodeCounts.get(key);
            count++;
            identityNodeCounts.put(key, count);
        } else {
            identityNodeCounts.put(key, 1);
        }
    }

    public void addInternalStateId(int internalStateId, int newestInternalStateId) {
        if (internalStateIds.contains(internalStateId)) {
            return;
        }
        if (newestInternalStateId > 0) {
            if (internalStateId == newestInternalStateId) {
                if (internalStateIds.isEmpty())
                    isNew = true;
                else
                    isUpdated = true;
            } else {
                if (isNew) {
                    isNew = false;
                    isUpdated = true;
                }
            }

        }
        internalStateIds.add(internalStateId);
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void addPlaceId(int placeId) {
        placeIds.add(placeId);
    }

//    public void addTrackId(int trackId) {
//        trackIds.add(trackId);
//    }

    public Set<Integer> getPlaceIds() {
        return placeIds;
    }

    public Map<PlaceidTypenameDuple, Integer> getTypeNodeCounts() {
        return typeNodeCounts;
    }

    public boolean isCombined() {
        return typeNodeCounts.keySet().size() > 1;
    }

    public void addDescriptions(String fieldName, String fieldValue) {
        Map<String, Integer> fieldDescription;
        if (descriptions.containsKey(fieldName)) {
            fieldDescription = descriptions.get(fieldName);
        } else {
            fieldDescription = new HashMap<>();
            descriptions.put(fieldName, fieldDescription);
        }
        if (fieldDescription.containsKey(fieldValue)) {
            fieldDescription.put(fieldValue, fieldDescription.get(fieldValue) + 1);
        } else {
            fieldDescription.put(fieldValue, 1);
        }
    }

    public Map<String, Map<String, Integer>> getDescriptions() {
        return descriptions;
    }

    public String getLabel() {
        if (labels.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String label : labels) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(label);
        }
        return sb.toString();
    }

    public void addLabel(String label) {
        if (label != null) {
            labels.add(label);
        }
    }

    public void setIconUri(PlaceidTypenameDuple key, String iconUri) {
        typeIconUriMap.put(key, iconUri);
    }

    public String getIconUri(Map<PlaceidTypenameDuple, Integer> typenameToId) {
        return typeIconUriMap.get(getTypeName(typenameToId));
    }

    public String getIconUri(PlaceidTypenameDuple placeType) {
        return typeIconUriMap.get(placeType);
    }

    public Integer getSize(Map<PlaceidTypenameDuple, Integer> typenameToId) {
        if (typeSizeMap.size() == 0) {
            return 1;
        } else {
            return getSize(getTypeName(typenameToId));
        }
    }

    public Integer getSize(PlaceidTypenameDuple typename) {
        Integer size = typeSizeMap.get(typename);
        if (size == null) {
            return 1;
        } else {
            return size;
        }
    }

    public void setSize(PlaceidTypenameDuple key, Integer size) {
        typeSizeMap.put(key, size);
    }

    public void setIdentitySize(TrackidTracknameDuple key, Integer size) {
        identitySizeMap.put(key, size);
    }

    public Integer getIdentitySize(Map<TrackidTracknameDuple, Integer> identityNameToId) {
        if (identitySizeMap.size() == 0) {
            return 1;
        } else {
            return getIdentitySize(getIdentityName(identityNameToId));
        }
    }

    private Integer getIdentitySize(TrackidTracknameDuple typename) {
        Integer size = identitySizeMap.get(typename);
        if (size == null) {
            return 1;
        } else {
            return size;
        }
    }

    public TypeSizeValue getPlaceSizeValue(int placeId) {
        return placeSizeValueMap.get(placeId);
    }

    public void setPlaceSizeValue(int placeId, TypeSizeValue typeSizeValue) {
        placeSizeValueMap.put(placeId, typeSizeValue);
    }

    public TypeSizeValue getTypeSizeValue(PlaceidTypenameDuple key) {
        return typeSizeValueMap.get(key);
    }

    public void setTypeSizeValue(PlaceidTypenameDuple key, TypeSizeValue typeSizeValue) {
        typeSizeValueMap.put(key, typeSizeValue);
    }

    public TypeSizeValue getIdentitySizeValue(TrackidTracknameDuple key) {
        return identitySizeValueMap.get(key);
    }

    public void setIdentitySizeValue(TrackidTracknameDuple key, TypeSizeValue typeSizeValue) {
        identitySizeValueMap.put(key, typeSizeValue);
    }

    public void addPlaceTypeForTrack(TrackidTracknameDuple track, PlaceidTypenameDuple type) {
        if (!placeTypeForTrack.containsKey(track)) {
            placeTypeForTrack.put(track, type);
        }
    }

    public PlaceidTypenameDuple getPlaceTypeForTrack(TrackidTracknameDuple track) {
        return placeTypeForTrack.get(track);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNodeId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AugmentedMapNode) {
            return getNodeId() == ((AugmentedMapNode) obj).getNodeId();
        }
        return false;
    }
}
