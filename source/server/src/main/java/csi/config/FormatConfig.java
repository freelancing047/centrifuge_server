package csi.config;

import java.text.DecimalFormat;

import csi.server.util.DateUtil;

public class FormatConfig extends AbstractConfigurationSettings {
   private String timeFormat = "HH:mm:ss";
   private String dateFormat = "MM/dd/yyyy";
   private String timestampFormat = "MM/dd/yyyy HH:mm:ss.S";
   private String numberFormat = "#0.########";

   public String getTimeFormat() {
      return timeFormat;
   }
   public String getDateFormat() {
      return dateFormat;
   }
   public String getTimestampFormat() {
      return timestampFormat;
   }
   public String getNumberFormat() {
      return numberFormat;
   }

   public void setTimeFormat(String format) {
      DateUtil.validTime(format);
      timeFormat = format;
   }
   public void setDateFormat(String format) {
      DateUtil.validDate(format);
      dateFormat = format;
   }
   public void setTimestampFormat(String format) {
      DateUtil.validDateTime(format);
      timestampFormat = format;
   }
   public void setNumberFormat(String formatArg) {
      try {
         new DecimalFormat(formatArg);
         numberFormat = formatArg;
      } catch (Exception exception) {
      }
   }
}
