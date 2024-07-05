package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class ValidationDO implements IsSerializable {

    public boolean structurallyValid;
    public boolean dvConsistent;
}
