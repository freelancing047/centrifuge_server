package csi.security.spi;

import java.security.BasicPermission;

import csi.security.Authorization;


public interface AuthorizationContext {
    
    Authorization getAuthorization();
    String getResourceId();
    BasicPermission getRequestedPermission();

}
