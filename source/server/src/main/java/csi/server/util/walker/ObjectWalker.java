/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.util.walker;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentSet;

import com.google.common.base.Throwables;

import csi.server.util.jogformer.ObjectIdentityWrapper;
import csi.server.util.reflect.Invoker;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ObjectWalker {

    private Map<ObjectIdentityWrapper, Object> visitationMap = new HashMap<ObjectIdentityWrapper, Object>();
    private ConcurrentMap<Class<?>, List<PropertyDescriptor>> beanPropertiesByClass = new ConcurrentHashMap<Class<?>, List<PropertyDescriptor>>();

   public void walk(Object instance, ObjectVisitor visitor) {
      if (instance != null) {
         ObjectIdentityWrapper instanceWrapper = new ObjectIdentityWrapper();

         instanceWrapper.setReference(instance);

         if (!visitationMap.containsKey(instanceWrapper)) {
            visitor.visit(instance);

            // Mark visited
            visitationMap.put(instanceWrapper, instance);

            // Visit bean properties and collection entries.
            visitBeanRelationships(instance, visitor);
            visitCollectionRelationships(instance, visitor);
         }
      }
   }

    private void visitCollectionRelationships(Object instance, ObjectVisitor visitor) {
        if (instance instanceof List<?>) {
            List<?> list = (List<?>) instance;
            if (!(list instanceof PersistentBag) || ((PersistentBag) list).isDirectlyAccessible()) {
                for (Object object : list) {
                    walk(object, visitor);
                }
            }
        }
        if (instance instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) instance;
            for (Object key : map.keySet()) {
                walk(key, visitor);
            }
            for (Object value : map.values()) {
                walk(value, visitor);
            }
        }
        if (instance instanceof Set<?>) {
            Set<?> set = (Set<?>) instance;
            if (!(set instanceof PersistentSet) || ((PersistentSet) set).isDirectlyAccessible()) {
                for (Object object : set) {
                    walk(object, visitor);
                }
            }
        }
    }

    private void visitBeanRelationships(Object instance, ObjectVisitor visitor) {
        List<PropertyDescriptor> properties = beanPropertiesByClass.get(instance.getClass());
        if (properties == null) {
            properties = getAssociations(instance.getClass());
            beanPropertiesByClass.put(instance.getClass(), properties);
        }
        for (PropertyDescriptor pd : properties) {
            Object returnValue = Invoker.withTarget(instance).invoke(pd.getReadMethod());
            walk(returnValue, visitor);
        }
    }

    private List<PropertyDescriptor> getAssociations(Class<?> clz) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clz);
        } catch (IntrospectionException e) {
            throw Throwables.propagate(e);
        }
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

        List<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();
        for (PropertyDescriptor pd : descriptors) {
            // Only look at read/write properties.
            if ((pd.getReadMethod() != null) && (pd.getWriteMethod() != null)) {
                list.add(pd);
            }
        }

        return list;
    }
}
