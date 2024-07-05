package csi.server.util;

import java.sql.Time;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.FormatConfig;
import csi.server.common.enumerations.CsiDataType;

public class CsiTypeUtil {
   private static final Logger LOG = LogManager.getLogger(CsiTypeUtil.class);

   public static String DEFAULT_NUMBER_FORMAT = "#0.0########";
    private static Pattern timePattern = Pattern.compile("^\\d+:\\d+:\\d+$");

    public static Object coerceType(Object obj, CsiDataType destType, String formatStr) {
      Object convertedValue = null;

      if (obj != null) {
         CsiDataType usingDestType = (destType == null) ? CsiDataType.String : destType;

         switch (usingDestType) {
            case String:
               convertedValue = coerceString(obj, formatStr);
               break;
            case Integer:
               convertedValue = coerceLong(obj);
               break;
            case Number:
               convertedValue = coerceDecimal(obj);
               break;
            case Date:
               convertedValue = coerceDate(obj);
               break;
            case Time:
               convertedValue = coerceTime(obj);
               break;
            case DateTime:
               convertedValue = coerceTimestamp(obj);
               break;
            case Boolean:
               convertedValue = coerceBoolean(obj);
               break;
            default:
               convertedValue = obj; // coerced to be itself.
               break;
         }
      }
      return convertedValue;
   }

   public static Boolean coerceBoolean(Object val) {
      Boolean convertedValue = null;

      if (val != null) {
         if (val instanceof Boolean) {
            convertedValue = (Boolean) val;
         } else if (val instanceof Number) {
            convertedValue = Boolean.valueOf(((Number) val).longValue() > 0);
         } else if (val instanceof String) {
            String s = (String) val;

            if (s.equalsIgnoreCase("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on")) {
               convertedValue = Boolean.TRUE;
            } else {
               convertedValue = Boolean.FALSE;
            }
         } else {
            convertedValue = Boolean.FALSE;
         }
      }
      return convertedValue;
   }

    public static Long coerceLong(Object val) {
        if (val == null) {
            return null;
        }

        Long convertedValue = null;
        if (val instanceof Long) {

            convertedValue = (Long) val;

        } else if (val instanceof Number) {

            convertedValue = ((Number) val).longValue();

        } else if (val instanceof String) {

            try {
                convertedValue = Long.parseLong((String) val);
            } catch (NumberFormatException nfex) {
               LOG.debug(String.format("Unable to parse integer value '%s'", val));
                convertedValue = null;
            }
        } else if (val instanceof java.util.Date) {
            convertedValue = Long.valueOf(((java.util.Date) val).getTime());

        } else if (val instanceof Boolean) {
            convertedValue = Long.valueOf(((Boolean) val).booleanValue() ? 0L : 1L);
        } else {
            throw new IllegalArgumentException("Unable to coerce " + val.getClass() + " to Long");
        }

        return convertedValue;
    }

    public static Double coerceDecimal(Object val) {
        if (val == null) {
            return null;
        }

        Double convertedValue = null;

        if (val instanceof Number) {

            convertedValue = ((Number) val).doubleValue();

        } else if (val instanceof String) {

            try {
                convertedValue = Double.parseDouble((String) val);
            } catch (NumberFormatException nfex) {
               LOG.debug(String.format("Unable to parse integer value '%s'", val));
                convertedValue = null;
            }
        } else if (val instanceof java.util.Date) {
            convertedValue = Double.valueOf(((java.util.Date) val).getTime());
        } else if (val instanceof Boolean) {
            convertedValue = Double.valueOf(((Boolean) val).booleanValue() ? 0D : 1D);
        } else {
            throw new IllegalArgumentException("Unable to coerce " + val.getClass() + " to Double");
        }

        return convertedValue;
    }

    public static java.sql.Time coerceTime(Object val) {
        if (val == null) {
            return null;
        }

        java.sql.Time convertedValue = null;

        if (val instanceof java.sql.Time) {
            convertedValue = (java.sql.Time) val;

        } else if (val instanceof String) {
            String strval = (String) val;

            try {
                FormatConfig config = Configuration.getInstance().getFormatConfig();
                LocalDateTime localDateTime = LocalDateTime.from(DateTimeFormatter.ofPattern(config.getTimeFormat()).parse(strval));
                java.util.Date parsed = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                convertedValue = new java.sql.Time(parsed.getTime());
            } catch (Exception e) {
                // ignore
            }

            if (convertedValue == null) {
               Long dateTimeMillis = parseMmDdYyString(strval);

               if (dateTimeMillis != null) {
                  convertedValue = new java.sql.Time(dateTimeMillis.longValue());
               }
            }

        } else if (val instanceof Number) {

            convertedValue = new java.sql.Time(((Number) val).longValue());

        } else if (val instanceof java.util.Date) {
            convertedValue = new java.sql.Time(((java.util.Date) val).getTime());

        } else {
            throw new IllegalArgumentException("Unable to coerce " + val.getClass() + " to Time");
        }

        if (convertedValue == null) {
           LOG.debug(String.format("Failed to coerce '%s' to time value", val));
        }

        return convertedValue;
    }

    public static java.sql.Date coerceDate(Object val) {
        if (val == null) {
            return null;
        }

        java.sql.Date convertedValue = null;

        if (val instanceof String) {
            Long dateTimeMillis = parseMmDdYyString((String) val);

            if (dateTimeMillis != null) {
               convertedValue = new java.sql.Date(dateTimeMillis.longValue());
            }

        } else if (val instanceof Number) {
            convertedValue = new java.sql.Date(((Number) val).longValue());

        } else if (val instanceof java.util.Date) {

            convertedValue = new java.sql.Date(((java.util.Date) val).getTime());

        } else {

            throw new IllegalArgumentException("Unable to coerce " + val.getClass() + " to Date");
        }

        if (convertedValue == null) {
           LOG.debug(String.format("Failed to coerce '%s' to date value", val));
        }

        return convertedValue;
    }

    public static java.sql.Timestamp coerceTimestamp(Object val) {
        if (val == null) {
            return null;
        }

        java.sql.Timestamp convertedValue = null;

        /*
         * If we are working on a Date/Time field, we need to create a Timestamp
         * object for Derby. The challenge here is we have no idea what the
         * string format of the date/time will be. So, we use a third party
         * Calendar Parser that covers most common date/time strings. We then
         * construct a java.sql.timestamp that will get inserted.
         */
        if (val instanceof String) {
           Long dateTimeMillis = parseMmDdYyString((String) val);

           if (dateTimeMillis != null) {
                convertedValue = new java.sql.Timestamp(dateTimeMillis.longValue());
            }

        } else if (val instanceof Number) {
            long millis = ((Number) val).longValue();

            convertedValue = new java.sql.Timestamp(millis);

        } else if (val instanceof java.sql.Timestamp) {
            convertedValue = (java.sql.Timestamp) val;

        } else if (val instanceof java.util.Date) {

            convertedValue = new java.sql.Timestamp(((java.util.Date) val).getTime());

        } else {

            throw new IllegalArgumentException("Unable to coerce " + val.getClass() + " to Timestamp");
        }

        if (convertedValue == null) {
           LOG.debug(String.format("Failed to coerce '%s' to timestamp value", val));
        }

        return convertedValue;
    }

    public static String coerceString(Object val, String formatStr) {
        if (val == null) {
            return null;
        }

        String asString;
        if (val instanceof java.lang.String) {
            asString = (String) val;

        } else if (val instanceof java.util.Date) {

            asString = formatDateValue((java.util.Date) val, formatStr);

        } else if (val instanceof java.util.GregorianCalendar) {

            asString = formatDateValue(((java.util.GregorianCalendar) val).getTime(), formatStr);

        } else if (val instanceof Number) {

            asString = formatNumberValue((Number) val, formatStr);

        } else if (val instanceof Boolean) {

            asString = ((Boolean) val).toString();
        } else {
           LOG.debug(String.format("Unable to coerce '%s' from type %s to String using format '%s'", val, val.getClass().getName(), formatStr));
            asString = val.toString();
        }

        return asString;
    }

    /**
     * Given a Number and a format, generate a formated number string.
     *
     * @param val
     *            the number to format
     * @param formatStr
     *            type or pattern to apply to the number.
     * @return formatted string.
     */
   public static String formatNumberValue(Number val, String formatStr) {
      DecimalFormat formatter = new DecimalFormat(DEFAULT_NUMBER_FORMAT);

      if (formatStr != null) {
         try {
            formatter = new DecimalFormat(formatStr);
         } catch (IllegalArgumentException ex) {
            LOG.warn(String.format("Illegal decimal format string: '%s'", formatStr));
            formatter = new DecimalFormat(DEFAULT_NUMBER_FORMAT);
         }
      }
      return formatter.format(val);
   }

   private static String stripTimeIfZeroes(String dateStr) {
      String result = dateStr;
      int idx = dateStr.indexOf("00:00:00");

      if (idx > 0) {
         result = dateStr.substring(0, idx - 1);
      }
      return result;
   }

   public static String coerceString(java.util.Date obj, DateTimeFormatter formatter) {
      String result = null;

      if (obj != null) {
         result = stripTimeIfZeroes(
                     formatter.format(Instant.ofEpochMilli(obj.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
      }
      return result;
   }

   public static String coerceString(java.sql.Date obj, DateTimeFormatter formatter) {
      String result = null;

      if (obj != null) {
         result = stripTimeIfZeroes(
                     formatter.format(Instant.ofEpochMilli(obj.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
      }
      return result;
   }

   public static String coerceString(java.sql.Time obj, DateTimeFormatter formatter) {
      String result = null;

      if (obj != null) {
         result = formatter.format(Instant.ofEpochMilli(obj.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());
      }
      return result;
   }

   public static String coerceString(java.sql.Timestamp obj, DateTimeFormatter formatter) {
      String result = null;

      if (obj != null) {
         result = stripTimeIfZeroes(
                     formatter.format(Instant.ofEpochMilli(obj.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
      }
      return result;
   }

   public static String coerceString(Calendar obj, DateTimeFormatter formatter) {
      String result = null;

      if (obj != null) {
         result = stripTimeIfZeroes(
                     formatter.format(Instant.ofEpochMilli(obj.getTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
      }
      return result;
   }

   public static String formatDateValue(java.util.Date val, String formatStr) {
      FormatConfig config = Configuration.getInstance().getFormatConfig();
      String usingFormat;

      if (val instanceof java.sql.Time) {
         usingFormat = StringUtils.isEmpty(formatStr) ? config.getTimeFormat() : formatStr;
      } else if (val instanceof java.sql.Date) {
         usingFormat = StringUtils.isEmpty(formatStr) ? config.getDateFormat() : formatStr;
      } else {
         usingFormat = StringUtils.isEmpty(formatStr) ? config.getTimestampFormat() : formatStr;
      }
      return stripTimeIfZeroes(
                DateTimeFormatter.ofPattern(usingFormat)
                   .format(Instant.ofEpochMilli(val.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
   }

   public static Long parseMmDdYyString(String mmDdYyStr) {
       try {
           if (timePattern.matcher(mmDdYyStr).matches()) {
               List<Integer> parts = Lists.newArrayList(Splitter.on(':').split(mmDdYyStr)).stream().map(Integer::parseInt).collect(Collectors.toList());
               Integer hours = parts.get(0);
               int minutes = hours * 60 + parts.get(1);
               int seconds = minutes * 60 + parts.get(2);
               long correctionForTimezone = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant().toEpochMilli();

               return (long) (seconds * 1000) + correctionForTimezone;

           }
       } catch (NumberFormatException ignored) {
       }
       try {
           LocalDate localDate = LocalDate.parse(mmDdYyStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
           LocalDateTime localDateTime = localDate.atStartOfDay();
           return localDateTimeToEpochMilli(localDateTime);
       } catch (DateTimeParseException ignored) {
       }
       try {
           LocalDateTime localDateTime = LocalDateTime.parse(mmDdYyStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
           return localDateTimeToEpochMilli(localDateTime);
       } catch (DateTimeParseException dtpe) {
       throw dtpe;
       }
   }

    public static Long localDateTimeToEpochMilli(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(TimeZone.getDefault().toZoneId());
        return zonedDateTime.toInstant().toEpochMilli();
    }
}
