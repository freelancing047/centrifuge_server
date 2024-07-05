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
import csi.server.util.sql.impl.spi.PredicateSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractPredicate implements PredicateSpi {

    @Override
    public Predicate or(Predicate predicate) {
        return new DisjunctivePredicate(this, predicate);
    }

    @Override
    public Predicate and(Predicate predicate) {
        return new ConjunctivePredicate(this, predicate);
    }

    @Override
    public Predicate negate() {
        return new NegatingPredicate(this);
    }

}
