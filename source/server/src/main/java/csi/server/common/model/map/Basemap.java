package csi.server.common.model.map;

import csi.security.monitors.ResourceACLMonitor;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.Resource;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import java.io.Serializable;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(ResourceACLMonitor.class)
public class Basemap extends Resource implements Serializable {
    private String url;
    private String type;
    private String layername;

    public Basemap() {
        super();
        setResourceType(AclResourceType.MAP_BASEMAP);
    }

    public Basemap(String uuidIn, String nameIn, String remarksIn, String ownerIn,
                   String urlIn, String typeIn, String layerNameIn) {

        this();
        setUuid(uuidIn);
        setName(nameIn);
        setRemarks(remarksIn);
        setOwner(ownerIn);
        setUrl(urlIn);
        setType(typeIn);
        setLayername(layerNameIn);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLayername() {
        return layername;
    }

    public void setLayername(String layername) {
        this.layername = layername;
    }

}
