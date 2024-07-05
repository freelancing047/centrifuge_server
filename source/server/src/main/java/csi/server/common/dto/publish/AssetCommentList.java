package csi.server.common.dto.publish;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class AssetCommentList implements IsSerializable {

    public String assetID;

    public List<AssetComment> comments;
}
