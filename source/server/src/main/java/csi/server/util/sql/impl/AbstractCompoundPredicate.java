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
package csi.server.util.sql.impl;

import java.util.ArrayList;
import java.util.List;

import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.impl.spi.PredicateSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractCompoundPredicate extends AbstractPredicate {
   private PredicateSpi a;
   private PredicateSpi b;

   public AbstractCompoundPredicate(Predicate a, Predicate b) {
      super();
      this.a = (PredicateSpi) a;
      this.b = (PredicateSpi) b;
   }

   public PredicateSpi getA() {
      return a;
   }

   public PredicateSpi getB() {
      return b;
   }

   public void setA(PredicateSpi a) {
      this.a = a;
   }

   public void setB(PredicateSpi b) {
      this.b = b;
   }

   @Override
   public List<Column> getColumns() {
      List<Column> list = new ArrayList<Column>();

      list.addAll(a.getColumns());
      list.addAll(b.getColumns());
      return list;
   }
}
