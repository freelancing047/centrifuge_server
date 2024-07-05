package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class ExtensionInfo implements IsSerializable {

    public String name;
    public String category;
    public String description;
    public boolean mandatory;
}
