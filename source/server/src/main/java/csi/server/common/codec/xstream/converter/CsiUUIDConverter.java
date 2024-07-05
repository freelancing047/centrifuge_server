package csi.server.common.codec.xstream.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import csi.server.business.helper.DeepCloner.CloneType;
import csi.server.common.model.CsiUUID;

/**
 * Converts Java UUIDs into an XML string. This forces XStream to skip
 * reflection and generate the standard looking UUID of hex encoded bytes.
 * <p>
 * 
 * @author Tildenwoods
 * 
 */
public class CsiUUIDConverter implements Converter {

    private CloneType type = CloneType.EXACT;

    private static ThreadLocal<HashMap<String, String>> newUuidMap = new ThreadLocal<HashMap<String, String>>();

    public static Map<String, String> getNewUuidMap() {
        if (newUuidMap.get() == null) {
            newUuidMap.set(new HashMap<String, String>());
        }
        return newUuidMap.get();
    }

    public static void clearNewUuidMap() {
        getNewUuidMap().clear();
    }

    public CsiUUIDConverter() {
        type = CloneType.EXACT;
    }

    public CsiUUIDConverter(CloneType type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
        return type.equals(CsiUUID.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (type == CloneType.NEW_ID) {
            String oldUuid = ((CsiUUID) source).getUuid();
            String newUuid = getNewUuidMap().get(oldUuid);
            if (newUuid == null) {
                newUuid = UUID.randomUUID().toString().toLowerCase();
                getNewUuidMap().put(oldUuid, newUuid);
            }
            writer.setValue(newUuid);
        } else {
            CsiUUID id = (CsiUUID) source;
            writer.setValue(id.getUuid());
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (reader.getValue() == null || reader.getValue().isEmpty()) {
            return null;
        }

        return new CsiUUID(reader.getValue());
    }
}
