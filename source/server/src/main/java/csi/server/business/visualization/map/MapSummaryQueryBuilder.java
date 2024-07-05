package csi.server.business.visualization.map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.service.AbstractQueryBuilder;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;

public class MapSummaryQueryBuilder extends AbstractQueryBuilder<MapViewDef> {
   private static final Logger LOG = LogManager.getLogger(MapSummaryQueryBuilder.class);

   private static final String LAT = "lat";
   private static final String LON = "long";

   public List<Geometry> rowIdsToSelectionInfo(List<? extends Number> rowIds) {
      List<Geometry> geometries = new ArrayList<Geometry>();
      QueryGenerator generator = new QueryGenerator(getDataView(), getViewDef(), rowIds);

      QueryExecutor.execute(LOG, generator.generate(), rs -> {
         int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();

         while (rs.next()) {
            double lat = rs.getDouble(LAT);
            double lon = rs.getDouble(LON);
            Geometry geometry = new Geometry(lon, lat);

            geometry.setSummaryLevel(summaryLevel);
            geometries.add(geometry);
         }
      });
      return geometries;
   }

   static class QueryGenerator {
      private DataView dataView;
      private MapViewDef mapViewDef;
      private MapSettings mapSettings;
      private List<Number> rowIds;

      public QueryGenerator(DataView dataView, MapViewDef mapViewDef, List<? extends Number> rowIds) {
         this.dataView = dataView;
         this.mapViewDef = mapViewDef;
         mapSettings = mapViewDef.getMapSettings();
         this.rowIds = new ArrayList<Number>();
         this.rowIds.addAll(rowIds);
      }

      public String generate() {
         return "SELECT " + LAT + ", " + LON + " FROM (" + getInnerQuery() + ") a";
      }

      private String getInnerQuery() {
         List<String> subQueries = new ArrayList<String>();

         for (int placeId = 0; placeId < mapSettings.getMapPlaces().size(); placeId++) {
            subQueries.add(getSubQuery(placeId));
         }
         return subQueries.stream().collect(Collectors.joining(" UNION "));
      }

      private String getSubQuery(int placeId) {
         return (rowIds.size() < 100)
                   ? getSelectClause(placeId) + " " + getFromClause() + " " + getWhereClause()
                   : getSelectClause(placeId) + " " + getValuesFromClause() + " " + getValuesWhereClause();
      }

      private String getSelectClause(int placeId) {
         List<String> columns = new ArrayList<String>();

         columns.add("\"" + mapSettings.getMapPlaces().get(placeId).getLatField().getLocalId().replace("-", "_") + "\" " + LAT);
         columns.add("\"" + mapSettings.getMapPlaces().get(placeId).getLongField().getLocalId().replace("-", "_") + "\" " + LON);

         return "SELECT " + columns.stream().collect(Collectors.joining(", "));
      }

      private String getFromClause() {
         return "FROM " + CacheUtil.getQuotedCacheTableName(dataView.getUuid());
      }

      private String getValuesFromClause() {
         List<String> rowIdStrings = new ArrayList<String>();

         rowIds.forEach(rowId -> rowIdStrings.add("(" + rowId + ")"));

         return new StringBuilder("FROM ").append(CacheUtil.getQuotedCacheTableName(dataView.getUuid())).append(
                                  " JOIN (VALUES ").append(rowIdStrings.stream().collect(Collectors.joining(", "))).append(")" +
                                    " ex(ex_" + CacheUtil.INTERNAL_ID_NAME +
                                    ") ON (" + CacheUtil.INTERNAL_ID_NAME + " = ex_" + CacheUtil.INTERNAL_ID_NAME + ")")
                          .toString();
      }

      private String getWhereClause() {
         String mapViewDefUuid = mapViewDef.getUuid();
         MapSettingsDTO mapSettingsDTO = MapCacheUtil.getMapSettings(mapViewDefUuid);
         String filter = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettingsDTO);
         String idFilter = getIdFilter();
         StringBuilder sb = new StringBuilder("WHERE ");

         return ((filter == null) || (filter.length() == 0))
                   ? sb.append(idFilter).toString()
                   : sb.append(filter).append(" AND ").append(idFilter).toString();
      }

      private String getIdFilter() {
         return rowIds.stream().map(i -> i.toString()).collect(Collectors.joining(", ", CacheUtil.INTERNAL_ID_NAME + " IN (", ")"));
      }

      private String getValuesWhereClause() {
         String mapViewDefUuid = mapViewDef.getUuid();
         MapSettingsDTO mapSettingsDTO = MapCacheUtil.getMapSettings(mapViewDefUuid);
         String filter = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettingsDTO);

         return ((filter == null) || (filter.length() == 0))
                   ? ""
                   : new StringBuilder("WHERE ").append(filter).toString();
      }
   }
}
