package csi.tools.migrate;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

public class LogUtil {

    /**
     * Configure log4j to avoid build warnings.
     * The BasicConfigurator.configure() sets everything up for
     * a console appender, but unfortunately sets the level at "DEBUG".
     * We use the PropertiesCOnfigurator to set the message threshold to WARN.
     * Note that hibernate puts out a lot of dumb messages at INFO level,
     * and we (Centrifuge) log a spurious ERROR message because the schema.ini
     * file is expected to be in a Samples directory.
     */
    public static void configureLog4J() {
        BasicConfigurator.configure();

        Properties log4jProps = new Properties();
        log4jProps.put("log4j.threshold", "FATAL");
        PropertyConfigurator.configure(log4jProps);
    }
}