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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VortexServiceProvider {

    /**
     * @param url RPC entry point URL (the url that the RPC servlet is listening to relative to 
     * the context path).
     */
    public void setRPCServiceEntryPointURL(String url);


    /**
     * Add one or more filters to the RPC call chain.
     * @param filter
     */
    public void addRPCServiceFilter(VortexClientFilter filter);
}
