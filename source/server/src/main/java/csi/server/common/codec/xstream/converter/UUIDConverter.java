package csi.server.common.codec.xstream.converter;

import java.util.UUID;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts Java UUIDs into an XML string. This forces XStream to skip
 * reflection and generate the standard looking UUID of hex encoded bytes.
 * <p>
 * 
 * @author Tildenwoods
 * 
 */
public class UUIDConverter implements Converter {

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
        return type.equals(UUID.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        UUID uuid = (UUID) source;
        writer.setValue(uuid.toString());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String value = reader.getValue();
        UUID uuid = UUID.fromString(value);
        return uuid;
    }

}
