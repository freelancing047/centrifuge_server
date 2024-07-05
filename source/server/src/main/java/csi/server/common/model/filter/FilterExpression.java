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
package csi.server.common.model.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.BundleFunctionParameter;
import csi.server.common.util.ValuePair;
import csi.server.util.sql.api.BundleFunction;
import csi.server.util.sql.api.HasBundleFunction;
import csi.shared.core.util.TypedClone;

/**
 * The definition of a single predicate in the filter.
 *
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FilterExpression extends ModelObject implements HasBundleFunction, TypedClone<FilterExpression> {
   public static final String NULL_INDICATOR = "<<null>>";

   private int ordinal;
   private int expressionId;

   @ManyToOne(cascade = CascadeType.MERGE)
   private FieldDef fieldDef;

   @Enumerated(EnumType.STRING)
   private BundleFunction bundleFunction = BundleFunction.NONE;

   @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   private List<BundleFunctionParameter> bundleFunctionParameters = new ArrayList<BundleFunctionParameter>();

   private boolean negated;

   @Enumerated(EnumType.STRING)
   private RelationalOperator operator;

   @Type(type = "csi.server.dao.jpa.xml.SerializedXMLType")
   @Column(columnDefinition = "TEXT")
   private ValueDefinition valueDefinition;

   private boolean isSelectionFilter = false;

   @Transient
   private List<FieldDef> crossColumns = Collections.emptyList();

   public FilterExpression() {
      super();
   }

   public int getOrdinal() {
      return ordinal;
   }

   public int getExpressionId() {
      return expressionId;
   }

   public FieldDef getFieldDef() {
      return fieldDef;
   }

   public BundleFunction getBundleFunction() {
      return bundleFunction;
   }

   public List<BundleFunctionParameter> getBundleFunctionParameters() {
      return bundleFunctionParameters;
   }

   public boolean isNegated() {
      return negated;
   }

   public RelationalOperator getOperator() {
      return operator;
   }

   public ValueDefinition getValueDefinition() {
      return valueDefinition;
   }

   public boolean isSelectionFilter() {
      return isSelectionFilter;
   }

   public List<FieldDef> getCrossColumns() {
      return crossColumns;
   }

   public void setOrdinal(int ordinal) {
      this.ordinal = ordinal;
   }

   public void setExpressionId(int expressionId) {
      this.expressionId = expressionId;
   }

   public void setFieldDef(FieldDef fieldDef) {
      this.fieldDef = fieldDef;
   }

   public void setBundleFunction(BundleFunction bundleFunction) {
      this.bundleFunction = bundleFunction;
   }

   public void setBundleFunctionParameters(List<BundleFunctionParameter> bundleFunctionParameters) {
      this.bundleFunctionParameters = bundleFunctionParameters;
   }

   public void setNegated(boolean negated) {
      this.negated = negated;
   }

   public void setOperator(RelationalOperator operator) {
      this.operator = operator;
   }

   public void setValueDefinition(ValueDefinition valueDefinition) {
      this.valueDefinition = valueDefinition;
   }

   public void setSelectionFilter(boolean isSelctionFilter) {
      this.isSelectionFilter = isSelctionFilter;
   }

   public void setCrossColumns(final Collection<FieldDef> fieldDefs) {
      if ((fieldDefs == null) || fieldDefs.isEmpty()) {
         crossColumns = Collections.emptyList();
      } else {
         crossColumns = new ArrayList<FieldDef>();

         for (FieldDef eachFieldDef : fieldDefs) {
            CsiDataType eachFielDefDataType = eachFieldDef.getDataType();

            for (FieldDef otherFieldDef : fieldDefs) {
               if (!eachFieldDef.equals(otherFieldDef) &&
                   eachFielDefDataType.equals(otherFieldDef.getDataType())) {
                  crossColumns.add(otherFieldDef);
               }
            }
         }
      }
   }

   public static FilterExpression getIsNull(FieldDef fieldIn) {
      FilterExpression expression = new FilterExpression();

      expression.setFieldDef(fieldIn);
      expression.setValueDefinition(new NullValueDefinition());
      expression.setOperator(RelationalOperator.IS_NULL);
      expression.setNegated(false);
      return expression;
   }

   public static FilterExpression getIsNotNull(FieldDef fieldIn) {
      FilterExpression expression = getIsNull(fieldIn);

      expression.setNegated(true);
      return expression;
   }

   public String getExpressionIdLabel() {
      return Integer.toString(expressionId);
   }

   public ValuePair<Boolean,FieldDef> fieldDefValue() {
      return new ValuePair<Boolean,FieldDef>(isSelectionFilter, fieldDef);
   }

   public ValuePair<Boolean,BundleFunction> bundleFunctionValue() {
      return new ValuePair<Boolean,BundleFunction>(isSelectionFilter, bundleFunction);
   }

   public List<String> getStringParamters() {
      List<String> params = new ArrayList<String>();

      for (BundleFunctionParameter bundleFunctionParamter : bundleFunctionParameters) {
         params.add(bundleFunctionParamter.getFunctionParameter());
      }
      return params;
   }

   @Override
   public CsiDataType getDataTypeForBundleFunction() {
      return fieldDef.getValueType();
   }

   public String getNegatedDescription() {
      return isSelectionFilter ? "" : negated ? "NOT" : "";
   }

   public String getOperatorDescription() {
      return operator.getLabel();
   }

   public String getValueDefinitionDescription() {
      return (!isSelectionFilter && (valueDefinition != null)) ? valueDefinition.getShortValueDescription() : "";
   }

   public String getDescription() {
      String result = "";

      if (isSelectionFilter) {
         if (operator == RelationalOperator.INCLUDED) {
            result = "Include Selection";
         } else if (operator == RelationalOperator.EXCLUDED) {
            result = "Exclude Selection";
         }
      } else {
         result = new StringBuilder(bundleFunction.getLabel(fieldDef, getStringParamters()))
                            .append(negated ? " NOT " : " ")
                            .append(getOperatorDescription())
                            .append(" ")
                            .append(valueDefinition.getValueDescription())
                            .toString();
      }
      return result;
   }

   @Override
   public FilterExpression getClone() {
      FilterExpression clone = new FilterExpression();

      clone.setBundleFunction(bundleFunction);

      for (BundleFunctionParameter paramValue : this.bundleFunctionParameters) {
         clone.bundleFunctionParameters.add(paramValue);
      }
      clone.setExpressionId(expressionId);
      clone.setFieldDef(fieldDef);
      clone.setNegated(negated);
      clone.setOperator(operator);

      if (valueDefinition != null) {
         clone.setValueDefinition(valueDefinition.cloneThis());
      }
      clone.setSelectionFilter(isSelectionFilter);
      return clone;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T extends ModelObject> FilterExpression clone(Map<String,T> fieldMapIn) {
      FilterExpression clone = new FilterExpression();

      super.cloneComponents(clone);
      clone.setExpressionId(expressionId);
      clone.setFieldDef((FieldDef) cloneFromOrToMap(fieldMapIn, (T) fieldDef, fieldMapIn));
      clone.setBundleFunction(bundleFunction);
      clone.setBundleFunctionParameters(cloneBundleFunctionParameters());
      clone.setNegated(negated);
      clone.setOperator(operator);
      clone.setValueDefinition(valueDefinition);
      clone.setSelectionFilter(isSelectionFilter);
      return clone;
   }

   public <T extends ModelObject> FilterExpression copy() {
      FilterExpression copy = new FilterExpression();

      super.copyComponents(copy);
      copy.setExpressionId(expressionId);
      copy.setFieldDef(fieldDef);
      copy.setBundleFunction(bundleFunction);
      copy.setBundleFunctionParameters(copyBundleFunctionParameters());
      copy.setNegated(negated);
      copy.setOperator(operator);
      copy.setValueDefinition(valueDefinition);
      copy.setSelectionFilter(isSelectionFilter);
      return copy;
   }

   private List<BundleFunctionParameter> cloneBundleFunctionParameters() {
      List<BundleFunctionParameter> result = null;

      if (bundleFunctionParameters != null) {
         result = new ArrayList<BundleFunctionParameter>();

         for (BundleFunctionParameter item : bundleFunctionParameters) {
            result.add(item.clone());
         }
      }
      return result;
   }

   private List<BundleFunctionParameter> copyBundleFunctionParameters() {
      List<BundleFunctionParameter> result = null;

      if (bundleFunctionParameters != null) {
         result = new ArrayList<BundleFunctionParameter>();

         for (BundleFunctionParameter item : bundleFunctionParameters) {
            result.add(item.copy());
         }
      }
      return result;
   }
}
