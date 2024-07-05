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
package csi.server.common.enumerations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum RelationalOperator implements HasLabel {
   LT("<", "<", OperandCardinality.SCALAR, Integer.valueOf(7)),
   LE("<=", "<=", OperandCardinality.SCALAR, Integer.valueOf(8)),
   GT(">", ">", OperandCardinality.SCALAR, Integer.valueOf(10)),
   GE(">=", ">=", OperandCardinality.SCALAR, Integer.valueOf(9)),
   EQUAL("=", "=", OperandCardinality.SCALAR, Integer.valueOf(5)),
   NOT_EQUAL("!=", "!=", OperandCardinality.SCALAR, Integer.valueOf(6)),
   IN("is in list", "IN", OperandCardinality.VECTOR, Integer.valueOf(1)),
   IS_NULL("is null", "IS NULL", OperandCardinality.NONE, Integer.valueOf(0)),
   LIKE("wildcard match", "LIKE", OperandCardinality.SCALAR, Integer.valueOf(4), CsiDataType.String),
   MATCHES("exact match", "~", OperandCardinality.SCALAR, Integer.valueOf(2), CsiDataType.String),
   MATCHES_CASELESS("caseless match", "~*", OperandCardinality.SCALAR, Integer.valueOf(3), CsiDataType.String),
   INCLUDED("Included", "IN", OperandCardinality.VECTOR, null),
   EXCLUDED("Excluded", "NOT IN", OperandCardinality.VECTOR, null),
   SOUNDEX("Sounds like", "DIFFERENCE", OperandCardinality.SCALAR, Integer.valueOf(11), CsiDataType.String),
   LEVENSHTEIN("Fuzzy spelling", "LEVENSHTEIN", OperandCardinality.SCALAR, Integer.valueOf(12), CsiDataType.String);

   private String label;
   private String sql;
   private OperandCardinality cardinality;
   private Integer displayOrder;
   private List<CsiDataType> applicableTypes = Collections.emptyList();

   private static TreeMap<Integer,RelationalOperator> displayMap = null;
   private static List<RelationalOperator> textDisplayList = null;
   private static List<RelationalOperator> nonTextDisplayList = null;
   private static String[] i18nLabels = null;

   private RelationalOperator(String label, String sql, OperandCardinality cardinality,
                              Integer displayOrderIn, CsiDataType... types) {
      this.label = label;
      this.sql = sql;
      this.cardinality = cardinality;
      this.displayOrder = displayOrderIn;

      if ((types != null) && (types.length > 0)) {
         applicableTypes = new ArrayList<CsiDataType>();

         for (CsiDataType csiDataType : types) {
            applicableTypes.add(csiDataType);
         }
      } else {
         applicableTypes = new ArrayList<CsiDataType>(Arrays.asList(CsiDataType.values()));
      }
   }

   @Override
   public String getLabel() {
      return (i18nLabels == null) ? label : i18nLabels[ordinal()];
   }

   public String getSQL() {
      return sql;
   }

   public OperandCardinality getCardinality() {
      return cardinality;
   }

   public Integer getDisplayOrder() {
      return displayOrder;
   }

   public boolean isApplicable(CsiDataType type) {
      return applicableTypes.contains(type);
   }

   public boolean isTextOnly() {
      return ((applicableTypes.size() == 1) && (applicableTypes.get(0) == CsiDataType.String));
   }

   public static final TreeMap<Integer,RelationalOperator> getDisplayMap() {
      if (displayMap == null) {
         displayMap = new TreeMap<Integer,RelationalOperator>();

         for (RelationalOperator operator : RelationalOperator.values()) {
            Integer displayOrder = operator.getDisplayOrder();

            if (displayOrder != null) {
               displayMap.put(displayOrder, operator);
            }
         }
      }
      return displayMap;
   }

   public static final List<RelationalOperator> getTextDisplayList() {
      if (textDisplayList == null) {
         textDisplayList = new ArrayList<RelationalOperator>();

         for (RelationalOperator operator : getDisplayMap().values()) {
            textDisplayList.add(operator);
         }
      }
      return textDisplayList;
   }

   public static final List<RelationalOperator> getFullOperatorList() {
      return getTextDisplayList();
   }

   public static final List<RelationalOperator> getNonTextDisplayList() {
      if (nonTextDisplayList == null) {
         nonTextDisplayList = new ArrayList<RelationalOperator>();

         for (RelationalOperator operator : getFullOperatorList()) {
            if (!operator.isTextOnly()) {
               nonTextDisplayList.add(operator);
            }
         }
      }
      return nonTextDisplayList;
   }

   public static void setI18nLabels(String[] i18nLabelsIn) {
      i18nLabels = i18nLabelsIn;
   }
}
