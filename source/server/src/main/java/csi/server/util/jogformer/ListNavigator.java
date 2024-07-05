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

import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ListNavigator extends BeanNavigator {

    @Override
    public boolean isNavigatorFor(Class<?> clz) {
        return List.class.isAssignableFrom(clz);
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void navigate(Object instance, Jogformer jogformer) {
        super.navigate(instance, jogformer);
        List list = (List) instance;
        for (int i = 0; i < list.size(); i++) {
            list.set(i, jogformer.transform(list.get(i)));
        }
    }

}
