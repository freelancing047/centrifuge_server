package csi.server.task.core;

/**
 * Receives notifications when the services report a task execution related event like: progress, complete, error.
 *  
 * @author dorel.matei
 *
 */
public interface StatusUpdateListener {
    void notifyUpdate();
}
