package csi.server.connector.jdbc.jstels;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.connector.jdbc.JdbcConnectionFactory;

public abstract class AbstractJStelsConnectionFactory extends JdbcConnectionFactory {
   public AbstractJStelsConnectionFactory() {
   }

   @Override
   public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
      return new ArrayList<String>();
   }

   @Override
   public List<CsiMap<String,String>> listSchemas(ConnectionDef dsdef, String catalog)
         throws CentrifugeException, GeneralSecurityException {
      return new ArrayList<CsiMap<String,String>>();
   }

   @Override
   public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
      return new ArrayList<String>();
   }

   @Override
   public List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type)
         throws CentrifugeException, GeneralSecurityException {
      synchronized (this) {
         List<SqlTableDef> list = new ArrayList<SqlTableDef>();
         Map<String,String> propertiesMap = getPropertiesMap(dsdef);
         String tableName = resolveTableName(propertiesMap);

         if (authorizedTable(tableName)) {
            list.add(new SqlTableDef(null, null, tableName, getAlias(tableName), "TABLE", null));
         }
         return list;
      }
   }

   /*
    * Overriding the JdbcConnectionFactory.getConnection method.
    */
   @Override
   public Connection getConnection(Map<String,String> propMap)
         throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException {
      if (LOG.isTraceEnabled()) {
         java.sql.DriverManager.setLogWriter(new PrintWriter(new OutputStreamWriter(System.out)));
      }
      String url = createConnectString(propMap);

      JdbcConnectionFactory.LOG.info("Connecting with connect string: " + super.obfuscatedPassword(url));

      Properties nativeProps = toNativeProperties(propMap);

      if (nativeProps.containsKey("fileExtension")) {
         url = url + (url.indexOf("?") == -1 ? "?" : "&") + "fileExtension=" + nativeProps.getProperty("fileExtension");
      }
      return getConnection(url, nativeProps);
   }

   public abstract String resolveTableName(Map<String,String> propertiesMap);
}
