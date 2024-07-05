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
import java.util.Iterator;
import java.util.List;

import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.PredicateSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractAggregateCompoundPredicate extends AbstractPredicate {
   private List<PredicateSpi> aggregate;

   public AbstractAggregateCompoundPredicate() {
      super();
      aggregate = new ArrayList<PredicateSpi>();
   }

   public void addToAggregate(PredicateSpi a) {
      aggregate.add(a);
   }

   public PredicateSpi get(int index) {
      return aggregate.get(index);
   }

   public boolean isEmpty() {
      return aggregate.isEmpty();
   }

   public Iterator<PredicateSpi> getAggregate() {
      return aggregate.iterator();
   }

   @Override
   public List<Column> getColumns() {
      List<Column> list = new ArrayList<Column>();

      for (PredicateSpi a : aggregate) {
         list.addAll(a.getColumns());
      }
      return list;
   }
}
