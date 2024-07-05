package csi.server.connector;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.security.CsiSecurityManager;
import csi.server.common.dto.CsiMap;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.JdbcTableFilters;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.Property;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.util.ConnectorSupport;
import csi.server.connector.config.JdbcDriver;
import csi.server.util.CacheUtil;

public abstract class AbstractConnectionFactory implements ConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(AbstractConnectionFactory.class);

   private static final String DEFAULT_ESCAPE_CHAR = "'";
   private static final String DEFAULT_SELECT_NULL_STRING = "NULL";
   private static final String DEFAULT_IDENT_QUALIFIER = "\"";

   public static final String CSI_DATABASENAME = "csi.databaseName";
   public static final String CSI_FILEPATH = "csi.filePath";
   public static final String CSI_FILETOKEN = "csi.filetoken";
   public static final String CSI_FORCE_USERNAME = "@USER";
   public static final String CSI_HOSTNAME = "csi.hostname";
   public static final String CSI_LOCALFILEPATH = "csi.localFilePath";
   public static final String CSI_PARAMS_PREFIX = "csi.params";
   public static final String CSI_PASSWORD = "csi.password";
   public static final String CSI_PORT = "csi.port";
   public static final String CSI_QUERY_TABLE_NAME = "query.tableName";
   public static final String CSI_REMOTEFILEPATH = "csi.remoteFilePath";
   public static final String CSI_RUNTIME_PASSWORD = "csi.runtime.password";
   public static final String CSI_RUNTIME_USERNAME = "csi.runtime.username";
   public static final String CSI_SCHEMA_CELLDELIM = "csi.schema.cellDelim";
   public static final String CSI_SCHEMA_CHARSET = "csi.schema.charset";
   public static final String CSI_SCHEMA_COLUMNS = "csi.schema.columns";
   public static final String CSI_SCHEMA_DATE_FORMAT = "csi.schema.dateFormat";
   public static final String CSI_SCHEMA_HASHEADERS = "csi.schema.firstRowHeaders";
   public static final String CSI_SCHEMA_NAMESPACE_PREFIX = "csi.schema.namespace";
   public static final String CSI_SCHEMA_ROWDELIM = "csi.schema.rowDelim";
   public static final String CSI_SCHEMA_TABLENAME = "csi.schema.tableName";
   public static final String CSI_SCHEMA_USEEXISTING = "csi.schema.useExisting";
   public static final String CSI_SCHEMA_XPATH = "csi.schema.xpath";
   public static final String CSI_USERNAME = "csi.username";

   public static final String JDBC_USER_PROP = "user";
   public static final String JDBC_PASSWORD_PROP = "password";

   public static final String CSI_QUERY_WORKSHEET = "query.worksheetName";

   public static final String CSI_INSTANCENAME = "csi.instanceName";

   public static final String PROXY_AUTHENTICATION = "proxyAuthentication";

    private String typeName;
    private String driverClass;
    private String driverAccessRole;
    private String sourceEditRole;
    private String connectionEditRole;
    private String queryEditRole;
    private String dataViewingRole;
    private String remarks;
    private String urlPrefix;
    private boolean jdbcFactory = true;
    private String escapeChar;
    private String selectNullString;
    private String tableNameQualifier;
    protected String tableNameAliasQualifier;
    private Properties defaultProperties;
    private Properties typeMapping;
    private Properties castMapping;
    private List<Set<String>> sourceFilters = null;
    private Integer sortOrder = null;
    private Map<String, String> tableAliasMap = null;
    private Map<String, String> capcoColumnMap = null;
    private Map<String, String> capcoStringMap = null;
    private List<Integer> authErrorCodes;
    private String allowCustomQueries = null;
    private boolean useProxyAuthentication = false;
    private boolean blockCustomQueries = false;
    private boolean castNulls = false;

    private List<String> authSqlStates;

   public boolean isInPlace() {
      return false;
   }

   public boolean isSimpleLoader() {
      return false;
   }

   public static Map<String,String> getPropertiesMap(ConnectionDef connectionDef) {
      Map<String,String> propertiesMap = null;

      if (connectionDef != null) {
         String username = connectionDef.getUsername();
         String password = connectionDef.getPassword();

         GenericProperties myVisibleProperties = null;
         Properties myHiddenProperties = getHiddenProperties(connectionDef.getType());
         myVisibleProperties = getProperties(connectionDef);
         propertiesMap = myVisibleProperties.getPropertiesMap();

         if (propertiesMap == null) {
            propertiesMap = new HashMap<String,String>();
         }

         if ((null != myHiddenProperties) && !myHiddenProperties.isEmpty()) {
            Enumeration<Object> myKeys = myHiddenProperties.keys();
            while (myKeys.hasMoreElements()) {
               String myKey = (String) myKeys.nextElement();
               String myValue = (null != myKey) ? myHiddenProperties.getProperty(myKey) : null;
               //
               // Check for @USER
               //
               if ((myValue != null) && myValue.equals(CSI_FORCE_USERNAME)) {

                  propertiesMap.put(CSI_FORCE_USERNAME, CsiSecurityManager.getUserName());

               } else {

                  propertiesMap.put(myKey, myValue);
               }
            }
         }

         if (!StringUtils.isEmpty(username)) {
            propertiesMap.put(CSI_RUNTIME_USERNAME, username);
         }
         if (!StringUtils.isEmpty(password)) {
            propertiesMap.put(CSI_RUNTIME_PASSWORD, password);
         }
      }
      return propertiesMap;
   }

   public static Map<String,String> getPropertiesMap(String type) {
      Map<String,String> propertiesMap = new HashMap<String,String>();

      if (type != null) {
         Properties hiddenProperties = getHiddenProperties(type);

         if ((hiddenProperties != null) && !hiddenProperties.isEmpty()) {
            for (Map.Entry<Object,Object> property : hiddenProperties.entrySet()) {
               String value = (String) property.getValue();

               // Check for @USER


               if (null != value) {


                        if (value.equals(CSI_FORCE_USERNAME)) {
                            propertiesMap.put(CSI_FORCE_USERNAME, CsiSecurityManager.getUserName());
                        } else {
                            propertiesMap.put((String)property.getKey(), value);
                        }


               }


            }
         }
      }
      return propertiesMap;
   }

   private static GenericProperties getProperties(ConnectionDef connectionDef) {

      GenericProperties visibleProperties = connectionDef.getProperties();
        boolean myForce = false;

        if (visibleProperties == null) {
            visibleProperties = new GenericProperties();
        }
      Properties hiddenProperties = getHiddenProperties(connectionDef.getType());

        if ((hiddenProperties != null) && !hiddenProperties.isEmpty()) {
            Enumeration<Object> myKeys = hiddenProperties.keys();
            if (myKeys.hasMoreElements())
            {
                GenericProperties myProperties = new GenericProperties();
                List<Property> myOldList = visibleProperties.getProperties();
                List<Property> myNewList = new ArrayList<Property>();

                while (myKeys.hasMoreElements())
                {
                    String myKey = (String)myKeys.nextElement();
                    String myValue = hiddenProperties.getProperty(myKey);
                    //
                    // Check for @USER
                    //
                    if (myKey.equals(CSI_USERNAME) && myValue.equals(CSI_FORCE_USERNAME)) {
                        myNewList.add(new Property(CSI_FORCE_USERNAME, CsiSecurityManager.getUserName()));
                        myForce = true;

                    } else {
                        myNewList.add(new Property(myKey, myValue));
                    }
                }

                for (Property myProperty : myOldList)
                {
                    if ((!myForce)
                            || ((!myProperty.getName().equals(CSI_USERNAME))
                            && (!myProperty.getName().equals(CSI_RUNTIME_USERNAME)))) {
                        myNewList.add(myProperty);
                    }
                }
                myProperties.setProperties(myNewList);

                return myProperties;
            }
        }
        return visibleProperties;
   }

   public static String forceUsername(ConnectionDef connectionDef) {
      String userNameFromForce = null;
      Properties hiddenProperties = getHiddenProperties(connectionDef.getType());

      if ((hiddenProperties != null) && hiddenProperties.containsKey(CSI_USERNAME)) {
         String value = hiddenProperties.getProperty(CSI_USERNAME);

         if ((value != null) && value.equals(CSI_FORCE_USERNAME)) {
            userNameFromForce = CsiSecurityManager.getUserName();
         }
      }
      return userNameFromForce;
   }

    @Override
    public List<String> listTableTypes(ConnectionDef dsdef, String catalog, String schema)
            throws CentrifugeException, GeneralSecurityException {

        return listTableTypes(dsdef);
    }

    @Override
    public synchronized List<String> listExtractedSchemas(ConnectionDef dsdef, String catalog)
            throws CentrifugeException, GeneralSecurityException {

        List<String> myList = new ArrayList<String>();
        List<CsiMap<String, String>> myListMaps = listSchemas(dsdef, catalog);
        Set<String> myKeys = new LinkedHashSet<String>();
        Set<String> myValues = new LinkedHashSet<String>();

        if (null != myListMaps) {

            for (CsiMap<String, String> myMap : myListMaps) {

                if (null != myMap) {

                    for (Map.Entry<String, String> entry : myMap.entrySet()) {
                        myKeys.add(entry.getKey());
                        myValues.add(entry.getValue());
                    }
                }
            }

            myList.addAll(myValues);
        }
        return myList;
    }

    @Override
    public List<ColumnDef> listColumnDefs(ConnectionDef connectionIn, SqlTableDef tableIn) throws CentrifugeException, GeneralSecurityException {

        String myCatalog = tableIn.getCatalogName();
        String mySchema = tableIn.getSchemaName();

        return listColumnDefs(connectionIn, myCatalog, mySchema, tableIn.getTableName());
    }

    private static Properties getTableFilterInfo(String typeIn) {
        Properties myFilters = null;
        JdbcDriver myDriver = getDriver(typeIn);
        if (null != myDriver) {
            myFilters = myDriver.getTableFilters();
        }
        return myFilters;
    }

    private static Properties getHiddenProperties(String typeIn) {
        Properties myProperties = null;

        if (null != typeIn) {

            JdbcDriver myDriver = getDriver(typeIn);
            if (null != myDriver) {
                myProperties = myDriver.getHiddenProperties();
            }
        }
        return myProperties;
    }

   public boolean getHasHiddenFlag() {

        Properties myHiddenProperties = getHiddenProperties(getTypeName());

        return (null != myHiddenProperties) && !myHiddenProperties.isEmpty();
    }

    public static JdbcDriver getDriver(String typeIn) {
        return Configuration.getInstance().getDbConfig().getDrivers().getDriver(typeIn);
    }

   public String getTypeName() {
        return typeName;
    }

   public void setJdbcFactory(boolean f) {
    	this.jdbcFactory = f;
    }

    public boolean isJdbcFactory() {
    	return this.jdbcFactory;
    }

   public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

   public String getDriverClass() {
        return driverClass;
    }

   public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

   public String getDriverAccessRole() {
        return driverAccessRole;
    }

   public void setDriverAccessRole(String driverAccessRole) {
        this.driverAccessRole = (null != driverAccessRole) ? driverAccessRole.trim().toLowerCase() : null;
    }

   public String getSourceEditRole() {
        return sourceEditRole;
    }

   public void setSourceEditRole(String sourceEditRole) {
        this.sourceEditRole = (null != sourceEditRole) ? sourceEditRole.trim().toLowerCase() : null;
    }

   public String getConnectionEditRole() {
        return connectionEditRole;
    }

   public void setConnectionEditRole(String connectionEditRole) {
        this.connectionEditRole = (null != connectionEditRole) ? connectionEditRole.trim().toLowerCase() : null;
    }

   public String getQueryEditRole() {
        return queryEditRole;
    }

   public void setQueryEditRole(String queryEditRole) {
        this.queryEditRole = (null != queryEditRole) ? queryEditRole.trim().toLowerCase() : null;
    }

   public String getDataViewingRole() {
        return dataViewingRole;
    }

   public void setDataViewingRole(String dataViewingRole) {
        this.dataViewingRole = (null != dataViewingRole) ? dataViewingRole.trim().toLowerCase() : null;
    }

   public String getRemarks() {
        if (null == remarks) {
            return "Customer supplied custom JDBC driver.";
        }
        return remarks;
    }

   public void setRemarks(String remarksIn) {
        remarks = remarksIn;
    }

   public String getUrlPrefix() {
        return urlPrefix;
    }

   public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

   public String getTableNameQualifier() {
        return (null != tableNameQualifier) ? tableNameQualifier : DEFAULT_IDENT_QUALIFIER;
    }

   public void setTableNameQualifier(String tableNameQualifierIn) {
        this.tableNameQualifier = (null != tableNameQualifierIn) ? tableNameQualifierIn.trim() : null;
    }

   public String getTableNameAliasQualifier() {
        return (null != tableNameAliasQualifier) ? tableNameAliasQualifier : null;
    }

   public void setTableNameAliasQualifier(String tableNameAliasQualifierIn) {
        this.tableNameAliasQualifier = (null != tableNameAliasQualifierIn) ? tableNameAliasQualifierIn.trim() : null;
    }

   public Properties getTypeMapping() {
        if (typeMapping == null) {
            typeMapping = new Properties();
        }
        return typeMapping;
    }

   public void setTypeMapping(Properties typeMappingIn) {

        typeMapping = (null != typeMappingIn) ? new Properties() : null;

        if (null != typeMappingIn) {

            for (String myKey : typeMappingIn.stringPropertyNames()) {

                typeMapping.setProperty(myKey.toLowerCase(), (String)typeMappingIn.get(myKey));
            }
        }
    }

   public CsiDataType getMappedCsiType(String dataTypeIn) {

        return ((null != typeMapping) && !typeMapping.isEmpty())
                ? CsiDataType.getMatchingType((String)typeMapping.get(CsiDataType.trimTypeName(dataTypeIn).toLowerCase()))
                : null;
    }

   public Properties getCastMapping() {
        if (castMapping == null) {
            castMapping = new Properties();
        }
        return castMapping;
    }

   public void setCastMapping(Properties castMappingIn) {

        castMapping = (null != castMappingIn) ? new Properties() : null;

        if (null != castMappingIn) {

            for (String myKey : castMappingIn.stringPropertyNames()) {

                castMapping.setProperty(myKey.toLowerCase(), (String)castMappingIn.get(myKey));
            }
        }
    }

    protected String getDefaultCastingString(CsiDataType csiTypeIn) {

        String myJdbcType = (null != csiTypeIn) ? csiTypeIn.getJdbcType() : null;

        return (null != myJdbcType) ? "CAST(% AS " + myJdbcType + ")" : null;
    }

   public String getMappedCastingString(CsiDataType csiTypeIn) {

        String myFormat = null;
        String myLabel = (null != csiTypeIn ) ? csiTypeIn.getLabel() : null;

        if (null != myLabel) {

            if ((null != castMapping) && !castMapping.isEmpty()) {

                myFormat = (String) castMapping.get(myLabel.toLowerCase());

            } else {

                myFormat = getDefaultCastingString(csiTypeIn);
            }
        }
        return myFormat;
    }

    public String getEscapeChar() {
        if (escapeChar == null) {
            return DEFAULT_ESCAPE_CHAR;
        }
        return escapeChar;
    }

    public void setEscapeChar(String escapeChar) {
        this.escapeChar = escapeChar;
    }

    public String getSelectNullString() {
        if (selectNullString == null) {
            return DEFAULT_SELECT_NULL_STRING;
        } else {
            return selectNullString;
        }
    }

    public void setSelectNullString(String selectNullString) {
        this.selectNullString = selectNullString;
    }

    public Properties getDefaultProperties() {
        if (defaultProperties == null) {
            defaultProperties = new Properties();
        }
        return defaultProperties;
    }

    public void setDefaultProperties(Properties props) {
        this.defaultProperties = props;
    }

    public List<Integer> getAuthErrorCodes() {
        return authErrorCodes;
    }

    public void setAuthErrorCodes(List<Integer> authErrorCodes) {
        this.authErrorCodes = authErrorCodes;
    }

    public List<String> getAuthSqlStates() {
        return authSqlStates;
    }

    public void setAuthSqlStates(List<String> authSqlStates) {
        this.authSqlStates = authSqlStates;
    }

    public Connection getConnection(GenericProperties propertiesIn) throws CentrifugeException, GeneralSecurityException {
        throw new CentrifugeException("Unsupported method for obtaining a connection!");
    }

    public Connection getConnection() throws CentrifugeException, GeneralSecurityException {
        throw new CentrifugeException("Unsupported method for obtaining a connection!");
    }

    public Connection getConnection(ConnectionDef ddef) throws CentrifugeException, GeneralSecurityException {

        ConnectorSupport mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName());

        if ((null != mySupport) && mySupport.isRestricted(getTypeName())) {

            try {
                if (!isJdbcFactory()) {
                    Map<String, String> propMap = getPropertiesMap(ddef);
                    Properties props = toNativeProperties(propMap);
                    return this.getConnection((ddef == null) ? null : ddef.getConnectString(), props);
                } else {
                    return this.getConnection(getPropertiesMap(ddef));
                }
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
                throw new CentrifugeException("Failed to get connection.");
            }

        } else {

            try {
                if (!isJdbcFactory()) {
                    Map<String, String> propMap = getPropertiesMap(ddef);
                    Properties props = toNativeProperties(propMap);
                    return this.getConnection((ddef == null) ? null : ddef.getConnectString(), props);
                } else {
                    return this.getConnection(getPropertiesMap(ddef));
                }
            } catch (SQLException e) {
                throw new GeneralSecurityException("Failed to get connection.", e);
            } catch (ClassNotFoundException e) {
                throw new CentrifugeException("Check presence of JDBC driver.", e);
            } catch (GeneralSecurityException e) {
                throw e;
            } catch (Throwable t) {
                throw new CentrifugeException("Failed to get connection.", t);
            }
        }
    }

    public abstract Properties toNativeProperties(Map<String, String> propMap);

    public List<Set<String>> getSourceFilters(ConnectionDef connectionIn) {

        if (null == sourceFilters) {

            sourceFilters = new ArrayList<Set<String>>();
            Properties myFilterInfo = getTableFilterInfo(connectionIn.getType());

            if ((null != myFilterInfo) && !myFilterInfo.isEmpty()) {

                for (int i = 0; JdbcTableFilters.values().length > i; i++) {

                    sourceFilters.add(extractStrings(myFilterInfo.getProperty(JdbcTableFilters.values()[i].getKey())));
                }
            }
            if ((null != tableAliasMap) && !tableAliasMap.isEmpty()) {

                Set<String> myNameFilter = sourceFilters.get(3);

                if (null == myNameFilter) {

                    myNameFilter = new TreeSet<String>();
                    sourceFilters.set(3, myNameFilter);
                }
                for (Map.Entry<String, String> myEntry : tableAliasMap.entrySet()) {

                    if (null == myEntry.getValue()) {

                        myNameFilter.add(myEntry.getKey());
                    }
                }
            }
        }
        return sourceFilters;
    }

    public String getDatabaseName(ConnectionDef connectionDefIn) {

        Map<String, String> myMap = (null != connectionDefIn)
                ? getPropertiesMap(connectionDefIn)
                : getPropertiesMap(getTypeName());

        return (null != myMap) ? myMap.get(CSI_DATABASENAME) : null;
    }

    public void setSortOrder(Integer sortOrderIn) {

        sortOrder = sortOrderIn;
    }

    public Integer getSortOrder() {

        return sortOrder;
    }

    public void setTableAliasMap(Map<String, String> tableAliasMapIn) {

        tableAliasMap = tableAliasMapIn;
    }

    public Map<String, String> getTableAliasMap() {

        return tableAliasMap;
    }

    public void setCapcoColumnMap(Map<String, String> capcoColumnMapIn) {

        capcoColumnMap = capcoColumnMapIn;
    }

    public Map<String, String> getCapcoColumnMap() {

        return capcoColumnMap;
    }

    public void setCapcoStringMap(Map<String, String> capcoStringMapIn) {

        capcoStringMap = capcoStringMapIn;
    }

    public Map<String, String> getCapcoStringMap() {

        return capcoStringMap;
    }

    public boolean authorizedTable(String tableIn) {

        return ((null == sourceFilters) || (4 > sourceFilters.size()))
                || (null == sourceFilters.get(3)) || sourceFilters.get(3).contains(tableIn)
                || ((null != tableAliasMap) && tableAliasMap.containsKey(tableIn));
    }

    public String getAlias(String tableIn) {

        // If an alias is found and it does not match the original name
        // and is not an empty string return it. Otherwise return null.
        String myAlias = ((null != tableAliasMap) && !tableAliasMap.isEmpty()) ? tableAliasMap.get(tableIn) : null;
        return ((null != myAlias) && (0 < myAlias.length())) ? (myAlias.equals(tableIn) ? null : myAlias) : null;
    }

    public String getQualifiedName(SqlTableDef tableDef) {

        List<String> nameParts = new ArrayList<String>();
        nameParts.add(tableDef.getCatalogName());
        nameParts.add(tableDef.getSchemaName());
        nameParts.add(tableDef.getTableName());

        return makeFullyQualifiedName(nameParts);
    }

    public String getQuotedName(String name) {
        String nameQualifier = getTableNameQualifier();
        String escapeChar = getEscapeChar();

        return quoteName(name, nameQualifier, escapeChar);
    }

    public String quoteName(String name, String nameQualifier, String escapeChar) {
        if ((null != name) && (!name.isEmpty())) {
            if ((null != nameQualifier) && (!nameQualifier.isEmpty())) {

                if((name.length() > 2) && name.startsWith(nameQualifier) && name.endsWith(nameQualifier)){
                    name = name.substring(1, name.length()-1);
                }

                String[] splitName = name.split(escapeChar);



                StringBuilder buffer = new StringBuilder();


                //Don't need another escape on these if escape and name are the same
                if(name.startsWith(escapeChar) && !escapeChar.equals(nameQualifier)){
                    buffer.append(escapeChar+escapeChar);
                }
                for(int ii=0; ii<splitName.length; ii++){
                    if(splitName[ii].length() > 0){
                        buffer.append(splitName[ii].replaceAll(nameQualifier, escapeChar + nameQualifier));
                        if(ii < (splitName.length - 1)) {
                           buffer.append(escapeChar+escapeChar);
                        }
                    } else {
                        buffer.append(escapeChar+escapeChar);
                    }

                }

                //Don't need another escape on these if escape and name are the same
                if(name.endsWith(escapeChar) && !escapeChar.equals(nameQualifier)){
                    buffer.append(escapeChar+escapeChar);
                }


                return nameQualifier + buffer.toString() + nameQualifier;

            } else {

                return name;
            }
        } else {
            return "";
        }
    }

    protected CsiDataType genCsiType(ColumnDef columnIn) {

        CsiDataType myCsiType = null;

        if (null != columnIn) {

            myCsiType = columnIn.getCsiType();

            if (null == myCsiType) {

                myCsiType = CacheUtil.resolveCsiType(columnIn.getDataTypeName(), columnIn.getJdbcDataType(), this);
            }
        }
        return myCsiType;
    }

    protected String castNullValue(CsiDataType dataTypeIn) {

        return castExpression("null", dataTypeIn);
    }

    public String castNull(CsiDataType dataTypeIn) {

        return (castNulls) ? castNullValue(dataTypeIn) : "null";
    }

   public String castExpression(String expressionIn, ColumnDef sourceColumnIn, ColumnDef targetColumnIn) {
      if ((expressionIn != null) && (targetColumnIn != null)) {
         CsiDataType mySourceType = genCsiType(sourceColumnIn);
         CsiDataType myTargetType = genCsiType(targetColumnIn);

         if ((myTargetType != null) && (myTargetType != mySourceType)) {
            return castExpression(expressionIn, myTargetType);
         }
      }
      return expressionIn;
   }

    protected String formatCast(String expressionIn, CsiDataType castIn) {

        String myFormat = ((null != expressionIn) && (null != castIn)) ? getMappedCastingString(castIn) : null;

        if (null != myFormat) {

            return myFormat.replace("%", expressionIn);
        }
        return null;
    }

    public String castExpression(String expressionIn, CsiDataType castIn) {

        String myExpression = formatCast(expressionIn, castIn);

        return (null != myExpression) ? myExpression : expressionIn;
    }

    public String getAllowCustomQueries() {
        return allowCustomQueries;
    }

    public void setUseProxyAuthentication(Boolean useProxyAuthenticatioIn) {

        useProxyAuthentication = useProxyAuthenticatioIn;
    }

    public boolean getUseProxyAuthentication() {

        return useProxyAuthentication;
    }

    public void setAllowCustomQueries(String allowCustomQueriesIn) {

        if( allowCustomQueriesIn != null ) {

            String myValue = allowCustomQueriesIn.trim().toLowerCase();

            if ("true".equals(myValue)) {

                allowCustomQueries = myValue;
                blockCustomQueries = false;

            } else if ("false".equals(myValue)) {

                allowCustomQueries = myValue;
                blockCustomQueries = true;
            }
        }
    }

    public Boolean getBlockCustomQueries() {
        return blockCustomQueries;
    }

    public void setBlockCustomQueries(Boolean blockCustomQueriesIn) {
        if( blockCustomQueriesIn != null ) {
            blockCustomQueries = blockCustomQueriesIn;
            allowCustomQueries = null;
        }
    }

    public Boolean getCastNulls() {
        return castNulls;
    }

    public void setCastNulls(Boolean castNullsIn) {
        if( castNullsIn != null ) {
            castNulls = castNullsIn;
        }
    }

   private String makeFullyQualifiedName(List<String> nameparts) {
      return nameparts.stream().filter(Objects::nonNull)
                               .filter(part -> !part.isEmpty())
                               .map(part -> getQuotedName(part))
                               .collect(Collectors.joining("."));
   }

   private Set<String> extractStrings(String stringIn) {
      Set<String> myResults = new TreeSet<String>();

      if ((null != stringIn) && (stringIn.length() > 0)) {
         String[] myWorkingList = stringIn.split("\\|");

         for (int i = 0; i < myWorkingList.length; i++) {
            String myCandidate = myWorkingList[i].trim();

            if (myCandidate.length() > 0) {
               myResults.add(myCandidate);
            }
         }
      }
      return myResults.isEmpty() ? null : myResults;
   }
}
