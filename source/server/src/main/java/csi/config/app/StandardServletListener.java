/**
 * 
 */
package csi.config.app;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * @author Centrifuge Systems, Inc.
 * 
 */
public class StandardServletListener
    extends GuiceServletContextListener
{
    static final String MODULES = "modules";
    static final String INVALID_MODULE_CONFIGURATION = "Invalid module name provided in the modules init-param value.";
    
    

    private ServletContext context;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        this.context = servletContextEvent.getServletContext();
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        this.context = null;
    }

    @Override
    protected Injector getInjector() {
        
        Injector injector = Guice.createInjector(new StandardConfiguration() );
        return injector;

    }

}
