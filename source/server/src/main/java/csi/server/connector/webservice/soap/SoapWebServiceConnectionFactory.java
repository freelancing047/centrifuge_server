package csi.server.connector.webservice.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.connector.webservice.AbstractWebServiceConnectionFactory;
import csi.server.connector.webservice.WebServiceConnection;

/**
 * Entry point for obtaining data from executing a web service operation that simulates JDBC.
 * <p>
 * This is a temporary approach. Longer term Centrifuge Server will support hierarchical/networked data by default.
 * Tabular data is a simple case of hierarchical where each row is represented as a node with simple properties.
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public class SoapWebServiceConnectionFactory extends AbstractWebServiceConnectionFactory {

    static final String CENTRIFUGE_TEMPLATE = "ws:centrifuge:";

    static final String STANDARD_TEMPLATE = "ws:standard:";

    static final String SOAP_TEMPLATE = "ws:soap:";

    private static final String TABLE_TYPE_SERVICE = "Web Service";

    {
        this.template = SOAP_TEMPLATE;
    }

    @Override
    protected WebServiceConnection createConnection() {
        return new SoapWebServiceConnection();
    }

    @Override
    public Connection getConnection(String url, Properties props) throws SQLException, GeneralSecurityException, ClassNotFoundException {

        String modifiedURL = url.substring(template.length());
        URL resource = null;
        try {
            resource = new URL(modifiedURL);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int queryIndex = modifiedURL.indexOf('?');
        if (queryIndex != -1) {

        }

        try {
            WebServiceConnection service = createConnection();
            service.setWSDLURL(resource);

            return service;
        } catch (Throwable t) {
            throw new SQLException(t.getMessage());
        }

    }

    @Override
    public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        // return empty list
        return new ArrayList<String>();
    }

    @Override
    public List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
        // return empty list
        return new ArrayList<CsiMap<String, String>>();
    }

    @Override
    public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        List<String> types = new ArrayList<String>();
        types.add(TABLE_TYPE_SERVICE);
        return types;
    }

}
