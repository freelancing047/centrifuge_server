package csi.config.app.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import csi.security.CsiSecurityManager;
import csi.security.spi.AuthorizationService;
import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.helper.DataCacheHelper;

/*
 * Ensures that mandatory bindings for injection are satisfied.  This class also
 * ensures that all classes that require special handling are properly bootstrapped via
 * an injection request.
 * <p>
 * This is intended to provide a sanity check to ensure that things are properly configured
 * prior to allowing the application to start.  Any unsatisfied dependencies are captured
 * and errors generated via the Guice framework.
 * <p>
 */
public class RequiredModule extends AbstractModule {
   @Override
   protected void configure() {
      // NB: this is always needed to ensure that we at least have an empty
      // set that is injected!
      /*Multibinder<DataSyncListener> binder =*/Multibinder.newSetBinder(binder(), DataSyncListener.class);

      // establish required bindings. Note that we do not explicitly add
      // DataSyncListener
      // as required. The set binder above ensures we have an empty set, but
      // that fails
      // the require check.
      requireBinding(AuthorizationService.class);

      requestStaticInjection(DataCacheHelper.class);
      requestStaticInjection(CsiSecurityManager.class);
   }
}
