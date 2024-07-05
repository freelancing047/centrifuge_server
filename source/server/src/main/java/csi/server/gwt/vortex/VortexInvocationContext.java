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

import java.lang.reflect.Method;

import csi.shared.gwt.vortex.VortexService;

/**
 * Holds a reference to the interface and method that can be called via RPC and the implementatng 
 * bean that can handle the call.
 * @author Centrifuge Systems, Inc.
 */
public class VortexInvocationContext {

    private Class<? extends VortexService> serviceInterface;

    public Class<? extends VortexService> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<? extends VortexService> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    private VortexService serviceImplementation;

    public VortexService getServiceImplementation() {
        return serviceImplementation;
    }

    public void setServiceImplementation(VortexService serviceImplementation) {
        this.serviceImplementation = serviceImplementation;
    }

    private Method method;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
