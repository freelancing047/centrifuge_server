package csi.server.connector.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.connector.AbstractConnectionFactory;

public class RestServiceConnection implements Connection {
    protected static final Logger LOG = LogManager.getLogger(RestServiceConnection.class);

    protected static String ID = "RestServiceConnection";
    protected String url;
    protected String httpURLString;
    protected URL httpURL;
    protected String protocol;
    protected Properties props;

    protected String http_port;
    protected String https_port;
    protected int http_port_int;
    protected int https_port_int;
    protected String keystore;
    protected String keystorepw;
    protected String keystoretp;
    protected String truststore;
    protected String truststorepw;
    protected String truststoretp;
    protected String urlparams = null;

    protected DocumentBuilderFactory dbf;
    protected DocumentBuilder db;

    protected boolean useNamespaces;
    protected String acceptAllCerts;

    protected static String OPNSUB = "{";
    protected static String CLSSUB = "}";

    public RestServiceConnection(String url, String urlPrefix, Properties props) throws CentrifugeException {
        this.url = url;
        this.props = props;

        http_port = props.getProperty(RestServiceConnectionFactory.HTTP_PORT);
        https_port = props.getProperty(RestServiceConnectionFactory.HTTPS_PORT);
        keystore = props.getProperty(RestServiceConnectionFactory.KEYSTORE);
        keystorepw = props.getProperty(RestServiceConnectionFactory.KEYSTOREPW);
        keystoretp = props.getProperty(RestServiceConnectionFactory.KEYSTORETP);
        truststore = props.getProperty(RestServiceConnectionFactory.TRUSTSTORE);
        truststorepw = props.getProperty(RestServiceConnectionFactory.TRUSTSTOREPW);
        truststoretp = props.getProperty(RestServiceConnectionFactory.TRUSTSTORETP);
        urlparams = props.getProperty(RestServiceConnectionFactory.URLPARAMS);
        acceptAllCerts = props.getProperty(RestServiceConnectionFactory.ACCEPT_ALL_CERTS);

        // get connection parameters
        String pvalue;
        int pcnt = 0;
        while (true) {
            pvalue = props.getProperty(AbstractConnectionFactory.CSI_PARAMS_PREFIX + "." + pcnt);
            if (null == pvalue) {
               break;
            }
            if ((0 == pcnt) && (null == urlparams)) {
                urlparams = pvalue;
            } else {
                urlparams = urlparams + "&" + pvalue;
            }
            LOG.debug(ID + ": url parameters: " + urlparams);
            pcnt++;
        }

        String s = props.getProperty(RestServiceConnectionFactory.USING_NAMESPACES, "false");
        useNamespaces = Boolean.parseBoolean(s);

        if (null == http_port) {
            http_port = "80";
        }
        if (null == https_port) {
            https_port = "443";
        }
        if (null == keystoretp) {
            keystoretp = "jks";
        }
        if (null == truststoretp) {
            truststoretp = "jks";
        }

        LOG.debug(ID + ": property http_port=" + http_port);
        LOG.debug(ID + ": property https_port=" + https_port);
        LOG.debug(ID + ": property keystore=" + keystore);
        LOG.debug(ID + ": property keystorepw=" + keystorepw);
        LOG.debug(ID + ": property keystoretp=" + keystoretp);
        LOG.debug(ID + ": property truststore=" + truststore);
        LOG.debug(ID + ": property truststorepw=" + truststorepw);
        LOG.debug(ID + ": property truststoretp=" + truststoretp);
        LOG.debug(ID + ": property urlparams=" + urlparams);
        LOG.debug(ID + ": property acceptAllCerts=" + acceptAllCerts);

        // convert port numbers to int
        try {
            http_port_int = Integer.parseInt(http_port);
            https_port_int = Integer.parseInt(https_port);
        } catch (NumberFormatException nfx) {
            throw new CentrifugeException("Invalid port number specified in driver configuration");
        }

        httpURLString = url;
        // strip off the driver identifier prefix
        if ((urlPrefix != null) && !urlPrefix.isEmpty()) {
            httpURLString = url.replaceFirst(urlPrefix, "");
        }

        // make sure the remaining URL is valid
        httpURL = isValidURL(httpURLString);
        if (null == httpURL) {
            throw new CentrifugeException("URL is malformed: " + httpURLString);
        }

        // save off the protocol
        protocol = httpURL.getProtocol().toLowerCase();

        // Get document builder to handle XML results from REST service
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(useNamespaces);
        try {

            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pcx) {
        }
    }

    void validate() {
        LOG.debug(ID + ": validate");
    }

    //
    // Get data
    //
    public Document getData(URL url) throws CentrifugeException {
        HttpEntity entity = null;
        boolean ssl = protocol.equals("https");

        LOG.debug(ID + ": getData url=" + url.toString() + " ssl=" + ssl);

        Document document = null;
        ClientConnectionManager cm = null;
        try {
            HttpParams params = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

            // Create and initialize scheme registry
            SchemeRegistry schemeRegistry = new SchemeRegistry();

            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), http_port_int));
            if (ssl) {
                SSLSocketFactory sslSF = null;
                if ((acceptAllCerts != null) && (acceptAllCerts.equalsIgnoreCase("true"))) {
                    LOG.debug(ID + ": getData: getNaiveSSLSocketFactory, accept all certs");
                    sslSF = getNaiveSSLSocketFactory();
                } else {
                    LOG.debug(ID + ": getData: getSSLSocketFactory, accept only known certs");
                    sslSF = getSSLSocketFactory(truststore, truststorepw, truststoretp, keystore, keystorepw, keystoretp);
                }
                schemeRegistry.register(new Scheme("https", sslSF, https_port_int));
            }

            // Create an HttpClient with the ThreadSafeClientConnManager.
            // This connection manager must be used if more than one thread will
            // be using the HttpClient.
            cm = new ThreadSafeClientConnManager(params, schemeRegistry);

            try (DefaultHttpClient httpclient = new DefaultHttpClient(cm, params)) {
               String urlString = url.toString();

               if ((urlparams != null) && !urlparams.trim().isEmpty()) {
                  String delimiter = "&";

                  if ((url.getQuery() == null) || url.getQuery().trim().isEmpty()) {
                     delimiter = "?";
                  }
                  urlString += delimiter + urlparams;
               }
               LOG.debug(ID + ": getData url=" + urlString);
               HttpGet httpget = new HttpGet(urlString);

               setAdditionalHeaders(httpget);

               LOG.debug(ID + ": getData requesting " + httpget.getRequestLine());
               HttpResponse response = httpclient.execute(httpget);

               if (response.getStatusLine().getStatusCode() != 200) {
                  throw new CentrifugeException(response.getStatusLine().toString());
               }
               entity = response.getEntity();

               if (entity != null) {
                  LOG.debug(ID + ": getData received " + entity.getContentLength() + " bytes back from REST service");

                  InputStream is = entity.getContent();
                  document = db.parse(is);
               }
            }
        } catch (Exception iox) {
            LOG.debug(ID + ": IOException: " + iox);
            throw new CentrifugeException(iox.getMessage());
        } finally {
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            if (cm != null) {
                cm.shutdown();
            }
        }

        return document;
    }

    //
    // Convert InputStream to String
    //
   public String convertStreamToString(InputStream is) {
      StringBuilder sb = new StringBuilder();
      String line = null;

      try (InputStreamReader inputStreamReader = new InputStreamReader(is);
           BufferedReader reader = new BufferedReader(inputStreamReader)) {
         while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
         }
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            is.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return sb.toString();
   }

   public String substituteParams(String url, String name, String val) {
      String mod = url;

      try {
         mod = url.replaceAll("\\" + OPNSUB + name + "\\" + CLSSUB, URLEncoder.encode(val, "UTF-8"));
      } catch (Exception ex) {
      }
      return mod;
   }

    //
    // See if URL is of valid shape
    //
    public URL isValidURL(String url) {
        URL u = null;

        try {
            u = new URL(url);
        } catch (MalformedURLException mux) {
            return null;
        }
        return u;
    }

    //
    // See if we can resolve the hostname in the REST URL
    //
    public boolean isReachable(URL url) {
        String host = "";
        boolean rc;

        try {
            LOG.debug(ID + ": isReachable URL=" + url.toString());
            host = url.getHost();
            InetAddress address = InetAddress.getByName(host);

            // rc = address.isReachable(3000);
            LOG.debug(ID + ": isReachable Host=" + address.getHostName() + " Addr=" + address.getHostAddress());
            rc = true;
        } catch (UnknownHostException e) {
            LOG.debug(ID + ": isReachable: Unable to resolve " + host);
            rc = false;
        }
        return rc;
    }

    public SSLSocketFactory getNaiveSSLSocketFactory() throws Exception {
        SSLSocketFactory sslSF = null;
        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                LOG.debug(ID + ": checkClientTrusted is ignoring the client credentials");
            }

            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                LOG.debug(ID + ": checkServerTrusted is ignoring the server credentials");
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[] { tm }, null);
        sslSF = new SSLSocketFactory(ctx);
        sslSF.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return sslSF;
    }

    public SSLSocketFactory getSSLSocketFactory(String tstore, String tpass, String ttype, String kstore, String kpass, String ktype) throws Exception {
        SSLSocketFactory sslSF = null;
        KeyStore ts = null;
        KeyStore ks = null;
        File tsfile = null;
        File ksfile = null;
        FileInputStream in_t = null;
        FileInputStream in_k = null;

        // if no truststore or keystore configured, use default
        if ((null == tstore) && (null == kstore)) {
            sslSF = SSLSocketFactory.getSocketFactory();
        }
        // both a truststore and keystore are configured
        else if ((null != tstore) && (null != kstore)) {
            // read in truststore (the thing that we need to validate that the server
            // we're connecting to is valid
            ts = KeyStore.getInstance(ttype);
            tsfile = new File(tstore);
            in_t = new FileInputStream(tsfile);
            try {
                ts.load(in_t, tpass.toCharArray());
            } finally {
                in_t.close();
            }

            // read in keystore (the thing that has the client cert that we will send
            // to the target server to identify us
            ks = KeyStore.getInstance(ktype);
            ksfile = new File(kstore);
            in_k = new FileInputStream(ksfile);
            try {
                ks.load(in_k, kpass.toCharArray());
            } finally {
                in_k.close();
            }

            sslSF = new SSLSocketFactory(ks, kpass, ts);
        }
        // if only truststore specified, take care of it
        else if (null != tstore) {
            ts = KeyStore.getInstance(ttype);
            tsfile = new File(tstore);
            in_t = new FileInputStream(tsfile);
            try {
                ts.load(in_t, tpass.toCharArray());
            } finally {
                in_t.close();
            }
            sslSF = new SSLSocketFactory(ts);
        }
        // only keystore is specified. do nothing
        else {
            //
        }

        return sslSF;
    }

    public void setAdditionalHeaders(HttpGet httpget) {

    }

    public String encodeSpaces(String s) {
       return (s == null) ? null : s.replace(" ", "%20");
    }

    public String urlEncode(String s) {
        if (null == s) {
         return null;
      }
        String rs = null;
        try {
            rs = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException uex) {
            LOG.debug(ID + ": urlEncode: " + uex.getMessage());
            rs = s;
        }
        return rs;
    }

    /*
     * java.sql.Connection Methods
     */

    public void clearWarnings() throws SQLException {
        LOG.debug(ID + ": clearWarnings");
    }

    public void close() throws SQLException {
        LOG.debug(ID + ": close");

    }

    public void commit() throws SQLException {
        LOG.debug(ID + ": commit");

    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        LOG.debug(ID + ": createArrayOf");
        return null;
    }

    public Blob createBlob() throws SQLException {
        LOG.debug(ID + ": createBlob");
        return null;
    }

    public Clob createClob() throws SQLException {
        LOG.debug(ID + ": createClob");
        return null;
    }

    public Statement createStatement() throws SQLException {
        LOG.debug(ID + ": createStatement()");
        return null;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        LOG.debug(ID + ": createStatement(2)");
        return null;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        LOG.debug(ID + ": createStatement(3)");
        return null;
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        LOG.debug(ID + ": createStruct");
        return null;
    }

    public void setSchema(String schema) throws SQLException {

    }

    public String getSchema() throws SQLException {
        return null;
    }

    public void abort(Executor executor) throws SQLException {

    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    public boolean getAutoCommit() throws SQLException {
        LOG.debug(ID + ": getAutoCommit");
        return false;
    }

    public String getCatalog() throws SQLException {
        LOG.debug(ID + ": getCatalog");
        return null;
    }

    public Properties getClientInfo() throws SQLException {
        LOG.debug(ID + ": getClientInfo()");
        return null;
    }

    public String getClientInfo(String name) throws SQLException {
        LOG.debug(ID + ": getClientInfo(1)");
        return null;
    }

    public int getHoldability() throws SQLException {
        LOG.debug(ID + ": getHoldability");
        return 0;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        LOG.debug(ID + ": getMetaData");
        return null;
    }

    public int getTransactionIsolation() throws SQLException {
        LOG.debug(ID + ": getTransactionIsolation");
        return 0;
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        LOG.debug(ID + ": getTypeMap");
        return null;
    }

    public SQLWarning getWarnings() throws SQLException {
        LOG.debug(ID + ": getWarnings");
        return null;
    }

    public boolean isClosed() throws SQLException {
        LOG.debug(ID + ": isClosed");
        return false;
    }

    public boolean isReadOnly() throws SQLException {
        LOG.debug(ID + ": isReadOnly");
        return false;
    }

    public boolean isValid(int timeout) throws SQLException {
        LOG.debug(ID + ": isValid");
        return false;
    }

    public String nativeSQL(String sql) throws SQLException {
        LOG.debug(ID + ": nativeSQL");
        return null;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        LOG.debug(ID + ": prepareCall(1)");
        return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        LOG.debug(ID + ": prepareCall(3)");
        return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        LOG.debug(ID + ": prepareCall(4)");
        return null;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        LOG.debug(ID + ": prepareStatement(1)");
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        LOG.debug(ID + ": prepareStatement(2)");
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        LOG.debug(ID + ": prepareStatement(2b)");
        return null;
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        LOG.debug(ID + ": prepareStatement(2c)");
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        LOG.debug(ID + ": prepareStatement(2d)");
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        LOG.debug(ID + ": prepareStatement(4a)");
        return null;
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        LOG.debug(ID + ": releaseSavePoint");

    }

    public void rollback() throws SQLException {
        LOG.debug(ID + ": rollback()");

    }

    public void rollback(Savepoint savepoint) throws SQLException {
        LOG.debug(ID + ": rollback(1)");

    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        LOG.debug(ID + ": setAutoCommt");

    }

    public void setCatalog(String catalog) throws SQLException {
        LOG.debug(ID + ": setCatalog");

    }

    public void setHoldability(int holdability) throws SQLException {
        LOG.debug(ID + ": setHoldability");

    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        LOG.debug(ID + ": setReadOnly");

    }

    public Savepoint setSavepoint() throws SQLException {
        LOG.debug(ID + ": setSavepoint()");
        return null;
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        LOG.debug(ID + ": setSavepoint(1)");
        return null;
    }

    public void setTransactionIsolation(int level) throws SQLException {
        LOG.debug(ID + ": setTransactionIsolation");

    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        LOG.debug(ID + ": setTypeMap");

    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        LOG.debug(ID + ": isWrapperFor");
        return false;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        LOG.debug(ID + ": unwrap");
        return null;
    }

    public ResultSet execute(String operationName, List<QueryParameterDef> params, String query) throws Throwable {
        String modifiedURLString = httpURLString;
        if ((query != null) && !query.trim().isEmpty()) {
            modifiedURLString = modifiedURLString + query.trim();
        }

        // make sure the remaining URL is valid
        URL modifiedURL = isValidURL(modifiedURLString);
        if (null == modifiedURL) {
            throw new Exception("URL is malformed: " + modifiedURLString);
        }
        boolean ok = isReachable(modifiedURL);
        if (!ok) {
            throw new Exception("Unable to resolve hostname in URL " + modifiedURLString);
        }
        // go get the data from the REST service
        Document document = getData(modifiedURL);

        RestServiceResultSet resultSet = new RestServiceResultSet(document, query);
        return resultSet;
    }

    @Override
    public NClob createNClob() throws SQLException {
        LOG.debug(ID + ": createNClob");
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        LOG.debug(ID + ": createSQLXML");
        return null;
    }

    @Override
    public void setClientInfo(Properties arg0) throws SQLClientInfoException {
        LOG.debug(ID + ": setClientInfo(1)");

    }

    @Override
    public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
        LOG.debug(ID + ": setClientInfo(2)");

    }

}
