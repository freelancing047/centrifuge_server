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

import csi.shared.gwt.vortex.VortexService;

/**
 * This is a workaround for the fact that GWT.create() cannot take a dynamic class as a paramter. The parameter has
 * to be a static class. The stub-instantiator works around that by creating the ability to instantiate bounded 
 * (T extends VortexService) classes using deferred binding.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VortexServiceStubFactory {

    public <V extends VortexService> V create(Class<V> clz);
}
