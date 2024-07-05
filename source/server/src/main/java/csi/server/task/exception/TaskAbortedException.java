package csi.server.task.exception;

public class TaskAbortedException extends RuntimeException {

    public TaskAbortedException() {
        super();
    }

    public TaskAbortedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskAbortedException(String message) {
        super(message);
    }

    public TaskAbortedException(Throwable cause) {
        super(cause);
    }

}
