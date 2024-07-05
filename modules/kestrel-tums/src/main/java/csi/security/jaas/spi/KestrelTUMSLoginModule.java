package csi.security.jaas.spi;

import csi.security.jaas.JAASRole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;

import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;

import csi.server.dao.Users;
import csi.server.dao.jpa.GroupDAOBean;
import csi.license.LicenseManager;
import csi.server.common.identity.Group;
import csi.server.common.identity.Role;
import csi.server.common.identity.User;
import csi.server.common.exception.CentrifugeException;
import csi.server.dao.RoleSupport;

import csi.security.jaas.spi.callback.X509Callback;
import csi.server.dao.CsiPersistenceManager;
import csi.security.CsiSecurityManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KestrelTUMSLoginModule extends SimpleLoginModule {

    private static String ID = "KestrelTUMSLoginModule";
    private static String PROPERTY_USER_AND_PERMISSIONS_SERVICE = "UserAndPermissionsService";
    private static String PROPERTY_PERMISSIONS_SERVICE = "PermissionsService";
    private static String PROPERTY_KEYSTOREPATH = "KeyStorePath";
    private static String PROPERTY_KEYSTOREPASS = "KeyStorePass";
    private static String PROPERTY_KEYSTORETYPE = "KeyStoreType";
    private static String PROPERTY_TRUSTSTOREPATH = "TrustStorePath";
    private static String PROPERTY_TRUSTSTOREPASS = "TrustStorePass";
    private static String PROPERTY_TRUSTSTORETYPE = "TrustStoreType";
    private static String PROPERTY_USE_NAMESPACES = "UseNamespaces";
    private static String PROPERTY_CREDENTIALS = "Credentials";
    private static String PROPERTY_TESTENDPOINT = "TestEndPoint";
    private static String PROPERTY_URLPARAMS = "URLParams";
    private static String PROPERTY_ROLESQUERY = "RolesQuery";
    private static String PROPERTY_ACTIVEQUERY = "ActiveQuery";
    private static String PROPERTY_RELAXED = "Relaxed"; // allow known users to login if TUMS is down

    private static String KEY_ROLES = "Roles";
    private static String KEY_ACTIVE = "Active";

    private static Boolean REMOVE = true;

    private static String USER_AND_PERMISSIONS_SERVICE;
    private static String PERMISSIONS_SERVICE;
    private static URL UP_SERVICE_URL;
    private static URL P_SERVICE_URL;
    private static String KEYSTOREPATH;
    private static String KEYSTOREPASS;
    private static String KEYSTORETYPE;
    private static String TRUSTSTOREPATH;
    private static String TRUSTSTOREPASS;
    private static String TRUSTSTORETYPE;
    private static String CREDENTIALS;
    private static String TESTENDPOINT;
    private static String URLPARAMS = null;
    private static String ROLESQUERY = "/tums_api/roles/role/name";
    private static String ACTIVEQUERY = "/tums_api/user/active";
    private static String usernamePATTERN = "{username}";

    protected static DocumentBuilderFactory dbf;
    protected static DocumentBuilder db;
    protected static boolean useNamespaces;
    protected static boolean relaxed;

    private XPath xpath = XPathFactory.newInstance().newXPath();

    public void initialize(Subject subject, CallbackHandler handler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, handler, sharedState, options);

        TESTENDPOINT = (String) options.get(PROPERTY_TESTENDPOINT);
        URLPARAMS = (String) options.get(PROPERTY_URLPARAMS);

        USER_AND_PERMISSIONS_SERVICE = (String) options.get(PROPERTY_USER_AND_PERMISSIONS_SERVICE);
        if (null == USER_AND_PERMISSIONS_SERVICE) {
            throw new IllegalStateException("Failed to initialize " + ID + ".  Missing property: " + PROPERTY_USER_AND_PERMISSIONS_SERVICE);
        } else {
            if (null != TESTENDPOINT) {
                UP_SERVICE_URL = isValidURL(USER_AND_PERMISSIONS_SERVICE + TESTENDPOINT);
            } else {
                UP_SERVICE_URL = isValidURL(USER_AND_PERMISSIONS_SERVICE);
            }
            if (null == UP_SERVICE_URL) {
                throw new IllegalStateException("Failed to initialize " + ID + ".  Malformed URL: " + USER_AND_PERMISSIONS_SERVICE);
            }
        }

        PERMISSIONS_SERVICE = (String) options.get(PROPERTY_PERMISSIONS_SERVICE);
        if (null == PERMISSIONS_SERVICE) {
            throw new IllegalStateException("Failed to initialize " + ID + ".  Missing property: " + PROPERTY_PERMISSIONS_SERVICE);
        } else {
            if (null != TESTENDPOINT) {
                P_SERVICE_URL = isValidURL(PERMISSIONS_SERVICE + TESTENDPOINT);
            } else {
                P_SERVICE_URL = isValidURL(PERMISSIONS_SERVICE);
            }
            if (null == P_SERVICE_URL) {
                throw new IllegalStateException("Failed to initialize " + ID + ".  Malformed URL: " + PERMISSIONS_SERVICE);
            }
        }

        String rq = (String) options.get(PROPERTY_ROLESQUERY);
        if (null != rq) {
            ROLESQUERY = rq;
        }
        String aq = (String) options.get(PROPERTY_ACTIVEQUERY);
        if (null != aq) {
            ACTIVEQUERY = aq;
        }

        String s = (String) options.get(PROPERTY_USE_NAMESPACES);
        if (null == s)
            s = "false";
        useNamespaces = Boolean.parseBoolean(s);

        String r = (String) options.get(PROPERTY_RELAXED);
        if (null == r)
            r = "false";
        relaxed = Boolean.parseBoolean(r);

        KEYSTOREPATH = (String) options.get(PROPERTY_KEYSTOREPATH);
        KEYSTOREPASS = (String) options.get(PROPERTY_KEYSTOREPASS);
        KEYSTORETYPE = (String) options.get(PROPERTY_KEYSTORETYPE);
        TRUSTSTOREPATH = (String) options.get(PROPERTY_TRUSTSTOREPATH);
        TRUSTSTOREPASS = (String) options.get(PROPERTY_TRUSTSTOREPASS);
        TRUSTSTORETYPE = (String) options.get(PROPERTY_TRUSTSTORETYPE);

        CREDENTIALS = (String) options.get(PROPERTY_CREDENTIALS);
        if (CREDENTIALS == null) {
            throw new IllegalStateException("Failed to initialize " + ID + ".  Missing property: " + PROPERTY_CREDENTIALS);
        }

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(useNamespaces);
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pcx) {
        }
    }

    @Override
    public boolean login() throws LoginException {
        try {
            Callback[] callbacks = configureCallbacks();
            handler.handle(callbacks);
            String username = ((NameCallback) callbacks[0]).getName();
            String userPass = new String(((PasswordCallback) callbacks[1]).getPassword());

            boolean knownuser = CsiSecurityManager.isRegisteredUser(username);

            // If the inbound user IS known, we just need to see if the user's roles have changed. If they
            // have, adjust the group memberships accordingly.
            if (knownuser) {
                String[] roles = getTUMSRoles(username);
                if (null == roles) {
                    if (!relaxed) {
                        throw new LoginException(ID + " unable to obtain information from TUMS");
                    } else {
                        log.info(ID + " is configured to allow authenticated existing users to login even if TUMS is not available");
                    }
                }
                updateRoles(username, roles);
            }
            //
            // If the inbound user IS NOT known, get information about the user from TUMS. Use that info
            // to create the user within Centrifuge and to establish the requisite group memberships.
            //
            else {
                HashMap ti = getTUMSInfo(username);

                if (ti.isEmpty()) {
                    throw new LoginException(ID + " unable to obtain information from TUMS");
                }

                boolean active = (Boolean) ti.get(KEY_ACTIVE);

                // if the user is active, create a centrifuge user
                if (active) {
                    addUser(username, userPass);

                    // establish group memberships
                    String[] roles = (String[]) ti.get(KEY_ROLES);
                    if (null != roles) {
                        int cnt = roles.length;
                        for (int i = 0; i < cnt; i++) {
                            assignRole(username, roles[i]);
                        }
                    }
                } else {
                    log.info("User '" + username + "' is inactive in TUMS and will not be auto-enrolled by " + ID);
                    throw new FailedLoginException();
                }
            }
        } catch (IOException e) {
            log.warn(ID + " security module encountered an error", e);
            e.printStackTrace();
        } catch (UnsupportedCallbackException e) {
            log
                    .warn(ID
                            + " security module is not configured properly.  Please verify that there are not multiple versions of the csi-kestrel-tums-login.jar installed with your server.");
        }
        return true;
    }

    private Callback[] configureCallbacks() {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("User Name: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        return callbacks;
    }

    /*
     * 
     * Get TUMS information for a given user and return info in a HashMap
     */
    private HashMap getTUMSInfo(String username) {
        HashMap hm = new HashMap();
        try {
            URL u;
            if (null != URLPARAMS) {
                String urlp = StringUtils.replace(URLPARAMS, usernamePATTERN, username);
                u = isValidURL(UP_SERVICE_URL.toString() + "?" + urlp);
            } else {
                u = UP_SERVICE_URL;
            }
            Document doc = getData(u);

            boolean active = isActiveUser(username, doc);
            hm.put(KEY_ACTIVE, active);

            String[] roles = getTUMSRoles(username, doc);
            if (null != roles) {
                hm.put(KEY_ROLES, roles);
            }
        } catch (Exception ex) {
            log.debug(ID + ":getTUMSInfo - unable to obtain information from TUMS");
        }
        return hm;
    }

    /*
     * 
     * Get active status for a given user
     */
    private boolean isActiveUser(String username, Document d) {
        boolean active = false;
        xpath.reset();
        try {
            NodeList nl = (NodeList) xpath.evaluate(ACTIVEQUERY, d, XPathConstants.NODESET);
            int ncount = nl.getLength();
            if (ncount == 1) {
                active = Boolean.parseBoolean(nl.item(0).getTextContent());
            }
        } catch (Exception ex) {
            log.debug(ID + ":isActiveUser - unable to obtain user status from TUMS");
        }
        return active;
    }

    /*
     * 
     * Get TUMS roles for a given user. Use Document object if provided,
     * otherwise contact TUMS
     */
    private String[] getTUMSRoles(String username) {
        return getTUMSRoles(username, null);
    }

    private String[] getTUMSRoles(String username, Document d) {
        Document doc = null;
        String[] roles = null;

        xpath.reset();
        try {
            URL u;
            if (null == d) {
                if (null != URLPARAMS) {
                    String urlp = StringUtils.replace(URLPARAMS, usernamePATTERN, username);
                    u = isValidURL(P_SERVICE_URL.toString() + "?" + urlp);
                } else {
                    u = P_SERVICE_URL;
                }
                doc = getData(u);
            } else {
                doc = d;
            }
            NodeList nl = (NodeList) xpath.evaluate(ROLESQUERY, doc, XPathConstants.NODESET);
            int ncount = nl.getLength();
            log.debug(ID + ": getTUMSRoles found " + ncount + " roles for " + username);

            if (ncount > 0) {
                roles = new String[ncount];
                for (int i = 0; i < ncount; i++) {
                    Node n = nl.item(i);
                    log.debug(ID + ": getTUMSRoles found " + n.getTextContent() + " role for " + username);
                    roles[i] = n.getTextContent();
                }
            }
        } catch (Exception ex) {
            log.debug(ID + ":getTUMSRoles - unable to obtain role information from TUMS");
        }
        return roles;
    }

    /*
     * 
     * Enroll new user in Centrifuge user database
     */
    private boolean addUser(String username) {
        return addUser(username, null);
    }

    private boolean addUser(String username, String password) {
        boolean rc = false;
        User user = new User();
        user.setName(username);
        user.setRemark("Auto-enrolled by " + ID);

        if (null == password) {
            UUID random = UUID.randomUUID();
            user.setPassword(random.toString());
        } else {
            user.setPassword(password);
        }

        try {
            long allowed = LicenseManager.getUserCount();
            long current = Users.getUserCount();

            if (++current > allowed) {
                log.info("Cannot auto-enroll user " + username + "; licensed user count (" + allowed + ") exceeded");
            } else {
                if (null != Users.findByName(username)) {
                    throw new CentrifugeException(String.format("Error occurred adding user '%s'; user already exists", username));
                }
                EntityManager entityManager = CsiPersistenceManager.getMetaEntityManager();
                EntityTransaction txn = entityManager.getTransaction();
                txn.begin();

                entityManager.merge(user);
                log.info("Previously unknown user " + username + " auto-enrolled by " + ID);

                txn.commit();
                rc = true;
            }
        } catch (CentrifugeException cex) {
            CsiPersistenceManager.rollback();
            log.info(ID + " encountered errors auto-enrolling user " + username);
        } finally {
            CsiPersistenceManager.close();
        }
        return rc;
    }

    /*
     * 
     * Assign/Unassign role
     */
    protected boolean assignRole(String username, String groupname) {
        return assignRole(username, groupname, false);
    }

    protected boolean assignRole(String username, String groupname, boolean remove) {
        boolean rc = false;
        try {
            User user = Users.findByName(username);
            if (null == user) {
                throw new CentrifugeException(String.format("Error occurred assigning/unassigning roles for user '%s'; user not found", username));
            }
            EntityManager entityManager = CsiPersistenceManager.getMetaEntityManager();
            EntityTransaction txn = entityManager.getTransaction();
            txn.begin();

            boolean done;
            GroupDAOBean bean = new GroupDAOBean();
            Group group = bean.findByName(groupname);
            if (group != null) {
                Set<Role> gs = group.getMembers();
                if (remove) {
                    done = gs.remove(user);
                    if (done) {
                        log.info(String.format("User '%s' removed from '%s' group by %s", username, groupname, ID));
                    }
                } else {
                    done = gs.add(user);
                    if (done) {
                        log.info(String.format("User '%s' added to '%s' group by %s", username, groupname, ID));
                    }
                }
                group.setMembers(gs);
                bean.merge(group);
                rc = true;
            }
            txn.commit();
        } catch (CentrifugeException cex) {
            CsiPersistenceManager.rollback();
            log.info(ID + " encountered errors assigning/unassigning roles for user '" + username + "'");
        } finally {
            CsiPersistenceManager.close();
        }
        return rc;
    }

    /*
     * 
     * Make sure user belongs only to the groups provided
     */
    private void updateRoles(String username, String[] roles) {
        if (null == roles)
            return;
        try {
            User user = Users.findByName(username);
            if (null == user) {
                throw new CentrifugeException(String.format("Error occurred updating roles for user '%s'; user not found", username));
            }

            int cnt = roles.length;

            // Get current group memberships
            List<Group> groups = user.getGroups();
            if (groups != null) {
                boolean current;
                String gn;
                for (Group g : user.getGroups()) {
                    current = false;
                    gn = g.getName();
                    // see if group is in the new list of groups
                    for (int i = 0; i < cnt; i++) {
                        if (gn.equals(roles[i])) {
                            current = true;
                            break;
                        }
                    }
                    // if group is not in the new list of groups, remove the
                    // user from it
                    if (!current) {
                        assignRole(username, gn, REMOVE);
                    }
                }
            }

            // add user to new list of groups (doesn't matter if they are
            // already members)
            for (int i = 0; i < cnt; i++) {
                assignRole(username, roles[i]);
            }
        } catch (CentrifugeException cex) {
            log.info(ID + " encountered errors updating roles for user '" + username + "'");
        }
    }

    /*
     * 
     * See if URL is valid shape
     */
    public URL isValidURL(String url) {
        URL u = null;

        try {
            u = new URL(url);
        } catch (MalformedURLException mux) {
            return null;
        }
        return u;
    }

    /*
     * 
     * Get data from URL
     */
    public Document getData(URL url) throws Exception {
        HttpEntity entity = null;
        String protocol = url.getProtocol().toLowerCase();
        boolean ssl = (protocol.equals("https")) ? true : false;

        int port = url.getPort();
        if (port < 0)
            port = url.getDefaultPort();

        Document document = null;
        try {
            HttpParams params = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

            // Create and initialize scheme registry
            SchemeRegistry schemeRegistry = new SchemeRegistry();

            if (ssl) {
                SSLSocketFactory sslSF = getSSLSocketFactory();
                schemeRegistry.register(new Scheme("https", sslSF, port));
            } else {
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port));
            }

            // Create an HttpClient with the ThreadSafeClientConnManager.
            // This connection manager must be used if more than one thread will
            // be using the HttpClient.
            ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
            HttpClient httpclient = new DefaultHttpClient(cm, params);

            String urlString = url.toString();
            HttpGet httpget = new HttpGet(urlString);

            setAdditionalHeaders(httpget);

            log.debug(ID + ": getData requesting " + httpget.getRequestLine());
            HttpResponse response = httpclient.execute(httpget);

            entity = response.getEntity();
            log.debug(ID + ": getData received " + entity.getContentLength() + " bytes back from REST service");

            if (entity != null) {
                InputStream is = entity.getContent();
                db.reset();
                document = db.parse(is);
            }

            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        } catch (IOException iox) {
            log.info("Error occurred obtaining information from TUMS: " + iox.getMessage());
            throw new Exception(iox.getMessage());
        }

        return document;
    }

    /*
     * 
     * Set additional HTTP headers in outbound HTTP request
     */
    public void setAdditionalHeaders(HttpGet httpget) {
        if (null != CREDENTIALS) {
            httpget.addHeader("Authorization", "Basic " + CREDENTIALS);
        }
    }

    /*
     * 
     * "Accept anything" from server
     */
    public SSLSocketFactory getSSLSocketFactory() throws Exception {
        SSLSocketFactory sslSF = null;
        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
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

    /*
     * 
     * Get SSLSocketFactory given truststore/keystore info
     */
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
            // read in truststore (the thing that we need to validate that the
            // server
            // we're connecting to is valid
            ts = KeyStore.getInstance(ttype);
            tsfile = new File(tstore);
            in_t = new FileInputStream(tsfile);
            try {
                ts.load(in_t, tpass.toCharArray());
            } finally {
                in_t.close();
            }

            // read in keystore (the thing that has the client cert that we will
            // send
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
        else if ((null != tstore) && (null == kstore)) {
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

    /*
     * 
     * Convert InputStream to String
     */
    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
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
}
