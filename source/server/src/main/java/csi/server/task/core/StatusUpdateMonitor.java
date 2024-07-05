package csi.server.task.core;

/**
 * Mutex object used to signal <code>PooledResultProcessor</code> that it might exist a task result to be consumed.
 * 
 * Must be called only from a synchronized section.
 */
public class StatusUpdateMonitor {

    private long updateCount = 0;

    public void incrementUpdateCount() {
        updateCount++;
    }

    public void resetUpdateCount() {
        updateCount = 0L;
    }

    public long getUpdateCount() {
        return updateCount;
    }
}
