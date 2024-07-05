package csi.server.business.service.export.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.MapSummaryQueryBuilder;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Created by Ivan on 7/20/2017.
 */

public class MapCSVWriter implements CsvWriter {
   private static final Logger LOG = LogManager.getLogger(MapSummaryQueryBuilder.class);

	private static String PLACENAME = "Placename";
	private static String TYPENAME = "Typename";
	private static String LAT = "Latitude";
	private static String LON = "Longitude";
	private static String LABEl = "Lbl";

	private static String SIZE = "Size";

	private String dvUuid;
	private MapViewDef visualizationDef;
	private boolean useSelectionOnly;

	private DataView dataView;
	private String mapViewDefUuid;
	private AbstractMapSelection selection;
	private MapSettingsDTO mapSettings;

	public MapCSVWriter(String dvUuid, MapViewDef visualizationDef, boolean useSelectionOnly) {
		this.dvUuid = dvUuid;
		this.visualizationDef = visualizationDef;
		this.useSelectionOnly = useSelectionOnly;
	}

	@Override
	public void writeCsv(File fileToWrite) {
		init();
		QueryExecutor.execute(LOG, getQuery(), new QueryExecutor.ResultSetProcessor() {
			@Override
			public void process(ResultSet rs) throws SQLException {
				try (CSVWriter csvWriter = new CSVWriter(new FileWriter(fileToWrite))) {
					csvWriter.writeNext(new String[] { PLACENAME, TYPENAME, LAT, LON, SIZE, "Label"});
					while (rs.next()) {
						String[] row = createRow(rs);
						if (row != null) {
                     csvWriter.writeNext(row);
                  }
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}


			private final String NULL_LAT_LONG_STRING = "NULL";

			private String[] createRow(ResultSet rs) throws SQLException {
			    // this will replace empty string on the lat long with NULL - or should we ignore that row all together?? // meeting.
				String latStringVal = rs.getString(LAT) == null ? NULL_LAT_LONG_STRING : rs.getString(LAT);
				String lonStringVal = rs.getString(LON) == null ? NULL_LAT_LONG_STRING : rs.getString(LON);
				if (!useSelectionOnly || inSelection(latStringVal, lonStringVal)) {
					String[] row = new String[6];
					row[0] = rs.getString(PLACENAME);
					row[1] = rs.getString(TYPENAME);
					row[2] = latStringVal;
					row[3] = lonStringVal;
					row[4] = rs.getString(SIZE);
                    row[5] = rs.getString(LABEl);
					return row;
				}
				return null;
			}

			private boolean inSelection(String latStringVal, String lonStringVal) {
				if ((latStringVal == null) || (lonStringVal == null)) {
               return false;
            }
				double latitude = Double.parseDouble(latStringVal);
				double longitude = Double.parseDouble(lonStringVal);
				Geometry geometry = new Geometry(longitude, latitude);
				geometry.setSummaryLevel(Configuration.getInstance().getMapConfig().getDetailLevel());
				return selection.containsGeometry(geometry);
			}
		});
	}

	private void init() {
		dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
		mapViewDefUuid = visualizationDef.getUuid();
		if (useSelectionOnly) {
         selection = MapServiceUtil.getMapSelection(dvUuid, mapViewDefUuid);
      }
		mapSettings = MapCacheUtil.getMapSettings(mapViewDefUuid);
	}

	private String getQuery() {
        // individual place queries
        List<String> preUnionQueries = new ArrayList<String>();


        String a = "a";
        String localname = "";
        for(PlaceSettingsDTO placeSettings : mapSettings.getPlaceSettings()){
            localname += a;
            List<String> columns = new ArrayList<String>();
            columns.add(PLACENAME);
            columns.add(TYPENAME);
            columns.add(LAT);
            columns.add(LON);
            columns.add(LABEl);

            if (placeSettings.isSizedByDynamicType().booleanValue()) {
                columns.add("sum(" + SIZE + ") OVER ( PARTITION BY " + PLACENAME + ", " + LAT + ", " + LON + " ORDER BY " + PLACENAME + ", " + LAT + ", " + LON+ ") " + SIZE);
            } else {
                columns.add(SIZE);
            }

            String innerQuery = getPlaceQuery(placeSettings);
            String q = "SELECT " + columns.stream().collect(Collectors.joining(", "));

            q += " FROM ( " + innerQuery + ") " + localname;

            preUnionQueries.add(q);
        }
        return preUnionQueries.stream().collect(Collectors.joining(" UNION "));
	}

	private String getSelectClauseForQuery() {
		List<String> columns = new ArrayList<String>();
		columns.add(PLACENAME);
		columns.add(TYPENAME);
		columns.add(LAT);
		columns.add(LON);
		columns.add(LABEl);
		columns.add(SIZE);
		return "SELECT " + columns.stream().collect(Collectors.joining(", "));
	}

	private String getFromClauseForQuery() {
		return "FROM (" + getInnerQuery() + ") a";
	}

	private String getInnerQuery() {
		List<String> placeQueries = new ArrayList<String>();
		for (PlaceSettingsDTO placeSettings : mapSettings.getPlaceSettings()) {
         placeQueries.add(getPlaceQuery(placeSettings));
      }
		return placeQueries.stream().collect(Collectors.joining(" UNION "));
	}

	private String getPlaceQuery(PlaceSettingsDTO placeSettings) {
		PlaceQueryGenerator generator = new PlaceQueryGenerator(placeSettings);
		generator.generate();
		return generator.getPlaceQuery();
	}

	class PlaceQueryGenerator {
		private PlaceSettingsDTO placeSettings;

		private String typeNameColumn;
		private String latColumn;
		private String lonColumn;

		private String placeQuery;
        int precision = Configuration.getInstance().getMapConfig().getDetailLevel();

		public PlaceQueryGenerator(PlaceSettingsDTO placeSettings) {
			this.placeSettings = placeSettings;
		}

		public void generate() {
			init();
			placeQuery = getSelectClause() + " " + getFromClause() + " " + getWhereClause() + " " + getGroupByClause();
		}

		public void init() {
			typeNameColumn = placeSettings.getTypeColumn() == null ? "\'" + placeSettings.getTypeName()+ "\'" : getCorrectColumn(placeSettings.getTypeColumn());
			latColumn = getCorrectColumn(placeSettings.getLatColumn());
			lonColumn = getCorrectColumn(placeSettings.getLongColumn());
		}

		private String getCorrectColumn(String orginalColumnString) {
			if (orginalColumnString == null) {
            return "'' || ''";
         } else {
            return "\"" + orginalColumnString + "\"";
         }
		}

		private String getSelectClause() {
			List<String> selectColumns = new ArrayList<String>();
			selectColumns.add("'" + placeSettings.getName() + "' " + PLACENAME);
			selectColumns.add(typeNameColumn + " " + TYPENAME);

            selectColumns.add(latColumn + "::Numeric " + LAT);
            selectColumns.add(lonColumn + "::Numeric " + LON);

            selectColumns.add("array_agg(DISTINCT "+ getCorrectColumn(placeSettings.getLabelColumn())+ "::varchar) " + LABEl );

            selectColumns.add(getSizeColumn(placeSettings) + " " + SIZE);
            return selectColumns.stream().collect(Collectors.joining(", ", "SELECT ", ""));
		}

		private String getSizeColumn(PlaceSettingsDTO placeSettings) {
		   return ((placeSettings.getSizeColumn() != null) && placeSettings.getSizeColumnNumerical().booleanValue())
		             ? placeSettings.getSizeFunction() + "(\"" + placeSettings.getSizeColumn() + "\")"
                   : "COUNT(*)";
		}

		private String getFromClause() {
			return "FROM " + CacheUtil.getQuotedCacheTableName(dvUuid);
		}

		private String getWhereClause() {

			String filter = FilterStringGenerator.generateFilterString(dataView, visualizationDef,
                    mapSettings);
			if ((filter != null) && (filter.length() > 0)) {
            return "WHERE " + getValidateLatLongClause() + " AND " + filter;
         } else {
            return "WHERE " + getValidateLatLongClause();
         }
		}

		private String getValidateLatLongClause(){
            return "(" + latColumn + " IS NOT NULL AND " + lonColumn + " IS NOT NULL AND " + latColumn + " >= -90 AND " + latColumn + " <= 90 AND" + lonColumn + " >= -180 AND " + lonColumn + " <=180)";
        }

		private String getGroupByClause() {
			List<String> groupByColumns = new ArrayList<String>();
            groupByColumns.add(PLACENAME);
            groupByColumns.add(TYPENAME);
			groupByColumns.add(latColumn);
			groupByColumns.add(lonColumn);
         return groupByColumns.stream().collect(Collectors.joining(", ", "GROUP BY ", ""));
		}

		public String getPlaceQuery() {
			return placeQuery;
		}
	}
}
