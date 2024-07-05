package csi.server.task.core.processor;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.ProtocolConfig;
import csi.server.task.api.TaskController;
import csi.server.task.api.callback.CallbackContext;
import csi.server.task.api.callback.CallbackRegistry;
import csi.server.task.core.StatusUpdateListener;
import csi.server.task.core.StatusUpdateMonitor;
import csi.server.task.core.worker.ResultHandlerTask;

/**
 * Processes task contexts resulted from tasks execution.
 * The processing is started by a notification coming from one of the following entities :
 * <li>
 *     <ul>The thread that reported an execution event (complete, progress, feedback, cancel)</ul>
 *     <ul>The thread that created and registered a new <code>CallbackContext</code>
 * </li>
 * Once the processing is started, in order the results to be successfully consumed, a rendez-vouz must be between
 * existence of a task in a client result queue and existence of a valid <code>CallbackContext</code> fro that client.
 *
 * @author dorel.matei
 *
 */
public class PooledResultProcessor implements Processor, StatusUpdateListener, Runnable {
   private static final Logger LOG = LogManager.getLogger(PooledResultProcessor.class);

   private ProtocolConfig protocolConfig = Configuration.getInstance().getProtocolConfig();

    private ResultThreadPoolExecutor executor = new ResultThreadPoolExecutor(protocolConfig.getStatusProcessorCoreThreads(), protocolConfig.getStatusProcessorMaxThreads(),
            protocolConfig.getStatusProcessorKeepAlive(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

    boolean stopped = false;

    private Thread owner;

    private CallbackRegistry callbackContextRegistry = CallbackRegistry.getInstance();
    private StatusUpdateMonitor updateMonitor = callbackContextRegistry.getStatusUpdateMonitor();

    @Override
   public void startProcessing() {
        owner = new Thread(this, "PooledResultProcessor");
        owner.setDaemon(true);
        owner.start();
        if (LOG.isDebugEnabled()) {
            LOG.debug("PooledResultProcessor started.");
        }
    }

    @Override
   public void notifyUpdate() {
        synchronized (updateMonitor) {
            updateMonitor.incrementUpdateCount();
            updateMonitor.notify();
        }
    }

    @Override
   public void run() {
        while (!stopped) {
            try {
                synchronized (updateMonitor) {
                    // Wait only if there were no previous notifications.
                    // Each notification implies updating the count in <code>updateMonitor</code>,
                    // so this thread will know about it without being in WAITING state.
                    if (updateMonitor.getUpdateCount() == 0) {
                        updateMonitor.wait();
                    }
                    updateMonitor.resetUpdateCount();
                }
                Set<String> registeredClientIds = callbackContextRegistry.getRegisteredClientIds();
                for (String clientId : registeredClientIds) {
                    if (TaskController.getInstance().clientHasStatus(clientId)) {
                        CallbackContext callbackContext = callbackContextRegistry.unregisterCallbackContext(clientId);
                        if ((callbackContext != null) && !callbackContext.isCommited()) {
                            ResultHandlerTask resultConsumerTask = new ResultHandlerTask(clientId, callbackContext);
                            executor.execute(resultConsumerTask);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOG.debug("Status processor interrupted", e);
            }
        }
    }

    @Override
   public void stopProcessing() {
        executor.shutdownNow();
        stopped = true;
        if (owner != null) {
            owner.interrupt();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("PooledResultProcessor stopped.");
        }
    }

}
