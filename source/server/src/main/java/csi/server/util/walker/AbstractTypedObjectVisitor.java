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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractTypedObjectVisitor<T> implements ObjectVisitor {
   private Class<T> classOfInterest;

   public AbstractTypedObjectVisitor(Class<T> classOfInterest) {
      super();
      this.classOfInterest = classOfInterest;
   }

   public Class<T> getClassOfInterest() {
      return classOfInterest;
   }

   public void setClassOfInterest(Class<T> classOfInterest) {
      this.classOfInterest = classOfInterest;
   }

   @SuppressWarnings("unchecked")
   @Override
   public final void visit(Object o) {
      if (getClassOfInterest().isAssignableFrom(o.getClass())) {
         visitType((T) o);
      }
   }

   public abstract void visitType(T instance);
}
