package csi.server.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
   public static final String JAVA_UTIL_DATE_TOSTRING_PATTERN = "EEE MMM d HH:mm:ss z yyyy";
   public static final String YYYY_UNDER_MM_UNDER_DD_PATTERN = "yyyy_MM_dd";
   public static final String FULL_DATE_TIME_PATTERN = "MMM dd yyyy, hh:mm:ss a zzz";
   public static final String JAVA_UTIL_DATE_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.S";
   public static final String MM_DD_YY_DATE_TIME_PATTERN = "MM dd yy";
   public static final String YYYY_SLASH_MM_SLASH_DD_DASH_TIME_PATTERN = "yyyy/MM/dd-HH:mm:ss";

   public static final DateTimeFormatter JAVA_UTIL_DATE_TOSTRING_FORMATTER = DateTimeFormatter.ofPattern(JAVA_UTIL_DATE_TOSTRING_PATTERN);
   public static final DateTimeFormatter YYYY_UNDER_MM_UNDER_DD_FORMATTER = DateTimeFormatter.ofPattern(YYYY_UNDER_MM_UNDER_DD_PATTERN);
   public static final DateTimeFormatter FULL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(FULL_DATE_TIME_PATTERN);
   public static final DateTimeFormatter JAVA_UTIL_DATE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(JAVA_UTIL_DATE_DATE_TIME_PATTERN);
   public static final DateTimeFormatter MM_DD_YY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(MM_DD_YY_DATE_TIME_PATTERN);
   public static final DateTimeFormatter YYYY_SLASH_MM_SLASH_DD_DASH_TIME_FORMATTER = DateTimeFormatter.ofPattern(YYYY_SLASH_MM_SLASH_DD_DASH_TIME_PATTERN);

   private DateUtil() {
   }

   public static boolean validDate(final String dateString) {
      boolean valid = false;

      try {
         LocalDate.parse(dateString);
         valid = true;
      } catch (DateTimeParseException dtpe) {
      }
      return valid;
   }
   public static boolean validDate(final String dateString, DateTimeFormatter formatter) {
      boolean valid = false;

      try {
         LocalDate.parse(dateString, formatter);
         valid = true;
      } catch (DateTimeParseException dtpe) {
      }
      return valid;
   }

   public static boolean validTime(final String timeString) {
      boolean valid = false;

      try {
         LocalTime.parse(timeString);
         valid = true;
      } catch (DateTimeParseException dtpe) {
      }
      return valid;
   }
   public static boolean validTime(final String timeString, DateTimeFormatter formatter) {
      boolean valid = false;

      try {
         LocalTime.parse(timeString, formatter);
         valid = true;
      } catch (DateTimeParseException dtpe) {
      }
      return valid;
   }

   public static boolean validDateTime(final String dateTimeString) {
      boolean valid = false;

      try {
         LocalDateTime.parse(dateTimeString);
         valid = true;
      } catch (DateTimeParseException dtpe) {
      }
      return valid;
   }
   public static boolean validDateTime(final String dateTimeString, DateTimeFormatter formatter) {
      boolean valid = false;

      try {
         LocalDateTime.parse(dateTimeString, formatter);
         valid = true;
      } catch (DateTimeParseException dtpe) {
      }
      return valid;
   }
}
