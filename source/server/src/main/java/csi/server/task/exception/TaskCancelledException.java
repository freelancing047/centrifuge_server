package csi.server.task.exception;

public class TaskCancelledException extends RuntimeException {

    public TaskCancelledException() {
        super();
    }

    public TaskCancelledException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskCancelledException(String message) {
        super(message);
    }

    public TaskCancelledException(Throwable cause) {
        super(cause);
    }

}
