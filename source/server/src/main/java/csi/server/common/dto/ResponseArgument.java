package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 12/27/2017.
 */
public class ResponseArgument implements IsSerializable {

    String _message = null;

    public ResponseArgument() {

    }

    public ResponseArgument(String messageIn) {

        _message = messageIn;
    }

    public String getMessage() {

        return (null != _message) ? _message : "";
    }
}
