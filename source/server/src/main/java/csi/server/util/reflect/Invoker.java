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
package csi.server.util.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Throwables;

/**
 * @author Centrifuge Systems, Inc.
 */
public class Invoker {

    private Object target;
    private Object[] args = new Object[0];

    public static Invoker withTarget(Object instance) {
        Invoker invoker = new Invoker();
        invoker.target = instance;
        return invoker;
    }

    public Invoker usingArgs(Object... args) {
        this.args = args;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(Method method) {
        try {
            return (T)method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            throw Throwables.propagate(e);
        } catch (IllegalAccessException e) {
            throw Throwables.propagate(e);
        } catch (InvocationTargetException e) {
            throw Throwables.propagate(e);
        }
    }
}
