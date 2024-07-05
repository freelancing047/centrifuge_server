package csi.server.business.visualization.deprecated.timeline;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import csi.server.business.visualization.graph.base.property.Property;

public class EventConverter extends JavaBeanConverter {

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class type) {
        return TemporalEvent.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        TemporalEvent event = (TemporalEvent) source;

        writer.startNode("id");
        writer.setValue(event.getId());
        writer.endNode();

        Map<String, Property> properties = event.getProperties();
        Property property = properties.get("label");
        if (property != null) {
            String label = (String) property.getValues().get(0);
            if (label == null) {
                label = event.getId();
            } else {
                label = event.getId();
            }
            writer.startNode("label");
            writer.setValue(label);
            writer.endNode();
        }

        writer.startNode("start");
        writer.setValue(Long.toString(event.getStart()));
        writer.endNode();
        writer.startNode("end");
        writer.setValue(Long.toString(event.getEnd()));
        writer.endNode();

        writer.startNode("properties");
        Iterator<Entry<String, Property>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Property> next = iterator.next();
            String propName = next.getKey();
            Property prop = next.getValue();
            Iterator<Object> values = prop.getValues().iterator();
            while (values.hasNext()) {
                writer.startNode("property");
                writer.addAttribute("name", propName);
                context.convertAnother(values.next());
                writer.endNode();
            }
        }

        writer.endNode();
    }

    @SuppressWarnings("deprecation")
    public EventConverter(Mapper mapper, String classAttributeIdentifier) {
        super(mapper, classAttributeIdentifier);
    }

}
