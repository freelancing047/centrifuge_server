package csi.security.jaas.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public enum CallBackId implements Serializable {

    INITIALIZE("0", "Initialize Callback", 0),
    AUTHORIZE("1", "Authorize Callback", 1),
    CERTIFICATE("2", "Certificate Callback", 2),
    LDAP_PASSWORD("3", "Raw Password Callback", 3),
    USERNAME("4", "Username Callback", 4),
    ENCR_PASSWORD("5", "Encrypted Password Callback", 5),
    RESPONSE("6", "Response Callback", 6),
    UNSUPPORTED("7", "Unsupported Callback", 7);

    private String _key;
    private String _label;
    private int _ordinal;
    private static List<CallBackId> sortedDataTypes = new ArrayList<CallBackId>();

    private CallBackId(String keyIn, String labelIn, int ordinalIn) {
        _key = keyIn;
        _label = labelIn;
        _ordinal = ordinalIn;
    }

    public String getKey() {
        return _key;
    }

    public String getLabel() {
        return _label;
    }
    
    public int getOrdinal() {
        return _ordinal;
    }

    private static Map<String, CallBackId> codeToEnumMapping = new HashMap<String, CallBackId>();

    static {
        for (CallBackId e : values()) {
            codeToEnumMapping.put(e._key, e);
            sortedDataTypes.add(e);
        }
        Collections.sort(sortedDataTypes, new Comparator<CallBackId>() {

            @Override
            public int compare(CallBackId o1, CallBackId o2) {
                return o1.getLabel().compareTo(o2.getLabel());
    }
        });
    }

    public static CallBackId getValue(String s) {
        if (s == null) {
            return null;
        }

        CallBackId type = codeToEnumMapping.get(s.toLowerCase());

        if (type == null) {
            type = CallBackId.UNSUPPORTED;
        }

        return type;
    }

    public static String toString(CallBackId type) {
        if (type == null) {
            return "";
        }
        return type.getLabel();
    }

    public static List<CallBackId> sortedValuesByLabel() {
        return sortedDataTypes;
    }
}
