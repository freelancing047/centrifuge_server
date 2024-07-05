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
package csi.server.util.sql.api;

import java.io.Serializable;
import java.util.List;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.visualization.BundleFunctionParameter;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface HasBundleFunction extends Serializable {

    public BundleFunction getBundleFunction();

    public List<BundleFunctionParameter> getBundleFunctionParameters();
    
    public CsiDataType getDataTypeForBundleFunction();
}
