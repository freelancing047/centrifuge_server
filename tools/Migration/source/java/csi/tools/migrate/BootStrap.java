package csi.tools.migrate;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class BootStrap {

    public static final String EXPORT = "csi.tools.migrate.Export";

    public static final String IMPORT = "csi.tools.migrate.Import";

    public static void main(String[] args) throws Throwable {
        File serverdir = new File(args[1]);
        File commonLib = new File(serverdir, "lib");
        File webinfLib = new File(serverdir, "webapps/Centrifuge/WEB-INF/lib");
        File webappclasses = new File(serverdir, "webapps/Centrifuge/WEB-INF/classes");
        File webappdir = new File(serverdir, "webapps/Centrifuge");

        List<URL> jars = new ArrayList<URL>();

        addResource(jars, new File(serverdir, "utils/migrate/migrate.jar"));
        addJars(jars, commonLib);
        addJars(jars, webinfLib);
        addResource(jars, webappclasses);
        addResource(jars, webappdir);

        URLClassLoader loader = new MigrationClassLoader(jars.toArray(new URL[0]));
        Thread.currentThread().setContextClassLoader(loader);

        Class<?> actionClz = null;
        if (args[0].equals("export")) {
            actionClz = loader.loadClass(EXPORT);
        } else if (args[0].equals("import")) {
            actionClz = loader.loadClass(IMPORT);
        } else {
            System.exit(0);
        }

        Method main = actionClz.getMethod("main", new Class[] { String[].class });

        String[] mainArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            mainArgs[i - 1] = args[i];
        }

        main.invoke(null, new Object[] { mainArgs });
    }

    private static void addResource(List<URL> jars, File path) throws MalformedURLException {
        jars.add(path.toURL());
    }

    private static void addJars(List<URL> jars, File path) throws MalformedURLException {
        if (path.isFile()) {
            jars.add(path.toURL());
        } else {
            if (!path.exists()) {
                return;
            }

            FileFilter jarFilter = new FileFilter() {

                public boolean accept(File pathname) {
                    return (pathname.getName().endsWith(".jar"));
                }
            };

            File[] listing = path.listFiles(jarFilter);
            for (File child : listing) {
            	if (child.getName().equalsIgnoreCase("saxon8.jar") || child.getName().equalsIgnoreCase("saxon8-xpath.jar")){
            		continue;  // do not include the saxon jar files
            	}
                jars.add(child.toURL());
            }
        }
    }
}