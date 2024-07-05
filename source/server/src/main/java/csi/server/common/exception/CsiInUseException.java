package csi.server.common.exception;

/**
 * Created by centrifuge on 1/8/2019.
 */
public class CsiInUseException extends CentrifugeException {

    public CsiInUseException() {
        super();
    }

    public CsiInUseException(String id, String message, Throwable cause) {
        super(id, message, cause);
    }

    public CsiInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsiInUseException(String message) {
        super(message);
    }

    public CsiInUseException(Throwable cause) {
        super(cause);
    }
}
