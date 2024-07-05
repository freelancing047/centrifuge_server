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

import java.util.Iterator;
import java.util.StringJoiner;

import csi.server.util.sql.impl.spi.PredicateSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class AggregateDisjunctivePredicate extends AbstractAggregateCompoundPredicate {
   private boolean conjunctive;

   public AggregateDisjunctivePredicate(boolean conjunctive) {
      super();
      this.conjunctive = conjunctive;
   }

   @Override
   public String getSQL() {
      String sql = "";

      if (isEmpty()) {
         sql = (conjunctive) ? " 1 = 1 " : " 1 != 1 ";
      } else {
         Iterator<PredicateSpi> iterator = getAggregate();
         StringJoiner joiner = new StringJoiner(" OR ", "(", ")");

         while (iterator.hasNext()) {
            joiner.add(iterator.next().getSQL());
         }
         sql = joiner.toString();
      }
      return sql;
   }

   @Override
   public String getAliasedSQL() {
      String sql = "";

      if (isEmpty()) {
         sql = (conjunctive) ? " 1 = 1 " : " 1 != 1 ";
      } else {
         Iterator<PredicateSpi> iterator = getAggregate();
         StringJoiner joiner = new StringJoiner(" OR ", "(", ")");

         while (iterator.hasNext()) {
            joiner.add(iterator.next().getAliasedSQL());
         }
         sql = joiner.toString();
      }
      return sql;
   }
}
