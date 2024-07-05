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

import csi.server.common.enumerations.RelationalOperator;
import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.ColumnSpi;
import csi.server.util.sql.impl.spi.PredicateFragmentSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractPredicateFragment implements PredicateFragmentSpi {
   private ColumnSpi column;
   private RelationalOperator operator;

   public AbstractPredicateFragment(ColumnSpi column, RelationalOperator operator) {
      super();
      this.column = column;
      this.operator = operator;
   }

   @Override
   public Column getColumn() {
      return column;
   }

   public RelationalOperator getOperator() {
      return operator;
   }

   public void setOperator(RelationalOperator operator) {
      this.operator = operator;
   }

   @Override
   public String getSQLFragment() {
      return column.getSQLWithoutTableAlias() + " " + operator.getSQL() + " ";
   }

   @Override
   public String getAliasedSQLFragment() {
      return column.getSQL() + " " + operator.getSQL() + " ";
   }

   @Override
   public String getRelationalOperator() {
      return operator.getSQL() + " ";
   }
}
