package csi.server.business.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.ibm.icu.text.DateTimePatternGenerator;
import com.ibm.icu.util.ULocale;

import csi.server.common.service.api.InternationalizationServiceProtocol;

public class InternationalizationService implements InternationalizationServiceProtocol {
   private static final String PREFIX = "webapps/Centrifuge/resources/CentrifugeConstants";
   private static final String POSTFIX = ".properties";

   @Override
   public Map<String,String> getProperties(String localeName) {
      return getProperties(getCurrentLocale(localeName));
   }

   private static Locale getCurrentLocale(String localeName) {
      Locale currentLocale;
      String[] localeInfo = localeName.split("-");

      if (localeInfo.length == 0) {
         String language = "en";
         String country = "US";
         currentLocale = new Locale(language, country);
      } else if (localeInfo.length == 1) {
         String language = localeInfo[0];
         currentLocale = new Locale(language);
      } else {
         String language = localeInfo[0];
         String country = localeInfo[1];
         currentLocale = new Locale(language, country);
      }
      return currentLocale;
   }

   public static Map<String,String> getProperties(Locale currentLocale) {
      Map<String,String> properties = null;

      try {
         ResourceBundle defaultProperties = loadDefaultProperties();
         ResourceBundle localeSpecificProperties = loadLocaleSpecificProperties(currentLocale);
         properties = extractProperties(defaultProperties, localeSpecificProperties);
      } catch (IOException e) {
         e.printStackTrace();
      }
      return properties;
   }

   public static Map<String,String> getProperties(Locale currentLocale, List<String> keys) {
      Map<String,String> properties = null;

      try {
         ResourceBundle defaultProperties = loadDefaultProperties();
         ResourceBundle localeSpecificProperties = loadLocaleSpecificProperties(currentLocale);
         properties = extractProperties(keys, defaultProperties, localeSpecificProperties);
      } catch (IOException e) {
         e.printStackTrace();
      }
      return properties;
   }

   private static ResourceBundle loadDefaultProperties() throws IOException {
      ResourceBundle prop = null;
      File file = new File(PREFIX + POSTFIX);

      if (file.exists()) {
         try (FileInputStream fis = new FileInputStream(file);
              InputStreamReader isr = new InputStreamReader(fis)) {
            prop = new PropertyResourceBundle(isr);
         }
      }
      return prop;
   }

   private static ResourceBundle loadLocaleSpecificProperties(Locale currentLocale) throws IOException {
      ResourceBundle prop = null;
      File file = new File(PREFIX + "_" + currentLocale.getLanguage() + "_" + currentLocale.getCountry() + POSTFIX);

      if (!file.exists()) {
         file = new File(PREFIX + "_" + currentLocale.getLanguage() + POSTFIX);
      }
      if (file.exists()) {
         try (FileInputStream fis = new FileInputStream(file);
              InputStreamReader isr = new InputStreamReader(fis, "UTF-8")) {
            prop = new PropertyResourceBundle(isr);
         }
      }
      return prop;
   }

   private static Map<String,String> extractProperties(ResourceBundle defaultProperties,
                                                       ResourceBundle languageSpecificProperties) {
      Map<String,String> retVal = new HashMap<String,String>();

      if (defaultProperties != null) {
         for (String key : defaultProperties.keySet()) {
            String valueString = defaultProperties.getString(key);

            if ((languageSpecificProperties != null) && languageSpecificProperties.containsKey(key)) {
               valueString = languageSpecificProperties.getString(key);
            }
            retVal.put(key, valueString);
         }
      }
      return retVal;
   }

   private static Map<String,String> extractProperties(List<String> keys,
                                                       ResourceBundle defaultProperties,
                                                       ResourceBundle languageSpecificProperties) {
      Map<String,String> retVal = new HashMap<String,String>();

      for (String key : keys) {
         String valueString = "";

         if ((defaultProperties != null) && defaultProperties.containsKey(key)) {
            valueString = defaultProperties.getString(key);
         }
         if ((languageSpecificProperties != null) && languageSpecificProperties.containsKey(key)) {
            valueString = languageSpecificProperties.getString(key);
         }
         retVal.put(key, valueString);
      }
      return retVal;
   }

   public static String utf8toascii(String s) {
      StringBuilder sb = new StringBuilder();

      try {
         Charset utf8charset = Charset.forName("UTF-8");
         Charset iso88591charset = Charset.forName("ISO-8859-1");
         ByteBuffer inputBuffer = ByteBuffer.wrap(s.getBytes("UTF-8"));
         CharBuffer data = utf8charset.decode(inputBuffer);
         ByteBuffer outputBuffer = iso88591charset.encode(data);
         byte[] outputData = outputBuffer.array();

         sb.append(outputData);
      } catch (UnsupportedEncodingException e) {
      }
      return sb.toString();
   }

   @Override
   public String getBestPattern(String localeName, String subformat) {
      String pattern = null;

      try {
         ULocale locale = new ULocale(localeName);
         DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance(locale);
         pattern = dtpg.getBestPattern(subformat);
      } catch (IllegalArgumentException e) {
      }
      return pattern;
   }
}
