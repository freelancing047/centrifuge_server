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
package csi.client.gwt.vortex.impl;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.ExceptionHandler;
import csi.shared.gwt.vortex.VortexService;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

/**
 * This is purely to allow the shell to call the VortexImpl without being aware of the implementation class.
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VortexSpi {

    // Call interface for the shell to invoke calls routed through it.
    public <E extends VortexService, R> E execute(Map<String, SerializableValueImpl> meta,
            final ExceptionHandler handler, final Callback<R> callback, Class<E> clz);
    
    // Call interface for vortex to be able to execute 
    public <V extends VortexService, R> V executeForFuture(Map<String, SerializableValueImpl> meta,
            VortexFutureSpi<R> vortexFuture, Class<V> clz);
    

    public <V extends VortexService, R> V executeForFuture(Map<String, SerializableValueImpl> meta,
            VortexFutureSpi<R> vortexFuture, Class<V> clz, String dvUuid);

    public String getCurrentMethodName();
    
    public void cancel(String taskId, AsyncCallback<Void> callback);


}
