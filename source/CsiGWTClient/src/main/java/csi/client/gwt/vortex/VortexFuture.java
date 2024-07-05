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
package csi.client.gwt.vortex;

import com.google.gwt.user.client.rpc.AsyncCallback;

import csi.shared.gwt.vortex.VortexResponse;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VortexFuture<R> {

    public void addEventHandler(VortexEventHandler<R> eventHandler);

    /**
     * @param clz VortexService derivative (interface that implements VortexService that has the business methods
     * you want to execute).
     * @return Vortex service instance. When you invoke the method, upon execution, the event handlers will be called.
     */
    public <V extends VortexService> V execute(Class<V> clz);

    public <V extends VortexService> V execute(Class<V> clz, String dvUuid);

    /**
     * Calls onSuccess on the vortex event handlers. Clients should typically not call this. This is called by the 
     * service stub on completion of the RPC call. However, in cases where the caller wants to simulate a return result
     * and not make an RPC to the server (e.g. cache the data locally or send back a mocked value), this method can be
     * called.
     * @param result
     */
    public void fireSuccess(R result);

    /**
     * Calls the onFailure of the vortex event handlers. See documentation for fireSuccess for client scenarios. 
     * @param t
     * @return
     */
    public boolean fireFailure(Throwable t);

    public void fireUpdate(VortexResponse response);
    
    public void fireCancel();
    
    public void setTaskId(String taskId);
    
    public void cancel(AsyncCallback<Void> callback);
}
