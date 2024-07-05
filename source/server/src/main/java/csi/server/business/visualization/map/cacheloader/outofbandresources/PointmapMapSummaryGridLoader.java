package csi.server.business.visualization.map.cacheloader.outofbandresources;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.PointMapSummaryGrid;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapConstants;
import csi.shared.core.visualization.map.MapSettingsDTO;

public class PointmapMapSummaryGridLoader {
   private static final Logger LOG = LogManager.getLogger(PointmapMapSummaryGridLoader.class);

   private static final String PLACE_ID = "placeId";
   private static final String LAT = "lat";
   private static final String LON = "long";
   private static final String ROW_ID = "rowId";
   private static final String INTERNAL_STATE_ID = "internalStateId";
   private static final String TYPENAME = "typename";

   private String dataViewUuid;
   private DataView dataView;
   private MapViewDef mapViewDef;
   private MapSettingsDTO mapSettings;

   private PointMapSummaryGrid mapSummaryGrid;

   PointmapMapSummaryGridLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef) {
      dataViewUuid = mapCacheHandler.getDvUuid();
      mapSettings = mapCacheHandler.getMapSettings();
      this.dataView = dataView;
      this.mapViewDef = mapViewDef;
   }

   public void setMapSummaryGrid(PointMapSummaryGrid mapSummaryGrid) {
      this.mapSummaryGrid = mapSummaryGrid;
   }

   public void load() {
      QueryExecutor.execute(LOG, generateQuery(), rs -> {
         int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();

         while (rs.next()) {
            int rowId = getRowId(rs);
            int internalStateId = getInternalStateId(rs);
            Double latitude = rs.getDouble(LAT);
            Double longitude = rs.getDouble(LON);
            Geometry geometry = new Geometry(longitude, latitude);
            geometry.setSummaryLevel(summaryLevel);
            Integer placeId = rs.getInt(PLACE_ID);
            String typename = getType(rs);
            typename = typename.trim();
            boolean processRow = true;

            if (typename.equals(MapConstants.NULL_TYPE_NAME)) {
               processRow = mapSettings.getPlaceSettings().get(placeId).isIncludeNullType();
            }
            if (processRow) {
               PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
               mapSummaryGrid.addNode(geometry, rowId, internalStateId, key);
            }
         }
      });
   }

   private String generateQuery() {
      return new StringBuilder(generateSelectClause()).append(" ").append(generateFromClause()).toString();
   }

   private String generateSelectClause() {
      List<String> columns = new ArrayList<String>();

      columns.add(PLACE_ID);
      columns.add(LAT);
      columns.add(LON);
      columns.add(ROW_ID);
      columns.add(INTERNAL_STATE_ID);
      columns.add(TYPENAME);
      return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
   }

   private String generateFromClause() {
      List<String> queriesForPlace = new ArrayList<String>();
      int howMany = mapSettings.getPlaceSettings().size();

      for (int placeId = 0; placeId < howMany; placeId++) {
         queriesForPlace.add(generateLatLongQueryForPlace(placeId));
      }
      return queriesForPlace.stream().collect(Collectors.joining(" UNION ", "FROM (", ") a"));
   }

   private String generateLatLongQueryForPlace(int placeId) {
      return new StringBuilder(generateSelectClauseForPlace(placeId)).append(" ")
                       .append(generateFromClauseForPlace()).append(" ")
                       .append(generateWhereClauseForPlace(placeId))
                       .toString();
   }

   private String generateSelectClauseForPlace(int placeId) {
      List<String> columns = new ArrayList<String>();

      columns.add(placeId + " " + PLACE_ID);
      columns.add("\"" + mapSettings.getPlaceSettings().get(placeId).getLatColumn() + "\" " + LAT);
      columns.add("\"" + mapSettings.getPlaceSettings().get(placeId).getLongColumn() + "\" " + LON);
      columns.add(CacheUtil.INTERNAL_ID_NAME + " " + ROW_ID);
      columns.add(CacheUtil.INTERNAL_STATEID + " " + INTERNAL_STATE_ID);
      columns.add(getTypeColumn(placeId) + " " + TYPENAME);
      return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
   }

   private String getTypeColumn(int placeId) {
      String typename = mapSettings.getPlaceSettings().get(placeId).getTypeName();
      if (typename == null) {
         String typeColumn = mapSettings.getPlaceSettings().get(placeId).getTypeColumn();
         if (typeColumn != null) {
            return "\"" + typeColumn + "\" || ''";
         }
         return "'' || ''";
      }
      return "'" + typename + "'";
   }

   private String generateFromClauseForPlace() {
      return "FROM " + CacheUtil.getQuotedCacheTableName(dataViewUuid);
   }

   private String generateWhereClauseForPlace(int placeId) {
      String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettings, placeId);

      return filterString.isEmpty() ? "" : new StringBuilder("WHERE ").append(filterString).toString();
   }

   private int getRowId(ResultSet rs) throws SQLException {
      return Integer.parseInt(rs.getString(ROW_ID));
   }

   private int getInternalStateId(ResultSet rs) throws SQLException {
      return Integer.parseInt(rs.getString(INTERNAL_STATE_ID));
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
}
