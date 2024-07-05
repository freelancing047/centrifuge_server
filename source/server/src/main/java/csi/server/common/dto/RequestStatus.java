package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class RequestStatus implements IsSerializable {

    public RequestStatusType status;
    public String message;
}
