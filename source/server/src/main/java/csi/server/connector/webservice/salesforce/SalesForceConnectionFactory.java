package csi.server.connector.webservice.salesforce;

/*
 * Connection factory for creating connections to salesforce.com.  This factory
 * will maintain a single instance of the connection to salesforce (Salesforce).
 * The actual credentials consist of a username, password and api token.  These
 * credentials are expected to be defined in jdbc-drivers.xml.
 */
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.centrifuge.sf.SFCredentials;
import com.centrifuge.sf.SalesForce;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.connector.AbstractConnectionFactory;

public class SalesForceConnectionFactory extends AbstractConnectionFactory {
   protected static final Logger LOG = LogManager.getLogger(SalesForceConnectionFactory.class);

   private static final String SF_USERNAME = "username";
   private static final String SF_PASSWORD = "password";
   private static final String SF_TOKEN = "token";

   protected SalesForce salesForce = null;

   public SalesForceConnectionFactory() {
   }

   /*
    * Connect to salesforce. Credentials should be provided in jdbc_drivers.xml
    */
   private void SFinit() {
      // Salesforce is a Singleton
      if (salesForce == null) {
         Properties props = getDefaultProperties();
         String username = props.getProperty(SF_USERNAME);
         String password = props.getProperty(SF_PASSWORD);
         String token = props.getProperty(SF_TOKEN);

         if ((username == null) || (password == null) || (token == null)) {
            LOG.error("Insufficient Salesforce Credentials");
            return;
         }
         SFCredentials creds = new SFCredentials(username, password, token);
         LOG.info("Connecting to salesforce.com...");
         salesForce = new SalesForce(creds);
      }
   }

   @Override
   public String createConnectString(Map<String,String> propertiesMap) {
      throw new IllegalStateException("Operation is not legal for this basic connection type");
   }

   @Override
   public Properties toNativeProperties(Map<String,String> propMap) {
      // construct properties to pass on to driver
      // based on the csi properties provided by the ui and default properties
      // in jdbc-driver.xml
      Properties nativeProps = new Properties();

      // for now just shove everything in as is
      nativeProps.putAll(propMap);
      return nativeProps;
   }

   @Override
   public Connection getConnection(String url, Properties props)
         throws ClassNotFoundException, SQLException, GeneralSecurityException {
      SFinit();
      return new SalesForceConnection(url, props, salesForce);
   }

   @Override
   public Connection getConnection(Map<String,String> propMap)
         throws SQLException, GeneralSecurityException, ClassNotFoundException {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
      return new ArrayList<String>();
   }

   @Override
   public List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table)
         throws CentrifugeException, GeneralSecurityException {
      return new ArrayList<ColumnDef>();
   }

   @Override
   public List<CsiMap<String,String>> listSchemas(ConnectionDef dsdef, String catalog)
         throws CentrifugeException, GeneralSecurityException {
      return new ArrayList<CsiMap<String,String>>();
   }

   @Override
   public List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type)
         throws CentrifugeException, GeneralSecurityException {
      return new ArrayList<SqlTableDef>();
   }

   @Override
   public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
      return new ArrayList<String>();
   }
}
