package csi.server.business.visualization.map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.helper.DataCacheHelper;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.FilterActionsService;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.impl.spi.PredicateSpi;
import csi.shared.core.visualization.map.MapBundleDefinitionDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class FilterStringGenerator {
   private static final Logger LOG = LogManager.getLogger(FilterStringGenerator.class);

   private DataView dataView;
    private MapViewDef mapViewDef;
    private MapSettingsDTO mapSettings;
    private List<Integer> placeIds;
    private List<MapSummaryExtent> mapSummaryExtents;
    private List<String> filters;
    private String filterString;

    public FilterStringGenerator(DataView dataView, MapViewDef mapViewDef, MapSettingsDTO mapSettings) {
        this.dataView = dataView;
        this.mapViewDef = mapViewDef;
        this.mapSettings = mapSettings;
        mapSummaryExtents = new ArrayList<MapSummaryExtent>();
        placeIds = new ArrayList<Integer>();
    }

    public static String generateFilterString(DataView dataView, MapViewDef mapViewDef, MapSettingsDTO mapSettings) {
        FilterStringGenerator generator = new FilterStringGenerator(dataView, mapViewDef, mapSettings);
        generator.generateFilterString();
        return generator.getFilterString();
    }

    public static String generateFilterString(DataView dataView, MapViewDef mapViewDef, MapSettingsDTO mapSettings, Integer placeId) {
        FilterStringGenerator generator = new FilterStringGenerator(dataView, mapViewDef, mapSettings);
        generator.addPlaceId(placeId);
        generator.generateFilterString();
        return generator.getFilterString();
    }

    public static String generateFilterString(DataView dataView, MapViewDef mapViewDef, MapSettingsDTO mapSettings, Integer sourcePlaceId, Integer destinationPlaceId) {
        FilterStringGenerator generator = new FilterStringGenerator(dataView, mapViewDef, mapSettings);
        generator.addPlaceId(sourcePlaceId);
        generator.addPlaceId(destinationPlaceId);
        generator.generateFilterString();
        return generator.getFilterString();
    }

    public static String generateFilterString(DataView dataView, MapViewDef mapViewDef, MapSettingsDTO mapSettings, Integer placeId, MapSummaryExtent mapSummaryExtent) {
        FilterStringGenerator generator = new FilterStringGenerator(dataView, mapViewDef, mapSettings);
        generator.addPlaceId(placeId);
        if (mapSummaryExtent != null) {
           LOG.info("mapSummaryExtent.getXMax(): " + mapSummaryExtent.getXMax());
           LOG.info("mapSummaryExtent.getXMin(): " + mapSummaryExtent.getXMin());
            if ((mapSummaryExtent.getXMax() - mapSummaryExtent.getXMin()) >= 359.9) {
                MapSummaryExtent fromNeg180toPos180 = new MapSummaryExtent(-180.0, mapSummaryExtent.getYMin(), 180.0, mapSummaryExtent.getYMax());
                generator.addMapSummaryExtent(fromNeg180toPos180);
            } else if (mapSummaryExtent.getXMin() < -180) {
                MapSummaryExtent fromNeg180 = new MapSummaryExtent(-180.0, mapSummaryExtent.getYMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMax());
                MapSummaryExtent toPos180 = new MapSummaryExtent(mapSummaryExtent.getXMin() + 360, mapSummaryExtent.getYMin(), 180.0, mapSummaryExtent.getYMax());
                generator.addMapSummaryExtent(fromNeg180);
                generator.addMapSummaryExtent(toPos180);
            } else if (mapSummaryExtent.getXMax() > 180) {
                MapSummaryExtent fromNeg180 = new MapSummaryExtent(-180.0, mapSummaryExtent.getYMin(), mapSummaryExtent.getXMax() - 360, mapSummaryExtent.getYMax());
                MapSummaryExtent toPos180 = new MapSummaryExtent(mapSummaryExtent.getXMin(), mapSummaryExtent.getYMin(), 180.0, mapSummaryExtent.getYMax());
                generator.addMapSummaryExtent(fromNeg180);
                generator.addMapSummaryExtent(toPos180);
            } else {
               generator.addMapSummaryExtent(mapSummaryExtent);
            }
        }
        generator.generateFilterString();
        return generator.getFilterString();
    }

    public static String getLatLongFilter(PlaceSettingsDTO placeSettingsDTO) {
        List<String> columnConditions = new ArrayList<String>();
        String latColumn = "\"" + placeSettingsDTO.getLatColumn() + "\"";
        String longColumn = "\"" + placeSettingsDTO.getLongColumn() + "\"";
        columnConditions.add(latColumn + " IS NOT NULL");
        columnConditions.add(longColumn + " IS NOT NULL");
        columnConditions.add(latColumn + " >= -90");
        columnConditions.add(latColumn + " <= 90");
        columnConditions.add(longColumn + " >= -180");
        columnConditions.add(longColumn + " <= 180");
        return columnConditions.stream().collect(Collectors.joining(" AND ", "(", ")"));
    }

    public static void checkWithinBoundingBox(String latColumn, String longColumn, List<String> conditionList, List<MapSummaryExtent> mapSummaryExtents) {
        List<String> boundaryConditions = new ArrayList<String>();

        for (MapSummaryExtent mapSummaryExtent : mapSummaryExtents) {
            List<String> latLongConditions = new ArrayList<String>();

            latLongConditions.add(latColumn + " >= " + mapSummaryExtent.getYMin());
            latLongConditions.add(latColumn + " <= " + mapSummaryExtent.getYMax());
            latLongConditions.add(longColumn + " >= " + mapSummaryExtent.getXMin());
            latLongConditions.add(longColumn + " <= " + mapSummaryExtent.getXMax());
            boundaryConditions.add(latLongConditions.stream().collect(Collectors.joining(" AND ", "(", ")")));
        }
        conditionList.add(boundaryConditions.parallelStream().collect(Collectors.joining(" OR ", "(", ")")));
    }

    private void addPlaceId(Integer placeId) {
        placeIds.add(placeId);
    }

    private void addMapSummaryExtent(MapSummaryExtent mapSummaryExtent) {
        mapSummaryExtents.add(mapSummaryExtent);
    }

    public void generateFilterString() {
        init();
        gatherFilters();
        generate();
    }

    private void init() {
        filters = new ArrayList<String>();
    }

    private void gatherFilters() {
        addToFilters(getFilter());
        addToFilters(getBroadcastFilter());
        addToFilters(getBreadcrumbFilter());
        for (int index = 0; index < placeIds.size(); index++) {
            addToFilters(getPlaceIdFilter(index));
        }
    }

    private void addToFilters(String filterString) {
        if ((filterString != null) && !filterString.isEmpty()) {
         filters.add(filterString);
      }
    }

    private String getFilter() {
        Filter filter = mapViewDef.getFilter();
        String retVal = null;
        if (filter != null) {
            List<FilterExpression> expressionList = filter.getFilterDefinition().getFilterExpressions();

            if (!expressionList.isEmpty()) {
                DataCacheHelper cacheHelper = new DataCacheHelper();
                FilterActionsService filterActionsService = cacheHelper.getFilterActionsService();
                CacheTableSource tableSource = filterActionsService.getCacheTableSource(dataView);
                Predicate predicate = filterActionsService.getPredicate(expressionList.get(0), tableSource);
                int howMany = expressionList.size();

                for (int i = 1; i < howMany; i++) {
                  predicate = predicate.and(filterActionsService.getPredicate(expressionList.get(i), tableSource));
                }
                retVal = ((PredicateSpi) predicate).getSQL();
            }
        }
        return retVal;
    }

    private String getBroadcastFilter() {
        BroadcastResult broadcastResult = AbstractBroadcastStorageService.instance().getBroadcast(mapViewDef.getUuid());
        if ((broadcastResult != null) && !broadcastResult.isEmpty()) {
         return DataCacheHelper.buildTableSelectionFilterClause(broadcastResult.getBroadcastFilter(), broadcastResult.isExcludeRows(), dataView);
      } else {
         return null;
      }
    }

    private String getBreadcrumbFilter() {
        List<Crumb> breadCrumb = MapServiceUtil.getBreadcrumb(dataView.getUuid(), mapViewDef.getUuid());
        return getBreadcrumbConditions(breadCrumb).stream().collect(Collectors.joining(" AND "));
    }

   private List<String> getBreadcrumbConditions(List<Crumb> breadCrumb) {
      List<String> conditions = new ArrayList<String>();

      if (mapSettings == null) {
         return conditions;
      }
      int index = 0;
      List<MapBundleDefinitionDTO> mapBundleDefinitions = mapSettings.getMapBundleDefinitions();

      if ((breadCrumb != null) && !breadCrumb.isEmpty()) {
         while (index < breadCrumb.size()) {
            String criterion = breadCrumb.get(index).getCriterion();
            String column = "\"" + mapBundleDefinitions.get(index).getFieldColumn() + "\"";

            if ((criterion == null) || criterion.equalsIgnoreCase("NULL")) {
               conditions.add(column + " IS NULL");
            } else {
               conditions.add(column + " = '" + criterion + "'");
            }
            index++;
         }
      }
      while (index < mapBundleDefinitions.size()) {
         MapBundleDefinitionDTO mapBundleDefinition = mapBundleDefinitions.get(index);

         if (!mapBundleDefinition.isAllowNulls()) {
            String column = "\"" + mapBundleDefinition.getFieldColumn() + "\"";
            conditions.add(column + " IS NOT NULL");
         }
         index++;
      }
      return conditions;
   }

    private String getPlaceIdFilter(int index) {
        String filter = null;
        Integer placeId = placeIds.get(index);
        if (placeId != null) {
         filter = getPlaceIdConditions(placeId).stream().collect(Collectors.joining(" AND "));
      }
        return filter;
    }

    private List<String> getPlaceIdConditions(int placeId) {
        PlaceSettingsDTO placeSettings = mapSettings.getPlaceSettings().get(placeId);
        String latColumn = "\"" + placeSettings.getLatColumn() + "\"";
        String longColumn = "\"" + placeSettings.getLongColumn() + "\"";
        List<String> conditionList = new ArrayList<String>();
        conditionList.add(latColumn + " IS NOT NULL");
        conditionList.add(longColumn + " IS NOT NULL");
        if (mapSummaryExtents.isEmpty()) {
            conditionList.add(latColumn + " >= -90");
            conditionList.add(latColumn + " <= 90");
            conditionList.add(longColumn + " >= -180");
            conditionList.add(longColumn + " <= 180");
        } else {
            checkWithinBoundingBox(latColumn, longColumn, conditionList, mapSummaryExtents);
        }
        return conditionList;
    }

    private void generate() {
        filterString = filters.parallelStream().collect(Collectors.joining(" AND "));
    }

    public String getFilterString() {
        return filterString;
    }
}
