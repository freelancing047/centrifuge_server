package example;


import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import csi.security.spi.AuthorizationGuard;
import csi.server.business.cachedb.DataSyncListener;
import example.AuditListener;


/**
 * The AuditConfig class registers the AuditListener with Centrifuge.
 * <p>
 * This class utilizes the Guice as the injection framework for registering
 * implementations of the extension modules.  
 * <p>
 * Both listener types--DataSyncListener and AuthorizationGuard--support
 * multiple registered instances.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class AuditConfig
    extends AbstractModule
{

    @Override
    protected void configure() {
        
        // Multiple DataSyncListeners can be registered with Centrifuge.  The callback methods
        // are invoked during data cache initialization and merges of Dataviews.
        Multibinder<DataSyncListener> binder = Multibinder.newSetBinder(binder(), DataSyncListener.class);
        binder.addBinding().to(AuditListener.class);
        
        // Multiple AuthorizationGuards can be registered with Centrifuge.  Each guard
        // is called to determine whether access is to the resource is allowed.
        Multibinder<AuthorizationGuard> guards = Multibinder.newSetBinder(binder(), AuthorizationGuard.class);
        guards.addBinding().to(AuditingSecurityGuard.class);
        guards.addBinding().to(AdminOrWorkingHoursGuard.class);
        
    }

}
