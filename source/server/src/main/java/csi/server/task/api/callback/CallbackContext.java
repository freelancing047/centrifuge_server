package csi.server.task.api.callback;

/**
 * 
 * A means for task management API to understand how the client who submited a particular task 
 * is doing during the task execution and to react accordingly in case this client is not able 
 * to receive the result of execution.
 * 
 * Any of these states are signals that this <code>CallbackContext</code> is not valid anymore 
 * so consuming this result of task execution is compromised. Knowing the current state of the client 
 * through this <code>CallbackContext</code>, will prevent loosing results.
 * 
 * The <code>ResultHandlerTask</code> has check points to identify one of this invalid states 
 * giving result another chance to be consumed when there is another <code>CallbackContext</code> registered and valid.
 * 
 * @author dorel.matei
 *
 */
public interface CallbackContext {
    
    /** This <code>CallbackContext</code> is finished */
    boolean isComplete();
    
    /** Marks this <code>CallbackContext</code> as complete */
    void markComplete();
    
    /** This <code>CallbackContext</code> is timed out */
    boolean isTimeout();
    
    /** Marks this <code>CallbackContext</code> as timeout */
    void markTimeout();
    
    /** This <code>CallbackContext</code> encountered an error */
    boolean isError();
    
    /** Marks this <code>CallbackContext</code> as error */
    void markError();

    /** This <code>CallbackContext</code> is about to finish */
    boolean isCommited();
}
