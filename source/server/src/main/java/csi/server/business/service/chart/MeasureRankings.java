package csi.server.business.service.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.chart.PositiveIntegerTypeCriterion;
import csi.server.common.model.visualization.chart.SingleDoubleTypeCriterion;
import csi.server.common.model.visualization.chart.SingleIntegerTypeCriterion;
import csi.server.common.model.visualization.chart.ZeroToOneTypeCriterion;

public class MeasureRankings {
   // Maps Criteria to the measure they use
   private Map<ChartCriterion,Integer> criteriaToMeasureIndexMap;
   // Maps Criteria to threshold values
   private Map<ChartCriterion,Double> criteriaToMeasureValueMap;
   // Maps Criteria to threshold index
   private Map<ChartCriterion,Integer> criteriaThresholds;
   // Contains the max allowable index for each criteria duplicate
   private Map<ChartCriterion,Integer> criterionMaxIndexes = new HashMap<ChartCriterion,Integer>();

   public MeasureRankings(List<List<Number>> seriesData, List<ChartCriterion> criteria, ChartSettings chartSettings, int categoryCount){
      List<MeasureDefinition> measureDefinitions = chartSettings.getMeasureDefinitions();
      List<String> headers = new ArrayList<String>();

      if (chartSettings.isUseCountStarForMeasure() || measureDefinitions.isEmpty()) {
         headers.add(ChartActionsServiceUtil.COUNT_STAR_MEASURE_NAME);
      } else {
         for (MeasureDefinition md : measureDefinitions) {
            headers.add(md.getComposedName());
         }
      }
      criteriaToMeasureIndexMap = new HashMap<ChartCriterion, Integer>();
        //TODO: This is bad, but criterion.getColumnIndex was done incorrectly and can't be relied on until fixed.

      for (ChartCriterion criterion: criteria){
         criteriaToMeasureIndexMap.put(criterion, headers.indexOf(criterion.getColumnHeader()));
      }
      List<List<Double>> seriesDataList = rankCategoryMeasures(seriesData);
      criteriaThresholds = createThresholdMap(criteria, categoryCount);
      criteriaToMeasureValueMap = new HashMap<ChartCriterion, Double>();

      for (Map.Entry<ChartCriterion,Integer> entry: criteriaThresholds.entrySet()) {
         ChartCriterion criterion = entry.getKey();
         double thresholdValue = getThresholdValue(criterion, entry.getValue(), seriesDataList);

         criteriaToMeasureValueMap.put(criterion, thresholdValue);
      }
      for (Map.Entry<ChartCriterion,Double> entry : criteriaToMeasureValueMap.entrySet()) {
         ChartCriterion criterion = entry.getKey();
         Double thresholdValue = entry.getValue();
         String operatorString = criterion.getOperatorString();
         List<Number> measures = seriesData.get(criteriaToMeasureIndexMap.get(criterion));
         int threshold = criteriaThresholds.get(criterion);

         switch (operatorString) {
            case "<":
            case "<=":
            case "==":
            case ">=":
            case ">":
            case "!=":
            case "<<":
               continue;
            case "Top":
            case "Top%":
               populateMaxMap(criterion, thresholdValue, measures, threshold);
               continue;
            case "Bottom%":
            case "Bottom":
               // reverse this to get the count vs array position
               threshold = categoryCount - threshold - 1;
               populateMinMap(criterion, thresholdValue, measures, threshold);
               break;
            default:
               break;
         }
      }
   }

   public void populateMaxMap(ChartCriterion criterion, Double thresholdValue, List<Number> measures, int threshold) {
      List<Integer> duplicateIndexes = new ArrayList<Integer>();
      int index = 0;
      int definiteMatches = 0;
      BigDecimal threshholdDecimal = BigDecimal.valueOf(thresholdValue.doubleValue());

      for (Number measure : measures) {
         double value = measure.doubleValue();

         if (BigDecimal.valueOf(value).compareTo(threshholdDecimal) == 0) {
            duplicateIndexes.add(Integer.valueOf(index));
         } else {
            if (value > thresholdValue.doubleValue()) {
               definiteMatches++;
            }
         }
         index++;
      }
      int overflowDuplicates = (threshold + 1) - definiteMatches - duplicateIndexes.size();
      overflowDuplicates = Math.abs(overflowDuplicates);
      Integer maxIndex = duplicateIndexes.get(duplicateIndexes.size() - overflowDuplicates - 1);
      criterionMaxIndexes.put(criterion, maxIndex);
   }

   public void populateMinMap(ChartCriterion criterion, Double thresholdValue, List<Number> measures, int threshold) {
      List<Integer> duplicateIndexes = new ArrayList<Integer>();
      int index = 0;
      int definiteMatches = 0;
      BigDecimal threshholdDecimal = BigDecimal.valueOf(thresholdValue.doubleValue());

      for (Number measure : measures) {
         double value = measure.doubleValue();

         if (BigDecimal.valueOf(value).compareTo(threshholdDecimal) == 0) {
            duplicateIndexes.add(Integer.valueOf(index));
         } else {
            if (value < thresholdValue.doubleValue()) {
               definiteMatches++;
            }
         }
         index++;
      }
      int overflowDuplicates = (threshold + 1) - definiteMatches - duplicateIndexes.size();
      overflowDuplicates = Math.abs(overflowDuplicates);
      Collections.reverse(duplicateIndexes);
      Integer maxIndex = duplicateIndexes.get(duplicateIndexes.size() - overflowDuplicates - 1);
      criterionMaxIndexes.put(criterion, maxIndex);
   }

   public List<List<Double>> rankCategoryMeasures(List<List<Number>> measures) {
      List<List<Double>> seriesDataList = new ArrayList<List<Double>>();

      for (List<Number> measure : measures) {
         List<Double> numericValue = new ArrayList<Double>(measure.size());

         for (Number value : measure) {
            numericValue.add(value.doubleValue());
         }
         seriesDataList.add(numericValue);
      }
      for (List<Double> series : seriesDataList) {
         Collections.sort(series);
         Collections.reverse(series);
      }
      return seriesDataList;
   }

	/**
	 * provides the value of the given threshold
	 *
	 * @param columnIndex
	 * @param threshold
	 * @return
	 */
  private static double getThresholdValue(int columnIndex, int threshold, List<List<Double>> seriesDataList) {
     return seriesDataList.get(columnIndex).get(threshold).doubleValue();
  }

    public Map<ChartCriterion, Integer> createThresholdMap(List<ChartCriterion> criteria, int categoryCount) {
        HashMap<ChartCriterion, Integer> hashMap = new HashMap<ChartCriterion, Integer>();
        int newThreshold;
        for(ChartCriterion criterion: criteria){
            String operatorString = criterion.getOperatorString();
			switch (operatorString) {
			case "<":
			case "<=":
			case "==":
			case ">=":
			case ">":
			case "!=":
			case "<<":
				continue;
			case "Top": {
				Integer testValue = validateIntegerTestValue(categoryCount, criterion);

				if (testValue > categoryCount) {
					testValue = categoryCount;
				}

				hashMap.put(criterion, testValue - 1);
				continue;
			}
			case "Bottom": {
				Integer testValue = validateIntegerTestValue(categoryCount, criterion);

				if (testValue > categoryCount) {
					testValue = categoryCount;
				}

				hashMap.put(criterion, categoryCount - testValue);
				continue;
			}
			case "Top%": {
				Double testValue = validateDoubleTestValue(categoryCount, criterion);

				// validate for over 100%
				if (testValue > 100) {
					testValue = 100.0;
				}

				newThreshold = (int) Math.floor((categoryCount * testValue) / 100);

				// if we are above 0, take one category off, this will fix
				// returning more categories than needed.
				if (newThreshold > 0) {
					newThreshold--;
				}

				hashMap.put(criterion, newThreshold);
				continue;
			}
			case "Bottom%": {
				Double testValue = validateDoubleTestValue(categoryCount, criterion);

				// validate for over 100%
				if (testValue > 100) {
					testValue = 100.0;
				}

				newThreshold = categoryCount - ((int) Math.ceil((categoryCount * testValue) / 100));

				if (newThreshold == categoryCount) {
					newThreshold--;
				}

				hashMap.put(criterion, newThreshold);
			}
			default:
			}
        }
        return hashMap;
    }

    public int validateIntegerTestValue(int categoryCount, ChartCriterion criterion) {
        int testValue;

        if(criterion instanceof PositiveIntegerTypeCriterion) {
            testValue = ((PositiveIntegerTypeCriterion) criterion).getTestValue();
        } else {
            testValue = ((SingleIntegerTypeCriterion) criterion).getTestValue();
        }

        if(testValue == 0){
            testValue = 1;
        }
        if(testValue < 0){
            testValue = Math.abs(testValue);
        }
        return testValue;
    }

    public Double validateDoubleTestValue(int categoryCount, ChartCriterion criterion) {
        Double testValue;
        if(criterion instanceof ZeroToOneTypeCriterion) {
            testValue = ((ZeroToOneTypeCriterion) criterion).getTestValue();
        } else {
            testValue = ((SingleDoubleTypeCriterion) criterion).getTestValue();
        }

        if(testValue < 0){
            testValue = Math.abs(testValue);
        }
        return testValue;
    }

    public void setCriteriaToMeasureIndexMap(Map<ChartCriterion, Integer> criteriaToMeasureIndexMap) {
        this.criteriaToMeasureIndexMap = criteriaToMeasureIndexMap;
    }

    public Map<ChartCriterion, Integer> getCriteriaToMeasureIndexMap() {
        return criteriaToMeasureIndexMap;
    }

    private double getThresholdValue(ChartCriterion criterion, int threshold, List<List<Double>> seriesDataList) {
        return getThresholdValue(criteriaToMeasureIndexMap.get(criterion), threshold, seriesDataList);
    }


    public double getCriteriaThresholdValue(ChartCriterion criterion){

        return criteriaToMeasureValueMap.get(criterion);
    }

    public int getCriteriaIndexThreshold(ChartCriterion criterion){
        return criterionMaxIndexes.get(criterion);
    }

    public int getCriteriaMeasureIndex(ChartCriterion criterion){
        return criteriaToMeasureIndexMap.get(criterion);
    }

}
