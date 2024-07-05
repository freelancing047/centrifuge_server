package csi.tools.migrate;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory;
import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;
import org.hibernate.ejb.Ejb3Configuration;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.XStream;

import csi.security.queries.AclRequest;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.Resource;
import csi.server.util.BuildNumber;

public class MigrateUtil {

    public static final String ALL = "all";

    public static final String DATAVIEWS = "dataviews";

    public static final String TEMPLATES = "templates";

    public static final String USERS = "users";

    public static final String ASSETS = "assets";

    public static final String USERFILES = "userfiles";

    public static final Set<String> SAMPLE_TEMPLATES = new HashSet<String>();
    static {
        SAMPLE_TEMPLATES.add("2eae559e-602d-47dd-baa7-8e57ca90e12a");
        SAMPLE_TEMPLATES.add("d3cb2939-f60e-473b-985e-631092b62870");
        SAMPLE_TEMPLATES.add("1e5ec4aa-329b-4c82-83c3-69c1261cbdba");
        SAMPLE_TEMPLATES.add("28414f5f-2ef6-4ee4-bb1a-1adeeead8a09");
        SAMPLE_TEMPLATES.add("41ff8a68-52a3-4284-8255-4e89777ef11e");
        SAMPLE_TEMPLATES.add("d16528b4-b6a3-40de-8bcd-ab1031e5fd19");
        SAMPLE_TEMPLATES.add("2f4802df-72eb-4628-8ce5-e8ab0032aaa2");
        SAMPLE_TEMPLATES.add("be37c74a-e068-411a-8551-aee3219da9a2");

        SAMPLE_TEMPLATES.add("19ed0294-5ddf-8f40-c6de-d0c053567b70");
        SAMPLE_TEMPLATES.add("2865be8c-d462-5ff6-84d4-d0acd1cf1e6c");
        SAMPLE_TEMPLATES.add("a5962e14-0218-68e4-8666-d0b19c8de352");
        SAMPLE_TEMPLATES.add("bb5dd9d5-a122-0ea1-e960-d0e789f96bb2");
        SAMPLE_TEMPLATES.add("8dccd728-ff25-4841-928c-76050681de02");
        SAMPLE_TEMPLATES.add("80d86355-18ac-fb37-d765-d1074f464d9d");
        SAMPLE_TEMPLATES.add("5caf5597-2bb6-f41b-58b7-d101d079ec56");
        SAMPLE_TEMPLATES.add("633bcdb7-17f7-87d7-f59f-d1177acd2fe6");

        SAMPLE_TEMPLATES.add("e9bd9ce1-67bf-43e2-8dba-f8f78ad62947");
        SAMPLE_TEMPLATES.add("251bd5c2-cebc-4dc2-ae1b-71eda94642f5");
        SAMPLE_TEMPLATES.add("9a18d40e-c4ca-449e-ad74-e68f21124246");
        SAMPLE_TEMPLATES.add("6bfe9f99-8c9f-4f68-b4ca-5c09fda63fbb");
        SAMPLE_TEMPLATES.add("6537373b-393d-4792-937c-17c98e52c90d");
        SAMPLE_TEMPLATES.add("456980da-1618-479c-be94-5dc4a859c44b");
        SAMPLE_TEMPLATES.add("b6a46ff1-f88b-414a-a874-7a945389e1fa");
        SAMPLE_TEMPLATES.add("4c931708-3d6e-4e09-ba34-382ff59efe23");
        SAMPLE_TEMPLATES.add("f0f44812-ccd5-489d-98e2-484910ff3791");
    }

    public static File getContextFile(File serverdir) throws Exception {
        File contextFile = new File(serverdir, "webapps/Centrifuge/META-INF/context.xml");
        if (!contextFile.exists()) {
            throw new Exception("Unable to locate configuration file: " + contextFile.getAbsolutePath());
        }
        return contextFile;
    }

    public static EntityManagerFactory createEntityManagerFactory(Properties dsProps, DataSource ds) {

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.connection.driver_class", dsProps.get("driverClassName"));
        props.put("hibernate.connection.url", dsProps.get("url"));

        Ejb3Configuration configuration = new Ejb3Configuration();
        configuration.configure("meta", props);
        // configuration.configure( "meta" );
        configuration.setDataSource(ds);
        configuration.buildMappings();

        EntityManagerFactory managerFactory = configuration.buildEntityManagerFactory();
        return managerFactory;
        // EntityManager manager = managerFactory.createEntityManager();
        // return manager;
    }

    public static DataSource createDataSource(Properties dsProps) throws Exception {
        Class.forName(dsProps.getProperty("driverClassName"));

        DataSource ds = BasicDataSourceFactory.createDataSource(dsProps);
        return ds;
    }

    public static Properties parseDataSourceContext(File serverdir) throws FileNotFoundException, XPathExpressionException, Exception {
        Properties props = new Properties();
        XPathFactory xpathFac = XPathFactory.newInstance();
        FileInputStream fin = null;
        try {
            File contextFile = getContextFile(serverdir);
            fin = new FileInputStream(contextFile);
            InputSource source = new InputSource(fin);

            XPath xpath = xpathFac.newXPath();
            XPathExpression searchExpr = xpath.compile("//Resource[@name = 'jdbc/MetaDB']");
            Node dsNode = (Node) searchExpr.evaluate(source, XPathConstants.NODE);
            if (dsNode == null) {
                throw new Exception("Unable to locate datasource configuration in: " + contextFile);
            }

            props.put("driverClassName", xpath.evaluate("@driverClassName", dsNode));
            props.put("url", xpath.evaluate("@url", dsNode));
            props.put("username", xpath.evaluate("@username", dsNode));
            props.put("password", xpath.evaluate("@password", dsNode));
            props.put("defaultAutoCommit", "false");
            props.put("maxActive", 10);
            props.put("minIdle", 1);
            props.put("maxIdle", 10);
            props.put("maxWait", 5000);
            props.put("accessToUnderlyingConnectionAllowed", "true");

        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (Exception e) {

                }
            }
        }
        return props;
    }

    public static CopyManager getCopyManager(Connection conn) throws SQLException {
        PGConnection pgConn = null;
        if (conn instanceof PGConnection) {
            pgConn = (PGConnection) conn;

        } else if (conn instanceof DelegatingConnection) {
            Connection c = ((DelegatingConnection) conn).getInnermostDelegate();

            if (c instanceof PGConnection) {
                pgConn = (PGConnection) c;
            }
        }

        if (pgConn == null) {
            throw new IllegalStateException("Native postgreSQL connection not found");
        }

        return pgConn.getCopyAPI();
    }

    public static String getCacheTableName(String dvUuid) {
        return "cache_" + toDbUuid(dvUuid);
    }

    public static String getBroadcastTableName(String dvUuid) {
        return "broadcast_" + toDbUuid(dvUuid);
    }

    public static String toDbUuid(String uuid) {
        return uuid.replace('-', '_');
    }

    public static Object invokeGetter(Object obj, String property) {
        Method method;
        try {
            method = obj.getClass().getMethod(property, new Class[0]);
            return method.invoke(obj, new Object[0]);
        } catch (Exception e) {
            // System.out.println("Error invoking getter: " + obj + "." + property);
            // e.printStackTrace();
            return null;

        }
    }

    public static void invokeSetter(Object obj, String property, Object val) {
        Method method;
        try {
            method = obj.getClass().getMethod(property, new Class[] { val.getClass() });
            method.invoke(obj, val);
        } catch (Exception e) {
            // ignore
            // System.out.println("Error invoking setter: " + obj + "." + property);
            // e.printStackTrace();
        }
    }

    public static Object invokeMethod(Object obj, String methodName, Class[] argclasses, Object... args) {
        Method method;
        try {
            method = obj.getClass().getMethod(methodName, argclasses);
            return method.invoke(obj, args);
        } catch (Exception e) {
            // System.out.println("Error invoking method: " + obj + "." + methodName);
            // e.printStackTrace();
        }
        return null;
    }

    public static Object invokeStaticGetter(Class clz, String property) throws Exception {
        try {
            Method method = clz.getMethod(property, new Class[0]);
            return method.invoke(null, new Object[0]);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object invokeStaticMethod(Class clz, String method, Object... argVals) {
        try {
            Class[] argClasses = new Class[argVals.length];
            for (int i = 0; i < argVals.length; i++) {
                argClasses[i] = argVals[i].getClass();
                if (Collection.class.isAssignableFrom(argVals[i].getClass())) {
                    argClasses[i] = Collection.class;
                }
            }
            Method m = clz.getMethod(method, argClasses);
            return m.invoke(null, argVals);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void copyDirectory(File sourceLocation, File targetLocation, FilenameFilter filter) throws IOException {
        if (!sourceLocation.exists()) {
            return;
        }
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }
            String[] children = null;
            if (filter == null) {
                children = sourceLocation.list();
            } else {
                children = sourceLocation.list(filter);
            }

            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]), filter);
            }
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    public static void copyFile(File source, File target) throws IOException {
        File parentFile = target.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }

        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);
        try {
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    // public static String getXStreamHelperClassName()
    // {
    // String xstreamHelperClassName = "csi.server.dao.helper.XStreamHelper";
    // if( isVersion( "1.6" ) ) {
    // xstreamHelperClassName = "centrifuge.model.dataview.persistence.XStreamHelper";
    // }
    //        
    // return xstreamHelperClassName;
    // }

    public static float parseVersion(String versionstr) throws CentrifugeException {
        if (versionstr == null || versionstr.isEmpty()) {
            throw new CentrifugeException("Encountered null or empty version");
        }
        try {
            return Float.parseFloat(versionstr.replaceAll("[^0-9]*([0-9]+\\.?[0-9]*)[^0-9]*", "$1"));
        } catch (Exception e) {
            throw new CentrifugeException("Unknown archive version " + versionstr, e);
        }
    }

    public static boolean isVersion(String startsWith) {
        return BuildNumber.getVersion().startsWith(startsWith);
    }

    public static boolean tableExists(Connection conn, String tableName) throws CentrifugeException {
        ResultSet rs = null;
        boolean exists = false;
        try {
            rs = conn.getMetaData().getTables(null, null, tableName, new String[] { "TABLE" });
            exists = rs.next();

        } catch (Throwable e) {
            System.out.println(String.format("Error checking cache for table %s", tableName));
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    // ignore;
                }
            }
        }

        return exists;
    }

    public static XStream getImportExportCodec() throws ClassNotFoundException, Exception {
        return XStreamHelper.getImportExportCodec();
    }

    public static File getServerThumbDir(File serverdir) {
        String name = "snapshots";
        if (isVersion("1.6")) {
            name = "webapps/Centrifuge/assets";
        }
        return new File(serverdir, name);
    }

    public static File getServerAssetDir(File serverdir) {
        String name = "webapps/Centrifuge/assets";
        return new File(serverdir, name);
    }

    public static File getServerVizDataDir(File serverdir) {
        String name = "vizdata";
        return new File(serverdir, name);
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static String makeUniqueResourceName(EntityManager em, Class<?> clz, String name) {
        if (isVersion("1.6")) {
            return null;
        }
        return AclRequest.makeUniqueResourceName((Class<Resource>) clz, name);
    }

    public static void quiteClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
            }
        }
    }

    public static boolean isSampleTemplate(String uuid) {
        return SAMPLE_TEMPLATES.contains(uuid.toLowerCase());
    }

}
