package csi.server.business.visualization.map.cacheloader.outofbandresources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.LinkGeometryPlus;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.AssociationSettingsDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class MapLinksLoader {
   private static final Logger LOG = LogManager.getLogger(MapLinksLoader.class);

   private static final String ID = "id";
    private static final String LAT1 = "lat1";
    private static final String LON1 = "lon1";
    private static final String LAT2 = "lat2";
    private static final String LON2 = "lon2";
    private static final String ROW_IDS = "rowIds";

    private String dataViewUuid;
    private DataView dataView;
    private MapViewDef mapViewDef;
    private MapSettingsDTO mapSettings;

    private Integer sourcePlaceId;
    private Integer destinationPlaceId;

    private Set<LinkGeometryPlus> links;
    private String sourceLatColumn;
    private String sourceLonColumn;
    private String destinationLatColumn;
    private String destinationLonColumn;

    MapLinksLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef) {
        dataViewUuid = mapCacheHandler.getDvUuid();
        mapSettings = mapCacheHandler.getMapSettings();
        this.dataView = dataView;
        this.mapViewDef = mapViewDef;
    }

    public void setLinks(Set<LinkGeometryPlus> links) {
        this.links = links;
    }

    public void load() {
        if (proceed()) {
            QueryExecutor.execute(LOG, generateOuterQuery(), rs -> {
               int detailLevel = Configuration.getInstance().getMapConfig().getDetailLevel();

               while (rs.next()) {
                    Integer associationId = rs.getInt(ID);
                    Double lat1 = rs.getDouble(LAT1);
                    Double lon1 = rs.getDouble(LON1);
                    Double lat2 = rs.getDouble(LAT2);
                    Double lon2 = rs.getDouble(LON2);
                    String[] rowIds = getStringArray(rs.getString(ROW_IDS));
                    Geometry geom1 = new Geometry(lon1, lat1);
                    geom1.setSummaryLevel(detailLevel);
                    Geometry geom2 = new Geometry(lon2, lat2);
                    geom2.setSummaryLevel(detailLevel);
                    LinkGeometryPlus link = new LinkGeometryPlus(associationId, geom1, geom2);
                    for (String s : rowIds) {
                        try {
                            int rowId = Integer.parseInt(s);
                            link.addRowId(rowId);
                        } catch (NumberFormatException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                    links.add(link);
                }
            });
        }
    }

    private String[] getStringArray(String longitudeArray) {
        if (longitudeArray == null) {
         return null;
      }
        return longitudeArray.substring(1, longitudeArray.length() - 1).split(",");
    }

    private boolean proceed() {
        return !mapSettings.getAssociationSettings().isEmpty();
    }

    private String generateOuterQuery() {
        String selectClause = generateOuterSelectClause();
        String fromClause = generateOuterFromClause();
        return selectClause + " " + fromClause;
    }

    private String generateOuterSelectClause() {
        List<String> columns = new ArrayList<String>();
        columns.add(ID);
        columns.add(LAT1);
        columns.add(LON1);
        columns.add(LAT2);
        columns.add(LON2);
        columns.add(ROW_IDS);
        return "SELECT " + columns.stream().collect(Collectors.joining(", "));
    }

    private String generateOuterFromClause() {
        List<String> innerQueries = generateInnerQueries();
        return "FROM (" + innerQueries.stream().collect(Collectors.joining(" UNION ")) + ") a";
    }

    private List<String> generateInnerQueries() {
        List<String> innerQueries = new ArrayList<String>();
        for (int index = 0; index < mapSettings.getAssociationSettings().size(); index++) {
            String selectClause = getInnerSelectClause(index);
            String fromClause = getInnerFromClause();
            String whereClause = getInnerWhereClause();
            String groupByClause = getInnerGroupByClause();
            innerQueries.add(selectClause + " " + fromClause + " " + whereClause + " " + groupByClause);
        }
        return innerQueries;
    }

    private String getInnerSelectClause(int index) {
        List<String> columns = new ArrayList<String>();
        AssociationSettingsDTO associationSettings = mapSettings.getAssociationSettings().get(index);
        String associationSource = associationSettings.getSource();
        sourcePlaceId = getPlaceId(associationSource);
        columns.add(index + " " + ID);
        PlaceSettingsDTO sourcePlaceSettings = mapSettings.getPlaceSettings().get(sourcePlaceId);
        sourceLatColumn = "\"" + sourcePlaceSettings.getLatColumn() + "\"";
        sourceLonColumn = "\"" + sourcePlaceSettings.getLongColumn() + "\"";
        columns.add(sourceLatColumn + " " + LAT1);
        columns.add(sourceLonColumn + " " + LON1);
        String associationDestination = associationSettings.getDestination();
        destinationPlaceId = getPlaceId(associationDestination);
        PlaceSettingsDTO destinationPlaceSettings = mapSettings.getPlaceSettings().get(destinationPlaceId);
        destinationLatColumn = "\"" + destinationPlaceSettings.getLatColumn() + "\"";
        destinationLonColumn = "\"" + destinationPlaceSettings.getLongColumn() + "\"";
        columns.add(destinationLatColumn + " " + LAT2);
        columns.add(destinationLonColumn + " " + LON2);
        columns.add("array_agg(\"" + CacheUtil.INTERNAL_ID_NAME + "\") " + ROW_IDS);
        return "SELECT DISTINCT " + columns.stream().collect(Collectors.joining(", "));
    }

    private Integer getPlaceId(String name) {
        for (int placeId = 0; placeId < mapSettings.getPlaceSettings().size(); placeId++) {
            PlaceSettingsDTO settings = mapSettings.getPlaceSettings().get(placeId);
            if (settings.getName().equals(name)) {
               return placeId;
            }
        }
        return null;
    }

    private String getInnerFromClause() {
        return "FROM " + CacheUtil.getQuotedCacheTableName(dataViewUuid);
    }

    private String getInnerWhereClause() {
        String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef,
                mapSettings, sourcePlaceId, destinationPlaceId);
        if (!filterString.isEmpty()) {
         return "WHERE " + filterString;
      } else {
         return "";
      }
    }

    private String getInnerGroupByClause() {
        List<String> columns = new ArrayList<String>();
        columns.add(sourceLatColumn);
        columns.add(sourceLonColumn);
        columns.add(destinationLatColumn);
        columns.add(destinationLonColumn);
        return "GROUP BY " + columns.stream().collect(Collectors.joining(", "));
    }

}
