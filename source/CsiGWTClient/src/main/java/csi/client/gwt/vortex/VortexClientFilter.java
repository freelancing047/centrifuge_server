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

import csi.shared.gwt.vortex.SerializableValue;
import csi.shared.gwt.vortex.VortexRequest;

/**
 * A filter for interceptors who want to process RPC requests.
 * 
 * @author Centrifuge Systems, Inc.
 */
public interface VortexClientFilter {

    /**
     * Called before the RPC request is sent.
     * @param request
     * @return true to continue processing, false to abort.
     */
    public boolean onStart(VortexRequest request);


    /**
     * Called when the RPC response has been received.
     * @param success true if the call was successful, false if an exception was thrown.
     * @param request
     * @param response
     */
    public void onEnd(boolean success, VortexRequest request, SerializableValue response);
}
