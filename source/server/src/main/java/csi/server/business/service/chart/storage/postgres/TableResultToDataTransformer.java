package csi.server.business.service.chart.storage.postgres;

import java.util.function.Function;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import csi.server.business.service.chart.TableResult;

public class TableResultToDataTransformer implements Function<TableResult,DBObject> {
   @Override
   public DBObject apply(TableResult table) {
      BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

      builder.append(ChartKeyConstants.CATEGORIES_KEY, table.getCategories());
      builder.append(ChartKeyConstants.DIMENSION_VALUES_KEY, table.getDimensionValues());
      // builder.append("nullCategory", table.getNullCategory());
      builder.append(ChartKeyConstants.TOTALS_KEY, table.getTotalValues());
      builder.add(ChartKeyConstants.TABLE_LIMIT_KEY, table.isTableLimitExceeded());
      builder.add(ChartKeyConstants.CHART_LIMIT_KEY, table.isChartLimitExceeded());
      return builder.get();
   }
}
