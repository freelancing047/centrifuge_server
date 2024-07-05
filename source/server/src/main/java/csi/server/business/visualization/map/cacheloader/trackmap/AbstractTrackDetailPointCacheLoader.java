package csi.server.business.visualization.map.cacheloader.trackmap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.MapConfig;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapLink;
import csi.server.business.visualization.map.MapNode;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.MapTrackInfo;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.business.visualization.map.TrackDynamicTypeInfo;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.business.visualization.map.UUIDUtil;
import csi.server.business.visualization.map.cacheloader.AbstractMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.MapNodeUtil;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.MapNodeStatisticsCalculator;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.PlaceTypenameProcessor;
import csi.server.business.visualization.map.mapserviceutil.typesorter.MapTrackSorter;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ColorWheel;
import csi.server.common.model.SortOrder;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.SpatialReference;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.model.visualization.map.MapTrack;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.util.StringUtil;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.MapTooltipFieldDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;
import csi.shared.core.visualization.map.UBox;

public abstract class AbstractTrackDetailPointCacheLoader extends AbstractMapCacheLoader {
   private static final Logger LOG = LogManager.getLogger(AbstractTrackDetailPointCacheLoader.class);

    private MapTheme mapTheme;
    private List<MapSummaryExtent> mapSummaryExtents;
    private boolean isPopulateTrackRelatedInfo;
    private Map<Geometry, Set<Integer>> trackGeometryToRowIds;
    private Map<LinkGeometry, Set<Integer>> trackLinkGeometryToRowIds;
    private Map<Integer, Set<Geometry>> trackRowIdToGeometries;
    private Map<Integer, Set<LinkGeometry>> trackRowIdToLinkGeometries;
    private Map<TrackidTracknameDuple, Set<Geometry>> trackTypeToGeometries;
    private Map<TrackidTracknameDuple, Set<LinkGeometry>> trackTypeToLinkGeometries;
    private boolean maxPlaceSizeEqualsMinPlaceSize;
    private MapNodeInfo mapNodeInfo;
    private Map<Geometry, AugmentedMapNode> mapNodeMapByGeom;
    private Map<Long, AugmentedMapNode> mapNodeMapById;
    private MapTrackInfo mapTrackInfo;
    private Map<String, PlaceStyle> typeNameToPlaceStyle;
    private List<PlaceSettingsDTO> placeSettings;
    private List<TrackSettingsDTO> trackSettings;
    private Map<TrackidTracknameDuple, Map<Object, Set<NodeIdRowIdPair>>> tracks;
    private TrackDynamicTypeInfo trackDynamicTypeInfo;
    private int trackIdUsed;
    private MapNode sourceMapNode;
    private Integer sourceRowId;
    private int nodeCount = 0;
    private int linkCount = 0;
    private MapNodeUtil mapNodeUtil;
    private int thePlaceId;
    private PlaceSettingsDTO thePlaceSettings;
    private TrackSettingsDTO theTrackSettings;
    private Set<LinkGeometry> linkGeometries;
    private boolean useRowId;
    private TrackMapSummaryGrid trackmapSummaryGrid;
    private Extent extent = null;

    public AbstractTrackDetailPointCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) throws CentrifugeException {
        super(mapCacheHandler, dataView, mapViewDef);
        this.mapTheme = mapTheme;
        mapSummaryExtents = mapCacheHandler.calculateMapSummaryExtents(uBox);
    }

    public static AbstractTrackDetailPointCacheLoader make(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) throws CentrifugeException {
        return new TrackDetailPointMapCacheLoader(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
    }

    static void registerTypenameToTrackId(int trackId, TrackidTracknameDuple key, Map<Integer, Set<String>> trackIdToTrackNames) {
        Set<String> typeNames;
        if (trackIdToTrackNames.containsKey(Integer.valueOf(trackId))) {
            typeNames = trackIdToTrackNames.get(Integer.valueOf(trackId));
        } else {
            typeNames = new TreeSet<String>();
            trackIdToTrackNames.put(Integer.valueOf(trackId), typeNames);
        }
        String typename = key.getTrackname();
        if (typename != null) {
            typeNames.add(typename);
        }
    }

    private ProcessRowStatus processRow(ResultSet rs, String identityString, Object sequence, TrackidTracknameDuple trackKey, Integer id, double latitude, double longitude, String typenameFromRS, String sizeFieldValue, String iconFieldValue, Map<String, String> descriptions, String label) throws SQLException {
        AugmentedMapNode mapNode = null;
        PlaceidTypenameDuple placeType = PlaceTypenameProcessor.process(mapSettings, thePlaceId, thePlaceSettings, mapNodeInfo, mapTheme, typeNameToPlaceStyle, typenameFromRS);
        if ((placeType != null) && (placeType.getTypename() != null)) {
            Geometry geometry = new Geometry(longitude, latitude);
            geometry.setSummaryLevel(Configuration.getInstance().getMapConfig().getDetailLevel());
            if (mapNodeInfo.getMapByGeometry().containsKey(geometry)) {
                mapNode = mapNodeInfo.getMapByGeometry().get(geometry);
            } else {
                nodeCount++;
                if (nodeCount > Configuration.getInstance().getMapConfig().getLinkLimit()) {
                    mapCacheHandler.setPointLimitReached(true);
                    clearCache();
                    extent = null;
                    return ProcessRowStatus.CLEAR;
                }
                mapNode = MapNodeUtil.createMapNode(mapCacheHandler.getVizUuid(), mapNodeInfo.getMapByGeometry(), mapNodeInfo.getMapById(), UUIDUtil.getUUIDLong(), geometry);
            }
            MapNodeUtil.setMapNodeTypeAndPlace(mapNode, mapNodeUtil.getInternalStateId(rs), thePlaceId, placeType, mapNodeInfo.getMapByType(), dataView);
            if (thePlaceSettings.getSizeColumn() == null) {
                mapNode.setSize(placeType, thePlaceSettings.getSize());
            }
            if (mapNode.isCombined()) {
                mapNodeInfo.getCombinedMapNodes().add(mapNode);
            }
            if (mapNode.isNew() && !mapNodeInfo.getNewMapNodes().contains(mapNode)) {
                mapNodeInfo.getNewMapNodes().add(mapNode);
            } else if (mapNode.isUpdated()) {
                mapNodeInfo.getUpdatedMapNodes().add(mapNode);
            }
            mapNode.getRowIds().add(id);
            mapNode.incrementHits();
            for (Map.Entry<String, String> entry : descriptions.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                mapNode.addDescriptions(key, value);
            }
            if (label != null) {
                mapNode.addLabel(label);
            }
            if (mapNodeUtil.shouldGatherStatistics(thePlaceId)) {
                String placeSizeFunction = thePlaceSettings.getSizeFunction();
                if (thePlaceSettings.isSizedByDynamicType().booleanValue()) {
                    mapNodeUtil.gatherMapNodeTypeStatistics(placeType, mapNode, placeSizeFunction, sizeFieldValue);
                } else {
                    mapNodeUtil.gatherMapNodePlaceStatistics(thePlaceId, mapNode, placeSizeFunction, sizeFieldValue);
                }
            }
            if (mapNodeInfo.getTypenameToIconUrl().get(placeType) != null) {
                mapNode.setIconUri(placeType, "id=" + mapNodeInfo.getTypenameToIconUrl().get(placeType));
            } else if (StringUtils.isNotBlank(iconFieldValue)) {
                mapNode.setIconUri(placeType, "id=" + iconFieldValue);
            }
        }
        if (mapNode == null) {
            return ProcessRowStatus.NULL;
        }
        if (isPopulateTrackRelatedInfo) {
            Geometry geometry = mapNode.getGeometry();
            Set<Integer> rowIds;
            if (trackGeometryToRowIds.containsKey(geometry)) {
                rowIds = trackGeometryToRowIds.get(geometry);
            } else {
                rowIds = new TreeSet<Integer>();
                trackGeometryToRowIds.put(geometry, rowIds);
            }
            rowIds.add(id);
            Set<Geometry> geometries;
            if (trackRowIdToGeometries.containsKey(id)) {
                geometries = trackRowIdToGeometries.get(id);
            } else {
                geometries = new TreeSet<Geometry>();
                trackRowIdToGeometries.put(id, geometries);
            }
            geometries.add(geometry);
        }
        if ((identityString != null) && (sequence != null)) {
            processTrack(id, trackKey, sequence, placeType, mapNode);
        }
        extent.addPoint(mapNode.getGeometry());
        return ProcessRowStatus.SUCCESS;
    }

    @Override
    public void load() {
        init();
        exec();
    }

    abstract void init();

    private void exec() {
        initMapCache();
        QueryExecutor.execute(LOG, generateQuery(), this::populateMapCache);
    }

    private void populateMapCache(ResultSet rs) throws SQLException {
        if (rs == null) {
            // TODO: probably should do something here
        } else {
            linkGeometries = new HashSet<LinkGeometry>();
            if (theTrackSettings != null) {
                mapCacheHandler.setPointLimitReached(false);
                Map<TrackidTracknameDuple, QueryRow> keyToFirstRow = new HashMap<TrackidTracknameDuple,QueryRow>();
                extent = new Extent();
                SpatialReference spatialReference = new SpatialReference();
                spatialReference.setWkid(4326);
                extent.setSpatialReference(spatialReference);
                while (rs.next() && !mapCacheHandler.isPointLimitReached()) {
                    String identityString;
                    Object sequence;
                    if (theTrackSettings.getIdentityColumn() == null) {
                        identityString = theTrackSettings.getIdentityName();
                    } else {
                        mapSettings.setTrackTypeFixed(false);
                        identityString = rs.getString(theTrackSettings.getIdentityColumn());
                    }
                    TrackidTracknameDuple trackKey = processTrackIdentityString(identityString);
                    String strId = rs.getString(CacheUtil.INTERNAL_ID_NAME);
                    Integer id = Integer.decode(strId);
                    double latitude = rs.getDouble(thePlaceSettings.getLatColumn());
                    if (rs.wasNull() || (latitude < -90) || (latitude > 90)) {
                        continue;
                    }
                    double longitude = rs.getDouble(thePlaceSettings.getLongColumn());
                    if (rs.wasNull() || (longitude < -180) || (longitude > 180)) {
                        continue;
                    }
                    String typenameFromRS = null;
                    String typeColumn = thePlaceSettings.getTypeColumn();
                    if (typeColumn != null) {
                        typenameFromRS = rs.getString(typeColumn);
                    }
                    String sizeFieldValue = null;
                    String sizeColumn = thePlaceSettings.getSizeColumn();
                    if (sizeColumn != null) {
                        sizeFieldValue = rs.getString(sizeColumn);
                    }
                    String iconFieldValue = null;
                    if (thePlaceSettings.getIconColumn() != null) {
                        iconFieldValue = rs.getString(thePlaceSettings.getIconColumn());
                    }
                    sequence = rs.getObject(theTrackSettings.getSequenceColumn());
                    Map<String, String> descriptions = new TreeMap<String,String>();
                    for (MapTooltipFieldDTO tooltipField : mapSettings.getTooltipFields().get(thePlaceId)) {
                        String fieldValue = rs.getString(tooltipField.getFieldColumn());
                        if (StringUtils.isNotBlank(fieldValue)) {
                            descriptions.put(tooltipField.getFieldName(), fieldValue);
                        }
                    }
                    String label = null;
                    if (thePlaceSettings.getLabelColumn() != null) {
                        String fieldValue = rs.getString(thePlaceSettings.getLabelColumn());
                        if (StringUtils.isNotBlank(fieldValue)) {
                            label = fieldValue;
                        }
                    }
                    if (useRowId) {
                        linkGeometries.addAll(trackmapSummaryGrid.getLinkGeometries(trackKey, sequence, id));
                    }

                    if (keyToFirstRow.containsKey(trackKey)) {
                        ProcessRowStatus status = processRow(rs, identityString, sequence, trackKey, id, latitude, longitude, typenameFromRS, sizeFieldValue, iconFieldValue, descriptions, label);
                        if (status == ProcessRowStatus.CLEAR) {
                            return;
                        }
                        QueryRow queryRow = keyToFirstRow.get(trackKey);
                        if (queryRow instanceof FirstQueryRow) {
                            FirstQueryRow fqr = (FirstQueryRow) queryRow;
                            status = processRow(rs, fqr.getIdentityString(), fqr.getSequence(), fqr.getTrackKey(), fqr.getId(), fqr.getLatitude(), fqr.getLongitude(), fqr.getTypenameFromRS(), fqr.getSizeFieldValue(), fqr.getIconFieldValue(), fqr.getDescriptions(), fqr.getLabel());
                            if (status == ProcessRowStatus.CLEAR) {
                                return;
                            }
                            keyToFirstRow.put(trackKey, new OKQueryRow());
                        }
                    } else {
                        FirstQueryRow firstQueryRow = new FirstQueryRow(identityString, sequence, trackKey, id, latitude, longitude, typenameFromRS, sizeFieldValue, iconFieldValue, descriptions, label);
                        keyToFirstRow.put(trackKey, firstQueryRow);
                    }
                }
                for (Map.Entry<TrackidTracknameDuple, Map<Object, Set<NodeIdRowIdPair>>> entry : tracks.entrySet()) {
                    sourceMapNode = null;
                    TrackidTracknameDuple key = entry.getKey();
                    Map<Object, Set<NodeIdRowIdPair>> track = entry.getValue();
                    for (Map.Entry<Object, Set<NodeIdRowIdPair>> entry2 : track.entrySet()) {
                        Set<NodeIdRowIdPair> valuesAtSameSequence = entry2.getValue();
                        addMapNodesToTrackType(valuesAtSameSequence, key);
                        if (addMapLinksToTrackType(valuesAtSameSequence, key)) {
                            return;
                        }
                    }
                }
                calculateMapNodeStatistics(mapNodeInfo.getMapByType(), mapNodeInfo.getPlaceIdToTypeNames());
                mapCacheHandler.sortMapNodeType();
                MapTrackSorter sorter = new MapTrackSorter(mapCacheHandler);
                sorter.sort();
                if (extent != null) {
                    Extent.expandExtentIfTooSmall(extent);
                    mapCacheHandler.setExtentIfMapNotPinned(extent);
                }
            }
        }
    }

    private void initMapCache() {
        isPopulateTrackRelatedInfo = true;
        trackGeometryToRowIds = new TreeMap<Geometry,Set<Integer>>();
        trackLinkGeometryToRowIds = new TreeMap<LinkGeometry,Set<Integer>>();
        trackRowIdToGeometries = new TreeMap<Integer,Set<Geometry>>();
        trackRowIdToLinkGeometries = new TreeMap<Integer,Set<LinkGeometry>>();
        trackTypeToGeometries = new HashMap<TrackidTracknameDuple,Set<Geometry>>();
        trackTypeToLinkGeometries = new HashMap<TrackidTracknameDuple,Set<LinkGeometry>>();
        mapCacheHandler.setTrackGeometryToRowIds(trackGeometryToRowIds);
        mapCacheHandler.setTrackLinkGeometryToRowIds(trackLinkGeometryToRowIds);
        mapCacheHandler.setTrackTypeToGeometries(trackTypeToGeometries);
        mapCacheHandler.setTrackTypeToLinkGeometries(trackTypeToLinkGeometries);
        MapConfig mapConfig = Configuration.getInstance().getMapConfig();
        maxPlaceSizeEqualsMinPlaceSize = mapConfig.getMaxPlaceSize() == mapConfig.getMinPlaceSize();
        mapNodeInfo = mapCacheHandler.getMapNodeInfo();
        mapNodeMapByGeom = mapNodeInfo.getMapByGeometry();
        mapNodeMapById = mapNodeInfo.getMapById();
        mapTrackInfo = mapCacheHandler.getMapTrackInfo();
        typeNameToPlaceStyle = new HashMap<String,PlaceStyle>();
        placeSettings = mapSettings.getPlaceSettings();
        trackSettings = mapSettings.getTrackSettings();
        trackIdUsed = 0;
        theTrackSettings = trackSettings.get(trackIdUsed);
        int howMany = placeSettings.size();

        for (int placeId = 0; placeId < howMany; placeId++) {
            String trackPlaceName = theTrackSettings.getPlace();
            thePlaceSettings = placeSettings.get(placeId);
            String placeName = thePlaceSettings.getName();
            if (trackPlaceName.equals(placeName)) {
                thePlaceId = placeId;
                break;
            }
        }
        tracks = new TreeMap<TrackidTracknameDuple, Map<Object,Set<NodeIdRowIdPair>>>();
        trackDynamicTypeInfo = mapCacheHandler.getTrackDynamicTypeInfo();
        mapNodeUtil = new MapNodeUtil(dataView, mapSettings, mapNodeInfo, mapTheme, typeNameToPlaceStyle);
    }

    private String generateQuery() {
        generateSelectedItemsString();
        trackmapSummaryGrid = mapCacheHandler.getTrackMapSummaryGrid();
        useRowId = true;
        Integer rangeStart = mapCacheHandler.getRangeStart();
        Integer rangeEnd = mapCacheHandler.getRangeEnd();
        List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = mapCacheHandler.getSeriesValues();

        if ((trackmapSummaryGrid == null) || (seriesValues == null)) {
            useRowId = false;
        }
        String fromClause;
        if (useRowId) {
            Set<Integer> rowIds = trackmapSummaryGrid.getRowIds(mapSummaryExtents, rangeStart, rangeEnd, seriesValues);

            if ((rowIds == null) || rowIds.isEmpty()) {
                fromClause = CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid());
                generateDefaultFilterString();
            } else {
                if (rowIds.size() < 100) {
                    fromClause = CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid());
                    filterString = rowIds.stream().map(i -> Integer.toString(i)).collect(Collectors.joining(", ", CacheUtil.INTERNAL_ID_NAME + " IN (", ")"));
                } else {
                    List<String> rowIdStrings = new ArrayList<String>();

                    rowIds.forEach(rowId -> rowIdStrings.add("(" + rowId + ")"));
                    fromClause =
                       rowIdStrings.stream()
                                   .collect(Collectors.joining(", ",
                                                               CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid()) + " JOIN (VALUES ",
                                                               ") ex(ex_" + CacheUtil.INTERNAL_ID_NAME + ") ON (" + CacheUtil.INTERNAL_ID_NAME + " = ex_" + CacheUtil.INTERNAL_ID_NAME + ")"));
                    filterString = "";
                }
            }
        } else {
            fromClause = CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid());
            generateDefaultFilterString();
        }
        String sql = String.format("SELECT %1$s FROM %2$s", selectedItemsString, fromClause);

        if (StringUtils.isNotEmpty(filterString)) {
            sql = sql + " WHERE " + filterString;
        }
        return sql;
    }

    private void generateDefaultFilterString() {
        filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettings);

        if (StringUtils.isBlank(filterString)) {
            filterString = "\"" + theTrackSettings.getSequenceColumn() + "\" IS NOT NULL";
        } else {
            filterString += "AND \"" + theTrackSettings.getSequenceColumn() + "\" IS NOT NULL";
        }
        String identityName = theTrackSettings.getIdentityName();
        if (identityName == null) {
            String typeColumn = theTrackSettings.getIdentityColumn();
            if (typeColumn != null) {
                filterString += " AND \"" + typeColumn + "\" IS NOT NULL";
            }
        }
        String latLongFilterString = getLatLongFilterString();
        if (latLongFilterString != null) {
            if (StringUtils.isBlank(filterString)) {
                filterString = latLongFilterString;
            } else {
                filterString += " AND " + latLongFilterString;
            }
        }
        String rangeFilterString = getRangeFilterString();
        if (rangeFilterString != null) {
            if (StringUtils.isBlank(filterString)) {
                filterString = rangeFilterString;
            } else {
                filterString += " AND " + rangeFilterString;
            }
        }
    }

    private void generateSelectedItemsString() {
        Set<String> selectedColumnNames = gatherSelectedColumns();
        formSelectedItemsString(selectedColumnNames);
    }

    private Set<String> gatherSelectedColumns() {
        Set<String> selectedColumnNames = gatherMinimumSelectedColumns();
        gatherSelectedColumnsForPlaceId(selectedColumnNames);
        return selectedColumnNames;
    }

    private Set<String> gatherMinimumSelectedColumns() {
        Set<String> selectedColumnNames = new TreeSet<String>();
        addToSetIfNotNull(selectedColumnNames, CacheUtil.INTERNAL_ID_NAME);
        addToSetIfNotNull(selectedColumnNames, CacheUtil.INTERNAL_STATEID);
        addToSetIfNotNull(selectedColumnNames, thePlaceSettings.getLatColumn());
        addToSetIfNotNull(selectedColumnNames, thePlaceSettings.getLongColumn());
        return selectedColumnNames;
    }

    private void gatherSelectedColumnsForPlaceId(Set<String> selectedColumnNames) {
        addToSetIfNotNull(selectedColumnNames, thePlaceSettings.getSizeColumn());
        addToSetIfNotNull(selectedColumnNames, thePlaceSettings.getTypeColumn());
        addToSetIfNotNull(selectedColumnNames, thePlaceSettings.getLabelColumn());
        addToSetIfNotNull(selectedColumnNames, thePlaceSettings.getIconColumn());
        for (MapTooltipFieldDTO tooltipField : mapSettings.getTooltipFields().get(thePlaceId)) {
            addToSetIfNotNull(selectedColumnNames, tooltipField.getFieldColumn());
        }
        if (theTrackSettings.getIdentityColumn() != null) {
            addToSetIfNotNull(selectedColumnNames, theTrackSettings.getIdentityColumn());
        }
        addToSetIfNotNull(selectedColumnNames, theTrackSettings.getSequenceColumn());
    }

   private String getLatLongFilterString() {
      List<String> latLongFilterStringForPlaces = new ArrayList<String>();
      String latLongFilterStringForPlaceId =
         MapNodeUtil.getLatLongFilterStringForPlaceId(placeSettings, thePlaceId, mapSummaryExtents);

      latLongFilterStringForPlaces.add(latLongFilterStringForPlaceId);
      return latLongFilterStringForPlaces.stream().collect(Collectors.joining(" OR ", "(", ")"));
   }

    private String getRangeFilterString() {
        String rangeFilterString = null;
        Integer rangeStart = mapCacheHandler.getRangeStart();
        Integer rangeEnd = mapCacheHandler.getRangeEnd();
        List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = mapCacheHandler.getSeriesValues();
        if ((rangeStart != null) && (rangeEnd != null) && (seriesValues != null) && !seriesValues.isEmpty() && !trackSettings.isEmpty()) {
            String columnString = theTrackSettings.getSequenceColumn();
            if (columnString != null) {
                columnString = "\"" + columnString + "\"";
                int seriesValuesSize = seriesValues.size();
                if (rangeStart >= seriesValuesSize) {
                    TrackMapSummaryGrid.SequenceSortValue endSequenceSortValue = seriesValues.get(seriesValuesSize - 1);
                    String rangeEndCriterion = endSequenceSortValue.getSequenceValue().toString();
                    String sequenceValueType = theTrackSettings.getSequenceValueType();
                    if (sequenceValueType.equals(CsiDataType.DateTime.toString())
                            || sequenceValueType.equals(CsiDataType.Date.toString())
                            || sequenceValueType.equals(CsiDataType.Time.toString())
                            || sequenceValueType.equals(CsiDataType.String.toString())) {
                        rangeEndCriterion = "'" + rangeEndCriterion + "'";
                    }
                    rangeFilterString = columnString + " = " + rangeEndCriterion;
                } else {
                    TrackMapSummaryGrid.SequenceSortValue startSequenceSortValue = seriesValues.get(rangeStart);
                    if (rangeEnd >= seriesValuesSize) {
                        rangeEnd = seriesValuesSize - 1;
                    }
                    TrackMapSummaryGrid.SequenceSortValue endSequenceSortValue = seriesValues.get(rangeEnd);
                    String rangeStartCriterion = startSequenceSortValue.getSequenceValue().toString();
                    String rangeEndCriterion = endSequenceSortValue.getSequenceValue().toString();
                    String sequenceValueType = theTrackSettings.getSequenceValueType();
                    if (sequenceValueType.equals(CsiDataType.DateTime.toString())
                            || sequenceValueType.equals(CsiDataType.Date.toString())
                            || sequenceValueType.equals(CsiDataType.Time.toString())
                            || sequenceValueType.equals(CsiDataType.String.toString())) {
                        rangeStartCriterion = "'" + rangeStartCriterion + "'";
                        rangeEndCriterion = "'" + rangeEndCriterion + "'";
                    }
                    if (theTrackSettings.getSequenceSortOrder().equals(SortOrder.ASC.toString())) {
                        rangeFilterString = rangeStartCriterion + " <= " + columnString + " AND " + columnString + " <= " + rangeEndCriterion;
                    } else {
                        rangeFilterString = rangeEndCriterion + " <= " + columnString + " AND " + columnString + " <= " + rangeStartCriterion;
                    }
                }
            }
        }
        return rangeFilterString;
    }

    private void processTrack(Integer id, TrackidTracknameDuple identity, Object sequence, PlaceidTypenameDuple placeType, AugmentedMapNode mapNode) {
        Map<Object, Set<NodeIdRowIdPair>> track;
        if (tracks.containsKey(identity)) {
            track = tracks.get(identity);
        } else {
            Comparator comparator;
            switch (theTrackSettings.getSequenceValueType()) {
                case "String":
                    comparator = StringUtil.getStringComparator();
                    break;
                case "Boolean":
                    comparator = (Comparator<Boolean>) (o1, o2) -> {
                        if (!(o1.booleanValue() ^ o2.booleanValue())) {
                            return 0;
                        } else if (!o1.booleanValue()) {
                            return -1;
                        } else {
                            return +1;
                        }
                    };
                    break;
                case "Number":
                    comparator = (Comparator<Double>) (arg0, arg1) -> arg0 < arg1 ? -1 : arg0 > arg1 ? +1 : 0;
                    break;
                case "Integer":
                case "Date":
                case "DateTime":
                    comparator = (Comparator<Long>) (arg0, arg1) -> arg0 < arg1 ? -1 : arg0 > arg1 ? +1 : 0;
                    break;
                case "Time":
                   comparator = new Comparator<Object>() {
                                   public int compare(Object arg0, Object arg1) {
                                      return ((arg0 instanceof Long) && (arg1 instanceof Long))
                                                ? Duration.ofMillis((Long) arg0).compareTo(Duration.ofMillis((Long) arg1))
                                                : 0;
                                   }
                   };
                   break;
                default:
                    comparator = null;
            }
            if ((comparator != null) && theTrackSettings.getSequenceSortOrder().equals(SortOrder.DESC.toString())) {
                comparator = comparator.reversed();
            }
            track = new TreeMap<Object,Set<NodeIdRowIdPair>>(comparator);
            tracks.put(identity, track);
        }
        Set<NodeIdRowIdPair> nodeIdRowIdPairs;
        if ("Date".equals(theTrackSettings.getSequenceValueType())) {
            if (sequence != null) {
                Object unknownType = sequence;
                try {
                    sequence = CsiTypeUtil.coerceDate(unknownType).getTime();
                } catch (Exception exception) {
                   LOG.debug("Can't handle object:" + unknownType.toString());
                    sequence = null;
                }
            }
        } else if ("DateTime".equals(theTrackSettings.getSequenceValueType())) {
            if (sequence != null) {
                Object unknownType = sequence;
                try {
                    Timestamp timestamp = CsiTypeUtil.coerceTimestamp(unknownType);
                    if (timestamp != null) {
                        sequence = timestamp.getTime();
                    }
                } catch (Exception exception) {
                   LOG.debug("Can't handle object:" + unknownType.toString());
                    sequence = null;
                }
            }
        }
        if (sequence != null) {
            if (track.containsKey(sequence)) {
                nodeIdRowIdPairs = track.get(sequence);
            } else {
                nodeIdRowIdPairs = new TreeSet<NodeIdRowIdPair>();
                track.put(sequence, nodeIdRowIdPairs);
            }
            if (mapNode != null) {
                nodeIdRowIdPairs.add(new NodeIdRowIdPair(mapNode.getNodeId(), id, placeType));
            }
        }
    }

    private TrackidTracknameDuple processTrackIdentityString(String identityString) {
        TrackidTracknameDuple key = new TrackidTracknameDuple(trackIdUsed, identityString);
        if (!mapTrackInfo.getTrackkeyToColor().containsKey(key)) {
            if (theTrackSettings.getIdentityColumn() != null) {
                processTrackDynamicType(key, getAssociationStyle(identityString));
            } else {
                processFixedType(key);
            }
            registerTypenameToTrackId(trackIdUsed, key, mapTrackInfo.getTrackidToTracknames());
        }
        return key;
    }

    private void processTrackDynamicType(TrackidTracknameDuple key, AssociationStyle associationStyle) {
        processTrackShape(key);
        processTrackColor(key, associationStyle);
        processTrackWidth(key, associationStyle);
    }

    private void processTrackShape(TrackidTracknameDuple key) {
        String lineStyle = theTrackSettings.getLineStyle();
        storeTrackShapeInfo(key, lineStyle);
    }

    private void storeTrackShapeInfo(TrackidTracknameDuple key, String shape) {
        if (!mapTrackInfo.getTrackkeyToShape().containsKey(key)) {
            mapTrackInfo.getTrackkeyToShape().put(key, shape);
            if (!trackDynamicTypeInfo.getTrackKeyToShape().containsKey(key)) {
                trackDynamicTypeInfo.getTrackKeyToShape().put(key, shape);
            }
        }
    }

    private void processTrackColor(TrackidTracknameDuple key, AssociationStyle associationStyle) {
        String colorString;
        if (theTrackSettings.isColorOverriden()) {
            colorString = theTrackSettings.getColorString();
        } else if (associationStyle != null) {
            colorString = ClientColorHelper.get().make(associationStyle.getColor()).toString();
        } else {
            colorString = getTrackColorFromDynamicTypeInfo(key);
        }
        storeTrackColorInfo(key, colorString.replace("#", ""));
    }

    private String getTrackColorFromDynamicTypeInfo(TrackidTracknameDuple key) {
        String colorString;
        if (trackDynamicTypeInfo.getTrackKeyToColor().containsKey(key)) {
            colorString = trackDynamicTypeInfo.getTrackKeyToColor().get(key);
        } else {
            colorString = ClientColorHelper.get().make(ColorWheel.next()).toString();
        }
        return colorString;
    }

    private void storeTrackColorInfo(TrackidTracknameDuple key, String color) {
        mapTrackInfo.getTrackkeyToColor().put(key, color);
        if (!trackDynamicTypeInfo.getTrackKeyToColor().containsKey(key)) {
            trackDynamicTypeInfo.getTrackKeyToColor().put(key, color);
        }
    }

    private void processTrackWidth(TrackidTracknameDuple key, AssociationStyle associationStyle) {
        int width;
        if (theTrackSettings.isWidthOverriden()) {
            width = theTrackSettings.getWidth();
        } else if (associationStyle != null) {
            width = associationStyle.getWidth().intValue();
        } else {
            width = getTrackWidthFromDynamicTypeInfo(key);
        }
        storeTrackWidthInfo(key, width);
    }

    private int getTrackWidthFromDynamicTypeInfo(TrackidTracknameDuple key) {
        int width;
        width = trackDynamicTypeInfo.getTrackKeyToWidth().getOrDefault(key, MapTrack.DEFAULT_TRACK_WIDTH);
        return width;
    }

    private void storeTrackWidthInfo(TrackidTracknameDuple key, int width) {
        mapTrackInfo.getTrackkeyToWidth().put(key, width);
        if (!trackDynamicTypeInfo.getTrackKeyToWidth().containsKey(key)) {
            trackDynamicTypeInfo.getTrackKeyToWidth().put(key, width);
        }
    }

    private AssociationStyle getAssociationStyle(String typename) {
        if (mapTheme != null) {
            return mapTheme.getAssociationStyleMap().get(typename);
        } else {
            return null;
        }
    }

    private void processFixedType(TrackidTracknameDuple key) {
        storeTrackShapeInfo(key, theTrackSettings.getLineStyle());
        storeTrackColorInfo(key, theTrackSettings.getColorString().replace("#", ""));
        storeTrackWidthInfo(key, theTrackSettings.getWidth());
    }

    private void addMapNodesToTrackType(Set<NodeIdRowIdPair> pairs, TrackidTracknameDuple key) {
        for (NodeIdRowIdPair pair : pairs) {
            Long nodeId = pair.getNodeId();
            AugmentedMapNode sourceMapNode = mapNodeMapById.get(nodeId);
            if (sourceMapNode != null) {
                if (!mapTrackInfo.getMapNodeByKey().containsKey(key)) {
                    mapTrackInfo.getMapNodeByKey().put(key, new HashSet<AugmentedMapNode>());
                }
                mapTrackInfo.getMapNodeByKey().get(key).add(sourceMapNode);
                if (isPopulateTrackRelatedInfo) {
                    Set<Geometry> geometries;
                    if (trackTypeToGeometries.containsKey(key)) {
                        geometries = trackTypeToGeometries.get(key);
                    } else {
                        geometries = new HashSet<Geometry>();
                        trackTypeToGeometries.put(key, geometries);
                    }
                    geometries.add(sourceMapNode.getGeometry());
                }
                sourceMapNode.addPlaceTypeForTrack(key, pair.getPlaceType());
            }
        }
    }

    private boolean addMapLinksToTrackType(Set<NodeIdRowIdPair> pairs, TrackidTracknameDuple key) {
        boolean pointLimitReached = false;
        MapLink mapLink;
        for (NodeIdRowIdPair pair : pairs) {
            Long nodeId = pair.getNodeId();
            if (sourceMapNode == null) {
                sourceMapNode = mapNodeMapById.get(nodeId);
                sourceRowId = pair.getRowId();
            } else {
                MapNode destinationMapNode = mapNodeMapById.get(nodeId);
                if (destinationMapNode != null) {
                    LinkGeometry linkGeometry = new LinkGeometry(key, sourceMapNode.getGeometry(), destinationMapNode.getGeometry());
                    if (useRowId) {
                        if (linkGeometries.contains(linkGeometry)) {
                            if (mapTrackInfo.getMapLinkByGeometry().containsKey(linkGeometry)) {
                                mapLink = retrieveMapLink(mapTrackInfo.getMapLinkByGeometry(), linkGeometry);
                            } else {
                                linkCount++;
                                if (linkCount > Configuration.getInstance().getMapConfig().getLinkLimit()) {
                                    pointLimitReached = true;
                                    mapCacheHandler.setPointLimitReached(true);
                                    clearCache();
                                    break;
                                }
                                mapLink = createMapLink(mapTrackInfo.getMapLinkById(), mapTrackInfo.getMapLinkByGeometry(), UUIDUtil.getUUIDLong(), sourceMapNode, destinationMapNode, linkGeometry);
                            }
                            updateMapLink(mapLink, key);
                            if (isPopulateTrackRelatedInfo) {
                                LinkGeometry linkgeometry = mapLink.getLinkGeometry();
                                Set<Integer> rowIds;
                                if (trackLinkGeometryToRowIds.containsKey(linkgeometry)) {
                                    rowIds = trackLinkGeometryToRowIds.get(linkgeometry);
                                } else {
                                    rowIds = new TreeSet<Integer>();
                                    trackLinkGeometryToRowIds.put(linkgeometry, rowIds);
                                }
                                rowIds.add(pair.rowId);
                                Set<LinkGeometry> linkGeometries2;
                                if (trackRowIdToLinkGeometries.containsKey(pair.rowId)) {
                                    linkGeometries2 = trackRowIdToLinkGeometries.get(pair.rowId);
                                } else {
                                    linkGeometries2 = new TreeSet<LinkGeometry>();
                                    trackRowIdToLinkGeometries.put(pair.rowId, linkGeometries2);
                                }
                                linkGeometries2.add(linkgeometry);
                                if (trackTypeToLinkGeometries.containsKey(key)) {
                                    linkGeometries2 = trackTypeToLinkGeometries.get(key);
                                } else {
                                    linkGeometries2 = new TreeSet<LinkGeometry>();
                                    trackTypeToLinkGeometries.put(key, linkGeometries2);
                                }
                                linkGeometries2.add(linkgeometry);
                            }
                            mapLink.getRowIds().add(sourceRowId);
                            sourceMapNode = destinationMapNode;
                            sourceRowId = pair.getRowId();
                            mapLink.getRowIds().add(sourceRowId);
                        } else {
                            sourceMapNode = destinationMapNode;
                            sourceRowId = pair.getRowId();
                        }
                    } else {
                        if (mapTrackInfo.getMapLinkByGeometry().containsKey(linkGeometry)) {
                            mapLink = retrieveMapLink(mapTrackInfo.getMapLinkByGeometry(), linkGeometry);
                        } else {
                            linkCount++;
                            if (linkCount > Configuration.getInstance().getMapConfig().getLinkLimit()) {
                                pointLimitReached = true;
                                mapCacheHandler.setPointLimitReached(true);
                                clearCache();
                                break;
                            }
                            mapLink = createMapLink(mapTrackInfo.getMapLinkById(), mapTrackInfo.getMapLinkByGeometry(), UUIDUtil.getUUIDLong(), sourceMapNode, destinationMapNode, linkGeometry);
                        }
                        updateMapLink(mapLink, key);
                        if (isPopulateTrackRelatedInfo) {
                            LinkGeometry linkgeometry = mapLink.getLinkGeometry();
                            Set<Integer> rowIds;
                            if (trackLinkGeometryToRowIds.containsKey(linkgeometry)) {
                                rowIds = trackLinkGeometryToRowIds.get(linkgeometry);
                            } else {
                                rowIds = new TreeSet<Integer>();
                                trackLinkGeometryToRowIds.put(linkgeometry, rowIds);
                            }
                            rowIds.add(pair.rowId);
                            Set<LinkGeometry> linkGeometries2;
                            if (trackRowIdToLinkGeometries.containsKey(pair.rowId)) {
                                linkGeometries2 = trackRowIdToLinkGeometries.get(pair.rowId);
                            } else {
                                linkGeometries2 = new TreeSet<LinkGeometry>();
                                trackRowIdToLinkGeometries.put(pair.rowId, linkGeometries2);
                            }
                            linkGeometries2.add(linkgeometry);
                            if (trackTypeToLinkGeometries.containsKey(key)) {
                                linkGeometries2 = trackTypeToLinkGeometries.get(key);
                            } else {
                                linkGeometries2 = new TreeSet<LinkGeometry>();
                                trackTypeToLinkGeometries.put(key, linkGeometries2);
                            }
                            linkGeometries2.add(linkgeometry);
                        }
                        mapLink.getRowIds().add(sourceRowId);
                        sourceMapNode = destinationMapNode;
                        sourceRowId = pair.getRowId();
                        mapLink.getRowIds().add(sourceRowId);
                    }
                }
            }
        }
        return pointLimitReached;
    }

    private MapLink retrieveMapLink(Map<LinkGeometry, MapLink> mapLinkMapByGeom, LinkGeometry linkGeometry) {
        MapLink mapLink = mapLinkMapByGeom.get(linkGeometry);
        mapLink.incrementHits();
        return mapLink;
    }

    private void clearCache() {
        mapNodeMapByGeom.clear();
        mapNodeMapById.clear();
        clearLinks();
    }

    private void clearLinks() {
        mapTrackInfo.getMapNodeByKey().clear();
        mapTrackInfo.getMapLinkById().clear();
        mapTrackInfo.getMapLinkByKey().clear();
        mapTrackInfo.getMapLinkByGeometry().clear();
    }

    private MapLink createMapLink(Map<Long, MapLink> mapLinkMapById, Map<LinkGeometry, MapLink> mapLinkMapByGeom, Long linkId, MapNode sourceMapNode, MapNode destinationMapNode, LinkGeometry linkGeometry) {
        MapLink mapLink = new MapLink(linkId);
        mapLink.setSourceNode(sourceMapNode);
        mapLink.setDestinationNode(destinationMapNode);
        mapLinkMapByGeom.put(linkGeometry, mapLink);
        mapLinkMapById.put(mapLink.getLinkId(), mapLink);
        return mapLink;
    }

    private void updateMapLink(MapLink mapLink, TrackidTracknameDuple type) {
        if (!mapTrackInfo.getMapLinkByKey().containsKey(type)) {
            mapTrackInfo.getMapLinkByKey().put(type, new ArrayList<>());
        }
        mapTrackInfo.getMapLinkByKey().get(type).add(mapLink);
        mapLink.setTracktype(type);
        mapLink.incrementHits();
    }

    private void calculateMapNodeStatistics(Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> placeIdToTypeNames) {
        if (!maxPlaceSizeEqualsMinPlaceSize) {
            MapNodeStatisticsCalculator calculator = new MapNodeStatisticsCalculator(mapCacheHandler, mapNodeByType, placeIdToTypeNames, true);
            calculator.calculate();
        }
    }

    enum ProcessRowStatus {
        CLEAR, NULL, SUCCESS
    }

    private interface QueryRow {
    }

    static class NodeIdRowIdPair implements Comparable<NodeIdRowIdPair> {
        private Long nodeId;
        private Integer rowId;
        private PlaceidTypenameDuple placeType;

        NodeIdRowIdPair(Long nodeId, Integer rowId, PlaceidTypenameDuple placeType) {
            this.nodeId = nodeId;
            this.rowId = rowId;
            this.placeType = placeType;
        }

        public Long getNodeId() {
            return nodeId;
        }

        public void setNodeId(Long nodeId) {
            this.nodeId = nodeId;
        }

        public Integer getRowId() {
            return rowId;
        }

        public void setRowId(Integer rowId) {
            this.rowId = rowId;
        }

        PlaceidTypenameDuple getPlaceType() {
            return placeType;
        }

        @Override
      public int hashCode() {
            return getRowId().hashCode();
        }

        @Override
      public boolean equals(Object other) {
            if (!(other instanceof NodeIdRowIdPair)) {
                return false;
            } else {
                NodeIdRowIdPair other2 = (NodeIdRowIdPair) other;
                return getRowId().equals(other2.getRowId());
            }
        }

        @Override
      public int compareTo(NodeIdRowIdPair o) {
            return getRowId() - o.getRowId();
        }
    }

    private static class OKQueryRow implements QueryRow {
    }

    private static class FirstQueryRow implements QueryRow {
        private String identityString;
        private Object sequence;
        private TrackidTracknameDuple trackKey;
        private Integer id;
        private double latitude;
        private double longitude;
        private String typenameFromRS;
        private String sizeFieldValue;
        private String iconFieldValue;
        private Map<String, String> descriptions;
        private String label;

        FirstQueryRow(String identityString, Object sequence, TrackidTracknameDuple trackKey, Integer id, double latitude, double longitude, String typenameFromRS, String sizeFieldValue, String iconFieldValue, Map<String, String> descriptions, String label) {
            this.identityString = identityString;
            this.sequence = sequence;
            this.trackKey = trackKey;
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
            this.typenameFromRS = typenameFromRS;
            this.sizeFieldValue = sizeFieldValue;
            this.iconFieldValue = iconFieldValue;
            this.descriptions = descriptions;
            this.label = label;
        }

        String getIdentityString() {
            return identityString;
        }

        public Object getSequence() {
            return sequence;
        }

        TrackidTracknameDuple getTrackKey() {
            return trackKey;
        }

        public Integer getId() {
            return id;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        String getTypenameFromRS() {
            return typenameFromRS;
        }

        String getSizeFieldValue() {
            return sizeFieldValue;
        }

        String getIconFieldValue() {
            return iconFieldValue;
        }

        Map<String, String> getDescriptions() {
            return descriptions;
        }

        public String getLabel() {
            return label;
        }
    }
}
