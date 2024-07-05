package csi.server.connector.webservice;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import org.xml.sax.InputSource;

import com.sun.tools.ws.WsImport;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler;
import com.sun.tools.ws.wscompile.ErrorReceiverFilter;
import com.sun.tools.ws.wscompile.WsimportOptions;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.connector.AbstractConnectionFactory;

public abstract class AbstractWebServiceConnectionFactory extends AbstractConnectionFactory {

    protected Map<String, Model> serviceTracker = new WeakHashMap<String, Model>();

    protected String template;

    public AbstractWebServiceConnectionFactory() {
        super();
    }

    public boolean isTracking(String url) {
        String augmented = url;
        if (url.startsWith(template)) {
            augmented = url.substring(template.length());
        }
        return serviceTracker.containsKey(augmented);
    }

    protected void track(String url, Model model) {
        serviceTracker.put(url, model);
    }

    void release(String url) {
        serviceTracker.remove(url);
    }

    public Model getServiceContract(String url) {
        String augmented = url;
        if (url.startsWith(template)) {
            augmented = url.substring(template.length());
        }
        return serviceTracker.get(augmented);
    }

    abstract protected WebServiceConnection createConnection();

    @Override
    public Properties toNativeProperties(Map<String, String> propMap) {

        // construct properties to pass on to driver
        // based on the csi properties provided by the ui and default properties
        // in jdbc-driver.xml
        Properties nativeProps = new Properties();

        nativeProps.putAll(getDefaultProperties());

        // for now just shove everything in as is
        nativeProps.putAll(propMap);
        return nativeProps;
    }

    public Connection getConnection(String url, Properties props) throws SQLException, GeneralSecurityException, ClassNotFoundException {

        String modifiedURL = url.substring(template.length());

        int queryIndex = modifiedURL.indexOf('?');
        if (queryIndex != -1) {

        }

        boolean isFile = false;
        File cachedLocation = null;
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();

        URL resource = tccl.getResource(modifiedURL);

        if (resource == null) {
            cachedLocation = new File(modifiedURL);
            if (cachedLocation.exists()) {
                try {
                    resource = cachedLocation.toURI().toURL();
                    isFile = true;
                } catch (MalformedURLException e) {
                    throw new SQLException(e.getMessage());
                }
            }
        }

        try {
            if (!isTracking(modifiedURL)) {
                WsimportOptions options = new WsimportOptions();
                options.compatibilityMode = WsimportOptions.EXTENSION;
                options.debug = true;

                ErrorReceiverFilter errorFilter = new ErrorReceiverFilter();

                if (resource != null) {
                   try (InputStream stream = resource.openStream()) {
                        InputSource wsdlSource = new InputSource(stream);
                      wsdlSource.setSystemId(modifiedURL);
                      options.addWSDL(wsdlSource);

                      WSDLModeler modeler = new WSDLModeler(options, errorFilter);
                      Model model = modeler.buildModel();

                      track(modifiedURL, model);

                // we're dealing w/ a non-pkg'ed service. dynamically import
                // to generate required classes -- only used for introspection
                // purposes
                      if (isFile) {
                         options = new WsimportOptions();

                         options.compatibilityMode = WsimportOptions.EXTENSION;
                         options.debug = true;
                         options.addWSDL(cachedLocation);

                         List<String> opts = new ArrayList<String>();

                         opts.add("-s");
                         opts.add("./webapps/Centrifuge/WEB-INF/classes");
                         opts.add("-d");
                         opts.add("./webapps/Centrifuge/WEB-INF/classes");
                         opts.add("-keep");
                         opts.add(cachedLocation.getAbsolutePath());

                         String[] args = opts.toArray(new String[0]);

                         WsImport.doMain(args);
                      }
                   }
                }
            }
            WebServiceConnection service = createConnection();
            Model contract = getServiceContract(modifiedURL);

            service.setModel(contract);
            service.setWSDLURL(resource);
            return service;
        } catch (Throwable t) {
            throw new SQLException(t.getMessage());
        }

    }

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
        return null;
    }

    @Override
    public Connection getConnection(Map<String, String> propMap) throws SQLException, GeneralSecurityException, ClassNotFoundException {
        return null;
    }

    @Override
    public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<String>();
    }

    @Override
    public List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<ColumnDef>();
    }

    @Override
    public List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<CsiMap<String, String>>();
    }

    @Override
    public List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<SqlTableDef>();
    }

    @Override
    public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        return new ArrayList<String>();
    }

}