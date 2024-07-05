package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class FileValidation implements IsSerializable {

    public boolean nameInUse;
    public boolean invalidChars;
}
