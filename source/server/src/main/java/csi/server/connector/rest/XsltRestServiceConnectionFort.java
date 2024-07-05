package csi.server.connector.rest;

import java.util.Properties;

import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.server.common.exception.CentrifugeException;

public class XsltRestServiceConnectionFort extends XsltRestServiceConnection {
   protected static final Logger LOG = LogManager.getLogger(XsltRestServiceConnectionFort.class);

   private static String ID = "XsltRestServiceConnectionFort";

    public XsltRestServiceConnectionFort(String url, String urlPrefix, Properties props) throws CentrifugeException {
        super(url, urlPrefix, props);
    }

    // Set additional HTTP headers as necessary.
    public void setAdditionalHeaders(HttpGet httpget) {
        // httpget.setHeader("X-ProxyUserDn", getDN());
        httpget.setHeader("X-ProxiedEntitiesChain", "<" + getDN() + ">");
        // log.debug(ID + ": setAdditionalHeaders: X-ProxyUserDn: "+getDN());
        LOG.debug(ID + ": setAdditionalHeaders: X-ProxiedEntitiesChain: <" + getDN() + ">");
    }

    // Get the Distinguished Name from the Authorization object
    public String getDN() {
        Authorization auth = CsiSecurityManager.getAuthorization();
        String dn = null;
        if (auth != null) {
            dn = auth.getDistinguishedName();
            if (null != dn) {
                dn = encodeSpaces(dn);
                // hack for casey. change ,%20 to just ,
                dn = dn.replace(",%20", ",");
            }
        }
        return dn;
    }
}