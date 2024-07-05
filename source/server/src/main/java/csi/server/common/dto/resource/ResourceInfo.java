package csi.server.common.dto.resource;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.dto.PermissionInfo;


public class ResourceInfo implements IsSerializable {

    public String uuid;
    public List<PermissionInfo> permissions;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<PermissionInfo> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionInfo> permissions) {
        this.permissions = permissions;
    }

}
