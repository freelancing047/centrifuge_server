package csi.server.common.codec.xstream.converter;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import csi.server.common.model.CsiUUID;

public class ResetCsiUUIDConverter implements Converter {

    private static ThreadLocal<Set<CsiUUID>> uuidList = new ThreadLocal<Set<CsiUUID>>();

    public static Set<CsiUUID> getUniqueIds() {
        Set<CsiUUID> set = uuidList.get();
        if (set == null) {
            set = new HashSet<CsiUUID>();
            uuidList.set(set);
        }
        return set;
    }

    public static void reset() {
        getUniqueIds().clear();
    }

    public ResetCsiUUIDConverter() {
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
        return CsiUUID.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        CsiUUID csiUuid = (CsiUUID) source;
        getUniqueIds().add(csiUuid);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }
}
