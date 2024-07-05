package csi.server.common.dto.resource;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.security.AccessControlEntry;


public class ResourceRequest implements IsSerializable {

    public Long id;

    public String uuid;

    public String owner;

    public List<AccessControlEntry> permissions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<AccessControlEntry> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<AccessControlEntry> permissions) {
        this.permissions = permissions;
    }

}
