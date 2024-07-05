package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.ConnectionDef;


public class ConnectionTest implements IsSerializable {

    public ConnectionDef connection;
    public String failureCause;
    public String dsLocalId;
}