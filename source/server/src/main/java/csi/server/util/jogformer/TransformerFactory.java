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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TransformerFactory {

    // We are using a concurrent-map as multiple http requests could hit this at the same time.
    private ConcurrentMap<Class<?>, List<Transformer>> prioritizedTransformersByClass = new ConcurrentHashMap<Class<?>, List<Transformer>>();

    /**
     * This is a prioritized (highest-first) list of transformers that can act on a given object.
     */
    private List<Transformer> transformers;

    public List<Transformer> getTransformers() {
        return transformers;
    }

    public void setTransformers(List<Transformer> transformers) {
        this.transformers = transformers;
    }

    public List<Transformer> getTransformersFor(Class<?> clz) {
        List<Transformer> candidateTransformers = prioritizedTransformersByClass.get(clz);
        if (candidateTransformers == null) {
            candidateTransformers = new ArrayList<Transformer>();
            for (Transformer transformer : getTransformers()) {
                if (transformer.isTransformerFor(clz)) {
                    candidateTransformers.add(transformer);
                }
            }
            prioritizedTransformersByClass.put(clz, candidateTransformers);
        }

        return candidateTransformers;
    }
}
