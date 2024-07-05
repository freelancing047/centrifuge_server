package csi.server.business.cachedb.script;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import csi.server.common.model.DurationUnit;
import csi.server.util.CsiTypeUtil;

public class CsiScriptLib {
   public static long duration(Object date1, Object date2, String unit) {
      long duration = 0L;
      Date start = null;
      Date now = new Date();

      if ((date1 instanceof String) && ((String) date1).equalsIgnoreCase("now")) {
         start = now;
      } else {
         start = CsiTypeUtil.coerceDate(date1);
      }
      if (start != null) {
         Date end = null;

         if ((date2 instanceof String) && ((String) date2).equalsIgnoreCase("now")) {
            end = now;
         } else {
            end = CsiTypeUtil.coerceDate(date2);
         }
         if (end != null) {
            DurationUnit unitType = null;

            try {
               unitType = DurationUnit.valueOf(unit.toUpperCase());
            } catch (Throwable t) {
            }
            if (unitType == null) {
               unitType = DurationUnit.MILLISECONDS;
            }
            switch (unitType) {
               case YEARS:
                  duration = ChronoUnit.YEARS.between(LocalDate.of(((Date) date1).getYear(),
                                                                   ((Date) date1).getMonth(),
                                                                   ((Date) date1).getDate()),
                                                      LocalDate.of(((Date) date2).getYear(),
                                                                   ((Date) date2).getMonth(),
                                                                   ((Date) date2).getDate()));
                  break;
               case MONTHS:
                  duration = ChronoUnit.MONTHS.between(LocalDate.of(((Date) date1).getYear(),
                                                                    ((Date) date1).getMonth(),
                                                                    ((Date) date1).getDate()),
                                                       LocalDate.of(((Date) date2).getYear(),
                                                                    ((Date) date2).getMonth(),
                                                                    ((Date) date2).getDate()));
                  break;
               case WEEKS:
                  duration = ChronoUnit.WEEKS.between(LocalDate.of(((Date) date1).getYear(),
                                                                   ((Date) date1).getMonth(),
                                                                   ((Date) date1).getDate()),
                                                      LocalDate.of(((Date) date2).getYear(),
                                                                   ((Date) date2).getMonth(),
                                                                   ((Date) date2).getDate()));
                  break;
               case DAYS:
                  duration = TimeUnit.MILLISECONDS.toDays(end.getTime() - start.getTime());
                  break;
               case HOURS:
                  duration = TimeUnit.MILLISECONDS.toHours(end.getTime() - start.getTime());
                  break;
               case MINUTES:
                  duration = TimeUnit.MILLISECONDS.toMinutes(end.getTime() - start.getTime());
                  break;
               case SECONDS:
                  duration = TimeUnit.MILLISECONDS.toSeconds(end.getTime() - start.getTime());
                  break;
               case MILLISECONDS:
               default:
                  duration = Math.abs(end.getTime() - start.getTime());
                  break;
            }
         }
      }
      return duration;
   }

   public static long durationMillis(Object date1, Object date2) {
      return duration(date1, date2, DurationUnit.MILLISECONDS.toString());
   }

   public static String substringOneBased(Object obj, int start, int end) {
      String result = "";

      if (obj != null) {
         String s = CsiTypeUtil.coerceString(obj, null);
         int len = s.length();

         if ((start <= len) && ((end <= 0) || (start <= end))) {
            int usingStart = Math.max(start, 1) - 1;

            result = ((end <= 0) || (end >= len)) ? s.substring(usingStart) : s.substring(usingStart, end);
         }
      }
      return result;
   }
}
