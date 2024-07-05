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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gwt.user.client.rpc.SerializationException;

import csi.shared.gwt.vortex.VortexRequest;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VortexFilterChainImpl implements VortexFilterChain {
   private static final Logger LOG = LogManager.getLogger(VortexFilterChainImpl.class);

    private List<VortexServerFilter> filters;

    public List<VortexServerFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<VortexServerFilter> filters) {
        this.filters = filters;
    }

    private int currentFilterIndex = 0;

    @Override
    public <R> R filter(VortexInvocationContext invocationContext, VortexRequest request) throws SerializationException {
        if (currentFilterIndex == getFilters().size()) {
            return null;
        } else {
            VortexServerFilter filter = getFilters().get(currentFilterIndex++);

            LOG.trace("Before " + filter.getClass());
            R response = filter.<R> filter(invocationContext, request, this);
            LOG.trace("After " + filter.getClass());
            return response;
        }
    }

}
