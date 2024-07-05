package csi.server.business.visualization.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import csi.config.Configuration;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class TrackMapSummaryGrid implements Serializable, MapSummaryGrid {
    private String mapViewDefUuid;
    private Map<PlaceidTypenameDuple, Short> typeToTypeIndex = Maps.newHashMap();
    private short lastTypeIndex = -1;
    private LatLongGrid latLongGrid = new LatLongGrid();
    private int combinedTypeCount = 0;
    private Map<Short, Integer> typeCount = Maps.newHashMap();
    private Map<TrackidTracknameDuple, Short> trackToTrackIndex = Maps.newHashMap();
    private short lastTrackIndex = -1;
    private Map<Short, TreeMap<SequenceSortValue, GeoInfo>> sequenceTrees = Maps.newConcurrentMap();
    private Map<Short, Multimap<GeoInfo, SequenceSortValue>> sequenceReverseTrees = Maps.newConcurrentMap();

    public TrackMapSummaryGrid(String mapViewDefUuid) {
        this.mapViewDefUuid = mapViewDefUuid;
    }

    @Override
   public void setNewestInternalTypeId(int newestInternalTypeId) {
    }

    @Override
   public void addNode(Geometry geometry, int rowId, int internalStateId, PlaceidTypenameDuple key) {
        short typeIndex = getTypeIndex(key);
        latLongGrid.addRowIdAndType(geometry, rowId, typeIndex, this);
    }

    // OR PUT...
    private short getTypeIndex(PlaceidTypenameDuple key) {
        if (typeToTypeIndex.containsKey(key)) {
         return typeToTypeIndex.get(key);
      }
        lastTypeIndex++;
        typeToTypeIndex.put(key, lastTypeIndex);
        return lastTypeIndex;
    }

    private void incrementCombinedTypeCount() {
        combinedTypeCount++;
    }

    private void incrementTypeCount(short typeIndex) {
        if (typeCount.containsKey(typeIndex)) {
         typeCount.put(typeIndex, typeCount.get(typeIndex) + 1);
      } else {
         typeCount.put(typeIndex, 1);
      }
    }

    @Override
   public Set<Geometry> getGeometriesOfType(PlaceidTypenameDuple key) {
        short typeIndex = getTypeIndex(key);
        return latLongGrid.getGeometriesOfType(typeIndex);
    }

    @Override
   public Set<Integer> getRowIds(Geometry geometry) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
   public Set<Geometry> getDescendants(Geometry geometry) {
        int summaryLevel = geometry.getSummaryLevel();
        double minX = geometry.getX();
        double maxX = minX + (1 * Math.pow(10, -summaryLevel));
        double minY = geometry.getY();
        double maxY = minY + (1 * Math.pow(10, -summaryLevel));
        return getDescendants(minX, maxX, minY, maxY);
    }

    private Set<Geometry> getDescendants(double minX, double maxX, double minY, double maxY, SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion) {
        Set<Geometry> descendants = Sets.newHashSet();
        for (Map.Entry<Short, TreeMap<SequenceSortValue, GeoInfo>> shortTreeMapEntry : sequenceTrees.entrySet()) {
            Multimap<GeoInfo, SequenceSortValue> geoInfoSequenceSortValueMultimap = sequenceReverseTrees.get(shortTreeMapEntry.getKey());
            Set<Geometry> entriesInBBox = getEntriesInBBox(minX, maxX, minY, maxY, geoInfoSequenceSortValueMultimap);
            Set<Geometry> entriesInRange = getEntriesInRange(rangeStartCriterion, rangeEndCriterion, shortTreeMapEntry);
            Sets.SetView<Geometry> intersection = Sets.intersection(entriesInBBox, entriesInRange);
            descendants.addAll(intersection);
        }
        return descendants;
    }

    private Set<Geometry> getEntriesInBBox(double minX, double maxX, double minY, double maxY, Multimap<GeoInfo, SequenceSortValue> geoInfoSequenceSortValueMultimap) {
        Set<Geometry> geometries = Sets.newHashSet();
        for (GeoInfo geoInfo : geoInfoSequenceSortValueMultimap.keySet()) {
            if (isInBBox(minX, maxX, minY, maxY, geoInfo)) {
                geometries.add(geoInfo.geometry);
            }
        }
        return geometries;
    }

    private boolean isInBBox(double minX, double maxX, double minY, double maxY, GeoInfo geoInfo) {
        return ((minX < geoInfo.geometry.getX()) && (maxX > geoInfo.geometry.getX())) && ((minY < geoInfo.geometry.getY()) && (maxY > geoInfo.geometry.getY()));
    }

    private Set<Geometry> getDescendants(double minX, double maxX, double minY, double maxY) {
        return latLongGrid.getDescendants(minX, maxX, minY, maxY);
    }

    @Override
   public Set<Geometry> getDescendants(MapSummaryExtent mapSummaryExtent) {
        return getDescendants(mapSummaryExtent.getXMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMin(), mapSummaryExtent.getYMax());
    }

    public Set<Geometry> getDescendants(MapSummaryExtent mapSummaryExtent, SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion) {
        return getDescendants(mapSummaryExtent.getXMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMin(), mapSummaryExtent.getYMax(), rangeStartCriterion, rangeEndCriterion);
    }

    public Set<Geometry> getDescendants(SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion) {
        Set<Geometry> descendants = Sets.newHashSet();
        for (Map.Entry<Short, TreeMap<SequenceSortValue, GeoInfo>> shortTreeMapEntry : sequenceTrees.entrySet()) {
            descendants.addAll(getEntriesInRange(rangeStartCriterion, rangeEndCriterion, shortTreeMapEntry));
        }
        return descendants;
    }

    private Set<Geometry> getEntriesInRange(SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion, Map.Entry<Short, TreeMap<SequenceSortValue, GeoInfo>> shortTreeMapEntry) {
        Set<Geometry> geometries = Sets.newHashSet();
        TreeMap<SequenceSortValue, GeoInfo> sequenceGeoMap = shortTreeMapEntry.getValue();
        Set<Map.Entry<SequenceSortValue, GeoInfo>> entries = sequenceGeoMap.entrySet();
        Map.Entry<SequenceSortValue, GeoInfo> previousEntry = null;
        boolean inRange = false;
        for (Map.Entry<SequenceSortValue, GeoInfo> entry : entries) {
            if (isInRange(rangeStartCriterion, rangeEndCriterion, entry.getKey())) {
                inRange = true;
                if (null != previousEntry) {
                    geometries.add(previousEntry.getValue().geometry);
                }
            } else if (inRange) {
                geometries.add(previousEntry.getValue().geometry);
                break;
            }
            previousEntry = entry;
        }
        return geometries;
    }

    @Override
   public Set<Geometry> getDescendants() {
        Set<Geometry> geometries = Sets.newHashSet();
        sequenceReverseTrees.values().forEach(geoInfoSequenceSortValueMultimap -> geoInfoSequenceSortValueMultimap.keySet().forEach(geoInfo -> geometries.add(geoInfo.getGeometry())));
        return geometries;
    }

    @Override
   public Set<Geometry> getCombinedTypeGeometries() {
        return latLongGrid.getCombinedTypeGeometries();
    }

    @Override
   public Set<Geometry> getNewTypeGeometries() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
   public Set<Geometry> getUpdatedTypeGeometries() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
   public Set<PlaceidTypenameDuple> getTypes() {
        return typeToTypeIndex.keySet();
    }

    @Override
   public boolean hasCombinedType() {
        return combinedTypeCount > 0;
    }

    @Override
   public int getNewCount() {
        return 0;
    }

    @Override
   public int getUpdateCount() {
        return 0;
    }

    public boolean containsKey(Geometry geometry) {
        return latLongGrid.containsKey(geometry);
    }

    public void registerNode(Geometry geometry, TrackidTracknameDuple trackKey, Object sequence, int rowId) {
        short trackIndex = getTrackIndex(trackKey);
        GeoInfo geoInfo = getGeoInfo(geometry);
        geoInfo.getSequences().add(trackIndex);

        TreeMap<SequenceSortValue, GeoInfo> geoInfos = getSequenceTrees().get(trackIndex);
        Multimap<GeoInfo, SequenceSortValue> reverseMap = sequenceReverseTrees.get(trackIndex);
        if (geoInfos == null) {
            geoInfos = Maps.newTreeMap();
            getSequenceTrees().put(trackIndex, geoInfos);
            reverseMap = HashMultimap.create();
            sequenceReverseTrees.put(trackIndex, reverseMap);
        }
        SequenceSortValue value = new SequenceSortValue((Comparable) sequence, rowId);
        geoInfos.put(value, geoInfo);
        reverseMap.put(geoInfo, value);
    }

    public boolean isTrackTypeEmpty(TrackidTracknameDuple trackKey) {
        short trackIndex = getTrackIndex(trackKey);
        TreeMap<SequenceSortValue, GeoInfo> sequenceSortValueGeoInfoTreeMap = sequenceTrees.get(trackIndex);
        if (sequenceSortValueGeoInfoTreeMap == null) {
         return true;
      }
        return sequenceSortValueGeoInfoTreeMap.size() < 2;
    }

    private short getTrackIndex(TrackidTracknameDuple key) {
        if (trackToTrackIndex.containsKey(key)) {
            return trackToTrackIndex.get(key);
        }
        lastTrackIndex++;
        trackToTrackIndex.put(key, lastTrackIndex);
        return lastTrackIndex;
    }

    private GeoInfo getGeoInfo(Geometry geometry) {
        return latLongGrid.getLongGrid(geometry).getGeoInfo(geometry);
    }

    private Map<Short, TreeMap<SequenceSortValue, GeoInfo>> getSequenceTrees() {
        return sequenceTrees;
    }

    Set<LinkGeometry> getLinkDescendants(Geometry geometry) {
        int summaryLevel = geometry.getSummaryLevel();
        double minX = geometry.getX();
        double maxX = minX + (1 * Math.pow(10, -summaryLevel));
        double minY = geometry.getY();
        double maxY = minY + (1 * Math.pow(10, -summaryLevel));
        return getLinkDescendants(minX, maxX, minY, maxY);
    }

    private Set<LinkGeometry> getLinkDescendants(double minX, double maxX, double minY, double maxY, SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion) {
        Set<LinkGeometry> linkDescendants = Sets.newHashSet();
        sequenceTrees.values().forEach(sequenceSortValueGeoInfoTreeMap -> {
            boolean previousValid = false;
            GeoInfo previous = null;
            for (Map.Entry<SequenceSortValue, GeoInfo> sequenceSortValueGeoInfoEntry : sequenceSortValueGeoInfoTreeMap.entrySet()) {
                SequenceSortValue currentSequenceSortValue = sequenceSortValueGeoInfoEntry.getKey();
                GeoInfo current = sequenceSortValueGeoInfoEntry.getValue();
                boolean currentValid = isInRange(rangeStartCriterion, rangeEndCriterion, currentSequenceSortValue) && isInBBox(minX, maxX, minY, maxY, current);
                if ((previous != null) && (previousValid || currentValid)) {
                    linkDescendants.add(new LinkGeometry(0, previous.getGeometry(), current.getGeometry()));
                }
                previous = current;
                previousValid = currentValid;
            }
        });
        return linkDescendants;
    }

    private Set<LinkGeometry> getLinkDescendants(double minX, double maxX, double minY, double maxY) {
        Set<Geometry> geometries = latLongGrid.getDescendants(minX, maxX, minY, maxY);
        Set<LinkGeometry> descendants = Sets.newHashSet();
        geometries.forEach(geometry -> descendants.addAll(getLinksComposedOf(geometry)));
        return descendants;
    }

    Set<LinkGeometry> getLinksComposedOf(Geometry geometry) {
        Set<LinkGeometry> descendants = Sets.newHashSet();

        GeoInfo geoInfo = getGeoInfo(geometry);
        geoInfo.getSequences().forEach(sequence -> descendants.addAll(getLinksOfGeoInfo(geoInfo, sequence)));

        return descendants;
    }

    public Set<LinkGeometry> getLinkGeometries(TrackidTracknameDuple key, Object sequenceValue, int rowId) {
        Set<LinkGeometry> linkGeometries = Sets.newHashSet();

        short trackIndex = getTrackIndex(key);
        TreeMap<SequenceSortValue, GeoInfo> sequenceTree = sequenceTrees.get(trackIndex);
        if (sequenceTree != null) {
            SequenceSortValue sequenceSortValue = new SequenceSortValue((Comparable) sequenceValue, rowId);
            GeoInfo geoInfo = sequenceTree.get(sequenceSortValue);
            if (geoInfo != null) {
                linkGeometries.addAll(getLinkGeometries(sequenceTree, sequenceSortValue, geoInfo));
            }
        }

        return linkGeometries;
    }

    private Set<LinkGeometry> getLinkGeometries(TreeMap<SequenceSortValue, GeoInfo> sequenceTree, SequenceSortValue sequenceSortValue, GeoInfo geoInfo) {
        Set<LinkGeometry> linkGeometries = Sets.newTreeSet();

        SequenceSortValue higherKey = sequenceTree.higherKey(sequenceSortValue);
        if (higherKey != null) {
            GeoInfo after = sequenceTree.get(higherKey);
            //FIXME: should probably not be 0
            linkGeometries.add(new LinkGeometry(0, geoInfo.geometry, after.geometry));
        }
        SequenceSortValue lowerKey = sequenceTree.lowerKey(sequenceSortValue);
        if (lowerKey != null) {
            GeoInfo before = sequenceTree.get(lowerKey);
            //FIXME: should probably not be 0
            linkGeometries.add(new LinkGeometry(0, before.geometry, geoInfo.geometry));
        }

        return linkGeometries;
    }

    public Set<LinkGeometry> getLinkDescendants(MapSummaryExtent mapSummaryExtent, SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion) {
        return getLinkDescendants(mapSummaryExtent.getXMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMin(), mapSummaryExtent.getYMax(), rangeStartCriterion, rangeEndCriterion);
    }

    public Set<LinkGeometry> getLinkDescendants(MapSummaryExtent mapSummaryExtent) {
        return getLinkDescendants(mapSummaryExtent.getXMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMin(), mapSummaryExtent.getYMax());
    }

    public Collection<Integer> getRowIds(LinkGeometry linkGeometry) {
        IntCollection rowIds = new IntCollection();
        for (Map.Entry<Short, TreeMap<SequenceSortValue, GeoInfo>> trackSequence : sequenceTrees.entrySet()) {
            Multimap<GeoInfo, SequenceSortValue> geoSeqMultiMap = sequenceReverseTrees.get(trackSequence.getKey());
            Collection<SequenceSortValue> seqValuesOfNode1 = geoSeqMultiMap.get(getGeoInfo(linkGeometry.getNode1Geometry()));
            for (SequenceSortValue sequenceValueNode1 : seqValuesOfNode1) {
                Map.Entry<SequenceSortValue, GeoInfo> node2CandidateEntry = trackSequence.getValue().higherEntry(sequenceValueNode1);
                if (node2CandidateEntry != null) {
                    if (linkGeometry.getNode2Geometry().equals(node2CandidateEntry.getValue().geometry)) {
                        rowIds.add(sequenceValueNode1.getRowId());
                        rowIds.add(node2CandidateEntry.getKey().getRowId());
                    }
                }
            }
        }
        return rowIds;
    }

    public Set<LinkGeometry> getLinkGeometriesOfType(PlaceidTypenameDuple key) {
        Set<LinkGeometry> linkGeometries = Sets.newHashSet();

        Set<Geometry> geometries = getGeometriesOfType(key);
        geometries.forEach(geometry ->
                {
                    GeoInfo geoInfo = getGeoInfo(geometry);
                    sequenceReverseTrees.keySet().forEach(trackIndex -> linkGeometries.addAll(getLinksOfGeoInfo(geoInfo, trackIndex)));
                }
        );

        return linkGeometries;
    }

    private Set<LinkGeometry> getLinksOfGeoInfo(GeoInfo geoInfo, Short trackIndex) {
        Set<LinkGeometry> linkGeometries = Sets.newHashSet();

        Multimap<GeoInfo, SequenceSortValue> geoInfoSequenceSortValueMultimap = sequenceReverseTrees.get(trackIndex);
        Collection<SequenceSortValue> sequenceSortValues = geoInfoSequenceSortValueMultimap.get(geoInfo);
        TreeMap<SequenceSortValue, GeoInfo> sequenceGeoMap = sequenceTrees.get(trackIndex);
        sequenceSortValues.forEach(sequenceSortValue -> linkGeometries.addAll(getLinkGeometries(sequenceGeoMap, sequenceSortValue, geoInfo)));

        return linkGeometries;
    }

    public Set<LinkGeometry> getLinkGeometriesOfTrack(TrackidTracknameDuple key) {
        Set<LinkGeometry> linkGeometries = Sets.newHashSet();

        short trackIndex = getTrackIndex(key);
        Map<SequenceSortValue, GeoInfo> sequenceTree = sequenceTrees.get(trackIndex);
        if (sequenceTree != null) {
            boolean first = true;
            GeoInfo pNode = null;
            for (Map.Entry<SequenceSortValue, GeoInfo> entry : sequenceTree.entrySet()) {
                if (first) {
                    pNode = entry.getValue();
                    first = false;
                } else {
                    GeoInfo qNode = entry.getValue();
                    /// FIXME: linkType default should not be 0
                    linkGeometries.add(new LinkGeometry(0, pNode.geometry, qNode.geometry));
                    pNode = qNode;
                }
            }
        }

        return linkGeometries;
    }

    public Set<Integer> getRowIds(List<MapSummaryExtent> mapSummaryExtents, SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion) {
        Set<Integer> rowIds = Sets.newHashSet();

        List<RangeChecker> rangeCheckers = new ArrayList<RangeChecker>();
        if (mapSummaryExtents != null) {
            mapSummaryExtents.forEach(mapSummaryExtent -> rangeCheckers.add(new RangeChecker(mapSummaryExtent)));
        }
        for (Map.Entry<Short, TreeMap<SequenceSortValue, GeoInfo>> entry : sequenceTrees.entrySet()) { //for each track
            Map<SequenceSortValue, GeoInfo> sequenceTree = entry.getValue();
            boolean first = true;
            SequenceSortValue p = null;
            boolean pInMapSummaryExtents = true;
            for (Map.Entry<SequenceSortValue, GeoInfo> entry2 : sequenceTree.entrySet()) {
                if (first) {
                    p = entry2.getKey();
                    GeoInfo node = entry2.getValue();
                    pInMapSummaryExtents = inMapSummaryExtents(rangeCheckers, node);
                    first = false;
                } else {
                    SequenceSortValue q = entry2.getKey();
                    GeoInfo node = entry2.getValue();
                    boolean qInMapSummaryExtents = inMapSummaryExtents(rangeCheckers, node);
                    int qVs = q.compareTo(rangeStartCriterion);
                    int qVe = q.compareTo(rangeEndCriterion);
                    int pVs = p.compareTo(rangeStartCriterion);
                    int pVe = p.compareTo(rangeEndCriterion);
//                    boolean rangeInPQ = isInRange(p, q, rangeStartCriterion, rangeEndCriterion);
//                    if ((pInMapSummaryExtents || qInMapSummaryExtents) && (pInRange || qInRange || rangeInPQ)) {
                    if (pInMapSummaryExtents || qInMapSummaryExtents) { //at least one point in segment must be in extents
                        if (((pVs <= 0) && (qVs >= 0)) || ((pVe <= 0) && (qVe >= 0)) || ((pVs >= 0) && (qVe <= 0))) {
                            rowIds.add(p.getRowId());
                            rowIds.add(q.getRowId());
                        }
                    }
                    p = q;
                    pInMapSummaryExtents = qInMapSummaryExtents;
                }
            }
        }

        return rowIds;
    }

    private boolean isInRange(SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion, SequenceSortValue sequenceSortValue) {
        if ((rangeStartCriterion == null) || (rangeEndCriterion == null)) {
         return true;
      }

        return (sequenceSortValue.compareTo(rangeStartCriterion) > 0) && (sequenceSortValue.compareTo(rangeEndCriterion) < 0);
    }

//    private boolean isInRange(SequenceSortValue p, SequenceSortValue q, Object rangeStartCriterion, Object rangeEndCriterion) {
//        if (rangeStartCriterion == null || rangeEndCriterion == null)
//            return true;
//        Comparable smallerSequenceValue = p.sequenceValue;
//        Comparable largerSequenceValue = q.sequenceValue;
//        return smallerSequenceValue.compareTo(rangeStartCriterion) <= 0 && largerSequenceValue.compareTo(rangeStartCriterion) >= 0;
//    }

    private boolean inMapSummaryExtents(List<RangeChecker> rangeCheckers, GeoInfo pNode) {
        if (rangeCheckers.isEmpty()) {
         return true;
      }
        Geometry geometry = pNode.getGeometry();
        for (RangeChecker rangeChecker : rangeCheckers) {
            if (rangeChecker.isInRange(geometry)) {
               return true;
            }
        }
        return false;
    }

    public SequenceSortValue getSequenceValue(Integer index) {
        List<SequenceSortValue> seriesValues = MapCacheUtil.getSeriesValues(mapViewDefUuid);

        return (seriesValues.isEmpty() || (seriesValues.size() <= index) || (index == -1)) ? null : seriesValues.get(index);
    }

    public Set<LinkGeometry> getLinkDescendants(SequenceSortValue rangeStartCriterion, SequenceSortValue rangeEndCriterion) {
        Set<LinkGeometry> descendants = Sets.newHashSet();
        for (Map.Entry<Short, TreeMap<SequenceSortValue, GeoInfo>> shortTreeMapEntry : sequenceTrees.entrySet()) {
            TreeMap<SequenceSortValue, GeoInfo> sequenceGeoMap = shortTreeMapEntry.getValue();
            Set<Map.Entry<SequenceSortValue, GeoInfo>> entries = sequenceGeoMap.entrySet();
            Map.Entry<SequenceSortValue, GeoInfo> previousEntry = null;
            boolean inRange = false;
            for (Map.Entry<SequenceSortValue, GeoInfo> entry : entries) {
                if (isInRange(rangeStartCriterion, rangeEndCriterion, entry.getKey())) {
                    inRange = true;
                    if (null != previousEntry) {
                        descendants.add(new LinkGeometry(0, previousEntry.getValue().geometry, entry.getValue().geometry));
                    }
                } else if (inRange) {
                    descendants.add(new LinkGeometry(0, previousEntry.getValue().geometry, entry.getValue().geometry));
                    break;
                }
                previousEntry = entry;
            }
        }
        return descendants;
    }

    public Set<LinkGeometry> getAllLinks() {
        Set<LinkGeometry> allLinks;
        int capacity = 1;
        for (TreeMap<SequenceSortValue, GeoInfo> sortValueGeoInfoTreeMap : sequenceTrees.values()) {
            capacity += sortValueGeoInfoTreeMap.size() - 1;
        }
        allLinks = new HashSet<>(capacity);
        sequenceTrees.values().forEach(sequenceSortValueGeoInfoTreeMap -> {
            GeoInfo previous = null;
            for (GeoInfo current : sequenceSortValueGeoInfoTreeMap.values()) {
                if (previous != null) {
                    allLinks.add(new LinkGeometry(0, previous.getGeometry(), current.getGeometry()));
                }
                previous = current;
            }
        });
        return allLinks;
    }

    public List<LinkGeometry> getSequenceFirstSegmentsAfter(Integer rangeStart, Integer rangeEnd) {
        List<LinkGeometry> out = new ArrayList<LinkGeometry>(sequenceTrees.size());
        SequenceSortValue rssv = getSequenceValue(rangeStart);
//        SequenceSortValue rangeEndSequenceValue = getSequenceValue(rangeEnd);
        if (rssv != null) {
            for (Map.Entry<Short, TreeMap<SequenceSortValue, GeoInfo>> shortTreeMapEntry : sequenceTrees.entrySet()) {
                TreeMap<SequenceSortValue, GeoInfo> sequenceTree = shortTreeMapEntry.getValue();
                SequenceSortValue key = new SequenceSortValue(rssv.getSequenceValue(), 0);
                Map.Entry<SequenceSortValue, GeoInfo> endGeo = sequenceTree.higherEntry(key);
                if (endGeo != null) {

                    Map.Entry<SequenceSortValue, GeoInfo> startEntry = sequenceTree.lowerEntry(key);
                    GeoInfo startGeo;
                    if (startEntry == null) {
                        startGeo = endGeo.getValue();
                        endGeo = sequenceTree.higherEntry(endGeo.getKey());
                    } else {
                        startGeo = startEntry.getValue();
                    }
                    if (endGeo.getKey().compareTo(rssv) > 0) {
                        out.add(new LinkGeometry(0, startGeo.getGeometry(), endGeo.getValue().getGeometry()));
                    }
                }
            }
        }
        return out;
    }

    public List<LinkGeometry> getSequenceLastSegmentBefore(Integer rangeStart, Integer rangeEnd) {
        List<LinkGeometry> out = new ArrayList<LinkGeometry>(sequenceTrees.size());
        SequenceSortValue resv = getSequenceValue(rangeEnd);
        if (resv != null) {
            for (Map.Entry<Short, TreeMap<SequenceSortValue, GeoInfo>> shortTreeMapEntry : sequenceTrees.entrySet()) {
                TreeMap<SequenceSortValue, GeoInfo> sequenceTree = shortTreeMapEntry.getValue();
                SequenceSortValue key = new SequenceSortValue(resv.getSequenceValue(), Integer.MAX_VALUE);
                Map.Entry<SequenceSortValue, GeoInfo> endEntry = sequenceTree.higherEntry(key);
                if (endEntry == null) {
                    endEntry = sequenceTree.lowerEntry(key);
                }
                GeoInfo endGeo = endEntry.getValue();
                Map.Entry<SequenceSortValue, GeoInfo> startGeo = sequenceTree.lowerEntry(endEntry.getKey());
                if (startGeo != null) {
                    if (new SequenceSortValue(startGeo.getKey().getSequenceValue(), 0).compareTo(resv) <= 0) {
                        out.add(new LinkGeometry(0, startGeo.getValue().getGeometry(), endGeo.getGeometry()));
                    }
                }
            }
        }
        return out;
    }

    public Set<Integer> getRowIds(List<MapSummaryExtent> mapSummaryExtents, Integer rangeStart, Integer rangeEnd, List<TrackMapSummaryGrid.SequenceSortValue> seriesValues) {
        Set<Integer> rowIds = Sets.newHashSet();
        TrackMapSummaryGrid.SequenceSortValue rangeStartCriterion;
        TrackMapSummaryGrid.SequenceSortValue rangeEndCriterion;
        if (seriesValues != null) {
            int seriesValuesSize = seriesValues.size();
            if (rangeStart == null) {
                rangeStart = 0;
            }
            if (rangeStart < seriesValuesSize) {
                if ((rangeEnd == null) || (rangeEnd >= seriesValuesSize)) {
                    rangeEnd = seriesValuesSize - 1;
                }
                rangeStartCriterion = seriesValues.get(rangeStart);
                rangeStartCriterion = new SequenceSortValue(rangeStartCriterion.getSequenceValue(), Integer.MIN_VALUE);
                rangeEndCriterion = seriesValues.get(rangeEnd);
                rangeEndCriterion = new SequenceSortValue(rangeEndCriterion.getSequenceValue(), Integer.MAX_VALUE);
                rowIds.addAll(getRowIds(mapSummaryExtents, rangeStartCriterion, rangeEndCriterion));
            }
        }
        return rowIds;
    }

    private static class LatLongGrid implements Serializable {
        //FIXME: inefficient structure for most data
        private TreeMap<Long, LongGrid> latToLongMap;

        LatLongGrid() {
            latToLongMap = Maps.newTreeMap();
        }

        private static long getKey(int summaryLevel, double y) {
            return (long) (Math.floor(y * Math.pow(10, summaryLevel)));
        }

        public boolean containsKey(Geometry geometry) {
            long key = getKey(geometry);
            if (latToLongMap.containsKey(key)) {
                LongGrid longGrid = latToLongMap.get(key);
                return longGrid.containsKey(geometry);
            }
            return false;
        }

        private long getKey(Geometry geometry) {
            int summaryLevel = geometry.getSummaryLevel();
            double y = geometry.getY();
            return getKey(summaryLevel, y);
        }

        void addRowIdAndType(Geometry geometry, Integer rowId, short typeIndex, TrackMapSummaryGrid trackMapSummaryGrid) {
            LongGrid longGrid = getLongGrid(geometry);
            longGrid.addRowIdAndType(geometry, rowId, typeIndex, trackMapSummaryGrid);
        }

        private LongGrid getLongGrid(Geometry geometry) {
            long key = getKey(geometry);
            LongGrid longGrid;
            if (latToLongMap.containsKey(key)) {
                longGrid = latToLongMap.get(key);
            } else {
                longGrid = new LongGrid();
                latToLongMap.put(key, longGrid);
            }
            return longGrid;
        }

        Set<Geometry> getDescendants(double minX, double maxX, double minY, double maxY) {
            int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
            Set<Geometry> descendants = Sets.newHashSet();
            Long fromKey = getKey(summaryLevel, minY);
            Long toKey = getKey(summaryLevel, maxY);
            Map<Long, LongGrid> subMap = latToLongMap.subMap(fromKey, toKey);
            for (Map.Entry<Long, LongGrid> entry : subMap.entrySet()) {
                LongGrid longGrid = entry.getValue();
                descendants.addAll(longGrid.getDescendants(minX, maxX));
            }
            return descendants;
        }

        Set<Geometry> getGeometriesOfType(short typeIndex) {
            Set<Geometry> descendants = Sets.newHashSet();
            for (Map.Entry<Long, LongGrid> entry : latToLongMap.entrySet()) {
                LongGrid longGrid = entry.getValue();
                descendants.addAll(longGrid.getGeometriesOfType(typeIndex));
            }
            return descendants;
        }

        Set<Geometry> getCombinedTypeGeometries() {
            Set<Geometry> descendants = Sets.newHashSet();
            for (Map.Entry<Long, LongGrid> entry : latToLongMap.entrySet()) {
                LongGrid longGrid = entry.getValue();
                descendants.addAll(longGrid.getCombinedTypeGeometries());
            }
            return descendants;
        }

    }

    private static class LongGrid implements Serializable {
        private TreeMap<Long, GeoInfo> longToGeoInfoMap;

        LongGrid() {
            longToGeoInfoMap = Maps.newTreeMap();
        }

        private static long getKey(int summaryLevel, double x) {
            return (long) (Math.floor(x * Math.pow(10, summaryLevel)));
        }

        public boolean containsKey(Geometry geometry) {
            long key = getKey(geometry);
            return longToGeoInfoMap.containsKey(key);
        }

        private long getKey(Geometry geometry) {
            int summaryLevel = geometry.getSummaryLevel();
            double x = geometry.getX();
            return getKey(summaryLevel, x);
        }

        void addRowIdAndType(Geometry geometry, int rowId, short typeIndex, TrackMapSummaryGrid trackMapSummaryGrid) {
            GeoInfo geoInfo = getGeoInfo(geometry);
            geoInfo.addRowIdAndType(rowId, typeIndex, trackMapSummaryGrid);
        }

        private GeoInfo getGeoInfo(Geometry geometry) {
            long key = getKey(geometry);
            GeoInfo geoInfo;
            if (longToGeoInfoMap.containsKey(key)) {
                geoInfo = longToGeoInfoMap.get(key);
            } else {
                geoInfo = new GeoInfo(geometry);
                longToGeoInfoMap.put(key, geoInfo);
            }
            return geoInfo;
        }

        Set<Geometry> getDescendants(double minX, double maxX) {
            int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
            Set<Geometry> descendants = Sets.newHashSet();
            Long fromKey = getKey(summaryLevel, minX);
            Long toKey = getKey(summaryLevel, maxX);
            Map<Long, GeoInfo> subMap = longToGeoInfoMap.subMap(fromKey, toKey);
            for (Map.Entry<Long, GeoInfo> entry : subMap.entrySet()) {
                GeoInfo geoInfo = entry.getValue();
                descendants.add(geoInfo.getGeometry());
            }
            return descendants;
        }

        Set<Geometry> getGeometriesOfType(short typeIndex) {
            Set<Geometry> descendants = Sets.newHashSet();
            for (Map.Entry<Long, GeoInfo> entry : longToGeoInfoMap.entrySet()) {
                GeoInfo geoInfo = entry.getValue();
                if (geoInfo.isOfType(typeIndex)) {
                  descendants.add(geoInfo.getGeometry());
               }
            }
            return descendants;
        }

        Set<Geometry> getCombinedTypeGeometries() {
            Set<Geometry> descendants = Sets.newHashSet();
            for (Map.Entry<Long, GeoInfo> entry : longToGeoInfoMap.entrySet()) {
                GeoInfo geoInfo = entry.getValue();
                if (geoInfo.isCombinedType()) {
                  descendants.add(geoInfo.getGeometry());
               }
            }
            return descendants;
        }
    }

    private static class GeoInfo implements Serializable {
        private Geometry geometry;
        //FIXME: might not need rowIds
        private IntCollection rowIds;
        private BitSet[] types;
        private Set<Short> sequences = Sets.newHashSet();
        private boolean isEmpty = true;
        private boolean combinedType = false;
        private int bitSetSize;

        GeoInfo(Geometry geometry) {
            this.geometry = geometry;
            rowIds = new IntCollection();
            types = new BitSet[1];
            BitSet typesBitSet = new BitSet();
            bitSetSize = typesBitSet.size();
            types[0] = typesBitSet;
        }

        Set<Short> getSequences() {
            return sequences;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        void addRowIdAndType(int rowId, short typeIndex, TrackMapSummaryGrid trackMapSummaryGrid) {
            addRowId(rowId);
            addType(typeIndex, trackMapSummaryGrid);
        }

        private void addRowId(int rowId) {
            rowIds.add(rowId);
        }

        private void addType(short typeIndex, TrackMapSummaryGrid trackMapSummaryGrid) {
            int arrayIndex = typeIndex / bitSetSize;
            int bitIndex = typeIndex % bitSetSize;

            int neededSize = arrayIndex + 1;
            if (types.length < neededSize) {
                BitSet[] newTypes = new BitSet[neededSize];
                System.arraycopy(types, 0, newTypes, 0, types.length);
                types = newTypes;
            }

            BitSet bitSet = types[arrayIndex];
            if ((bitSet == null) || !bitSet.get(bitIndex)) {
                if (!combinedType && !isEmpty) {
                    combinedType = true;
                    trackMapSummaryGrid.incrementCombinedTypeCount();
                }
                if (bitSet == null) {
                    bitSet = new BitSet();
                    types[arrayIndex] = bitSet;
                }
                isEmpty = false;
                bitSet.set(bitIndex);
                trackMapSummaryGrid.incrementTypeCount(typeIndex);
            }
        }

        public Set<Integer> getRowIds() {
            return Sets.newHashSet(Ints.asList(rowIds.toIntArray()));
        }

        boolean isOfType(short typeIndex) {
            int arrayIndex = typeIndex / bitSetSize;
            if (types.length < (arrayIndex + 1)) {
               return false;
            }

            BitSet bitSet = types[arrayIndex];
            if (bitSet == null) {
               return false;
            }

            int bitIndex = typeIndex % bitSetSize;
            return bitSet.get(bitIndex);
        }

        boolean isCombinedType() {
            return combinedType;
        }
    }

    public static class SequenceSortValue implements Comparable<SequenceSortValue>, Serializable {
        private final Comparable sequenceValue;
        private final int rowId;

        public SequenceSortValue(Comparable sequenceValue, int rowId) {
            this.sequenceValue = sequenceValue;
            this.rowId = rowId;
        }

        public Comparable getSequenceValue() {
            return sequenceValue;
        }

        public int getRowId() {
            return rowId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sequenceValue, rowId);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SequenceSortValue)) {
                return false;
            } else {
                SequenceSortValue other = (SequenceSortValue) o;
                if (this.sequenceValue.compareTo(other.sequenceValue) == 0) {
                    return this.rowId == other.rowId;
                } else {
                    return false;
                }
            }
        }

        @Override
        public int compareTo(SequenceSortValue o) {
            return ComparisonChain.start().compare(this.sequenceValue, o.sequenceValue).compare(this.rowId, o.rowId).result();
        }
    }
}