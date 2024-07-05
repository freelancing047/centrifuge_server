package csi.server.util;

/**
 * Created by centrifuge on 12/3/2015.
 */
public class ThreadTerminatedException extends Exception {

    private Exception _exception = null;

    public ThreadTerminatedException(Exception exceptionIn) {

        _exception = exceptionIn;
    }

    public ThreadTerminatedException(String reasonIn) {

        super(reasonIn);
    }

    public Exception getException() {

        return _exception;
    }
}
