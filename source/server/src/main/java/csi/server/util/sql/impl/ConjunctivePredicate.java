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
package csi.server.util.sql.impl;

import csi.server.util.sql.Predicate;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ConjunctivePredicate extends AbstractCompoundPredicate {

    public ConjunctivePredicate(Predicate a, Predicate b) {
        super(a, b);
    }

    @Override
    public String getSQL() {
        return "(" + getA().getSQL() + " AND " + getB().getSQL() + ")";
    }

    @Override
    public String getAliasedSQL() {
        return "(" + getA().getAliasedSQL() + " AND " + getB().getAliasedSQL() + ")";
    }
}
