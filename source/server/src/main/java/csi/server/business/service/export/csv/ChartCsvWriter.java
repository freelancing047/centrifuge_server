package csi.server.business.service.export.csv;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Throwables;
import com.mongodb.DBObject;

import csi.server.business.selection.torows.SelectionToRowsConverter;
import csi.server.business.selection.torows.SelectionToRowsCoverterFactory;
import csi.server.business.service.chart.ChartActionsServiceUtil;
import csi.server.business.service.chart.ChartTableGenerator;
import csi.server.business.service.chart.TableResult;
import csi.server.business.service.chart.storage.AbstractChartStorageService;
import csi.server.business.service.chart.storage.ChartStorage;
import csi.server.business.service.chart.storage.postgres.DataToTableResultTransformer;
import csi.server.business.service.chart.storage.postgres.SelectionToTableResultTransformer;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.dao.CsiPersistenceManager;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Writes chart data as a CSV
 * @author Centrifuge Systems, Inc.
 */
public class ChartCsvWriter implements CsvWriter {

    private String dvUuid;
    private DrillChartViewDef visualizationDef;
    private boolean useSelectionOnly;

    public ChartCsvWriter(String dvUuid, DrillChartViewDef visualizationDef, boolean useSelectionOnly) {
        this.dvUuid = dvUuid;
        this.visualizationDef = visualizationDef;
        this.useSelectionOnly = useSelectionOnly;
    }

	@Override
	public void writeCsv(File fileToWrite) {
		List<String> headers = new ArrayList<String>();
		ChartSettings chartSettings = visualizationDef.getChartSettings();
		List<CategoryDefinition> categoryDefinitions = chartSettings.getCategoryDefinitions();
		headers.add(categoryDefinitions.get(categoryDefinitions.size() - 1).getComposedName());
		List<MeasureDefinition> measureDefinitions = chartSettings.getMeasureDefinitions();
		if (measureDefinitions.isEmpty()) {
			headers.add(ChartActionsServiceUtil.COUNT_STAR_MEASURE_NAME);
		} else {
			for (MeasureDefinition measureDefinition : measureDefinitions) {
				headers.add(measureDefinition.getComposedName());
			}
		}


		AbstractChartStorageService storageService = AbstractChartStorageService.instance();
        int drillKey = storageService.createDrillKey(visualizationDef.getDrillSelection().getCategories());
        boolean isCached = storageService.hasVisualizationDataAt(visualizationDef.getUuid(), drillKey);
        TableResult chartTable = null;
        if (isCached) {
            ChartStorage chartStorage = storageService.getChartStorage(visualizationDef.getUuid(), drillKey);
            DBObject storedTable = (DBObject) chartStorage.getResult();
            if(useSelectionOnly) {
                SelectionToTableResultTransformer transformer = new SelectionToTableResultTransformer();
                transformer.setSelection(visualizationDef.getSelection());
                chartTable = transformer.apply(storedTable);
            } else {
                DataToTableResultTransformer transformer = new DataToTableResultTransformer();
                chartTable = transformer.apply(storedTable);
            }
        } else {
            DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
            ChartTableGenerator chartTableGenerator = new ChartTableGenerator(visualizationDef, dataView, visualizationDef.getDrillSelection().getCategories());
            if (useSelectionOnly) {
                SelectionToRowsConverter selectionToRowsConverter = new SelectionToRowsCoverterFactory(dataView, visualizationDef).create();
                Set<Integer> intRowIds = selectionToRowsConverter.convertToRows(visualizationDef.getSelection(), false);
                List<Number> rowIds = new ArrayList<Number>();
                for (Integer rowId : intRowIds) {
                    rowIds.add(rowId);
                }
                chartTableGenerator.setRowIdsToFilterBy(rowIds);
            }
            chartTable = chartTableGenerator.getChartTable();

        }



		List<List<String>> dataLines = new ArrayList<List<String>>();
		int row = 0;
		while (row < chartTable.getRowCount()) {
			List<String> dataLine = new ArrayList<String>();
			dataLine.add(chartTable.getCategories().get(row));
			int col = 0;
			while (col < chartTable.getDimensionValues().size()) {
				Number number = chartTable.getDimensionValues().get(col).get(row);
				if(number == null) {
				    dataLine.add("");
				} else {
				    dataLine.add(number.toString());
				}
				col++;
			}
			dataLines.add(dataLine);
			row++;
		}
//		try {
//			DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
//			connection = CsiPersistenceManager.getCacheConnection();
//			ChartQueryBuilder qb = new ChartQueryBuilder();
//			List<String> categoryDrills = visualizationDef.getDrillSelection().getCategories();
//			qb.setCategoryDrills(visualizationDef.getDrillSelection().getCategories());
//			qb.setViewDef(visualizationDef);
//			qb.setDataView(dataView);
//			qb.setSqlFactory(new SQLFactoryImpl());
//			qb.setFilterActionsService(new FilterActionsService());
//
//			if (useSelectionOnly) {
//				SelectionToRowsConverter selectionToRowsConverter = new SelectionToRowsCoverterFactory(dataView, visualizationDef).create();
//				Set<Integer> intRowIds = selectionToRowsConverter.convertToRows(visualizationDef.getSelection(), false);
//				List<Number> rowIds = new ArrayList<Number>();
//				for (Integer rowId : intRowIds) {
//					rowIds.add(rowId);
//				}
//				qb.setRowIdsToFilterBy(rowIds);
//			}
//
//			int drillDimensionsCount = categoryDrills.size();
//			SelectResultSet resultSet = qb.getQuery().execute();
//
//			int categoryIndex = drillDimensionsCount;
//			for (SelectResultRow row : resultSet) {
//				Object val = row.getValue(categoryIndex);
//				String valstr = ChartActionsService.getParsedString(val);
//				boolean valNull = false;
//				if (valstr.equals("null")) {
//					valNull = true;
//				}
//
//				if (valNull) {
//					continue;
//				}
//				List<String> dataLine = new ArrayList<String>();
//				dataLine.add(valstr);
//
//				for (int index = 0; index < resultSet.getColumnCount() - 1 - drillDimensionsCount; index++) {
//					Number value = row.getValue(categoryIndex + index + 1);
//					if (value.doubleValue() - Math.floor(value.doubleValue()) != 0) {
//						dataLine.add(String.valueOf(Math.round(value.doubleValue() * 100) / 100.0));
//					} else {
//						dataLine.add(String.valueOf(value));
//					}
//				}
//				dataLines.add(dataLine);
//			}
//		} catch (CentrifugeException e) {
//		} finally {
//			SqlUtil.quietCloseConnection(connection);
//		}

		try (FileWriter fileWriter = new FileWriter(fileToWrite);
		     CSVWriter csvWriter = new CSVWriter(fileWriter)) {
			String[] headersLine = new String[headers.size()];
			headersLine = headers.toArray(headersLine);

			csvWriter.writeNext(headersLine);

			for (List<String> line : dataLines) {
				String[] dataLine = new String[line.size()];
				dataLine = line.toArray(dataLine);

				csvWriter.writeNext(dataLine);
			}
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}
}
