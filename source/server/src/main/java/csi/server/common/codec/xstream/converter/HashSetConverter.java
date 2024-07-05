package csi.server.common.codec.xstream.converter;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class HashSetConverter extends CollectionConverter {

    public HashSetConverter(Mapper mapper) {
        super(mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class type) {
        return Set.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Set set = new HashSet();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            Object readItem = readItem(reader, context, set);
            set.add(readItem);
            reader.moveUp();
        }

        return set;
    }
}
