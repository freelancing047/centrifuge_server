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
package csi.server.util.jogformer;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ArrayTransformer implements Navigator {

    @Override
    public boolean isNavigatorFor(Class<?> clz) {
        return clz.isArray();
    }

    @Override
    public void navigate(Object instance, Jogformer jogformer) {
        Class<?> clazz = instance.getClass();
        if(clazz.getComponentType().isPrimitive()){
            return;//we don't transform primitives
        }
            
        Object[] array = (Object[])instance;

        for (int i = 0; i < array.length; i++) {
            array[i] = jogformer.transform(array[i]);
        }
    }

}
