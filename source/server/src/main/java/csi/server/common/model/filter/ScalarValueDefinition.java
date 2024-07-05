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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.OperandCardinality;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("ScalarValueDefinition")
public class ScalarValueDefinition<T> implements ValueDefinition {
   @XStreamAsAttribute
   private OperandTypeAndValue<T> value;
   @XStreamAsAttribute
   private CsiDataType dataType;

   public OperandTypeAndValue<T> getValue() {
      return value;
   }
   public CsiDataType getDataType() {
      return dataType;
   }

   public void setValue(OperandTypeAndValue<T> value) {
      this.value = value;
   }
   public void setDataType(CsiDataType dataType) {
      this.dataType = dataType;
   }

   @Override
   public OperandCardinality getCardinality() {
      return OperandCardinality.SCALAR;
   }

   @Override
   public String getShortValueDescription() {
      return (dataType == CsiDataType.String)
                ? new StringBuilder("'").append(value.getValue()).append("'").toString()
                : value.getValue().toString();
   }

   @Override
   public String getValueDescription() {
      return getShortValueDescription();
   }

   @Override
   public ValueDefinition cloneThis() {
      ScalarValueDefinition<T> vd = new ScalarValueDefinition<T>();
      OperandTypeAndValue<T> cloneValueDefinitionValue = new OperandTypeAndValue<T>();

      cloneValueDefinitionValue.setType(value.getType());
      cloneValueDefinitionValue.setValue(value.getValue());
      vd.setDataType(dataType);
      vd.setValue(cloneValueDefinitionValue);
      return vd;
   }
}
