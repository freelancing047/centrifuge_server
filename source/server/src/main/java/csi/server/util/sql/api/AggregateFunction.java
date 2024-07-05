/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 *
 **/
package csi.server.util.sql.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.util.HasOrdinal;
import csi.shared.core.util.HasComparator;
import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum AggregateFunction implements HasLabel, HasComparator<AggregateFunction>, HasOrdinal {
   COUNT("Count of", "count", 0, CsiDataType.values()),
   STD_DEV("Standard Deviation of", "stddev_pop", 1, CsiDataType.Integer, CsiDataType.Number),
   VARIANCE("Variance of", "var_pop", 2, CsiDataType.Integer, CsiDataType.Number),
   MINIMUM("Minimum of", "min", 3, CsiDataType.Integer, CsiDataType.Number),
   MAXIMUM("Maximum of", "max", 4, CsiDataType.Integer, CsiDataType.Number),
   SUM("Sum of", "sum", 5, CsiDataType.Integer, CsiDataType.Number),
   AVERAGE("Average of", "avg", 6, CsiDataType.Integer, CsiDataType.Number),
   COUNT_DISTINCT("Count distinct of", "", 7, CsiDataType.values()) {
      @Override
      public String getAggregateExpression(String columnName) {
         return new StringBuilder("count(distinct ").append(columnName).append(")").toString();
      }
   },
   UNITY("Unity of", "", 8, CsiDataType.values()) {
      @Override
      public String getAggregateExpression(String columnName) {
         return new StringBuilder("case when (count(").append(columnName).append(")) <> 0 then 1 else 0 end").toString();
      }
   },
   ARRAY_AGG("Array Aggregate of", "array_agg", 9, CsiDataType.Unsupported) {
      @Override
      public String getAggregateExpression(String columnName) {
         return new StringBuilder("array_agg(distinct ").append(columnName).append(")").toString();
      }
   },
   MEDIAN("Median of", "PERCENTILE_CONT", 10, CsiDataType.Integer, CsiDataType.Number) {
      @Override
      public String getAggregateExpression(String columnName) {
         return new StringBuilder("PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY ").append(columnName).append(")::numeric").toString();
      }
   };

   private String sqlFunction;
   private String label;
   private List<CsiDataType> dataTypes;
   private int ordinal;

   private static String[] i18nLabels = null;

   private AggregateFunction(String label, String sqlFunction, int ordinal, CsiDataType... dataTypes) {
      this.label = label;
      this.sqlFunction = sqlFunction;
      this.ordinal = ordinal;
      this.dataTypes = new ArrayList<CsiDataType>(Arrays.asList(dataTypes));
   }

   @Override
   public String getLabel() {
      return (i18nLabels == null) ? label : i18nLabels[ordinal()];
   }

   @Override
   public int getOrdinal() {
      return ordinal;
   }

   @Override
   public Comparator<AggregateFunction> getComparator() {
      return Comparator.comparing(AggregateFunction::getLabel);
   }

   public boolean isApplicableFor(CsiDataType dataType) {
      return dataTypes.contains(dataType);
   }

   public String getAggregateExpression(String columnName) {
      return new StringBuilder(sqlFunction).append("(").append(columnName).append(")").toString();
   }

   public static List<AggregateFunction> forType(CsiDataType dataType) {
      List<AggregateFunction> list = new ArrayList<AggregateFunction>();

      for (AggregateFunction af : AggregateFunction.values()) {
         if (af.dataTypes.contains(dataType)) {
            list.add(af);
         }
      }
      return list;
   }

   public static void setI18nLabels(String[] i18nLabelsIn) {
      i18nLabels = i18nLabelsIn;
   }
}
