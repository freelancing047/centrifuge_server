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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@XStreamAlias("OperandCardinality")
public enum OperandCardinality {
   NONE,
   SCALAR,
   VECTOR,
   RANGE;
}
