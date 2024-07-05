/*
 * @(#) BusinessContext.java,  Jul 8, 2009
 *
 */
package csi.server.business.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.exception.CentrifugeException;

/**
 * <p>
 * Keeps all business components references in one place, prevents instances duplication, prevents the existence of
 * components beyond their necessity, provides lazy loading / eager loading policy according to components usage.
 * 
 * <p>
 * The business service will not be instantiated directly by the clients, instead this object will be used to obtain the
 * required service
 * 
 * <p>
 * This object is the only responsible with business objects creation
 * 
 * @author <a href="mailto:iulian.boanca@lpro.leverpointinc.com">Iulian Boanca</a>
 * 
 */
public class BusinessServiceManager {
   private static final Logger LOG = LogManager.getLogger(BusinessServiceManager.class);
   
   private Map<Class<?>, Object> businessComponents = new HashMap<Class<?>, Object>();
    private static BusinessServiceManager businessServiceLocator;

    public static synchronized BusinessServiceManager getInstance() {
        if (businessServiceLocator == null) {
            businessServiceLocator = new BusinessServiceManager();
        }
        return businessServiceLocator;
    }

    private BusinessServiceManager() {
        List<Class<?>> defaultComponents = new ArrayList<Class<?>>();
        try {
            loadComponents(defaultComponents);
        } catch (CentrifugeException e) {
           LOG.error("BusinessServiceManager#constructor: " + e.getMessage());
        }
    }

    private void loadComponents(List<Class<?>> componentIdentifiers) throws CentrifugeException {
        for (Class<?> componentIdentifier : componentIdentifiers) {
            Object component = buildComponent(componentIdentifier);
            businessComponents.put(componentIdentifier, component);
        }
    }

    /**
     * Provides a business component based on the input <code>componentClass</code>
     * 
     * @param componentClass the class of the required component
     * @return the business component
     */
    public synchronized Object getComponent(Class<?> componentClass) {
        assert componentClass != null : "Component class is null";
        Object component = businessComponents.get(componentClass);
        ;
        if (component == null) {
            component = buildComponent(componentClass);
            businessComponents.put(componentClass, component);
        }
        assert component != null : " Component returned is null";
        return component;
    }

    private Object buildComponent(Class<?> componentClass) {
        Object component = null;
        try {
            component = componentClass.newInstance();
        } catch (InstantiationException e) {
            System.out.println("BusinessServiceManager: buildComponent: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println("BusinessServiceManager: buildComponent: " + e.getMessage());
        }
        return component;
    }

}
