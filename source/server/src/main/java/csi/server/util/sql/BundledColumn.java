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
package csi.server.util.sql;

import java.util.List;

import csi.server.util.sql.api.BundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface BundledColumn {

    /**
     * @param function Function to use to bundle.
     * @return Self reference.
     */
    public Column with(BundleFunction function);

    /**
     * @param bundleParams Parameters for the bundling function (function specific)
     * @return Self reference.
     */
    public Column withBundleParams(List<String> bundleParams);
}
