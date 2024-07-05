package csi.server.business.visualization.map.cacheloader.trackmap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.MapConfig;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.visualization.map.AbstractBoundaryChecker;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.business.visualization.map.TrackmapNodeInfo;
import csi.server.business.visualization.map.UUIDUtil;
import csi.server.business.visualization.map.cacheloader.AbstractMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.MapNodeUtil;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.MapSummaryColumnStringBuilder;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary.AbstractMapSummary;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary.IdentitySummary;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.CountTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.TypeSizeValue;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.SpatialReference;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;
import csi.shared.core.visualization.map.UBox;

public class TrackSummaryPointCacheLoader extends AbstractMapCacheLoader {
   private static final Logger LOG = LogManager.getLogger(TrackSummaryPointCacheLoader.class);

   private static final String LAT = "lat";
    private static final String LON = "long";
    private static final String INTERNAL_STATE_ID = "internal_state_id";
    private static final String TRACK_ID = "track_id";
    private static final String PLACE_ID = "place_id";
    private static final String TRACK_TYPE = "track_type";
    private static final String PLACE_TYPE = "place_type";
    protected int precision;
    private MapTheme mapTheme;
    private List<MapSummaryExtent> mapSummaryExtents;
    private boolean maxPlaceSizeEqualsMinPlaceSize;
    private TrackmapNodeInfo trackmapNodeInfo;
    private int summaryLevel;
    private Map<Integer, Set<String>> trackIdToTypeNameMap;
    private TrackSummaryTypenameProcessor trackSummaryTypenameProcessor;
    private TrackMapSummaryGrid trackmapSummaryGrid;
    private AbstractBoundaryChecker boundaryChecker;

    public TrackSummaryPointCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) throws CentrifugeException {
        super(mapCacheHandler, dataView, mapViewDef);
        this.mapTheme = mapTheme;
        MapSummaryExtent mapSummaryExtent;
        if (uBox == null) {
            mapSummaryExtent = null;
        } else {
            mapSummaryExtent = uBox.getMapSummaryExtent();
        }
        boundaryChecker = AbstractBoundaryChecker.get(mapSummaryExtent);
        mapSummaryExtents = mapCacheHandler.calculateMapSummaryExtents(mapSummaryExtent);
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    private Extent extent = null;

    @Override
    public void load() {
        init();
//		for (int trackId = 0; trackId < mapSettingsDTO.getTrackSettings().size(); trackId++) {
        int trackId = 0;
        TrackSettingsDTO trackSettingsDTO = mapSettings.getTrackSettings().get(trackId);
        int placeId = 0;
        while (placeId < mapSettings.getPlaceSettings().size()) {
            PlaceSettingsDTO placeSettingsDTO = mapSettings.getPlaceSettings().get(placeId);
            if (trackSettingsDTO.getPlace().equals(placeSettingsDTO.getName())) {
               break;
            }
            placeId++;
        }
        String query = generateQueryForTrack(trackId, placeId, trackSettingsDTO);
        QueryExecutor.execute(LOG, query, this::populateSummaryCache);
//		}
    }

    private void init() {
        MapConfig mapConfig = Configuration.getInstance().getMapConfig();
        maxPlaceSizeEqualsMinPlaceSize = mapConfig.getMaxPlaceSize() == mapConfig.getMinPlaceSize();

        trackmapNodeInfo = new TrackmapNodeInfo();
        mapCacheHandler.setTrackMapNodeInfo(trackmapNodeInfo);
        summaryLevel = mapCacheHandler.getCurrentMapSummaryPrecision();

//        mapTrackNameNodeId = Maps.newHashMap();
        trackIdToTypeNameMap = new HashMap<Integer,Set<String>>();

        trackSummaryTypenameProcessor = new TrackSummaryTypenameProcessor(trackmapNodeInfo, mapTheme, mapSettings);
        trackmapSummaryGrid = mapCacheHandler.getTrackMapSummaryGrid();
    }

    private String generateQueryForTrack(int trackId, int placeId, TrackSettingsDTO trackSettingsDTO) {
        MapSummaryColumnStringBuilder builder = new MapSummaryColumnStringBuilder(mapSettings, placeId, precision);
        boolean useRowId = true;
        Integer rangeStart = mapCacheHandler.getRangeStart();
        Integer rangeEnd = mapCacheHandler.getRangeEnd();
        List<TrackMapSummaryGrid.SequenceSortValue> seriesValues = mapCacheHandler.getSeriesValues();
        if ((trackmapSummaryGrid == null) || (rangeStart == null) || (rangeEnd == null) || (seriesValues == null)) {
         useRowId = false;
      }

        if (!useRowId) {
            return generateSelectClause(trackId, placeId, builder) + " " + generateFromClause() + " "
                    + generateWhereClause(placeId, trackSettingsDTO);
        } else {
            BroadcastResult broadcastResult = AbstractBroadcastStorageService.instance().getBroadcast(mapCacheHandler.getVizUuid());
            List<Integer> rowIdFromFilter = broadcastResult.getBroadcastFilter().getSelectedItems();
            if (broadcastResult.isExcludeRows()) {
                rowIdFromFilter = DataCacheHelper.inverseRows(dataView, rowIdFromFilter);
            }
            Set<Integer> rowIdsFromSummaryGrid = trackmapSummaryGrid.getRowIds(mapSummaryExtents, rangeStart, rangeEnd, seriesValues);
            if (((rowIdFromFilter == null) || rowIdFromFilter.isEmpty()) && ((rowIdsFromSummaryGrid == null) || rowIdsFromSummaryGrid.isEmpty())) {
               return generateSelectClause(trackId, placeId, builder) + " " + generateFromClause();
            } else {
                Set<Integer> rowIds = new HashSet<Integer>();
                if ((rowIdFromFilter == null) || rowIdFromFilter.isEmpty()) {
                    rowIds = rowIdsFromSummaryGrid;
                } else if ((rowIdsFromSummaryGrid == null) || rowIdsFromSummaryGrid.isEmpty()) {
                    rowIds.addAll(rowIdFromFilter);
                } else {
                    rowIds.addAll(rowIdFromFilter);
                    rowIds.retainAll(rowIdsFromSummaryGrid);
                }
                if (rowIds.isEmpty()) {
                   return generateSelectClause(trackId, placeId, builder) + " " + generateFromClause();
                } else {
                   StringBuilder sb = new StringBuilder(generateSelectClause(trackId, placeId, builder))
                                                .append(" ");

                   if (rowIds.size() < 100) {
                       return sb.append(generateFromClause())
                              .append(" WHERE ").append(CacheUtil.INTERNAL_ID_NAME)
                              .append(" IN (").append(rowIds.stream().map(i -> i.toString()).collect(Collectors.joining(", ")))
                              .append(")").toString();
                    } else {
                        return sb.append(generateSelectClause(trackId, placeId, builder))
                                 .append(" ").append(generateValueFromClause(rowIds)).toString();
                    }
                }
            }
        }
    }

    private String generateSelectClause(int trackId, int placeId, MapSummaryColumnStringBuilder builder) {
        List<String> columns = new ArrayList<String>();

        columns.add(builder.getLatColumnString() + " AS " + LAT);
        columns.add(builder.getLongColumnString() + " AS " + LON);
        columns.add(CacheUtil.INTERNAL_STATEID + " AS " + INTERNAL_STATE_ID);
        columns.add(trackId + " AS " + TRACK_ID);
        columns.add(placeId + " AS " + PLACE_ID);
        columns.add(getTrackType(mapSettings, trackId) + " AS " + TRACK_TYPE);
        columns.add(getPlaceType(mapSettings, placeId) + " AS " + PLACE_TYPE);
        return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
    }

    private String getTrackType(MapSettingsDTO mapSettings, int trackId) {
        String trackTypeColumn = mapSettings.getTrackSettings().get(trackId).getIdentityColumn();
        if (trackTypeColumn == null) {
         return "'' || ''";
      } else {
         return "\"" + trackTypeColumn + "\" || ''";
      }
    }

    private String getPlaceType(MapSettingsDTO mapSettings, int placeId) {
        String placeTypeColumn = mapSettings.getPlaceSettings().get(placeId).getTypeColumn();
        if (placeTypeColumn == null) {
         return "'' || ''";
      } else {
         return "\"" + placeTypeColumn + "\" || ''";
      }
    }

    private String generateFromClause() {
        return "FROM " + CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid());
    }

    private String generateWhereClause(int placeId, TrackSettingsDTO trackSettingsDTO) {
        String trackFilterString = "\"" + trackSettingsDTO.getSequenceColumn() + "\" IS NOT NULL";
        String identityName = trackSettingsDTO.getIdentityName();
        if (identityName == null) {
            String typeColumn = trackSettingsDTO.getIdentityColumn();
            if (typeColumn != null) {
                trackFilterString += " AND \"" + typeColumn + "\" IS NOT NULL";
            }
        }
        String retVal = "WHERE " + trackFilterString;
        String generatedFilterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettings, placeId);
        if (!generatedFilterString.isEmpty()) {
         retVal += " AND " + generatedFilterString;
      }
        return retVal;
    }

   private String generateValueFromClause(Set<Integer> rowIds) {
      List<String> rowIdStrs = new ArrayList<String>();

      rowIds.forEach(rowId -> rowIdStrs.add("(" + rowId + ")"));
      return new StringBuilder("FROM ").append(CacheUtil.getQuotedCacheTableName(dataView.getUuid()))
                       .append(" JOIN (VALUES ").append(rowIdStrs.stream().collect(Collectors.joining(", ")))
                       .append(") ex(ex_").append(CacheUtil.INTERNAL_ID_NAME)
                       .append(") ON (").append(CacheUtil.INTERNAL_ID_NAME)
                       .append(" = ex_").append(CacheUtil.INTERNAL_ID_NAME).append(")")
                       .toString();
   }

    private void populateSummaryCache(ResultSet rs) throws SQLException {
        if (rs == null) {
            // TODO: probably should do something here
        } else {
//          for (int placeId = mapSettingsDTO.getPlaceSettings().size() - 1; placeId >= 0; placeId--)
            int trackId;
//            mapTrackNameNodeId.put(mapSettings.getTrackSettings().get(trackId).getName(), trackId);
            Map<TrackidTracknameDuple, QueryRow> keyToFirstRow = new HashMap<TrackidTracknameDuple,QueryRow>();
            extent = new Extent();
            SpatialReference spatialReference = new SpatialReference();
            spatialReference.setWkid(4326);
            extent.setSpatialReference(spatialReference);
            while (rs.next()) {
                mapCacheHandler.iterate();
                double latitude = rs.getDouble(LAT);
                if (rs.wasNull() || (latitude < -90) || (latitude > 90)) {
                  continue;
               }
                double longitude = rs.getDouble(LON);
                if (rs.wasNull() || (longitude < -180) || (longitude > 180)) {
                  continue;
               }

                if (!boundaryChecker.isPointInBoundary(latitude, longitude)) {
                    continue;
                }
                trackId = rs.getInt(TRACK_ID);
                Integer internalStateId = getInternalStateId(rs);

                TrackSettingsDTO trackSettings = mapSettings.getTrackSettings().get(trackId);
                String trackTypename = trackSettings.getIdentityName();
                if (trackTypename == null) {
                    trackTypename = getTrackTypenameFromRow(rs);
                }
                TrackidTracknameDuple trackKey = trackSummaryTypenameProcessor.process(trackSettings, trackId, trackTypename);
                if (keyToFirstRow.containsKey(trackKey)) {
                    processRow(trackId, latitude, longitude, internalStateId, trackKey);
                    QueryRow queryRow = keyToFirstRow.get(trackKey);
                    if (queryRow instanceof FirstQueryRow) {
                        FirstQueryRow fqr = (FirstQueryRow) queryRow;
                        processRow(fqr.getTrackId(), fqr.getLatitude(), fqr.getLongitude(), fqr.getInternalStateId(), trackKey);
                        keyToFirstRow.put(trackKey, new OKQueryRow());
                    }
                } else {
                    FirstQueryRow firstQueryRow = new FirstQueryRow(trackId, latitude, longitude, internalStateId);
                    keyToFirstRow.put(trackKey, firstQueryRow);
                }
            }

            if (mapCacheHandler.getItemsInViz() == 0) {
                trackmapNodeInfo.getMapNodeById().clear();
                trackmapNodeInfo.getMapNodeByKey().clear();
                trackmapNodeInfo.getCombinedMapNodes().clear();
                extent = null;
            } else {
                calculateMapNodeStatistics(trackmapNodeInfo.getMapNodeByKey(), trackmapNodeInfo.getTrackidToTracknames());
                mapCacheHandler.sortCurrentMapTrackSummaryMapNodeType();
                if (extent != null) {
                    Extent.expandExtentIfTooSmall(extent);
                    mapCacheHandler.setExtentIfMapNotPinned(extent);
                }
            }
        }
    }

    private void processRow(int trackId, double latitude, double longitude, Integer internalStateId, TrackidTracknameDuple key) {
        if ((key != null) && (key.getTrackname() != null)) {
            if (!trackIdToTypeNameMap.containsKey(trackId)) {
               trackIdToTypeNameMap.put(trackId, new HashSet<String>());
            }
            trackIdToTypeNameMap.get(trackId).add(key.getTrackname());

            Geometry geometry = new Geometry(longitude, latitude);
            geometry.setSummaryLevel(summaryLevel);
            AugmentedMapNode mapNode;
            if (trackmapNodeInfo.getMapNodeByGeometry().containsKey(geometry)) {
                mapNode = trackmapNodeInfo.getMapNodeByGeometry().get(geometry);
            } else {
                mapNode = MapNodeUtil.createMapNode(mapCacheHandler.getVizUuid(), trackmapNodeInfo.getMapNodeByGeometry(), trackmapNodeInfo.getMapNodeById(), UUIDUtil.getUUIDLong(), geometry);
            }
            updateMapNode(mapNode, internalStateId, key, trackmapNodeInfo);
            extent.addPoint(mapNode.getGeometry());
        }
    }

    private Integer getInternalStateId(ResultSet rs) throws SQLException {
        String strId = rs.getString(INTERNAL_STATE_ID);
        return Integer.parseInt(strId);
    }

    private String getTrackTypenameFromRow(ResultSet rs) throws SQLException {
        String trackTypename;
        mapSettings.setTrackTypeFixed(false);
        trackTypename = rs.getString(TrackSummaryPointCacheLoader.TRACK_TYPE);
        if ((trackTypename == null) || (trackTypename.trim().length() == 0)) {
            return null;
        } else {
            trackTypename = trackTypename.trim();
        }
        return trackTypename;
    }

    private void updateMapNode(AugmentedMapNode mapNode, Integer internalStateId, TrackidTracknameDuple key, TrackmapNodeInfo mapNodeInfo) throws NumberFormatException {
//        updateMapNode(mapNode, internalStateId, trackId, key, mapNodeInfo.getMapNodeByKey(), mapNodeInfo.getCombinedMapNodes(), mapNodeInfo.getNewMapNodes(),
//                mapNodeInfo.getUpdatedMapNodes());
        updateMapNode(mapNode, internalStateId, key, mapNodeInfo.getMapNodeByKey(), mapNodeInfo.getCombinedMapNodes(), mapNodeInfo.getNewMapNodes(), mapNodeInfo.getUpdatedMapNodes());
    }

    private void updateMapNode(AugmentedMapNode mapNode, Integer internalStateId, TrackidTracknameDuple key, Map<TrackidTracknameDuple, Set<AugmentedMapNode>> mapNodeByType, Set<AugmentedMapNode> combinedMapNodes, Set<AugmentedMapNode> newMapNodes, Set<AugmentedMapNode> updatedMapNodes)
            throws NumberFormatException {
        //setMapNodeIdentityAndTrack(mapNode, mapNodeByType, combinedMapNodes, internalStateId, trackId, key);
        setMapNodeIdentityAndTrack(mapNode, mapNodeByType, combinedMapNodes, internalStateId, key);
        if (mapNode.isNew() && !newMapNodes.contains(mapNode)) {
            newMapNodes.add(mapNode);
        } else if (mapNode.isUpdated()) {
            updatedMapNodes.add(mapNode);
        }
        mapNode.incrementHits();
        gatherMapNodeStatistics(mapNode, key);
    }

    private void setMapNodeIdentityAndTrack(AugmentedMapNode mapNode, Map<TrackidTracknameDuple, Set<AugmentedMapNode>> mapNodeByType, Set<AugmentedMapNode> combinedMapNodes, Integer internalStateId, TrackidTracknameDuple key) {
        if (!mapNodeByType.containsKey(key)) {
         mapNodeByType.put(key, new HashSet<AugmentedMapNode>());
      }
        mapNodeByType.get(key).add(mapNode);
        mapNode.addInternalStateId(internalStateId, dataView.getNextLinkupId() - 1);
        mapNode.addIdentityName(key);
//        mapNode.addTrackId(trackId);
        if (mapNode.isCombined()) {
            combinedMapNodes.add(mapNode);
        }
    }

    private void gatherMapNodeStatistics(AugmentedMapNode mapNode, TrackidTracknameDuple key) throws NumberFormatException {
        if (!maxPlaceSizeEqualsMinPlaceSize) {
         gatherMapNodeTypeStatistics(mapNode, key);
      }
    }

    private void gatherMapNodeTypeStatistics(AugmentedMapNode mapNode, TrackidTracknameDuple key) {
        TypeSizeValue typeSizeValue = mapNode.getIdentitySizeValue(key);
        if (typeSizeValue == null) {
            typeSizeValue = new CountTypeSizeValue();
            mapNode.setIdentitySizeValue(key, typeSizeValue);
        }
        ((CountTypeSizeValue) typeSizeValue).incrementCount();
    }

    private void calculateMapNodeStatistics(Map<TrackidTracknameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> trackIdToTypeNames) {
        if (!maxPlaceSizeEqualsMinPlaceSize) {
//            for (int placeId = mapSettingsDTO.getPlaceSettings().size() - 1; placeId >= 0; placeId--)
            int trackId = 0;
            calculateMapNodeTypeStatistics(mapNodeByType, trackIdToTypeNames, trackId);
        }
    }

    private void calculateMapNodeTypeStatistics(Map<TrackidTracknameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> trackIdToTypeNames, int trackId) {
        if (trackIdToTypeNames.isEmpty() || !trackIdToTypeNames.containsKey(trackId)) {
            return;
        }
        for (String typename : trackIdToTypeNames.get(trackId)) {
            calculateMapNodeTypeStatisticsByTypename(mapNodeByType, trackId, typename);
        }
    }

    private void calculateMapNodeTypeStatisticsByTypename(Map<TrackidTracknameDuple, Set<AugmentedMapNode>> mapNodeByType, int trackId, String typename) {
        TrackidTracknameDuple key = new TrackidTracknameDuple(trackId, typename);
        Set<AugmentedMapNode> mapNodes = mapNodeByType.get(key);
        if (mapNodes != null) {
         generateTypeSummary(key, mapNodes);
      }
    }

    private void generateTypeSummary(TrackidTracknameDuple key, Set<AugmentedMapNode> mapNodes) {
        AbstractMapSummary identitySummary = new IdentitySummary(key);
        for (AugmentedMapNode mapNode : mapNodes) {
            TypeSizeValue typeSizeValue = mapNode.getIdentitySizeValue(key);
            Double count = null;
            if (typeSizeValue != null) {
               count = typeSizeValue.getValue();
            }
            updateMapSummary(identitySummary, mapNode, count);
        }
        if (identitySummary.hasMapNodes()) {
         identitySummary.setMapNodeSizes();
      }
    }

    private void updateMapSummary(AbstractMapSummary mapSummary, AugmentedMapNode mapNode, Double count) {
        if (count != null) {
            mapSummary.addMapNode(mapNode, count);
            mapSummary.updateMinMax(count);
        }
    }

    private interface QueryRow {
    }

    private static class OKQueryRow implements QueryRow {
    }

    private static class FirstQueryRow implements QueryRow {
        private final int trackId;
        private final double latitude;
        private final double longitude;
        private final Integer internalStateId;

        FirstQueryRow(int trackId, double latitude, double longitude, Integer internalStateId) {
            this.trackId = trackId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.internalStateId = internalStateId;
        }

        public int getTrackId() {
            return trackId;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public Integer getInternalStateId() {
            return internalStateId;
        }
    }
}