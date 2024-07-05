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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What JOGformer? Jogformer is a Java Object Graph transformer. It navigates an object and its relationships to 
 * perform in-place transformations.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class Jogformer {

    // This instance is set when a new instance of the Jogformer is passed to the navigator. This prevents a new
    // trasform map from being created in the transform method and allows the main class to be multi-thread safe.
    private Map<ObjectIdentityWrapper, Object> portableTransformMap;

    // Don't auto-inject dependencies to allow for multiple instances of Jogformer with their own transformer factories.
    private TransformerFactory transformerFactory;

    public TransformerFactory getTransformerFactory() {
        return transformerFactory;
    }

    public void setTransformerFactory(TransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    // Don't auto-inject dependencies to allow for multiple instances of Jogformer with their own transformer factories.
    private NavigatorFactory navigatorFactory;

    public NavigatorFactory getNavigatorFactory() {
        return navigatorFactory;
    }

    public void setNavigatorFactory(NavigatorFactory navigatorFactory) {
        this.navigatorFactory = navigatorFactory;
    }

    public <T> T transform(T instance) {
        return transform(instance, portableTransformMap == null ? new HashMap<ObjectIdentityWrapper, Object>()
                : portableTransformMap);
    }

    /**
     * @param instance
     * @param transformMap The transform map holds a mapping of pre-transformed object to transformed object. The
     * pre-transformed object is wrapped in an object-identity-wrapper so that the hash and equality check done by the
     * map is done against the raw memory reference (i.e., a.equals(b) only if a == b) as opposed to the actual object's 
     * identity.
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T transform(T instance, Map<ObjectIdentityWrapper, Object> transformMap) {
        if (instance == null) {
            return instance;
        }

        ObjectIdentityWrapper instanceWrapper = new ObjectIdentityWrapper();
        instanceWrapper.setReference(instance);
        T transformed = (T) transformMap.get(instanceWrapper);

        if (transformed == null) {
            transformed = instance;

            List<Transformer> transformers = getTransformerFactory().getTransformersFor(instance.getClass());
            for (Transformer transformer : transformers) {
                transformed = transformer.transform(transformed);
            }
            transformMap.put(instanceWrapper, transformed);

            // Navigate the relationships.
            Navigator navigator = getNavigatorFactory().getNavigatorFor(transformed.getClass());
            Jogformer jogformer = new Jogformer();
            jogformer.portableTransformMap = transformMap;
            jogformer.setNavigatorFactory(getNavigatorFactory());
            jogformer.setTransformerFactory(getTransformerFactory());
            navigator.navigate(instance, jogformer);
        }

        return transformed;
    }
}
