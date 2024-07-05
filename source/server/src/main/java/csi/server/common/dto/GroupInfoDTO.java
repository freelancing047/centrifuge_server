package csi.server.common.dto;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class GroupInfoDTO implements IsSerializable {

    public Long id;

    public String name;

    public List<RoleInfoDTO> membership;

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

    public List<RoleInfoDTO> getMembership() {
        return membership;
    }

    public void setMembership(List<RoleInfoDTO> membership) {
        this.membership = membership;
    }

}
