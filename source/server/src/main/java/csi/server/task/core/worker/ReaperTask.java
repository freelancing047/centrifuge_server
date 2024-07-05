package csi.server.task.core.worker;

import csi.server.task.api.TaskController;

/**
 * @author dorel.matei
 * Cleans up the old task statuses not consumed after a while.
 */
public class ReaperTask implements Runnable {

    private static final long OLD_TASKS_TIMEOUT = 1000L * 60 * 1;
    private static final long SLEEP_TIMEOUT = 1000L * 60 * 1;
    private boolean stopped = false;

    private Thread owner;

    @Override
    public void run() {
        owner = Thread.currentThread();

        while (!isStopped()) {
            TaskController.getInstance().removeOldTerminalTasks(OLD_TASKS_TIMEOUT);
            try {
                Thread.sleep(SLEEP_TIMEOUT);
            } catch (InterruptedException e) {
            }
        }

    }

    public synchronized boolean isStopped() {
        return stopped;
    }

    public synchronized void setStopped(boolean stopped) {
        this.stopped = stopped;

        try {
            this.owner.interrupt();
        } catch (Throwable t) {
        }

    }
}
