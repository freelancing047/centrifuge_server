package csi.server.business.visualization.map.cacheloader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.BundleMapNode;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.UUIDUtil;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.dao.CsiConnection;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.visualization.map.MapBundleDefinitionDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class BundlemapMapCacheLoader extends AbstractMapCacheLoader {
   private static final Logger LOG = LogManager.getLogger(BundlemapMapCacheLoader.class);

   private boolean countChild;

    public BundlemapMapCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef)
            throws CentrifugeException {
        super(mapCacheHandler, dataView, mapViewDef);
    }

   @Override
   public void load() {
      try (CsiConnection conn = CsiPersistenceManager.getCacheConnection()) {
         mapCacheHandler.invalidateExtentIfMapNotPinned();
         mapCacheHandler.invalidateInitialExtent();
         mapCacheHandler.initializeMapNodeInfoAndMapLinkInfo();

         generateSelectedItemsString();
         filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapCacheHandler.getMapSettings());
         generateGroupByString();

         try (ResultSet rs = getData(conn)) {
            populateMapCache(rs);
         }
      } catch (CentrifugeException | SQLException e) {
         LOG.error(e);
      }
   }

    private void generateSelectedItemsString() {
        Map<String, String> selectedColumnNames = gatherSelectedColumns();
        List<String> columns = selectedColumnNames.entrySet().stream().map(entry -> entry.getKey() + " AS " + "\"" + entry.getValue() + "\"").collect(Collectors.toList());
        selectedItemsString = columns.stream().collect(Collectors.joining(", "));
    }

    private Map<String, String> gatherSelectedColumns() {
        Map<String, String> selectedColumnNames = new TreeMap<String, String>();
        List<Crumb> breadCrumb = mapCacheHandler.getBreadcrumb();

        MapBundleDefinitionDTO mapBundleDefinitionDTO;
        MapBundleDefinitionDTO childMapBundleDefinitionDTO;
        int index = 0;
        if ((breadCrumb != null) && !breadCrumb.isEmpty()) {
            index = breadCrumb.size() - 1;
        }

        mapBundleDefinitionDTO = mapSettings.getMapBundleDefinitions().get(index);
        String childFieldColumn = null;
        if ((index + 1) < mapSettings.getMapBundleDefinitions().size()) {
            childMapBundleDefinitionDTO = mapSettings.getMapBundleDefinitions().get(index + 1);
            childFieldColumn = childMapBundleDefinitionDTO.getFieldColumn();
        }

        countChild = mapCacheHandler.isCountChildren() && (childFieldColumn != null);
        if (countChild) {
            String queryString = "COUNT(DISTINCT \"" + childFieldColumn + "\")";
            if (mapBundleDefinitionDTO.isAllowNulls()) {
               queryString += " + COUNT(DISTINCT CASE WHEN \"" + childFieldColumn + "\" IS NULL THEN 1 END)";
            }
            addToMapIfNotNull(selectedColumnNames, queryString, "ChildrenCount");
        }
        String fieldColumn;
        if ((breadCrumb != null) && !breadCrumb.isEmpty()) {
            fieldColumn = mapSettings.getMapBundleDefinitions().get(breadCrumb.size()).getFieldColumn();
        } else {
            fieldColumn = mapSettings.getMapBundleDefinitions().get(0).getFieldColumn();
        }
        addToMapIfNotNull(selectedColumnNames, "\"" + fieldColumn + "\"", "Label");

        addToMapIfNotNull(selectedColumnNames, "COUNT(*)", "Hits");
        for (int placeId = 0; placeId < mapSettings.getPlaceSettings().size(); placeId++) {
            PlaceSettingsDTO placeSettings = mapSettings.getPlaceSettings().get(placeId);
            addToMapIfNotNull(selectedColumnNames, "AVG(\"" + placeSettings.getLatColumn() + "\")", placeSettings.getLatColumn());
            addToMapIfNotNull(selectedColumnNames, "AVG(\"" + placeSettings.getLongColumn() + "\")", placeSettings.getLongColumn());
        }
        return selectedColumnNames;
    }

    private void addToMapIfNotNull(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void generateGroupByString() {
        StringBuilder out = new StringBuilder();
        List<Crumb> breadCrumb = mapCacheHandler.getBreadcrumb();
        String fieldColumn;
        if ((breadCrumb != null) && !breadCrumb.isEmpty()) {
            fieldColumn = mapSettings.getMapBundleDefinitions().get(breadCrumb.size()).getFieldColumn();
        } else {
            fieldColumn = mapSettings.getMapBundleDefinitions().get(0).getFieldColumn();
        }
        out.append("\"" + fieldColumn + "\"");
        groupByString = out.toString();
    }

    private void populateMapCache(ResultSet rs) throws SQLException {
        if (rs == null) {
            // TODO: probably should do something here
        } else {
            if (mapCacheHandler.getBreadcrumb() == null) {
                List<Crumb> bundleLevel = new ArrayList<Crumb>();
                mapCacheHandler.addBreadcrumb(bundleLevel);
            }

            Map<Geometry, AugmentedMapNode> mapNodeMapByGeom = mapCacheHandler.getMapNodeByGeometryMap();
            Map<Long, AugmentedMapNode> mapNodeMapById = mapCacheHandler.getMapNodeByIdMap();
            int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
            int count = 0;
            mapCacheHandler.setPointLimitReached(false);
            Map<String, BundleMapNode> bundleMapNodeMap = new HashMap<String, BundleMapNode>();
            while (rs.next() && !mapCacheHandler.isPointLimitReached()) {
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
                    String label = rs.getString("Label");
                    BundleMapNode bundleMapNode;
                    if (bundleMapNodeMap.containsKey(label)) {
                        bundleMapNode = bundleMapNodeMap.get(label);
                    } else {
                        count++;
                        if (count > Configuration.getInstance().getMapConfig().getPointLimit()) {
                            mapCacheHandler.setPointLimitReached(true);
                            break;
                        }
                        bundleMapNode = new BundleMapNode(mapCacheHandler.getVizUuid(), UUIDUtil.getUUIDLong(), "Label", label, "");
                        bundleMapNodeMap.put(label, bundleMapNode);
                    }
                    double hits = rs.getDouble("Hits");
                    bundleMapNode.setHits(hits);
                    bundleMapNode.setGeometry(geometry);
                    if (countChild) {
                        bundleMapNode.setChildrenCount(rs.getInt("ChildrenCount"));
                    }
                }
            }
            if ((count == 0) || mapCacheHandler.isPointLimitReached()) {
                mapNodeMapByGeom.clear();
                mapNodeMapById.clear();
            } else {
                for (BundleMapNode bundleMapNode : bundleMapNodeMap.values()) {
                    if (bundleMapNode != null) {
                        mapNodeMapById.put(bundleMapNode.getNodeId(), bundleMapNode);
                        mapNodeMapByGeom.put(bundleMapNode.getGeometry(), bundleMapNode);
                    }
                }
            }
        }
    }
}
