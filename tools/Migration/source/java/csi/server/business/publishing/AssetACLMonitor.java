package csi.server.business.publishing;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;

import csi.server.common.publishing.Asset;

public class AssetACLMonitor
{
    @PrePersist
    public void assetAdded( Asset asset )
    {
    }
    
    @PreRemove
    public void assetRemoved( Asset asset )
    {
    }

}