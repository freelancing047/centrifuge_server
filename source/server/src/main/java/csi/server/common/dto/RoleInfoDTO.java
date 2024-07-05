package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class RoleInfoDTO implements IsSerializable {

    public Long id;
    public String name;
    public boolean isGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

}
