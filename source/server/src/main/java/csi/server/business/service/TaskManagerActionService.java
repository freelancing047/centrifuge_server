package csi.server.business.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import csi.security.CsiSecurityManager;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.TaskManagerActionServiceProtocol;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskStatus;
import csi.server.task.core.TaskGroupId;

@Service(path = "/services/task")
public class TaskManagerActionService extends AbstractService implements TaskManagerActionServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(TaskManagerActionService.class);

    @Override
    public void initMarshaller(XStream xstream) {
        xstream.omitField(TaskContext.class, "method");
        xstream.omitField(TaskContext.class, "methodArgs");
        xstream.omitField(TaskContext.class, "securityToken");
        xstream.omitField(TaskContext.class, "taskSession");
        xstream.omitField(TaskContext.class, "cancelRequested");
        xstream.omitField(TaskContext.class, "parameters");
        xstream.omitField(TaskContext.class, "executingThread");
        xstream.omitField(TaskContext.class, "codec");

    }

    @Operation
    public String cancelTask(@QueryParam("canceledTaskId") String taskId) throws CentrifugeException {
        if (taskId == null) {
            throw new CentrifugeException("Missing parameter taskId");
        }

        TaskContext ctx = TaskController.getInstance().getTaskContext(taskId);

        if (ctx == null) {
           LOG.info("Cannot cancel task: Task not found.");
        } else {
            checkTaskAuthorization(ctx);
            TaskController.getInstance().cancelTask(taskId);
        }
        return taskId;
    }

    @Operation
    public void cancelGroupTasks(@QueryParam("groupTaskId") String groupTaskId, @QueryParam("cancelClientId") String clientId) throws CentrifugeException {
        if (groupTaskId == null) {
            throw new CentrifugeException("Missing parameter groupTaskId");
        }
        if (clientId == null) {
            throw new CentrifugeException("Missing parameter cancelClientId");
        }

        TaskGroupId taskGroupId = new TaskGroupId(groupTaskId);
        // Get the list of eligible TaskContexts for the given taskGroupdId and clientId
        List<TaskContext> taskContexts = TaskController.getInstance().getTaskContexts(taskGroupId, clientId);
        if ((taskContexts != null) && !taskContexts.isEmpty()) {
            // Check security for these taskContext
            checkTasksAuthorization(taskContexts);

            // Cancel those eligible TaskContexts
            TaskController.getInstance().cancelTasks(taskContexts);
        } else {
           LOG.info("Cannot cancel tasks for groupTaskId: " + groupTaskId + ", clientId: " + clientId + ". No tasks were found.");
        }
    }

    @Operation
    public List<TaskContext> listGroupTasks(@QueryParam("groupTaskId") String groupTaskId, @QueryParam("checkClientId") String clientId) throws CentrifugeException {
        if (!CsiSecurityManager.isAdmin()) {
            throw new CentrifugeException("Not authorized to manage tasks.");
        }
        if (groupTaskId == null) {
            throw new CentrifugeException("Missing parameter groupTaskId");
        }
        if (clientId == null) {
            throw new CentrifugeException("Missing parameter checkClientId");
        }

        TaskGroupId taskGroupId = new TaskGroupId(groupTaskId);
        // Get the list of eligible TaskContexts for the given taskGroupdId and clientId
        return TaskController.getInstance().getTaskContexts(taskGroupId, clientId);
    }

    @Operation
    public List<String> listGroupTasksInfo(@QueryParam("groupTaskId") String groupTaskId, @QueryParam("checkClientId") String clientId) throws CentrifugeException {
        List<TaskContext> taskContexts = listGroupTasks(groupTaskId, clientId);
        return TaskController.getInstance().listTasksInfo(taskContexts);
    }

    @Operation
    public TaskContext getTaskContext(@QueryParam("taskId") String taskId) throws CentrifugeException {
        if (taskId == null) {
            throw new CentrifugeException("Missing parameter taskId");
        }

        TaskContext ctx = TaskController.getInstance().getTaskContext(taskId);
        if (ctx == null) {
           LOG.info("Task not found.");
        }
        checkTaskAuthorization(ctx);
        return ctx;
    }

   @Operation
   public TaskStatus getTaskStatus(@QueryParam("taskId") String taskId) throws CentrifugeException {
      if (taskId == null) {
         throw new CentrifugeException("Missing parameter taskId");
      }
      TaskContext ctx = TaskController.getInstance().getTaskContext(taskId);

      if (ctx == null) {
         LOG.info("Task not found.");
      }
      checkTaskAuthorization(ctx);
      return (ctx == null) ? null : ctx.getStatus();
   }

    @Operation
    public List<TaskContext> listTasks() throws CentrifugeException {
        if (!CsiSecurityManager.isAdmin()) {
            throw new CentrifugeException("Not authorized to manage tasks.");
        }

        return TaskController.getInstance().listAllTasks();
    }

    @Operation
    public List<TaskContext> listSessionTasks() {
        String sessionId = TaskController.getInstance().getCurrentSession().getId();
        return TaskController.getInstance().listSessionTasks(sessionId);
    }

    /**
     * Checks if the given non-admin user can securely manage the given TaskContext.
     *
     * @param ctx the TaskContext that needs to be checked
     * @param userName the name of the user the check needs to be perform against
     * @return true if the TaskContext is manageable, false otherwise.
     */
   private boolean isManageableTaskByUser(TaskContext ctx, String userName) {
      return ctx.getSecurityToken().getName().equals(userName);
   }

    private void checkTaskAuthorization(TaskContext ctx) throws CentrifugeException {
        // admin can manage all tasks
        if (!CsiSecurityManager.isAdmin()) {
            String curUser = CsiSecurityManager.getUserName();
            if (!isManageableTaskByUser(ctx, curUser)) {
               LOG.warn("User: " + curUser + " is not authorized to manage task: " + ctx.getTaskId());
                throw new CentrifugeException("Not authorized to manage task: " + ctx.getTaskId());
            }
        }
    }

    private void checkTasksAuthorization(List<TaskContext> taskContexts) throws CentrifugeException {
        assert (taskContexts != null) && !taskContexts.isEmpty() : "taskContexts is null or empty";

        // admin can manage all tasks
        if (!CsiSecurityManager.isAdmin()) {
            List<String> notManagableTasks = new ArrayList<String>();
            String curUser = CsiSecurityManager.getUserName();
            for (TaskContext taskContext : taskContexts) {
                if (!isManageableTaskByUser(taskContext, curUser)) {
                    notManagableTasks.add(taskContext.getTaskId());
                }
            }
            if (!notManagableTasks.isEmpty()) {
               LOG.warn("User: " + curUser + " is not authorized to manage tasks: " + notManagableTasks.toString());
                throw new CentrifugeException("Not authorized to manage tasks: " + notManagableTasks.toString());
            }
        }
    }
}
