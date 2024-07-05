package csi.server.connector.jdbc;

import java.util.Map;

public class PostgreSQLConnectionFactory extends JdbcConnectionFactory {
   public PostgreSQLConnectionFactory() {
   }

   @Override
   public String createConnectString(Map<String,String> propertiesMap) {
      String host = propertiesMap.get(CSI_HOSTNAME);

      if ((host == null) || host.isEmpty()) {
         // throw new RuntimeException("Missing required property " + CSI_HOSTNAME);
         host = "localhost";
      }
      String port = propertiesMap.get(CSI_PORT);

      if ((port == null) || port.isEmpty()) {
         port = "5432";
      }
      String dbname = propertiesMap.get(CSI_DATABASENAME);
      StringBuilder buf =
         new StringBuilder(getUrlPrefix()).append("//").append(host).append(':').append(port);

      if ((dbname != null) && !dbname.isEmpty()) {
         buf.append('/').append(dbname);
      }
      return buf.toString();
   }
}
