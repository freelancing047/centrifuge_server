package csi.server.ws.support;

import java.util.Map;

import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;

public class Utility
{
    public static Object getPropertyValue(NodeStore details, String name) {
        Map<String, Property> attrs = details.getAttributes();
        Object value = null;

        if (attrs.containsKey(name)) {
            Property prop = attrs.get(name);
            if (prop.hasValues()) {
                value = prop.getValues().get(0);
            }
        }

        return value;
    }
    
    public static void addProperty(NodeStore details, String name, Object value) {
        if (value == null) {
            return;
        }
        Map<String, Property> attributes = details.getAttributes();
        Property prop = new Property(name);
        prop.getValues().add(value);
        attributes.put(name, prop);
    }

}
