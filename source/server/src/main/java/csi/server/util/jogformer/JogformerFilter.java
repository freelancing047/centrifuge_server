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
package csi.server.util.jogformer;

import com.google.gwt.user.client.rpc.SerializationException;

import csi.server.gwt.vortex.VortexFilterChain;
import csi.server.gwt.vortex.VortexInvocationContext;
import csi.server.gwt.vortex.VortexServerFilter;
import csi.shared.gwt.vortex.VortexRequest;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class JogformerFilter implements VortexServerFilter {

    // Don't auto-inject these dependencies to allow for multiple jogformer filters.
    private Jogformer jogformer;

    public Jogformer getJogformer() {
        return jogformer;
    }

    public void setJogformer(Jogformer jogformer) {
        this.jogformer = jogformer;
    }

    @Override
    public <R> R filter(VortexInvocationContext invocationContext, VortexRequest request, VortexFilterChain chain)
            throws SerializationException {
        return jogformer.<R>transform(chain.<R>filter(invocationContext, request));
    }

}
