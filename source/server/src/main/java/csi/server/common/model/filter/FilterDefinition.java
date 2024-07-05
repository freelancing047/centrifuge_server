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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FilterDefinition extends ModelObject implements Serializable {
   @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   private List<FilterExpression> filterExpressions = new ArrayList<FilterExpression>();

   private String expressionComposition;

   public FilterDefinition() {
      super();
   }

   public List<FilterExpression> getFilterExpressions() {
      return filterExpressions;
   }

   public String getExpressionComposition() {
      return expressionComposition;
   }

   public void setFilterExpressions(List<FilterExpression> filterExpressions) {
      this.filterExpressions = filterExpressions;

      if (filterExpressions != null) {
         Collection<FieldDef> fieldDefs = new ArrayList<FieldDef>();

         for (FilterExpression filterExpression : filterExpressions) {
            fieldDefs.add(filterExpression.getFieldDef());
         }
         for (FilterExpression filterExpression : filterExpressions) {
            filterExpression.setCrossColumns(fieldDefs);
         }
      }
   }

   public void setExpressionComposition(String expressionComposition) {
      this.expressionComposition = expressionComposition;
   }

   @Override
   public <T extends ModelObject> FilterDefinition clone(Map<String,T> fieldMapIn) {
      FilterDefinition clone = new FilterDefinition();

      super.cloneComponents(clone);
      clone.setExpressionComposition(expressionComposition);
      clone.setFilterExpressions(cloneFilterExpressions(fieldMapIn));
      return clone;
   }

   public <T extends ModelObject> FilterDefinition copy() {
      FilterDefinition copy = new FilterDefinition();

      super.copyComponents(copy);
      copy.setExpressionComposition(expressionComposition);
      copy.setFilterExpressions(copyFilterExpressions());
      return copy;
   }

   private <T extends ModelObject> List<FilterExpression> cloneFilterExpressions(Map<String,T> fieldMapIn) {
      List<FilterExpression> result = null;

      if (filterExpressions != null) {
         result = new ArrayList<FilterExpression>();

         for (FilterExpression item : filterExpressions) {
            result.add(item.clone(fieldMapIn));
         }
      }
      return result;
   }

   private <T extends ModelObject> List<FilterExpression> copyFilterExpressions() {
      List<FilterExpression> result = null;

      if (filterExpressions != null) {
         result = new ArrayList<FilterExpression>();

         for (FilterExpression item : filterExpressions) {
            result.add(item.copy());
         }
      }
      return result;
   }
}
