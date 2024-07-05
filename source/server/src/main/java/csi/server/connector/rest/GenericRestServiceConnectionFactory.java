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
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;

public class GenericRestServiceConnectionFactory extends RestServiceConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(GenericRestServiceConnectionFactory.class);
   
    private static final String ID = "GenericRestServiceConnectionFactory";
    private static final String TABLE_TYPE_JSON = "Generic Rest Service";

    public GenericRestServiceConnectionFactory() {
        super();
    }

    @Override
    public Connection getConnection(String url, Properties props) throws ClassNotFoundException, SQLException, GeneralSecurityException {
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
        try {
            connection = new GenericRestServiceConnection(url, this.getUrlPrefix(), allProps);
        } catch (Exception ex) {
           LOG.info(ex);
            connection = null;
        }
        return connection;
    }

    @Override
    public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        // return empty list
        List<String> list = new ArrayList<String>();
        return list;
    }

    @Override
    public List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
        // return empty list
        List<CsiMap<String, String>> list = new ArrayList<CsiMap<String, String>>();
        return list;
    }

    @Override
    public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        List<String> types = new ArrayList<String>();
        types.add(TABLE_TYPE_JSON);
        return types;
    }
}
