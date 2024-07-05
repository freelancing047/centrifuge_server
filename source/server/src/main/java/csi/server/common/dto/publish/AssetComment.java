package csi.server.common.dto.publish;


import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;


public class AssetComment implements IsSerializable {

    public String creator;
    public String value;
    public Date timeStamp;

    public AssetComment() {
        // default c'tor
    }

    public AssetComment(String creator, String value, Date timeStamp) {
        this.creator = creator;
        this.value = value;
        this.timeStamp = timeStamp;
    }
}
