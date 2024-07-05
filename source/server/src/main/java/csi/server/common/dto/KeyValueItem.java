package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class KeyValueItem implements IsSerializable {

    public KeyValueItem(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public KeyValueItem() {
        super();
    }

    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
