package csi.server.task.core.processor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.ProtocolConfig;
import csi.log.LogThreadContextUtil;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskStatusCode;
import csi.server.task.core.worker.ServiceInvokerTask;

/**
 * Processes the tasks by scheduling them for synch or async execution according to their type.
 *
 * @author dorel.matei
 *
 */
public class PooledTaskProcessor implements TaskProcessor {
   private static final Logger LOG = LogManager.getLogger(PooledTaskProcessor.class);

    protected ProtocolConfig protocolConfig = Configuration.getInstance().getProtocolConfig();
    protected TaskThreadPoolExecutor executor = new TaskThreadPoolExecutor(protocolConfig.getTaskManagerCoreThreads(), protocolConfig.getTaskManagerMaxThreads(), protocolConfig
            .getTaskManagerKeepAlive(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DaemonThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
   public void startProcessing() {
        if (LOG.isDebugEnabled()) {
           LOG.debug("PooleadTaskProcessor is started.");
        }
    }

    @Override
   public void processTaskSynchronously(TaskContext taskContext) {

        taskContext.setSynchronous(true);
        executor.setCurrentContext(taskContext);
        LogThreadContextUtil.putContextThreadContext(taskContext);
        try {
            ServiceInvokerTask serviceInvokerTask = new ServiceInvokerTask(taskContext);
            taskContext.getStatus().setTaskStatus(TaskStatusCode.TASK_STATUS_RUNNING);
            serviceInvokerTask.run();
        } finally {
            taskContext.getStatus().setTaskStatus(TaskStatusCode.TASK_STATUS_COMPLETE);
            if((executor.getCurrentContext() != null) && executor.getCurrentContext().equals(taskContext)){
                taskContext.clearTaskSession();
                executor.setCurrentContext(null);
            }
            taskContext.setSynchronousHttpResponse(null);
            LogThreadContextUtil.clearThreadContext();
        }
    }

    @Override
   public void processTaskAsynchronously(TaskContext taskContext) {
        ServiceInvokerTask serviceInvokerTask = new ServiceInvokerTask(taskContext);
        executor.execute(serviceInvokerTask);
    }

    @Override
   public TaskContext getCurrentContext() {
        return executor.getCurrentContext();
    }

    @Override
   public TaskThreadPoolExecutor getExecutor() {
    	return executor;
    }

    @Override
   public void stopProcessing() {
        executor.shutdownNow();
        if (LOG.isDebugEnabled()) {
           LOG.debug("PooleadTaskProcessor is stopped.");
        }
    }

}
