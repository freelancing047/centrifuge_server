package csi.server.business.service.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.BundleMapNode;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapBundleDefinitionDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;

public class NumPointCounter {
    private String dataViewUuid;
    private DataView dataView;
    private Long id;
    private String mapViewDefUuid;
    private MapViewDef mapViewDef;
    private MapSettingsDTO mapSettings;
    private List<MapBundleDefinitionDTO> mapBundleDefinitions;
    private int breadcrumbSize;
    private String criterion = null;
    private Integer numPoints = null;

    public NumPointCounter(String dataViewUuid, String mapViewDefUuid, Long id) {
        this.dataViewUuid = dataViewUuid;
        this.mapViewDefUuid = mapViewDefUuid;
        this.id = id;
    }

   public void calculate() {
      init();

      if (criterion != null) {
         proceed();
      }
   }

    private void init() {
        initFilterInfo();
        initCriterionInfo();
        numPoints = null;
    }

    private void initFilterInfo() {
        dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
        mapViewDef = (MapViewDef) dataView.getMeta().getModelDef().findVisualizationByUuid(mapViewDefUuid);
        mapSettings = MapCacheUtil.getMapSettings(mapViewDefUuid);
    }

    private void initCriterionInfo() {
        mapBundleDefinitions = mapSettings.getMapBundleDefinitions();
        initBreadcrumbSize();
        initCriterion();
    }

    private void initBreadcrumbSize() {
        breadcrumbSize = MapServiceUtil.getBreadcrumb(dataViewUuid, mapViewDefUuid).size();
    }

    private void initCriterion() {
        Map<Long, AugmentedMapNode> mapNodeMap = MapCacheUtil.getMapNodeByIdMap(mapViewDefUuid);
        AugmentedMapNode mapNode = mapNodeMap.get(id);
        BundleMapNode bundleMapNode = (BundleMapNode) mapNode;
        if (bundleMapNode != null) {
         criterion = bundleMapNode.getBundleValue();
      }
    }

    private void proceed() {
        QueryExecutor.execute(null, getCountPointsQuery(), rs -> {
            if (rs.next()) {
               numPoints = rs.getInt(1);
            }
        });
    }

    private String getCountPointsQuery() {
        return "SELECT COUNT(DISTINCT POINT) FROM (" + getInnerQuery() + ") a";
    }

   private String getInnerQuery() {
      List<String> selectionQueries = new ArrayList<String>();
      int howMany = mapSettings.getPlaceSettings().size();

      for (int placeId = 0; placeId < howMany; placeId++) {
         selectionQueries.add(getSelectionQueryForPlace(placeId));
      }
      return selectionQueries.stream().collect(Collectors.joining(" UNION "));
   }

    private String getSelectionQueryForPlace(int placeId) {
        return getSelectClause(placeId) + " " + getFromClause() + " " + getWhereClause(placeId);
    }

    private String getSelectClause(int placeId) {
        return "SELECT " + "\"" + mapSettings.getPlaceSettings().get(placeId).getLatColumn() + "\"" + " || ',' || " + "\""
                + mapSettings.getPlaceSettings().get(placeId).getLongColumn() + "\"" + " POINT";
    }

    private String getFromClause() {
        return "FROM " + CacheUtil.getQuotedCacheTableName(dataViewUuid);
    }

   private String getWhereClause(int placeId) {
      String result = "";
      List<String> conditions = new ArrayList<String>();

      addToConditions(conditions, FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettings, placeId));
      addToConditions(conditions, getColumnCondition());

      if (!conditions.isEmpty()) {
         result = conditions.stream().collect(Collectors.joining(" AND ", "WHERE ", ""));
      }
      return result;
   }

    private void addToConditions(List<String> conditions, String condition) {
        if (condition.length() > 0) {
         conditions.add(condition);
      }
    }

   private String getColumnCondition() {
      return ((criterion == null) || criterion.equalsIgnoreCase("NULL"))
                ? getColumnIsNullCondition()
                : getColumnIsCriterionCondition();
   }

    private String getColumnIsNullCondition() {
        return "\"" + mapBundleDefinitions.get(breadcrumbSize).getFieldColumn() + "\" IS NULL";
    }

    private String getColumnIsCriterionCondition() {
        return "\"" + mapBundleDefinitions.get(breadcrumbSize).getFieldColumn() + "\" = '" + criterion + "'";
    }

    public Integer getNumPoints() {
        return numPoints;
    }
}
