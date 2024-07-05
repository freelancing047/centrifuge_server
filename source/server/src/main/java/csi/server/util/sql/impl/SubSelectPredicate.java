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

import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.impl.spi.PredicateFragmentSpi;
import csi.server.util.sql.impl.spi.SelectSQLSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SubSelectPredicate extends AbstractSimplePredicate {

    private SelectSQLSpi select;
    private String arbitrarySql;

    public SubSelectPredicate(PredicateFragmentSpi predicateFragment, SelectSQL select) {
        super(predicateFragment);
        this.select = (SelectSQLSpi) select;
    }

    public SubSelectPredicate(PredicateFragmentSpi predicateFragment, String sql) {
        super(predicateFragment);
        this.arbitrarySql = sql;
    }

    @Override
    public String getSQL() {
        return getPredicateFragment().getSQLFragment() + getValue();
    }

    @Override
    public String getAliasedSQL() {
        return getPredicateFragment().getAliasedSQLFragment() + getValue();
    }

    private String getValue() {
        return "(" + (arbitrarySql != null ? arbitrarySql : select.getSQL()) + ")";
    }
}
