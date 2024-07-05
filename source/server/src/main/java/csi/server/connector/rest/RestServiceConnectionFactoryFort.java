package csi.server.connector.rest;

/*
 * Connection factory for creating connections to RESTful Web service.
 */
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.exception.CentrifugeException;

public class RestServiceConnectionFactoryFort extends RestServiceConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(RestServiceConnectionFactoryFort.class);

   private static final String ID = "RestServiceConnectionFactoryFort";

    static final String DOCUMENT_METADATA_NS_URI = "metaNamespaceURI";

    public RestServiceConnectionFactoryFort() {
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
        connection = new RestServiceConnectionFort(url, this.getUrlPrefix(), allProps);
        return connection;
    }

   @Override
   public Properties toNativeProperties(Map<String, String> propMap) {
      Properties props = super.toNativeProperties(propMap);

      for (Map.Entry<String,String> entry : propMap.entrySet()) {
         String key = entry.getKey();

         if (key.startsWith(CSI_PARAMS_PREFIX)) {
            String decoded = URLDecoder.decode(entry.getValue());
            String[] keyValue = decoded.split("=", 2);
            String value = keyValue[1];
            String propKey = keyValue[0];

            if ((propKey == null) || (value == null)) {
               continue;
            }
            if (DOCUMENT_METADATA_NS_URI.equalsIgnoreCase(propKey)) {
               props.put(DOCUMENT_METADATA_NS_URI, value);
            } else if (USING_NAMESPACES.equalsIgnoreCase(propKey)) {
               props.put(USING_NAMESPACES, value);
            }
         }
      }
      return props;
   }
}
