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

import csi.server.common.enumerations.OperandCardinality;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface ValueDefinition extends Serializable {
   public OperandCardinality getCardinality();
   public String getShortValueDescription();
   public String getValueDescription();
   public ValueDefinition cloneThis();
}
