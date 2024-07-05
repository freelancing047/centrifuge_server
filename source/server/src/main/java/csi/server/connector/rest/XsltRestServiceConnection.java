package csi.server.connector.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;

public class XsltRestServiceConnection extends RestServiceConnection {
   protected static final Logger LOG = LogManager.getLogger(XsltRestServiceConnection.class);

   private static String ID = "XsltRestServiceConnection";
    // hack required due JStels XML driver requiring the saxon8.jar file
    private static TransformerFactory transformerFactory = new com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl();
//    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public XsltRestServiceConnection(String url, String urlPrefix, Properties props) throws CentrifugeException {
        super(url, urlPrefix, props);
        OPNSUB = "{:";
        CLSSUB = "}";
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

        // if there are any parameters, substitute them in the URL strings accordingly
        String modifiedURLString = httpURLString;
        String modifiedXsltUrl = query;
        if (null != params) {
            ListIterator<QueryParameterDef> li = params.listIterator();
            while (li.hasNext()) {
                QueryParameterDef qpd = li.next();
                LOG.debug(ID + " modifiedURLString before: " + modifiedURLString);
                modifiedURLString = substituteParams(modifiedURLString, qpd.getName(), qpd.getValue());
                LOG.debug(ID + " modifiedURLString after : " + modifiedURLString);

                if ((query != null) && !query.trim().isEmpty()) {
                   LOG.debug(ID + " modifiedXsltURL before: " + modifiedXsltUrl);
                    modifiedXsltUrl = substituteParams(modifiedXsltUrl, qpd.getName(), qpd.getValue());
                    LOG.debug(ID + " modifiedXsltURL after : " + modifiedXsltUrl);
                }
            }
        }
        // make sure the resulting URL is valid
        URL modifiedURL = isValidURL(modifiedURLString);
        if (null == modifiedURL) {
            throw new Exception("URL is malformed: " + modifiedURLString);
        }

        // go get the data from the REST service
        LOG.debug(ID + ": retrieving data (" + modifiedURLString + ")");
        Document dataDoc = getData(modifiedURL);

        if (dataDoc == null) {
            throw new CentrifugeException("No data document returned");
        }

        Document resultdoc = null;

        // go get the xsl file
        LOG.debug(ID + ": retrieving xsl file (" + modifiedXsltUrl + ")");
        Document xsltdoc = getXsltDoc(modifiedXsltUrl);
        if (xsltdoc == null) {
            resultdoc = dataDoc;
        } else {
            resultdoc = db.newDocument();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xslDocSource = new DOMSource(xsltdoc);
            Result outputTarget = new StreamResult(outputStream);
            //
            // this hack required because JStels XML driver requires the saxon8.jar file
            //
            TransformerFactory tfac = new com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl();
            tfac.newTransformer().transform(xslDocSource, outputTarget);
            //
//            TransformerFactory.newInstance().newTransformer().transform(xslDocSource, outputTarget);
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

            DOMSource dataSource = new DOMSource(dataDoc);
            // DOMSource xsltSource = new DOMSource(xsltdoc);
            Source xsltSource = new StreamSource(is);
            DOMResult domResult = new DOMResult(resultdoc);

            Templates template = transformerFactory.newTemplates(xsltSource);
            Transformer transformer = template.newTransformer();
            transformer.transform(dataSource, domResult);
        }
        NodeList resultNodes = resultdoc.getElementsByTagName("Row");
        return new GenericRestServiceResultSet(resultNodes);
    }

    private Document getXsltDoc(String xsltUrlStr) throws Exception {
        if ((xsltUrlStr == null) || xsltUrlStr.isEmpty()) {
            return null;
        }

        URL xsltURL = null;
        try {
            xsltURL = new URL(xsltUrlStr);
        } catch (MalformedURLException e) {
            throw new CentrifugeException("Invalid url for xslt");
        }

        HttpEntity entity = null;
        boolean ssl = xsltURL.getProtocol().equalsIgnoreCase("https");

        int port = xsltURL.getPort();
        if (port == -1) {
            port = 80;
        }

        Document document = null;
        ClientConnectionManager cm = null;
        try {
            HttpParams params = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

            // Create and initialize scheme registry
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            // schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port));
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), http_port_int));
            if (ssl) {
                SSLSocketFactory sslSF = null;
                if ((acceptAllCerts != null) && (acceptAllCerts.equalsIgnoreCase("true"))) {
                    sslSF = getNaiveSSLSocketFactory();
                } else {
                    sslSF = getSSLSocketFactory(truststore, truststorepw, truststoretp, keystore, keystorepw, keystoretp);
                }
                // schemeRegistry.register(new Scheme("https", sslSF, port));
                schemeRegistry.register(new Scheme("https", sslSF, https_port_int));
            }

            // Create an HttpClient with the ThreadSafeClientConnManager.
            // This connection manager must be used if more than one thread will
            // be using the HttpClient.
            cm = new ThreadSafeClientConnManager(params, schemeRegistry);

            try (DefaultHttpClient httpclient = new DefaultHttpClient(cm, params)) {
               HttpGet httpget = new HttpGet(xsltURL.toString());

               setAdditionalHeaders(httpget);

               HttpResponse response = httpclient.execute(httpget);
               entity = response.getEntity();

               if (entity != null) {
                  try (InputStream is = entity.getContent()) {
                     document = db.parse(is);
                  }
               }
            }
            return document;
        } catch (IOException iox) {
           LOG.debug(ID + ": IOException: " + iox);
            throw new Exception(iox.getMessage());
        } finally {
            if (cm != null) {
                cm.shutdown();
            }
        }
    }
}