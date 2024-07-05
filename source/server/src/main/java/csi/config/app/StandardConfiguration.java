/**
 *
 */
package csi.config.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import csi.config.app.modules.AuthorizationModule;
import csi.config.app.modules.RequiredModule;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class StandardConfiguration extends AbstractModule {
   /*
    * (non-Javadoc)
    *
    * @see com.google.inject.AbstractModule#configure()
    */
   @Override
   protected void configure() {
      Collection<Module> modules = getModules();

      for (Module module : modules) {
         install(module);
      }
   }

   private Collection<Module> getModules() {
      return new ArrayList<Module>(Arrays.asList(new AuthorizationModule(), new RequiredModule()));
   }
}
