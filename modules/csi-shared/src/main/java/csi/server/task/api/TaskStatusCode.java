package csi.server.task.api;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum TaskStatusCode implements IsSerializable{
    TASK_STATUS_NEW, TASK_STATUS_RUNNING, TASK_STATUS_UPDATE, TASK_STATUS_COMPLETE, TASK_STATUS_ERROR, TASK_STATUS_CANCELED, TASK_STATUS_CONFLICT;
}
