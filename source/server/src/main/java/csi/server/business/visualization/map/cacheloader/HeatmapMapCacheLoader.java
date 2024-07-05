package csi.server.business.visualization.map.cacheloader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.UUIDUtil;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class HeatmapMapCacheLoader extends AbstractMapCacheLoader {
   private static final Logger LOG = LogManager.getLogger(HeatmapMapCacheLoader.class);

    public HeatmapMapCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef) throws CentrifugeException {
        super(mapCacheHandler, dataView, mapViewDef);
    }

   @Override
   public void load() {
      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         mapCacheHandler.invalidateExtentIfMapNotPinned();
         mapCacheHandler.invalidateInitialExtent();
         mapCacheHandler.initializeMapNodeInfo();
         generateSelectedItemsString();

         filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettings);

         try (ResultSet rs = getData(conn)) {
            populateMapCache(rs);
         }
      } catch (CentrifugeException | SQLException e) {
         LOG.error(e);
      }
   }

    private void generateSelectedItemsString() {
        Set<String> selectedColumnNames = gatherSelectedColumns();
        formSelectedItemsString(selectedColumnNames);
    }

    private Set<String> gatherSelectedColumns() {
        Set<String> selectedColumnNames = gatherMinimumSelectedColumns();
        addToSetIfNotNull(selectedColumnNames, mapSettings.getWeightColumn());
        return selectedColumnNames;
    }

    private Set<String> gatherMinimumSelectedColumns() {
        Set<String> selectedColumnNames = new TreeSet<String>();
        addToSetIfNotNull(selectedColumnNames, CacheUtil.INTERNAL_ID_NAME);
        for (int placeId = 0; placeId < mapSettings.getPlaceSettings().size(); placeId++) {
            PlaceSettingsDTO placeSettings = mapSettings.getPlaceSettings().get(placeId);
            addToSetIfNotNull(selectedColumnNames, placeSettings.getLatColumn());
            addToSetIfNotNull(selectedColumnNames, placeSettings.getLongColumn());
        }
        return selectedColumnNames;
    }

    private void populateMapCache(ResultSet rs) throws SQLException {
        if (rs == null) {
            // TODO: probably should do something here
        } else {
            Map<Geometry, AugmentedMapNode> mapNodeMapByGeom = mapCacheHandler.getMapNodeByGeometryMap();
            Map<Long, AugmentedMapNode> mapNodeMapById = mapCacheHandler.getMapNodeByIdMap();
            int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
            int count = 0;
            mapCacheHandler.setPointLimitReached(false);
            while (rs.next() && !mapCacheHandler.isPointLimitReached()) {
                String strId = rs.getString(CacheUtil.INTERNAL_ID_NAME);
                Integer id = Integer.parseInt(strId);
                for (int placeId = 0; placeId < mapSettings.getPlaceSettings().size(); placeId++) {
                    PlaceSettingsDTO placeSettings = mapSettings.getPlaceSettings().get(placeId);
                    double latitude = rs.getDouble(placeSettings.getLatColumn());
                    if (rs.wasNull() || (latitude < -90) || (latitude > 90)) {
                        continue;
                    }
                    double longitude = rs.getDouble(placeSettings.getLongColumn());
                    if (rs.wasNull() || (longitude < -180) || (longitude > 180)) {
                        continue;
                    }
                    Geometry geometry = new Geometry(longitude, latitude);
                    geometry.setSummaryLevel(summaryLevel);
                    AugmentedMapNode mapNode;
                    if (mapNodeMapByGeom.containsKey(geometry)) {
                        mapNode = mapNodeMapByGeom.get(geometry);
                    } else {
                        count++;
                        if (count > Configuration.getInstance().getMapConfig().getPointLimit()) {
                            mapCacheHandler.setPointLimitReached(true);
                            break;
                        }
                        mapNode = MapNodeUtil.createMapNode(mapCacheHandler.getVizUuid(), mapNodeMapByGeom, mapNodeMapById, UUIDUtil.getUUIDLong(), geometry);
                    }
                    mapNode.getRowIds().add(id);
                    if (mapSettings.getWeightColumn() == null) {
                        mapNode.incrementHits();
                    } else {
                        double weight = rs.getDouble(mapSettings.getWeightColumn());
                        mapNode.incrementHits(weight);
                    }
                }
            }
            if ((count == 0) || mapCacheHandler.isPointLimitReached()) {
                mapNodeMapByGeom.clear();
                mapNodeMapById.clear();
            }
        }
    }

}
