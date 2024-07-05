/**
 * 
 */
package csi.config.app;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import csi.config.app.modules.RequiredModule;

/**
 * @author Centrifuge Systems, Inc.
 * 
 */
public class DynamicConfiguration
    extends AbstractModule
{
    private String[] names;

    public DynamicConfiguration(String[] names) {
        this.names = names;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        for (String name : names) {
            try {
                install(createModule(name));
            } catch (Throwable t) {
                addError(t);
            }
        }

        // always install this module.  ensures that
        // we bootstrap with minimal requirements.
        install(new RequiredModule());
    }

    private Module createModule(String name)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ClassLoader loader = this.getClass().getClassLoader();
        Class<Module> type = Module.class;

        name = name.trim();

        Module module = type.cast(loader.loadClass(name).newInstance());
        return module;
    }

}
