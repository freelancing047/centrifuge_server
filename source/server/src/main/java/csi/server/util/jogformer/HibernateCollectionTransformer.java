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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentCollection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("rawtypes")
public class HibernateCollectionTransformer implements Transformer {

    private boolean inSessionScope;

    public boolean isInSessionScope() {
        return inSessionScope;
    }

    public void setInSessionScope(boolean inSessionScope) {
        this.inSessionScope = inSessionScope;
    }

    @Override
    public boolean isTransformerFor(Class<?> clz) {
        return PersistentCollection.class.isAssignableFrom(clz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T transform(T instance) {
        Class<?> clz = instance.getClass();
        boolean initialized = Hibernate.isInitialized(instance);

        T returnValue = null;
        if (SortedMap.class.isAssignableFrom(clz)) {
            if (initialized || isInSessionScope()) {
                TreeMap map = new TreeMap();
                map.putAll((Map) instance);
                returnValue = (T) map;
            } else {
                returnValue = (T) Maps.newTreeMap();
            }
        } else if (SortedSet.class.isAssignableFrom(clz)) {
            if (initialized || isInSessionScope()) {
                TreeSet set = new TreeSet();
                set.addAll((Set) instance);
                returnValue = (T) set;
            } else {
                returnValue = (T) Sets.newTreeSet();
            }
        } else if (Map.class.isAssignableFrom(clz)) {
            if (initialized || isInSessionScope()) {
                Map map = new HashMap();
                map.putAll((Map) instance);
                returnValue = (T) map;
            } else {
                returnValue = (T) Maps.newHashMap();
            }
        } else if (List.class.isAssignableFrom(clz)) {
            if (initialized || isInSessionScope()) {
                List list = new ArrayList();
                list.addAll((List) instance);
                returnValue = (T) list;
            } else {
                returnValue = (T) Lists.newArrayList();
            }
        } else if (Set.class.isAssignableFrom(clz)) {
            if (initialized || isInSessionScope()) {
                Set set = new HashSet();
                set.addAll((Set) instance);
                returnValue = (T) set;
            } else {
                returnValue = (T) Sets.newHashSet();
            }
        } else {
            throw new RuntimeException("Unknown type passed for transformation: " + clz.getName());
        }
        return returnValue;
    }

}
