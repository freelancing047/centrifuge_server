package csi.server.common.dto.publish;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Client request for the list of assets that is constrained by the list of 
 * tag values, if any.
 */

public class AssetRequest implements IsSerializable {

    public List<String> tags;
}
