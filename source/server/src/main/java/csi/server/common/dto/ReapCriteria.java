package csi.server.common.dto;


import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;


public enum ReapCriteria implements IsSerializable {

    CREATE, OPEN, UPDATE;

    private static final Map<ReapCriteria, String> attributeMap = new HashMap<ReapCriteria, String>();
    static {
        attributeMap.put(CREATE, "createDate");
        attributeMap.put(OPEN, "lastOpenDate");
        attributeMap.put(UPDATE, "lastUpdateDate");
    }

    public static String getMappedAttribute(ReapCriteria c) {
        return attributeMap.get(c);
    }
}
