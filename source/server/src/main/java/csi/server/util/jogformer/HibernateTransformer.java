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

import javax.persistence.Entity;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class HibernateTransformer implements Transformer {

    private boolean inSessionScope;

    public void setInSessionScope(boolean inSessionScope) {
        this.inSessionScope = inSessionScope;
    }

    public boolean isInSessionScope() {
        return inSessionScope;
    }

    @Override
    public boolean isTransformerFor(Class<?> clz) {
        while (clz != null) {
            if (clz.isAnnotationPresent(Entity.class)) {
                return true;
            }
            clz = clz.getSuperclass();
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T transform(T instance) {
        if (Hibernate.isInitialized(instance)) {
            return instance;
        } else if (isInSessionScope()) {
            Hibernate.initialize(instance);
            return instance;
        } else {
            return (T)((HibernateProxy) instance).writeReplace();
        }
    }

}
