package csi.security;

import java.security.BasicPermission;


public class ResourcePermission extends BasicPermission {
    
    protected String actions;

    public ResourcePermission(String name, String actions) {
        super(name, actions);
        this.actions = actions;
    }

    @Override
    public String getActions() {
        return actions;
    }
    
    
    

}
