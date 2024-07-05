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

import java.util.List;

import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.PredicateSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class NegatingPredicate extends AbstractPredicate {

    private PredicateSpi predicateToNegate;

    public NegatingPredicate(PredicateSpi predicateToNegate) {
        super();
        this.predicateToNegate = predicateToNegate;
    }

    @Override
    public String getSQL() {
        return "NOT (" + predicateToNegate.getSQL() + ")";
    }

    @Override
    public String getAliasedSQL() {
        return "NOT (" + predicateToNegate.getAliasedSQL() + ")";
    }

    @Override
    public List<Column> getColumns() {
        return predicateToNegate.getColumns();
    }
}
