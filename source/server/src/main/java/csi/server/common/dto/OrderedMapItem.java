package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class OrderedMapItem implements IsSerializable {

    public int ordinal;
    public String key;
    public String value;
}
