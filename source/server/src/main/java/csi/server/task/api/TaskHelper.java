package csi.server.task.api;

import java.util.Map;


public class TaskHelper {

    public static TaskController taskController = TaskController.getInstance();
    
    public static void abortTask(String msg) {
        if (taskController.isManagedTask()) {
            taskController.abortTask(msg);
        }
    }

    public static void checkForCancel() {
        if (taskController.isManagedTask()) {
            taskController.checkForCancel();
        }
    }

    public static TaskContext getCurrentContext() {
        if (taskController.isManagedTask()) {
            return taskController.getCurrentContext();
        } else {
            return null;
        }
    }

    public static TaskSession getCurrentSession() {
        if (taskController.isManagedTask()) {
            return taskController.getCurrentSession();
        } else {
            return null;
        }
    }

    public static void reportCancelled() {
        if (taskController.isManagedTask()) {
            taskController.reportCancelled();
        }
    }

    public static void reportFeedback(Map<String, Object> map) {
        if (taskController.isManagedTask()) {
            taskController.reportFeedback(map);
        }
    }

    public static void reportFeedback(String key, Object value) {
        if (taskController.isManagedTask()) {
            taskController.reportFeedback(key, value);
        }
    }

    public static void reportProgress(int percent) {
        if (taskController.isManagedTask()) {
            taskController.reportProgress(percent);
        }
    }

    public static void reportProgress(String label, int percent) {
        if (taskController.isManagedTask()) {
            taskController.reportProgress(label, percent);
        }
    }

    public static void reportWarning(String message) {
        if (taskController.isManagedTask()) {
            taskController.reportWarning(message);
        }
    }

    public static void reportComplete(Object result) {
        if (taskController.isManagedTask()) {
            taskController.reportComplete(result);
        }
    }

    public static void reportError(String message, Throwable t) {
        if (taskController.isManagedTask()) {
            taskController.reportError(message, t);
        }
    }

    public static void reportTaskID() {
        if (taskController.isManagedTask()) {
            TaskContext myContext = taskController.getCurrentContext();
            if (null != myContext) {
                taskController.reportProgress(myContext.getTaskId(), -1);
            }
        }
    }

    public static void reportConflict() {
        if (taskController.isManagedTask()) {
            taskController.reportConflict();
        }
    }
}
