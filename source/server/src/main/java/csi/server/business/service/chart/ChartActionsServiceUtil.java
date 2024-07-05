package csi.server.business.service.chart;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;

import csi.server.business.service.FilterActionsService;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.util.CsiTypeUtil;
import csi.server.util.DateUtil;
import csi.server.util.sql.SQLFactory;

public class ChartActionsServiceUtil {
   public static final String COUNT_STAR_MEASURE_NAME = "Count (*)";

   @Inject
   private SQLFactory sqlFactory;

   @Inject
   private FilterActionsService filterActionsService;

   public ChartActionsServiceUtil() {
   }

   public static String getParsedString(Object val) {
      String result = null;

      if (val == null) {
         result = ChartTableGenerator.SECRET_NULL_CHART_LABEL;
      } else {
         if (val instanceof java.sql.Date) {
            result = CsiTypeUtil.coerceString((java.sql.Date) val, DateTimeFormatter.ISO_LOCAL_DATE);
         } else if (val instanceof java.sql.Time) {
            result = CsiTypeUtil.coerceString((java.sql.Time) val, DateTimeFormatter.ISO_LOCAL_TIME);
         } else if (val instanceof java.sql.Timestamp) {
            result = CsiTypeUtil.coerceString((java.sql.Timestamp) val, DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
         } else {
            result = val.toString();
         }
      }
      return result;
   }

   public SQLFactory getSqlFactory() {
      return sqlFactory;
   }

   public FilterActionsService getFilterActionsService() {
      return filterActionsService;
   }

   public ChartQueryBuilder getChartQueryBuilder(DrillChartViewDef viewDef, DataView dataView,
                                                 List<String> categoryDrills) {
      ChartQueryBuilder qb = new ChartQueryBuilder();

      qb.setCategoryDrills(categoryDrills);
      qb.setViewDef(viewDef);
      qb.setDataView(dataView);
      qb.setSqlFactory(getSqlFactory());
      qb.setFilterActionsService(getFilterActionsService());
      return qb;
   }
}
