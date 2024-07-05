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

import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface VortexRequest extends IsSerializable {

    /**
     * @return The FQCN followed by a dot and method name followed by method parameters. Each non-
     * primitive parameter appears with a FQCN. Primitives and arrays are as they are in source
     * code.
     */
    public String getMethodSignature();

    /**
     * @return Parameters being passed to the above method.
     */
    public SerializableValue[] getParameters();

    /**
     * @return Metadata associated with this request.
     */
    public Map<String, ? extends SerializableValue> getMeta();

    /**
     * Sets the request's associated task id
     * @param taskId
     */
    public void setTaskId(final String taskId);

    /**
     * @return The requests associated task id
     */
    public String getTaskId();

    void setClientId(String clientId);

    String getClientId();

    void setExecutionPoolId(String uuid);

    String getExecutionPoolId();

    public void setResourceUuid(String uuid);
    
    public String getResourceUuid();
}
