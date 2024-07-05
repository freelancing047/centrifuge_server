package csi.server.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.connector.config.JdbcDriver;
//import csi.server.connector.jdbc.CacheConnectionFactory;
import csi.server.connector.jdbc.JdbcConnectionFactory;

public class ConnectionFactoryManager {
    private static final Logger LOG = LogManager.getLogger(ConnectionFactoryManager.class);

    public static final String FAILED_CONNECTION_FACTORY_PREFIX = "Failed to find connection factory for type name: ";

    private static boolean doDebug = LOG.isDebugEnabled();
    private static ConnectionFactoryManager singleton = new ConnectionFactoryManager();
    public static ConnectionFactory cacheFactory = null;
    public static String cacheDatabase = null;

    protected Map<String, ConnectionFactory> factoryByTypeName = new HashMap<String, ConnectionFactory>();

    public static ConnectionFactoryManager getInstance() {
        return singleton;
    }

    public void init() {
        if (doDebug) {
         LOG.debug("** ** **  ConnectionFactoryManager::init()");
      }

        for (JdbcDriver d : Configuration.getInstance().getDbConfig().getDrivers().getDrivers()) {
           LOG.info("Encountered request for driver {}", () -> d.getName());
            ConnectionFactory factory = null;
            if (d.getFactory() != null) {
                try {
                    Class<?> facClz = Class.forName(d.getFactory());
                    if (ConnectionFactory.class.isAssignableFrom(facClz)) {
                        factory = (ConnectionFactory) facClz.newInstance();
                    } else {
                       LOG.error("Invalid configuration for JDBC driver '{}'.  Factory '{}' does not implement {}",
                                 () -> d.getName(), () -> d.getFactory(), () -> ConnectionFactory.class.getName());
                    }
                } catch (InstantiationException e) {
                   LOG.error("Failed to instatiate factory for JDBC driver '" + d.getName() + "'.", e);
                } catch (IllegalAccessException e) {
                   LOG.error("Failed to instatiate factory for JDBC driver '" + d.getName() + "'.", e);
                } catch (ClassNotFoundException e) {
                   LOG.error("Factory class not found for JDBC driver '" + d.getName() + "'.", e);
                }

                if (factory == null) {
                    continue;
                }
            } else {
                factory = new JdbcConnectionFactory();
            }

            if (d.getDriverClass() != null) {
                try {
                    /*Class<?> cls = */Class.forName(d.getDriverClass());
                } catch (ClassNotFoundException e) {
                   LOG.error("Class not found for JDBC driver '" + d.getDriverClass() + "'.", e);
                    continue;
                }
            }
            factory.setTypeName(d.getKey());
            factory.setDriverClass(d.getDriverClass());
            if (doDebug) {
               LOG.debug("           -- Read creator role from config file.");
            }
            if ((d.getDriverAccessRole() != null) && !d.getDriverAccessRole().trim().isEmpty()) {
                if (doDebug) {
                  LOG.debug("           -- Creator for factory \"" + d.getKey() + "\" must belong to role\"" + d.getDriverAccessRole() + "\".");
               }
                factory.setDriverAccessRole(d.getDriverAccessRole());
            }
            if (doDebug) {
               LOG.debug("           -- Read source edit role from config file.");
            }
            if ((d.getSourceEditRole() != null) && !d.getSourceEditRole().trim().isEmpty()) {
                if (doDebug) {
                  LOG.debug("           -- Source editor \"" + d.getKey() + "\" must belong to role\"" + d.getSourceEditRole() + "\".");
               }
                factory.setSourceEditRole(d.getSourceEditRole());
            }
            if (doDebug) {
               LOG.debug("           -- Read connection access role from config file.");
            }
            if ((d.getConnectionEditRole() != null) && !d.getConnectionEditRole().trim().isEmpty()) {
                if (doDebug) {
                  LOG.debug("           -- Connection editor/viewer for factory \"" + d.getKey() + "\" must belong to role\"" + d.getConnectionEditRole() + "\".");
               }
                factory.setConnectionEditRole(d.getConnectionEditRole());
            }
            if (doDebug) {
               LOG.debug("           -- Read query access role from config file.");
            }
            if ((d.getQueryEditRole() != null) && !d.getQueryEditRole().trim().isEmpty()) {
                if (doDebug) {
                  LOG.debug("           -- Query editor/viewer for factory \"" + d.getKey() + "\" must belong to role\"" + d.getQueryEditRole() + "\".");
               }
                factory.setQueryEditRole(d.getQueryEditRole());
            }
            if (doDebug) {
               LOG.debug("           -- Read data viewer role from config file.");
            }
            if ((d.getDataViewingRole() != null) && !d.getDataViewingRole().trim().isEmpty()) {
                if (doDebug) {
                  LOG.debug("           -- Data viewer for factory \"" + d.getKey() + "\" must belong to role\"" + d.getDataViewingRole() + "\".");
               }
                factory.setDataViewingRole(d.getDataViewingRole());
            }
            if (doDebug) {
               LOG.debug("           -- Read block custom queries from config file.");
            }
            if (d.getBlockCustomQueries() != null) {
                if (doDebug) {
                  LOG.debug("           -- Data viewer for factory \"" + d.getKey() + "\" must belong to role\"" + d.getDataViewingRole() + "\".");
               }
                factory.setBlockCustomQueries(d.getBlockCustomQueries());
            }
            d.setBlockCustomQueries(factory.getBlockCustomQueries());
            factory.setUrlPrefix(d.getBaseUrl());
            if (d.getEscapeChar() != null) {
                factory.setEscapeChar(d.getEscapeChar());
            }
            factory.setUseProxyAuthentication(d.getUseProxyAuthentication());
            factory.setTableNameQualifier(d.getTableNameQualifier());
            factory.setTableNameAliasQualifier(d.getTableNameAliasQualifier());
            factory.setSelectNullString(d.getSelectNullString());

            factory.setAuthErrorCodes(d.getAuthErrorCodes());
            factory.setAuthSqlStates(d.getAuthSqlStates());
            if (d.getDefaultProperties() != null) {
                factory.setDefaultProperties(d.getDefaultProperties());
            }

            if (d.getJdbcFactory() != null) {
                if (d.getJdbcFactory().equals("false")) {
                    factory.setJdbcFactory(false);
                }
            }
            if (d.getTableAliasMap() != null) {
                factory.setTableAliasMap(d.getTableAliasMap());
            }
            if (d.getCapcoColumnMap() != null) {
                factory.setCapcoColumnMap(d.getCapcoColumnMap());
            }
            if (d.getCapcoStringMap() != null) {
                factory.setCapcoStringMap(d.getCapcoStringMap());
            }
            if (d.getTypeMapping() != null) {
                factory.setTypeMapping(d.getTypeMapping());
            }
            if (d.getCastMapping() != null) {
                factory.setCastMapping(d.getCastMapping());
            }
            if (d.getCastNulls() != null) {
                factory.setCastNulls(d.getCastNulls());
            }
            try {
                registerFactory(factory);
/*
                if ((factory.getUrlPrefix() != null) && (factory.getUrlPrefix().equalsIgnoreCase(CacheConnectionFactory.DELEGATE_URL_PREFIX))) {
                    CacheConnectionFactory cacheFactory = new CacheConnectionFactory(factory);
                    registerFactory(cacheFactory);
                }
*/
            } catch (ClassNotFoundException e) {
               LOG.error("Factory class not found for JDBC driver '" + d.getName() + "'.", e);
            }
        }
    }

    private void registerFactory(ConnectionFactory factoryIn) throws ClassNotFoundException {

        if (factoryIn.getTypeName() != null) {

            String myKey = factoryIn.getTypeName().toLowerCase();

            if (myKey.equals("cache")) {
                cacheFactory = factoryIn;
                cacheDatabase = ((AbstractConnectionFactory)cacheFactory).getDatabaseName(null);
                LOG.info("{} cache loaded and hidden from selection.", () -> factoryIn.getTypeName());
            } else {
                factoryByTypeName.put(myKey, factoryIn);
                LOG.info("{} driver loaded.", () -> factoryIn.getTypeName());
            }
        }
    }

    public ConnectionFactory getFactoryForType(String connType) throws CentrifugeException {
      if (connType == null) {
            return new JdbcConnectionFactory();
      }
        ConnectionFactory factory = factoryByTypeName.get(connType.toLowerCase());
        if (factory == null) {
            throw new CentrifugeException(FAILED_CONNECTION_FACTORY_PREFIX + connType);
        }
        return factory;
    }

    public ConnectionFactory getConnectionFactory(ConnectionDef dsDef) throws CentrifugeException {
        return getFactoryForType(dsDef.getType().toLowerCase());
    }
    public ConnectionFactory getConnectionFactory(String connType) throws CentrifugeException {
        return getFactoryForType(connType);
    }

    public Set<String> getTypeNames() {
        return factoryByTypeName.keySet();
    }
/*
    public ConnectionFactory getCacheConnectionFactory() throws CentrifugeException {
        return getFactoryForType(CacheConnectionFactory.CACHE_TYPE_NAME);
    }
*/
    public static boolean isAdvancedFactory(ConnectionFactory factory) {
        if (factory == null) {
            return false;
        }

        return factory instanceof JdbcConnectionFactory;
//        return (factory instanceof JdbcConnectionFactory || factory instanceof CacheConnectionFactory);
    }

    public static boolean isAdvancedFactory(ConnectionDef conndef) throws CentrifugeException {
        ConnectionFactory factory = getInstance().getConnectionFactory(conndef);
        return isAdvancedFactory(factory);
    }

    public static boolean isStreaming(ConnectionFactory factory) {
        Properties props = factory.getDefaultProperties();
        if (!props.containsKey("streaming")) {
            return false;
        }

        String value = props.getProperty("streaming");
        return (value != null) && value.equals("true");
    }
}
