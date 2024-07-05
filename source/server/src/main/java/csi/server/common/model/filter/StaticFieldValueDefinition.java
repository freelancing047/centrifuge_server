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

import csi.server.common.enumerations.OperandCardinality;
import csi.server.common.model.FieldReference;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("StaticFieldValueDefinition")
public class StaticFieldValueDefinition implements ValueDefinition {
   private FieldReference staticFieldReference;

   public FieldReference getStaticFieldReference() {
      return staticFieldReference;
   }

   public void setStaticFieldReference(FieldReference staticFieldReference) {
      this.staticFieldReference = staticFieldReference;
   }

   @Override
   public OperandCardinality getCardinality() {
      return OperandCardinality.SCALAR;
   }

   @Override
   public String getShortValueDescription() {
      return getStaticFieldReference().getField().getStaticText();
   }

   @Override
   public String getValueDescription() {
      return getShortValueDescription();
   }

   @Override
   public ValueDefinition cloneThis() {
      StaticFieldValueDefinition vd = new StaticFieldValueDefinition();

      vd.setStaticFieldReference(staticFieldReference);  //TODO: clone FieldReference
      return vd;
   }
}
