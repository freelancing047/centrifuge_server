package csi.server.task.core.worker;

import java.io.InterruptedIOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import csi.log.LogThreadContextUtil;
import csi.security.CsiSecurityManager;
import csi.server.business.service.BusinessServiceManager;
import csi.server.common.exception.CentrifugeException;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.InvokeListener;
import csi.server.task.api.PostInvoke;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskAbortedException;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.jogformer.Jogformer;
import csi.startup.CleanUpThread;

/**
 * Invokes a business service using the arguments from <code>TaskContext</code>, 
 * wrapping each invocation in a separate transaction.
 * 
 * @author dorel.matei
 *
 */
public class ServiceInvokerTask implements Runnable {
   private static final Logger LOG = LogManager.getLogger(ServiceInvokerTask.class);

   private static Map<String, Semaphore> semaphoreMap = Maps.newConcurrentMap();
    private static Map<String, String> resourceControlMap = Maps.newConcurrentMap();

    /** The Context for the Current Task */
    private TaskContext taskContext;

    public ServiceInvokerTask(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    public TaskContext getTaskContext() {
        return this.taskContext;
    }

    public void run() {
        CsiSecurityManager.setAuthorization(taskContext.getSecurityToken());
        invoke(this.taskContext);
    }

    /**
     * Invokes a business service method based on the given <code>taskContext</code>.
     * 
     * @param taskContext the TaskContext object that holds relevant information regarding the Method and its parameters.
     * @return the Service's Method output
     * @throws CentrifugeException in case there is an exception during invocation
     */
    private Object invoke(TaskContext taskContext) {
        assert taskContext != null : "PreCondition error: taskContext is null";

        Method method = taskContext.getMethod();
        assert method != null : "PreCondition error: method is null";

        Object result = null;
        CsiPersistenceManager.logEntry("ServiceInvokerTask:invoke", false);
        CsiPersistenceManager.begin();

        String pool = taskContext.getExecutionPoolId();
        Semaphore semaphore = null;
        String methodName = method.getName();
        if (pool != null) {

            semaphore = semaphoreMap.get(pool);
            if (semaphore == null) {
                semaphore = new Semaphore(1,true);
                semaphoreMap.put(pool, semaphore);
                semaphore = semaphoreMap.get(pool);
            }
            
            //TODO need to fix this mess.
            if (!"componentLayoutAction".equals(methodName) &&
                  !"loadGraph".equals(methodName)&&
                    !"computeSNA".equals(methodName)&&
                    !"findPaths".equals(methodName)&&
                    !"findPatterns".equals(methodName)&&
                    !"gwtFindItem2".equals(methodName) &&
                    !"getEndPoints".equals(methodName) &&
                    !"listIcons".equals(methodName) &&
                    !"getChartMetrics".equals(methodName) &&
                    !"getMapTotalMetrics".equals(methodName) &&
                    !"getViewMetrics".equals(methodName) &&
                    !"listThemesByType".equals(methodName)
                    ) { //NON-NLS
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    //TODO: proper error here?
                    //e.printStackTrace();
                }
            }
            else{
                CsiPersistenceManager.getMetaEntityManager().getTransaction().setRollbackOnly();
            }
        }
        
        //Take write control here
        String resourceUuid = taskContext.getResourceUuid();
        if(resourceUuid != null && ("openDataView".equals(methodName) || "getTemplate".equals(methodName))) {

            String clientId = resourceControlMap.get(resourceUuid);
            //Add new task runtime as current writer
            resourceControlMap.put(resourceUuid, taskContext.getClientId());
            
            //Loop over taskController tasks, canceling the ones with old gwtRuntime
            if(clientId != null) {
                boolean reopening = clientId.equals(taskContext.getClientId());
                TaskController taskController = TaskController.getInstance();
                List<TaskContext> tasks = taskController.listAllTasks();
                for (TaskContext task : tasks) {
                    if (task != null && task.getClientId() != null && task.getClientId().equals(clientId)) {
                        if (!reopening) {
                            task.conflict();
                        }
                        task.cancel();
                    }
                }
            }
            
        }
        
        
        
        try {
            

            //Checks for write control, if not in control, cancels this task
            if(resourceUuid != null) { 
                String clientId = resourceControlMap.get(resourceUuid);
                if(clientId != null && !taskContext.getClientId().equals(clientId)) {
                    //This just sets the task as conflict so we can send down a different response, everything else is still cancel logic
                    taskContext.conflict();
                    taskContext.cancel();
                    CsiPersistenceManager.getMetaEntityManager().getTransaction().setRollbackOnly();
                }
                
            }
            
            LOG.debug("Executing Task\n" + taskContext);
            TaskHelper.checkForCancel();

            Object[] args = taskContext.getMethodArgs();
            if (args == null) {
                args = new Object[0];
            }

            LogThreadContextUtil.putContextThreadContext(taskContext);
            


            
            if (taskContext.isGwtService()) {
                result = method.invoke(taskContext.getServiceClass(), args);

            } else {
                result = method.invoke(BusinessServiceManager.getInstance().getComponent(method.getDeclaringClass()),
                        args);
            }

            if (CsiPersistenceManager.isRollbackOnly()) {
                CsiPersistenceManager.rollback();
            } else {
                CsiPersistenceManager.commit();
                CleanUpThread.finalizeDeletes();

                if (taskContext.isGwtService()) {
	                Jogformer preSessionJogformer = taskContext.getPreSessionJogformer();
	                result = preSessionJogformer.transform(result);
                }
            }
            
            
            LOG.debug(" +++ WorkerTask complete: reporting complete ... for taskId: " + taskContext.getTaskId());

            try {
                CsiPersistenceManager.close();
            }
            catch (Exception e){
                LOG.warn(e);
            }
            if (taskContext.isGwtService()) {
                try {
                    Jogformer postSessionJogformer = taskContext.getPostSessionJogformer();
                    result = postSessionJogformer.transform(result);


                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            TaskHelper.reportComplete(result);

        } catch (Throwable ex) {
            LOG.warn("Rolling back transaction for task:\n"+ taskContext);
            CsiPersistenceManager.rollback();

            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            if (rootCause == null) {
                rootCause = ex;
            }
            String msg = ExceptionUtils.getRootCauseMessage(ex);

            if (rootCause instanceof TaskCancelledException || rootCause instanceof InterruptedException
                    || rootCause instanceof InterruptedIOException) {
                LOG.info("Cancelled task: " + methodName);
                LOG.debug(" +++ WorkerTask cancelled: reporting cancelled for taskId: " + taskContext.getTaskId(), rootCause);
                if(taskContext.isConflict()) {
                    TaskHelper.reportConflict();
                } else {
                    TaskHelper.reportCancelled();
                }
            } else if (rootCause instanceof TaskAbortedException) {
                LOG.info("Aborted task " + methodName + ": " + msg);
                LOG.debug(" +++ WorkerTask aborted: reporting cancelled for taskId: " + taskContext.getTaskId(), rootCause);
                if(taskContext.isConflict()) {
                    TaskHelper.reportConflict();
                } else {
                    TaskHelper.reportCancelled();
                }

            } else {
                LOG.error(msg, ex);
                LOG.debug(" +++ WorkerTask exception: reporting error ... for taskId: " + taskContext.getTaskId());
                String message = String.format("Error executing %s: %s", methodName, msg);
                //TaskHelper.reportError(message, rootCause);
                TaskHelper.reportError(message, ex);
            }
                        
        }finally {
            try {
                CsiPersistenceManager.close();
            } catch (Throwable e) {
                LOG.error(e);
            }
            CsiPersistenceManager.releaseUserConnection();
            CsiPersistenceManager.releaseCacheConnection();
            if (semaphore != null) {
                semaphore.release();
            }
        }
        processPostInvoke(method);

        CsiPersistenceManager.logExit("ServiceInvokerTask:invoke", false);

        return result;
    }

    private void processPostInvoke(Method method) {
        processAnnotation(method);
        Class<?> declaringClass = method.getDeclaringClass();
        processAnnotation(declaringClass);
    }

    private void processAnnotation(AnnotatedElement target) {
        if (target.isAnnotationPresent(PostInvoke.class)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Post-Invoke processing : " + target.toString());
            }
            PostInvoke annotation = target.getAnnotation(PostInvoke.class);
            Class<? extends InvokeListener>[] listeners = annotation.listeners();
            for (Class<? extends InvokeListener> listener : listeners) {
                try {
                    listener.newInstance().notify();
                } catch (Throwable t) {
                }
            }
        }
    }
}
