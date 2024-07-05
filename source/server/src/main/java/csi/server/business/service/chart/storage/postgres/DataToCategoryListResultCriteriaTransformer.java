package csi.server.business.service.chart.storage.postgres;

import java.util.List;
import java.util.function.Function;

import com.mongodb.DBObject;

import csi.server.business.service.chart.CriteriaFilterOperator;
import csi.server.business.service.chart.TableResult;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.DrillChartViewDef;

public class DataToCategoryListResultCriteriaTransformer implements Function<DBObject,List<String>> {
   private List<ChartCriterion> criteria;
   private DrillChartViewDef viewDef;

   public DataToCategoryListResultCriteriaTransformer(List<ChartCriterion> criteria, DrillChartViewDef viewDef) {
      this.viewDef = viewDef;
      this.criteria = criteria;
   }

   @Override
   public List<String> apply(DBObject dbObject) {
      List<String> categories = (List<String>) dbObject.get(ChartKeyConstants.CATEGORIES_KEY);
      List<List<Number>> table = (List<List<Number>>) dbObject.get(ChartKeyConstants.DIMENSION_VALUES_KEY);
      TableResult result = new TableResult();

      result.setCategories(categories);
      result.setDimensionValues(table);
      result.setRowCount(categories.size());

      CriteriaFilterOperator operator = new CriteriaFilterOperator(result, criteria, viewDef);
      result = operator.getFilteredResults();

      return result.getCategories();
   }
}
