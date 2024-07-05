package csi.server.connector.jdbc;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetezzaConnectionFactory extends JdbcConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(NetezzaConnectionFactory.class);

   public NetezzaConnectionFactory() {
   }

   @Override
   public String createConnectString(Map<String,String> propertiesMap) {
      // Connection string format : jdbc:netezza://host:port/database
      String host = propertiesMap.get(CSI_HOSTNAME);

      if ((host == null) || host.isEmpty()) {
         throw new RuntimeException("Missing required property " + CSI_HOSTNAME);
      }
      String port = propertiesMap.get(CSI_PORT);

      if ((port == null) || port.isEmpty()) {
         port = "5480";
      }
      String databaseName = propertiesMap.get(CSI_DATABASENAME);

      if ((databaseName == null) || databaseName.isEmpty()) {
         throw new RuntimeException("Missing required property " + CSI_DATABASENAME);
      }
      String connectString = new StringBuilder(getUrlPrefix())
                                       .append("//").append(host).append(':').append(port)
                                       .append('/').append(databaseName).toString();

      if (LOG.isDebugEnabled()) {
         LOG.debug("Netezza connection string : " + connectString);
      }
      return connectString;
   }
}
