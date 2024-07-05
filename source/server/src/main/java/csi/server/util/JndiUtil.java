package csi.server.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import csi.server.common.data.LRUCache;
import csi.server.common.exception.CentrifugeException;

public class JndiUtil {

    private static LRUCache<String, Object> objectCache = new LRUCache<String, Object>(1000);

    @SuppressWarnings("unchecked")
    public static <T> T lookupResource(Class<T> clazz, String resUrl) throws CentrifugeException {
        Context initCtx = null;
        try {
            Object obj = objectCache.get(resUrl);
            if (obj != null) {
                return (T) obj;
            }

            initCtx = new InitialContext();
            obj = initCtx.lookup(resUrl);
            if (obj != null) {
                objectCache.put(resUrl, obj);
            }
            return (T) obj;
        } catch (NamingException e) {
            throw new CentrifugeException("Failed to lookup JNDI resource: " + resUrl, e);
        } finally {
            try {
                if (initCtx != null) {
                    initCtx.close();
                }
            } catch (NamingException e) {
                // ignore
            }
        }
    }
    
    public static <T> T internalResourceLookup( Class<T> clazz, String url ) throws NamingException {
        Context initCtx = null;
        try {
            Object obj = objectCache.get(url);
            if (obj != null) {
                return clazz.cast(obj);
            }

            initCtx = new InitialContext();
            obj = initCtx.lookup(url);
            if (obj != null) {
                objectCache.put(url, obj);
            }
            return clazz.cast(obj);
        } finally {
            try {
                if (initCtx != null) {
                    initCtx.close();
                }
            } catch (NamingException e) {
                // ignore
            }
        }

        
    }
}
