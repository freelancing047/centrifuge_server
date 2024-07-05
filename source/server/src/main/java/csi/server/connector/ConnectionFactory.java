package csi.server.connector;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import csi.server.common.dto.CsiMap;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;

public interface ConnectionFactory {
   public boolean isInPlace();

   public boolean isSimpleLoader();

   public Connection getConnection(String url, Properties props)
         throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException;

   /*
    * This method should only be invoked if the connection type is NOT "Legacy"
    */
   public Connection getConnection(GenericProperties propertiesIn)
         throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException;

   public Connection getConnection(Map<String,String> propMap)
         throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException;

   public Connection getConnection()
         throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException;

   /**
    * Create a connection String for the JDBC connection driver
    *
    * @param propertiesMap
    * @return A JDBC connection String
    */
   public String createConnectString(Map<String,String> propertiesMap);

   public String getDriverClass();

   public void setDriverClass(String className);

   public String getUrlPrefix();

   public void setJdbcFactory(boolean f);

   public void setUrlPrefix(String urlPrefix);

   public List<Integer> getAuthErrorCodes();

   public void setAuthErrorCodes(List<Integer> codes);

   public List<String> getAuthSqlStates();

   public void setAuthSqlStates(List<String> states);

   public Properties getDefaultProperties();

   public void setDefaultProperties(Properties props);

   public String getTypeName();

   public void setTypeName(String name);

   public String getTableNameQualifier();

   public void setTableNameQualifier(String q);

   public String getTableNameAliasQualifier();

   public void setTableNameAliasQualifier(String tableNameAliaseQualifier);

   public void setTypeMapping(Properties typeMapping);

   public Properties getTypeMapping();

   public CsiDataType getMappedCsiType(String dataTypeIn);

   public void setCastMapping(Properties typeMapping);

   public Properties getCastMapping();

   public String getMappedCastingString(CsiDataType dataTypeIn);

   public boolean getHasHiddenFlag();

   public void setDriverAccessRole(String createRole);

   public String getDriverAccessRole();

   public void setSourceEditRole(String sourceEditRole);

   public String getSourceEditRole();

   public void setConnectionEditRole(String connectionAccessRole);

   public String getConnectionEditRole();

   public void setQueryEditRole(String queryAccessRole);

   public String getQueryEditRole();

   public void setDataViewingRole(String dataViewerRole);

   public String getDataViewingRole();

   public String getRemarks();

   public void setRemarks(String s);

   public void setEscapeChar(String escapeChar);

   public String getEscapeChar();

   public String getSelectNullString();

   public void setSelectNullString(String s);

   public void setSortOrder(Integer sortOrderIn);

   public Integer getSortOrder();

   public void setTableAliasMap(Map<String,String> tableAliasMapIn);

   public Map<String,String> getTableAliasMap();

   public void setCapcoColumnMap(Map<String,String> capcoColumnMapIn);

   public Map<String,String> getCapcoColumnMap();

   public void setCapcoStringMap(Map<String,String> capcoColumnMapIn);

   public Map<String,String> getCapcoStringMap();

   public Boolean getBlockCustomQueries();

   public void setBlockCustomQueries(Boolean blockCustomQueriesIn);

   public String getQualifiedName(SqlTableDef tableDef);

   public String getQuotedName(String name);

   public Connection getConnection(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException;

   public List<String> listTableTypes(ConnectionDef dsdef, String catalog, String schema)
         throws CentrifugeException, GeneralSecurityException;

   public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException;

   public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException;

   public List<String> listExtractedSchemas(ConnectionDef dsdef, String catalog)
         throws CentrifugeException, GeneralSecurityException;

   public List<CsiMap<String,String>> listSchemas(ConnectionDef dsdef, String catalog)
         throws CentrifugeException, GeneralSecurityException;

   public List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type)
         throws CentrifugeException, GeneralSecurityException;

   public List<ColumnDef> listColumnDefs(ConnectionDef connectionIn, SqlTableDef tableIn)
         throws CentrifugeException, GeneralSecurityException;

   public List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table)
         throws CentrifugeException, GeneralSecurityException;

   public List<Set<String>> getSourceFilters(ConnectionDef connectionIn);

   public void setCastNulls(Boolean castNullsIn);

   public Boolean getCastNulls();

   public String castNull(CsiDataType dataTypeIn);

   public String castExpression(String expressionIn, ColumnDef sourceColumnIn, ColumnDef targetColumnIn);

   public String castExpression(String expressionIn, CsiDataType castIn);

   public void setUseProxyAuthentication(Boolean useProxyAuthenticatioIn);

   public boolean getUseProxyAuthentication();
}
