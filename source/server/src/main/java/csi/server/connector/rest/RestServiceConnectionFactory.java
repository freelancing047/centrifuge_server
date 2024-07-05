package csi.server.connector.rest;

/*
 * Connection factory for creating connections to RESTful Web service.  
 */
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.connector.AbstractConnectionFactory;

public class RestServiceConnectionFactory extends AbstractConnectionFactory {
   protected static final Logger LOG = LogManager.getLogger(RestServiceConnectionFactory.class);

   private static final String ID = "RestServiceConnectionFactory";
    protected static final String TRUSTSTORE = "truststoreFile";
    protected static final String TRUSTSTOREPW = "truststorePass";
    protected static final String TRUSTSTORETP = "truststoreType";
    protected static final String KEYSTORE = "keystoreFile";
    protected static final String KEYSTOREPW = "keystorePass";
    protected static final String KEYSTORETP = "keystoreType";
    protected static final String HTTP_PORT = "http_port";
    protected static final String HTTPS_PORT = "https_port";
    protected static final String URLPARAMS = "urlparams";
    protected static final String ACCEPT_ALL_CERTS = "acceptAllCerts";

    static final String USING_NAMESPACES = "useNamespaces";

    public RestServiceConnectionFactory() {
    }

    protected void rinit() {
        // perform any initialization bits here.
    }

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
       LOG.debug(ID + ": createConnectString");
        return null;
    }

    // @Override
    public Properties toNativeProperties(Map<String, String> propMap) {
        // construct properties to pass on to driver
        // based on the csi properties provided by the ui and default properties
        // in jdbc-driver.xml
        Properties nativeProps = new Properties();

        // for now just shove everything in as is
        nativeProps.putAll(propMap);
        return nativeProps;
    }

    @Override
    public Connection getConnection(String url, Properties props) throws ClassNotFoundException, SQLException, GeneralSecurityException, CentrifugeException {
       LOG.debug(ID + ": getConnection url=" + url + "  props=" + props);
        if (null != props) {
            Enumeration e = props.propertyNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                LOG.debug("  " + name + "=" + props.getProperty(name));
            }
        }

        rinit();
        Properties allProps = new Properties();
        allProps.putAll(getDefaultProperties());
        allProps.putAll(props);

        RestServiceConnection connection = null;
        connection = new RestServiceConnection(url, this.getUrlPrefix(), allProps);
        return connection;
    }

    @Override
    public Connection getConnection(Map<String, String> propMap) throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException {
        // TODO Auto-generated method stub
        throw new CentrifugeException(this.getClass().getName() + "Unsupported method call.");
    }

    @Override
    public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<String>();
    }

    @Override
    public List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<ColumnDef>();
    }

    @Override
    public List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<CsiMap<String, String>>();
    }

    @Override
    public List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<SqlTableDef>();
    }

    @Override
    public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<String>();
    }
    

}
