/*
 * @(#) AsyncConfiguration.java,  29.04.2010
 *
 */
package csi.server.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Asynchronous Initialization Parameters Configuration.
 * 
 * @author <a href="mailto:iulian.boanca@lpro.leverpointinc.com">Iulian Boanca</a>
 * 
 */
public class AsyncConfiguration {

    private static Map<String, Object> configMap = new ConcurrentHashMap<String, Object>();

    public static Object getAttribute(String key) {
        return configMap.get(key);
    }

    public static void setAttribute(String key, Object value) {
        if (value != null) {
            configMap.put(key, value);
        }
    }

    public void removeAttribute(String key) {
        configMap.remove(key);
    }

}
