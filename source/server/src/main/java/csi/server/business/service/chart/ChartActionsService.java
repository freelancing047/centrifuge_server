/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.business.service.chart;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.DBObject;

import csi.config.Configuration;
import csi.server.business.service.chart.storage.AbstractChartStorageService;
import csi.server.business.service.chart.storage.ChartStorage;
import csi.server.business.service.chart.storage.postgres.DataToCategoryListResultCriteriaTransformer;
import csi.server.business.service.chart.storage.postgres.DataToCategoryListResultQueryTransformer;
import csi.server.business.service.chart.storage.postgres.DataToFullSelectionTransformer;
import csi.server.business.service.chart.storage.postgres.DataToSingleIndexTransformer;
import csi.server.business.service.chart.storage.postgres.DataToTableResultTransformer;
import csi.server.business.service.chart.storage.postgres.TableResultToDataTransformer;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DisplayFirst;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.service.api.ChartActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.sql.Column;
import csi.server.util.sql.ScrollCallback;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.SubSelectTableSource;
import csi.server.util.sql.TableSource;
import csi.server.util.sql.api.AggregateFunction;
import csi.shared.core.visualization.chart.AxisScale;
import csi.shared.core.visualization.chart.ChartMetrics;
import csi.shared.core.visualization.chart.HighchartPagingRequest;
import csi.shared.core.visualization.chart.HighchartPagingResponse;
import csi.shared.core.visualization.chart.HighchartRequest;
import csi.shared.core.visualization.chart.HighchartResponse;
import csi.shared.core.visualization.chart.HighchartSeriesData;
import csi.shared.core.visualization.chart.MeasureChartType;
import csi.shared.core.visualization.chart.OverviewRequest;
import csi.shared.core.visualization.chart.OverviewResponse;
import csi.shared.core.visualization.chart.SeriesInfo;
import csi.shared.gwt.viz.chart.ChartOverviewColorMapper;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartActionsService implements ChartActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(ChartActionsService.class);

   private static final int BIN_SIZE_LIMIT = 10000;
   private static final String COUNT_STAR_MEASURE_COLOR = "#06446D";
   private static final int DEFAULT_WIDTH = 2000;
   private static final BigDecimal BIG_DECIMAL_MIN_DOUBLE = BigDecimal.valueOf(Double.MIN_VALUE);
   private static final BigDecimal BIG_DECIMAL_MIN_INTEGER = BigDecimal.valueOf(Integer.MIN_VALUE);

	@Inject
    private ChartActionsServiceUtil chartActionsServiceUtil;

	@Override
	public HighchartResponse getChart(HighchartRequest request) {
		DrillChartViewDef viewDef = CsiPersistenceManager.findObject(DrillChartViewDef.class, request.getVizUuid());

		DataView dataView = CsiPersistenceManager.findObject(DataView.class, request.getDvUuid());

		HighchartResponse response = new HighchartResponse();

        // TODO: We already query the DB for the exact same information on getChartScalesForMeasures
        // Get the full table related to the chart.
        TableResult chartTable = new ChartTableGenerator(viewDef, dataView, request.getDrillDimensions()).getChartTable();

		response.setAxisScales(getChartScalesForMeasures(viewDef, dataView, request.getDrillDimensions()));
		convertToHighchartResponse(response, chartTable, request.getDrillDimensions().size(), viewDef);

        response.setChartLimitExceeded(chartTable.isChartLimitExceeded());
        response.setTableLimitExceeded(chartTable.isTableLimitExceeded());
        response.setRowCount(chartTable.getRowCount());
        response.setTotals(chartTable.getTotalValues());

        return response;

	}

	@Override
	public HighchartPagingResponse getChartPage(HighchartPagingRequest request) {
		DrillChartViewDef viewDef = CsiPersistenceManager.findObject(DrillChartViewDef.class, request.getVizUuid());

		DataView dataView = CsiPersistenceManager.findObject(DataView.class, request.getDvUuid());

		HighchartPagingResponse response = new HighchartPagingResponse();


		AbstractChartStorageService storageService = AbstractChartStorageService.instance();
		int drillKey = storageService.createDrillKey(request.getDrillDimensions());
		boolean isCached = storageService.hasVisualizationDataAt(request.getVizUuid(), drillKey);
		TableResult chartTable = null;
		if (isCached) {
			chartTable = retrieveFromCache(request, storageService, drillKey);
		} else {
			chartTable = new ChartTableGenerator(viewDef, dataView, request.getDrillDimensions()).getChartTable();
			cache(chartTable, request.getVizUuid(), drillKey);
		}
		//Create overview before any paging happens
		OverviewRequest overviewRequest = new OverviewRequest();
		if (!chartTable.getDimensionValues().isEmpty()) {
			overviewRequest.setSeriesData(chartTable.getDimensionValues().get(0));
		} else {
			request.setOverviewNeeded(false);
		}
		overviewRequest.setCurrentWidth(request.getVizWidth());
		if (request.isOverviewNeeded()) {
			response.setOverviewResponse(getOverview(overviewRequest));
		}

        //remove everything but the page we want from request
        if(chartTable.getRowCount() > Configuration.getInstance().getChartConfig().getMaxChartCategories()){//chartTable.isChartLimitExceeded() && chartTable.isTableLimitExceeded()){
            int start = request.getStart();
            int total = chartTable.getRowCount();

            if(response.getOverviewResponse() != null){
                response.getOverviewResponse().setTotalCategories(total);
            }

            doPaging(chartTable, request);
            response.setStart(start);
        }

        response.setLimit(chartTable.getRowCount());


        // Let outside code catch any error, too slow
        {
            ArrayList<AxisScale> axisScales = new ArrayList<AxisScale>();
            for (List<Number> numbers : chartTable.getDimensionValues()) {
                double minValue = Double.MAX_VALUE;
                double maxValue = Double.MIN_VALUE;

                for (Number number : numbers) {
                    minValue = Math.min(number.doubleValue(), minValue);
                    maxValue = Math.max(number.doubleValue(), maxValue);
//					sumValue += number.doubleValue();
                }

                AxisScale scale = new AxisScale();
                scale.setMinValue(minValue < 0 ? minValue : 0);
                scale.setMaxValue(maxValue < 0 ? 0 : maxValue);
//				scale.setSum(sumValue);
                axisScales.add(scale);
            }

            if (viewDef.getChartSettings().isAlignAxes()) {
                response.setAxisScales(new ArrayList<AxisScale>(axisScales.subList(0, 1)));
            }else{
            	response.setAxisScales(axisScales);
			}
        }

		for (AxisScale axisScale : response.getAxisScales()) {
			double minValue = axisScale.getMinValue();

			if(minValue < 0 ){
				response.getAxisScales().forEach(a->{
					a.setMaxValue(Math.max(Math.abs(a.getMaxValue()), Math.abs(a.getMinValue())));
					a.setMinValue(-a.getMaxValue());
				});
				break;
			}
		}


		convertToHighchartResponse(response, chartTable, request.getDrillDimensions().size(), viewDef);



        response.setChartLimitExceeded(chartTable.isChartLimitExceeded());
        response.setTableLimitExceeded(chartTable.isTableLimitExceeded());
        response.setRowCount(chartTable.getRowCount());
        response.setTotals(chartTable.getTotalValues());

        return response;

    }

    private static void cache(TableResult chartTable, String vizUuid, Integer drillKey) {
        TableResultToDataTransformer transformer = new TableResultToDataTransformer();
        AbstractChartStorageService storageService = AbstractChartStorageService.instance();
        ChartStorage chartStorage = storageService.createEmptyStorage(vizUuid);
        chartStorage.addResult(transformer.apply(chartTable));
        storageService.save(vizUuid, drillKey, chartStorage);
    }

    public static TableResult retrieveFromCache(HighchartPagingRequest request, AbstractChartStorageService storageService, int drillKey) {
        TableResult chartTable;
        ChartStorage chartStorage = storageService.getChartStorage(request.getVizUuid(), drillKey);
        DBObject storedTable = (DBObject) chartStorage.getResult();
        DataToTableResultTransformer transformer = new DataToTableResultTransformer();
        chartTable = transformer.apply(storedTable);
        return chartTable;
    }

    private static void doPaging(TableResult chartTable, HighchartPagingRequest request) {
        int start = request.getStart();
        int limit = request.getLimit();

        if(limit <= 0){
            limit = Configuration.getInstance().getChartConfig().getMaxChartCategories();
        }
        if(start < 0) {
            start = 0;
        }
        if((start+limit) > chartTable.getCategories().size()){
            start = chartTable.getCategories().size()  - limit;
        }
        List<String> categories = chartTable.getCategories().subList(start, start+limit);
        chartTable.setCategories(categories);

        List<List<Number>> dimensionValues = new ArrayList<List<Number>>();
        for(List<Number> measures: chartTable.getDimensionValues()){
            dimensionValues.add(measures.subList(start, start+limit));
        }

        chartTable.setDimensionValues(dimensionValues);
        chartTable.setRowCount(limit);
    }

    /**
     * Returns the list of AxisScales
     * @param viewDef
     * @param dataView
     * @param categoryDrills
     * @return
     */
	private List<AxisScale> getChartScalesForMeasures(DrillChartViewDef viewDef, DataView dataView, final List<String> categoryDrills) {
		final ChartSettings settings = viewDef.getChartSettings();
		ChartQueryBuilder qb = chartActionsServiceUtil.getChartQueryBuilder(viewDef, dataView, categoryDrills);

		SelectSQL innerSQL = qb.getQuery(false);

		SubSelectTableSource innerSqlSource = chartActionsServiceUtil.getSqlFactory().getTableSourceFactory().create(innerSQL);
		SelectSQL outerSQL = chartActionsServiceUtil.getSqlFactory().createSelect(innerSqlSource);

		int totalColumns = innerSqlSource.getSubSelectColumns().size();
		if (settings.isUseCountStarForMeasure() || settings.getMeasureDefinitions().isEmpty()) {
			Column column = innerSqlSource.getSubSelectColumns().get(totalColumns - 1);
			outerSQL.select(innerSqlSource.getColumn(column).with(AggregateFunction.MINIMUM));
			outerSQL.select(innerSqlSource.getColumn(column).with(AggregateFunction.MAXIMUM));
			outerSQL.select(innerSqlSource.getColumn(column).with(AggregateFunction.SUM));
		} else {
			int totalMeasures = settings.getMeasureDefinitions().size();
			for (int i = 0; i < totalMeasures; i++) {
				Column column = innerSqlSource.getSubSelectColumns().get((totalColumns - totalMeasures) + i);
				outerSQL.select(innerSqlSource.getColumn(column).with(AggregateFunction.MINIMUM));
				outerSQL.select(innerSqlSource.getColumn(column).with(AggregateFunction.MAXIMUM));
				outerSQL.select(innerSqlSource.getColumn(column).with(AggregateFunction.SUM));
			}
		}

		return outerSQL.scroll(new ScrollCallback<List<AxisScale>>() {
			private int count = 0;
			@Override
			public List<AxisScale> scroll(ResultSet resultSet) throws SQLException {
				List<AxisScale> scales = new ArrayList<AxisScale>();
				resultSet.next();

                int measures = settings.isUseCountStarForMeasure() || settings.getMeasureDefinitions().isEmpty() ? 1 : settings.getMeasureDefinitions().size();
                int columnCounter = 1;
                count++;
                for (int i = 0; i < measures; i++) {
                    double minValue = resultSet.getDouble(columnCounter++);
                    double maxValue = resultSet.getDouble(columnCounter++);
                    double sumValue = resultSet.getDouble(columnCounter++);

                    AxisScale scale = new AxisScale();
                    scale.setMinValue(minValue < 0 ? minValue : 0);
                    scale.setMaxValue(maxValue < 0 ? 0 : maxValue);
                    scale.setSum(sumValue);
                    scales.add(scale);
                }
                if (settings.isAlignAxes()) {
                    double minValue = Double.MAX_VALUE;
                    double maxValue = Double.MIN_VALUE;
                    for (AxisScale scale : scales) {
                        minValue = Math.min(minValue, scale.getMinValue());
                        maxValue = Math.max(maxValue, scale.getMaxValue());
                    }
                    for (AxisScale scale : scales) {
                        scale.setMaxValue(maxValue);
                        scale.setMinValue(minValue);
                    }
                }

				return scales;
			}
		});
	}

	/**
	 * @param table
	 * @param dimensionValuesSize The number of dimensions that have been specified (translates to drill-in level).
	 * @param viewDef
	 * @return
	 */
	private static void convertToHighchartResponse(HighchartResponse response, TableResult table, int dimensionValuesSize, final DrillChartViewDef viewDef) {
		if (LOG.isTraceEnabled()) {
			for (int i = 0; i < table.getRowCount(); i++) {
				StringBuilder builder = new StringBuilder();
				builder.append(table.getCategories().get(i)).append("\t");
				for (int j = 0; j < (table.getColumnCount() - 1); j++) {
					builder.append(table.getDimensionValues().get(j).get(i)).append("\t");
				}
				LOG.info(builder);
			}
		}
		// Preserve order. Thats why we use a list and not HashSet or TreeSet.
		for (int row = 0; row < table.getRowCount(); row++) {
			response.getCategoryNames().add(table.getCategories().get(row));
		}

		ChartSettings settings = viewDef.getChartSettings();
		if (settings.isUseCountStarForMeasure() || settings.getMeasureDefinitions().isEmpty()) {
			HighchartSeriesData seriesData = new HighchartSeriesData();
			SeriesInfo seriesInfo = new SeriesInfo();
			seriesInfo.setMetricName(ChartActionsServiceUtil.COUNT_STAR_MEASURE_NAME);
			seriesInfo.setHexColor(COUNT_STAR_MEASURE_COLOR);
			seriesInfo.setMeasureChartType(MeasureChartType.DEFAULT);
         int tableRows = table.getRowCount();

			for (int row = 0; row < tableRows; row++) {
				seriesData.getData().add(table.getDimensionValues().get(0).get(row));
			}
			response.getSeriesInfos().add(seriesInfo);
			response.getSeriesData().add(seriesData);
		} else {
		   int howMany = settings.getMeasureDefinitions().size();

		   for (int i = 0; i < howMany; i++) {
				MeasureDefinition md = settings.getMeasureDefinitions().get(i);
				HighchartSeriesData seriesData = new HighchartSeriesData();
				SeriesInfo seriesInfo = new SeriesInfo();
				seriesInfo.setMetricName(md.intedComposedName());
				seriesInfo.setHexColor(md.getColor());
				seriesInfo.setMeasureChartType(md.getMeasureChartType());
				int tableRows = table.getRowCount();

				// Add the data.
                for (int row = 0; row < tableRows; row++) {
                    Number number = null;
                    try {
                        number = table.getDimensionValues().get(i).get(row);
                        if (number.intValue() == Integer.MIN_VALUE) {
                            number = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    seriesData.getData().add(number);
                }
                response.getSeriesData().add(seriesData);
                response.getSeriesInfos().add(seriesInfo);
            }
        }
        response.setTotals(table.getTotalValues());
	}

	@Override
	public OverviewResponse getOverview(OverviewRequest overviewRequest) {
		return calculateBinnedOverview(overviewRequest);
	}

	private static OverviewResponse calculateBinnedOverview(OverviewRequest overviewRequest) {
		OverviewResponse overviewResponse = new OverviewResponse();
		if (overviewRequest.getCurrentWidth() < 1) {
            overviewRequest.setCurrentWidth(DEFAULT_WIDTH);
        }
		int binSize = ((overviewRequest.getSeriesData().size() - 1) / overviewRequest.getCurrentWidth()) + 1;
		overviewResponse.setOverviewBinSize(binSize);

        Double minValue = ChartOverviewColorMapper.calculateMinValue(overviewRequest.getSeriesData());
        Double maxValue = ChartOverviewColorMapper.calculateMaxValue(overviewRequest.getSeriesData());

		try {
			int index = 0;
			if (binSize < 1) {
				return null;
			}
			while (index < overviewRequest.getSeriesData().size()) {
				List<Number> numbers = collectBinSizeNumbers(index, binSize, overviewRequest.getSeriesData());
                Number number = maxNumberInBin(numbers);
                //Number number = averageNumbersInBin(numbers);
				overviewResponse.getOverviewColors().add(ChartOverviewColorMapper.getWeight(number, minValue, maxValue));
				index += binSize;
				if (numbers.size() > BIN_SIZE_LIMIT) {
					break;
				}
			}

			overviewResponse.setTotalCategories(overviewRequest.getSeriesData().size());
		} catch (Exception error) {
		   LOG.error(error);
			return null;
		}
		return overviewResponse;
	}

   private static List<Number> collectBinSizeNumbers(int index, int binSize, List<Number> numbers) {
      List<Number> numbersInBin = new ArrayList<Number>();

      for (int i = 0; i < binSize; i++) {
         if ((i + index) == numbers.size()) {
            break;
         }
         numbersInBin.add(numbers.get(i + index));
      }
      return numbersInBin;
   }

   private Number averageNumbersInBin(List<Number> numbers) {
      return Double.valueOf(sumNumbersInBin(numbers).doubleValue() / numbers.size());
   }

   private static Number sumNumbersInBin(List<Number> numbers) {
      double sum = 0;

      for (Number n : numbers) {
         sum += n.doubleValue();
      }
      return Double.valueOf(sum);
   }

   private static Number maxNumberInBin(List<Number> numbers) {
      double max = Double.MIN_VALUE;

      for (Number n : numbers) {
         max = Math.max(max, n.doubleValue());
      }
      return Double.valueOf((BigDecimal.valueOf(max).compareTo(BIG_DECIMAL_MIN_DOUBLE) == 0) ? 0 : max);
   }

	/**
	 * @param viewDef
	 * @param dataView
	 * @param categoryDrills current drill values for each category
	 * @return
	 */
	private int getChartTableCount(DrillChartViewDef viewDef, DataView dataView, List<String> categoryDrills) {
		ChartQueryBuilder qb = chartActionsServiceUtil.getChartQueryBuilder(viewDef, dataView, categoryDrills);

		SelectSQL innerSQL = qb.getQuery(false);

		TableSource countSqlSource = chartActionsServiceUtil.getSqlFactory().getTableSourceFactory().create(innerSQL);
		SelectSQL outerSQL = chartActionsServiceUtil.getSqlFactory().createSelect(countSqlSource);
		outerSQL.select(countSqlSource.createConstantColumn(1).with(AggregateFunction.COUNT));

		return outerSQL.scroll(new ScrollCallback<Integer>() {
			@Override
			public Integer scroll(ResultSet resultSet) throws SQLException {
				resultSet.next();
				return resultSet.getInt(1);
			}
		});
	}

    public static void clearCache(VisualizationDef viz) {
        AbstractChartStorageService storageService = AbstractChartStorageService.instance();
        storageService.resetData(viz.getUuid());
    }

    @Override
    public List<String> getQueryCategories(String vizUuid, String query, List<String> drills) {

        AbstractChartStorageService storageService = AbstractChartStorageService.instance();

        ChartStorage chartStorage = storageService.getChartStorage(vizUuid, storageService.createDrillKey(drills));
        DBObject storedTable = (DBObject) chartStorage.getResult();
        int maxChartCategories = Configuration.getInstance().getChartConfig().getMaxChartCategories();
        DataToCategoryListResultQueryTransformer transformer = new DataToCategoryListResultQueryTransformer(maxChartCategories, query);
        List<String> categories = transformer.apply(storedTable);

        return categories;
    }

    @Override
    public Integer getCategoryRangeIndex(String uuid,  List<String> drills, String category) {
        AbstractChartStorageService storageService = AbstractChartStorageService.instance();

        ChartStorage chartStorage = storageService.getChartStorage(uuid, storageService.createDrillKey(drills));
        DBObject storedTable = (DBObject) chartStorage.getResult();
        DataToSingleIndexTransformer transformer = new DataToSingleIndexTransformer(category);
        Integer index = transformer.apply(storedTable);

        return index;
    }

    @Override
    public List<String> selectCategories(List<ChartCriterion> criteria, String vizUuid, List<String> drills){
        AbstractChartStorageService storageService = AbstractChartStorageService.instance();

        ChartStorage chartStorage = storageService.getChartStorage(vizUuid, storageService.createDrillKey(drills));
        DBObject storedTable = (DBObject) chartStorage.getResult();


        DrillChartViewDef viewDef = CsiPersistenceManager.findObject(DrillChartViewDef.class, vizUuid);
        DataToCategoryListResultCriteriaTransformer transformer = new DataToCategoryListResultCriteriaTransformer(criteria, viewDef);
        List<String> categories = transformer.apply(storedTable);

        return categories;
    }

    @Override
    public List<String> selectAll(String uuid, List<String> drillSelections) {
        AbstractChartStorageService storageService = AbstractChartStorageService.instance();

        ChartStorage chartStorage = storageService.getChartStorage(uuid, storageService.createDrillKey(drillSelections));
        DBObject storedTable = (DBObject) chartStorage.getResult();
        DataToFullSelectionTransformer transformer = new DataToFullSelectionTransformer(drillSelections);
        List<String> categories = transformer.apply(storedTable);


        return categories;
    }

	@Override
	public List<ChartMetrics> getChartMetrics(HighchartPagingRequest requestForHighchartData ) {
    	try {
			AbstractChartStorageService storageService = AbstractChartStorageService.instance();
			DrillChartViewDef viewDef = CsiPersistenceManager.findObject(DrillChartViewDef.class, requestForHighchartData.getVizUuid());
			DataView dataView = CsiPersistenceManager.findObject(DataView.class, requestForHighchartData.getDvUuid());

			List<AxisScale> chartScalesForMeasures = getChartScalesForMeasures(viewDef, dataView, requestForHighchartData.getDrillDimensions());

			int drillKey = storageService.createDrillKey(requestForHighchartData.getDrillDimensions());
			boolean isCached = storageService.hasVisualizationDataAt(requestForHighchartData.getVizUuid(), drillKey);
			TableResult chartTable = null;
			if (isCached) {
				chartTable = retrieveFromCache(requestForHighchartData, storageService, drillKey);
			} else {
				chartTable = new ChartTableGenerator(viewDef, dataView, requestForHighchartData.getDrillDimensions()).getChartTable();
				cache(chartTable, requestForHighchartData.getVizUuid(), drillKey);
			}

			int i = 0;
			List<ChartMetrics> allMetrics = new ArrayList<ChartMetrics>(viewDef.getChartSettings().getMeasureDefinitions().size());
			if (!viewDef.getChartSettings().isUseCountStarForMeasure()) {
				for (AxisScale scale : chartScalesForMeasures) {
					ChartMetrics m = new ChartMetrics();
					m.setSeriesName(viewDef.getChartSettings().getMeasureDefinitions().get(i).getComposedName());

					findMinMax(m, chartTable.getDimensionValues().get(i));

					m.setCategoryCount(chartTable.getDimensionValues().get(i).size());
					i++;
					allMetrics.add(m);
				}
			} else {
				ChartMetrics m = new ChartMetrics();
				m.setSeriesName("Count (*)");
				findMinMax(m, chartTable.getDimensionValues().get(i));
				m.setCategoryCount(chartTable.getDimensionValues().get(0).size());
				allMetrics.add(m);
			}
			return allMetrics;
		}catch(Exception e){
		   LOG.error(e.getLocalizedMessage());
    		e.printStackTrace();
    		return new ArrayList<ChartMetrics>();
		}
	}

    /**
     *
     * @param vizUuid
     * @param view
     * @return true if updated, false if illegal arguments, or not necessary.
     */
	@Override
	public boolean updateCurrentView(String vizUuid, DisplayFirst view) {
	   boolean result = false;

	   if (view != null) {
	      DrillChartViewDef viewDef = CsiPersistenceManager.findObject(DrillChartViewDef.class, vizUuid);

	      if (view != viewDef.getChartSettings().getCurrentView()) {
	         viewDef.getChartSettings().setCurrentView(view);
	         CsiPersistenceManager.merge(viewDef);
	         CsiPersistenceManager.commit();
	         CsiPersistenceManager.close();
	         result = true;
	      }
	   }
	   return result;
	}

   private static void findMinMax(ChartMetrics m, List<Number> data) {
      Number min, max;

      if (data.isEmpty()) {
         min = 0;
         max = 0;
      } else {
         min = data.get(0);
         max = data.get(0);
      }
      for (Number number : data) {
         // if we have a min int - its a null in the measure.
         if (number != null) {
            if (BigDecimal.valueOf(number.doubleValue()).compareTo(BIG_DECIMAL_MIN_INTEGER) != 0) {
               min = Double.valueOf(Math.min(min.doubleValue(), number.doubleValue()));
            }
            max = Double.valueOf(Math.max(max.doubleValue(), number.doubleValue()));
         }
      }

//		max = max.doubleValue() == Integer.MIN_VALUE ? 0 : max;
//		min = min.doubleValue() == Integer.MAX_VALUE ? 0 : min;

      m.setMax(max);
      m.setMin(min);
   }

   public static void createCache(VisualizationDef viz, DataView dataView) {
      DrillChartViewDef chartViewDef = (DrillChartViewDef) viz;
      AbstractChartStorageService storageService = AbstractChartStorageService.instance();
      ArrayList<String> drillDimensions = new ArrayList<String>();
      int drillKey = storageService.createDrillKey(drillDimensions);

      if (!storageService.hasVisualizationDataAt(viz.getUuid(), drillKey)) {
         TableResult chartTable = new ChartTableGenerator(chartViewDef, dataView, drillDimensions).getChartTable();

         cache(chartTable, chartViewDef.getUuid(), drillKey);
      }
   }
}
