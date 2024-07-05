package csi.startup;

import csi.server.common.exception.CentrifugeException;

@SuppressWarnings("serial")
public class InitializationException extends CentrifugeException {

    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(Throwable cause) {
        super(cause);
    }

}
