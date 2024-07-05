package csi.server.common.data;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int capacity = 50;

    public LRUCache() {

    }

    public LRUCache(int capacity) {
        super(capacity + 1, 1.1f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        return size() > capacity;
    }
}
