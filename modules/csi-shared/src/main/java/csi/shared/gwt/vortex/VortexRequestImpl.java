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
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public final class VortexRequestImpl implements VortexRequest, IsSerializable {

    private String taskId;
    private String methodSignature;
    private SerializableValueImpl[] parameters;
    private Map<String, SerializableValueImpl> meta;
    private String clientId;
    private String executionPoolId;
    private String resourceUuid = null;

    @Override
    public final String getMethodSignature() {
        return methodSignature;
    }

    public final void setMethodSignature(final String methodSignature) {
        this.methodSignature = methodSignature;
    }

    @Override
    public final SerializableValue[] getParameters() {
        return parameters;
    }

    public final void setParameters(final SerializableValueImpl[] parameters) {
        this.parameters = parameters;
    }

    public final Map<String, ? extends SerializableValue> getMeta() {
        return meta;
    }

    public final void setMeta(final Map<String, SerializableValueImpl> headers) {
        this.meta = headers;
    }

    @Override
    public final void setTaskId(final String taskId) {
        this.taskId = taskId;
    }

    @Override
    public final String getTaskId() {
        return taskId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public void setExecutionPoolId(String uuid) {
        executionPoolId = uuid;
    }

    @Override
    public String getExecutionPoolId() {
        return executionPoolId;
    }

    public String getResourceUuid() {
        return resourceUuid;
    }

    public void setResourceUuid(String resourceUuid) {
        this.resourceUuid = resourceUuid;
    }

}
