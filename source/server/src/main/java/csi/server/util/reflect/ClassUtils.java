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
package csi.server.util.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.gentyref.GenericTypeReflector;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ClassUtils {
   private static final Logger LOG = LogManager.getLogger(ClassUtils.class);

   /**
    * Iterates through the complete hierarchy of interfaces to find all interfaces
    * that this class implements that derive from interfaceType.
    *
    * @param clz
    * @param interfaceType
    * @return
    */
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public static <T> Set<Class<? extends T>> getAllInterfacesOfType(Class<?> clz, Class<T> interfaceType) {
      ArrayDeque<Class> stack = new ArrayDeque<Class>();

      for (Class clz1 : clz.getInterfaces()) {
         if (interfaceType.isAssignableFrom(clz1)) {
            stack.push(clz1);
         }
      }
      Set<Class<? extends T>> paritcularInterfaces = new HashSet<Class<? extends T>>();

      while (!stack.isEmpty()) {
         Class<?> aInterface = stack.pop();

         if (interfaceType.isAssignableFrom(aInterface) && !interfaceType.equals(aInterface)) {
            paritcularInterfaces.add((Class<? extends T>) aInterface);

            for (Class class1 : aInterface.getInterfaces()) {
               stack.push(class1);
            }
         }
      }
      return paritcularInterfaces;
   }

   /**
    * Get a typed method signature (typed in cases where a parameterized interface
    * is extended by another interface with a specific type provided for the
    * parameter) with actual type parameters substituted in the method signature.
    *
    * @param beanInterface
    * @param beanImplementation
    * @param method
    * @return
    */
   public static String getTypedMethodSignature(Class<?> beanInterface, Object beanImplementation, Method method) {
      StringBuilder builder = new StringBuilder(beanInterface.getName()).append(".").append(method.getName());
      StringBuilder parameters = new StringBuilder();
      Type[] actualParameters = GenericTypeReflector.getExactParameterTypes(method, beanImplementation.getClass());

      for (int i = 0; i < method.getParameterTypes().length; i++) {
         Class<?> clz = method.getParameterTypes()[i];
         String parameterName = null;

         if (method.getGenericParameterTypes()[i] instanceof TypeVariable<?>) {
            parameterName = ((Class<?>) actualParameters[i]).getName();
         } else {
            // For whaterver reason, commons-lang does not expose getCanonicalName!
            String packageName = org.apache.commons.lang.ClassUtils.getPackageCanonicalName(clz);
            parameterName = org.apache.commons.lang.ClassUtils.getShortCanonicalName(clz);

            if (StringUtils.isNotBlank(packageName)) {
               parameterName = packageName + "." + parameterName;
            }
         }
         parameters.append(",").append(parameterName);
      }
      String parameterList = parameters.length() > 0 ? parameters.substring(1) : "";
      builder.append("(").append(parameterList).append(")");
      LOG.trace("typed method signature: " + builder);
      return builder.toString();
   }
}
