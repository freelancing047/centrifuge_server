package csi.server.connector;

public class ConnectionFactoryNotFoundException extends RuntimeException {

    public ConnectionFactoryNotFoundException() {
    }

    public ConnectionFactoryNotFoundException(String msg) {
        super(msg);
    }

    public ConnectionFactoryNotFoundException(Throwable t) {
        super(t);
    }

    public ConnectionFactoryNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

}
