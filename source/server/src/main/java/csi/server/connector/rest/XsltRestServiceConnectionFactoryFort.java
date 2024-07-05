package csi.server.connector.rest;

/*
 * Connection factory for creating connections to RESTful Web service.  
 */
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.exception.CentrifugeException;

public class XsltRestServiceConnectionFactoryFort extends XsltRestServiceConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(XsltRestServiceConnectionFactoryFort.class);

    private static final String ID = "XsltRestServiceConnectionFactoryFort";
    private static final String TABLE_TYPE_JSON = "Xslt Rest Service - Fort";

    public XsltRestServiceConnectionFactoryFort() {
        super();
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
        connection = new XsltRestServiceConnectionFort(url, this.getUrlPrefix(), allProps);
        return connection;
    }
}
