package csi.server.common.dto.publish;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class AssetTagList implements IsSerializable {

    public String assetID;

    public List<AssetTag> tags;
}
