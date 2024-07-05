package csi.security.auth.bridge;

import java.security.Principal;

public class BridgePrincipal
    implements Principal
{
    
    protected String name;
    
    public BridgePrincipal(String name)
    {
        this.name = name;
        
    }

    @Override
    public String getName()
    {
        return name;
    }

}
