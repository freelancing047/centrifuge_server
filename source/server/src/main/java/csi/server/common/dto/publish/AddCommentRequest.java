package csi.server.common.dto.publish;


import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Request to add one or more tags to an asset. 
 */

public class AddCommentRequest implements IsSerializable {

    public String assetID;

    public String text;
}
