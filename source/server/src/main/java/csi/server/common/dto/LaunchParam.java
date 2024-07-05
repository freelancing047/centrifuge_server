package csi.server.common.dto;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class LaunchParam implements IsSerializable {

    private String name;
    private List<String> values;

    public LaunchParam() {
        
    }

    public LaunchParam(String nameIn, List<String> listIn) {
        
        name = nameIn;
        values = listIn;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

}
