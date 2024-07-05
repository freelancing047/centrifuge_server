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
public class ColumnReferencingPredicate extends AbstractSimplePredicate {

    private ColumnSpi column;

    public ColumnReferencingPredicate(PredicateFragmentSpi predicateFragment, Column column) {
        super(predicateFragment);
        this.column = (ColumnSpi) column;
    }

    @Override
    public String getSQL() {
        return getPredicateFragment().getSQLFragment() + column.getSQLWithoutTableAlias();
    }

    @Override
    public String getAliasedSQL() {
        return getPredicateFragment().getAliasedSQLFragment() + column.getSQL();
    }
}
