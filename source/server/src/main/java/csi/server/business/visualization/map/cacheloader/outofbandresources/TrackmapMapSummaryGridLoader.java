package csi.server.business.visualization.map.cacheloader.outofbandresources;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapConstants;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;

public class TrackmapMapSummaryGridLoader {
   private static final Logger LOG = LogManager.getLogger(TrackmapMapSummaryGridLoader.class);

   private static final String PLACE_ID = "placeId";
   private static final String TRACK_ID = "trackId";
   private static final String LAT = "lat";
   private static final String LON = "long";
   private static final String ROW_ID = "rowId";
   private static final String INTERNAL_STATE_ID = "internalStateId";
   private static final String TYPENAME = "typename";
   private static final String TRACK_NAME = "trackName";
   private static final String SEQUENCE = "zequence";
   private final MapSettingsDTO mapSettings;
   private MapCacheHandler mapCacheHandler;
   private DataView dataView;
   private MapViewDef mapViewDef;
   private TrackMapSummaryGrid mapSummaryGrid;

   public TrackmapMapSummaryGridLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef) {
      this.mapCacheHandler = mapCacheHandler;
      this.dataView = dataView;
      this.mapViewDef = mapViewDef;
      mapSettings = mapCacheHandler.getMapSettings();
   }

   public void setMapSummaryGrid(MapSummaryGrid mapSummaryGrid) {
      this.mapSummaryGrid = (TrackMapSummaryGrid) mapSummaryGrid;
   }

   public void load() {
      populateMapSummaryGrid();
   }

   private void populateMapSummaryGrid() {
      QueryExecutor.execute(LOG, generateQuery(), (ResultSet rs) -> {
         Map<TrackidTracknameDuple,QueryRow> keyToFirstRow = new TreeMap<TrackidTracknameDuple,QueryRow>();
         int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();

         while (rs.next()) {
            Object sequenceObject = rs.getObject(SEQUENCE);
            if (sequenceObject == null) {
               continue;
            }
            int rowId = getRowId(rs);
            // FIXME:unused
            int internalStateId = getInternalStateId(rs);
            double latitude = rs.getDouble(LAT);
            double longitude = rs.getDouble(LON);
            Geometry geometry = new Geometry(longitude, latitude);
            geometry.setSummaryLevel(summaryLevel);
            Integer placeId = rs.getInt(PLACE_ID);
            int trackId = rs.getInt(TRACK_ID);
            String typeName = getType(rs);
            typeName = typeName.trim();
            String trackName = getTrack(rs);
            trackName = trackName.trim();
            boolean processRow = true;

            if (typeName.equals(MapConstants.NULL_TYPE_NAME)) {
               processRow = mapSettings.getPlaceSettings().get(placeId).isIncludeNullType();
            }
            if (trackName == null) {
               processRow = false;
            }
            if (processRow) {
               TrackidTracknameDuple trackKey = new TrackidTracknameDuple(trackId, trackName);

               if (keyToFirstRow.containsKey(trackKey)) {
                  processRow(sequenceObject, rowId, internalStateId, geometry, placeId, typeName, trackKey);
                  QueryRow queryRow = keyToFirstRow.get(trackKey);

                  if (queryRow instanceof FirstQueryRow) {
                     FirstQueryRow fqr = (FirstQueryRow) queryRow;
                     processRow(fqr.getSequenceObject(), fqr.getRowId(), fqr.getInternalStateId(), fqr.getGeometry(),
                                fqr.getPlaceId(), fqr.getTypeName(), trackKey);
                     keyToFirstRow.put(trackKey, new OKQueryRow());
                  }
               } else {
                  FirstQueryRow firstQueryRow = new FirstQueryRow(sequenceObject, rowId, internalStateId, geometry,
                                                                  placeId, typeName);
                  keyToFirstRow.put(trackKey, firstQueryRow);
               }
            }
         }
      });
   }

   private void processRow(Object sequenceObject, int rowId, int internalStateId, Geometry geometry, Integer placeId,
                           String typeName, TrackidTracknameDuple trackKey) {
      PlaceidTypenameDuple placeKey = new PlaceidTypenameDuple(placeId, typeName);
      mapSummaryGrid.addNode(geometry, rowId, internalStateId, placeKey);
      mapSummaryGrid.registerNode(geometry, trackKey, sequenceObject, rowId);
   }

   private String generateQuery() {
      String selectClause = generateSelectClause();
      String fromClause = generateFromClause();
      return selectClause + " " + fromClause;
   }

   private String generateSelectClause() {
      List<String> columns = new ArrayList<String>();

      columns.add(PLACE_ID);
      columns.add(TRACK_ID);
      columns.add(LAT);
      columns.add(LON);
      columns.add(ROW_ID);
      columns.add(INTERNAL_STATE_ID);
      columns.add(TYPENAME);
      columns.add(TRACK_NAME);
      columns.add(SEQUENCE);
      return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
   }

   private String generateFromClause() {
      List<String> queriesForTrack = new ArrayList<String>();
      int trackId = 0;
      TrackSettingsDTO trackSettingsDTO = mapSettings.getTrackSettings().get(trackId);
      PlaceSettingsDTO placeSettingsDTO = null;
      String place = trackSettingsDTO.getPlace();
      int placeId = 0;

      while (placeId < mapSettings.getPlaceSettings().size()) {
         placeSettingsDTO = mapSettings.getPlaceSettings().get(placeId);
         if (place.equals(placeSettingsDTO.getName())) {
            break;
         }
         placeId++;
      }
      queriesForTrack.add(generateLatLongQueryForTrack(trackId, trackSettingsDTO, placeId, placeSettingsDTO));
      return queriesForTrack.stream().collect(Collectors.joining(" UNION ", "FROM (", ") a"));
   }

   private String generateLatLongQueryForTrack(int trackId, TrackSettingsDTO trackSettingsDTO, int placeId,
                                               PlaceSettingsDTO placeSettingsDTO) {
      return new StringBuilder(generateSelectClauseForTrack(trackId, trackSettingsDTO, placeId, placeSettingsDTO))
                       .append(" ").append(generateFromClauseForTrack())
                       .append(" ").append(generateWhereClauseForTrack(trackSettingsDTO, placeId))
                       .toString();
   }

   private String generateSelectClauseForTrack(int trackId, TrackSettingsDTO trackSettingsDTO, int placeId,
                                               PlaceSettingsDTO placeSettingsDTO) {
      List<String> columns = new ArrayList<String>();

      columns.add(placeId + " " + PLACE_ID);
      columns.add(trackId + " " + TRACK_ID);
      columns.add("\"" + placeSettingsDTO.getLatColumn() + "\" " + LAT);
      columns.add("\"" + placeSettingsDTO.getLongColumn() + "\" " + LON);
      columns.add(CacheUtil.INTERNAL_ID_NAME + " " + ROW_ID);
      columns.add(CacheUtil.INTERNAL_STATEID + " " + INTERNAL_STATE_ID);
      columns.add(getTypeColumn(placeSettingsDTO) + " " + TYPENAME);
      columns.add(getTrackColumn(trackSettingsDTO) + " " + TRACK_NAME);
      columns.add("\"" + trackSettingsDTO.getSequenceColumn() + "\"" + " " + SEQUENCE);
      return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
   }

   private String getTypeColumn(PlaceSettingsDTO placeSettingsDTO) {
      String typename = placeSettingsDTO.getTypeName();
      if (typename == null) {
         String typeColumn = placeSettingsDTO.getTypeColumn();
         if (typeColumn != null) {
            return "\"" + typeColumn + "\" || ''";
         }
         return "'' || ''";
      }
      return "'" + typename + "'";
   }

   private String getTrackColumn(TrackSettingsDTO trackSettingsDTO) {
      String identityName = trackSettingsDTO.getIdentityName();
      if (identityName == null) {
         String typeColumn = trackSettingsDTO.getIdentityColumn();
         if (typeColumn != null) {
            return "\"" + typeColumn + "\" || ''";
         }
         return "'' || ''";
      }
      return "'" + identityName + "'";
   }

   private String generateFromClauseForTrack() {
      return new StringBuilder("FROM ")
                       .append(CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid()))
                       .toString();
   }

   private String generateWhereClauseForTrack(TrackSettingsDTO trackSettingsDTO, int placeId) {
      String trackFilterString = "\"" + trackSettingsDTO.getSequenceColumn() + "\" IS NOT NULL";
      String identityName = trackSettingsDTO.getIdentityName();
      if (identityName == null) {
         String typeColumn = trackSettingsDTO.getIdentityColumn();
         if (typeColumn != null) {
            trackFilterString += " AND \"" + typeColumn + "\" IS NOT NULL";
         }
      }
      String retVal = "WHERE " + trackFilterString;
      String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettings, placeId);
      if (!filterString.isEmpty()) {
         retVal += " AND " + filterString;
      }
      return retVal;
   }

   private int getRowId(ResultSet rs) throws SQLException {
      String strId = rs.getString(ROW_ID);
      return Integer.parseInt(strId);
   }

   private int getInternalStateId(ResultSet rs) throws SQLException {
      String strId = rs.getString(INTERNAL_STATE_ID);
      return Integer.parseInt(strId);
   }

   private String getType(ResultSet rs) throws SQLException {
      String placeType = rs.getString(TYPENAME);
      if (placeType == null) {
         placeType = MapConstants.NULL_TYPE_NAME;
      } else if (placeType.trim().length() == 0) {
         placeType = MapConstants.EMPTY_TYPE_NAME;
      }
      return placeType;
   }

   private String getTrack(ResultSet rs) throws SQLException {
      return rs.getString(TRACK_NAME);
   }

   private interface QueryRow {
   }

   private static class OKQueryRow implements QueryRow {
   }

   private static class FirstQueryRow implements QueryRow {
      private Object sequenceObject;
      private int rowId;
      private int internalStateId;
      private Geometry geometry;
      private Integer placeId;
      private String typeName;

      FirstQueryRow(Object sequenceObject, int rowId, int internalStateId, Geometry geometry, Integer placeId,
                    String typeName) {
         this.sequenceObject = sequenceObject;
         this.rowId = rowId;
         this.internalStateId = internalStateId;
         this.geometry = geometry;
         this.placeId = placeId;
         this.typeName = typeName;
      }

      Object getSequenceObject() {
         return sequenceObject;
      }

      public int getRowId() {
         return rowId;
      }

      public int getInternalStateId() {
         return internalStateId;
      }

      public Geometry getGeometry() {
         return geometry;
      }

      public Integer getPlaceId() {
         return placeId;
      }

      public String getTypeName() {
         return typeName;
      }
   }
}
