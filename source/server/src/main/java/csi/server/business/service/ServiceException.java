package csi.server.business.service;

import csi.server.common.exception.CentrifugeException;

public class ServiceException extends CentrifugeException {

    private static final long serialVersionUID = -5680882673896978855L;

    public ServiceException(String id, String message, Throwable cause) {
        super(id, message, cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }
    
}
