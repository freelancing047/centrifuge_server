package csi.server.common.codec.xstream.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.thoughtworks.xstream.converters.SingleValueConverter;

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
public class CsiUUIDSingleValueConverter implements SingleValueConverter {

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

    public CsiUUIDSingleValueConverter() {
        type = CloneType.EXACT;
    }

    public CsiUUIDSingleValueConverter(CloneType type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
        return type.equals(CsiUUID.class);
    }

    @Override
    public Object fromString(String sval) {
        if ((sval == null) || sval.trim().isEmpty()) {
            return new CsiUUID();
        }

        return new CsiUUID(sval);
    }

    @Override
    public String toString(Object sourceIn) {

        String myNewUuid = null;

        if (null != sourceIn) {

            CsiUUID mySourceUuid = (CsiUUID)sourceIn;
            String myOldUuid = mySourceUuid.getUuid();

            if (CloneType.NEW_ID == type) {

                myNewUuid = getNewUuidMap().get(myOldUuid);
                if (null == myNewUuid) {

                    myNewUuid = UUID.randomUUID().toString().toLowerCase();
                    getNewUuidMap().put(myOldUuid, myNewUuid);
                }
            } else {

                myNewUuid = myOldUuid;
            }

        } else {

            myNewUuid = UUID.randomUUID().toString().toLowerCase();
        }
        return myNewUuid;
    }
/*
    @Override
    public String toString(Object source) {
        CsiUUID id = (CsiUUID) source;
        String sid = null;
        if (type == CloneType.NEW_ID) {
            String oldUuid = id.getUuid();
            sid = getNewUuidMap().get(oldUuid);
            if (sid == null) {
                sid = UUID.randomUUID().toString().toLowerCase();
                getNewUuidMap().put(oldUuid, sid);
            }
        } else {
            sid = id.getUuid();
        }

        return sid;
    }
*/
}
