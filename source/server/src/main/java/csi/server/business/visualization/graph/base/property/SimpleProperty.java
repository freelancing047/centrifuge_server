package csi.server.business.visualization.graph.base.property;

import java.util.Collections;
import java.util.List;

public class SimpleProperty extends Property {

    protected Object value;

    public SimpleProperty(String name) {
        super(name);
    }

    @Override
    public boolean hasValues() {
        return (value != null);
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    public Object getValue() {
    	return value;
    }

    @Override
    public List<Object> getValues() {
    	if (value != null) {
    		return Collections.singletonList(value);
    	} else {
    		return Collections.emptyList();
    	}
    }
}
