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

import java.util.Collections;
import java.util.List;

import csi.server.util.sql.Column;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TautologicalPredicate extends AbstractPredicate {
   @Override
   public String getAliasedSQL() {
      return getSQL();
   }

   @Override
   public String getSQL() {
      return " 1 = 1 ";
   }

   @Override
   public List<Column> getColumns() {
      return Collections.emptyList();
   }
}
