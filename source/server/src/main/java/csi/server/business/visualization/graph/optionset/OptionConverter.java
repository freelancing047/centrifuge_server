package csi.server.business.visualization.graph.optionset;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class OptionConverter implements Converter {

    @Override
    public boolean canConvert(Class type) {
        return Options.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        Options opt = (Options) obj;
        writer.addAttribute("key", opt.key);

        if (opt.properties != null) {
            Set<String> keySet = opt.properties.keySet();
            for (String key : keySet) {
                String val = opt.properties.get(key);
                if (val != null) {
                    writer.addAttribute(key, val);
                }
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Options opt = new Options();
        opt.optionType = reader.getNodeName();

        opt.properties = new HashMap<String, String>();
        Iterator attrs = reader.getAttributeNames();
        while (attrs.hasNext()) {
            String attrName = (String) attrs.next();
            String val = reader.getAttribute(attrName);
            if (attrName.equalsIgnoreCase("nodeType") || attrName.equalsIgnoreCase("linkType") || attrName.equalsIgnoreCase("key")) {
                opt.key = val;
            } else {
                opt.properties.put(attrName, reader.getAttribute(attrName));
            }
        }
        return opt;
    }

}
