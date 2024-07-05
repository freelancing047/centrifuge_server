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
import java.util.Arrays;
import java.util.List;

import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.PredicateFragmentSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractSimplePredicate extends AbstractPredicate {
   private PredicateFragmentSpi predicateFragment;

   public AbstractSimplePredicate(PredicateFragmentSpi predicateFragment) {
      super();
      this.predicateFragment = predicateFragment;
   }

   public PredicateFragmentSpi getPredicateFragment() {
      return predicateFragment;
   }

   @Override
   public List<Column> getColumns() {
      return new ArrayList<Column>(Arrays.asList(predicateFragment.getColumn()));
   }
}
