package csi.server.connector.config;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DriverList {
   private Map<String,JdbcDriver> drivers = Collections.synchronizedMap(new LinkedHashMap<String,JdbcDriver>());

   public Collection<JdbcDriver> getDrivers() {
      return drivers.values();
   }

   public JdbcDriver getDriver(String key) {
      return drivers.get(key);
   }

   public void setDrivers(Collection<JdbcDriver> drivers) {
      this.drivers.clear();

      if (drivers != null) {
         for (JdbcDriver driver : drivers) {
            String key = driver.getKey();

            if (key != null) {
               this.drivers.put(driver.getKey(), driver);
            }
         }
      }
   }
}
