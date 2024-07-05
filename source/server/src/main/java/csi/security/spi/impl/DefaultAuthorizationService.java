package csi.security.spi.impl;

import csi.security.CsiSecurityManager;
import csi.security.spi.AuthorizationContext;
import csi.security.spi.AuthorizationService;

/**
 * Encapsulates standard ACL check for a particular resource.
 * <p>
 * This is a simple and direct implemenation of the {@link AuthorizationService}.  All checks
 * are performed on the local Centrifuge Server.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class DefaultAuthorizationService implements AuthorizationService {
    
    private String[] EMPTY = new String[0];

    @Override
    public boolean isAuthorized(AuthorizationContext context) {
        return CsiSecurityManager.isAuthorized(context);
    }

}
