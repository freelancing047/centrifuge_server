/**
 * 
 */
package csi.config.app.modules;


import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import csi.security.spi.AuthorizationGuard;
import csi.security.spi.AuthorizationService;
import csi.security.spi.impl.ChainedAuthorizationService;
import csi.security.spi.impl.DefaultAuthorizationService;

/**
 * The AuthorizationModule provides configures the standard mechanisms for
 * authorization checks.
 * <p>
 * The standard mechanisms are currently limited to ACL checks.
 * <p>
 * 
 * @author Centrifuge Systems, Inc.
 * 
 */
public class AuthorizationModule
    extends AbstractModule
{

    /*
     * (non-Javadoc)
     * 
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {

        // bind in our ACL check
        Multibinder<AuthorizationGuard> guards = Multibinder.newSetBinder(binder(), AuthorizationGuard.class);
        guards.addBinding().to(DefaultAuthorizationService.class);
        
        // bind our standard ACL checks in for authorization checks
        bind(AuthorizationService.class).to(ChainedAuthorizationService.class);
        
        

    }

}
