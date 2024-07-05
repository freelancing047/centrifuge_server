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

import java.io.Serializable;

import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface Vortex {

    /**
     * Sets up execution context
     * @param name Name of the header
     * @param value Value of the header
     * @return Self-reference for fluent interface.
     */
    public Vortex withMeta(String name, Serializable value);

    public <R> VortexFuture<R> createFuture();

    /**
     * Execute the RPC call against the given Vortex enabled service class without regard for the 
     * return value of the call. Use this when you want to call a void method or you don't care 
     * about the return value.
     * @param clz The class corresponding to the VortexService for the RPC.
     * @return The RPC enabled service against which a method can be called.
     */
    public <V extends VortexService> V execute(Class<V> clz);

    /**
     * Similar to the execute() method above except this method accepts a callback that will be 
     * invoked when the asynchronous call has completed. The callback's onSuccess method gets called
     * if the RPC executed successfully. The parameter to the onSuccess method is the return value.
     * @param callback A callback to be executed on success.
     * @param clz The class corresponding to the VortexService for the RPC.
     * @return
     */
    public <V extends VortexService, R> V execute(Callback<R> callback, Class<V> clz);

    /**
     * Similar to the execute() method above except that this method also allows the specification 
     * of an exception handler. The exception handler should return true if "default" exception 
     * handling should continue.
     * @param handler The Exception handler.
     * @param callback A callback to be executed on success.
     * @param clz: The class corresponding to the VortexService for the RPC.
     * @return
     */
    public <V extends VortexService, R> V execute(ExceptionHandler handler, Callback<R> callback, Class<V> clz);
}
