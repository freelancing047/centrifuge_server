package csi.server.business.visualization.map;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import csi.config.Configuration;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class PointMapSummaryGrid implements Serializable, MapSummaryGrid {
    private Map<PlaceidTypenameDuple, Short> typeToTypeIndex;
    private short lastTypeIndex;
    private LatLongGrid latLongGrid;
    private int numberOfItems;
    private boolean hasCombinedType;
    private Map<Short, Integer> typeCount;
    private int combinedTypeCount;
    private int newestInternalStateId;
    private int newCount;
    private int updateCount;

    public PointMapSummaryGrid() {
        typeToTypeIndex = Maps.newHashMap();
        lastTypeIndex = -1;
        latLongGrid = new LatLongGrid();
        numberOfItems = 0;
        hasCombinedType = false;
        typeCount = Maps.newHashMap();
        combinedTypeCount = 0;
        newCount = 0;
        updateCount = 0;
    }

    @Override
   public void setNewestInternalTypeId(int newestInternalTypeId) {
        this.newestInternalStateId = newestInternalTypeId;
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

    @Override
   public Set<Geometry> getGeometriesOfType(PlaceidTypenameDuple key) {
        short typeIndex = getTypeIndex(key);
        return latLongGrid.getGeometriesOfType(typeIndex);
    }

    public boolean containsKey(Geometry geometry) {
        return latLongGrid.containsKey(geometry);
    }

    @Override
   public void addNode(Geometry geometry, int rowId, int internalStateId, PlaceidTypenameDuple key) {
        short typeIndex = getTypeIndex(key);
        if (latLongGrid.addRowIdAndType(geometry, rowId, internalStateId, typeIndex)) {
         numberOfItems++;
      }
    }

    @Override
   public Set<Integer> getRowIds(Geometry geometry) {
        if (geometry.getSummaryLevel() == Configuration.getInstance().getMapConfig().getDetailLevel()) {
         return latLongGrid.getRowIds(geometry);
      } else {
            Set<Integer> rowIds = Sets.newHashSet();
            for (Geometry geometry1 : getDescendants(geometry)) {
                Set<Integer> ids = getRowIds(geometry1);
                rowIds.addAll(ids);
            }
            return rowIds;
        }
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

    @Override
   public Set<Geometry> getDescendants(MapSummaryExtent mapSummaryExtent) {
        return getDescendants(mapSummaryExtent.getXMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMin(), mapSummaryExtent.getYMax());
    }

    private Set<Geometry> getDescendants(double minX, double maxX, double minY, double maxY) {
        return latLongGrid.getDescendants(minX, maxX, minY, maxY);
    }

    @Override
   public Set<Geometry> getCombinedTypeGeometries() {
        return latLongGrid.getCombinedTypeGeometries();
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    @Override
   public boolean hasCombinedType() {
        return combinedTypeCount > 0;
    }

    @Override
   public Set<PlaceidTypenameDuple> getTypes() {
        return typeToTypeIndex.keySet();
    }

    private void incrementTypeCount(short typeIndex) {
        if (typeCount.containsKey(typeIndex)) {
         typeCount.put(typeIndex, typeCount.get(typeIndex) + 1);
      } else {
         typeCount.put(typeIndex, 1);
      }
    }

    public int getTypeCount(PlaceidTypenameDuple key) {
        short typeIndex = getTypeIndex(key);
        return typeCount.get(typeIndex);
    }

    private void incrementCombinedTypeCount() {
        combinedTypeCount++;
    }

    public int getCombinedTypeCount() {
        return combinedTypeCount;
    }

    private void incrementNewCount() {
        newCount++;
    }

    private void decrementNewCount() {
        newCount--;
    }

    @Override
   public int getNewCount() {
        return newCount;
    }

    private void incrementUpdateCount() {
        updateCount++;
    }

    @Override
   public int getUpdateCount() {
        return updateCount;
    }

    @Override
   public Set<Geometry> getNewTypeGeometries() {
        return latLongGrid.getNewTypeGeometries();
    }

    @Override
   public Set<Geometry> getUpdatedTypeGeometries() {
        return latLongGrid.getUpdatedTypeGeometries();
    }

    class LatLongGrid implements Serializable {
        private TreeMap<Long, LongGrid> latToLongMap;

        LatLongGrid() {
            latToLongMap = Maps.newTreeMap();
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

        private long getKey(int summaryLevel, double y) {
            return (long) (Math.floor(y * Math.pow(10, summaryLevel)));
        }

        boolean addRowIdAndType(Geometry geometry, Integer rowId, int internalStateId, short typeIndex) {
            LongGrid longGrid = getLongGrid(geometry);
            return longGrid.addRowIdAndType(geometry, rowId, internalStateId, typeIndex);
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

        Set<Integer> getRowIds(Geometry geometry) {
            long key = getKey(geometry);
            LongGrid longGrid = latToLongMap.get(key);
            if (longGrid != null) {
               return longGrid.getRowIds(geometry);
            } else {
               return Sets.newHashSet();
            }
        }

        Set<Geometry> getDescendants(double minX, double maxX, double minY, double maxY) {
            int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
            Set<Geometry> descendants = Sets.newHashSet();
            Long fromKey = getKey(summaryLevel, minY);
            Long toKey = getKey(summaryLevel, maxY);
            Map<Long, LongGrid> subMap = latToLongMap.subMap(fromKey, toKey);
            for (Long key : subMap.keySet()) {
                LongGrid longGrid = latToLongMap.get(key);
                descendants.addAll(longGrid.getDescendants(minX, maxX));
            }
            return descendants;
        }

        Set<Geometry> getGeometriesOfType(short typeIndex) {
            Set<Geometry> descendants = Sets.newHashSet();
            for (LongGrid longGrid : latToLongMap.values()) {
                descendants.addAll(longGrid.getGeometriesOfType(typeIndex));
            }
            return descendants;
        }

        Set<Geometry> getCombinedTypeGeometries() {
            Set<Geometry> descendants = Sets.newHashSet();
            for (LongGrid longGrid : latToLongMap.values()) {
                descendants.addAll(longGrid.getCombinedTypeGeometries());
            }
            return descendants;
        }

        Set<Geometry> getNewTypeGeometries() {
            Set<Geometry> descendants = Sets.newHashSet();
            for (LongGrid longGrid : latToLongMap.values()) {
                descendants.addAll(longGrid.getNewTypeGeometries());
            }
            return descendants;
        }

        Set<Geometry> getUpdatedTypeGeometries() {
            Set<Geometry> descendants = Sets.newHashSet();
            for (LongGrid longGrid : latToLongMap.values()) {
                descendants.addAll(longGrid.getUpdatedTypeGeometries());
            }
            return descendants;
        }

        Set<Geometry> getDescendants() {
            Set<Geometry> geometries = Sets.newHashSet();
            latToLongMap.values().forEach(longGrid -> geometries.addAll(longGrid.getDescendants()));
            return geometries;
        }
    }

    class LongGrid implements Serializable {
        private TreeMap<Long, GeoInfo> longToGeoInfoMap;

        LongGrid() {
            longToGeoInfoMap = Maps.newTreeMap();
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

        private long getKey(int summaryLevel, double x) {
            return (long) (Math.floor(x * Math.pow(10, summaryLevel)));
        }

        boolean addRowIdAndType(Geometry geometry, int rowId, int internalStateId, short typeIndex) {
            GeoInfo geoInfo = getGeoInfo(geometry);
            return geoInfo.addRowIdAndType(rowId, internalStateId, typeIndex);
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

        Set<Integer> getRowIds(Geometry geometry) {
            long key = getKey(geometry);
            GeoInfo geoInfo = longToGeoInfoMap.get(key);
            if (geoInfo != null) {
               return geoInfo.getRowIds();
            } else {
               return Sets.newHashSet();
            }
        }

        Set<Geometry> getDescendants(double minX, double maxX) {
            int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
            Set<Geometry> descendants = Sets.newHashSet();
            Long fromKey = getKey(summaryLevel, minX);
            Long toKey = getKey(summaryLevel, maxX);
            Map<Long, GeoInfo> subMap = longToGeoInfoMap.subMap(fromKey, toKey);
            for (Long key : subMap.keySet()) {
                GeoInfo geoInfo = longToGeoInfoMap.get(key);
                descendants.add(geoInfo.getGeometry());
            }
            return descendants;
        }

        Set<Geometry> getGeometriesOfType(short typeIndex) {
            Set<Geometry> descendants = Sets.newHashSet();
            for (Long key : longToGeoInfoMap.keySet()) {
                GeoInfo geoInfo = longToGeoInfoMap.get(key);
                if (geoInfo.isOfType(typeIndex)) {
                  descendants.add(geoInfo.getGeometry());
               }
            }
            return descendants;
        }

        Set<Geometry> getCombinedTypeGeometries() {
            Set<Geometry> descendants = Sets.newHashSet();
            for (Long key : longToGeoInfoMap.keySet()) {
                GeoInfo geoInfo = longToGeoInfoMap.get(key);
                if (geoInfo.isCombinedType()) {
                  descendants.add(geoInfo.getGeometry());
               }
            }
            return descendants;
        }

        Set<Geometry> getNewTypeGeometries() {
            Set<Geometry> descendants = Sets.newHashSet();
            for (Long key : longToGeoInfoMap.keySet()) {
                GeoInfo geoInfo = longToGeoInfoMap.get(key);
                if (geoInfo.isNew()) {
                  descendants.add(geoInfo.getGeometry());
               }
            }
            return descendants;
        }

        Set<Geometry> getUpdatedTypeGeometries() {
            Set<Geometry> descendants = Sets.newHashSet();
            for (Long key : longToGeoInfoMap.keySet()) {
                GeoInfo geoInfo = longToGeoInfoMap.get(key);
                if (geoInfo.isUpdated()) {
                  descendants.add(geoInfo.getGeometry());
               }
            }
            return descendants;
        }

        Set<Geometry> getDescendants() {
            Set<Geometry> geometries = Sets.newHashSet();
            longToGeoInfoMap.values().forEach(geoInfo -> geometries.add(geoInfo.getGeometry()));
            return geometries;
        }
    }

    class RowNode implements Serializable {
        private int rowId;
        private RowNode next;

        RowNode(int rowId) {
            this.rowId = rowId;
        }

        RowNode getNext() {
            return next;
        }

        void setNext(RowNode next) {
            this.next = next;
        }

        int getRowId() {
            return rowId;
        }
    }

    class GeoInfo implements Serializable {
        private Geometry geometry;
        private RowNode first = null;
        private RowNode last = null;
        private BitSet[] types = new BitSet[1];
        private BitSet internalStateIds = new BitSet(1);
        private int largestInternalStateId = -1;
        private boolean isEmpty = true;
        private boolean isNew = false;
        private boolean isUpdated = false;
        private boolean combinedType = false;
        private int bitSetSize;

        GeoInfo(Geometry geometry) {
            this.geometry = geometry;
            BitSet typesBitSet = new BitSet();
            bitSetSize = typesBitSet.size();
            types[0] = typesBitSet;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        boolean addRowIdAndType(int rowId, int internalStateId, short typeIndex) {
            boolean isEmpty = first == null;
            addRowId(rowId);
            addInternalStateId(internalStateId);
            addType(typeIndex);
            return isEmpty;
        }

        private void addRowId(int rowId) {
            RowNode newNode = new RowNode(rowId);
            if (first == null) {
                first = newNode;
                last = newNode;
            } else {
                last.setNext(newNode);
                last = newNode;
            }
        }

        private void addType(short typeIndex) {
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
                    incrementCombinedTypeCount();
                }
                if (bitSet == null) {
                    bitSet = new BitSet();
                    types[arrayIndex] = bitSet;
                }
                isEmpty = false;
                bitSet.set(bitIndex);
                incrementTypeCount(typeIndex);
            }
        }

        private void addInternalStateId(int internalStateId) {
            if (internalStateIds.size() <= internalStateId) {
                BitSet newInternalStateIds = new BitSet(internalStateId + 1);
                copyBitSet(internalStateIds, newInternalStateIds, largestInternalStateId);
                internalStateIds = newInternalStateIds;
            }
            if (newestInternalStateId > 0) {
                if (!internalStateIds.get(internalStateId)) {
                    if (internalStateId == newestInternalStateId) {
                        if (internalStateIds.isEmpty()) {
                            isNew = true;
                            incrementNewCount();
                        } else {
                            isUpdated = true;
                            incrementUpdateCount();
                        }
                    } else {
                        if (isNew) {
                            isNew = false;
                            decrementNewCount();
                            isUpdated = true;
                            incrementUpdateCount();
                        }
                    }
                    if (internalStateId > largestInternalStateId) {
                     largestInternalStateId = internalStateId;
                  }
                    internalStateIds.set(internalStateId);
                }
            }
        }

        private void copyBitSet(BitSet oldBitSet, BitSet newBitSet, int largestIndex) {
            for (int i = 0; i <= largestIndex; i++) {
               if (oldBitSet.get(i)) {
                  newBitSet.set(i);
               }
            }
        }

        public Set<Integer> getRowIds() {
            HashSet<Integer> integers = Sets.newHashSet();
            RowNode current = first;
            while (current != null) {
                integers.add(current.getRowId());
                current = current.getNext();
            }
            return integers;
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

        public boolean isNew() {
            return isNew;
        }

        public boolean isUpdated() {
            return isUpdated;
        }
    }

    @Override
   public Set<Geometry> getDescendants() {
        return latLongGrid.getDescendants();
    }
}
