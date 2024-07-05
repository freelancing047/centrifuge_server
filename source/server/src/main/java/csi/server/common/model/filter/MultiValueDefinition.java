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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.OperandCardinality;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("MultiValueDefinition")
public class MultiValueDefinition<T> implements ValueDefinition {
   @XStreamAsAttribute
   private CsiDataType dataType;

   private List<OperandTypeAndValue<T>> values = Collections.emptyList();

   public CsiDataType getDataType() {
      return dataType;
   }

   public List<OperandTypeAndValue<T>> getValues() {
      return values;
   }

   public void setDataType(CsiDataType dataType) {
      this.dataType = dataType;
   }

   public void setValues(List<OperandTypeAndValue<T>> values) {
      if ((values == null) || values.isEmpty()) {
         this.values = Collections.emptyList();
      } else {
         if (this.values.isEmpty()) {
            this.values = new ArrayList<OperandTypeAndValue<T>>();
         }
         this.values.addAll(values);
      }
   }

   @Override
   public OperandCardinality getCardinality() {
      return OperandCardinality.VECTOR;
   }

   @Override
   public String getShortValueDescription() {
      return "<multiple values>";
   }

   private static String singleQuote(String str) {
      return new StringBuilder("'").append(str).append("'").toString();
   }

   @Override
   public String getValueDescription() {
      return (CsiDataType.String == dataType)
                ? values.stream().map(i -> singleQuote(i.getValue().toString())).collect(Collectors.joining(", ", "(", ")"))
                : values.stream().map(i -> i.getValue().toString()).collect(Collectors.joining(", ", "(", ")"));
   }

   @Override
   public ValueDefinition cloneThis() {
      MultiValueDefinition<T> vd = new MultiValueDefinition<T>();

      vd.setDataType(dataType);
      vd.setValues(values);
      return vd;
   }
}
