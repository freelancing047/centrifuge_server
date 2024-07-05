package csi.server.common.dto.publish;


import com.google.gwt.user.client.rpc.IsSerializable;


public class AssetTag implements IsSerializable {

    public Long id;
    public String creator;
    public String value;

    public AssetTag() {
        // default c'tor
    }

    public AssetTag(Long id, String creator, String value) {
        this.id = id;
        this.creator = creator;
        this.value = value;
    }
}
