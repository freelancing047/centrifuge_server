package csi.server.task.api.callback;

import csi.server.task.api.TaskContext;

/**
 * Consumes a task result of an execution.
 * The implementation will be provided by the client of task management API 
 * who decides how this result will be consumed.
 * 
 * @author dorel.matei
 *
 */
public interface CallbackConsumer {

    /**
     * @return true if the <code>TaskContext</code> was successfully consumed, 
     * false otherwise and in this case the <code>TaskContext</code> will ne reschduled for consuming.
     */
    boolean consume(CallbackContext callbackContext, TaskContext taskContext);
}
