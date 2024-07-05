/**
 * Copyright 2013 Centrifuge Systems, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package csi.shared.gwt.vortex;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.task.api.TaskStatusCode;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VortexResponse implements IsSerializable, Serializable {

    private Throwable _exception;
    private SerializableValueImpl _response;
    private String taskId;
    private TaskStatusCode taskStatus;
    private String taskMessage;
    private String user;
    private int taskProgess;

    public boolean hasException() {
        return (null != _exception);
    }

    public Throwable getException() {
        return _exception;
    }

    public void setException(Throwable exceptionIn) {
        if (null != exceptionIn) {
            String myMessage = exceptionIn.getMessage();
            StackTraceElement[] myStackTrace = exceptionIn.getStackTrace();
            
            _exception = new Throwable(myMessage);
            _exception.setStackTrace(myStackTrace);
        }
    }

    public SerializableValue getResponse() {
        return _response;
    }

    public void setResponse(SerializableValueImpl responseIn) {
        _response = responseIn;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUser() { return user; }

    public void setUser(String user) { this.user = user; }

    public TaskStatusCode getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatusCode taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setTaskMessage(String taskMessage) {
        this.taskMessage = taskMessage;
    }

    public String getTaskMessage() {
        return taskMessage;
    }

    public void setTaskProgess(int taskProgess) {
        this.taskProgess = taskProgess;
    }

    public int getTaskProgess() {
        return taskProgess;
    }

}
