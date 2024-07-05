package csi.server.business.service.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.chart.SingleDoubleTypeCriterion;
import csi.server.common.model.visualization.chart.TwoDoubleTypeCriterion;

public class CriteriaFilterOperator {

    private TableResult filteredResults;

    public CriteriaFilterOperator(TableResult fullTable, List<ChartCriterion> criteria, DrillChartViewDef viewDef){
        doFilter(fullTable, criteria, viewDef.getChartSettings());
    }

    public TableResult getFilteredResults() {
        return filteredResults;
    }

    /**
     *
     *
     * @param fullTable
     * @param criteria
     * @param chartSettings
     * @return
     */
    private void doFilter(TableResult fullTable, List<ChartCriterion> criteria, ChartSettings chartSettings) {
        filteredResults = new TableResult();
        filteredResults.getNullCategory().addAll(fullTable.getNullCategory());

        List<List<Number>> measures = fullTable.getDimensionValues();
        int howMany = measures.size();

        for (int i = 0; i < howMany; i++) {
            filteredResults.getDimensionValues().add(new ArrayList<Number>());
        }
        int categoryCount = fullTable.getCategories().size();
        MeasureRankings measureRankings = new MeasureRankings(measures, criteria, chartSettings, categoryCount);
        Map<ChartCriterion, Integer> criteriaAndHitCount = new HashMap<ChartCriterion, Integer>();

        for(ChartCriterion criterion: criteria){
            criteriaAndHitCount.put(criterion, Integer.valueOf(0));
        }
        int count = 0;
        int categoryIndex = 0;
        boolean pass;

        while (categoryIndex < fullTable.getRowCount()) {
           Number measure;
           pass = true;

           for (ChartCriterion criterion: criteria) {
              measure = measures.get(measureRankings.getCriteriaMeasureIndex(criterion)).get(categoryIndex);
              double measureValue = measure.doubleValue();
              pass = testCriteria(measureValue, criterion, measureRankings, categoryIndex);

              if (!pass) {
                 break;
              }
           }
           if (pass) {
              addFilterResult(fullTable, measures, categoryIndex);
              count++;
           }
           categoryIndex++;
        }
        filteredResults.setRowCount(count);
    }


    public Map<ChartCriterion,Integer> createCountMap(List<ChartCriterion> criteria) {
       Map<ChartCriterion,Integer> hashMap = new HashMap<ChartCriterion,Integer>();

       for (ChartCriterion criterion : criteria) {
          hashMap.put(criterion, Integer.valueOf(0));
       }
       return hashMap;
    }

    public void addFilterResult(TableResult fullTable, List<List<Number>> measures, int categoryIndex) {
        Number measure;
        String categoryName = fullTable.getCategories().get(categoryIndex);
        filteredResults.getCategories().add(categoryName);
        int howMany = measures.size();
        int col = 0;

        while (col < howMany) {
            measure = measures.get(col).get(categoryIndex);
            filteredResults.getDimensionValues().get(col).add(measure);
            col++;
        }
    }

   public boolean testCriteria(double measureValue, ChartCriterion criterion, MeasureRankings measureRankings,
                               int categoryIndex) {
      boolean result = true;
      String operatorString = criterion.getOperatorString();
      double thresholdValue;
      int criteriaIndexThreshold;

      switch (operatorString) {
         case "<":
         case "<=":
         case "==":
         case ">=":
         case ">":
         case "!=":
            result = testSingleDoubleTypeCriterion(measureValue, operatorString, (SingleDoubleTypeCriterion) criterion);
            break;
         case "<<":
            result = testTwoDoubleTypeCriterion(measureValue, operatorString, (TwoDoubleTypeCriterion) criterion);
            break;
         case "Top":
         case "Top%":
            criteriaIndexThreshold = measureRankings.getCriteriaIndexThreshold(criterion);
            thresholdValue = measureRankings.getCriteriaThresholdValue(criterion);

            if (measureValue < thresholdValue) {
               result = false;
            } else if (BigDecimal.valueOf(measureValue).compareTo(BigDecimal.valueOf(thresholdValue)) == 0) {
               if (categoryIndex > criteriaIndexThreshold) {
                  result = false;
               }
            }
            break;
         case "Bottom":
         case "Bottom%":
            criteriaIndexThreshold = measureRankings.getCriteriaIndexThreshold(criterion);
            thresholdValue = measureRankings.getCriteriaThresholdValue(criterion);

            if (measureValue > thresholdValue) {
               result = false;
            } else if (BigDecimal.valueOf(measureValue).compareTo(BigDecimal.valueOf(thresholdValue)) == 0) {
               if (categoryIndex < criteriaIndexThreshold) {
                  result = false;
               }
            }
            break;
         default:
            result = false;
            break;
      }
      return result;
   }

   private static boolean testSingleDoubleTypeCriterion(double doubleValue, String operatorString, SingleDoubleTypeCriterion criterion) {
      boolean result = false;

      switch (operatorString) {
         case "<":
            result = (doubleValue < criterion.getTestValue().doubleValue());
            break;
         case "<=":
            result = (doubleValue < criterion.getTestValue().doubleValue()) ||
                     (BigDecimal.valueOf(doubleValue).compareTo(BigDecimal.valueOf(criterion.getTestValue().doubleValue())) == 0);
            break;
         case "==":
            result = (BigDecimal.valueOf(doubleValue).compareTo(BigDecimal.valueOf(criterion.getTestValue().doubleValue())) == 0);
            break;
         case ">=":
            result = (doubleValue > criterion.getTestValue().doubleValue()) ||
                     (BigDecimal.valueOf(doubleValue).compareTo(BigDecimal.valueOf(criterion.getTestValue().doubleValue())) == 0);
            break;
         case ">":
            result = (doubleValue > criterion.getTestValue().doubleValue());
            break;
         case "!=":
            result = (BigDecimal.valueOf(doubleValue).compareTo(BigDecimal.valueOf(criterion.getTestValue().doubleValue())) != 0);
            break;
         default:
            break;
      }
      return result;
   }

   private static boolean testTwoDoubleTypeCriterion(double doubleValue, String operatorString,
                                                     TwoDoubleTypeCriterion criterion) {
      boolean result = false;

      switch (operatorString) {
         case "<<":
            result = (criterion.getMinValue().doubleValue() < doubleValue) &&
                     (doubleValue < criterion.getMaxValue().doubleValue());
            break;
         default:
            break;
      }
      return result;
   }
}
