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

import csi.server.common.enumerations.RelationalOperator;
import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.PredicateFragment;
import csi.server.util.sql.api.AggregateFunction;
import csi.server.util.sql.api.BundleFunction;
import csi.server.util.sql.impl.spi.ColumnSpi;
import csi.server.util.sql.impl.spi.TableSourceSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractColumn implements ColumnSpi {
   private TableSourceSpi tableSource;
   private boolean aliasEnabled = true;
   private String alias;
   private BundleFunction bundleFunction;
   private List<String> bundleFunctionParams = new ArrayList<String>();
   private AggregateFunction aggregateFunction;

   public AbstractColumn(TableSourceSpi tableSource) {
      super();
      this.tableSource = tableSource;
   }

   public TableSourceSpi getTableSource() {
      return tableSource;
   }

   public BundleFunction getBundleFunction() {
      return bundleFunction;
   }

   public List<String> getBundleFunctionParams() {
      return bundleFunctionParams;
   }

   public AggregateFunction getAggregateFunction() {
      return aggregateFunction;
   }

   @Override
   public String getAlias() {
      if (alias == null) {
         alias = AliasFactory.getColumnAlias();
      }
      return alias;
   }

   @Override
   public void setAlias(String alias) {
      this.alias = alias;
   }

   @Override
   public String getAliasedSQL() {
      if (aliasEnabled) {
         return getSQL() + AliasFactory.getAliasFragment(getAlias());
      } else {
         return getSQL();
      }
   }

   @Override
   public Column with(BundleFunction bundle) {
      this.bundleFunction = bundle;
      return this;
   }

   @Override
   public Column with(AggregateFunction aggregate) {
      this.aggregateFunction = aggregate;
      return this;
   }

   @Override
   public Column withBundleParams(List<String> bundleParams) {
      this.bundleFunctionParams = bundleParams;
      return this;
   }

   @Override
   public PredicateFragment $(RelationalOperator operator) {
      return new PredicateFragmentImpl(this, operator);
   }

   @Override
   public Predicate isNull() {
      return new NullPredicate(new PredicateFragmentImpl(this, RelationalOperator.IS_NULL));
   }

   protected String getAggregatedBundledColumnExpression(String columnName) {
      String retVal = columnName;
      if (getBundleFunction() != null) {
         retVal = BundleFunctionEvaluator.getFieldExpression(getBundleFunction(), retVal, getBundleFunctionParams());
      }
      if (getAggregateFunction() != null) {
         retVal = getAggregateFunction().getAggregateExpression(retVal);
      }
      return retVal;
   }

   public boolean isAliasEnabled() {
      return aliasEnabled;
   }

   public void setAliasEnabled(boolean aliasEnabled) {
      this.aliasEnabled = aliasEnabled;
   }
}
