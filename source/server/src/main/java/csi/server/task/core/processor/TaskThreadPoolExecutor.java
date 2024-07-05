package csi.server.task.core.processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import csi.log.LogThreadContextUtil;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskStatusCode;
import csi.server.task.core.worker.ServiceInvokerTask;

/**
 * Handles paralel execution of tasks.
 * 
 * @author dorel.matei
 *
 */
public class TaskThreadPoolExecutor extends ThreadPoolExecutor {

    private ThreadLocal<TaskContext> localTaskContext = new InheritableThreadLocal<TaskContext>();

    public TaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public TaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public TaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public TaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

        if (r instanceof ServiceInvokerTask) {
            TaskContext ctx = ((ServiceInvokerTask) r).getTaskContext();
            LogThreadContextUtil.putContextThreadContext(ctx);
            ctx.getStatus().setTaskStatus(TaskStatusCode.TASK_STATUS_RUNNING);
            localTaskContext.set(ctx);
        } else {
            localTaskContext.set(null);
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        TaskContext ctx = localTaskContext.get();
        ctx.clearTaskSession();
        localTaskContext.set(null);
        LogThreadContextUtil.clearThreadContext();
    }

    public TaskContext getCurrentContext() {
        return localTaskContext.get();
    }
    
    public void setCurrentContext(TaskContext ctx) {
        localTaskContext.set(ctx);
    }
}
