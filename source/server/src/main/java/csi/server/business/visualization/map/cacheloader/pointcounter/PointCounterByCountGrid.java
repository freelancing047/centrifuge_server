package csi.server.business.visualization.map.cacheloader.pointcounter;

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
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.UBox;

public class PointCounterByCountGrid extends AbstractPointCounter {
   private static final Logger LOG = LogManager.getLogger(PointCounterByCountGrid.class);

   private static final String LAT = "lat";
   private static final String LON = "long";

   private DataView dataView;
   private MapViewDef mapViewDef;
   private MapSettingsDTO mapSettings;
   private MapSummaryExtent mapSummaryExtent;

   private CountGrid countGrid;

   public PointCounterByCountGrid(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef,
                                  UBox uBox) {
      super(mapCacheHandler);

      this.dataView = dataView;
      this.mapViewDef = mapViewDef;

      if (uBox == null) {
         mapSummaryExtent = null;
      } else {
         mapSummaryExtent = uBox.getMapSummaryExtent();
      }
   }

   public PointCounterByCountGrid(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef) {
      this(mapCacheHandler, dataView, mapViewDef, null);
   }

   @Override
   public void calculatePrecision(int startingPrecision) {
      init();

      countGrid = new CountGrid(POINT_LIMIT);
      populateCountGrid();

      precision = Configuration.getInstance().getMapConfig().getDetailLevel();
      numPoints = getNumPoints();

      if (numPoints != 0) {
         if (numPoints < POINT_LIMIT) {
            coarsestPrecision = precision;
         } else {
            precision = startingPrecision;
            getCorrectPrecision();
         }
      }
   }

   private void init() {
      mapSettings = mapCacheHandler.getMapSettings();
   }

   private void populateCountGrid() {
      QueryExecutor.execute(LOG, generateLatLongQueryForPlaces(), new QueryExecutor.ResultSetProcessor() {
         @Override
         public void process(ResultSet rs) throws SQLException {
            if (!rs.isBeforeFirst()) {
               return;
            }
            while (rs.next()) {
               String[] longitudes = getStringArray(rs.getString(LON));
               String[] latitudes = getStringArray(rs.getString(LAT));
               if ((longitudes != null) && (latitudes != null)) {
                  for (int i = 0; i < longitudes.length; i++) {
                     countGrid.addCoordinate(latitudes[i], longitudes[i]);
                  }
               }
            }
         }

         private String[] getStringArray(String longitudeArray) {
            if (longitudeArray == null) {
               return null;
            }
            return longitudeArray.substring(1, longitudeArray.length() - 1).split(",");
         }
      });
   }

   @Override
   int getNumPoints() {
      return countGrid.getCount(precision);
   }

   private String generateLatLongQueryForPlaces() {
      List<String> queriesForPlace = new ArrayList<String>();
      int howMany = mapSettings.getPlaceSettings().size();

      for (int placeId = 0; placeId < howMany; placeId++) {
         queriesForPlace.add(generateLatLongQueryForPlace(placeId));
      }
      return queriesForPlace.stream().collect(Collectors.joining(" UNION "));
   }

   private String generateLatLongQueryForPlace(int placeId) {
      return new StringBuilder(generateSelectClause(placeId)).append(" ")
                       .append(generateFromClause()).append(" ")
                       .append(generateWhereClause(placeId))
                       .toString();
   }

   private String generateSelectClause(int placeId) {
      List<String> columns = new ArrayList<String>();

      columns.add("array_agg(\"" + mapSettings.getPlaceSettings().get(placeId).getLatColumn() + "\") " + LAT);
      columns.add("array_agg(\"" + mapSettings.getPlaceSettings().get(placeId).getLongColumn() + "\") " + LON);
      return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
   }

   private String generateFromClause() {
      return new StringBuilder("FROM ").append(CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid())).toString();
   }

   private String generateWhereClause(int placeId) {
      String filterString =
         FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettings, placeId, mapSummaryExtent);

      return filterString.isEmpty() ? "" : new StringBuilder("WHERE ").append(filterString).toString();
   }
}
