package csi.server.business.service.map;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;

public class TypeLimitStatusChecker {
   private static final Logger LOG = LogManager.getLogger(TypeLimitStatusChecker.class);

    private static final String COUNT_COL = "c";

    private MapSettingsDTO mapSettingsDTO;
    private String dataViewUuid;
    private String mapViewDefUuid;
    private DataView dataView;
    private MapViewDef mapViewDef;
    private String fromClause;
    private String trackQuery;
    private String placeQuery;

    public TypeLimitStatusChecker(MapSettingsDTO mapSettingsDTO, String dataViewUuid, DataView dataView, String mapViewDefUuid, MapViewDef mapViewDef) {
        this.mapSettingsDTO = mapSettingsDTO;
        this.dataViewUuid = dataViewUuid;
        this.dataView = dataView;
        this.mapViewDefUuid = mapViewDefUuid;
        this.mapViewDef = mapViewDef;
    }

    private void init(String dataViewUuid) {
        fromClause = "FROM " + CacheUtil.getQuotedCacheTableName(dataViewUuid);
    }

    public void check() {
        init(dataViewUuid);
        if ((dataView == null) || (mapViewDef == null))
            return;
        createQueries();
        useQueries();
    }

    private void createQueries() {
        if (mapSettingsDTO.isUseTrackMap()) {
            List<String> trackSelectQueries = new ArrayList<String>();
            List<String> placeSelectQueries = new ArrayList<String>();
            int trackId = 0;
            TrackSettingsDTO trackSettingsDTO = mapSettingsDTO.getTrackSettings().get(trackId);
            PlaceSettingsDTO placeSettingsDTO = null;
            String place = trackSettingsDTO.getPlace();
            int placeId = 0;
            while (placeId < mapSettingsDTO.getPlaceSettings().size()) {
                placeSettingsDTO = mapSettingsDTO.getPlaceSettings().get(placeId);
                if (placeSettingsDTO.getName().equals(place))
                    break;
                placeId++;
            }
            collectTrackSelectQuery(trackSelectQueries, trackSettingsDTO, placeSettingsDTO);
            trackQuery = "SELECT " + COUNT_COL + " FROM (" + trackSelectQueries.stream().collect(Collectors.joining(" UNION ")) + ") a";
            if (placeSettingsDTO != null) {
                collectPlaceSelectQuery(placeSelectQueries, placeSettingsDTO);
                placeQuery = "SELECT " + COUNT_COL + " FROM (" + placeSelectQueries.stream().collect(Collectors.joining(" UNION ")) + ") a";
            }
        } else if (!mapSettingsDTO.isUseHeatMap() && !mapSettingsDTO.isBundleUsed()) {
            List<String> placeSelectQueries = new ArrayList<String>();
            for (int placeId = 0; placeId < mapSettingsDTO.getPlaceSettings().size(); placeId++) {
                PlaceSettingsDTO placeSettingsDTO = mapSettingsDTO.getPlaceSettings().get(placeId);
                collectPlaceSelectQuery(placeSelectQueries, placeSettingsDTO);
            }
            placeQuery = "SELECT " + COUNT_COL + " FROM (" + placeSelectQueries.stream().collect(Collectors.joining(" UNION ")) + ") a";
        }
    }

    private void collectTrackSelectQuery(List<String> trackSelectQueries, TrackSettingsDTO trackSettingsDTO, PlaceSettingsDTO placeSettingsDTO) {
        String selectClause;
        if (trackSettingsDTO.getIdentityColumn() != null) {
            selectClause = "SELECT COUNT(DISTINCT ";
            selectClause += "\"" + trackSettingsDTO.getIdentityColumn() + "\"";
            selectClause += ") AS " + COUNT_COL;
            String whereClause = getWhereClauseForTrack(trackSettingsDTO, placeSettingsDTO);
            trackSelectQueries.add(selectClause + " " + fromClause + " " + whereClause);
        } else {
            selectClause = "SELECT 1 AS " + COUNT_COL;
            trackSelectQueries.add(selectClause + " " + fromClause);
        }
    }

    private String getWhereClauseForTrack(TrackSettingsDTO trackSettingsDTO, PlaceSettingsDTO placeSettingsDTO) {
        String whereClause = getWhereClauseForPlace(placeSettingsDTO);
        whereClause += " AND \"" + trackSettingsDTO.getSequenceColumn() + "\" IS NOT NULL";
        return whereClause;
    }

    private void collectPlaceSelectQuery(List<String> placeSelectQueries, PlaceSettingsDTO placeSettingsDTO) {
        String selectClause;
        if (placeSettingsDTO.getTypeColumn() != null) {
            selectClause = "SELECT COUNT(DISTINCT ";
            selectClause += "\"" + placeSettingsDTO.getTypeColumn() + "\"";
            if (placeSettingsDTO.isIncludeNullType()) {
                selectClause += " || \' \'";
            }
            selectClause += ") AS " + COUNT_COL;
            String whereClause = getWhereClauseForPlace(placeSettingsDTO);
            placeSelectQueries.add(selectClause + " " + fromClause + " " + whereClause);
        } else {
            selectClause = "SELECT 1 AS " + COUNT_COL;
            placeSelectQueries.add(selectClause + " " + fromClause);
        }
    }

    private String getWhereClauseForPlace(PlaceSettingsDTO placeSettingsDTO) {
        String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef, mapSettingsDTO);
        String latLongFilterString = FilterStringGenerator.getLatLongFilter(placeSettingsDTO);
        if ((filterString == null) || (filterString.trim().length() == 0))
            return "WHERE " + latLongFilterString;
        else
            return "WHERE " + filterString + " AND " + latLongFilterString;
    }

    private void useQueries() {
        if (trackQuery != null) {
            QueryExecutor.execute(LOG, trackQuery, (ResultSet rs) -> {
                if (rs.next()) {
                    int trackCount = rs.getInt(1);
                    if (trackCount > Configuration.getInstance().getMapConfig().getTypeLimit()) {
                        MapCacheUtil.setTrackTypeLimitReached(mapViewDefUuid, true);
                    }
                }
            });
        }
        if (placeQuery != null) {
            QueryExecutor.execute(LOG, placeQuery, (ResultSet rs) -> {
                if (rs.next()) {
                    int trackCount = rs.getInt(1);
                    if (trackCount > Configuration.getInstance().getMapConfig().getTypeLimit()) {
                        MapCacheUtil.setPlaceTypeLimitReached(mapViewDefUuid, true);
                    }
                }
            });
        }
    }
}
