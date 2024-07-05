package csi.server.connector.jdbc;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.CsiSecurityManager;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.util.Format;

import oracle.jdbc.OracleConnection;

public class OracleConnectionFactory extends JdbcConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(OracleConnectionFactory.class);

    private static String PKI_PROVIDER = "oracle.security.pki.OraclePKIProvider";
    static {
        // init the oracle pki provider if it's available
        Class clz = null;
        try {
            clz = Class.forName(PKI_PROVIDER);
        } catch (ClassNotFoundException e) {
            // ignore
        }

        if (clz != null) {
            try {
                clz.newInstance();
            } catch (Throwable t) {
               LOG.error("Failed to initialize Oracle PKI provider", t);
            }
        }
    }

    public OracleConnectionFactory() {

    }

    @Override
    public Connection getConnection(ConnectionDef connectionDefIn) throws CentrifugeException, GeneralSecurityException {

        Connection myConnection = super.getConnection(connectionDefIn);

        if (getUseProxyAuthentication() && (null != myConnection)) {

            String myUserName = CsiSecurityManager.getDistinguishedName();
            Properties myProperties = new Properties();

            try {

                if (null != myUserName) {

                    myProperties.setProperty(OracleConnection.PROXY_DISTINGUISHED_NAME, myUserName);
                    ((OracleConnection) myConnection).openProxySession(OracleConnection.PROXYTYPE_DISTINGUISHED_NAME, myProperties);

                } else {

                    myUserName = CsiSecurityManager.getUserName();
                    if (null != myUserName) {

                        myProperties.clear();
                        myProperties.setProperty(OracleConnection.PROXY_USER_NAME, myUserName);
                        ((OracleConnection) myConnection).openProxySession(OracleConnection.PROXYTYPE_USER_NAME, myProperties);

                    } else {

                        throw new CentrifugeException("Unable to find authorized username");
                    }
                }

            } catch (Exception myException) {

                try {

                    myConnection.close();

                } catch (Exception IGNORE) {
                }
                throw new CentrifugeException("Caught exception proxying user " + Format.value(myUserName));
            }
        }
        return myConnection;
    }

   @Override
   public String createConnectString(Map<String, String> propertiesMap) {
      String host = propertiesMap.get(CSI_HOSTNAME);

      if ((host == null) || host.isEmpty()) {
         throw new RuntimeException("Missing required property " + CSI_HOSTNAME);
      }
      String port = propertiesMap.get(CSI_PORT);

      if ((port == null) || port.isEmpty()) {
         port = "1521";
      }
      String dbname = propertiesMap.get(CSI_DATABASENAME);
      StringBuilder buf = new StringBuilder(getUrlPrefix());

      buf.append('@').append(host).append(':').append(port);

      if ((dbname != null) && !dbname.isEmpty()) {
         buf.append(':').append(dbname);
      }
      return buf.toString();
   }
}
