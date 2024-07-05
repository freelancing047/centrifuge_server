package csi.server.common.dto;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CsiMap<K, V> extends HashMap<K, V> implements IsSerializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CsiMap() {
        super();
    }

    public CsiMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CsiMap(int initialCapacity) {
        super(initialCapacity);
    }

    public CsiMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public CsiMap<K, V> clone() {
        
        CsiMap<K, V> myClone = new CsiMap<K, V>();
        
        myClone.putAll(this);
        
        return myClone;
    }
}
