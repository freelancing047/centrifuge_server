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

import csi.server.util.sql.api.AggregateFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface AggregateColumn {

    /**
     * @param function Aggregation function
     * @return Self reference.
     */
    public Column with(AggregateFunction function);
}
