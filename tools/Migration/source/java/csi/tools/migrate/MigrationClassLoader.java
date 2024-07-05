package csi.tools.migrate;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class MigrationClassLoader extends URLClassLoader {

    public MigrationClassLoader(URL[] urls) {
        super(urls);
        // TODO Auto-generated constructor stub
    }

    private Map<String, Class> cache = new HashMap<String, Class>();

    public synchronized Class loadClass(String name) throws ClassNotFoundException {
       
        Class clz = null;
        clz = cache.get(name);
        if (clz != null) {
            return clz;
        }

        try {
            clz = super.findClass(name);
        } catch (Exception e) {

        }

        if (clz == null) {
            try {
                clz = super.loadClass(name);
            } catch (Exception e) {

            }
        }

        if (clz == null) {
            throw new ClassNotFoundException(name);
        }

        cache.put(name, clz);

        return clz;
    }
}
