package csi.server.common.exception;

/**
 * Created by centrifuge on 3/3/2017.
 */
public class CsiSecurityException extends CentrifugeException {

    public CsiSecurityException() {
        super();
    }

    public CsiSecurityException(String id, String message, Throwable cause) {
        super(id, message, cause);
    }

    public CsiSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsiSecurityException(String message) {
        super(message);
    }

    public CsiSecurityException(Throwable cause) {
        super(cause);
    }
}
