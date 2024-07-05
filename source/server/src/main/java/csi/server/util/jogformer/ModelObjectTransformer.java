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

import csi.server.common.dto.CsiMap;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ModelObjectTransformer implements Transformer {
   @Override
   public boolean isTransformerFor(Class<?> clz) {
      return ModelObject.class.isAssignableFrom(clz);
   }

   @Override
   public <T> T transform(T instance) {
      ModelObject object = (ModelObject) instance;
      CsiMap<String,String> map = object.getClientProperties();

      for (String key : map.keySet()) {
         if (!(map.get(key) instanceof String)) {  //TODO: always false
            Object o = map.get(key);
            map.put(key, o.toString());
         }
      }
      return instance;
   }
}
