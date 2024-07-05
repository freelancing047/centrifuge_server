package csi.server.connector.rest;

import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.server.common.model.query.QueryParameterDef;

public class GenericRestServiceConnection extends RestServiceConnection {
   protected static final Logger LOG = LogManager.getLogger(GenericRestServiceConnection.class);

   private static String ID = "SaffronRestServiceConnection";
    private static String OPNSUB = "{:";
    private static String CLSSUB = "}";

    public GenericRestServiceConnection(String url, String urlPrefix, Properties props) throws Exception {
        super(url, urlPrefix, props);
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
        int pcnt = 0;
        if (null != params) {
         pcnt = params.size();
      }

        LOG.debug(ID + ": execute " + operationName + " " + query + "  params=" + pcnt);

        // if there are any parameters, substitute them in the URL string
        String modifiedURLString = httpURLString;
//        String sessionId = null;
//        String basicStrEncoded = null;
        if (null != params) {
            // modifiedURLString = httpURLString;
            ListIterator<QueryParameterDef> li = params.listIterator();
            while (li.hasNext()) {
                QueryParameterDef qpd = li.next();
                LOG.debug(ID + ": execute param=" + qpd.getName() + " value=" + qpd.getValue());
                if (qpd.getName().equals("sessionid")) {
//                    sessionId = qpd.getValue();
                } else if (qpd.getName().equals("basicStrEncoded")) {
//                    basicStrEncoded = qpd.getValue();
                } else {
                    modifiedURLString = substituteParams(modifiedURLString, qpd.getName(), qpd.getValue());
                    LOG.debug(ID + ": modifiedURLString=" + modifiedURLString);
                }
            }
        }

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

        NodeList resultNodes = document.getElementsByTagName(query);

        GenericRestServiceResultSet resultSet = new GenericRestServiceResultSet(resultNodes);

        return resultSet;
    }

    public String substituteParams(String url, String name, String val) {
        String mod = url;
        try {
            mod = url.replaceAll("\\" + OPNSUB + name + "\\" + CLSSUB, URLEncoder.encode(val, "UTF-8"));
            // mod = url.replaceAll("\\"+OPNSUB+name+"\\"+CLSSUB, val);
        } catch (Exception ex) {
        }
        return mod;
    }

}
