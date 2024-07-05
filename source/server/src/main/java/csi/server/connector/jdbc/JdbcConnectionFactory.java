package csi.server.connector.jdbc;

import java.io.File;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.CsiSecurityManager;
import csi.server.business.service.TestActionsService;
import csi.server.common.dto.CsiMap;
import csi.server.common.enumerations.JdbcTableFilters;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.util.ConnectorSupport;
import csi.server.common.util.Format;
import csi.server.connector.AbstractConnectionFactory;
import csi.server.util.SqlUtil;
import csi.server.ws.filemanager.FileProcessor;

public class JdbcConnectionFactory extends AbstractConnectionFactory {
   protected static final Logger LOG = LogManager.getLogger(JdbcConnectionFactory.class);

    private static final Pattern PASSWORD_SEARCH_PATTERN = Pattern.compile("(?i)(password|pass|passwd|PWD)=[.[^;]]*");

    /**
     * Get a connection for the indicated URL (connection string). If the
     * attempt causes an SQLException, then check the error code and SQL state
     * to see whether the problem is missing or incorrect user/password. If this
     * is the problem, then throw GeneralSecurityException.
     *
     * @return a DB connection.
     */
    public Connection getConnection(String url, Properties props) throws ClassNotFoundException, SQLException, GeneralSecurityException, CentrifugeException {

        Connection connection = null;
        try {
            if (url == null) {
                return connection;
            }
            synchronized(this) {
                Driver driver = DriverManager.getDriver(url);
                if (driver != null) {
                    connection = driver.connect(url, props);
                }
            }
        } catch (SQLException sqle) {
            Integer myErrorCode = Integer.valueOf(sqle.getErrorCode());
            String mySqlState = sqle.getSQLState();
            List<Integer> authErrorCodes = getAuthErrorCodes();
            List<String> authSqlStates = getAuthSqlStates();
            if (((authErrorCodes != null) && authErrorCodes.contains(myErrorCode))
                    || ((authSqlStates != null) && authSqlStates.contains(mySqlState))) {
                if (null != props.getProperty(CSI_FORCE_USERNAME)) {
                    throw new GeneralSecurityException(TestActionsService.PASSWORD_REQUIRED + props.getProperty(CSI_FORCE_USERNAME));
                } else {
                    throw new GeneralSecurityException("Authentication required");
                }
            } else {
                LOG.error("SQLSTATE: " + sqle.getSQLState() + " ERROR CODE: " + sqle.getErrorCode());
                LOG.error(sqle);
                throw sqle;
            }
        }

        if ((connection != null) && (connection.getTransactionIsolation() != Connection.TRANSACTION_NONE)) {
            // only turn off auto commit if transactions are supported. The Jstels
            // drivers throws an exception when we try to turn off auto commit if the connection
            // is in read only mode.
            connection.setAutoCommit(false);
        }
        return connection;
    }

    /* This method should only be invoked if the connection type
     * is NOT "Legacy"
     */
    @Override
    public Connection getConnection(Map<String, String> propMap) throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException {

        // // convert file token to actual path
        // // Note: this is only relavent to file based drivers
        // String path = resolveFilePath( propMap );
        // if (path != null) {
        // propMap.put( CSI_FILEPATH, path );
        // }

        String url = createConnectString(propMap);

        LOG.info("Connecting with connect string: " + obfuscatedPassword(url));

        Properties nativeProps = toNativeProperties(propMap);
        Connection conn = getConnection(url, nativeProps);
        return conn;
    }

    public Connection getConnection() throws CentrifugeException, GeneralSecurityException {

        ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

        if ((null != mySupport) && mySupport.isRestricted(getTypeName())) {

            try {
                return this.getConnection(getPropertiesMap(this.getTypeName()));
            } catch (SQLException e) {
                LOG.error("Failed to get connection.", e);
                throw new GeneralSecurityException("Failed to get connection.");
            } catch (ClassNotFoundException e) {
                LOG.error("Failed to locate driver.", e);
                throw new CentrifugeException("Failed to locate driver.", e);
            } catch (GeneralSecurityException e) {
                LOG.error("Security blocked connection.", e);
                throw new GeneralSecurityException("Security blocked connection.");
            } catch (Throwable t) {
                LOG.error("Failed to get connection.", t);
                throw new GeneralSecurityException("Failed to get connection.");
            }

        } else {

            try {
                return this.getConnection(getPropertiesMap(this.getTypeName()));
            } catch (SQLException e) {
                throw new GeneralSecurityException("Failed to get connection.", e);
            } catch (ClassNotFoundException e) {
                throw new CentrifugeException("Check presence of JDBC driver.", e);
            } catch (GeneralSecurityException e) {
                throw e;
            } catch (Throwable t) {
                throw new GeneralSecurityException("Failed to get connection.", t);
            }
        }
    }

    public synchronized String resolveFilePath(Map<String, String> propMap) throws RuntimeException {

        String path = Format.normalizePath(propMap.get(CSI_FILEPATH));
        String ftok = propMap.get(CSI_FILETOKEN);

        if (path != null) {
            return path;
        }

        if (ftok != null) {
            File fileFromToken = FileProcessor.getFileFromToken(ftok);
            if (fileFromToken.exists()) {
                path = fileFromToken.getPath();
            } else {
                path = "userfiles/" + CsiSecurityManager.getUserName() + "/datafiles/" + fileFromToken.getName();
            }

        } else {
            // use local file path for existing legacy dataviews
            path = Format.normalizePath(propMap.get(CSI_LOCALFILEPATH));
        }

        if (path == null) {
            throw new RuntimeException("Missing required property " + CSI_FILEPATH);
        }

        return path;
    }

    protected String obfuscatedPassword(String url) {
        Matcher matcher = PASSWORD_SEARCH_PATTERN.matcher(url);
        return matcher.replaceAll("$1=*****");

    }

    public synchronized Properties toNativeProperties(Map<String, String> propMap) {

        // construct properties to pass on to driver
        Properties connectProps = new Properties();

        // add default properties
        connectProps.putAll(getDefaultProperties());

        // add username and password
        String user = propMap.containsKey(CSI_RUNTIME_USERNAME) ? propMap.get(CSI_RUNTIME_USERNAME) : null;
        String pass = propMap.containsKey(CSI_RUNTIME_PASSWORD) ? propMap.get(CSI_RUNTIME_PASSWORD) : null;

        /*
         * if there's no runtime override, check to see if we have
         * credentials declared for this connection def
         */
        if (((null == user) || (0 == user.trim().length()))
                && propMap.containsKey(CSI_USERNAME)) {
            user = propMap.get(CSI_USERNAME);
        }
        if (((null == pass) || (0 == pass.trim().length()))
                && propMap.containsKey(CSI_PASSWORD)) {
            pass = propMap.get(CSI_PASSWORD);
        }

        // if we have a forced username value, use it
        if (propMap.containsKey(CSI_FORCE_USERNAME)) {
            user = propMap.get(CSI_FORCE_USERNAME);
        }
        user = (null != user) ? user.trim() : "";
        pass = (null != pass) ? pass.trim() : "";

        if ((user != null) && !user.isEmpty()) {
            connectProps.put(JDBC_USER_PROP, user);
            connectProps.put(JDBC_PASSWORD_PROP, pass);
        }

        // add other properties
        for (Map.Entry<String, String> entry : propMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null) {
                String decodedKey = URLDecoder.decode(key.replace("%", "%25"));
                String decodedValue = URLDecoder.decode(value.replace("%", "%25"));
                if (decodedKey.startsWith("csi.")) {
                    if (decodedKey.startsWith(CSI_PARAMS_PREFIX)) {
                        String[] strings = decodedValue.split("=", 2);
                        if (strings.length == 2) {
                            connectProps.put(strings[0], strings[1]);
                        }
                    }
                } else {
                    connectProps.put(decodedKey, decodedValue);
                }
            }
        }
        return connectProps;
    }

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
        throw new IllegalStateException("Operation is not legal for this basic connection type");
    }

   public synchronized List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
      List<String> list = new ArrayList<String>();

      try (Connection conn = this.getConnection(dsdef)) {
         if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getTableTypes()) {
               while (rs.next()) {
                  list.add(rs.getString("TABLE_TYPE"));
               }
            }
         }
      } catch (SQLException e) {
         ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

         LOG.error("Failed to list table types", e);

         if ((null != mySupport) && mySupport.isRestricted(getTypeName())) {
            throw new CentrifugeException("Failed to list table types");
         }
         throw new CentrifugeException("Failed to list table types", e);
      }
      return list;
   }

   public synchronized List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
      List<String> list = new ArrayList<String>();

      try (Connection conn = this.getConnection(dsdef)) {
         if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getCatalogs()) {
               while ((rs != null) && rs.next()) {
                  String cat = rs.getString("TABLE_CAT");

                  if (cat != null) {
                     list.add(cat);
                  }
               }
            }
         }
      } catch (SQLException e) {
         ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

         LOG.error("Failed to list catalogs", e);

         if ((null != mySupport) && mySupport.isRestricted(getTypeName())) {
            throw new CentrifugeException("Failed to list catalogs");
         }
         throw new CentrifugeException("Failed to list catalogs", e);
      }
      return list;
   }

   public synchronized List<CsiMap<String,String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
      List<CsiMap<String,String>> result = new ArrayList<CsiMap<String,String>>();

      try (Connection conn = this.getConnection(dsdef)) {
         if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getSchemas()) {
               result = metaRsToMap(rs);
            }
         }
      } catch (SQLException e) {
         ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

         LOG.error("Failed to list schemas", e);

         if ((null != mySupport) && mySupport.isRestricted(getTypeName())) {
            throw new CentrifugeException("Failed to list schemas");
         }
         throw new CentrifugeException("Failed to list schemas", e);
      }
      return result;
   }

   public synchronized List<String> listExtractedSchemas(ConnectionDef sourceDefinitionIn, String catalogIn) throws CentrifugeException, GeneralSecurityException {
      List<String> schemas = new ArrayList<String>();
      Map<String, Integer> schemaMap = new TreeMap<String, Integer>();
      List<Set<String>> filters = getSourceFilters(sourceDefinitionIn);
      Set<String> authorizedSchemaList =
         ((filters != null) && (JdbcTableFilters.SCHEMA.ordinal() < filters.size())) ? filters.get(JdbcTableFilters.SCHEMA.ordinal()) : null;
      Set<String> authorizedTypeList =
         ((filters != null) && (JdbcTableFilters.TYPE.ordinal() < filters.size())) ? filters.get(JdbcTableFilters.TYPE.ordinal()) : null;
      String[] authorizedTypes =
         ((authorizedTypeList != null) && !authorizedTypeList.isEmpty()) ? authorizedTypeList.toArray(new String[0]) : null;

      try (Connection conn = getConnection(sourceDefinitionIn)) {
         if (conn != null) {
            DatabaseMetaData myMetaData = conn.getMetaData();

            if ((authorizedSchemaList != null) && !authorizedSchemaList.isEmpty()) {
               for (String schema : authorizedSchemaList) {
                  try (ResultSet results = myMetaData.getTables(catalogIn, schema, null, authorizedTypes)) {
                     while (results.next()) {
                        if (authorizedTable(results.getString(3))) {
                           schemaMap.put(results.getString(2), Integer.valueOf(0));
                           break;
                        }
                     }
                  }
               }
            } else {
               String formerSchema = null;

               try (ResultSet results = myMetaData.getTables(catalogIn, null, null, authorizedTypes)) {
                  while (results.next()) {
                     String schema = results.getString(2);

                     if ((schema != null) && (!schema.equals(formerSchema))) {
                        if (authorizedTable(results.getString(3))) {
                           schemaMap.put(schema, Integer.valueOf(0));
                           formerSchema = schema;
                        }
                     }
                  }
               }
            }
            for (String mySchema : schemaMap.keySet()) {
               schemas.add(mySchema);
            }
         }
      } catch (SQLException myException) {
         ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

         LOG.error("Failed to list tables", myException);

         if ((null != mySupport) && mySupport.isRestricted(getTypeName())) {
            throw new CentrifugeException("Failed to list tables");
         }
         throw new CentrifugeException("Failed to list tables", myException);
      }
      return schemas;
   }

   public synchronized List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type) throws CentrifugeException, GeneralSecurityException {
      return listTableDefs(dsdef, catalog, schema, (null == type) ? null : new String[] { type });
   }

   public synchronized List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String[] typesIn) throws CentrifugeException, GeneralSecurityException {
      List<SqlTableDef> list = new ArrayList<SqlTableDef>();

      try (Connection conn = this.getConnection(dsdef)) {
         if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getTables(catalog, schema, null, typesIn)) {
               while (rs.next()) {
                  String myTableName = rs.getString(3);

                  if (authorizedTable(myTableName)) {
                     list.add(new SqlTableDef(rs.getString(1), rs.getString(2), myTableName,
                                              getAlias(myTableName), rs.getString(4), null));
                  }
               }
            }
         }
      } catch (SQLException e) {
         ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

         LOG.error("Failed to list tables", e);

         if ((null != mySupport) && mySupport.isRestricted(getTypeName())) {
            throw new CentrifugeException("Failed to list tables");
         }
         throw new CentrifugeException("Failed to list tables", e);
      }
      return list;
   }

   public synchronized List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table) throws CentrifugeException, GeneralSecurityException {
      List<ColumnDef> list = new ArrayList<ColumnDef>();

      try (Connection conn = getConnection(dsdef)) {
         if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getColumns(catalog, schema, table, null)) {
               while (rs.next()) {
                  ColumnDef col = new ColumnDef();

                  col.setCatalogName(rs.getString(1));
                  col.setSchemaName(rs.getString(2));
                  col.setTableName(rs.getString(3));

                  String colname = rs.getString(4);
                  //TODO: WHat is going on here ????
                  if ((colname != null) && !colname.equals(colname.trim())) {
                     colname = getTableNameQualifier() + colname + getTableNameQualifier();
                  }
                  col.setColumnName(colname);
                  col.setLocalId(UUID.randomUUID().toString().toLowerCase()); // Double-check this !!!!!!!!!!!!!!
                  col.setJdbcDataType(rs.getInt(5));
                  col.setDataTypeName(rs.getString(6));
                  col.setColumnSize(rs.getInt(7));
                  col.setDecimalDigits(rs.getInt(9));
                  col.setDefaultValue(rs.getString(13));
                  col.setOrdinal(rs.getInt(17));
                  col.setNullable(rs.getString(18));
                  col.setCsiType(genCsiType(col));
                  list.add(col);
               }
            }
         }
      } catch (SQLException e) {
         ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

         LOG.error("Failed to list tables", e);

         if ((null != mySupport) && mySupport.isRestricted(getTypeName())) {
            throw new CentrifugeException("Failed to list tables");
         }
         throw new CentrifugeException("Failed to list tables", e);
      }
      return list;
   }

   protected List<SqlTableDef> formatTableList(ConnectionDef connectionDefIn, ResultSet resultlistIn)
         throws SQLException {
      List<SqlTableDef> list = new ArrayList<SqlTableDef>();

      while (resultlistIn.next()) {
         String tableName = resultlistIn.getString(3);

         if (authorizedTable(tableName)) {
            list.add(new SqlTableDef(resultlistIn.getString(1), resultlistIn.getString(2), tableName,
                                       getAlias(tableName), resultlistIn.getString(4), null));
         }
      }
      return list;
   }

   protected List<CsiMap<String, String>> metaRsToMap(ResultSet rs) throws SQLException {
      List<CsiMap<String, String>> list = new ArrayList<CsiMap<String, String>>();
      ResultSetMetaData rsMeta = rs.getMetaData();

      while (rs.next()) {
         CsiMap<String, String> map = new CsiMap<String, String>();
         int count = rsMeta.getColumnCount();

         for (int i = 1; i <= count; i++) {
            Object val = rs.getObject(i);
            String colname = SqlUtil.getColumnName(rsMeta, i).toUpperCase();

            map.put(colname, (val == null) ? "" : val.toString());
         }
         list.add(map);
      }
      return list;
   }
}
