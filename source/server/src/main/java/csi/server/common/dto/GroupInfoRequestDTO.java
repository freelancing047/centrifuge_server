package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class GroupInfoRequestDTO implements IsSerializable {

    public Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
