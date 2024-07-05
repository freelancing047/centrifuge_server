package csi.server.common.codec.xstream.converter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import csi.server.common.dto.CsiMap;

/**
 * Provides a converter for generic Maps.  By default XStream only
 * converts specific concrete types.  This causes issues for JPA where
 * we deal with vendor specific collection classes that enable lazy loading
 * of data.  We'll handle the generic java.util.Map interface here to avoid
 * XStream dropping into reflection based serialization!
 * <p>
 */
public class MapConverter extends com.thoughtworks.xstream.converters.collections.MapConverter {

    public MapConverter(Mapper mapper) {
        super(mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class type) {
        return Map.class.isAssignableFrom(type) && !CsiMap.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map map = (Map) source;
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            writer.startNode("entry");
            writer.addAttribute("key", key.toString());
            writeItem(entry.getValue(), context, writer);

            writer.endNode();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map map = new HashMap();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String key = reader.getAttribute("key");
            if (key != null) {
                reader.moveDown();
                Object value = readItem(reader, context, map);
                reader.moveUp();
                map.put(key, value);
            }
            reader.moveUp();
        }

        return map;

    }

}
