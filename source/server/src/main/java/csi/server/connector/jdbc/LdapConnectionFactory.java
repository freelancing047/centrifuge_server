package csi.server.connector.jdbc;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;

public class LdapConnectionFactory extends JdbcConnectionFactory {
   protected static final Logger LOG = LogManager.getLogger(LdapConnectionFactory.class);

    private static final String CSI_LDAP_BASEDN = "csi.ldap.baseDN";
    private static final String CSI_LDAP_DEREF_ALAIS = "csi.ldap.derefAlias";

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
        // Properties are CaSe SeNsItIvE !!!
        String connString = "jdbc:ldap://";
        connString += propertiesMap.get(CSI_HOSTNAME);

        if (propertiesMap.containsKey(CSI_PORT) && !propertiesMap.get(CSI_PORT).trim().isEmpty()) {
            connString += ":";
            connString += propertiesMap.get(CSI_PORT);
        } else {
            connString += ":389";
        }
        if (propertiesMap.containsKey(CSI_USERNAME) && !propertiesMap.get(CSI_USERNAME).trim().isEmpty()) {
            connString += ";user=";
            connString += propertiesMap.get(CSI_USERNAME);
        }
        if (propertiesMap.containsKey(CSI_PASSWORD) && !propertiesMap.get(CSI_PASSWORD).trim().isEmpty()) {
            connString += ";password=";
            connString += propertiesMap.get(CSI_PASSWORD);
        }

        if (propertiesMap.containsKey(CSI_LDAP_BASEDN)) {
            connString += ";baseDN=";
            connString += propertiesMap.get(CSI_LDAP_BASEDN);
        } else {
            throw new RuntimeException("Unable to set up LDAP without Base DN.");
        }
        connString += ";useCleartext=true";

        return connString;
    }

    @Override
    public synchronized List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        // TODO Auto-generated method stub
        return new ArrayList<String>();
    }

    @Override
    public synchronized List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        // TODO Auto-generated method stub'
        return super.listCatalogs(dsdef);
    }

    @Override
    public synchronized List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog)
            throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<CsiMap<String, String>>();
    }

    @Override
    public synchronized List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type)
            throws CentrifugeException, GeneralSecurityException {
        // TODO Auto-generated method stub
        List<SqlTableDef> toReturn = super.listTableDefs(dsdef, catalog, schema, type);
        if (LOG.isDebugEnabled()) {
            countResultsPerTable(dsdef, toReturn);
        }
        return toReturn;
    }

   private void countResultsPerTable(ConnectionDef dsdef, List<SqlTableDef> toReturn) {
      LOG.debug("Examining table results.");

      try (Connection conn = this.getConnection(dsdef);
           Statement stmt = conn.createStatement()) {
         for (SqlTableDef td : toReturn) {
            try {
               //exclude tables with access right controls due to bug in driver
               if (td.getTableName().contains("ight")) {
                  continue;
               }
               String q = "SELECT * FROM \"" + td.getTableName() + "\"";
               LOG.trace("SQL Query: " + q);

               try (ResultSet rs = stmt.executeQuery(q)) {
                  int i = 0;

                  while (rs.next()) {
                     i++;
                  }
                  LOG.info("Table " + td.getTableName() + ": " + i);
               }
            } catch (Exception e) {
               LOG.error(e.toString());
            }
         }
      } catch (Exception e) {
         LOG.error(e.toString());
      }
   }

    @Override
    public synchronized List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table)
            throws CentrifugeException, GeneralSecurityException {
        // TODO Auto-generated method stub
        return super.listColumnDefs(dsdef, catalog, schema, table);
    }

}
