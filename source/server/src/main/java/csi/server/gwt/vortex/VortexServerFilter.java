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
package csi.server.gwt.vortex;

import com.google.gwt.user.client.rpc.SerializationException;

import csi.shared.gwt.vortex.VortexRequest;

/**
 * Interface that filters (or interceptors) to the RPC request should implement and register via
 * the vortex-web.xml spring file.
 * 
 * @author Centrifuge Systems, Inc.
 */
public interface VortexServerFilter {

    /**
     * Just as with ServletFilter, call the chain to continue processing. You can add before/after
     * processing around the chain call.
     * @param invocationContext
     * @param request
     * @param chain
     * @return
     * @throws SerializationException
     */
    public <R> R filter(VortexInvocationContext invocationContext, VortexRequest request,
            VortexFilterChain chain) throws SerializationException;
}
