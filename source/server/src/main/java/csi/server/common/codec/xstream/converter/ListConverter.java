package csi.server.common.codec.xstream.converter;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class ListConverter extends CollectionConverter {

    public ListConverter(Mapper mapper) {
        super(mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class type) {
        return List.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        List list = new ArrayList();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            Object readItem = readItem(reader, context, list);
            list.add(readItem);
            reader.moveUp();
        }

        return list;
    }

}
