package csi.server.common.publishing.live;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.publishing.Asset;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LiveAsset extends Asset {

    protected String modelObjectUUID;

    public String getModelObjectUUID() {
        return modelObjectUUID;
    }

    public void setModelObjectUUID(String modelObjectUUID) {
        this.modelObjectUUID = modelObjectUUID;
    }

}
