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

import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.ColumnSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SoundexPredicate extends PrimitivePredicate {
   private ColumnSpi column;

   public <T> SoundexPredicate(Column column, T value) {
      super(null, value);
      this.column = (ColumnSpi) column;
   }

   private String formSQL(String sqlFragment, String columnFragment) {
      //difference(columnStr, 'value') > 2
      return new StringBuilder(sqlFragment.trim())
                       .append("(")
                       .append(columnFragment)
                       .append(", ")
                       .append(getValue())
                       .append(") > 2")
                       .toString();
   }

   @Override
   public String getSQL() {
      return formSQL(column.getSQLWithoutTableAlias(), column.getSQLWithoutTableAlias());
   }

   @Override
   public String getAliasedSQL() {
      return formSQL(column.getSQL(), column.getSQL());
   }
}
