package csi.security;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;


public class ResourceACLMonitor
{
    
    @PostPersist
    public void resourceAdded( Object resource )
    {
 
    }
    
    @PostRemove void resourceRemoved( Object resource )
    {
           
    }

}
