package csi.server.dao;

import csi.server.common.exception.CentrifugeException;


public class DataAccessException extends CentrifugeException {

    private static final long serialVersionUID = -3366485763475183500L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DataAccessException(String message) {
        super(message);
    }
    
    public DataAccessException(Throwable cause) {
        super(cause);
    }

}
