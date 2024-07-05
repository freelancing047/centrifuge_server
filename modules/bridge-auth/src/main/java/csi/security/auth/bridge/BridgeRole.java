package csi.security.auth.bridge;

import java.security.Principal;

public class BridgeRole
    implements Principal
{
    protected String name;
    
    public BridgeRole( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

}
