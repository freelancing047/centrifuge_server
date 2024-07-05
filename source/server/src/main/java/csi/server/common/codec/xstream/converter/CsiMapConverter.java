package csi.server.common.codec.xstream.converter;

import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
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
public class CsiMapConverter extends com.thoughtworks.xstream.converters.collections.MapConverter {

    public CsiMapConverter(Mapper mapper) {
        super(mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class type) {
        return CsiMap.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map map = (Map) source;
        /*  53*/for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); writer.endNode()) {
            /*  54*/java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
            /*  55*/ExtendedHierarchicalStreamWriterHelper.startNode(writer, mapper().serializedClass(java.util.Map.Entry.class), java.util.Map.Entry.class);
            /*  57*/writeItem(entry.getKey(), context, writer);
            /*  58*/writeItem(entry.getValue(), context, writer);
        }

    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        /*  65*/Map map = (Map) createCollection(context.getRequiredType());
        /*  66*/populateMap(reader, context, map);
        /*  67*/return map;
    }

    public void populateMap(HierarchicalStreamReader reader, UnmarshallingContext context, Map map) {
        /*  71*/for (; reader.hasMoreChildren(); reader.moveUp()) {
            /*  72*/reader.moveDown();
            // Check if tag has more children: <entry><key>key</key><value>value</value></entry> format
            if (reader.hasMoreChildren()) {
                /*  74*/reader.moveDown();
                /*  75*/Object key = readItem(reader, context, map);
                /*  76*/reader.moveUp();
                /*  78*/reader.moveDown();
                /*  79*/Object value = readItem(reader, context, map);
                /*  80*/reader.moveUp();
                /*  82*/map.put(key, value);
            }
        }

    }

}
