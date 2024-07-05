package csi.server.connector.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.ConfigurationException;
import csi.server.common.dto.config.connection.DriverConfigInfo;
import csi.server.common.exception.CentrifugeException;

public class JdbcDriver {
   protected static final Logger LOG = LogManager.getLogger(JdbcDriver.class);

    private String name;
    private String key;
    private String baseUrl;
    private String jdbcFactory;
    private String tableNameQualifier;
    private String tableNameAliasQualifier;
    private String factory;
    private String driverClass;
    private String driverAccessRole;
    private String escapeChar;
    private String sourceEditRole;
    private String connectionEditRole;
    private String queryEditRole;
    private String dataViewingRole;
    private String remarks;
    private String selectNullString;
    private List<Integer> authErrorCodes = new ArrayList<Integer>();
    private List<String> authSqlStates = new ArrayList<String>();
    private Integer sortOrder = null;
    private Map<String, String> tableAliasMap = null;
    private Map<String, String> capcoColumnMap = null;
    private Map<String, String> capcoStringMap = null;
    private Properties defaultProperties = new Properties();
    private Properties typeMapping = new Properties();
    private Properties castMapping = new Properties();
    private Properties hiddenProperties = new Properties();
    private Properties tableFilters = new Properties();
    private String allowCustomQueries;
    private Boolean blockCustomQueries = null;
    private Boolean castNulls = null;
    private Boolean useProxyAuthentication = null;

    private DriverConfigInfo uiConnectionConfig;


    /*
     * Validate that:
     * 1) The driver has a key name
     * 2) A UI config has been provided.
     * 3) The UI config can be validated.
     */
    public void validate() throws ConfigurationException, CentrifugeException {

        if ((key == null) || "".equals(key)) {
            throw new ConfigurationException("Server configuration contains a driver definition with no key specified.");
        }

        if (uiConnectionConfig == null) {
            throw new ConfigurationException("Server configuration contains a driver definition for " + key
                    + " with no UI Connection Config specified.");
        } else {
            uiConnectionConfig.validate(key);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getJdbcFactory() {
        return jdbcFactory;
    }

    public void setJdbcFactory(String jdbcFactory) {
        this.jdbcFactory = jdbcFactory;
    }

    public String getTableNameQualifier() {
        return tableNameQualifier;
    }

    public void setTableNameQualifier(String tableNameQualifier) {
        this.tableNameQualifier = tableNameQualifier;
    }

    public String getTableNameAliasQualifier() {
        return tableNameAliasQualifier;
    }

    public void setTableNameAliasQualifier(String tableNameAliasQualifier) {
        this.tableNameAliasQualifier = tableNameAliasQualifier;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
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
        this.driverAccessRole = driverAccessRole;
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

    public String getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(String escapeChar) {
        this.escapeChar = escapeChar;
    }

    public String getSelectNullString() {
        return selectNullString;
    }

    public void setSelectNullString(String selectNullString) {
        this.selectNullString = selectNullString;
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

    public Properties getDefaultProperties() {
        return defaultProperties;
    }

    public void setDefaultProperties(Properties defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    public Properties getTypeMapping() {
        return typeMapping;
    }

    public void setTypeMapping(Properties typeMapping) {
        this.typeMapping = typeMapping;
    }

    public Properties getCastMapping() {
        return castMapping;
    }

    public void setCastMapping(Properties castMapping) {
        this.castMapping = castMapping;
    }

    public Properties getHiddenProperties() {
        return hiddenProperties;
    }

    public void setHiddenProperties(Properties hiddenProperties) {
        this.hiddenProperties = hiddenProperties;
    }

    public Properties getTableFilters() {
        return tableFilters;
    }

    public void setTableFilters(Properties tableFiltersIn) {
        this.tableFilters = tableFiltersIn;
    }

    public DriverConfigInfo getUiConnectionConfig() {
        return uiConnectionConfig;
    }

    public void setUiConnectionConfig(DriverConfigInfo uiConnectionConfig) {
        this.uiConnectionConfig = uiConnectionConfig;
    }

    public String getSourceEditRole() {
        return sourceEditRole;
    }

    public void setSourceEditRole(String sourceEditRole) {
        this.sourceEditRole = sourceEditRole;
    }

    public String getConnectionEditRole() {
        return connectionEditRole;
    }

    public void setConnectionEditRole(String connectionEditRole) {
        this.connectionEditRole = connectionEditRole;
    }

    public String getQueryEditRole() {
        return queryEditRole;
    }

    public void setQueryEditRole(String queryEditRole) {
        this.queryEditRole = queryEditRole;
    }

    public String getDataViewingRole() {
        return dataViewingRole;
    }

    public void setDataViewingRole(String dataViewingRole) {
        this.dataViewingRole = dataViewingRole;
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

    public String getAllowCustomQueries() {
        return allowCustomQueries;
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
      return Boolean.valueOf((castNulls != null) && castNulls);
   }

    public void setCastNulls(Boolean castNullsIn) {
        castNulls = castNullsIn;
    }

   public Boolean getUseProxyAuthentication() {
      return Boolean.valueOf((useProxyAuthentication != null) && useProxyAuthentication);
   }

    public void setUseProxyAuthentication(Boolean useProxyAuthenticationIn) {
        useProxyAuthentication = useProxyAuthenticationIn;
    }
}
