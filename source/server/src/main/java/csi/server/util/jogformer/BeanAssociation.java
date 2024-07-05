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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class BeanAssociation {

    private static final Object[] EMPTY_PARAMS = new Object[0];

    private PropertyDescriptor propertyDescriptor;

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object instance) {
        try {
            return (T) getPropertyDescriptor().getReadMethod().invoke(instance, EMPTY_PARAMS);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public void set(Object instance, Object oldValue, Object newValue) {
        if (oldValue != newValue) {
            try {
                getPropertyDescriptor().getWriteMethod().invoke(instance, newValue);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
