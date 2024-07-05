package csi.server.business.service.chronos;

import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import csi.server.business.cachedb.querybuilder.TimelineQueryBuilder;
import csi.server.business.service.FilterActionsService;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.util.CsiTypeUtil;
import csi.server.util.DateUtil;
import csi.server.util.sql.SQLFactory;

public class TimelineActionsServiceUtil {
   public static final String COUNT_STAR_MEASURE_NAME = "Count (*)";

   public static String getParsedString(Object val) {
      String result = "null";

      if (val != null) {
         if (val instanceof java.sql.Date) {
            result = CsiTypeUtil.coerceString((java.sql.Date) val, DateTimeFormatter.ISO_LOCAL_DATE);
         } else if (val instanceof java.sql.Time) {
            result = CsiTypeUtil.coerceString((java.sql.Time) val, DateTimeFormatter.ISO_LOCAL_TIME);
         } else if (val instanceof java.sql.Timestamp) {
            result = CsiTypeUtil.coerceString((java.sql.Timestamp) val, DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
         }
      }
      return result;
   }

   @Inject
   private SQLFactory sqlFactory;

   public SQLFactory getSqlFactory() {
      return sqlFactory;
   }

   @Inject
   private FilterActionsService filterActionsService;

   public FilterActionsService getFilterActionsService() {
      return filterActionsService;
   }

   public TimelineActionsServiceUtil() {}

   public TimelineQueryBuilder getTimelineQueryBuilder(DataView dataView, TimelineViewDef viewDef) {
      TimelineQueryBuilder qb = new TimelineQueryBuilder(dataView, viewDef);

      qb.setSqlFactory(getSqlFactory());
      qb.setFilterActionsService(getFilterActionsService());
      return qb;
   }
}
