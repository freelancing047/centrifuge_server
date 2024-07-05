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

import csi.server.common.enumerations.RelationalOperator;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface PredicateLHS {

    /**
     * @param operator operator to apply.
     * @return
     */
    public PredicateFragment $(RelationalOperator operator);

    /**
     * @return Predicate representing (x is null)
     */
    public Predicate isNull();
}
