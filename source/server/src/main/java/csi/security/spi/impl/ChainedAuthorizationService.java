package csi.security.spi.impl;

import java.util.Iterator;
import java.util.Set;

import com.google.inject.Inject;

import csi.security.spi.AuthorizationContext;
import csi.security.spi.AuthorizationGuard;
import csi.security.spi.AuthorizationService;


/**
 * 
 * The ChainedAuthorizationService enables checking verifying that a set of AuthorizationGuards
 * all allow the requested operation.  The requested is denied if any one of the registered
 * guards do not allow the user to perform the operation on the target resource.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChainedAuthorizationService 
    implements AuthorizationService
{
    
    @Inject
    Set<AuthorizationGuard> guards;

    @Override
    public boolean isAuthorized(AuthorizationContext context) {
        
        Iterator<AuthorizationGuard> iterator = guards.iterator();
        
        boolean isPermitted = true;
        while( iterator.hasNext() && isPermitted ) {
            isPermitted = iterator.next().isAuthorized(context);
        }
        
        return isPermitted;
    }

}
