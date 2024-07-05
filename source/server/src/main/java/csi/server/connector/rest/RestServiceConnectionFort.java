package csi.server.connector.rest;

import java.net.URL;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;

public class RestServiceConnectionFort extends RestServiceConnection {
   protected static final Logger LOG = LogManager.getLogger(RestServiceConnectionFort.class);
   
   private static String ID = "RestServiceConnectionFort";

    private String metaNamespaceURI;

    public RestServiceConnectionFort(String url, String urlPrefix, Properties props) throws CentrifugeException {
        super(url, urlPrefix, props);

        metaNamespaceURI = props.getProperty(RestServiceConnectionFactoryFort.DOCUMENT_METADATA_NS_URI);
        if (metaNamespaceURI == null) {
            metaNamespaceURI = "";
        }

        if (useNamespaces && metaNamespaceURI.length() == 0) {
            throw new CentrifugeException("Namespaces are enabled, but no target namespace URI provided");
        }
    }

    // Set additional HTTP headers as necessary.
    public void setAdditionalHeaders(HttpGet httpget) {
        httpget.setHeader("X-ProxyUserDn", getDN());
        LOG.debug(ID + ": setAdditionalHeaders: X-ProxyUserDn: " + getDN());
    }

    // Get the Distinguished Name from the Authorization object
    public String getDN() {
        Authorization auth = CsiSecurityManager.getAuthorization();
        String dn = null;
        if (auth != null) {
            dn = auth.getDistinguishedName();
        }
        return dn;
    }

    /*
     * java.sql.Connection Methods
     */
    public ResultSet execute(String operationName, List<QueryParameterDef> params, String query) throws Throwable {

        String modifiedURLString = httpURLString + query;

        // go get the data from the REST service
        Document document = null;

        // make sure the remaining URL is valid
        URL modifiedURL = isValidURL(modifiedURLString);
        if (null == modifiedURL) {
            throw new Exception("URL is malformed: " + modifiedURLString);
        }
        boolean ok = isReachable(modifiedURL);
        if (!ok) {
            throw new Exception("Unable to resolve hostname in URL " + modifiedURLString);
        }
        document = getData(modifiedURL);

        RestServiceResultSetFort resultSet = new RestServiceResultSetFort(this, document, query);
        return resultSet;
    }

    boolean isUseNamespaces() {
        return useNamespaces;
    }

    String getMetaNamespaceURI() {
        return metaNamespaceURI;
    }
}
