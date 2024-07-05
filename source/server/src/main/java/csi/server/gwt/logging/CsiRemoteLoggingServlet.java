package csi.server.gwt.logging;

import java.util.logging.LogRecord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gwt.core.server.StackTraceDeobfuscator;
import com.google.gwt.logging.shared.RemoteLoggingService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CsiRemoteLoggingServlet extends RemoteServiceServlet implements RemoteLoggingService {
   private static final Logger LOG = LogManager.getLogger(CsiRemoteLoggingServlet.class);

    private StackTraceDeobfuscator deobfuscator = StackTraceDeobfuscator.fromFileSystem("webapps/Centrifuge/WEB-INF/deploy/csi/symbolMaps/");

    @Override
    public String logOnServer(LogRecord record) {
        try {
            Throwable thrown = record.getThrown();
            while (thrown != null) {

                thrown.setStackTrace(deobfuscator.resymbolize(thrown.getStackTrace(), getPermutationStrongName()));
                thrown = thrown.getCause();
            }
            Logger logger = LogManager.getLogger(record.getLoggerName());
            Level level = getLevel(record);
            logger.log(level, record.getMessage(), record.getThrown());

        } catch (Exception e) {
            LOG.error("Remote logging failed", e);
            return "Remote logging failed, check stack trace for details.";
        }
        return null;
    }

    /* Mapping should be (see http://www.slf4j.org/apidocs/org/slf4j/bridge/SLF4JBridgeHandler.html)
    ALL     -> TRACE
    FINEST  -> TRACE
    FINER   -> DEBUG
    FINE    -> DEBUG
    CONFIG  -> INFO
    INFO    -> INFO
    WARNING -> WARN
    SEVERE  -> ERROR
    OFF     -> ERROR*/
    public Level getLevel(LogRecord record) {
        Level level = Level.ERROR;
        java.util.logging.Level utilLevel = record.getLevel();
        switch (utilLevel.toString()) {
            case "ALL":
                break;
            case "FINEST":
                level = Level.TRACE;
                break;
            case "FINER":
                level = Level.DEBUG;
                break;
            case "FINE":
                level = Level.DEBUG;
                break;
            case "CONFIG":
                level = Level.INFO;
                break;
            case "INFO":
                level = Level.INFO;
                break;
            case "WARNING":
                level = Level.WARN;
                break;
            case "SEVERE":
                level = Level.ERROR;
                break;
            case "OFF":
                level = Level.ERROR;
                break;
        }
        return level;
    }

    public void setSymbolMapsDirectory(String symbolMapsDir) {
        deobfuscator = StackTraceDeobfuscator.fromFileSystem(symbolMapsDir);
    }
}
