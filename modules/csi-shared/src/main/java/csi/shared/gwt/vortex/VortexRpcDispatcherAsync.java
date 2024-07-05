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

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

/**
 * Async version of GWT RPC interface.
 * 
 * @author Centrifuge Systems, Inc.
 */
public interface VortexRpcDispatcherAsync {

    void dispatch(VortexRequest action, AsyncCallback<VortexResponse> callback);

    void getMessages(String clientId, AsyncCallback<ArrayList<VortexResponse>> async);
    
    void cancelTask(String taskId, AsyncCallback<Void> async);
}
