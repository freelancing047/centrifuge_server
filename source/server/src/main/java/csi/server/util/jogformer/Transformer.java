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


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface Transformer {

    /**
     * @param clz
     * @return true if this transformer knows how to transform the given class, false otherwise.
     */
    public boolean isTransformerFor(Class<?> clz);
    
    
    /**
     * Transform the instance.
     * @param instance
     * @return transformed instance.
     */
    public <T> T transform(T instance);
}
