package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class FileManagerMessageData implements IsSerializable {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
