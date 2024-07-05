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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.ColumnSpi;
import csi.server.util.sql.impl.spi.TableSourceSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractCompositeColumn extends AbstractColumn {
   private List<ColumnSpi> columns = new ArrayList<ColumnSpi>();

   public AbstractCompositeColumn(TableSourceSpi tableSource, Column... columns) {
      super(tableSource);
      checkNotNull(columns);

      for (Column column : columns) {
         this.columns.add((ColumnSpi) column);
      }
   }

   @Override
   public String getSQL() {
      return columns.stream().map(column -> column.getSQL()).collect(Collectors.joining(","));
   }

   @Override
   public String getSQLWithoutTableAlias() {
      return columns.stream().map(column -> column.getSQLWithoutTableAlias()).collect(Collectors.joining(","));
   }
}
