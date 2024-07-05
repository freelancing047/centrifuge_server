package csi.server.business.visualization.map.cacheloader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import csi.config.Configuration;
import csi.config.MapConfig;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapNode;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.UUIDUtil;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.PlaceTypenameProcessor;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.AbsAvgTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.AbsSumTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.AvgTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.CountDistinctTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.CountTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.MaxTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.MinTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.SumTypeSizeValue;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.TypeSizeValue;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.MapTooltipFieldDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class MapNodeUtil {
    private static Comparator<MapNode> mapNodeComparator;
    private boolean maxPlaceSizeEqualsMinPlaceSize;
    private DataView dataView;
    private MapSettingsDTO mapSettings;
    private String mapViewDefUuid;
    private List<PlaceSettingsDTO> placeSettings;
    private MapNodeInfo mapNodeInfo;
    private Map<PlaceidTypenameDuple, String> typenameToIconUrl;
    private Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType;
    private Set<AugmentedMapNode> combinedMapNodes;
    private Set<AugmentedMapNode> newMapNodes;
    private Set<AugmentedMapNode> updatedMapNodes;
    private MapTheme mapTheme;
    private Map<String, PlaceStyle> typeNameToPlaceStyle;

    public MapNodeUtil(DataView dataView, MapSettingsDTO mapSettings, MapNodeInfo mapNodeInfo, MapTheme mapTheme, Map<String, PlaceStyle> typeNameToPlaceStyle) {
        MapConfig mapConfig = Configuration.getInstance().getMapConfig();
        maxPlaceSizeEqualsMinPlaceSize = mapConfig.getMaxPlaceSize() == mapConfig.getMinPlaceSize();
        this.dataView = dataView;
        this.mapSettings = mapSettings;
        mapViewDefUuid = mapSettings.getUuid();
        placeSettings = mapSettings.getPlaceSettings();
        this.mapNodeInfo = mapNodeInfo;
        typenameToIconUrl = mapNodeInfo.getTypenameToIconUrl();
        mapNodeByType = mapNodeInfo.getMapByType();
        combinedMapNodes = mapNodeInfo.getCombinedMapNodes();
        newMapNodes = mapNodeInfo.getNewMapNodes();
        updatedMapNodes = mapNodeInfo.getUpdatedMapNodes();
        this.mapTheme = mapTheme;
        this.typeNameToPlaceStyle = typeNameToPlaceStyle;
    }

    public static String getLatLongFilterStringForPlaceId(List<PlaceSettingsDTO> placeSettings, int placeId, List<MapSummaryExtent> mapSummaryExtents) {
        List<String> columnConditions = new ArrayList<String>();
        PlaceSettingsDTO placeSettingsDTO = placeSettings.get(placeId);
        String latColumn = "\"" + placeSettingsDTO.getLatColumn() + "\"";
        String longColumn = "\"" + placeSettingsDTO.getLongColumn() + "\"";
        columnConditions.add(latColumn + " IS NOT NULL");
        columnConditions.add(longColumn + " IS NOT NULL");

        if (mapSummaryExtents.isEmpty()) {
            columnConditions.add(latColumn + " >= -90");
            columnConditions.add(latColumn + " <= 90");
            columnConditions.add(longColumn + " >= -180");
            columnConditions.add(longColumn + " <= 180");
        } else {
            FilterStringGenerator.checkWithinBoundingBox(latColumn, longColumn, columnConditions, mapSummaryExtents);
        }
        return columnConditions.stream().collect(Collectors.joining(" AND ", "(", ")"));
    }

    public static AugmentedMapNode createMapNode(String vizUuid, Map<Geometry, AugmentedMapNode> mapNodeMapByGeom, Map<Long, AugmentedMapNode> mapNodeMapById, Long nodeId, Geometry geometry) {
        AugmentedMapNode mapNode;
        mapNode = new AugmentedMapNode(vizUuid, nodeId);
        mapNode.setGeometry(geometry);
        mapNodeMapByGeom.put(geometry, mapNode);
        mapNodeMapById.put(mapNode.getNodeId(), mapNode);
        return mapNode;
    }

    public static void setMapNodeTypeAndPlace(AugmentedMapNode mapNode, Integer internalStateId, int placeId, PlaceidTypenameDuple key, Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, DataView dataView) {
        if (!mapNodeByType.containsKey(key)) {
         mapNodeByType.put(key, new TreeSet<>(getMapNodeComparator()));
      }
        mapNodeByType.get(key).add(mapNode);
        mapNode.addInternalStateId(internalStateId, dataView.getNextLinkupId() - 1);
        mapNode.addTypeName(key);
        mapNode.addPlaceId(placeId);
    }

    public static Comparator<MapNode> getMapNodeComparator() {
        if (mapNodeComparator == null) {
            mapNodeComparator = (o1, o2) -> {
                long diff = o1.getNodeId() - o2.getNodeId();
                if (diff > 0) {
                  return 1;
               } else if (diff < 0) {
                  return -1;
               } else {
                  return 0;
               }
            };
        }
        return mapNodeComparator;
    }

    public AugmentedMapNode processMapNode(ResultSet rs, Integer id, int placeId, double latitude, double longitude)
            throws SQLException {
        AugmentedMapNode mapNode = null;
        PlaceidTypenameDuple key = processPlaceTypename(rs, placeId);
        if ((key != null) && (key.getTypename() != null)) {
            mapNode = getMapNode(latitude, longitude);
            updateMapNode(rs, mapNode, id, getInternalStateId(rs), placeId, key);
        }
        return mapNode;
    }

    private PlaceidTypenameDuple processPlaceTypename(ResultSet rs, int placeId) throws SQLException {
        PlaceSettingsDTO placeSettingsDTO = placeSettings.get(placeId);
        String typeColumn = placeSettingsDTO.getTypeColumn();
        String typenameFromRS = null;
        if (typeColumn != null) {
         typenameFromRS = rs.getString(typeColumn);
      }
        return PlaceTypenameProcessor.process(mapSettings, placeId, placeSettingsDTO, mapNodeInfo, mapTheme, typeNameToPlaceStyle, typenameFromRS);
    }

    public Integer getInternalStateId(ResultSet rs) throws SQLException {
        String strId = rs.getString(CacheUtil.INTERNAL_STATEID);
        return Integer.parseInt(strId);
    }

    private void updateMapNode(ResultSet rs, AugmentedMapNode mapNode, Integer id, Integer internalStateId, int placeId, PlaceidTypenameDuple key) throws SQLException, NumberFormatException {
        PlaceSettingsDTO placeSettings = mapSettings.getPlaceSettings().get(placeId);
        updateMapNode(mapNode, id, internalStateId, placeId, key, placeSettings, rs);

        if (shouldGatherStatistics(placeId)) {
         gatherMapNodeStatistics(rs, placeId, key, mapNode);
      }

        if (typenameToIconUrl.get(key) != null) {
            mapNode.setIconUri(key, "id=" + typenameToIconUrl.get(key));
        } else if (placeSettings.getIconColumn() != null) {
            String fieldValue = rs.getString(placeSettings.getIconColumn());
            if ((fieldValue != null) && (fieldValue.trim().length() > 0)) {
               mapNode.setIconUri(key, "id=" + fieldValue);
            }
        }
    }

    private void updateMapNode(AugmentedMapNode mapNode, Integer id, Integer internalStateId, int placeId, PlaceidTypenameDuple key, PlaceSettingsDTO placeSettings, ResultSet rs) throws SQLException {
        setMapNodeTypeAndPlace(mapNode, internalStateId, placeId, key, mapNodeByType, dataView);
        if (placeSettings.getSizeColumn() == null) {
            mapNode.setSize(key, placeSettings.getSize());
        }
        if (mapNode.isCombined()) {
         combinedMapNodes.add(mapNode);
      }
        if (mapNode.isNew() && !newMapNodes.contains(mapNode)) {
            newMapNodes.add(mapNode);
        } else if (mapNode.isUpdated()) {
            updatedMapNodes.add(mapNode);
        }
        mapNode.getRowIds().add(id);

        mapNode.incrementHits();
        for (MapTooltipFieldDTO tooltipField : mapSettings.getTooltipFields().get(placeId)) {
            String fieldValue = rs.getString(tooltipField.getFieldColumn());
            if ((fieldValue != null) && (fieldValue.trim().length() > 0)) {
               mapNode.addDescriptions(tooltipField.getFieldName(), fieldValue);
            }
        }

        if (placeSettings.getLabelColumn() != null) {
            String fieldValue = rs.getString(placeSettings.getLabelColumn());
            if ((fieldValue != null) && (fieldValue.trim().length() > 0)) {
               mapNode.addLabel(fieldValue);
            }
        }
    }

    public boolean shouldGatherStatistics(int placeId) {
        return (mapSettings.getPlaceSettings().get(placeId).getSizeColumn() != null) && (mapSettings.getPlaceSettings().get(placeId).getSizeFunction() != null) && !maxPlaceSizeEqualsMinPlaceSize;
    }

   private void gatherMapNodeStatistics(ResultSet rs, int placeId, PlaceidTypenameDuple key, AugmentedMapNode mapNode) throws SQLException, NumberFormatException {
      PlaceSettingsDTO placeSettings = mapSettings.getPlaceSettings().get(placeId);
      String placeSizeFunction = placeSettings.getSizeFunction();
      String fieldValue = rs.getString(placeSettings.getSizeColumn());

      if (placeSettings.isSizedByDynamicType().booleanValue()) {
         gatherMapNodeTypeStatistics(key, mapNode, placeSizeFunction, fieldValue);
      } else {
         gatherMapNodePlaceStatistics(placeId, mapNode, placeSizeFunction, fieldValue);
      }
   }

    public void gatherMapNodeTypeStatistics(PlaceidTypenameDuple key, AugmentedMapNode mapNode, String placeSizeFunction, String fieldValue) {
        TypeSizeValue typeSizeValue = mapNode.getTypeSizeValue(key);
        if (placeSizeFunction.equals("COUNT")) {
            if (typeSizeValue == null) {
                typeSizeValue = new CountTypeSizeValue();
                mapNode.setTypeSizeValue(key, typeSizeValue);
            }
            ((CountTypeSizeValue) typeSizeValue).incrementCount();
        } else if (placeSizeFunction.equals("COUNT_DIST")) {
            if (typeSizeValue == null) {
                typeSizeValue = new CountDistinctTypeSizeValue();
                mapNode.setTypeSizeValue(key, typeSizeValue);
            }
            ((CountDistinctTypeSizeValue) typeSizeValue).addValue(fieldValue);
        } else if (mapSettings.getPlaceSettings().get(key.getPlaceid()).getSizeColumnNumerical().booleanValue()) {
            try {
                double doubleValue = Double.parseDouble(fieldValue);
                switch (placeSizeFunction) {
                    case "MIN":
                        if (typeSizeValue == null) {
                            typeSizeValue = new MinTypeSizeValue();
                            mapNode.setTypeSizeValue(key, typeSizeValue);
                        }
                        ((MinTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "MAX":
                        if (typeSizeValue == null) {
                            typeSizeValue = new MaxTypeSizeValue();
                            mapNode.setTypeSizeValue(key, typeSizeValue);
                        }
                        ((MaxTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "AVG":
                        if (typeSizeValue == null) {
                            typeSizeValue = new AvgTypeSizeValue();
                            mapNode.setTypeSizeValue(key, typeSizeValue);
                        }
                        ((AvgTypeSizeValue) typeSizeValue).incrementCount();
                        ((AvgTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "SUM":
                        if (typeSizeValue == null) {
                            typeSizeValue = new SumTypeSizeValue();
                            mapNode.setTypeSizeValue(key, typeSizeValue);
                        }
                        ((SumTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "ABS_AVG":
                        if (typeSizeValue == null) {
                            typeSizeValue = new AbsAvgTypeSizeValue();
                            mapNode.setTypeSizeValue(key, typeSizeValue);
                        }
                        ((AbsAvgTypeSizeValue) typeSizeValue).incrementCount();
                        ((AbsAvgTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "ABS_SUM":
                        if (typeSizeValue == null) {
                            typeSizeValue = new AbsSumTypeSizeValue();
                            mapNode.setTypeSizeValue(key, typeSizeValue);
                        }
                        ((AbsSumTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException | NullPointerException ignored) {
            }
        }
    }

    public void gatherMapNodePlaceStatistics(int placeId, AugmentedMapNode mapNode, String placeSizeFunction, String fieldValue) {
        TypeSizeValue typeSizeValue = mapNode.getPlaceSizeValue(placeId);
        if (placeSizeFunction.equals("COUNT")) {
            if (typeSizeValue == null) {
                typeSizeValue = new CountTypeSizeValue();
                mapNode.setPlaceSizeValue(placeId, typeSizeValue);
            }
            if (fieldValue != null) {
                ((CountTypeSizeValue) typeSizeValue).incrementCount();
            }
        } else if (placeSizeFunction.equals("COUNT_DIST")) {
            if (typeSizeValue == null) {
                typeSizeValue = new CountDistinctTypeSizeValue();
                mapNode.setPlaceSizeValue(placeId, typeSizeValue);
            }
            ((CountDistinctTypeSizeValue) typeSizeValue).addValue(fieldValue);
        } else if (mapSettings.getPlaceSettings().get(placeId).getSizeColumnNumerical().booleanValue()) {
            try {
                double doubleValue = Double.parseDouble(fieldValue);
                switch (mapSettings.getPlaceSettings().get(placeId).getSizeFunction()) {
                    case "MIN":
                        if (typeSizeValue == null) {
                            typeSizeValue = new MinTypeSizeValue();
                            mapNode.setPlaceSizeValue(placeId, typeSizeValue);
                        }
                        ((MinTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "MAX":
                        if (typeSizeValue == null) {
                            typeSizeValue = new MaxTypeSizeValue();
                            mapNode.setPlaceSizeValue(placeId, typeSizeValue);
                        }
                        ((MaxTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "AVG":
                        if (typeSizeValue == null) {
                            typeSizeValue = new AvgTypeSizeValue();
                            mapNode.setPlaceSizeValue(placeId, typeSizeValue);
                        }
                        ((AvgTypeSizeValue) typeSizeValue).incrementCount();
                        ((AvgTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "SUM":
                        if (typeSizeValue == null) {
                            typeSizeValue = new SumTypeSizeValue();
                            mapNode.setPlaceSizeValue(placeId, typeSizeValue);
                        }
                        ((SumTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "ABS_AVG":
                        if (typeSizeValue == null) {
                            typeSizeValue = new AbsAvgTypeSizeValue();
                            mapNode.setPlaceSizeValue(placeId, typeSizeValue);
                        }
                        ((AbsAvgTypeSizeValue) typeSizeValue).incrementCount();
                        ((AbsAvgTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    case "ABS_SUM":
                        if (typeSizeValue == null) {
                            typeSizeValue = new AbsSumTypeSizeValue();
                            mapNode.setPlaceSizeValue(placeId, typeSizeValue);
                        }
                        ((AbsSumTypeSizeValue) typeSizeValue).addValue(doubleValue);
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException | NullPointerException ignored) {
            }
        }
    }

    private AugmentedMapNode getMapNode(double latitude, double longitude) {
        Geometry geometry = new Geometry(longitude, latitude);
        geometry.setSummaryLevel(Configuration.getInstance().getMapConfig().getDetailLevel());
        if (mapNodeInfo.getMapByGeometry().containsKey(geometry)) {
         return mapNodeInfo.getMapByGeometry().get(geometry);
      } else {
         return createMapNode(mapViewDefUuid, mapNodeInfo.getMapByGeometry(), mapNodeInfo.getMapById(), UUIDUtil.getUUIDLong(), geometry);
      }
    }

    public boolean isMaxPlaceSizeEqualsMinPlaceSize() {
        return maxPlaceSizeEqualsMinPlaceSize;
    }
}
