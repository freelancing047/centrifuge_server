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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;

import java.util.ArrayList;

/**
 * The actual GWT RPC interface that all method calls are routed through.
 * @author Centrifuge Systems, Inc.
 */
public interface VortexRpcDispatcher extends RemoteService {

	/**
	 * A note on why VortexRequestImpl as opposed to VortexRequest (the interface): GWT does not like
	 * abstract types in the RPC service interface as it cannot figure out what to instantiate 
	 * behind the scenes for it. Therefore for this internal interface, we use the -Impl class 
	 * while only exposing VortexRequest to client-api (filters).
	 * @param request
	 * @return
	 * @throws SerializationException
	 */
    public VortexResponse dispatch(VortexRequest request) throws SerializationException;

    public ArrayList<VortexResponse> getMessages(String clientId) throws SerializationException;

	public void cancelTask(String taskId) throws SerializationException;
}
