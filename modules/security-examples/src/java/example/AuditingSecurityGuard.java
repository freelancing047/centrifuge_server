package example;

import java.security.BasicPermission;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.security.spi.AuthorizationContext;
import csi.security.spi.AuthorizationGuard;

/**
 * This class is an example implementation of an authorization module.
 * <p>
 * Authorization modules are consulted to determine whether the current
 * request for a resource is permitted.  The <i>AuthorizationContext</i>
 * provides the relevant contextual.
 * <p>
 * This implementation always grants permission while logging the
 * access attempt if configured via the log4j.xml configuration
 * file.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class AuditingSecurityGuard
    implements AuthorizationGuard
{
    Logger log = Logger.getLogger(AuditingSecurityGuard.class);

    @Override
    public boolean isAuthorized(AuthorizationContext context) {
        Authorization authZ = context.getAuthorization();
        String user = authZ.getName();
        
        BasicPermission permission = context.getRequestedPermission();
        String operation = permission.getActions();
        
        // the resourceId represents a unique identifier for the requested resource.  
        String resourceUuid = context.getResourceId();
        
        String template = " User %1$s attempted to perform %2$s on resource %3$s";
        String msg = String.format(template, user, operation, resourceUuid);
        log.info( msg );
        
        return true;
    }
    
    
    

}
