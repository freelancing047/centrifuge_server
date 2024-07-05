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

import csi.shared.gwt.vortex.VortexRequestImpl;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

/**
 * The client-side stub implementation (generated using deferred binding) that stubs extend.
 * @author Centrifuge Systems, Inc.
 */
public class AbstractVortexEnabledStub<R> {

    private VortexFutureSpi<R> vortexFuture;
    private VortexDispatchSender dispatchSender;
    private Map<String, SerializableValueImpl> meta;
    private String resourceUuid = null;


    /**
     * This method is called by the VortexImpl class so that the stub can call the dispatch-sender
     * back with the callback and exception handlers after populating the VortexRequestImpl object.
     * @param callback
     * @param exceptionHandler
     * @param dispatchSender
     * @param dataViewUuid 
     */
    public void setDispatchParameters(VortexFutureSpi<R> vortexFuture,
            Map<String, SerializableValueImpl> meta, VortexDispatchSender dispatchSender) {
        this.vortexFuture = vortexFuture;
        this.dispatchSender = dispatchSender;
        this.meta = meta;
    }
    
    public void setDispatchParameters(VortexFutureSpi<R> vortexFuture,
            Map<String, SerializableValueImpl> meta, VortexDispatchSender dispatchSender, String resourceUuid) {
        setDispatchParameters(vortexFuture, meta, dispatchSender);
        this.resourceUuid = resourceUuid;
    }


    /**
     * Each stubbed method captures the input parameters and calls this to make the RPC call.
     * @param methodSignature
     * @param methodParameters
     */
    protected void dispatchRequestToSender(String methodSignature, SerializableValueImpl[] methodParameters) {
        VortexRequestImpl request = new VortexRequestImpl();
        request.setMethodSignature(methodSignature);
        request.setParameters(methodParameters);
        request.setMeta(meta);
        request.setResourceUuid(resourceUuid);
        dispatchSender.dispatch(request, vortexFuture);
    }
}
