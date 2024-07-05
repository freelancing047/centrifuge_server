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

import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.ColumnSpi;
import csi.server.util.sql.impl.spi.PredicateFragmentSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RegexPredicate extends AbstractSimplePredicate {

    private String regex;
    private ColumnSpi column;

    public RegexPredicate(PredicateFragmentSpi predicateFragment, String regex) {
        super(predicateFragment);
        this.regex = regex;
    }

    public RegexPredicate(PredicateFragmentSpi predicateFragment, Column column) {
        super(predicateFragment);
        this.column = (ColumnSpi) column;
    }

    @Override
    public String getSQL() {
        if (column == null) {
            return getPredicateFragment().getSQLFragment() + regex;
        } else {
            return getPredicateFragment().getSQLFragment() + column.getSQLWithoutTableAlias();
        }
    }

    @Override
    public String getAliasedSQL() {
        if (column == null) {
            return getPredicateFragment().getAliasedSQLFragment() + regex;
        } else {
            return getPredicateFragment().getAliasedSQLFragment() + column.getSQL();
        }
    }

}
