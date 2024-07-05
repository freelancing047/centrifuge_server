package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class PermissionInfo implements IsSerializable {

    public String name;

    public String role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
