package csi.client.gwt.widget.combo_boxes;

import java.util.HashMap;
import java.util.Map;

import com.sencha.gxt.data.shared.LabelProvider;

public class DualStringLabelProvider implements LabelProvider<String> {

    public Map<String, String> labelMap = new HashMap<String, String>();
    
    public DualStringLabelProvider(){
        
    }
    
    @Override
    public String getLabel(String item) {
        if(labelMap.containsKey(item)){
            return labelMap.get(item);
        } else {
            return item;
        }
    }

    public void addPair(String value, String label) {
        labelMap.put(value, label);
    }

}
