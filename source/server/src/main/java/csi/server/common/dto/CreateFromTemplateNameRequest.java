package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class CreateFromTemplateNameRequest implements IsSerializable {

    public String dvTemplateName;
    public String dvName;
    public CsiMap<String, String> params;
}