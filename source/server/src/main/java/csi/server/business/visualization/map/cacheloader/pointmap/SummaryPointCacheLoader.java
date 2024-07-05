package csi.server.business.visualization.map.cacheloader.pointmap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.MapConfig;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.business.visualization.map.UUIDUtil;
import csi.server.business.visualization.map.cacheloader.AbstractMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.MapNodeUtil;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.MapSummaryColumnStringBuilder;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.PlaceTypenameProcessor;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary.AbstractMapSummary;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary.PlaceSummary;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary.TypeSummary;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.CountTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.TypeSizeValue;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.UBox;

public class SummaryPointCacheLoader extends AbstractMapCacheLoader {
    private static final Logger LOG = LogManager.getLogger(SummaryPointCacheLoader.class);

    private static final String LAT = "lat";
    private static final String LON = "long";
    private static final String INTERNAL_STATE_ID = "internal_state_id";
    private static final String PLACE_ID = "place_id";
    private static final String PLACE_TYPE = "place_type";
    private static final String PLACE_ICON = "place_icon";
    protected int precision;
    private MapTheme mapTheme;
    private MapSummaryExtent mapSummaryExtent;

    private boolean maxPlaceSizeEqualsMinPlaceSize;
    private MapNodeInfo mapNodeInfo;
    private int summaryLevel;
    private Map<String, Integer> mapPlaceNameNodeId;
    private Map<String, PlaceStyle> typeNameToPlaceStyle;
    private Map<Integer, Set<String>> placeIdToTypeNameMap;

    public SummaryPointCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) throws CentrifugeException {
        super(mapCacheHandler, dataView, mapViewDef);
        this.mapTheme = mapTheme;
        if (uBox == null) {
            mapSummaryExtent = null;
        } else {
            mapSummaryExtent = uBox.getMapSummaryExtent();
        }
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    @Override
    public void load() {
        MapConfig mapConfig = Configuration.getInstance().getMapConfig();
        maxPlaceSizeEqualsMinPlaceSize = mapConfig.getMaxPlaceSize() == mapConfig.getMinPlaceSize();

        mapNodeInfo = new MapNodeInfo();
        mapCacheHandler.setMapNodeInfo(mapNodeInfo);
        summaryLevel = mapCacheHandler.getCurrentMapSummaryPrecision();

        mapPlaceNameNodeId = new HashMap<String,Integer>();
        typeNameToPlaceStyle = new HashMap<String,PlaceStyle>();
        placeIdToTypeNameMap = new HashMap<Integer, Set<String>>();
        for (int placeId = 0; placeId < mapSettings.getPlaceSettings().size(); placeId++) {
            String query = generateQueryForPlace(dataView, mapViewDef, mapSettings, placeId);
            QueryExecutor.execute(LOG, query, this::populateSummaryCache);
        }
    }

    private String generateQueryForPlace(DataView dataView, MapViewDef mapViewDef, MapSettingsDTO mapSettings, int placeId) {
        MapSummaryColumnStringBuilder builder = new MapSummaryColumnStringBuilder(mapSettings, placeId, precision);
        return generateSelectClause(mapSettings, placeId, builder) + " " +
                generateFromClause(mapCacheHandler.getDvUuid()) + " " +
                generateWhereClause(dataView, mapViewDef, mapSettings, placeId);

    }

    private String generateSelectClause(MapSettingsDTO mapSettings, int placeId, MapSummaryColumnStringBuilder builder) {
        List<String> columns = new ArrayList<String>();

        columns.add(builder.getLatColumnString() + " AS " + LAT);
        columns.add(builder.getLongColumnString() + " AS " + LON);
        columns.add(CacheUtil.INTERNAL_STATEID + " AS " + INTERNAL_STATE_ID);
        columns.add(placeId + " AS " + PLACE_ID);
        columns.add(getPlaceType(mapSettings, placeId) + " AS " + PLACE_TYPE);
        columns.add(getPlaceIcon(mapSettings, placeId) + " AS " + PLACE_ICON);
        return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
    }

    private String getPlaceType(MapSettingsDTO mapSettings, int placeId) {
        String placeTypeColumn = mapSettings.getPlaceSettings().get(placeId).getTypeColumn();
        if (placeTypeColumn == null) {
         return "'' || ''";
      } else {
         return "\"" + placeTypeColumn + "\" || ''";
      }
    }

    private String getPlaceIcon(MapSettingsDTO mapSettings, int placeId) {
        String placeIconColumn = mapSettings.getPlaceSettings().get(placeId).getIconColumn();
        if (placeIconColumn == null) {
         return "'' || ''";
      } else {
         return "\"" + placeIconColumn + "\"";
      }
    }

    private String generateFromClause(String dataViewUuid) {
        return "FROM " + CacheUtil.getQuotedCacheTableName(dataViewUuid);
    }

    private String generateWhereClause(DataView dataView, MapViewDef mapViewDef, MapSettingsDTO mapSettings, int placeId) {
        String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettings, placeId, mapSummaryExtent);
        if (!filterString.isEmpty()) {
         return "WHERE " + filterString;
      } else {
         return "";
      }
    }

    private void populateSummaryCache(ResultSet rs) throws SQLException {
        if (rs == null) {
            // TODO: probably should do something here
        } else {
            for (int placeId = mapSettings.getPlaceSettings().size() - 1; placeId >= 0; placeId--) {
               mapPlaceNameNodeId.put(mapSettings.getPlaceSettings().get(placeId).getName(), placeId);
            }

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
                int placeId = rs.getInt(PLACE_ID);
                Integer internalStateId = getInternalStateId(rs);

                PlaceidTypenameDuple key = processPlaceTypename(placeId, rs, mapNodeInfo, typeNameToPlaceStyle);
                if ((key != null) && (key.getTypename() != null)) {
                    if (!placeIdToTypeNameMap.containsKey(placeId)) {
                     placeIdToTypeNameMap.put(placeId, new HashSet<String>());
                  }
                    placeIdToTypeNameMap.get(placeId).add(key.getTypename());

                    Geometry geometry = new Geometry(longitude, latitude);
                    geometry.setSummaryLevel(summaryLevel);
                    AugmentedMapNode mapNode;
                    if (mapNodeInfo.getMapByGeometry().containsKey(geometry)) {
                     mapNode = mapNodeInfo.getMapByGeometry().get(geometry);
                  } else {
                     mapNode = MapNodeUtil.createMapNode(mapCacheHandler.getVizUuid(), mapNodeInfo.getMapByGeometry(), mapNodeInfo.getMapById(), UUIDUtil.getUUIDLong(), geometry);
                  }
                    updateMapNode(rs, mapNode, internalStateId, placeId, key, mapNodeInfo);
                }
            }

            if (mapCacheHandler.getItemsInViz() == 0) {
                mapNodeInfo.getMapById().clear();
                mapNodeInfo.getMapByType().clear();
                mapNodeInfo.getCombinedMapNodes().clear();
            } else {
                calculateMapNodeStatistics(mapNodeInfo.getMapByType(), mapNodeInfo.getPlaceIdToTypeNames());
                mapCacheHandler.sortCurrentMapSummaryMapNodeType();
            }
        }
    }

   private Integer getInternalStateId(ResultSet rs) throws SQLException {
      return Integer.decode(rs.getString(INTERNAL_STATE_ID));
   }

    private PlaceidTypenameDuple processPlaceTypename(int placeId, ResultSet rs, MapNodeInfo mapNodeInfo, Map<String, PlaceStyle> typeNameToPlaceStyle) throws SQLException {
        PlaceSettingsDTO placeSettings = mapSettings.getPlaceSettings().get(placeId);
        String typenameFromRS = rs.getString(PLACE_TYPE);
        return PlaceTypenameProcessor.process(mapSettings, placeId, placeSettings, mapNodeInfo, mapTheme, typeNameToPlaceStyle, typenameFromRS);
    }

    private void updateMapNode(ResultSet rs, AugmentedMapNode mapNode, Integer internalStateId, int placeId, PlaceidTypenameDuple key, MapNodeInfo mapNodeInfo) throws SQLException, NumberFormatException {
        updateMapNode(rs, mapNode, internalStateId, placeId, key, mapNodeInfo.getMapByType(), mapNodeInfo.getTypenameToIconUrl(), mapNodeInfo.getCombinedMapNodes(), mapNodeInfo.getNewMapNodes(), mapNodeInfo.getUpdatedMapNodes());
    }

    private void updateMapNode(ResultSet rs, AugmentedMapNode mapNode, Integer internalStateId, int placeId, PlaceidTypenameDuple key, Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<PlaceidTypenameDuple, String> typenameToIconUrl, Set<AugmentedMapNode> combinedMapNodes, Set<AugmentedMapNode> newMapNodes, Set<AugmentedMapNode> updatedMapNodes) throws SQLException, NumberFormatException {
        setMapNodeTypeAndPlace(mapNode, mapNodeByType, combinedMapNodes, internalStateId, placeId, key);
        initMapNodeSize(mapNode, placeId, key);
        if (mapNode.isNew() && !newMapNodes.contains(mapNode)) {
            newMapNodes.add(mapNode);
        } else if (mapNode.isUpdated()) {
            updatedMapNodes.add(mapNode);
        }
        mapNode.incrementHits();
        gatherMapNodeStatistics(mapNode, placeId, key);
        setMapNodeIconUrl(mapNode, rs, key, typenameToIconUrl);
    }

    private void setMapNodeTypeAndPlace(AugmentedMapNode mapNode, Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Set<AugmentedMapNode> combinedMapNodes, Integer internalStateId, int placeId, PlaceidTypenameDuple key) {
        MapNodeUtil.setMapNodeTypeAndPlace(mapNode, internalStateId, placeId, key, mapNodeByType, dataView);
        if (mapNode.isCombined()) {
         combinedMapNodes.add(mapNode);
      }
    }

    private void initMapNodeSize(AugmentedMapNode mapNode, int placeId, PlaceidTypenameDuple key) {
        if (mapSettings.getPlaceSettings().get(placeId).getSizeColumn() == null) {
            mapNode.setSize(key, mapSettings.getPlaceSettings().get(placeId).getSize());
        }
    }

   private void gatherMapNodeStatistics(AugmentedMapNode mapNode, int placeId, PlaceidTypenameDuple key) throws NumberFormatException {
      if (!maxPlaceSizeEqualsMinPlaceSize) {
         if (mapSettings.getPlaceSettings().get(placeId).isSizedByDynamicType().booleanValue()) {
            gatherMapNodeTypeStatistics(mapNode, key);
         } else {
            gatherMapNodePlaceStatistics(mapNode, placeId);
         }
      }
   }

    private void gatherMapNodeTypeStatistics(AugmentedMapNode mapNode, PlaceidTypenameDuple key) {
        TypeSizeValue typeSizeValue = mapNode.getTypeSizeValue(key);
        if (typeSizeValue == null) {
            typeSizeValue = new CountTypeSizeValue();
            mapNode.setTypeSizeValue(key, typeSizeValue);
        }
        ((CountTypeSizeValue) typeSizeValue).incrementCount();
    }

    private void gatherMapNodePlaceStatistics(AugmentedMapNode mapNode, int placeId) {
        TypeSizeValue typeSizeValue = mapNode.getPlaceSizeValue(placeId);
        if (typeSizeValue == null) {
            typeSizeValue = new CountTypeSizeValue();
            mapNode.setPlaceSizeValue(placeId, typeSizeValue);
        }
        ((CountTypeSizeValue) typeSizeValue).incrementCount();
    }

    private void setMapNodeIconUrl(AugmentedMapNode mapNode, ResultSet rs, PlaceidTypenameDuple key,
                                   Map<PlaceidTypenameDuple, String> typenameToIconUrl) throws SQLException {
        if (typenameToIconUrl.get(key) != null) {
         mapNode.setIconUri(key, "id=" + typenameToIconUrl.get(key));
      } else {
            String fieldValue = rs.getString(PLACE_ICON);
            if ((fieldValue != null) && (fieldValue.trim().length() > 0)) {
               mapNode.setIconUri(key, "id=" + fieldValue);
            }
        }
    }

   private void calculateMapNodeStatistics(Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> placeIdToTypeNames) {
      if (!maxPlaceSizeEqualsMinPlaceSize) {
         for (int placeId = mapSettings.getPlaceSettings().size() - 1; placeId >= 0; placeId--) {
            if (mapSettings.getPlaceSettings().get(placeId).isSizedByDynamicType().booleanValue()) {
               calculateMapNodeTypeStatistics(mapNodeByType, placeIdToTypeNames, placeId);
            } else {
               calculateMapNodePlaceStatistics(mapNodeByType, placeIdToTypeNames, placeId);
            }
         }
      }
   }

    private void calculateMapNodeTypeStatistics(Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> placeIdToTypeNames, int placeId) {
        if (placeIdToTypeNames.isEmpty() || !placeIdToTypeNames.containsKey(placeId)) {
            return;
        }
        for (String typename : placeIdToTypeNames.get(placeId)) {
            calculateMapNodeTypeStatisticsByTypename(mapNodeByType, placeId, typename);
        }
    }

    private void calculateMapNodeTypeStatisticsByTypename(Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, int placeId, String typename) {
        PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
        Set<AugmentedMapNode> mapNodes = mapNodeByType.get(key);
        if (mapNodes != null) {
         generateTypeSummary(key, mapNodes);
      }
    }

    private void generateTypeSummary(PlaceidTypenameDuple key, Set<AugmentedMapNode> mapNodes) {
        AbstractMapSummary typeSummary = new TypeSummary(key);
        for (AugmentedMapNode mapNode : mapNodes) {
            TypeSizeValue typeSizeValue = mapNode.getTypeSizeValue(key);
            Double count = null;
            if (typeSizeValue != null) {
               count = typeSizeValue.getValue();
            }
            updateMapSummary(typeSummary, mapNode, count);
        }
        if (typeSummary.hasMapNodes()) {
         typeSummary.setMapNodeSizes();
      }
    }

    private void updateMapSummary(AbstractMapSummary placeSummary, AugmentedMapNode mapNode, Double count) {
        if (count != null) {
            placeSummary.addMapNode(mapNode, count);
            placeSummary.updateMinMax(count);
        }
    }

    private void calculateMapNodePlaceStatistics(Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> placeIdToTypeNames, int placeId) {
        if (placeIdToTypeNames.isEmpty() || !placeIdToTypeNames.containsKey(placeId)) {
            return;
        }
        generatePlaceSummary(mapNodeByType, placeIdToTypeNames, placeId);
    }

    private void generatePlaceSummary(Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> placeIdToTypeNames, int placeId) {
        Set<String> typeNames = placeIdToTypeNames.get(placeId);
        Set<PlaceidTypenameDuple> keys = new TreeSet<PlaceidTypenameDuple>();
        for (String typename : typeNames) {
            PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
            keys.add(key);
        }
        AbstractMapSummary placeSummary = new PlaceSummary();
        for (String typename : placeIdToTypeNames.get(placeId)) {
         calculateMapNodePlaceStatisticsByTypename(mapNodeByType, placeId, placeSummary, typename);
      }
        if (placeSummary.hasMapNodes()) {
         placeSummary.setMapNodeSizes();
      }
    }

    private void calculateMapNodePlaceStatisticsByTypename(Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, int placeId, AbstractMapSummary placeSummary, String typename) {
        PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
        Set<AugmentedMapNode> mapNodes = mapNodeByType.get(key);
        if (mapNodes != null) {
         for (AugmentedMapNode mapNode : mapNodes) {
             if (placeSummary.nodeValueAlreadySubmitted(mapNode.getNodeId())) {
               continue;
            }
             TypeSizeValue typeSizeValue = mapNode.getPlaceSizeValue(placeId);
             Double count = null;
             if (typeSizeValue != null) {
               count = typeSizeValue.getValue();
            }

             updateMapSummary(placeSummary, mapNode, count);
         }
      }
    }
}
