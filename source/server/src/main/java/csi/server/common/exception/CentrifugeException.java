package csi.server.common.exception;

import java.io.Serializable;

/**
 * Root application exception serving the purposes:
 * <ul>
 *     <li>Wrap-exceptions thrown by the platform and 3rd party libraries.</li>
 *     <li>Mark whether an exception has been logged.</li>
 *     <li>Mark an exception as retryable so that the caller could repeat the action in case of transient problems</li>
 * </ul>
 * @author Tildenwoods
 * @author dorel
 */

@SuppressWarnings("serial")
public class CentrifugeException extends Exception implements Serializable {

    protected String id;
    protected boolean logged;
    protected boolean retryable;
    
    public CentrifugeException() {}
    
    public CentrifugeException(String id, String message, Throwable cause) {
        super (message, cause);
        this.id = id;
    }

    public CentrifugeException(String message, Throwable cause) {
        this (null, message, cause);
    }

    public CentrifugeException(String message) {
        this (message, null);
    }

    public CentrifugeException(Throwable cause) {
        this (null, cause);
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }
    
    public String getId() {
        return id;
    }
    
    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }
    
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Returns whether this exception been logged yet.
     * 
     * @postcondition marks the exception as logged.
     * @return has this been logged previously.
     */
    public boolean hasLoggedConditional() {
        boolean orig = logged;
        logged = true;
        return orig;
    }

    public static CentrifugeException getLoggedException(Throwable ex) {
        CentrifugeException ce = new CentrifugeException(ex);
        ce.setLogged(true);
        return ce;
    }

    public static CentrifugeException getLoggedException(String msg) {
        CentrifugeException ce = new CentrifugeException(msg);
        ce.setLogged(true);
        return ce;
    }

}
