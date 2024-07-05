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
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldReferencingEntity;
import csi.server.common.model.HasFieldReference;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Filter extends ModelObject implements FieldReferencingEntity {
   private int ordinal;
   private String name;

   @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
   private FilterDefinition filterDefinition;

   public Filter() {
      super();
   }

   public int getOrdinal() {
      return ordinal;
   }

   public String getName() {
      return name;
   }

   @HasFieldReference
   public FilterDefinition getFilterDefinition() {
      return filterDefinition;
   }

   public void setOrdinal(int ordinal) {
      this.ordinal = ordinal;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setFilterDefinition(FilterDefinition filterDefinition) {
      this.filterDefinition = filterDefinition;
   }

   @Override
   public List<FieldDef> getReferencedFields() {
      List<FieldDef> referencedFields = new ArrayList<FieldDef>();
      List<FilterExpression> filterExpressions = filterDefinition.getFilterExpressions();

      for (FilterExpression filterExpression : filterExpressions) {
         FieldDef fieldDef = filterExpression.getFieldDef();

         if (fieldDef != null) {
            referencedFields.add(fieldDef);
         }
      }
      return referencedFields;
   }

   @Override
   public int hashCode() {
      return getUuid().hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return (this == obj) ||
             ((obj != null) &&
              (obj instanceof Filter) &&
              Objects.equal(getUuid(), ((Filter) obj).getUuid()));
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
                        .add("uuid", getUuid())
                        .add("name", getName())
                        .toString();
   }

   public Filter cloneThis() {
      Filter filter = new Filter();
      FilterDefinition filterDef = new FilterDefinition();

      filter.setFilterDefinition(filterDef);
      filter.setUuid(this.getUuid());
      filter.setName(this.getName());
      filterDef.setExpressionComposition(filterDefinition.getExpressionComposition());

      for (FilterExpression fe : filterDefinition.getFilterExpressions()) {
         filterDef.getFilterExpressions().add(fe.getClone());
      }
      return filter;
   }

   @Override
   public <T extends ModelObject> Filter clone(Map<String,T> fieldMapIn) {
      Filter clone = new Filter();

      super.cloneComponents(clone);
      clone.setName(getName());

      if (filterDefinition != null) {
         clone.setFilterDefinition(filterDefinition.clone(fieldMapIn));
      }
      return clone;
   }

   @Override
   protected void debugContents(StringBuilder bufferIn, String indentIn) {
      debugObject(bufferIn, name, indentIn, "name");
      debugObject(bufferIn, filterDefinition, indentIn, "filterDefinition");
   }

   public Filter copy() {
      Filter copy = new Filter();

      super.copyComponents(copy);
      copy.setName(getName());

      if (filterDefinition != null) {
         copy.setFilterDefinition(filterDefinition.copy());
      }
      return copy;
   }
}
