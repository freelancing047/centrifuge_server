package csi.security.spi;


/**
 * An authorization service is the fundamental entry point for determining
 * whether a request for a particular resource is permitted.  
 * <p>
 * This is a marker interface to isolate the service from supporting
 * authorization modules.
 * <p>
 * 
 * @author Centrifuge Systems Inc.
 */
public interface AuthorizationService 
    extends AuthorizationGuard
{
    
}
