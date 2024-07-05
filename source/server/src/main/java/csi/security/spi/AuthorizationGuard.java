package csi.security.spi;


/**
 * The AuthorizationGuard defines the contract that external modules must implement to
 * participate in authorization decisions.
 * <p>
 * 
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public interface AuthorizationGuard {

    boolean isAuthorized( AuthorizationContext context );
}
