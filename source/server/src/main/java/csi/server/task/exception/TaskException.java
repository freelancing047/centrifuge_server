package csi.server.task.exception;

public class TaskException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TaskException(String message) {
        super(message);
    }

    public TaskException() {
        super();
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskException(Throwable cause) {
        super(cause);
    }
}
