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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class NavigatorFactory {

    private ConcurrentMap<Class<?>, Navigator> navigatorsByClass = new ConcurrentHashMap<Class<?>, Navigator>();
    
    
    private List<Navigator> navigators;

    public List<Navigator> getNavigators() {
        return navigators;
    }

    public void setNavigators(List<Navigator> navigators) {
        this.navigators = navigators;
    }

    public Navigator getNavigatorFor(Class<?> clz) {
        Navigator navigator = navigatorsByClass.get(clz);
        if (navigator == null) {
            for (Navigator aNavigator : getNavigators()) {
                if (aNavigator.isNavigatorFor(clz)) {
                    navigatorsByClass.put(clz, aNavigator);
                    navigator = aNavigator;
                    break;
                }
            }
        }
        return navigator;
    }
}
