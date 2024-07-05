package csi.server.common.dto.publish;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Request to add one or more tags to an asset. 
 */

public class AddTagsRequest implements IsSerializable {

    public String assetID;

    public List<String> values;
}
