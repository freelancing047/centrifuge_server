package csi.server.common.codec.xstream.converter;

import org.bson.types.ObjectId;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MongoObjectIdConverter
    implements Converter
{
    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
        return type.equals(ObjectId.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ObjectId oid = (ObjectId) source;
        writer.setValue(oid.toString());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String value = reader.getValue();
        ObjectId oid = new ObjectId(value);
        return oid;
    }

}
