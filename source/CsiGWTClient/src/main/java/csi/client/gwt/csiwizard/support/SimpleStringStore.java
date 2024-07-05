package csi.client.gwt.csiwizard.support;

public class SimpleStringStore {

    public String value = null;

    public SimpleStringStore() {
    }
    
    public SimpleStringStore(String valueIn) {
        value = valueIn;
    }

    public void setActive(Boolean activeIn) {
     }

    public Boolean getActive() {
        return true;
    }
    
    public void setKey(String valueIn) {
        value = valueIn;
   }

    public String getKey() {
        return value;
    }
    
    public void setLabel(String valueIn) {
        value = valueIn;
   }

    public String getLabel() {
        return value;
    }
    
    public void setValue(String valueIn) {
        value = valueIn;
   }

    public String getValue() {
        return value;
    }
}
