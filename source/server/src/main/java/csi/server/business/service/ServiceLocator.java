/*
 * @(#) ServiceDiscover.java,  25.03.2010
 *
 */
package csi.server.business.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;

import csi.server.business.service.annotation.Service;

/**
 * Locates annotated services classes, load them and register them.
 *
 * @author <a href="mailto:iulian.boanca@lpro.leverpointinc.com">Iulian Boanca</a>
 *
 */
public class ServiceLocator {
   private static final Logger LOG = LogManager.getLogger(ServiceLocator.class);

    /** The mapping between ServicePath and corresponding Class instance */
    private Map<String, Class<?>> registeredServices = new HashMap<String, Class<?>>();
    private Map<String, Class<?>> registeredRestServices = new HashMap<String, Class<?>>();

    /** Singleton instance */
    private static ServiceLocator instance;

    /**
     * Singleton's private constructor
     */
    private ServiceLocator() {
        loadServiceClasses();
    }

    /**
     * Gets a singleton instance of this class.
     *
     * @return the class singleton instance
     */
    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    /**
     * Gets a Registered Annotated Service Class based on the given <code>servicePath</code>.
     *
     * @param servicePath
     *            the path of the Service Class found in URL and used to annotate the class. E.g.:
     *            /services/graphs2/actions
     * @return the loaded Service Class
     * @throws ClassNotFoundException
     *             in case the Class cannot be found
     */
    public synchronized Class<?> getServiceClass(String servicePath) throws ClassNotFoundException {
        assert (servicePath != null) && !servicePath.isEmpty() : "PreCondition error: servicePath is null or empty";

        Class<?> serviceClass = null;
        if (registeredServices.containsKey(servicePath)) {
            serviceClass = registeredServices.get(servicePath);
        } else {
           LOG.error("ServiceLocator: getServiceClass: There is no registered annotated service class for the given service path: " + servicePath);
            throw new ClassNotFoundException("There is no registered annotated service class for the given service path: " + servicePath);
        }
        assert serviceClass != null : "PostCondition error: serviceClass is null";
        return serviceClass;
    }

    /**
     * Gets a Registered Annotated Rest Service Class based on the given <code>pathInfo</code>.
     *
     * @param pathInfo
     *            the path of the Rest Service Class found in URL and used to annotate the class. E.g.:
     *            /relGraph
     * @return the loaded Rest Service Class
     * @throws ClassNotFoundException
     *             in case the Class cannot be found
     */
    public synchronized Class<?> getRestServiceClass(String pathInfo) throws ClassNotFoundException {
        assert (pathInfo != null) && !pathInfo.isEmpty() : "PreCondition error: pathInfo is null or empty";

        Class<?> restServiceClass = null;
        if (registeredRestServices.containsKey(pathInfo)) {
            restServiceClass = registeredRestServices.get(pathInfo);
        } else {
           LOG.error("ServiceLocator: getServiceClass: There is no registered annotated service class for the given service path: " + pathInfo);
            throw new ClassNotFoundException("There is no registered annotated service class for the given service path: " + pathInfo);
        }
        assert restServiceClass != null : "PostCondition error: serviceClass is null";
        return restServiceClass;
    }

    public synchronized Set<Class<?>> getRestServiceClassNames() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.addAll(registeredRestServices.values());
        return classes;
    }

    /**
     * Sets up the Service Classes URLs and call for each URL the {@link (<code>loadServiceClassesFromUrl</code>)}
     * method. It searches and loads all the annotated Service Classes and makes them available by the
     * <code>getServiceClass</code> method.
     */
    private void loadServiceClasses() {
        try {
            String path = (new File(".")).getCanonicalPath() + "/webapps/Centrifuge/WEB-INF/lib/centrifuge-services.jar";
            LOG.debug("ServiceLocator: loadServiceClasses: Scanning the following URL: " + path);

            File file = new File(path);
            URL url = file.toURI().toURL();

            loadServiceClassesFromUrl(url);
            LOG.debug("ServiceLocator: loadServiceClasses: Finished scanning for annotated service classes");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Load from the given URL the annotated Service Classes and store them in the <code>registeredServices</code> map,
     * based on the annotation <code>path</code> parameter.
     *
     * @param url
     *            the URL to scan for Service Classes
     * @throws IOException
     *             in case the given URL cannot be scanned
     */
    private void loadServiceClassesFromUrl(URL url) throws IOException {
        AnnotationDB db = new AnnotationDB();
        LOG.debug("ServiceLocator: loadServiceClassesFromUrl: Start scanning ...");
        db.scanArchives(url);
        LOG.debug("ServiceLocator: loadServiceClassesFromUrl: Finished scanning");

        registerServices(db, Service.class, registeredServices, "integrated services");

        registerServices(db, Path.class, registeredRestServices, "restful services");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerServices(AnnotationDB db, Class serviceClass, Map<String, Class<?>> map, String serviceCategory) {
        Set<String> annotatedClassesNames = db.getAnnotationIndex().get(serviceClass.getName());

        if (annotatedClassesNames == null) {
           LOG.info("ServiceLocator: loadServiceClassesFromUrl: found no " + serviceCategory + ".");
            return;
        } else {

           LOG.info("ServiceLocator: loadServiceClassesFromUrl: Found " + Integer.toString(annotatedClassesNames.size()) + " " + serviceCategory + ".");
        }

        LOG.debug("ServiceLocator: loadServiceClassesFromUrl: Found " + (annotatedClassesNames != null ? annotatedClassesNames.size() : "null") + " annotated service classes");
        Class<?> annotatedClass = null;

        for (String annotatedClassName : annotatedClassesNames) {
           LOG.debug("ServiceLocator: loadServiceClassesFromUrl: Annotated class: " + annotatedClassName);
            try {
                annotatedClass = Class.forName(annotatedClassName);
                String annotationPath = "";
                if (serviceClass.getName().endsWith("Service")) {
                    Service annotation = annotatedClass.getAnnotation(Service.class);
                    annotationPath = annotation.path();
                } else if (serviceClass.getName().endsWith("Path")) {
                    Path annotation = (Path)annotatedClass.getAnnotation(serviceClass);
                    annotationPath = annotation.value().toLowerCase();  //This allows the map.get() method to be case-insensitive .
                }
                if ((annotationPath != null) && !annotationPath.isEmpty()) {
                    if (!map.containsKey(annotationPath)) {
                        map.put(annotationPath, annotatedClass);
                    } else {
                       LOG.warn("ServiceLocator: loadServiceClassesFromUrl: Failed to register annotated class: " + annotatedClassName
                                + " because there is already a class registered with the same path: " + annotationPath);
                    }
                }
            } catch (ClassNotFoundException e) {
               LOG.error("ServiceLocator: loadServiceClassesFromUrl: Cannot instantiate class: " + annotatedClassName + ". Reason: " + e.getMessage());
            }
        }
    }
}
