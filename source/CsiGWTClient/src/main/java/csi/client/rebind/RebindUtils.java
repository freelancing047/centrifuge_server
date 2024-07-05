/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.rebind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RebindUtils {

    /**
     * Gets all interfaces implemented by this type (or extended if this itself is an interface) recursively going
     * up the inheritance hierarchy.
     * @param classType
     * @return
     */
    public static List<JClassType> getAllImplementedInterfaces(JClassType classType) {
        Stack<JClassType> stack = new Stack<JClassType>();
        stack.push(classType);
        List<JClassType> allInterfaces = new ArrayList<JClassType>();

        while (!stack.isEmpty()) {
            JClassType tempType = stack.pop();
            for (JClassType jClassType : tempType.getImplementedInterfaces()) {
                stack.push(jClassType);
                allInterfaces.add(jClassType);
            }
        }

        return allInterfaces;
    }


    /**
     * The built-in getMethods only returns methods of the given type. This method returns all 
     * methods of the given interface and all its parent interfaces.
     * 
     * @param classType
     * @return
     */
    public static Set<JMethod> getAllMethodsOfInterface(JClassType classType) {
        Set<JMethod> methods = new HashSet<JMethod>();

        for (JMethod jMethod : classType.getMethods()) {
            methods.add(jMethod);
        }

        List<JClassType> allInterfaces = getAllImplementedInterfaces(classType);
        for (JClassType jClassType : allInterfaces) {
            for (JMethod method : jClassType.getMethods()) {
                methods.add(method);
            }
        }
        return methods;
    }
}
