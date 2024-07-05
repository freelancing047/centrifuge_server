package csi.server.connector.jdbc;

import java.util.Map;

public class JtdsSqlServerConnectionFactory extends JdbcConnectionFactory {
   public JtdsSqlServerConnectionFactory() {
   }

   @Override
   public String createConnectString(Map<String,String> propertiesMap) {
      String host = propertiesMap.get(CSI_HOSTNAME);

      if ((host == null) || host.isEmpty()) {
         throw new RuntimeException("Missing required property " + CSI_HOSTNAME);
      }
      String port = propertiesMap.get(CSI_PORT);

      if ((port == null) || port.isEmpty()) {
         port = "1433";
      }
      String dbname = propertiesMap.get(CSI_DATABASENAME);

      // driver requires username password in connect string
      // or use getConnection(url, user, pass);
      String user = propertiesMap.get(CSI_RUNTIME_USERNAME);
      String pass = propertiesMap.get(CSI_RUNTIME_PASSWORD);

      StringBuilder buf = new StringBuilder(getUrlPrefix());

      buf.append(host).append(':').append(port);

      if ((dbname != null) && !dbname.isEmpty()) {
         buf.append('/').append(dbname);
      }
      String instanceName = propertiesMap.get(CSI_INSTANCENAME);

      if ((instanceName != null) && !instanceName.trim().isEmpty()) {
         buf.append(';').append("instance=").append(instanceName);
      }
      if ((user != null) && !user.trim().isEmpty()) {
         buf.append(';').append("user=").append(user)
            .append(';').append("password=").append(pass);
      }
      return buf.toString();
   }
}
