package csi.client.gwt.events;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CsiEventHeader {

    private TreeMap<String, String> map;


    public CsiEventHeader() {
        setMap(new TreeMap<String, String>());
    }


    public CsiEventHeader(Map<String, String> m) {
        if (m == null) {
            setMap(new TreeMap<String, String>());
        }
        setMap(new TreeMap<String, String>(m));
    }


    public TreeMap<String, String> addHeader(String k, String v) {
        if (k != null && v != null) {
            // can only add strings to header.
            getMap().put(k, v);
        }
        return getMap();
    }


    public TreeMap<String, String> removeHeader(String key) {
        getMap().remove(key);
        return getMap();
    }


    public String getValue(String key) {
        return getMap().get(key);
    }


    public TreeMap<String, String> getMap() {
        return map;
    }


    public void setMap(TreeMap<String, String> map) {
        this.map = map;
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Set<String> keys = map.keySet();
        for (Iterator<String> i = keys.iterator(); i.hasNext();) {
            String key = i.next();
            String value = map.get(key);
            if (value != null) {
                sb.append(key);
                sb.append(":");
                sb.append(value);
                sb.append(";");
            }
        }
        return sb.toString();
    }
}