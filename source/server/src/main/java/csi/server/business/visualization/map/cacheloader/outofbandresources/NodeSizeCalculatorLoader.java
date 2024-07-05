package csi.server.business.visualization.map.cacheloader.outofbandresources;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.NodeSizeCalculator;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.MapNodeStatisticsCalculator;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class NodeSizeCalculatorLoader {
    private static final Logger LOG = LogManager.getLogger(NodeSizeCalculatorLoader.class);

    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String PLACE_TYPE = "placeType";
    private static final String MEASURE = "measure";
    private static final String SIZE_COLUMN = "sizeColumn";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private final MapSettingsDTO mapSettingsDTO;
    private DataView dataView;
    private MapViewDef mapViewDef;
    private Map<Integer, Map<String, NodeSizeCalculator>> registry;
    private MapCacheHandler mapCacheHandler;
    private String filterString = null;
    private boolean filterStringCalculated = false;
    private List<MapSummaryExtent> mapSummaryExtents;

    NodeSizeCalculatorLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, List<MapSummaryExtent> mapSummaryExtents) {
        this.mapCacheHandler = mapCacheHandler;
        this.dataView = dataView;
        this.mapViewDef = mapViewDef;
        mapSettingsDTO = mapCacheHandler.getMapSettings();
        this.mapSummaryExtents = mapSummaryExtents;
    }

    void setRegistry(Map<Integer, Map<String, NodeSizeCalculator>> registry) {
        this.registry = registry;
    }

    public void load() {
        for (int placeId = 0; placeId < mapSettingsDTO.getPlaceSettings().size(); placeId++) {
            if (MapNodeStatisticsCalculator.shouldCreateCalculators(placeId, mapCacheHandler.getMapSettings())) {
                createCalculators(placeId);
            }
        }
    }

    private void createCalculators(int placeId) {
        Map<String, NodeSizeCalculator> typenameToCalculator = createTypenameToCalculatorForPlaceId(placeId);
        retrieveDataAndCreateCalculators(placeId, typenameToCalculator);
    }

    private Map<String, NodeSizeCalculator> createTypenameToCalculatorForPlaceId(int placeId) {
        Map<String, NodeSizeCalculator> typenameToCalculator = Maps.newHashMap();
        registry.put(placeId, typenameToCalculator);
        return typenameToCalculator;
    }

    private void retrieveDataAndCreateCalculators(int placeId, Map<String, NodeSizeCalculator> typenameToCalculator) {
        QueryExecutor.execute(LOG, generateQuery(placeId), rs -> createCalculators(typenameToCalculator, rs));
    }

    private String generateQuery(int placeId) {
        return generateSelectClause() + " " + generateFromClause(placeId) + " " + generateGroupByClause();
    }

    private String generateSelectClause() {
        return "SELECT " + PLACE_TYPE + ", MAX(measure) AS " + MAX + ", MIN(measure) AS " + MIN;
    }

    private String generateFromClause(int placeId) {
        return "FROM (" + generateAggregateQuery(placeId) + ") b";
    }

    private String generateGroupByClause() {
        return "GROUP BY " + PLACE_TYPE;
    }

    private String generateAggregateQuery(int placeId) {
        return generateAggregateSelectClause(placeId) + " " + generateAggregateFromClause(placeId) + " GROUP BY " + PLACE_TYPE + ", " + LAT + ", " + LON;
    }

    private String generateAggregateSelectClause(int placeId) {
        List<String> columns = Lists.newArrayList();
        columns.add(PLACE_TYPE);
        String sizeFunction = mapSettingsDTO.getPlaceSettings().get(placeId).getSizeFunction();
        switch (sizeFunction) {
            case "COUNT_DIST":
                sizeFunction = "count (distinct ";
                break;
            case "ABS_SUM":
                sizeFunction = "sum (abs ";
                break;
            case "ABS_AVG":
                sizeFunction = "avg (abs  ";
                break;
            default:
                sizeFunction += "(";
        }
        columns.add(sizeFunction + "(" + SIZE_COLUMN + ")) AS " + MEASURE);
        return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
    }

    private String generateAggregateFromClause(int placeId) {
        return "FROM (" + generateInnerQuery(placeId) + ") a";
    }

    private String generateInnerQuery(int placeId) {
        return generateInnerSelectClause(placeId) + " " + generateInnerFromClause() + " " + generateWhereClause();
    }

    private String generateInnerSelectClause(int placeId) {
        List<String> columns = Lists.newArrayList();
        columns.add("\"" + mapSettingsDTO.getPlaceSettings().get(placeId).getLatColumn() + "\" AS " + LAT);
        columns.add("\"" + mapSettingsDTO.getPlaceSettings().get(placeId).getLongColumn() + "\" AS " + LON);
        columns.add(generatePlaceType(placeId) + " AS " + PLACE_TYPE);
        columns.add("\"" + mapSettingsDTO.getPlaceSettings().get(placeId).getSizeColumn() + "\" AS " + SIZE_COLUMN);
        return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
    }

    private String generatePlaceType(int placeId) {
        if (sizeByDynamicType(placeId) && (mapSettingsDTO.getPlaceSettings().get(placeId).getTypeColumn() != null)) {
            return "\"" + mapSettingsDTO.getPlaceSettings().get(placeId).getTypeColumn() + "\"";
        } else {
            return "'' || ''";
        }

    }

    private boolean sizeByDynamicType(int placeId) {
        return !mapSettingsDTO.isPlaceTypeFixed() && mapSettingsDTO.getPlaceSettings().get(placeId).isSizedByDynamicType();
    }

    private String generateInnerFromClause() {
        return "FROM " + CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid());
    }

    private String generateWhereClause() {
        if (!filterStringCalculated) {
            filterString = mapCacheHandler.generateFilterString(dataView, mapViewDef, mapSummaryExtents);
            filterStringCalculated = true;
        }
        if (filterString == null) {
            return "";
        } else {
            return "WHERE " + filterString;
        }
    }

    private void createCalculators(Map<String, NodeSizeCalculator> typenameToCalculator, ResultSet rs) throws SQLException {
        while (rs.next()) {
            String placeType = rs.getString(PLACE_TYPE);
            double max = rs.getDouble(MAX);
            double min = rs.getDouble(MIN);
            typenameToCalculator.put(placeType, new NodeSizeCalculator(min, max));
        }
    }
}