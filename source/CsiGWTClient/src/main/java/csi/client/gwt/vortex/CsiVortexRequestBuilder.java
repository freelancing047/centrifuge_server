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

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;

import csi.client.gwt.vortex.impl.VortexSpi;

/**
 * @author Centrifuge Systems, Inc.
 */
public class CsiVortexRequestBuilder extends RpcRequestBuilder {

    private VortexSpi vortexSpi;

    public CsiVortexRequestBuilder(VortexSpi vortex) {
        super();
        vortexSpi = vortex;
    }

    @Override
    protected void doFinish(RequestBuilder rb) {
        super.doFinish(rb);
//        rb.setHeader(Constants.UIConstants.RPC_HEADER_CSI_METHOD, vortexSpi.getCurrentMethodName());
    }
}
