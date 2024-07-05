package csi.config;

import java.util.HashSet;
import java.util.Set;

import csi.server.common.exception.CentrifugeException;
import csi.server.connector.config.DriverList;
import csi.server.connector.config.JdbcDriver;

public class DBConfig extends AbstractConfigurationSettings {
   private static final int DEFAULT_RECORD_FETCH_SIZE = 256;

   private DriverList drivers;
   private int recordFetchSize = DEFAULT_RECORD_FETCH_SIZE;
   private boolean usingRemoteDB = false;

   private static String normalizeString(String s) {
       return ((s == null) || s.equals(" ")) ? s : s.trim();
    }

   @Override
   public void normalize() {
      for (JdbcDriver driver : drivers.getDrivers()) {
         driver.setName(normalizeString(driver.getName()));
         driver.setBaseUrl(normalizeString(driver.getBaseUrl()));
         driver.setTableNameQualifier(normalizeString(driver.getTableNameQualifier()));
         driver.setTableNameAliasQualifier(normalizeString(driver.getTableNameAliasQualifier()));
         driver.setFactory(normalizeString(driver.getFactory()));
         driver.setDriverClass(normalizeString(driver.getDriverClass()));
         driver.setDataViewingRole(normalizeString(driver.getDataViewingRole()));
         driver.setDriverAccessRole(normalizeString(driver.getDriverAccessRole()));
         driver.setSourceEditRole(normalizeString(driver.getSourceEditRole()));
         driver.setQueryEditRole(normalizeString(driver.getQueryEditRole()));
         driver.setConnectionEditRole(normalizeString(driver.getConnectionEditRole()));
         driver.setEscapeChar(normalizeString(driver.getEscapeChar()));
         driver.setSelectNullString(normalizeString(driver.getSelectNullString()));
      }
      if (recordFetchSize <= 0) {
         recordFetchSize = DEFAULT_RECORD_FETCH_SIZE;
      }
   }

   @Override
   public void validate() throws ConfigurationException, CentrifugeException {
      Set<String> keys = new HashSet<String>();

      for (JdbcDriver driver : drivers.getDrivers()) {
         driver.validate();

         if (keys.contains(driver.getKey())) {
            throw new ConfigurationException(
                  "Server configuration contains multiple driver configurations with key " + driver.getKey());
         }
         keys.add(driver.getKey());
      }
   }

   public DriverList getDrivers() {
      return drivers;
   }

   public void setDrivers(DriverList drivers) {
      this.drivers = drivers;
   }

   public int getRecordFetchSize() {
      return recordFetchSize;
   }

   public void setRecordFetchSize(int recordFetchSize) {
      this.recordFetchSize = recordFetchSize;
   }

   public boolean isUsingRemoteDB() {
      return usingRemoteDB;
   }

   public void setUsingRemoteDB(boolean usingRemoteDB) {
      this.usingRemoteDB = usingRemoteDB;
   }
}
