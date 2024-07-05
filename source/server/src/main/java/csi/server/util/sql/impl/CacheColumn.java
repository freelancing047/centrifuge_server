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

import com.google.common.base.Objects;

import csi.server.common.model.FieldDef;
import csi.server.util.CacheUtil;
import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.ColumnSpi;
import csi.server.util.sql.impl.spi.TableSourceSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class CacheColumn extends AbstractColumn {

   private FieldDef fieldDef;

   public FieldDef getFieldDef() {
      return fieldDef;
   }

   public CacheColumn(TableSourceSpi tableSource, FieldDef field) {
      super(tableSource);
      this.fieldDef = field;
   }

   @Override
   public String getSQL() {
      String fieldExpression = CacheUtil.makeAliasedCastExpression(getFieldDef(), getTableSource().getAlias());
      return getAggregatedBundledColumnExpression(fieldExpression);
   }

   @Override
   public String getSQLWithoutTableAlias() {
      String fieldExpression = CacheUtil.makeCastExpression(getFieldDef());
      return getAggregatedBundledColumnExpression(fieldExpression);
   }

   @Override
   public int hashCode() {
      return getAlias().hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj instanceof Column == false) {
         return false;
      } else {
         ColumnSpi typed = (ColumnSpi) obj;
         return Objects.equal(this.getAlias(), typed.getAlias());
      }
   }
}
