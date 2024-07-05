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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Centrifuge Systems, Inc.
 */
public class BeanNavigator implements Navigator {

    private ConcurrentMap<Class<?>, List<BeanAssociation>> beanPropertiesByClass = new ConcurrentHashMap<Class<?>, List<BeanAssociation>>();

    @Override
    public boolean isNavigatorFor(Class<?> clz) {
        return true;
    }

    @Override
    public void navigate(Object instance, Jogformer jogformer) {
        List<BeanAssociation> associations = beanPropertiesByClass.get(instance.getClass());
        if (associations == null) {
            // Find the bean properties
            try {
                associations = getAssociations(instance.getClass());
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }

            beanPropertiesByClass.put(instance.getClass(), associations);
        }

        for (BeanAssociation ba : associations) {
            Object oldValue = ba.get(instance);
            ba.set(instance, oldValue, jogformer.transform(oldValue));
        }
    }

    private List<BeanAssociation> getAssociations(Class<?> clz) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(clz);
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

        List<BeanAssociation> list = new ArrayList<BeanAssociation>();
        for (PropertyDescriptor pd : descriptors) {
            if (pd.getWriteMethod() != null && pd.getReadMethod() != null
                    && isPropertyTypeOfInterest(pd.getPropertyType())) {
                BeanAssociation association = new BeanAssociation();
                association.setPropertyDescriptor(pd);
                list.add(association);
            }
        }

        return list;
    }

    private boolean isPropertyTypeOfInterest(Class<?> propertyType) {
        if (propertyType.isPrimitive()) {
            return false;
        } else if (propertyType.equals(BigDecimal.class) || propertyType.equals(BigInteger.class)) {
            return false;
        } else if (propertyType.equals(Object.class)) {
            return true;
        } else if (propertyType.getPackage() != null && propertyType.getPackage().getName().startsWith("java.lang")) {
            return false;
        } else {
            return true;
        }
    }

}
