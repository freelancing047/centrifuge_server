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

import java.util.List;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.impl.spi.ColumnSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PredicateFragmentImpl extends AbstractPredicateFragment {
   public PredicateFragmentImpl(ColumnSpi column, RelationalOperator operator) {
      super(column, operator);
   }

   private Predicate nullValueCheck(Predicate candiatePredicate, Object value) {
      Predicate result = null;

      if ((value == null) || value.toString().equalsIgnoreCase("null")) {
         setOperator(RelationalOperator.IS_NULL);

         result = new NullPredicate(this);
      } else {
         result = candiatePredicate;
      }
      return result;
   }

   @Override
   public Predicate value(Object value, CsiDataType dataType) {
      return nullValueCheck(new PrimitivePredicate(this, value, dataType), value);
   }

   @Override
   public <T> Predicate value(T value) {
      return nullValueCheck(new PrimitivePredicate(this, value), value);
   }

   @Override
   /**
    * Used when we want to handle nulls outside of this
    */
   public Predicate value(Object value, CsiDataType dataType, boolean nullCheck) {
      return (nullCheck) ? value(value, dataType) : new PrimitivePredicate(this, value, dataType);
   }

   @Override
   public Predicate column(Column column) {
      return new ColumnReferencingPredicate(this, column);
   }

   @Override
   public Predicate subselect(SelectSQL selectSql) {
      return new SubSelectPredicate(this, selectSql);
   }

   @Override
   public Predicate subSelect(String arbitrarySQL) {
      return new SubSelectPredicate(this, arbitrarySQL);
   }

   @Override
   public Predicate list(List<? extends Object> values, CsiDataType dataType) {
      return new MultiValuedPredicate(this, values, dataType);
   }
}
