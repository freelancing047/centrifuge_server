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

import csi.server.common.enumerations.CsiDataType;
import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.SelectSQL;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.util.sql.impl.spi.ColumnSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RegexPredicateFragment extends AbstractPredicateFragment {

    public RegexPredicateFragment(ColumnSpi column, RelationalOperator predicateType) {
        super(column, predicateType);
    }

    @Override
    public <T> Predicate value(T value) {
        return new RegexPredicate(this, value.toString());
    }

    @Override
    public Predicate list(List<? extends Object> values, CsiDataType dataType) {
        throw new RuntimeException("Expect regular expression as a string");
    }

    @Override
    public Predicate column(Column column) {
        return new RegexPredicate(this, column);
    }

    @Override
    public Predicate subselect(SelectSQL selectSql) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Predicate subSelect(String arbitrarySQL) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Predicate value(Object value, CsiDataType dataType) {
        return value(value);
    }

    @Override
    public Predicate value(Object value, CsiDataType dataType, boolean nullCheck) {
        // TODO Auto-generated method stub
        return value(value);
    }
}
