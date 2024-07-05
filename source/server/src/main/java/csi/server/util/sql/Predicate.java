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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface Predicate {

    /**
     * @param predicate
     * @return Predicate that represents (this OR predicate).
     */
    public Predicate or(Predicate predicate);

    /**
     * @param predicate
     * @return Predicate that represents (this AND predicate). 
     */
    public Predicate and(Predicate predicate);

    /**
     * @return Predicate that represents NOT (Predicate)
     */
    public Predicate negate();
}
