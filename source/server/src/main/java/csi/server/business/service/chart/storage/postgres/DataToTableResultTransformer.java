package csi.server.business.service.chart.storage.postgres;

import java.util.List;
import java.util.function.Function;

import com.mongodb.DBObject;

import csi.server.business.service.chart.TableResult;

public class DataToTableResultTransformer implements Function<DBObject,TableResult> {
   @Override
   public TableResult apply(DBObject table) {
      TableResult tableResult = new TableResult();

      tableResult.setCategories((List<String>) table.get(ChartKeyConstants.CATEGORIES_KEY));
      tableResult.setChartLimitExceeded((boolean) table.get(ChartKeyConstants.CHART_LIMIT_KEY));
      tableResult.setDimensionValues((List<List<Number>>) table.get(ChartKeyConstants.DIMENSION_VALUES_KEY));
      tableResult.setTotalValues((List<Number>) table.get(ChartKeyConstants.TOTALS_KEY));
      tableResult.setTableLimitExceeded((boolean) table.get(ChartKeyConstants.TABLE_LIMIT_KEY));
      tableResult.setRowCount(tableResult.getCategories().size());
      return tableResult;
   }
}
