package csi.config;

public class ProtocolConfig
        extends AbstractConfigurationSettings {

    private static final int DEFAULT_COMET_SESSION_TIMEOUT = 60 * 1000;
    private static final int DEFAULT_TASK_MANAGER_CORE_THREADS = 5;
    private static final int DEFAULT_TASK_MANAGER_MAX_THREADS = 30;
    private static final long DEFAULT_TASK_MANAGER_KEEP_ALIVE = 300L;
    private static final int DEFAULT_STATUS_PROCESSOR_CORE_THREADS = 5;
    private static final int DEFAULT_STATUS_PROCESSOR_MAX_THREADS = 30;
    private static final long DEFAULT_STATUS_PROCESSOR_KEEP_ALIVE = 300L;

    /**
     * The timeout period in milliseconds associated to an event of type :
     * <code>CometEvent.EventType.BEGIN</code>
     */
    private int cometSessionTimeout;

    /**
     * The minimum size of threads to be executed in parallel by the
     * <code>TaskThreadPoolExecutor</code>
     */
    private int taskManagerCoreThreads;

    /**
     * The maximum size of threads accepted by the
     * <code>TaskThreadPoolExecutor</code>
     */
    private int taskManagerMaxThreads;

    /**
     * The timeout period to keep idle threads alive in
     * <code>TaskThreadPoolExecutor</code>
     */
    private long taskManagerKeepAlive;

    /**
     * The minimum size of threads to be executed in parallel by the
     * <code>ResultThreadPoolExecutor</code>
     */
    private int statusProcessorCoreThreads;

    /**
     * The maximum size of threads accepted by the
     * <code>ResultThreadPoolExecutor</code>
     */
    private int statusProcessorMaxThreads;

    /**
     * The timeout period to keep idle threads alive in
     * <code>ResultThreadPoolExecutor</code>
     */
    private long statusProcessorKeepAlive;

    public int getCometSessionTimeout() {
        if (cometSessionTimeout <= 0) {
            return DEFAULT_COMET_SESSION_TIMEOUT;
        }
        return cometSessionTimeout;
    }

    public void setCometSessionTimeout(int cometSessionTimeout) {
        this.cometSessionTimeout = cometSessionTimeout;
    }

    public int getTaskManagerCoreThreads() {
        if (taskManagerCoreThreads <= 0) {
            return DEFAULT_TASK_MANAGER_CORE_THREADS;
        }
        return taskManagerCoreThreads;
    }

    public void setTaskManagerCoreThreads(int taskManagerCoreThreads) {
        this.taskManagerCoreThreads = taskManagerCoreThreads;
    }

    public int getTaskManagerMaxThreads() {
        if (taskManagerMaxThreads <= 0) {
            return DEFAULT_TASK_MANAGER_MAX_THREADS;
        }
        return taskManagerMaxThreads;
    }

    public void setTaskManagerMaxThreads(int taskManagerMaxThreads) {
        this.taskManagerMaxThreads = taskManagerMaxThreads;
    }

    public long getTaskManagerKeepAlive() {
        if (taskManagerKeepAlive <= 0) {
            return DEFAULT_TASK_MANAGER_KEEP_ALIVE;
        }
        return taskManagerKeepAlive;
    }

    public void setTaskManagerKeepAlive(long taskManagerKeepAlive) {
        this.taskManagerKeepAlive = taskManagerKeepAlive;
    }

    public int getStatusProcessorCoreThreads() {
        if (statusProcessorCoreThreads <= 0) {
            return DEFAULT_STATUS_PROCESSOR_CORE_THREADS;
        }
        return statusProcessorCoreThreads;
    }

    public void setStatusProcessorCoreThreads(int statusProcessorCoreThreads) {
        this.statusProcessorCoreThreads = statusProcessorCoreThreads;
    }

    public int getStatusProcessorMaxThreads() {
        if (statusProcessorMaxThreads <= 0) {
            return DEFAULT_STATUS_PROCESSOR_MAX_THREADS;
        }
        return statusProcessorMaxThreads;
    }

    public void setStatusProcessorMaxThreads(int statusProcessorMaxThreads) {
        this.statusProcessorMaxThreads = statusProcessorMaxThreads;
    }

    public long getStatusProcessorKeepAlive() {
        if (statusProcessorKeepAlive <= 0) {
            return DEFAULT_STATUS_PROCESSOR_KEEP_ALIVE;
        }
        return statusProcessorKeepAlive;
    }

    public void setStatusProcessorKeepAlive(long statusProcessorKeepAlive) {
        this.statusProcessorKeepAlive = statusProcessorKeepAlive;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cometSessionTimeout :" + getCometSessionTimeout() + "\n");
        sb.append("taskManagerCoreThreads :" + getTaskManagerCoreThreads() + "\n");
        sb.append("taskManagerMaxThreads :" + getTaskManagerMaxThreads() + "\n");
        sb.append("taskManagerKeepAlive :" + getTaskManagerKeepAlive() + "\n");
        sb.append("statusProcessorCoreThreads :" + getStatusProcessorCoreThreads() + "\n");
        sb.append("statusProcessorMaxThreads :" + getStatusProcessorMaxThreads() + "\n");
        sb.append("statusProcessorKeepAlive :" + getStatusProcessorKeepAlive() + "\n");
        return sb.toString();
    }
}
