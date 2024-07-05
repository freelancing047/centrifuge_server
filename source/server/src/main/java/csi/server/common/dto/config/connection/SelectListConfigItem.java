package csi.server.common.dto.config.connection;



import java.util.LinkedList;

import com.google.gwt.user.client.rpc.IsSerializable;


public class SelectListConfigItem extends ConfigItem implements IsSerializable {
    
    private LinkedList<ListItem> listItemValues;
    private boolean allowMultiSelection = false;

    public SelectListConfigItem(){
        //no arg constructor to enable serialization
    }
    
    public LinkedList<ListItem> getListItemValues() {
        return listItemValues;
    }

    public void setListItemValues(LinkedList<ListItem> listItemValues) {
        this.listItemValues = listItemValues;
    }

    public boolean isAllowMultiSelection() {
        return allowMultiSelection;
    }

    public void setAllowMultiSelection(boolean allowMultiSelection) {
        this.allowMultiSelection = allowMultiSelection;
    }
    
}
