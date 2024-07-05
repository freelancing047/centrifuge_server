package csi.server.business.service.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class MapPlaceCounter {
   private static final Logger LOG = LogManager.getLogger(MapPlaceCounter.class);

	private static final String LAT = "lat";
	private static final String LON = "long";

	private String dataViewUuid;
	private DataView dataView;
	private String mapViewDefUuid;
	private MapViewDef mapViewDef;
	private MapSettingsDTO mapSettings;
	private MapSummaryExtent mapSummaryExtent;

	private int count;
    int precision = Configuration.getInstance().getMapConfig().getDetailLevel();

	public MapPlaceCounter(String dataViewUuid, DataView dataView, String mapViewDefUuid, MapViewDef mapViewDef,
			MapSettingsDTO mapSettings, MapSummaryExtent mapSummaryExtent) {
		this.dataViewUuid = dataViewUuid;
		this.dataView = dataView;

		this.mapViewDefUuid = mapViewDefUuid;
		this.mapViewDef = mapViewDef;
		this.mapSettings = mapSettings;
		this.mapSummaryExtent = mapSummaryExtent;
	}

	public void load() throws CentrifugeException {
		QueryExecutor.execute(LOG, generateQuery(), new QueryExecutor.ResultSetProcessor() {
			@Override
			public void process(ResultSet rs) throws SQLException {
				rs.next();
				count = rs.getInt(1);
			}
		});
	}

	private String generateQuery() {
        String selectClause = "SELECT COUNT(1)";
		String fromClause = generateFromClause();
		return selectClause + " " + fromClause;
	}

	private String generateFromClause() {
		List<String> queriesForPlace = new ArrayList<String>();
		for (int placeid = 0; placeid < mapSettings.getPlaceSettings().size(); placeid++)
			queriesForPlace.add(generateLatLongQueryForPlace(placeid));
		return "FROM (" + queriesForPlace.stream().collect(Collectors.joining(" UNION ")) + ") as a";
	}

	private String generateLatLongQueryForPlace(int placeid) {
		return generateSelectClauseForPlace(placeid) + " " + generateFromClauseForPlace() + " "
				+ generateWhereClauseForPlace(placeid);
	}

	private String generateSelectClauseForPlace(int placeid) {
		List<String> columns = new ArrayList<String>();
		columns.add("trunc(\"" + mapSettings.getPlaceSettings().get(placeid).getLatColumn() + "\"::numeric, "+precision+") " + LAT);
		columns.add("trunc(\"" + mapSettings.getPlaceSettings().get(placeid).getLongColumn() + "\"::numeric, "+precision+") " + LON);
		return "SELECT DISTINCT " + columns.stream().collect(Collectors.joining(", "));
	}

	private String generateFromClauseForPlace() {
		return "FROM " + CacheUtil.getQuotedCacheTableName(dataViewUuid);
	}

	private String generateWhereClauseForPlace(int placeid) {
		String filterString = FilterStringGenerator.generateFilterString(dataView, mapViewDef,
                mapSettings, placeid, mapSummaryExtent);
		if (!filterString.isEmpty())
			return "WHERE " + filterString;
		else
			return "";
	}

	public int getCount() {
		return count;
	}

}
