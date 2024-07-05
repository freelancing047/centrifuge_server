package csi.server.business.service.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import csi.config.Configuration;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.util.sql.SelectResultRow;
import csi.server.util.sql.SelectResultSet;

public class ChartTableGenerator {
    private DrillChartViewDef viewDef;
    private DataView dataView;
    private List<String> categoryDrills;
    private List<Number> rowIdsToFilterBy = null;
    private TableResult aggregateResults;
    private TableResult filteredResults;
//    private List<String> headers;
//    private StatisticsHolder statisticsHolder;
//    private List<String> categories;
//    private List<List<Number>> table;
    public static final String SECRET_NULL_CHART_LABEL = "\u0020\u0009\u0020";

    public ChartTableGenerator(DrillChartViewDef viewDef, DataView dataView, List<String> categoryDrills) {
        this.viewDef = viewDef;
        this.dataView = dataView;
        this.categoryDrills = categoryDrills;
    }

    public void setRowIdsToFilterBy(List<Number> rowIdsToFilterBy) {
        this.rowIdsToFilterBy = rowIdsToFilterBy;
    }

    public TableResult getChartTable() {

        SelectResultSet groupResults = getCategoryResults();
        SelectResultSet totalResults = getTotalResults();

//        boolean tableLimit = result.getRowCount() >= Configuration.getInstance().getChartConfig().getMaxTableCategories();
        boolean chartLimit = groupResults.getRowCount() >= Configuration.getInstance().getChartConfig().getMaxChartCategories();

        List<ChartCriterion> criteria = viewDef.getChartSettings().getFilterCriteria();
        //if we hit both limits there is no need to do the rest
        //		if(tableLimit && chartLimit && !hasCriteria(criteria)) {
        //			return createLimitExceededResult(result.getRowCount());
        //		}

        getAggregateResults(categoryDrills.size(), groupResults, totalResults );


        //If we have filter, we apply it, unless this is an export, then we check if rowIdsToFilterBy or categoryDrills.size are there
        if (aggregateResults.getRowCount() == 0) {
            return aggregateResults;
        } else if (!hasCriteria(criteria) || (rowIdsToFilterBy != null) || !categoryDrills.isEmpty()) {
//            aggregateResults.setTableLimitExceeded(tableLimit);
            aggregateResults.setChartLimitExceeded(chartLimit);
            return aggregateResults;
        } else {
            //applyFilter();
            CriteriaFilterOperator operator = new CriteriaFilterOperator(aggregateResults, criteria, viewDef);
            filteredResults = operator.getFilteredResults();
//            tableLimit = filteredResults.getCategories().size() >= Configuration.getInstance().getChartConfig().getMaxTableCategories();
            chartLimit = filteredResults.getCategories().size() >= Configuration.getInstance().getChartConfig().getMaxChartCategories();
//            filteredResults.setTableLimitExceeded(tableLimit);
            filteredResults.setChartLimitExceeded(chartLimit);

            return filteredResults;
        }
    }
/*
    public TableResult getChartPage(){
        ChartQueryBuilder qb =
                Configuration.getInstance().getWebApplicationContext()
                .getBean(csi.server.business.service.chart.ChartActionsServiceUtil.class)
                .getChartQueryBuilder(viewDef, dataView, categoryDrills);

        if (rowIdsToFilterBy != null) qb.setRowIdsToFilterBy(rowIdsToFilterBy);


        SelectResultSet result = qb.getPagedQuery().execute();

//        boolean tableLimit = result.getRowCount() >= Configuration.getInstance().getChartConfig().getMaxTableCategories();
        boolean chartLimit = result.getRowCount() >= Configuration.getInstance().getChartConfig().getMaxChartCategories();

        List<ChartCriterion> criteria = viewDef.getChartSettings().getFilterCriteria();
        //if we hit both limits there is no need to do the rest
        if(chartLimit && !hasCriteria(criteria)) {
            return createLimitExceededResult(result.getRowCount());
        }

        getAggregateResults(categoryDrills.size(), result );

//        aggregateResults.setTableLimitExceeded(tableLimit);
        aggregateResults.setChartLimitExceeded(chartLimit);
        return aggregateResults;
    }

    public TableResult createLimitExceededResult(int count) {
        TableResult tempTableResult = new TableResult();
        tempTableResult.setTableLimitExceeded(true);
        tempTableResult.setChartLimitExceeded(true);
        tempTableResult.setRowCount(count);
        return tempTableResult;
    }
*/
    public boolean hasCriteria(List<ChartCriterion> criteria) {
        return ((criteria != null) && !criteria.isEmpty());
    }

    private void getAggregateResults(int drillDimensionsCount, SelectResultSet groupedResultSet,
                                     SelectResultSet totalResultSet) {
        aggregateResults = new TableResult();

        int categoryIndex = drillDimensionsCount;
        int howMany = groupedResultSet.getColumnCount();

        for (int colInx = categoryIndex + 1; colInx < howMany; colInx++) {
            aggregateResults.getDimensionValues().add(new ArrayList<Number>());
        }

        int counter = 0;
        for (SelectResultRow row : groupedResultSet) {
            Object val = row.getValue(categoryIndex);
            String valstr = ChartActionsServiceUtil.getParsedString(val);
            boolean valNull = false;
            if ((valstr == null) || valstr.equals(SECRET_NULL_CHART_LABEL)) {
                valNull = true;
                valstr = SECRET_NULL_CHART_LABEL;
            }

            aggregateResults.getCategories().add(valstr);
            if (valNull) {
                aggregateResults.getNullCategory().add(counter);
            }
            howMany = groupedResultSet.getColumnCount() - 1 - drillDimensionsCount;

            for (int index = 0; index < howMany; index++) {
                Number value = row.getValue(categoryIndex + index + 1);

                if (value != null) {
                    if (BigDecimal.valueOf(value.doubleValue() - Math.floor(value.doubleValue())).compareTo(BigDecimal.ZERO) != 0) {
                        aggregateResults.getDimensionValues().get(index).add(Double.valueOf(Math.round(value.doubleValue() * 100) / 100.0));
                    } else {
                        aggregateResults.getDimensionValues().get(index).add(value);
                    }
                 } else {
                    aggregateResults.getDimensionValues().get(index).add(Integer.MIN_VALUE);
                }
            }

            counter++;
        }
        howMany = totalResultSet.getColumnCount();

        for (SelectResultRow row : totalResultSet) {
             for (int index = drillDimensionsCount; index < howMany; index++) {
                Number value = row.getValue(index);
                if (value != null) {
                    value = value.doubleValue();
                    if (BigDecimal.valueOf(value.doubleValue() - Math.floor(value.doubleValue())).compareTo(BigDecimal.ZERO) != 0) {
                        value = Double.valueOf(Math.round(value.doubleValue() * 100) / 100.0);
                    }
                }
                aggregateResults.getTotalValues().add(value);
            }
        }

        aggregateResults.setRowCount(counter);
    }

//    private void applyFilter() {
//        generateHeaders();
//        gatherRawResultsInfo();
//        populateFilterResults();
//    }

//    private void generateHeaders() {
//        ChartSettings settings = viewDef.getChartSettings();
//        headers = new ArrayList<String>();
//        if (settings.isUseCountStarForMeasure() || settings.getMeasureDefinitions().size() == 0) {
//            headers.add(ChartActionsServiceUtil.COUNT_STAR_MEASURE_NAME);
//        } else {
//            for (int i = 0; i < settings.getMeasureDefinitions().size(); i++) {
//                MeasureDefinition md = settings.getMeasureDefinitions().get(i);
//                headers.add(md.getComposedName());
//            }
//        }
//    }

//    private void gatherRawResultsInfo() {
//        statisticsHolder = new StatisticsHolderImpl();
//        statisticsHolder.setCategoryNames(aggregateResults.getCategories());
//        statisticsHolder.setData(aggregateResults.getDimensionValues());
//        categories = aggregateResults.getCategories();
//        table = aggregateResults.getDimensionValues();
//    }
//
//    private void populateFilterResults() {
//        filteredResults = new TableResult();
//        filteredResults.getNullCategory().addAll(aggregateResults.getNullCategory());
//
//        int j = 0;
//        while (j < aggregateResults.getDimensionValues().size()) {
//            filteredResults.getDimensionValues().add(new ArrayList<Number>());
//            j++;
//        }
//
//        int row = 0;
//        int count = 0;
//
//        while (row < aggregateResults.getRowCount()) {
//            if (test(row)) {
//                filteredResults.getCategories().add(aggregateResults.getCategories().get(row));
//                int col = 0;
//                while (col < aggregateResults.getDimensionValues().size()) {
//                    filteredResults.getDimensionValues().get(col).add(aggregateResults.getDimensionValues().get(col).get(row));
//                    col++;
//                }
//                count++;
//            }
//            row++;
//        }
//
//        filteredResults.setRowCount(count);
//    }




//    private boolean test(int row) {
//        boolean retVal = true;
//        for (ChartCriterion criterion : viewDef.getChartSettings().getFilterCriteria()) {
//            String operatorString = criterion.getOperatorString();
//            switch (operatorString) {
//            case "<":
//            case "<=":
//            case "==":
//            case ">=":
//            case ">":
//            case "!=":
//                if (!testSingleDoubleTypeCriterion(
//                        table.get(headers.indexOf(criterion.getColumnHeader())).get(row).doubleValue(),
//                        operatorString,
//                        (SingleDoubleTypeCriterion) criterion)
//                        )
//                    return false;
//                break;
//            case "<<":
//                if (!testTwoDoubleTypeCriterion(
//                        table.get(headers.indexOf(criterion.getColumnHeader())).get(row).doubleValue(),
//                        operatorString,
//                        (TwoDoubleTypeCriterion) criterion)
//                        )
//                    return false;
//                break;
//            case "Top":
//            case "Top%":
//            case "Bottom":
//            case "Bottom%":
//                if (!testSingleIntegerTypeCriterion(
//                        categories.get(row),
//                        operatorString,
//                        (SingleIntegerTypeCriterion) criterion,
//                        statisticsHolder)
//                        )
//                    return false;
//                break;
//            default:
//                return false;
//            }
//        }
//        return retVal;
//    }
//
//    private static double EPSILON = 0.000000001;
//
//    private static boolean testSingleDoubleTypeCriterion(Double doubleValue, String operatorString, SingleDoubleTypeCriterion criterion) {
//        switch (operatorString) {
//        case "<":
//            return doubleValue < 	criterion.getTestValue();
//        case "<=":
//            return doubleValue <= 	criterion.getTestValue();
//        case "==":
//            double x = Math.abs(doubleValue - criterion.getTestValue());
//            return x < EPSILON;
//        case ">=":
//            return doubleValue >= 	criterion.getTestValue();
//        case ">":
//            return doubleValue > 	criterion.getTestValue();
//        case "!=":
//            double y = Math.abs(doubleValue - criterion.getTestValue());
//            return y > EPSILON;
//        default:
//            return false;
//        }
//    }
//
//    private static boolean testTwoDoubleTypeCriterion(Double doubleValue, String operatorString, TwoDoubleTypeCriterion criterion) {
//        switch (operatorString) {
//        case "<<":
//            return criterion.getMinValue() < doubleValue && doubleValue < criterion.getMaxValue();
//        default:
//            return false;
//        }
//    }
//
//    private static boolean testSingleIntegerTypeCriterion(String key, String operatorString, SingleIntegerTypeCriterion criterion, StatisticsHolder statisticsHolder) {
//        switch (operatorString) {
//        case "Top":
//            return statisticsHolder.isTop(criterion.getColumnIndex(), criterion.getTestValue(), key);
//        case "Top%":
//            return statisticsHolder.isTopPercent(criterion.getColumnIndex(), criterion.getTestValue(), key);
//        case "Bottom":
//            return statisticsHolder.isBottom(criterion.getColumnIndex(), criterion.getTestValue(), key);
//        case "Bottom%":
//            return statisticsHolder.isBottomPercent(criterion.getColumnIndex(), criterion.getTestValue(), key);
//        default:
//            return false;
//        }
//    }

   private SelectResultSet getCategoryResults() {
      ChartQueryBuilder qb =
         Configuration.getInstance().getWebApplicationContext()
            .getBean(csi.server.business.service.chart.ChartActionsServiceUtil.class)
            .getChartQueryBuilder(viewDef, dataView, categoryDrills);

      if (rowIdsToFilterBy != null) {
         qb.setRowIdsToFilterBy(rowIdsToFilterBy);
      }
      return qb.getQuery(false).execute();
   }

   private SelectResultSet getTotalResults() {
      ChartQueryBuilder qb =
         Configuration.getInstance().getWebApplicationContext()
            .getBean(csi.server.business.service.chart.ChartActionsServiceUtil.class)
            .getChartQueryBuilder(viewDef, dataView, categoryDrills);

       qb.setAggregateOnly();

       if (rowIdsToFilterBy != null) {
          qb.setRowIdsToFilterBy(rowIdsToFilterBy);
       }
       return qb.getQuery(true).execute();
    }
}
