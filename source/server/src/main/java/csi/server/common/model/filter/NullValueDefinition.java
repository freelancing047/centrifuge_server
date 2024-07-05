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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("NullValueDefinition")
public class NullValueDefinition implements ValueDefinition {
   @Override
   public OperandCardinality getCardinality() {
      return OperandCardinality.NONE;
   }

   @Override
   public String getShortValueDescription() {
      return "";
   }

   @Override
   public String getValueDescription() {
      return "";
   }

   @Override
   public ValueDefinition cloneThis() {
      return this;
   }
}
