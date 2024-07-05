package csi.server.ws.actions;


import com.google.gwt.user.client.rpc.IsSerializable;


public class PagingInfo implements IsSerializable {

    public long totalRecords;
    public boolean complete;

}