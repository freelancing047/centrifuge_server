package csi.server.business.visualization.map.cacheloader.pointcounter;

import static csi.server.business.visualization.map.FilterStringGenerator.generateFilterString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.business.visualization.map.QueryExecutor.ResultSetProcessor;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;
import csi.shared.core.visualization.map.UBox;

public class TrackPointCounterByCountGrid extends AbstractPointCounter {
   private static final Logger LOG = LogManager.getLogger(TrackPointCounterByCountGrid.class);

    private static final String LAT = "lat";
    private static final String LON = "long";
    private static final String IDENTITY_COL = "col";
    private static final String IDENTITY_NAME = "nam";
    private static final String USE_COL = "__2_7__";
    private static final String USE_NAME = "__4_5__";
    private static final int DETAIL_LEVEL = Configuration.getInstance().getMapConfig().getDetailLevel();
    private static int LINK_LIMIT = Configuration.getInstance().getMapConfig().getLinkLimit();

    private DataView dataView;
    private MapViewDef mapViewDef;
    private MapSettingsDTO mapSettings;
    private MapSummaryExtent mapSummaryExtent;

    private TrackCountGrid countGrid;

    public TrackPointCounterByCountGrid(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, UBox uBox) {
        super(mapCacheHandler);
        this.dataView = dataView;
        this.mapViewDef = mapViewDef;
        if (uBox == null) {
            mapSummaryExtent = null;
        } else {
            mapSummaryExtent = uBox.getMapSummaryExtent();
        }
    }

    @Override
   public void calculatePrecision(int startingPrecision) {
        init();

        countGrid = new TrackCountGrid(POINT_LIMIT);
        populateCountGrid();

        precision = DETAIL_LEVEL;
        numPoints = getNumPoints();
        if (numPoints == 0) {
            coarsestPrecision = DETAIL_LEVEL;
        } else {
            if (numPoints < POINT_LIMIT) {
                if (getNumLinks() > LINK_LIMIT) {
                  precision--;
               }
                coarsestPrecision = precision;
            } else {
                precision = startingPrecision;
                getCorrectPrecision();
                if ((coarsestPrecision == DETAIL_LEVEL) && (getNumLinks() > LINK_LIMIT)) {
                  coarsestPrecision--;
               }
            }
        }
    }

    private void init() {
        mapSettings = mapCacheHandler.getMapSettings();
    }

    private void populateCountGrid() {
        QueryExecutor.execute(LOG, generateLatLongQueryForPlaces(), new ResultSetProcessor() {
            @Override
            public void process(ResultSet rs) throws SQLException {
                if (!rs.isBeforeFirst()) {
                    return;
                }
                while (rs.next()) {
                    String[] longitudes = getStringArray(rs.getString(LON));
                    String[] latitudes = getStringArray(rs.getString(LAT));
                    String[] ids = null;
                    String idName = rs.getString(IDENTITY_NAME);
                    if (idName.equals(USE_COL)) {
                        ids = getStringArray(rs.getString(IDENTITY_COL));
                    }
                    if ((longitudes != null) && (latitudes != null)) {
                     for (int i = 0; i < longitudes.length; i++) {
                         if (ids == null) {
                           countGrid.addCoordinate(latitudes[i], longitudes[i], idName);
                        } else {
                           countGrid.addCoordinate(latitudes[i], longitudes[i], ids[i]);
                        }
                     }
                  }
                }
            }

            private String[] getStringArray(String longitudeArray) {
                if (longitudeArray == null) {
                  return null;
               }
                String substring = longitudeArray.substring(1, longitudeArray.length() - 1);
                return substring.split(",");
            }
        });
    }

    @Override
   int getNumPoints() {
        return countGrid.getNumPoints(precision);
    }

    private int getNumLinks() {
        return countGrid.getNumLinks();
    }

    private String generateLatLongQueryForPlaces() {
        List<String> queriesForPlace = new ArrayList<String>();
        int trackId = 0;
        TrackSettingsDTO trackSettingsDTO = mapSettings.getTrackSettings().get(trackId);
        PlaceSettingsDTO placeSettingsDTO = null;
        String place = trackSettingsDTO.getPlace();
        int placeId = 0;
        while (placeId < mapSettings.getPlaceSettings().size()) {
            placeSettingsDTO = mapSettings.getPlaceSettings().get(placeId);
            if (place.equals(placeSettingsDTO.getName())) {
               break;
            }
            placeId++;
        }

        queriesForPlace.add(generateLatLongQueryForTrack(trackSettingsDTO, placeSettingsDTO, placeId));
//		}
        return queriesForPlace.stream().collect(Collectors.joining(" UNION "));
    }

    private String generateLatLongQueryForTrack(TrackSettingsDTO trackSettingsDTO, PlaceSettingsDTO placeSettingsDTO, int placeId) {
        return generateSelectClause(trackSettingsDTO, placeSettingsDTO) + " " + generateFromClause() + " " + generateWhereClause(placeId);
    }

    private String generateSelectClause(TrackSettingsDTO trackSettingsDTO, PlaceSettingsDTO placeSettingsDTO) {
        List<String> columns = new ArrayList<String>();
        columns.add("array_agg(\"" + placeSettingsDTO.getLatColumn() + "\") " + LAT);
        columns.add("array_agg(\"" + placeSettingsDTO.getLongColumn() + "\") " + LON);
        if (trackSettingsDTO.getIdentityName() == null) {
            columns.add("array_agg(\"" + trackSettingsDTO.getIdentityColumn() + "\") AS " + IDENTITY_COL);
            columns.add("'" + USE_COL + "' AS " + IDENTITY_NAME);
        } else {
            columns.add("'" + USE_NAME + "' AS " + IDENTITY_COL);
            columns.add("'" + trackSettingsDTO.getIdentityName().trim() + "' AS " + IDENTITY_NAME);
        }
        return columns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
    }

    private String generateFromClause() {
        return "FROM " + CacheUtil.getQuotedCacheTableName(mapCacheHandler.getDvUuid());
    }

    private String generateWhereClause(int placeId) {
        String filterString = generateFilterString(dataView, mapViewDef, mapSettings, placeId, mapSummaryExtent);
        if (!filterString.isEmpty()) {
            return "WHERE " + filterString;
        } else {
            return "";
        }
    }
}
