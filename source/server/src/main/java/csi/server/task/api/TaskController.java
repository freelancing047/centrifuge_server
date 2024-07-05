package csi.server.task.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.codec.Codec;
import csi.server.common.codec.NullWriter;
import csi.server.common.exception.CentrifugeException;
import csi.server.message.MessageBroker;
import csi.server.task.core.StatusUpdateListener;
import csi.server.task.core.TaskGroupId;
import csi.server.task.core.processor.PooledResultProcessor;
import csi.server.task.core.processor.PooledTaskProcessor;
import csi.server.task.core.processor.TaskProcessor;
import csi.server.task.core.worker.ReaperTask;
import csi.server.task.core.worker.ResultHandlerTask;
import csi.server.task.exception.DuplicateTaskException;
import csi.server.task.exception.TaskAbortedException;
import csi.server.task.exception.TaskCancelledException;
import csi.server.task.exception.TaskException;
import csi.server.util.CsiUtil;
import csi.shared.gwt.vortex.VortexResponse;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

/**
 * Controlls the entities responsible with tasks execution and results harvesting.
 *
 * @author dorel.matei
 */
public class TaskController {
   private static final Logger LOG = LogManager.getLogger(TaskController.class);

    private TaskProcessor taskProcessor;

    private PooledResultProcessor resultProcessor;

    private ReaperTask reaperTask;

    /**
     * Keep a list of all submitted tasks so we can lookup later build a task management console to view all running
     * tasks and their statuses. Tasks that complete should remain in this map for X amount of time. This gives us the
     * ability to see the final status in the manangement console for a little while after the task has completed.
     */
    private ConcurrentHashMap<String, TaskContext> managedTasks = new ConcurrentHashMap<String, TaskContext>();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, TaskSession>> managedSessionByUser =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, TaskSession>>();

    /**
     * Linked list of task contexts that have been updated.
     */
    private ConcurrentHashMap<String, LinkedBlockingQueue<TaskContext>> updateQueues = new ConcurrentHashMap<String, LinkedBlockingQueue<TaskContext>>();

    private List<StatusUpdateListener> updateListeners = new ArrayList<StatusUpdateListener>();

    private static TaskController taskController = new TaskController();

    public static TaskController getInstance() {
        return taskController;
    }

    private TaskController() {
        taskProcessor = new PooledTaskProcessor();
        taskProcessor.startProcessing();

        resultProcessor = new PooledResultProcessor();
        resultProcessor.startProcessing();
        updateListeners.add(resultProcessor);

        reaperTask = new ReaperTask();
        Thread reaperThread = new Thread(reaperTask, "ReaperTask");
        reaperThread.setDaemon(true);
        reaperThread.start();
    }


    /**
     * Delegates a task for execution to corresponding <code>TaskProcessor</code>
     */
    public void submitTask(TaskContext taskContext) throws TaskException {
        registerTask(taskContext);
        if (taskContext.isSynchronous()) {
            taskProcessor.processTaskSynchronously(taskContext);
        } else {
            taskProcessor.processTaskAsynchronously(taskContext);
        }
    }

    private LinkedBlockingQueue<TaskContext> getClientQueue(String clientId) {
        LinkedBlockingQueue<TaskContext> queue = updateQueues.get(clientId);
        if (queue == null) {
            // ensure that a queue exists
            LinkedBlockingQueue<TaskContext> newQ = new LinkedBlockingQueue<TaskContext>();
            queue = updateQueues.putIfAbsent(clientId, newQ);

            if (queue == null) {
                queue = newQ;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("createIfAbsentClientQueue: NEW Queue created for clientId: " + clientId);
                }
            }
        }
        return queue;
    }

    public void registerTask(TaskContext taskContext) throws TaskException {
        // don't add task admin requests to the managed task queue
        if (!taskContext.isAdminTask()) {
            String taskId = taskContext.getTaskId();
            TaskContext existingCtx = managedTasks.putIfAbsent(taskId, taskContext);
            if (existingCtx != null) {
                throw new DuplicateTaskException("Duplicate task id: " + taskId + ".  thread:" + Thread.currentThread());
            } else {
                LOG.debug("registered task: " + taskId + " url:" + taskContext.getRequestURL() + " thread:" + Thread.currentThread() + " groupId: " + taskContext.getTaskGroupId());// , new Exception());
            }
        }
        String user = taskContext.getSecurityToken().getName();
        ConcurrentHashMap<String, TaskSession> userSessions = managedSessionByUser.get(user);
        if (userSessions == null) {
            userSessions = new ConcurrentHashMap<String, TaskSession>();
            managedSessionByUser.put(user, userSessions);
        }
        String sessionId = taskContext.getSessionId();
        if (!userSessions.containsKey(sessionId)) {
            userSessions.put(sessionId, taskContext.getTaskSession());
        }
        if (taskContext.isGwtService()) {
            taskProcessor.getExecutor().setCurrentContext(taskContext);
        }
    }

    /**
     * Requests that the task be canceled. It is possible that the task is chooses not to stop or that the task will
     * completed before it's next check for the cancel request. It is up to the task as to whether or not to ignore the
     * cancellation request.
     * <p/>
     * A task should check for this via TaskManger.isCancelled() at the appropriate places during processing and
     * gracefully clean up and terminate it's processing. Upon sucessfully cancellation of its processing, the task
     * should call TaskManger.reportCancelled().
     * <p/>
     *
     * @param taskId
     */
    public void cancelTask(String taskId) {
        // Gracefully cancel
        TaskContext taskContext = managedTasks.get(taskId);
        if (taskContext != null) {
            TaskStatus status = taskContext.getStatus();
            synchronized (status) {
                if (!isTerminalStatus(status)) {
                    LOG.debug(" +++ Cancelling task: " + taskId + ", method: " + taskContext.getMethodName());
                    taskContext.cancel();
                    this.reportCancelled(taskContext);
                } else {
                    LOG.debug(" +++ Cannot gracefully cancel task: " + taskId + " because the task status is: " + status.getTaskStatus());
                }
            }
        } else {
            LOG.debug(" +++ Cannot gracefully cancel task: " + taskId + " because the task doesn't exist.");
        }
    }

    /**
     * Cancels all tasks that corresponds to the given <code>taskContexts</code>.
     *
     * @param taskContexts the TaskContext list for which the cancel operation needs to be performed
     */
    public void cancelTasks(List<TaskContext> taskContexts) {
        assert (taskContexts != null) && !taskContexts.isEmpty() : "taskContexts is null or empty";

        LOG.debug(" +++ Started cancelling " + taskContexts.size() + " tasks.");

        for (TaskContext taskContext : taskContexts) {
            cancelTask(taskContext.getTaskId());
        }
        LOG.debug(" +++ Finished cancelling " + taskContexts.size() + " tasks.");
    }

    /**
     * Get the list of TaskContext that corresponds to the given <code>taskGroupdId</code> and <code>clientId</code>.
     *
     * @param taskGroupId the TaskGroupId instance criteria
     * @param clientId the clientId string criteria
     * @return a List of TaskContext instances.
     */
    public List<TaskContext> getTaskContexts(TaskGroupId taskGroupId, String clientId) {
        if ((taskGroupId == null) || (clientId == null)) {
            return Collections.emptyList();
        }
        List<TaskContext> taskContexts = new ArrayList<TaskContext>();
        Iterator<Map.Entry<String, TaskContext>> it = managedTasks.entrySet().iterator();
        TaskContext taskContext = null;
        Map.Entry<String, TaskContext> entry = null;
        while (it.hasNext()) {
            entry = it.next();
            taskContext = entry.getValue();
            //check if the task belongs to this client and if it is part of the given taskGroupdId
            if (clientId.equals(taskContext.getClientId()) && taskGroupId.includes(taskContext.getTaskGroupId())) {
                taskContexts.add(taskContext);
            }
        }
        return taskContexts;
    }

    public TaskContext getTaskContext(String taskId) {
        return managedTasks.get(taskId);
    }

    public List<TaskContext> listAllTasks() {
        List<TaskContext> tasks = new ArrayList<TaskContext>();
        tasks.addAll(managedTasks.values());

        // don't include the task that's asking for the list of tasks
        if (taskProcessor.getCurrentContext() != null) {
            tasks.remove(taskProcessor.getCurrentContext());
        }

        return tasks;
    }

    public List<TaskContext> listSessionTasks(String sessionId) {
        List<TaskContext> tasks = new ArrayList<TaskContext>();
        Iterator<TaskContext> iter = managedTasks.values().iterator();
        while (iter.hasNext()) {
            TaskContext tc = iter.next();
            if (tc.getSessionId().equalsIgnoreCase(sessionId) && (tc != taskProcessor.getCurrentContext())) {
                tasks.add(tc);
            }
        }
        return tasks;
    }

    public List<String> listTasksInfo(List<TaskContext> taskContexts) {
        List<String> infoList = new ArrayList<String>();

        int newly = 0, running = 0, update = 0, complete = 0, error = 0, canceled = 0;
        for (TaskContext taskContext : taskContexts) {
            if (taskContext.getStatus().getTaskStatus() == TaskStatusCode.TASK_STATUS_NEW) {
                newly++;
            } else if (taskContext.getStatus().getTaskStatus() == TaskStatusCode.TASK_STATUS_RUNNING) {
                running++;
            } else if (taskContext.getStatus().getTaskStatus() == TaskStatusCode.TASK_STATUS_UPDATE) {
                update++;
            } else if (taskContext.getStatus().getTaskStatus() == TaskStatusCode.TASK_STATUS_COMPLETE) {
                complete++;
            } else if (taskContext.getStatus().getTaskStatus() == TaskStatusCode.TASK_STATUS_ERROR) {
                error++;
            } else if (taskContext.getStatus().getTaskStatus() == TaskStatusCode.TASK_STATUS_CANCELED) {
                canceled++;
            }
        }
        infoList.add(TaskStatusCode.TASK_STATUS_NEW + ": " + newly);
        infoList.add(TaskStatusCode.TASK_STATUS_RUNNING + ": " + running);
        infoList.add(TaskStatusCode.TASK_STATUS_UPDATE + ": " + update);
        infoList.add(TaskStatusCode.TASK_STATUS_COMPLETE + ": " + complete);
        infoList.add(TaskStatusCode.TASK_STATUS_ERROR + ": " + error);
        infoList.add(TaskStatusCode.TASK_STATUS_CANCELED + ": " + canceled);
        return infoList;
    }

    public void removeOldTerminalTasks(long timeInterval) {
        LOG.debug(" +++ Remove old tasks started...");
        Iterator<Map.Entry<String, TaskContext>> it = managedTasks.entrySet().iterator();
        int removedcnt = 0;
        while (it.hasNext()) {
            Map.Entry<String, TaskContext> entry = it.next();
            TaskContext taskContext = entry.getValue();
            if (isTerminalStatus(taskContext.getStatus())) {
                Date lastUpdate = taskContext.getStatus().getLastUpdate();
                if ((lastUpdate != null) && ((System.currentTimeMillis() - lastUpdate.getTime()) > timeInterval)) {
                    it.remove();
                    removedcnt++;
                }
            }
        }

        LOG.debug("Reaped " + removedcnt + " tasks");
    }

    /**
     * Waits on the update queue until an update task is put on the queue. The caller should synchronize on the
     * TaskStatus before doing serializing it since it's content may still be changing.
     * <p/>
     * Note that even when the TaskStatus is synchronized, the progressData and warnings may still be changing. The
     * status and result object are guaranteed to be unchanged within a synchronize block.
     * <p/>
     * To avoid unnecessary sending of duplicate progress statuses, all instances of the retrieved task is removed from
     * the update queue.
     * <p/>
     * It is still possible to have a duplicate status progress update. However, there should never duplicates once the
     * task has reported a terminal status (COMPLETE, ERROR, CANCELLED). This is enforced by the reportXXX methods.
     *
     * @param clientId
     * @param timeout - max amount of time in millis to wait.  A value of zero will return immediately.  A value of -1
     *                  will wait indefinitely
     * @return
     * @throws InterruptedException
     */
    public TaskContext waitForUpdate(String clientId, long timeout) throws InterruptedException {

        LinkedBlockingQueue<TaskContext> updateClientQueue = getClientQueue(clientId);

        TaskContext ctx = null;
        if (timeout == -1) {
            // wait forever till there's an update
            ctx = updateClientQueue.take();
        } else if (timeout == 0) {
            // remove update without any wait
            ctx = updateClientQueue.poll();
        } else {
            // wait for next update with timeout
            ctx = updateClientQueue.poll(timeout, TimeUnit.MILLISECONDS);
        }

        return ctx;
    }

    /**
     * Queue an task context that's been updated onto the updatequeue. The status for each task are allowed to be
     * updated while they are in the queue.
     * <p/>
     * Multiple entries of the same instances are allowed on the queue. However, when an entry is taken of the queue an
     * attempt is made to delete any duplicates in the queue. This is so running tasks are not slowed down when they try
     * to update their status.
     * <p/>
     * We may need to synchronize the update queue, but for now this should work.
     *
     * @param task
     */
    public void queueUpdate(TaskContext task) {
        if (task.isSynchronous()) {
            if (isTerminalStatus(task.getStatus())) {

                //TODO this task should not be sent from here
                ResultHandlerTask.sendSynchronous(task);
            }
            return;
        }
        if (!task.isGwtService()) {
            // HACK: do this to force jpa to be fully materialize
            // lazy load collections so the response thread can
            // properly serialize the object to the response
            TaskStatus status = task.getStatus();
            if (status.getResultData() != null) {
                Codec codec = task.getCodec();
                if(codec != null) {
                  codec.marshal(status.getResultData(), new NullWriter());
               }
            }
        }
        try {
            if (task.isGwtService() && TaskStatusCode.TASK_STATUS_COMPLETE.equals(task.getStatus().getTaskStatus())) {
                final VortexResponse vortexResponse = new VortexResponse();
                final Object myTaskResultData = task.getStatus().getResultData();
                SerializableValueImpl myValue = new SerializableValueImpl();
                vortexResponse.setTaskStatus(task.getStatus().getTaskStatus());
                myValue.setValue(myTaskResultData);
                vortexResponse.setResponse(myValue);
                vortexResponse.setTaskId(task.getTaskId());
                MessageBroker.get().publishObject(task.getClientId(), vortexResponse);
            }
            if (task.isGwtService() && (TaskStatusCode.TASK_STATUS_UPDATE == task.getStatus().getTaskStatus())) {
                final VortexResponse vortexResponse = new VortexResponse();
                vortexResponse.setTaskProgess(task.getStatus().getProgress());
                vortexResponse.setTaskMessage(task.getStatus().getProgressLabel());
                vortexResponse.setTaskStatus(task.getStatus().getTaskStatus());
                vortexResponse.setTaskId(task.getTaskId());
                MessageBroker.get().publishObject(task.getClientId(), vortexResponse);
            }
            if (task.isGwtService() && (TaskStatusCode.TASK_STATUS_ERROR == task.getStatus().getTaskStatus())) {
                final VortexResponse vortexResponse = new VortexResponse();
                vortexResponse.setTaskStatus(task.getStatus().getTaskStatus());
                vortexResponse.setException(new CentrifugeException(task.getStatus().getErrorMessage(), task.getStatus().getException()));
                vortexResponse.setTaskId(task.getTaskId());
                MessageBroker.get().publishObject(task.getClientId(), vortexResponse);
            }
            if (task.isGwtService() && (TaskStatusCode.TASK_STATUS_CANCELED == task.getStatus().getTaskStatus())) {
                final VortexResponse vortexResponse = new VortexResponse();
                vortexResponse.setTaskStatus(task.getStatus().getTaskStatus());
                vortexResponse.setTaskId(task.getTaskId());
                MessageBroker.get().publishObject(task.getClientId(), vortexResponse);
            }
            if (task.isGwtService() && (TaskStatusCode.TASK_STATUS_CONFLICT == task.getStatus().getTaskStatus())) {
                final VortexResponse vortexResponse = new VortexResponse();
                vortexResponse.setTaskStatus(task.getStatus().getTaskStatus());
                vortexResponse.setTaskId(task.getTaskId());
                vortexResponse.setTaskMessage(task.getResourceUuid());
                MessageBroker.get().publishObject(task.getClientId(), vortexResponse);
            }
            /*LinkedBlockingQueue<TaskContext> updateClientQueue = */getClientQueue(task.getClientId());
//            updateClientQueue.put(task);
            notifyUpdateListeners();
        } catch (Exception e) {
            LOG.trace("Unable to queue status update", e);
        }
    }

    /**
     * Retrieve the current task context for the caller's thread. This method is used by the reportXXX methods to allow
     * a running thread to update it's status at anytime without the burden of having to pass the task context all over
     * the place.
     * <p/>
     * This will throw a runtime IllegalStateException if called from a thread that is not managed by the TaskManager.
     *
     * @return
     */
    public TaskContext getCurrentContext() {
        TaskContext ctx = taskProcessor.getCurrentContext();
        if (ctx == null) {
            throw new IllegalStateException("Task context does not exist on calling thread.");
        }
        return ctx;
    }

    public TaskSession getCurrentSession() {
        TaskContext ctx = taskProcessor.getCurrentContext();
        if (ctx == null) {
            throw new IllegalStateException("Task context does not exist on calling thread.");
        }
        return ctx.getTaskSession();
    }

    /**
     * Reports that the current task was sucessfully cancelled
     */
    public void reportCancelled() {
        TaskContext ctx = getCurrentContext();
        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_CANCELED);
                status.setLastUpdate(new Date());
                status.setResultData("");
                queueUpdate(ctx);
            }
        }
    }

    /**
     * Reports that the current task had a conflicting dataview
     * @param conflictResourceUuid
     */
    public void reportConflict() {
        TaskContext ctx = getCurrentContext();
        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_CONFLICT);
                status.setLastUpdate(new Date());
                status.setResultData("");
                queueUpdate(ctx);
            }
        }
    }

    /**
     * Reports that the indicated task was sucessfully cancelled
     */
    public void reportCancelled(TaskContext ctx) {
        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_CANCELED);
                status.setResultData("");
                status.setProgress(100);
                status.setLastUpdate(new Date());
                queueUpdate(ctx);
            }
        }
    }

    /**
     * Reports that the current task was successfully completed
     */
    public void reportComplete(Object result) {
        TaskContext ctx = getCurrentContext();
        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_COMPLETE);
                status.setResultData(result);
                status.setProgress(100);
                status.setLastUpdate(new Date());
                queueUpdate(ctx);
            }
        }
    }

    /**
     * Reports that the current task encoutered an error
     */
    public void reportError(String message, Throwable t) {
        TaskContext ctx = getCurrentContext();
        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_ERROR);
                if (message == null) {
                    if (null != t) {
                        message = t.getMessage();
                    }
                }
                if (message == null) {
                    message = "";
                }
                if (null == status.getException()) {

                    status.setException(t);
                    status.setErrorDetail(CsiUtil.getStackTraceString(t));
                }
                if ((null == status.getErrorMessage()) || (0 == status.getErrorMessage().length())) {

                    status.setErrorMessage(message);
                }
                status.setLastUpdate(new Date());
                queueUpdate(ctx);
            }
        }
    }

    /**
     * Reports an intermediate progress of the current task
     */
    public void reportProgress(int percent) {
        reportProgress(null, percent);
    }

    /**
     * Reports an intermediate progress of the current task
     */
    public void reportProgress(String label, int percent) {
        TaskContext ctx = getCurrentContext();

        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_UPDATE);
                if (label != null) {
                    status.setProgressLabel(label);
                }
                status.setProgress(percent);
                status.setLastUpdate(new Date());
                queueUpdate(ctx);
            }
        }
    }

    public void reportFeedback(String key, Object value) {
        TaskContext ctx = getCurrentContext();

        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_UPDATE);
                status.getFeedbackData().put(key, value);
                status.setLastUpdate(new Date());
                queueUpdate(ctx);
            }
        }
    }

    public void reportFeedback(Map<String, Object> map) {
        TaskContext ctx = getCurrentContext();

        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_UPDATE);
                status.getFeedbackData().putAll(map);
                status.setLastUpdate(new Date());
                queueUpdate(ctx);
            }
        }
    }

    public void reportWarning(String message) {
        TaskContext ctx = getCurrentContext();

        TaskStatus status = ctx.getStatus();
        synchronized (status) {
            if (!isTerminalStatus(status)) {
                status.setTaskStatus(TaskStatusCode.TASK_STATUS_UPDATE);
                status.getWarnings().add(message);
                status.setLastUpdate(new Date());
                queueUpdate(ctx);
            }
        }
    }

    private boolean isTerminalStatus(TaskStatus status) {
        return ((TaskStatusCode.TASK_STATUS_CANCELED == status.getTaskStatus()) || (TaskStatusCode.TASK_STATUS_COMPLETE == status.getTaskStatus()) || (TaskStatusCode.TASK_STATUS_ERROR == status
                .getTaskStatus()));
    }

    /**
     * Clear queue of all duplicates that were in the queue
     * while the status was sent.
     *
     * Needs to be called in a synchronized context on TaskContext.getStatus().
     *
     * @param ctx
     */
    public void clearDuplicates2(TaskContext ctx) {
        // cleanup queue from possible duplicate instances
        LOG.debug("clearDuplicates: removing taskId: " + ctx.getTaskId());
        List<TaskContext> list = new ArrayList<TaskContext>(1);
        list.add(ctx);
        LinkedBlockingQueue<TaskContext> updateClientQueue = getClientQueue(ctx.getClientId());
        updateClientQueue.removeAll(list);
    }

    /**
     * This should be invoked whenever the Task's session is terminated (i.e. logout, session timeout, etc).
     * <p/>
     * This method should clean up all tasks currently executing or queued for the given sessionId. Executing task
     * should be cancelled and queue tasks should be removed.
     *
     * @param sessionId
     */
    public void cleanSessionTasks(String sessionId) {
        Iterator<TaskContext> iter = this.managedTasks.values().iterator();
        boolean sessionCleared = false;
        while (iter.hasNext()) {
            TaskContext tc = iter.next();
            if (sessionId.equals(tc.getSessionId())) {
                tc.cancel();
                managedTasks.remove(tc.getClientId());
                LinkedBlockingQueue<TaskContext> queue = updateQueues.remove(tc.getClientId());
                if (queue != null) {
                    queue.clear();
                }
                if (!sessionCleared) {
                    String user = tc.getSecurityToken().getName();
                    ConcurrentHashMap<String, TaskSession> userSessions = managedSessionByUser.get(user);
                    if (userSessions != null) {
                        userSessions.remove(sessionId);
                        if (userSessions.isEmpty()) {
                            managedSessionByUser.remove(user);
                        }
                    }
                    sessionCleared = true;
                }
            }
        }
    }

    public List<TaskSession> getUserSessions() {
        List<TaskSession> sessions = new ArrayList<TaskSession>();
        List<TaskSession> invalidSessions = new ArrayList<TaskSession>();
        String user = getCurrentContext().getSecurityToken().getName();
        ConcurrentHashMap<String, TaskSession> userSessions = managedSessionByUser.get(user);
        if (userSessions != null) {
            for (TaskSession taskSession : userSessions.values()) {
                if (taskSession.isValidSession()) {
                    sessions.add(taskSession);
                } else {
                    invalidSessions.add(taskSession);
                }
            }
            for (TaskSession taskSession : invalidSessions) {
                LOG.warn("Removing invalid user session, Id=" + taskSession.getId());
                userSessions.remove(taskSession.getId());
                if (userSessions.isEmpty()) {
                    managedSessionByUser.remove(user);
                    break;
                }
            }
        }
        return sessions;
    }

    public void shutdown() {
        taskProcessor.stopProcessing();
        resultProcessor.stopProcessing();
        updateListeners = Collections.emptyList();
        if (reaperTask != null) {
            reaperTask.setStopped(true);
        }
    }

    private void notifyUpdateListeners() {
        for (StatusUpdateListener listener : updateListeners) {
            listener.notifyUpdate();
        }
    }

    public void addUpdateListener(StatusUpdateListener listener) {
        this.updateListeners.add(listener);
    }

    public void removeUpdateListener(StatusUpdateListener listener) {
        this.updateListeners.remove(listener);
    }

    public boolean clientHasStatus(String clientId) {
        return getClientQueue(clientId).peek() != null;
    }

    /**
     * Convenience method to abort the current running task.
     *
     * It simply throws the runtime TaskAbortedException so
     * the WorkerTask can report the appropriate status to the client
     *
     * TODO: add an abort status and update client to handle it.
     * For now we'll overload the cancel status so the client.
     */
    public void abortTask(String msg) {
        throw new TaskAbortedException(msg);
    }

    /**
     * Convenience method to test if the current task has been cancelled.
     *
     *if it has been cancelled a runtime exception TaskCancelledException will be thrown.
     * If a service operation needs to clean up resources, it should either catch the
     * TaskCancelledException or cleanup via a finally clause as necessary
     *
     * If the service catches the TaskCancelledException, it should re-throw the exception.
     *
     * @throws TaskCancelledException
     */
    public void checkForCancel() {
        TaskContext ctx = getCurrentContext();
        if (ctx.isCancelled()) {
            throw new TaskCancelledException();
        }
    }

    public boolean isManagedTask() {
        return (taskProcessor.getCurrentContext() != null);
    }

    public void setCurrentContext(TaskContext taskContext) {
        taskProcessor.getExecutor().setCurrentContext(taskContext);
    }
}