package csi.shared.core.visualization.timeline;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Tooltip implements IsSerializable {
    
    
    private HashMap<String, String> fieldValuePairs = new HashMap<String, String>();

    public HashMap<String, String> getFieldValuePairs() {
        return fieldValuePairs;
    }

    public void setFieldValuePairs(HashMap<String, String> fieldValuePairs) {
        this.fieldValuePairs = fieldValuePairs;
    }
    
    public void addField(String key, String value){
        fieldValuePairs.put(key, value);
    }

}
